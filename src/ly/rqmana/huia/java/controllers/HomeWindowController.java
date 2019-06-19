package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.scene.control.Label;
import ly.rqmana.huia.java.security.Auth;
import ly.rqmana.huia.java.util.Controllable;

import java.net.URL;
import java.util.ResourceBundle;

public class HomeWindowController implements Controllable {
    public Label companyName;
    public JFXButton loginBtn;

    public void onLoginBtnClicked() {
        Auth.authenticate();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
