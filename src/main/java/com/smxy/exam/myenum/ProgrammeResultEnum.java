package com.smxy.exam.myenum;

import lombok.Getter;

/**
 * 编程题填空题结果枚举类
 *
 * @author 范颂扬
 * @create 2022-04-28 22:37
 */
@Getter
public enum ProgrammeResultEnum {

    Accepted("答案正确", 0),
    PresentationError("格式错误", 1),
    TimeLimitExceed("超过时限", 2),
    MemoryLimitExceed("内存超限", 3),
    WrongAnswer("答案错误", 4),
    RuntimeError("运行错误", 5),
    OutputLimitExceed("输出超限", 6),
    CompileError("编译错误", 7),
    // 多个结果显示
    MoreWrong("多种错误", 8),
    PartiallyCorrect("部分正确", 9),
    SystemError("系统错误", 98),
    NoResult("等待判题", -1);

    private String name;

    private Integer code;

    ProgrammeResultEnum(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public static String getNameByCode(Integer code) {
        for (ProgrammeResultEnum c : ProgrammeResultEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.name;
            }
        }
        return NoResult.getName();
    }

    public static Integer getCodeByName(String name) {
        for (ProgrammeResultEnum value : ProgrammeResultEnum.values()) {
            if (value.getName().equals(name)) {
                return value.getCode();
            }
        }
        return -1;
    }

}