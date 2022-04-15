package com.smxy.exam.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * 文件处理工具
 *
 * @author 范颂扬
 * @create 2022-04-13 20:27
 */
public class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger("business");

    /**
     * 判断该文件夹是否存在
     *
     * @param folderPath
     * @return java.io.File
     * @author 范颂扬
     * @date 2022-04-13 20:32
     */
    public static boolean isExist(String folderPath) {
        File file = new File(folderPath);
        return file.exists();
    }

    /**
     * 创建文件夹
     *
     * @param folderPath
     * @return void
     * @author 范颂扬
     * @date 2022-04-13 20:34
     */
    public static boolean createFolder(String folderPath) {
        File file = new File(folderPath);
        if (!file.exists()) {
            return file.mkdirs();
        }
        return true;
    }

    /**
     * 创建文件
     *
     * @param filePath
     * @return
     */
    public static boolean createFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                printErrorLog("createFile", e.getMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * 关闭流
     *
     * @param bufferedWriter
     * @param outputStreamWriter
     * @param fileOutputStream
     * @param bufferedReader
     * @param inputStreamReader
     * @param fileInputStream
     * @return void
     * @author 范颂扬
     * @date 2022-04-13 21:11
     */
    public static void closeStream(BufferedWriter bufferedWriter
            , OutputStreamWriter outputStreamWriter, FileOutputStream fileOutputStream
            , BufferedReader bufferedReader, InputStreamReader inputStreamReader
            , FileInputStream fileInputStream) {
        try {
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IOException e) {
            printErrorLog("closeStream", e.getMessage());
        }
    }

    /**
     * 写入文件
     *
     * @param filePath 文件路径
     * @param data     数据
     * @return boolean
     * @author 范颂扬
     * @date 2022-04-13 20:54
     */
    public static boolean writeFile(String filePath, String data) {
        FileOutputStream fileOutputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            fileOutputStream = new FileOutputStream(filePath);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            bufferedWriter = new BufferedWriter(outputStreamWriter);
            bufferedWriter.write(data);
        } catch (FileNotFoundException e) {
            printErrorLog("writeFile", e.getMessage());
            return false;
        } catch (IOException e) {
            printErrorLog("writeFile", e.getMessage());
            return false;
        } finally {
            closeStream(bufferedWriter, outputStreamWriter, fileOutputStream
                    , null, null, null);
        }
        return true;
    }

    /**
     * 读取文件
     *
     * @param filePath
     * @return java.lang.String
     * @author 范颂扬
     * @date 2022-04-13 21:07
     */
    public static String readFile(String filePath) {
        StringBuffer dataBuffer = new StringBuffer();
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileInputStream = new FileInputStream(filePath);
            inputStreamReader = new InputStreamReader(fileInputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String temp = null;
            while ((temp = bufferedReader.readLine()) != null) {
                dataBuffer.append(temp);
                dataBuffer.append("\n");
            }
        } catch (FileNotFoundException e) {
            printErrorLog("readFile", e.getMessage());
            return null;
        } catch (IOException e) {
            printErrorLog("readFile", e.getMessage());
            return null;
        } finally {
            closeStream(null, null, null
                    , bufferedReader, inputStreamReader, fileInputStream);
        }
        return dataBuffer.toString();
    }

    /**
     * 获取文件夹下的文件集合
     *
     * @param folderPath
     * @return java.io.File[]
     * @author 范颂扬
     * @date 2022-04-15 8:59
     */
    public static File[] getFolderBelowFiles(String folderPath) {
        File folder = new File(folderPath);
        File[] files = folder.listFiles();
        return files == null ? new File[0] : files;
    }

    /**
     * 删除文件/文件夹
     *
     * @param filePath
     * @return boolean
     * @author 范颂扬
     * @date 2022-04-15 14:12
     */
    public static boolean deleteFileGeneral(String filePath) {
        File file = new File(filePath);
        if (file.isFile()) {
            return deleteFile(filePath);
        }
        return deleteFolder(filePath);
    }

    /**
     * 删除文件夹
     *
     * @param filePath
     * @return boolean
     * @author 范颂扬
     * @date 2022-04-15 14:20
     */
    private static boolean deleteFolder(String filePath) {
        Path path = Paths.get(filePath);
        try {
            Stream<Path> stream = Files.walk(path);
            stream.sorted(Comparator.reverseOrder()).forEach(FileUtil::deleteSteam);
            return true;
        } catch (IOException e) {
            printErrorLog("deleteFolder", e.getMessage());
        }
        return false;
    }

    /**
     * 删除流
     *
     * @param path
     * @return void
     * @author 范颂扬
     * @date 2022-04-15 14:35
     */
    private static void deleteSteam(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            printErrorLog("deleteSteam", e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param filePath
     * @return boolean
     * @author 范颂扬
     * @date 2022-04-15 14:20
     */
    private static boolean deleteFile(String filePath) {
        Path path = Paths.get(filePath);
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            printErrorLog("deleteFile", e.getMessage());
            return false;
        }
    }

    /**
     * 重命名文件
     *
     * @param filePath 原文件路径
     * @param newName  新名字
     * @return boolean
     * @author 范颂扬
     * @date 2022-04-15 14:43
     */
    public static boolean rename(String filePath, String newName) {
        File file = new File(filePath);
        return rename(file, newName);
    }

    /**
     * 重命名文件
     *
     * @param file
     * @param newName
     * @return boolean
     * @author 范颂扬
     * @date 2022-04-15 14:50
     */
    public static boolean rename(File file, String newName) {
        String parent = file.getParent();
        return file.renameTo(new File(parent + "\\" + newName));
    }

    /**
     * 输出错误日志
     *
     * @param methodName
     * @param errorMessage
     * @return void
     * @author 范颂扬
     * @date 2022-04-13 23:08
     */
    private static void printErrorLog(String methodName, String errorMessage) {
        LOGGER.error("carry out method name: " + methodName);
        LOGGER.error("exception message: " + errorMessage);
    }

}
