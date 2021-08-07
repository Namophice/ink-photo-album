package icu.namophice.inkphotoalbum.business;

import icu.namophice.inkphotoalbum.config.DefaultConfig;
import icu.namophice.inkphotoalbum.driver.EPaper;
import icu.namophice.inkphotoalbum.utils.CommonUtil;
import icu.namophice.inkphotoalbum.utils.ImageUtil;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Namophice
 * @createTime 2021-08-05 10:27
 */
public class MasterService {

    public static void master() throws IOException, InterruptedException {
        CommonUtil.printLogToConsole("The master program is running ...");

        if (DefaultConfig.imageIndex < 0) {
            DefaultConfig.imageIndex = 0;
        }

        EPaper ePaper = EPaper.getInstance();
        ePaper.init();

        if (DefaultConfig.imageIndex == 0) {
            BufferedImage defaultImage = ImageUtil.getImageToScreen();

            CommonUtil.printLogToConsole("Print images to screen ...");
            ePaper.drawImage(defaultImage);
        }

        DefaultConfig.defaultImageIsLoaded = true;

        if (DefaultConfig.local_images_mode()) { // local
            CommonUtil.printLogToConsole("Enabled local images mode ...");

            CommonUtil.printLogToConsole("Find images directory ...");
            File imagesDir = new File(CommonUtil.rootPath + "/" + DefaultConfig.imagePath);

            if (imagesDir.exists()) {
                if (imagesDir.isDirectory()) {
                    CommonUtil.printLogToConsole("Images directory is exists ...");

                    CommonUtil.printLogToConsole("Find images files ...");
                    File[] imageList = imagesDir.listFiles();
                    imagesDir = null;

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

            StringBuffer htmlStr = new StringBuffer();

            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader bufferedReader = null;

            try {
                URL url = new URL("https://wall.alphacoders.com/popular.php?page=" + (DefaultConfig.imageIndex + 1));
                inputStream = url.openStream();
                inputStreamReader = new InputStreamReader(inputStream, "utf-8");
                bufferedReader = new BufferedReader(inputStreamReader);

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    htmlStr.append(line).append("\n");
                }

                bufferedReader.close();
            } catch (IOException e) {
                DefaultConfig.imageIndex = 0;
                throw e;
            }finally {
                try {
                    if(bufferedReader!=null){
                        bufferedReader.close();
                        bufferedReader=null;
                    }
                    if(inputStreamReader!=null){
                        inputStreamReader.close();
                        inputStreamReader=null;
                    }
                    if(inputStream!=null){
                        inputStream.close();
                        inputStream=null;
                    }
                } catch (IOException e) {
                    throw e;
                }
            }

            if (htmlStr.length() > 0) {
                final List<String> imageUrlList = new ArrayList<>();

                final String IMGURL_REG = "(https://images(.*)thumbbig-(\\d*).jpg)";
                Matcher matcher = Pattern.compile(IMGURL_REG).matcher(htmlStr);
                while (matcher.find()){
                    imageUrlList.add(matcher.group());
                }

                if (imageUrlList.size() < 1) {
                    DefaultConfig.imageIndex = 0;
                    return;
                }

                String imageUrl = imageUrlList.get(new Random().nextInt(imageUrlList.size()));
                imageUrl = imageUrl.replace("/thumbbig-", "/");

                BufferedImage targetImage = ImageUtil.getImageToScreen(imageUrl, true);

                CommonUtil.printLogToConsole("Print images to screen ...");
                ePaper.drawImage(targetImage);

                DefaultConfig.imageIndex++;
            }
        }
    }

}
