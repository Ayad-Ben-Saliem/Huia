package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import ly.rqmana.huia.java.models.SubscriberIdentification;

public class IdentificationsWindowController {
    
    @FXML private VBox searchFieldsContainer;
    @FXML private JFXTextField nameFilterTF;
    @FXML private JFXTextField workIdTF;
    @FXML private JFXDatePicker fromDateFilterDatePicker;
    @FXML private JFXDatePicker toDateFilterDatePicker;
    @FXML private Spinner lastHoursSpinner;
    @FXML private JFXButton showLastButton;
    
    @FXML private TableView<SubscriberIdentification> tableView;
    @FXML private TableColumn<SubscriberIdentification, String> identificationId;
    @FXML private TableColumn<SubscriberIdentification, String> nameColumn;
    @FXML private TableColumn<SubscriberIdentification, String> workIdColumn;
    @FXML private TableColumn<SubscriberIdentification, String> userColumn;
    @FXML private TableColumn<SubscriberIdentification, Boolean> statusColumn;

    @FXML
    private void initialize(){
        initComponents();
        initListeners();

    }

    private void initComponents(){

        identificationId.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        workIdColumn.setCellValueFactory(new PropertyValueFactory<>("workId"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    private void initListeners(){

    }


}
