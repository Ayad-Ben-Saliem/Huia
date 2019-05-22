package ly.rqmana.huia.java.controls.alerts;

import com.jfoenix.controls.JFXButton;
import javafx.scene.Node;

import static ly.rqmana.huia.java.util.Utils.getI18nString;


public class AlertAction {

    public static final AlertAction YES = new AlertAction(getI18nString("ALERT_YES"));
    public static final AlertAction NO = new AlertAction(getI18nString("ALERT_NO"));
    public static final AlertAction OK = new AlertAction(getI18nString("ALERT_OK"));
    public static final AlertAction CANCEL = new AlertAction(getI18nString("ALERT_CANCEL"));

    public static final AlertAction QUIT = new AlertAction(getI18nString("ALERT_QUIT"));
    public static final AlertAction CONTINUE = new AlertAction(getI18nString("ALERT_CONTINUE"));

    public static final AlertAction ABORT = new AlertAction(getI18nString("ALERT_ABORT"));
    public static final AlertAction TRY_AGAIN = new AlertAction(getI18nString("ALERT_TRY_AGAIN"));
    public static final AlertAction DISCARD = new AlertAction(getI18nString("ALERT_DISCARD"));
    public static final AlertAction DELETE = new AlertAction(getI18nString("ALERT_DELETE"));

    private final String text;
    private final Node graphic;

    public AlertAction(String text){
        this(text, null);
    }


    public AlertAction(String text, Node graphic){
        this.text = text;
        this.graphic = graphic;
    }


    public String getText() {
        return text;
    }

    public Node getGraphic() {
        return graphic;
    }

    JFXButton constructButton(){
        JFXButton button = new JFXButton();
        button.getStyleClass().addAll("alert-action-button", "common-button");
        button.setText(this.getText());
        button.setGraphic(this.getGraphic());
        button.setUserData(this);

        return button;
    }
}
