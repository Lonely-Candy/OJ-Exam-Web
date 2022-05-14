package com.smxy.exam.myenum;

import lombok.Getter;

/**
 * 填空题结果枚举类
 *
 * @author 范颂扬
 * @create 2022-04-30 0:53
 */
@Getter
public enum CompletionResultEnum {
    Correct("正确", 0), PartiallyCorrect("部分正确", 1), Mistake("错误", 2);

    private String name;

    private Integer code;

    CompletionResultEnum(String name, Integer code) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(Integer code) {
        for (CompletionResultEnum c : CompletionResultEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.name;
            }
        }
        return "";
    }

    public static Integer getCodeByName(String name) {
        for (CompletionResultEnum value : CompletionResultEnum.values()) {
            if (value.getName().equals(name)) {
                return value.code;
            }
        }
        return -1;
    }
}