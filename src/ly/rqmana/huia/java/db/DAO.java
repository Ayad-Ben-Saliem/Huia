package ly.rqmana.huia.java.db;

import com.jfoenix.controls.JFXDialog;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Region;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.concurrent.Threading;
import ly.rqmana.huia.java.controllers.DatabaseConfigurationDialogController;
import ly.rqmana.huia.java.controllers.RootWindowController;
import ly.rqmana.huia.java.controls.alerts.AlertAction;
import ly.rqmana.huia.java.models.*;
import ly.rqmana.huia.java.security.Auth;
import ly.rqmana.huia.java.storage.BaseInfo;
import ly.rqmana.huia.java.storage.DataStorage;
import ly.rqmana.huia.java.util.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class DAO {

    private static Connection DB_CONNECTION;
    private static Connection OLD_DB_CONNECTION;
    private static final String DB_NAME = Utils.APP_NAME;

    public final static ObservableList<Subscriber> SUBSCRIBERS = FXCollections.observableArrayList();
    public final static ObservableList<IdentificationRecord> IDENTIFICATION_RECORDS = FXCollections.observableArrayList();
    public final static ObservableList<String> WORK_IDES = FXCollections.observableArrayList();

    public static void initialize() {
        Task<Boolean> initializeDBTask = initializeDBTask();
        RootWindowController rootWindowController = Windows.ROOT_WINDOW.getController();

        initializeDBTask.addOnSucceeded(event -> {
            rootWindowController.getHomeWindowController().loginBtn.setDisable(false);

            SUBSCRIBERS.addListener((ListChangeListener<Subscriber>) change -> {
                while (change.next()) {
                    if (change.wasRemoved() || change.wasPermutated()) {
                        continue;
                    }
                    try {
                        change.getAddedSubList().forEach(subscriber -> {
                            if (!WORK_IDES.contains(subscriber.getWorkId())) {
                                WORK_IDES.add(subscriber.getWorkId());
                            }
                        });
                    } catch (ConcurrentModificationException e) {
                        e.printStackTrace();
                    }
                }
            });

            Task<Collection<Subscriber>> fillSubscribersTask = fillSubscribers(SUBSCRIBERS);
            fillSubscribersTask.addOnFailed(event1 -> {
                Windows.errorAlert(
                        Utils.getI18nString("ERROR"),
                        fillSubscribersTask.getException().getMessage(),
                        fillSubscribersTask.getException(),
                        AlertAction.OK
                );
            });
            Threading.MAIN_EXECUTOR_SERVICE.submit(fillSubscribersTask);


            // TODO: This should remove
            Task<Collection<Subscriber>> fillNewSubscribersTask = fillNewSubscribers(SUBSCRIBERS);
            fillNewSubscribersTask.addOnFailed(event1 -> {
                Windows.errorAlert(
                        Utils.getI18nString("ERROR"),
                        fillNewSubscribersTask.getException().getMessage(),
                        fillNewSubscribersTask.getException(),
                        AlertAction.OK
                );
            });
            Threading.MAIN_EXECUTOR_SERVICE.submit(fillNewSubscribersTask);

            Task<Collection<IdentificationRecord>> fillIdentificationRecordsTask = fillIdentificationRecords(IDENTIFICATION_RECORDS);
            fillIdentificationRecordsTask.addOnFailed(event1 -> {
                Windows.errorAlert(
                        Utils.getI18nString("ERROR"),
                        fillIdentificationRecordsTask.getException().getMessage(),
                        fillIdentificationRecordsTask.getException(),
                        AlertAction.OK
                );
            });
            Threading.MAIN_EXECUTOR_SERVICE.submit(fillIdentificationRecordsTask);
        });

        initializeDBTask.setOnFailed(event -> {
            Throwable throwable = initializeDBTask.getException();

            AlertAction configureDB = new AlertAction(Utils.getI18nString("DB_CONFIG"));

            Optional<AlertAction> answer = Windows.errorAlert(
                    Utils.getI18nString("ERROR"),
                    throwable.getMessage(),
                    throwable,
                    AlertAction.OK,
                    AlertAction.TRY_AGAIN,
                    configureDB
            );

            if (answer.isPresent()){
                if (answer.get().equals(AlertAction.TRY_AGAIN)) {
                    initialize();
                }
                if (answer.get().equals(configureDB)) {
                    try {
                        FXMLLoader loader = new FXMLLoader(Res.Fxml.DATABASE_CONFIGURATION_DIALOG.getUrl(), Utils.getBundle());
                        Region content = loader.load();
                        JFXDialog dialog = new JFXDialog(Windows.getRootStack(), content, JFXDialog.DialogTransition.CENTER);
                        DatabaseConfigurationDialogController controller = loader.getController();
                        controller.setDialog(dialog);
                        dialog.show();
                        dialog.setOnDialogClosed(event1 -> initialize());

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        initializeDBTask.runningProperty().addListener((observable, oldValue, newValue) -> rootWindowController.updateLoadingView(newValue));

        Threading.MAIN_EXECUTOR_SERVICE.submit(initializeDBTask);
    }

//    public static Connection getConnection() {
//        if (DB_CONNECTION.isClosed()) {
//
//        }
//        return DB_CONNECTION;
//    }

    private static Task<Boolean> initializeDBTask() {

        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                String dbUrl = getDBUrl();
                String dbUsername = BaseInfo.getDbUsername();
                String dbPassword = BaseInfo.getDbPassword();

                // Just to ensure that the date is correct
                Thread.sleep(6000);

                Class.forName("com.mysql.jdbc.Driver");
                Class.forName("org.sqlite.JDBC");

                DB_CONNECTION = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
                OLD_DB_CONNECTION = DriverManager.getConnection(DAO.getOldDBUrl());

                Statement statement = DB_CONNECTION.createStatement();
                statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + DB_NAME + "`;");
                statement.executeUpdate("USE `" + DB_NAME + "`;");
                statement.executeUpdate("ALTER DATABASE `" + DB_NAME + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");

                createTables();

                if (countRecords("Users").runAndGet() == 0) {
                    insertAdminUser().runAndGet();
                }

                if (countRecords("Institutes").runAndGet() == 0) {
                    insertInstitute("الشركة الليبية للحديد والصلب").runAndGet();
                }

                if (countRecords("Subscribers").runAndGet() == 0) {
                    migrateOldData().runAndGet();
                }
                return true;
            }
        };
    }

    private static String getDBUrl() {
        String dbUrl = "jdbc:mysql://" + BaseInfo.getDbServerHost();
        if (!dbUrl.endsWith("/"))
            dbUrl += "/";
        dbUrl += "?useUnicode=true&characterEncoding=utf-8";
        return dbUrl;
    }

    private static Task<Boolean> migrateOldData() {

        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                ResultSet oldDataSet = OLD_DB_CONNECTION.createStatement().executeQuery("SELECT * FROM Fingerprint where company_id ='02';");

                String insertQuery = "INSERT INTO Subscribers("
                                    + "firstName        ,"
                                    + "fatherName           ,"
                                    + "familyName           ,"
                                    + "nationalId           ,"
                                    + "birthday             ,"
                                    + "gender               ,"
                                    + "instituteId          ,"
                                    + "workId               ,"
                                    + "relationship         ,"
                                    + "isActive             ,"
                                    + "allFingerprintTemplates ,"
                                    + "dateAdded ,"
                                    + "user,"
                                    + "notes"
                                    + ") "
                                    + "Values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                PreparedStatement pStatement = DB_CONNECTION.prepareStatement(insertQuery);

                while (oldDataSet.next()) {

                    String firstName = oldDataSet.getString("first_name");
                    String fatherName = oldDataSet.getString("father_name");
                    String familyName = oldDataSet.getString("last_name");
                    String nationalId = oldDataSet.getString("national_id");

                    String birthdayString = oldDataSet.getString("birthday");
                    LocalDate birthday = Utils.toLocalDate(birthdayString);

                    String genderString = oldDataSet.getString("sex");

                    Gender gender;
                    gender = genderString.equals("M") ? Gender.MALE: Gender.FEMALE;

                    String allFingerprintsTemplates = oldDataSet.getString("fingerprint_template");
                    String workId = oldDataSet.getString("work_id");

                    String relationshipRawString = oldDataSet.getString("relationship");

                    String relationship;
                    if (relationshipRawString.equals("خاص")) {
                        relationship = "SPECIAL";
                    } else
                    if (relationshipRawString.equals("لا يوجد")) {
                        relationship = "NOT EXIST";
                    } else {
                        relationship = Relationship.parseArabic(relationshipRawString).name();
                    }

                    String dateAdded = oldDataSet.getString("start_date");

                    boolean isActive = SQLUtils.getBoolean(oldDataSet.getString("is_active"));
                    String notes = oldDataSet.getString("notes");

                    pStatement.setString(1, firstName);
                    pStatement.setString(2, fatherName);
                    pStatement.setString(3, familyName);

                    pStatement.setString(4, nationalId);
                    pStatement.setString(5, birthday.toString());
                    pStatement.setString(6, gender.name());
                    //TODO: institute id set to 1
                    pStatement.setLong(7, 1);
                    pStatement.setString(8, workId);
                    pStatement.setString(9, relationship);
                    pStatement.setBoolean(10, isActive);
                    pStatement.setString(11, allFingerprintsTemplates);
                    pStatement.setString(12, dateAdded);

                    pStatement.setLong(13, 1);
                    pStatement.setString(14, notes);

//                    pStatement.execute();
                    pStatement.addBatch();
        //            "select * from Fingerprint where relationship!='الزوجةالإبن' and relationship!='المشترك' and relationship!='الإبنة' and relationship!='والدة الموظف' and relationship!='والد الموظف' and relationship!='لا يوجد'";
                }

                DB_CONNECTION.setAutoCommit(false);
                pStatement.executeBatch();
                DB_CONNECTION.commit();
                DB_CONNECTION.setAutoCommit(true);
                return true;
            }
        };
    }

    private static void createTables() throws SQLException {
        createUsersTable();
        createInstitutesTable();
        createSubscribersTable();
        createNewSubscribersTable();
        createContactsTable();
        createIdentificationsTable();
    }

    private static void createIdentificationsTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS Identifications("
                + "id                   INT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                + "subscriberId         INTEGER NOT NULL,"
                + "datetime             DATETIME NOT NULL,"
                + "isIdentified         BOOLEAN NOT NULL ,"
                + "username             VARCHAR(25),"
                + "notes                TEXT"
                + ");";
        statement.execute(createQuery);

        statement.executeUpdate("ALTER TABLE Identifications CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        statement.close();

    }

    private static void createUsersTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS Users("
                + "id                INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                + "username          VARCHAR(25) NOT NULL UNIQUE,"
                + "password          TEXT,"
                + "email             VARCHAR(50),"
                + "firstName         VARCHAR(25) NOT NULL,"
                + "fatherName        VARCHAR(25),"
                + "grandfatherName   VARCHAR(25),"
                + "familyName        VARCHAR(25),"
                + "nationality       VARCHAR(25),"
                + "nationalId        VARCHAR(25),"
                + "birthday          DATETIME,"
                + "gender            ENUM ('MALE', 'FEMALE') NOT NULL,"
                + "passport          VARCHAR(25),"
                + "familyId          VARCHAR(25),"
                + "residence         VARCHAR(25),"
                + "dateJoined        DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "isSuperuser       BOOLEAN NOT NULL,"
                + "isStaff           BOOLEAN NOT NULL,"
                + "isActive          BOOLEAN NOT NULL,"
                + "lastLogin         DATETIME"
                + ");";
        statement.execute(createQuery);

        statement.executeUpdate("ALTER TABLE Users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        statement.close();
    }

    private static void createInstitutesTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS Institutes("
                + "id           INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                + "name         VARCHAR(100) NOT NULL,"
                + "description  TEXT"
                + ");";
        statement.execute(createQuery);

        statement.executeUpdate("ALTER TABLE Institutes CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        statement.close();
    }

    private static void createSubscribersTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS Subscribers"
                + "(id                      INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                + "firstName                VARCHAR(25) NOT NULL,"
                + "fatherName               VARCHAR(25),"
                + "grandfatherName          VARCHAR(25),"
                + "familyName               VARCHAR(25) NOT NULL,"
                + "nationality              VARCHAR(25),"
                + "nationalId               VARCHAR(25),"
                + "birthday                 DATE NOT NULL ,"
                + "gender                   ENUM ('MALE', 'FEMALE') NOT NULL,"
                + "passport                 VARCHAR(25) UNIQUE,"
                + "familyId                 VARCHAR(25),"
                + "residence                VARCHAR(25),"
                + "instituteId              INTEGER NOT NULL,"
                + "workId                   VARCHAR(10),"
                + "relationship             ENUM('SUBSCRIBER', 'FATHER', 'MOTHER', 'SON', 'DAUGHTER', 'WIFE', 'HUSBAND', 'SPECIAL', 'NOT EXIST') NOT NULL,"
                + "isActive                 BOOLEAN,"
                + "rightThumbFingerprint    TEXT,"
                + "rightIndexFingerprint    TEXT,"
                + "rightMiddleFingerprint   TEXT,"
                + "rightRingFingerprint     TEXT,"
                + "rightLittleFingerprint   TEXT,"
                + "leftThumbFingerprint     TEXT,"
                + "leftIndexFingerprint     TEXT,"
                + "leftMiddleFingerprint    TEXT,"
                + "leftRingFingerprint      TEXT,"
                + "leftLittleFingerprint    TEXT,"
                + "allFingerprintTemplates  TEXT,"
                + "dataPath                 TEXT,"
                + "user                     INTEGER NOT NULL,"
                + "dateAdded                DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "dateUploaded             DATETIME,"
                + "notes                    TEXT,"
                + "FOREIGN KEY (instituteId) REFERENCES Institutes(id),"
                + "FOREIGN KEY (user) REFERENCES Users(id)"
                + ");";
        statement.execute(createQuery);

        statement.executeUpdate("ALTER TABLE Subscribers CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        statement.close();
    }

    private static void createNewSubscribersTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS NewSubscribers ("
                + "id                       INTEGER PRIMARY KEY AUTO_INCREMENT,"
                + "firstName                VARCHAR(25) NOT NULL,"
                + "fatherName               VARCHAR(25),"
                + "grandfatherName          VARCHAR(25),"
                + "familyName               VARCHAR(25) NOT NULL,"
                + "nationality              VARCHAR(25),"
                + "nationalId               VARCHAR(25),"
                + "birthday                 DATE NOT NULL,"
                + "gender                   ENUM ('MALE', 'FEMALE') NOT NULL,"
                + "passport                 VARCHAR(25) UNIQUE,"
                + "familyId                 VARCHAR(25),"
                + "residence                VARCHAR(25),"
                + "instituteId              INTEGER NOT NULL,"
                + "workId                   VARCHAR(10),"
                + "relationship             ENUM('SUBSCRIBER', 'FATHER', 'MOTHER', 'SON', 'DAUGHTER', 'WIFE', 'HUSBAND', 'SPECIAL', 'NOT EXIST') NOT NULL,"
                + "rightThumbFingerprint    TEXT,"
                + "rightIndexFingerprint    TEXT,"
                + "rightMiddleFingerprint   TEXT,"
                + "rightRingFingerprint     TEXT,"
                + "rightLittleFingerprint   TEXT,"
                + "leftThumbFingerprint     TEXT,"
                + "leftIndexFingerprint     TEXT,"
                + "leftMiddleFingerprint    TEXT,"
                + "leftRingFingerprint      TEXT,"
                + "leftLittleFingerprint    TEXT,"
                + "allFingerprintTemplates  TEXT,"
                + "dataPath                 TEXT,"
                + "isActive                 BOOLEAN,"
                + "user                     INTEGER NOT NULL,"
                + "dateAdded                DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "isUploaded               BOOLEAN NOT NULL DEFAULT FALSE,"
                + "isViewed                 BOOLEAN NOT NULL DEFAULT FALSE,"
                + "dateUploaded             DATETIME,"
                + "hasProblem               BOOLEAN DEFAULT FALSE,"
                + "notes                    TEXT,"
                + "FOREIGN KEY (instituteId) REFERENCES Institutes(id),"
                + "FOREIGN KEY (user) REFERENCES Users(id)"
                + ");";
        statement.execute(createQuery);

        statement.executeUpdate("ALTER TABLE NewSubscribers CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        statement.close();
    }

    private static void createContactsTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS Contacts ("
                + "id          INTEGER PRIMARY KEY AUTO_INCREMENT,"
                + "personId    INTEGER NOT NULL,"
                + "label       VARCHAR(25),"
                + "contact     VARCHAR(250),"
                + "FOREIGN KEY (personId) REFERENCES Subscribers(id)"
                + ");";
        statement.execute(createQuery);

        statement.executeUpdate("ALTER TABLE Contacts CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        statement.close();
    }

    public static Task<Long> countRecords(String tableName){
        return new Task<Long>() {
            @Override
            protected Long call() throws Exception {
                Statement statement = DB_CONNECTION.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT Count(*) FROM " + tableName);
                long count = -1;
                while (resultSet.next()) {
                    count = resultSet.getLong(1);
                }
                return count;
            }
        };
    }

    private static Task<Boolean> insertAdminUser() {
        return new Task<Boolean>(){
            @Override
            protected Boolean call() throws Exception {

                    User adminUser = new User();
                    adminUser.setUsername("Admin");
                    adminUser.setPassword("Admin");
                    adminUser.setFirstName("Admin");
                    adminUser.setGender(Gender.MALE);
                    adminUser.setSuperuser(true);
                    adminUser.setStaff(true);
                    adminUser.setActive(true);

                    if (! userExits(adminUser.getUsername()).runAndGet()) {
                        return insertUser(adminUser).runAndGet();
                    }
                    return false;
                };
            };
    }

    public static Task<Boolean> insertUser(User user) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                String insertQuery = "INSERT INTO Users (" +
                        "username," +
                        "password," +
                        "email," +
                        "firstName," +
                        "fatherName," +
                        "grandfatherName," +
                        "familyName," +
                        "nationality," +
                        "nationalId," +
                        "birthday," +
                        "gender," +
                        "passport," +
                        "familyId," +
                        "residence," +
                        "dateJoined," +
                        "isSuperuser," +
                        "isStaff," +
                        "isActive" +
                        ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

                PreparedStatement pStatement = DB_CONNECTION.prepareStatement(insertQuery);

                pStatement.setString(1, user.getUsername());
                pStatement.setString(2, user.getHashedPassword());
                pStatement.setString(3, user.getEmail());
                pStatement.setString(4, user.getFirstName());
                pStatement.setString(5, user.getFatherName());
                pStatement.setString(6, user.getGrandfatherName());
                pStatement.setString(7, user.getFamilyName());
                pStatement.setString(8, user.getNationality());
                pStatement.setString(9, user.getNationalId());
                String birthday = user.getBirthday()==null? null : user.getBirthday().toString();
                pStatement.setString(10, birthday);
                String gender = user.getGender()==null? null : user.getGender().name();
                pStatement.setString(11, gender);
                pStatement.setString(12, user.getPassportNumber());
                pStatement.setString(13, user.getFamilyId());
                pStatement.setString(14, user.getResidence());
                pStatement.setString(15, LocalDateTime.now().toString());
                pStatement.setBoolean(16, user.isSuperuser());
                pStatement.setBoolean(17, user.isStaff());
                pStatement.setBoolean(18, user.isActive());

                pStatement.execute();
                pStatement.close();
                return true;
            }
        };
    }

    public static Task<Boolean> userExits(String username){
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Statement statement = DB_CONNECTION.createStatement();
                ResultSet resultSet = statement.executeQuery(String.format("SELECT id FROM Users WHERE username = '%s'", username));
                return resultSet.next();
            }
        };
    }

    private static Task<Void> insertInstitute(String institute) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Statement statement = DB_CONNECTION.createStatement();

                String insertQuery = "INSERT INTO Institutes (name) VALUES ('" + institute + "');";
                statement.execute(insertQuery);

                statement.close();
                return null;
            }
        };
    }

    private static final String DATA_DB_NAME = "FingerprintData.db";

    private static String getOldDBUrl() {
        return "jdbc:sqlite:" + DataStorage.getDataPath().resolve(DATA_DB_NAME).toString();
    }

    public static User getUserByUsername(String username) throws SQLException {
        String query = "SELECT   " +
                "id             ," +
                "username       ," +
                "password       ," +
                "email          ," +
                "firstName      ," +
                "fatherName     ," +
                "grandfatherName," +
                "familyName     ," +
                "nationality    ," +
                "nationalId     ," +
                "birthday       ," +
                "gender         ," +
                "dateJoined     ," +
                "isSuperuser    ," +
                "isStaff        ," +
                "isActive       ," +
                "lastLogin       " +
                "FROM Users WHERE username=?";

        PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(query);
        pStatement.setString(1, username);
        ResultSet resultSet = pStatement.executeQuery();

        User user = null;
        if (resultSet.next()) {
            user = new User();
            user.setId(resultSet.getLong("id"));
            user.setUsername(resultSet.getString("username"));
            user.setHashedPassword(resultSet.getString("password"));
            user.setEmail(resultSet.getString("email"));
            user.setFirstName(resultSet.getString("firstName"));
            user.setFatherName(resultSet.getString("fatherName"));
            user.setGrandfatherName(resultSet.getString("grandfatherName"));
            user.setFamilyName(resultSet.getString("familyName"));
            user.setNationality(resultSet.getString("nationality"));
            user.setNationalId(resultSet.getString("nationalId"));
            user.setBirthday(Utils.toLocalDate(resultSet.getString("birthday")));
            String gender = resultSet.getString("gender");
            user.setGender(Gender.MALE.name().equalsIgnoreCase(gender)? Gender.MALE : Gender.FEMALE);
            user.setDateJoined(Utils.toLocalDateTime(resultSet.getString("dateJoined")));
            user.setSuperuser(resultSet.getBoolean("isSuperuser"));
            user.setStaff(resultSet.getBoolean("isStaff"));
            user.setActive(resultSet.getBoolean("isActive"));
            user.setLastLogin(Utils.toLocalDateTime(resultSet.getString("lastLogin")));
        }
        return user;
    }

    public static Task<Boolean> updateUser(User user, String field, Object value) throws SQLException {
        if (user.getId() > 0)
            return updateUserById(user.getId(), field, value);
        else if (user.getUsername() != null && !user.getUsername().isEmpty())
            return updateUserByUsername(user.getUsername(), field, value);
        else
            throw new RuntimeException("Wrong User");
    }

    public static Task<Boolean> updateUser(User user, Map<String, Object> updateMap)  {
        if (user.getId() > 0)
            return updateUserById(user.getId(), updateMap);
        else if (user.getUsername() != null && !user.getUsername().isEmpty())
            return updateUserByUsername(user.getUsername(), updateMap);
        else
            throw new RuntimeException("Wrong User");
    }

    public static Task<Boolean> updateUserById(long userId, String field, Object value) {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put(field, value);
        return updateUserById(userId, updateMap);
    }

    public static Task<Boolean> updateUserById(long userId, Map<String, Object> updateMap) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Map<String, Object> filterMap = new HashMap<>();
                filterMap.put("id", userId);
                PreparedStatement pStatement = prepareUpdateStatement("Users", updateMap, filterMap);
                pStatement.execute();

                return true;
            }
        };
    }

    public static Task<Boolean> updateUserByUsername(String username, String field, Object value) throws SQLException {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put(field, value);
        return updateUserByUsername(username, updateMap);
    }

    public static Task<Boolean> updateUserByUsername(String username, Map<String, Object> updateMap) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Map<String, Object> filterMap = new HashMap<>();
                filterMap.put("username", username);
                PreparedStatement pStatement = prepareUpdateStatement("Users", updateMap, filterMap);
                pStatement.execute();

                return true;
            }
        };
    }

    public static Task<Collection<Subscriber>> getSubscribers() throws SQLException {
        return getSubscribers("Subscribers");
    }

    public static Task<Collection<Subscriber>> getNewSubscribers() throws SQLException {
        return getSubscribers("NewSubscribers");
    }

    public static Task<Collection<Subscriber>> fillSubscribers(Collection<Subscriber> subscribers) {
        return fillSubscribers("Subscribers", subscribers);
    }

    public static Task<Collection<Subscriber>> fillNewSubscribers(Collection<Subscriber> subscribers) {
        return fillSubscribers("NewSubscribers", subscribers);
    }

    private static Task<Collection<Subscriber>> getSubscribers(String tableName) {
        return fillSubscribers(tableName, FXCollections.observableArrayList());
    }

    private static Task<Collection<Subscriber>> fillSubscribers(String tableName, Collection<Subscriber> subscribers) {
        return new Task<Collection<Subscriber>>() {
            @Override
            protected Collection<Subscriber> call() throws Exception {
                @SuppressWarnings("SqlResolve")
                String query = "SELECT " +
                        tableName + ".id," +
                        "firstName," +
                        "fatherName," +
                        "grandfatherName," +
                        "instituteId, " +
                        "Institutes.name AS instituteName, "+
                        "familyName," +
                        "birthday," +
                        "nationalId," +
                        "familyId," +
                        "gender," +
                        "workId," +
                        "relationship," +
                        "allFingerprintTemplates," +
                        "rightThumbFingerprint," +
                        "rightIndexFingerprint," +
                        "rightMiddleFingerprint," +
                        "rightRingFingerprint," +
                        "rightLittleFingerprint," +
                        "leftThumbFingerprint," +
                        "leftIndexFingerprint," +
                        "leftMiddleFingerprint," +
                        "leftRingFingerprint," +
                        "leftLittleFingerprint," +
                        "dataPath," +
                        "isActive" +
                        " FROM " + tableName +
                        " INNER JOIN Institutes ON Institutes.id = "+ tableName + ".instituteId;";

                ResultSet resultSet = DB_CONNECTION.createStatement().executeQuery(query);

                while (resultSet.next()) {
                    Subscriber subscriber = new Subscriber();

                    subscriber.setId(resultSet.getLong("id"));

                    subscriber.setFirstName(resultSet.getString("firstName"));
                    subscriber.setFatherName(resultSet.getString("fatherName"));
                    subscriber.setGrandfatherName(resultSet.getString("grandfatherName"));
                    subscriber.setFamilyName(resultSet.getString("familyName"));

                    String birthday = resultSet.getString("birthday");
                    subscriber.setBirthday(birthday==null? null : Utils.toLocalDate(birthday));
                    subscriber.setNationalId(resultSet.getString("nationalId"));
                    subscriber.setGender(Gender.valueOf(resultSet.getString("gender")));


                    long instituteId = resultSet.getLong("instituteId");
                    String instituteName = resultSet.getString("instituteName");
                    subscriber.setInstitute(new Institute(instituteId, instituteName));

                    subscriber.setWorkId(resultSet.getString("workId"));
                    subscriber.setRelationship(Relationship.parse(resultSet.getString("relationship")));

                    subscriber.setAllFingerprintsTemplate(resultSet.getString("allFingerprintTemplates"));

                    subscriber.setRightThumbFingerprint(resultSet.getString("rightThumbFingerprint"));
                    subscriber.setRightIndexFingerprint(resultSet.getString("rightIndexFingerprint"));
                    subscriber.setRightMiddleFingerprint(resultSet.getString("rightMiddleFingerprint"));
                    subscriber.setRightRingFingerprint(resultSet.getString("rightRingFingerprint"));
                    subscriber.setRightLittleFingerprint(resultSet.getString("rightLittleFingerprint"));

                    subscriber.setLeftThumbFingerprint(resultSet.getString("leftThumbFingerprint"));
                    subscriber.setLeftIndexFingerprint(resultSet.getString("leftIndexFingerprint"));
                    subscriber.setLeftMiddleFingerprint(resultSet.getString("leftMiddleFingerprint"));
                    subscriber.setLeftRingFingerprint(resultSet.getString("leftRingFingerprint"));
                    subscriber.setLeftLittleFingerprint(resultSet.getString("leftLittleFingerprint"));

                    subscriber.setDataPath(resultSet.getString("dataPath"));

                    subscriber.setActive(resultSet.getBoolean("isActive"));

                    subscribers.add(subscriber);
                }
                return subscribers;
            }
        };
    }

    public static Task<Subscriber> getSubscriberById(long subscriberId)  {
        return new Task<Subscriber>() {
            @Override
            protected Subscriber call() throws Exception {
                PreparedStatement pStatement = DB_CONNECTION.prepareStatement("SELECT * FROM Subscribers WHERE id=?;");
                pStatement.setLong(1, subscriberId);
                ResultSet resultSet = pStatement.executeQuery();
                if (resultSet.next()) {
                    Subscriber subscriber = new Subscriber();

                    subscriber.setId(subscriberId);
                    subscriber.setFirstName(resultSet.getString("firstName"));
                    subscriber.setFirstName(resultSet.getString("workId"));

                    return subscriber;
                }
                return null;
            }
        };
    }

    public static Task<ObservableList<Subscriber>> getSubscribersByIdes(ObservableList<Long> subscribersByIdes)  {
        return new Task<ObservableList<Subscriber>>() {
            @Override
            protected ObservableList<Subscriber> call() throws Exception {
                final ObservableList<Subscriber> result = FXCollections.observableArrayList();
                subscribersByIdes.forEach(subscriberById -> {
                    result.add(getSubscriberById(subscriberById).runAndGet());
                });
                return result;
            }
        };
    }

    public static long insertSubscriber(Subscriber subscriber) throws SQLException {
        final String INSERT_QUERY =
                "INSERT INTO Subscribers ("
                        + "firstName,"
                        + "fatherName,"
                        + "grandfatherName,"
                        + "familyName,"
                        + "nationality,"
                        + "nationalId,"
                        + "birthday,"
                        + "gender,"
                        + "instituteId,"
                        + "familyId,"
                        + "residence,"
                        + "passport,"
                        + "workId,"
                        + "relationship,"
                        + "rightThumbFingerprint,"
                        + "rightIndexFingerprint,"
                        + "rightMiddleFingerprint,"
                        + "rightRingFingerprint,"
                        + "rightLittleFingerprint,"
                        + "leftThumbFingerprint,"
                        + "leftIndexFingerprint,"
                        + "leftMiddleFingerprint,"
                        + "leftRingFingerprint,"
                        + "leftLittleFingerprint,"
                        + "allFingerprintTemplates,"
                        + "user"
                        + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        pStatement.setString(1, subscriber.getFirstName());
        pStatement.setString(2, subscriber.getFatherName());
        pStatement.setString(3, subscriber.getGrandfatherName());
        pStatement.setString(4, subscriber.getFamilyName());
        pStatement.setString(5, subscriber.getNationality());
        pStatement.setString(6, subscriber.getNationalId());
        LocalDate birthday = subscriber.getBirthday();
        pStatement.setString(7, birthday==null? null : birthday.toString());
        pStatement.setString(8, subscriber.getGender().name());
        pStatement.setLong(9, subscriber.getInstitute().getId());

        pStatement.setString(10, subscriber.getFamilyId());
        pStatement.setString(11, subscriber.getResidence());
        pStatement.setString(12, subscriber.getPassport().getNumber());

        pStatement.setString(13, subscriber.getWorkId());
        pStatement.setString(14, subscriber.getRelationship().name());

        pStatement.setString(15, subscriber.getRightThumbFingerprint());
        pStatement.setString(16, subscriber.getRightIndexFingerprint());
        pStatement.setString(17, subscriber.getRightMiddleFingerprint());
        pStatement.setString(18, subscriber.getRightRingFingerprint());
        pStatement.setString(19, subscriber.getRightLittleFingerprint());

        pStatement.setString(20, subscriber.getLeftThumbFingerprint());
        pStatement.setString(21, subscriber.getLeftIndexFingerprint());
        pStatement.setString(22, subscriber.getLeftMiddleFingerprint());
        pStatement.setString(23, subscriber.getLeftRingFingerprint());
        pStatement.setString(24, subscriber.getLeftLittleFingerprint());

        pStatement.setString(25, subscriber.getAllFingerprintsTemplate());

        pStatement.setLong(26, Auth.getCurrentUser().getId());
        pStatement.executeUpdate();

        ResultSet generatedKeys = pStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getLong(1);
        }
        return -1;
    }

    public static long insertNewSubscriber(Subscriber subscriber) throws SQLException {
        final String INSERT_QUERY =
                "INSERT INTO NewSubscribers ("
                        + "firstName,"
                        + "fatherName,"
                        + "grandfatherName,"
                        + "familyName,"
                        + "nationality,"
                        + "nationalId,"
                        + "birthday,"
                        + "gender,"
                        + "instituteId,"
                        + "familyId,"
                        + "residence,"
                        + "passport,"
                        + "workId,"
                        + "relationship,"
                        + "rightThumbFingerprint,"
                        + "rightIndexFingerprint,"
                        + "rightMiddleFingerprint,"
                        + "rightRingFingerprint,"
                        + "rightLittleFingerprint,"
                        + "leftThumbFingerprint,"
                        + "leftIndexFingerprint,"
                        + "leftMiddleFingerprint,"
                        + "leftRingFingerprint,"
                        + "leftLittleFingerprint,"
                        + "allFingerprintTemplates,"
                        + "user"
                        + ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);

        pStatement.setString(1, subscriber.getFirstName());
        pStatement.setString(2, subscriber.getFatherName());
        pStatement.setString(3, subscriber.getGrandfatherName());
        pStatement.setString(4, subscriber.getFamilyName());
        pStatement.setString(5, subscriber.getNationality());
        pStatement.setString(6, subscriber.getNationalId());
        LocalDate birthday = subscriber.getBirthday();
        pStatement.setString(7, birthday==null? null : birthday.toString());
        pStatement.setString(8, subscriber.getGender().name());
        pStatement.setLong(9, subscriber.getInstitute().getId());

        pStatement.setString(10, subscriber.getFamilyId());
        pStatement.setString(11, subscriber.getResidence());
        pStatement.setString(12, subscriber.getPassport().getNumber());

        pStatement.setString(13, subscriber.getWorkId());
        pStatement.setString(14, subscriber.getRelationship().name());

        pStatement.setString(15, subscriber.getRightThumbFingerprint());
        pStatement.setString(16, subscriber.getRightIndexFingerprint());
        pStatement.setString(17, subscriber.getRightMiddleFingerprint());
        pStatement.setString(18, subscriber.getRightRingFingerprint());
        pStatement.setString(19, subscriber.getRightLittleFingerprint());

        pStatement.setString(20, subscriber.getLeftThumbFingerprint());
        pStatement.setString(21, subscriber.getLeftIndexFingerprint());
        pStatement.setString(22, subscriber.getLeftMiddleFingerprint());
        pStatement.setString(23, subscriber.getLeftRingFingerprint());
        pStatement.setString(24, subscriber.getLeftLittleFingerprint());

        pStatement.setString(25, subscriber.getAllFingerprintsTemplate());

        pStatement.setLong(26, Auth.getCurrentUser().getId());
        pStatement.executeUpdate();

        ResultSet generatedKeys = pStatement.getGeneratedKeys();
        if (generatedKeys.next()) {
            return generatedKeys.getLong(1);
        }
        return -1;
    }

    public static Task<Boolean> updateSubscriberById(long subscriberId, Map<String, Object> updateMap){
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                Map<String, Object> filterMap = new HashMap<>();
                filterMap.put("id", subscriberId);

                PreparedStatement preparedStatement = prepareUpdateStatement("Subscribers", updateMap, filterMap);

                preparedStatement.execute();
                return true;
            }
        };
    }

    public static void updateSubscriberDataPath(Subscriber subscriber) throws SQLException {
        PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement("UPDATE NewSubscribers SET dataPath=? WHERE id=?");
        pStatement.setString(1, subscriber.getDataPath());
        pStatement.setLong(2, subscriber.getId());

        pStatement.executeUpdate();
    }

    public static Task<Long> insertIdentificationRecord(IdentificationRecord record) {
        return new Task<Long>() {
            @Override
            protected Long call() throws Exception {
                final String insertQuery
                        = "INSERT INTO Identifications ("
                        + "subscriberId,"
                        + "username,"
                        + "isIdentified,"
                        + "datetime,"
                        + "notes"
                        + ") VALUES (?,?,?,?,?)";

                PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);

                pStatement.setLong(1, record.getSubscriber().getId());
                pStatement.setString(2, record.getUser().getUsername());
                pStatement.setBoolean(3, record.isIdentified());
                pStatement.setString(4, record.getDatetime().toString());
                String notes = "";
                if (!record.getSubscriber().isActive())
                    notes += Utils.getI18nString("SUBSCRIBER_NOT_ACTIVE");
                if (!record.getSubscriber().hasFingerprint())
                    notes += Utils.getI18nString("FINGERPRINT_NOT_REGISTERED");
                pStatement.setString(5, notes);

                pStatement.execute();

                ResultSet generatedKeys = pStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    record.setId(generatedKeys.getLong(1));
                }
                return record.getId();
            }
        };
    }

    public static Task<Boolean> updateIdentificationRecord(IdentificationRecord record, String ... fields) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                Map<String, Object> updateMap = new HashMap<>();
                for (String field : fields) {
                    switch (field) {
                        case "isIdentified":
                            updateMap.put(field, record.isIdentified());
                            break;
                        case "notes":
                            updateMap.put(field, record.getNotes());
                            break;
                    }
                }

//                System.out.println("updateMap = " + updateMap);

                Map<String, Object> filterMap = new HashMap<>();
                filterMap.put("id", record.getId());

                PreparedStatement pStatement = prepareUpdateStatement("Identifications", updateMap, filterMap);

                pStatement.execute();

                return true;
            }
        };
    }

    public static Task<Collection<User>> getAllUsers() {
        return new Task<Collection<User>>() {
            @Override
            protected Collection<User> call() throws Exception {
                Collection<User> resultUsers = new ArrayList<>();
                String query = "SELECT * FROM Users;";
                PreparedStatement pStatement = DB_CONNECTION.prepareStatement(query);
                ResultSet resultSet = pStatement.executeQuery();
                while (resultSet.next()) {
                    User user = new User();
                    user.setId(resultSet.getLong("id"));
                    user.setUsername(resultSet.getString("username"));
                    user.setPassword(resultSet.getString("password"));
                    user.setFirstName(resultSet.getString("firstName"));
                    user.setFatherName(resultSet.getString("fatherName"));
                    user.setGrandfatherName(resultSet.getString("grandfatherName"));
                    user.setFamilyName(resultSet.getString("familyName"));
                    user.setEmail(resultSet.getString("email"));
                    user.setGender(Gender.MALE.name().equals(resultSet.getString("gender"))? Gender.MALE : Gender.FEMALE);
                    String birthday = resultSet.getString("birthday");
                    user.setBirthday(birthday==null? null : Utils.toLocalDate(birthday));
                    user.setNationalId(resultSet.getString("nationalId"));
                    user.setNationality(resultSet.getString("nationality"));
                    user.setPassportNumber(resultSet.getString("passport"));
                    user.setFamilyId(resultSet.getString("familyId"));
                    user.setResidence(resultSet.getString("residence"));
                    user.setSuperuser(resultSet.getBoolean("isSuperuser"));
                    user.setStaff(resultSet.getBoolean("isStaff"));
                    user.setActive(resultSet.getBoolean("isActive"));
                    String dateJoined = resultSet.getString("dateJoined");
                    user.setDateJoined((dateJoined==null || dateJoined.isEmpty())? null : Utils.toLocalDateTime(dateJoined));
                    String lastLogin = resultSet.getString("lastLogin");
                    user.setLastLogin((lastLogin==null||lastLogin.isEmpty())? null : Utils.toLocalDateTime(lastLogin));

                    resultUsers.add(user);
                }
                return resultUsers;
            }
        };
    }
    public static Task<ObservableList<IdentificationRecord>> getIdentificationRecords() {
        return getIdentificationRecords(false);
    }

    public static Task<ObservableList<IdentificationRecord>> getIdentificationRecords(boolean identifiedOnly) {
        return new Task<ObservableList<IdentificationRecord>>() {
            @Override
            protected ObservableList<IdentificationRecord> call() throws Exception {
                ObservableList<IdentificationRecord> identifications = FXCollections.observableArrayList();
                fillIdentificationRecords(identifications, identifiedOnly);
                return identifications;
            }
        };
    }

    private static Task<Collection<IdentificationRecord>> fillIdentificationRecords(Collection<IdentificationRecord> identificationRecords) {
        return fillIdentificationRecords(identificationRecords, false);
    }

    private static Task<Collection<IdentificationRecord>> fillIdentificationRecords(Collection<IdentificationRecord> identificationRecords, boolean identifiedOnly) {
        return new Task<Collection<IdentificationRecord>>() {
            @Override
            protected Collection<IdentificationRecord> call() throws Exception {
                String query = "SELECT "
                        + "Identifications.id,"
                        + "subscriberId,"
                        + "Subscribers.firstName AS subFirstName,"
                        + "Subscribers.fatherName AS subFatherName,"
                        + "Subscribers.grandfatherName AS subGrandfatherName,"
                        + "Subscribers.familyName AS subFamilyName,"
                        + "workId,"
                        + "datetime,"
                        + "isIdentified,"
                        + "Identifications.notes,"
                        + "Identifications.username,"
                        + "Users.firstName AS userFirstName,"
                        + "Users.fatherName AS userFatherName,"
                        + "Users.grandfatherName AS userGrandfatherName,"
                        + "Users.familyName AS userFamilyName"
                        + " FROM Identifications "
                        + " INNER JOIN Subscribers ON Subscribers.id = Identifications.subscriberId"
                        + " INNER JOIN Users ON Users.username = Identifications.username";

                if (identifiedOnly)
                    query += " WHERE isIdentified = 1;";

                Statement statement = DB_CONNECTION.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    IdentificationRecord identification = new IdentificationRecord();

                    identification.setId(resultSet.getLong("id"));
                    identification.setDatetime(Utils.toLocalDateTime(resultSet.getString("datetime")));
                    identification.setIdentified(resultSet.getBoolean("isIdentified"));
                    identification.setNotes(resultSet.getString("notes"));

                    Subscriber subscriber = new Subscriber();
                    subscriber.setId(resultSet.getLong("subscriberId"));
                    subscriber.setFirstName(resultSet.getString("subFirstName"));
                    subscriber.setFatherName(resultSet.getString("subFatherName"));
                    subscriber.setGrandfatherName(resultSet.getString("subGrandfatherName"));
                    subscriber.setFamilyName(resultSet.getString("subFamilyName"));
                    subscriber.setWorkId(resultSet.getString("workId"));

                    identification.setSubscriber(subscriber);

                    User user = new User();
                    user.setUsername(resultSet.getString("username"));
                    user.setFirstName(resultSet.getString("userFirstName"));
                    user.setFatherName(resultSet.getString("userFatherName"));
                    user.setGrandfatherName(resultSet.getString("userGrandfatherName"));
                    user.setFamilyName(resultSet.getString("userFamilyName"));

                    identification.setUser(user);

                    identificationRecords.add(identification);
                }
                return identificationRecords;
            }
        };
    }

    private static PreparedStatement prepareUpdateStatement(String tableName, Map<String, Object> updateMap, Map<String, Object> filterMap) throws SQLException {
        StringBuilder query = new StringBuilder("UPDATE "+ tableName +" SET ");
        updateMap.keySet().forEach(key -> query.append(String.format("%s=?,", key)));
        query.deleteCharAt(query.length() - 1);

        query.append(" WHERE ");
        filterMap.keySet().forEach(key -> query.append(String.format("%s=? AND ", key)));
        query.delete(query.length() - 5, query.length()).append(";");

//        System.out.println("query = " + query);

        PreparedStatement pStatement = DB_CONNECTION.prepareStatement(query.toString());
        int i = 1;
        for (Map.Entry<String, Object> entry : updateMap.entrySet()) {
            pStatement.setObject(i++, entry.getValue());
        }
        for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
            pStatement.setObject(i++, entry.getValue());
        }
        return pStatement;
    }

    public static Task<ObservableList<Institute>> getInstitutes() {
        return new Task<ObservableList<Institute>>() {
            @Override
            protected ObservableList<Institute> call() throws Exception {
                PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement("SELECT id, name FROM Institutes;");
                ResultSet resultSet = pStatement.executeQuery();
                ObservableList<Institute> institutes = FXCollections.observableArrayList();
                while (resultSet.next()) {
                    int id = resultSet.getInt("id");
                    String name = resultSet.getString("name");
                    institutes.add(new Institute(id, name));
                }
                return institutes;
            }
        };
    }

    public static Task<ObservableSet<String>> getWorkIdes() {
        return new Task<ObservableSet<String>>() {
            @Override
            protected ObservableSet<String> call() throws Exception {
                ObservableSet<String> workIdes = FXCollections.observableSet();
                String[] queries = {"SELECT workId FROM Subscribers;", "SELECT workId FROM NewSubscribers;"};
                for (String query : queries) {
                    PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(query);
                    ResultSet resultSet = pStatement.executeQuery();
                    while (resultSet.next()) {
                        workIdes.add(resultSet.getString("workId"));
                    }
                }
                return workIdes;
            }
        };
    }
}
