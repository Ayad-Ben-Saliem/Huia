package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.models.SubscriberIdentification;
import ly.rqmana.huia.java.models.User;
import ly.rqmana.huia.java.util.Utils;

import java.time.LocalDateTime;

public class IdentificationHistoryWindowController {
    
    @FXML private VBox searchFieldsContainer;
    @FXML private JFXTextField nameFilterTF;
    @FXML private JFXTextField workIdTF;
    @FXML private JFXDatePicker fromDateFilterDatePicker;
    @FXML private JFXDatePicker toDateFilterDatePicker;
    @FXML private Spinner lastHoursSpinner;
    @FXML private JFXButton showLastButton;
    
    @FXML private TableView<SubscriberIdentification> tableView;
    @FXML private TableColumn<SubscriberIdentification, Long> identificationId;
    @FXML private TableColumn<SubscriberIdentification, String> nameColumn;
    @FXML private TableColumn<SubscriberIdentification, String> workIdColumn;
    @FXML private TableColumn<SubscriberIdentification, String> userColumn;
    @FXML private TableColumn<SubscriberIdentification, Boolean> statusColumn;

    @FXML
    private void initialize(){
        initComponents();
        initListeners();

        for (int i = 0; i < 4; i++){

            Subscriber subscriber = new Subscriber();
            subscriber.setWorkId("TWorker" + i);

            subscriber.setFirstName("Test" + i);
            subscriber.setFatherName("Test");
            subscriber.setGrandfatherName("Test");
            subscriber.setFamilyName("Test");

            User user = new User();
            user.setFirstName("Test " + i);
            user.setFamilyName("User");

            SubscriberIdentification identification = new SubscriberIdentification();
            identification.setIdentified(true);
            identification.setDateTime(LocalDateTime.now());
            identification.setSubscriber(subscriber);
            identification.setUser(user);

            identification.setId((long) (Math.random() * 1000 + i));

            tableView.getItems().add(identification);
        }
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

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(param -> new TableCell<SubscriberIdentification, Boolean>(){
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);

                if (! empty && item != null){

                    String statusText = Utils.getI18nString("IDENTIFICATION_FAILED");
                    if (item)
                        statusText = Utils.getI18nString("IDENTIFICATION_SUCCEEDED");

                    setText(statusText);
                }
                else{
                    setText(null);
                    setGraphic(null);
                }
            }
        });
    }

    private void initListeners(){

    }

    private void onSelectedItemChanged(SubscriberIdentification oldId, SubscriberIdentification newId){

    }

    /* ***************************************** *
     *
     *                 Utilities
     *
     * ***************************************** */
}
