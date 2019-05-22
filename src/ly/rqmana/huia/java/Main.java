package ly.rqmana.huia.java;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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

        Windows.MAIN_WINDOW.open();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
