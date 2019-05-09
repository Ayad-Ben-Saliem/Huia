package ly.rqmana.huia.java.controls;

import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import ly.rqmana.huia.java.util.Res;

public class ToggleSwitch extends Control {

    private double width = 151;
    private double height = 27;
    private double padding = 1;
    private double radius = height / 2.0;

    private final StackPane container = new StackPane();
    private final Rectangle background = new Rectangle(width, height);
    private final Circle trigger = new Circle(radius);

    private final Label onLabel = new Label("On");
    private final Label offLabel = new Label("Off");

    private BooleanProperty status = new SimpleBooleanProperty(false);
    private final DoubleProperty animationDuration = new SimpleDoubleProperty(0.5);
    private ChangeListener<Boolean> statusChangeListener;

    public ToggleSwitch() {
        init();
        initStyle();

        container.getChildren().addAll(background, onLabel, offLabel, trigger);
        container.setAlignment(Pos.CENTER_LEFT);
        getChildren().add(container);

        setAnimation();

        Platform.runLater(() -> statusChangeListener.changed(null, null, getStatus()));

        setOnMouseClicked(event -> status.set(!status.get()));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SkinBase(this) {};
    }

    public void init() {
//        background.widthProperty().bind(widthProperty());
        widthProperty().addListener((observable, oldValue, newValue) -> {
            onLabel.setPrefWidth(newValue.doubleValue());
            offLabel.setPrefWidth(newValue.doubleValue());
        });

        setPrefSize(width, height);
    }

    private void initStyle() {
        this.getStyleClass().add("toggle-switch");
        container.getStyleClass().add("container");
        background.getStyleClass().add("background");
        trigger.getStyleClass().add("trigger");
        onLabel.getStyleClass().add("on-label");
        offLabel.getStyleClass().add("off-label");
        onLabel.getStyleClass().add("label");
        offLabel.getStyleClass().add("label");

        getStylesheets().add(Res.Stylesheet.DEFAULT_TOGGLE_SWITCH_STYLE.getUrl());
    }

    private void setAnimation() {
        TranslateTransition translateAnimation = new TranslateTransition(Duration.seconds(getAnimationDuration()));
        FadeTransition offFadeAnimation = new FadeTransition(Duration.seconds(getAnimationDuration()));
        FadeTransition onFadeAnimation = new FadeTransition(Duration.seconds(getAnimationDuration()));
        FillTransition fillAnimation = new FillTransition(Duration.seconds(getAnimationDuration()));
        ParallelTransition animation = new ParallelTransition(translateAnimation, fillAnimation, onFadeAnimation, offFadeAnimation);

        translateAnimation.setNode(trigger);
        onFadeAnimation.setNode(onLabel);
        offFadeAnimation.setNode(offLabel);
        fillAnimation.setShape(background);

        statusChangeListener = (observableValue, oldState, newState) -> {
            boolean isOn = newState;
            translateAnimation.setToX(isOn ? getWidth() - 2 * radius : 0);

            fillAnimation.setFromValue(isOn ? Color.WHITESMOKE : Color.WHITESMOKE);
            fillAnimation.setToValue(isOn ? Color.WHITESMOKE : Color.WHITESMOKE);

            onFadeAnimation.setFromValue(isOn ? 0 : 1);
            onFadeAnimation.setToValue(isOn ? 1 : 0);

            offFadeAnimation.setFromValue(isOn ? 1 : 0);
            offFadeAnimation.setToValue(isOn ? 0 : 1);

            animation.play();
        };

        status.addListener(statusChangeListener);
    }

    public void setStatus(Boolean status) {
        this.status.set(status);
    }

    public Boolean getStatus() {
        return status.get();
    }

    public BooleanProperty statusProperty() {
        return status;
    }

    public String getTextOn() {
        return this.onLabel.textProperty().get();
    }

    public StringProperty textOnProperty() {
        return onLabel.textProperty();
    }

    public void setTextOn(String textOn) {
        this.onLabel.textProperty().set(textOn);
    }

    public String getTextOff() {
        return offLabel.textProperty().get();
    }

    public StringProperty textOffProperty() {
        return offLabel.textProperty();
    }

    public void setTextOff(String textOff) {
        this.offLabel.textProperty().set(textOff);
    }

    public double getAnimationDuration() { return animationDuration.get(); }

    public DoubleProperty animationDurationProperty() { return animationDuration;}

    public void setAnimationDuration(double animationDuration) {
        this.animationDuration.set(animationDuration);
    }

    public Boolean getValue(){
        return getStatus();
    }

    public String getSelectionText() {
        if (getStatus())
            return getTextOn();
        else
            return getTextOff();
    }
}