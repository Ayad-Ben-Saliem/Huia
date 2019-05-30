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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.ContactField;
import ly.rqmana.huia.java.controls.CustomComboBox;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.Institute;
import ly.rqmana.huia.java.models.Relationship;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.security.Auth;
import ly.rqmana.huia.java.storage.DataStorage;
import ly.rqmana.huia.java.util.*;
import ly.rqmana.huia.java.util.fingerprint.Finger;
import ly.rqmana.huia.java.util.fingerprint.FingerprintManager;
import ly.rqmana.huia.java.util.fingerprint.FingerprintSensor;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

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
    public ImageView rightHandImageView;
    @FXML
    public ImageView leftHandImageView;
    @FXML
    public StackPane rightHandImageContainer;
    @FXML
    public StackPane leftHandImageContainer;
    @FXML
    public CustomComboBox<Gender> genderComboBox;
    @FXML
    public CustomComboBox<Relationship> relationshipComboBox;

    @FXML
    public ImageView rightThumbFingerImageView;
    @FXML
    public ImageView rightThumbFingerTrueImageView;
    @FXML
    public ImageView rightIndexFingerImageView;
    @FXML
    public ImageView rightIndexFingerTrueImageView;
    @FXML
    public ImageView rightMiddleFingerImageView;
    @FXML
    public ImageView rightMiddleFingerTrueImageView;
    @FXML
    public ImageView rightRingFingerImageView;
    @FXML
    public ImageView rightRingFingerTrueImageView;
    @FXML
    public ImageView rightLittleFingerImageView;
    @FXML
    public ImageView rightLittleFingerTrueImageView;

    @FXML
    public ImageView leftThumbFingerImageView;
    @FXML
    public ImageView leftThumbFingerTrueImageView;
    @FXML
    public ImageView leftIndexFingerImageView;
    @FXML
    public ImageView leftIndexFingerTrueImageView;
    @FXML
    public ImageView leftMiddleFingerImageView;
    @FXML
    public ImageView leftMiddleFingerTrueImageView;
    @FXML
    public ImageView leftRingFingerImageView;
    @FXML
    public ImageView leftRingFingerTrueImageView;
    @FXML
    public ImageView leftLittleFingerImageView;
    @FXML
    public ImageView leftLittleFingerTrueImageView;

    @FXML
    public Label fingerprintNoteLabel;

    private final ContextMenu menu = new ContextMenu();
    private final MenuItem thumbMenuItem = new MenuItem(Utils.getI18nString("THUMB_FINGER"));
    private final MenuItem indexMenuItem = new MenuItem(Utils.getI18nString("INDEX_FINGER"));
    private final MenuItem middleMenuItem = new MenuItem(Utils.getI18nString("MIDDLE_FINGER"));
    private final MenuItem ringMenuItem = new MenuItem(Utils.getI18nString("RING_FINGER"));
    private final MenuItem littleMenuItem = new MenuItem(Utils.getI18nString("LITTLE_FINGER"));

    private final FingerprintManager fingerprintManager = new FingerprintManager();
    private FingerprintSensor.OnCaptureListener onCaptureListener;
    private ScheduledFuture<?> scheduledFuture;

    private final MainWindowController mainWindowController = Windows.MAIN_WINDOW.getController();

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

        menu.getItems().addAll(thumbMenuItem, indexMenuItem, middleMenuItem, ringMenuItem, littleMenuItem);

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

        rightHandImageContainer.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onRightHandImageViewClicked);
        leftHandImageContainer.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onLeftHandImageViewClicked);

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

                setTemplateToStatement(pStatement, 12, fingerprintManager.getRightHand().getThumbFinger());
                setTemplateToStatement(pStatement, 13, fingerprintManager.getRightHand().getIndexFinger());
                setTemplateToStatement(pStatement, 14, fingerprintManager.getRightHand().getMiddleFinger());
                setTemplateToStatement(pStatement, 15, fingerprintManager.getRightHand().getRingFinger());
                setTemplateToStatement(pStatement, 16, fingerprintManager.getRightHand().getLittleFinger());

                setTemplateToStatement(pStatement, 17, fingerprintManager.getLeftHand().getThumbFinger());
                setTemplateToStatement(pStatement, 18, fingerprintManager.getLeftHand().getIndexFinger());
                setTemplateToStatement(pStatement, 19, fingerprintManager.getLeftHand().getMiddleFinger());
                setTemplateToStatement(pStatement, 20, fingerprintManager.getLeftHand().getRingFinger());
                setTemplateToStatement(pStatement, 21, fingerprintManager.getLeftHand().getLittleFinger());

                String institute = instituteComboBox.getValue().getName();
                String fullName = firstNameTextField.getText() + " " + fatherNameTextField.getText();
                String gfn = grandfatherNameTextField.getText();
                fullName += (gfn == null || gfn.isEmpty())? "" : gfn;
                fullName += " " + familyNameTextField.getText();

                String imagesDir = DataStorage.getNewRegFingerprintDir(institute, fullName, workId);
                System.out.println(imagesDir);
                DataStorage.saveNewFingerprintImages(institute, fullName,workId, fingerprintManager.getRightHand(), fingerprintManager.getLeftHand());
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
                statement.setBytes(columnIndex, new byte[]{});
            } else {
                statement.setBytes(columnIndex, finger.getFingerprintTemplate());
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

        if (fingerprintManager.getRightHand().getThumbFinger() != null)
            subscriber.setRightThumbFingerprint(fingerprintManager.getRightHand().getThumbFinger().getFingerprintTemplate());
        if (fingerprintManager.getRightHand().getIndexFinger() != null)
            subscriber.setRightIndexFingerprint(fingerprintManager.getRightHand().getIndexFinger().getFingerprintTemplate());
        if (fingerprintManager.getRightHand().getMiddleFinger() != null)
            subscriber.setRightMiddleFingerprint(fingerprintManager.getRightHand().getMiddleFinger().getFingerprintTemplate());
        if (fingerprintManager.getRightHand().getRingFinger() != null)
            subscriber.setRightRingFingerprint(fingerprintManager.getRightHand().getRingFinger().getFingerprintTemplate());
        if (fingerprintManager.getRightHand().getLittleFinger() != null)
            subscriber.setRightLittleFingerprint(fingerprintManager.getRightHand().getLittleFinger().getFingerprintTemplate());

        if (fingerprintManager.getLeftHand().getThumbFinger() != null)
            subscriber.setLeftThumbFingerprint(fingerprintManager.getLeftHand().getThumbFinger().getFingerprintTemplate());
        if (fingerprintManager.getLeftHand().getIndexFinger() != null)
            subscriber.setLeftIndexFingerprint(fingerprintManager.getLeftHand().getIndexFinger().getFingerprintTemplate());
        if (fingerprintManager.getLeftHand().getMiddleFinger() != null)
            subscriber.setLeftMiddleFingerprint(fingerprintManager.getLeftHand().getMiddleFinger().getFingerprintTemplate());
        if (fingerprintManager.getLeftHand().getRingFinger() != null)
            subscriber.setLeftRingFingerprint(fingerprintManager.getLeftHand().getRingFinger().getFingerprintTemplate());
        if (fingerprintManager.getLeftHand().getLittleFinger() != null)
            subscriber.setLeftLittleFingerprint(fingerprintManager.getLeftHand().getLittleFinger().getFingerprintTemplate());

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

    private void onRightHandImageViewClicked(MouseEvent mouseEvent) {
        onHandClicked(mouseEvent, rightThumbFingerImageView, rightIndexFingerImageView, rightMiddleFingerImageView, rightRingFingerImageView, rightLittleFingerImageView, rightThumbFingerTrueImageView, rightIndexFingerTrueImageView, rightMiddleFingerTrueImageView, rightRingFingerTrueImageView, rightLittleFingerTrueImageView, HandType.RIGHT);
    }

    private void onLeftHandImageViewClicked(MouseEvent mouseEvent) {
        onHandClicked(mouseEvent, leftThumbFingerImageView, leftIndexFingerImageView, leftMiddleFingerImageView, leftRingFingerImageView, leftLittleFingerImageView, leftThumbFingerTrueImageView, leftIndexFingerTrueImageView, leftMiddleFingerTrueImageView, leftRingFingerTrueImageView, leftLittleFingerTrueImageView, HandType.LEFT);
    }

    private void onHandClicked(MouseEvent mouseEvent, ImageView thumbIV, ImageView indexIV, ImageView middleIV, ImageView ringIV, ImageView littleIV, ImageView trueThumbIV, ImageView trueIndexIV, ImageView trueMiddleIV, ImageView trueRingIV, ImageView trueLittleIV, HandType handType) {
        if (handType.equals(HandType.RIGHT))
            menu.show(rightHandImageView, mouseEvent.getScreenX(), mouseEvent.getScreenY());
        else
            menu.show(leftHandImageView, mouseEvent.getScreenX(), mouseEvent.getScreenY());

        AtomicReference<ImageView> fingerIV = new AtomicReference<>();
        AtomicReference<ImageView> trueFingerIV = new AtomicReference<>();

        EventHandler<ActionEvent> eventHandler = getMenuItemActionEvent(fingerIV, trueFingerIV);

        thumbMenuItem.setOnAction(event -> {
            setupMenuItemAction(fingerIV, thumbIV, trueFingerIV, trueThumbIV, eventHandler);
            onFingerprintTaken = (finger1, finger2, finger3) -> {
                byte[] template = fingerprintManager.mergeTemplates(finger1.getFingerprintTemplate(), finger2.getFingerprintTemplate(), finger3.getFingerprintTemplate());
                if (handType.equals(HandType.RIGHT))
                    fingerprintManager.getRightHand().setThumbFinger(finger1.getFingerprintImage(), finger2.getFingerprintImage(), finger3.getFingerprintImage(), template);
                else
                    fingerprintManager.getLeftHand().setThumbFinger(finger1.getFingerprintImage(), finger2.getFingerprintImage(), finger3.getFingerprintImage(), template);
            };
        });

        indexMenuItem.setOnAction(event1 -> {
            setupMenuItemAction(fingerIV, indexIV, trueFingerIV, trueIndexIV, eventHandler);
            onFingerprintTaken = (finger1, finger2, finger3) -> {
                byte[] template = fingerprintManager.mergeTemplates(finger1.getFingerprintTemplate(), finger2.getFingerprintTemplate(), finger3.getFingerprintTemplate());
                if (handType.equals(HandType.RIGHT))
                    fingerprintManager.getRightHand().setIndexFinger(finger1.getFingerprintImage(), finger2.getFingerprintImage(), finger3.getFingerprintImage(), template);
                else
                    fingerprintManager.getLeftHand().setIndexFinger(finger1.getFingerprintImage(), finger2.getFingerprintImage(), finger3.getFingerprintImage(), template);
            };
        });

        middleMenuItem.setOnAction(event1 -> {
            setupMenuItemAction(fingerIV, middleIV, trueFingerIV, trueMiddleIV, eventHandler);
            onFingerprintTaken = (finger1, finger2, finger3) -> {
                byte[] template = fingerprintManager.mergeTemplates(finger1.getFingerprintTemplate(), finger2.getFingerprintTemplate(), finger3.getFingerprintTemplate());
                if (handType.equals(HandType.RIGHT))
                    fingerprintManager.getRightHand().setMiddleFinger(finger1.getFingerprintImage(), finger2.getFingerprintImage(), finger3.getFingerprintImage(), template);
                else
                    fingerprintManager.getLeftHand().setMiddleFinger(finger1.getFingerprintImage(), finger2.getFingerprintImage(), finger3.getFingerprintImage(), template);
            };
        });

        ringMenuItem.setOnAction(event1 -> {
            setupMenuItemAction(fingerIV, ringIV, trueFingerIV, trueRingIV, eventHandler);
            onFingerprintTaken = (finger1, finger2, finger3) -> {
                byte[] template = fingerprintManager.mergeTemplates(finger1.getFingerprintTemplate(), finger2.getFingerprintTemplate(), finger3.getFingerprintTemplate());
                if (handType.equals(HandType.RIGHT))
                    fingerprintManager.getRightHand().setRingFinger(finger1.getFingerprintImage(), finger2.getFingerprintImage(), finger3.getFingerprintImage(), template);
                else
                    fingerprintManager.getLeftHand().setRingFinger(finger1.getFingerprintImage(), finger2.getFingerprintImage(), finger3.getFingerprintImage(), template);
            };
        });

        littleMenuItem.setOnAction(event1 -> {
            setupMenuItemAction(fingerIV, littleIV, trueFingerIV, trueLittleIV, eventHandler);
            onFingerprintTaken = (finger1, finger2, finger3) -> {
                byte[] template = fingerprintManager.mergeTemplates(finger1.getFingerprintTemplate(), finger2.getFingerprintTemplate(), finger3.getFingerprintTemplate());
                if (handType.equals(HandType.RIGHT))
                    fingerprintManager.getRightHand().setLittleFinger(finger1.getFingerprintImage(), finger2.getFingerprintImage(), finger3.getFingerprintImage(), template);
                else
                    fingerprintManager.getLeftHand().setLittleFinger(finger1.getFingerprintImage(), finger2.getFingerprintImage(), finger3.getFingerprintImage(), template);
            };
        });
    }

    enum HandType {
        RIGHT,
        LEFT
    }

    private OnFingerprintTaken onFingerprintTaken;

    interface OnFingerprintTaken {
        void handle(Finger finger1, Finger finger2, Finger finger3);
    }

    private EventHandler<ActionEvent> getMenuItemActionEvent(AtomicReference<ImageView> fingerIV, AtomicReference<ImageView> trueFingerIV) {
        final AtomicReference<Finger> finger1 = new AtomicReference<>();
        final AtomicReference<Finger> finger2 = new AtomicReference<>();
        final AtomicReference<Finger> finger3 = new AtomicReference<>();

        return event -> {
            checkFingers();

            FingerprintManager.getSensor().removeOnCaptureListener("REG_LISTENER");

            fingerprintNoteLabel.setText(Utils.getI18nString("ENSURE_REGISTER"));
            onCaptureListener = (imageBuffer, template) -> {
                if (finger1.get() == null) {
                    finger1.set(new Finger(imageBuffer, template));
                    Platform.runLater(() -> fingerprintNoteLabel.setText(Utils.getI18nString("ENSURE_REGISTER2")));
                } else if (finger2.get() == null) {
                    finger2.set(new Finger(imageBuffer, template));
                    Platform.runLater(() -> fingerprintNoteLabel.setText(Utils.getI18nString("ENSURE_REGISTER3")));
                } else if (finger3.get() == null) {
                    finger3.set(new Finger(imageBuffer, template));
                    Platform.runLater(() -> fingerprintNoteLabel.setText(Utils.getI18nString("ENSURE_REGISTER")));
                    fingerIV.get().setVisible(true);
                    trueFingerIV.get().setVisible(true);

                    scheduledFuture.cancel(false);
                    onFingerprintTaken.handle(finger1.get(), finger1.get(), finger3.get());
                }
            };

            FingerprintManager.getSensor().addOnCaptureListener("REG_LISTENER", onCaptureListener);
            try {
                if (FingerprintManager.getSensor().isClosed())
                    FingerprintManager.getSensor().openDevice(0);
            } catch (Throwable t) {
                t.printStackTrace();
            }

            if (scheduledFuture != null)
                scheduledFuture.cancel(false);
            scheduledFuture = Threading.REGISTRATION_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> fingerIV.get().setVisible(!fingerIV.get().isVisible()), 0, 1, TimeUnit.SECONDS);
        };
    }

    private void setupMenuItemAction(AtomicReference<ImageView> fingerIV, ImageView fingetImageView, AtomicReference<ImageView> trueFingerIV, ImageView trueFingerImageView, EventHandler<ActionEvent> eventHandler) {
        fingerIV.set(fingetImageView);
        trueFingerIV.set(trueFingerImageView);
        eventHandler.handle(null);
    }

    private void checkFingers() {
        rightThumbFingerImageView.setVisible(fingerprintManager.getRightHand().getThumbFinger() != null);
        rightThumbFingerTrueImageView.setVisible(fingerprintManager.getRightHand().getThumbFinger() != null);

        rightIndexFingerImageView.setVisible(fingerprintManager.getRightHand().getIndexFinger() != null);
        rightIndexFingerTrueImageView.setVisible(fingerprintManager.getRightHand().getIndexFinger() != null);

        rightMiddleFingerImageView.setVisible(fingerprintManager.getRightHand().getMiddleFinger() != null);
        rightMiddleFingerTrueImageView.setVisible(fingerprintManager.getRightHand().getMiddleFinger() != null);

        rightRingFingerImageView.setVisible(fingerprintManager.getRightHand().getRingFinger() != null);
        rightRingFingerTrueImageView.setVisible(fingerprintManager.getRightHand().getRingFinger() != null);

        rightLittleFingerImageView.setVisible(fingerprintManager.getRightHand().getLittleFinger() != null);
        rightLittleFingerTrueImageView.setVisible(fingerprintManager.getRightHand().getLittleFinger() != null);

        leftThumbFingerImageView.setVisible(fingerprintManager.getLeftHand().getThumbFinger() != null);
        leftThumbFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getThumbFinger() != null);

        leftIndexFingerImageView.setVisible(fingerprintManager.getLeftHand().getIndexFinger() != null);
        leftIndexFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getIndexFinger() != null);

        leftMiddleFingerImageView.setVisible(fingerprintManager.getLeftHand().getMiddleFinger() != null);
        leftMiddleFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getMiddleFinger() != null);

        leftRingFingerImageView.setVisible(fingerprintManager.getLeftHand().getRingFinger() != null);
        leftRingFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getRingFinger() != null);

        leftLittleFingerImageView.setVisible(fingerprintManager.getLeftHand().getLittleFinger() != null);
        leftLittleFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getLittleFinger() != null);
    }
}
