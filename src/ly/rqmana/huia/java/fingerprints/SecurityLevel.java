package ly.rqmana.huia.java.fingerprints;

public enum SecurityLevel {

    LOWEST          (1),
    LOWER           (2),
    LOW             (3),
    BELOW_NORAML    (4),
    NORMAL          (5),
    ABOVE_NORMAL    (6),
    HIGH            (7),
    HIGHER          (8),
    HIGHEST         (9),
    ;

    public final int LEVEL;

    SecurityLevel(int level){
        this.LEVEL = level;
    }
}
