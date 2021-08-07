package icu.namophice.inkphotoalbum;

import icu.namophice.inkphotoalbum.business.MasterService;
import icu.namophice.inkphotoalbum.config.DefaultConfig;
import icu.namophice.inkphotoalbum.utils.CommonUtil;

/**
 * @author Namophice
 * @createTime 2021-07-29 16:42
 */
public class Main {

    public static void main(String [] args) {
        try {
            CommonUtil.printLogToConsole("Application Started ...");

            // 初始化配置
            DefaultConfig.initConfig();

            // 从cache文件获取imageIndex
            DefaultConfig.initImageIndexWithCache();

            // 进入主流程
            MasterService.master();
        } catch (Exception e) {
            CommonUtil.printErrorToLogFile(e);
        } finally {
            try {
                // 打印下次获取的图片下标位置到cache文件
                CommonUtil.printImageIndexToCache(DefaultConfig.imageIndex);
            } catch (Exception e) {
                CommonUtil.printErrorToLogFile(e);
            }

            CommonUtil.printLogToConsole("Application Exit !");
        }
    }

}
