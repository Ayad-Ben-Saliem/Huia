package ly.rqmana.huia.java.security;

import com.jfoenix.controls.JFXDialog;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import ly.rqmana.huia.java.concurrent.Task;
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

    private static final ObjectProperty<User> currentUser = new SimpleObjectProperty<>(new User());

    static {
        RootWindowController rwc = Windows.ROOT_WINDOW.getController();
        try {
            loginDialog = new JFXDialog(rwc.getRootStack(), FXMLLoader.load(Res.Fxml.LOGIN_DIALOG.getUrl(), Utils.getBundle()), JFXDialog.DialogTransition.CENTER, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void authenticate() {
        loginDialog.show();
    }

    public static Task<Boolean> login(String username, String password) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                User user = DAO.getUserByUsername(username);

                if (Hasher.checkPassword(password, user.getPassword())) {
                    currentUser.set(user);
                    DAO.updateUserByUsername(username, "lastLogin", LocalDateTime.now().toString());
                    return true;
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
