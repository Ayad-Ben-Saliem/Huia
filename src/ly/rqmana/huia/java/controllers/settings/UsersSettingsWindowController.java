package ly.rqmana.huia.java.controllers.settings;

import com.jfoenix.controls.JFXDialog;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;
import ly.rqmana.huia.java.controllers.MainWindowController;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UsersSettingsWindowController implements Controllable {

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void onAddNewUserBtnClicked(ActionEvent actionEvent) throws IOException {
        Region content = FXMLLoader.load(Res.Fxml.ADD_NEW_USER_WINDOW.getUrl(), Utils.getBundle());
        JFXDialog dialog = new JFXDialog(getRootStack(), content, JFXDialog.DialogTransition.CENTER);
        dialog.show();
    }
}
