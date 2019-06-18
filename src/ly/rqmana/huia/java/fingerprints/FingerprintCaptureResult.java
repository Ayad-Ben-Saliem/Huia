package ly.rqmana.huia.java.fingerprints;

import ly.rqmana.huia.java.fingerprints.hand.Hand;
import ly.rqmana.huia.java.fingerprints.hand.HandType;

public class FingerprintCaptureResult {

    private final Hand rightHand = new Hand(HandType.RIHGT);
    private final Hand leftHand = new Hand(HandType.LEFT);
    private final String fingerprintsTemplate;

    public FingerprintCaptureResult(Hand rightHand, Hand leftHand, String fingerprintsTemplate){

        if (rightHand != null)
            this.rightHand.updateFingers(rightHand.getFingersUnmodifiable());
        if (leftHand != null)
            this.leftHand.updateFingers(leftHand.getFingersUnmodifiable());
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

    public boolean isEmpty(){
        return rightHand.isEmpty() && leftHand.isEmpty() && (fingerprintsTemplate == null || fingerprintsTemplate.isEmpty());
    }
}
