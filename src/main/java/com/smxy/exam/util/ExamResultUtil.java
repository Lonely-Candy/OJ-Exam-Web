package com.smxy.exam.util;

import lombok.Getter;

/**
 * @author 范颂扬
 * @create 2022-04-28 22:25
 */
public class ExamResultUtil {

    /**
     * 编程题填空题结果枚举类
     *
     * @author 范颂扬
     * @create 2022-04-28 22:37
     */
    @Getter
    public enum ProgrammeResult {

        Accepted("答案正确", 0),
        PresentationError("格式错误", 1),
        TimeLimitExceed("超过时限", 2),
        MemoryLimitExceed("内存超限", 3),
        WrongAnswer("答案错误", 4),
        RuntimeError("运行错误", 5),
        OutputLimitExceed("输出超限", 6),
        CompileError("编译错误", 7),
        SystemError("系统错误", 98),
        NoResult("无运行结果", -1);

        private String name;

        private Integer code;

        ProgrammeResult(String name, Integer code) {
            this.name = name;
            this.code = code;
        }

        public static String getNameByCode(Integer code) {
            for (ProgrammeResult c : ProgrammeResult.values()) {
                if (c.getCode().equals(code)) {
                    return c.name;
                }
            }
            return NoResult.getName();
        }

        public static Integer getCodeByName(String name) {
            for (ProgrammeResult value : ProgrammeResult.values()) {
                if (value.getName().equals(name)) {
                    return value.getCode();
                }
            }
            return -1;
        }

    }

    /**
     * 填空题结果枚举类
     *
     * @author 范颂扬
     * @create 2022-04-30 0:53
     */
    @Getter
    public enum CompletionResult {
        Correct("正确", 0), PartiallyCorrect("部分正确", 1), Mistake("错误", 2);

        private String name;

        private Integer code;

        CompletionResult(String name, Integer code) {
            this.code = code;
            this.name = name;
        }

        public static String getNameByCode(Integer code) {
            for (CompletionResult c : CompletionResult.values()) {
                if (c.getCode().equals(code)) {
                    return c.name;
                }
            }
            return "";
        }

        public static Integer getCodeByName(String name) {
            for (CompletionResult value : CompletionResult.values()) {
                if (value.getName().equals(name)) {
                    return value.code;
                }
            }
            return -1;
        }
    }

    /**
     * 根据运行结果得分数
     *
     * @param
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-28 22:21
     */
    public static String getScoreByRunResult(Integer result, String totalScore) {
        switch (result) {
            case 0:
                return totalScore;
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            default:
                return "0";
        }

    }

}
