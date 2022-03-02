package com.smxy.exam.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("exam_completion_status")
public class ExamCompletionStatus implements Serializable {

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
     * 考试题号
     */
    private String problemId;

    /**
     * 题目题号
     */
    private Integer problemNum;

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


}
