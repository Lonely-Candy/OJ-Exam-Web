package com.smxy.exam.service.async.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.smxy.exam.beans.ExamCompletionStatus;
import com.smxy.exam.beans.ExamProcedureStatus;
import com.smxy.exam.beans.ExamRecord;
import com.smxy.exam.beans.User;
import com.smxy.exam.controller.ProblemBankController;
import com.smxy.exam.service.*;
import com.smxy.exam.service.async.ExamAsyncService;
import com.smxy.exam.service.async.ExamJudgeAsyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * 线程执行服务类
 *
 * @author 范颂扬
 * @create 2022-04-27 14:58
 */
@Service
public class ExamAsyncServiceImpl implements ExamAsyncService {

    private IExamRecordService examRecordService;

    private ExamJudgeAsyncService examJudgeAsyncService;

    private IExamProcedureStatusService examProcedureStatusService;

    private IExamCompletionStatusService examCompletionStatusService;

    private static final Logger LOGGER = LoggerFactory.getLogger("business");

    @Autowired
    public ExamAsyncServiceImpl(ExamJudgeAsyncService examJudgeAsyncService
            , IExamProcedureStatusService examProcedureStatusService, IExamRecordService examRecordService
            , IExamCompletionStatusService examCompletionStatusService) {
        this.examRecordService = examRecordService;
        this.examJudgeAsyncService = examJudgeAsyncService;
        this.examProcedureStatusService = examProcedureStatusService;
        this.examCompletionStatusService = examCompletionStatusService;
    }

    @Override
    @Async("asyncJudgeExecutor")
    public void executeJudgeAsync(User userData, List<ExamProcedureStatus> procedureStatuses
            , List<ExamCompletionStatus> completionStatuses, Integer examId
            , List<ProblemBankController.ProblemShowData> completionProblems
            , List<ProblemBankController.ProblemShowData> programmeProblems) {
        LOGGER.info("judge async begin");
        // 线程执行结果集
        // 1. 填空题---判题处理(从线程池中启动线程进行处理)
        Future<Boolean> future1 = new AsyncResult<>(true);
        if (completionProblems != null && completionProblems.size() != 0) {
            future1 = examJudgeAsyncService.executeExamCompletionJudgeAsync(userData, completionStatuses, completionProblems);
        }
        // 2. 编程填空题---处理(从线程池中启动线程进行处理)
        Future<Boolean> future2 = new AsyncResult<>(true);
        if (programmeProblems != null && programmeProblems.size() != 0) {
            Map<Integer, String> proIdMapScores = programmeProblems.stream()
                    .collect(Collectors.toMap(ProblemBankController.ProblemShowData::getId
                            , ProblemBankController.ProblemShowData::getScore));
            future2 = examJudgeAsyncService.executeExamProgrammeJudgeAsync(userData, procedureStatuses, proIdMapScores);
        }
    }

    /**
     * 计算总分
     *
     * @return void
     * @author 范颂扬
     * @date 2022-04-30 2:39
     */
    private void calculateTotalScore(Integer examId, String userId) {
        Wrapper<ExamProcedureStatus> programmeQueryWrapper = new QueryWrapper<ExamProcedureStatus>()
                .eq("exam_id", examId).eq("user_id", userId)
                .select("problem_id, case_test_data_id, max(score) as score")
                .groupBy("problem_id", "case_test_data_id");
        Wrapper<ExamCompletionStatus> completionQueryWrapper = new QueryWrapper<ExamCompletionStatus>()
                .eq("exam_id", examId).eq("user_id", userId).select("sum(score) as score");
        List<ExamProcedureStatus> procedureStatuses = examProcedureStatusService.list(programmeQueryWrapper);
        ExamCompletionStatus completionStatuses = examCompletionStatusService.getOne(completionQueryWrapper);
        // 使用填空题总分数初始化总分
        float totalScore = completionStatuses != null ? totalScore = completionStatuses.getScore() : 0f;
        // 统计分数
        for (int i = 0; i < procedureStatuses.size(); i++) {
            totalScore += procedureStatuses.get(i).getScore();
        }
        Wrapper<ExamRecord> updateWrapper = new UpdateWrapper<ExamRecord>()
                .eq("exam_id", examId).eq("user_id", userId)
                .set("score", totalScore);
        boolean res = examRecordService.update(updateWrapper);
        if (res) {
            LOGGER.info("examId:" + examId);
            LOGGER.info("userId: " + userId);
            LOGGER.info("calculate total score: " + totalScore);
        }
    }

}
