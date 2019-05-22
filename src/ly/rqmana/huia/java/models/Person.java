package ly.rqmana.huia.java.models;

import java.time.LocalDate;

public class Person {

    private String firstName;
    private String fatherName;
    private String grandfatherName;
    private String familyName;

    private Gender gender;

    private LocalDate birthday;

    private String nationality;
    private String nationalId;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getGrandfatherName() {
        return grandfatherName;
    }

    public void setGrandfatherName(String grandfatherName) {
        this.grandfatherName = grandfatherName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getFullName() {
        StringBuilder result = new StringBuilder(firstName);
        if (fatherName != null) {
            result.append(" ");
            result.append(fatherName);
            if (grandfatherName != null) {
                result.append(" ");
                result.append(grandfatherName);
            }
        }
        if (familyName != null) {
            result.append(" ");
            result.append(familyName);
        }
        return result.toString();
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }
}
