package com.smxy.exam.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smxy.exam.util.StringUtil;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author fsy
 * @since 2022-02-10
 */
@Data
@Accessors(chain = true)
@TableName("exam_procedure_status")
public class ExamProcedureStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学生学号
     */
    private String userId;

    /**
     * 考试ID
     */
    private Integer examId;

    /**
     * 题库中对应的题目ID
     */
    private Integer problemId;

    /**
     * 题目ID
     */
    private Integer problemNum;

    /**
     * 测试数据ID
     */
    private Integer caseTestDataId;

    /**
     * 运行内存大小
     */
    private Integer memory;

    /**
     * 运行时间
     */
    private Integer time;

    /**
     * 编译器
     */
    private String compiler;

    /**
     * 代码长度
     */
    private Integer codeLength;

    /**
     * 提交答案
     */
    private String source;

    /**
     * 结果
     */
    private Integer result;

    /**
     * 得分
     */
    private Float score;

    /**
     * 答题时间
     */
    private LocalDateTime submitTime;

    /**
     * 答案
     */
    private String answer;

    /**
     * 编译信息
     */
    private String compilerMessage;

    /**
     * 是否修改，1修改，0未修改
     */
    private String isChange;

    public ExamProcedureStatus() {
        super();
    }

    /**
     * 根据判题结果，重新封装判题后的结果数据
     *
     * @param record
     * @param compilerMessage
     */
    public ExamProcedureStatus(RunRecord record, String compilerMessage) {
        this.id = record.getId();
        this.compilerMessage = compilerMessage;
        this.memory = record.getMemory();
        this.time = record.getTime();
        this.result = record.getResult();
        this.score = Float.parseFloat(record.getScores() == null ? "0" : record.getScores());
    }

}
