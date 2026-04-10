package com.zhanglx.sso.auth.listener.excel;

import com.zhanglx.sso.auth.domain.vo.PermissionExcelVO;
import com.zhanglx.sso.auth.utils.excel.ImportProgressManager;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.read.listener.ReadListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @Author: Zhang L X
 * @Create: 2026/3/26 17:22
 * @ClassName: BestEffortImportListener
 * @Description: 尽力而为策略的 Fesod 监听器 (已修复进度同步机制)
 */
@Slf4j
public class BestEffortImportListener implements ReadListener<PermissionExcelVO> {

    private final Validator validator;
    private final String taskId;
    private final ImportProgressManager progressManager; // 直接注入进度管理器

    // 回调接口：接收 (有效数据批次, 全局失败数据集合)
    private final BiConsumer<List<PermissionExcelVO>, List<PermissionExcelVO>> batchConsumer;

    // 积攒的有效数据批次
    private final List<PermissionExcelVO> validDataBatch = new ArrayList<>();

    // 收集所有失败的数据（用于最后生成错误 Excel）
    @Getter
    private final List<PermissionExcelVO> allFailedData = new ArrayList<>();

    private final int batchSize = 1000;

    // ======== 用于精确控制 Redis 更新频率的游标 ========
    private int unsyncedProcessed = 0; // 尚未同步到 Redis 的已处理行数
    private int unsyncedSuccess = 0;   // 尚未同步的成功数
    private int unsyncedFail = 0;      // 尚未同步的失败数

    public BestEffortImportListener(Validator validator,
                                    String taskId,
                                    ImportProgressManager progressManager,
                                    BiConsumer<List<PermissionExcelVO>, List<PermissionExcelVO>> batchConsumer) {
        this.validator = validator;
        this.taskId = taskId;
        this.progressManager = progressManager;
        this.batchConsumer = batchConsumer;
    }

    @Override
    public void invoke(PermissionExcelVO data, AnalysisContext context) {
        unsyncedProcessed++;

        // 1. JSR-303 校验
        Set<ConstraintViolation<PermissionExcelVO>> violations = validator.validate(data);
        if (!violations.isEmpty()) {
            // 校验失败：填入错误原因，放入失败集合
            String errorMsg = violations.iterator().next().getMessage();
            data.setErrorMessage(errorMsg);
            allFailedData.add(data);
            unsyncedFail++; // 记录一次失败
        } else {
            // 校验成功：放入有效批次
            validDataBatch.add(data);
        }

        // 2. 满一批次，执行入库逻辑
        if (validDataBatch.size() >= batchSize) {
            processValidBatch();
        }

        // 3. 每解析 100 行，向 Redis 汇报一次进度，让前端进度条动起来
        if (unsyncedProcessed >= 100) {
            flushProgressToRedis();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 处理最后一批不足 batchSize 的有效数据
        if (!validDataBatch.isEmpty()) {
            processValidBatch();
        }

        // 结束时，把剩下没汇报的零星进度刷入 Redis
        flushProgressToRedis();
    }

    /**
     * 处理当前批次的内部逻辑。
     */
    private void processValidBatch() {
        try {
            int attemptedSize = validDataBatch.size();
            int initialFailedSize = allFailedData.size();

            // 执行真实入库，业务层会把映射失败的从 validDataBatch 剔除，并加入 allFailedData
            batchConsumer.accept(validDataBatch, allFailedData);

            // 业务层新增加的失败数量（如找不到父节点、循环依赖）
            int newlyFailed = allFailedData.size() - initialFailedSize;
            // 真正入库成功的数量
            int newlySuccess = attemptedSize - newlyFailed;

            // 累加到未同步游标中
            unsyncedSuccess += newlySuccess;
            unsyncedFail += newlyFailed;

        } catch (Exception e) {
            log.error("数据库批量入库异常", e);
            // 如果发生如唯一索引冲突等致命异常，整批标记失败
            for (PermissionExcelVO vo : validDataBatch) {
                vo.setErrorMessage("数据库写入失败: " + e.getMessage());
                allFailedData.add(vo);
            }
            unsyncedFail += validDataBatch.size();
        } finally {
            validDataBatch.clear();
            // 每次执行完数据库批量插入，强制刷新一次进度
            flushProgressToRedis();
        }
    }

    /**
     * 将尚未同步的增量数据刷入 Redis，并重置游标
     */
    /**
     * 刷新当前处理状态。
     */
    private void flushProgressToRedis() {
        if (unsyncedProcessed > 0 || unsyncedSuccess > 0 || unsyncedFail > 0) {
            progressManager.updateProgress(taskId, unsyncedProcessed, unsyncedSuccess, unsyncedFail);

            // 汇报完后清零
            unsyncedProcessed = 0;
            unsyncedSuccess = 0;
            unsyncedFail = 0;
        }
    }
}