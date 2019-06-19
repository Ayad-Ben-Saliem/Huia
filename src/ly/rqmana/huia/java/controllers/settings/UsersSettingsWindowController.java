package ly.rqmana.huia.java.controllers.settings;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Region;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.models.User;
import ly.rqmana.huia.java.security.Auth;
import ly.rqmana.huia.java.util.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.ResourceBundle;

public class UsersSettingsWindowController implements Controllable {

    public JFXButton editBtn;
    public TableView<User> tableView;
    public TableColumn<String, User> usernameColumn;
    public TableColumn<String, User> nameColumn;
    public TableColumn<String, User> emailColumn;
    public TableColumn<String, User> userTypeColumn;
    public TableColumn<LocalDateTime, User> lastLoginColumn;

    public JFXDialog addEditUserDialog;

    private ObjectProperty<User> selectedUser = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        lastLoginColumn.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));
        userTypeColumn.setCellValueFactory(new PropertyValueFactory<>("userType"));

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldUser, newUser) -> {
            selectedUser.set(newUser);
        });

        selectedUser.addListener((observable, oldValue, newValue) -> editBtn.setDisable(newValue == null));

        Task<Collection<User>> getAllUsersTask = DAO.getAllUsers();

        getAllUsersTask.addOnSucceeded(event -> tableView.getItems().addAll(getAllUsersTask.getValue()));

        getAllUsersTask.addOnFailed(event -> {
            Throwable t = getAllUsersTask.getException();
            Windows.errorAlert(
                    Utils.getI18nString("ERROR"),
                    t.getLocalizedMessage(),
                    t,
                    AlertAction.OK
            );
        });

        Threading.MAIN_EXECUTOR_SERVICE.submit(getAllUsersTask);
    }

    public void onAddNewUserBtnClicked(ActionEvent actionEvent) throws IOException {
        Region content = FXMLLoader.load(Res.Fxml.ADD_NEW_USER_WINDOW.getUrl(), Utils.getBundle());
        addEditUserDialog = new JFXDialog(getRootStack(), content, JFXDialog.DialogTransition.CENTER);
        addEditUserDialog.show();
    }

    public void onEditUserBtnClicked(ActionEvent actionEvent) throws IOException {
        if (Auth.getCurrentUser().isSuperuser()) {
            FXMLLoader fxmlLoader = new FXMLLoader(Res.Fxml.ADD_NEW_USER_WINDOW.getUrl(), Utils.getBundle());
            Region content = fxmlLoader.load();
            AddNewUserWindowController controller = fxmlLoader.getController();
            controller.editUser(selectedUser.get());
            addEditUserDialog = new JFXDialog(getRootStack(), content, JFXDialog.DialogTransition.CENTER);
            addEditUserDialog.show();
        } else {
            Windows.infoAlert(
                    Utils.getI18nString("SUPERUSER_REQUIRED_HEADING"),
                    Utils.getI18nString("SUPERUSER_REQUIRED_BODY"),
                    AlertAction.OK
            );
        }
    }
}
