package ly.rqmana.huia.java.util.fingerprint;

public class Hand {

    private Finger thumbFinger;
    private Finger indexFinger;
    private Finger middleFinger;
    private Finger ringFinger;
    private Finger pinkyFinger;

    public Finger getThumbFinger() {
        return thumbFinger;
    }

    public void setThumbFinger(Finger thumbFinger) {
        this.thumbFinger = thumbFinger;
    }

    public void setThumbFinger(byte[] imageBuffer, byte[] template) {
        this.thumbFinger = new Finger(imageBuffer, template);
    }

    public void setThumbFinger(byte[] imageBuffer1, byte[] imageBuffer2, byte[] imageBuffer3, byte[] template) {
        this.thumbFinger = new Finger(imageBuffer1, imageBuffer2, imageBuffer3, template);
    }

    public Finger getIndexFinger() {
        return indexFinger;
    }

    public void setIndexFinger(Finger indexFinger) {
        this.indexFinger = indexFinger;
    }

    public void setIndexFinger(byte[] imageBuffer, byte[] template) {
        this.indexFinger = new Finger(imageBuffer, template);
    }

    public void setIndexFinger(byte[] imageBuffer1, byte[] imageBuffer2, byte[] imageBuffer3, byte[] template) {
        this.indexFinger = new Finger(imageBuffer1, imageBuffer2, imageBuffer3, template);
    }

    public Finger getMiddleFinger() {
        return middleFinger;
    }

    public void setMiddleFinger(Finger middleFinger) {
        this.middleFinger = middleFinger;
    }

    public void setMiddleFinger(byte[] imageBuffer, byte[] template) {
        this.middleFinger = new Finger(imageBuffer, template);
    }

    public void setMiddleFinger(byte[] imageBuffer1, byte[] imageBuffer2, byte[] imageBuffer3, byte[] template) {
        this.middleFinger = new Finger(imageBuffer1, imageBuffer2, imageBuffer3, template);
    }

    public Finger getRingFinger() {
        return ringFinger;
    }

    public void setRingFinger(Finger ringFinger) {
        this.ringFinger = ringFinger;
    }

    public void setRingFinger(byte[] imageBuffer, byte[] template) {
        this.ringFinger = new Finger(imageBuffer, template);
    }

    public void setRingFinger(byte[] imageBuffer1, byte[] imageBuffer2, byte[] imageBuffer3, byte[] template) {
        this.ringFinger = new Finger(imageBuffer1, imageBuffer2, imageBuffer3, template);
    }

    public Finger getPinkyFinger() {
        return pinkyFinger;
    }

    public void setPinkyFinger(Finger pinkyFinger) {
        this.pinkyFinger = pinkyFinger;
    }

    public void setPinkyFinger(byte[] imageBuffer, byte[] template) {
        this.pinkyFinger = new Finger(imageBuffer, template);
    }

    public void setPinkyFinger(byte[] imageBuffer1, byte[] imageBuffer2, byte[] imageBuffer3, byte[] template) {
        this.pinkyFinger = new Finger(imageBuffer1, imageBuffer2, imageBuffer3, template);
    }
}
