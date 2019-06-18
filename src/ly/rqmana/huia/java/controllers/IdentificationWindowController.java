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
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;
import ly.rqmana.huia.java.fingerprints.hand.Finger;
import ly.rqmana.huia.java.fingerprints.hand.FingerID;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.Relationship;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class IdentificationWindowController implements Controllable {

    @FXML
    public Label nameLabel;
    @FXML
    public Label fingerprintLabel;
    @FXML
    public JFXButton fingerprintBtn;

    @FXML
    public TableView<Subscriber> tableView;
    @FXML
    public TableColumn<String, Subscriber> nameColumn;
    @FXML
    public TableColumn<String, Subscriber> workIdColumn;
    @FXML
    public TableColumn<Gender, Subscriber> genderColumn;
    @FXML
    public TableColumn<LocalDate, Subscriber> birthdayColumn;
    @FXML
    public TableColumn<String, Subscriber> fingerprintColumn;
    @FXML
    public TableColumn<String, Subscriber> relationshipColumn;
    @FXML
    public TableColumn<ImageView, Subscriber> isActiveColumn;

    @FXML
    public VBox searchFieldsContainer;
    @FXML
    public JFXTextField nameFilterTF;
    @FXML
    public JFXTextField workIdTF;
    @FXML
    public JFXDatePicker fromDateFilterDatePicker;
    @FXML
    public JFXDatePicker toDateFilterDatePicker;
    @FXML
    public JFXComboBox<String> genderFilterComboBox;
    @FXML
    public JFXComboBox<String> fingerprintFilterComboBox;
    @FXML
    public JFXComboBox<Relationship> relationshipFilterComboBox;
    @FXML
    public JFXComboBox<String> isActiveFilterComboBox;

    private ObjectProperty<Subscriber> selectedSubscriber = new SimpleObjectProperty<>();

    private FilteredList<Subscriber> filteredList;
    private Predicate<Subscriber> subscriberPredicate;

    @FXML
    private void initialize() {

        Task<Boolean> loadTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                loadDataFromDatabase();
                return true;
            }
        };

//        loadTask.runningProperty().addListener((observable, oldValue, newValue) -> getMainController().updateLoadingView(newValue));

        loadTask.addOnFailed(event -> {
            Throwable ex = event.getSource().getException();
            Windows.errorAlert(Utils.getI18nString("ERROR"),
                                ex.getLocalizedMessage(),
                                ex,
                                AlertAction.OK);
        });

        Threading.MAIN_EXECUTOR_SERVICE.submit(loadTask);

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        workIdColumn.setCellValueFactory(new PropertyValueFactory<>("workId"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        relationshipColumn.setCellValueFactory(new PropertyValueFactory<>("relationship"));
        fingerprintColumn.setCellValueFactory(new PropertyValueFactory<>("fingerprintsNodes"));
        isActiveColumn.setCellValueFactory(new PropertyValueFactory<>("activeNode"));

        searchFieldsContainer.minWidthProperty().bind(tableView.widthProperty());

        genderFilterComboBox.getItems().addAll(Utils.getI18nString("BOTH"), Utils.getI18nString("MALE"), Utils.getI18nString("FEMALE"));
        relationshipFilterComboBox.getItems().addAll(Arrays.asList(Relationship.values()));
        fingerprintFilterComboBox.getItems().addAll(Utils.getI18nString("BOTH"), Utils.getI18nString("FILLED"), Utils.getI18nString("UNFILLED"));
        isActiveFilterComboBox.getItems().addAll(Utils.getI18nString("BOTH"), Utils.getI18nString("ACTIVE"), Utils.getI18nString("NOT_ACTIVE"));

        genderFilterComboBox.setValue(Utils.getI18nString("BOTH"));
        fingerprintFilterComboBox.setValue(Utils.getI18nString("BOTH"));
        isActiveFilterComboBox.setValue(Utils.getI18nString("BOTH"));

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (nameLabel != null && newValue != null) {
                selectedSubscriber.setValue(newValue);
                nameLabel.setText(newValue.getFullName());
            }
        });

        //FIXME: fix make these fields available again to the user
        fromDateFilterDatePicker.setVisible(false);
        toDateFilterDatePicker.setVisible(false);
        genderFilterComboBox.setVisible(false);
        relationshipFilterComboBox.setVisible(false);
        fingerprintFilterComboBox.setVisible(false);
        isActiveFilterComboBox.setVisible(false);

    }

    private void loadDataFromDatabase() throws SQLException {

        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList();

        subscribers.addAll(DAO.getOldSubscribers());

        subscribers.addAll(DAO.getSubscribers());

        subscribers.addAll(DAO.getNewSubscribers());

        setToTableView(subscribers);

        subscriberPredicate = subscriber -> {
            String name = nameFilterTF.getText();
            String workId = workIdTF.getText();
            LocalDate fromBirthday = fromDateFilterDatePicker.getValue();
            LocalDate toBirthday = toDateFilterDatePicker.getValue();
            String gender = genderFilterComboBox.getValue();
            String isActive = isActiveFilterComboBox.getValue();
            String hasFingerprint = fingerprintFilterComboBox.getValue();

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
            if (gender != null) {
                if (gender.equals("BOTH") || gender.equals(subscriber.getGender().toString()))
                    return true;
            }

            if (hasFingerprint != null) {
                if ((subscriber.hasFingerprint() && hasFingerprint.equals(Utils.getI18nString("UNFILLED"))) ||
                        (!subscriber.hasFingerprint() && hasFingerprint.equals(Utils.getI18nString("FILLED"))))
                    return false;
            }

            if (isActive != null) {
                if ((subscriber.isActive() && isActive.equals(Utils.getI18nString("NOT_ACTIVE"))) ||
                        (!subscriber.isActive() && isActive.equals(Utils.getI18nString("ACTIVE"))))
                    return false;
            }
            return name.isEmpty() && workId.isEmpty();
        };

        filteredList.setPredicate(subscriberPredicate);

        nameFilterTF.textProperty().addListener(observable -> refreshTable());
        workIdTF.textProperty().addListener(observable -> refreshTable());
        fromDateFilterDatePicker.valueProperty().addListener(observable -> refreshTable());
        toDateFilterDatePicker.valueProperty().addListener(observable -> refreshTable());
        genderFilterComboBox.valueProperty().addListener(observable -> refreshTable());
        fingerprintFilterComboBox.valueProperty().addListener(observable -> refreshTable());
        isActiveFilterComboBox.valueProperty().addListener(observable -> refreshTable());
}

    public void addToTableView(Subscriber subscriber) {
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList(filteredList.getSource());
        subscribers.add(subscriber);
        setToTableView(subscribers);
    }

    private void setToTableView(ObservableList<Subscriber> subscribers) {
        filteredList = new FilteredList<>(subscribers, subscriberPredicate);

        tableView.setItems(filteredList);
    }

    private void refreshTable() {
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList(filteredList.getSource());
        setToTableView(subscribers);
    }

    @FXML
    public void onFingerprintBtnClicked(ActionEvent actionEvent) {

        Subscriber subscriber = selectedSubscriber.get();
        if (subscriber == null) {
            Windows.errorAlert(
                    Utils.getI18nString("ERROR"),
                    Utils.getI18nString("SELECT_SUBSCRIBER_FIRST"),
                    null,
                    AlertAction.OK
            );
            return;
        }

        Task<Boolean> openDeviceTask = FingerprintManager.openDeviceIfNotOpen(FingerprintDeviceType.HAMSTER_DX);

        openDeviceTask.addOnSucceeded(event -> {

            long identificationId;
            try {
                identificationId = DAO.insertSubscriberIdentification(subscriber);
            } catch (SQLException e) {
                Windows.errorAlert(
                        Utils.getI18nString("ERROR"),
                        Utils.getI18nString("INSERT_IDENTIFICATION_ERROR_BODY"),
                        e,
                        AlertAction.OK
                );
                return;
            }

            Task<Finger> captureFingerTask = new Task<Finger>() {
                @Override
                protected Finger call() throws Exception {
                    return FingerprintManager.device().captureFinger(FingerID.UNKNOWN);
                }
            };

            captureFingerTask.addOnSucceeded(event1 -> {

                Finger scannedFinger = captureFingerTask.getValue();
                // if the user cancels capturing null finger is returned
                if (scannedFinger == null || scannedFinger.isEmpty()) {
                    try {
                        DAO.updateSubscriberIdentification(identificationId, subscriber, false, Utils.getI18nString("SUBSCRIBER_NOT_IDENTIFIED"));
                    } catch (SQLException e) {
                        Windows.errorAlert(
                                Utils.getI18nString("IDENTIFICATION_CANCELLED_ERROR_HEADING"),
                                Utils.getI18nString("IDENTIFICATION_CANCELLED_ERROR_BODY"),
                                e,
                                AlertAction.OK
                        );
                    }
                    return;
                }

                if (!subscriber.isActive()) {
                    Windows.warningAlert(
                            Utils.getI18nString("NOT_ACTIVE_SUBSCRIBER_WARRING_HEADING"),
                            Utils.getI18nString("NOT_ACTIVE_SUBSCRIBER_WARRING_BODY"),
                            AlertAction.OK);
                    return;
                }

                if (!subscriber.hasFingerprint()) {
                    Optional<AlertAction> alertAction = Windows.infoAlert(
                            Utils.getI18nString("ADD_MISSING_FINGERPRINTS_HEADING"),
                            Utils.getI18nString("ADD_MISSING_FINGERPRINTS_BODY"),
                            AlertAction.NO, AlertAction.YES
                    );

                    if (alertAction.isPresent()) {
                        if (alertAction.get().equals(AlertAction.YES)) {
                            // TODO: Add new subscriber depend on current data
                            System.out.println("Yes");
                        }
                    }
                    return;
                }

                Task<Boolean> matchTemplatesTask = new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        String subscriberFingerprint = subscriber.getAllFingerprintsTemplate();
                        String scannedFingerprint = scannedFinger.getFingerprintTemplate();
                        return FingerprintManager.device().matchFingerprintTemplate(scannedFingerprint, subscriberFingerprint);
                    }
                };

                matchTemplatesTask.addOnSucceeded(e -> {

                    boolean match = matchTemplatesTask.getValue();

                    try {
                        DAO.updateSubscriberIdentification(identificationId, subscriber, match, match ? null : Utils.getI18nString("SUBSCRIBER_NOT_IDENTIFIED"));
                    } catch (SQLException ex) {
                        Windows.errorAlert(
                                Utils.getI18nString("ERROR"),
                                Utils.getI18nString("UPDATE_IDENTIFICATION_ERROR_BODY"),
                                ex,
                                AlertAction.OK
                        );
                    }

                    showIdentificationState(match, subscriber, identificationId);
                });

                matchTemplatesTask.addOnFailed(e -> {
                    Windows.errorAlert(Utils.getI18nString("ERROR"),
                            Utils.getI18nString("UPDATE_IDENTIFICATION_ERROR_BODY"),
                            e.getSource().getException(),
                            AlertAction.OK);
                });

                Threading.MAIN_EXECUTOR_SERVICE.submit(matchTemplatesTask);
            });

            captureFingerTask.addOnFailed(event1 -> {
                Windows.errorAlert(Utils.getI18nString("ERROR"),
                        event1.getSource().getException().getLocalizedMessage(),
                        event1.getSource().getException(),
                        AlertAction.OK);
            });

            Threading.MAIN_EXECUTOR_SERVICE.submit(captureFingerTask);
        });

        openDeviceTask.addOnFailed(event -> {
            Optional<AlertAction> result = Windows.showFingerprintDeviceError(event.getSource().getException());
            if (result.isPresent() && result.get() == AlertAction.TRY_AGAIN) {
                onFingerprintBtnClicked(null);
            }
        });

        Threading.MAIN_EXECUTOR_SERVICE.submit(openDeviceTask);
    }

    private void showIdentificationState(boolean state, @Nullable Subscriber subscriber, long identificationId) {

        if (state) {
            String heading = Utils.getI18nString("IDENTIFICATION_FOUND_HEADING");
            String body = Utils.getI18nString("IDENTIFICATION_FOUND_BODY");
            body = body.replace("{0}", subscriber.getFullName());
            body = body.replace("{1}", String.valueOf(identificationId));

            Windows.infoAlert(
                    heading,
                    body,
                    AlertAction.OK);
        } else {

            String heading = Utils.getI18nString("IDENTIFICATION_NOT_FOUND_HEADING");
            String body = Utils.getI18nString("IDENTIFICATION_NOT_FOUND_BODY");

            Windows.infoAlert(
                    heading,
                    body,
                    AlertAction.OK);
        }
    }
}
