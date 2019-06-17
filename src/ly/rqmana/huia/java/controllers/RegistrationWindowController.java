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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.stage.StageStyle;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.ContactField;
import ly.rqmana.huia.java.controls.CustomComboBox;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.controls.alerts.Alerts;
import ly.rqmana.huia.java.controls.alerts.LoadingAlert;
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
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class RegistrationWindowController implements Controllable {

    @FXML public FlowPane contactsContainer;
    @FXML public JFXTextField firstNameTextField;
    @FXML public JFXTextField fatherNameTextField;
    @FXML public JFXTextField grandfatherNameTextField;
    @FXML public JFXTextField familyNameTextField;
    @FXML public JFXTextField passportTextField;
    @FXML public JFXTextField familyIdTextField;
    @FXML public JFXTextField residenceTextField;
    @FXML public JFXTextField newEmployeeWorkIdTextField;
    @FXML public CustomComboBox<String> employeesWorkIdComboBox;
    @FXML public CustomComboBox<Institute> instituteComboBox;
    @FXML public JFXDatePicker birthdayDatePicker;
    @FXML public JFXTextField nationalityTextField;
    @FXML public JFXTextField nationalIdTextField;
    @FXML public CustomComboBox<Gender> genderComboBox;
    @FXML public CustomComboBox<Relationship> relationshipComboBox;

    @FXML public ImageView personalPictureIV;

    private final MainWindowController mainWindowController = Windows.MAIN_WINDOW.getController();
    private FingerprintCaptureResult fingerprintCaptureResult;
    private Map<Integer, Image> personalPictures;

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

        personalPictureIV.setClip(new Circle(50, 50, 50));

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

        clearInput();
    }

    @FXML
    private void onPersonalImageViewClicked(MouseEvent mouseEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Res.Fxml.PERSONAL_IMAGE_WINDOW.getUrl(), Utils.getBundle());
            Region content = fxmlLoader.load();

            Windows.LOAD_PERSONAL_PICTURE_DIALOG.setDialogContainer(getMainController().getRootStack());
            Windows.LOAD_PERSONAL_PICTURE_DIALOG.setContent(content);
            Windows.LOAD_PERSONAL_PICTURE_DIALOG.setTransitionType(JFXDialog.DialogTransition.CENTER);

            Windows.LOAD_PERSONAL_PICTURE_DIALOG.setOnDialogClosed(event -> {

                PersonalImageLoaderController personalChooser = fxmlLoader.getController();
                personalPictures = personalChooser.getResult();
                Image personalPicture = personalPictures.get(0);

                personalPictureIV.setImage(personalPicture);
            });

            Windows.LOAD_PERSONAL_PICTURE_DIALOG.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onDeviceRefreshButtonClicked(ActionEvent actionEvent){

        Task<Boolean> openDeviceTask = FingerprintManager.openDevice(FingerprintDeviceType.HAMSTER_DX);

        openDeviceTask.runningProperty().addListener((observable, oldValue, newValue) -> updateLoadingView(newValue));
        openDeviceTask.addOnSucceeded(event -> {
            Alerts.infoAlert(Windows.MAIN_WINDOW,
                    Utils.getI18nString("FINGERPRINT_DEVICE_OPENED_HEADING"),
                    Utils.getI18nString("FINGERPRINT_DEVICE_OPENED_BODY"),
                    AlertAction.OK);
        });

        openDeviceTask.addOnFailed(event -> {
            fingerprintDeviceError(event.getSource().getException(), () -> onDeviceRefreshButtonClicked(actionEvent));
        });

        Threading.MAIN_EXECUTOR_SERVICE.submit(openDeviceTask);
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

    public void onEnterBtnClicked() {
        if (validate()) {
            saveToDB();
        }
    }

    private void clearInput() {

        firstNameTextField.clear();
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
                                + "familyId,"
                                + "residence,"
                                + "passport,"
                                + "workId,"
                                + "relationship,"
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
                                + "allFingerprintTemplates,"
                                + "user"
                                + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

                PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

                pStatement.setString(1, firstNameTextField.getText());
                pStatement.setString(2, fatherNameTextField.getText());
                pStatement.setString(3, grandfatherNameTextField.getText());
                pStatement.setString(4, familyNameTextField.getText());
                pStatement.setString(5, nationalityTextField.getText());
                pStatement.setString(6, nationalIdTextField.getText());
                pStatement.setLong(7, SQLUtils.dateToTimestamp(birthdayDatePicker.getValue()));
                pStatement.setString(8, genderComboBox.getValue().name());
                pStatement.setInt(9, instituteComboBox.getValue().getId());

                pStatement.setString(10, familyIdTextField.getText());
                pStatement.setString(11, residenceTextField.getText());
                pStatement.setString(12, passportTextField.getText());

                String workId = relationshipComboBox.getValue().equals(Relationship.EMPLOYEE) ? newEmployeeWorkIdTextField.getText() : employeesWorkIdComboBox.getValue();
                pStatement.setString(13, workId);
                pStatement.setString(14, relationshipComboBox.getValue().toString());

                if (fingerprintCaptureResult != null) {

                    String fingerprintsCode = fingerprintCaptureResult.getFingerprintsTemplate();
                    pStatement.setString(15, fingerprintsCode);

                    Hand rightHand = fingerprintCaptureResult.getRightHand();
                    if (rightHand != null) {
                        pStatement.setString(16, rightHand.getThumb().getFingerprintTemplate());
                        pStatement.setString(17, rightHand.getIndex().getFingerprintTemplate());
                        pStatement.setString(18, rightHand.getMiddle().getFingerprintTemplate());
                        pStatement.setString(19, rightHand.getRing().getFingerprintTemplate());
                        pStatement.setString(20, rightHand.getLittle().getFingerprintTemplate());
                    }

                    Hand leftHand = fingerprintCaptureResult.getLeftHand();
                    if (leftHand != null) {
                        pStatement.setString(21, leftHand.getThumb().getFingerprintTemplate());
                        pStatement.setString(22, leftHand.getIndex().getFingerprintTemplate());
                        pStatement.setString(23, leftHand.getMiddle().getFingerprintTemplate());
                        pStatement.setString(24, leftHand.getRing().getFingerprintTemplate());
                        pStatement.setString(25, leftHand.getLittle().getFingerprintTemplate());
                    }
                }
                pStatement.setString(26, Auth.getCurrentUser().getUsername());
                pStatement.executeUpdate();

                ResultSet generatedKeys = pStatement.getGeneratedKeys();

                long subscriberId = generatedKeys.getLong(1);
                newSubscriber.setId(subscriberId);

                String imagesDir = DataStorage.saveSubscriberData(newSubscriber).toString();

                pStatement = DAO.DB_CONNECTION.prepareStatement("UPDATE NewRegistrations SET fingerprintImagesDir= ? WHERE id= ?");
                pStatement.setString(1, imagesDir);
                pStatement.setLong(2, subscriberId);

                pStatement.executeUpdate();
                return newSubscriber;
            }
        };

        saveTask.addOnSucceeded(event -> {

            Subscriber newSubscriber = saveTask.getValue();
            updateWorkIdes(newSubscriber.getWorkId());

            MainWindowController mwc = Windows.MAIN_WINDOW.getController();
            mwc.getIdentificationWindowController().addToTableView(newSubscriber);

            String heading = Utils.getI18nString("SUBSCRIBER_ADDED_SUCCESSFULLY_HEADING");
            String body = Utils.getI18nString("SUBSCRIBER_ADDED_SUCCESSFULLY_BODY").replace("{0}", newSubscriber.getFullName());

            clearInput();

            Alerts.infoAlert(Windows.MAIN_WINDOW,
                            heading,
                            body,
                            AlertAction.OK);
        });

        saveTask.addOnFailed(event -> {
            event.getSource().getException().printStackTrace();
            Alerts.errorAlert(Windows.MAIN_WINDOW,
                                Utils.getI18nString("SUBSCRIBER_ADD_ERROR_HEADING"),
                                Utils.getI18nString("SUBSCRIBER_ADD_ERROR_BODY"),
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
        subscriber.setAllFingerprintsTemplate(fingerprintCaptureResult.getFingerprintsTemplate());

        subscriber.setInstitute(instituteComboBox.getValue());

        return subscriber;
    }

    private void updateAuthTable(Subscriber subscriber) {
        MainWindowController mwc = Windows.MAIN_WINDOW.getController();
        FilteredList<Subscriber> oldFilteredSubscribers = (FilteredList<Subscriber>) mwc.getIdentificationWindowController().tableView.getItems();
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList(oldFilteredSubscribers.getSource());
        subscribers.add(subscriber);
        mwc.getIdentificationWindowController().setToTableView(subscribers);
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

    public void fingerprintButtonAction(ActionEvent actionEvent) {

        Task<Boolean> openDeviceTask = FingerprintManager.openDeviceIfNotOpen(FingerprintDeviceType.HAMSTER_DX);
        openDeviceTask.addOnSucceeded(event -> {
            fingerprintCaptureResult = FingerprintManager.device().captureHands();

        });

        openDeviceTask.addOnFailed(event -> {
            fingerprintDeviceError(event.getSource().getException(), ()-> fingerprintButtonAction(actionEvent));
        });

        Threading.MAIN_EXECUTOR_SERVICE.submit(openDeviceTask);
    }

    private final LoadingAlert loadingAlert = new LoadingAlert(null, LoadingAlert.LoadingStyle.SPINNER);
    {
        loadingAlert.setHeadingText(Utils.getI18nString("LOADING_CONNECTING"));
        loadingAlert.setBodyText(Utils.getI18nString("LOADING_WAIT_TEXT"));
        loadingAlert.initStyle(StageStyle.TRANSPARENT);

    }
    private void updateLoadingView(boolean loading){

        if (loading)
            loadingAlert.show();
        else
            loadingAlert.close();

        double width = loadingAlert.getNativeAlert().getWidth();
        double height = loadingAlert.getNativeAlert().getHeight();
        Windows.centerDialog(loadingAlert.getNativeAlert(), width, height);
    }

    private void fingerprintDeviceError(Throwable throwable, Runnable tryAgainBlock) {

        Optional<AlertAction> result = Windows.showFingerprintDeviceError(throwable);

        if (result.isPresent() && result.get() == AlertAction.TRY_AGAIN) {
            tryAgainBlock.run();
        }
    }

}
