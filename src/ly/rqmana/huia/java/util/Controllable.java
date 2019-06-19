package ly.rqmana.huia.java.util;

import javafx.fxml.Initializable;
import javafx.scene.layout.StackPane;
import ly.rqmana.huia.java.controllers.*;
import ly.rqmana.huia.java.controllers.settings.SettingsWindowController;
import ly.rqmana.huia.java.controls.alerts.AlertAction;

import java.util.Optional;

public interface Controllable extends Initializable {

    /**
     * This should invokes when attached window is selected ti be viewed.
     */
//    void onWindowSelected();


    default RootWindowController getRootWindowController() {
        return Windows.ROOT_WINDOW.getController();
    }

    default HomeWindowController getHomeWindowController() {
        return getRootWindowController().getHomeWindowController();
    }

    default MainWindowController getMainWindowController() {
        return getRootWindowController().getMainWindowController();
    }

    default RegistrationWindowController getRegistrationWindowController() {
        return getMainWindowController().getRegistrationWindowController();
    }

    default IdentificationWindowController getIdentificationWindowController() {
        return getMainWindowController().getIdentificationWindowController();
    }

    default SettingsWindowController getSettingsWindowController() {
        return getMainWindowController().getSettingsWindowController();
    }

    default StackPane getRootStack() {
        RootWindowController controller = Windows.ROOT_WINDOW.getController();
        return controller.getRootStack();
    }

    default void lock(Boolean isLocked) {
        getRootWindowController().lock(isLocked);
    }

    default void fingerprintDeviceError(Throwable throwable, Runnable tryAgainBlock) {
        Optional<AlertAction> result = Windows.showFingerprintDeviceError(throwable);

        if (result.isPresent() && result.get() == AlertAction.TRY_AGAIN) {
            tryAgainBlock.run();
        }
    }

    default void updateLoadingView(boolean loading){
        if (loading)
            Windows.showLoadingAlert();
        else
            Windows.closeLoadingAlert();
    }

}
