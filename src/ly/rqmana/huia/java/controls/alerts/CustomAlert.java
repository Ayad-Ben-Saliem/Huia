package ly.rqmana.huia.java.controls.alerts;

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.sun.istack.internal.Nullable;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.DialogPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import ly.rqmana.huia.java.util.Res;

import java.util.ArrayList;
import java.util.List;

public class CustomAlert<T extends AlertLayout> extends JFXAlert<AlertAction> {

    private final ObjectProperty<T> layout = new SimpleObjectProperty<>();

    private final ObservableList<AlertAction> alertActions = FXCollections.observableArrayList();

    public CustomAlert(){
        this((AlertAction) null);
    }

    public CustomAlert(AlertAction... actions){
        this(null, actions);
    }

    public CustomAlert(@Nullable Stage stage, AlertAction... actions){
        super(stage);

        setLayout((T) new AlertLayout());
        this.setContent(getLayout());

        prepareListeners();
        for (AlertAction action : actions) {
            if (action != null)
                getAlertActions().add(action);
        }

        getDialogPane().getStyleClass().add("custom-alert-pane");
        getDialogPane().getStylesheets().add(Res.Stylesheet.DEFAULT_CUSTOM_ALERT_STYLE.getUrl());
        getDialogPane().getStylesheets().add(Res.Stylesheet.THEME.getUrl());
        getDialogPane().getStylesheets().add(Res.Stylesheet.TEMPLATES.getUrl());
    }

    private void prepareListeners(){

        layoutProperty().addListener((observable, oldValue, newValue) -> {
            this.setContent(newValue);
        });

        getAlertActions().addListener((ListChangeListener<? super AlertAction>) c -> {
            while(c.next()){
                if (c.wasAdded()){
                    c.getAddedSubList().forEach(action -> {

                        final JFXButton actionButton = action.constructButton();

                        actionButton.setOnAction(event -> {
                            this.setResult(action);
                        });

                        layout().actionsFlow.getChildren().add(actionButton);
                    });
                }
                else if(c.wasRemoved()){
                    List<Node> toRemove = new ArrayList<>();

                    c.getRemoved().forEach(action -> {
                        for (Node node : layout().actionsFlow.getChildren()) {
                            if (node.getUserData() == action)
                                toRemove.add(node);
                        }
                    });

                    layout().actionsFlow.getChildren().removeAll(toRemove);
                }
            }
        });

        this.setOnCloseRequest(event -> {

            if (getResult() != null) {
                setResult(null);
            }
        });
    }

    public ObservableList<AlertAction> getAlertActions() {
        return alertActions;
    }

    protected T layout(){
        return this.getLayout();
    }

    public T getLayout() {
        return layout.get();
    }

    public ObjectProperty<T> layoutProperty() {
        return layout;
    }

    public  void setLayout(T layout) {
        this.layout.set(layout);
    }

    public void setHeadingText(String text){
        layout().headingLabel.setText(text);
    }

    public String getHeadingText(){
        return layout().headingLabel.getText();
    }

    public StringProperty headingTextProperty(){
        return layout().headingLabel.textProperty();
    }

    public void setBodyText(String text){
        layout().bodyLabel.setText(text);
    }

    public String getBodyText(){
        return layout().bodyLabel.getText();
    }

    public StringProperty bodyTextProperty(){
        return layout().bodyLabel.textProperty();
    }

    public void setGraphic(FontAwesomeIcon graphic){
        layout().graphicView.setIcon(graphic);
    }

    public void setGraphicFill(Paint fill){
        layout().graphicView.setFill(fill);
    }

    public Paint getGraphicFill(){
        return layout().graphicView.getFill();
    }

    public ObjectProperty<Paint> fillProperty(){
        return layout().graphicView.fillProperty();
    }

//    protected FontAwesomeIcon getGraphic(){
//        return layout()graphicView.getIcon();
//    }

//    protected ObjectProperty<FontAwesomeIcon> graphicProperty(){
//        return layout()graphicView.icon();
//    }

    public void setGraphicSize(double size){
        layout().graphicView.setGlyphSize(size);
    }

    public Number getGraphicSize(){
        return layout().graphicView.getGlyphSize();
    }

    public void setNodeOrientation(NodeOrientation orientation) {
        getLayout().setNodeOrientation(orientation);
    }

    public NodeOrientation getNodeOrientation() {
        return getLayout().getNodeOrientation();
    }

    public ObjectProperty<NodeOrientation> nodeOrientationProperty() {
        return getLayout().nodeOrientationProperty();
    }
}
