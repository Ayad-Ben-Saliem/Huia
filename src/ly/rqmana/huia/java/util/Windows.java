package ly.rqmana.huia.java.util;

import javafx.stage.StageStyle;

public class Windows {

    public final static Window MAIN_WINDOW = new Window(Res.Fxml.MAIN_WINDOW);
    public static final Window INSTALLATION_WINDOW = new Window(Res.Fxml.INSTALLATION_WINDOW);

    static {
        MAIN_WINDOW.setTitle(Utils.getI18nString("APP_NAME"));
        MAIN_WINDOW.setMinWidth(1000);
        MAIN_WINDOW.setMinHeight(700);

//        INSTALLATION_WINDOW.initStyle(StageStyle.UNDECORATED);
        INSTALLATION_WINDOW.setResizable(false);

    }
}
