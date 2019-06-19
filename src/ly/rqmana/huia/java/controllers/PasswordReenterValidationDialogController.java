package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.event.ActionEvent;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.PasswordValidator;
import ly.rqmana.huia.java.util.Utils;

import java.net.URL;
import java.util.ResourceBundle;

public class PasswordReenterValidationDialogController implements Controllable {
    public JFXPasswordField passwordField;
    public JFXButton cancelBtn;
    public JFXButton okBtn;

    String firstPassword;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        passwordField.getValidators().add(new ValidatorBase() {
            {
                setMessage(Utils.getBundle().getString("PASSWORDS_NOT_MATCH"));
                setIcon(Utils.getErrorIcon());
            }

            @Override
            protected void eval() {
                hasErrors.set(!passwordField.getText().equals(firstPassword));
            }
        });

        passwordField.requestFocus();
    }

    public boolean validate(String firstPassword) {
        this.firstPassword = firstPassword;
        return passwordField.validate();
    }
}
