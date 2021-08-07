package icu.namophice.inkphotoalbum.driver;

import com.pi4j.io.gpio.*;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import icu.namophice.inkphotoalbum.utils.CommonUtil;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;

/**
 * @author Namophice
 * @createTime 2021-07-29 17:00
 */
public class EPaper {

    public static final int width = 800;
    public static final int height = 480;
    public static final long resolution = width * height;

    private EPaper() {}

    private static EPaper ePaper;

    public static EPaper getInstance() {
        return ePaper = new EPaper();
    }

    private static GpioPinDigitalOutput CS;
    private static GpioPinDigitalOutput DC;
    private static GpioPinDigitalOutput RST;
    private static GpioPinDigitalInput BUSY;

    public static SpiDevice spiDevice;

    static {
        RaspiGpioProvider raspiGpioProvider = new RaspiGpioProvider(RaspiPinNumberingScheme.BROADCOM_PIN_NUMBERING);

        final GpioController gpio = GpioFactory.getInstance();

        CS = gpio.provisionDigitalOutputPin(raspiGpioProvider, RaspiBcmPin.GPIO_08, "CS", PinState.HIGH);
        DC = gpio.provisionDigitalOutputPin(raspiGpioProvider, RaspiBcmPin.GPIO_25, "DC", PinState.HIGH);
        RST = gpio.provisionDigitalOutputPin(raspiGpioProvider, RaspiBcmPin.GPIO_17, "RST", PinState.HIGH);
        BUSY = gpio.provisionDigitalInputPin(raspiGpioProvider, RaspiBcmPin.GPIO_24, "BUSY");

        try {
            spiDevice = SpiFactory.getInstance(SpiChannel.CS1, SpiDevice.DEFAULT_SPI_SPEED, SpiDevice.DEFAULT_SPI_MODE);
        } catch (Exception e) {
            CommonUtil.printErrorToLogFile(e);
        }
    }

    /**
     * 绘制图片到屏幕
     * @param image
     * @throws IOException
     * @throws InterruptedException
     */
    public void drawImage(BufferedImage image) throws IOException, InterruptedException {
            this.initLut();
            this.clear();
            this.display(image);
    }

    /**
     * 重置屏幕
     * @throws InterruptedException
     */
    public void reset() throws InterruptedException {
        CommonUtil.printLogToConsole("Reset the screen ...");

        RST.high();
        Thread.sleep(200);
        RST.low();
        Thread.sleep(10);
        RST.high();
        Thread.sleep(200);
    }

    /**
     * 写入指令
     * @param data
     * @throws IOException
     */
    public void sendCommand(int data) throws IOException {
        DC.low();
        CS.low();
        spiDevice.write((byte) data);
        CS.high();
    }

    /**
     * 写入数据
     * @param data
     * @throws IOException
     */
    public void sendData(int data) throws IOException {
        DC.high();
        CS.low();
        spiDevice.write((byte) data);
        CS.high();
    }

    /**
     * 检查屏幕是否Busy
     * @throws IOException
     * @throws InterruptedException
     */
    public void checkBusy() throws IOException, InterruptedException {
        CommonUtil.printLogToConsole("Check whether the screen is busy ...");

        this.sendCommand((byte) 0x71);
        while (BUSY.isLow()) {
            this.sendCommand((byte) 0x71);
            Thread.sleep(200);
        }
    }

    public void init() throws IOException, InterruptedException {
        CommonUtil.printLogToConsole("Init the driver ...");

        reset();

        this.sendCommand(0x01); // power setting
        this.sendData(0x17); // 1-0=11: internal power
        this.sendData(Voltage_Frame_7IN5_V2[6]); // VGH&VGL
        this.sendData(Voltage_Frame_7IN5_V2[1]); // VSH
        this.sendData(Voltage_Frame_7IN5_V2[2]); // VSL
        this.sendData(Voltage_Frame_7IN5_V2[3]); // VSHR

        this.sendCommand(0x82); // # VCOM DC Setting
        this.sendData(Voltage_Frame_7IN5_V2[4]); // VCOM

        this.sendCommand(0x06); // Booster Setting
        this.sendData(0x27);
        this.sendData(0x27);
        this.sendData(0x2F);
        this.sendData(0x17);

        this.sendCommand(0x30); // OSC Setting
        this.sendData(Voltage_Frame_7IN5_V2[0]); // 2-0=100: N=4   5-3=111: M=7    3C=50Hz     3A=100HZ

        this.sendCommand(0x04); // power on
        this.checkBusy();

        this.sendCommand(0X00); // PANNEL SETTING
        this.sendData(0x3F); // #KW-3f   KWR-2F	BWROTP 0f	BWOTP 1f

        this.sendCommand(0x61); // tres
        this.sendData(0x03); // source 800
        this.sendData(0x20);
        this.sendData(0x01); // gate 480
        this.sendData(0xE0);

        this.sendCommand(0X15);
        this.sendData(0x00);

        this.sendCommand(0X50); // VCOM AND DATA INTERVAL SETTING
        this.sendData(0x10);
        this.sendData(0x07);

        this.sendCommand(0X60); // TCON SETTING
        this.sendData(0x22);

        this.sendCommand(0x65); // Resolution setting
        this.sendData(0x00);
        this.sendData(0x00); // 800*480
        this.sendData(0x00);
        this.sendData(0x00);
    }

    /**
     * 初始化LUT
     * @throws IOException
     */
    public void initLut() throws IOException {
        CommonUtil.printLogToConsole("Set LUT ...");

        this.sendCommand(0x20);
        for (int data : LUT_VCOM_7IN5_V2) {
            this.sendData(data);
        }

        this.sendCommand(0x21);
        for (int data : LUT_WW_7IN5_V2) {
            this.sendData(data);
        }

        this.sendCommand(0x22);
        for (int data : LUT_BW_7IN5_V2) {
            this.sendData(data);
        }

        this.sendCommand(0x23);
        for (int data : LUT_WB_7IN5_V2) {
            this.sendData(data);
        }

        this.sendCommand(0x24);
        for (int data : LUT_BB_7IN5_V2) {
            this.sendData(data);
        }
    }

    /**
     * 清屏
     * @throws IOException
     * @throws InterruptedException
     */
    public void clear() throws IOException, InterruptedException {
        CommonUtil.printLogToConsole("Clear screen ...");

        this.initLut();

        this.sendCommand(0x10);

        for (int i = 0; i < resolution; i++) {
            this.sendData(0xFF);
        }

        this.sendCommand(0x13);

        for (int i = 0; i < resolution; i++) {
            this.sendData(0xFF);
        }

        this.sendCommand(0x12);

        this.checkBusy();
    }

    /**
     * 绘制屏幕画面
     * @param image
     * @throws IOException
     * @throws InterruptedException
     */
    public void display(BufferedImage image) throws IOException, InterruptedException {
        final byte[] pixels = ((DataBufferByte)(image.getRaster().getDataBuffer())).getData();
        this.display(pixels);
    }

    /**
     * 绘制屏幕画面
     * @param pixels
     * @throws IOException
     * @throws InterruptedException
     */
    public void display(byte[] pixels) throws IOException, InterruptedException {
        CommonUtil.printLogToConsole("Print image to screen ...");

        this.initLut();

        this.sendCommand(0x13);

        int step = 0;
        int temp = 0;

        for (int i = 0; i < 800 * 480; i++) {
            int grayNum = pixels[i] & 0xFF;
            if (grayNum < GRAY_SCALE_OF_4[0]) {
                temp |= 1;
            } else if(grayNum < GRAY_SCALE_OF_4[1]){
                temp |= 0;
            }else if (grayNum < GRAY_SCALE_OF_4[2]) {
                temp |= 1;
            } else if(grayNum <= GRAY_SCALE_OF_4[3]){
                temp |= 0;
            }

            step++;
            if (step != 8) {
                temp = temp << 1;
            } else {
                step = 0;
                this.sendData(temp);
                temp = 0;
            }
        }

        this.sendCommand(0x12);

        this.checkBusy();
    }

    private final static int[] Voltage_Frame_7IN5_V2 = { 0x6, 0x3F, 0x3F, 0x11, 0x24, 0x7, 0x17 };

    private static final int[] GRAY_SCALE_OF_4 = {0xff / 4, 0xff / 4 * 2, 0xff / 4 * 3, 0xff};

    private final static int[] LUT_VCOM_7IN5_V2 = {
        0x0, 0xF, 0xF, 0x0, 0x0, 0x1,
        0x0, 0xF, 0x1, 0xF, 0x1, 0x2,
        0x0, 0xF, 0xF, 0x0, 0x0, 0x1,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0
    };

    private final static int[] LUT_WW_7IN5_V2 = {
        0x10, 0xF, 0xF, 0x0, 0x0, 0x1,
        0x84, 0xF, 0x1, 0xF, 0x1, 0x2,
        0x20, 0xF, 0xF, 0x0, 0x0, 0x1,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0
    };

    private final static int[] LUT_BW_7IN5_V2 = {
        0x10, 0xF, 0xF, 0x0, 0x0, 0x1,
        0x84, 0xF, 0x1, 0xF, 0x1, 0x2,
        0x20, 0xF, 0xF, 0x0, 0x0, 0x1,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0
    };

    private final static int[] LUT_WB_7IN5_V2 = {
        0x80, 0xF, 0xF, 0x0, 0x0, 0x1,
        0x84, 0xF, 0x1, 0xF, 0x1, 0x2,
        0x40, 0xF, 0xF, 0x0, 0x0, 0x1,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0
    };

    private final static int[] LUT_BB_7IN5_V2 = {
        0x80, 0xF, 0xF, 0x0, 0x0, 0x1,
        0x84, 0xF, 0x1, 0xF, 0x1, 0x2,
        0x40, 0xF, 0xF, 0x0, 0x0, 0x1,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0,
        0x0, 0x0, 0x0, 0x0, 0x0, 0x0
    };

}
