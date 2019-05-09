package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import ly.rqmana.huia.java.util.Controllable;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginWindowController implements Controllable {
    public VBox formContainer;
    public JFXTextField usernameTF;
    public JFXPasswordField passwordTF;
    public JFXButton loginBtn;
    public JFXButton cancelBtn;
    public Label authFailed;
    public JFXSpinner spinner;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

}
