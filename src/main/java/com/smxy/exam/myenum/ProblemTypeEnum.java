package com.smxy.exam.myenum;

import lombok.Getter;

/**
 * @author 范颂扬
 * @create 2022-05-10 17:39
 */
@Getter
public enum ProblemTypeEnum {

    Completion("completion"), Programme("programme");

    private String name;

    ProblemTypeEnum(String name) {
        this.name = name;
    }
}
