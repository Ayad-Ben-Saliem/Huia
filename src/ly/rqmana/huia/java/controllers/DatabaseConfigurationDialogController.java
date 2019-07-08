package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import ly.rqmana.huia.java.storage.BaseInfo;
import ly.rqmana.huia.java.util.Controllable;

import java.net.URL;
import java.util.ResourceBundle;

public class DatabaseConfigurationDialogController implements Controllable {

    @FXML public JFXTextField dbServerHost;
    @FXML public JFXPasswordField dbUsername;
    @FXML public JFXPasswordField dbPassword;

    @FXML public JFXButton okBtn;
    @FXML public JFXButton cancelBtn;

    private JFXDialog dialog;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dbServerHost.setText(BaseInfo.getDbServerHost());
        dbUsername.setText(BaseInfo.getDbUsername());
        dbPassword.setText(BaseInfo.getDbPassword());
    }

    public void setDialog(JFXDialog dialog) {
        this.dialog = dialog;
    }

    public void onOkBtnClicked(ActionEvent actionEvent) {
        BaseInfo.setDbServerHost(dbServerHost.getText());
        BaseInfo.setDbUsername(dbUsername.getText());
        BaseInfo.setDbPassword(dbPassword.getText());

        dialog.close();
    }

    public void onCancelBtnClicked(ActionEvent actionEvent) {
        dialog.close();
    }
}
