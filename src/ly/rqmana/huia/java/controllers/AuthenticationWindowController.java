package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import com.sun.xml.internal.ws.message.ByteArrayAttachment;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import ly.rqmana.huia.java.controls.CustomComboBox;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.controls.alerts.Alerts;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.Person;
import ly.rqmana.huia.java.models.Relationship;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;
import ly.rqmana.huia.java.util.fingerprint.FingerprintManager;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthenticationWindowController implements Controllable {

    private final FingerprintManager fingerprintManager = new FingerprintManager();

    private final MainWindowController mainWindowController = Windows.MAIN_WINDOW.getController();

    @FXML public Label nameLabel;
    @FXML public Label fingerprintLabel;
    @FXML public JFXButton fingerprintBtn;

    @FXML public TableView<Subscriber> tableView;
    @FXML public TableColumn<String, Person> nameColumn;
    @FXML public TableColumn<String, Person> workIdColumn;
    @FXML public TableColumn<Gender, Person> genderColumn;
    @FXML public TableColumn<LocalDate, Person> birthdayColumn;
    @FXML public TableColumn<String, Person> fingerprintColumn;
    @FXML public TableColumn relationshipColumn;
    @FXML public TableColumn isActiveColumn;

    @FXML public JFXTextField nameFilterTF;
    @FXML public JFXTextField workIdTF;
    @FXML public JFXDatePicker fromDateFilterDatePicker;
    @FXML public JFXDatePicker toDateFilterDatePicker;
    @FXML public CustomComboBox<String> genderFilterComboBox;
    @FXML public CustomComboBox<String> fingerprintFilterComboBox;
    @FXML public CustomComboBox<Relationship> relationshipFilterComboBox;
    @FXML public CustomComboBox<String> isActiveFilterComboBox;

    private ObjectProperty<Person> lastSelectedPerson = new SimpleObjectProperty<>();

    private FilteredList<Subscriber> filteredList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        new Thread(this::loadDataFromDatabase).start();

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        workIdColumn.setCellValueFactory(new PropertyValueFactory<>("workId"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        fingerprintColumn.setCellValueFactory(new PropertyValueFactory<>("fingerprint"));

        nameColumn.widthProperty().addListener((observable, oldValue, newValue) -> nameFilterTF.setPrefWidth(newValue.doubleValue()));
        workIdColumn.widthProperty().addListener((observable, oldValue, newValue) -> workIdTF.setPrefWidth(newValue.doubleValue()));
        birthdayColumn.widthProperty().addListener((observable, oldValue, newValue) -> {
            fromDateFilterDatePicker.setPrefWidth(newValue.doubleValue()/2);
            toDateFilterDatePicker.setPrefWidth(newValue.doubleValue()/2);
        });
        genderColumn.widthProperty().addListener((observable, oldValue, newValue) -> genderFilterComboBox.setPrefWidth(newValue.doubleValue()));
        relationshipColumn.widthProperty().addListener((observable, oldValue, newValue) -> relationshipFilterComboBox.setPrefWidth(newValue.doubleValue()));
        fingerprintColumn.widthProperty().addListener((observable, oldValue, newValue) -> fingerprintFilterComboBox.setPrefWidth(newValue.doubleValue()));
        isActiveColumn.widthProperty().addListener((observable, oldValue, newValue) -> isActiveFilterComboBox.setPrefWidth(newValue.doubleValue()));

        nameFilterTF.setPrefWidth(nameColumn.getPrefWidth());
        workIdTF.setPrefWidth(workIdColumn.getPrefWidth());
        fromDateFilterDatePicker.setPrefWidth(birthdayColumn.getPrefWidth()/2);
        toDateFilterDatePicker.setPrefWidth(birthdayColumn.getPrefWidth()/2);
        genderFilterComboBox.setPrefWidth(genderColumn.getPrefWidth());
        relationshipFilterComboBox.setPrefWidth(relationshipFilterComboBox.getPrefWidth());
        fingerprintFilterComboBox.setPrefWidth(fingerprintColumn.getPrefWidth());
        isActiveFilterComboBox.setPrefWidth(isActiveFilterComboBox.getPrefWidth());

        genderFilterComboBox.getItems().addAll(Utils.getI18nString("BOTH"), Utils.getI18nString("MALE"), Utils.getI18nString("FEMALE"));
        relationshipFilterComboBox.getItems().addAll(Arrays.asList(Relationship.values()));
        fingerprintFilterComboBox.getItems().addAll(Utils.getI18nString("BOTH"), Utils.getI18nString("FILLED"), Utils.getI18nString("UNFILLED"));
        isActiveFilterComboBox.getItems().addAll(Utils.getI18nString("ACTIVE"), Utils.getI18nString("NOT_ACTIVE"));

        genderFilterComboBox.setValue("Both");
        fingerprintFilterComboBox.setValue("Both");

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (nameLabel != null) {
                lastSelectedPerson.setValue(newValue);
                nameLabel.setText(newValue.getFullName());
                fingerprintBtn.setDisable(false);
            }
        });

        try {
            FingerprintManager.SENSOR.addOnCaptureListener("AUTH_LISTENER", (imageBuffer, template) -> {
                if (mainWindowController.selectedPageProperty.get().equals(MainWindowController.SelectedPage.AUTHENTICATION)) {
                    ExecutorService executorService = Executors.newWorkStealingPool(tableView.getItems().size());
                    for (Subscriber subscriber : tableView.getItems()) {
                        executorService.submit(() -> {

                            int rt = FingerprintManager.MatchFP(subscriber.getRightThumbFingerprint(), template);
                            int ri = FingerprintManager.MatchFP(subscriber.getRightIndexFingerprint(), template);
                            int rm = FingerprintManager.MatchFP(subscriber.getRightMiddleFingerprint(), template);
                            int rr = FingerprintManager.MatchFP(subscriber.getRightRingFingerprint(), template);
                            int rl = FingerprintManager.MatchFP(subscriber.getRightLittleFingerprint(), template);

                            int lt = FingerprintManager.MatchFP(subscriber.getLeftThumbFingerprint(), template);
                            int li = FingerprintManager.MatchFP(subscriber.getLeftIndexFingerprint(), template);
                            int lm = FingerprintManager.MatchFP(subscriber.getLeftMiddleFingerprint(), template);
                            int lr = FingerprintManager.MatchFP(subscriber.getLeftRingFingerprint(), template);
                            int ll = FingerprintManager.MatchFP(subscriber.getRightLittleFingerprint(), template);

                            int matchResult = Math.max(rt, Math.max(ri, Math.max(rm, Math.max(rr, Math.max(rl, Math.max(lt, Math.max(li, Math.max(lm, Math.max(ll, lr)))))))));
                            System.out.println("matchResult = " + matchResult);

                            if (matchResult > 50) {
                                Platform.runLater(() -> Alerts.infoAlert(Windows.MAIN_WINDOW, "Recognition", subscriber.getFullName(), AlertAction.OK));
                            }
                        });
                    }
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void loadDataFromDatabase() {
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList();
        String query;
        try {
            try (Connection connection = DriverManager.getConnection(DAO.getDataDBUrl())) {
                query = "SELECT " +
                        "first_name," +
                        "father_name," +
                        "last_name," +
                        "birthday," +
                        "national_id," +
                        "sex," +
                        "fingerprint_template," +
                        "work_id," +
                        "relationship," +
                        "is_active FROM Fingerprint";
                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery(query);

                    while (resultSet.next()) {
                        Subscriber subscriber = new Subscriber();
                        subscriber.setFirstName(resultSet.getString(1));
                        subscriber.setFatherName(resultSet.getString(2));
                        subscriber.setFamilyName(resultSet.getString(3));

                        subscriber.setBirthday(LocalDate.parse(resultSet.getString(4)));
                        subscriber.setNationalId(resultSet.getString(5));
                        subscriber.setGender("M".equals(resultSet.getString(6)) ? Gender.MALE : Gender.FEMALE);
                        subscriber.setFingerprint(resultSet.getString(7));
                        subscriber.setWorkId(resultSet.getString(8));
                        subscriber.setRelationship(resultSet.getString(9));
                        subscriber.setActive(resultSet.getString(10).equals("True"));

                        subscribers.add(subscriber);
                    }
                }
            }

            query = "SELECT " +
                    "firstName," +
                    "fatherName," +
                    "grandfatherName," +
                    "familyName," +
                    "birthday," +
                    "nationalId," +
                    "gender," +
                    "workId," +
                    "relationship" +
                    " FROM People";

            try (Statement statement = DAO.DB_CONNECTION.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    Subscriber subscriber = new Subscriber();
                    subscriber.setFirstName(resultSet.getString(1));
                    subscriber.setFatherName(resultSet.getString(2));
                    subscriber.setGrandfatherName(resultSet.getString(3));
                    subscriber.setFamilyName(resultSet.getString(4));

                    subscriber.setBirthday(LocalDate.parse(resultSet.getString(5)));
                    subscriber.setNationalId(resultSet.getString(6));
                    subscriber.setGender("M".equals(resultSet.getString(7)) ? Gender.MALE : Gender.FEMALE);
//                subscriber.setFingerprint(resultSet.getString(8));
                    subscriber.setWorkId(resultSet.getString(8));
                    subscriber.setRelationship(resultSet.getString(9));
//                subscriber.setActive(resultSet.getString(15).equals("True"));

                    subscribers.add(subscriber);
                }
            }

            query = "SELECT " +
                    "firstName," +
                    "fatherName," +
                    "grandfatherName," +
                    "familyName," +
                    "birthday," +
                    "nationalId," +
                    "gender," +
                    "workId," +
                    "relationship," +
                    "rightThumbFingerprint," +
                    "rightIndexFingerprint," +
                    "rightMiddleFingerprint," +
                    "rightRingFingerprint," +
                    "rightLittleFingerprint," +
                    "leftThumbFingerprint," +
                    "leftIndexFingerprint," +
                    "leftMiddleFingerprint," +
                    "leftRingFingerprint," +
                    "leftLittleFingerprint" +
                    " FROM NewRegistrations";

            try (Statement statement = DAO.DB_CONNECTION.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    Subscriber subscriber = new Subscriber();
                    subscriber.setFirstName(resultSet.getString("firstName"));
                    subscriber.setFatherName(resultSet.getString("fatherName"));
                    subscriber.setGrandfatherName(resultSet.getString("grandfatherName"));
                    subscriber.setFamilyName(resultSet.getString("familyName"));

                    subscriber.setBirthday(LocalDate.parse(resultSet.getString("birthday")));
                    subscriber.setNationalId(resultSet.getString("nationalId"));
                    subscriber.setGender("M".equals(resultSet.getString("gender")) ? Gender.MALE : Gender.FEMALE);
//                subscriber.setFingerprint(resultSet.getString(8));
                    subscriber.setWorkId(resultSet.getString("workId"));
                    subscriber.setRelationship(resultSet.getString("relationship"));
//                subscriber.setActive(resultSet.getString(15).equals("True"));

                    subscriber.setRightThumbFingerprint(resultSet.getBytes("rightThumbFingerprint"));
                    subscriber.setRightIndexFingerprint(resultSet.getBytes("rightIndexFingerprint"));
                    subscriber.setRightMiddleFingerprint(resultSet.getBytes("rightMiddleFingerprint"));
                    subscriber.setRightRingFingerprint(resultSet.getBytes("rightRingFingerprint"));
                    subscriber.setRightLittleFingerprint(resultSet.getBytes("rightLittleFingerprint"));

                    subscriber.setLeftThumbFingerprint(resultSet.getBytes("leftThumbFingerprint"));
                    subscriber.setLeftIndexFingerprint(resultSet.getBytes("leftIndexFingerprint"));
                    subscriber.setLeftMiddleFingerprint(resultSet.getBytes("leftMiddleFingerprint"));
                    subscriber.setLeftRingFingerprint(resultSet.getBytes("leftRingFingerprint"));
                    subscriber.setLeftLittleFingerprint(resultSet.getBytes("leftLittleFingerprint"));

                    subscribers.add(subscriber);
                }
            }

            addToTableView(subscribers);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addToTableView(ObservableList<Subscriber> subscribers) {
        filteredList = new FilteredList<>(subscribers, subscriber -> true);

        nameFilterTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            if (newValue.isEmpty()) filteredList.setPredicate(subscriber -> true);
            filteredList.setPredicate(person -> {
                for (String newWord : newValue.toLowerCase().split(" ")) {
                    if (person.getFullName().toLowerCase().contains(newWord)) return true;
                }
                return false;
            });
        });

        fromDateFilterDatePicker.valueProperty().addListener((observable, oldValue, newValue) ->
                filteredList.setPredicate(person -> person.getBirthday() == null || newValue == null || person.getBirthday().isAfter(newValue)));

        toDateFilterDatePicker.valueProperty().addListener((observable, oldValue, newValue) ->
                filteredList.setPredicate(person -> person.getBirthday() == null || newValue == null || person.getBirthday().isBefore(newValue)));

        genderFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;
            filteredList.setPredicate(person -> person.getGender() == null || person.getGender().toString().equalsIgnoreCase(newValue));
        });

        fingerprintFilterComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;
            filteredList.setPredicate(person -> {
                if (newValue.equals("Both")) {
                    return true;
                }
                if (newValue.equals("Filled")) {
                    return !person.getFingerprint().isEmpty();
                } else {
                    return person.getFingerprint().isEmpty();
                }
            });
        });

        tableView.setItems(filteredList);
    }

    public void onFingerprintBtnClicked(ActionEvent actionEvent) {

    }
}
