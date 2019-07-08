package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.models.IdentificationRecord;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.models.User;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.net.URL;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class IdentificationsRecordsWindowController implements Controllable {

    /* ************************************************* *
     *
     *
     *                  Test & Dev
     *
     *  ************************************************ */

    private void addTestRecords(int count){

        for (int i = 0; i < count; i++){

            Subscriber subscriber = new Subscriber();
            subscriber.setWorkId("TWorker" + i);

            subscriber.setFirstName("Test" + i);
            subscriber.setFatherName("Test");
            subscriber.setGrandfatherName("Test");
            subscriber.setFamilyName("Test");

            User user = new User();
            user.setFirstName("Test " + i);
            user.setFamilyName("User");

            IdentificationRecord identification = new IdentificationRecord();
            identification.setIdentified(true);
            identification.setDateTime(LocalDateTime.now());
            identification.setSubscriber(subscriber);
            identification.setUser(user);

            identification.setId((long) (Math.random() * 1000 + i));

            tableView.getItems().add(identification);
        }
    }

    // ********************** ******************************** //

    @FXML public GridPane headerDetailsPane;
    @FXML private Label subscriberNameLabel;
    @FXML private Label subscriberWorkIdLabel;
    @FXML private Label identificationIdLabel;

    @FXML private Label hoursCountLabel;
    @FXML private Label minutesCountLabel;
    @FXML private Label secondsCountLabel;

    @FXML private TitledPane filtersBox;
    @FXML private JFXTextField nameFilterField;
    @FXML private JFXTextField workIdFilterField;
    @FXML private JFXDatePicker lowerBoundDatePicker;
    @FXML private JFXDatePicker upperBoundDatePicker;

    @FXML private JFXCheckBox searchByHoursCheck;
    @FXML private Spinner<Integer> lastHoursSpinner;
    @FXML private JFXButton searchButton;
    
    @FXML private TableView<IdentificationRecord> tableView;
    @FXML private TableColumn<IdentificationRecord, Long> identificationId;
    @FXML private TableColumn<IdentificationRecord, String> nameColumn;
    @FXML private TableColumn<IdentificationRecord, String> workIdColumn;
    @FXML private TableColumn<IdentificationRecord, String> userColumn;

    private final ObjectProperty<LocalDateTime> upperDateTimeBound = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> lowerDateTimeBound = new SimpleObjectProperty<>();

    private final ObservableList<IdentificationRecord> cachedData = FXCollections.observableArrayList();
    private final FilteredList<IdentificationRecord> filteredList = new FilteredList<>(cachedData);

    private final static Long IDENTIFICATION_VALIDITY = 24L;

    public void initialize(URL location, ResourceBundle resources) {

        initComponents();
        initListeners();

        onSelectedItemChanged(null, null);
        searchByHoursCheck.setSelected(true);

        lowerBoundDatePicker.setValue(LocalDate.now().minusDays(1));
        upperBoundDatePicker.setValue(LocalDate.now());

        loadFromDatabase();
    }

    private void initComponents(){

        // set the source of the data of the table from the filtered list

        tableView.setItems(filteredList);

        lastHoursSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 24));

        identificationId.setCellValueFactory(new PropertyValueFactory<>("id"));

        identificationId.setCellFactory(param -> new TableCell<IdentificationRecord, Long>(){
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);

                if (! empty && item != null){
                    long idInDecimal = item;
                    String hexId = Long.toHexString(idInDecimal).toUpperCase();
                    setText(hexId);
                }
                else{
                    setText(null);
                }
            }
        });

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("subscriberName"));
        workIdColumn.setCellValueFactory(new PropertyValueFactory<>("subscriberWorkId"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("providingUserName"));
    }

    private void initListeners(){

        upperBoundDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                setUpperDateTimeBound(null);
            else
                setUpperDateTimeBound(LocalDateTime.of(newValue, LocalTime.MAX));
        });

        lowerBoundDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null)
                setLowerDateTimeBound(null);
            else
                setLowerDateTimeBound(LocalDateTime.of(newValue, LocalTime.MIN));
        });

        upperDateTimeBoundProperty().addListener((observable, oldValue, newValue) -> {
            upperBoundDatePicker.setValue(newValue.toLocalDate());
        });

        lowerDateTimeBoundProperty().addListener((observable, oldValue, newValue) -> {
            lowerBoundDatePicker.setValue(newValue.toLocalDate());
        });

        lowerBoundDatePicker.disableProperty().bind(searchByHoursCheck.selectedProperty());
        upperBoundDatePicker.disableProperty().bind(searchByHoursCheck.selectedProperty());

        searchByHoursCheck.selectedProperty().addListener((observable, oldValue, newValue) -> lastHoursSpinner.setDisable(! newValue));

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> onSelectedItemChanged(oldValue, newValue));

        // *** filtering-related listeners *****
        searchButton.addEventFilter(ActionEvent.ACTION, event -> applyFilters());

        searchByHoursCheck.selectedProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        nameFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        workIdFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        lowerDateTimeBoundProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        upperDateTimeBoundProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        lastHoursSpinner.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // ================== remaining time service ===================== //
        Threading.TIME_COUNT_SERVICE.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {

                if (isEmptySelection())
                    return;

                LocalDateTime startDate = getSelectedRecord().getDateTime();
                LocalDateTime endDate = startDate.plusHours(IDENTIFICATION_VALIDITY);
                LocalDateTime now = LocalDateTime.now();


                long hours = now.until( endDate, ChronoUnit.HOURS);
                now = now.plusHours( hours );

                long minutes = now.until( endDate, ChronoUnit.MINUTES);
                now = now.plusMinutes( minutes );

                long seconds = now.until( endDate, ChronoUnit.SECONDS);

                if (now.isAfter(endDate)){
                    hours = 0;
                    minutes = 0;
                    seconds = 0;
                }

                String format = "%02d";

                hoursCountLabel.setText(String.format(format, hours));
                minutesCountLabel.setText(String.format(format, minutes));
                secondsCountLabel.setText(String.format(format, seconds));

            });
        }, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void loadFromDatabase(){

        //TODO: check if you want to load identified only or not
        Task<ObservableList<IdentificationRecord>> loadTask = DAO.getIdentificationRecords();

        loadTask.addOnSucceeded(event -> {
            cachedData.setAll(loadTask.getValue());
            applyFilters();
        });

        loadTask.addOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            Windows.errorAlert(
                    Utils.getI18nString("ERROR"),
                    exception.getMessage(),
                    exception,
                    AlertAction.OK
            );
        });
        loadTask.runningProperty().addListener((observable, oldValue, newValue) -> updateLoadingView(newValue));

        Threading.MAIN_EXECUTOR_SERVICE.submit(loadTask);
    }

    private void onSelectedItemChanged(IdentificationRecord oldId, IdentificationRecord newId){
        headerDetailsPane.setVisible(newId != null);

        if (newId != null) {
            identificationIdLabel.setText(Long.toHexString(newId.getId()).toUpperCase());
            subscriberNameLabel.setText(newId.getSubscriberName());
            subscriberWorkIdLabel.setText(newId.getSubscriberWorkId());

            // TODO: remaining Time
        }
    }

    /* ***************************************** *
     *
     *                 Utilities
     *
     * ***************************************** */

    private void applyFilters(){

        String nameFilter = nameFilterField.getText();
        String wordIdFilter = workIdFilterField.getText();

        boolean isSearchByHours = searchByHoursCheck.isSelected();

        LocalDateTime lowerBound = getLowerDateTimeBound();
        LocalDateTime upperBound = getUpperDateTimeBound();

        filteredList.setPredicate(record -> {
            boolean match = true;

            if (!record.isIdentified())
                return false;

            if (! nameFilter.isEmpty())
                match = record.getSubscriberName().contains(nameFilter);

            if (! wordIdFilter.isEmpty())
                match = match && record.getSubscriberWorkId().contains(wordIdFilter);

            LocalDateTime dateTime = record.getDateTime();

            if (! isSearchByHours){

                if (lowerBound != null)
                    match = match && (dateTime.isAfter(lowerBound) || dateTime.isEqual(lowerBound));

                if (upperBound != null)
                    match = match && (dateTime.isBefore(upperBound) || dateTime.isEqual(upperBound));
            }
            else{
                LocalDateTime hoursFilter = LocalDateTime.now().minusHours(lastHoursSpinner.getValue());
                match = match && (dateTime.isAfter(hoursFilter) || dateTime.isEqual(hoursFilter));
            }

            return match;
        });
    }

    private ObservableList<IdentificationRecord> getVisibleRecords(){
        return tableView.getItems();
    }

    private IdentificationRecord getSelectedRecord(){
        return tableView.getSelectionModel().getSelectedItem();
    }

    public boolean isEmptySelection(){
        return getSelectedRecord() == null;
    }

    public void addIdentificationRecord(IdentificationRecord record){
        cachedData.add(record);
    }

    public void addIdentificationRecords(List<IdentificationRecord> records){
        cachedData.addAll(records);
    }

    /* ***************************************** *
     *
     *             Setters && Getters
     *
     * ***************************************** */

    public LocalDateTime getUpperDateTimeBound() {
        return upperDateTimeBound.get();
    }

    public ObjectProperty<LocalDateTime> upperDateTimeBoundProperty() {
        return upperDateTimeBound;
    }

    public void setUpperDateTimeBound(LocalDateTime upperDateTimeBound) {
        this.upperDateTimeBound.set(upperDateTimeBound);
    }

    public LocalDateTime getLowerDateTimeBound() {
        return lowerDateTimeBound.get();
    }

    public ObjectProperty<LocalDateTime> lowerDateTimeBoundProperty() {
        return lowerDateTimeBound;
    }

    public void setLowerDateTimeBound(LocalDateTime lowerDateTimeBound) {
        this.lowerDateTimeBound.set(lowerDateTimeBound);
    }
}
