package ly.rqmana.huia.java.models;

public class EmployeeRelated extends Person {

    private static final String TYPE = "EmployeeRelated";

    private long relatedTo;

    public static String getTYPE() {
        return TYPE;
    }

    public long getRelatedTo() {
        return relatedTo;
    }

    public void setRelatedTo(long relatedTo) {
        this.relatedTo = relatedTo;
    }
}
