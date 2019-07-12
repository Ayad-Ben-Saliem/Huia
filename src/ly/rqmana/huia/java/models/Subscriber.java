package ly.rqmana.huia.java.models;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.event.MouseStationaryEvent;
import ly.rqmana.huia.java.fingerprints.hand.Finger;
import ly.rqmana.huia.java.fingerprints.hand.Hand;
import ly.rqmana.huia.java.fingerprints.hand.HandType;
import ly.rqmana.huia.java.util.Utils;
import org.controlsfx.control.PopOver;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

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
        fontAwesomeIconView.setMouseTransparent(true);
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
        result.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        result.setAlignment(Pos.CENTER);

        if (hasFingerprint() && ! isFingerprintsDetailed()) {
            result.getChildren().add(getFingerprintNode(getAllFingerprintsTemplate(), "FINGERPRINT"));
        } else {
            result.getChildren().add(getFingerprintNode(getRightLittleFingerprint(), "RIGHT_LITTLE"));
            result.getChildren().add(getFingerprintNode(getRightRingFingerprint(), "RIGHT_RING"));
            result.getChildren().add(getFingerprintNode(getRightMiddleFingerprint(), "RIGHT_MIDDLE"));
            result.getChildren().add(getFingerprintNode(getRightIndexFingerprint(), "RIGHT_INDEX"));
            result.getChildren().add(getFingerprintNode(getRightThumbFingerprint(), "RIGHT_THUMB"));

            result.getChildren().add(getFingerprintNode(getLeftThumbFingerprint(), "LEFT_THUMB"));
            result.getChildren().add(getFingerprintNode(getLeftIndexFingerprint(), "LEFT_INDEX"));
            result.getChildren().add(getFingerprintNode(getLeftMiddleFingerprint(), "LEFT_MIDDLE"));
            result.getChildren().add(getFingerprintNode(getLeftRingFingerprint(), "LEFT_RING"));
            result.getChildren().add(getFingerprintNode(getLeftLittleFingerprint(), "LEFT_LITTLE"));
        }
        return result;
    }

    public boolean isFingerprintsDetailed() {
        boolean isDetailed = false;
        for (Finger finger : getRightHand().getFingersUnmodifiable()) {
            isDetailed |= ! finger.isEmpty();
        }

        if (isDetailed) return true;

        for (Finger finger : getLeftHand().getFingersUnmodifiable()) {
            isDetailed |= !finger.isEmpty();
        }
        return isDetailed;
    }

    private Node getFingerprintNode(String fingerprint, String stringKey) {
        FontAwesomeIconView fontAwesomeIconView;
        if (fingerprint != null && !fingerprint.isEmpty())
            fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.CHECK_CIRCLE);
        else
            fontAwesomeIconView = new FontAwesomeIconView(FontAwesomeIcon.CIRCLE_ALT);
        fontAwesomeIconView.setSize("25");
        fontAwesomeIconView.setMouseTransparent(true);
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
