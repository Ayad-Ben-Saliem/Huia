package ly.rqmana.huia.java.controllers.installation;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Pagination;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Res;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class InstallationWindowController implements Controllable {

    public Pagination pagination;
    public JFXButton nextBtn;
    public JFXButton backBtn;
    public JFXButton cancelBtn;
    public CheckBox acceptTermsCheckBox;


    private Node licenseTermsPage;
    private Node healthCenterInfoPage;

    private LicenseTermsPageController licenseTermsPageController;
    private HealthCenterInfoPageController healthCenterInfoPageController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        FXMLLoader loader1 = new FXMLLoader(Res.Fxml.LICENSE_TERMS_PAGE.getUrl(), Utils.getBundle());
        FXMLLoader loader2 = new FXMLLoader(Res.Fxml.HEALTH_CENTER_INFO_PAGE.getUrl(), Utils.getBundle());
        FXMLLoader loader3 = new FXMLLoader(Res.Fxml.INSTALLATION_DIR_PAGE.getUrl(), Utils.getBundle());

        try {
            licenseTermsPage = loader1.load();
            licenseTermsPageController = loader1.getController();

            healthCenterInfoPage = loader2.load();
            healthCenterInfoPageController = loader2.getController();

        } catch (IOException e) {
            e.printStackTrace();
        }

        pagination.setPageFactory(index -> {
            switch (index){
                case 0:
                    return licenseTermsPage;
                case 1:
                    return healthCenterInfoPage;
            }
            return null;
        });
        pagination.setPageCount(2);

        licenseTermsPageController.acceptTermsCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> nextBtn.setDisable(!newValue));

        healthCenterInfoPageController.mapLocation.addListener((observable, oldValue, newValue) -> Platform.runLater(this::checkHealthInfo));
        healthCenterInfoPageController.instituteTitle.textProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::checkHealthInfo));
        healthCenterInfoPageController.installationDir.textProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(this::checkHealthInfo));
    }

    public void onCancelBtnClicked(ActionEvent actionEvent) {
        Windows.INSTALLATION_WINDOW.close();
    }

    public void onBackBtnClicked(ActionEvent actionEvent) {
        pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() - 1);
        if (pagination.getCurrentPageIndex() == 0) {
            backBtn.setDisable(true);
            nextBtn.setDisable(false);
            nextBtn.setText(Utils.getI18nString("NEXT"));
        }
    }

    public void onNextBtnClicked(ActionEvent actionEvent) {
        if (pagination.getCurrentPageIndex() == 0) {
            pagination.setCurrentPageIndex(pagination.getCurrentPageIndex() + 1);
            nextBtn.setText(Utils.getI18nString("INSTALL"));
            backBtn.setDisable(false);
            checkHealthInfo();
        } else if (pagination.getCurrentPageIndex() == 1) {
            // Installation
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("instituteTitle", healthCenterInfoPageController.instituteTitle.getText());

            JSONObject mapLocationJsonObject = new JSONObject();
            mapLocationJsonObject.put("Lat", healthCenterInfoPageController.mapLocation.getValue().getLatitude());
            mapLocationJsonObject.put("Long", healthCenterInfoPageController.mapLocation.getValue().getLongitude());

            jsonObject.put("location", mapLocationJsonObject);

            System.out.println(jsonObject.toString(4));

            try {
                Files.createDirectories(Paths.get(healthCenterInfoPageController.installationDir.getText()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (FileWriter file = new FileWriter(healthCenterInfoPageController.installationDir.getText() + "/BaseInfo.json")) {
                file.write(jsonObject.toString(4));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkHealthInfo() {
        if (healthCenterInfoPageController.mapLocation.getValue() == null ||
            healthCenterInfoPageController.instituteTitle.getText().isEmpty() ||
            healthCenterInfoPageController.installationDir.getText().isEmpty()) {
            nextBtn.setDisable(true);
        } else {
            nextBtn.setDisable(false);
        }
    }
}
