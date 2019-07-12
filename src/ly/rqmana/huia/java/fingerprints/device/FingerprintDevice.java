package ly.rqmana.huia.java.fingerprints.device;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.fingerprints.FingerprintCaptureResult;
import ly.rqmana.huia.java.fingerprints.device.impl.HamsterDX;
import ly.rqmana.huia.java.fingerprints.hand.Finger;
import ly.rqmana.huia.java.fingerprints.hand.FingerID;
import ly.rqmana.huia.java.fingerprints.SecurityLevel;

import java.io.Closeable;
import java.io.IOException;

public abstract class FingerprintDevice implements Closeable {

    public static FingerprintDevice getInstance(final FingerprintDeviceType deviceType,
                                         long timoutMillis,
                                         SecurityLevel securityLevel){

        switch (deviceType){
            case HAMSTER_DX:
                return new HamsterDX(timoutMillis, securityLevel);
        }

        throw new IllegalStateException("Unknown FingerprintDeviceType: " + deviceType);
    }


    private final ObjectProperty<SecurityLevel> securityLevel = new SimpleObjectProperty<>(SecurityLevel.LOWEST);
    private final LongProperty timeoutMillis = new SimpleLongProperty(30 * 1000);

    public FingerprintDevice(long timeoutMillis, SecurityLevel securityLevel){
        setSecurityLevel(securityLevel);
        setTimeoutMillis(timeoutMillis);
    }

    public abstract Finger captureFinger(FingerID fingerID);

    public abstract FingerprintCaptureResult captureHands();

    /**
     * captures a new finger using the device and then matches it with the supplied {@code target}
     */
    public Task<Boolean> matchFinger(Finger target){
        Finger sourceFinger = captureFinger(target.getId());
        return matchFingerprintTemplate(sourceFinger.getFingerprintTemplate(), target.getFingerprintTemplate());
    }

    public Task<Boolean> matchFinger(Finger source, Finger target){
        return matchFingerprintTemplate(source.getFingerprintTemplate(), target.getFingerprintTemplate());
    }

    public abstract Task<Boolean> matchFingerprintTemplate(String source, String target);

    public abstract void open() throws IOException;

    public abstract boolean isOpen();

    public abstract void close() throws IOException;


    public abstract boolean isTimeoutSupported();

    public abstract boolean isFingerImageSupported();

    public abstract boolean isHandCaptureSupported();


    public SecurityLevel getSecurityLevel() {
        return securityLevel.get();
    }

    public ObjectProperty<SecurityLevel> securityLevelProperty() {
        return securityLevel;
    }

    public void setSecurityLevel(SecurityLevel securityLevel) {
        this.securityLevel.set(securityLevel);
    }

    public long getTimeoutMillis() {
        return timeoutMillis.get();
    }

    public LongProperty timeoutMillisProperty() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis.set(timeoutMillis);
    }
}

