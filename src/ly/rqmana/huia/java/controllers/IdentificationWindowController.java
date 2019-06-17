package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.sun.istack.internal.Nullable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.controls.alerts.Alerts;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;
import ly.rqmana.huia.java.fingerprints.hand.Finger;
import ly.rqmana.huia.java.fingerprints.hand.FingerID;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.Person;
import ly.rqmana.huia.java.models.Relationship;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.SQLUtils;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class IdentificationWindowController implements Controllable {

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

    @FXML public VBox searchFieldsContainer;
    @FXML public JFXTextField nameFilterTF;
    @FXML public JFXTextField workIdTF;
    @FXML public JFXDatePicker fromDateFilterDatePicker;
    @FXML public JFXDatePicker toDateFilterDatePicker;
    @FXML public JFXComboBox<String> genderFilterComboBox;
    @FXML public JFXComboBox<String> fingerprintFilterComboBox;
    @FXML public JFXComboBox<Relationship> relationshipFilterComboBox;
    @FXML public JFXComboBox<String> isActiveFilterComboBox;

    private ObjectProperty<Person> lastSelectedPerson = new SimpleObjectProperty<>();

    private FilteredList<Subscriber> filteredList;
    private Predicate<Subscriber> subscriberPredicate;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        new Thread(this::loadDataFromDatabase).start();

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        workIdColumn.setCellValueFactory(new PropertyValueFactory<>("workId"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        fingerprintColumn.setCellValueFactory(new PropertyValueFactory<>("fingerprint"));

        searchFieldsContainer.minWidthProperty().bind(tableView.widthProperty());

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
            }
        });
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
                        subscriber.setAllFingerprintsTemplate(resultSet.getString(7));
                        subscriber.setWorkId(resultSet.getString(8));
                        subscriber.setRelationship(resultSet.getString(9));
                        subscriber.setActive(resultSet.getString(10).equals("True"));

                        subscribers.add(subscriber);
                    }
                }
            }

            //todo: change this to load from People table not NewRegeneration

            query = "SELECT " +
                    "firstName," +
                    "fatherName," +
                    "grandfatherName," +
                    "familyName," +
                    "birthday," +
                    "nationalId," +
                    "familyId," +
                    "gender," +
                    "workId," +
                    "relationship," +
                    "isActive," +
                    "allFingerprintTemplates," +
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
                    " FROM People";

            try (Statement statement = DAO.DB_CONNECTION.createStatement()) {
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    Subscriber subscriber = new Subscriber();

                    subscriber.setFirstName(resultSet.getString("firstName"));
                    subscriber.setFatherName(resultSet.getString("fatherName"));
                    subscriber.setGrandfatherName(resultSet.getString("grandfatherName"));
                    subscriber.setFamilyName(resultSet.getString("familyName"));

                    subscriber.setBirthday(SQLUtils.timestampToDate(resultSet.getLong("birthday")));
                    subscriber.setNationalId(resultSet.getString("nationalId"));
                    subscriber.setGender(Gender.valueOf(resultSet.getString("gender")));

                    subscriber.setWorkId(resultSet.getString("workId"));
                    subscriber.setRelationship(resultSet.getString("relationship"));

                    subscriber.setActive(resultSet.getBoolean("isActive"));

                    subscriber.setAllFingerprintsTemplate(resultSet.getString("allFingerprintTemplates"));
                    subscriber.setRightThumbFingerprint(resultSet.getString("rightThumbFingerprint"));
                    subscriber.setRightIndexFingerprint(resultSet.getString("rightIndexFingerprint"));
                    subscriber.setRightMiddleFingerprint(resultSet.getString("rightMiddleFingerprint"));
                    subscriber.setRightRingFingerprint(resultSet.getString("rightRingFingerprint"));
                    subscriber.setRightLittleFingerprint(resultSet.getString("rightLittleFingerprint"));

                    subscriber.setLeftThumbFingerprint(resultSet.getString("leftThumbFingerprint"));
                    subscriber.setLeftIndexFingerprint(resultSet.getString("leftIndexFingerprint"));
                    subscriber.setLeftMiddleFingerprint(resultSet.getString("leftMiddleFingerprint"));
                    subscriber.setLeftRingFingerprint(resultSet.getString("leftRingFingerprint"));
                    subscriber.setLeftLittleFingerprint(resultSet.getString("leftLittleFingerprint"));

                    subscribers.add(subscriber);
                }
            }

            setToTableView(subscribers);

            subscriberPredicate = subscriber -> {
                String name = nameFilterTF.getText();
                String workId = workIdTF.getText();
                LocalDate fromBirthday = fromDateFilterDatePicker.getValue();
                LocalDate toBirthday = toDateFilterDatePicker.getValue();
//            Gender gender = genderFilterComboBox.getValue();

                if (!name.isEmpty()) {
                    for (String namePart : name.split(" ")) {
                        if (subscriber.getFullName().contains(namePart)) {
                            return true;
                        }
                    }
                }
                if (!workId.isEmpty() && subscriber.getWorkId().contains(workId))
                    return true;
                if (fromBirthday != null && toBirthday != null) {
                    if (subscriber.getBirthday().isAfter(fromBirthday) && subscriber.getBirthday().isBefore(toBirthday))
                        return true;
                }
                System.out.println("here");
                return name.isEmpty() && workId.isEmpty() && fromBirthday == null && toBirthday == null;
            };

            filteredList.setPredicate(subscriberPredicate);

            nameFilterTF.textProperty().addListener(observable -> refreshTable());
            workIdTF.textProperty().addListener(observable -> refreshTable());
            fromDateFilterDatePicker.valueProperty().addListener(observable -> refreshTable());
            toDateFilterDatePicker.valueProperty().addListener(observable -> refreshTable());
            genderFilterComboBox.valueProperty().addListener(observable -> refreshTable());
            fingerprintFilterComboBox.valueProperty().addListener(observable -> refreshTable());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addToTableView(Subscriber subscriber){
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList(filteredList.getSource());
        subscribers.add(subscriber);
        setToTableView(subscribers);
    }

    void setToTableView(ObservableList<Subscriber> subscribers) {
        filteredList = new FilteredList<>(subscribers, subscriberPredicate);

        tableView.setItems(filteredList);
    }

    private void refreshTable() {
        System.out.println("refreshTable");
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList(filteredList.getSource());
        setToTableView(subscribers);
    }

    @FXML
    public void fingerprintButtonAction(ActionEvent actionEvent) {

        ObservableList<Subscriber> searchSubs = tableView.getItems();

        Task<Boolean> openDeviceTask = FingerprintManager.openDeviceIfNotOpen(FingerprintDeviceType.HAMSTER_DX);

        openDeviceTask.addOnSucceeded(event -> {

            Finger scannedFinger = FingerprintManager.device().captureFinger(FingerID.UNKNOWN);

            // if the user cancels capturing
            // an empty finger is returned
            if (scannedFinger.isEmpty())
                return;

            boolean foundMatch = false;

            for (Subscriber subscriber : searchSubs) {

                String subscriberFingerprint = subscriber.getAllFingerprintsTemplate();
                if (subscriberFingerprint != null && ! subscriberFingerprint.isEmpty()){

                    String scannedFingerprint = scannedFinger.getFingerprintTemplate();
                    boolean match = FingerprintManager.device().matchFingerprintCode(scannedFingerprint, subscriberFingerprint);

                    if (match) {
                        foundMatch = true;
                        showIdentificationStateError(true, subscriber);
                        break;
                    }
                }
            }

            if (! foundMatch) {
                showIdentificationStateError(false, null);
            }
        });

        openDeviceTask.addOnFailed(event -> {

            Optional<AlertAction> result = Windows.showFingerprintDeviceError(event.getSource().getException());
            if (result.isPresent() && result.get() == AlertAction.TRY_AGAIN){
                fingerprintButtonAction(null);
            }

        });

        Threading.MAIN_EXECUTOR_SERVICE.submit(openDeviceTask);
    }

    private void showIdentificationStateError(boolean state, @Nullable Subscriber subscriber){

        if (state) {
            System.out.printf("[Found match] %s%n", subscriber.getFullName());

            String heading = Utils.getI18nString("IDENTIFICATION_FOUND_HEADING");
            String body = Utils.getI18nString("IDENTIFICATION_FOUND_BODY").replace("{0}", subscriber.getFullName());

            Alert alert= new Alert(Alert.AlertType.INFORMATION, body, ButtonType.OK);
            alert.setTitle(heading);
            alert.show();
        }
        else {

            String heading = Utils.getI18nString("IDENTIFICATION_NOT_FOUND_HEADING");
            String body = Utils.getI18nString("IDENTIFICATION_NOT_FOUND_BODY");

            Alerts.infoAlert(Windows.MAIN_WINDOW,
                    heading,
                    body,
                    AlertAction.OK);
        }
    }
}
