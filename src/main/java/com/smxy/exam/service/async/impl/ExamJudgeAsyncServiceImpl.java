package com.smxy.exam.service.async.impl;

import com.smxy.exam.beans.ExamCompletionStatus;
import com.smxy.exam.beans.ExamProcedureStatus;
import com.smxy.exam.beans.RunRecord;
import com.smxy.exam.beans.User;
import com.smxy.exam.controller.ProblemBankController;
import com.smxy.exam.service.async.ExamJudgeAsyncService;
import com.smxy.exam.service.async.ExamRunCodeAsyncService;
import com.smxy.exam.service.IExamCompletionStatusService;
import com.smxy.exam.util.CodeRunJudgeUtil;
import com.smxy.exam.util.CompletionJudgeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author 范颂扬
 * @create 2022-04-30 1:46
 */
@Service
public class ExamJudgeAsyncServiceImpl implements ExamJudgeAsyncService {

    private ExamRunCodeAsyncService examRunCodeAsyncService;

    private IExamCompletionStatusService examCompletionStatusService;

    private static final Logger LOGGER = LoggerFactory.getLogger("business");

    @Autowired
    public ExamJudgeAsyncServiceImpl(ExamRunCodeAsyncService examRunCodeAsyncService
            , IExamCompletionStatusService examCompletionStatusService) {
        this.examRunCodeAsyncService = examRunCodeAsyncService;
        this.examCompletionStatusService = examCompletionStatusService;
    }

    @Override
    @Async("asyncJudgeExecutor")
    public Future<Boolean> executeExamCompletionJudgeAsync(User userData, List<ExamCompletionStatus> completionStatuses
            , List<ProblemBankController.ProblemShowData> completionProblems) {
        try {
            LOGGER.info("completion async judge begin");
            // 循环执行判题
            for (int i = 0; i < completionStatuses.size(); i++) {
                ExamCompletionStatus examCompletionStatus = completionStatuses.get(i);
                ProblemBankController.ProblemShowData completionProblem = completionProblems.get(i);
                // 判题计算分数
                Map<Float, Integer> judgeResult = CompletionJudgeUtil.judge(examCompletionStatus.getSource().split("#")
                        , completionProblem.getAnswer().split("#"), completionProblem.getScore().split("#"));
                Float score = judgeResult.keySet().iterator().next();
                Integer result = judgeResult.get(score);
                // 记录分数
                completionStatuses.set(i, examCompletionStatus.setScore(score).setResult(result));
            }
            // 将判题结果写入数据
            boolean res = examCompletionStatusService.updateBatchById(completionStatuses);
            if (!res) {
                LOGGER.error("Fill-in-the-blank question scores failed to be stored in the database");
            }
        } catch (Exception e) {
            LOGGER.error("carry out method name: executeExamCompletionJudgeAsync");
            LOGGER.error("Error message: ", e);
            return new AsyncResult<>(false);
        }
        return new AsyncResult<>(true);
    }

    @Override
    @Async("asyncJudgeExecutor")
    public Future<Boolean> executeExamProgrammeJudgeAsync(User userData, List<ExamProcedureStatus> procedureStatuses
            , Map<Integer, String> proIdMapScores) {
        try {
            LOGGER.info("programme async judge begin");
            List<Future<Boolean>> futures = new ArrayList<>();
            // 创建代码运行对象
            for (int i = 0, j = 0; i < procedureStatuses.size(); i++, j++) {
                ExamProcedureStatus procedureStatus = procedureStatuses.get(i);
                // 转换运行记录
                RunRecord record = new RunRecord(procedureStatus, "programme", LocalDateTime.now(), proIdMapScores);
                // 开始判题
                synchronized (CodeRunJudgeUtil.list) {
                    CodeRunJudgeUtil.list.add(record);
                    if (!CodeRunJudgeUtil.judging) {
                        CodeRunJudgeUtil.judging = true;
                        // 开启线程执行代码
                        futures.add(examRunCodeAsyncService.executeRunCodeAsync());
                    }
                }
            }
            // 等待所有线程结束
            for (int i = 0; i < futures.size(); i++) {
                Future<Boolean> future = futures.get(i);
                while (true) {
                    if (future.isDone()) {
                        break;
                    } else {
                        Thread.sleep(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("carry out method name: executeExamCodeRunAsync");
            LOGGER.error("Error message: ", e);
            return new AsyncResult<>(false);
        }
        return new AsyncResult<>(true);
    }

}
