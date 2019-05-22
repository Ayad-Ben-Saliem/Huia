package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.WindowEvent;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController implements Controllable {

    public StackPane rootStack;
    public StackPane centralStack;
    public BorderPane mainContainer;
    public JFXButton registrationBtn;
    public JFXButton authenticationBtn;

    private BooleanProperty locked = new SimpleBooleanProperty(true);

    private Node homeWindow;
    private Node registrationWindow;
    private Node authenticationWindow;

    private HomeWindowController homeWindowController;
    private RegistrationWindowController registrationWindowController;
    private AuthenticationWindowController authenticationWindowController;

    private EventHandler<WindowEvent> windowEventHandler;

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
                    authenticationBtn.setStyle("-fx-background-color: -rq-background;" +
                                               "-fx-text-fill: -rq-text-background;");
                    getRegistrationWindow().toFront();
                    break;
                case AUTHENTICATION:
                    registrationBtn.setStyle("-fx-background-color: -rq-background;" +
                                             "-fx-text-fill: -rq-text-background;");
                    authenticationBtn.setStyle("-fx-background-color: -rq-background-alt;" +
                                               "-fx-text-fill: -rq-text-background-alt;");
                    getAuthenticationWindow().toFront();
                    break;
            }
        });

        registrationBtn.setOnAction(event -> selectedPageProperty.set(SelectedPage.REGISTRATION));
        authenticationBtn.setOnAction(event -> selectedPageProperty.set(SelectedPage.AUTHENTICATION));

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
                authenticationWindowController = loader.getController();
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

    public AuthenticationWindowController getAuthenticationWindowController() {
        return authenticationWindowController;
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
