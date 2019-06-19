package ly.rqmana.huia.java.controllers.settings;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.storage.DataStorage;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Window;
import ly.rqmana.huia.java.util.Windows;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

public class SettingsWindowController implements Controllable {
    public JFXButton dbSettingsBtn;
    public JFXButton usersSettingsBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void onApplyBtnClicked(ActionEvent actionEvent) {
        Optional<AlertAction> alertAction = Windows.infoAlert(
                Utils.getI18nString("SAVE_NEW_SITTING_HEADING"),
                Utils.getI18nString("SAVE_NEW_SITTING_BODY"),
                AlertAction.OK, AlertAction.CANCEL
        );
        if (alertAction.isPresent()) {
            if (alertAction.get().equals(AlertAction.CANCEL))
                return;
        } else {
            return;
        }


        DatabaseSettingsWindow databaseSettingsWindow = null;
        String dbPath = databaseSettingsWindow.getDbPath();
        if (dbPath != null && !dbPath.isEmpty() && Files.exists(Paths.get(dbPath)))
            DataStorage.updateBaseInfo("dbPath", dbPath);

        System.exit(0);
    }
}
