package ly.rqmana.huia.java.fingerprints.hand;

public enum FingerID {

    UNKNOWN     (0),

    R_THUMB     (1),
    R_INDEX     (2),
    R_MIDDLE    (3),
    R_RING      (4),
    R_LITTLE    (5),


    L_THUMB     (6),
    L_INDEX     (7),
    L_MIDDLE    (8),
    L_RING      (9),
    L_LITTLE    (10),
    ;

    private int index;

    FingerID(int index) {
        this.index = index;
    }

    public int index(){
        return this.index;
    }

    public boolean isRightFinger(){
        return index >= 1 && index <= 5 ;
    }

    public static FingerID valueOf(int index){
        for (FingerID fingerID : FingerID.values()) {
            if (fingerID.index == index)
                return fingerID;
        }

        return null;
    }
}
