package ly.rqmana.huia.java.db;

import ly.rqmana.huia.java.security.Hasher;
import ly.rqmana.huia.java.storage.DataStorage;
import ly.rqmana.huia.java.util.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
                + "fingerprintImagesDir    TEXT,"
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
                + "fingerprintImagesDir    TEXT,"
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

}
