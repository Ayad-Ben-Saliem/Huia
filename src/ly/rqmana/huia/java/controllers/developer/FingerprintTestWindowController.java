package ly.rqmana.huia.java.controllers.developer;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;
import ly.rqmana.huia.java.fingerprints.hand.Finger;
import ly.rqmana.huia.java.fingerprints.hand.Hand;

public class FingerprintTestWindowController {
    
    @FXML private JFXComboBox deviceCombo;
    @FXML private HBox rightHandStage;
    @FXML private HBox leftHandStage;

    @FXML private JFXButton captureFingerButton;
    @FXML private JFXButton captureHandsButton;

    @FXML private JFXButton closeButton;
    
    @FXML
    private void initialize(){

        Task<Boolean> openDeviceTask = FingerprintManager.openDevice(FingerprintDeviceType.HAMSTER_DX);
        Threading.MAIN_EXECUTOR_SERVICE.submit(openDeviceTask);

        initComponents();
        initListeners();
    }
    
    private void initComponents(){

    }
    
    private void initListeners(){

        captureHandsButton.addEventFilter(ActionEvent.ACTION, event -> {

            clearFingerprintsStages();

            Pair<Hand, Hand> hands = FingerprintManager.device().captureHands();
            Hand rightHand = hands.getKey();
            Hand leftHand = hands.getValue();

            if (rightHand == null || leftHand == null){
                // null hands usually means the use canceled
                // the capture process
                System.err.println("Null hands nothing to do");
                return;
            }

            for (Finger finger : rightHand.getFingersUnmodifiable()) {
                rightHandStage.getChildren().add(createFingerView(finger));
            }

            for (Finger finger : leftHand.getFingersUnmodifiable()) {
                leftHandStage.getChildren().add(createFingerView(finger));
            }

        });
    }

    
    private VBox createFingerView(Finger finger){
        Label fingerLabel = new Label(String.format("%s (%d)", finger.getId().name(), finger.getId().index()));

        WritableImage image1 = SwingFXUtils.toFXImage(finger.getFingerprintImages().get(0), null);
        WritableImage image2 = SwingFXUtils.toFXImage(finger.getFingerprintImages().get(1), null);

        ImageView fingerprintView1 = new ImageView(image1);
        ImageView fingerprintView2 = new ImageView(image2);

        VBox fingerView = new VBox(fingerLabel, fingerprintView1, fingerprintView2);
        fingerView.setSpacing(5);
        fingerLabel.setMaxWidth(Double.MAX_VALUE);
        fingerLabel.setAlignment(Pos.CENTER);

        return fingerView;
    }

    private void clearFingerprintsStages(){
        rightHandStage.getChildren().clear();
        leftHandStage.getChildren().clear();
        System.gc();
    }
}
