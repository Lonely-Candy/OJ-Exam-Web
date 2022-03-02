package com.smxy.exam.beans;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

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
public class Exam implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 考试ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 考试标题
     */
    private String title;

    /**
     * 考试开始时间
     */
    private LocalDateTime beginTime;

    /**
     * 考试结束时间
     */
    private LocalDateTime endTime;

    /**
     * 考试时长，单位：分钟
     */
    private Integer length;

    /**
     * 考试状态，-1未开始，1已开始
     */
    private Integer flag;

    /**
     * 创建人
     */
    private String author;

    /**
     * 是否开启监控，-1否，1是
     */
    private Integer isCheck;

}
