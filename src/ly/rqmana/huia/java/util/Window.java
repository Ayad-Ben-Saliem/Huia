package ly.rqmana.huia.java.util;

import com.jfoenix.controls.JFXProgressBar;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Window extends Stage {
    private Res.Fxml fxml;
    private FXMLLoader loader;

    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final DoubleProperty loadingProgress = new SimpleDoubleProperty(0);
    private final StringProperty loadingText = new SimpleStringProperty("Loading...");

    public Window(Res.Fxml fxml) {
        if (fxml != null) {
            this.fxml = fxml;
            loader = new FXMLLoader(fxml.getUrl(), Utils.getBundle());
        }
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
}
