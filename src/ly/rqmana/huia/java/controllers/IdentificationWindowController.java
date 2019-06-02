package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
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
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.CustomComboBox;
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
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;

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
            }
        });

//        ExecutorService executorService = Executors.newWorkStealingPool(tableView.getItems().size());
//        for (Subscriber subscriber : tableView.getItems()) {
//            executorService.submit(() -> {
//
//                int rt = FingerprintManager.MatchFP(subscriber.getRightThumbFingerprint(), template);
//                int ri = FingerprintManager.MatchFP(subscriber.getRightIndexFingerprint(), template);
//                int rm = FingerprintManager.MatchFP(subscriber.getRightMiddleFingerprint(), template);
//                int rr = FingerprintManager.MatchFP(subscriber.getRightRingFingerprint(), template);
//                int rl = FingerprintManager.MatchFP(subscriber.getRightLittleFingerprint(), template);
//
//                int lt = FingerprintManager.MatchFP(subscriber.getLeftThumbFingerprint(), template);
//                int li = FingerprintManager.MatchFP(subscriber.getLeftIndexFingerprint(), template);
//                int lm = FingerprintManager.MatchFP(subscriber.getLeftMiddleFingerprint(), template);
//                int lr = FingerprintManager.MatchFP(subscriber.getLeftRingFingerprint(), template);
//                int ll = FingerprintManager.MatchFP(subscriber.getRightLittleFingerprint(), template);
//
//                int matchResult = Math.max(rt, Math.max(ri, Math.max(rm, Math.max(rr, Math.max(rl, Math.max(lt, Math.max(li, Math.max(lm, Math.max(ll, lr)))))))));
//                System.out.println("matchResult = " + matchResult);
//
//                if (matchResult > 50) {
//                    Platform.runLater(() -> Alerts.infoAlert(Windows.MAIN_WINDOW, "Recognition", subscriber.getFullName(), AlertAction.OK));
//                }
//            });
//        }
    }

    private void loadDataFromDatabase() {
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList();

        String query;
        try {

            //todo: change this to load from People table not NewRegeneration

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
                    "fingerprintsCode," +
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

                    subscriber.setWorkId(resultSet.getString("workId"));
                    subscriber.setRelationship(resultSet.getString("relationship"));

                    subscriber.setFingerprintsCode(resultSet.getString("fingerprintsCode"));
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
                    return !person.getFingerprintsCode().isEmpty();
                } else {
                    return person.getFingerprintsCode().isEmpty();
                }
            });
        });

        tableView.setItems(filteredList);
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

                String subscriberFingerprint = subscriber.getFingerprintsCode();
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
