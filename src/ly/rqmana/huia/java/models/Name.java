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
                if (firstName == null || firstName.isEmpty()) {
//                    throw new RuntimeException("Set firstName first");
                }
                if (familyName == null || familyName.isEmpty()) {
//                    throw new RuntimeException("Set familyName first");
                }
                return getFamilyName() + " " + getFirstName();
            case FIRST_FATHER:
                if (firstName == null || firstName.isEmpty()) {
//                    throw new RuntimeException("Set firstName first");
                }
                if (fatherName == null || fatherName.isEmpty()) {
//                    throw new RuntimeException("Set fatherName first");
                }
                return getFirstName() + " " + getFatherName();
            case FIRST_FAMILY:
                if (firstName == null || firstName.isEmpty()) {
//                    throw new RuntimeException("Set firstName first");
                }
                if (familyName == null || familyName.isEmpty()) {
//                    throw new RuntimeException("Set familyName first");
                }
                return getFirstName() + " " + getFamilyName();
            case FULL_NAME:
                if (firstName == null || firstName.isEmpty()) {
//                    throw new RuntimeException("Set firstName first");
                }
                if (fatherName == null || fatherName.isEmpty()) {
                    //throw new RuntimeException("Set fatherName first");
                }
                if (familyName == null || familyName.isEmpty()) {
//                    throw new RuntimeException("Set familyName first");
                }
                String result = firstName + " " + fatherName;
                if (grandfatherName != null && !grandfatherName.isEmpty()) {
                    result += " " + grandfatherName;
                }
                result += " " + familyName;
                return result;
            default:
                return "error";
        }
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
