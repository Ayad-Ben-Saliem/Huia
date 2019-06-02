package ly.rqmana.huia.java.security;

import com.jfoenix.controls.JFXDialog;
import javafx.fxml.FXMLLoader;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.controllers.MainWindowController;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.User;
import ly.rqmana.huia.java.storage.DataStorage;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Auth {

    private static JFXDialog loginDialog;

    private static User currentUser;

    static {
        MainWindowController mwc = Windows.MAIN_WINDOW.getController();
        try {
            loginDialog = new JFXDialog(mwc.getRootStack(), FXMLLoader.load(Res.Fxml.LOGIN_DIALOG.getUrl(), Utils.getBundle()), JFXDialog.DialogTransition.CENTER, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentUser = new User();
    }

    public static void authenticate() {
        loginDialog.show();
    }

    public static Task<Boolean> login(String username, String password) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                String query = "SELECT   " +
                        "username       ," +
                        "password       ," +
                        "email          ," +
                        "firstName      ," +
                        "fatherName     ," +
                        "grandfatherName," +
                        "familyName     ," +
                        "nationality    ," +
                        "nationalId     ," +
                        "birthday       ," +
                        "gender         ," +
                        "dateJoined     ," +
                        "isSuperuser    ," +
                        "lastLogin       " +
                        "FROM Users WHERE username=? COLLATE NOCASE";


                PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(query);
                pStatement.setString(1, username);
                ResultSet resultSet = pStatement.executeQuery();

                String hashPassword = resultSet.getString("password");
                if (Hasher.checkPassword(password, hashPassword)) {
                    currentUser.setUsername       (resultSet.getString("username"));
                    currentUser.setPassword       (resultSet.getString("password"));
                    currentUser.setEmail          (resultSet.getString("email"));
                    currentUser.setFirstName      (resultSet.getString("firstName"));
                    currentUser.setFatherName     (resultSet.getString("fatherName"));
                    currentUser.setGrandfatherName(resultSet.getString("grandfatherName"));
                    currentUser.setFamilyName     (resultSet.getString("grandfatherName"));
                    currentUser.setNationality    (resultSet.getString("nationality"));
                    currentUser.setNationalId     (resultSet.getString("nationalId"));

                    String birthday = resultSet.getString("birthday");
                    if (birthday != null)
                        currentUser.setBirthday(LocalDate.parse(birthday));

                    String gender = resultSet.getString("gender");
                    if (gender != null)
                        currentUser.setGender("Male".equals(gender)? Gender.MALE : Gender.FEMALE);

                    String dateJoined = resultSet.getString("dateJoined");
                    if (dateJoined != null)
                        currentUser.setDateJoined(LocalDateTime.parse(dateJoined, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSz")));

                    currentUser.setSuperuser("True".equals(resultSet.getString("isSuperuser")));


                    String lastLogin = resultSet.getString("lastLogin");
                    if (lastLogin != null)
                        currentUser.setLastLogin(LocalDateTime.parse(lastLogin, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSz")));

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
        return currentUser;
    }
}
