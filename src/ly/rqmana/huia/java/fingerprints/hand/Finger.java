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

    public void fill(Finger other){
        if (other.getId() != id) {
            throw new IllegalStateException("Cannot fill finger from id:" + other.getId() + " required id: " + id);
        }
        this.fingerprintTemplate = other.fingerprintTemplate;
        this.fingerprintImages.setAll(other.fingerprintImages);
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

    public boolean isEmpty(){

        if (fingerprintTemplate != null && ! fingerprintTemplate.isEmpty())
            return false;

        return fingerprintImages.isEmpty();
    }

    public boolean isThumb(){
        return id == FingerID.L_THUMB || id == FingerID.R_THUMB;
    }

    public boolean isIndex(){
        return id == FingerID.L_INDEX || id == FingerID.R_INDEX;
    }

    public boolean isMiddle(){
        return id == FingerID.L_MIDDLE || id == FingerID.R_MIDDLE;
    }

    public boolean isRing(){
        return id == FingerID.L_RING || id == FingerID.R_RING;
    }

    public boolean isLittle(){
        return id == FingerID.L_LITTLE || id == FingerID.R_LITTLE;
    }
}
