package ly.rqmana.huia.java.controllers.settings;

import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {

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

}
