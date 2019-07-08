package ly.rqmana.huia.java.security;

import com.jfoenix.controls.JFXDialog;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.controllers.LoginDialogController;
import ly.rqmana.huia.java.controllers.MainWindowController;
import ly.rqmana.huia.java.controllers.RootWindowController;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.models.User;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.io.IOException;
import java.time.LocalDateTime;

public class Auth {

    private static JFXDialog loginDialog;
    private static LoginDialogController loginDialogController;

    private static final ObjectProperty<User> currentUser = new SimpleObjectProperty<>(new User());

    static {
        RootWindowController rwc = Windows.ROOT_WINDOW.getController();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Res.Fxml.LOGIN_DIALOG.getUrl(), Utils.getBundle());
            Region content = fxmlLoader.load();
            loginDialogController = fxmlLoader.getController();
            loginDialog = new JFXDialog(rwc.getRootStack(), content, JFXDialog.DialogTransition.CENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentUserProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                RootWindowController rootWindowController = Windows.ROOT_WINDOW.getController();
                MainWindowController mainWindowController = rootWindowController.getMainWindowController();
                if (mainWindowController != null) {
                    Platform.runLater(() -> mainWindowController.setCurrentUserName(newValue.getFullName()));
                }
            }
        });
    }

    public static void authenticate() {
        loginDialogController.clearInputs();
        loginDialog.show();
    }

    public static Task<Boolean> login(String username, String password) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                User user = DAO.getUserByUsername(username);

                if (user != null && Hasher.checkPassword(password, user.getHashedPassword())) {
                    if (user.isActive()) {
                        currentUser.set(user);
                        DAO.updateUserByUsername(username, "lastLogin", LocalDateTime.now().toString()).runAndGet();
                        return true;
                    } else {
                        Platform.runLater(() -> loginDialogController.setAuthFailMsg(Utils.getI18nString("USER_NOT_ACTIVE")));
                    }
                } else {
                    Platform.runLater(() -> loginDialogController.setAuthFailMsg(Utils.getI18nString("AUTH_FAILED")));
                }
                return false;
            }
        };
    }

    public static void cancel() {
        loginDialog.close();
    }

    public static JFXDialog getLoginDialog() {
        return loginDialog;
    }

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static ObjectProperty<User> currentUserProperty() {
        return currentUser;
    }
}
