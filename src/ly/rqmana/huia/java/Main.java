package ly.rqmana.huia.java;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    static {
        try {
            Utils.loadNBioBSPJNILib();
            Utils.establishFingerprintDevice();
            Utils.startFingerprintBackgroundService();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Logger.getLogger("org.apache.catalina.core").setLevel(Level.OFF);

        System.setProperty("prism.lcdtext", "false");

        Font.loadFont(getClass().getResource(Res.Font.CAIRO_SEMI_BOLD.getUrl()).toExternalForm(), 14);

        Windows.ROOT_WINDOW.open();

        DAO.initialize();
    }

    @Override
    public void stop() throws Exception {
        if (FingerprintManager.isDeviceOpen())
            FingerprintManager.closeDevice();

        Threading.shutdown();

        Utils.stopFingerprintBackgroundService();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
