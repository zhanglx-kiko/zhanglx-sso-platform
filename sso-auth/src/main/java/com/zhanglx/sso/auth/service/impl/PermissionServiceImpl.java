package com.zhanglx.sso.auth.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.zhanglx.sso.auth.domain.dto.PermissionDTO;
import com.zhanglx.sso.auth.domain.dto.UserDTO;
import com.zhanglx.sso.auth.domain.dto.excel.ResolvedNode;
import com.zhanglx.sso.auth.domain.po.PermissionPO;
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
import com.zhanglx.sso.core.exception.CommonErrorCode;
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
        // 查询全局是否存在重复的权限标识。
        if (permissionMapper.exists(new LambdaQueryWrapperX<PermissionPO>()
                .eq(PermissionPO::getIdentification, permissionDTO.getIdentification()))) {
            throw new BusinessException(CommonErrorCode.CONFLICT);
        }

        PermissionPO permissionPO = IPermissionMapper.INSTANCE.toPO(permissionDTO);

        if (Objects.nonNull(permissionPO.getParentId())) {
            PermissionPO parentPermissionPO = permissionMapper.selectById(permissionPO.getParentId());
            if (Objects.isNull(parentPermissionPO)) {
                // 父节点不存在时，主动降级为顶级节点，避免出现脏引用。
                permissionPO.setParentId(null);
            } else {
                // 继承父节点血缘，生成当前节点的 identityLineage。
                permissionPO.setIdentityLineage(buildIdentityLineage(parentPermissionPO.getIdentification(),
                        parentPermissionPO.getIdentityLineage()));
            }
        }

        permissionMapper.insert(permissionPO);
        return IPermissionMapper.INSTANCE.toDTO(permissionPO);
    }

    /**
     * 构建权限节点的血缘路径。
     *
     * <p>该方法会对前端传入的权限标识做统一规范化处理，并根据父级血缘生成当前节点的
     * {@code identityLineage}，用于后续树结构查询、导出和递归更新。</p>
     *
     * @param identity      当前权限标识，例如 {@code mall:goods:add} 或 {@code add}
     * @param parentLineage 父级权限的血缘路径，例如 {@code mall.goods.list}
     * @return 当前节点规范化后的完整血缘路径，例如 {@code mall.goods.list.add}
     */
    private String buildIdentityLineage(String identity, String parentLineage) {
        if (StringUtils.isBlank(identity)) {
            return parentLineage;
        }

        // 1. 统一分隔符，将冒号和中划线都转换为点号。
        String normalizedIdentity = identity.replace(":", ".").replace("-", ".");

        // 2. 顶级节点没有父级血缘时，直接返回规范化后的标识。
        if (StringUtils.isBlank(parentLineage)) {
            return normalizedIdentity;
        }

        // 3. 如果前端传入的标识已经包含父级前缀，直接复用即可。
        // 例如：parent="mall.goods"，identity="mall.goods.add"。
        if (normalizedIdentity.startsWith(parentLineage + ".")) {
            return normalizedIdentity;
        }

        // 4. 否则只取当前标识的最后一段，拼接到父级血缘后面。
        // 例如：parent="system.user.list"，identity="system:user:add"。
        String[] parts = normalizedIdentity.split("\\.");
        String uniqueSuffix = parts[parts.length - 1];

        return parentLineage + "." + uniqueSuffix;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "PermissionTree", allEntries = true)
    public PermissionDTO delPermission(Long id) {
        AssertUtils.notNull(id, "business.data.invalid");

        PermissionPO permissionPO = permissionMapper.selectById(id);
        AssertUtils.notNull(permissionPO, CommonErrorCode.NOT_FOUND);

        // 递归删除当前节点及其所有子孙节点。
        recursiveDelFuncPerm(Lists.newArrayList(IPermissionMapper.INSTANCE.toDTO(permissionPO)));

        return IPermissionMapper.INSTANCE.toDTO(permissionPO);
    }

    /**
     * 递归删除权限节点及其子节点。
     *
     * <p>该方法会先找出当前批次节点的直接子节点，再执行当前层删除，最后递归向下清理所有
     * 子孙权限以及对应的角色权限关联关系。</p>
     *
     * @param permissionDTOS 需要删除的权限节点列表
     * @return 按递归顺序汇总后的已删除权限列表
     */
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

        // 删除当前层权限节点。
        permissionMapper.deleteByIdsWithFill(idDatas);

        // 删除权限与角色之间的关联关系。
        rolePermissionRelationshipMappingMapper.deleteByPermissionIds(idDatas);

        // 继续递归删除子节点。
        if (CollectionUtils.isNotEmpty(subFuncPerms)) {
            results.addAll(recursiveDelFuncPerm(IPermissionMapper.INSTANCE.toDTOList(subFuncPerms)));
        }

        return results;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "PermissionTree", allEntries = true)
    public List<PermissionDTO> batchDelPermission(List<Long> idList) {
        AssertUtils.notEmpty(idList, "business.data.invalid");

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
        AssertUtils.notNull(permissionPO, CommonErrorCode.NOT_FOUND);

        if (permissionMapper.exists(new LambdaQueryWrapperX<PermissionPO>()
                .eq(PermissionPO::getIdentification, permissionDTO.getIdentification())
                .ne(PermissionPO::getId, id))) {
            throw new BusinessException(CommonErrorCode.CONFLICT);
        }

        PermissionPO updateMenu = IPermissionMapper.INSTANCE.toPO(permissionDTO);
        updateMenu.setId(id);
        updateMenu.setCreateTime(permissionPO.getCreateTime());
        updateMenu.setCreateBy(permissionPO.getCreateBy());
        permissionMapper.updateById(updateMenu);

        // 如果权限标识发生变化，需要同步递归更新所有子节点的血缘路径。
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

    /**
     * 递归刷新子节点的血缘路径。
     *
     * <p>当父节点的 {@code identification} 发生变化时，所有子节点都需要基于新的父级血缘重新计算
     * 自身的 {@code identityLineage}。</p>
     *
     * @param identity              当前父节点的权限标识
     * @param parentIdentityLineage 当前父节点的血缘路径
     * @param childrenPermission    待更新的子节点树
     * @return 所有被更新的子节点平铺列表
     */
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

            // 避免父子节点同时命中关键字时结果重复。
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

    /**
     * 为搜索结果补齐祖先节点。
     *
     * <p>关键字搜索时，命中的往往是某个深层子节点。为了让前端仍能正确展示树形层级，需要把这些
     * 节点的所有祖先节点递归补齐回来。</p>
     *
     * @param searchFuncPermLabels 命中的权限节点列表
     * @param allFuncPermLabels    全量权限节点列表
     * @return 包含祖先节点的结果集
     */
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
    public List<PermissionVO> selPermissionByIdentification(String username, List<String> identifications, List<String> permissionTypes) {
        AssertUtils.notBlank(username, "business.data.invalid");

        UserDTO user = userService.findUserByUsername(username);
        AssertUtils.notNull(user, CommonErrorCode.NOT_FOUND);

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
        AssertUtils.notNull(roleId, "business.data.invalid");

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
            rolePermissionRelationshipMappingMapper.deleteByRoleIds(roleId);
        }
    }

    @Async
    @Override
    public void executeImportTask(String taskId, File tempFile) {
        try {
            // 1. 初始化尽力而为监听器，并注入批量落库回调。
            BestEffortImportListener listener = new BestEffortImportListener(
                    validator,
                    taskId,
                    progressManager,
                    this::saveToDatabase
            );

            // 2. Fesod 直接读取磁盘临时文件，避免把整份 Excel 拉进 JVM 内存。
            FesodSheet.read(tempFile, PermissionExcelVO.class, listener)
                    .sheet()
                    .doRead();

            // 3. 如果存在失败数据，则生成错误文件供前端下载定位。
            List<PermissionExcelVO> failedData = listener.getAllFailedData();
            String errorFileUrl = "";
            if (!failedData.isEmpty()) {
                errorFileUrl = generateAndUploadErrorExcel(failedData, taskId);
            }

            // 4. 标记导入任务完成。
            progressManager.completeTask(taskId, errorFileUrl);

            // 5. 导入成功后发布权限变更事件，用于后续缓存刷新。
            eventPublisher.publishEvent(new PermissionChangedEvent(this, taskId));

        } catch (Exception e) {
            log.error("解析 Excel 发生致命异常", e);
            progressManager.failTask(taskId, "导入失败: " + e.getMessage());
        } finally {
            // 6. 无论成功还是失败，都必须清理上传的临时文件。
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * 将一批校验通过的 Excel 数据解析后批量入库。
     *
     * <p>这个方法负责把 Excel 行数据映射成权限实体、解析父子血缘关系、剔除失败记录，并通过编程式
     * 事务统一提交到数据库，保证批次内写入的原子性。</p>
     *
     * @param validBatch 当前批次校验通过的数据
     * @param failedList 当前任务累计失败的数据列表
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
                // 纯 CPU 运算：DFS 解析血缘，不占用数据库连接资源。
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

        // 只有真正写库时才开启事务，尽量缩短连接占用时间。
        if (CollectionUtils.isNotEmpty(poListToInsert)) {
            transactionTemplate.execute(status -> {
                try {
                    permissionMapper.insert(poListToInsert);
                    return true;
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw e;
                }
            });
        }
    }

    /**
     * 生成失败数据的错误 Excel 文件并返回下载地址。
     *
     * <p>当前实现先把失败数据写入本地临时文件，并预留 OSS 上传扩展点；后续接入对象存储后，只需在
     * 待补充 位置补充上传逻辑即可。</p>
     *
     * @param failedData 导入失败的数据列表
     * @param taskId     导入任务 ID
     * @return 错误文件下载地址；生成失败时返回空字符串
     */
    private String generateAndUploadErrorExcel(List<PermissionExcelVO> failedData, String taskId) {
        File tempErrorFile = null;
        try {
            // 使用物理临时文件代替内存流，避免大文件场景下出现 OOM。
            tempErrorFile = File.createTempFile("error_import_" + taskId, ".xlsx");
            FesodSheet.write(tempErrorFile, PermissionExcelVO.class).sheet("失败数据").doWrite(failedData);

            // 待补充: 调用 OSS 客户端将 tempErrorFile 上传到 MinIO 或阿里云。
            return "http://your-oss-url/error_import_" + taskId + ".xlsx";
        } catch (Exception e) {
            log.error("生成错误 Excel 失败", e);
            return "";
        } finally {
            if (tempErrorFile != null && tempErrorFile.exists()) {
                tempErrorFile.delete();
            }
        }
    }

    /**
     * 深度优先解析当前节点的父子血缘关系。
     *
     * <p>该方法会综合当前批次数据、数据库中已存在的数据和已解析缓存，递归构建出节点的
     * {@code id}、{@code parentId} 以及 {@code identityLineage}，同时检测孤儿节点和循环依赖。</p>
     *
     * @param identification 当前要解析的权限标识
     * @param batchMap       当前批次的 Excel 数据缓存
     * @param dbMap          数据库中已存在的权限缓存
     * @param resolvedCache  已完成解析的节点缓存，用于记忆化加速
     * @param visiting       当前递归链路上的访问集合，用于检测循环依赖
     * @return 解析完成后的节点核心信息
     */
    private ResolvedNode resolveNode(String identification,
                                     Map<String, PermissionExcelVO> batchMap,
                                     Map<String, PermissionPO> dbMap,
                                     Map<String, ResolvedNode> resolvedCache,
                                     Set<String> visiting) {

        // 命中已解析缓存时，直接返回。
        if (resolvedCache.containsKey(identification)) {
            return resolvedCache.get(identification);
        }

        // 命中数据库中已存在的节点时，直接复用数据库信息。
        if (dbMap.containsKey(identification)) {
            PermissionPO po = dbMap.get(identification);
            return new ResolvedNode(po.getId(), po.getParentId(), po.getIdentityLineage());
        }

        // 既不在缓存，也不在数据库，还不在当前批次中时，说明它是孤儿节点。
        if (!batchMap.containsKey(identification)) {
            throw BusinessException.badRequest("映射失败：找不到权限标识 [" + identification + "]");
        }

        // 检测循环依赖，例如 A -> B -> A。
        if (visiting.contains(identification)) {
            throw BusinessException.badRequest("侦测到循环依赖：权限标识 [" + identification + "] 构成了死循环");
        }

        visiting.add(identification);

        PermissionExcelVO vo = batchMap.get(identification);
        ResolvedNode currentNode = new ResolvedNode(SnowFlakeUtils.generateId(), null, null);

        // 没有父级标识时，说明当前节点是顶级节点。
        if (StringUtils.isBlank(vo.getParentIdentification())) {
            currentNode.setParentId(null);
            currentNode.setLineage(vo.getIdentification());
        } else {
            // 否则递归解析父节点，并继承父级 ID 与血缘路径。
            ResolvedNode parentNode = resolveNode(vo.getParentIdentification(), batchMap, dbMap, resolvedCache, visiting);
            currentNode.setParentId(parentNode.getId());
            currentNode.setLineage(buildIdentityLineage(vo.getIdentification(), parentNode.getLineage()));
        }

        visiting.remove(identification);
        resolvedCache.put(identification, currentNode);

        return currentNode;
    }

    @Async // 使用异步线程执行导出任务。
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
            // 1. 使用物理临时文件承接导出结果，避免内存膨胀。
            tempExportFile = File.createTempFile("export_permissions_" + taskId, ".xlsx");

            // 2. 按分页写入 Excel，确保大数据量场景下仍可稳定运行。
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
            }

            // 3. 待补充: 将导出的物理文件上传到 OSS。
            String fileUrl = "http://your-oss-url/export_permissions_" + taskId + ".xlsx";

            // 4. 标记任务完成。
            exportProgressManager.completeTask(taskId, fileUrl);

        } catch (Exception e) {
            log.error("权限导出失败", e);
            exportProgressManager.failTask(taskId, "导出生成失败: " + e.getMessage());
        } finally {
            // 5. 清理本地导出的临时文件。
            if (tempExportFile != null && tempExportFile.exists()) {
                tempExportFile.delete();
            }
        }
    }

    @Override
    public List<String> selectPermissionCodesByUserId(Long userId) {
        AssertUtils.notNull(userId, "business.data.invalid");

        List<String> permissionCodes = permissionMapper.selectPermissionCodesByUserId(userId);

        if (CollectionUtils.isEmpty(permissionCodes)) {
            return Lists.newArrayList();
        }

        return permissionCodes;
    }

    /**
     * 将权限实体转换为导出用的 Excel 视图对象。
     *
     * <p>导出时需要把数据库中的 {@code parentId} 翻译回更直观的
     * {@code parentIdentification}，以便导入导出格式保持一致。</p>
     *
     * @param poList 待导出的权限实体列表
     * @return 转换后的 Excel 视图对象列表
     */
    private List<PermissionExcelVO> convertToExcelVO(List<PermissionPO> poList) {
        List<Long> parentIds = poList.stream()
                .map(PermissionPO::getParentId)
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

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

        return poList.stream().map(po -> {
            PermissionExcelVO vo = IPermissionMapper.INSTANCE.poToExcelVo(po);
            if (po.getParentId() != null) {
                vo.setParentIdentification(finalParentIdToIdentityMap.get(po.getParentId()));
            }
            return vo;
        }).toList();
    }

}
