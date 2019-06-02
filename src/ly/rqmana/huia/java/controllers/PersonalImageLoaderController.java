package ly.rqmana.huia.java.controllers;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import ly.rqmana.huia.java.util.Controllable;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PersonalImageLoaderController implements Controllable {
    public ImageView centralIV;
    public HBox webcamContainer;
    public JFXButton defaultWebcamBtn;
    public JFXButton loadImagesBtn;

    private Webcam webcam;
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final EventHandler<ActionEvent> webcamBtnAction = (event) -> {
        webcamContainer.setDisable(true);
        JFXButton button = (JFXButton) event.getSource();
        webcam = Webcam.getWebcamByName(button.getText());
        webcam.setViewSize(WebcamResolution.VGA.getSize());

        startCapturing();
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        new Thread(this::setWebcams).start();

    }

    private void setWebcams() {
        webcamContainer.setAlignment(Pos.CENTER_LEFT);

        Webcam defaultWebcam = Webcam.getDefault();
        Platform.runLater(() -> defaultWebcamBtn.setText(defaultWebcam.getName()));
        defaultWebcamBtn.setOnAction(webcamBtnAction);
        for (Webcam webcam : Webcam.getWebcams()) {
            if (!webcam.equals(defaultWebcam)) {
                JFXButton button = new JFXButton(webcam.getName());
                button.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.CAMERA));
                button.setContentDisplay(ContentDisplay.LEFT);
                Platform.runLater(() -> webcamContainer.getChildren().add(button));

                button.setOnAction(webcamBtnAction);
            }
        }
    }

    public void tackAPicture(ActionEvent actionEvent) {
        scheduledExecutorService.shutdown();

        if (webcam.isOpen()) {
            WritableImage image = SwingFXUtils.toFXImage(webcam.getImage(), null);
            centralIV.setImage(image);
        }
    }

    public void onRefreshWebcamClicked(ActionEvent actionEvent) {
        webcamContainer.getChildren().clear();
        webcamContainer.getChildren().add(defaultWebcamBtn);
    }

    private void startCapturing() {
        new Thread(() -> {
            webcam.open();

            scheduledExecutorService.scheduleAtFixedRate(() -> {
                WritableImage image = SwingFXUtils.toFXImage(webcam.getImage(), null);
                Platform.runLater(() -> centralIV.setImage(image));
            }, 0, 100, TimeUnit.MILLISECONDS);
        }).start();
    }
}
