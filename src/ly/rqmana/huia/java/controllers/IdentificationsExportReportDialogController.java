package ly.rqmana.huia.java.controllers;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import jxl.Workbook;
import jxl.write.*;
import ly.rqmana.huia.java.db.DAO;
import ly.rqmana.huia.java.models.IdentificationRecord;
import ly.rqmana.huia.java.models.Relationship;
import ly.rqmana.huia.java.models.User;
import ly.rqmana.huia.java.util.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class IdentificationsExportReportDialogController implements Controllable {

    @FXML public JFXDatePicker fromDatePicker;
    @FXML public JFXDatePicker toDatePicker;
    @FXML public JFXTimePicker fromTimePicker;
    @FXML public JFXTimePicker toTimePicker;

    @FXML public JFXCheckBox identifiedCheckBox;
    @FXML public JFXCheckBox unidentifiedCheckBox;
    @FXML public JFXCheckBox detailedCheckBox;

    @FXML public JFXCheckBox numberCheckBox;
    @FXML public JFXCheckBox identificationIdCheckBox;
    @FXML public JFXCheckBox nameCheckBox;
    @FXML public JFXCheckBox employeeNameCheckBox;
    @FXML public JFXCheckBox workIdCheckBox;
    @FXML public JFXCheckBox isIdentifiedCheckBox;
    @FXML public JFXCheckBox relationshipCheckBox;
    @FXML public JFXCheckBox userCheckBox;
    @FXML public JFXCheckBox datetimeCheckBox;
    @FXML public JFXCheckBox notesCheckBox;

    @FXML public TableView<IdentificationRecord> tableView;

    @FXML public TableColumn<IdentificationRecord, Integer> numberColumn;
    @FXML public TableColumn<IdentificationRecord, String> identificationIdColumn;
    @FXML public TableColumn<IdentificationRecord, String> nameColumn;
    @FXML public TableColumn<IdentificationRecord, String> employeeNameColumn;
    @FXML public TableColumn<IdentificationRecord, String> workIdColumn;
    @FXML public TableColumn<IdentificationRecord, String> isIdentifiedColumn;
    @FXML public TableColumn<IdentificationRecord, Relationship> relationshipColumn;
    @FXML public TableColumn<IdentificationRecord, User> userColumn;
    @FXML public TableColumn<IdentificationRecord, LocalDateTime> datetimeColumn;
    @FXML public TableColumn<IdentificationRecord, String> notesColumn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        initComponents();

    }

    private void initComponents(){
        LocalDate today = LocalDate.now();
        toDatePicker.setValue(today);
        fromDatePicker.setValue(LocalDate.of(today.getYear(), today.getMonth(), 1));

        fromTimePicker.setValue(LocalTime.of(0, 0, 0, 0));
        toTimePicker.setValue(LocalTime.now());

        fromDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                fromDatePicker.setValue(oldValue);
            } else {
                LocalDateTime fromDateTime = LocalDateTime.of(newValue, fromTimePicker.getValue());
                LocalDateTime toDateTime = LocalDateTime.of(toDatePicker.getValue(), toTimePicker.getValue());
                if (fromDateTime.isAfter(toDateTime)) {
                    fromDatePicker.setValue(oldValue);
                }
            }
        });

        toDatePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                toDatePicker.setValue(oldValue);
            } else {
                LocalDateTime fromDateTime = LocalDateTime.of(fromDatePicker.getValue(), fromTimePicker.getValue());
                LocalDateTime toDateTime = LocalDateTime.of(newValue, toTimePicker.getValue());
                if (toDateTime.isBefore(fromDateTime) || toDateTime.isAfter(LocalDateTime.now())) {
                    toDatePicker.setValue(oldValue);
                }
            }
        });

        fromTimePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                fromTimePicker.setValue(oldValue);
            } else {
                LocalDateTime fromDateTime = LocalDateTime.of(fromDatePicker.getValue(), newValue);
                LocalDateTime toDateTime = LocalDateTime.of(toDatePicker.getValue(), toTimePicker.getValue());
                if (fromDateTime.isAfter(toDateTime)) {
                    fromTimePicker.setValue(oldValue);
                }
            }
        });

        toTimePicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                toTimePicker.setValue(oldValue);
            } else {
                LocalDateTime fromDateTime = LocalDateTime.of(fromDatePicker.getValue(), fromTimePicker.getValue());
                LocalDateTime toDateTime = LocalDateTime.of(toDatePicker.getValue(), newValue);
                if (toDateTime.isBefore(fromDateTime) || toDateTime.isAfter(LocalDateTime.now())) {
                    toTimePicker.setValue(oldValue);
                }
            }
        });

        numberColumn.setCellFactory(Utils.getAutoNumberCellFactory());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("subscriberName"));
        identificationIdColumn.setCellValueFactory(new PropertyValueFactory<>("stringId"));
        employeeNameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        workIdColumn.setCellValueFactory(new PropertyValueFactory<>("subscriberWorkId"));
        isIdentifiedColumn.setCellValueFactory(cellDataFeatures -> {
            if (cellDataFeatures.getValue().isIdentified())
                return new SimpleStringProperty(Utils.getI18nString("IDENTIFIED"));
            else
                return new SimpleStringProperty(Utils.getI18nString("UNIDENTIFIED"));
        });
        relationshipColumn.setCellValueFactory(new PropertyValueFactory<>("relationship"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("providingUserName"));
        datetimeColumn.setCellValueFactory(new PropertyValueFactory<>("datetime"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

        numberColumn.visibleProperty().bind(numberCheckBox.selectedProperty());
        nameColumn.visibleProperty().bind(nameCheckBox.selectedProperty());
        identificationIdColumn.visibleProperty().bind(identificationIdCheckBox.selectedProperty());
        employeeNameColumn.visibleProperty().bind(employeeNameCheckBox.selectedProperty());
        workIdColumn.visibleProperty().bind(workIdCheckBox.selectedProperty());
        isIdentifiedColumn.visibleProperty().bind(isIdentifiedCheckBox.selectedProperty());
        relationshipColumn.visibleProperty().bind(relationshipCheckBox.selectedProperty());
        userColumn.visibleProperty().bind(userCheckBox.selectedProperty());
        datetimeColumn.visibleProperty().bind(datetimeCheckBox.selectedProperty());
        notesColumn.visibleProperty().bind(notesCheckBox.selectedProperty());

        FilteredList<IdentificationRecord> filteredIdentificationRecords = new FilteredList<>(DAO.IDENTIFICATION_RECORDS);
        filteredIdentificationRecords.setPredicate(getFilteredListPredicate());
        tableView.setItems(filteredIdentificationRecords);

        InvalidationListener invalidationListener = observable -> filteredIdentificationRecords.setPredicate(getFilteredListPredicate());
        identifiedCheckBox.selectedProperty().addListener(invalidationListener);
        unidentifiedCheckBox.selectedProperty().addListener(invalidationListener);
        detailedCheckBox.selectedProperty().addListener(invalidationListener);
    }

    private Predicate<? super IdentificationRecord> getFilteredListPredicate() {
        return identificationRecord -> {

            LocalDateTime fromDateTime = LocalDateTime.of(fromDatePicker.getValue(), fromTimePicker.getValue());
            if (identificationRecord.getDatetime().isBefore(fromDateTime))
                return false;

            LocalDateTime toDateTime = LocalDateTime.of(toDatePicker.getValue(), toTimePicker.getValue());
            if (identificationRecord.getDatetime().isAfter(toDateTime))
                return false;

            if (detailedCheckBox.isSelected()) {
                return true;
            } else
            if (identifiedCheckBox.isSelected() && identificationRecord.isIdentified()) {
                return true;
            } else
            if (unidentifiedCheckBox.isSelected() && !identificationRecord.isIdentified()) {
                int index = DAO.IDENTIFICATION_RECORDS.indexOf(identificationRecord);
                try {
                    IdentificationRecord nextRecord = DAO.IDENTIFICATION_RECORDS.get(++index);
                    return identificationRecord.getSubscriber().getId() != nextRecord.getSubscriber().getId();
                } catch (IndexOutOfBoundsException e) {
                    return true;
                }
            }
            return false;
        };
    }

    public void onExportPDFClicked(ActionEvent actionEvent) {

    }

    public void onExportExcelClicked(ActionEvent actionEvent) {
        try {
            File file = Files.createTempFile("identifications", "xls").toFile();
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            WritableSheet identificationsSheet = workbook.createSheet(Utils.getI18nString("IDENTIFICATION_RECORDS"), 0);

            WritableFont font = new WritableFont(WritableFont.ARIAL,10,WritableFont.BOLD);
            WritableCellFormat titleCellFormat = new WritableCellFormat(font);

            Method<Void> addTitleMethod = values -> {
                try {
                    int column1 = (int) values[0];
                    int row1 = (int) values[1];
                    String strKey = (String) values[2];
                    Label cell = new Label(column1, row1, Utils.getI18nString(strKey));
                    cell.setCellFormat(titleCellFormat);
                    identificationsSheet.addCell(cell);
                } catch (WriteException e) {
                    e.printStackTrace();
                }
                return null;
            };

            int row = 0;
            int column = 0;

            if (identificationIdCheckBox.isSelected())
                addTitleMethod.call(column++, row, "IDENTIFICATION_CODE");
            if (nameCheckBox.isSelected())
                addTitleMethod.call(column++, row, "NAME");
            if (employeeNameCheckBox.isSelected())
                addTitleMethod.call(column++, row, "EMPLOYEE");
            if (workIdCheckBox.isSelected())
                addTitleMethod.call(column++, row, "WORK_ID");
            if (isIdentifiedCheckBox.isSelected())
                addTitleMethod.call(column++, row, "IDENTIFIED");
            if (relationshipCheckBox.isSelected())
                addTitleMethod.call(column++, row, "RELATIONSHIP");
            if (userCheckBox.isSelected())
                addTitleMethod.call(column++, row, "USER");
            if (datetimeCheckBox.isSelected())
                addTitleMethod.call(column++, row, "DATETIME");
            if (notesCheckBox.isSelected())
                addTitleMethod.call(column, row, "NOTES");


            for (IdentificationRecord record : tableView.getItems()) {
                column = 0;
                row++;
                if (identificationIdCheckBox.isSelected())
                    identificationsSheet.addCell(new Label(column++, row, record.getStringId()));
                if (nameCheckBox.isSelected())
                    identificationsSheet.addCell(new Label(column++, row, record.getSubscriberName()));
                if (employeeNameCheckBox.isSelected())
                    // Not implemented yet
                    identificationsSheet.addCell(new Label(column++, row, ""));
                if (workIdCheckBox.isSelected())
                    identificationsSheet.addCell(new Label(column++, row, record.getSubscriberWorkId()));
                if (isIdentifiedCheckBox.isSelected())
                    identificationsSheet.addCell(new Label(column++, row, record.isIdentified()? Utils.getI18nString("IDENTIFIED") : Utils.getI18nString("UNIDENTIFIED")));
                if (relationshipCheckBox.isSelected())
                    // Not implemented yet
                    identificationsSheet.addCell(new Label(column++, row, ""));
                if (userCheckBox.isSelected())
                    identificationsSheet.addCell(new Label(column++, row, record.getProvidingUserName()));
                if (datetimeCheckBox.isSelected())
                    identificationsSheet.addCell(new Label(column++, row, record.getDatetime().toString()));
                if (notesCheckBox.isSelected())
                    identificationsSheet.addCell(new Label(column, row, record.getNotes()));
            }

            workbook.write();
            workbook.close();

            FileChooser fileChooser = new FileChooser();
            Path path = fileChooser.showSaveDialog(Windows.ROOT_WINDOW).toPath();
            String stringPath = path.toAbsolutePath().toString();
            if (!stringPath.endsWith(".xls")) {
                stringPath += ".xls";
                path = Paths.get(stringPath);
            }
            Files.copy(file.toPath(), path);

        } catch (IOException | WriteException e) {
            e.printStackTrace();
        }
    }

    public void onSendReportClicked(ActionEvent actionEvent) {

    }
}
