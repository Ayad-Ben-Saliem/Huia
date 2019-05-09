package ly.rqmana.huia.java.controllers;

import com.google.gson.Gson;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXTextField;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import ly.rqmana.huia.java.controls.ToggleSwitch;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.Relationship;
import ly.rqmana.huia.java.util.*;
import ly.rqmana.huia.java.util.fingerprint.Finger;
import ly.rqmana.huia.java.util.fingerprint.FingerprintManager;
import ly.rqmana.huia.java.util.fingerprint.FingerprintSensor;
import org.apache.commons.codec.Charsets;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class RegistrationWindowController implements Controllable {

    @FXML
    public ToggleSwitch isEmployeeToggleSwitch;
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
    public CustomComboBox<String> instituteComboBox;
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
    public FontAwesomeIconView zoomIconView;
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

    private boolean isZoomed = false;
    private final FingerprintSensor sensor = new FingerprintSensor();

    private final ContextMenu menu = new ContextMenu();
    private final MenuItem thumbMenuItem = new MenuItem("Thumb Finger");
    private final MenuItem indexMenuItem = new MenuItem("Index Finger");
    private final MenuItem middleMenuItem = new MenuItem("Middle Finger");
    private final MenuItem ringMenuItem = new MenuItem("Ring Finger");
    private final MenuItem pinkyMenuItem = new MenuItem("Pinky Finger");

    private final FingerprintManager fingerprintManager = new FingerprintManager();
    private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final Timer timer = new Timer("Huia-Timer");
    private InterruptableTimerTask pinkingTimerTask = new InterruptableTimerTask() {
        @Override
        public void run() {
        }
    };

    private final MainWindowController mainWindowController = Windows.MAIN_WINDOW.getController();

    @Override
    public void initialize(URL location, ResourceBundle resources) {



        isEmployeeToggleSwitch.statusProperty().addListener((observable, oldValue, newValue) -> {
            newEmployeeWorkIdTextField.setManaged(newValue);
            newEmployeeWorkIdTextField.setVisible(newValue);
            employeesWorkIdComboBox.setManaged(!newValue);
            employeesWorkIdComboBox.setVisible(!newValue);

            if (newValue) {
//                executorService.
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        relationshipComboBox.setMaxWidth(relationshipComboBox.getWidth() - 10);
                        if (relationshipComboBox.getWidth() <= 0) {
                            relationshipComboBox.setManaged(false);
                            relationshipComboBox.setVisible(false);
                            cancel();
                        }
                    }
                }, 0, 25);
            } else {
                relationshipComboBox.setManaged(true);
                relationshipComboBox.setVisible(true);
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        relationshipComboBox.setMaxWidth(relationshipComboBox.getWidth() + 10);
                        if (relationshipComboBox.getWidth() >= 151) {
                            relationshipComboBox.setMaxWidth(151);
                            cancel();
                        }
                    }
                }, 0, 25);
            }
        });


        menu.getItems().addAll(thumbMenuItem, indexMenuItem, middleMenuItem, ringMenuItem, pinkyMenuItem);

        mainWindowController.setOnCloseRequest(event -> {
            timer.purge();
            timer.cancel();
        });

        new Thread(() -> {
            try {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet("http://huia.herokuapp.com//getEmployees/");
                HttpResponse response = client.execute(httpGet);
                String content = EntityUtils.toString(response.getEntity());

                if (content == null || content.length() < 4) return;

                content = content.substring(1, content.length() - 2);
                String[] employees = content.split("}, ");
                Gson gson = new Gson();
                ObservableList<String> workIdes = FXCollections.observableArrayList();
                for (String employee : employees) {
                    employee += "}";
                    Map map = gson.fromJson(employee, Map.class);
                    workIdes.add((String) map.get("workId"));
                }
                employeesWorkIdComboBox.setItems(workIdes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpGet httpGet = new HttpGet("http://huia.herokuapp.com//getInstitutes/");
                HttpResponse response = client.execute(httpGet);
                String content = EntityUtils.toString(response.getEntity());
                if (content.length() > 2) {
                    content = content.substring(2, content.length() - 2);
                    String[] institutes = content.split("', '");
                    instituteComboBox.setItems(FXCollections.observableArrayList(institutes));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        genderComboBox.setItems(FXCollections.observableArrayList(Gender.values()));
        genderComboBox.getSelectionModel().select(0);

        relationshipComboBox.setItems(FXCollections.observableArrayList(Relationship.values()));

        Utils.setFieldRequired(firstNameTextField);
        Utils.setFieldRequired(fatherNameTextField);
        Utils.setFieldRequired(grandfatherNameTextField);
        Utils.setFieldRequired(familyNameTextField);

        Utils.setFieldRequired(genderComboBox);
        Utils.setFieldRequired(instituteComboBox);
        Utils.setFieldRequired(birthdayDatePicker);
        Utils.setFieldRequired(nationalIdTextField);
        Utils.setFieldRequired(nationalityTextField);
        Utils.setFieldRequired(relationshipComboBox);
        Utils.setFieldRequired(employeesWorkIdComboBox);
        Utils.setFieldRequired(newEmployeeWorkIdTextField);

        contactsContainer.getChildren().forEach(node -> {
            ContactField field = (ContactField) node;
            Utils.setFieldRequired(field);
        });

        rightHandImageContainer.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onRightHandImageViewClicked);
        leftHandImageContainer.addEventFilter(MouseEvent.MOUSE_CLICKED, this::onLeftHandImageViewClicked);
    }

    private void sendToServer(String data) throws Exception {
        System.out.println(data);
//        System.out.println(Request.Post("http://huia.herokuapp.com//insert/").bodyString(data, ContentType.APPLICATION_JSON));
//        Request.Post("http://huia.herokuapp.com//insert/").bodyString(data, ContentType.APPLICATION_JSON).execute();

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://huia.herokuapp.com//insert/");

        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("data", data));
        urlParameters.add(new BasicNameValuePair("type", "addPerson"));

        httpPost.setEntity(new UrlEncodedFormEntity(urlParameters));

//        StringEntity requestEntity = new StringEntity(data, ContentType.APPLICATION_JSON);
//        httpPost.setEntity(requestEntity);
        CloseableHttpResponse response = client.execute(httpPost);
        System.out.println(response);
//        client.close();
    }

    public void onAddContactBtnClicked(ActionEvent actionEvent) {
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

    public void onEnterBtnClicked(ActionEvent actionEvent) {

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
        if (isEmployeeToggleSwitch.getStatus()) {
            validate &= newEmployeeWorkIdTextField.validate();
        } else {
            validate &= employeesWorkIdComboBox.validate();
            validate &= relationshipComboBox.validate();
        }

        validate &= contactsContainer.getChildren().stream().map(node -> (ContactField) node).map(ContactField::validate).reduce(true, (a, b) -> a && b);

        System.out.println("validate = " + validate);
        if (!validate) return;

        new Thread(() -> {
            try {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost("http://huia.herokuapp.com//insert/");

                List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
                urlParameters.add(new BasicNameValuePair("firstName", firstNameTextField.getText()));
                urlParameters.add(new BasicNameValuePair("fatherName", fatherNameTextField.getText()));
                urlParameters.add(new BasicNameValuePair("grandfatherName", grandfatherNameTextField.getText()));
                urlParameters.add(new BasicNameValuePair("familyName", familyNameTextField.getText()));
                urlParameters.add(new BasicNameValuePair("nationality", nationalityTextField.getText()));
                urlParameters.add(new BasicNameValuePair("nationalId", nationalIdTextField.getText()));
                urlParameters.add(new BasicNameValuePair("birthday", birthdayDatePicker.getValue().toString()));
                urlParameters.add(new BasicNameValuePair("gender", genderComboBox.getValue().toString()));
                urlParameters.add(new BasicNameValuePair("institute", instituteComboBox.getValue()));
                urlParameters.add(new BasicNameValuePair("workId", newEmployeeWorkIdTextField.getText()));
                if (!isEmployeeToggleSwitch.getStatus()) {
                    urlParameters.add(new BasicNameValuePair("relationship", relationshipComboBox.getValue().toString()));
                }

                contactsContainer.getChildren().forEach(node -> {
                    ContactField contactField = (ContactField) node;
                    urlParameters.add(new BasicNameValuePair(contactField.getType().name(), contactField.getText()));
                });

                urlParameters.add(new BasicNameValuePair("rightThumbFinger", getFingerprintTemplate(fingerprintManager.getRightHand().getThumbFinger())));
                urlParameters.add(new BasicNameValuePair("rightIndexFinger", getFingerprintTemplate(fingerprintManager.getRightHand().getIndexFinger())));
                urlParameters.add(new BasicNameValuePair("rightMiddleFinger", getFingerprintTemplate(fingerprintManager.getRightHand().getMiddleFinger())));
                urlParameters.add(new BasicNameValuePair("rightRingFinger", getFingerprintTemplate(fingerprintManager.getRightHand().getRingFinger())));
                urlParameters.add(new BasicNameValuePair("rightPinkyFinger", getFingerprintTemplate(fingerprintManager.getRightHand().getPinkyFinger())));

                urlParameters.add(new BasicNameValuePair("leftThumbFinger", getFingerprintTemplate(fingerprintManager.getLeftHand().getThumbFinger())));
                urlParameters.add(new BasicNameValuePair("leftIndexFinger", getFingerprintTemplate(fingerprintManager.getLeftHand().getIndexFinger())));
                urlParameters.add(new BasicNameValuePair("leftMiddleFinger", getFingerprintTemplate(fingerprintManager.getLeftHand().getMiddleFinger())));
                urlParameters.add(new BasicNameValuePair("leftRingFinger", getFingerprintTemplate(fingerprintManager.getLeftHand().getRingFinger())));
                urlParameters.add(new BasicNameValuePair("leftPinkyFinger", getFingerprintTemplate(fingerprintManager.getLeftHand().getPinkyFinger())));

                System.out.println(urlParameters);

                httpPost.setEntity(new UrlEncodedFormEntity(urlParameters, Charsets.UTF_8));

                HttpResponse response = client.execute(httpPost);
                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity);
                EntityUtils.consume(entity);

                System.out.println("Content: " + content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, "UPLOAD-THREAD").start();
    }

    public String getFingerprintTemplate(Finger finger) {
        if (finger == null) return "";
        StringBuilder stringTemplate = new StringBuilder();
        for (byte b : finger.getFingerprintTemplate()) {
            stringTemplate.append(b);
        }
        return stringTemplate.toString();
    }

    public void onLogoutBtnClicked(ActionEvent actionEvent) {
        mainWindowController.lock(true);
    }

    public void onRightHandClicked(ActionEvent event) {
        sensor.setOnCaptureListener((imageBuffer, template) -> {
            try {
                Files.deleteIfExists(new File("fingerprint.bmp").toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sensor.open();
    }

    public void onLeftHandClicked(ActionEvent event) {
        sensor.setOnCaptureListener((imageBuffer, template) -> {
            try {
                Files.deleteIfExists(new File("fingerprint.bmp").toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        sensor.open();
    }

    public void onZoomBtnClicked(ActionEvent event) {
        isZoomed = !isZoomed;
        double initWidth = rightHandImageView.getFitWidth();

        final double zoomRatio = isZoomed ? 1.1 : 0.9;
        if (isZoomed) {
            zoomIconView.setIcon(FontAwesomeIcon.SEARCH_MINUS);
        } else {
            zoomIconView.setIcon(FontAwesomeIcon.SEARCH_PLUS);
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    rightHandImageView.setFitWidth(zoomRatio * rightHandImageView.getFitWidth());
                    rightHandImageView.setFitHeight(zoomRatio * rightHandImageView.getFitHeight());

                    leftHandImageView.setFitWidth(zoomRatio * leftHandImageView.getFitWidth());
                    leftHandImageView.setFitHeight(zoomRatio * leftHandImageView.getFitHeight());
                });
                if (isZoomed) {
                    if (rightHandImageView.getFitWidth() > 2 * initWidth) {
                        cancel();
                    }
                } else {
                    if (rightHandImageView.getFitWidth() < initWidth / 2) {
                        cancel();
                    }
                }
            }
        }, 0, 100);
    }

    public void onRightHandImageViewClicked(MouseEvent mouseEvent) {
        onHandClicked(mouseEvent, rightThumbFingerImageView, rightIndexFingerImageView, rightMiddleFingerImageView, rightRingFingerImageView, rightPinkyFingerImageView, rightThumbFingerTrueImageView, rightIndexFingerTrueImageView, rightMiddleFingerTrueImageView, rightRingFingerTrueImageView, rightPinkyFingerTrueImageView, HandType.RIGHT);
    }

    public void onLeftHandImageViewClicked(MouseEvent mouseEvent) {
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
            fingerprintManager.getSensor().setOnCaptureListener((imageBuffer, template) -> {
                if (finger1.get() == null) {
                    finger1.set(new Finger(imageBuffer, template));
                    Platform.runLater(() -> fingerprintNoteLabel.setText(Utils.getI18nString("ENSURE_REGISTER2")));
                } else if (finger2.get() == null) {
                    finger2.set(new Finger(imageBuffer, template));
                    Platform.runLater(() -> fingerprintNoteLabel.setText(Utils.getI18nString("ENSURE_REGISTER3")));
                } else if (finger3.get() == null) {
                    finger3.set(new Finger(imageBuffer, template));
                    Platform.runLater(() -> fingerprintNoteLabel.setText(Utils.getI18nString("ENSURE_REGISTER")));
                    pinkingTimerTask.setFinished(true);
                    fingerIV.get().setVisible(true);
                    trueFingerIV.get().setVisible(true);

                    onFingerprintTaken.handle(finger1.get(), finger1.get(), finger3.get());
                }
            });
            try {
                if (!fingerprintManager.getSensor().isOpened())
                    fingerprintManager.getSensor().open();
            } catch (Throwable throwable) {
//                error.printStackTrace();
            }

            if (pinkingTimerTask != null) {
                pinkingTimerTask.setFinished(true);
            }
            pinkingTimerTask = new InterruptableTimerTask() {
                @Override
                public void run() {
                    if (isFinished()) {
                        cancel();
                        return;
                    }
                    fingerIV.get().setVisible(!fingerIV.get().isVisible());
                }
            };
            timer.scheduleAtFixedRate(pinkingTimerTask, 0, 1000);
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

        rightPinkyFingerImageView.setVisible(fingerprintManager.getRightHand().getPinkyFinger() != null);
        rightPinkyFingerTrueImageView.setVisible(fingerprintManager.getRightHand().getPinkyFinger() != null);

        leftThumbFingerImageView.setVisible(fingerprintManager.getLeftHand().getThumbFinger() != null);
        leftThumbFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getThumbFinger() != null);

        leftIndexFingerImageView.setVisible(fingerprintManager.getLeftHand().getIndexFinger() != null);
        leftIndexFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getIndexFinger() != null);

        leftMiddleFingerImageView.setVisible(fingerprintManager.getLeftHand().getMiddleFinger() != null);
        leftMiddleFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getMiddleFinger() != null);

        leftRingFingerImageView.setVisible(fingerprintManager.getLeftHand().getRingFinger() != null);
        leftRingFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getRingFinger() != null);

        leftPinkyFingerImageView.setVisible(fingerprintManager.getLeftHand().getPinkyFinger() != null);
        leftPinkyFingerTrueImageView.setVisible(fingerprintManager.getLeftHand().getPinkyFinger() != null);
    }
}
