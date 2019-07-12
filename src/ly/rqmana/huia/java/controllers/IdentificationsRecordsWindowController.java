package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.*;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.models.IdentificationRecord;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.models.User;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;
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
            identification.setDatetime(LocalDateTime.now());
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

    @FXML public JFXButton showHideSearchPaneBtn;

    @FXML private TitledPane filtersBox;
    @FXML private JFXTextField nameFilterField;
    @FXML private JFXTextField workIdFilterField;
    @FXML private JFXTextField identificationCodeFilterField;
    @FXML private JFXTextField userFilterField;
    @FXML private JFXDatePicker lowerBoundDatePicker;
    @FXML private JFXDatePicker upperBoundDatePicker;

    @FXML private JFXCheckBox searchByHoursCheck;
    @FXML private Spinner<Integer> lastHoursSpinner;

    @FXML public JFXButton refreshBtn;

    @FXML private TableView<IdentificationRecord> tableView;
    @FXML private TableColumn<IdentificationRecord, Integer> numberColumn;
    @FXML private TableColumn<IdentificationRecord, String> identificationId;
    @FXML private TableColumn<IdentificationRecord, String> nameColumn;
    @FXML private TableColumn<IdentificationRecord, String> workIdColumn;
    @FXML private TableColumn<IdentificationRecord, String> userColumn;

    private final ObjectProperty<LocalDateTime> upperDateTimeBound = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> lowerDateTimeBound = new SimpleObjectProperty<>();

//    private final ObservableList<IdentificationRecord> cachedData = FXCollections.observableArrayList();
    private FilteredList<IdentificationRecord> filteredList = new FilteredList<>(DAO.IDENTIFICATION_RECORDS);

    private final static Long IDENTIFICATION_VALIDITY = 24L;

    public void initialize(URL location, ResourceBundle resources) {
        initComponents();
        initListeners();

        onSelectedItemChanged(null, null);
        searchByHoursCheck.setSelected(true);

        lowerBoundDatePicker.setValue(LocalDate.now().minusDays(1));
        upperBoundDatePicker.setValue(LocalDate.now());

//        cachedData.addAll(DAO.IDENTIFICATION_RECORDS);
//        DAO.IDENTIFICATION_RECORDS.addListener((InvalidationListener) change -> {
//            cachedData.clear();
//            cachedData.addAll(DAO.IDENTIFICATION_RECORDS);
//        });
        applyFilters();
    }

    private void initComponents(){

        // set the source of the data of the table from the filtered list
        tableView.setItems(filteredList);

        lastHoursSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 24));

        numberColumn.setCellFactory(Utils.getAutoNumberCellFactory());
        identificationId.setCellValueFactory(new PropertyValueFactory<>("stringId"));
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
        searchByHoursCheck.selectedProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        nameFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        workIdFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        identificationCodeFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        userFilterField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        lowerDateTimeBoundProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        upperDateTimeBoundProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        lastHoursSpinner.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());

        // ================== remaining time service ===================== //
        Threading.TIME_COUNT_SERVICE.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {

                if (isEmptySelection())
                    return;

                LocalDateTime startDate = getSelectedRecord().getDatetime();
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

        filtersBox.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                showHideSearchPaneBtn.setText(Utils.getI18nString("HIDE_SEARCH_PANE"));
            } else {
                showHideSearchPaneBtn.setText(Utils.getI18nString("SHOW_SEARCH_PANE"));
            }
        });
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

    void refreshTable() {
        applyFilters();
        tableView.refresh();
    }

    private void applyFilters() {
        filteredList.setPredicate(record -> {
            boolean match = true;
            String nameFilter = nameFilterField.getText();
            String wordIdFilter = workIdFilterField.getText();
            String identificationCode = identificationCodeFilterField.getText();
            String username = userFilterField.getText();

            if (!record.isIdentified())
                return false;

            if (!nameFilter.isEmpty())
                match = record.getSubscriberName().toUpperCase().contains(nameFilter.toUpperCase());

            if (!wordIdFilter.isEmpty())
                match &= record.getSubscriberWorkId().toUpperCase().startsWith(wordIdFilter.toUpperCase());

            if (!identificationCode.isEmpty())
                match &= record.getStringId().toUpperCase().startsWith(identificationCode.toUpperCase());

            if (!username.isEmpty())
                match &= record.getUser().getUsername().toUpperCase().startsWith(username.toUpperCase());

            LocalDateTime datetime = record.getDatetime();

            if (searchByHoursCheck.isSelected()) {
                LocalDateTime hoursFilter = LocalDateTime.now().minusHours(lastHoursSpinner.getValue());
                match &= (datetime.isAfter(hoursFilter) || datetime.isEqual(hoursFilter));

            } else {
                LocalDateTime lowerBound = getLowerDateTimeBound();
                if (lowerBound != null)
                    match &= (datetime.isAfter(lowerBound) || datetime.isEqual(lowerBound));

                LocalDateTime upperBound = getUpperDateTimeBound();
                if (upperBound != null)
                    match &= (datetime.isBefore(upperBound) || datetime.isEqual(upperBound));
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

    public void onShowHideSearchPaneBtnClicked(ActionEvent actionEvent) {
        filtersBox.setExpanded(!filtersBox.isExpanded());
    }

    public void onRefreshBtnClicked(ActionEvent actionEvent) {
        applyFilters();
    }

    public void onExportReportBtnClicked(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(Res.Fxml.IDENTIFICATIONS_EXPORT_REPORT_DIALOG.getUrl(), Utils.getBundle());
        try {
            Region content = loader.load();
//            IdentificationsExportReportDialogController controller = loader.getController();
            JFXDialog dialog = new JFXDialog(getRootStack(), content, JFXDialog.DialogTransition.CENTER);

            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
