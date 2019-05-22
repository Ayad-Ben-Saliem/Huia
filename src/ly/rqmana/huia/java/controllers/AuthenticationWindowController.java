package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import ly.rqmana.huia.java.controls.CustomComboBox;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.Person;
import ly.rqmana.huia.java.models.Relationship;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;
import ly.rqmana.huia.java.util.fingerprint.FingerprintManager;
import ly.rqmana.huia.java.util.fingerprint.FingerprintSensor;

import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ResourceBundle;

public class AuthenticationWindowController implements Controllable {

    private final FingerprintSensor sensor = new FingerprintSensor();
    private final FingerprintManager fingerprintManager = new FingerprintManager();

    private final MainWindowController mainWindowController = Windows.MAIN_WINDOW.getController();

    @FXML public Label nameLabel;
    @FXML public Label fingerprintLabel;
    @FXML public JFXButton fingerprintBtn;

    @FXML public TableView<Subscriber> tableView;
    @FXML public TableColumn<String, Person> nameColumn;
    @FXML public TableColumn<String, Person> employeeIdColumn;
    @FXML public TableColumn<Gender, Person> genderColumn;
    @FXML public TableColumn<LocalDate, Person> birthdayColumn;
    @FXML public TableColumn<String, Person> fingerprintColumn;
    @FXML public TableColumn relationshipColumn;
    @FXML public TableColumn isActiveColumn;

    @FXML public JFXTextField nameFilterTF;
    @FXML public JFXTextField employeeIdTF;
    @FXML public JFXDatePicker fromDateFilterDatePicker;
    @FXML public JFXDatePicker toDateFilterDatePicker;
    @FXML public CustomComboBox<String> genderFilterComboBox;
    @FXML public CustomComboBox<String> fingerprintFilterComboBox;
    @FXML public CustomComboBox<Relationship> relationshipFilterComboBox;
    @FXML public CustomComboBox<String> isActiveFilterComboBox;

    private ObjectProperty<Person> lastSelectedPerson = new SimpleObjectProperty<>();

    private FilteredList<Subscriber> filteredList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        new Thread(this::loadDataFromDatabase).start();

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        birthdayColumn.setCellValueFactory(new PropertyValueFactory<>("birthday"));
        fingerprintColumn.setCellValueFactory(new PropertyValueFactory<>("fingerprint"));

        nameColumn.widthProperty().addListener((observable, oldValue, newValue) -> nameFilterTF.setPrefWidth(newValue.doubleValue()));
        employeeIdColumn.widthProperty().addListener((observable, oldValue, newValue) -> employeeIdTF.setPrefWidth(newValue.doubleValue()));
        birthdayColumn.widthProperty().addListener((observable, oldValue, newValue) -> {
            fromDateFilterDatePicker.setPrefWidth(newValue.doubleValue()/2);
            toDateFilterDatePicker.setPrefWidth(newValue.doubleValue()/2);
        });
        genderColumn.widthProperty().addListener((observable, oldValue, newValue) -> genderFilterComboBox.setPrefWidth(newValue.doubleValue()));
        relationshipColumn.widthProperty().addListener((observable, oldValue, newValue) -> relationshipFilterComboBox.setPrefWidth(newValue.doubleValue()));
        fingerprintColumn.widthProperty().addListener((observable, oldValue, newValue) -> fingerprintFilterComboBox.setPrefWidth(newValue.doubleValue()));
        isActiveColumn.widthProperty().addListener((observable, oldValue, newValue) -> isActiveFilterComboBox.setPrefWidth(newValue.doubleValue()));

        nameFilterTF.setPrefWidth(nameColumn.getPrefWidth());
        employeeIdTF.setPrefWidth(employeeIdColumn.getPrefWidth());
        fromDateFilterDatePicker.setPrefWidth(birthdayColumn.getPrefWidth()/2);
        toDateFilterDatePicker.setPrefWidth(birthdayColumn.getPrefWidth()/2);
        genderFilterComboBox.setPrefWidth(genderColumn.getPrefWidth());
        relationshipFilterComboBox.setPrefWidth(relationshipFilterComboBox.getPrefWidth());
        fingerprintFilterComboBox.setPrefWidth(fingerprintColumn.getPrefWidth());
        isActiveFilterComboBox.setPrefWidth(isActiveFilterComboBox.getPrefWidth());

        genderFilterComboBox.getItems().addAll(Utils.getI18nString("BOTH"), Utils.getI18nString("MALE"), Utils.getI18nString("FEMALE"));
        relationshipFilterComboBox.getItems().addAll(Arrays.asList(Relationship.values()));
        fingerprintFilterComboBox.getItems().addAll(Utils.getI18nString("BOTH"), Utils.getI18nString("FILLED"), Utils.getI18nString("UNFILLED"));
        isActiveFilterComboBox.getItems().addAll(Utils.getI18nString("ACTIVE"), Utils.getI18nString("NOT_ACTIVE"));

        genderFilterComboBox.setValue("Both");
        fingerprintFilterComboBox.setValue("Both");

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (nameLabel != null) {
                lastSelectedPerson.setValue(newValue);
                nameLabel.setText(newValue.getFullName());
                fingerprintBtn.setDisable(false);
            }
        });

        try {
            FingerprintManager.SENSOR.addOnCaptureListener((imageBuffer, template) -> {
                if (mainWindowController.selectedPageProperty.get().equals(MainWindowController.SelectedPage.AUTHENTICATION)) {
//                    FingerprintManager.getSensor()
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void loadDataFromDatabase() {
        try {
            try (Connection connection = DriverManager.getConnection(DAO.getDataDBUrl())) {
                String query = "SELECT *  FROM Fingerprint";
                try (Statement statement = connection.createStatement()) {
                    ResultSet resultSet = statement.executeQuery(query);

                    ObservableList<Subscriber> subscribers = FXCollections.observableArrayList();

                    while (resultSet.next()) {
                        Subscriber subscriber = new Subscriber();
                        subscriber.setFirstName(resultSet.getString(2));
                        subscriber.setFatherName(resultSet.getString(3));
                        subscriber.setFamilyName(resultSet.getString(4));

                        subscriber.setBirthday(LocalDate.parse(resultSet.getString(5)));
                        subscriber.setNationalId(resultSet.getString(6));
                        subscriber.setGender("M".equals(resultSet.getString(7)) ? Gender.MALE : Gender.FEMALE);
                        subscriber.setFingerprint(resultSet.getString(8));
                        subscriber.setWorkId(resultSet.getString(9));
                        subscriber.setRelationship(resultSet.getString(10));
                        subscriber.setActive(resultSet.getString(15).equals("True"));

                        subscribers.add(subscriber);
                    }

                    addToTableView(subscribers);
                }
            }

            DAO.DB_CONNECTION.prepareStatement("SELECT " +
                    "firstName," +
                    "fatherName," +
                    "grandfatherName," +
                    "familyName," +
                    "birthday," +
                    "nationalId," +
                    "gender," +
                    "workId," +
                    "relationship" +
                    " FROM People");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addToTableView(ObservableList<Subscriber> subscribers) {
        filteredList = new FilteredList<>(subscribers, subscriber -> true);

        nameFilterTF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) return;
            if (newValue.isEmpty()) filteredList.setPredicate(subscriber -> true);
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

    public void onFingerprintBtnClicked(ActionEvent actionEvent) {

    }

}
