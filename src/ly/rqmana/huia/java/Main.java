package ly.rqmana.huia.java;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Logger.getLogger("org.apache.catalina.core").setLevel(Level.OFF);

        Font.loadFont(getClass().getResource(Res.Font.CAIRO_SEMI_BOLD.getUrl()).toExternalForm(), 14);

        Windows.MAIN_WINDOW.open();
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
