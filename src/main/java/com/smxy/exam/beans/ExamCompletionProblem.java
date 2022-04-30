package com.smxy.exam.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
@TableName("exam_completion_problem")
public class ExamCompletionProblem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
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
     * 分数
     */
    private String score;

}
