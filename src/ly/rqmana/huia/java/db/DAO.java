package ly.rqmana.huia.java.db;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ly.rqmana.huia.java.fingerprints.hand.Hand;
import ly.rqmana.huia.java.models.Gender;
import ly.rqmana.huia.java.models.Relationship;
import ly.rqmana.huia.java.models.Subscriber;
import ly.rqmana.huia.java.security.Auth;
import ly.rqmana.huia.java.security.Hasher;
import ly.rqmana.huia.java.storage.DataStorage;
import ly.rqmana.huia.java.util.SQLUtils;
import ly.rqmana.huia.java.util.Utils;

import java.sql.*;
import java.time.LocalDate;
import java.util.Observable;

public class DAO {

    public static final Connection DB_CONNECTION;
    private static final String DB_NAME = Utils.APP_NAME + ".sqlite";

    static {
        try {
            DB_CONNECTION = DriverManager.getConnection(getDBUrl());
//            initializeDB();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void initializeDB() throws SQLException {

        createTables();

        addAdminUser();

        addInstitute("الحديد");
        addInstitute("المباحث الجنائية");
    }

    private static void createTables() throws SQLException {
        createUsersTable();
        createInstitutesTable();
        createPeopleTable();
        createNewRegistrationsTable();
        createContactsTable();
    }

    private static void createUsersTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS Users("
                + "id                  INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username          TEXT NOT NULL UNIQUE,"
                + "password          TEXT NOT NULL,"
                + "email             TEXT,"
                + "firstName         TEXT,"
                + "fatherName        TEXT,"
                + "grandfatherName   TEXT,"
                + "familyName        TEXT,"
                + "nationality       TEXT,"
                + "nationalId        TEXT,"
                + "birthday          DATE,"
                + "gender            TEXT,"
                + "dateJoined        DATE,"
                + "isSuperuser       BOOLEAN NOT NULL,"
                + "lastLogin         DATE"
                + ");";
        statement.execute(createQuery);

        statement.close();
    }

    private static void createInstitutesTable() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String createQuery = "CREATE TABLE IF NOT EXISTS Institutes("
                + "id                  INTEGER PRIMARY KEY AUTOINCREMENT,"
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
                + "dateAdded            DATE NOT NULL DEFAULT (STRFTIME('%s', 'now')),"
                + "dateUploaded         DATE,"
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
                + "birthday             DATE NOT NULL ,"
                + "gender               TEXT NOT NULL,"
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
                + "dateAdded            DATE NOT NULL DEFAULT (STRFTIME('%s', 'now')),"
                + "isUploaded           BOOLEAN NOT NULL DEFAULT FALSE,"
                + "isViewed             BOOLEAN NOT NULL DEFAULT FALSE,"
                + "dateUploaded         DATE,"
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

    private static void addAdminUser() throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String hashedPassword = Hasher.encode("Admin", "Salt");
        String insertQuery = "INSERT INTO Users (username, password, email, firstName, isSuperuser) VALUES ('Admin', '" + hashedPassword + "', 'admin@huia.ly', 'Admin', FALSE);";
        statement.execute(insertQuery);

        statement.close();
    }

    private static void addInstitute(String institute) throws SQLException {
        Statement statement = DB_CONNECTION.createStatement();

        String insertQuery = "INSERT INTO Institutes (name) VALUES ('" + institute + "');";
        statement.execute(insertQuery);

        statement.close();
    }

    private static String getDBUrl() {
        return "jdbc:sqlite:" + DataStorage.getDataPath().resolve(DB_NAME);
    }

    public static final String DATA_DB_NAME = "FingerprintData.db";

    public static String getDataDBUrl() {
        return "jdbc:sqlite:" + DataStorage.getDataPath().resolve(DATA_DB_NAME).toString();
    }

    public static ObservableList<Subscriber> getOldSubscribers() throws SQLException {
        ObservableList<Subscriber> subscribers = FXCollections.observableArrayList();
        Connection connection = DriverManager.getConnection(DAO.getDataDBUrl());
        String query = "SELECT " +
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
                subscriber.setFirstName(resultSet.getString(1));
                subscriber.setFatherName(resultSet.getString(2));
                subscriber.setFamilyName(resultSet.getString(3));

                subscriber.setBirthday(LocalDate.parse(resultSet.getString(4)));
                subscriber.setNationalId(resultSet.getString(5));
                subscriber.setGender("M".equals(resultSet.getString(6)) ? Gender.MALE : Gender.FEMALE);
                subscriber.setAllFingerprintsTemplate(resultSet.getString(7));
                subscriber.setWorkId(resultSet.getString(8));
                subscriber.setRelationship(resultSet.getString(9));
                subscriber.setActive(resultSet.getString(10).equals("True"));

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

            subscriber.setFirstName(resultSet.getString("firstName"));
            subscriber.setFatherName(resultSet.getString("fatherName"));
            subscriber.setGrandfatherName(resultSet.getString("grandfatherName"));
            subscriber.setFamilyName(resultSet.getString("familyName"));

            subscriber.setBirthday(SQLUtils.timestampToDate(resultSet.getLong("birthday")));
            subscriber.setNationalId(resultSet.getString("nationalId"));
            subscriber.setGender(Gender.valueOf(resultSet.getString("gender")));

            subscriber.setWorkId(resultSet.getString("workId"));
            subscriber.setRelationship(resultSet.getString("relationship"));

            if (tableName.equals("People"))
                subscriber.setActive(resultSet.getBoolean("isActive"));

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
        pStatement.setLong(7, SQLUtils.dateToTimestamp(subscriber.getBirthday()));
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
}
