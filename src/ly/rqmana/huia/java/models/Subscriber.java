package ly.rqmana.huia.java.models;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import ly.rqmana.huia.java.fingerprints.hand.Hand;
import ly.rqmana.huia.java.fingerprints.hand.HandType;

import java.time.LocalDate;

public class Subscriber extends Person {

    private final Hand rightHand = new Hand(HandType.RIHGT);
    private final Hand leftHand = new Hand(HandType.LEFT);
    private String allFingerprintsTemplate;

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
                this.relationship = Relationship.SUBSCRIBER;
                break;
            case "والد الموظف":
                this.relationship = Relationship.FATHER;
                break;
            case "والدة الموظف":
                this.relationship = Relationship.MOTHER;
                break;
            case "الإبن":
                this.relationship = Relationship.SON;
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

    public void fillRightHand(Hand hand) {
        if (hand.getType() != HandType.RIHGT)
            throw new IllegalStateException("Hand type must be right, got HandType: " + hand.getType());

        this.rightHand.updateFingers(hand.getFingersUnmodifiable());
    }

    public Hand getLeftHand() {
        return leftHand;
    }

    public void fillLeftHand(Hand hand) {
        if (hand.getType() != HandType.LEFT)
            throw new IllegalStateException("Hand type must be left got HandType: " + hand.getType());

        this.leftHand.updateFingers(hand.getFingersUnmodifiable());
    }

    public String getRightThumbFingerprint() {
        return rightHand.getThumb().getFingerprintTemplate();
    }

    public void setRightThumbFingerprint(String template) {
        rightHand.getThumb().setFingerprintTemplate(template);
    }

    public String getRightIndexFingerprint() {
        return rightHand.getIndex().getFingerprintTemplate();
    }

    public void setRightIndexFingerprint(String template) {
        rightHand.getIndex().setFingerprintTemplate(template);
    }

    public String getRightMiddleFingerprint() {
        return rightHand.getMiddle().getFingerprintTemplate();
    }

    public void setRightMiddleFingerprint(String template) {
        rightHand.getMiddle().setFingerprintTemplate(template);
    }

    public String getRightRingFingerprint() {
        return rightHand.getRing().getFingerprintTemplate();
    }

    public void setRightRingFingerprint(String template) {
        rightHand.getRing().setFingerprintTemplate(template);
    }

    public String getRightLittleFingerprint() {
        return rightHand.getLittle().getFingerprintTemplate();
    }

    public void setRightLittleFingerprint(String template) {
        rightHand.getLittle().setFingerprintTemplate(template);
    }

    public String getLeftThumbFingerprint() {
        return leftHand.getThumb().getFingerprintTemplate();
    }

    public void setLeftThumbFingerprint(String template) {
        leftHand.getThumb().setFingerprintTemplate(template);
    }

    public String getLeftIndexFingerprint() {
        return leftHand.getIndex().getFingerprintTemplate();
    }

    public void setLeftIndexFingerprint(String template) {
        leftHand.getIndex().setFingerprintTemplate(template);
    }

    public String getLeftMiddleFingerprint() {
        return leftHand.getMiddle().getFingerprintTemplate();
    }

    public void setLeftMiddleFingerprint(String template) {
        leftHand.getMiddle().setFingerprintTemplate(template);
    }

    public String getLeftRingFingerprint() { return leftHand.getRing().getFingerprintTemplate();}

    public void setLeftRingFingerprint(String template) {
        leftHand.getRing().setFingerprintTemplate(template);
    }

    public String getLeftLittleFingerprint() {
        return leftHand.getLittle().getFingerprintTemplate();
    }

    public void setLeftLittleFingerprint(String template) {
        leftHand.getLittle().setFingerprintTemplate(template);
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
