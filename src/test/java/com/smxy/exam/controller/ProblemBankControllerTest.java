package com.smxy.exam.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

/**
 * @author 范颂扬
 * @create 2022-04-05 18:16
 */
public class ProblemBankControllerTest {

    /**
     * 测试填空题html内容替换
     */
    @Test
    void testHTMLDispose() {
        String htmlText = "<p>Java把数据类型分为<t></t>和<t></t>。<br></p>";
        String answer = "数据类型#引用数据类型";
        String score = "5#5";
        int index = 0;
        while (htmlText.indexOf("<t></t>") != -1) {
            htmlText = htmlText.replaceFirst("<t></t>", "<div id=\"input_answer_"
                    + (index++) + "\" class=\"input-answer input-group m-b\"></div>");
        }
        htmlText = htmlText.substring(3, htmlText.length() - 4);
        System.out.println(htmlText);
        String[] answers = answer.split("#");
        String[] scores = score.split("#");
        Document document = Jsoup.parse(htmlText);
        for (int i = 0; i < index; i++) {
            Element element = document.getElementById("input_answer_" + i);
            element.html("<input disabled value=\"" + answers[i] + "\" type=\"text\" class=\"form-control\"> " +
                    "<span class=\"input-answer-addon input-group-addon\">" + scores[i] + " 分</span>");
            element.getElementsByTag("input").get(0).removeAttr("disabled");
            System.out.println(element);
        }
        System.out.println(document.body().html());
    }

}