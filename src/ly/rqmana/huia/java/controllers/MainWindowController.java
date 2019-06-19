package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.WindowEvent;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controllers.settings.SettingsWindowController;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;
import ly.rqmana.huia.java.models.User;
import ly.rqmana.huia.java.security.Auth;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainWindowController implements Controllable {

    @FXML public StackPane centralStack;
    @FXML public JFXButton registrationBtn;
    @FXML public JFXButton identificationBtn;
    @FXML public JFXButton settingsBtn;

    private Node registrationWindow;
    private Node identificationWindow;
    private Node settingsWindow;

    private RegistrationWindowController registrationWindowController;
    private IdentificationWindowController identificationWindowController;
    private SettingsWindowController settingsWindowController;

    private EventHandler<WindowEvent> windowEventHandler;

    enum SelectedPage {
        REGISTRATION,
        IDENTIFICATION,
        SETTINGS
    }

    final ObjectProperty<SelectedPage> selectedPageProperty = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        centralStack.getChildren().addAll(getRegistrationWindow(), getIdentificationWindow(), getSettingsWindow());

        selectedPageProperty.addListener((observable, oldValue, newValue) -> activeTab(newValue));

        registrationBtn.setOnAction(event -> selectedPageProperty.set(SelectedPage.REGISTRATION));
        identificationBtn.setOnAction(event -> selectedPageProperty.set(SelectedPage.IDENTIFICATION));
        settingsBtn.setOnAction(event -> selectedPageProperty.set(SelectedPage.SETTINGS));

        User currentUser = Auth.getCurrentUser();
        if (currentUser.isSuperuser())
            registrationBtn.fire();
        else {
            settingsBtn.setDisable(true);
            settingsBtn.setManaged(false);
            if (!currentUser.isStaff()) {
                registrationBtn.setDisable(true);
                registrationBtn.setManaged(false);
            }
            identificationBtn.fire();
        }
    }

    @FXML
    private void onDeviceRefreshButtonClicked(ActionEvent actionEvent){

        Task<Boolean> openDeviceTask = FingerprintManager.openDevice(FingerprintDeviceType.HAMSTER_DX);

        openDeviceTask.runningProperty().addListener((observable, oldValue, newValue) -> updateLoadingView(newValue));
        openDeviceTask.addOnSucceeded(event -> {
            Windows.infoAlert(
                    Utils.getI18nString("FINGERPRINT_DEVICE_OPENED_HEADING"),
                    Utils.getI18nString("FINGERPRINT_DEVICE_OPENED_BODY"),
                    AlertAction.OK);
        });

        openDeviceTask.addOnFailed(event -> {
            fingerprintDeviceError(event.getSource().getException(), () -> onDeviceRefreshButtonClicked(actionEvent));
        });

        Threading.MAIN_EXECUTOR_SERVICE.submit(openDeviceTask);
    }

    @FXML
    public void onLogoutBtnClicked(ActionEvent actionEvent) {
        lock(true);
    }

    private void activeTab(SelectedPage selectedPage) {
        activeBtn(registrationBtn, selectedPage.equals(SelectedPage.REGISTRATION));
        activeBtn(identificationBtn, selectedPage.equals(SelectedPage.IDENTIFICATION));
        activeBtn(settingsBtn, selectedPage.equals(SelectedPage.SETTINGS));

        switch (selectedPage) {
            case REGISTRATION:
                getRegistrationWindow().toFront();
                break;
            case IDENTIFICATION:
                getIdentificationWindow().toFront();
                break;
            case SETTINGS:
                getSettingsWindow().toFront();
                break;
        }
    }

    private void activeBtn(JFXButton button, boolean active) {
        if (active)
            button.setStyle("-fx-background-color: -rq-background-alt;-fx-text-fill: -rq-text-background-alt;");
        else
            button.setStyle("-fx-background-color: -rq-background;-fx-text-fill: -rq-text-background;");
    }




    private Node getRegistrationWindow() {
        if (registrationWindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(Res.Fxml.REGISTRATION_WINDOW.getUrl(), Utils.getBundle());
                registrationWindow = loader.load();
                registrationWindowController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return registrationWindow;
    }

    private Node getIdentificationWindow() {
        if (identificationWindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(Res.Fxml.IDENTIFICATION_WINDOW.getUrl(), Utils.getBundle());
                identificationWindow = loader.load();
                identificationWindowController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return identificationWindow;
    }

    private Node getSettingsWindow() {
        if (settingsWindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(Res.Fxml.SETTINGS_WINDOW.getUrl(), Utils.getBundle());
                settingsWindow = loader.load();
                settingsWindowController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return settingsWindow;
    }

    @Override
    public RegistrationWindowController getRegistrationWindowController() {
        return registrationWindowController;
    }

    @Override
    public IdentificationWindowController getIdentificationWindowController() {
        return identificationWindowController;
    }

    @Override
    public SettingsWindowController getSettingsWindowController() {
        return settingsWindowController;
    }

    public StackPane getCentralStack() {
        return centralStack;
    }

    public EventHandler<WindowEvent> getWindowEventHandler() {
        return windowEventHandler;
    }

    public void setOnCloseRequest(EventHandler<WindowEvent> eventHandler) {
        windowEventHandler = eventHandler;
    }

}
