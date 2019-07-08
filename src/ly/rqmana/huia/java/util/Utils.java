package ly.rqmana.huia.java.util;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.base.IFXValidatableControl;
import com.nitgen.SDK.BSP.NBioBSPJNI;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import com.sun.javafx.scene.control.skin.TitledPaneSkin;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.Duration;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controls.ContactField;
import ly.rqmana.huia.java.controls.CustomComboBox;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.fingerprints.activity.FingerprintManager;
import ly.rqmana.huia.java.fingerprints.device.FingerprintDeviceType;
import ly.rqmana.huia.java.models.IdentificationRecord;
import ly.rqmana.huia.java.storage.DataStorage;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static final String APP_NAME = "Huia Healthcare";

    private static final String DATE_TIME_FORMAT
            = "[yyyy-MM-dd[ HH:mm:ss[.S[SS[SSS]][ Z]]]]"
            + "[ddMMMyyyy[ HH:mm:ss[.S[SS[SSS]][ Z]]]]"
            + "[yyyy-MM-dd['T'HH:mm:ss[.S[SS[SSS]][ Z]]]]"
            + "[ddMMMyyyy['T'HH:mm:ss[.S[SS[SSS]][ Z]]]]"
            + "[ddMMMyyyy[:HH:mm:ss[.S[SS[SSS]][ Z]]]]";

    private static Process fingerprintBackgroundServer;

    static {
        try {
            setTitledPaneDuration(new Duration(500));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

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

    public static void loadNBioBSPJNILib() throws IOException, InterruptedException {
        final String CHANGE_DATE_COMMAND = "cmd /c date ";
        final LocalDate TODAY = LocalDate.now();

        Process process = Runtime.getRuntime().exec(CHANGE_DATE_COMMAND + "7/7/2019");
        process.waitFor();
        System.loadLibrary("NBioBSPJNI");

        new Thread(() -> {
            try {
                Thread.sleep(6000);
                Runtime.getRuntime().exec(CHANGE_DATE_COMMAND + TODAY.format(DateTimeFormatter.ofPattern("MM/dd/YYYY")));
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public static void establishFingerprintDevice(){
        Task<Boolean> openDeviceTask = FingerprintManager.openDeviceIfNotOpen(FingerprintDeviceType.HAMSTER_DX);

        openDeviceTask.addOnFailed(event -> {
            Optional<AlertAction> result = Windows.showFingerprintDeviceError(event.getSource().getException());

            if (result.isPresent() && result.get() == AlertAction.TRY_AGAIN){
                establishFingerprintDevice();
            }
        });

        Threading.MAIN_EXECUTOR_SERVICE.submit(openDeviceTask);
    }

    public static void startFingerprintBackgroundService() throws IOException {
        fingerprintBackgroundServer = Runtime.getRuntime().exec("java -jar \"C:\\Program Files\\Huia Healthcare\\FingerprintBackgroundServer.jar\"");
    }

    public static void stopFingerprintBackgroundService() {
        fingerprintBackgroundServer.destroy();
    }

    public static LocalDateTime toLocalDateTime(String datetimeString) {
        if (datetimeString == null) return null;
        return LocalDateTime.parse(datetimeString, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    public static LocalDate toLocalDate(String datetimeString) {
        if (datetimeString == null) return null;
        return LocalDate.parse(datetimeString, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    public static LocalTime toLocalTime(String datetimeString) {
        if (datetimeString == null) return null;
        return LocalTime.parse(datetimeString, DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
    }

    public static long toTimestamp(LocalDateTime localDate){
        String dateString = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).format(localDate);

        long epochMillis = Timestamp.valueOf(dateString).getTime();
        return TimeUnit.SECONDS.convert(epochMillis, TimeUnit.MILLISECONDS);
    }

    public static long toTimestamp(LocalDate localDate){
        return toTimestamp(LocalDateTime.of(localDate, LocalTime.MIN));
    }

    public static LocalDateTime toLocalDateTime(long timestamp){
        long epochMillis = TimeUnit.MILLISECONDS.convert(timestamp, TimeUnit.SECONDS);
        return new Timestamp(epochMillis).toLocalDateTime();
    }

    public static LocalDate toLocalDate(long timestamp){
        return toLocalDateTime(timestamp).toLocalDate();
    }

    public static LocalTime toLocalTime(long timestamp){
        return toLocalDateTime(timestamp).toLocalTime();
    }

    public static void setTitledPaneDuration(Duration duration) throws NoSuchFieldException, IllegalAccessException {
        Field durationField = TitledPaneSkin.class.getField("TRANSITION_DURATION");

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(durationField, durationField.getModifiers() & ~Modifier.FINAL);

        durationField.setAccessible(true);
        durationField.set(TitledPaneSkin.class, duration);
    }

    public static <T> Callback<TableColumn<T, Integer>, TableCell<T, Integer>> getAutoNumberCellFactory() {
        return new Callback<TableColumn<T, Integer>, TableCell<T, Integer>>() {
            @Override
            public TableCell<T, Integer> call(TableColumn<T, Integer> param) {
                return new TableCell<T, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (!empty)
                            setText(String.valueOf(getIndex()+1));
                        else
                            setText("");
                    }
                };
            }
        };
    }
}
