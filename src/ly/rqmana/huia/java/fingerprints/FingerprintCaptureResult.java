package ly.rqmana.huia.java.fingerprints;

import ly.rqmana.huia.java.fingerprints.hand.Hand;

public class FingerprintCaptureResult {

    private final Hand rightHand;
    private final Hand leftHand;
    private final String fingerprintsTemplate;

    public FingerprintCaptureResult(Hand rightHand, Hand leftHand, String fingerprintsTemplate){

        this.rightHand = rightHand;
        this.leftHand = leftHand;
        this.fingerprintsTemplate = fingerprintsTemplate;
    }

    public Hand getRightHand() {
        return rightHand;
    }

    public Hand getLeftHand() {
        return leftHand;
    }

    public String getFingerprintsTemplate() {
        return fingerprintsTemplate;
    }
}
