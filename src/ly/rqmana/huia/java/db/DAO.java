package ly.rqmana.huia.java.db;

import ly.rqmana.huia.java.storage.DataStorage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DAO {

    public static final Connection DB_CONNECTION;
    public static final Statement STATEMENT;

    static {
        try {
            DB_CONNECTION = DriverManager.getConnection(DataStorage.getDBUrl());
            STATEMENT = DB_CONNECTION.createStatement();

            String createQuery = "CREATE TABLE IF NOT EXISTS Users("
                    + "id                INTEGER primary key autoincrement,"
                    + "username          TEXT NOT NULL UNIQUE,"
                    + "password          TEXT NOT NULL,"
                    + "email             TEXT,"
                    + "firstName         TEXT,"
                    + "fatherName        TEXT,"
                    + "grandfatherName   TEXT,"
                    + "familyName        TEXT,"
                    + "nationality       TEXT,"
                    + "nationalId        TEXT,"
                    + "birthday          TEXT,"
                    + "gender            TEXT,"
                    + "dateJoined        TEXT,"
                    + "isSuperuser       TEXT NOT NULL,"
                    + "lastLogin         TEXT"
                    + ");";
            STATEMENT.execute(createQuery);

            createQuery = "CREATE TABLE IF NOT EXISTS Institutes("
                    + "id           INTEGER primary key autoincrement,"
                    + "name         TEXT NOT NULL,"
                    + "description  TEXT"
                    + ");";
            STATEMENT.execute(createQuery);

            createQuery = "CREATE TABLE IF NOT EXISTS People"
                    + "(id                   INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "firstName            TEXT NOT NULL,"
                    + "fatherName           TEXT,"
                    + "grandfatherName      TEXT,"
                    + "familyName           TEXT NOT NULL,"
                    + "nationality          TEXT,"
                    + "nationalId           TEXT,"
                    + "birthday             TEXT,"
                    + "gender               TEXT NOT NULL,"
                    + "instituteId          INTEGER NOT NULL,"
                    + "workId               TEXT NOT NULL,"
                    + "relationship         TEXT NOT NULL,"
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
                    + "fingerprintImagesDir    TEXT,"
                    + "user                 TEXT NOT NULL,"
                    + "dateAdded            TEXT NOT NULL,"
                    + "dateUploaded         TEXT,"
                    + "notes                TEXT,"
                    + "FOREIGN KEY (instituteId) REFERENCES Institutes(id),"
                    + "FOREIGN KEY (user) REFERENCES Users(username)"
                    + ");";
            STATEMENT.execute(createQuery);

            createQuery = "CREATE TABLE IF NOT EXISTS NewRegistrations"
                    + "(id                   INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "firstName            TEXT NOT NULL,"
                    + "fatherName           TEXT,"
                    + "grandfatherName      TEXT,"
                    + "familyName           TEXT NOT NULL,"
                    + "nationality          TEXT,"
                    + "nationalId           TEXT,"
                    + "birthday             TEXT,"
                    + "gender               TEXT NOT NULL,"
                    + "instituteId          INTEGER NOT NULL,"
                    + "workId               TEXT NOT NULL,"
                    + "relationship         TEXT NOT NULL,"
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
                    + "fingerprintImagesDir    TEXT,"
                    + "user                 TEXT NOT NULL,"
                    + "dateAdded            TEXT NOT NULL,"
                    + "isUploaded           TEXT NOT NULL DEFAULT 'NO',"
                    + "isViewed             TEXT NOT NULL DEFAULT 'NO',"
                    + "dateUploaded         TEXT,"
                    + "hasProblem           TEXT,"
                    + "notes                TEXT,"
                    + "FOREIGN KEY (instituteId) REFERENCES Institutes(id),"
                    + "FOREIGN KEY (user) REFERENCES Users(username)"
                    + ");";
            STATEMENT.execute(createQuery);

            createQuery = "CREATE TABLE IF NOT EXISTS Contacts"
                    + "(id          INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "personId    INTEGER NOT NULL,"
                    + "label       TEXT,"
                    + "contact     TEXT,"
                    + "FOREIGN KEY (personId) REFERENCES People(id)"
                    + ");";
            STATEMENT.execute(createQuery);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
