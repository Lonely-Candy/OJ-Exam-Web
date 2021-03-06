package com.smxy.exam.beans;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

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
public class Admin implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "adminid")
    private String adminid;

    private String password;

    @TableField(value = "registerTime")
    private Date registerTime;

    private Date lasttime;

    private String rank;

    private String ip;


}
