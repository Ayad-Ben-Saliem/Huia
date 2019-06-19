package ly.rqmana.huia.java.controllers.settings;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import ly.rqmana.huia.java.controllers.MainWindowController;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.storage.DataStorage;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;

public class SettingsWindowController implements Controllable {
    public JFXButton dbSettingsBtn;
    public JFXButton usersSettingsBtn;
    public StackPane centralStack;

    private Node databaseSettingsWindow;
    private Node usersSettingsWindow;

    private DatabaseSettingsWindowController databaseSettingsWindowController;
    private UsersSettingsWindowController usersSettingsWindowController;

    enum SelectedPage {
        DATABASE,
        USERS,
    }

    private final ObjectProperty<SelectedPage> selectedPageProperty = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        centralStack.getChildren().addAll(getDatabaseSettingsWindow(), getUsersSettingsWindow());

        selectedPageProperty.addListener((observable, oldValue, newValue) -> activeTab(newValue));

        dbSettingsBtn.setOnAction(event -> selectedPageProperty.set(SelectedPage.DATABASE));
        usersSettingsBtn.setOnAction(event -> selectedPageProperty.set(SelectedPage.USERS));

        dbSettingsBtn.fire();
    }

    private void activeTab(SelectedPage selectedPage) {
        activeBtn(dbSettingsBtn, selectedPage.equals(SelectedPage.DATABASE));
        activeBtn(usersSettingsBtn, selectedPage.equals(SelectedPage.USERS));

        switch (selectedPage) {
            case DATABASE:
                getDatabaseSettingsWindow().toFront();
                break;
            case USERS:
                getUsersSettingsWindow().toFront();
                break;
        }
    }

    private void activeBtn(JFXButton button, boolean active) {
        if (active)
            button.setStyle("-fx-background-color: -rq-background-alt;-fx-text-fill: -rq-text-background-alt;");
        else
            button.setStyle("-fx-background-color: -rq-background;-fx-text-fill: -rq-text-background;");
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

        String dbPath = databaseSettingsWindowController.getDbPath();
        if (dbPath != null && !dbPath.isEmpty() && Files.exists(Paths.get(dbPath)))
            DataStorage.updateBaseInfo("dbPath", dbPath);

        System.exit(0);
    }

    public Node getDatabaseSettingsWindow() {
        if (databaseSettingsWindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(Res.Fxml.DATABASE_SITTINGS_WINDOW.getUrl(), Utils.getBundle());
                databaseSettingsWindow = loader.load();
                databaseSettingsWindowController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return databaseSettingsWindow;
    }

    public Node getUsersSettingsWindow() {
        if (usersSettingsWindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(Res.Fxml.USERS_SITTINGS_WINDOW.getUrl(), Utils.getBundle());
                usersSettingsWindow = loader.load();
                usersSettingsWindowController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return usersSettingsWindow;
    }

    @Override
    public DatabaseSettingsWindowController getDatabaseSettingsWindowController() {
        return databaseSettingsWindowController;
    }

    @Override
    public UsersSettingsWindowController getUsersSettingsWindowController() {
        return usersSettingsWindowController;
    }
}
