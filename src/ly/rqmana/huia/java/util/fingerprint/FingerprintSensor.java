package ly.rqmana.huia.java.util.fingerprint;

import com.zkteco.biometric.FingerprintCaptureListener;
import com.zkteco.biometric.FingerprintSensorErrorCode;

import java.util.HashMap;
import java.util.Map;

public class FingerprintSensor extends com.zkteco.biometric.FingerprintSensor {

    private Map<String, OnCaptureListener> onCaptureListeners = new HashMap<>();

    FingerprintSensor() {
        super();
    }

    @Override
    public int openDevice(int index) {
        int ret = super.openDevice(0);
        if (ret != FingerprintSensorErrorCode.ERROR_OPEN_FAIL) {
            super.setFingerprintCaptureListener(new FingerprintCaptureListener() {
                byte[] imageBuffer;

                @Override
                public void captureOK(byte[] imageBuffer) {
                    this.imageBuffer = imageBuffer;
                }

                @Override
                public void captureError(int i) { }

                @Override
                public void extractOK(byte[] template) {
                    onCaptureListeners.forEach((key, onCaptureListener) -> onCaptureListener.onCapture(imageBuffer.clone(), template.clone()));
                }
            });
        }
        return ret;
    }

    public boolean isOpened() {
        byte[] values = new byte[100];
        int[] len = {100};
        return GetParameter(ParameterCode.PRODUCT_NAME, values, len) != FingerprintSensorErrorCode.ERROR_NOT_OPENED;
    }

    @Override
    @Deprecated
    public void setFingerprintCaptureListener(FingerprintCaptureListener listener) {
    }

    public void addOnCaptureListener(String key, OnCaptureListener listener) {
        onCaptureListeners.put(key, listener);
    }

    public void removeOnCaptureListener(String key) {
        onCaptureListeners.remove(key);
    }

    public boolean isClosed() {
        return !isOpened();
    }

    public interface OnCaptureListener {
        void onCapture(byte[] imageBuffer, byte[] template);
    }
}
