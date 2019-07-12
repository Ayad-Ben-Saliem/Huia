package ly.rqmana.huia.java.controllers.settings;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.storage.DataStorage;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Window;
import ly.rqmana.huia.java.util.Windows;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class DatabaseSettingsWindowController implements Controllable {
    public JFXTextField dbPathTF;
    public JFXButton loadDBBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        dbPathTF.textProperty().addListener((observable, oldValue, newValue) -> loadDBBtn.setDisable(newValue.isEmpty()));
    }

    public void onSelectDatabasePathClicked(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(Utils.getI18nString("CHOOSE_DATABASE_TITLE"));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Database file", "*.db", "*.sql", "*.sqlite", "*.sqlite3"));
        File sqlFile = fileChooser.showOpenDialog(Windows.ROOT_WINDOW);

        if (sqlFile != null) {
            if (sqlFile.exists()) {
                dbPathTF.setText(sqlFile.getAbsolutePath());
            }
        }
    }

    public String getDbPath() {
        return dbPathTF.getText();
    }

    public void onLoadDBBtnClicked(ActionEvent actionEvent) {
        if (getSettingsWindowController().askForSaveSetting()) {
            Task<Boolean> loadDBTask = DAO.migrateExternalData(dbPathTF.getText());

            loadDBTask.addOnSucceeded(event -> System.exit(0));

            loadDBTask.addOnFailed(event -> Windows.errorAlert(
                    Utils.getI18nString("ERROR"),
                    loadDBTask.getException().getMessage(),
                    loadDBTask.getException(),
                    AlertAction.OK
            ));

            Threading.MAIN_EXECUTOR_SERVICE.submit(loadDBTask);

            Windows.showLoadingAlert();
        }
    }
}
