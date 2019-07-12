package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.sun.istack.internal.Nullable;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.fingerprints.FingerprintCaptureResult;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;
import ly.rqmana.huia.java.fingerprints.hand.Finger;
import ly.rqmana.huia.java.fingerprints.hand.FingerID;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.IdentificationRecord;
import ly.rqmana.huia.java.models.Relationship;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.security.Auth;
import ly.rqmana.huia.java.storage.DataStorage;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;

public class IdentificationWindowController implements Controllable {

    @FXML public Label nameLabel;
    @FXML public Label fingerprintLabel;
    @FXML public JFXButton fingerprintBtn;

    @FXML public TableView<Subscriber> tableView;
    @FXML public TableColumn<String, Subscriber> nameColumn;
    @FXML public TableColumn<String, Subscriber> workIdColumn;
    @FXML public TableColumn<Gender, Subscriber> genderColumn;
    @FXML public TableColumn<LocalDate, Subscriber> birthdayColumn;
    @FXML public TableColumn<String, Subscriber> fingerprintColumn;
    @FXML public TableColumn<String, Subscriber> relationshipColumn;
    @FXML public TableColumn<ImageView, Subscriber> isActiveColumn;

    @FXML public VBox searchFieldsContainer;
    @FXML public JFXTextField nameFilterTF;
    @FXML public JFXTextField workIdFilterTF;
    @FXML public JFXDatePicker fromDateFilterDatePicker;
    @FXML public JFXDatePicker toDateFilterDatePicker;
    @FXML public JFXComboBox<String> genderFilterComboBox;
    @FXML public JFXComboBox<String> fingerprintFilterComboBox;
    @FXML public JFXComboBox<Relationship> relationshipFilterComboBox;
    @FXML public JFXComboBox<String> isActiveFilterComboBox;

    private ObjectProperty<Subscriber> selectedSubscriber = new SimpleObjectProperty<>();

    private final ObservableList<Subscriber> subscribers = FXCollections.observableArrayList();
    private Predicate<Subscriber> subscriberPredicate = getPredicate();
    private FilteredList<Subscriber> filteredList = new FilteredList<>(subscribers, subscriberPredicate);

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        workIdColumn.setCellValueFactory(new PropertyValueFactory<>("workId"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        relationshipColumn.setCellValueFactory(new PropertyValueFactory<>("relationship"));
        fingerprintColumn.setCellValueFactory(new PropertyValueFactory<>("fingerprintsNodes"));
        isActiveColumn.setCellValueFactory(new PropertyValueFactory<>("activeNode"));

        subscribers.addAll(DAO.SUBSCRIBERS);
        DAO.SUBSCRIBERS.addListener((InvalidationListener) change -> {
            subscribers.clear();
            subscribers.addAll(DAO.SUBSCRIBERS);
        });

        nameFilterTF.textProperty().addListener(observable -> refreshTable());
        workIdFilterTF.textProperty().addListener(observable -> refreshTable());
        fromDateFilterDatePicker.valueProperty().addListener(observable -> refreshTable());
        toDateFilterDatePicker.valueProperty().addListener(observable -> refreshTable());
        genderFilterComboBox.valueProperty().addListener(observable -> refreshTable());
        fingerprintFilterComboBox.valueProperty().addListener(observable -> refreshTable());
        isActiveFilterComboBox.valueProperty().addListener(observable -> refreshTable());

        searchFieldsContainer.minWidthProperty().bind(tableView.widthProperty());

        genderFilterComboBox.getItems().addAll(Utils.getI18nString("BOTH"), Utils.getI18nString("MALE"), Utils.getI18nString("FEMALE"));
        relationshipFilterComboBox.getItems().addAll(Arrays.asList(Relationship.values()));
        fingerprintFilterComboBox.getItems().addAll(Utils.getI18nString("BOTH"), Utils.getI18nString("FILLED"), Utils.getI18nString("UNFILLED"));
        isActiveFilterComboBox.getItems().addAll(Utils.getI18nString("BOTH"), Utils.getI18nString("ACTIVE"), Utils.getI18nString("NOT_ACTIVE"));

        genderFilterComboBox.setValue(Utils.getI18nString("BOTH"));
        fingerprintFilterComboBox.setValue(Utils.getI18nString("BOTH"));
        isActiveFilterComboBox.setValue(Utils.getI18nString("BOTH"));

        Platform.runLater(() ->  tableView.setItems(filteredList));

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (nameLabel != null && newValue != null) {
                selectedSubscriber.setValue(newValue);
                nameLabel.setText(newValue.getFullName());
            }
        });

        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Node node = ((Node) event.getTarget()).getParent();
                if (node instanceof TableRow || node.getParent() instanceof TableRow) {
                    onFingerprintBtnClicked(null);
                }
            }
        });

        trackTableColumns();

        //FIXME: fix make these fields available again to the user
//        fromDateFilterDatePicker.setVisible(false);
//        toDateFilterDatePicker.setVisible(false);
//        genderFilterComboBox.setVisible(false);
//        relationshipFilterComboBox.setVisible(false);
//        fingerprintFilterComboBox.setVisible(false);
//        isActiveFilterComboBox.setVisible(false);
    }

    private void trackTableColumns() {
        nameColumn.widthProperty().addListener((observable, oldValue, newValue) -> nameFilterTF.setPrefWidth(newValue.doubleValue()));
        workIdColumn.widthProperty().addListener((observable, oldValue, newValue) -> workIdFilterTF.setPrefWidth(newValue.doubleValue()));
        birthdayColumn.widthProperty().addListener((observable, oldValue, newValue) -> {
            fromDateFilterDatePicker.setPrefWidth(Math.ceil(newValue.doubleValue()/2));
            toDateFilterDatePicker.setPrefWidth(Math.ceil(newValue.doubleValue()/2));
        });
        genderColumn.widthProperty().addListener((observable, oldValue, newValue) -> genderFilterComboBox.setPrefWidth(newValue.doubleValue()));
        relationshipColumn.widthProperty().addListener((observable, oldValue, newValue) -> relationshipColumn.setPrefWidth(newValue.doubleValue()));
        fingerprintColumn.widthProperty().addListener((observable, oldValue, newValue) -> fingerprintFilterComboBox.setPrefWidth(newValue.doubleValue()));
        isActiveColumn.widthProperty().addListener((observable, oldValue, newValue) -> isActiveFilterComboBox.setPrefWidth(newValue.doubleValue()));

        Platform.runLater(() -> {
            nameFilterTF.setPrefWidth(nameColumn.getWidth());
            workIdFilterTF.setPrefWidth(workIdColumn.getWidth());
            fromDateFilterDatePicker.setPrefWidth(Math.ceil(birthdayColumn.getWidth()/2));
            toDateFilterDatePicker.setPrefWidth(Math.ceil(birthdayColumn.getWidth()/2));
            genderFilterComboBox.setPrefWidth(genderColumn.getWidth());
            relationshipFilterComboBox.setPrefWidth(relationshipColumn.getWidth());
            fingerprintFilterComboBox.setPrefWidth(fingerprintColumn.getWidth());
            isActiveFilterComboBox.setPrefWidth(isActiveColumn.getWidth());
        });
    }

    private Predicate<Subscriber> getPredicate() {
        return subscriber -> {
            if (subscriber == null)
                return false;
            String name = nameFilterTF.getText();
            String workId = workIdFilterTF.getText();
            LocalDate fromBirthday = fromDateFilterDatePicker.getValue();
            LocalDate toBirthday = toDateFilterDatePicker.getValue();
            String gender = genderFilterComboBox.getValue();
            String isActive = isActiveFilterComboBox.getValue();
            String hasFingerprint = fingerprintFilterComboBox.getValue();

            // TODO: this should depend on settings
            if (!subscriber.isActive())
                return false;

            if (name.isEmpty() && workId.isEmpty())
                return true;

            boolean isSubscriberMatch = subscriber.getWorkId().startsWith(workId);
            if (! workId.isEmpty()) {
                if (!isSubscriberMatch)
                    return false;
            }

            for (String namePart : name.split(" ")) {
                isSubscriberMatch = subscriber.getFullName().toUpperCase().contains(namePart.toUpperCase());
                if (!isSubscriberMatch)
                    return false;
            }
//            if (fromBirthday != null) {
//                isSubscriberMatch = subscriber.getBirthday().isAfter(fromBirthday);
//            }
//            if (fromBirthday != null) {
//                isSubscriberMatch = subscriber.getBirthday().isBefore(toBirthday);
//            }
//            if (gender != null) {
//                isSubscriberMatch = gender.equals("BOTH") || gender.equals(subscriber.getGender().toString());
//            }
//
//            if (hasFingerprint != null) {
//                isSubscriberMatch =
//                        ( subscriber.hasFingerprint() && hasFingerprint.equals(Utils.getI18nString("FILLED"))) ||
//                        (!subscriber.hasFingerprint() && hasFingerprint.equals(Utils.getI18nString("UNFILLED")));
//            }
//
//            if (isActive != null) {
//                isSubscriberMatch =
//                        ( subscriber.isActive() && isActive.equals(Utils.getI18nString("ACTIVE"))) ||
//                        (!subscriber.isActive() && isActive.equals(Utils.getI18nString("NOT_ACTIVE")));
//            }
            return isSubscriberMatch;
        };
    }

    void addToTableView(Subscriber subscriber) {
        subscribers.add(subscriber);
    }

    private void refreshTable() {
        filteredList = new FilteredList<>(subscribers, subscriberPredicate);
        tableView.setItems(filteredList);
    }

    @FXML public void onFingerprintBtnClicked(ActionEvent actionEvent) {

        Subscriber subscriber = selectedSubscriber.get();
        if (subscriber == null) {
            showSelectSubscriberFirstAlert();
            return;
        }

        IdentificationRecord identificationRecord = new IdentificationRecord();
        identificationRecord.setSubscriber(subscriber);
        identificationRecord.setUser(Auth.getCurrentUser());
        identificationRecord.setDatetime(LocalDateTime.now());
        identificationRecord.setIdentified(false);
        DAO.IDENTIFICATION_RECORDS.add(identificationRecord);

        Task<Long> insertIdentificationRecordTask = DAO.insertIdentificationRecord(identificationRecord);
        insertIdentificationRecordTask.addOnSucceeded(event -> {
            if (!subscriber.isActive()) {
                showSubscriberNotActiveAlert();
                return;
            }

            Task<Boolean> openDeviceTask = FingerprintManager.openDeviceIfNotOpen(FingerprintDeviceType.HAMSTER_DX);
            openDeviceTask.addOnFailed(event1 -> {
                Optional<AlertAction> result = Windows.showFingerprintDeviceError(event1.getSource().getException());
                if (result.isPresent() && result.get() == AlertAction.TRY_AGAIN) {
                    onFingerprintBtnClicked(null);
                }
            });
            if (subscriber.hasFingerprint()) {
                openDeviceTask.addOnSucceeded(event1 -> {
                    Task<Finger> captureFingerTask = new Task<Finger>() {
                        @Override
                        protected Finger call() throws Exception {
                            return FingerprintManager.device().captureFinger(FingerID.UNKNOWN);
                        }
                    };

                    captureFingerTask.addOnSucceeded(event2 -> {
                        Finger scannedFinger = captureFingerTask.getValue();

                        // if the user cancels capturing, null finger will return
                        if (scannedFinger == null || scannedFinger.isEmpty()) {
                            identificationRecord.addNote(Utils.getI18nString("IDENTIFICATION_CANCELLED_ERROR_HEADING"));
                            Task<Boolean> updateSubscriberIdentificationTask = DAO.updateIdentificationRecord(identificationRecord, "notes");
                            updateSubscriberIdentificationTask.addOnFailed(event3 -> showUpdateIdentificationErrorAlert(updateSubscriberIdentificationTask.getException()));
                            Threading.MAIN_EXECUTOR_SERVICE.submit(updateSubscriberIdentificationTask);
                            return;
                        }

                        String subscriberFingerprint = subscriber.getAllFingerprintsTemplate();
                        String scannedFingerprint = scannedFinger.getFingerprintTemplate();
                        Task<Boolean> matchFingerprintsTemplateTask = FingerprintManager.matchFingerprintTemplate(scannedFingerprint, subscriberFingerprint);

                        matchFingerprintsTemplateTask.addOnSucceeded(e -> {
                            boolean match = (Boolean) e.getSource().getValue();
                            identificationRecord.setIdentified(match);
                            if (!match)
                                identificationRecord.addNote(Utils.getI18nString("SUBSCRIBER_NOT_IDENTIFIED"));
                            getIdentificationsRecordsWindowController().refreshTable();

                            Task<Boolean> updateSubscriberIdentificationTask = DAO.updateIdentificationRecord(identificationRecord, "isIdentified", "notes");
                            updateSubscriberIdentificationTask.addOnSucceeded(event3 -> showIdentificationState(match, identificationRecord));
                            updateSubscriberIdentificationTask.addOnFailed(event3 -> showUpdateIdentificationErrorAlert(updateSubscriberIdentificationTask.getException()));

                            Threading.MAIN_EXECUTOR_SERVICE.submit(updateSubscriberIdentificationTask);
                        });

                        matchFingerprintsTemplateTask.addOnFailed(e -> {
                            Throwable t = matchFingerprintsTemplateTask.getException();
                            showFailMatchFingerprintsAlert(t);
                        });
                        Threading.MAIN_EXECUTOR_SERVICE.submit(matchFingerprintsTemplateTask);
                    });
                    captureFingerTask.addOnFailed(event2 -> showFailCaptureFingerprintsAlert(captureFingerTask.getException()));

                    Threading.MAIN_EXECUTOR_SERVICE.submit(captureFingerTask);
                });
                Threading.MAIN_EXECUTOR_SERVICE.submit(openDeviceTask);
            } else if ( Auth.getCurrentUser().isSuperuser() || Auth.getCurrentUser().isStaff() ) {
                Optional<AlertAction> alertAction = showAddMissingFingerprintAlert();
                if (alertAction.isPresent() && AlertAction.YES.equals(alertAction.get())) {
                    // in case we want to register fingerprint for a subscriber
//                    openDeviceTask = FingerprintManager.openDeviceIfNotOpen(FingerprintDeviceType.HAMSTER_DX);
                    openDeviceTask.addOnSucceeded(event1 -> {
                        Task<FingerprintCaptureResult> addFingerprintToSubscriberTask = new Task<FingerprintCaptureResult>() {
                            @Override
                            protected FingerprintCaptureResult call() throws Exception {
                                return FingerprintManager.device().captureHands();
                            }
                        };

                        addFingerprintToSubscriberTask.addOnSucceeded(event3 ->{
                            FingerprintCaptureResult captureResult = addFingerprintToSubscriberTask.getValue();

                            if (captureResult == null || captureResult.isEmpty()) {
                                Windows.warningAlert(
                                        Utils.getI18nString("WARNING"),
                                        Utils.getI18nString("SCAN_FINGERPRINT_WARNING"),
                                        AlertAction.OK);
                            } else {
                                subscriber.fillRightHand(captureResult.getRightHand());
                                subscriber.fillLeftHand(captureResult.getLeftHand());
                                subscriber.setAllFingerprintsTemplate(captureResult.getFingerprintsTemplate());

                                Task<Boolean> updateTask = new Task<Boolean>() {
                                    @Override
                                    protected Boolean call() throws Exception {
                                        String dataPath = DataStorage.saveSubscriberData(subscriber).toString();
                                        subscriber.setDataPath(dataPath);

                                        Map<String, Object> updateMap = new HashMap<>();
                                        updateMap.put("dataPath", dataPath);

                                        updateMap.put("rightThumbFingerprint", subscriber.getRightThumbFingerprint());
                                        updateMap.put("rightIndexFingerprint", subscriber.getRightIndexFingerprint());
                                        updateMap.put("rightMiddleFingerprint", subscriber.getRightMiddleFingerprint());
                                        updateMap.put("rightRingFingerprint", subscriber.getRightRingFingerprint());
                                        updateMap.put("rightLittleFingerprint", subscriber.getRightLittleFingerprint());

                                        updateMap.put("leftThumbFingerprint", subscriber.getLeftThumbFingerprint());
                                        updateMap.put("leftIndexFingerprint", subscriber.getLeftIndexFingerprint());
                                        updateMap.put("leftMiddleFingerprint", subscriber.getLeftMiddleFingerprint());
                                        updateMap.put("leftRingFingerprint", subscriber.getLeftRingFingerprint());
                                        updateMap.put("leftLittleFingerprint", subscriber.getLeftLittleFingerprint());

                                        updateMap.put("allFingerprintTemplates", subscriber.getAllFingerprintsTemplate());
                                        updateMap.put("user", Auth.getCurrentUser().getId());

//                                        System.out.println("currentUserId : " + Auth.getCurrentUser().getId());

//                                        System.out.println("Auth.getCurrentUser().getId() = " + Auth.getCurrentUser().getId());

                                        DAO.updateSubscriberById(subscriber.getId(), updateMap).runAndGet();

                                        tableView.refresh();

                                        return true;
                                    }
                                };

                                updateTask.addOnSucceeded(event4 -> {
                                    showFingerprintRegisterAddedSuccessfully(subscriber);
                                    refreshTable();
                                });

                                updateTask.addOnFailed(event4 -> showFingerprintRegisterErrorAlert(event4.getSource().getException()));

                                updateTask.runningProperty().addListener((observable, oldValue, newValue) -> updateLoadingView(newValue));

                                Threading.MAIN_EXECUTOR_SERVICE.submit(updateTask);
                            }
                        });

                        addFingerprintToSubscriberTask.addOnFailed(event3 -> showFingerprintRegisterErrorAlert(event3.getSource().getException()));

                        Threading.MAIN_EXECUTOR_SERVICE.submit(addFingerprintToSubscriberTask);
                    });
                    Threading.MAIN_EXECUTOR_SERVICE.submit(openDeviceTask);
                }
            }
        });
        insertIdentificationRecordTask.addOnFailed(event -> showInsertIdentificationErrorAlert(insertIdentificationRecordTask.getException()));
        Threading.MAIN_EXECUTOR_SERVICE.submit(insertIdentificationRecordTask);
    }

    private void showIdentificationState(boolean state, @Nullable IdentificationRecord record) {

        String heading;
        String body;
        if (state) {
            heading = Utils.getI18nString("SUBSCRIBER_IDENTIFIED");
            body = Utils.getI18nString("IDENTIFICATION_FOUND_BODY");
            body = body.replace("{0}", record.getSubscriber().getFullName());
            body = body.replace("{1}", Long.toHexString(record.getId()).toUpperCase());
        } else {
            heading = Utils.getI18nString("SUBSCRIBER_NOT_IDENTIFIED");
            body = Utils.getI18nString("IDENTIFICATION_NOT_FOUND_BODY");
        }
        Windows.infoAlert(
                heading,
                body,
                AlertAction.OK);
    }

    private void showFailCaptureFingerprintsAlert(Throwable t) {
        Windows.errorAlert(
                Utils.getI18nString("ERROR"),
                Utils.getI18nString("CAPTURE_FINGERPRINTS_TEMPLATE_FAILED"),
                t,
                AlertAction.OK);
    }

    private void showFailMatchFingerprintsAlert(Throwable t) {
        Windows.errorAlert(
                Utils.getI18nString("ERROR"),
                Utils.getI18nString("MATCH_FINGERPRINTS_TEMPLATE_FAILED"),
                t,
                AlertAction.OK);
    }

    private Optional<AlertAction> showAddMissingFingerprintAlert() {
        return Windows.infoAlert(
                Utils.getI18nString("ADD_MISSING_FINGERPRINTS_HEADING"),
                Utils.getI18nString("ADD_MISSING_FINGERPRINTS_BODY"),
                AlertAction.NO, AlertAction.YES
        );
    }

    private void showSubscriberNotActiveAlert() {
        Windows.warningAlert(
                Utils.getI18nString("NOT_ACTIVE_SUBSCRIBER_WARRING_HEADING"),
                Utils.getI18nString("NOT_ACTIVE_SUBSCRIBER_WARRING_BODY"),
                AlertAction.OK);
    }

    private void showIdentificationProcessCanceledAlert() {
        Windows.errorAlert(
                Utils.getI18nString("IDENTIFICATION_CANCELLED_ERROR_HEADING"),
                Utils.getI18nString("IDENTIFICATION_CANCELLED_ERROR_BODY"),
                null,
                AlertAction.OK
        );
    }

    private void showInsertIdentificationErrorAlert(Throwable t) {
        Windows.errorAlert(
                Utils.getI18nString("ERROR"),
                Utils.getI18nString("INSERT_IDENTIFICATION_ERROR_BODY"),
                t,
                AlertAction.OK
        );
    }

    private void showUpdateIdentificationErrorAlert(Throwable t) {
        Windows.errorAlert(
                Utils.getI18nString("ERROR"),
                Utils.getI18nString("UPDATE_IDENTIFICATION_ERROR_BODY"),
                t,
                AlertAction.OK
        );

    }

    private void showSelectSubscriberFirstAlert() {
        Windows.errorAlert(
                Utils.getI18nString("ERROR"),
                Utils.getI18nString("SELECT_SUBSCRIBER_FIRST"),
                null,
                AlertAction.OK
        );
    }

    private void showFingerprintRegisterErrorAlert(Throwable throwable){

        Windows.errorAlert(
                Utils.getI18nString("FINGERPRINT_REGISTER_FAILED_HEADING"),
                Utils.getI18nString("FINGERPRINT_REGISTER_FAILED_BODY"),
                throwable,
                AlertAction.OK
        );
    }

    private void showFingerprintRegisterAddedSuccessfully(Subscriber subscriber){
         Windows.infoAlert(
                Utils.getI18nString("FINGERPRINT_REGISTER_SUCCESS_HEADING"),
                Utils.getI18nString("FINGERPRINT_REGISTER_SUCCESS_BODY").replace("{0}", subscriber.getFullName()),
                AlertAction.OK
        );
    }

}
