package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.ContactField;
import ly.rqmana.huia.java.controls.CustomComboBox;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.fingerprints.FingerprintCaptureResult;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.Institute;
import ly.rqmana.huia.java.models.Relationship;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.storage.DataStorage;
import ly.rqmana.huia.java.util.*;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
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

    public VBox mainContainer;
    private FingerprintCaptureResult fingerprintCaptureResult;
    private Map<Integer, Image> personalPictures;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        relationshipComboBox.setItems(FXCollections.observableArrayList(Relationship.values()));
        relationshipComboBox.setValue(Relationship.SUBSCRIBER);
        relationshipComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            boolean status = newValue.equals(Relationship.SUBSCRIBER);
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

        fatherNameTextField.setOnKeyPressed(event -> {
            if (fatherNameTextField.getText().isEmpty() && event.getCode().equals(KeyCode.BACK_SPACE))
                firstNameTextField.requestFocus();
        });

        grandfatherNameTextField.setOnKeyPressed(event -> {
            if (grandfatherNameTextField.getText().isEmpty() && event.getCode().equals(KeyCode.BACK_SPACE))
                fatherNameTextField.requestFocus();
        });

        familyNameTextField.setOnKeyPressed(event -> {
            if (familyNameTextField.getText().isEmpty() && event.getCode().equals(KeyCode.BACK_SPACE))
                grandfatherNameTextField.requestFocus();
        });

        clearInput();
    }

    @FXML
    private void onPersonalImageViewClicked(MouseEvent mouseEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Res.Fxml.PERSONAL_IMAGE_WINDOW.getUrl(), Utils.getBundle());
            Region content = fxmlLoader.load();

            Windows.LOAD_PERSONAL_PICTURE_DIALOG.setDialogContainer(getRootStack());
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
            Pane content = loader.load();
            JFXDialog dialog = new JFXDialog(getRootStack(), content, JFXDialog.DialogTransition.CENTER, true);
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

        if (relationshipComboBox.getValue().equals(Relationship.SUBSCRIBER)) {
            validate &= newEmployeeWorkIdTextField.validate();
        } else {
            validate &= employeesWorkIdComboBox.validate();
        }

        validate &= contactsContainer.getChildren().stream().map(node -> (ContactField) node).map(ContactField::validate).reduce(true, (a, b) -> a && b);

        if (fingerprintCaptureResult == null || fingerprintCaptureResult.isEmpty()) {
            Windows.warningAlert(
                                Utils.getI18nString("WARNING"),
                                Utils.getI18nString("SCAN_FINGERPRINT_WARNING"),
                                AlertAction.OK);
            validate = false;
        }

        return validate;
    }

    private void saveToDB() {

        Task<Subscriber> saveTask = new Task<Subscriber>() {
            @Override
            protected Subscriber call() throws Exception {

                Subscriber subscriber = constructSubscriber();
                subscriber.setActive(true);

                long subscriberId = DAO.insertNewSubscriber(subscriber);

                subscriber.setId(subscriberId);

                String dataPath = DataStorage.saveSubscriberData(subscriber).toString();
                subscriber.setDataPath(dataPath);

                DAO.updateSubscriberDataPath(subscriber);
                return subscriber;
            }
        };

        saveTask.addOnSucceeded(event -> {

            Subscriber newSubscriber = saveTask.getValue();
            updateWorkIdes(newSubscriber.getWorkId());

            getMainWindowController().getIdentificationWindowController().addToTableView(newSubscriber);

            String heading = Utils.getI18nString("SUBSCRIBER_ADDED_SUCCESSFULLY_HEADING");
            String body = Utils.getI18nString("SUBSCRIBER_ADDED_SUCCESSFULLY_BODY").replace("{0}", newSubscriber.getFullName());

            clearInput();

            Windows.infoAlert(
                            heading,
                            body,
                            AlertAction.OK);
        });

        saveTask.addOnFailed(event -> {
            event.getSource().getException().printStackTrace();
            Windows.errorAlert(
                    Utils.getI18nString("SUBSCRIBER_ADD_ERROR_HEADING"),
                    Utils.getI18nString("SUBSCRIBER_ADD_ERROR_BODY"),
                    event.getSource().getException(),
                    AlertAction.OK
            );
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
        subscriber.setFamilyId(familyIdTextField.getText());
        subscriber.setBirthday(birthdayDatePicker.getValue());
        subscriber.setGender(genderComboBox.getValue());

        String passport = passportTextField.getText().isEmpty()? null : passportTextField.getText();
        subscriber.getPassport().setNumber(passport);
        subscriber.setResidence(residenceTextField.getText());

        String workId = relationshipComboBox.getValue().equals(Relationship.SUBSCRIBER)? newEmployeeWorkIdTextField.getText() : employeesWorkIdComboBox.getValue();
        subscriber.setWorkId(workId);
        subscriber.setRelationship(relationshipComboBox.getValue());

        subscriber.fillRightHand(fingerprintCaptureResult.getRightHand());

        subscriber.fillLeftHand(fingerprintCaptureResult.getLeftHand());

        subscriber.setAllFingerprintsTemplate(fingerprintCaptureResult.getFingerprintsTemplate());

        subscriber.setInstitute(instituteComboBox.getValue());

        return subscriber;
    }

    private void updateWorkIdes(String newWorkId) {
        ObservableSet<String> workIdes = FXCollections.observableSet();
        workIdes.addAll(employeesWorkIdComboBox.getItems());
        workIdes.add(newWorkId);
        employeesWorkIdComboBox.setItems(FXCollections.observableArrayList(workIdes));
    }

    public void onFingerprintBtnClicked(ActionEvent actionEvent) {

        Task<Boolean> openDeviceTask = FingerprintManager.openDeviceIfNotOpen(FingerprintDeviceType.HAMSTER_DX);
        openDeviceTask.addOnSucceeded(event -> {
            if (FingerprintManager.device() != null)
                fingerprintCaptureResult = FingerprintManager.device().captureHands();
        });

        openDeviceTask.addOnFailed(event -> {
            fingerprintDeviceError(event.getSource().getException(), ()-> onFingerprintBtnClicked(actionEvent));
        });

        Threading.MAIN_EXECUTOR_SERVICE.submit(openDeviceTask);
    }
}
