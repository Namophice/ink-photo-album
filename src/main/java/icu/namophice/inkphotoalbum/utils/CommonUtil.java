package icu.namophice.inkphotoalbum.utils;

import icu.namophice.inkphotoalbum.config.DefaultConfig;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Namophice
 * @createTime 2021-08-01 17:44
 */
public class CommonUtil {

    /**
     * 获取程序运行根目录
     * @return
     */
    public static String rootPath = System.getProperty("user.dir");

    /**
     * 打印异常信息至error.log
     * @param exception
     */
    public static void printErrorToLogFile(Exception exception) {
        printErrorToLogFile(exception, true);
    }

    /**
     * 打印异常信息至error.log
     * @param exception
     * @param clearLogFile
     */
    public static void printErrorToLogFile(Exception exception, boolean clearLogFile) {
        if (exception != null) {
            try {
                exception.printStackTrace();

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);

                exception.printStackTrace(pw);
                final String errorDetail = "Error [" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:ms").format(LocalDateTime.now()) + "] " + sw;

                sw.close();
                pw.close();

                File logFile = new File(rootPath + "/error.log");
                if ((!logFile.exists()) || logFile.isDirectory()) {
                    logFile.createNewFile();
                }

                FileWriter fileWriter = new FileWriter(logFile, clearLogFile);
                fileWriter.write(errorDetail);
                fileWriter.flush();
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取日志时间头
     * @return
     */
    public static String getLogTime() {
        return "[" + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:ms").format(LocalDateTime.now()) + "] ";
    }

    /**
     * 打印日志到控制台
     * @param log
     */
    public static void printLogToConsole(final String log) {
        System.out.println("Info " + getLogTime() + log);
    }

    /**
     * 打印图片下标位置到cache文件
     * @param imageIndex
     * @throws IOException
     */
    public static void printImageIndexToCache(int imageIndex) throws IOException {
        CommonUtil.printLogToConsole("Print image index to cache file ...");

        File cacheFile = new File(CommonUtil.rootPath + "/" + DefaultConfig.cacheFileName);
        if ((!cacheFile.exists()) || cacheFile.isDirectory()) {
            cacheFile.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(cacheFile, false);
        fileWriter.write(String.valueOf(imageIndex));
        fileWriter.flush();
        fileWriter.close();
    }

}
