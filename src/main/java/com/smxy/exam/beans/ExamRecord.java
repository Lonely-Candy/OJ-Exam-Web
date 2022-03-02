package com.smxy.exam.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

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
@TableName("exam_record")
public class ExamRecord implements Serializable {

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
     * 学号
     */
    private String userId;

    /**
     * 学生姓名
     */
    private String userName;

    /**
     * 进入考试时间
     */
    private LocalDateTime beginTime;

    /**
     * 提交考试时间
     */
    private LocalDateTime submitTime;

    /**
     * 得分
     */
    private Float score;


}
