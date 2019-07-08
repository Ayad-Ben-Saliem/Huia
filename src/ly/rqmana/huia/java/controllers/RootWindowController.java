package ly.rqmana.huia.java.controllers;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.security.Auth;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RootWindowController implements Controllable {
    @FXML
    public StackPane rootStack;

    private BooleanProperty locked = new SimpleBooleanProperty(true);

    private Node homeWindow;
    private Node mainWindow;

    private HomeWindowController homeWindowController;
    private MainWindowController mainWindowController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        rootStack.getChildren().addAll(getHomeWindow());
        Auth.currentUserProperty().addListener(observable -> {
            Platform.runLater(() -> {
                if (!rootStack.getChildren().contains(getMainWindow()))
                    rootStack.getChildren().add(getMainWindow());
                getMainWindow().toFront();
            });
        });
    }

    public void lock(Boolean isLocked) {
        Task<Void> lockTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (isLocked) {
                    Platform.runLater(() -> getHomeWindow().toFront());
                } else {
                    Platform.runLater(() -> getMainWindow().toFront());
                }
                return null;
            }
        };
        Threading.MAIN_EXECUTOR_SERVICE.submit(lockTask);
    }

    private void lock() {
        lock(isLocked());
    }

    private boolean isLocked() {
        return locked.get();
    }

    private Node getMainWindow() {
//        if (mainWindow == null){
            try {
                FXMLLoader loader = new FXMLLoader(Res.Fxml.MAIN_WINDOW.getUrl(), Utils.getBundle());
                mainWindow = loader.load();
                mainWindowController = loader.getController();
            } catch (IOException e) {
                e.printStackTrace();
            }
//        }
        return mainWindow;
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

    @Override
    public HomeWindowController getHomeWindowController() {
        return homeWindowController;
    }

    @Override
    public MainWindowController getMainWindowController() {
        return mainWindowController;
    }

    public StackPane getRootStack() {
        return rootStack;
    }
}
