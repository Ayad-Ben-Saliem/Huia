package ly.rqmana.huia.java.fingerprints;

import ly.rqmana.huia.java.fingerprints.hand.Hand;

public class FingerprintCaptureResult {

    private final Hand rightHand;
    private final Hand leftHand;
    private final String fingerprintsCode;

    public FingerprintCaptureResult(Hand rightHand, Hand leftHand, String fingerprintsCode){

        this.rightHand = rightHand;
        this.leftHand = leftHand;
        this.fingerprintsCode = fingerprintsCode;
    }

    public Hand getRightHand() {
        return rightHand;
    }

    public Hand getLeftHand() {
        return leftHand;
    }

    public String getFingerprintsCode() {
        return fingerprintsCode;
    }
}
