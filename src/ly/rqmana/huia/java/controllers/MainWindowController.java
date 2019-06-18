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
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.controls.alerts.Alerts;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainWindowController implements Controllable {

    public StackPane rootStack;
    public StackPane centralStack;
    public BorderPane mainContainer;
    public JFXButton registrationBtn;
    public JFXButton identificationBtn;

    private BooleanProperty locked = new SimpleBooleanProperty(true);

    private Node homeWindow;
    private Node registrationWindow;
    private Node authenticationWindow;

    private HomeWindowController homeWindowController;
    private RegistrationWindowController registrationWindowController;
    private IdentificationWindowController identificationWindowController;

    private EventHandler<WindowEvent> windowEventHandler;

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

    public void onLogoutBtnClicked() {
        lock(true);
    }


    void updateLoadingView(boolean loading){
        if (loading)
            Windows.showLoadingAlert();
        else
            Windows.closeLoadingAlert();
    }

    void fingerprintDeviceError(Throwable throwable, Runnable tryAgainBlock) {

        Optional<AlertAction> result = Windows.showFingerprintDeviceError(throwable);

        if (result.isPresent() && result.get() == AlertAction.TRY_AGAIN) {
            tryAgainBlock.run();
        }
    }


    public void onLogoutBtnClicked(ActionEvent actionEvent) {

    }

    enum SelectedPage {
        REGISTRATION,
        AUTHENTICATION
    }

    final ObjectProperty<SelectedPage> selectedPageProperty = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        rootStack.getChildren().addAll(getHomeWindow());
        centralStack.getChildren().addAll(getRegistrationWindow(), getAuthenticationWindow());

        selectedPageProperty.addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case REGISTRATION:
                    registrationBtn.setStyle("-fx-background-color: -rq-background-alt;" +
                                             "-fx-text-fill: -rq-text-background-alt;");
                    identificationBtn.setStyle("-fx-background-color: -rq-background;" +
                                               "-fx-text-fill: -rq-text-background;");
                    getRegistrationWindow().toFront();
                    break;
                case AUTHENTICATION:
                    registrationBtn.setStyle("-fx-background-color: -rq-background;" +
                                             "-fx-text-fill: -rq-text-background;");
                    identificationBtn.setStyle("-fx-background-color: -rq-background-alt;" +
                                               "-fx-text-fill: -rq-text-background-alt;");
                    getAuthenticationWindow().toFront();
                    break;
            }
        });

        registrationBtn.setOnAction(event -> selectedPageProperty.set(SelectedPage.REGISTRATION));
        identificationBtn.setOnAction(event -> selectedPageProperty.set(SelectedPage.AUTHENTICATION));

        registrationBtn.fire();

        lock(true);
    }

    public void lock(Boolean isLocked) {
        if (isLocked) {
            getHomeWindow().toFront();
        } else {
            mainContainer.toFront();
        }
    }

    private void lock() {
        lock(isLocked());
    }

    private boolean isLocked() {
        return locked.get();
    }

    private Node getHomeWindow() {
        if (homeWindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(Res.Fxml.HOME_WINDOW.getUrl(), Utils.getBundle());
                homeWindow = loader.load();
                homeWindowController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return homeWindow;
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

    private Node getAuthenticationWindow() {
        if (authenticationWindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(Res.Fxml.AUTHENTICATION.getUrl(), Utils.getBundle());
                authenticationWindow = loader.load();
                identificationWindowController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return authenticationWindow;
    }

    public HomeWindowController getHomeWindowController() {
        return homeWindowController;
    }

    public RegistrationWindowController getRegistrationWindowController() {
        return registrationWindowController;
    }

    public IdentificationWindowController getIdentificationWindowController() {
        return identificationWindowController;
    }

    public StackPane getRootStack() {
        return rootStack;
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
