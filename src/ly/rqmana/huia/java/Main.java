package ly.rqmana.huia.java;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Windows;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    private final static String CHANGE_DATE_EXE = "cmd /c \"C:\\Program Files\\Huia Healthcare\\ChangeDate.exe\" ";
    private final static LocalDate TODAY = LocalDate.now();

    private static Process fingerprintBackgroundServer;

    static {
//        try {
//            Runtime.getRuntime().exec(CHANGE_DATE_EXE + "1/1/2019");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

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

        try {
            System.out.println(CHANGE_DATE_EXE + TODAY.format(DateTimeFormatter.ofPattern("MM/dd/YYYY")));
//            Runtime.getRuntime().exec(CHANGE_DATE_EXE + TODAY.format(DateTimeFormatter.ofPattern("MM/dd/YYYY")));

            fingerprintBackgroundServer = Runtime.getRuntime().exec("java -jar \"C:\\Program Files\\Huia Healthcare\\FingerprintBackgroundServer.jar\"");

            Logger.getLogger("org.apache.catalina.core").setLevel(Level.OFF);

            System.setProperty("prism.lcdtext", "false");

            Font.loadFont(getClass().getResource(Res.Font.CAIRO_SEMI_BOLD.getUrl()).toExternalForm(), 14);

            Windows.ROOT_WINDOW.open();
            establishFingerprintDevice();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {

        if (FingerprintManager.isDeviceOpen())
            FingerprintManager.closeDevice();

        Threading.shutdown();

        fingerprintBackgroundServer.destroy();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
