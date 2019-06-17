package ly.rqmana.huia.java.models;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import ly.rqmana.huia.java.fingerprints.hand.Hand;

import java.time.LocalDate;

public class Subscriber extends Person {

    private Hand rightHand;
    private Hand leftHand;
    private String allFingerprintsTemplate;

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
    private Institute institute;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;

    private String dataPath;

    public String getAllFingerprintsTemplate() {
        return allFingerprintsTemplate;
    }

    public void setAllFingerprintsTemplate(String allFingerprintsTemplate) {
        this.allFingerprintsTemplate = allFingerprintsTemplate;
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

    public Institute getInstitute() {
        return institute;
    }

    public void setInstitute(Institute institute) {
        this.institute = institute;
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
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    // This method used for TableView
    public Node getActiveNode() {
        FontAwesomeIconView fontAwesomeIconView;
        if (isActive()) {
            fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
        } else {
            fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.TIMES_CIRCLE);
        }
        fontAwesomeIconView.setSize("25");
        return fontAwesomeIconView;
    }

    public Hand getRightHand() {
        return rightHand;
    }

    public void setRightHand(Hand rightHand) {
        this.rightHand = rightHand;
    }

    public Hand getLeftHand() {
        return leftHand;
    }

    public void setLeftHand(Hand leftHand) {
        this.leftHand = leftHand;
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

    // This method is used for TableView
    public Node getFingerprintsNodes() {
        HBox result = new HBox();

        result.getChildren().add(getFingerprintNode(getRightThumbFingerprint()));
        result.getChildren().add(getFingerprintNode(getRightIndexFingerprint()));
        result.getChildren().add(getFingerprintNode(getRightMiddleFingerprint()));
        result.getChildren().add(getFingerprintNode(getRightRingFingerprint()));
        result.getChildren().add(getFingerprintNode(getRightLittleFingerprint()));

        result.getChildren().add(getFingerprintNode(getLeftThumbFingerprint()));
        result.getChildren().add(getFingerprintNode(getLeftIndexFingerprint()));
        result.getChildren().add(getFingerprintNode(getLeftMiddleFingerprint()));
        result.getChildren().add(getFingerprintNode(getLeftRingFingerprint()));
        result.getChildren().add(getFingerprintNode(getLeftLittleFingerprint()));

        boolean isThereFP = false;
        for (Node child : result.getChildren()) {
            FontAwesomeIconView fontAwesomeIconView = (FontAwesomeIconView) child;
            if (FontAwesomeIcon.CHECK_CIRCLE.name().equals(fontAwesomeIconView.getDefaultGlyph().name())) {
                isThereFP = true;
                break;
            }
        }
        if (!isThereFP) {
            if (getAllFingerprintsTemplate() != null && !getAllFingerprintsTemplate().isEmpty()) {
                FontAwesomeIconView fontAwesomeIconView = (FontAwesomeIconView) result.getChildren().get(0);
                fontAwesomeIconView.setIcon(FontAwesomeIcon.CHECK_CIRCLE);
            }
        }

        return result;
    }

    private Node getFingerprintNode(String fingerprint) {
        FontAwesomeIconView fontAwesomeIconView;
        if (fingerprint != null && !fingerprint.isEmpty())
            fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
        else
            fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.CIRCLE_ALT);
        fontAwesomeIconView.setSize("25");
        return fontAwesomeIconView;
    }

    public boolean hasFingerprint() {
        return getAllFingerprintsTemplate() != null && !getAllFingerprintsTemplate().isEmpty();
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
