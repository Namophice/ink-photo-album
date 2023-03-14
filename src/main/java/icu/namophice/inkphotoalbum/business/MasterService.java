package icu.namophice.inkphotoalbum.business;

import icu.namophice.inkphotoalbum.config.DefaultConfig;
import icu.namophice.inkphotoalbum.driver.EPaper;
import icu.namophice.inkphotoalbum.utils.CommonUtil;
import icu.namophice.inkphotoalbum.utils.ImageUtil;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
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

            final StringBuilder imageUrlStr = new StringBuilder();
            try {
                final URL url = new URL(DefaultConfig.imageUrlArr[new Random().nextInt(DefaultConfig.imageUrlArr.length)]);

                trustAllHttpsCertificates();
                HostnameVerifier hv = (urlHostName, session) -> {
                    System.out.println("Warning: URL Host: " + urlHostName + " vs. " + session.getPeerHost());
                    return true;
                };
                HttpsURLConnection.setDefaultHostnameVerifier(hv);

                final HttpURLConnection conn = (HttpURLConnection) (url.openConnection());
                final URL imageUrl = conn.getURL();
                imageUrlStr.append(imageUrl.getProtocol()).append("://").append(imageUrl.getHost()).append(imageUrl.getPath());
            } catch (IOException e) {
                DefaultConfig.imageIndex = 0;
                throw e;
            }

            if (imageUrlStr.length() > 0) {
                BufferedImage targetImage = ImageUtil.getImageToScreen(imageUrlStr.toString(), true);

                CommonUtil.printLogToConsole("Print images to screen ...");
                ePaper.drawImage(targetImage);
                DefaultConfig.imageIndex++;
            }
        }
    }

    /**
     * 跳过SSL证书验证
     * @throws Exception
     */
    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    private static class miTM implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

}
