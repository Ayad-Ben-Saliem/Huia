package ly.rqmana.huia.java.controls.alerts;

import com.jfoenix.animation.alert.JFXAlertAnimation;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXSpinner;
import com.sun.istack.internal.Nullable;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Optional;

public class LoadingAlert{

    public enum LoadingStyle {
        SPINNER,
        BAR,
    }

    private final CustomAlert<AlertLayout> mainAlert;

    private ProgressIndicator progressIndicator;

    // in bar layout only
    private Label percentageLabel;

    private final ObjectProperty<LoadingStyle> loadingStyle = new SimpleObjectProperty<>();
    private final DoubleProperty progress = new SimpleDoubleProperty(-1);

    public LoadingAlert(@Nullable Stage stage){
        this(stage, LoadingStyle.SPINNER);
    }

    public LoadingAlert(@Nullable Stage stage, AlertAction... actions){
        this(stage, LoadingStyle.SPINNER, actions);
    }

    public LoadingAlert(@Nullable Stage stage, LoadingStyle loadingStyle, AlertAction... actions){
        this.mainAlert = new CustomAlert<>(stage);
        setLoadingStyle(loadingStyle);

        AlertLayout layout;
        if (loadingStyle == LoadingStyle.SPINNER)
            layout = prepareSpinnerLayout();
        else
            layout = prepareBarLayout();

        mainAlert.setLayout(layout);
        mainAlert.getAlertActions().addAll(actions);

        initialize();

        mainAlert.setGraphicFill(Color.TRANSPARENT);
    }

    private void initialize(){

        // invoked after the alert is instantiated
        initListeners();

        this.mainAlert.setAnimation(JFXAlertAnimation.CENTER_ANIMATION);
        this.mainAlert.getDialogPane().getStyleClass().add("loading-alert-pane");
        this.mainAlert.setOverlayClose(false);
        this.mainAlert.setHideOnEscape(false);

        boolean isSpinner = getLoadingStyle() == LoadingStyle.SPINNER;

        mainAlert.getLayout().getStyleClass().add("loading-alert-layout");
        mainAlert.getLayout().pseudoClassStateChanged(PseudoClass.getPseudoClass("spinner"), isSpinner);
        mainAlert.getLayout().pseudoClassStateChanged(PseudoClass.getPseudoClass("bar"), ! isSpinner);

        progressIndicator.progressProperty().bindBidirectional(this.progressProperty());

        if (! isSpinner){
            percentageLabel.getStyleClass().add("percentage");
        }
    }

    private void initListeners(){
        progressProperty().addListener((observable, oldValue, newValue) -> {
            updatePercentageText();
        });
    }

    private AlertLayout prepareSpinnerLayout(){

        this.progressIndicator = new JFXSpinner();

        return new AlertLayout(){

            @Override
            protected void initialize(){
                super.initialize();

                bodyPane.getChildren().remove(bodyLabel);
                HBox hbox = new HBox(progressIndicator, bodyLabel);

                hbox.setSpacing(30);
                hbox.setAlignment(Pos.CENTER_LEFT);

                bodyPane.getChildren().add(hbox);
            }
        };
    }

    private AlertLayout prepareBarLayout(){

        this.progressIndicator = new JFXProgressBar();
        this.progressIndicator.setMaxWidth(Double.MAX_VALUE);

        return new AlertLayout(){

            @Override
            protected void initialize(){
                super.initialize();

                bodyPane.getChildren().remove(bodyLabel);

                percentageLabel = new Label();
                HBox hbox = new HBox(percentageLabel);
                hbox.setAlignment(Pos.CENTER_RIGHT);

                VBox vbox = new VBox(bodyLabel, progressIndicator, hbox);
                vbox.setAlignment(Pos.CENTER_LEFT);
                vbox.setSpacing(10);

                bodyPane.getChildren().addAll(vbox);
            }
        };
    }

    private void updatePercentageText(){
        if (getLoadingStyle() == LoadingStyle.BAR && percentageLabel != null){
            double value = getProgress();

            if (value < 0)
                percentageLabel.setText(null);
            else {
                String percent = String.format("%02d%%", (int) (value * 100));
                percentageLabel.setText(percent);
            }
        }
    }

    private void setLoadingStyle(LoadingStyle loadingStyle){this.loadingStyle.set(loadingStyle);}
    public LoadingStyle getLoadingStyle(){return this.loadingStyle.get();}
    public ReadOnlyObjectProperty<LoadingStyle> loadingStyleProperty(){return this.loadingStyle;}

    public double getProgress() {  return progress.get();}
    public DoubleProperty progressProperty() {  return progress; }
    public void setProgress(double progress) {  this.progress.set(progress);}

    public void hide() {
        mainAlert.hideWithAnimation();
    }

    public void show() {
        mainAlert.show();
    }

    public Optional<AlertAction> showAndWait() {
        return mainAlert.showAndWait();
    }

    public void close() {
        mainAlert.close();
    }

    public void setHeadingText(String text) {
        mainAlert.setHeadingText(text);
    }

    public String getHeadingText() {
        return mainAlert.getHeadingText();
    }

    public StringProperty headingTextProperty() {
        return mainAlert.headingTextProperty();
    }

    public void setBodyText(String text) {
        mainAlert.setBodyText(text);
    }

    public String getBodyText() {
        return mainAlert.getBodyText();
    }

    public StringProperty bodyTextProperty() {
        return mainAlert.bodyTextProperty();
    }

    public ObservableList<AlertAction> getAlertActions() {
        return mainAlert.getAlertActions();
    }


    public void initModality(Modality modality) {
        mainAlert.initModality(modality);
    }

    public Modality getModality() {
        return mainAlert.getModality();
    }

    public void initStyle(StageStyle style) {
        mainAlert.initStyle(style);
    }

    public double getX() {
        return mainAlert.getX();
    }

    public void setX(double x) {
        mainAlert.setX(x);
    }

    public ReadOnlyDoubleProperty xProperty() {
        return mainAlert.xProperty();
    }

    public double getY() {
        return mainAlert.getY();
    }

    public void setY(double y) {
        mainAlert.setY(y);
    }

    public ReadOnlyDoubleProperty yProperty() {
        return mainAlert.yProperty();
    }

    public CustomAlert<AlertLayout> getNativeAlert() {
        return mainAlert;
    }
}
