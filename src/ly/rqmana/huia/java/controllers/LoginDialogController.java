package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.security.Auth;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginDialogController implements Controllable {
    @FXML VBox formContainer;
    @FXML JFXTextField usernameTF;
    @FXML JFXPasswordField passwordTF;
    @FXML JFXButton loginBtn;
    @FXML JFXButton cancelBtn;
    @FXML Label authFailed;
    @FXML ProgressIndicator progress;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(() -> Auth.getLoginDialog().setOnDialogOpened(event -> usernameTF.requestFocus()));

        cancelBtn.setOnAction(event -> Auth.cancel());
        loginBtn.setOnAction(this::login);

        usernameTF.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
               if (!usernameTF.getText().isEmpty()) {
                   passwordTF.requestFocus();
               }
           }
        });

        passwordTF.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                login(null);
            }
        });
    }

    private void login(ActionEvent event) {
        authFailed.setVisible(false);
        formContainer.setDisable(true);
        progress.setVisible(true);

        Task<Boolean> loginTask = Auth.login(usernameTF.getText(), passwordTF.getText());
        loginTask.setOnSucceeded(e -> {
            Boolean isSuccess = (Boolean) e.getSource().getValue();

            if (isSuccess) {
                Auth.cancel();

                MainWindowController mainWindowController = Windows.MAIN_WINDOW.getController();
                Platform.runLater(() -> mainWindowController.lock(false));
            } else {
                authFailed.setVisible(true);
            }
        });

        loginTask.setOnFailed(e -> e.getSource().getException().printStackTrace());

        loginTask.addOnComplete(event1 -> {
            formContainer.setDisable(false);
            progress.setVisible(false);
        });

        loginTask.start();
    }

}
