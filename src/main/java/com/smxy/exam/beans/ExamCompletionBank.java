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
@TableName("exam_completion_bank")
public class ExamCompletionBank implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 题干
     */
    private String content;

    /**
     * 参考答案，每个空用#隔开
     */
    private String answer;

    /**
     * 得分，每个空用#隔开
     */
    private String score;

    /**
     * 创建人
     */
    private String adminid;

    /**
     * 创建时间
     */
    private LocalDateTime time;


}
