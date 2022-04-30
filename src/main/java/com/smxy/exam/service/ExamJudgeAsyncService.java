package com.smxy.exam.service;

import com.smxy.exam.beans.ExamCompletionStatus;
import com.smxy.exam.beans.ExamProcedureStatus;
import com.smxy.exam.beans.User;
import com.smxy.exam.controller.ProblemBankController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author 范颂扬
 * @create 2022-04-30 1:44
 */
public interface ExamJudgeAsyncService {

    /**
     * 执行填空题判题异步处理
     *
     * @param userData
     * @param completionProblems
     * @param completionStatuses
     * @return void
     * @author 范颂扬
     * @date 2022-04-29 21:28
     */
    Future<Boolean> executeExamCompletionJudgeAsync(User userData, List<ExamCompletionStatus> completionStatuses
            , List<ProblemBankController.ProblemShowData> completionProblems);

    /**
     * 执行提交代码异步处理
     *
     * @param userData
     * @param procedureStatuses
     * @param proIdMapScores
     * @return void
     * @author 范颂扬
     * @date 2022-04-29 22:23
     */
    Future<Boolean> executeExamProgrammeJudgeAsync(User userData, List<ExamProcedureStatus> procedureStatuses
            , Map<Integer, String> proIdMapScores);

}
