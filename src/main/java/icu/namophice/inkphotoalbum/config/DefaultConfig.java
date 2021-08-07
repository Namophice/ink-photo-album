package icu.namophice.inkphotoalbum.config;

import com.alibaba.fastjson.JSONObject;
import icu.namophice.inkphotoalbum.utils.CommonUtil;

import java.io.*;

/**
 * @author Namophice
 * @createTime 2021-08-04 11:46
 */
public class DefaultConfig {

    public static final String configFileName = "conf.json";
    public static final String cacheFileName = "cache";

    public static final String imagePath = "images";

    public static int imageIndex;

    /**
     * 默认图片是否被加载过
     */
    public static boolean defaultImageIsLoaded = false;

    /**
     * 是否开启图片填充
     */
    private static boolean enable_image_fill = false;
    public static final boolean enable_image_fill() {
        return enable_image_fill;
    }

    /**
     * 本地图片库模式
     */
    private static boolean local_images_mode = true;
    public static boolean local_images_mode() {
        return local_images_mode;
    }

    /**
     * 初始化配置文件
     */
    public static void initConfig() {
        CommonUtil.printLogToConsole("Init config ...");

        File configFile = new File(CommonUtil.rootPath + "/" + configFileName);
        if (configFile.exists()) {

            BufferedReader bufferedReader = null;
            final StringBuilder confJsonStrBuilder = new StringBuilder();

            try {
                bufferedReader = new BufferedReader(new FileReader(configFile));

                String lineStr;

                while ((lineStr = bufferedReader.readLine()) != null) {
                    confJsonStrBuilder.append(lineStr).append("\n");
                }

                bufferedReader.close();
            } catch (Exception e) {
                CommonUtil.printErrorToLogFile(e);
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e) {
                        CommonUtil.printErrorToLogFile(e);
                    }
                }
            }

            final String confJsonStr = confJsonStrBuilder.toString();
            if (confJsonStr.length() > 2 && confJsonStr.contains("{") && confJsonStr.contains("}")) {
                JSONObject conJsonMap = JSONObject.parseObject(confJsonStr);
                if (conJsonMap.size() > 0) {
                    if (conJsonMap.get("enable_image_fill") != null) {
                        final Boolean cst_enable_image_fill = conJsonMap.getBoolean("enable_image_fill");
                        if (cst_enable_image_fill != null) {
                            enable_image_fill = cst_enable_image_fill;
                        }
                    }
                    if (conJsonMap.get("local_images_mode") != null) {
                        final Boolean cst_local_images_mode = conJsonMap.getBoolean("local_images_mode");
                        if (cst_local_images_mode != null) {
                            local_images_mode = cst_local_images_mode;
                        }
                    }
                }
            }
        }
    }

    /**
     * 从cache文件获取imageIndex
     * @throws IOException
     */
    public static void initImageIndexWithCache() throws IOException {
        CommonUtil.printLogToConsole("Init image index with cache ...");

        DefaultConfig.imageIndex = 0;

        File cacheFile = new File(CommonUtil.rootPath + "/" + cacheFileName);
        if (cacheFile.exists() && !cacheFile.isDirectory()) {
            BufferedReader bufferedReader = null;
            String lineStr = null;

            try {
                bufferedReader = new BufferedReader(new FileReader(cacheFile));

                lineStr = bufferedReader.readLine().trim();

                bufferedReader.close();
            } catch (Exception e) {
                CommonUtil.printErrorToLogFile(e);
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Exception e) {
                        CommonUtil.printErrorToLogFile(e);
                    }
                }
            }

            if (lineStr.length() > 0) {
                DefaultConfig.imageIndex = Integer.parseInt(lineStr);
            }
        }
    }

}
