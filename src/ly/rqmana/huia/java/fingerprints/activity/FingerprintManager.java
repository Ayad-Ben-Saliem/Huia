package ly.rqmana.huia.java.fingerprints.activity;

import com.sun.istack.internal.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.fingerprints.SecurityLevel;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDevice;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;

import java.io.IOException;

public class FingerprintManager {

    private FingerprintManager(){

    }

    private static final SecurityLevel DEFAULT_SECURITY_LEVEL = SecurityLevel.HIGH;
    private static final long DEFAULT_TIMOUT = 20 * 1000;

    private static final ObjectProperty<FingerprintDevice> device = new SimpleObjectProperty<>(null);

    /**
     * provides access to {@link FingerprintDevice} currently {@link FingerprintManager} uses
     * @return the currently used device in the Fingerprint
     */
    public static FingerprintDevice device() {
        if (device.get() == null || ! device.get().isOpen()){
            throw new FingerprintDeviceNotOpenedException("Fingerprint device is not opened and cannot handle operations.");
        }
        return device.get();
    }

    public static Task<Boolean> openDeviceIfNotOpen(@NotNull FingerprintDeviceType deviceType){

        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return true;
            }
        };

        if (! isDeviceOpen())
            task = openDevice(deviceType);

        return task;
    }

    public static Task<Boolean> openDevice(@NotNull FingerprintDeviceType deviceType){
        return openDevice(deviceType, DEFAULT_TIMOUT, DEFAULT_SECURITY_LEVEL);
    }

    public static Task<Boolean> openDevice(@NotNull FingerprintDeviceType deviceType, long timoutMillis, SecurityLevel securityLevel){
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                FingerprintDevice fingerprintDevice = FingerprintDevice.getInstance(deviceType, timoutMillis, securityLevel);
                fingerprintDevice.open();

                device.set(fingerprintDevice);
                return true;
            }
        };
    }

    public static boolean isDeviceOpen(){
        try{
            return device().isOpen();
        }
        catch (Exception ex){
            return false;
        }
    }

    public static void closeDevice() throws IOException{
        device().close();
    }
}
