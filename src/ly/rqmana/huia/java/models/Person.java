package ly.rqmana.huia.java.models;

import javafx.scene.image.Image;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Person {

    private long id;

    private final Name name = new Name();
    private Gender gender;
    private LocalDate birthday;
    private String nationality;
    private String nationalId;
    private final Passport passport = new Passport();
    private String familyId;
    private String residence;

    private Map<Integer, Image> personalPictures = new HashMap<>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return name.getFirstName();
    }

    public void setFirstName(String firstName) {
        name.setFirstName(firstName);
    }

    public String getFatherName() {
        return name.getFatherName();
    }

    public void setFatherName(String fatherName) {
        name.setFatherName(fatherName);
    }

    public String getGrandfatherName() {
        return name.getGrandfatherName();
    }

    public void setGrandfatherName(String grandfatherName) {
        name.setGrandfatherName(grandfatherName);
    }

    public String getFamilyName() {
        return name.getFamilyName();
    }

    public void setFamilyName(String familyName) {
        name.setFamilyName(familyName);
    }

    public String getFullName() {
        return name.getFormattedName(Name.NameFormat.FULL_NAME);
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

    public Name getName() {
        return name;
    }

    public Passport getPassport() {
        return passport;
    }

    public String getPassportNumber() {
        return getPassport().getNumber();
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public String getResidence() {
        return residence;
    }

    public void setResidence(String residence) {
        this.residence = residence;
    }

    public Map<Integer, Image> getPersonalPictures() {
        return personalPictures;
    }
}
