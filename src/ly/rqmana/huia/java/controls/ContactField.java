package ly.rqmana.huia.java.controls;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import ly.rqmana.huia.java.util.Res;

public class ContactField extends Control {

    private final ObjectProperty<HBox> container = new SimpleObjectProperty<>(new HBox());
    private final ObjectProperty<Type> type = new SimpleObjectProperty<>();
    private final ObjectProperty<Node> icon = new SimpleObjectProperty<>();
    private final ObjectProperty<JFXTextField> inputField = new SimpleObjectProperty<>(new JFXTextField());
    private final ObjectProperty<JFXButton> cancelBtn = new SimpleObjectProperty<>(new JFXButton());
    private final ObjectProperty<Node> cancelIcon = new SimpleObjectProperty<>(new FontAwesomeIconView(FontAwesomeIcon.TIMES));
    private final BooleanProperty cancelable = new SimpleBooleanProperty(true);

    private final StringProperty promptText = new SimpleStringProperty();
    private final BooleanProperty labelFloat = new SimpleBooleanProperty();


    public ContactField() {
        this(Type.OTHER);
    }

    public ContactField(Type type) {
        this(type, true);
    }

    public ContactField(Type type, Boolean cancelable) {
        setType(type);
        setCancelable(cancelable);

        init();
        initStyle();

        getChildren().add(getContainer());
    }

    private void init() {

        setIcon(getType().getIcon());

        iconProperty().addListener((observable, oldValue, newValue) -> {
            getContainer().getChildren().remove(0);
            getContainer().getChildren().add(0, getIcon());
        });

        typeProperty().addListener((observable, oldValue, newValue) -> {
            setIcon(newValue.getIcon());
        });

        getInputField().labelFloatProperty().bind(labelFloatProperty());
        getInputField().promptTextProperty().bind(promptTextProperty());

        getCancelBtn().setGraphic(getCancelIcon());
        getCancelBtn().setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        getCancelBtn().setVisible(false);
        getCancelBtn().setMinSize(0, 0);

        cancelableProperty().addListener((observable, oldValue, newValue) -> {
            getCancelBtn().setManaged(!newValue);
            if (newValue) {
                getCancelBtn().setVisible(false);
            }
        });

        setOnMouseEntered(event -> {
            if (isCancelable()) {
                getCancelBtn().setVisible(true);
            }
        });

        setOnMouseExited(event -> {
            if (isCancelable()) {
                getCancelBtn().setVisible(false);
            }
        });

        getCancelBtn().setFocusTraversable(true);

//        getCancelBtn().focusedProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue && !isCancelable()) {
//                getInputField().requestFocus();
//            }
//        });

        getContainer().getChildren().addAll(getIcon(), getInputField(), getCancelBtn());
        getContainer().setAlignment(Pos.CENTER);
        getInputField().setPadding(new Insets(0, 0, 0, 10));
        getInputField().setPrefHeight(27);
    }

    private void initStyle() {
        getStyleClass().add("rq-contact-field");
        getContainer().getStyleClass().add("container");
        getIcon().getStyleClass().add("icon");
        getInputField().getStyleClass().add("input-field");
        getCancelBtn().getStyleClass().add("cancel-button");
        getCancelBtn().getGraphic().getStyleClass().add("cancel-button-icon");

        getStylesheets().add(Res.Stylesheet.DEFAULT_CONTACT_FIELD_STYLE.getUrl());
    }

    protected Skin<?> createDefaultSkin() {
        return new SkinBase(this) {};
    }


    public HBox getContainer() {
        return container.get();
    }

    public ObjectProperty<HBox> containerProperty() {
        return container;
    }

    public void setContainer(HBox container) {
        this.container.set(container);
    }

    public Type getType() {
        return type.get();
    }

    public ObjectProperty<Type> typeProperty() {
        return type;
    }

    public void setType(Type type) {
        this.type.set(type);
    }

    public Node getIcon() {
        return icon.get();
    }

    public ObjectProperty<Node> iconProperty() {
        return icon;
    }

    public void setIcon(Image icon) {
        ImageView imageIcon = new ImageView(icon);
        imageIcon.setFitWidth(27);
        imageIcon.setFitHeight(27);
        setIcon(imageIcon);
    }

    public void setIcon(FontAwesomeIcon icon) {
        setIcon(new FontAwesomeIconView(icon));
    }

    public void setIcon(Node icon) {
        this.icon.set(icon);
    }

    public JFXTextField getInputField() {
        return inputField.get();
    }

    public ObjectProperty<JFXTextField> inputFieldProperty() {
        return inputField;
    }

    public void setInputField(JFXTextField inputField) {
        this.inputField.set(inputField);
    }

    public JFXButton getCancelBtn() {
        return cancelBtn.get();
    }

    public ObjectProperty<JFXButton> cancelBtnProperty() {
        return cancelBtn;
    }

    public void setCancelBtn(JFXButton cancelBtn) {
        this.cancelBtn.set(cancelBtn);
    }

    public Node getCancelIcon() {
        return cancelIcon.get();
    }

    public ObjectProperty<Node> cancelIconProperty() {
        return cancelIcon;
    }

    public void setCancelIcon(Node cancelIcon) {
        this.cancelIcon.set(cancelIcon);
    }

    public boolean isCancelable() {
        return cancelable.get();
    }

    public BooleanProperty cancelableProperty() {
        return cancelable;
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable.set(cancelable);
    }

    public String getPromptText() {
        return promptText.get();
    }

    public StringProperty promptTextProperty() {
        return promptText;
    }

    public void setPromptText(String promptText) {
        this.promptText.set(promptText);
    }

    public boolean isLabelFloat() {
        return labelFloat.get();
    }

    public BooleanProperty labelFloatProperty() {
        return labelFloat;
    }

    public void setLabelFloat(boolean labelFloat) {
        this.labelFloat.set(labelFloat);
    }

    public void setOnInputFieldFired(EventHandler<ActionEvent> eventHandler) {
        getInputField().setOnAction(eventHandler);
    }

    public <T extends Event> void addInputFieldEventFilter(EventType<T> eventType, EventHandler<? super T> eventFilter) {
        getInputField().addEventFilter(eventType, eventFilter);
    }

    public <T extends Event> void addInputFieldEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        getInputField().addEventHandler(eventType, eventHandler);
    }

    public String getText() {
        return getInputField().getText();
    }

    public void setValidator(ValidatorBase ... validators) {
        getInputField().setValidators(validators);
    }

    public void resetValidation() {
        getInputField().resetValidation();
    }

    public ValidatorBase getActiveValidator() {
        return getInputField().getActiveValidator();
    }

    public ReadOnlyObjectProperty<ValidatorBase> activeValidatorProperty() {
        return getInputField().activeValidatorProperty();
    }

    public void setValidators(ValidatorBase ... validators) {
        getInputField().setValidators(validators);
    }

    public boolean validate() {
        return getInputField().validate();
    }

    public ObservableList<ValidatorBase> getValidators() {
        return getInputField().getValidators();
    }

    public void setOnCancelBtnFired(EventHandler<ActionEvent> eventHandler) {
        getCancelBtn().setOnAction(eventHandler);
    }

    public <T extends Event> void addCancelBtnEventFilter(EventType<T> eventType, EventHandler<? super T> eventFilter) {
        getCancelBtn().addEventFilter(eventType, eventFilter);
    }

    public <T extends Event> void addCancelBtnEventHandler(EventType<T> eventType, EventHandler<? super T> eventHandler) {
        getCancelBtn().addEventHandler(eventType, eventHandler);
    }

    public enum Type {
        ADDRESS(Res.Image.HOME_ADDRESS),
        PHONE(FontAwesomeIcon.PHONE),
        MAILBOX(FontAwesomeIcon.INBOX),
        EMAIL(FontAwesomeIcon.ENVELOPE),
        FACEBOOK(FontAwesomeIcon.FACEBOOK),
        INSTAGRAM(FontAwesomeIcon.INSTAGRAM),
        TWITTER(FontAwesomeIcon.TWITTER),
        WHATSAPP(FontAwesomeIcon.WHATSAPP),
        VIBER(Res.Image.VIBER),
        OTHER(Res.Image.CONTACTS);

        private final Object icon;
        private Node iconNode;

        Type(FontAwesomeIcon icon) {
            this.icon = icon;
        }

        Type(Res.Image imageIcon) {
            icon = imageIcon;
        }

        public Node getIcon() {
            if (icon instanceof FontAwesomeIcon) {
                iconNode = new FontAwesomeIconView((FontAwesomeIcon) icon);
                ((FontAwesomeIconView) this.iconNode).setSize("22");
            } else
            if (icon instanceof Res.Image) {
                iconNode = new ImageView(((Res.Image) icon).getImage());
                ((ImageView) iconNode).setFitHeight(27);
                ((ImageView) iconNode).setFitWidth(27);
            }
            iconNode.getStyleClass().add("icon");
            return iconNode;
        }
    }
}