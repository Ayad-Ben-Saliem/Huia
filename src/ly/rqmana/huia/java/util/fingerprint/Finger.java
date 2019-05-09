package ly.rqmana.huia.java.util.fingerprint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Finger {

//    private byte[] fingerprintImage;
    private final List<byte[]> fingerprintImage = new ArrayList<>();
    private byte[] fingerprintTemplate;

    public Finger() {

    }

    public Finger(byte[] image, byte[] template) {
        fingerprintImage.add(image);
        fingerprintTemplate = template;
    }

    public Finger(byte[] image1, byte[] image2, byte[] image3, byte[] template) {
        fingerprintImage.add(image1);
        fingerprintImage.add(image2);
        fingerprintImage.add(image3);
        fingerprintTemplate = template;
    }

    public byte[] getFingerprintImage() {
        return fingerprintImage.get(0);
    }

    public List<byte[]> getFingerprintImages() {
        return fingerprintImage;
    }

    public void setFingerprintImage(byte[] fingerprintImage) {
        this.fingerprintImage.clear();
        this.fingerprintImage.add(fingerprintImage);
    }

    public void setFingerprintImages(byte[] ... fingerprintImage) {
        this.fingerprintImage.clear();
        this.fingerprintImage.addAll(Arrays.asList(fingerprintImage));
    }

    public void addFingerprintImage(byte[] fingerprintImage) {
        this.fingerprintImage.add(fingerprintImage);
    }

    public void addFingerprintImages(byte[] ... fingerprintImage) {
        this.fingerprintImage.addAll(Arrays.asList(fingerprintImage));
    }

    public byte[] getFingerprintTemplate() {
        return fingerprintTemplate;
    }

    public void setFingerprintTemplate(byte[] fingerprintTemplate) {
        this.fingerprintTemplate = fingerprintTemplate;
    }
}
