package ly.rqmana.huia.java.controllers;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import ly.rqmana.huia.java.util.Controllable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PersonalImageLoaderController implements Controllable {
    public ImageView centralIV;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

//        Alerts.infoAlert(Windows.MAIN_WINDOW, Utils.getI18nString("CHOSE_IMAGE_SOURCE"), Utils.getI18nString("CHOSE"))


        new Thread(() -> {
            Webcam webcam = Webcam.getDefault();
            webcam.setViewSize(new Dimension(640, 480));
            webcam.open();

            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                WritableImage image = SwingFXUtils.toFXImage(webcam.getImage(), null);
                Platform.runLater(() -> centralIV.setImage(image));
            }, 0, 100, TimeUnit.MILLISECONDS);
        }).start();

    }
}
