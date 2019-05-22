package ly.rqmana.huia.java.util.fingerprint;

//import com.zkteco.biometric.FingerprintSensor;

public class FingerprintManager {

    public static final FingerprintSensor SENSOR = new FingerprintSensor();

    static {
        SENSOR.open();
    }

    private final Hand rightHand = new Hand();
    private final Hand leftHand = new Hand();

    public static FingerprintSensor getSensor() {
        return SENSOR;
    }

    public Hand getRightHand() {
        return rightHand;
    }

    public Hand getLeftHand() {
        return leftHand;
    }

    public byte[] mergeTemplates(byte[] ...  templates) {
        return SENSOR.merge(templates);
    }
}
