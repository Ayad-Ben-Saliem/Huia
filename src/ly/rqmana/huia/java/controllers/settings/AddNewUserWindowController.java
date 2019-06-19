package ly.rqmana.huia.java.controllers.settings;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.CustomComboBox;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.User;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Window;
import ly.rqmana.huia.java.util.Windows;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AddNewUserWindowController implements Controllable {

    public Label title;

    public JFXTextField firstNameTF;
    public JFXTextField fatherNameTF;
    public JFXTextField grandfatherNameTF;
    public JFXTextField familyNameTF;
    public JFXTextField usernameTF;
    public JFXPasswordField passwordTF;
    public JFXTextField emailTF;
    public JFXTextField nationality;
    public JFXTextField nationalId;
    public JFXTextField passportTF;
    public JFXTextField familyIdTF;
    public JFXTextField residenceTF;
    public JFXDatePicker birthdayDP;
    public CustomComboBox<Gender> gender;
    public JFXCheckBox isSuperuser;
    public JFXCheckBox isStaff;
    public JFXCheckBox isActive;

    private boolean editMode = false;
    private User user;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Utils.setFieldRequired(usernameTF);
        Utils.setFieldRequired(passwordTF);
        Utils.setFieldRequired(firstNameTF);

        gender.getItems().addAll(Gender.values());
        gender.setValue(Gender.MALE);
    }

    public void editUser(User user) {
        editMode = true;
        this.user = user;

        title.setText(Utils.getI18nString("EDIT_USER"));

        if (user.getFirstName() != null)
            firstNameTF.setText(user.getFirstName());
        if (user.getFatherName() != null)
            fatherNameTF.setText(user.getFatherName());
        if (user.getGrandfatherName() != null)
            grandfatherNameTF.setText(user.getGrandfatherName());
        if (user.getFamilyName() != null)
            familyNameTF.setText(user.getFamilyName());

        if (user.getUsername() != null)
            usernameTF.setText(user.getUsername());
        if (user.getEmail() != null)
            emailTF.setText(user.getEmail());
        if (user.getNationality() != null)
            nationality.setText(user.getNationality());
        if (user.getNationalId() != null)
            nationalId.setText(user.getNationalId());
        if (user.getPassport() != null)
            passportTF.setText(user.getPassportNumber());
        if (user.getFamilyId() != null)
            familyIdTF.setText(user.getFamilyId());
        if (user.getResidence() != null)
            residenceTF.setText(user.getResidence());
        if (user.getBirthday() != null)
            birthdayDP.setValue(user.getBirthday());
        if (user.getGender() != null)
            gender.setValue(user.getGender());

        isSuperuser.setSelected(user.isSuperuser());
        isStaff.setSelected(user.isStaff());
        isActive.setSelected(user.isActive());
    }

    @FXML
    public void onOkBtnClicked(ActionEvent actionEvent) {
        if (validate()) {
            if (editMode) {
                // Register User

                Task<Void> updateUserTask = DAO.updateUser(user, getUpdateMap());
                updateUserTask.addOnSucceeded(event -> onCancelBtnClicked(null));
                updateUserTask.addOnFailed(event -> {
                    Throwable t = updateUserTask.getException();
                    Windows.errorAlert(
                            Utils.getI18nString("ERROR"),
                            t.getLocalizedMessage(),
                            t,
                            AlertAction.OK
                    );
                });
                Threading.MAIN_EXECUTOR_SERVICE.submit(updateUserTask);
            } else {
                // Register new User

                user = constructUser();
                Task<Void> insertUserTask = DAO.insertUser(user);
                insertUserTask.addOnSucceeded(event -> onCancelBtnClicked(null));
                insertUserTask.addOnFailed(event -> {
                    Throwable t = insertUserTask.getException();
                    Windows.errorAlert(
                            Utils.getI18nString("ERROR"),
                            t.getLocalizedMessage(),
                            t,
                            AlertAction.OK
                    );
                });
                Threading.MAIN_EXECUTOR_SERVICE.submit(insertUserTask);
            }
        } else {
            Windows.errorAlert(
                    Utils.getI18nString("ERROR"),
                    Utils.getI18nString("FILL_REQUIRED_FIELDS_FIRST"),
                    null,
                    AlertAction.OK
            );
        }
    }

    private User constructUser() {
        user = new User();
        user.setUsername(usernameTF.getText());
        user.setPassword(passwordTF.getText());
        user.setFirstName(firstNameTF.getText());
        user.setFatherName(fatherNameTF.getText());
        user.setGrandfatherName(grandfatherNameTF.getText());
        user.setFamilyName(familyNameTF.getText());
        user.setEmail(emailTF.getText());
        user.setNationality(nationality.getText());
        user.setNationalId(nationalId.getText());
        user.setPassportNumber(passportTF.getText());
        user.setFamilyId(familyIdTF.getText());
        user.setResidence(residenceTF.getText());
        user.setBirthday(birthdayDP.getValue());
        user.setGender(gender.getValue());
        user.setSuperuser(isSuperuser.isSelected());
        user.setStaff(isStaff.isSelected());
        user.setActive(isActive.isSelected());
        return user;
    }

    private Map<String, Object> getUpdateMap() {
        Map<String, Object> updateMap = new HashMap<>();

        updateMap.put("username", usernameTF.getText());
        updateMap.put("passwordTF", passwordTF.getText());
        updateMap.put("firstNameTF", firstNameTF.getText());
        updateMap.put("fatherNameTF", fatherNameTF.getText());
        updateMap.put("grandfatherNameTF", grandfatherNameTF.getText());
        updateMap.put("familyNameTF", familyNameTF.getText());
        updateMap.put("emailTF", emailTF.getText());
        updateMap.put("nationality", nationality.getText());
        updateMap.put("nationalId", nationalId.getText());
        updateMap.put("passport", passportTF.getText());
        updateMap.put("familyId", familyIdTF.getText());
        updateMap.put("residence", residenceTF.getText());
        updateMap.put("birthdayDP", birthdayDP.getValue());
        updateMap.put("gender", gender.getValue());
        updateMap.put("isSuperuser", isSuperuser.isSelected());
        updateMap.put("isStaff", isStaff.isSelected());
        updateMap.put("isActive", isActive.isSelected());

        return updateMap;
    }

    private boolean validate() {
        return usernameTF.validate() && passwordTF.validate() && firstNameTF.validate();
    }

    @FXML
    public void onCancelBtnClicked(ActionEvent actionEvent) {
        getUsersSettingsWindowController().addEditUserDialog.close();
    }
}
