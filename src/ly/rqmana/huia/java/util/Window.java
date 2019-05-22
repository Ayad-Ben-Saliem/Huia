package ly.rqmana.huia.java.util;

import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
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

    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final DoubleProperty loadingProgress = new SimpleDoubleProperty(0);
    private final StringProperty loadingText = new SimpleStringProperty("Loading...");

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
            // newly added

            StackPane layout = new StackPane(root);
            layout.setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
            layout.setMaxSize(root.getMaxWidth(), root.getMaxHeight());

            layout.getStyleClass().add("window-layout");

            setScene(new Scene(layout));
            initLoadingBehavior();

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

    /* ***************************************** *
     *                                           *
     * loading stack related stuff               *
     * ***************************************** */

    private static class LoadingPane extends StackPane{
        private final JFXProgressBar progressBar;

        private final Label textLabel;
//        private final JFXSpinner progressSpinner;

        private final DoubleProperty progress = new SimpleDoubleProperty(0.0, "progress");
        private final StringProperty text = new SimpleStringProperty("");
//        private final BooleanProperty spinnerVisible = new SimpleBooleanProperty(true);

        private LoadingPane(
                final ReadOnlyDoubleProperty loadingProgressProperty,
                final ReadOnlyStringProperty loadingTextProperty){
            super();

            progress.bind(loadingProgressProperty);
            text.bind(loadingTextProperty);
//            spinnerVisible.bind(loadingSpinnerVisibleProperty);

            this.progressBar = new JFXProgressBar();

            this.textLabel = new Label();
//            this.progressSpinner = new JFXSpinner();

            // add spinner to pane if decided to use it
            this.getChildren().addAll(textLabel, progressBar);

            constructScene();
            this.getStyleClass().add("window-loading-pane");
        }

        private void constructScene(){
            textLabel.textProperty().bind(text);

            progressBar.setMaxWidth(Double.MAX_VALUE);
            progressBar.setProgress(JFXProgressBar.INDETERMINATE_PROGRESS);

//            progressSpinner.visibleProperty().bind(visibleProperty());
//            progressSpinner.progressProperty().bind(progress);

            StackPane.setAlignment(textLabel, Pos.CENTER);
            StackPane.setAlignment(progressBar, Pos.TOP_CENTER);
//            StackPane.setAlignment(progressSpinner, Pos.CENTER);
        }
    }

    private void initLoadingBehavior(){
        loadingProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue){
                Parent root = this.getScene().getRoot();
                if (root instanceof StackPane){
                    StackPane layoutPane = (StackPane) root;
                    ObservableList<Node> children = layoutPane.getChildren();
                    LoadingPane loadingPane =
                            new LoadingPane(loadingProgressProperty(), loadingTextProperty());

                    // add the loading pane at the top of the scene graph
                    children.add(children.size(), loadingPane);
                }
            }
            else{
                Parent root = this.getScene().getRoot();
                if (root instanceof StackPane){
                    StackPane layoutPane = (StackPane) root;
                    ObservableList<Node> children = layoutPane.getChildren();
                    for (Node child : children)
                        if (child instanceof LoadingPane) {
                            children.remove(child);
                            break;
                        }
                }
            }
        });
    }

    public boolean isLoading() {
        return loading.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading.set(loading);
    }

    public double getLoadingProgress() {
        return loadingProgress.get();
    }

    public DoubleProperty loadingProgressProperty() {
        return loadingProgress;
    }

    public void setLoadingProgress(double loadingProgress) {
        this.loadingProgress.set(loadingProgress);
    }

    public String getLoadingText() {
        return loadingText.get();
    }

    public StringProperty loadingTextProperty() {
        return loadingText;
    }

    public void setLoadingText(String loadingText) {
        this.loadingText.set(loadingText);
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
