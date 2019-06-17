package ly.rqmana.huia.java.util;

import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.function.Consumer;

public class Window extends Stage {
    private Res.Fxml fxml;
    private FXMLLoader loader;

    private final ListProperty<EventHandler<WindowEvent>> onShowingEventHandlers = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<EventHandler<WindowEvent>> onShownEventHandlers = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<EventHandler<WindowEvent>> onHiddenEventHandlers = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<EventHandler<WindowEvent>> onHidingEventHandlers = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<EventHandler<WindowEvent>> onCloseRequestEventHandlers = new SimpleListProperty<>(FXCollections.observableArrayList());

    Window(Res.Fxml fxml) {
        if (fxml != null) {
            this.fxml = fxml;
            loader = new FXMLLoader(fxml.getUrl(), Utils.getBundle());
        }

        final EventHandler<WindowEvent> onShownEventHandler = event -> onShownEventHandlers.forEach(_onShownEventHandler -> _onShownEventHandler.handle(event));
        final EventHandler<WindowEvent> onShowingEventHandler = event -> onShowingEventHandlers.forEach(_onShowingEventHandler -> _onShowingEventHandler.handle(event));
        final EventHandler<WindowEvent> onHiddenEventHandler = event -> onHiddenEventHandlers.forEach(_onHiddenEventHandler -> _onHiddenEventHandler.handle(event));
        final EventHandler<WindowEvent> onHidingEventHandler = event -> onHidingEventHandlers.forEach(_onHidingEventHandler -> _onHidingEventHandler.handle(event));
        final EventHandler<WindowEvent> onCloseRequestEventHandler = event -> onCloseRequestEventHandlers.forEach(_onCloseRequestEventHandler -> _onCloseRequestEventHandler.handle(event));

        setOnShown(onShownEventHandler);
        setOnShowing(onShowingEventHandler);
        setOnHidden(onHiddenEventHandler);
        setOnHiding(onHidingEventHandler);
        setOnCloseRequest(onCloseRequestEventHandler);

        onShownProperty().addListener((observable, oldValue, newValue) -> {
            if (onShownEventHandler.equals(newValue)) return;
            onShownEventHandlers.add(newValue);
            setOnShown(onShownEventHandler);
        });
        onShowingProperty().addListener((observable, oldValue, newValue) -> {
            if (onShowingEventHandler.equals(newValue)) return;
            onShowingEventHandlers.add(newValue);
            setOnShowing(onShowingEventHandler);
        });
        onHiddenProperty().addListener((observable, oldValue, newValue) -> {
            if (onHiddenEventHandler.equals(newValue)) return;
            onHiddenEventHandlers.add(newValue);
            setOnHidden(onHiddenEventHandler);
        });
        onHidingProperty().addListener((observable, oldValue, newValue) -> {
            if (onHidingEventHandler.equals(newValue)) return;
            onHidingEventHandlers.add(newValue);
            setOnHiding(onHidingEventHandler);
        });
        onCloseRequestProperty().addListener((observable, oldValue, newValue) -> {
            if (onCloseRequestEventHandler.equals(newValue)) return;
            onCloseRequestEventHandlers.add(newValue);
            setOnCloseRequest(onCloseRequestEventHandler);
        });
    }

    public <T> T getController() {
        return loader.getController();
    }

    protected void initialize() {
        try {
            loader = new FXMLLoader(fxml.getUrl(), Utils.getBundle());
            Pane root = loader.load();

            StackPane layout = new StackPane(root);
            layout.setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
            layout.setMaxSize(root.getMaxWidth(), root.getMaxHeight());

            layout.getStyleClass().add("window-layout");

            NodeOrientation orientation = Utils.getNodeOrientation();
            this.setScene(new Scene(layout));
            this.getScene().setNodeOrientation(orientation);

            getScene().getStylesheets().add(Res.Stylesheet.THEME.getUrl());
            getScene().getStylesheets().add(Res.Stylesheet.TEMPLATES.getUrl());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void open() {
        initialize();
        if (!isShowing()) {
            show();
        }
    }

    public void openAndWait() {
        initialize();
        if (!isShowing()) {
            showAndWait();
        }
    }

    @Override
    public void hide() {
        setScene(null);
        super.hide();
    }

    public void addOnCloseRequest(EventHandler<WindowEvent> onCloseRequestEventHandler) {
        onCloseRequestEventHandlers.add(onCloseRequestEventHandler);
    }

    public void addOnShowing(EventHandler<WindowEvent> onShowingEventHandler) {
        onShowingEventHandlers.add(onShowingEventHandler);
    }

    public void addOnShown(EventHandler<WindowEvent> onShownEventHandler) {
        onShownEventHandlers.add(onShownEventHandler);
    }

    public void addOnHidden(EventHandler<WindowEvent> onHiddenEventHandler) {
        onHiddenEventHandlers.add(onHiddenEventHandler);
    }

    public void addOnHiding(EventHandler<WindowEvent> onHidingEventHandler) {
        onHidingEventHandlers.add(onHidingEventHandler);
    }
}
