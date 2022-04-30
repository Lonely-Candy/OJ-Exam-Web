package com.smxy.exam.service;

import com.smxy.exam.beans.ExamCompletionStatus;
import com.smxy.exam.beans.ExamProcedureStatus;
import com.smxy.exam.beans.User;
import com.smxy.exam.controller.ProblemBankController;
import com.smxy.exam.controller.StudentExamController;

import java.util.List;
import java.util.Map;

/**
 * @author 范颂扬
 * @create 2022-04-27 14:54
 */
public interface ExamAsyncService {

    /**
     * 执行判题异步处理
     *
     * @param userData
     * @param procedureStatuses
     * @param examId
     * @param completionStatuses
     * @param completionProblems
     * @param programmeProblems
     * @return void
     * @author 范颂扬
     * @date 2022-04-30 1:35
     */
    void executeJudgeAsync(User userData, List<ExamProcedureStatus> procedureStatuses
            , List<ExamCompletionStatus> completionStatuses, Integer examId
            , List<ProblemBankController.ProblemShowData> completionProblems
            , List<ProblemBankController.ProblemShowData> programmeProblems);

}
