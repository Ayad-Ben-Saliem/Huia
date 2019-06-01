package ly.rqmana.huia.java.storage;

import com.sun.istack.internal.NotNull;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.hand.Finger;
import ly.rqmana.huia.java.fingerprints.hand.Hand;
import ly.rqmana.huia.java.util.OSValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DataStorage {

    public static final String WINDOWS_DATA_DIR = "C:/ProgramData/Huia/";
    public static final String UNIX_DATA_DIR = System.getProperty("user.home") + "/.Huia/";
    public static final String MAC_DATA_DIR = System.getProperty("user.home") + "/Huia/";


    public static final String FINGERPRINTS_DIR_NAME = "Fingerprints/";
    public static final String NEW_REG_DIR_NAME = "New Registrations/";
    public static final String TEMP_FINGERPRINT_IMAGES = getNewRegDir() + "Temp/";

    public static String getDataDir() {
        if (OSValidator.isWindows()) {
            return WINDOWS_DATA_DIR;
        } else if(OSValidator.isUnix()) {
            return UNIX_DATA_DIR;
        } else if (OSValidator.isMac()) {
            return MAC_DATA_DIR;
        }
        return null;
    }

    public static String getFingerprintsDir() {
        return getDataDir() + FINGERPRINTS_DIR_NAME;
    }

    public static String getNewRegDir() {
        return getFingerprintsDir() + NEW_REG_DIR_NAME;
    }

    public static String getNewRegFingerprintDir(@NotNull String institute, @NotNull String fullName, @NotNull String workId) {
        if (institute == null || institute.isEmpty())
            throw new RuntimeException("Invalid institute");
        if (fullName == null || fullName.isEmpty())
            throw new RuntimeException("Invalid name");
        if (workId == null || workId.isEmpty())
            throw new RuntimeException("Invalid work id");

        String dirName = fullName + " (" + workId + ")";
        return getNewRegDir() + institute + fullName;
    }

    public static void saveNewFingerprintImages(String institute, String fullName, String workId, Hand rightHand, Hand leftHand) {
        String newDir = getNewRegFingerprintDir(institute, fullName, workId);
        try {
            String path = Files.createDirectories(Paths.get(newDir)).toString();

//            saveImage( path + "/RightThumb", rightHand.getThumbFinger());
//            saveImage( path + "/RightIndex", rightHand.getIndexFinger());
//            saveImage( path + "/RightMiddle", rightHand.getMiddleFinger());
//            saveImage( path + "/RightRing", rightHand.getRingFinger());
//            saveImage( path + "/RightLittle", rightHand.getLittleFinger());
//
//            saveImage( path + "/LeftThumb", leftHand.getThumbFinger());
//            saveImage( path + "/LeftIndex", leftHand.getIndexFinger());
//            saveImage( path + "/LeftMiddle", leftHand.getMiddleFinger());
//            saveImage( path + "/LeftRing", leftHand.getRingFinger());
//            saveImage( path + "/LeftLittle", leftHand.getLittleFinger());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//
//    private static void saveImage(String path, Finger finger) {
//        if (finger == null) return;
//        for (int i = 0; i < finger.getFingerprintImages().size(); i++) {
//            try {
//                writeBitmap(finger.getFingerprintImages().get(i), FingerprintManager.device().getImageWidth(), FingerprintManager.device().getImageHeight(), path + (i+1) + ".bmp");
////                BufferedImage image = ImageIO.read( new ByteArrayInputStream( finger.getFingerprintImages().get(i)));
////                ImageIO.write(image, "BMP", new File(path + (i+1) + ".bmp"));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

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
        if (w > nWidth)
        {
            filter = new byte[w-nWidth];
        }

        for(int i=0;i<nHeight;i++)
        {
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
        // "&" 与（AND），对两个整型操作数中对应位执行布尔代数，两个位都为1时输出1，否则0。
        abyte[0] = (byte) (0xff & number);
        // ">>"右移位，若为正数则高位补0，若为负数则高位补1
        abyte[1] = (byte) ((0xff00 & number) >> 8);
        abyte[2] = (byte) ((0xff0000 & number) >> 16);
        abyte[3] = (byte) ((0xff000000 & number) >> 24);
        return abyte;
    }
}
