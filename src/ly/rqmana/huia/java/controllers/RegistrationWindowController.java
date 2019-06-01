package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.base.ValidatorBase;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.util.Pair;
import ly.rqmana.huia.java.controls.ContactField;
import ly.rqmana.huia.java.controls.CustomComboBox;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.controls.alerts.Alerts;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintDeviceNotOpenedException;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;
import ly.rqmana.huia.java.fingerprints.hand.Finger;
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
import java.util.concurrent.ScheduledFuture;

public class RegistrationWindowController implements Controllable {

    @FXML
    public FlowPane contactsContainer;
    @FXML
    public JFXTextField firstNameTextField;
    @FXML
    public JFXTextField fatherNameTextField;
    @FXML
    public JFXTextField grandfatherNameTextField;
    @FXML
    public JFXTextField familyNameTextField;
    @FXML
    public JFXTextField newEmployeeWorkIdTextField;
    @FXML
    public CustomComboBox<String> employeesWorkIdComboBox;
    @FXML
    public CustomComboBox<Institute> instituteComboBox;
    @FXML
    public JFXDatePicker birthdayDatePicker;
    @FXML
    public JFXTextField nationalityTextField;
    @FXML
    public JFXTextField nationalIdTextField;
    @FXML
    public CustomComboBox<Gender> genderComboBox;
    @FXML
    public CustomComboBox<Relationship> relationshipComboBox;

    private ScheduledFuture<?> scheduledFuture;

    private final MainWindowController mainWindowController = Windows.MAIN_WINDOW.getController();

    private Hand rightHand;
    private Hand leftHand;

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

    public void onEnterBtnClicked() {
        if (validate()) {
            upload();
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

        if (rightHand == null && leftHand == null) {
//            TODO: print validation message
            validate = false;
        }

        return validate;
    }

    private void upload() {
        new Thread(() -> {
            try {
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
                                + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

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

                String workId = relationshipComboBox.getValue().equals(Relationship.EMPLOYEE)? newEmployeeWorkIdTextField.getText() : employeesWorkIdComboBox.getValue();
                pStatement.setString(10, workId);
                pStatement.setString(11, relationshipComboBox.getValue().toString());

                if (rightHand != null) {
                    pStatement.setString(12, rightHand.getThumbFinger().getFingerprintTemplate());
                    pStatement.setString(13, rightHand.getIndexFinger().getFingerprintTemplate());
                    pStatement.setString(14, rightHand.getMiddleFinger().getFingerprintTemplate());
                    pStatement.setString(15, rightHand.getRingFinger().getFingerprintTemplate());
                    pStatement.setString(16, rightHand.getLittleFinger().getFingerprintTemplate());
                }

                if (leftHand != null) {
                    pStatement.setString(17, leftHand.getThumbFinger().getFingerprintTemplate());
                    pStatement.setString(18, leftHand.getIndexFinger().getFingerprintTemplate());
                    pStatement.setString(19, leftHand.getMiddleFinger().getFingerprintTemplate());
                    pStatement.setString(20, leftHand.getRingFinger().getFingerprintTemplate());
                    pStatement.setString(21, leftHand.getLittleFinger().getFingerprintTemplate());
                }

                String institute = instituteComboBox.getValue().getName();
                String fullName = firstNameTextField.getText() + " " + fatherNameTextField.getText();
                String gfn = grandfatherNameTextField.getText();
                fullName += (gfn == null || gfn.isEmpty())? "" : gfn;
                fullName += " " + familyNameTextField.getText();

                String imagesDir = DataStorage.getNewRegFingerprintDir(institute, fullName, workId);
                System.out.println(imagesDir);
                DataStorage.saveNewFingerprintImages(institute, fullName,workId, rightHand, leftHand);
                pStatement.setString(22, imagesDir);

                pStatement.setString(23, Auth.getCurrentUser().getUsername());
                pStatement.setString(24, LocalDate.now().toString());

                pStatement.executeUpdate();

                Platform.runLater(() -> updateWorkIdes(workId));

                updateAuthTable(constructSubscriber());

                setupInputFields();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, "UPLOAD-THREAD").start();
    }

    private void setTemplateToStatement(PreparedStatement statement, int columnIndex, Finger finger) {
        try {
            if (statement.isClosed()) return;
            if (finger == null || finger.getFingerprintTemplate() == null) {
//                statement.setString(columnIndex, "");
            } else {
                statement.setString(columnIndex, finger.getFingerprintTemplate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        if (rightHand != null) {
            if (rightHand.getThumbFinger() != null)
                subscriber.setRightThumbFingerprint(rightHand.getThumbFinger().getFingerprintTemplate());
            if (rightHand.getIndexFinger() != null)
                subscriber.setRightIndexFingerprint(rightHand.getIndexFinger().getFingerprintTemplate());
            if (rightHand.getMiddleFinger() != null)
                subscriber.setRightMiddleFingerprint(rightHand.getMiddleFinger().getFingerprintTemplate());
            if (rightHand.getRingFinger() != null)
                subscriber.setRightRingFingerprint(rightHand.getRingFinger().getFingerprintTemplate());
            if (rightHand.getLittleFinger() != null)
                subscriber.setRightLittleFingerprint(rightHand.getLittleFinger().getFingerprintTemplate());
        }

        if (leftHand != null) {
            if (leftHand.getThumbFinger() != null)
                subscriber.setLeftThumbFingerprint(leftHand.getThumbFinger().getFingerprintTemplate());
            if (leftHand.getIndexFinger() != null)
                subscriber.setLeftIndexFingerprint(leftHand.getIndexFinger().getFingerprintTemplate());
            if (leftHand.getMiddleFinger() != null)
                subscriber.setLeftMiddleFingerprint(leftHand.getMiddleFinger().getFingerprintTemplate());
            if (leftHand.getRingFinger() != null)
                subscriber.setLeftRingFingerprint(leftHand.getRingFinger().getFingerprintTemplate());
            if (leftHand.getLittleFinger() != null)
                subscriber.setLeftLittleFingerprint(leftHand.getLittleFinger().getFingerprintTemplate());
        }

        return subscriber;
    }

    private void updateAuthTable(Subscriber subscriber) {
        MainWindowController mwc = Windows.MAIN_WINDOW.getController();
        FilteredList<Subscriber> oldFilteredSubscribers = (FilteredList<Subscriber>) mwc.getAuthenticationWindowController().tableView.getItems();
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList(oldFilteredSubscribers.getSource());
        subscribers.add(subscriber);
        mwc.getAuthenticationWindowController().addToTableView(subscribers);
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

    public void onFingerprintBtnClicked(ActionEvent actionEvent) {
        if (!FingerprintManager.isDeviceOpen()) {
            FingerprintManager.openDevice(FingerprintDeviceType.HAMSTER_DX).addOnSucceeded(event -> {
                try {
                    Pair<Hand, Hand> handHandPair = FingerprintManager.device().captureHands();
                    rightHand = handHandPair.getKey();
                    leftHand = handHandPair.getValue();
                } catch (Exception ex) {
                    fingerprintDeviceError(ex);
                }
            }).addOnFailed(event -> {
                fingerprintDeviceError(event.getSource().getException());
            }).start();
        }

//        catch (IOException ex) {
//            Alerts.errorAlert(
//                    Windows.MAIN_WINDOW,
//                    Utils.getI18nString("ERROR"),
//                    ex.getMessage(),
//                    ex,
//                    AlertAction.CANCEL);
//        }
    }

    private void fingerprintDeviceError(Throwable throwable) {
        if (throwable instanceof FingerprintDeviceNotOpenedException) {
            Optional<AlertAction> alertAction = Alerts.errorAlert(
                    Windows.MAIN_WINDOW,
                    Utils.getI18nString("ERROR"),
                    Utils.getI18nString("DEVICE_NOT_OPENED_ERROR_BODY"),
                    throwable,
                    AlertAction.CANCEL, AlertAction.TRY_AGAIN);

            if (alertAction.isPresent() && alertAction.get().equals(AlertAction.TRY_AGAIN)) {
                onFingerprintBtnClicked(null);
            }
        } else
        if (throwable instanceof UnsatisfiedLinkError) {
            Optional<AlertAction> alertAction = Alerts.errorAlert(
                    Windows.MAIN_WINDOW,
                    Utils.getI18nString("ERROR"),
                    Utils.getI18nString("LIBRARY_NOT_EXIST"),
                    throwable,
                    AlertAction.CANCEL, AlertAction.TRY_AGAIN);

            if (alertAction.isPresent() && alertAction.get().equals(AlertAction.TRY_AGAIN)) {
                onFingerprintBtnClicked(null);
            }
        }
    }
}
