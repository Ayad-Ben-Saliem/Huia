package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.css.PseudoClass;
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
    public JFXButton identificationHistoryBtn;

    private BooleanProperty locked = new SimpleBooleanProperty(true);

    private Node homeWindow;

    private final ObservableMap<MainPage, Node> pageNodeMap = FXCollections.observableHashMap();

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


    enum MainPage {

        REGISTRATION(Res.Fxml.REGISTRATION_WINDOW.getUrl()),
        IDENTIFICATION(Res.Fxml.IDENTIFICATION.getUrl()),
        IDENTIFICATION_HISTORY(Res.Fxml.IDENTIFICATION_HISTORY.getUrl()),
        ;

        private final URL fxmlUrl;
        MainPage(URL fxmlUrl){
            this.fxmlUrl = fxmlUrl;
        }

        public URL fxmlUrl(){
            return fxmlUrl;
        }
    }

    final ObjectProperty<MainPage> selectedPageProperty = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootStack.getChildren().addAll(getHomeWindow());

        selectedPageProperty.addListener((observable, oldValue, newValue) -> {

            boolean isRegister = newValue == MainPage.REGISTRATION;
            boolean isIdentification = newValue == MainPage.IDENTIFICATION;
            boolean isHistory = newValue == MainPage.IDENTIFICATION_HISTORY;

            registrationBtn.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), isRegister);
            identificationBtn.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), isIdentification);
            identificationHistoryBtn.pseudoClassStateChanged(PseudoClass.getPseudoClass("selected"), isHistory);

            Node pageNode = pageNodeMap.get(newValue);
            if (pageNode == null){
                FXMLLoader loader = new FXMLLoader(newValue.fxmlUrl(), Utils.getBundle());
                try {
                    pageNode = loader.load();
                    // a loaded node means that it's not previously added.
                    centralStack.getChildren().addAll(pageNode);

                    pageNodeMap.put(newValue, pageNode);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (pageNode == null)
                throw new IllegalStateException("An error occurred while loading MainPage:" + newValue.name());

            pageNode.toFront();
        });

        registrationBtn.setOnAction(event -> selectedPageProperty.set(MainPage.REGISTRATION));
        identificationBtn.setOnAction(event -> selectedPageProperty.set(MainPage.IDENTIFICATION));
        identificationHistoryBtn.setOnAction(event -> selectedPageProperty.set(MainPage.IDENTIFICATION_HISTORY));

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
