package ly.rqmana.huia.java.controllers.installation;

import com.jfoenix.controls.JFXTextField;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import ly.rqmana.huia.java.util.Controllable;
import ly.rqmana.huia.java.util.Utils;
import ly.rqmana.huia.java.util.Windows;

import java.io.File;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class HealthCenterInfoPageController implements Controllable {

    @FXML public GoogleMapView googleMapView;
    @FXML public JFXTextField instituteTitle;
    @FXML public JFXTextField installationDir;

    private GoogleMap map;

    private final ContextMenu menu = new ContextMenu();
    private final MenuItem markerMenuItem = new MenuItem(Utils.getI18nString("ADD_MARKER"));

    public final ObjectProperty<LatLong> mapLocation = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        menu.getItems().add(markerMenuItem);
        markerMenuItem.setOnAction(event -> {
            map.clearMarkers();

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(mapLocation.get());
            markerOptions.title(instituteTitle.getText());
            map.addMarker(new Marker(markerOptions));

            instituteTitle.textProperty().addListener((observable, oldValue, newValue) -> markerOptions.title(newValue));
        });

        googleMapView.addMapInializedListener(() -> {
            MapOptions mapOptions = new MapOptions();

            mapOptions.center(new LatLong(32.376195,15.091354))
                    .mapType(MapTypeIdEnum.ROADMAP)
                    .zoom(9);
            map = googleMapView.createMap(mapOptions, false);

            map.addMouseEventHandler(UIEventType.rightclick, event -> mapLocation.setValue(event.getLatLong()));

            googleMapView.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton().equals(MouseButton.SECONDARY)) {
                    menu.show(googleMapView, event.getScreenX(), event.getScreenY());
                }
            });
        });

        installationDir.setText(Objects.requireNonNull(Utils.getInstallationPath()).toString());
    }

    public void onBrowseBtnClicked(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(Windows.INSTALLATION_WINDOW);

        if(selectedDirectory != null) {
            installationDir.setText(selectedDirectory.getAbsolutePath() + File.separator + "Huia");
        }
    }
}
