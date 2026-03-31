package com.zhanglx.sso.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.zhanglx.sso.auth.domain.dto.PermissionDTO;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.excel.ResolvedNode;
import com.zhanglx.sso.auth.domain.po.PermissionPO;
import com.zhanglx.sso.auth.domain.po.RolePermissionRelationshipMappingPO;
import com.zhanglx.sso.auth.domain.vo.PermissionExcelVO;
import com.zhanglx.sso.auth.domain.vo.PermissionVO;
import com.zhanglx.sso.auth.event.PermissionChangedEvent;
import com.zhanglx.sso.auth.listener.excel.BestEffortImportListener;
import com.zhanglx.sso.auth.mapper.PermissionMapper;
import com.zhanglx.sso.auth.mapper.RolePermissionRelationshipMappingMapper;
import com.zhanglx.sso.auth.service.PermissionService;
import com.zhanglx.sso.auth.service.UserService;
import com.zhanglx.sso.auth.utils.IPermissionMapper;
import com.zhanglx.sso.auth.utils.excel.ExportProgressManager;
import com.zhanglx.sso.auth.utils.excel.ImportProgressManager;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.strategy.TreeFilterStrategy;
import com.zhanglx.sso.core.utils.AssertUtils;
import com.zhanglx.sso.core.utils.collection.CollectionUtils;
import com.zhanglx.sso.core.utils.tree.HighPerfTreeBuilder;
import com.zhanglx.sso.mybatis.query.LambdaQueryWrapperX;
import com.zhanglx.sso.mybatis.utils.SnowFlakeUtils;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.fesod.sheet.ExcelWriter;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.fesod.sheet.write.metadata.WriteSheet;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/17 14:59
 * @ClassName: PermissionServiceImpl
 * @Description: 权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final Validator validator;
    private final UserService userService;
    private final HighPerfTreeBuilder treeBuilder;
    private final PermissionMapper permissionMapper;
    private final ImportProgressManager progressManager;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ExportProgressManager exportProgressManager;
    private final RolePermissionRelationshipMappingMapper rolePermissionRelationshipMappingMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "PermissionTree", allEntries = true)
    public PermissionDTO addPermission(PermissionDTO permissionDTO) {
        // 查询全局是否有重复
        if (permissionMapper.exists(new LambdaQueryWrapperX<PermissionPO>()
                .eq(PermissionPO::getIdentification, permissionDTO.getIdentification()))) {
            throw new BusinessException("exception.business.data.duplicate");
        }

        PermissionPO permissionPO = IPermissionMapper.INSTANCE.toPO(permissionDTO);

        if (Objects.nonNull(permissionPO.getParentId())) {
            PermissionPO parentPermissionPO = permissionMapper.selectById(permissionPO.getParentId());
            if (Objects.isNull(parentPermissionPO)) {
                // 避免错误的引用
                permissionPO.setParentId(null);
            } else {
                // 设置标识血缘
                permissionPO.setIdentityLineage(buildIdentityLineage(parentPermissionPO.getIdentification(),
                        parentPermissionPO.getIdentityLineage()));
            }
        }

        permissionMapper.insert(permissionPO);
        return IPermissionMapper.INSTANCE.toDTO(permissionPO);
    }

    /**
     * 构建最健壮的标识血缘 (Identity Lineage)
     *
     * @param identity      当前权限的标识 (如 mall:goods:add 或 add)
     * @param parentLineage 父级权限的血缘 (如 mall.goods.list)
     * @return 规范化后的纯净树形血缘 (如 mall.goods.list.add)
     */
    private String buildIdentityLineage(String identity, String parentLineage) {
        if (StringUtils.isBlank(identity)) {
            return parentLineage;
        }

        // 1. 规范化分隔符：将前端可能传的冒号(:)、中划线(-)统一替换为标准的点(.)
        String normalizedIdentity = identity.replace(":", ".").replace("-", ".");

        // 2. 顶级节点：如果没有父级血缘，直接返回自身规范化后的标识
        if (StringUtils.isBlank(parentLineage)) {
            return normalizedIdentity;
        }

        // 3. 场景 A（完整包含）：前端传的全量标识已经完美包含了父级血缘前缀
        // 例如：parent="mall.goods", identity="mall.goods.add"
        if (normalizedIdentity.startsWith(parentLineage + ".")) {
            return normalizedIdentity; // 直接使用，无需拼接
        }

        // 4. 场景 B（部分重叠 或 极简短标识）：
        // 例如：parent="system.user.list", identity="system:user:add"
        // 核心算法：树形血缘的本质永远是【父级绝对路径】+【当前节点的独有核心后缀】。
        // 我们直接将当前标识按点切分，提取最后一段作为独有后缀。
        String[] parts = normalizedIdentity.split("\\.");
        String uniqueSuffix = parts[parts.length - 1];

        // 完美拼接：system.user.list + . + add -> system.user.list.add
        return parentLineage + "." + uniqueSuffix;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "PermissionTree", allEntries = true)
    public PermissionDTO delPermission(Long id) {
        AssertUtils.notNull(id, "exception.business.data.invalid");

        PermissionPO permissionPO = permissionMapper.selectById(id);
        AssertUtils.notNull(permissionPO, "exception.business.resource.not.found");

        // 递归删除权限标签项
        recursiveDelFuncPerm(Lists.newArrayList(IPermissionMapper.INSTANCE.toDTO(permissionPO)));

        return IPermissionMapper.INSTANCE.toDTO(permissionPO);
    }

    private List<PermissionDTO> recursiveDelFuncPerm(List<PermissionDTO> permissionDTOS) {
        if (CollectionUtils.isEmpty(permissionDTOS)) {
            return Lists.newArrayList();
        }

        List<PermissionDTO> results = new ArrayList<>(permissionDTOS);
        List<PermissionPO> subFuncPerms = Lists.newArrayList();
        List<Long> idDatas = permissionDTOS.stream().map(PermissionDTO::getId).toList();

        List<PermissionPO> tempData = permissionMapper.selectList(new LambdaQueryWrapperX<PermissionPO>()
                .in(PermissionPO::getParentId, idDatas));

        if (CollectionUtils.isNotEmpty(tempData)) {
            subFuncPerms.addAll(tempData);
        }

        // 删除功能权限项
        permissionMapper.deleteByIdsWithFill(idDatas);

        // 删除功能权限项与角色关联关系
        rolePermissionRelationshipMappingMapper.delete(new LambdaQueryWrapperX<RolePermissionRelationshipMappingPO>()
                .in(RolePermissionRelationshipMappingPO::getPermissionId, idDatas));

        // 递归执行删除
        if (CollectionUtils.isNotEmpty(subFuncPerms)) {
            results.addAll(recursiveDelFuncPerm(IPermissionMapper.INSTANCE.toDTOList(subFuncPerms)));
        }

        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "PermissionTree", allEntries = true)
    public List<PermissionDTO> batchDelPermission(List<Long> idList) {
        AssertUtils.notEmpty(idList, "exception.business.data.invalid");

        List<PermissionDTO> results = Lists.newArrayList();
        AtomicInteger counter = new AtomicInteger(0);
        int groupSize = 50;

        Map<Integer, List<Long>> groupedDatas = idList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(e -> counter.getAndIncrement() / groupSize));

        groupedDatas.forEach((key, groupIds) -> {
            List<PermissionPO> permissionPOS = permissionMapper.selectList(new LambdaQueryWrapperX<PermissionPO>()
                    .in(PermissionPO::getId, groupIds));
            results.addAll(recursiveDelFuncPerm(IPermissionMapper.INSTANCE.toDTOList(permissionPOS)));
        });

        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "PermissionTree", allEntries = true)
    public PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO) {
        PermissionPO permissionPO = permissionMapper.selectById(id);
        AssertUtils.notNull(permissionPO, "exception.business.resource.not.found");

        if (permissionMapper.exists(new LambdaQueryWrapperX<PermissionPO>()
                .eq(PermissionPO::getIdentification, permissionDTO.getIdentification())
                .ne(PermissionPO::getId, id))) {
            throw new BusinessException("exception.business.data.duplicate");
        }

        PermissionPO updateMenu = IPermissionMapper.INSTANCE.toPO(permissionDTO);
        updateMenu.setId(id);
        updateMenu.setCreateTime(permissionPO.getCreateTime());
        updateMenu.setCreateBy(permissionPO.getCreateBy());
        permissionMapper.updateById(updateMenu);

        // 如果 identification 有变化，更新关联的 lineage
        if (!Strings.CS.equals(permissionPO.getIdentification(), updateMenu.getIdentification())) {
            List<PermissionPO> children = permissionMapper.selChildrenPerm(permissionPO.getIdentification());
            List<PermissionDTO> childrenDTOs = IPermissionMapper.INSTANCE.toDTOList(
                    children.stream().sorted(Comparator.comparingInt(PermissionPO::getDisplayNo)).toList()
            );

            List<PermissionDTO> childrenTree = treeBuilder.buildTree(childrenDTOs, TreeFilterStrategy.STRICT_BRANCH_HIDE, false);

            List<PermissionDTO> updatedPermission = recursiveUpdateIdentityLineage(
                    updateMenu.getIdentification(), updateMenu.getIdentityLineage(), childrenTree);

            if (CollectionUtils.isNotEmpty(updatedPermission)) {
                permissionMapper.updateById(IPermissionMapper.INSTANCE.toPOList(updatedPermission));
            }
        }

        return IPermissionMapper.INSTANCE.toDTO(updateMenu);
    }

    private List<PermissionDTO> recursiveUpdateIdentityLineage(String identity, String parentIdentityLineage, List<PermissionDTO> childrenPermission) {
        if (CollectionUtils.isEmpty(childrenPermission)) {
            return Lists.newArrayList();
        }

        List<PermissionDTO> result = Lists.newArrayList();
        childrenPermission.forEach(item -> {
            item.setIdentityLineage(buildIdentityLineage(identity, parentIdentityLineage));
            result.add(item);
            List<PermissionDTO> childrenPermissionResult = recursiveUpdateIdentityLineage(
                    item.getIdentification(), item.getIdentityLineage(), item.getChildren());

            if (CollectionUtils.isNotEmpty(childrenPermissionResult)) {
                result.addAll(childrenPermissionResult);
            }
        });

        return result;
    }

    @Override
    @Cacheable(value = "PermissionTree", key = "T(cn.dev33.satoken.stp.StpUtil).getRoleList().toString()", unless = "#result == null or #result.size() == 0")
    public List<PermissionDTO> selPermission(String searchKey) {
        List<PermissionDTO> permissionDTOS = new ArrayList<>(permissionMapper.selectCount().intValue());

        permissionMapper.streamAllPermissions(resultContext -> {
            PermissionPO po = resultContext.getResultObject();
            PermissionDTO dto = IPermissionMapper.INSTANCE.toDTO(po);
            permissionDTOS.add(dto);
        });

        if (CollectionUtils.isEmpty(permissionDTOS)) {
            return Lists.newArrayList();
        }

        List<PermissionDTO> results;
        if (StringUtils.isNotBlank(searchKey)) {
            results = permissionDTOS.stream()
                    .filter(funcPerm -> Strings.CI.equals(funcPerm.getName(), searchKey)
                            || Strings.CI.equals(funcPerm.getIdentification(), searchKey))
                    .toList();

            if (CollectionUtils.isEmpty(results)) {
                return Lists.newArrayList();
            }

            // 避免查找时，部分子节点的父节点也满足查找条件，需要对结果进行去重
            results = findAncestor(results, permissionDTOS);
            results = new ArrayList<>(results.stream()
                    .collect(Collectors.toMap(PermissionDTO::getId, Function.identity(), (existing, replacement) -> existing))
                    .values());
        } else {
            results = permissionDTOS;
        }

        if (CollectionUtils.isEmpty(results)) {
            return Lists.newArrayList();
        }

        return treeBuilder.buildTree(results, TreeFilterStrategy.STRICT_BRANCH_HIDE, true);
    }

    private List<PermissionDTO> findAncestor(List<PermissionDTO> searchFuncPermLabels, List<PermissionDTO> allFuncPermLabels) {
        List<PermissionDTO> results = Lists.newArrayList();
        List<PermissionDTO> ancestorNodes = Lists.newArrayList();

        searchFuncPermLabels.stream()
                .filter(item -> Objects.nonNull(item.getParentId()))
                .forEach(item -> {
                    Optional<PermissionDTO> findResult = allFuncPermLabels.stream()
                            .filter(tempDept -> tempDept.getId().equals(item.getParentId()))
                            .findFirst();

                    if (findResult.isPresent()) {
                        ancestorNodes.add(findResult.get());
                    } else {
                        results.add(item);
                    }
                });

        if (CollectionUtils.isNotEmpty(ancestorNodes)) {
            results.addAll(findAncestor(ancestorNodes, allFuncPermLabels));
        }

        results.addAll(searchFuncPermLabels);
        return results;
    }

    @Override
    public List<PermissionVO> selPermissionByIdentification(String username, List<String> identifications, List<String> permissionTypes, String tenantId) {
        AssertUtils.notBlank(username, "exception.business.data.invalid");

        UserDTO user = userService.findUserByUsername(username);
        AssertUtils.notNull(user, "exception.business.resource.not.found");

        List<PermissionPO> results = permissionMapper.selectByUserWithIdentityAndType(
                user.getId(),
                Objects.isNull(identifications) ? Lists.newArrayList() : identifications,
                Objects.isNull(permissionTypes) ? Lists.newArrayList() : permissionTypes);

        if (CollectionUtils.isNotEmpty(results)) {
            return IPermissionMapper.INSTANCE.toVOList(results);
        }

        return Lists.newArrayList();
    }

    @Override
    public List<PermissionVO> selPermissionByRoleId(long roleId) {
        AssertUtils.notNull(roleId, "exception.business.data.invalid");

        List<PermissionPO> result = permissionMapper.selPermissionByRoleId(roleId);
        if (CollectionUtils.isNotEmpty(result)) {
            return IPermissionMapper.INSTANCE.toVOList(result);
        }

        return Lists.newArrayList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "PermissionTree", allEntries = true)
    public void delMappingByRoleId(List<Long> roleId) {
        if (CollectionUtils.isNotEmpty(roleId)) {
            rolePermissionRelationshipMappingMapper.delete(
                    new LambdaQueryWrapperX<RolePermissionRelationshipMappingPO>()
                            .in(RolePermissionRelationshipMappingPO::getRoleId, roleId)
            );
        }
    }

    @Async
    @Override
    public void executeImportTask(String taskId, File tempFile) {
        try {
            // 1. 初始化尽力而为监听器，正确传入依赖和 BiConsumer 方法引用
            BestEffortImportListener listener = new BestEffortImportListener(
                    validator,
                    taskId,
                    progressManager,
                    this::saveToDatabase
            );

            // 2. 【核心优化】：Fesod 直接读取磁盘物理文件，不占用 JVM 内存流
            FesodSheet.read(tempFile, PermissionExcelVO.class, listener)
                    .sheet()
                    .doRead();

            // 3. 检查是否有失败的数据，如果有，生成错误 Excel 并模拟上传 OSS
            List<PermissionExcelVO> failedData = listener.getAllFailedData();
            String errorFileUrl = "";
            if (!failedData.isEmpty()) {
                errorFileUrl = generateAndUploadErrorExcel(failedData, taskId);
            }

            // 4. 标记任务完成
            progressManager.completeTask(taskId, errorFileUrl);

            // 5. 数据落地且任务完成后，发布权限变更事件（清理缓存等）
            eventPublisher.publishEvent(new PermissionChangedEvent(this, taskId));

        } catch (Exception e) {
            log.error("解析 Excel 发生致命异常", e);
            progressManager.failTask(taskId, "导入失败: " + e.getMessage());
        } finally {
            // 6. 【核心收尾】：无论成功失败，务必删除上传的临时文件，防止塞满服务器硬盘
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * 高效入库与映射逻辑（DFS 记忆化算法 + 编程式极速事务）
     */
    private void saveToDatabase(List<PermissionExcelVO> validBatch, List<PermissionExcelVO> failedList) {
        if (CollectionUtils.isEmpty(validBatch)) {
            return;
        }

        Map<String, PermissionExcelVO> batchMap = validBatch.stream()
                .collect(Collectors.toMap(PermissionExcelVO::getIdentification, vo -> vo, (v1, v2) -> v1));

        Set<String> allParentIds = validBatch.stream()
                .map(PermissionExcelVO::getParentIdentification)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        Map<String, PermissionPO> dbMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(allParentIds)) {
            List<PermissionPO> existingParents = permissionMapper.selectList(
                    new LambdaQueryWrapperX<PermissionPO>()
                            .select(PermissionPO::getId, PermissionPO::getIdentification, PermissionPO::getIdentityLineage)
                            .in(PermissionPO::getIdentification, allParentIds)
            );
            existingParents.forEach(p -> dbMap.put(p.getIdentification(), p));
        }

        Map<String, ResolvedNode> resolvedCache = new HashMap<>();
        List<PermissionPO> poListToInsert = new ArrayList<>();

        Iterator<PermissionExcelVO> iterator = validBatch.iterator();
        while (iterator.hasNext()) {
            PermissionExcelVO vo = iterator.next();
            try {
                // 纯 CPU 运算：DFS 解析血缘，不需要数据库连接
                ResolvedNode node = resolveNode(vo.getIdentification(), batchMap, dbMap, resolvedCache, new HashSet<>());

                PermissionPO po = IPermissionMapper.INSTANCE.excelVOToPo(vo);
                po.setId(node.getId());
                po.setParentId(node.getParentId());
                po.setIdentityLineage(node.getLineage());
                poListToInsert.add(po);
            } catch (Exception e) {
                vo.setErrorMessage(e.getMessage());
                failedList.add(vo);
                iterator.remove();
            }
        }

        // 【高可用核心】：编程式事务
        // 只有在这里，真正向数据库发送 Insert 指令的一瞬间，才去获取数据库连接开启事务。
        // 一旦抛出异常，立刻回滚并上报。
        if (CollectionUtils.isNotEmpty(poListToInsert)) {
            transactionTemplate.execute(status -> {
                try {
                    // 请确保你的 PermissionMapper 里有批量插入方法，比如 mybatis-plus 的 insertBatch 或自定义的 foreach insert
                    permissionMapper.insert(poListToInsert);
                    return true; // 返回任意值表示执行成功
                } catch (Exception e) {
                    status.setRollbackOnly(); // 标记事务必须回滚
                    throw e; // 继续抛出，交给外层的 listener.invoke 的 try-catch 处理
                }
            });
        }
    }

    /**
     * 将失败的数据利用 Fesod 写回到一个新的本地 Excel 并上传
     */
    private String generateAndUploadErrorExcel(List<PermissionExcelVO> failedData, String taskId) {
        File tempErrorFile = null;
        try {
            // 使用临时物理文件，代替容易 OOM 的 ByteArrayOutputStream
            tempErrorFile = File.createTempFile("error_import_" + taskId, ".xlsx");
            FesodSheet.write(tempErrorFile, PermissionExcelVO.class).sheet("失败数据").doWrite(failedData);

            // TODO: 调用 OSS 客户端将 tempErrorFile 上传到 MinIO 或阿里云
            return "http://your-oss-url/error_import_" + taskId + ".xlsx";
        } catch (Exception e) {
            log.error("生成错误 Excel 失败", e);
            return "";
        } finally {
            // 清理本地临时错误文件
            if (tempErrorFile != null && tempErrorFile.exists()) {
                tempErrorFile.delete();
            }
        }
    }

    /**
     * DFS 深度优先解析节点血缘关系
     *
     * @param identification 当前要解析的权限标识
     * @param batchMap       当前批次数据缓存
     * @param dbMap          数据库已有数据缓存
     * @param resolvedCache  已解析完成的节点缓存（记忆化，避免重复计算）
     * @param visiting       当前递归链路中正在访问的节点集合（用于精准侦测循环依赖）
     * @return 解析完成的节点核心信息
     */
    private ResolvedNode resolveNode(String identification,
                                     Map<String, PermissionExcelVO> batchMap,
                                     Map<String, PermissionPO> dbMap,
                                     Map<String, ResolvedNode> resolvedCache,
                                     Set<String> visiting) {

        // 命中已解析缓存，直接返回 O(1)
        if (resolvedCache.containsKey(identification)) {
            return resolvedCache.get(identification);
        }

        // 命中数据库已有节点，直接提取信息返回
        if (dbMap.containsKey(identification)) {
            PermissionPO po = dbMap.get(identification);
            return new ResolvedNode(po.getId(), po.getParentId(), po.getIdentityLineage());
        }

        // 既不在缓存，也不在数据库，更不在当前批次中 -> 孤儿节点，报错抛弃
        if (!batchMap.containsKey(identification)) {
            throw new RuntimeException("映射失败：找不到权限标识 [" + identification + "]");
        }

        // 循环依赖侦测（A -> B -> A）
        if (visiting.contains(identification)) {
            throw new RuntimeException("侦测到循环依赖：权限标识 [" + identification + "] 构成了死循环");
        }

        // 将当前节点加入正在访问清单
        visiting.add(identification);

        // 获取当前节点的原始数据
        PermissionExcelVO vo = batchMap.get(identification);
        ResolvedNode currentNode = new ResolvedNode(SnowFlakeUtils.generateId(), null, null);

        // 如果没有父级标识，说明它是顶级节点
        if (StringUtils.isBlank(vo.getParentIdentification())) {
            currentNode.setParentId(null);
            currentNode.setLineage(vo.getIdentification());
        } else {
            // 如果有父级标识，递归去解析它的父级！
            ResolvedNode parentNode = resolveNode(vo.getParentIdentification(), batchMap, dbMap, resolvedCache, visiting);

            // 父级解析完毕后，继承父级的 ID 和 Lineage
            currentNode.setParentId(parentNode.getId());
            currentNode.setLineage(buildIdentityLineage(vo.getIdentification(), parentNode.getLineage()));
        }

        // 当前节点解析完毕，移除访问标记，并加入完成缓存
        visiting.remove(identification);
        resolvedCache.put(identification, currentNode);

        return currentNode;
    }

    @Async // 使用虚拟线程池
    @Override
    public void executeExportTask(String taskId) {
        long totalCount = permissionMapper.selectCount(new LambdaQueryWrapperX<>());
        exportProgressManager.initTask(taskId, totalCount);

        if (totalCount == 0) {
            exportProgressManager.completeTask(taskId, "");
            return;
        }

        File tempExportFile = null;
        try {
            // 1. 全量导出使用物理临时文件接收，杜绝 OOM
            tempExportFile = File.createTempFile("export_permissions_" + taskId, ".xlsx");

            // 2. try-with-resources 自动关闭 Writer，将缓冲数据刷入磁盘
            try (ExcelWriter excelWriter = FesodSheet.write(tempExportFile, PermissionExcelVO.class).build()) {

                WriteSheet writeSheet = FesodSheet.writerSheet("权限数据").build();
                int pageSize = 2000;
                long totalPages = (totalCount + pageSize - 1) / pageSize;

                for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                    Page<PermissionPO> pageParam = Page.of(pageNum, pageSize);

                    LambdaQueryWrapperX<PermissionPO> queryWrapper = new LambdaQueryWrapperX<PermissionPO>()
                            .orderByAsc(PermissionPO::getIdentityLineage)
                            .orderByAsc(PermissionPO::getDisplayNo);

                    Page<PermissionPO> pageResult = permissionMapper.selectPage(pageParam, queryWrapper);
                    List<PermissionPO> poList = pageResult.getRecords();

                    if (poList.isEmpty()) {
                        break;
                    }

                    List<PermissionExcelVO> voList = convertToExcelVO(poList);
                    excelWriter.write(voList, writeSheet);

                    exportProgressManager.updateProgress(taskId, poList.size());
                }
            } // 离开此块时，ExcelWriter 触发 finish()，文件安全刷盘

            // 3. TODO: 将物理文件上传到 OSS
            String fileUrl = "http://your-oss-url/export_permissions_" + taskId + ".xlsx";

            // 4. 标记任务完成
            exportProgressManager.completeTask(taskId, fileUrl);

        } catch (Exception e) {
            log.error("权限导出失败", e);
            exportProgressManager.failTask(taskId, "导出生成失败: " + e.getMessage());
        } finally {
            // 5. 务必清理本地导出的临时文件
            if (tempExportFile != null && tempExportFile.exists()) {
                tempExportFile.delete();
            }
        }
    }

    /**
     * 将 PO 转换为给用户看的 VO
     * 难点：我们需要把数据库里的 parentId 翻译回直观的 parentIdentification
     */
    private List<PermissionExcelVO> convertToExcelVO(List<PermissionPO> poList) {
        // 提取这批数据中所有的 parentId
        List<Long> parentIds = poList.stream()
                .map(PermissionPO::getParentId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        // 批量查询父节点标识，避免 N+1
        Map<Long, String> parentIdToIdentityMap = Map.of();
        if (!parentIds.isEmpty()) {
            List<PermissionPO> parentPOs = permissionMapper.selectList(
                    new LambdaQueryWrapperX<PermissionPO>()
                            .select(PermissionPO::getId, PermissionPO::getIdentification)
                            .in(PermissionPO::getId, parentIds)
            );
            parentIdToIdentityMap = parentPOs.stream()
                    .collect(Collectors.toMap(PermissionPO::getId, PermissionPO::getIdentification));
        }

        Map<Long, String> finalParentIdToIdentityMap = parentIdToIdentityMap;

        // 映射转换
        return poList.stream().map(po -> {
            PermissionExcelVO vo = IPermissionMapper.INSTANCE.poToExcelVo(po);
            if (po.getParentId() != null) {
                vo.setParentIdentification(finalParentIdToIdentityMap.get(po.getParentId()));
            }
            return vo;
        }).toList();
    }

}