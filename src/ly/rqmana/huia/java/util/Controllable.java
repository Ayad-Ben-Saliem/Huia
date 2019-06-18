package ly.rqmana.huia.java.util;

import javafx.fxml.Initializable;
import ly.rqmana.huia.java.controllers.MainWindowController;

public interface Controllable{

    /**
     * This should invokes when attached window is selected ti be viewed.
     */
//    void onWindowSelected();

    default MainWindowController getMainController() {
        return Windows.MAIN_WINDOW.getController();
    }
}
