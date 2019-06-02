package ly.rqmana.huia.java.controls.alerts;

import com.sun.istack.internal.Nullable;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.util.Optional;

public abstract class Alerts {

    private Alerts(){
    }

    public static Optional<AlertAction> confirmAlert(@Nullable Stage stage, String heading, String body, AlertAction... actions){
        InfoAlert alert = prepAlert(stage, InfoAlertType.CONFIRM, heading, body, actions);
        Toolkit.getDefaultToolkit().beep();
        return alert.showAndWait();
    }

    public static Optional<AlertAction> infoAlert(@Nullable Stage stage, String heading, String body, AlertAction... actions){
        InfoAlert alert = prepAlert(stage, InfoAlertType.INFO, heading, body, actions);
        return alert.showAndWait();
    }

    public static Optional<AlertAction> warningAlert(@Nullable Stage stage, String heading, String body, AlertAction... actions){
        InfoAlert alert = prepAlert(stage, InfoAlertType.WARNING, heading, body, actions);
        Toolkit.getDefaultToolkit().beep();
        return alert.showAndWait();
    }

    public static Optional<AlertAction> errorAlert(@Nullable Stage stage, String heading, String body,@Nullable Throwable throwable, AlertAction... actions){


        Toolkit.getDefaultToolkit().beep();
        return createErrorAlert(stage, heading, body, throwable, actions).showAndWait();
    }

    public static InfoAlert createErrorAlert(@Nullable Stage stage, String heading, String body,@Nullable Throwable throwable, AlertAction... actions){
        InfoAlert alert = prepAlert(stage, InfoAlertType.ERROR, heading, body, actions);

        if (throwable != null){
            alert.setUseDetails(true);
            alert.visualizeStackTrace(throwable);
        }
        return alert;
    }

    private static InfoAlert prepAlert(@Nullable Stage stage, InfoAlertType type, String heading, String body, AlertAction... actions){

        InfoAlert alert = new InfoAlert(stage, type, actions);
        alert.setHeadingText(heading);
        alert.setBodyText(body);

        if (stage == null){
            alert.initStyle(StageStyle.TRANSPARENT);

            alert.getLayout().heightProperty().addListener((observable, oldValue, newValue) -> {
                // causes too much load on cpu,
                // but may not be that problem used only once or twice..
                alert.setHeight(newValue.doubleValue());

                // dont use the following way, bad look, very recourse consumer..
                //      alert.getLayout().getScene().getWindow().sizeToScene();
            });
        }

        return alert;
    }
}