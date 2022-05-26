package com.smxy.exam.processing;

import com.smxy.exam.beans.ExamProcedureStatus;
import com.smxy.exam.myenum.ProgrammeResultEnum;
import com.smxy.exam.util.StringUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 运行状态显示
 *
 * @author 范颂扬
 * @create 2022-04-30 11:59
 */
@Data
@Accessors(chain = true)
public class ProblemStateListData {

    /**
     * 考试ID
     */
    private Integer examId;

    /**
     * 运行结果
     */
    private String state;

    /**
     * 题目ID
     */
    private Integer proId;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 分数(字符串，方便显示)
     */
    private String totalScore;

    /**
     * 数值分数
     */
    private Float totalScoreFloat;

    /**
     * 考试中的题号
     */
    private Integer proNum;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 编译器
     */
    private String compiler;

    /**
     * 源码
     */
    private String source;

    /**
     * 测试点集合
     */
    private List<TestPoint> testPoints;

    /**
     * 测试点对应的分数
     */
    private Map<Integer, String> castIdScoreMap;

    @Data
    @Accessors(chain = true)
    private class TestPoint {

        private Integer id;

        private Integer castId;

        private String state;

        private String score;

        private Integer memory;

        private Integer time;

        private String compiler;

        private String isChange;

        public TestPoint() {
            super();
        }

        public TestPoint(ExamProcedureStatus procedureStatus) {
            this.id = procedureStatus.getId();
            this.state = ProgrammeResultEnum.getNameByCode(procedureStatus.getResult());
            this.castId = procedureStatus.getCaseTestDataId();
            this.time = procedureStatus.getTime();
            this.memory = procedureStatus.getMemory();
            this.score = StringUtil.getNumberNoInvalidZero(procedureStatus.getScore());
            this.compiler = procedureStatus.getCompiler();
            this.isChange = procedureStatus.getIsChange();
        }

    }

    public ProblemStateListData() {
        super();
    }

    public ProblemStateListData(ExamProcedureStatus procedureStatus) {
        this.examId = procedureStatus.getExamId();
        this.proId = procedureStatus.getProblemId();
        this.submitTime = procedureStatus.getSubmitTime();
        this.proNum = procedureStatus.getProblemNum();
        this.userId = procedureStatus.getUserId();
        this.totalScoreFloat = 0f;
        this.testPoints = new ArrayList<>();
        this.compiler = procedureStatus.getCompiler();
        this.source = procedureStatus.getSource();
    }

    public TestPoint getTestPoint(ExamProcedureStatus procedureStatus) {
        return new TestPoint(procedureStatus);
    }

    public void addTotalScore(Float totalScoreFloat) {
        this.totalScoreFloat += totalScoreFloat;
    }

    /**
     * 根据所有的测试点判断当前题目的状态
     *
     * @param
     * @return void
     * @author 范颂扬
     * @date 2022-05-26 21:32
     */
    public void setStateByTestPoints() {
        if (testPoints == null || testPoints.size() == 0) {
            return;
        }
        if (testPoints.size() == 1) {
            this.state = testPoints.get(0).getState();
            return;
        }
        // 统计结果数
        Map<String, Integer> map = new HashMap<>(10);
        for (int i = 0; i < testPoints.size(); i++) {
            TestPoint testPoint = testPoints.get(i);
            Integer count = map.get(testPoint.state);
            if (count == null) {
                count = 0;
            }
            map.put(testPoint.state, count++);
        }
        // 获取当前结果
        Set<String> resultSet = map.keySet();
        // 只有一个结果
        if (resultSet.size() == 1) {
            this.state = resultSet.iterator().next();
            return;
        }
        // 出现系统错误
        if (resultSet.contains(ProgrammeResultEnum.SystemError.getName())) {
            this.state = ProgrammeResultEnum.SystemError.getName();
        }
        // 正在判题
        if (resultSet.contains(ProgrammeResultEnum.NoResult.getName())) {
            this.state = ProgrammeResultEnum.NoResult.getName();
        }
        // 部分正确
        if (resultSet.contains(ProgrammeResultEnum.Accepted.getName())) {
            this.state = ProgrammeResultEnum.Accepted.getName();
        } else {
            this.state = ProgrammeResultEnum.MoreWrong.getName();
        }
    }

    /**
     * 不对其进行组合，直接一条一条记录显示
     *
     * @param procedureStatuses
     * @return java.util.List<com.smxy.exam.processing.ProblemStateListData>
     * @author 范颂扬
     * @date 2022-05-16 22:13
     */
    public static List<ProblemStateListData> getProblemStateListDataNoGroup(List<ExamProcedureStatus> procedureStatuses) {
        if (procedureStatuses == null) {
            return null;
        }
        List<ProblemStateListData> problemStateListDataList = new ArrayList<>();
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
                // 2.2.1 按时间进行多次提交的分割，分割后，一个时间就对应的一次提交的所有运行记录（此时得到的就是一条条的提交对应的运行记录集）
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
                // 2.2.2 整合
                for (String submitTime : submitTimeStatusListMap.keySet()) {
                    List<ExamProcedureStatus> statuses = submitTimeStatusListMap.get(submitTime);
                    ProblemStateListData problemStateListData = new ProblemStateListData(statuses.get(0));
                    Map<Integer, Float> castIdScoreMap = new HashMap<>(10);
                    // 过滤同一个测试点的分数
                    for (int i = 0; i < statuses.size(); i++) {
                        ExamProcedureStatus status = statuses.get(i);
                        Integer caseTestDataId = status.getCaseTestDataId();
                        Float score = status.getScore();
                        Float oldScore = castIdScoreMap.get(caseTestDataId);
                        if (oldScore == null || oldScore.compareTo(score) < 0) {
                            castIdScoreMap.put(caseTestDataId, score);
                        }
                        ProblemStateListData.TestPoint testPoint = problemStateListData.getTestPoint(status);
                        problemStateListData.getTestPoints().add(testPoint);
                    }
                    // 计算总分
                    for (Integer castId : castIdScoreMap.keySet()) {
                        Float score = castIdScoreMap.get(castId);
                        problemStateListData.addTotalScore(score);
                    }
                    Float totalScoreFloat = problemStateListData.getTotalScoreFloat();
                    problemStateListData.setTotalScore(StringUtil.getNumberNoInvalidZero(totalScoreFloat));
                    // 设置总状态
                    problemStateListData.setStateByTestPoints();
                    problemStateListDataList.add(problemStateListData);
                }

            }
        }
        return problemStateListDataList;
    }

    /**
     * 该此提交记录中，是否有修改过
     *
     * @param statuses
     * @return boolean
     * @author 范颂扬
     * @date 2022-05-17 16:35
     */
    public static boolean isChangeScore(List<ExamProcedureStatus> statuses) {
        for (int i = 0; i < statuses.size(); i++) {
            ExamProcedureStatus status = statuses.get(i);
            String isChange = status.getIsChange();
            if ("1".equals(isChange)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理获取到的编程填空题的记录
     *
     * @param examProcedureStatuses
     * @return java.util.List<com.smxy.exam.controller.ExamProblemController.ProblemStateListData>
     * @author 范颂扬
     * @date 2022-05-10 17:50
     */
    public static List<ProblemStateListData> getProblemStateListData(List<ExamProcedureStatus> examProcedureStatuses) {
        if (examProcedureStatuses == null) {
            return new ArrayList<>();
        }
        // 封装数据，将题号对应的多组测试数据使用键值对对应起来
        // Map<学号, Map<题目ID, Map<测试数据ID, List<状态>>>>
        // 注意：这里状态使用集合，是因为，可能存在测试数据使用不同的编译器得出的分数，选出来的最高分都是相同的。
        Map<String, Map<Integer, Map<Integer, List<ExamProcedureStatus>>>> userIdStatusMap = new HashMap<>(5);
        for (int i = 0; i < examProcedureStatuses.size(); i++) {
            // 获取待处理状态
            ExamProcedureStatus pendingStatus = examProcedureStatuses.get(i);
            // 第一层----处理学生学号
            String userId = pendingStatus.getUserId();
            // 查看学号是否有对应的值
            // 学号----Map<题目ID, Map<测试数据ID, List<状态>>>
            Map<Integer, Map<Integer, List<ExamProcedureStatus>>> proIdStatusMap = userIdStatusMap.get(userId);
            if (proIdStatusMap == null) {
                // 学号没有对应的值，进行初始化
                // List<状态>
                List<ExamProcedureStatus> statuses = new ArrayList<>();
                statuses.add(pendingStatus);
                // 测试数据ID
                Integer caseId = pendingStatus.getCaseTestDataId();
                // Map<测试数据ID, List<状态>>
                Map<Integer, List<ExamProcedureStatus>> castIdStatusMap = new HashMap<>();
                castIdStatusMap.put(caseId, statuses);
                // 题目ID
                Integer proId = pendingStatus.getProblemId();
                // Map<题目ID, Map<测试数据ID, List<状态>>>
                proIdStatusMap = new HashMap<>(5);
                proIdStatusMap.put(proId, castIdStatusMap);
            } else {
                // 学号有对应的值，判断题目ID
                // 第二层----处理题目ID
                Integer proId = pendingStatus.getProblemId();
                // 查看题目ID是否有对应的值
                // 题目ID----Map<测试数据ID, List<状态>>
                Map<Integer, List<ExamProcedureStatus>> caseIdStatusMap = proIdStatusMap.get(proId);
                if (caseIdStatusMap == null) {
                    // 题目ID没有对应的值，进行初始化
                    // List<状态>
                    List<ExamProcedureStatus> statuses = new ArrayList<>();
                    statuses.add(pendingStatus);
                    // 测试数据ID
                    Integer castId = pendingStatus.getCaseTestDataId();
                    // Map<测试数据ID, List<状态>>
                    caseIdStatusMap = new HashMap<>();
                    caseIdStatusMap.put(castId, statuses);
                } else {
                    // 题目ID有对应的值，判断测试数据ID
                    // 第三层----处理测试数据ID
                    Integer caseId = pendingStatus.getCaseTestDataId();
                    // 测试数据ID----List<状态>
                    List<ExamProcedureStatus> statusList = caseIdStatusMap.get(caseId);
                    if (statusList == null) {
                        // 测试数据ID没有对应的值，进行初始化
                        statusList = new ArrayList<>();
                        statusList.add(pendingStatus);
                    } else {
                        // 测试数据ID有对应的值，判断状态是否需要保存
                        // 第四成----处理状态
                        ExamProcedureStatus status = statusList.get(0);
                        // 获分数进行判断是否带处理
                        Float oldScore = status.getScore();
                        Float pendingScore = pendingStatus.getScore();
                        if (oldScore.equals(pendingScore)) {
                            // 分数相同，加入状态集中
                            statusList.add(pendingStatus);
                        } else if (oldScore < pendingScore) {
                            // 待判断状态分数更高记录显示高分
                            statusList.clear();
                            statusList.add(pendingStatus);
                        }
                    }
                    // 第三层----结束
                    // 更新 Map<测试数据ID, List<状态>>
                    caseIdStatusMap.put(caseId, statusList);
                }
                // 第二层----结束
                // 更新 Map<题目ID, Map<测试数据ID, List<状态>>>
                proIdStatusMap.put(proId, caseIdStatusMap);
            }
            // 第一层----结束
            // 更新 Map<学号, Map<题目ID, Map<测试数据ID, List<状态>>>>
            userIdStatusMap.put(userId, proIdStatusMap);
        }
        List<ProblemStateListData> problemStateListDataList = getProblemStateListData(userIdStatusMap);
        return problemStateListDataList;
    }

    /**
     * 封装代码运行状态显示界面数据
     *
     * @param userIdStatusMap
     * @return java.util.List<com.smxy.exam.controller.ExamProblemController.ProblemStateListData>
     * @author 范颂扬
     * @date 2022-05-01 17:26
     */
    public static List<ProblemStateListData> getProblemStateListData(Map<String, Map<Integer, Map<Integer, List<ExamProcedureStatus>>>> userIdStatusMap) {
        // 进一步封装成页面显示数据
        List<ProblemStateListData> problemStateListDataList = new ArrayList<>();
        for (String userId : userIdStatusMap.keySet()) {
            Map<Integer, Map<Integer, List<ExamProcedureStatus>>> proIdStatusMap = userIdStatusMap.get(userId);
            for (Integer proId : proIdStatusMap.keySet()) {
                Map<Integer, List<ExamProcedureStatus>> castIdStatusMap = proIdStatusMap.get(proId);
                ProblemStateListData problemStateListData = null;
                for (Integer castId : castIdStatusMap.keySet()) {
                    List<ExamProcedureStatus> statusList = castIdStatusMap.get(castId);
                    // 是否将当前记录加入分数中
                    // 注意：该集合中都是对应同一个测试数据下的分数，分数只需要添加一次即可
                    boolean isAddScore = true;
                    for (ExamProcedureStatus status : statusList) {
                        if (problemStateListData == null) {
                            problemStateListData = new ProblemStateListData(status);
                        }
                        if (isAddScore) {
                            isAddScore = false;
                            Float score = status.getScore();
                            problemStateListData.addTotalScore(score);
                        }
                        ProblemStateListData.TestPoint testPoint = problemStateListData.getTestPoint(status);
                        problemStateListData.getTestPoints().add(testPoint);
                    }
                }
                Float totalScoreFloat = problemStateListData.getTotalScoreFloat();
                problemStateListData.setTotalScore(StringUtil.getNumberNoInvalidZero(totalScoreFloat));
                problemStateListDataList.add(problemStateListData);
            }
        }
        return problemStateListDataList;
    }

}