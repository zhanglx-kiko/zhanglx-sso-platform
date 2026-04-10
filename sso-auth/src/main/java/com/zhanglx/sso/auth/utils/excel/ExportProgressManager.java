package com.zhanglx.sso.auth.utils.excel;

import cn.hutool.json.JSONUtil;
import com.zhanglx.sso.auth.domain.dto.excel.ExportProgressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/27 16:23
 * @ClassName: ExportProgressManager
 * @Description: Excel 导出进度 Redis 管理器
 */
@Component
@RequiredArgsConstructor
public class ExportProgressManager {

    private static final String PROGRESS_KEY_PREFIX = "sso:export:progress:";
    private static final long EXPIRE_TIME_HOURS = 24L;
    private final StringRedisTemplate stringRedisTemplate;

    // 初始化任务 (需要传入预估的总行数)
    public void initTask(String taskId, long totalRows) {
        ExportProgressDTO progress = ExportProgressDTO.builder()
                .taskId(taskId)
                .totalRows(totalRows)
                .processedRows(0L)
                .status("PROCESSING")
                .fileUrl("")
                .errorMsg("")
                .build();
        saveProgress(progress);
    }

    // 更新处理进度（增量更新）
    public void updateProgress(String taskId, long processedInc) {
        ExportProgressDTO progress = getProgress(taskId);
        if (progress != null) {
            progress.setProcessedRows(progress.getProcessedRows() + processedInc);
            saveProgress(progress);
        }
    }

    // 标记任务完成
    public void completeTask(String taskId, String fileUrl) {
        ExportProgressDTO progress = getProgress(taskId);
        if (progress != null) {
            progress.setStatus("COMPLETED");
            progress.setFileUrl(fileUrl);
            // 确保进度条达到 100%
            progress.setProcessedRows(progress.getTotalRows());
            saveProgress(progress);
        }
    }

    // 标记任务失败
    public void failTask(String taskId, String errorMsg) {
        ExportProgressDTO progress = getProgress(taskId);
        if (progress != null) {
            progress.setStatus("FAILED");
            progress.setErrorMsg(errorMsg);
            saveProgress(progress);
        }
    }

    // 获取进度
    public ExportProgressDTO getProgress(String taskId) {
        String json = stringRedisTemplate.opsForValue().get(PROGRESS_KEY_PREFIX + taskId);
        if (json != null) {
            return JSONUtil.toBean(json, ExportProgressDTO.class);
        }
        return null;
    }

/**
 * 保存任务进度。
 */
    private void saveProgress(ExportProgressDTO progress) {
        stringRedisTemplate.opsForValue().set(
                PROGRESS_KEY_PREFIX + progress.getTaskId(),
                JSONUtil.toJsonStr(progress),
                EXPIRE_TIME_HOURS, TimeUnit.HOURS
        );
    }

}
