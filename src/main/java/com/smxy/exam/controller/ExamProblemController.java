package com.smxy.exam.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smxy.exam.beans.*;
import com.smxy.exam.service.*;
import com.smxy.exam.util.ExamResultUtil;
import com.smxy.exam.util.ResultDataUtil;
import com.smxy.exam.util.StringUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考试题目处理器
 *
 * @author 范颂扬
 * @create 2022-02-23 19:14
 */
@Controller
@RequestMapping("/exam")
public class ExamProblemController {

    private IExamProcedureBankService examProcedureBankService;

    private IExamCompletionBankService examCompletionBankService;

    private IExamProcedureProblemService examProcedureProblemService;

    private IExamCompletionProblemService examCompletionProblemService;

    private IExamProcedureStatusService examProcedureStatusService;

    private IExamCompletionStatusService examCompletionStatusService;

    @Autowired
    public ExamProblemController(IExamCompletionBankService examCompletionBankService
            , IExamProcedureProblemService examProcedureProblemService, IExamProcedureBankService examProcedureBankService
            , IExamCompletionProblemService examCompletionProblemService, IExamProcedureStatusService examProcedureStatusService
            , IExamCompletionStatusService examCompletionStatusService) {
        this.examProcedureBankService = examProcedureBankService;
        this.examCompletionBankService = examCompletionBankService;
        this.examProcedureProblemService = examProcedureProblemService;
        this.examCompletionProblemService = examCompletionProblemService;
        this.examCompletionStatusService = examCompletionStatusService;
        this.examProcedureStatusService = examProcedureStatusService;
    }

    /**
     * 获取所有的考试题目 分页查询
     *
     * @param index
     * @param type
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-15 16:11
     */
    @ResponseBody
    @GetMapping("/getAllProblem/{index}")
    public ResultData getAllProblem(@PathVariable("index") Integer index, @PathParam("type") String type) {
        Page page = null;
        if ("completion".equals(type)) {
            page = new Page<ExamCompletionBank>(index, 8);
            examCompletionBankService.page(page);
        } else if ("programme".equals(type)) {
            page = new Page<ExamProcedureBank>(index, 8);
            examProcedureBankService.page(page);
        }
        return ResultDataUtil.success(page);
    }

    /**
     * 添加题目到考试中
     *
     * @param completions
     * @param programmes
     * @param examId
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-17 15:57
     */
    @ResponseBody
    @Transactional(rollbackFor = {Exception.class})
    @PostMapping("/addProblemToExam")
    public ResultData addProblemToExam(@RequestParam(value = "completions[]", required = false) Integer[] completions
            , @RequestParam(value = "programmes[]", required = false) Integer[] programmes
            , @RequestParam Integer examId) {
        boolean completionSaveResult = false;
        boolean procedureSaveResult = false;
        if (programmes != null) {
            // 查询是否出现题目重复添加
            Wrapper<ExamProcedureProblem> procedureWrapper = new QueryWrapper<ExamProcedureProblem>()
                    .eq("exam_id", examId)
                    .in("problem_id", programmes);
            long isExistProcedure = examProcedureProblemService.count(procedureWrapper);
            if (isExistProcedure > 0) {
                return ResultDataUtil.error(222, "重复添加题目，请重新选择");
            }
            // 查询当前考试中的题数
            procedureWrapper = new QueryWrapper<ExamProcedureProblem>().eq("exam_id", examId);
            Integer procedureCount = Math.toIntExact(examProcedureProblemService.count(procedureWrapper));
            // 查询对应的题目信息
            List<ExamProcedureBank> procedureBanks = examProcedureBankService.listByIds(Arrays.asList(programmes));
            // List 转 Map
            Map<Integer, String> procedureIdBothScoreMap = procedureBanks.stream()
                    .collect(Collectors.toMap(ExamProcedureBank::getId, ExamProcedureBank::getScore));
            // 封装数据
            Collection<ExamProcedureProblem> procedureProblems = new ArrayList<>();
            for (int i = 0; i < programmes.length; i++) {
                ExamProcedureProblem procedureProblem = new ExamProcedureProblem().setProblemId(programmes[i])
                        .setProblemNum(++procedureCount).setExamId(examId)
                        .setSubmitSum(0).setAcceptedNum(0)
                        .setScore(procedureIdBothScoreMap.get(programmes[i]));
                procedureProblems.add(procedureProblem);
            }
            // 批量添加
            procedureSaveResult = examProcedureProblemService.saveBatch(procedureProblems);
        }
        if (completions != null) {
            Wrapper<ExamCompletionProblem> completionWrapper = new QueryWrapper<ExamCompletionProblem>()
                    .eq("exam_id", examId)
                    .in("problem_id", completions);
            long isExistCompletion = examCompletionProblemService.count(completionWrapper);
            if (isExistCompletion > 0) {
                return ResultDataUtil.error(222, "重复添加题目，请重新选择");
            }
            completionWrapper = new QueryWrapper<ExamCompletionProblem>().eq("exam_id", examId);
            List<ExamCompletionBank> completionBanks = examCompletionBankService.listByIds(Arrays.asList(completions));
            Map<Integer, String> completionIdBothScoreMap = completionBanks.stream()
                    .collect(Collectors.toMap(ExamCompletionBank::getId, ExamCompletionBank::getScore));
            Integer completionCount = Math.toIntExact(examCompletionProblemService.count(completionWrapper));
            Collection<ExamCompletionProblem> completionProblems = new ArrayList<>();
            for (int i = 0; i < completions.length; i++) {
                ExamCompletionProblem examCompletionProblem = new ExamCompletionProblem().setProblemId(completions[i])
                        .setProblemNum(++completionCount).setExamId(examId)
                        .setScore(completionIdBothScoreMap.get(completions[i]));
                completionProblems.add(examCompletionProblem);
            }
            completionSaveResult = examCompletionProblemService.saveBatch(completionProblems);
        }
        if (programmes != null && completions != null) {
            return procedureSaveResult && completionSaveResult ? ResultDataUtil.success()
                    : ResultDataUtil.error(666, "数据库插入失败");
        } else if (programmes != null) {
            return procedureSaveResult ? ResultDataUtil.success() : ResultDataUtil.error(666, "数据库插入失败");
        } else {
            return completionSaveResult ? ResultDataUtil.success() : ResultDataUtil.error(666, "数据库插入失败");
        }
    }

    /**
     * 获取数据前往考试题目列表界面
     *
     * @param examId
     * @param model
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-19 16:55
     */
    @GetMapping("/toProblemList/{examId}")
    public String toProblemListPage(@PathVariable Integer examId, Model model) {
        // 查询考试题目
        Wrapper<ExamProcedureProblem> procedureQueryWrapper = new QueryWrapper<ExamProcedureProblem>()
                .eq("exam_id", examId).select("problem_id", "score");
        Wrapper<ExamCompletionProblem> completionQueryWrapper = new QueryWrapper<ExamCompletionProblem>()
                .eq("exam_id", examId).select("problem_id", "score");
        List<ExamProcedureProblem> procedureProblem = examProcedureProblemService.list(procedureQueryWrapper);
        List<ExamCompletionProblem> completionProblem = examCompletionProblemService.list(completionQueryWrapper);
        // 抽离题目ID
        List<Integer> procedureProblemIds = procedureProblem.stream().map(ExamProcedureProblem::getProblemId).collect(Collectors.toList());
        List<Integer> completionProblemIds = completionProblem.stream().map(ExamCompletionProblem::getProblemId).collect(Collectors.toList());
        // 抽离题目ID和分数，键值对
        Map<Integer, String> procedureProblemIdMapScore = procedureProblem.stream().collect(Collectors.toMap(ExamProcedureProblem::getProblemId, ExamProcedureProblem::getScore));
        Map<Integer, String> completionProblemIdMapScore = completionProblem.stream().collect(Collectors.toMap(ExamCompletionProblem::getProblemId, ExamCompletionProblem::getScore));
        // 查询详细的题目信息
        List<ExamProcedureBank> procedureProblemDataList = procedureProblemIds == null || procedureProblemIds.size() == 0 ?
                new ArrayList<>() : examProcedureBankService.listByIds(procedureProblemIds);
        List<ExamCompletionBank> completionProblemBankDataList = completionProblemIds == null || completionProblemIds.size() == 0 ?
                new ArrayList<>() : examCompletionBankService.listByIds(completionProblemIds);
        // 封装数据
        List<ProblemListData> completionProblems = new ArrayList<>();
        List<ProblemListData> programmeProblems = new ArrayList<>();
        List<ProblemScoreListData> completionProblemScores = new ArrayList<>();
        List<ProblemScoreListData> programmeProblemScores = new ArrayList<>();
        for (int i = 0; i < procedureProblemDataList.size(); i++) {
            ExamProcedureBank examProcedureBank = procedureProblemDataList.get(i);
            ProblemListData problemListData = new ProblemListData(examProcedureBank
                    , procedureProblemIdMapScore.get(examProcedureBank.getId()));
            programmeProblems.add(problemListData);
            for (int j = 0; j < problemListData.scores.length; j++) {
                programmeProblemScores.add(new ProblemScoreListData((j + 1), problemListData));
            }
        }
        for (int i = 0; i < completionProblemBankDataList.size(); i++) {
            ExamCompletionBank examCompletionBank = completionProblemBankDataList.get(i);
            ProblemListData problemListData = new ProblemListData(examCompletionBank, completionProblemIdMapScore
                    .get(examCompletionBank.getId()));
            completionProblems.add(problemListData);
            for (int j = 0; j < problemListData.scores.length; j++) {
                completionProblemScores.add(new ProblemScoreListData((j + 1), problemListData));
            }
        }
        model.addAttribute("examId", examId);
        model.addAttribute("completionProblems", completionProblems);
        model.addAttribute("programmeProblems", programmeProblems);
        model.addAttribute("completionProblemScores", completionProblemScores);
        model.addAttribute("programmeProblemScores", programmeProblemScores);
        return "/exam/questionSet/problemlist";
    }

    /**
     * 获取编程填空题运行数据显示列表
     *
     * @param examId
     * @param model
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-30 11:52
     */
    @GetMapping("/toStateList/{examId}")
    public String toStateListPage(@PathVariable Integer examId, Model model) {
        // 查询考试对应的所有记录
        Wrapper<ExamProcedureStatus> queryWrapper = new QueryWrapper<ExamProcedureStatus>()
                .eq("exam_Id", examId);
        List<ExamProcedureStatus> examProcedureStatuses = examProcedureStatusService.list(queryWrapper);
        // 封装数据，将题号对应的多组测试数据使用键值对对应起来
        Map<Integer, List<ExamProcedureStatus>> proIdStatusMap = new HashMap<>(5);
        for (int i = 0; i < examProcedureStatuses.size(); i++) {
            ExamProcedureStatus examProcedureStatus = examProcedureStatuses.get(i);
            Integer proId = examProcedureStatus.getProblemId();
            List<ExamProcedureStatus> procedureStatusList = proIdStatusMap.get(proId);
            if (procedureStatusList == null) {
                procedureStatusList = new ArrayList<>();
            }
            procedureStatusList.add(examProcedureStatus);
            proIdStatusMap.put(proId, procedureStatusList);
        }
        // 进一步封装成页面显示数据
        List<ProblemStateListData> problemStateListData = new ArrayList<>();
        for (Integer proId : proIdStatusMap.keySet()) {
            List<ExamProcedureStatus> statuses = proIdStatusMap.get(proId);
            problemStateListData.add(new ProblemStateListData(statuses));
        }
        model.addAttribute("examId", examId);
        model.addAttribute("problemStates", problemStateListData);
        return "/exam/questionSet/submitCondition";
    }

    /**
     * 修改题目分数
     *
     * @param problemArray
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-20 14:16
     */
    @ResponseBody
    @Transactional(rollbackFor = {Exception.class})
    @PostMapping("/editProblemScore/{examId}")
    public ResultData editProblemScore(@RequestBody ProblemListData[][] problemArray
            , @PathVariable("examId") Integer examId) {
        for (int i = 0; i < problemArray[0].length; i++) {
            String[] scores = problemArray[0][i].getScores();
            StringBuffer scoreBuffer = new StringBuffer(scores[0]);
            for (int j = 1; j < scores.length; j++) {
                scoreBuffer.append("#");
                scoreBuffer.append(scores[j]);
            }
            ExamProcedureProblem procedureProblem = new ExamProcedureProblem().setScore(scoreBuffer.toString());
            Wrapper<ExamProcedureProblem> updateWrapper = new UpdateWrapper<ExamProcedureProblem>()
                    .eq("exam_id", examId).eq("problem_id", problemArray[0][i].getId());
            boolean res = examProcedureProblemService.update(procedureProblem, updateWrapper);
            if (!res) {
                return ResultDataUtil.error(666, "数据库更新失败");
            }
        }
        for (int i = 0; i < problemArray[1].length; i++) {
            String[] scores = problemArray[1][i].getScores();
            StringBuffer scoreBuffer = new StringBuffer(scores[0]);
            for (int j = 1; j < scores.length; j++) {
                scoreBuffer.append("#");
                scoreBuffer.append(scores[j]);
            }
            ExamCompletionProblem completionProblem = new ExamCompletionProblem().setScore(scoreBuffer.toString());
            Wrapper<ExamCompletionProblem> updateWrapper = new UpdateWrapper<ExamCompletionProblem>()
                    .eq("exam_id", examId).eq("problem_id", problemArray[1][i].getId());
            boolean res = examCompletionProblemService.update(completionProblem, updateWrapper);
            if (!res) {
                return ResultDataUtil.error(666, "数据库更新失败");
            }
        }
        return ResultDataUtil.success();
    }

    /**
     * 从考试中删除题目
     *
     * @param proId
     * @param examId
     * @param type
     * @return com.smxy.exam.beans.ResultData
     * @author 范颂扬
     * @date 2022-04-21 16:24
     */
    @ResponseBody
    @GetMapping("/delProblem/{proId}/{examId}")
    public ResultData delProblem(@PathVariable("proId") Integer proId, @PathVariable("examId") Integer examId
            , @PathParam("type") String type) {
        if ("completion".equals(type)) {
            Wrapper<ExamCompletionProblem> queryWrapper = new QueryWrapper<ExamCompletionProblem>()
                    .eq("exam_id", examId).eq("problem_id", proId);
            boolean res = examCompletionProblemService.remove(queryWrapper);
            if (!res) {
                return ResultDataUtil.error(666, "数据库删除失败");
            }
        }
        if ("programme".equals(type)) {
            Wrapper<ExamProcedureProblem> queryWrapper = new QueryWrapper<ExamProcedureProblem>()
                    .eq("exam_id", examId).eq("problem_id", proId);
            boolean res = examProcedureProblemService.remove(queryWrapper);
            if (!res) {
                return ResultDataUtil.error(666, "数据库删除失败");
            }
        }
        return ResultDataUtil.success();
    }

    /**
     * 题目列表显示
     *
     * @author 范颂扬
     * @create 2022-04-19 16:00
     */
    @Data
    @Accessors(chain = true)
    public static class ProblemListData {
        /**
         * 题号
         */
        private Integer id;

        /**
         * 标题
         */
        private String title;

        /**
         * 题干
         */
        private String content;

        /**
         * 填空代码
         */
        private String code;

        /**
         * 参考答案，每个空用#隔开
         */
        private String answer;

        /**
         * 得分，每个空用#隔开
         */
        private String score;

        /**
         * 编辑器
         */
        private String compile;

        /**
         * 创建人
         */
        private String adminid;

        /**
         * 创建时间
         */
        private LocalDateTime time;

        /**
         * 具体分数
         */
        private String[] scores;

        /**
         * 总分
         */
        private String totalPoints;

        /**
         * 空数
         */
        private Integer blankSum;

        public ProblemListData() {
            super();
        }

        public ProblemListData(ExamProcedureBank examProcedureBank, String score) {
            this.time = examProcedureBank.getTime();
            this.adminid = examProcedureBank.getAdminid();
            this.compile = examProcedureBank.getCompile();
            this.score = score;
            this.answer = examProcedureBank.getAnswer();
            this.code = examProcedureBank.getCode();
            this.content = examProcedureBank.getContent();
            this.title = examProcedureBank.getTitle();
            this.id = examProcedureBank.getId();
            this.calculateScore();
        }

        public ProblemListData(ExamCompletionBank examCompletionBank, String score) {
            this.time = examCompletionBank.getTime();
            this.adminid = examCompletionBank.getAdminid();
            this.score = score;
            this.answer = examCompletionBank.getAnswer();
            this.content = examCompletionBank.getContent();
            this.title = examCompletionBank.getTitle();
            this.id = examCompletionBank.getId();
            this.calculateScore();
        }

        /**
         * 计算分数
         *
         * @return void
         * @author 范颂扬
         * @date 2022-04-19 16:11
         */
        public void calculateScore() {
            this.scores = this.score.split("#");
            this.blankSum = this.scores.length;
            double totalPoints = 0d;
            for (int i = 0; i < scores.length; i++) {
                totalPoints += Double.parseDouble(scores[i]);
            }
            this.totalPoints = new BigDecimal(String.valueOf(totalPoints))
                    .stripTrailingZeros().toPlainString();
        }

    }

    /**
     * 题目分数显示
     *
     * @author 范颂扬
     * @create 2022-04-19 16:39
     */
    @Data
    @Accessors(chain = true)
    private class ProblemScoreListData {
        /**
         * 题号
         */
        private Integer id;

        /**
         * 填空号
         */
        private Integer blankId;

        /**
         * 标题
         */
        private String title;

        /**
         * 具体分数
         */
        private String score;

        /**
         * 总分
         */
        private String totalPoints;

        /**
         * 空数
         */
        private Integer blankSum;

        public ProblemScoreListData(Integer blankId, ProblemListData problemListData) {
            this.blankId = blankId;
            this.blankSum = problemListData.getBlankSum();
            this.totalPoints = problemListData.getTotalPoints();
            this.score = problemListData.getScore().split("#")[blankId - 1];
            this.title = problemListData.getTitle();
            this.id = problemListData.getId();
        }

        public ProblemScoreListData(Integer blankId, ExamProcedureBank examProcedureBank) {
            this.blankId = blankId;
            this.title = examProcedureBank.getTitle();
            this.id = examProcedureBank.getId();
            this.score = examProcedureBank.getScore().split("#")[blankId - 1];
            this.calculateScore(examProcedureBank.getScore());
        }

        public ProblemScoreListData(Integer blankId, ExamCompletionBank examCompletionBank) {
            this.blankId = blankId;
            this.title = examCompletionBank.getTitle();
            this.id = examCompletionBank.getId();
            this.score = examCompletionBank.getScore().split("#")[blankId - 1];
            this.calculateScore(examCompletionBank.getScore());
        }

        /**
         * 计算分数
         *
         * @return void
         * @author 范颂扬
         * @date 2022-04-19 16:11
         */
        public void calculateScore(String score) {
            String[] scores = score.split("#");
            this.blankSum = scores.length;
            double totalPoints = 0d;
            for (int i = 0; i < scores.length; i++) {
                totalPoints += Double.parseDouble(scores[i]);
            }
            this.totalPoints = new BigDecimal(String.valueOf(totalPoints))
                    .stripTrailingZeros().toPlainString();
        }
    }

    /**
     * 运行状态显示
     *
     * @author 范颂扬
     * @create 2022-04-30 11:59
     */
    @Data
    @Accessors(chain = true)
    private class ProblemStateListData {

        /**
         * 提交时间
         */
        private LocalDateTime submitTime;

        /**
         * 分数
         */
        private String totalScore;

        /**
         * 考试中的题号
         */
        private Integer proNum;


        /**
         * 用户 ID
         */
        private String userId;

        /**
         * 测试点集合
         */
        private List<TestPoint> testPoints;

        @Data
        @Accessors(chain = true)
        class TestPoint {

            private Integer castId;

            private String state;

            private String score;

            private Integer memory;

            private Integer time;

            private String compiler;

        }

        public ProblemStateListData() {
            super();
        }

        public ProblemStateListData(List<ExamProcedureStatus> examProcedureStatusList) {
            Float totalScore = 0f;
            this.testPoints = new ArrayList<>();
            for (int i = 0; i < examProcedureStatusList.size(); i++) {
                ExamProcedureStatus examProcedureStatus = examProcedureStatusList.get(i);
                if (i == 0) {
                    this.submitTime = examProcedureStatus.getSubmitTime();
                    this.proNum = examProcedureStatus.getProblemId();
                    this.userId = examProcedureStatus.getUserId();
                }
                totalScore += examProcedureStatus.getScore();
                TestPoint testPoint = new TestPoint().setState(ExamResultUtil.ProgrammeResult
                        .getNameByCode(examProcedureStatus.getResult())).setCastId(examProcedureStatus.getCaseTestDataId())
                        .setTime(examProcedureStatus.getTime()).setMemory(examProcedureStatus.getMemory())
                        .setScore(StringUtil.getNumberNoInvalidZero(examProcedureStatus.getScore()))
                        .setCompiler(examProcedureStatus.getCompiler());
                this.testPoints.add(testPoint);
            }
            this.totalScore = StringUtil.getNumberNoInvalidZero(totalScore);
        }
    }

}
