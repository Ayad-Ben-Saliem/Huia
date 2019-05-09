package ly.rqmana.huia.java.models;

public class Employee extends Person {

    private static final String TYPE = "Employee";

    private Institute institute;
    private String workId;

    public static String getTYPE() {
        return TYPE;
    }

    public Institute getInstitute() {
        return institute;
    }

    public void setInstitute(Institute institute) {
        this.institute = institute;
    }

    public String getWorkId() {
        return workId;
    }

    public void setWorkId(String workId) {
        this.workId = workId;
    }
}
