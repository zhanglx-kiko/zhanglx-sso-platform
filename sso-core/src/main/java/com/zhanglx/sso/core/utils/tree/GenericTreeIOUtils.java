package com.zhanglx.sso.core.utils.tree;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.type.CollectionType;
import com.zhanglx.sso.core.domain.tree.TreeNode;
import com.zhanglx.sso.core.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GenericTreeIOUtils {

    public static void exportTreeStream(OutputStream out, List<? extends TreeNode<?, ?>> roots, ObjectMapper mapper) {
        try {
            mapper.writeValue(out, roots);
        } catch (Exception e) {
            log.error("Generic tree export failed", e);
            throw BusinessException.internalError("树形数据导出异常", e);
        }
    }

    public static <T extends TreeNode<T, ID>, ID> List<T> importAndFlattenStream(
            InputStream in, Class<T> nodeClass, ID defaultRootId, ObjectMapper mapper) {

        try {
            CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, nodeClass);
            List<T> treeRoots = mapper.readValue(in, listType);

            List<T> flatList = new ArrayList<>();
            if (treeRoots != null) {
                for (T root : treeRoots) {
                    flattenNode(root, defaultRootId, flatList);
                }
            }
            return flatList;
        } catch (Exception e) {
            log.error("Generic tree import failed", e);
            throw BusinessException.internalError("树形数据导入异常", e);
        }
    }

    private static <T extends TreeNode<T, ID>, ID> void flattenNode(T node, ID parentId, List<T> flatList) {
        node.setParentId(parentId);
        flatList.add(node);

        List<T> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            for (T child : children) {
                flattenNode(child, node.getId(), flatList);
            }
            node.setChildren(null);
        }
    }

}
