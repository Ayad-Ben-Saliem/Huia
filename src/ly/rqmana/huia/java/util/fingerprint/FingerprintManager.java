package ly.rqmana.huia.java.util.fingerprint;

import com.zkteco.biometric.FingerprintSensorErrorCode;
import com.zkteco.biometric.FingerprintSensorEx;
import ly.rqmana.huia.java.util.Windows;

import java.io.ByteArrayInputStream;

public class FingerprintManager {

    public static final FingerprintSensor SENSOR = new FingerprintSensor();

    static {
        int ret = SENSOR.openDevice(0);
        if (ret == FingerprintSensorErrorCode.ERROR_OPEN_FAIL) {
            System.out.println("SENSOR doesn't opened.");
        } else {
            System.out.println("SENSOR has opened.");
            SENSOR.startCapture();
            Windows.MAIN_WINDOW.addOnCloseRequest(event -> SENSOR.closeDevice());
        }
    }

    private final Hand rightHand = new Hand();
    private final Hand leftHand = new Hand();

    public static FingerprintSensor getSensor() {
        return SENSOR;
    }

    public static int MatchFP(byte[] template1, byte[] template2) {
        if (template1 == null || template2 == null) {
            return -1;
        }
        return SENSOR.MatchFP(template1, template2);
    }

    public Hand getRightHand() {
        return rightHand;
    }

    public Hand getLeftHand() {
        return leftHand;
    }

    public byte[] mergeTemplates(byte[] template1, byte[] template2, byte[] template3) {
        byte[] resultTemplate = new byte[2048];
        if (FingerprintSensorEx.DBMerge(1, template1, template2, template3, resultTemplate, new int[]{2048}) == 0 ) {
            return resultTemplate;
        }
        return null;
    }

    public static ByteArrayInputStream templateToByteArrayInputStream(Finger finger) {
        if (finger == null) return null;
        return templateToByteArrayInputStream(finger.getFingerprintTemplate());
    }

    public static ByteArrayInputStream templateToByteArrayInputStream(byte[] template) {
        if (template == null)
            return new ByteArrayInputStream(new byte[]{});
        return new ByteArrayInputStream(template);
    }
}
