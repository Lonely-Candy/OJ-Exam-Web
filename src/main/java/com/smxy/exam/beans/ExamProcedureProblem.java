package com.smxy.exam.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

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
@TableName("exam_procedure_problem")
public class ExamProcedureProblem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 考试ID
     */
    private Integer examId;

    /**
     * 题目ID
     */
    private Integer problemId;

    /**
     * 题目题号
     */
    private Integer problemNum;

    /**
     * 提交正确人数
     */
    private Integer acceptedNum;

    /**
     * 总提交次数
     */
    private Integer submitSum;

    /**
     * 分数
     */
    private String score;


}
