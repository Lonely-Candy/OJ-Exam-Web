package com.smxy.exam.util;

import com.smxy.exam.beans.RunRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

/**
 * 代码运行工具
 *
 * @author 范颂扬
 * @create 2022-04-27 15:12
 */
public class CodeRunJudgeUtil {

    public static ArrayList<RunRecord> list = new ArrayList<RunRecord>();

    /**
     * 是否在判题
     */
    public static boolean judging = false;

    public static String comInfo = null;

    public static Object mute = new Object();

    /**
     * 每错误的提交一次, 罚时 20mins
     */
    private static int Penalty = 20;

    /**
     * 每错误的提交一次, 罚时 20mins
     */
    private static int PenaltyMS = Penalty * 60 * 1000;

    private final static EntPath Ent = EntPath.getInstance();

    /**
     * 日志控制
     */
    private static final Logger LOGGER = LoggerFactory.getLogger("business");

    /**
     * 代码编译
     *
     * @param record
     * @return boolean
     * @author 范颂扬
     * @date 2022-04-27 16:49
     */
    public static boolean compile(RunRecord record) {
        String thisExtName = null;
        String[] languageName = Ent.getLanguageName();
        String language = record.getLanguage();
        // 该下标是为了获取编译器对应的参数的
        int i = 0;
        for (i = 0; i < Ent.getLanguageNum(); i++) {
            if (language.equals(languageName[i])) {
                break;
            }
        }
        // 源代码扩展名
        thisExtName = Ent.getExtName()[i];
        // 创建 Temp 文件夹
        if (!FileUtil.createFolder(Ent.getWorkPath())) {
            return false;
        }
        File workFile = new File(Ent.getWorkPath());
        // Main
        String sourceName = Ent.getSourceName();
        // .getAbsolutePath()获取绝对路径
        String sourcePath = workFile.getAbsolutePath() + "\\" + sourceName + thisExtName;
        File exe = new File(workFile.getAbsolutePath() + "\\" + sourceName + record.getExeExtName());
        // 以上代码是为了以防万一，temp 里面还有之前残留的文件，所以要先删除 Main.exe,Main.c,Main.cpp,Main.java
        // (一般在判题结束后都会删除，但是有时会有异常导致有残留文件)
        FileUtil.deleteFile(exe.getPath());
        FileUtil.deleteFile(workFile.getAbsolutePath() + "\\" + sourceName + ".cpp");
        FileUtil.deleteFile(workFile.getAbsolutePath() + "\\" + sourceName + ".c");
        FileUtil.deleteFile(workFile.getAbsolutePath() + "\\" + sourceName + ".java");
        // 创建源码文件
        // 如果没有创建创建成功，在这里再创建一次。Main.c 或 Main.class 或 Main.cpp 等文件
        boolean isOk = FileUtil.createFile(sourcePath);
        if (!isOk) {
            LOGGER.error("Create Main File error!");
            return false;
        }
        // java 输出流 FileOutputStream，把数据写入本地文件
        boolean writeResult = FileUtil.writeFile(sourcePath, record.getSource());
        if (!writeResult) {
            LOGGER.error("Write source into File error!");
            return false;
        }
        BufferedReader brError = null;
        String command = Ent.getCompileCmd()[i];
        try {
            // ProcessBuilder 为进程提供了更多的控制，例如，可以设置当前工作目录，还可以改变环境参数。
            ProcessBuilder pb = new ProcessBuilder();
            // 设置环境
            Map<String, String> map = pb.environment();
            map.putAll(Ent.getEnvironmentMaps().get(i));
            // 执行命令
            pb.command(command.split(" "));
            // 开始执行
            Process comPro = pb.start();
            // 特别注意 VC++ 编译错误信息不再错误流中输出， 而在输入流中。。。
            if (language.equals("C") || language.equals("C++") || language.equals("C#")) {
                brError = new BufferedReader(new InputStreamReader(comPro.getInputStream()));
            } else {
                brError = new BufferedReader(new InputStreamReader(comPro.getErrorStream()));
            }
            comInfo = "";
            String tmp = null;
            long cnt = System.currentTimeMillis();
            // 逐行读取
            while (cnt + 15000 > System.currentTimeMillis() && (tmp = brError.readLine()) != null) {
                comInfo = comInfo + subCompileInfo(tmp) + "\n";
            }
            if (comInfo != null && !"".equals(comInfo)) {
                // 如果程序在这里阻塞了，会导致导致其他人提交的代码进不来，一直在 waiting 状态。
                comPro.waitFor();
            }
            brError.close();
            if (!exe.isFile()) {
                LOGGER.error("Failed to compile and generate exe file!");
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("carry out method name: compile");
            LOGGER.error("Error Message:", e);
            return false;
        } finally {
            FileUtil.closeStream(null, null, null, brError, null, null, null);
            FileUtil.deleteFileGeneral(sourcePath);
        }
        return true;
    }

    /**
     * 运行代码
     */
    public static RunRecord toRun(RunRecord record) {
        String runShell = Ent.getRunShell();
        Runtime run = Runtime.getRuntime();
        OutputStream os = null;
        BufferedReader brOut = null;
        BufferedReader brError = null;
        try {
            // 调用 Judge.exe 评判程序
            Process runPro = run.exec(runShell);
            // 往当前获取的 Process（judge.exe）进程中输入数据，那么你要调用 Process 的 getOutputStream 方法！
            os = runPro.getOutputStream();
            // 时限
            os.write((record.getTimeLimit() + "\n").getBytes());
            // 每个测试样例的时限
            os.write((record.getCaseTimeLimit() + "\n").getBytes());
            // 内存限制
            os.write((record.getMemLimit() + "\n").getBytes());
            // 运行命令
            os.write((record.getRunCmd() + "\n").getBytes());
            // temp 文件夹路径
            os.write((Ent.getWorkPath() + "\n").getBytes());
            os.write((1 + "\n").getBytes());
            // input.in
            os.write((Ent.getInputPath() + "\\" + record.getProType() + "\\" + record.getProId() + "\\"
                    + record.getCaseTestDataPath() + "\\" + Ent.getInputFileName() + "\n").getBytes());
            // Main.out
            os.write((Ent.getWorkPath() + "\\" + Ent.getProOutputFileName() + "\n").getBytes());
            // output.out
            os.write((Ent.getInputPath() + "\\" + record.getProType() + "\\" + record.getProId() + "\\"
                    + record.getCaseTestDataPath() + "\\" + Ent.getOutputFileName() + "\n").getBytes());
            os.flush();
            // 调用 Judge.exe 结束
            brOut = new BufferedReader(new InputStreamReader(runPro.getInputStream()));
            brError = new BufferedReader(new InputStreamReader(runPro.getErrorStream()));
            String errorInfo = "";
            // 获取运行时间和内存消耗
            int time = Integer.parseInt(brOut.readLine());
            int memory = Integer.parseInt(brOut.readLine());
            String tmp = null;
            while ((tmp = brOut.readLine()) != null) {
                errorInfo = errorInfo + tmp + "\n";
            }
            time -= record.getExtTime();
            if (time < 0) {
                time = 0;
            }
            memory -= record.getExtMemory();
            if (memory < 0) {
                memory = 0;
            }
            runPro.waitFor();
            // 得到结果
            int result = runPro.exitValue();
            if ((result != 0) && (errorInfo.indexOf("Exception") != -1)) {
                // Runtime Error
                comInfo = errorInfo;
                result = 5;
            }
            record.setTime(time).setMemory(memory).setResult(result);
        } catch (Exception e) {
            LOGGER.error("carry out method name: toRun");
            LOGGER.error("Error Message: ", e);
            return null;
        } finally {
            FileUtil.closeStream(null, null, null, null, null, null, os);
            FileUtil.closeStream(null, null, null, brOut, null, null, null);
            FileUtil.closeStream(null, null, null, brError, null, null, null);
            // 删除所有工作目录下的文件
            FileUtil.deleteFileGeneral(Ent.getWorkPath());
        }
        return record;
    }

    /**
     * 转换编译信息
     *
     * @param compileInfo
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-27 18:49
     */
    public static String subCompileInfo(String compileInfo) {
        int last1 = compileInfo.lastIndexOf('/');
        int last2 = compileInfo.lastIndexOf('\\');
        int last = last1 > last2 ? last1 : last2;
        String ans = compileInfo.substring(last + 1);
        return ans;
    }

}
