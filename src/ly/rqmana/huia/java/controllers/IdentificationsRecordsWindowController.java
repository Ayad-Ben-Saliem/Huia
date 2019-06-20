package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.models.SubscriberIdentification;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class IdentificationsRecordsWindowController implements Controllable {

    @FXML public GridPane headerDetailsPane;
    @FXML private Label subscriberNameLabel;
    @FXML private Label subscriberWorkIdLabel;
    @FXML private Label identificationIdLabel;
    @FXML private Label remainingTimeLabel;

    @FXML private VBox searchFieldsContainer;
    @FXML private JFXTextField nameFilterTF;
    @FXML private JFXTextField workIdTF;
    @FXML private JFXDatePicker lowerBoundDatePicker;
    @FXML private JFXDatePicker upperBoundDatePicker;

    @FXML private JFXCheckBox searchByHoursCheck;
    @FXML private Spinner lastHoursSpinner;
    @FXML private JFXButton searchButton;
    
    @FXML private TableView<SubscriberIdentification> tableView;
    @FXML private TableColumn<SubscriberIdentification, Long> identificationId;
    @FXML private TableColumn<SubscriberIdentification, String> nameColumn;
    @FXML private TableColumn<SubscriberIdentification, String> workIdColumn;
    @FXML private TableColumn<SubscriberIdentification, String> userColumn;

    private final ObjectProperty<LocalDateTime> upperDateTimeBound = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> lowerDateTimeBound = new SimpleObjectProperty<>();

    private final ObservableList<SubscriberIdentification> cachedData = FXCollections.observableArrayList();
    private final FilteredList<SubscriberIdentification> filteredList = new FilteredList<SubscriberIdentification>(cachedData);

    public void initialize(URL location, ResourceBundle resources) {
        initComponents();
        initListeners();

        onSelectedItemChanged(null, null);

        lowerBoundDatePicker.disableProperty().bind(searchByHoursCheck.selectedProperty());
        upperBoundDatePicker.disableProperty().bind(searchByHoursCheck.selectedProperty());

//        for (int i = 0; i < 4; i++){
//
//            Subscriber subscriber = new Subscriber();
//            subscriber.setWorkId("TWorker" + i);
//
//            subscriber.setFirstName("Test" + i);
//            subscriber.setFatherName("Test");
//            subscriber.setGrandfatherName("Test");
//            subscriber.setFamilyName("Test");
//
//            User user = new User();
//            user.setFirstName("Test " + i);
//            user.setFamilyName("User");
//
//            SubscriberIdentification identification = new SubscriberIdentification();
//            identification.setIdentified(true);
//            identification.setDateTime(LocalDateTime.now());
//            identification.setSubscriber(subscriber);
//            identification.setUser(user);
//
//            identification.setId((long) (Math.random() * 1000 + i));
//
//            tableView.getItems().add(identification);
//        }

        loadFromDatabase();
        tableView.setItems(filteredList);
    }

    private void initComponents(){

        identificationId.setCellValueFactory(new PropertyValueFactory<>("id"));

        identificationId.setCellFactory(param -> new TableCell<SubscriberIdentification, Long>(){
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);

                if (! empty && item != null){
                    long idInDecimal = item;
                    String hexId = Long.toHexString(idInDecimal);
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

        upperDateTimeBoundProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                throw new IllegalArgumentException("DateTime bound cannot be null");
            }
            upperBoundDatePicker.setValue(newValue.toLocalDate());
        });

        lowerDateTimeBoundProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                throw new IllegalArgumentException("DateTime bound cannot be null");
            }
            lowerBoundDatePicker.setValue(newValue.toLocalDate());
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> onSelectedItemChanged(oldValue, newValue));
    }

    private void loadFromDatabase(){

        //TODO: check if you want to load identified only or not
        Task<ObservableList<SubscriberIdentification>> loadTask = DAO.getSubscribersIdentifications(false);

        loadTask.addOnSucceeded(event -> {
            ObservableList<SubscriberIdentification> loadedData = loadTask.getValue();
            cachedData.setAll(loadedData);
            System.out.println("loadedData = " + loadedData);
//            syncTableData();
        });

        loadTask.addOnFailed(event -> {
            Throwable exception = event.getSource().getException();
            Windows.errorAlert(
                    Utils.getI18nString("ERROR"),
                    exception.getLocalizedMessage(),
                    exception,
                    AlertAction.OK
            );
        });
        loadTask.runningProperty().addListener((observable, oldValue, newValue) -> updateLoadingView(newValue));

        Threading.MAIN_EXECUTOR_SERVICE.submit(loadTask);
    }


    private void onSelectedItemChanged(SubscriberIdentification oldId, SubscriberIdentification newId){
        headerDetailsPane.setVisible(newId != null);

        if (newId != null) {
            identificationIdLabel.setText(Long.toHexString(newId.getId()));
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

    private void syncTableData(){
        getIdentifications().setAll(cachedData);
        System.out.println(cachedData.size());
//        getIdentifications().removeIf(identification -> {
//
//        });
    }

    public ObservableList<SubscriberIdentification> getIdentifications(){
        return tableView.getItems();
    }

    public SubscriberIdentification getSelectedIdentification(){
        return tableView.getSelectionModel().getSelectedItem();
    }

    public boolean isEmptySelection(){
        return getSelectedIdentification() == null;
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
