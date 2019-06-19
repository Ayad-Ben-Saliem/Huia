package ly.rqmana.huia.java.controllers.settings;

import com.jfoenix.controls.*;
import javafx.event.ActionEvent;
import ly.rqmana.huia.java.controls.CustomComboBox;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;

import java.net.URL;
import java.util.ResourceBundle;

public class AddNewUserWindowController implements Controllable {
    public JFXTextField firstNameTF;
    public JFXTextField fatherNameTF;
    public JFXTextField grandfatherNameTF;
    public JFXTextField familyNameTF;
    public JFXTextField usernameTF;
    public JFXPasswordField passwordTF;
    public JFXTextField emailTF;
    public JFXTextField nationality;
    public JFXTextField nationalId;
    public JFXDatePicker birthdayDP;
    public CustomComboBox gender;
    public JFXCheckBox isSuperuser;
    public JFXCheckBox isStuff;
    public JFXCheckBox isActive;

    public JFXButton cancelBtn;
    public JFXButton okBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utils.setFieldRequired(usernameTF);
        Utils.setFieldRequired(passwordTF);
        Utils.setFieldRequired(firstNameTF);
    }
}
