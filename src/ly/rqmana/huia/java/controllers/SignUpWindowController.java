package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;

import java.net.URL;
import java.util.ResourceBundle;

public class SignUpWindowController implements Controllable {

    public Label titleLabel;
    public JFXTextField nameTF;
    public JFXPasswordField passwordTF;
    public JFXTextField usernameTF;
    public JFXPasswordField rePasswordTF;
    public JFXButton enterBtn;
    public JFXButton cancelBtn;

     public void setOnEnterBtnClicked(EventHandler<ActionEvent> handler) {
        enterBtn.setOnAction(handler);
    }

    public void setOnCancelBtnClicked(EventHandler<ActionEvent> handler) {
        cancelBtn.setOnAction(handler);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
