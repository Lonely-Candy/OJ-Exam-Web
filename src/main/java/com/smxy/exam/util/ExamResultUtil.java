package com.smxy.exam.util;

/**
 * @author 范颂扬
 * @create 2022-04-28 22:25
 */
public class ExamResultUtil {

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
            case 1:
                return String.valueOf(Integer.parseInt(totalScore) - 2);
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
