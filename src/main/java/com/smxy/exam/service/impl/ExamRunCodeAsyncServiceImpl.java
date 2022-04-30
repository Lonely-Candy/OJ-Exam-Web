package com.smxy.exam.service.impl;

import com.smxy.exam.beans.ExamProcedureStatus;
import com.smxy.exam.beans.RunRecord;
import com.smxy.exam.service.ExamRunCodeAsyncService;
import com.smxy.exam.service.IExamCompletionProblemService;
import com.smxy.exam.service.IExamProcedureStatusService;
import com.smxy.exam.util.CodeRunJudgeUtil;
import com.smxy.exam.util.ExamResultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

/**
 * @author 范颂扬
 * @create 2022-04-30 1:50
 */
@Service
public class ExamRunCodeAsyncServiceImpl implements ExamRunCodeAsyncService {

    private static final Logger LOGGER = LoggerFactory.getLogger("business");

    private IExamProcedureStatusService examProcedureStatusService;

    @Autowired
    public ExamRunCodeAsyncServiceImpl(IExamProcedureStatusService examProcedureStatusService) {
        this.examProcedureStatusService = examProcedureStatusService;
    }

    @Override
    @Async("asyncJudgeExecutor")
    public Future<Boolean> executeRunCodeAsync() {
        while (true) {
            LOGGER.info("run code async begin");
            RunRecord record = null;
            try {
                synchronized (CodeRunJudgeUtil.list) {
                    // 判断当前是否待判断的代码队列为空
                    if (CodeRunJudgeUtil.list.isEmpty()) {
                        CodeRunJudgeUtil.judging = false;
                        return new AsyncResult<>(true);
                    }
                    // 获取当前排在队首的待判断代码
                    record = CodeRunJudgeUtil.list.get(0);
                    // 移除队首
                    CodeRunJudgeUtil.list.remove(0);
                }
                synchronized (CodeRunJudgeUtil.mute) {
                    // 获取当前编译器
                    String compiler = record.getLanguage();
                    LOGGER.info("run compiler: " + compiler);
                    // 开始编译
                    if (!CodeRunJudgeUtil.compile(record)) {
                        // 编译错误
                        LOGGER.warn("compile error");
                        LOGGER.warn("\n" + record.getSource());
                        LOGGER.warn("\n" + CodeRunJudgeUtil.comInfo);
                        // 编译错误的信息写入数据库中
                        saveJudgeResult(record.setResult(7), CodeRunJudgeUtil.comInfo);
                    } else {
                        // 编译成功, 开始执行
                        record = CodeRunJudgeUtil.toRun(record);
                        String score = ExamResultUtil.getScoreByRunResult(record.getResult(), record.getNetCaseScore());
                        record.setScores(score);
                        // 代码运行结束后写入结果
                        saveJudgeResult(record, CodeRunJudgeUtil.comInfo);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("carry out method name: executeRunCodeAsync");
                LOGGER.error("Error message: ", e);
                saveJudgeResult(record, CodeRunJudgeUtil.comInfo);
                // 线程出错后，检测是否还有待判断的代码，若是有则启动线程，对后面的提交进行判题，没有则结束
                synchronized (CodeRunJudgeUtil.list) {
                    if (!CodeRunJudgeUtil.list.isEmpty()) {
                        CodeRunJudgeUtil.judging = false;
                        return executeRunCodeAsync();
                    }
                }
            }
        }
    }

    /**
     * 保存编程填空题判题结果
     *
     * @param
     * @return
     * @author 范颂扬
     * @date 2022-04-28 21:52
     */
    private void saveJudgeResult(RunRecord record, String comInfo) {
        ExamProcedureStatus examProcedureStatus = new ExamProcedureStatus(record, comInfo);
        boolean res = examProcedureStatusService.updateById(examProcedureStatus);
        if (res) {
            LOGGER.info("The operation record is stored in the data");
        } else {
            LOGGER.warn("Failed to save operation records to database");
        }
    }

}
