package ly.rqmana.huia.java.util.fingerprint;

import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.FingerprintSensorEx;

import java.io.IOException;
import java.util.*;


class ParameterCode {

    public static final int IMAGE_WIDTH = 1;
    public static final int IMAGE_HEIGHT = 2;

    // Image DPI (750/1000 is recommended for children.)
    public static final int IMAGE_DPI = 3;
    public static final int IMAGE_DATA_SIZE = 106;

    // VID&PID (The former two bytes indicate VID and the latter two bytes indicate PID.)
    public static final int VID_PID = 1015;

    // Anti-fake function (1: enable; 0: disable)
    public static final int ANTI_FAKE = 2002;

    // A fingerprint image is true if the lower five bits are all 1's (value&31==31).
    public static final int X =2004;
    public static final int VENDOR_INFO = 1101;
    public static final int PRODUCT_NAME = 1102;
    public static final int DEVICE_SN = 1103;

    public static final int WHITE_LIGHT = 101;
    public static final int GREEN_LIGHT = 102;
    public static final int RED_LIGHT = 103;
    public static final int BUZZING = 104;

    public static final int ANSI_ISO = 10001;

}
