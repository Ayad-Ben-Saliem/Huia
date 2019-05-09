package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import com.zkteco.biometric.FingerprintSensorEx;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import ly.rqmana.huia.java.controls.CustomComboBox;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.Person;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Windows;
import ly.rqmana.huia.java.util.fingerprint.Finger;
import ly.rqmana.huia.java.util.fingerprint.FingerprintManager;
import ly.rqmana.huia.java.util.fingerprint.FingerprintSensor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class AuthenticationWindowController implements Controllable {

    private final FingerprintSensor sensor = new FingerprintSensor();

    private final FingerprintManager fingerprintManager = new FingerprintManager();

    private final MainWindowController mainWindowController = Windows.MAIN_WINDOW.getController();

    @FXML
    public JFXButton fingerprintBtn;
    @FXML public TableView<Person> tableView;
    @FXML public TableColumn<String, Person> nameColumn;
    @FXML public TableColumn<Gender, Person> genderColumn;
    @FXML public TableColumn<LocalDate, Person> birthdayColumn;
    @FXML public TableColumn<String, Person> fingerprintColumn;

    @FXML public JFXTextField nameFilterTF;
    @FXML public JFXDatePicker fromDateFilterDatePicker;
    @FXML public JFXDatePicker toDateFilterDatePicker;
    @FXML public CustomComboBox<String> genderFilterComboBox;
    @FXML public CustomComboBox<String> fingerprintFilterComboBox;
    @FXML public Label nameLabel;
    @FXML public Label fingerprintLabel;

    private ObjectProperty<Person> lastSelectedPerson = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        new Thread(this::loadDataFromDatabase).start();

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        fingerprintColumn.setCellValueFactory(new PropertyValueFactory<>("fingerprint"));

        nameColumn.widthProperty().addListener((observable, oldValue, newValue) -> nameFilterTF.setPrefWidth(newValue.doubleValue()));
        birthdayColumn.widthProperty().addListener((observable, oldValue, newValue) -> {
            fromDateFilterDatePicker.setPrefWidth(newValue.doubleValue()/2);
            toDateFilterDatePicker.setPrefWidth(newValue.doubleValue()/2);
        });
        genderColumn.widthProperty().addListener((observable, oldValue, newValue) -> genderFilterComboBox.setPrefWidth(newValue.doubleValue()));
        fingerprintColumn.widthProperty().addListener((observable, oldValue, newValue) -> fingerprintFilterComboBox.setPrefWidth(newValue.doubleValue()));

        nameFilterTF.setPrefWidth(nameColumn.getPrefWidth());
        fromDateFilterDatePicker.setPrefWidth(birthdayColumn.getPrefWidth()/2);
        toDateFilterDatePicker.setPrefWidth(birthdayColumn.getPrefWidth()/2);
        genderFilterComboBox.setPrefWidth(genderColumn.getPrefWidth());
        fingerprintFilterComboBox.setPrefWidth(fingerprintColumn.getPrefWidth());

        genderFilterComboBox.getItems().addAll("Both", "Male", "Female");
        fingerprintFilterComboBox.getItems().addAll("Bath", "Filled", "UnFilled");

        genderFilterComboBox.setValue("Both");
        fingerprintFilterComboBox.setValue("Both");

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            lastSelectedPerson.setValue(newValue);
            nameLabel.setText(newValue.getFullName());
            fingerprintBtn.setDisable(false);
        });

        try {

            fingerprintManager.getSensor().open();
            getMainController().setOnCloseRequest(event -> {
                fingerprintManager.getSensor().close();
            });
            fingerprintManager.getSensor().setOnCaptureListener((imageBuffer, template) -> {
                StringBuilder stringBuilder = new StringBuilder();
                short x = 0;
                for (byte b : template) {
    //                x = (short)(((short)b)+128);
                    x %= 40;
                    x += 82;
                    stringBuilder.append(b);
                    stringBuilder.append(',');
                }
                System.out.println(stringBuilder);
                Platform.runLater(() -> fingerprintLabel.setText(stringBuilder.toString()));
    //            Person person = lastSelectedPerson.getValue();
    //            if (person != null) {
    //                matchFingerprints(person, template);
    //            } else {
    //                ExecutorService executor = Executors.newFixedThreadPool(100);
    //                tableView.getItems().forEach(p -> executor.submit(() -> {
    //                    if (matchFingerprints(p, template) > 0) {
    //                        executor.shutdown();
    //                        tableView.getSelectionModel().select(p);
    //                    }
    //                }));
    //            }
            });

        } catch (Throwable e) {

        }
    }

    private int matchFingerprints(Person person, byte[] template) {
        int result = 0;
        String fingerprint = person.getFingerprint();
        if (fingerprint != null && !fingerprint.isEmpty()) {
            byte[] template1 = new byte[fingerprint.length()];
            char[] charFingerprint = fingerprint.toCharArray();
            for (int i = 0; i < fingerprint.length(); i++) {
                template1[i] = (byte) charFingerprint[i];
            }
            result = FingerprintSensorEx.DBMatch(1, template1, template);

            System.out.println(result);
        }
        return result;
    }

    private void loadDataFromDatabase() {
        try {
            String connectionUrl = "jdbc:sqlite:C:\\FingerprintData.db";
            try (Connection connection = DriverManager.getConnection(connectionUrl)) {
                String query = "SELECT *  FROM Fingerprint";
                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery(query);

                    ObservableList<Person> people = FXCollections.observableArrayList();

                    while (resultSet.next()) {
                        Person person = new Person();
                        person.setFirstName(resultSet.getString(2));
                        person.setFatherName(resultSet.getString(3));
                        person.setFamilyName(resultSet.getString(4));

                        person.setBirthday(LocalDate.parse(resultSet.getString(5)));
                        person.setNationalId(resultSet.getString(6));
                        person.setGender("M".equals(resultSet.getString(7)) ? Gender.MALE : Gender.FEMALE);
                        person.setFingerprint(resultSet.getString(8));

                        people.add(person);
                    }

                    FilteredList<Person> filteredList = new FilteredList<>(people, person -> true);

                    nameFilterTF.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue == null || newValue.isEmpty()) return;
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
                                return !person.getFingerprint().isEmpty();
                            } else {
                                return person.getFingerprint().isEmpty();
                            }
                        });
                    });

                    tableView.setItems(filteredList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onEnterBtnClicked(ActionEvent actionEvent) {
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

    private OnFingerprintTaken onFingerprintTaken;

    interface OnFingerprintTaken {
        void handle(Finger finger1, Finger finger2, Finger finger3);
    }

    public void onFingerprintBtnClicked(ActionEvent actionEvent) {

    }

    private EventHandler<ActionEvent> getMenuItemActionEvent() {
        final AtomicReference<Finger> finger1 = new AtomicReference<>();
        final AtomicReference<Finger> finger2 = new AtomicReference<>();
        final AtomicReference<Finger> finger3 = new AtomicReference<>();

        return event -> {

            fingerprintManager.getSensor().setOnCaptureListener((imageBuffer, template) -> {
                if (finger1.get() == null) {
                    finger1.set(new Finger(imageBuffer, template));
                } else if (finger2.get() == null) {
                    finger2.set(new Finger(imageBuffer, template));
                } else if (finger3.get() == null) {
                    finger3.set(new Finger(imageBuffer, template));

                    onFingerprintTaken.handle(finger1.get(), finger1.get(), finger3.get());
                }
            });
            try {
                if (!fingerprintManager.getSensor().isOpened())
                    fingerprintManager.getSensor().open();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        };
    }
}
