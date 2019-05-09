package ly.rqmana.huia.java.controllers.installation;

import com.jfoenix.controls.JFXCheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.TextFlow;
import ly.rqmana.huia.java.util.Controllable;

import java.net.URL;
import java.util.ResourceBundle;

public class LicenseTermsPageController implements Controllable {
    public TextFlow licenseTextFlow;
    public ScrollPane licenseContainer;
    public JFXCheckBox acceptTermsCheckBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        licenseContainer.widthProperty().addListener((observable, oldValue, newValue) -> licenseTextFlow.setPrefWidth(newValue.doubleValue() - 25));
    }
}
