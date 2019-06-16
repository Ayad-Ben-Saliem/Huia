package ly.rqmana.huia.java.util;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.stage.Screen;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.controls.alerts.Alerts;
import ly.rqmana.huia.java.controls.alerts.InfoAlert;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintDeviceNotOpenedException;

import java.io.IOException;
import java.util.Optional;

import static ly.rqmana.huia.java.util.Utils.getI18nString;

public class Windows {

    public static final Window MAIN_WINDOW = new Window(Res.Fxml.MAIN_WINDOW);
    public static final JFXDialog LOAD_PERSONAL_PICTURE_DIALOG = new JFXDialog();

    public static final InfoAlert NETWORK_ERROR_ALERT;

    static {

        MAIN_WINDOW.getIcons().addAll(Res.Image.PERSON.getImage());

        MAIN_WINDOW.setTitle(getI18nString("APP_NAME"));
        MAIN_WINDOW.setMinWidth(1000);
        MAIN_WINDOW.setMinHeight(700);

        NETWORK_ERROR_ALERT = null;
//        NETWORK_ERROR_ALERT = Alerts.createErrorAlert(Windows.MAIN_WINDOW,
//                            getI18nString("NETWORK_ERROR"),
//                            getI18nString("NO_INTERNET"),
//                            null,
//                            AlertAction.OK);
    }

    public static Optional<AlertAction> showFingerprintDeviceError(Throwable throwable) {

        String heading = getI18nString("ERROR");
        String body = getI18nString("LIBRARY_NOT_EXIST");

        if (throwable instanceof FingerprintDeviceNotOpenedException ||
                throwable instanceof IOException) {

            heading = getI18nString("FINGERPRINT_DEVICE_ERROR_HEADING");
            body = getI18nString("FINGERPRINT_DEVICE_ERROR_BODY");
        }

        return Alerts.errorAlert(MAIN_WINDOW,
                heading,
                body,
                throwable,
                AlertAction.TRY_AGAIN,
                AlertAction.CANCEL);
    }

    public static void centerWindow(Window stage, double width, double height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - width) / 2);
        stage.setY((screenBounds.getHeight() - height) / 2);
    }

    public static void centerDialog(Dialog<?> dialog, double width, double height) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        dialog.setX((screenBounds.getWidth() - width) / 2);
        dialog.setY((screenBounds.getHeight() - height) / 2);
    }
}
