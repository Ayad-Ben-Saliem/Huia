package ly.rqmana.huia.java.controls.alerts;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;

import java.io.IOException;

public class AlertLayout extends VBox {

    @FXML protected HBox headingPane;
    @FXML protected Label headingLabel;
    @FXML protected FontAwesomeIconView graphicView;

    @FXML protected StackPane bodyPane;
    @FXML protected Label bodyLabel;

    @FXML protected VBox footerPane;
    @FXML protected HBox buttonsBar;
    @FXML protected FlowPane actionsFlow;

    public AlertLayout(){
        super();
        createSceneGraph();
    }

    private void createSceneGraph(){
        FXMLLoader fxmlLoader = new FXMLLoader(Res.Fxml.CUSTOM_ALERT_LAYOUT.getUrl(), Utils.getBundle());
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    protected void initialize(){

    }

}
