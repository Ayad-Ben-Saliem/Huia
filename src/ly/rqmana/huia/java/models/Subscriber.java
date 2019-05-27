package ly.rqmana.huia.java.models;

import java.time.LocalDate;

public class Subscriber extends Person {

    private String fingerprint;

    private byte[] rightThumbFingerprint;
    private byte[] rightIndexFingerprint;
    private byte[] rightMiddleFingerprint;
    private byte[] rightRingFingerprint;
    private byte[] rightLittleFingerprint;

    private byte[] leftThumbFingerprint;
    private byte[] leftIndexFingerprint;
    private byte[] leftMiddleFingerprint;
    private byte[] leftRingFingerprint;
    private byte[] leftLittleFingerprint;

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

    public byte[] getRightThumbFingerprint() {
        return rightThumbFingerprint;
    }

    public void setRightThumbFingerprint(byte[] rightThumbFingerprint) {
        this.rightThumbFingerprint = rightThumbFingerprint;
    }

    public byte[] getRightIndexFingerprint() {
        return rightIndexFingerprint;
    }

    public void setRightIndexFingerprint(byte[] rightIndexFingerprint) {
        this.rightIndexFingerprint = rightIndexFingerprint;
    }

    public byte[] getRightMiddleFingerprint() {
        return rightMiddleFingerprint;
    }

    public void setRightMiddleFingerprint(byte[] rightMiddleFingerprint) {
        this.rightMiddleFingerprint = rightMiddleFingerprint;
    }

    public byte[] getRightRingFingerprint() {
        return rightRingFingerprint;
    }

    public void setRightRingFingerprint(byte[] rightRingFingerprint) {
        this.rightRingFingerprint = rightRingFingerprint;
    }

    public byte[] getRightLittleFingerprint() {
        return rightLittleFingerprint;
    }

    public void setRightLittleFingerprint(byte[] rightLittleFingerprint) {
        this.rightLittleFingerprint = rightLittleFingerprint;
    }

    public byte[] getLeftThumbFingerprint() {
        return leftThumbFingerprint;
    }

    public void setLeftThumbFingerprint(byte[] leftThumbFingerprint) {
        this.leftThumbFingerprint = leftThumbFingerprint;
    }

    public byte[] getLeftIndexFingerprint() {
        return leftIndexFingerprint;
    }

    public void setLeftIndexFingerprint(byte[] leftIndexFingerprint) {
        this.leftIndexFingerprint = leftIndexFingerprint;
    }

    public byte[] getLeftMiddleFingerprint() {
        return leftMiddleFingerprint;
    }

    public void setLeftMiddleFingerprint(byte[] leftMiddleFingerprint) {
        this.leftMiddleFingerprint = leftMiddleFingerprint;
    }

    public byte[] getLeftRingFingerprint() {
        return leftRingFingerprint;
    }

    public void setLeftRingFingerprint(byte[] leftRingFingerprint) {
        this.leftRingFingerprint = leftRingFingerprint;
    }

    public byte[] getLeftLittleFingerprint() {
        return leftLittleFingerprint;
    }

    public void setLeftLittleFingerprint(byte[] leftLittleFingerprint) {
        this.leftLittleFingerprint = leftLittleFingerprint;
    }
}
