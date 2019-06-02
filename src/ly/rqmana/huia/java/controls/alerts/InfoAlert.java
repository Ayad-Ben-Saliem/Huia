package ly.rqmana.huia.java.controls.alerts;

import com.jfoenix.controls.JFXButton;
import com.sun.istack.internal.Nullable;
import javafx.beans.property.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static ly.rqmana.huia.java.util.Utils.getI18nString;

public class InfoAlert extends CustomAlert<InfoAlert.InfoAlertLayout>{

    private final ObjectProperty<InfoAlertType> alertType = new SimpleObjectProperty<>();

    private final BooleanProperty useDetails = new SimpleBooleanProperty();
    private final BooleanProperty showingDetails = new SimpleBooleanProperty();

    // got to use this because the super layout() method doesn't return
    // InfoAlertLayout but AlertLayout and this doesn't contain details stuff..
    private final InfoAlertLayout myCustomLayout = new InfoAlertLayout();

    protected class InfoAlertLayout extends AlertLayout{

        private JFXButton detailsButton = new JFXButton(getI18nString("ALERT_SHOW_DETAILS"));

        private TitledPane detailsPane;
        private TextArea detailsArea;
        private JFXButton copyDetailsButton;

        private InfoAlertLayout(){
            super();
            initComponents();

            this.getStyleClass().add("info-alert-layout");
        }

        private void initComponents(){

            try {
                this.detailsPane = FXMLLoader.load(Res.Fxml.INFO_ALERT_DETAILS_LAYOUT.getUrl(), Utils.getBundle());
                VBox content = (VBox) detailsPane.getContent();
                this.detailsArea = (TextArea) content.getChildren().get(0);
                this.copyDetailsButton = (JFXButton) content.getChildren().get(1);

            } catch (IOException e) {
                e.printStackTrace();
            }

            buttonsBar.getChildren().add(0, detailsButton);
            footerPane.getChildren().add(detailsPane);

            this.detailsArea.setText(null);
            this.detailsButton.setVisible(false);
            this.detailsButton.setManaged(false);

            this.detailsPane.setVisible(false);
            this.detailsPane.setManaged(false);
            this.detailsPane.setExpanded(false);
        }
    }

    public InfoAlert(){
        this(InfoAlertType.NONE, (AlertAction) null);
    }

    public InfoAlert(InfoAlertType infoAlertType, AlertAction... actions){
        this(null, infoAlertType, actions);
    }

    public InfoAlert(@Nullable Stage stage, InfoAlertType infoAlertType, AlertAction... actions){
        super(stage);

        setLayout(myCustomLayout);

        initListeners();
        setAlertType(infoAlertType);
        getAlertActions().addAll(actions);

        getDialogPane().getStyleClass().add("info-alert-pane");
    }

    private void initListeners(){

        alertTypeProperty().addListener((observable, oldValue, newValue) -> {
            myCustomLayout.graphicView.setIcon(newValue.graphic);
            myCustomLayout.graphicView.setFill(newValue.fill);
            myCustomLayout.graphicView.setGlyphSize(newValue.glyphSize);
        });

        useDetailsProperty().addListener((observable, oldValue, newValue) -> onUseDetailsChanged());

        myCustomLayout.detailsButton.setOnAction(event -> {
            setShowingDetails(! isShowingDetails());
        });

        myCustomLayout.copyDetailsButton.setOnAction(event -> {
            Clipboard systemClipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(getDetailsText());
            systemClipboard.setContent(content);

        });

        showingDetails.bindBidirectional(myCustomLayout.detailsPane.expandedProperty());

        myCustomLayout.detailsPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                myCustomLayout.detailsButton.setText(getI18nString("ALERT_HIDE_DETAILS"));
            else
                myCustomLayout.detailsButton.setText(getI18nString("ALERT_SHOW_DETAILS"));
        });

    }

    private void onUseDetailsChanged(){
        myCustomLayout.detailsPane.setVisible(isUseDetails());
        myCustomLayout.detailsPane.setManaged(isUseDetails());
        myCustomLayout.detailsButton.setVisible(isUseDetails());
        myCustomLayout.detailsButton.setManaged(isUseDetails());
    }

    public void visualizeStackTrace(Throwable throwable){

        StringWriter stackTraceWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTraceWriter));

        String detailsText = stackTraceWriter.toString();
        setDetailsText(detailsText);
    }

    public void setDetailsText(String text){
        if (! isUseDetails())
            throw new IllegalStateException("InfoAlert is not set to use details");
        this.myCustomLayout.detailsArea.setText(text);
    }

    public String getDetailsText(){
        if (! isUseDetails())
            throw new IllegalStateException("InfoAlert is not set to use details");

        return this.myCustomLayout.detailsArea.getText();
    }

    public StringProperty detailsTextProperty(){
        if (! isUseDetails())
            throw new IllegalStateException("InfoAlert is not set to use details");

        return this.myCustomLayout.detailsArea.textProperty();
    }

    public boolean isShowingDetails() {
        if (! isUseDetails())
            throw new IllegalStateException("InfoAlert is not set to use details");

        return showingDetails.get();
    }

    public BooleanProperty showingDetailsProperty() {
        if (! isUseDetails())
            throw new IllegalStateException("InfoAlert is not set to use details");
        return showingDetails;
    }

    public void setShowingDetails(boolean showingDetails) {
        if (! isUseDetails())
            throw new IllegalStateException("InfoAlert is not set to use details");

        this.showingDetails.set(showingDetails);
    }


    public boolean isUseDetails() {
        return useDetails.get();
    }

    public BooleanProperty useDetailsProperty() {
        return useDetails;
    }

    public void setUseDetails(boolean useDetails) {
        this.useDetails.set(useDetails);
    }

    public InfoAlertType getAlertType() {
        return alertType.get();
    }

    public ObjectProperty<InfoAlertType> alertTypeProperty() {
        return alertType;
    }

    public void setAlertType(InfoAlertType infoAlertType) {
        this.alertType.set(infoAlertType);
    }
}
