package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.WindowEvent;
import ly.rqmana.huia.java.controllers.installation.InstallationWindowController;
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
    public JFXButton registrationBan;
    public JFXButton authenticationBtn;
    private BooleanProperty locked = new SimpleBooleanProperty(true);


    private Node homeWindow;
    private Node registrationWindow;
    private Node authenticationWindow;
    private Node installationWindow;

    private HomeWindowController homeWindowController;
    private RegistrationWindowController registrationWindowController;
    private AuthenticationWindowController authWindowController;
    private InstallationWindowController installationWindowController;

    private EventHandler<WindowEvent> windowEventHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        rootStack.getChildren().addAll(getHomeWindow());
        centralStack.getChildren().addAll(getRegistrationWindow(), getAuthenticationWindow());

        registrationBan.setOnAction(event -> {
            registrationBan.setStyle("-fx-background-color: -rq-background-alt;");
            authenticationBtn.setStyle("-fx-background-color: -rq-background;");
            getRegistrationWindow().toFront();
        });

        authenticationBtn.setOnAction(event -> {
            registrationBan.setStyle("-fx-background-color: -rq-background;");
            authenticationBtn.setStyle("-fx-background-color: -rq-background-alt;");
            getAuthenticationWindow().toFront();
        });

        registrationBan.fire();

        lock(true);
    }

    void lock(Boolean isLocked) {
        if (isLocked) {
            getHomeWindow().toFront();
        } else {
            mainContainer.toFront();
        }
    }

    private void lock() {
        lock(isLocked());
    }

    public boolean isLocked() {
        return locked.get();
    }

    public Node getHomeWindow() {
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

    public Node getInstallationWindow() {
        if (installationWindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(Res.Fxml.INSTALLATION_WINDOW.getUrl(), Utils.getBundle());
                installationWindow = loader.load();
                installationWindowController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return installationWindow;
    }

    public Node getRegistrationWindow() {
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

    public Node getAuthenticationWindow() {
        if (authenticationWindow == null) {
            try {
                FXMLLoader loader = new FXMLLoader(Res.Fxml.AUTHENTICATION.getUrl(), Utils.getBundle());
                authenticationWindow = loader.load();
                authWindowController = loader.getController();
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

    public AuthenticationWindowController getAuthWindowController() {
        return authWindowController;
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
