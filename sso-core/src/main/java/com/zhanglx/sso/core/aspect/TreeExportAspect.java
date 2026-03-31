package com.zhanglx.sso.core.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhanglx.sso.core.annotation.ExportTreeJson;
import com.zhanglx.sso.core.domain.tree.TreeNode;
import com.zhanglx.sso.core.utils.tree.GenericTreeIOUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/17 17:49
 * @ClassName: TreeExportAspect
 * @Description:
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class TreeExportAspect {

    private final ObjectMapper objectMapper;

    /**
     * 环绕通知：拦截标有 @ExportTreeJson 的方法
     */
    @Around("@annotation(exportTreeJson)")
    public Object aroundExport(ProceedingJoinPoint joinPoint, ExportTreeJson exportTreeJson) throws Throwable {
        // 1. 执行原 Controller 方法，获取内存中的树形 DTO 集合
        Object result = joinPoint.proceed();

        // 如果返回值为空或者不是 List，直接放行，交给 Spring 原生处理
        if (!(result instanceof List<?> list) || list.isEmpty() || !(list.get(0) instanceof TreeNode)) {
            return result;
        }

        // 2. 获取当前 HTTP 响应对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return result; // 非 Web 环境（如单测）直接返回
        }

        HttpServletResponse response = attributes.getResponse();
        if (response == null) {
            return result;
        }

        // 3. 配置 HTTP Header，触发浏览器文件下载逻辑
        String fileName = URLEncoder.encode(exportTreeJson.fileName(), StandardCharsets.UTF_8);
        response.setContentType("application/json;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".json");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        // 4. 调用底层的泛型流式导出工具，直接向 Socket 缓冲区写字节
        try (var out = response.getOutputStream()) {
            GenericTreeIOUtils.exportTreeStream(out, (List<? extends TreeNode<?, ?>>) list, objectMapper);
            out.flush();
        } catch (Exception e) {
            log.error("声明式树结构导出失败, 切点: {}", joinPoint.getSignature().toShortString(), e);
            // 发生异常时重置 response（避免写入了一半的脏 JSON）
            if (!response.isCommitted()) {
                response.reset();
                response.setStatus(500);
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"code\":500, \"msg\":\"树结构导出失败\"}");
            }
        }

        // 5. 核心：返回 null。由于我们已经直接操作了 Response 输出流并 committed，
        // 返回 null 会让 Spring 的 Jackson HttpMessageConverter 放弃二次序列化，彻底避免 OOM。
        return null;
    }

}
