package icu.namophice.inkphotoalbum.business;

import icu.namophice.inkphotoalbum.config.DefaultConfig;
import icu.namophice.inkphotoalbum.driver.EPaper;
import icu.namophice.inkphotoalbum.utils.CommonUtil;
import icu.namophice.inkphotoalbum.utils.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Random;

/**
 * @author Namophice
 * @createTime 2021-08-05 10:27
 */
public class MasterService {

    public static void master() throws Exception {
        CommonUtil.printLogToConsole("The master program is running ...");

        final EPaper ePaper = EPaper.getInstance();
        ePaper.init();

        // 从cache文件获取imageIndex
        DefaultConfig.initImageIndexWithCache();
        if (DefaultConfig.imageIndex < 0) {
            DefaultConfig.imageIndex = 0;
        }
        if (DefaultConfig.imageIndex == 0) {
            BufferedImage defaultImage = ImageUtil.getImageToScreen();
            CommonUtil.printLogToConsole("Print images to screen ...");
            ePaper.drawImage(defaultImage);
        }

        DefaultConfig.defaultImageIsLoaded = true;

        if (DefaultConfig.local_images_mode()) { // local
            CommonUtil.printLogToConsole("Enabled local images mode ...");

            CommonUtil.printLogToConsole("Find images directory ...");
            final File imagesDir = new File(CommonUtil.rootPath + "/" + DefaultConfig.imagePath);

            if (imagesDir.exists()) {
                if (imagesDir.isDirectory()) {
                    CommonUtil.printLogToConsole("Images directory is exists ...");

                    CommonUtil.printLogToConsole("Find images files ...");
                    File[] imageList = imagesDir.listFiles();

                    if (imageList == null || imageList.length < 1) {
                        DefaultConfig.imageIndex = 0;
                        return;
                    }

                    imageList = Arrays.stream(imageList).filter(imageFile -> !imageFile.isDirectory()).toArray(File[]::new);
                    if (imageList.length < 1) {
                        DefaultConfig.imageIndex = 0;
                        return;
                    }

                    if (DefaultConfig.imageIndex < imageList.length) {
                        BufferedImage targetImage = ImageUtil.getImageToScreen(imageList[DefaultConfig.imageIndex]);

                        CommonUtil.printLogToConsole("Print images to screen ...");
                        ePaper.drawImage(targetImage);

                        DefaultConfig.imageIndex++;
                    } else {
                        DefaultConfig.imageIndex = 0;
                    }
                }
            }
        } else { // remote
            CommonUtil.printLogToConsole("Enabled remote images mode ...");

            if (!(DefaultConfig.imageIndex < 20)) {
                DefaultConfig.imageIndex = 0;
            }

            BufferedImage targetImage = ImageUtil.getImageToScreen(
                    DefaultConfig.imageUrlArr[new Random().nextInt(DefaultConfig.imageUrlArr.length)],
                    true
            );

            CommonUtil.printLogToConsole("Print images to screen ...");
            ePaper.drawImage(targetImage);
            DefaultConfig.imageIndex++;
        }
    }

}
