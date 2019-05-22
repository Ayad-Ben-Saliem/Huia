package ly.rqmana.huia.java.util.fingerprint;

import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.FingerprintSensorEx;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class FingerprintSensor {

    private final Set<OnCaptureListener> onCaptureListeners = new HashSet<>();
    private final Set<OnImageCaptureListener> onImageCaptureListeners = new HashSet<>();
    private final Set<OnTemplateCaptureListener> onTemplateCaptureListeners = new HashSet<>();

    //the width of fingerprint image
    private int fingerprintWidth = 0;
    //the height of fingerprint image
    private int fingerprintHeight = 0;
    //for verify test
    private byte[] lastRegTemp = new byte[2048];
    //the length of lastRegTemp
    private int cbRegTemp = 0;
    //pre-register template
    private byte[][] regtemparray = new byte[3][2048];
    //Register
    private boolean bRegister = false;
    //Identify
    private boolean bIdentify = true;
    //finger id
    private int iFid = 1;

    //must be 3
    static final int enroll_cnt = 3;
    //the index of pre-register function
    private int enroll_idx = 0;

    private byte[] imageBuffer = null;
    private int templateLen = 2048;
    private byte[] template = new byte[templateLen];


    private volatile boolean mbStop = true;
    private long deviceHandle = 0;
    private long mhDB = 0;

    public void open() {
        if (System.getProperty("os.name").contains("Windows")) {
            if (deviceHandle != 0) { // Already opened
                System.out.println("Hamster DX already opened, please close device first!");
                return;
            }

            // Initialize
            cbRegTemp = 0;
            bRegister = false;
            bIdentify = false;
            iFid = 1;
            enroll_idx = 0;

            if (FingerprintSensorEx.Init() != FingerprintSensorErrorCode.ZKFP_ERR_OK) {
                System.out.println("Init failed!");
                return;
            }
            int deviceCount = FingerprintSensorEx.GetDeviceCount();
            if (deviceCount < 0) {
                System.out.println("No devices connected!");
                freeSensor();
                return;
            }
            if ((deviceHandle = FingerprintSensorEx.OpenDevice(0)) == 0) {
                System.out.println("Open device fail, index = " + deviceCount + "!");
                freeSensor();
                return;
            }
            if ((mhDB = FingerprintSensorEx.DBInit()) == 0) {
                System.out.println("Init DB fail, index = " + deviceCount + "!");
                freeSensor();
                return;
            }

            //For ISO/Ansi
            int nFmt = 0;    //Ansi
//        if (radioISO.isSelected()) {
//            nFmt = 1;	//ISO
//        }
            FingerprintSensorEx.DBSetParameter(mhDB, ParameterCode.ANSI_ISO, nFmt);
            FingerprintSensorEx.DBSetParameter(mhDB, 5010, nFmt);
            //For ISO/Ansi End

            //set fakefun offs

//        FingerprintSensorEx.SetParameters(deviceHandle, ParameterCode.ANTI_FAKE, changeByte(nFakeFunOn), 4);
            //Set DPI
            int nDPI = 750;
            FingerprintSensorEx.SetParameters(deviceHandle, ParameterCode.IMAGE_DPI, changeByte(nDPI), 4);

            byte[] paramValue = new byte[4];
            int[] size = new int[1];
            //GetFakeOn
            size[0] = 4;
            FingerprintSensorEx.GetParameters(deviceHandle, ParameterCode.ANTI_FAKE, paramValue, size);
//        nFakeFunOn = byteArrayToInt(paramValue);

            size[0] = 4;
            FingerprintSensorEx.GetParameters(deviceHandle, ParameterCode.IMAGE_WIDTH, paramValue, size);
            fingerprintWidth = byteArrayToInt(paramValue);
            size[0] = 4;
            FingerprintSensorEx.GetParameters(deviceHandle, ParameterCode.IMAGE_HEIGHT, paramValue, size);
            fingerprintHeight = byteArrayToInt(paramValue);
            // width = fingerprintSensor.getImageWidth();
            // height = fingerprintSensor.getImageHeight();
            imageBuffer = new byte[fingerprintWidth * fingerprintHeight];
            mbStop = false;
            startCapturing();
            System.out.println("Open succ!");
        }
    }

    public void close() {
        freeSensor();
        System.out.println("Closed");
    }

    public void enroll() {
        if(deviceHandle == 0) {
            System.out.println("Please Open device first!");
            return;
        }
        if(!bRegister) {
            enroll_idx = 0;
            bRegister = true;
            System.out.println("Please your finger 3 times!");
        }
    }

    public void verify() {
        if(deviceHandle == 0) {
            System.out.println("Please Open device first!");
            return;
        }
        if(bRegister) {
            enroll_idx = 0;
            bRegister = false;
        }
        if(bIdentify) {
            bIdentify = false;
        }
    }

    public void identify() {

    }

    public void registerImage() {
        if(mhDB == 0) {
            System.out.println("Please open device first!");
        }
        String path = "d:\\test\\fingerprint.bmp";
        int[] sizeFPTemp = {2048};
        byte[] fpTemplate = new byte[sizeFPTemp[0]];
        int ret = FingerprintSensorEx.ExtractFromImage( mhDB, path, 500, fpTemplate, sizeFPTemp);
        if (ret == 0) {
            ret = FingerprintSensorEx.DBAdd( mhDB, iFid, fpTemplate);
            if (ret == 0) {
                //String base64 = fingerprintSensor.BlobToBase64(fpTemplate, sizeFPTemp[0]);
                iFid++;
                cbRegTemp = sizeFPTemp[0];
                System.arraycopy(fpTemplate, 0, lastRegTemp, 0, cbRegTemp);
                //Base64 Template
                //String strBase64 = Base64.encodeToString(regTemp, 0, ret, Base64.NO_WRAP);
                System.out.println("enroll succ");
            }
            else {
                System.out.println("DBAdd fail, ret=" + ret);
            }
        }
        else {
            System.out.println("ExtractFromImage fail, ret=" + ret);
        }
    }

    public void identifyImage() {
        if(mhDB == 0) {
            System.out.println("Please open device first!");
        }
        String path = "d:\\test\\fingerprint.bmp";
        byte[] fpTemplate = new byte[2048];
        int[] sizeFPTemp = new int[1];
        sizeFPTemp[0] = 2048;
        int ret = FingerprintSensorEx.ExtractFromImage(mhDB, path, 500, fpTemplate, sizeFPTemp);
        if (ret == 0) {
            if (bIdentify) {
                int[] fid = new int[1];
                int[] score = new int [1];
                ret = FingerprintSensorEx.DBIdentify(mhDB, fpTemplate, fid, score);
                if (ret == 0) {
                    System.out.println("Identify succ, fid=" + fid[0] + ",score=" + score[0]);
                }
                else {
                    System.out.println("Identify fail, errcode=" + ret);
                }
            }
            else {
                if(cbRegTemp <= 0) {
                    System.out.println("Please register first!");
                }
                else {
                    ret = FingerprintSensorEx.DBMatch(mhDB, lastRegTemp, fpTemplate);
                    if(ret > 0) {
                        System.out.println("Verify succ, score=" + ret);
                    }
                    else {
                        System.out.println("Verify fail, ret=" + ret);
                    }
                }
            }
        }
        else {
            System.out.println("ExtractFromImage fail, ret=" + ret);
        }
    }

    private void freeSensor() {
        mbStop = true;
        try {		//wait for thread stopping
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (0 != mhDB) {
            FingerprintSensorEx.DBFree(mhDB);
            mhDB = 0;
        }
        if (0 != deviceHandle) {
            FingerprintSensorEx.CloseDevice(deviceHandle);
            deviceHandle = 0;
        }
        FingerprintSensorEx.Terminate();
    }

    public static void writeBitmap(byte[] imageBuf, int nWidth, int nHeight, String path) throws IOException {
        java.io.FileOutputStream fos = new java.io.FileOutputStream(path);
        java.io.DataOutputStream dos = new java.io.DataOutputStream(fos);

        int w = (((nWidth+3)/4)*4);
        int bfType = 0x424d; // 位图文件类型（0—1字节）
        int bfSize = 54 + 1024 + w * nHeight;// bmp文件的大小（2—5字节）
        int bfReserved1 = 0;// 位图文件保留字，必须为0（6-7字节）
        int bfReserved2 = 0;// 位图文件保留字，必须为0（8-9字节）
        int bfOffBits = 54 + 1024;// 文件头开始到位图实际数据之间的字节的偏移量（10-13字节）

        dos.writeShort(bfType); // 输入位图文件类型'BM'
        dos.write(changeByte(bfSize), 0, 4); // 输入位图文件大小
        dos.write(changeByte(bfReserved1), 0, 2);// 输入位图文件保留字
        dos.write(changeByte(bfReserved2), 0, 2);// 输入位图文件保留字
        dos.write(changeByte(bfOffBits), 0, 4);// 输入位图文件偏移量

        int biSize = 40;// 信息头所需的字节数（14-17字节）
        int biWidth = nWidth;// 位图的宽（18-21字节）
        int biHeight = nHeight;// 位图的高（22-25字节）
        int biPlanes = 1; // 目标设备的级别，必须是1（26-27字节）
        int biBitcount = 8;// 每个像素所需的位数（28-29字节），必须是1位（双色）、4位（16色）、8位（256色）或者24位（真彩色）之一。
        int biCompression = 0;// 位图压缩类型，必须是0（不压缩）（30-33字节）、1（BI_RLEB压缩类型）或2（BI_RLE4压缩类型）之一。
        int biSizeImage = w * nHeight;// 实际位图图像的大小，即整个实际绘制的图像大小（34-37字节）
        int biXPelsPerMeter = 0;// 位图水平分辨率，每米像素数（38-41字节）这个数是系统默认值
        int biYPelsPerMeter = 0;// 位图垂直分辨率，每米像素数（42-45字节）这个数是系统默认值
        int biClrUsed = 0;// 位图实际使用的颜色表中的颜色数（46-49字节），如果为0的话，说明全部使用了
        int biClrImportant = 0;// 位图显示过程中重要的颜色数(50-53字节)，如果为0的话，说明全部重要

        dos.write(changeByte(biSize), 0, 4);// 输入信息头数据的总字节数
        dos.write(changeByte(biWidth), 0, 4);// 输入位图的宽
        dos.write(changeByte(biHeight), 0, 4);// 输入位图的高
        dos.write(changeByte(biPlanes), 0, 2);// 输入位图的目标设备级别
        dos.write(changeByte(biBitcount), 0, 2);// 输入每个像素占据的字节数
        dos.write(changeByte(biCompression), 0, 4);// 输入位图的压缩类型
        dos.write(changeByte(biSizeImage), 0, 4);// 输入位图的实际大小
        dos.write(changeByte(biXPelsPerMeter), 0, 4);// 输入位图的水平分辨率
        dos.write(changeByte(biYPelsPerMeter), 0, 4);// 输入位图的垂直分辨率
        dos.write(changeByte(biClrUsed), 0, 4);// 输入位图使用的总颜色数
        dos.write(changeByte(biClrImportant), 0, 4);// 输入位图使用过程中重要的颜色数

        for (int i = 0; i < 256; i++) {
            dos.writeByte(i);
            dos.writeByte(i);
            dos.writeByte(i);
            dos.writeByte(0);
        }

        byte[] filter = null;
        if (w > nWidth) {
            filter = new byte[w-nWidth];
        }

        for(int i=0;i<nHeight;i++) {
            dos.write(imageBuf, (nHeight-1-i)*nWidth, nWidth);
            if (w > nWidth)
                dos.write(filter, 0, w-nWidth);
        }
        dos.flush();
        dos.close();
        fos.close();
    }

    public static byte[] changeByte(int data) {
        return intToByteArray(data);
    }

    public static byte[] intToByteArray (final int number) {
        byte[] abyte = new byte[4];
        abyte[0] = (byte) ( 0x000000ff & number);
        abyte[1] = (byte) ((0x0000ff00 & number) >> 8);
        abyte[2] = (byte) ((0x00ff0000 & number) >> 16);
        abyte[3] = (byte) ((0xff000000 & number) >> 24);
        return abyte;
    }

    public static int byteArrayToInt(byte[] bytes) {
        int number = bytes[0]       & 0x000000FF;
        number |= ((bytes[1] << 8)  & 0x0000FF00);
        number |= ((bytes[2] << 16) & 0x00FF0000);
        number |= ((bytes[3] << 24) & 0xFF000000);
        return number;
    }

    public void startCapturing() {
        int[] size = {2048};
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if (FingerprintSensorEx.AcquireFingerprint(deviceHandle, imageBuffer, template, size) == 0) {
                onCaptureListeners.forEach(onCaptureListener -> {
                    onCaptureDone(imageBuffer.clone());
                    onExtractDone(template.clone(), size[0]);
                    onCaptureListener.onCapture(imageBuffer.clone(), template.clone());
                });
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    public boolean isOpened() {
        return deviceHandle != 0;
    }

    public boolean isClosed() {
        return !isOpened();
    }

    private void onCaptureDone(byte[] imgBuf) {
        try {
            writeBitmap(imgBuf, fingerprintWidth, fingerprintHeight, "fingerprint.bmp");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    byte[] merge(byte[]... templates) {
        byte[] resultTemplate = new byte[2048];
        if (FingerprintSensorEx.DBMerge(mhDB, templates[0], templates[1], templates[2], resultTemplate, new int[]{2048}) == 0 ) {
            return resultTemplate;
        }
        return null;
    }

    private void onExtractDone(byte[] template, int size) {
        if (bRegister) {
            int[] fid = new int[1];
            int[] score = new int [1];
            int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid, score);
            if (ret == 0) {
                System.out.println("the finger already enroll by " + fid[0] + ",cancel enroll");
                bRegister = false;
                enroll_idx = 0;
                return;
            }
            if (enroll_idx > 0 && FingerprintSensorEx.DBMatch(mhDB, regtemparray[enroll_idx-1], template) <= 0) {
                System.out.println("please press the same finger 3 times for the enrollment");
                return;
            }
            System.arraycopy(template, 0, regtemparray[enroll_idx], 0, 2048);
            enroll_idx++;
            if (enroll_idx == 3) {
                int[] _retLen = new int[1];
                _retLen[0] = 2048;
                byte[] regTemp = new byte[_retLen[0]];

                if (    (ret = FingerprintSensorEx.DBMerge(mhDB, regtemparray[0], regtemparray[1], regtemparray[2], regTemp, _retLen)) == 0 &&
                        (ret = FingerprintSensorEx.DBAdd(mhDB, iFid, regTemp)) == 0) {
                    iFid++;
                    cbRegTemp = _retLen[0];
                    System.arraycopy(regTemp, 0, lastRegTemp, 0, cbRegTemp);
                    String strBase64 = FingerprintSensorEx.BlobToBase64(regTemp, cbRegTemp);
                    //Base64 Template
                    System.out.println("enroll succ");
                } else {
                    System.out.println("enroll fail, error code=" + ret);
                }
                bRegister = false;
            } else {
                System.out.println("You need to press the " + (3 - enroll_idx) + " times fingerprint");
            }
        }
        else if (bIdentify) {
            int[] fid = new int[1];
            int[] score = new int [1];
            int ret = FingerprintSensorEx.DBIdentify(mhDB, template, fid, score);
            if (ret == 0) {
                System.out.println("Identify succ, fid=" + fid[0] + ",score=" + score[0]);
            }
            else {
                System.out.println("Identify fail, errcode=" + ret);
            }
        }
        else if (cbRegTemp <= 0) {
            System.out.println("Please register first!");
        }
        else {
            int ret = FingerprintSensorEx.DBMatch(mhDB, lastRegTemp, template);
            if(ret > 0) {
                System.out.println("Verify succ, score=" + ret);
            }
            else {
                System.out.println("Verify fail, ret=" + ret);
            }
        }
    }

    public void setOnCaptureListener(OnCaptureListener captureListener) {
        onCaptureListeners.clear();
        onCaptureListeners.add(captureListener);
    }

    public void addOnCaptureListener(OnCaptureListener captureListener) {
        onCaptureListeners.add(captureListener);
    }

    public void setOnImageCaptureListener(OnImageCaptureListener captureListener) {
        onImageCaptureListeners.clear();
        onImageCaptureListeners.add(captureListener);
    }

    public void addOnImageCaptureListener(OnImageCaptureListener captureListener) {
        onImageCaptureListeners.add(captureListener);
    }

    public void setOnTemplateCaptureListener(OnTemplateCaptureListener captureListener) {
        onTemplateCaptureListeners.clear();
        onTemplateCaptureListeners.add(captureListener);
    }

    public void addOnTemplateCaptureListener(OnTemplateCaptureListener captureListener) {
        onTemplateCaptureListeners.add(captureListener);
    }

    public static interface OnCaptureListener {
        void onCapture(byte[] imageBuffer, byte[] template);
    }

    public static interface OnImageCaptureListener {
        void onCapture(byte[] imageBuffer);
    }

    public static interface OnTemplateCaptureListener {
        void onCapture(byte template);
    }
}
