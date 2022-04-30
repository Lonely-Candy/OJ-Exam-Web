package com.smxy.exam.util;

import org.aspectj.apache.bcel.classfile.Code;

import java.util.HashMap;
import java.util.Map;

/**
 * 填空题判题工具
 *
 * @author 范颂扬
 * @create 2022-04-26 17:06
 */
public class CompletionJudgeUtil {

    /**
     * 填空题判题
     *
     * @param submitAnswer 学生提交的答案
     * @param answer       正确答案
     * @param scores       每空对应的答案
     * @author 范颂扬
     * @date 2022-04-26 18:00
     */
    public static Map<Float, Integer> judge(String[] submitAnswer, String[] answer, String[] scores) {
        float totalPoints = 0f;
        ExamResultUtil.CompletionResult result = ExamResultUtil.CompletionResult.Correct;
        for (int i = 0; i < submitAnswer.length; i++) {
            if (submitAnswer[i] == null) {
                result = ExamResultUtil.CompletionResult.PartiallyCorrect;
                continue;
            }
            if (submitAnswer[i].trim().equals(answer[i])) {
                totalPoints += Double.parseDouble(scores[i]);
            }
        }
        if (totalPoints == 0f) {
            result = ExamResultUtil.CompletionResult.Mistake;
        }
        HashMap<Float, Integer> floatIntegerHashMap = new HashMap<>();
        floatIntegerHashMap.put(totalPoints, ExamResultUtil.CompletionResult.getCodeByName(result.getName()));
        return floatIntegerHashMap;
    }

}
