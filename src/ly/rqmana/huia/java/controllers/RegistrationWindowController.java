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

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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
    public ImageView rightPinkyFingerImageView;
    @FXML
    public ImageView rightPinkyFingerTrueImageView;

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
    public ImageView leftPinkyFingerImageView;
    @FXML
    public ImageView leftPinkyFingerTrueImageView;

    @FXML
    public Label fingerprintNoteLabel;

    private final ContextMenu menu = new ContextMenu();
    private final MenuItem thumbMenuItem = new MenuItem("Thumb Finger");
    private final MenuItem indexMenuItem = new MenuItem("Index Finger");
    private final MenuItem middleMenuItem = new MenuItem("Middle Finger");
    private final MenuItem ringMenuItem = new MenuItem("Ring Finger");
    private final MenuItem pinkyMenuItem = new MenuItem("Pinky Finger");

    private final FingerprintManager fingerprintManager = new FingerprintManager();
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

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

        menu.getItems().addAll(thumbMenuItem, indexMenuItem, middleMenuItem, ringMenuItem, pinkyMenuItem);

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
        ObservableList<String> workIdes = FXCollections.observableArrayList();
        String[] queries = {"SELECT workId FROM People;", "SELECT workId FROM NewRegistrations;"};
        for (String query : queries) {
            PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(query);
            ResultSet resultSet = pStatement.executeQuery();
            while (resultSet.next()) {
                workIdes.add(resultSet.getString("workId"));
            }
        }
        employeesWorkIdComboBox.setItems(workIdes);
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

                pStatement.setString(12, getFingerprintTemplate(fingerprintManager.getRightHand().getThumbFinger()));
                pStatement.setString(13, getFingerprintTemplate(fingerprintManager.getRightHand().getIndexFinger()));
                pStatement.setString(14, getFingerprintTemplate(fingerprintManager.getRightHand().getMiddleFinger()));
                pStatement.setString(15, getFingerprintTemplate(fingerprintManager.getRightHand().getRingFinger()));
                pStatement.setString(16, getFingerprintTemplate(fingerprintManager.getRightHand().getLittleFinger()));

                pStatement.setString(17, getFingerprintTemplate(fingerprintManager.getLeftHand().getThumbFinger()));
                pStatement.setString(18, getFingerprintTemplate(fingerprintManager.getLeftHand().getIndexFinger()));
                pStatement.setString(19, getFingerprintTemplate(fingerprintManager.getLeftHand().getMiddleFinger()));
                pStatement.setString(20, getFingerprintTemplate(fingerprintManager.getLeftHand().getRingFinger()));
                pStatement.setString(21, getFingerprintTemplate(fingerprintManager.getLeftHand().getLittleFinger()));

                String imagesDir = DataStorage.getNewRegFingerprintDir(workId);
                DataStorage.saveNewFingerprintImages(workId, fingerprintManager.getRightHand(), fingerprintManager.getLeftHand());
                pStatement.setString(22, imagesDir);

                pStatement.setString(23, Auth.getCurrentUser().getUsername());
                pStatement.setString(24, LocalDate.now().toString());

                pStatement.executeUpdate();

                updateWorkIdes(workId);

                updateAuthTable(constructSubscriber());

                setupInputFields();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, "UPLOAD-THREAD").start();
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

//        subscriber.setFirstName(firstNameTextField.getText());
        return subscriber;
    }

    private void updateAuthTable(Subscriber subscriber) {
        MainWindowController mwc = Windows.MAIN_WINDOW.getController();
        FilteredList<Subscriber> oldFilteredSubscribers = (FilteredList<Subscriber>) mwc.getAuthenticationWindowController().tableView.getItems();
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList(oldFilteredSubscribers.getSource());
        subscribers.add(subscriber);
        System.out.println(subscribers.size() + " subscriber");
        mwc.getAuthenticationWindowController().addToTableView(subscribers);
    }

    private void updateWorkIdes(String newWorkId) {
        ObservableSet<String> workIdes = FXCollections.observableSet();
        workIdes.addAll(employeesWorkIdComboBox.getItems());
        workIdes.add(newWorkId);
        employeesWorkIdComboBox.setItems(FXCollections.observableArrayList(workIdes));
    }

    private String getFingerprintTemplate(Finger finger) {
        if (finger == null) return "";
        StringBuilder stringTemplate = new StringBuilder();
        for (byte b : finger.getFingerprintTemplate()) {
            stringTemplate.append(b);
        }
        return stringTemplate.toString();
    }

    public void onLogoutBtnClicked() {
        mainWindowController.lock(true);
    }

//    public void onRightHandClicked(ActionEvent event) {
//        FingerprintManager.SENSOR.setOnCaptureListener((imageBuffer, template) -> {
//            try {
//                Files.deleteIfExists(new File("fingerprint.bmp").toPath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }

//    public void onLeftHandClicked(ActionEvent event) {
//        FingerprintManager.SENSOR.setOnCaptureListener((imageBuffer, template) -> {
//            try {
//                Files.deleteIfExists(new File("fingerprint.bmp").toPath());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }

    private void onRightHandImageViewClicked(MouseEvent mouseEvent) {
        onHandClicked(mouseEvent, rightThumbFingerImageView, rightIndexFingerImageView, rightMiddleFingerImageView, rightRingFingerImageView, rightPinkyFingerImageView, rightThumbFingerTrueImageView, rightIndexFingerTrueImageView, rightMiddleFingerTrueImageView, rightRingFingerTrueImageView, rightPinkyFingerTrueImageView, HandType.RIGHT);
    }

    private void onLeftHandImageViewClicked(MouseEvent mouseEvent) {
        onHandClicked(mouseEvent, leftThumbFingerImageView, leftIndexFingerImageView, leftMiddleFingerImageView, leftRingFingerImageView, leftPinkyFingerImageView, leftThumbFingerTrueImageView, leftIndexFingerTrueImageView, leftMiddleFingerTrueImageView, leftRingFingerTrueImageView, leftPinkyFingerTrueImageView, HandType.LEFT);
    }

    private void onHandClicked(MouseEvent mouseEvent, ImageView thumbIV, ImageView indexIV, ImageView middleIV, ImageView ringIV, ImageView pinkyIV, ImageView trueThumbIV, ImageView trueIndexIV, ImageView trueMiddleIV, ImageView trueRingIV, ImageView truePinkyIV, HandType handType) {
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

        pinkyMenuItem.setOnAction(event1 -> {
            setupMenuItemAction(fingerIV, pinkyIV, trueFingerIV, truePinkyIV, eventHandler);
            onFingerprintTaken = (finger1, finger2, finger3) -> {
                byte[] template = fingerprintManager.mergeTemplates(finger1.getFingerprintTemplate(), finger2.getFingerprintTemplate(), finger3.getFingerprintTemplate());
                if (handType.equals(HandType.RIGHT))
                    fingerprintManager.getRightHand().setPinkyFinger(finger1.getFingerprintImage(), finger2.getFingerprintImage(), finger3.getFingerprintImage(), template);
                else
                    fingerprintManager.getLeftHand().setPinkyFinger(finger1.getFingerprintImage(), finger2.getFingerprintImage(), finger3.getFingerprintImage(), template);
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

            fingerprintNoteLabel.setText(Utils.getI18nString("ENSURE_REGISTER"));
            FingerprintManager.getSensor().addOnCaptureListener((imageBuffer, template) -> {
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

                    onFingerprintTaken.handle(finger1.get(), finger1.get(), finger3.get());
                }
            });
            try {
                if (FingerprintManager.getSensor().isClosed())
                    FingerprintManager.getSensor().open();
            } catch (Throwable throwable) {
//                error.printStackTrace();
            }

            executorService.scheduleAtFixedRate(() -> fingerIV.get().setVisible(!fingerIV.get().isVisible()), 0, 1, TimeUnit.SECONDS);
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

        rightPinkyFingerImageView.setVisible(fingerprintManager.getRightHand().getLittleFinger() != null);
        rightPinkyFingerTrueImageView.setVisible(fingerprintManager.getRightHand().getLittleFinger() != null);

        leftThumbFingerImageView.setVisible(fingerprintManager.getLeftHand().getThumbFinger() != null);
        leftThumbFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getThumbFinger() != null);

        leftIndexFingerImageView.setVisible(fingerprintManager.getLeftHand().getIndexFinger() != null);
        leftIndexFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getIndexFinger() != null);

        leftMiddleFingerImageView.setVisible(fingerprintManager.getLeftHand().getMiddleFinger() != null);
        leftMiddleFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getMiddleFinger() != null);

        leftRingFingerImageView.setVisible(fingerprintManager.getLeftHand().getRingFinger() != null);
        leftRingFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getRingFinger() != null);

        leftPinkyFingerImageView.setVisible(fingerprintManager.getLeftHand().getLittleFinger() != null);
        leftPinkyFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getLittleFinger() != null);
    }
}
