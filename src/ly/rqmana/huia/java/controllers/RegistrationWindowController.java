package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.ContactField;
import ly.rqmana.huia.java.controls.CustomComboBox;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.controls.alerts.Alerts;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.fingerprints.FingerprintCaptureResult;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;
import ly.rqmana.huia.java.fingerprints.hand.Hand;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.Institute;
import ly.rqmana.huia.java.models.Relationship;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.security.Auth;
import ly.rqmana.huia.java.storage.DataStorage;
import ly.rqmana.huia.java.util.*;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;

public class RegistrationWindowController implements Controllable {

    @FXML public FlowPane contactsContainer;
    @FXML public JFXTextField firstNameTextField;
    @FXML public JFXTextField fatherNameTextField;
    @FXML public JFXTextField grandfatherNameTextField;
    @FXML public JFXTextField familyNameTextField;
    @FXML public JFXTextField newEmployeeWorkIdTextField;
    @FXML public CustomComboBox<String> employeesWorkIdComboBox;
    @FXML public CustomComboBox<Institute> instituteComboBox;
    @FXML public JFXDatePicker birthdayDatePicker;
    @FXML public JFXTextField nationalityTextField;
    @FXML public JFXTextField nationalIdTextField;
    @FXML public CustomComboBox<Gender> genderComboBox;
    @FXML public CustomComboBox<Relationship> relationshipComboBox;
    private final MainWindowController mainWindowController = Windows.MAIN_WINDOW.getController();

    private FingerprintCaptureResult fingerprintCaptureResult;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        relationshipComboBox.setItems(FXCollections.observableArrayList(Relationship.values()));
        relationshipComboBox.setValue(Relationship.EMPLOYEE);
        relationshipComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            boolean status = newValue.equals(Relationship.EMPLOYEE);
            newEmployeeWorkIdTextField.setVisible(status);
            newEmployeeWorkIdTextField.setManaged(status);
            employeesWorkIdComboBox.setVisible(!status);
            employeesWorkIdComboBox.setManaged(!status);
        });

        loadDatabase();

        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        genderComboBox.getSelectionModel().select(0);

        Utils.setFieldRequired(firstNameTextField);
        Utils.setFieldRequired(fatherNameTextField);
//        Utils.setFieldRequired(grandfatherNameTextField);
        Utils.setFieldRequired(familyNameTextField);

        Utils.setFieldRequired(genderComboBox);
        Utils.setFieldRequired(instituteComboBox);
        Utils.setFieldRequired(birthdayDatePicker);
//        Utils.setFieldRequired(nationalIdTextField);
        Utils.setFieldRequired(nationalityTextField);
        Utils.setFieldRequired(relationshipComboBox);

        newEmployeeWorkIdTextField.setValidators(new RequiredFieldValidator(),
        new ValidatorBase() {
            {
                setMessage(Utils.getBundle().getString("WORK_ID_ALREADY_EXIST"));
                setIcon(Utils.getErrorIcon());
            }
            @Override
            protected void eval() {
                hasErrors.set(employeesWorkIdComboBox.getItems().contains(newEmployeeWorkIdTextField.getText()));
            }
        });

        setupInputFields();
    }

    private void loadDatabase() {
        try {
            loadInstituteNames();
            loadWorkIdes();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadInstituteNames() throws SQLException {
        PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement("SELECT id, name FROM Institutes;");
        ResultSet resultSet = pStatement.executeQuery();
        ObservableList<Institute> institutes = FXCollections.observableArrayList();
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            institutes.add(new Institute(id, name));
        }
        instituteComboBox.setItems(institutes);
    }

    private void loadWorkIdes() throws SQLException {
        ObservableSet<String> workIdes = FXCollections.observableSet();
        String[] queries = {"SELECT workId FROM People;", "SELECT workId FROM NewRegistrations;"};
        for (String query : queries) {
            PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(query);
            ResultSet resultSet = pStatement.executeQuery();
            while (resultSet.next()) {
                workIdes.add(resultSet.getString("workId"));
            }
        }
        employeesWorkIdComboBox.setItems(FXCollections.observableArrayList(workIdes));
    }

    public void onAddContactBtnClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(Res.Fxml.ADD_CONTACTS_METHOD_WINDOW.getUrl(), Utils.getBundle());
            Pane rootPane = loader.load();
            JFXDialog dialog = new JFXDialog(getMainController().getRootStack(), rootPane, JFXDialog.DialogTransition.CENTER, true);
            AddContactMethodDialogController controller = loader.getController();
            controller.setOnSelectListener(type -> {
                ContactField contactField = null;
                switch (type) {
                    case ADDRESS:
                        contactField = new ContactField(ContactField.Type.ADDRESS);
                        contactField.setPromptText(Utils.getI18nString("ADDRESS"));
                        break;
                    case PHONE:
                        contactField = new ContactField(ContactField.Type.PHONE);
                        contactField.setPromptText(Utils.getI18nString("PHONE_NUMBER"));
                        break;
                    case MAILBOX:
                        contactField = new ContactField(ContactField.Type.MAILBOX);
                        contactField.setPromptText(Utils.getI18nString("MAILBOX"));
                        break;
                    case EMAIL:
                        contactField = new ContactField(ContactField.Type.EMAIL);
                        contactField.setPromptText(Utils.getI18nString("EMAIL"));
                        break;
                    case FACEBOOK:
                        contactField = new ContactField(ContactField.Type.FACEBOOK);
                        contactField.setPromptText(Utils.getI18nString("FACEBOOK"));
                        break;
                    case INSTAGRAM:
                        contactField = new ContactField(ContactField.Type.INSTAGRAM);
                        contactField.setPromptText(Utils.getI18nString("INSTAGRAM"));
                        break;
                    case TWITTER:
                        contactField = new ContactField(ContactField.Type.TWITTER);
                        contactField.setPromptText(Utils.getI18nString("TWITTER"));
                        break;
                    case VIBER:
                        contactField = new ContactField(ContactField.Type.VIBER);
                        contactField.setPromptText(Utils.getI18nString("VIBER"));
                        break;
                    case WHATSAPP:
                        contactField = new ContactField(ContactField.Type.WHATSAPP);
                        contactField.setPromptText(Utils.getI18nString("WHATSAPP"));
                        break;
                    case OTHER:
                        contactField = new ContactField(ContactField.Type.OTHER);
                        contactField.setPromptText(Utils.getI18nString("OTHER_CONTACT"));
                        break;
                }
                contactField.setLabelFloat(true);
                contactsContainer.getChildren().add(contactField);
                deleteContactFieldOnCancelBtnFired(contactField);
                dialog.close();
            });
            dialog.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteContactFieldOnCancelBtnFired(ContactField contactField) {
        if (contactField.isCancelable())
            contactField.addCancelBtnEventFilter(ActionEvent.ANY, event -> contactsContainer.getChildren().remove(contactField));
    }

    public void fingerprintButtonAction(ActionEvent actionEvent) {

        Task<Boolean> openDeviceTask = FingerprintManager.openDeviceIfNotOpen(FingerprintDeviceType.HAMSTER_DX);
        openDeviceTask.addOnSucceeded(event -> {
            fingerprintCaptureResult = FingerprintManager.device().captureHands();

        })
            .addOnFailed(event -> {
                fingerprintDeviceError(event.getSource().getException());
        });

        Threading.MAIN_EXECUTOR_SERVICE.submit(openDeviceTask);
    }

    public void onEnterBtnClicked() {
        if (validate()) {
            saveToDB();
        }
    }

    private void setupInputFields() {

        firstNameTextField.setText("");
        fatherNameTextField.setText("");
        grandfatherNameTextField.setText("");
        familyNameTextField.setText("");
        birthdayDatePicker.setValue(null);

        genderComboBox.setValue(Gender.MALE);
        if (instituteComboBox.getValue() == null && instituteComboBox.getItems().size() > 0) {
            instituteComboBox.setValue(instituteComboBox.getItems().get(0));
        }
        if (nationalityTextField.getText() == null || nationalityTextField.getText().isEmpty()) {
            nationalityTextField.setText(Utils.getI18nString("LIBYAN"));
        }
        newEmployeeWorkIdTextField.setText("");

//        contactsContainer.getChildren().remove(3, contactsContainer.getChildren().size());
//        contactsContainer.getChildren().forEach(node -> {
//            ContactField contactField = (ContactField) node;
//            contactField.getInputField().setText("");
//        });
    }

    private boolean validate() {

        boolean validate;
        validate = firstNameTextField.validate();
        validate &= fatherNameTextField.validate();
        validate &= grandfatherNameTextField.validate();
        validate &= familyNameTextField.validate();

        validate &= genderComboBox.validate();
        validate &= instituteComboBox.validate();
        validate &= birthdayDatePicker.validate();
        validate &= nationalIdTextField.validate();
        validate &= nationalityTextField.validate();

        if (relationshipComboBox.getValue().equals(Relationship.EMPLOYEE)) {
            validate &= newEmployeeWorkIdTextField.validate();
        } else {
            validate &= employeesWorkIdComboBox.validate();
        }

        validate &= contactsContainer.getChildren().stream().map(node -> (ContactField) node).map(ContactField::validate).reduce(true, (a, b) -> a && b);

        if (fingerprintCaptureResult == null) {
            Alerts.warningAlert(Windows.MAIN_WINDOW, "WARNING", "Please enter a fingerprint", AlertAction.OK);
            validate = false;
        }

        return validate;
    }

    private void saveToDB() {

        Task<Subscriber> saveTask = new Task<Subscriber>() {
            @Override
            protected Subscriber call() throws Exception {

                Subscriber newSubscriber = constructSubscriber();
                final String INSERT_QUERY =
                        "INSERT INTO NewRegistrations ("
                                + "firstName,"
                                + "fatherName,"
                                + "grandfatherName,"
                                + "familyName,"
                                + "nationality,"
                                + "nationalId,"
                                + "birthday,"
                                + "gender,"
                                + "instituteId,"
                                + "workId,"
                                + "relationship,"
                                + "fingerprintsCode,"
                                + "rightThumbFingerprint,"
                                + "rightIndexFingerprint,"
                                + "rightMiddleFingerprint,"
                                + "rightRingFingerprint,"
                                + "rightLittleFingerprint,"
                                + "leftThumbFingerprint,"
                                + "leftIndexFingerprint,"
                                + "leftMiddleFingerprint,"
                                + "leftRingFingerprint,"
                                + "leftLittleFingerprint,"
                                + "fingerprintImagesDir,"
                                + "user,"
                                + "dateAdded"
                                + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(INSERT_QUERY);

                pStatement.setString(1, firstNameTextField.getText());
                pStatement.setString(2, fatherNameTextField.getText());
                pStatement.setString(3, grandfatherNameTextField.getText());
                pStatement.setString(4, familyNameTextField.getText());
                pStatement.setString(5, nationalityTextField.getText());
                pStatement.setString(6, nationalIdTextField.getText());
                pStatement.setString(7, birthdayDatePicker.getValue().toString());
                pStatement.setString(8, genderComboBox.getValue().toString());
                pStatement.setInt(9, instituteComboBox.getValue().getId());

                String workId = relationshipComboBox.getValue().equals(Relationship.EMPLOYEE) ? newEmployeeWorkIdTextField.getText() : employeesWorkIdComboBox.getValue();
                pStatement.setString(10, workId);
                pStatement.setString(11, relationshipComboBox.getValue().toString());

                if (fingerprintCaptureResult != null) {

                    String fingerprintsCode = fingerprintCaptureResult.getFingerprintsCode();
                    pStatement.setString(12, fingerprintsCode);

                    Hand rightHand = fingerprintCaptureResult.getRightHand();
                    if (rightHand != null) {
                        pStatement.setString(13, rightHand.getThumb().getFingerprintTemplate());
                        pStatement.setString(14, rightHand.getIndex().getFingerprintTemplate());
                        pStatement.setString(15, rightHand.getMiddle().getFingerprintTemplate());
                        pStatement.setString(16, rightHand.getRing().getFingerprintTemplate());
                        pStatement.setString(17, rightHand.getLittle().getFingerprintTemplate());
                    }

                    Hand leftHand = fingerprintCaptureResult.getLeftHand();
                    if (leftHand != null) {
                        pStatement.setString(18, leftHand.getThumb().getFingerprintTemplate());
                        pStatement.setString(19, leftHand.getIndex().getFingerprintTemplate());
                        pStatement.setString(20, leftHand.getMiddle().getFingerprintTemplate());
                        pStatement.setString(21, leftHand.getRing().getFingerprintTemplate());
                        pStatement.setString(22, leftHand.getLittle().getFingerprintTemplate());
                    }
                }
                String imagesDir = DataStorage.saveSubscriberFingerprintImages(newSubscriber).toString();

                pStatement.setString(23, imagesDir);

                pStatement.setString(24, Auth.getCurrentUser().getUsername());
                pStatement.setString(25, LocalDate.now().toString());

                pStatement.executeUpdate();

                setupInputFields();

                return newSubscriber;
            }
        };

        saveTask.addOnSucceeded(event -> {

            Subscriber newSubscriber = saveTask.getValue();
            updateWorkIdes(newSubscriber.getWorkId());
            updateAuthTable(newSubscriber);
        });

        saveTask.addOnFailed(event -> {
            event.getSource().getException().printStackTrace();
            Alerts.errorAlert(Windows.MAIN_WINDOW,
                        "Couldn't Enter",
                          "An error occurred while entering a new subscriber",
                                event.getSource().getException(),
                                AlertAction.OK);
        });

        saveTask.runningProperty().addListener((observable, oldValue, newValue) -> updateLoadingView(newValue));
        Threading.MAIN_EXECUTOR_SERVICE.submit(saveTask);
    }

    private Subscriber constructSubscriber() {
        Subscriber subscriber = new Subscriber();

        subscriber.setFirstName(firstNameTextField.getText());
        subscriber.setFatherName(fatherNameTextField.getText());
        subscriber.setGrandfatherName(grandfatherNameTextField.getText());
        subscriber.setFamilyName(familyNameTextField.getText());
        subscriber.setNationality(nationalityTextField.getText());
        subscriber.setNationalId(nationalIdTextField.getText());
        subscriber.setBirthday(birthdayDatePicker.getValue());
        subscriber.setGender(genderComboBox.getValue());

        subscriber.setFirstName(firstNameTextField.getText());

        String workId = relationshipComboBox.getValue().equals(Relationship.EMPLOYEE)? newEmployeeWorkIdTextField.getText() : employeesWorkIdComboBox.getValue();
        subscriber.setWorkId(workId);
        subscriber.setRelationship(relationshipComboBox.getValue());

        subscriber.setRightHand(fingerprintCaptureResult.getRightHand());
        subscriber.setLeftHand(fingerprintCaptureResult.getLeftHand());
        subscriber.setFingerprintsCode(fingerprintCaptureResult.getFingerprintsCode());

        subscriber.setInstuteId(String.valueOf(instituteComboBox.getValue().getId()));

        return subscriber;
    }

    private void updateAuthTable(Subscriber subscriber) {
        MainWindowController mwc = Windows.MAIN_WINDOW.getController();
        FilteredList<Subscriber> oldFilteredSubscribers = (FilteredList<Subscriber>) mwc.getIdentificationWindowController().tableView.getItems();
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList(oldFilteredSubscribers.getSource());
        subscribers.add(subscriber);
        mwc.getIdentificationWindowController().addToTableView(subscribers);
    }

    private void updateWorkIdes(String newWorkId) {
        ObservableSet<String> workIdes = FXCollections.observableSet();
        workIdes.addAll(employeesWorkIdComboBox.getItems());
        workIdes.add(newWorkId);
        employeesWorkIdComboBox.setItems(FXCollections.observableArrayList(workIdes));
    }

    public void onLogoutBtnClicked() {
        mainWindowController.lock(true);
    }

    private void updateLoadingView(boolean loading){
        StackPane rootStack = getMainController().rootStack;
        rootStack.setDisable(loading);

        if (loading)
            rootStack.setCursor(Cursor.WAIT);
        else
            rootStack.setCursor(Cursor.DEFAULT);
    }


    private void fingerprintDeviceError(Throwable throwable) {

        Optional<AlertAction> result = Windows.showFingerprintDeviceError(throwable);

        if (result.isPresent() && result.get() == AlertAction.TRY_AGAIN) {
                fingerprintButtonAction(null);
        }
    }

    public void onPersonalImageViewClicked(MouseEvent mouseEvent) {
        try {
            Region content = FXMLLoader.load(Res.Fxml.PERSONAL_IMAGE_WINDOW.getUrl(), Utils.getBundle());
            JFXDialog dialog = new JFXDialog(getMainController().getRootStack(), content, JFXDialog.DialogTransition.CENTER);
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
