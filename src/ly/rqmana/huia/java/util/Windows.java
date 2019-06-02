package ly.rqmana.huia.java.util;

import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.controls.alerts.Alerts;
import ly.rqmana.huia.java.controls.alerts.InfoAlert;

public class Windows {

    public static final Window MAIN_WINDOW = new Window(Res.Fxml.MAIN_WINDOW);

    public static InfoAlert NETWORK_ERROR_ALERT;

    static {
        MAIN_WINDOW.setTitle(Utils.getI18nString("APP_NAME"));
        MAIN_WINDOW.setMinWidth(1000);
        MAIN_WINDOW.setMinHeight(700);

        MAIN_WINDOW.addOnShown(event -> {

            NETWORK_ERROR_ALERT = Alerts.createErrorAlert(Windows.MAIN_WINDOW,
                                                        Utils.getI18nString("NETWORK_ERROR"),
                                                        Utils.getI18nString("NO_INTERNET"),
                                                        null,
                                                        AlertAction.OK);
        });
    }
}
