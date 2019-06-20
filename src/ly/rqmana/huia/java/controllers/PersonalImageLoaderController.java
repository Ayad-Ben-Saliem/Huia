package ly.rqmana.huia.java.controllers;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.controls.alerts.Alerts;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PersonalImageLoaderController implements Controllable {

    private final Map<Integer, Image> result = new HashMap<>();

    @FXML private ImageView centralIV;
    @FXML private HBox webcamContainer;
    @FXML private JFXButton defaultWebcamBtn;
    @FXML private JFXButton tackPictureBtn;
    @FXML private ImageView leftIV;
    @FXML private ImageView frontIV;
    @FXML private ImageView rightIV;
    @FXML private JFXButton addPictureBtn;
    @FXML private HBox picturesContainer;

    private ObjectProperty<ImageView> currentIVProperty = new SimpleObjectProperty<>();
    private Image grayPersonImage = Res.Image.PERSON.getImage();
    private Image greenPersonImage = Res.Image.GREEN_PERSON.getImage();

    private Webcam webcam;
    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;

    private final EventHandler<ActionEvent> webcamBtnAction = (event) -> {
        JFXButton button = (JFXButton) event.getSource();
        webcam = Webcam.getWebcamByName(button.getText());
        webcam.setViewSize(WebcamResolution.VGA.getSize());

        FontAwesomeIconView graphic = (FontAwesomeIconView) tackPictureBtn.getGraphic();
        graphic.setIcon(FontAwesomeIcon.CAMERA);

        startCapturing();
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        leftIV.setImage(grayPersonImage);
        rightIV.setImage(grayPersonImage);
        centralIV.setImage(grayPersonImage);

        currentIVProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && oldValue.getImage().equals(greenPersonImage)) {
                oldValue.setImage(grayPersonImage);
            }
            newValue.setImage(greenPersonImage);

            centralIV.imageProperty().bind(newValue.imageProperty());
        });
        currentIVProperty.setValue(frontIV);
        new Thread(this::setWebcams).start();

    }

    private void setWebcams() {
        webcamContainer.setAlignment(Pos.CENTER_LEFT);

        Webcam defaultWebcam = Webcam.getDefault();
        if (defaultWebcam != null)
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

    public void tackPicture(ActionEvent action) {
        if (webcam.isOpen()) {
            WritableImage image = SwingFXUtils.toFXImage(webcam.getImage(), null);
            currentIVProperty.get().setImage(image);

            stopCapturing();
        }
    }

    public void onRefreshWebcamClicked(ActionEvent actionEvent) {
        webcamContainer.getChildren().clear();
        webcamContainer.getChildren().add(defaultWebcamBtn);
    }

    private void startCapturing() {
        new Thread(() -> {
            webcamContainer.setDisable(true);
            tackPictureBtn.setDisable(false);

            webcam.open();

            scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
                WritableImage image = SwingFXUtils.toFXImage(webcam.getImage(), null);
                Platform.runLater(() -> currentIVProperty.get().setImage(image));
            }, 0, 100, TimeUnit.MILLISECONDS);
        }).start();
    }

    private void stopCapturing() {
        new Thread(() -> {
            webcam.close();

            tackPictureBtn.setDisable(true);
            webcamContainer.setDisable(false);

            scheduledFuture.cancel(true);
        }).start();
    }

    public void loadPicture(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Utils.getI18nString("CHOOSE_PERSONAL_PICTURE"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(Utils.getI18nString("PERSONAL_PICTURE"), "*.jpg", "*.png", "*.bmp", "*.gif"));
        File file = fileChooser.showOpenDialog(Windows.ROOT_WINDOW);
        try {
            Image image = SwingFXUtils.toFXImage(ImageIO.read(file), null);
            currentIVProperty.get().setImage(image);
        } catch (IOException e) {
            Windows.errorAlert(
                    Utils.getI18nString("ERROR"),
                    e.getLocalizedMessage(),
                    e,
                    AlertAction.OK
            );
        }
    }

    public void selectIV(MouseEvent mouseEvent) {
        ImageView iv = (ImageView) mouseEvent.getSource();
        currentIVProperty.setValue(iv);
    }

    public void onAddPictureBtnClicked(ActionEvent event) {
        ImageView iv = new ImageView(grayPersonImage);
        iv.setFitHeight(48);
        iv.setFitWidth(64);
        iv.setPreserveRatio(true);
        iv.setOnMouseClicked(this::selectIV);
        picturesContainer.getChildren().add(iv);
    }

    public void onOKBtnClicked(ActionEvent event) {
        if (validateResult()) {
            setResult();
            Windows.LOAD_PERSONAL_PICTURE_DIALOG.close();
        }
        else {
            Windows.warningAlert(
                    Utils.getI18nString("ERROR"),
                    Utils.getI18nString("SELECT_PICTURES_FIRST_ERROR"),
                    AlertAction.OK);
        }
    }

    public void onCancelBtnClicked(ActionEvent event) {
        Windows.LOAD_PERSONAL_PICTURE_DIALOG.close();
    }

    private boolean validateResult() {

        return ! (rightIV.getImage().equals(grayPersonImage) || rightIV.getImage().equals(greenPersonImage)
                    || frontIV.getImage().equals(grayPersonImage) || frontIV.getImage().equals(greenPersonImage)
                    || leftIV .getImage().equals(grayPersonImage) || leftIV .getImage().equals(greenPersonImage) );
    }

    private void setResult() {

        // indexing is as follows:
        // 0: for front
        // 1: for right
        // 2: for left
        // 3: any other picture
        // n: ....

        result.put(0, frontIV.getImage());
        result.put(1, rightIV.getImage());
        result.put(2, leftIV.getImage());

        for (int i = 3; i < picturesContainer.getChildren().size(); i++) {
            ImageView iv = (ImageView) picturesContainer.getChildren().get(i);
            result.put(i, iv.getImage());
        }

        System.out.println(result);
    }

    public Map<Integer, Image> getResult(){
        return result;
    }
}
