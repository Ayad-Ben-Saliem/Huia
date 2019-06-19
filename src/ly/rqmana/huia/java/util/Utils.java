package ly.rqmana.huia.java.util;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.base.IFXValidatableControl;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import ly.rqmana.huia.java.controls.ContactField;
import ly.rqmana.huia.java.controls.CustomComboBox;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;

public class Utils {

    public static final String APP_NAME = "Huia Health Care";

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle(Res.LANGUAGE_PATH, new Locale("ar", "SA"));
    }

    public static String getI18nString(String key) {
        return getBundle().getString(key);
    }

    public static NodeOrientation getNodeOrientation(){

        ResourceBundle bundle = getBundle();
        String orientation = bundle.getString("NODE_ORIENTATION");
        if (orientation.equals("LTR"))
            return NodeOrientation.LEFT_TO_RIGHT;
        else if (orientation.equals("RTL"))
            return NodeOrientation.RIGHT_TO_LEFT;
        else
            return NodeOrientation.INHERIT;
    }

    public static void setTextFieldNumeric(TextField field) {
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                field.setText(oldValue);
            }
        });
    }

    public static Node getErrorIcon() {
        FontAwesomeIconView view = new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_CIRCLE);
        view.setScaleX(0.7);
        view.setScaleY(0.7);
        return view;
    }

    public static void setFieldRequired(IFXValidatableControl field) {
        field.setValidators(new RequiredFieldValidator());
    }

    public static void setFieldRequired(CustomComboBox comboBox) {
        comboBox.setValidators(new RequiredFieldValidator());
    }

    public static void setFieldRequired(ContactField comboBox) {
        comboBox.setValidators(new RequiredFieldValidator());
    }

    @Nullable
    public static <T extends Enum> T getEnumByString(@NotNull String enumValue, Class<T> aClass) {
        if (enumValue == null)
            return null;
        for (T enumConstant : aClass.getEnumConstants()) {
            if (enumValue.equals(enumConstant.toString())) {
                return enumConstant;
            }
        }
        return null;
    }


    public static void printStackTrace() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (int i = 3; i < stackTraceElements.length; i++) {
            System.out.println(stackTraceElements[i]);
        }
    }

    public static OS getOSArch() {
        if (System.getProperty("os.name").contains("Windows")) {
            if (System.getenv("ProgramFiles(x86)") != null)
                return OS.WINDOWS7_x64;
            else
                return OS.WINDOWS7_x86;
        } else {
            if (System.getProperty("os.arch").contains("64"))
                return OS.Mac_x64;
            else
                return OS.Mac_x86;
        }
    }

    public static Path getInstallationPath() {
        switch (Utils.getOSArch()) {
            case WINDOWS7_x86:
            case WINDOWS7_x64:
            case WINDOWS8_x86:
            case WINDOWS8_x64:
            case WINDOWS8_1_x86:
            case WINDOWS8_1_x64:
            case WINDOWS10_x86:
            case WINDOWS10_x64:
                return Paths.get("C:\\Program Files\\Huia");
            case Mac_x64:
            case Mac_x86:
                return Paths.get("/Application/Huia");
            case Linux_x64:
            case Linux_x86:
                return Paths.get("/opt/Huia");
        }
        return null;
    }

    private static long alertNetworkErrorTime = 0;
    public static void alertNetworkError(Exception ex) {
        Platform.runLater(() -> {
            if (System.currentTimeMillis() - alertNetworkErrorTime < 1000000) return;
            if (Windows.NETWORK_ERROR_ALERT.isShowing()) return;

            alertNetworkErrorTime = System.currentTimeMillis();
            Windows.NETWORK_ERROR_ALERT.visualizeStackTrace(ex);
            Windows.NETWORK_ERROR_ALERT.showAndWait();
        });
    }

    public static String getRandomString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(26) + 65;
            stringBuilder.append((char) num);
        }
        return stringBuilder.toString();
    }
}
