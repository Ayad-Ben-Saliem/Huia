package ly.rqmana.huia.java.fingerprints.hand;

import com.sun.istack.internal.Nullable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.awt.image.BufferedImage;
import java.util.List;

public class Finger {

    private final FingerID id;
    private String fingerprintTemplate = "";
    private final ObservableList<BufferedImage> fingerprintImages = FXCollections.observableArrayList();

    public Finger(FingerID id, @Nullable String fingerprintTemplate, @Nullable List<BufferedImage> fingerprintImages) {

        if (id == null)
            throw new NullPointerException("FingerID cannot be null.");

        this.id = id;

        if (fingerprintTemplate != null)
            this.fingerprintTemplate = fingerprintTemplate;

        if (fingerprintImages != null)
            setFingerprintImages(fingerprintImages);
    }

    public FingerID getId(){
        return this.id;
    }

    public String getFingerprintTemplate() {
        return fingerprintTemplate;
    }

    public void setFingerprintTemplate(String fingerprintTemplate) {
        this.fingerprintTemplate = fingerprintTemplate;
    }

    public ObservableList<BufferedImage> getFingerprintImages() {
        return fingerprintImages;
    }

    public void setFingerprintImages(List<BufferedImage> fingerprintImages) {
        this.fingerprintImages.setAll(fingerprintImages);
    }
}
