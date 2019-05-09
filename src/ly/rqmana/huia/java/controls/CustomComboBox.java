package ly.rqmana.huia.java.controls;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.converters.ButtonTypeConverter;
import com.jfoenix.validation.base.ValidatorBase;
import com.sun.javafx.css.converters.BooleanConverter;
import com.sun.javafx.css.converters.PaintConverter;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.css.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import ly.rqmana.huia.java.util.Res;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomComboBox<T> extends Control {

    private final ReadOnlyObjectProperty<HBox> container = new SimpleObjectProperty<>(new HBox());
    private final ReadOnlyObjectProperty<VBox> buttonsContainer = new SimpleObjectProperty<>(new VBox());
    private final ReadOnlyObjectProperty<JFXComboBox<T>> comboBox = new SimpleObjectProperty<>(new JFXComboBox<T>());
    private final ReadOnlyObjectProperty<JFXButton> upButton = new SimpleObjectProperty<>(new JFXButton());
    private final ReadOnlyObjectProperty<JFXButton> downButton = new SimpleObjectProperty<>(new JFXButton());

    private FontAwesomeIconView upArrow = new FontAwesomeIconView(FontAwesomeIcon.ANGLE_UP);
    private FontAwesomeIconView downArrow = new FontAwesomeIconView(FontAwesomeIcon.ANGLE_DOWN);


    /***************************************************************************
     *                                                                         *
     * Properties                                                              *
     *                                                                         *
     **************************************************************************/
    /**
     * the ripple color property of JFXButton.
     */
    private ObjectProperty<Paint> ripplerFill = new SimpleObjectProperty<>(null);

    public final ObjectProperty<Paint> ripplerFillProperty() {
        return this.ripplerFill;
    }

    /**
     * @return the ripple color
     */
    public final Paint getRipplerFill() {
        return this.ripplerFillProperty().get();
    }

    /**
     * set the ripple color
     *
     * @param ripplerFill the color of the ripple effect
     */
    public final void setRipplerFill(final Paint ripplerFill) {
        this.ripplerFillProperty().set(ripplerFill);
    }


    /**
     * default color used when the field is focused
     */
    private StyleableObjectProperty<Paint> focusColor = new SimpleStyleableObjectProperty<>(StyleableProperties.FOCUS_COLOR, this, "focusColor", Color.valueOf("#FF0000"));

    /**
     * default color used when the field is unfocused
     */
    private StyleableObjectProperty<Paint> unFocusColor = new SimpleStyleableObjectProperty<>(StyleableProperties.UNFOCUS_COLOR, this, "unFocusColor", Color.valueOf("#FF8888"));

    /**
     * set true to show a float the prompt text when focusing the field
     */
    private StyleableBooleanProperty labelFloat = new SimpleStyleableBooleanProperty(StyleableProperties.LABEL_FLOAT,this,"labelFloat",false);

    /**
     * disable animation on validation
     */
    private StyleableBooleanProperty disableAnimation = new SimpleStyleableBooleanProperty(StyleableProperties.DISABLE_ANIMATION,this,"disableAnimation",false);

    /**
     * according to material design the button has two types:
     * - flat : only shows the ripple effect upon clicking the button
     * - raised : shows the ripple effect and change in depth upon clicking the button
     */
    private StyleableObjectProperty<JFXButton.ButtonType> buttonType = new SimpleStyleableObjectProperty<>(StyleableProperties.BUTTON_TYPE,this,"buttonType", JFXButton.ButtonType.FLAT);

    /**
     * Disable the visual indicator for focus
     */
    private StyleableBooleanProperty disableVisualFocus = new SimpleStyleableBooleanProperty(StyleableProperties.DISABLE_VISUAL_FOCUS,this,"disableVisualFocus",false);


    public CustomComboBox() {
        init();

        getComboBox().unFocusColorProperty().bind(unFocusColorProperty());
        getComboBox().focusColorProperty().bind(focusColorProperty());
        getComboBox().labelFloatProperty().bind(labelFloatProperty());
        getComboBox().disableAnimationProperty().bind(disableAnimationProperty());

        getUpButton().ripplerFillProperty().bind(ripplerFillProperty());
        getUpButton().buttonTypeProperty().bind(buttonTypeProperty());
        getUpButton().disableVisualFocusProperty().bind(disableVisualFocusProperty());
        getDownButton().ripplerFillProperty().bind(ripplerFillProperty());
        getDownButton().buttonTypeProperty().bind(buttonTypeProperty());
        getDownButton().disableVisualFocusProperty().bind(disableVisualFocusProperty());

        getUpButton().ripplerFillProperty().bind(unFocusColorProperty());
        getDownButton().ripplerFillProperty().bind(unFocusColorProperty());

        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                getComboBox().requestFocus();
            }
        });

        getComboBox().focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                upArrow.setFill(getComboBox().getFocusColor());
                downArrow.setFill(getComboBox().getFocusColor());
            } else {
                upArrow.setFill(getComboBox().getUnFocusColor());
                downArrow.setFill(getComboBox().getUnFocusColor());
            }
        });

        getComboBox().focusColorProperty().addListener((observable, oldValue, newValue) -> {
            if (getComboBox().isFocused()) {
                upArrow.setFill(getComboBox().getFocusColor());
                downArrow.setFill(getComboBox().getFocusColor());
            }
        });

        getComboBox().unFocusColorProperty().addListener((observable, oldValue, newValue) -> {
            if (!getComboBox().isFocused()) {
                upArrow.setFill(getComboBox().getUnFocusColor());
                downArrow.setFill(getComboBox().getUnFocusColor());
            }
        });

        upArrow.setFill(getComboBox().getUnFocusColor());
        downArrow.setFill(getComboBox().getUnFocusColor());
    }

    private void init() {
        getUpButton().setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        getDownButton().setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        getUpButton().setGraphic(upArrow);
        getDownButton().setGraphic(downArrow);

        getUpButton().setOnAction(event -> getComboBox().getSelectionModel().select(getComboBox().getSelectionModel().getSelectedIndex() - 1));
        getDownButton().setOnAction(event -> getComboBox().getSelectionModel().select(getComboBox().getSelectionModel().getSelectedIndex() + 1));
        getUpButton().setFocusTraversable(false);
        getDownButton().setFocusTraversable(false);


        getButtonsContainer().getChildren().addAll(getUpButton(), getDownButton());
        getContainer().getChildren().addAll(getComboBox(), getButtonsContainer());
        HBox.setHgrow(getComboBox(), Priority.ALWAYS);
        getComboBox().setMaxWidth(Double.MAX_VALUE);

        getComboBox().heightProperty().addListener((observable, oldValue, newValue) ->  {
            getButtonsContainer().setMaxHeight(newValue.doubleValue());
            getButtonsContainer().setMinHeight(newValue.doubleValue());
        });

        getButtonsContainer().heightProperty().addListener((observable, oldValue, newValue) -> {
            getUpButton().setMaxHeight(newValue.doubleValue() / 2);
            getUpButton().setMinHeight(newValue.doubleValue() / 2);
            getDownButton().setMaxHeight(newValue.doubleValue() / 2);
            getDownButton().setMinHeight(newValue.doubleValue() / 2);
        });

        getContainer().setMinHeight(0);
        setPrefSize(151, 27);
        getComboBox().setMinWidth(0);
        getComboBox().setMaxWidth(Double.MAX_VALUE);

        getChildren().add(getContainer());


        initializeStyle();
    }

    private void initializeStyle() {
        this.getStyleClass().add("rq-custom-combo-box");
        getContainer().getStyleClass().add("rq-container");
        getComboBox().getStyleClass().add("rq-combo-box");
        getComboBox().getStyleClass().add("main-field");
        getButtonsContainer().getStyleClass().add("rq-buttons-container");
        getUpButton().getStyleClass().add("rq-up-button");
        getDownButton().getStyleClass().add("rq-down-button");
        getUpButton().getStyleClass().add("rq-button");
        getDownButton().getStyleClass().add("rq-button");
        upArrow.getStyleClass().add("rq-up-arrow");
        downArrow.getStyleClass().add("rq-down-arrow");
        upArrow.getStyleClass().add("rq-arrow");
        downArrow.getStyleClass().add("rq-arrow");

        getStylesheets().add(Res.Stylesheet.DEFAULT_CUSTOM_COMBO_BOX_STYLE.getUrl());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SkinBase(this) {};
    }

    public String getPromptText() {
        return getComboBox().getPromptText();
    }

    public StringProperty promptTextProperty() {
        return getComboBox().promptTextProperty();
    }

    public void setPromptText(String promptText) {
        getComboBox().setPromptText(promptText);
    }

    public HBox getContainer() {
        return container.get();
    }

    public ReadOnlyObjectProperty<HBox> containerProperty() {
        return container;
    }

    public VBox getButtonsContainer() {
        return buttonsContainer.get();
    }

    public ReadOnlyObjectProperty<VBox> buttonsContainerProperty() {
        return buttonsContainer;
    }

    public JFXComboBox<T> getComboBox() {
        return comboBox.get();
    }

    public ReadOnlyObjectProperty<JFXComboBox<T>> comboBoxProperty() {
        return comboBox;
    }

    public JFXButton getUpButton() {
        return upButton.get();
    }

    public ReadOnlyObjectProperty<JFXButton> upButtonProperty() {
        return upButton;
    }

    public JFXButton getDownButton() {
        return downButton.get();
    }

    public ReadOnlyObjectProperty<JFXButton> downButtonProperty() {
        return downButton;
    }

    public void setItems(ObservableList<T> items) {
        getComboBox().setItems(items);
    }

    public ObservableList<T> getItems() {
        return getComboBox().getItems();
    }

    public Paint getFocusColor() {
        return focusColor.get();
    }

    public StyleableObjectProperty<Paint> focusColorProperty() {
        return focusColor;
    }

    public void setFocusColor(Paint focusColor) {
        this.focusColor.set(focusColor);
    }

    public Paint getUnFocusColor() {
        return unFocusColor.get();
    }

    public StyleableObjectProperty<Paint> unFocusColorProperty() {
        return unFocusColor;
    }

    public void setUnFocusColor(Paint unFocusColor) {
        this.unFocusColor.set(unFocusColor);
    }

    public boolean isLabelFloat() {
        return labelFloat.get();
    }

    public StyleableBooleanProperty labelFloatProperty() {
        return labelFloat;
    }

    public void setLabelFloat(boolean labelFloat) {
        this.labelFloat.set(labelFloat);
    }

    public boolean isDisableAnimation() {
        return disableAnimation.get();
    }

    public StyleableBooleanProperty disableAnimationProperty() {
        return disableAnimation;
    }

    public void setDisableAnimation(boolean disableAnimation) {
        this.disableAnimation.set(disableAnimation);
    }

    public void setValue(T value) {
        getComboBox().setValue(value);
    }

    public SingleSelectionModel<T> getSelectionModel() {
        return getComboBox().getSelectionModel();
    }

    public T getValue() {
        return getComboBox().getValue();
    }

    public ObjectProperty<T> valueProperty() {
        return getComboBox().valueProperty();
    }

    public enum ButtonType {FLAT, RAISED}

    public JFXButton.ButtonType getButtonType() {
        return buttonType == null ? JFXButton.ButtonType.FLAT : buttonType.get();
    }

    public StyleableObjectProperty<JFXButton.ButtonType> buttonTypeProperty() {
        return this.buttonType;
    }

    public void setButtonType(JFXButton.ButtonType type) {
        this.buttonType.set(type);
    }

    public void setValidators(ValidatorBase ... validators) {
        getComboBox().setValidators(validators);
    }

    public boolean validate() {
        return getComboBox().validate();
    }

    /**
     * Setting this property disables this {@link JFXButton} from showing keyboard focus.
     * @return A property that will disable visual focus if true and enable it if false.
     */
    public final StyleableBooleanProperty disableVisualFocusProperty() {
        return this.disableVisualFocus;
    }

    /**
     * Indicates whether or not this {@link JFXButton} will show focus when it receives keyboard focus.
     * @return False if this {@link JFXButton} will show visual focus and true if it will not.
     */
    public final Boolean isDisableVisualFocus() {
        return disableVisualFocus != null && this.disableVisualFocusProperty().get();
    }

    /**
     * Setting this to true will disable this {@link JFXButton} from showing focus when it receives keyboard focus.
     * @param disabled True to disable visual focus and false to enable it.
     */
    public final void setDisableVisualFocus(final Boolean disabled) {
        this.disableVisualFocusProperty().set(disabled);
    }


    private static class StyleableProperties {
        private static final CssMetaData<CustomComboBox<?>, Paint> UNFOCUS_COLOR =
                new CssMetaData<CustomComboBox<?>, Paint>("-rq-unfocus-color", PaintConverter.getInstance(), Color.valueOf("#A6A6A6")) {
                    @Override
                    public boolean isSettable(CustomComboBox<?> control) {
                        return control.unFocusColor == null || !control.unFocusColor.isBound();
                    }

                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(CustomComboBox<?> control) {
                        return control.unFocusColorProperty();
                    }
                };

        private static final CssMetaData<CustomComboBox<?>, Paint> FOCUS_COLOR =
                new CssMetaData<CustomComboBox<?>, Paint>("-rq-focus-color", PaintConverter.getInstance(), Color.valueOf("#3f51b5")) {
                    @Override
                    public boolean isSettable(CustomComboBox<?> control) {
                        return control.focusColor == null || !control.focusColor.isBound();
                    }

                    @Override
                    public StyleableProperty<Paint> getStyleableProperty(CustomComboBox<?> control) {
                        return control.focusColorProperty();
                    }
                };

        private static final CssMetaData<CustomComboBox<?>, Boolean> LABEL_FLOAT =
                new CssMetaData<CustomComboBox<?>, Boolean>("-rq-label-float", BooleanConverter.getInstance(),false) {
                    @Override
                    public boolean isSettable(CustomComboBox<?> control) {
                        return control.labelFloat == null || !control.labelFloat.isBound();
                    }

                    @Override
                    public StyleableBooleanProperty getStyleableProperty(CustomComboBox<?> control) {
                        return control.labelFloatProperty();
                    }
                };

        private static final CssMetaData<CustomComboBox<?>, Boolean> DISABLE_ANIMATION =
                new CssMetaData<CustomComboBox<?>, Boolean>("-rq-disable-animation", BooleanConverter.getInstance(), false) {
                    @Override
                    public boolean isSettable(CustomComboBox control) {
                        return control.disableAnimation == null || !control.disableAnimation.isBound();
                    }

                    @Override
                    public StyleableBooleanProperty getStyleableProperty(CustomComboBox<?> control) {
                        return control.disableAnimationProperty();
                    }
                };

        private static final CssMetaData<CustomComboBox<?>, JFXButton.ButtonType> BUTTON_TYPE =
                new CssMetaData<CustomComboBox<?>, JFXButton.ButtonType>("-rq-button-type", ButtonTypeConverter.getInstance(), JFXButton.ButtonType.FLAT) {
                    @Override
                    public boolean isSettable(CustomComboBox<?> control) {
                        return control.buttonType == null || !control.buttonType.isBound();
                    }

                    @Override
                    public StyleableProperty<JFXButton.ButtonType> getStyleableProperty(CustomComboBox<?> control) {
                        return control.buttonTypeProperty();
                    }
                };

        private static final CssMetaData<CustomComboBox<?>, Boolean> DISABLE_VISUAL_FOCUS =
                new CssMetaData<CustomComboBox<?>, Boolean>("-rq-disable-visual-focus", BooleanConverter.getInstance(), false) {
                    @Override
                    public boolean isSettable(CustomComboBox<?> control) {
                        return control.disableVisualFocus == null || !control.disableVisualFocus.isBound();
                    }

                    @Override
                    public StyleableBooleanProperty getStyleableProperty(CustomComboBox<?> control) {
                        return control.disableVisualFocusProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> CHILD_STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>();
            Collections.addAll(styleables, UNFOCUS_COLOR, FOCUS_COLOR, LABEL_FLOAT, DISABLE_ANIMATION, BUTTON_TYPE,DISABLE_VISUAL_FOCUS);
            CHILD_STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    // inherit the styleable properties from parent
    private List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        if (STYLEABLES == null) {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Control.getClassCssMetaData());
            styleables.addAll(ComboBox.getClassCssMetaData());
            styleables.addAll(getClassCssMetaData());
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
        return STYLEABLES;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.CHILD_STYLEABLES;
    }
}
