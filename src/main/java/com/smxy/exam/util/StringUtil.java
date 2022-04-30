package com.smxy.exam.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

import java.math.BigDecimal;

/**
 * 字符串处理工具
 *
 * @author 范颂扬
 * @create 2022-04-29 21:12
 */
public class StringUtil {

    /**
     * 反转义字符
     *
     * @param str
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-28 21:49
     */
    public static String unescape(String str) {
        str = str.replaceAll("&lt;", "<");
        str = str.replaceAll("&gt;", ">");
        str = str.replaceAll("&amp;", "&");
        str = str.replaceAll("&nbsp;", " ");
        return str;
    }

    /**
     * 转义字符
     *
     * @param str
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-28 21:49
     */
    public static String escape(String str) {
        str = str.replaceAll("<", "&lt;");
        str = str.replaceAll(">", "&gt;");
        str = str.replaceAll("&", "&amp;");
        return str;
    }

    /**
     * 处理代码
     *
     * @param html
     * @param answer
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-28 14:55
     */
    public static String getRunCodeByHtml(String html, String[] answer) {
        String runCode = "";
        int index = 0;
        while (html.indexOf("<t></t>") != -1) {
            html = html.replaceFirst("<t></t>", answer[index++]);
        }
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByTag("code");
        Element element = elements.get(0);
        runCode = Jsoup.clean(element.toString(), "", Safelist.none(), new Document.OutputSettings().prettyPrint(false));
        // 过滤字符
        runCode = unescape(runCode);
        return runCode == null ? new String() : runCode;
    }

    /**
     * 将数字去除多余的0返回字符串形式的数字
     *
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-30 12:27
     */
    public static String getNumberNoInvalidZero(Float v) {
        return new BigDecimal(String.valueOf(v)).stripTrailingZeros().toPlainString();
    }

    public static String getNumberNoInvalidZero(Double v) {
        return new BigDecimal(String.valueOf(v)).stripTrailingZeros().toPlainString();
    }

}
