package ly.rqmana.huia.java.models;

import java.time.LocalDate;

public class Subscriber extends Person {

    private String fingerprint;

    private String workId;
    private Relationship relationship;
    private String beneficiaryName;
    private String companyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getWorkId() {
        return workId;
    }

    public void setWorkId(String workId) {
        this.workId = workId;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public void setRelationship(Relationship relationship) {
        this.relationship = relationship;
    }

    public void setRelationship(String relationship) {
        relationship = relationship.toUpperCase();
        switch (relationship) {
            case "المشترك":
                this.relationship = Relationship.EMPLOYEE;
                break;
            case "والد الموظف":
                this.relationship = Relationship.FATHER;
                break;
            case "والدة الموظف":
                this.relationship = Relationship.MOTHER;
                break;
            case "الإبن":
                this.relationship = Relationship.SUN;
                break;
            case "الإبنة":
                this.relationship = Relationship.DAUGHTER;
                break;
            case "الزوجة":
                this.relationship = Relationship.WIFE;
                break;
        }
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
