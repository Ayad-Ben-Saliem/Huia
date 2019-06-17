package ly.rqmana.huia.java;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.controls.alerts.Alerts;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    private void establishFingerprintDevice(){

        Task<Boolean> openDeviceTask = FingerprintManager.openDeviceIfNotOpen(FingerprintDeviceType.HAMSTER_DX);

        openDeviceTask.addOnFailed(event -> {

            Optional<AlertAction> result = Windows.showFingerprintDeviceError(event.getSource().getException());

            if (result.isPresent() && result.get() == AlertAction.TRY_AGAIN){
                this.establishFingerprintDevice();
            }
        });

        Threading.MAIN_EXECUTOR_SERVICE.submit(openDeviceTask);
    }

    @Override
    public void start(Stage primaryStage) {
        Logger.getLogger("org.apache.catalina.core").setLevel(Level.OFF);

        System.setProperty("prism.lcdtext", "false");

        Font.loadFont(getClass().getResource(Res.Font.CAIRO_SEMI_BOLD.getUrl()).toExternalForm(), 14);

        Windows.MAIN_WINDOW.open();
        establishFingerprintDevice();
    }

    @Override
    public void stop() throws Exception {

        if (FingerprintManager.isDeviceOpen())
            FingerprintManager.closeDevice();

        Threading.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
