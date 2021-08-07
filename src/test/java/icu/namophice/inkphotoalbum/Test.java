package icu.namophice.inkphotoalbum;

import icu.namophice.inkphotoalbum.config.DefaultConfig;
import icu.namophice.inkphotoalbum.utils.CommonUtil;
import icu.namophice.inkphotoalbum.utils.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Namophice
 * @createTime 2021-07-29 16:43
 */
public class Test {

    public static void main(String[] args) {
        try {
            BufferedImage targetImage = ImageUtil.getImageToScreen(CommonUtil.rootPath + "/" + DefaultConfig.imagePath + "/" + "self_22.jpg", false);
            printImage(targetImage, CommonUtil.rootPath + "/test.jpg");
        } catch (Exception e) {
            e.printStackTrace();
            CommonUtil.printErrorToLogFile(e);
        }
    }

    private static void printImage(BufferedImage image, String path) throws IOException {
        File file = new File(path);
        ImageIO.write(image, "JPG", file);
    }

}
