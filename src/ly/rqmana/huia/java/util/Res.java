package ly.rqmana.huia.java.util;

import com.sun.istack.internal.NotNull;
import javafx.fxml.FXMLLoader;

import java.net.URL;

public final class Res {

    private final static String RES = "/ly/rqmana/huia/res/";
    private final static String CSS = RES + "css/";
    private final static String FONTS = RES + "fonts/";
    private final static String FXML = RES + "fxml/";
    private final static String DEVELOPER_FXML = "developer/";
    private final static String IMAGES = RES + "images/";
    private final static String ICONS = IMAGES + "icons/";

    // Temporary
    private final static String INSTALLATION = "installation/";
    private final static String ALERTS = "alerts/";

    private final static String AUTH = "auth/";

    private final static String SETTING = "settings/";

    public final static String LANGUAGE_PATH = "ly.rqmana.huia.res.strings.strings";


    public enum Fxml {
        ROOT_WINDOW             ("RootWindow.fxml"),
        MAIN_WINDOW             ("MainWindow.fxml"),
        HOME_WINDOW             ("HomeWindow.fxml"),
        REGISTRATION_WINDOW     ("RegistrationWindow.fxml"),
        LOGIN_DIALOG            ("LoginDialog.fxml"),
        SIGN_UP_WINDOW          ("SignUpWindow.fxml"),
        IDENTIFICATION_WINDOW   ("IdentificationWindow.fxml"),

        SETTINGS_WINDOW         (SETTING + "SettingsWindow.fxml"),
        DATABASE_SITTINGS_WINDOW(SETTING + "DatabaseSettingsWindow.fxml"),
        USERS_SITTINGS_WINDOW   (SETTING + "UsersSettingsWindow.fxml"),
        IDENTIFICATIONS_RECORDS("IdentificationsRecordsWindow.fxml"),
        IDENTIFICATIONS_EXPORT_REPORT_DIALOG("IdentificationsExportReportDialog.fxml"),

        ADD_CONTACTS_METHOD_WINDOW("AddContactMethodDialog.fxml"),
        PERSONAL_IMAGE_WINDOW("PersonalImageLoader.fxml"),
        ADD_NEW_USER_WINDOW(SETTING + "AddEditUserWindow.fxml"),
        PASSWORD_REENTER_VALIDATOR_DIALOG("PasswordReenterValidationDialog.fxml"),
        INSTALLATION_WINDOW(INSTALLATION + "InstallationWindow.fxml"),
        LICENSE_TERMS_PAGE(INSTALLATION + "License&TermsPage.fxml"),
        INSTALLATION_DIR_PAGE(INSTALLATION + "InstallationDirectoryPage.fxml"),
        HEALTH_CENTER_INFO_PAGE(INSTALLATION + "HealthCenterInfoPage.fxml"),
        INFO_DIALOG("AlertDialog.fxml"),
        ERROR_DIALOG("AlertDialog.fxml"),
        WARNING_DIALOG("AlertDialog.fxml"),

        CUSTOM_ALERT_LAYOUT     (ALERTS + "CustomAlertLayout.fxml"),
        INFO_ALERT_DETAILS_LAYOUT(ALERTS + "InfoAlertDetailsLayout.fxml"),

        ONE_TEXT_FIELD_ENTRY_DIALOG("OneTextFieldEntryDialog.fxml"),
        DATABASE_CONFIGURATION_DIALOG("DatabaseConfigurationDialog.fxml"),

        FINGERPRINT_TEST_WINDOW_D (DEVELOPER_FXML + "FingerprintTestWindow.fxml"),
        ;
        private final String url;
        Fxml(String url){
            this.url = url;
        }

        public URL getUrl() {
            return getClass().getResource(FXML + url);
        }

        @Override
        public String toString() {
            return url;
        }
        }


    public enum Stylesheet {

        TEMPLATES("templates.css"),
        THEME("theme.css"),
        DEFAULT_CUSTOM_COMBO_BOX_STYLE("default-custom-combo-box-style.css"),
        DEFAULT_TOGGLE_SWITCH_STYLE("default-toggle-switch-style.css"),
        DEFAULT_CONTACT_FIELD_STYLE("default-contact-field-style.css"),
        DEFAULT_CUSTOM_ALERT_STYLE("default-custom-alert-style.css")
        ;

        private final String url;
        Stylesheet(String url) {
            this.url = CSS + url;
        }

        @NotNull
        public String getUrl() {
            return getClass().getResource(url).toExternalForm();
        }
    }

    public enum Image {

        PERSON               (IMAGES + "person.png"),
        HOME_ADDRESS         (IMAGES + "address.png"),
        VIBER                (IMAGES + "viber.png"),
        CONTACTS             (IMAGES + "contacts.png"),
        SETTINGS             (IMAGES + "settings.png"),
        USERS_SETTINGS       (IMAGES + "users_settings.png"),
        MEMBERS_SETTINGS     (IMAGES + "members_settings.png"),
        ADD_IMAGE_ICON       (ICONS + "add_image_icon.png"),
        ARROW_ICON           (ICONS + "arrow_icon.png"),
        ATTENDANT_BUTTON_ICON(ICONS + "attendance_button_icon.png"),
        CHARACTER_SORT_ICON  (ICONS + "char_sort.png"),
        NUMBER_SORT_ICON     (ICONS + "number_sort.png"),
        CLUB_LOGO            (ICONS + "club_logo.png"),
        ERROR_LOGO           (ICONS + "error_logo.png"),
        MATCHES_BUTTON_LOGO  (ICONS + "matches_button_icon.png"),
        PLAYER_BUTTON_LOGO   (ICONS + "player_button_icon.png"),
        REPORT_BUTTON_LOGO   (ICONS + "reports_button_icon.png"),
        SEARCH_LOGO          (ICONS + "search_logo.png"),

        SORT_ICON            (ICONS + "sort_icon.png"),
//        ASCENDING_SORT_ICON  (ICONS + "ascending_sort_icon.png"),
//        DESCENDING_SORT_ICON (ICONS + "descending_sort_icon.png"),

        X_LOGO               (ICONS + "x_logo.png"),
        PENCIL_ICON          (ICONS + "pencil_icon.png"),
        PDF_LOGO             (ICONS + "pdf_logo.png"),
        ATTACH_ICON          (ICONS + "attach_icon.png"),
        IMAGE_LOGO           (ICONS + "image_logo.png"),

        FLOPPY_ICON          (ICONS + "floppy_icon.png"),

        FACEBOOK_ICON        (ICONS + "facebook_icon.png"),
        INSTAGRAM_ICON       (ICONS + "instagram_icon.png"),
        TWITTER_ICON         (ICONS + "twitter_icon.png"),
        ADDRESS_ICON         (ICONS + "address_icon.png"),

        GREEN_PERSON         (IMAGES + "green person.png"),

        HUIA_LOGO             (IMAGES + "huia_logo.png"),

        CRESTYANO            (IMAGES + "players_photos/Crestyano.jpg");



        final String url;
        Image(String url){
            this.url = url;
        }

        @NotNull
        public javafx.scene.image.Image getImage() {
            return new javafx.scene.image.Image(getClass().getResourceAsStream(url));
        }
    }

    public enum  Font {
        CAIRO_BLACK("Cairo/Cairo-Black.ttf"),
        CAIRO_BOLD("Cairo/Cairo-Bold.ttf"),
        CAIRO_EXTRA_LIGHT("Cairo/Cairo-ExtraLight.ttf"),
        CAIRO_LIGHT("Cairo/Cairo-Light.ttf"),
        CAIRO_REGULAR("Cairo/Cairo-Regular.ttf"),
        CAIRO_SEMI_BOLD("Cairo/Cairo-SemiBold.ttf"),
        ;

        final String url;

        Font(String url) {
            this.url = FONTS + url;
        }

        public String getUrl() {
            return url;
        }
    }
}
