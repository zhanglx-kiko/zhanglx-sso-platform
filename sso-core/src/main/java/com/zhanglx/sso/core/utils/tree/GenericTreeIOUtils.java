package com.zhanglx.sso.core.utils.tree;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.zhanglx.sso.core.exception.BusinessException;
import com.zhanglx.sso.core.domain.tree.TreeNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/17 17:27
 * @ClassName: GenericTreeIOUtils
 * @Description:
 */
@Slf4j
public class GenericTreeIOUtils {

    /**
     * 1. 泛型流式导出：直接将内存中的树写入输出流，拒绝 String 驻留
     *
     * @param out    如 HttpServletResponse.getOutputStream()
     * @param roots  树的根节点集合
     * @param mapper Spring 管理的 ObjectMapper（自带时间、Long格式化配置）
     */
    public static void exportTreeStream(
            OutputStream out, List<? extends TreeNode<?, ?>> roots, ObjectMapper mapper) {
        try {
            // Jackson 的 writeValue 本身接收的就是 Object，因此通配符完全不影响底层序列化
            mapper.writeValue(out, roots);
        } catch (IOException e) {
            log.error("Generic tree export failed", e);
            throw new BusinessException("树形数据导出异常", e.getMessage());
        }
    }

    /**
     * 2. 泛型流式导入与拍平：从 JSON 流解析并转为平铺 List，供批量入库
     *
     * @param in           如 HttpServletRequest.getInputStream() 或 FileInputStream
     * @param nodeClass    具体节点类型（如 PermissionDTO.class）
     * @param defaultRootId 根节点的默认父ID（如 0L）
     * @param mapper       ObjectMapper
     * @return 拍平后的泛型列表，直接可交由 MyBatis-Plus 的 saveBatch 处理
     */
    public static <T extends TreeNode<T, ID>, ID> List<T> importAndFlattenStream(
            InputStream in, Class<T> nodeClass, ID defaultRootId, ObjectMapper mapper) {

        try {
            // 构造泛型集合类型：List<T>
            CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, nodeClass);

            // 直接从流中反序列化为树形结构
            List<T> treeRoots = mapper.readValue(in, listType);

            // 拍平处理
            List<T> flatList = new ArrayList<>();
            if (treeRoots != null) {
                for (T root : treeRoots) {
                    flattenNode(root, defaultRootId, flatList);
                }
            }
            return flatList;

        } catch (IOException e) {
            log.error("Generic tree import failed", e);
            throw new BusinessException("树形数据导入异常", e.getMessage());
        }
    }

    /**
     * 泛型 DFS 拍平算法
     */
    private static <T extends TreeNode<T, ID>, ID> void flattenNode(
            T node, ID parentId, List<T> flatList) {

        node.setParentId(parentId);
        flatList.add(node);

        List<T> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            for (T child : children) {
                // 子节点的 parentId 指向当前节点的 ID
                flattenNode(child, node.getId(), flatList);
            }
            // 拍平后切断子节点引用，帮助 GC 快速回收无用关系，减轻批量插入内存压力
            node.setChildren(null);
        }
    }

}
