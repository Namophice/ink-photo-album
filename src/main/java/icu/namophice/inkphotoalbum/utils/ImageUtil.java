package icu.namophice.inkphotoalbum.utils;

import icu.namophice.inkphotoalbum.config.DefaultConfig;
import icu.namophice.inkphotoalbum.driver.EPaper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.Random;

/**
 * @author Namophice
 * @createTime 2021-08-04 16:28
 */
public class ImageUtil {

    /**
     * 获取可直接打印至屏幕的Image流
     * @param imagePath
     * @param isRemoteUrl
     * @return
     * @throws IOException
     */
    public static BufferedImage getImageToScreen(final String imagePath, final boolean isRemoteUrl) throws IOException {
        BufferedImage targetImage = getImage(imagePath, isRemoteUrl);
        targetImage = tailoringImage(targetImage, EPaper.width, EPaper.height, DefaultConfig.enable_image_fill());
        targetImage = getGaryImg(targetImage);

        return targetImage;
    }

    /**
     * 获取可直接打印至屏幕的Image流
     * @param imageFile
     * @return
     * @throws IOException
     */
    public static BufferedImage getImageToScreen(File imageFile) throws IOException {
        BufferedImage targetImage = getImage(imageFile);
        targetImage = tailoringImage(targetImage, EPaper.width, EPaper.height, DefaultConfig.enable_image_fill());
        targetImage = getGaryImg(targetImage);

        return targetImage;
    }

    /**
     * 获取可直接打印至屏幕的Image流
     * @return
     * @throws IOException
     */
    public static BufferedImage getImageToScreen() throws IOException {
        return getImageToScreen(null);
    }

    /**
     * 获取图片流
     * @param imageUrl
     * @param isRemoteUrl
     * @return
     * @throws IOException
     */
    public static BufferedImage getImage(final String imageUrl, final boolean isRemoteUrl) throws IOException {
        CommonUtil.printLogToConsole("Input image to BufferedImage ...");

        if (imageUrl == null || imageUrl.length() < 1) {
            return getDefaultImage();
        } else {
            InputStream imageStream;

            if (isRemoteUrl) {
                CommonUtil.printLogToConsole("Image path: " + imageUrl + " ...");

                URL url = new URL(imageUrl);
                imageStream = url.openStream();
            } else {
                File imgFile = new File(imageUrl);
                if (imgFile.exists()) {
                    CommonUtil.printLogToConsole("Image path: " + imgFile.getAbsolutePath() + " ...");

                    imageStream = new FileInputStream(imgFile);
                } else {
                    return getDefaultImage();
                }
            }

            BufferedImage originImage = ImageIO.read(imageStream);
            imageStream.close();
            imageStream = null;

            BufferedImage targetImage = new BufferedImage(originImage.getWidth(), originImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            for (int i = 0; i < originImage.getWidth(); i++) {
                for (int j = 0; j < originImage.getHeight(); j++) {
                    targetImage.setRGB(i, j, originImage.getRGB(i, j));
                }
            }

            return targetImage;
        }
    }

    /**
     * 获取图片流
     * @param imageFile
     * @return
     * @throws IOException
     */
    public static BufferedImage getImage(File imageFile) throws IOException {
        CommonUtil.printLogToConsole("Input image to BufferedImage ...");

        if (imageFile == null || !imageFile.exists() || imageFile.isDirectory()) {
            return getDefaultImage();
        } else {
            CommonUtil.printLogToConsole("Image path: " + imageFile.getAbsolutePath() + " ...");

            InputStream imageStream = new FileInputStream(imageFile);
            BufferedImage originImage = ImageIO.read(imageStream);
            imageStream.close();

            BufferedImage targetImage = new BufferedImage(originImage.getWidth(), originImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            for (int i = 0; i < originImage.getWidth(); i++) {
                for (int j = 0; j < originImage.getHeight(); j++) {
                    targetImage.setRGB(i, j, originImage.getRGB(i, j));
                }
            }

            return targetImage;
        }
    }

    /**
     * 获取默认图片流
     * @return
     * @throws IOException
     */
    public static BufferedImage getDefaultImage() throws IOException {
        CommonUtil.printLogToConsole("Input default image to BufferedImage ...");

        InputStream defaultImageStream = CommonUtil.class.getClassLoader().getResourceAsStream("images/default.bmp");
        BufferedImage originImage = ImageIO.read(defaultImageStream);
        defaultImageStream.close();
        defaultImageStream = null;

        BufferedImage targetImage = new BufferedImage(originImage.getWidth(), originImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < originImage.getWidth(); i++) {
            for (int j = 0; j < originImage.getHeight(); j++) {
                targetImage.setRGB(i, j, originImage.getRGB(i, j));
            }
        }

        return targetImage;
    }

    /**
     * 对图片做自适应or填充处理（默认自适应）
     * @param originImage
     * @param screenWidth
     * @param screenHeight
     * @param enable_image_fill
     * @return
     */
    public static BufferedImage tailoringImage(BufferedImage originImage, final int screenWidth, final int screenHeight, final boolean enable_image_fill) {
        CommonUtil.printLogToConsole("Tailoring image with screen ...");

        // 获取原始图片宽高
        final BigDecimal originWidth = new BigDecimal(originImage.getWidth());
        final BigDecimal originHeight = new BigDecimal(originImage.getHeight());

        BufferedImage targetImage;

        if (originWidth.intValue() == screenWidth && originHeight.intValue() == screenHeight) { // 图片宽高 == 屏幕宽高
            return originImage;
        } else if (originWidth.intValue() < screenWidth && originHeight.intValue() < screenHeight) { // 图片宽高 皆小于 屏幕宽高
            targetImage = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_RGB);

            // 将原图居中，计算原图在输出图片中的位置
            final int xStart = (screenWidth - originWidth.intValue()) / 2;
            final int xEnd = originWidth.intValue() + xStart;
            final int yStart = (screenHeight - originHeight.intValue()) / 2;
            final int yEnd = originHeight.intValue() + yStart;

            for (int x = 0; x < screenWidth; x++) {
                for (int y = 0; y < screenHeight; y++) {
                    // 在输出图片中未被原始图片覆盖的位置直接做涂黑处理
                    if (x < xStart || x >= xEnd) {
                        targetImage.setRGB(x, y, 0);
                    } else {
                        if (y < yStart || y >= yEnd) {
                            targetImage.setRGB(x, y, 0);
                        } else {
                            targetImage.setRGB(x, y, originImage.getRGB(x - xStart, y - yStart));
                        }
                    }
                }
            }

            return targetImage;
        } else { // 图片宽 > 屏幕宽 or 图片高 > 屏幕高
            // 屏幕宽高比例
            final BigDecimal screenPercentage = new BigDecimal(screenWidth).divide(new BigDecimal(screenHeight), 5, RoundingMode.HALF_DOWN);
            // 原图宽高比例
            final BigDecimal imagePercentage = originWidth.divide(originHeight, 5, RoundingMode.HALF_UP);


            int percentageCompareRes = screenPercentage.compareTo(imagePercentage);
            if (percentageCompareRes == 0) { // 图片宽高比例 == 屏幕宽高比例
                return originImage;
            } else {
                // 等比例放大，相同高度下，屏幕宽度小于原图宽度
                boolean check = originHeight.multiply(new BigDecimal(screenWidth)).compareTo(originWidth.multiply(new BigDecimal(screenHeight))) < 1;

                if (!DefaultConfig.defaultImageIsLoaded || !enable_image_fill) { // 自适应模式
                    CommonUtil.printLogToConsole("Enabled image adaptive mode ...");

                    if (check) {
                        final int targetWight = originWidth.intValue();
                        final int targetHeight = originWidth.divide(screenPercentage, 0, RoundingMode.HALF_DOWN).intValue();

                        targetImage = new BufferedImage(targetWight, targetHeight, BufferedImage.TYPE_INT_RGB);

                        final int yStart = (targetHeight - originHeight.intValue()) / 2;
                        final int yEnd = originHeight.intValue() + yStart;

                        for (int x = 0; x < targetWight; x++) {
                            for (int y = 0; y < targetHeight; y++) {
                                if (yStart <= y && y < yEnd) {
                                    targetImage.setRGB(x, y, originImage.getRGB(x, y - yStart));
                                } else {
                                    targetImage.setRGB(x, y, 0);
                                }
                            }
                        }
                    } else {
                        final int targetWight = originHeight.multiply(screenPercentage).intValue();
                        final int targetHeight = originHeight.intValue();

                        targetImage = new BufferedImage(targetWight, targetHeight, BufferedImage.TYPE_INT_RGB);

                        final int xStart = (targetWight - originWidth.intValue()) / 2;
                        final int xEnd = originWidth.intValue() + xStart;

                        for (int x = 0; x < targetWight; x++) {
                            for (int y = 0; y < targetHeight; y++) {
                                if (xStart <= x && x < xEnd) {
                                    targetImage.setRGB(x, y, originImage.getRGB(x - xStart, y));
                                } else {
                                    targetImage.setRGB(x, y, 0);
                                }
                            }
                        }
                    }
                } else { // 填充模式
                    CommonUtil.printLogToConsole("Enabled image fill mode ...");

                    if (check) {
                        final int targetWight = originHeight.multiply(screenPercentage).intValue();
                        final int targetHeight = originHeight.intValue();

                        targetImage = new BufferedImage(targetWight, targetHeight, BufferedImage.TYPE_INT_RGB);

                        final int xStart = originWidth.subtract(new BigDecimal(targetWight)).divide(new BigDecimal(2), 0, RoundingMode.HALF_UP).intValue();
                        final int xEnd = originWidth.intValue() - xStart;

                        for (int x = 0; x < originWidth.intValue(); x++) {
                            for (int y = 0; y < originHeight.intValue(); y++) {
                                if (xStart <= x && x < xEnd) {
                                    targetImage.setRGB(x - xStart, y, originImage.getRGB(x, y));
                                }
                            }
                        }
                    } else {
                        final int targetWight = originWidth.intValue();
                        final int targetHeight = originWidth.divide(screenPercentage, 0, RoundingMode.HALF_UP).intValue();

                        targetImage = new BufferedImage(targetWight, targetHeight, BufferedImage.TYPE_INT_RGB);

                        final int yStart = originHeight.subtract(new BigDecimal(targetHeight)).divide(new BigDecimal(2), 0, RoundingMode.HALF_UP).intValue();
                        final int yEnd = originHeight.intValue() - yStart;

                        for (int x = 0; x < originWidth.intValue(); x++) {
                            for (int y = 0; y < originHeight.intValue(); y++) {
                                if (yStart <= y && y < yEnd) {
                                    targetImage.setRGB(x, y - yStart, originImage.getRGB(x, y));
                                }
                            }
                        }
                    }
                }

                // 根据屏幕做等比例缩放
                CommonUtil.printLogToConsole("Scale image to the screen ...");
                BufferedImage finallyImage = new BufferedImage(EPaper.width, EPaper.height, BufferedImage.TYPE_BYTE_GRAY);
                Graphics graphics = finallyImage.getGraphics();
                graphics.drawImage(targetImage, 0, 0, EPaper.width, EPaper.height, null);

                return finallyImage;
            }
        }
    }

    /**
     * 获取灰度图
     * @param originImage
     * @return
     */
    public static BufferedImage getGaryImg(BufferedImage originImage) {
        CommonUtil.printLogToConsole("Convert RGB to GRAY ...");

        // 获取原始图片宽高
        final int width = originImage.getWidth();
        final int height = originImage.getHeight();

        BufferedImage targetImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        final int maxValue = 0xf73140, minValue = 0x83fd10;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = originImage.getRGB(i, j);
                int oneGate = rgb & 0xffffff;
                int randomNum = new Random().nextInt(0xffffff);
                int binValue;

                // 0是黑  1是白 ，或者说数值小就靠近黑色，数值大就靠近白色
                if (oneGate > maxValue) { // 大于一定数值，直接用白点
                    binValue = 0xffffff;
                } else if (oneGate < minValue) { // 小于一定数值直接用黑点
                    binValue = 0;
                } else {
                    // 模拟灰阶使用随机数画白点
                    final int random = new Random().nextInt(100);
                    if (oneGate > ((maxValue - minValue) / 2) + minValue) {
                        binValue = random < 60 ? 0xffffff : 0;
                    } else {
                        binValue = random < 80 ? 0 : 0xffffff;
                    }
                }
                targetImage.setRGB(i, j, binValue);
            }
        }

        return targetImage;
    }

}
