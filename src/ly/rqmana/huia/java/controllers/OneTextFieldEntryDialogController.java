package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.Label;
import ly.rqmana.huia.java.util.Controllable;

import java.net.URL;
import java.util.ResourceBundle;

public class OneTextFieldEntryDialogController implements Controllable {
    public Label label;
    public JFXTextField textField;
    public JFXButton okBtn;
    public JFXButton cancelBtn;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }
}
