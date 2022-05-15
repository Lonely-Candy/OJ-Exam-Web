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

    private IExamService examService;

    @Autowired
    public StudentExamController(IExamRecordService examRecordService, IExamProcedureBankService examProcedureBankService
            , IExamCompletionBankService examCompletionBankService, IExamProcedureProblemService examProcedureProblemService
            , IExamCompletionProblemService examCompletionProblemService, ExamAsyncService examAsyncService
            , IExamCompletionStatusService examCompletionStatusService
            , IExamProcedureStatusService examProcedureStatusService, IExamService examService) {
        this.examService = examService;
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
     * 学生开始考试-跳转至填空题考题界面
     *
     * @param examId
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-21 18:00
     */
    @GetMapping("/beginExam/{examId}")
    public String beginExam(@PathVariable("examId") Integer examId, HttpSession session, Model model) {
        User user = (com.smxy.exam.beans.User) session.getAttribute("loginUserData");
        Exam exam = examService.getById(examId);
        if (exam.getFlag() == -1) {
            return "redirect:/exam/examList?message=exam_isNotStarted";
        }
        if (exam.getFlag() == 0) {
            return "redirect:/exam/examList?message=exam_isOver";
        }
        Wrapper<ExamRecord> queryWrapper = new QueryWrapper<ExamRecord>()
                .eq("exam_id", examId).eq("user_id", user.getUserid());
        ExamRecord examRecord = examRecordService.getOne(queryWrapper);
        if (examRecord == null || examRecord.getSubmitTime() == null) {
            if (examRecord == null) {
                examRecord = new ExamRecord().setExamId(examId).setBeginTime(new Date())
                        .setUserId(user.getUserid()).setUserName(user.getName());
                examRecordService.save(examRecord);
            }
            List<ProblemBankController.ProblemShowData> completionProblems = getCompletionProblemList(examId, false);
            model.addAttribute("completionProblems", completionProblems);
        }
        return "exam/student/completionProblemList";
    }

    /**
     * 跳转编程填空题考题界面
     *
     * @param examId
     * @param model
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-05-15 22:15
     */
    @GetMapping("/toProgrammeProblemListPage/{examId}")
    public String toProgrammeProblemListPage(@PathVariable("examId") Integer examId, Model model) {
        Exam exam = examService.getById(examId);
        if (exam.getFlag() == -1) {
            return "redirect:/exam/examList?message=exam_isNotStarted";
        }
        if (exam.getFlag() == 0) {
            return "redirect:/exam/examList?message=exam_isOver";
        }
        List<ProblemBankController.ProblemShowData> programmeProblem = getProgrammeProblemList(examId, false);
        model.addAttribute("programmeProblems", programmeProblem);
        return "exam/student/programmeProblemList";
    }

    /**
     * 获取编程填空题对应的题目集
     *
     * @param examId
     * @param isShowAnswer
     * @return java.util.List<com.smxy.exam.controller.ProblemBankController.ProblemShowData>
     * @author 范颂扬
     * @date 2022-05-15 21:58
     */
    private List<ProblemBankController.ProblemShowData> getProgrammeProblemList(Integer examId, boolean isShowAnswer) {
        // 查询考试中的题目数据
        Wrapper<ExamProcedureProblem> examProcedureQueryWrapper = new QueryWrapper<ExamProcedureProblem>().eq("exam_id", examId);
        List<ExamProcedureProblem> examProcedureProblemList = examProcedureProblemService.list(examProcedureQueryWrapper);
        // 获取考试中的题目ID
        List<Integer> procedureProblemIds = examProcedureProblemList.stream().map(ExamProcedureProblem::getProblemId).collect(Collectors.toList());
        List<ExamProcedureBank> procedures = new ArrayList<>();
        // 查询题库中的题目数据
        if (procedureProblemIds.size() != 0) {
            procedures = examProcedureBankService.listByIds(procedureProblemIds);
        }
        // 将考试中的题目ID和分数封装成 Map
        Map<Integer, String> procedureIdAndScoreMap = examProcedureProblemList.stream()
                .collect(Collectors.toMap(ExamProcedureProblem::getProblemId, ExamProcedureProblem::getScore));
        List<ProblemBankController.ProblemShowData> procedureProblemShowDataList = new ArrayList<>();
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
        return procedureProblemShowDataList;
    }

    /**
     * 获取填空题对应的题目集
     *
     * @param examId
     * @param isShowAnswer
     * @return java.util.List<com.smxy.exam.controller.ProblemBankController.ProblemShowData>[]
     * @author 范颂扬
     * @date 2022-04-23 15:25
     */
    private List<ProblemBankController.ProblemShowData> getCompletionProblemList(Integer examId, boolean isShowAnswer) {
        // 查询考试中的题目数据
        Wrapper<ExamCompletionProblem> examCompletionQueryWrapper = new QueryWrapper<ExamCompletionProblem>().eq("exam_id", examId);
        List<ExamCompletionProblem> examCompletionProblemList = examCompletionProblemService.list(examCompletionQueryWrapper);
        // 获取考试中的题目ID
        List<Integer> completionProblemIds = examCompletionProblemList.stream().map(ExamCompletionProblem::getProblemId).collect(Collectors.toList());
        // 查询题库中的题目数据
        List<ExamCompletionBank> completions = new ArrayList<>();
        if (completionProblemIds.size() != 0) {
            completions = examCompletionBankService.listByIds(completionProblemIds);
        }
        // 将考试中的题目ID和分数封装成 Map
        Map<Integer, String> completionIdAndScoreMap = examCompletionProblemList.stream()
                .collect(Collectors.toMap(ExamCompletionProblem::getProblemId, ExamCompletionProblem::getScore));
        List<ProblemBankController.ProblemShowData> completionProblemShowDataList = new ArrayList<>();
        // 将题目详细的内容和考试中题目的分数进行组合
        for (int i = 0; i < completions.size(); i++) {
            ExamCompletionBank completion = completions.get(i);
            completion.setScore(completionIdAndScoreMap.get(completion.getId()));
            ProblemBankController.ProblemShowData problemShowData = new ProblemBankController.ProblemShowData(completion);
            if (!isShowAnswer) {
                problemShowData.setAnswer(null).setReplaceContext(null).setReplaceContextShowAnswerNoEdit(null);
            }
            completionProblemShowDataList.add(problemShowData);
        }
        return completionProblemShowDataList;
    }

    /**
     * 提交填空题
     *
     * @param submitData
     * @param session
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-27 14:00
     */
    @ResponseBody
    @Transactional(rollbackFor = {Exception.class})
    @PostMapping("/submitCompletionProblem")
    public ResultData submitCompletionProblem(@RequestBody SubmitCompletionDataPackage submitData, HttpSession session) {
        // 获取用户数据
        User userData = (User) session.getAttribute("loginUserData");
        // 获取考试题目
        List<ProblemBankController.ProblemShowData> completionProblems = getCompletionProblemList(submitData.examId, true);
        List<ExamCompletionStatus> completionStatuses = null;
        // 填空题保存提交记录
        if (completionProblems != null && completionProblems.size() != 0) {
            completionStatuses = saveCompletionSubmitRecord(userData, submitData, completionProblems);
            if (completionStatuses == null) {
                return ResultDataUtil.error(666, "保存填空题失败");
            }
        }
        // 提交成功后，添加结束考试记录
        Wrapper<ExamRecord> queryWrapper = new UpdateWrapper<ExamRecord>().eq("exam_id", submitData.getExamId())
                .eq("user_id", userData.getUserid()).set("submit_time", new Date());
        boolean res = examRecordService.update(queryWrapper);
        if (!res) {
            return ResultDataUtil.error(666, "考试记录保存失败");
        }
        // 开启判题线程
        examAsyncService.executeJudgeAsync(userData, null, completionStatuses
                , submitData.examId, completionProblems, null);
        return ResultDataUtil.success();
    }

    /**
     * 提交编程填空题
     *
     * @param submitData
     * @param session
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-05-15 22:07
     */
    @ResponseBody
    @Transactional(rollbackFor = {Exception.class})
    @PostMapping("/submitProgrammeProblem")
    public ResultData submitProgrammeProblem(@RequestBody SubmitProgrammeDataPackage submitData, HttpSession session) {
        // 获取用户数据
        User userData = (User) session.getAttribute("loginUserData");
        // 获取考试题目
        List<ProblemBankController.ProblemShowData> programmeProblems = getProgrammeProblemList(submitData.examId, true);
        // 编程填题保存提交记录
        List<ExamProcedureStatus> procedureStatuses = null;
        if (programmeProblems != null && programmeProblems.size() != 0) {
            procedureStatuses = saveProcedureSubmitRecord(userData, submitData, programmeProblems);
            if (procedureStatuses == null) {
                return ResultDataUtil.error(666, "保存编程填空题失败");
            }
        }
        // 开启判题线程
        examAsyncService.executeJudgeAsync(userData, procedureStatuses, null, submitData.examId, null, programmeProblems);
        return ResultDataUtil.success();
    }

    /**
     * 提交填空题答案封装
     *
     * @author 范颂扬
     * @create 2022-04-26 17:20
     */
    @Data
    @Accessors(chain = true)
    public static class SubmitCompletionDataPackage {

        private Integer examId;

        private List<String[]> completionAnswers;

    }

    /**
     * 提交编程填空题答案封装
     *
     * @author 范颂扬
     * @create 2022-05-15 22:05
     */
    @Data
    @Accessors(chain = true)
    public static class SubmitProgrammeDataPackage {

        private Integer examId;

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
    private List<ExamCompletionStatus> saveCompletionSubmitRecord(User userData, SubmitCompletionDataPackage submitData
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
    private List<ExamProcedureStatus> saveProcedureSubmitRecord(User userData, SubmitProgrammeDataPackage submitData
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
