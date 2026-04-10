package com.zhanglx.sso.auth.utils.excel;

import cn.hutool.json.JSONUtil;
import com.zhanglx.sso.auth.domain.dto.excel.ImportProgressDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/26 17:52
 * 类名：ImportProgressManager
 * 说明：Excel 导入进度 Redis 管理器
 */
@Component
@RequiredArgsConstructor
public class ImportProgressManager {

    private static final String PROGRESS_KEY_PREFIX = "sso:import:progress:";
    private static final long EXPIRE_TIME_HOURS = 24L; // 进度保留24小时
    private final StringRedisTemplate stringRedisTemplate;

    // 初始化任务
    public void initTask(String taskId) {
        ImportProgressDTO progress = ImportProgressDTO.builder()
                .taskId(taskId)
                .processedRows(0)
                .successCount(0)
                .failCount(0)
                .status("PROCESSING")
                .errorExcelUrl("")
                .build();
        saveProgress(progress);
    }

    // 更新处理进度（增量更新）
    public void updateProgress(String taskId, int processedInc, int successInc, int failInc) {
        String key = PROGRESS_KEY_PREFIX + taskId;
        // 使用 Redis 的 Hash 结构或者简单的加锁机制更严谨，但为了性能和简便，
        // 这里采用拉取 -> 修改 -> 覆盖的方式（单虚拟线程更新同一任务，无并发冲突）
        ImportProgressDTO progress = getProgress(taskId);
        if (progress != null) {
            progress.setProcessedRows(progress.getProcessedRows() + processedInc);
            progress.setSuccessCount(progress.getSuccessCount() + successInc);
            progress.setFailCount(progress.getFailCount() + failInc);
            saveProgress(progress);
        }
    }

    // 标记任务完成
    public void completeTask(String taskId, String errorExcelUrl) {
        ImportProgressDTO progress = getProgress(taskId);
        if (progress != null) {
            progress.setStatus("COMPLETED");
            progress.setErrorExcelUrl(errorExcelUrl);
            saveProgress(progress);
        }
    }

    // 标记任务致命异常
    public void failTask(String taskId, String errorMsg) {
        ImportProgressDTO progress = getProgress(taskId);
        if (progress != null) {
            progress.setStatus("FAILED");
            progress.setErrorExcelUrl(errorMsg); // 借用此字段存储致命错误信息
            saveProgress(progress);
        }
    }

    // 获取进度
    public ImportProgressDTO getProgress(String taskId) {
        String json = stringRedisTemplate.opsForValue().get(PROGRESS_KEY_PREFIX + taskId);
        if (json != null) {
            return JSONUtil.toBean(json, ImportProgressDTO.class);
        }

        return null;
    }

    /**
     * 保存任务进度。
     */
    private void saveProgress(ImportProgressDTO progress) {
        stringRedisTemplate.opsForValue().set(
                PROGRESS_KEY_PREFIX + progress.getTaskId(),
                JSONUtil.toJsonStr(progress),
                EXPIRE_TIME_HOURS, TimeUnit.HOURS
        );
    }

}