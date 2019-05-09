package ly.rqmana.huia.java.util.fingerprint;

//import com.zkteco.biometric.FingerprintSensor;

import com.zkteco.biometric.FingerprintSensorEx;

public class FingerprintManager {

    private FingerprintSensor sensor = new FingerprintSensor();
    private Hand rightHand = new Hand();
    private Hand leftHand = new Hand();

    public FingerprintSensor getSensor() {
        return sensor;
    }

    public void setSensor(FingerprintSensor sensor) {
        this.sensor = sensor;
    }

    public Hand getRightHand() {
        return rightHand;
    }

    public void setRightHand(Hand rightHand) {
        this.rightHand = rightHand;
    }

    public Hand getLeftHand() {
        return leftHand;
    }

    public void setLeftHand(Hand leftHand) {
        this.leftHand = leftHand;
    }

    public byte[] mergeTemplates(byte[] ...  templates) {
        return sensor.merge(templates);
    }
}
