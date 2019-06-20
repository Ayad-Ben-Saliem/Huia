package ly.rqmana.huia.java.db;

import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ly.rqmana.huia.java.concurrent.Task;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.models.User;
import ly.rqmana.huia.java.fingerprints.hand.Hand;
import ly.rqmana.huia.java.models.*;
import ly.rqmana.huia.java.security.Auth;
import ly.rqmana.huia.java.storage.DataStorage;
import ly.rqmana.huia.java.util.SQLUtils;
import ly.rqmana.huia.java.util.Utils;
import org.sqlite.core.DB;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Observable;

public class DAO {

    public static final Connection DB_CONNECTION;
    private static final String DB_NAME = Utils.APP_NAME + ".sqlite";

    static {
        try {
            DB_CONNECTION = DriverManager.getConnection(getDBUrl());
            initializeDB();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initializeDB() throws SQLException {
        createTables();


        if (countRecords("Users").runAndGet() == 0) {
            insertAdminUser().runAndGet();
        }

        if (countRecords("Institutes").runAndGet() == 0) {
            insertInstitute("الشركة الليبية للحديد والصلب").runAndGet();
        }

        if (countRecords("People").runAndGet() == 0) {
            migrateOldData().runAndGet();
        }
    }

    private static Task<Boolean> migrateOldData() throws SQLException {

        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                Connection oldConnection = DriverManager.getConnection(getDataDBUrl());

                ResultSet oldDataSet = oldConnection.createStatement().executeQuery("SELECT * FROM Fingerprint where company_id ='02';");

                String insertQuery = "INSERT INTO People("
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

                System.out.println(System.currentTimeMillis());
                while (oldDataSet.next()) {

                    String firstName = oldDataSet.getString("first_name");
                    String fatherName = oldDataSet.getString("father_name");
                    String familyName = oldDataSet.getString("last_name");
                    String nationalId = oldDataSet.getString("national_id");

                    String birthdayString = oldDataSet.getString("birthday");
                    LocalDate birthday = LocalDate.parse(birthdayString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

                    String genderString = oldDataSet.getString("sex");

                    Gender gender;
                    gender = genderString.equals("M") ? Gender.MALE: Gender.FEMALE;

                    String allFingerprintsTemplates = oldDataSet.getString("fingerprint_template");
                    String workId = oldDataSet.getString("work_id");

                    String relationshipRawString = oldDataSet.getString("relationship");

                    String relationship;
                    if (relationshipRawString.equals("خاص") || relationshipRawString.equals("لا يوجد")){
                        relationship = relationshipRawString;
                    }
                    else{
                        relationship = Relationship.parseArabic(relationshipRawString).name();
                    }

                    String dateAdded = oldDataSet.getString("start_date");

                    boolean isActive = SQLUtils.getBoolean(oldDataSet.getString("is_active"));
                    String notes = oldDataSet.getString("notes");

                    //TODo; sett institute id to 1

                    pStatement.setString(1, firstName);
                    pStatement.setString(2, fatherName);
                    pStatement.setString(3, familyName);

                    pStatement.setString(4, nationalId);
                    pStatement.setString(5, birthday.toString());
                    pStatement.setString(6, gender.name());
                    pStatement.setLong(7, 1);
                    pStatement.setString(8, workId);
                    pStatement.setString(9, relationship);
                    pStatement.setBoolean(10, isActive);
                    pStatement.setString(11, allFingerprintsTemplates);
                    pStatement.setString(12, dateAdded);

                    pStatement.setLong(13, 1);
                    pStatement.setString(14, notes);
                    pStatement.addBatch();
        //            "select * from Fingerprint where relationship!='الزوجةالإبن' and relationship!='المشترك' and relationship!='الإبنة' and relationship!='والدة الموظف' and relationship!='والد الموظف' and relationship!='لا يوجد'";
                }
                System.out.println(System.currentTimeMillis());

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
        createPeopleTable();
        createNewRegistrationsTable();
        createContactsTable();
        createIdentificationsTable();
    }

    private static void createIdentificationsTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS Identifications("
                + "id                  INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "subscriberId        INTEGER NOT NULL,"
                + "datetime            DATETIME NOT NULL DEFAULT (DATETIME(CURRENT_TIMESTAMP)),"
                + "isIdentified        BOOLEAN NOT NULL ,"
                + "username            TEXT,"
                + "notes"
                + ");";
        statement.execute(createQuery);

        statement.close();

    }

    private static void createUsersTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS Users("
                + "id                INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                + "username          TEXT NOT NULL UNIQUE,"
                + "password          TEXT NOT NULL,"
                + "email             TEXT,"
                + "firstName         TEXT NOT NULL,"
                + "fatherName        TEXT,"
                + "grandfatherName   TEXT,"
                + "familyName        TEXT,"
                + "nationality       TEXT,"
                + "nationalId        TEXT,"
                + "birthday          DATETIME,"
                + "gender            TEXT,"
                + "passport          TEXT,"
                + "familyId          TEXT,"
                + "residence         TEXT,"
                + "dateJoined        DATETIME,"
                + "isSuperuser       BOOLEAN NOT NULL,"
                + "isStaff           BOOLEAN NOT NULL,"
                + "isActive          BOOLEAN NOT NULL,"
                + "lastLogin         DATETIME"
                + ");";
        statement.execute(createQuery);

        statement.close();
    }

    private static void createInstitutesTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS Institutes("
                + "id           INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name         TEXT NOT NULL,"
                + "description  TEXT"
                + ");";
        statement.execute(createQuery);

        statement.close();
    }

    private static void createPeopleTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS People"
                + "(id                  INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "firstName            TEXT NOT NULL,"
                + "fatherName           TEXT,"
                + "grandfatherName      TEXT,"
                + "familyName           TEXT NOT NULL,"
                + "nationality          TEXT,"
                + "nationalId           TEXT,"
                + "birthday             DATE NOT NULL ,"
                + "gender               TEXT NOT NULL,"
                + "passport             TEXT UNIQUE,"
                + "familyId             TEXT,"
                + "residence            TEXT,"
                + "instituteId          INTEGER NOT NULL,"
                + "workId               TEXT,"
                + "relationship         TEXT,"
                + "isActive             BOOLEAN,"
                + "rightThumbFingerprint   TEXT,"
                + "rightIndexFingerprint   TEXT,"
                + "rightMiddleFingerprint  TEXT,"
                + "rightRingFingerprint    TEXT,"
                + "rightLittleFingerprint  TEXT,"
                + "leftThumbFingerprint    TEXT,"
                + "leftIndexFingerprint    TEXT,"
                + "leftMiddleFingerprint   TEXT,"
                + "leftRingFingerprint     TEXT,"
                + "leftLittleFingerprint   TEXT,"
                + "allFingerprintTemplates TEXT,"
                + "dataPath                 TEXT,"
                + "user                 INTEGER NOT NULL,"
                + "dateAdded            DATETIME NOT NULL,"
                + "dateUploaded         DATETIME,"
                + "notes                TEXT,"
                + "FOREIGN KEY (instituteId) REFERENCES Institutes(id),"
                + "FOREIGN KEY (user) REFERENCES Users(id)"
                + ");";
        statement.execute(createQuery);

        statement.close();
    }

    private static void createNewRegistrationsTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS NewRegistrations ("
                + "id                   INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "firstName            TEXT NOT NULL,"
                + "fatherName           TEXT,"
                + "grandfatherName      TEXT,"
                + "familyName           TEXT NOT NULL,"
                + "nationality          TEXT,"
                + "nationalId           TEXT,"
                + "birthday             DATE,"
                + "gender               TEXT,"
                + "passport             TEXT UNIQUE,"
                + "familyId             TEXT,"
                + "residence            TEXT,"
                + "instituteId          INTEGER NOT NULL,"
                + "workId               TEXT,"
                + "relationship         TEXT,"
                + "rightThumbFingerprint   TEXT,"
                + "rightIndexFingerprint   TEXT,"
                + "rightMiddleFingerprint  TEXT,"
                + "rightRingFingerprint    TEXT,"
                + "rightLittleFingerprint  TEXT,"
                + "leftThumbFingerprint    TEXT,"
                + "leftIndexFingerprint    TEXT,"
                + "leftMiddleFingerprint   TEXT,"
                + "leftRingFingerprint     TEXT,"
                + "leftLittleFingerprint   TEXT,"
                + "allFingerprintTemplates TEXT,"
                + "dataPath                 TEXT,"
                + "user                 INTEGER NOT NULL,"
                + "dateAdded            DATETIME NOT NULL DEFAULT (DATETIME(CURRENT_TIMESTAMP)),"
                + "isUploaded           BOOLEAN NOT NULL DEFAULT FALSE,"
                + "isViewed             BOOLEAN NOT NULL DEFAULT FALSE,"
                + "dateUploaded         DATETIME,"
                + "hasProblem           BOOLEAN DEFAULT FALSE,"
                + "notes                TEXT,"
                + "FOREIGN KEY (instituteId) REFERENCES Institutes(id),"
                + "FOREIGN KEY (user) REFERENCES Users(id)"
                + ");";
        statement.execute(createQuery);

        statement.close();
    }

    private static void createContactsTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS Contacts ("
                + "id          INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "personId    INTEGER NOT NULL,"
                + "label       TEXT,"
                + "contact     TEXT,"
                + "FOREIGN KEY (personId) REFERENCES People(id)"
                + ");";
        statement.execute(createQuery);

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
                        "password," +
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

    private static String getDBUrl() {
        return "jdbc:sqlite:" + DataStorage.getDataPath().resolve(DB_NAME);
    }

    public static final String DATA_DB_NAME = "FingerprintData.db";

    public static String getDataDBUrl() {
        return "jdbc:sqlite:" + DataStorage.getDataPath().resolve(DATA_DB_NAME).toString();
    }

    public static User getUserByUsername(String username) throws SQLException {
        String query = "SELECT   " +
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
                "FROM Users WHERE username=? COLLATE NOCASE";

        PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(query);
        pStatement.setString(1, username);
        ResultSet resultSet = pStatement.executeQuery();

        User user = null;
        if (resultSet.next()) {
            user = new User();
            user.setUsername(username);
            user.setHashedPassword(resultSet.getString("password"));
            user.setEmail(resultSet.getString("email"));
            user.setFirstName(resultSet.getString("firstName"));
            user.setFatherName(resultSet.getString("fatherName"));
            user.setGrandfatherName(resultSet.getString("grandfatherName"));
            user.setFamilyName(resultSet.getString("familyName"));
            user.setNationality(resultSet.getString("nationality"));
            user.setNationalId(resultSet.getString("nationalId"));
            String birthday = resultSet.getString("birthday");
            user.setBirthday(birthday == null? null : LocalDate.parse(birthday));
            String gender = resultSet.getString("gender");
            user.setGender(Gender.MALE.name().equalsIgnoreCase(gender)? Gender.MALE : Gender.FEMALE);
            String dateJoined = resultSet.getString("dateJoined");
            user.setDateJoined(dateJoined == null? null : LocalDateTime.parse(dateJoined));
            user.setSuperuser(resultSet.getBoolean("isSuperuser"));
            user.setStaff(resultSet.getBoolean("isStaff"));
            user.setActive(resultSet.getBoolean("isActive"));
            String lastLogin = resultSet.getString("lastLogin");
            user.setLastLogin(lastLogin == null? null : LocalDateTime.parse(lastLogin));
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
                PreparedStatement pStatement = setUpdateMap("Users", updateMap, filterMap);
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
                PreparedStatement pStatement = setUpdateMap("Users", updateMap, filterMap);
                pStatement.execute();

                return true;
            }
        };
    }

    public static ObservableList<Subscriber> getOldSubscribers() throws SQLException {
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList();
        Connection connection = DriverManager.getConnection(DAO.getDataDBUrl());
        String query = "SELECT " +
                "id," +
                "first_name," +
                "father_name," +
                "last_name," +
                "birthday," +
                "national_id," +
                "sex," +
                "fingerprint_template," +
                "work_id," +
                "relationship," +
                "is_active FROM Fingerprint";
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                Subscriber subscriber = new Subscriber();

                subscriber.setId(resultSet.getLong("id"));

                subscriber.setFirstName(resultSet.getString("first_name"));
                subscriber.setFatherName(resultSet.getString("father_name"));
                subscriber.setFamilyName(resultSet.getString("last_name"));

                subscriber.setBirthday(LocalDate.parse(resultSet.getString("birthday")));
                subscriber.setNationalId(resultSet.getString("national_id"));
                subscriber.setGender("M".equals(resultSet.getString("sex")) ? Gender.MALE : Gender.FEMALE);
                subscriber.setAllFingerprintsTemplate(resultSet.getString("fingerprint_template"));
                subscriber.setWorkId(resultSet.getString("work_id"));
                subscriber.setRelationship(resultSet.getString("relationship"));
                subscriber.setActive(resultSet.getString("is_active").equals("True"));

                subscribers.add(subscriber);
            }
        }
        return subscribers;
    }

    public static ObservableList<Subscriber> getSubscribers() throws SQLException {
        return getHuiaSubscribers("People");
    }

    public static ObservableList<Subscriber> getNewSubscribers() throws SQLException {
        return getHuiaSubscribers("NewRegistrations");
    }

    private static ObservableList<Subscriber> getHuiaSubscribers(String tableName) throws SQLException {
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList();

        String query = "SELECT " +
                "id," +
                "firstName," +
                "fatherName," +
                "grandfatherName," +
                "familyName," +
                "birthday," +
                "nationalId," +
                "familyId," +
                "gender," +
                "workId," +
                "relationship,";
        if (tableName.equals("People"))
                query += "isActive,";
        query += "allFingerprintTemplates," +
                 "rightThumbFingerprint," +
                 "rightIndexFingerprint," +
                 "rightMiddleFingerprint," +
                 "rightRingFingerprint," +
                 "rightLittleFingerprint," +
                 "leftThumbFingerprint," +
                 "leftIndexFingerprint," +
                 "leftMiddleFingerprint," +
                 "leftRingFingerprint," +
                 "leftLittleFingerprint" +
                 " FROM " + tableName;

        Statement statement = DB_CONNECTION.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            Subscriber subscriber = new Subscriber();

            subscriber.setId(resultSet.getLong("id"));

            subscriber.setFirstName(resultSet.getString("firstName"));
            subscriber.setFatherName(resultSet.getString("fatherName"));
            subscriber.setGrandfatherName(resultSet.getString("grandfatherName"));
            subscriber.setFamilyName(resultSet.getString("familyName"));

            String birthday = resultSet.getString("birthday");
            subscriber.setBirthday(birthday==null? null : LocalDate.parse(birthday));
            subscriber.setNationalId(resultSet.getString("nationalId"));
            subscriber.setGender(Gender.valueOf(resultSet.getString("gender")));

            subscriber.setWorkId(resultSet.getString("workId"));
            subscriber.setRelationship(resultSet.getString("relationship"));

            if (tableName.equals("People"))
                subscriber.setActive(resultSet.getBoolean("isActive"));
            else
                subscriber.setActive(true);

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

            subscribers.add(subscriber);
        }
        return subscribers;
    }

    public static long insertNewSubscriber(Subscriber subscriber) throws SQLException {
        final String INSERT_QUERY =
                "INSERT INTO NewRegistrations ("
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
        pStatement.setInt(9, subscriber.getInstitute().getId());

        pStatement.setString(10, subscriber.getFamilyId());
        pStatement.setString(11, subscriber.getResidence());
        pStatement.setString(12, subscriber.getPassport().getNumber());

        pStatement.setString(13, subscriber.getWorkId());
        pStatement.setString(14, subscriber.getRelationship().toString());

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

        pStatement.setString(26, Auth.getCurrentUser().getUsername());
        pStatement.executeUpdate();

        ResultSet generatedKeys = pStatement.getGeneratedKeys();

        return generatedKeys.getLong(1);
    }

    public static void updateSubscriberDataPath(Subscriber subscriber) throws SQLException {
        PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement("UPDATE NewRegistrations SET dataPath= ? WHERE id= ?");
        pStatement.setString(1, subscriber.getDataPath());
        pStatement.setLong(2, subscriber.getId());

        pStatement.executeUpdate();
    }

    public static long insertSubscriberIdentification(Subscriber subscriber) throws SQLException {
        final String insertQuery
                        = "INSERT INTO Identifications ("
                        + "subscriberId,"
                        + "username,"
                        + "isIdentified,"
                        + "notes"
                        + ") VALUES (?,?,?,?)";

        PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);

        pStatement.setLong(1, subscriber.getId());
        pStatement.setString(2, Auth.getCurrentUser().getUsername());
        pStatement.setBoolean(3, false);
        String notes = "";
        if (!subscriber.isActive())
            notes += Utils.getI18nString("SUBSCRIBER_NOT_ACTIVE");
        if (!subscriber.hasFingerprint())
            notes += Utils.getI18nString("FINGERPRINT_NOT_REGISTERED");
        pStatement.setString(4, notes);

        pStatement.execute();

        ResultSet generatedKeys = pStatement.getGeneratedKeys();
        return generatedKeys.getLong(1);
    }

    public static Task<Boolean> updateSubscriberIdentification(long identificationId, Subscriber subscriber, boolean isIdentified, String notes) {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                String selectQuery = "SELECT notes FROM Identifications WHERE id=? AND subscriberId=?;";
                PreparedStatement pStatement = DAO.DB_CONNECTION.prepareStatement(selectQuery);
                pStatement.setLong(1, identificationId);
                pStatement.setLong(2, subscriber.getId());
                ResultSet resultSet = pStatement.executeQuery();

                String _notes = "";
                if (resultSet.next()) {
                    _notes = resultSet.getString("notes");
                }
                if (notes != null)
                    _notes += ":\n:" + notes;

                final String insertQuery = "UPDATE Identifications SET isIdentified=?, notes=? WHERE id=? AND subscriberId=?;";

                pStatement = DAO.DB_CONNECTION.prepareStatement(insertQuery);

                pStatement.setBoolean(1, isIdentified);
                pStatement.setString(2, _notes);
                pStatement.setLong(3, identificationId);
                pStatement.setLong(4, subscriber.getId());

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
                    user.setBirthday(birthday==null? null : LocalDate.parse(birthday));
                    user.setNationalId(resultSet.getString("nationalId"));
                    user.setNationality(resultSet.getString("nationality"));
                    user.setPassportNumber(resultSet.getString("passport"));
                    user.setFamilyId(resultSet.getString("familyId"));
                    user.setResidence(resultSet.getString("residence"));
                    user.setSuperuser(resultSet.getBoolean("isSuperuser"));
                    user.setStaff(resultSet.getBoolean("isStaff"));
                    user.setActive(resultSet.getBoolean("isActive"));
                    String dateJoined = resultSet.getString("dateJoined");
                    user.setDateJoined((dateJoined==null || dateJoined.isEmpty())? null : LocalDateTime.parse(dateJoined));
                    String lastLogin = resultSet.getString("lastLogin");
                    user.setLastLogin((lastLogin==null||lastLogin.isEmpty())? null : LocalDateTime.parse(lastLogin));

                    resultUsers.add(user);
                }
                return resultUsers;
            }
        };
    }

    public static ObservableList<SubscriberIdentification> getSubscriberIdentifications(boolean identifiedOnly) throws SQLException {
        ObservableList<SubscriberIdentification> identifications = FXCollections.observableArrayList();

        String query = "SELECT "
                + "Identifications.id,"
                + "NewRegistrations.firstName AS subFirstName,"
                + "NewRegistrations.fatherName AS subFatherName,"
                + "NewRegistrations.grandfatherName AS subGrandfatherName,"
                + "NewRegistrations.familyName AS subFamilyName,"
                + "workId,"
                + "datetime,"
                + "isIdentified,"
                + "Identifications.username,"
                + "Users.firstName AS userFirstName,"
                + "Users.fatherName AS userFatherName,"
                + "Users.grandfatherName AS userGrandfatherName,"
                + "Users.familyName AS userFamilyName"
                + " FROM Identifications "
                + " INNER JOIN NewRegistrations ON NewRegistrations.id = Identifications.subscriberId"
                + " INNER JOIN Users ON Users.username = Identifications.username";

        if (identifiedOnly)
            query += " WHERE isIdentified = 1;";

        Statement statement = DB_CONNECTION.createStatement();
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {

            SubscriberIdentification identification = new SubscriberIdentification();

            identification.setId(resultSet.getLong("id"));
            identification.setDateTime(SQLUtils.timestampToDateTime(resultSet.getLong("datetime")));
            identification.setIdentified(resultSet.getBoolean("isIdentified"));

            Subscriber subscriber = new Subscriber();
            subscriber.setFirstName(resultSet.getString("subFirstName"));
            subscriber.setFatherName(resultSet.getString("subFatherName"));
            subscriber.setGrandfatherName(resultSet.getString("subGrandfatherName"));
            subscriber.setFamilyName(resultSet.getString("subFamilyName"));
            subscriber.setWorkId(resultSet.getString("workId"));
            subscriber.setWorkId(resultSet.getString("workId"));


            User user = new User();
            user.setFirstName(resultSet.getString("userFirstName"));
            user.setFatherName(resultSet.getString("userFatherName"));
            user.setGrandfatherName(resultSet.getString("userGrandfatherName"));
            user.setFamilyName(resultSet.getString("userFamilyName"));

            identification.setSubscriber(subscriber);
            identification.setUser(user);
        }

        return identifications;
    }

    private static PreparedStatement setUpdateMap(String tableName, Map<String, Object> updateMap, Map<String, Object> filterMap) throws SQLException {
        StringBuilder query = new StringBuilder("UPDATE Users SET ");
        updateMap.keySet().forEach(key -> query.append(String.format("%s=?,", key)));
        query.deleteCharAt(query.length() - 1);

        query.append(" WHERE ");
        filterMap.keySet().forEach(key -> query.append(String.format("%s=? AND ", key)));
        query.delete(query.length() - 5, query.length()).append(";");

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

}
