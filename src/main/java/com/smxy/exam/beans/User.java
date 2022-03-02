package com.smxy.exam.beans;

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
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userid;

    private String password;

    private String email;

    private String sex;

    private String school;

    private String compiler;

    private String nick;

    private Integer submit;

    private Integer solved;

    private LocalDateTime registertime;

    private LocalDateTime lasttime;

    private String name;

    private String className;

    private Integer baseSubmit;

    private Integer baseSolved;

    private Integer debugSubmit;

    private Integer debugSolved;

    private Integer raptorSubmit;

    private Integer raptorSolved;

    private LocalDateTime archiveTime;

    private String ip;

    private String lockip;


}
