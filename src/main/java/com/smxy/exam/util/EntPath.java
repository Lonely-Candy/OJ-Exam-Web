package com.smxy.exam.util;

import com.smxy.exam.beans.RunRecord;
import lombok.Data;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 这是个单例类, 用来从xmlFilePath指定文件处读取配置信息
 *
 * @author 范颂扬
 * @create 2022-04-27 15:15
 */
@Data
public class EntPath {

    private static EntPath instance = null;

    /**
     * xml存放位置
     */
    private static String xmlFilePath = "D:\\OnlineJudge\\exam\\config\\ServerConfig.xml";

    /**
     * OJ语言的种类
     */
    private int languageNum = 0;

    /**
     * 环境变量的数量
     */
    private int[] environmentNum;

    /**
     * 语言名称
     */
    private String[] languageName;

    /**
     * 额外时间
     */
    private int[] extTime;

    /**
     * 额外内存
     */
    private int[] extMemony;

    /**
     * 扩展名
     */
    private String[] extName;

    /**
     * 可执行程序的扩展名
     */
    private String[] exeExtName;

    /**
     * 存放编译器的路径
     */
    private String[] compilePath;

    /**
     * 存放临时文件如: 生成的源代码文件, 编译后的文件, 程序输出的文件
     */
    private String workPath;

    /**
     * 根路径
     */
    private String baseFilePath;

    /**
     * run.exe 路径
     */
    private String runShell;

    /**
     * 开发者人数
     */
    private int DManNum;

    /**
     * 测试输入数据存放位置
     */
    private String inputPath;

    /**
     * 测试输出数据存放位置
     */
    private String outputPath;

    /**
     * 输入文件扩展名
     */
    private String inputExt;

    /**
     * 输出文件扩展名
     */
    private String outputExt;

    /**
     * 生成的源代码的文件名
     */
    private String sourceName;

    /**
     * 编译命令
     */
    private String[] compileCmd;

    /**
     * 运行命令
     */
    private String[] runCmd;

    /**
     * 程序测试输入文件名称
     */
    private String inputFileName;

    /**
     * 程序测试输出文件名称
     */
    private String outputFileName;

    /**
     * 程序输出文件名
     */
    private String proOutputFileName;

    /**
     * 语言的时限为 n*time_limit
     */
    private double[] factorOfTimeLimit;

    /**
     * 语言的内存限制为 n*memory_limit
     */
    private double[] factorOfMemoryLimit;

    /**
     * 存放语言关键字的文件夹
     */
    private String languageColorFilePath;

    private List<Map<String, String>> environmentMaps;

    private EntPath() {
        //从xml文档中读取配置信息
        try {
            File xmlFile = new File(xmlFilePath);
            if (!xmlFile.isFile()) {
                System.out.println(xmlFilePath + ", 该配置文件不存在...");
                return;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);

            this.baseFilePath = doc.getElementsByTagName("baseFilePath").item(0).getFirstChild().getNodeValue();
            this.workPath = doc.getElementsByTagName("workPath").item(0).getFirstChild().getNodeValue();
            this.runShell = doc.getElementsByTagName("runShell").item(0).getFirstChild().getNodeValue();
            this.inputPath = doc.getElementsByTagName("inputPath").item(0).getFirstChild().getNodeValue();
            this.outputPath = doc.getElementsByTagName("outputPath").item(0).getFirstChild().getNodeValue();
            this.inputExt = doc.getElementsByTagName("inputExt").item(0).getFirstChild().getNodeValue();
            this.outputExt = doc.getElementsByTagName("outputExt").item(0).getFirstChild().getNodeValue();
            this.sourceName = doc.getElementsByTagName("sourceName").item(0).getFirstChild().getNodeValue();
            this.inputFileName = doc.getElementsByTagName("inputFileName").item(0).getFirstChild().getNodeValue();
            this.outputFileName = doc.getElementsByTagName("outputFileName").item(0).getFirstChild().getNodeValue();
            this.proOutputFileName = doc.getElementsByTagName("proOutputFileName").item(0).getFirstChild().getNodeValue();
            this.languageColorFilePath = doc.getElementsByTagName("languageColorFilePath").item(0).getFirstChild().getNodeValue();
            // language
            try {
                this.languageNum = Integer.parseInt(doc.getElementsByTagName("languageNum").item(0).getFirstChild().getNodeValue());
            } catch (NumberFormatException e) {
                this.languageNum = 1;
            }
            this.languageName = new String[this.languageNum];
            this.extTime = new int[this.languageNum];
            this.extMemony = new int[this.languageNum];
            this.extName = new String[this.languageNum];
            this.exeExtName = new String[this.languageNum];
            this.factorOfTimeLimit = new double[this.languageNum];
            this.factorOfMemoryLimit = new double[this.languageNum];
            this.compilePath = new String[this.languageNum];
            this.compileCmd = new String[this.languageNum];
            this.runCmd = new String[this.languageNum];

            this.environmentNum = new int[this.languageNum];
            environmentMaps = new ArrayList<Map<String, String>>(this.languageNum);

            NodeList langList = doc.getElementsByTagName("language");
            int len = langList.getLength();
            if (this.languageNum < len) {
                this.languageNum = len;
            }
            String tmpWorkPath = "";
            String tmpCompilePath = "";
            for (int i = 0; i < this.workPath.length(); i++) {
                if (this.workPath.charAt(i) == '\\') {
                    tmpWorkPath += "\\\\";
                } else {
                    tmpWorkPath += this.workPath.charAt(i);
                }
            }
            for (int i = 0; i < this.languageNum; i++) {
                tmpCompilePath = "";
                this.languageName[i] = doc.getElementsByTagName("languageName").item(i).getFirstChild().getNodeValue();
                try {
                    this.extTime[i] = Integer.parseInt(doc.getElementsByTagName("extTime").item(i).getFirstChild().getNodeValue());
                } catch (NumberFormatException e) {
                    this.extTime[i] = 0;
                }

                try {
                    if (doc.getElementsByTagName("environmentNum").item(i) == null) {
                        throw new NumberFormatException();
                    }
                    this.environmentNum[i] = Integer.parseInt(doc.getElementsByTagName("environmentNum").item(i).getFirstChild().getNodeValue());
                } catch (NumberFormatException e) {
                    this.environmentNum[i] = 0;
                }

                this.extMemony[i] = Integer.parseInt(doc.getElementsByTagName("extMemony").item(i).getFirstChild().getNodeValue());
                this.extName[i] = doc.getElementsByTagName("extName").item(i).getFirstChild().getNodeValue();
                this.exeExtName[i] = doc.getElementsByTagName("exeExtName").item(i).getFirstChild().getNodeValue();
                this.factorOfTimeLimit[i] = Double.parseDouble(doc.getElementsByTagName("factorOfTimeLimit").item(i).getFirstChild().getNodeValue());
                this.factorOfMemoryLimit[i] = Double.parseDouble(doc.getElementsByTagName("factorOfMemoryLimit").item(i).getFirstChild().getNodeValue());
                this.compilePath[i] = doc.getElementsByTagName("compilePath").item(i).getFirstChild().getNodeValue();

                for (int j = 0; j < this.compilePath[i].length(); j++) {
                    if (this.compilePath[i].charAt(j) == '\\') {
                        tmpCompilePath += "\\\\";
                    } else {
                        tmpCompilePath += this.compilePath[i].charAt(j);
                    }
                }

                Map<String, String> envMap = new HashMap<String, String>();
                if (this.environmentNum[i] > 0) {
                    NodeList envNameList = langList.item(i).getOwnerDocument().getElementsByTagName("environments").item(0).getOwnerDocument().getElementsByTagName("envName");
                    NodeList envValueList = langList.item(i).getOwnerDocument().getElementsByTagName("environments").item(0).getOwnerDocument().getElementsByTagName("envValue");
                    for (int j = 0; j < this.environmentNum[i]; j++) {
                        String name = envNameList.item(j).getFirstChild().getNodeValue();
                        String value = envValueList.item(j).getFirstChild().getNodeValue();
                        envMap.put(name, value);
                    }
                }
                environmentMaps.add(envMap);

                this.compileCmd[i] = doc.getElementsByTagName("compileCmd").item(i).getFirstChild().getNodeValue();
                this.compileCmd[i] = this.compileCmd[i].replaceAll("%COMPILEPATH%", tmpCompilePath);
                this.compileCmd[i] = this.compileCmd[i].replaceAll("%WORKPATH%", tmpWorkPath);
                this.compileCmd[i] = this.compileCmd[i].replaceAll("%SOURCENAME%", this.sourceName);
                this.compileCmd[i] = this.compileCmd[i].replaceAll("%EXTNAME%", this.extName[i]);
                this.compileCmd[i] = this.compileCmd[i].replaceAll("%EXEEXTNAME%", this.exeExtName[i]);

                this.runCmd[i] = doc.getElementsByTagName("runCmd").item(i).getFirstChild().getNodeValue();
                this.runCmd[i] = this.runCmd[i].replaceAll("%WORKPATH%", tmpWorkPath);
                this.runCmd[i] = this.runCmd[i].replaceAll("%SOURCENAME%", this.sourceName);
                this.runCmd[i] = this.runCmd[i].replaceAll("%EXTNAME%", this.extName[i]);
                this.runCmd[i] = this.runCmd[i].replaceAll("%EXEEXTNAME%", this.exeExtName[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized EntPath getInstance() {
        if (instance == null) {
            instance = new EntPath();
        }
        return instance;
    }

    /**
     * 根据编译器，获取对应的时间限制和内存限制
     *
     * @param language
     * @return double[]
     * @author 范颂扬
     * @date 2022-04-28 15:15
     */
    public static double[] getFactorOfTimeLimitAndMemLimitByLanguage(String language) {
        EntPath entPath = getInstance();
        double[] result = new double[2];
        for (int i = 0; i < entPath.languageName.length; i++) {
            if (language.equals(entPath.languageName[i])) {
                result[0] = entPath.getFactorOfTimeLimit()[i];
                result[1] = entPath.getFactorOfMemoryLimit()[i];
                return result;
            }
        }
        return result;
    }

    /**
     * 根据配置设置数据到运行参数中
     *
     * @param record
     * @return com.smxy.exam.beans.RunRecord
     * @author 范颂扬
     * @date 2022-04-29 23:05
     */
    public static RunRecord getRunCodeBySetting(RunRecord record) {
        EntPath ent = getInstance();
        String[] languageName = ent.getLanguageName();
        // 获取对应的判断语言
        for (int i = 0; i < ent.languageNum; i++) {
            String language = record.getLanguage();
            if (language.equals(languageName[i])) {
                // 额外时间
                record.setExtTime(ent.getExtTime()[i]);
                // 额外内容
                record.setExtMemory(ent.getExtMemony()[i]);
                // 运行命令
                record.setRunCmd(ent.getRunCmd()[i]);
                // 可执行文件的扩展名
                record.setExeExtName(ent.getExeExtName()[i]);
                return record;
            }
        }
        return null;
    }

}
