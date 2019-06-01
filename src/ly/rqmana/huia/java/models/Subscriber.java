package ly.rqmana.huia.java.models;

import java.time.LocalDate;

public class Subscriber extends Person {

    private String fingerprint;

    private String rightThumbFingerprint;
    private String rightIndexFingerprint;
    private String rightMiddleFingerprint;
    private String rightRingFingerprint;
    private String rightLittleFingerprint;

    private String leftThumbFingerprint;
    private String leftIndexFingerprint;
    private String leftMiddleFingerprint;
    private String leftRingFingerprint;
    private String leftLittleFingerprint;

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

    public String getRightThumbFingerprint() {
        return rightThumbFingerprint;
    }

    public void setRightThumbFingerprint(String rightThumbFingerprint) {
        this.rightThumbFingerprint = rightThumbFingerprint;
    }

    public String getRightIndexFingerprint() {
        return rightIndexFingerprint;
    }

    public void setRightIndexFingerprint(String rightIndexFingerprint) {
        this.rightIndexFingerprint = rightIndexFingerprint;
    }

    public String getRightMiddleFingerprint() {
        return rightMiddleFingerprint;
    }

    public void setRightMiddleFingerprint(String rightMiddleFingerprint) {
        this.rightMiddleFingerprint = rightMiddleFingerprint;
    }

    public String getRightRingFingerprint() {
        return rightRingFingerprint;
    }

    public void setRightRingFingerprint(String rightRingFingerprint) {
        this.rightRingFingerprint = rightRingFingerprint;
    }

    public String getRightLittleFingerprint() {
        return rightLittleFingerprint;
    }

    public void setRightLittleFingerprint(String rightLittleFingerprint) {
        this.rightLittleFingerprint = rightLittleFingerprint;
    }

    public String getLeftThumbFingerprint() {
        return leftThumbFingerprint;
    }

    public void setLeftThumbFingerprint(String leftThumbFingerprint) {
        this.leftThumbFingerprint = leftThumbFingerprint;
    }

    public String getLeftIndexFingerprint() {
        return leftIndexFingerprint;
    }

    public void setLeftIndexFingerprint(String leftIndexFingerprint) {
        this.leftIndexFingerprint = leftIndexFingerprint;
    }

    public String getLeftMiddleFingerprint() {
        return leftMiddleFingerprint;
    }

    public void setLeftMiddleFingerprint(String leftMiddleFingerprint) {
        this.leftMiddleFingerprint = leftMiddleFingerprint;
    }

    public String getLeftRingFingerprint() {
        return leftRingFingerprint;
    }

    public void setLeftRingFingerprint(String leftRingFingerprint) {
        this.leftRingFingerprint = leftRingFingerprint;
    }

    public String getLeftLittleFingerprint() {
        return leftLittleFingerprint;
    }

    public void setLeftLittleFingerprint(String leftLittleFingerprint) {
        this.leftLittleFingerprint = leftLittleFingerprint;
    }
}
