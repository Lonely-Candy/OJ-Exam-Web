package com.smxy.exam.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.smxy.exam.beans.*;
import com.smxy.exam.service.*;
import com.smxy.exam.service.async.ExamAsyncService;
import com.smxy.exam.util.ResultDataUtil;
import com.smxy.exam.util.StringUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学生考试控制器
 *
 * @author 范颂扬
 * @create 2022-04-21 17:56
 */
@Controller
@RequestMapping("/student")
public class StudentExamController {

    private IExamRecordService examRecordService;

    private IExamProcedureBankService examProcedureBankService;

    private IExamProcedureStatusService examProcedureStatusService;

    private IExamProcedureProblemService examProcedureProblemService;

    private IExamCompletionBankService examCompletionBankService;

    private IExamCompletionStatusService examCompletionStatusService;

    private IExamCompletionProblemService examCompletionProblemService;

    private ExamAsyncService examAsyncService;

    @Autowired
    public StudentExamController(IExamRecordService examRecordService, IExamProcedureBankService examProcedureBankService
            , IExamCompletionBankService examCompletionBankService, IExamProcedureProblemService examProcedureProblemService
            , IExamCompletionProblemService examCompletionProblemService, ExamAsyncService examAsyncService
            , IExamCompletionStatusService examCompletionStatusService
            , IExamProcedureStatusService examProcedureStatusService) {
        this.examRecordService = examRecordService;
        this.examProcedureBankService = examProcedureBankService;
        this.examCompletionBankService = examCompletionBankService;
        this.examProcedureProblemService = examProcedureProblemService;
        this.examCompletionProblemService = examCompletionProblemService;
        this.examAsyncService = examAsyncService;
        this.examCompletionStatusService = examCompletionStatusService;
        this.examProcedureStatusService = examProcedureStatusService;
    }

    /**
     * 学生考试考试
     *
     * @param examId
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-21 18:00
     */
    @GetMapping("/beginExam/{examId}")
    public String beginExam(@PathVariable("examId") Integer examId, HttpSession session, Model model) {
        User user = (com.smxy.exam.beans.User) session.getAttribute("loginUserData");
        Wrapper<ExamRecord> queryWrapper = new QueryWrapper<ExamRecord>().eq("exam_id", examId)
                .eq("user_id", user.getUserid());
        ExamRecord examRecord = examRecordService.getOne(queryWrapper);
        if (examRecord == null || examRecord.getSubmitTime() == null) {
            if (examRecord == null) {
                examRecord = new ExamRecord().setExamId(examId).setBeginTime(new Date())
                        .setUserId(user.getUserid()).setUserName(user.getName());
                examRecordService.save(examRecord);
            }
            List<ProblemBankController.ProblemShowData>[] problemShowData = getProblemShowData(examId, false);
            model.addAttribute("programmeProblems", problemShowData[0]);
            model.addAttribute("completionProblems", problemShowData[1]);
            return "/exam/student/problemlist";
        }
        return "redirect:/exam/examList?message=exam_isSubmit";
    }

    /**
     * 查询考试题目数据
     *
     * @param examId
     * @param isShowAnswer
     * @return java.util.List<com.smxy.exam.controller.ProblemBankController.ProblemShowData>[]
     * @author 范颂扬
     * @date 2022-04-23 15:25
     */
    private List<ProblemBankController.ProblemShowData>[] getProblemShowData(Integer examId, boolean isShowAnswer) {
        // 查询考试中的题目数据
        Wrapper<ExamProcedureProblem> examProcedureQueryWrapper = new QueryWrapper<ExamProcedureProblem>().eq("exam_id", examId);
        Wrapper<ExamCompletionProblem> examCompletionQueryWrapper = new QueryWrapper<ExamCompletionProblem>().eq("exam_id", examId);
        List<ExamProcedureProblem> examProcedureProblemList = examProcedureProblemService.list(examProcedureQueryWrapper);
        List<ExamCompletionProblem> examCompletionProblemList = examCompletionProblemService.list(examCompletionQueryWrapper);
        // 获取考试中的题目ID
        List<Integer> procedureProblemIds = examProcedureProblemList.stream().map(ExamProcedureProblem::getProblemId).collect(Collectors.toList());
        List<Integer> completionProblemIds = examCompletionProblemList.stream().map(ExamCompletionProblem::getProblemId).collect(Collectors.toList());
        // 查询题库中的题目数据
        List<ExamProcedureBank> procedures = procedureProblemIds != null ? examProcedureBankService.listByIds(procedureProblemIds) : null;
        List<ExamCompletionBank> completions = examCompletionBankService.listByIds(completionProblemIds);
        // 将考试中的题目ID和分数封装成 Map
        Map<Integer, String> procedureIdAndScoreMap = examProcedureProblemList.stream()
                .collect(Collectors.toMap(ExamProcedureProblem::getProblemId, ExamProcedureProblem::getScore));
        Map<Integer, String> completionIdAndScoreMap = examCompletionProblemList.stream()
                .collect(Collectors.toMap(ExamCompletionProblem::getProblemId, ExamCompletionProblem::getScore));
        List<ProblemBankController.ProblemShowData> procedureProblemShowDataList = new ArrayList<>();
        List<ProblemBankController.ProblemShowData> completionProblemShowDataList = new ArrayList<>();
        // 将题目详细的内容和考试中题目的分数进行组合
        for (int i = 0; i < procedures.size(); i++) {
            ExamProcedureBank procedure = procedures.get(i);
            procedure.setScore(procedureIdAndScoreMap.get(procedure.getId()));
            ProblemBankController.ProblemShowData problemShowData = new ProblemBankController.ProblemShowData(procedure);
            if (!isShowAnswer) {
                problemShowData.setAnswer(null).setReplaceContext(null).setReplaceContextShowAnswerNoEdit(null);
            }
            procedureProblemShowDataList.add(problemShowData);
        }
        for (int i = 0; i < completions.size(); i++) {
            ExamCompletionBank completion = completions.get(i);
            completion.setScore(completionIdAndScoreMap.get(completion.getId()));
            ProblemBankController.ProblemShowData problemShowData = new ProblemBankController.ProblemShowData(completion);
            if (!isShowAnswer) {
                problemShowData.setAnswer(null).setReplaceContext(null).setReplaceContextShowAnswerNoEdit(null);
            }
            completionProblemShowDataList.add(problemShowData);
        }
        return new List[]{procedureProblemShowDataList, completionProblemShowDataList};
    }

    /**
     * 提交考试
     *
     * @param submitData
     * @param session
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-27 14:00
     */
    @ResponseBody
    @Transactional(rollbackFor = {Exception.class})
    @PostMapping("/submitExam")
    public ResultData submitExam(@RequestBody SubmitDataPackage submitData, HttpSession session) {
        // 获取用户数据
        User userData = (User) session.getAttribute("loginUserData");
        // 获取考试题目
        List<ProblemBankController.ProblemShowData>[] problemShowData = getProblemShowData(submitData.examId, true);
        List<ProblemBankController.ProblemShowData> programmeProblems = problemShowData[0];
        List<ProblemBankController.ProblemShowData> completionProblems = problemShowData[1];
        // 保存提交记录
        List<ExamCompletionStatus> completionStatuses = saveCompletionSubmitRecord(userData, submitData, completionProblems);
        if (completionStatuses == null) {
            return ResultDataUtil.error(666, "保存填空题失败");
        }
        List<ExamProcedureStatus> procedureStatuses = saveProcedureSubmitRecord(userData, submitData, programmeProblems);
        if (procedureStatuses == null) {
            return ResultDataUtil.error(666, "保存编程填空题失败");
        }
        // 提交成功后，添加结束考试记录
        Wrapper<ExamRecord> queryWrapper = new UpdateWrapper<ExamRecord>().eq("exam_id", submitData.getExamId())
                .eq("user_id", userData.getUserid()).set("submit_time", new Date());
        boolean res = examRecordService.update(queryWrapper);
        if (!res) {
            return ResultDataUtil.error(666, "考试记录保存失败");
        }
        // 开启判题线程
        examAsyncService.executeJudgeAsync(userData, procedureStatuses, completionStatuses
                , submitData.examId, completionProblems, programmeProblems);
        return ResultDataUtil.success();
    }

    /**
     * 提交考试答案封装
     *
     * @author 范颂扬
     * @create 2022-04-26 17:20
     */
    @Data
    @Accessors(chain = true)
    public static class SubmitDataPackage {

        private Integer examId;

        private List<String[]> completionAnswers;

        private List<String[]> programmeAnswers;

    }

    /**
     * 保存学生提交的填空题记录
     *
     * @param userData
     * @param submitData
     * @param completionProblems
     * @return java.util.List<com.smxy.exam.beans.ExamCompletionStatus>
     * @author 范颂扬
     * @date 2022-04-29 17:53
     */
    private List<ExamCompletionStatus> saveCompletionSubmitRecord(User userData, SubmitDataPackage submitData
            , List<ProblemBankController.ProblemShowData> completionProblems) {
        // 封装提交记录
        List<ExamCompletionStatus> completionStatuses = new ArrayList<>();
        for (int i = 0; i < completionProblems.size(); i++) {
            ProblemBankController.ProblemShowData completionProblem = completionProblems.get(i);
            // 处理提交的答案
            String answer = getAnswerBuffer(submitData.getCompletionAnswers(), i);
            // 获取提交的源码
            completionStatuses.add(new ExamCompletionStatus().setExamId(submitData.getExamId())
                    .setProblemId(String.valueOf(completionProblem.getId()))
                    .setProblemNum(i + 1).setUserId(userData.getUserid()).setSource(answer)
                    .setSubmitTime(LocalDateTime.now()));
        }
        boolean res = examCompletionStatusService.saveBatch(completionStatuses);
        if (res) {
            return completionStatuses;
        }
        return null;
    }

    /**
     * 保存学生提交的编程填空提记录
     *
     * @param userData
     * @param submitData
     * @param programmeProblems
     * @return java.util.List<com.smxy.exam.beans.ExamProcedureStatus>
     * @author 范颂扬
     * @date 2022-04-29 20:52
     */
    private List<ExamProcedureStatus> saveProcedureSubmitRecord(User userData, SubmitDataPackage submitData
            , List<ProblemBankController.ProblemShowData> programmeProblems) {
        List<ExamProcedureStatus> procedureStatuses = new ArrayList<>();
        for (int i = 0; i < programmeProblems.size(); i++) {
            ProblemBankController.ProblemShowData programmeProblem = programmeProblems.get(i);
            // 获取学生提交的答案
            String answer = getAnswerBuffer(submitData.getProgrammeAnswers(), i);
            // 获取将题目和代码进行整合获取到可运行的代码
            String source = StringUtil.getRunCodeByHtml(programmeProblem.getContent()
                    , submitData.getProgrammeAnswers().get(i));
            // 不同的编译器是单独一条记录
            String[] compilers = programmeProblem.getCompilers().split("#");
            // 不同的测试点是单独一条记录
            String[] scores = programmeProblem.getScore().split("#");
            for (int j = 0; j < compilers.length; j++) {
                for (int z = 0; z < scores.length; z++) {
                    procedureStatuses.add(new ExamProcedureStatus().setProblemNum(i + 1)
                            .setUserId(userData.getUserid()).setCaseTestDataId(z)
                            .setExamId(submitData.getExamId()).setSource(source)
                            .setProblemId(programmeProblem.getId()).setAnswer(answer)
                            .setCompiler(compilers[j]).setCodeLength(source.length())
                            .setSubmitTime(LocalDateTime.now()));
                }
            }
        }
        boolean res = examProcedureStatusService.saveBatch(procedureStatuses);
        if (res) {
            return procedureStatuses;
        }
        return null;
    }

    /**
     * 处理学生提交的答案
     *
     * @param answersList
     * @param i
     * @return java.lang.StringBuffer
     * @author 范颂扬
     * @date 2022-04-29 20:58
     */
    private String getAnswerBuffer(List<String[]> answersList, int i) {
        // 获取学生提交的答案
        String[] answers = answersList.get(i);
        // 没有提交答案的空设置为空白字符串（防止异常）
        for (int j = 0; j < answers.length; j++) {
            if (answers[j] == null) {
                answers[j] = new String();
            }
        }
        // 封装学生提交的答案
        StringBuffer answerBuffer = new StringBuffer(answers[0]);
        for (int j = 1; j < answers.length; j++) {
            answerBuffer.append("#");
            answerBuffer.append(answers[j]);
        }
        return answerBuffer.toString();
    }

}
