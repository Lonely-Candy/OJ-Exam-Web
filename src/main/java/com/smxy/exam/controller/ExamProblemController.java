package com.smxy.exam.controller;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smxy.exam.beans.*;
import com.smxy.exam.processing.ProblemStateListData;
import com.smxy.exam.processing.ResultData;
import com.smxy.exam.service.*;
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

    private IExamRecordService examRecordService;

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
            , IExamCompletionStatusService examCompletionStatusService, IExamRecordService examRecordService) {
        this.examRecordService = examRecordService;
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
     * @return com.smxy.exam.processing.ResultData
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
     * @return com.smxy.exam.processing.ResultData
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
                    .collect(HashMap::new
                            , (map, examProcedureBank) -> map.put(examProcedureBank.getId(), examProcedureBank.getScore())
                            , HashMap::putAll);
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
        return "exam/questionSet/problemList";
    }

    /**
     * 获取编程填空题运行数据前往考试状态显示列表
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
        Wrapper<ExamProcedureStatus> queryWrapper = new QueryWrapper<ExamProcedureStatus>().eq("exam_Id", examId);
        List<ExamProcedureStatus> examProcedureStatuses = examProcedureStatusService.list(queryWrapper);
        List<ProblemStateListData> problemStateListDataList = ProblemStateListData.getProblemStateListDataNoGroup(examProcedureStatuses);
        problemStateListDataList.sort((o1, o2) -> {
            return o2.getSubmitTime().compareTo(o1.getSubmitTime());
        });
        model.addAttribute("examId", examId);
        model.addAttribute("problemStates", problemStateListDataList);
        return "exam/questionSet/submitCondition";
    }

    /**
     * 获取学生考试记录前往显示界面
     *
     * @param examId
     * @param model
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-05-01 19:38
     */
    @GetMapping("/toStudentList/{examId}")
    public String toStudentListPage(@PathVariable Integer examId, Model model) {
        Wrapper<ExamRecord> queryWrapper = new QueryWrapper<ExamRecord>().eq("exam_id", examId);
        List<ExamRecord> examRecords = examRecordService.list(queryWrapper);
        model.addAttribute("examId", examId);
        model.addAttribute("examRecords", examRecords);
        return "exam/questionSet/studentList";
    }

    /**
     * 获取排名信息前往显示页面
     *
     * @param examId
     * @param model
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-05-01 20:17
     */
    @GetMapping("/toRanking/{examId}")
    public String toRankingPage(@PathVariable Integer examId, Model model) {
        // 1. 获取本场考试记录
        Wrapper<ExamRecord> recordQueryWrapper = new QueryWrapper<ExamRecord>().eq("exam_id", examId);
        List<ExamRecord> examRecords = examRecordService.list(recordQueryWrapper);
        // 1.1 获取键值对，key:学号 value:姓名
        Map<String, String> userIdUserNameMap = examRecords.stream().collect(Collectors.toMap(ExamRecord::getUserId, ExamRecord::getUserName));
        // 2. 获取本场考试的所有提交记录(填空题)
        Wrapper<ExamCompletionStatus> completionStatusQueryWrapper = new QueryWrapper<ExamCompletionStatus>().eq("exam_id", examId);
        List<ExamCompletionStatus> completionStatuses = examCompletionStatusService.list(completionStatusQueryWrapper);
        // 2.1 获取键值对，key:学号 value:总分(填空题)
        // 2.2 获取键值对，key:学号 value:记录(填空题)
        Map<String, Float> userIdTotalCompletionMap = new HashMap<>(10);
        Map<String, List<ExamCompletionStatus>> userIdCompletionStatusMap = new HashMap<>(10);
        for (ExamCompletionStatus status : completionStatuses) {
            String userId = status.getUserId();
            // 1) 计算总分
            Float score = userIdTotalCompletionMap.get(userId);
            if (score == null) {
                score = status.getScore();
            } else {
                score += status.getScore();
            }
            userIdTotalCompletionMap.put(userId, score);
            // 2) 归类记录
            List<ExamCompletionStatus> completionStatusList = userIdCompletionStatusMap.get(userId);
            if (completionStatusList == null) {
                completionStatusList = new ArrayList<>();
            }
            completionStatusList.add(status);
            userIdCompletionStatusMap.put(userId, completionStatusList);
        }
        // 3. 获取本场考试的所有提交记录(编程填空题)
        Wrapper<ExamProcedureStatus> procedureStatusQueryWrapper = new QueryWrapper<ExamProcedureStatus>()
                .eq("exam_id", examId);
        List<ExamProcedureStatus> procedureStatuses = examProcedureStatusService.list(procedureStatusQueryWrapper);
        // 数据处理
        procedureStatuses = implementDataProcessing(procedureStatuses);
        // 3.1 获取键值对，key:学号 value:记录(编程填空题)
        Map<String, List<ExamProcedureStatus>> userIdProcedureStatusMap = new HashMap<>(10);
        for (ExamProcedureStatus status : procedureStatuses) {
            String userId = status.getUserId();
            // 1) 归类记录
            List<ExamProcedureStatus> procedureStatusList = userIdProcedureStatusMap.get(userId);
            if (procedureStatusList == null) {
                procedureStatusList = new ArrayList<>();
            }
            procedureStatusList.add(status);
            userIdProcedureStatusMap.put(userId, procedureStatusList);
        }
        // 4. 整合所有的数据
        List<RankingListData> rankingListDataList = new ArrayList<>();
        for (String userId : userIdUserNameMap.keySet()) {
            String userName = userIdUserNameMap.get(userId);
            // 获取对应的记录列表
            List<ExamCompletionStatus> completionStatusList = userIdCompletionStatusMap.get(userId);
            List<ExamProcedureStatus> procedureStatusList = userIdProcedureStatusMap.get(userId);
            // 获取对应的部分分数
            Float procedureTotalScore = 0f;
            Float completionTotalScore = userIdTotalCompletionMap.get(userId);
            // 计算总分
            completionTotalScore = completionTotalScore == null ? 0f : completionTotalScore;
            // 1) 设置基础字段
            RankingListData rankingListData = new RankingListData()
                    .setUserId(userId).setUserName(userName)
                    .setCompletionTotalScore(StringUtil.getNumberNoInvalidZero(completionTotalScore));
            List<CompletionShowData> completionShowDataList = new ArrayList<>();
            if (completionStatusList != null) {
                for (ExamCompletionStatus examCompletionStatus : completionStatusList) {
                    completionShowDataList.add(new CompletionShowData(examCompletionStatus));
                }
            }
            List<ProblemStateListData> problemStateListData = ProblemStateListData.getProblemStateListData(procedureStatusList);
            // 2) 获取对应的题目分数
            for (int i = 0; i < problemStateListData.size(); i++) {
                ProblemStateListData stateListData = problemStateListData.get(i);
                Integer stateListDataExamId = stateListData.getExamId();
                Integer proId = stateListData.getProId();
                Wrapper<ExamProcedureProblem> wrapper = new QueryWrapper<ExamProcedureProblem>()
                        .eq("exam_id", stateListDataExamId).eq("problem_id", proId);
                ExamProcedureProblem procedureProblem = examProcedureProblemService.getOne(wrapper);
                String score = procedureProblem.getScore();
                if (score == null || score.equals("")) {
                    continue;
                }
                String[] scores = score.split("#");
                Map<Integer, String> castIdScoreMap = new HashMap<>();
                for (int j = 0; j < scores.length; j++) {
                    castIdScoreMap.put(j, scores[j]);
                }
                stateListData.setCastIdScoreMap(castIdScoreMap);
                procedureTotalScore += stateListData.getTotalScoreFloat();
            }
            Float totalScore = procedureTotalScore + completionTotalScore;
            rankingListData.setProgrammeTotalScore(StringUtil.getNumberNoInvalidZero(procedureTotalScore))
                    .setTotalScore(StringUtil.getNumberNoInvalidZero(totalScore));
            // 3) 设置对应的记录
            rankingListDataList.add(rankingListData.setCompletionShowDataList(completionShowDataList)
                    .setProgrammeShowDataList(problemStateListData));
        }
        Collections.sort(rankingListDataList);
        model.addAttribute("examId", examId);
        model.addAttribute("rankingListData", rankingListDataList);
        return "exam/questionSet/ranking";
    }

    /**
     * 进行所有的编程填空题提交记录数据的处理
     *
     * @param procedureStatuses
     * @return java.util.List<com.smxy.exam.beans.ExamProcedureStatus>
     * @author 范颂扬
     * @date 2022-05-16 19:56
     */
    private List<ExamProcedureStatus> implementDataProcessing(List<ExamProcedureStatus> procedureStatuses) {
        if (procedureStatuses == null) {
            return null;
        }
        List<ExamProcedureStatus> procedureStatusList = new ArrayList<>();
        // 1. 获取键值对, Map<学号, List<记录>>
        Map<String, List<ExamProcedureStatus>> studentIdStatusMap = new HashMap<>();
        for (int i = 0; i < procedureStatuses.size(); i++) {
            ExamProcedureStatus status = procedureStatuses.get(i);
            String userId = status.getUserId();
            List<ExamProcedureStatus> statusValue = studentIdStatusMap.get(userId);
            if (statusValue == null) {
                statusValue = new ArrayList<>();
            }
            statusValue.add(status);
            studentIdStatusMap.put(userId, statusValue);
        }
        // 2. 处理每个学号对应的记录
        for (String studentId : studentIdStatusMap.keySet()) {
            List<ExamProcedureStatus> studentIdStatusMapValue = studentIdStatusMap.get(studentId);
            // 2.1 将每个学生对应的记录进行分类
            // 学生提交的多次记录，一题对应多次的提交记录
            Map<Integer, List<ExamProcedureStatus>> proIdStatusMap = new HashMap<>(5);
            for (int i = 0; i < studentIdStatusMapValue.size(); i++) {
                ExamProcedureStatus status = studentIdStatusMapValue.get(i);
                Integer problemId = status.getProblemId();
                List<ExamProcedureStatus> proIdStatusMapValue = proIdStatusMap.get(problemId);
                if (proIdStatusMapValue == null) {
                    proIdStatusMapValue = new ArrayList<>();
                }
                proIdStatusMapValue.add(status);
                proIdStatusMap.put(problemId, proIdStatusMapValue);
            }
            // 2.2 将每个学生对应的每个题目的多条记录进行分类
            for (Integer proId : proIdStatusMap.keySet()) {
                List<ExamProcedureStatus> statusList = proIdStatusMap.get(proId);
                // 2.2.1 按时间进行多次提交的分割，分割后，一个时间就对应的一次提交的所有运行记录
                Map<String, List<ExamProcedureStatus>> submitTimeStatusListMap = new TreeMap<>();
                for (int i = 0; i < statusList.size(); i++) {
                    ExamProcedureStatus status = statusList.get(i);
                    String submitTime = status.getSubmitTime().toString();
                    List<ExamProcedureStatus> submitTimeStatusListMapValue = submitTimeStatusListMap.get(submitTime);
                    if (submitTimeStatusListMapValue == null) {
                        submitTimeStatusListMapValue = new ArrayList<>();
                    }
                    submitTimeStatusListMapValue.add(status);
                    submitTimeStatusListMap.put(submitTime, submitTimeStatusListMapValue);
                }
                // 2.2.2 对每次的记录进行判断，选出得分最高的，保存
                String submitTimeKey = "";
                Float maxScore = Float.MIN_VALUE;
                for (String submitTime : submitTimeStatusListMap.keySet()) {
                    List<ExamProcedureStatus> statuses = submitTimeStatusListMap.get(submitTime);
                    boolean isChange = ProblemStateListData.isChangeScore(statuses);
                    Float countScore = 0f;
                    for (int i = 0; i < statuses.size(); i++) {
                        ExamProcedureStatus status = statuses.get(i);
                        countScore += status.getScore();
                    }
                    if (isChange || maxScore.compareTo(countScore) != 1) {
                        maxScore = countScore;
                        submitTimeKey = submitTime;
                        if (isChange) {
                            break;
                        }
                    }
                }
                List<ExamProcedureStatus> waitSaveStatus = submitTimeStatusListMap.get(submitTimeKey);
                if (waitSaveStatus != null) {
                    procedureStatusList.addAll(waitSaveStatus);
                }
            }
        }
        return procedureStatusList;
    }

    /**
     * 修改题目分数
     *
     * @param problemArray
     * @return com.smxy.exam.processing.ResultData
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
     * @return com.smxy.exam.processing.ResultData
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
     * 获取对应学生详细的填空题做题记录
     *
     * @param id
     * @return com.smxy.exam.processing.ResultData
     * @author 范颂扬
     * @date 2022-05-10 16:57
     */
    @ResponseBody
    @GetMapping("/getCompletionDetail/{id}")
    public ResultData getCompletionDetailToRanking(@PathVariable Integer id) {
        // 1. 查询记录
        ExamCompletionStatus completionStatus = examCompletionStatusService.getById(id);
        if (completionStatus == null) {
            return ResultDataUtil.error(666, "查无对应的记录");
        }
        Integer statusId = completionStatus.getId();
        Integer examId = completionStatus.getExamId();
        String problemId = completionStatus.getProblemId();
        String source = completionStatus.getSource();
        Integer problemNum = completionStatus.getProblemNum();
        // 2. 查询对应题库的题目
        ExamCompletionBank completionBank = examCompletionBankService.getById(problemId);
        // 3. 查询对应的分数
        Wrapper<ExamCompletionProblem> queryWrapper = new QueryWrapper<ExamCompletionProblem>()
                .eq("exam_id", examId).eq("problem_id", problemId);
        ExamCompletionProblem completionProblem = examCompletionProblemService.getOne(queryWrapper);
        if (completionProblem == null) {
            return ResultDataUtil.error(666, "查无对应的题目分数");
        }
        String score = completionProblem.getScore();
        // 4. 封装正确答案(正确答案要先封装，否则会覆盖正确答案)
        ProblemBankController.ProblemShowData problemCorrectAnswer = new ProblemBankController.ProblemShowData(
                completionBank.setScore(score));
        // 5. 封装学生答案
        ProblemBankController.ProblemShowData problemStudentAnswer = new ProblemBankController.ProblemShowData(
                completionBank.setAnswer(source).setScore(score).setId(statusId)).setProNum(problemNum);
        // 6. 正确答案转换只显示答案
        problemCorrectAnswer.replaceContextOnlyAnswer();
        Map<String, Object> map = new HashMap<>();
        map.put("problemShowData", new ProblemBankController.ProblemShowData[]{problemStudentAnswer, problemCorrectAnswer});
        map.put("studentScore", completionStatus.getScore());
        return ResultDataUtil.success(map);
    }

    /**
     * 修改学生填空题分数
     *
     * @param id
     * @param score
     * @return com.smxy.exam.processing.ResultData
     * @author 范颂扬
     * @date 2022-05-13 21:34
     */
    @ResponseBody
    @GetMapping("/changeCompletionScore/{id}")
    public ResultData changeStudentCompletionScore(@PathVariable Integer id, @PathParam("score") String score) {
        Wrapper<ExamCompletionStatus> updateWrapper = new UpdateWrapper<ExamCompletionStatus>()
                .eq("id", id).set("score", Float.parseFloat(score)).set("is_change", "1");
        boolean res = examCompletionStatusService.update(updateWrapper);
        if (res) {
            return ResultDataUtil.success();
        }
        return ResultDataUtil.error(666, "修改失败");
    }

    /**
     * 修改学生编程填空题分数
     *
     * @param ids
     * @param scores
     * @return com.smxy.exam.processing.ResultData
     * @author 范颂扬
     * @date 2022-05-14 16:47
     */
    @ResponseBody
    @PostMapping("/changeProgrammeScore")
    @Transactional(rollbackFor = {Exception.class})
    public ResultData changeStudentProgrammeScore(@RequestParam("ids[]") Integer[] ids, @RequestParam("scores[]") String[] scores) {
        if (ids == null || scores == null) {
            return ResultDataUtil.error(111, "参数为空");
        }
        List<Integer> idList = Arrays.asList(ids);
        List<ExamProcedureStatus> procedureStatusList = examProcedureStatusService.listByIds(idList);
        Map<Integer, ExamProcedureStatus> procedureIdMap = procedureStatusList.stream().collect(HashMap::new
                , (map, examProcedureStatus) -> map.put(examProcedureStatus.getId(), examProcedureStatus)
                , HashMap::putAll);
        List<Integer> changeIndex = new ArrayList<>();
        Map<Integer, Float> testIdScoreMap = new HashMap<>();
        for (int i = 0; i < ids.length; i++) {
            ExamProcedureStatus status = procedureIdMap.get(ids[i]);
            Float score = status.getScore();
            Integer caseTestDataId = status.getCaseTestDataId();
            if (!score.equals(new Float(scores[i]))) {
                float v = Float.parseFloat(scores[i]);
                status.setScore(v);
                Wrapper<ExamProcedureStatus> updateWrapper = new UpdateWrapper<ExamProcedureStatus>()
                        .eq("id", status.getId()).set("score", v).set("is_change", "1");
                examProcedureStatusService.update(updateWrapper);
                changeIndex.add(i);
            }
            Float aFloat = testIdScoreMap.get(caseTestDataId);
            if (aFloat == null) {
                aFloat = status.getScore();
            }
            testIdScoreMap.put(caseTestDataId, Float.max(aFloat, status.getScore()));
        }
        Map<String, Object> itemMap = new HashMap<>();
        Float changeScore = 0f;
        for (Integer testId : testIdScoreMap.keySet()) {
            changeScore += testIdScoreMap.get(testId);
        }
        itemMap.put("changeIndex", changeIndex);
        itemMap.put("changeScore", changeScore);
        return ResultDataUtil.success(itemMap);
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

    @Data
    @Accessors(chain = true)
    private class CompletionShowData {

        private Integer id;

        private Integer proNum;

        private String score;

        private String submitTime;

        private String isChange;

        private Integer result;

        public CompletionShowData() {
            super();
        }

        public CompletionShowData(ExamCompletionStatus completionStatus) {
            this.id = completionStatus.getId();
            this.proNum = completionStatus.getProblemNum();
            this.score = StringUtil.getNumberNoInvalidZero(completionStatus.getScore());
            this.submitTime = completionStatus.getSubmitTime().toString().replace('T', ' ');
            this.isChange = completionStatus.getIsChange();
            this.result = completionStatus.getResult();
        }

    }

    /**
     * 学生成绩排名显示
     *
     * @author 范颂扬
     * @create 2022-05-01 19:46
     */
    @Data
    @Accessors(chain = true)
    public class RankingListData implements Comparable<RankingListData> {

        private String userId;

        private String userName;

        private String completionTotalScore;

        private String programmeTotalScore;

        private String totalScore;

        private List<CompletionShowData> completionShowDataList;

        private List<ProblemStateListData> programmeShowDataList;

        @Override
        public int compareTo(RankingListData o) {
            return Float.compare(Float.valueOf(o.getTotalScore()), Float.valueOf(this.getTotalScore()));
        }

    }

}
