package ly.rqmana.huia.java.models;

public class Name {

    public enum NameFormat {
        FIRST_FAMILY,
        FIRST_FATHER,
        FAMILY_FIRST,
        FULL_NAME,
    }


    public final static String DB_FIELD_FIRST_NAME = "firstName";
    public final static String DB_FIELD_FATHER_NAME = "fatherName";
    public final static String DB_FIELD_GRANDFATHER_NAME = "grandfatherName";
    public final static String DB_FIELD_FAMILY_NAME = "familyName";

    private String firstName;
    private String fatherName;
    private String grandfatherName;
    private String familyName;

    public Name() { }

    public Name(String firstName, String fatherName, String grandfatherName, String familyName) {
        setFirstName(firstName);
        setFatherName(fatherName);
        setGrandfatherName(grandfatherName);
        setFamilyName(familyName);
    }

    public String getFormattedName(NameFormat nameFormat){
        switch (nameFormat) {
            case FAMILY_FIRST:
                return getTwoSegmentName(familyName, firstName);
            case FIRST_FATHER:
                return getTwoSegmentName(firstName, fatherName);
            case FIRST_FAMILY:
                return getTwoSegmentName(firstName, familyName);
            case FULL_NAME:
                String fullName = getTwoSegmentName(firstName, familyName);
//                if (fullName==null) return null;
                fullName = getTwoSegmentName(fullName, grandfatherName);
//                if (fullName==null) return null;
                return getTwoSegmentName(fullName, familyName);
            default:
                return null;
        }
    }

    private String getTwoSegmentName(String first, String second) {
        String result = "";
        if (first != null && !first.isEmpty()) {
            result = first;
        }
        if (second != null && !second.isEmpty()) {
            if (!result.isEmpty())
                result += " ";
            result += second;
        }
        return result;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getFatherName() {
        return fatherName;
    }

    public String getGrandfatherName() {
        return grandfatherName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public void setGrandfatherName(String grandfatherName) {
        this.grandfatherName = grandfatherName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    @Override
    public String toString() {
        return "firstName: " + firstName + '\n' +
                "fatherName: " + fatherName + '\n' +
                "grandfatherName: " + grandfatherName + '\n' +
                "familyName: " + familyName + '\n';
    }

    public String getSimpleName() {
        return getFormattedName(NameFormat.FIRST_FAMILY);
    }
}
