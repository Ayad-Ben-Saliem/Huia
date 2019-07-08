package ly.rqmana.huia.java.util;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXDialog;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ly.rqmana.huia.java.controllers.RootWindowController;
import ly.rqmana.huia.java.controls.alerts.*;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintDeviceNotOpenedException;

import java.io.IOException;
import java.util.Optional;

import static ly.rqmana.huia.java.util.Utils.getI18nString;

public class Windows {

    public static final Window ROOT_WINDOW = new Window(Res.Fxml.ROOT_WINDOW);
    public static final JFXDialog LOAD_PERSONAL_PICTURE_DIALOG = new JFXDialog();

    public static final InfoAlert NETWORK_ERROR_ALERT;

    private static InfoAlert ALERT;
    private static InfoAlert INFO_ALERT;
    private static InfoAlert CONFIRM_ALERT;
    private static InfoAlert WARNING_ALERT;
    private static InfoAlert ERROR_ALERT;

    //fixme: fix weird dialog behavior
    private static final LoadingAlert LOADING_ALERT = new LoadingAlert(null, LoadingAlert.LoadingStyle.SPINNER);

    static {

//        System.out.println("static :: Windows");
        ROOT_WINDOW.getIcons().addAll(Res.Image.PERSON.getImage());

        ROOT_WINDOW.setTitle(Utils.APP_NAME);
        ROOT_WINDOW.setMinWidth(1000);
        ROOT_WINDOW.setMinHeight(700);
        ROOT_WINDOW.setMaximized(true);

        NETWORK_ERROR_ALERT = null;
//        NETWORK_ERROR_ALERT = Alerts.createErrorAlert(Windows.MAIN_WINDOW,
//                getI18nString("NETWORK_ERROR"),
//                getI18nString("NO_INTERNET"),
//                null,
//                AlertAction.OK);
        ROOT_WINDOW.addOnShown(event -> {
            ALERT = new InfoAlert(ROOT_WINDOW, InfoAlertType.NONE);
            INFO_ALERT = new InfoAlert(ROOT_WINDOW, InfoAlertType.INFO);
            CONFIRM_ALERT = new InfoAlert(ROOT_WINDOW, InfoAlertType.CONFIRM);
            WARNING_ALERT = new InfoAlert(ROOT_WINDOW, InfoAlertType.WARNING);
            ERROR_ALERT = new InfoAlert(ROOT_WINDOW, InfoAlertType.ERROR);
        });

        LOADING_ALERT.setHeadingText(Utils.getI18nString("LOADING_CONNECTING"));
        LOADING_ALERT.setBodyText(Utils.getI18nString("LOADING_WAIT_TEXT"));
        LOADING_ALERT.initStyle(StageStyle.TRANSPARENT);
        LOADING_ALERT.initModality(Modality.APPLICATION_MODAL);
    }

    public static Optional<AlertAction> showFingerprintDeviceError(Throwable throwable) {

        String heading = getI18nString("ERROR");
        String body = getI18nString("LIBRARY_NOT_EXIST");

        if (throwable instanceof FingerprintDeviceNotOpenedException ||
                throwable instanceof IOException) {

            heading = getI18nString("FINGERPRINT_DEVICE_ERROR_HEADING");
            body = getI18nString("FINGERPRINT_DEVICE_ERROR_BODY");
        }

        return Windows.errorAlert(
                heading,
                body,
                throwable,
                AlertAction.TRY_AGAIN,
                AlertAction.CANCEL);
    }

    public static void showLoadingAlert(){
        LOADING_ALERT.show();
        centerDialog(LOADING_ALERT.getNativeAlert());
    }

    public static void closeLoadingAlert(){
        LOADING_ALERT.close();
    }

    public static void centerDialog(Dialog<?> dialog) {
        double width = dialog.getWidth();
        double height = dialog.getHeight();
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        dialog.setX((screenBounds.getWidth() - width) / 2);
        dialog.setY((screenBounds.getHeight() - height) / 2);
    }

    public static Optional<AlertAction> infoAlert(String heading, String body, AlertAction ... alertActions) {
//        prepAlert(InfoAlertType.INFO, heading, body, alertActions);
        prepAlert(INFO_ALERT, heading, body, alertActions);
        return INFO_ALERT.showAndWait();
    }

    public static Optional<AlertAction> confirmAlert(String heading, String body, AlertAction ... alertActions) {
//        prepAlert(InfoAlertType.CONFIRM, heading, body, alertActions);
        prepAlert(CONFIRM_ALERT, heading, body, alertActions);
        return CONFIRM_ALERT.showAndWait();
    }

    public static Optional<AlertAction> warningAlert(String heading, String body, AlertAction ... alertActions) {
//        prepAlert(InfoAlertType.WARNING, heading, body, alertActions);
        prepAlert(WARNING_ALERT, heading, body, alertActions);
        return WARNING_ALERT.showAndWait();
    }

    public static Optional<AlertAction> errorAlert(String heading, String body, Throwable throwable, AlertAction ... alertActions) {
//        prepAlert(InfoAlertType.ERROR, heading, body, alertActions);
        prepAlert(ERROR_ALERT, heading, body, alertActions);
        ERROR_ALERT.setUseDetails(throwable != null);
        ERROR_ALERT.visualizeStackTrace(throwable);
        throwable.printStackTrace();
        return ERROR_ALERT.showAndWait();
    }

    private static void prepAlert(InfoAlertType alertType, String heading, String body, AlertAction ... alertActions) {
        ALERT.setAlertType(alertType);
        ALERT.setUseDetails(alertType.equals(InfoAlertType.ERROR));
        ALERT.setHeadingText(heading);
        ALERT.setBodyText(body);

        // fixme: alertActions just works for once at first, then doesn't close the alert.
        ALERT.getAlertActions().clear();
        ALERT.getAlertActions().addAll(alertActions);
    }

    private static void prepAlert(InfoAlert alert, String heading, String body, AlertAction ... alertActions) {
        alert.setHeadingText(heading);
        alert.setBodyText(body);
        // fixme: alertActions just works for once at first, then doesn't close the alert.
        alert.getAlertActions().clear();
        alert.getAlertActions().addAll(alertActions);
    }

    public static StackPane getRootStack() {
        RootWindowController controller = ROOT_WINDOW.getController();
        return controller.getRootStack();
    }
}
