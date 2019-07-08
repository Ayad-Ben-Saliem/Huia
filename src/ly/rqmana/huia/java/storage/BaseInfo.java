package ly.rqmana.huia.java.storage;

import ly.rqmana.huia.java.Main;
import ly.rqmana.huia.java.models.Location;

import java.util.prefs.Preferences;

public class BaseInfo {

    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(Main.class);

    private final static String LATITUDE_STRING_KEY = "LATITUDE";
    private final static String LONGITUDE_STRING_KEY = "LONGITUDE";
    private final static String ALTITUDE_STRING_KEY = "ALTITUDE";
    private final static String DATA_PATH_STRING_KEY = "DATA_PATH";
    private final static String DB_SERVER_HOST_STRING_KEY = "DB_SERVER_HOST";
    private final static String DB_USERNAME_STRING_KEY = "DB_USERNAME";
    private final static String DB_PASSWORD_STRING_KEY = "DB_PASSWORD";

    private static Location location;
    private static String dataPath;
    private static String dbServerHost;
    private static String dbUsername;
    private static String dbPassword;

    public static Location getLocation() {
        return location;
    }

    public static void setLocation(Location location) {
        BaseInfo.location = location;
    }

    public static String getDbServerHost() {
        if (dbServerHost == null || dbServerHost.isEmpty()) {
            dbServerHost = PREFERENCES.get(DB_SERVER_HOST_STRING_KEY, null);
        }
        return dbServerHost;
    }

    public static void setDbServerHost(String dbServerHost) {
        PREFERENCES.put(DB_SERVER_HOST_STRING_KEY, dbServerHost);
        BaseInfo.dbServerHost = dbServerHost;
    }

    public static String getDataPath() {
        return dataPath;
    }

    public static void setDataPath(String dataPath) {
        BaseInfo.dataPath = dataPath;
    }

    public static String getDbUsername() {
        if (dbUsername == null || dbUsername.isEmpty()) {
            dbUsername = PREFERENCES.get(DB_USERNAME_STRING_KEY, null);
        }
        return dbUsername;
    }

    public static void setDbUsername(String dbUsername) {
        PREFERENCES.put(DB_USERNAME_STRING_KEY, dbUsername);
        BaseInfo.dbUsername = dbUsername;
    }

    public static String getDbPassword() {
        if (dbPassword == null || dbPassword.isEmpty()) {
            dbPassword = PREFERENCES.get(DB_PASSWORD_STRING_KEY, null);
        }
        return dbPassword;
    }

    public static void setDbPassword(String dbPassword) {
        PREFERENCES.put(DB_PASSWORD_STRING_KEY, dbPassword);
        BaseInfo.dbPassword = dbPassword;
    }
}
