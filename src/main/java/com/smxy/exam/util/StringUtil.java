package com.smxy.exam.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串处理工具
 *
 * @author 范颂扬
 * @create 2022-04-29 21:12
 */
public class StringUtil {

    private static final Pattern pattern = Pattern.compile("<.+?>", Pattern.DOTALL);

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
        Document document = Jsoup.parse(html);
        Elements elements = document.getElementsByTag("code");
        Element element = elements.get(0);
        // 加回 <code> 标签，防止出现分割错误。
        String codeHtmlValue = "<code>" + element.html() + "</code>";
        String[] htmlValue = codeHtmlValue.split("<t></t>");
        for (int i = 0; i < htmlValue.length; i++) {
            Matcher matcher = pattern.matcher(htmlValue[i]);
            htmlValue[i] = matcher.replaceAll("");
        }
        StringBuffer runCode = new StringBuffer(htmlValue[0]);
        int index = 1;
        for (int i = 0; i < answer.length; i++, index++) {
            runCode.append(answer[i]);
            runCode.append(htmlValue[index]);
        }
        while (index + 1 < htmlValue.length) {
            runCode.append(htmlValue[index]);
            runCode.append(htmlValue[index + 1]);
        }
        // 过滤字符
        String runCodeStr = unescape(runCode.toString());
        // 去除注释
        runCodeStr = dislodge(runCodeStr);
        return runCodeStr == null ? new String() : runCodeStr;
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

    /**
     * 去除注释
     *
     * @param s
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-05-03 16:02
     */
    public static String dislodge(String s) {
        String[] processStr = s.split("\n");
        // 去掉//后面的
        for (int i = 0; i < processStr.length; i++) {
            String str1 = "";
            if (processStr[i].contains("//")) {
                for (int j = 0; j < processStr[i].length(); j++) {
                    if (processStr[i].charAt(j) == '/') {
                        break;
                    } else {
                        str1 += processStr[i].charAt(j);
                    }
                }
                processStr[i] = str1;
            }

        }
        // 去掉/*---*/
        for (int i = 0; i < processStr.length; i++) {
            String str1 = "";
            // 如果有/*....*/
            if (processStr[i].contains("/*") && processStr[i].contains("*/")) {
                processStr[i] = "";
            } else if (processStr[i].contains("/*")) {
                for (int j = 0; j < processStr[i].length(); j++) {
                    if (processStr[i].charAt(j) == '/') {
                        break;
                    } else {
                        str1 += processStr[i].charAt(j);
                    }
                }
                processStr[i] = str1;
                i++;
                while (!processStr[i].contains("*/")) {
                    processStr[i++] = "";
                }
                processStr[i] = "";
            }
        }
        StringBuffer code = new StringBuffer();
        for (int i = 0; i < processStr.length; i++) {
            code.append(processStr[i]);
            code.append("\n");
        }
        return code.toString();
    }

}
