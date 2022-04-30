package com.smxy.exam.beans;

import com.smxy.exam.util.EntPath;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 运行记录类
 *
 * @author 范颂扬
 * @create 2022-04-27 15:25
 */
@Data
@Accessors(chain = true)
public class RunRecord {

    /**
     * 数据库记录对应 ID（和 ExamProcedureStatus 中的 ID 是一直的）
     */
    private Integer id;

    /**
     * 题目 ID
     */
    private Integer proId;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * 考试 ID
     */
    private Integer examId;

    /**
     * 测试点对应的分数数组
     */
    private String[] caseTestDataScores;

    /**
     * 分数 (分点)（运行结果的分数，对应某个测试点，会在判题结束后写入该字段中）
     */
    private String scores;

    /**
     * 题目类型
     */
    private String proType;

    /**
     * 当前测试数据的索引
     */
    private Integer caseTestDataIndex;

    /**
     * 当前测试数据的路径
     */
    private String caseTestDataPath;

    /**
     * 时限
     */
    private int timeLimit;

    /**
     * 内存限制
     */
    private int memLimit;

    /**
     * 每个测试样例的时限
     */
    private int caseTimeLimit;

    /**
     * 编译器
     */
    private String language;

    /**
     * 额外时间
     */
    private int extTime;

    /**
     * 额外内存
     */
    private int extMemory;

    /**
     * 运行命令
     */
    private String runCmd;

    /**
     * 可执行文件的扩展名
     */
    private String exeExtName;

    /**
     * 源码
     */
    private String source;

    /**
     * 耗时
     */
    private Integer time;

    /**
     * 内存
     */
    private Integer memory;

    /**
     * 运行结果
     */
    private Integer result;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 答案
     */
    private String answer;

    public RunRecord() {
    }

    /**
     * @param procedureStatus
     * @param proType
     * @param submitTime
     * @param proIdMapScores
     * @return
     * @author 范颂扬
     * @date 2022-04-29 23:10
     */
    public RunRecord(ExamProcedureStatus procedureStatus, String proType, LocalDateTime submitTime
            , Map<Integer, String> proIdMapScores) {
        this.proType = proType;
        this.submitTime = submitTime;
        this.id = procedureStatus.getId();
        this.proId = procedureStatus.getProblemId();
        this.userId = procedureStatus.getUserId();
        this.examId = procedureStatus.getExamId();
        String scores = proIdMapScores.get(this.proId);
        this.caseTestDataScores = scores == null ? new String[1] : scores.split("#");
        this.scores = "0";
        this.caseTestDataIndex = procedureStatus.getCaseTestDataId();
        this.caseTestDataPath = "test-" + caseTestDataIndex;
        this.language = procedureStatus.getCompiler();
        setParameterByCompiler();
        EntPath.getRunCodeBySetting(this);
        this.source = procedureStatus.getSource();
        this.answer = procedureStatus.getAnswer();
    }

    /**
     * 根据编译器设置对应的参数
     *
     * @return void
     * @author 范颂扬
     * @date 2022-04-28 21:45
     */
    private void setParameterByCompiler() {
        // 设置时间限制和内存限制
        double[] limits = EntPath.getFactorOfTimeLimitAndMemLimitByLanguage(this.language);
        this.setTimeLimit((int) (1000 * limits[0]));
        this.setMemLimit(32768 * 1024);
        // 设置一个测试数据中单个运行时间
        this.setCaseTimeLimit(1000);
    }

    /**
     * 获取当前测试点对应的分数
     *
     * @param
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-30 10:32
     */
    public String getNetCaseScore() {
        return caseTestDataScores[caseTestDataIndex];
    }

}