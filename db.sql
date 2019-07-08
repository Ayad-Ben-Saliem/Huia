USE `huia healthcare`;

DROP TABLE Users;

CREATE TABLE IF NOT EXISTS Users(
    id                INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    username          VARCHAR(25) NOT NULL UNIQUE,
    password          VARCHAR(25) NOT NULL,
    email             VARCHAR(50),
    firstName         VARCHAR(25) NOT NULL,
    fatherName        VARCHAR(25),
    grandfatherName   VARCHAR(25),
    familyName        VARCHAR(25),
    nationality       VARCHAR(25),
    nationalId        VARCHAR(25),
    birthday          DATE,
    gender            ENUM ('Male', 'Female') NOT NULL,
    passport          VARCHAR(25),
    familyId          VARCHAR(25),
    residence         VARCHAR(25),
    dateJoined        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    isSuperuser       BOOLEAN NOT NULL,
    isStaff           BOOLEAN NOT NULL,
    isActive          BOOLEAN NOT NULL,
    lastLogin         DATETIME
);


INSERT INTO Users
(username, password, email, firstName, gender, isSuperuser, isStaff, isActive)
VALUES
('Admin', 'Admin', 'admin@huia.ly', 'Admin', 'Male', TRUE, TRUE, TRUE);



CREATE TABLE IF NOT EXISTS Institutes(
     id           INTEGER PRIMARY KEY AUTO_INCREMENT,
     name         VARCHAR(100) NOT NULL,
     description  TEXT
);


CREATE TABLE IF NOT EXISTS Subscribers
(
    id                      INTEGER PRIMARY KEY AUTO_INCREMENT,
    firstName               VARCHAR(25) NOT NULL,
    fatherName              VARCHAR(25),
    grandfatherName         VARCHAR(25),
    familyName              VARCHAR(25) NOT NULL,
    nationality             VARCHAR(25),
    nationalId              VARCHAR(25),
    birthday                DATE NOT NULL ,
    gender                  ENUM ('Male', 'Female') NOT NULL,
    passport                VARCHAR(25) UNIQUE,
    familyId                VARCHAR(25),
    residence               VARCHAR(25),
    instituteId             INTEGER NOT NULL,
    workId                  VARCHAR(10),
    relationship            ENUM('Employee', 'Father', 'Mother', 'Sun', 'Daughter', 'Wife', 'Husband'),
    isActive                BOOLEAN NOT NULL,
    rightThumbFingerprint   TEXT,
    rightIndexFingerprint   TEXT,
    rightMiddleFingerprint  TEXT,
    rightRingFingerprint    TEXT,
    rightLittleFingerprint  TEXT,
    leftThumbFingerprint    TEXT,
    leftIndexFingerprint    TEXT,
    leftMiddleFingerprint   TEXT,
    leftRingFingerprint     TEXT,
    leftLittleFingerprint   TEXT,
    allFingerprintTemplates TEXT,
    dataPath                TEXT,
    user                    INTEGER NOT NULL,
    dateAdded               DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dateUploaded            DATETIME,
    notes                   TEXT,
    FOREIGN KEY (instituteId) REFERENCES Institutes(id),
    FOREIGN KEY (user) REFERENCES Users(id)
);


CREATE TABLE IF NOT EXISTS NewSubscribers (
    id                       INTEGER PRIMARY KEY AUTO_INCREMENT,
    firstName                VARCHAR(25) NOT NULL,
    fatherName               VARCHAR(25),
    grandfatherName          VARCHAR(25),
    familyName               VARCHAR(25) NOT NULL,
    nationality              VARCHAR(25),
    nationalId               VARCHAR(25),
    birthday                 DATE NOT NULL,
    gender               ENUM ('Male', 'Female') NOT NULL,
    passport                 VARCHAR(25) UNIQUE,
    familyId                 VARCHAR(25),
    residence                VARCHAR(25),
    instituteId              INTEGER NOT NULL,
    workId                   VARCHAR(10),
    relationship         ENUM('Employee', 'Father', 'Mother', 'Sun', 'Daughter', 'Wife', 'Husband') NOT NULL,
    rightThumbFingerprint    TEXT,
    rightIndexFingerprint    TEXT,
    rightMiddleFingerprint   TEXT,
    rightRingFingerprint     TEXT,
    rightLittleFingerprint   TEXT,
    leftThumbFingerprint     TEXT,
    leftIndexFingerprint     TEXT,
    leftMiddleFingerprint    TEXT,
    leftRingFingerprint      TEXT,
    leftLittleFingerprint    TEXT,
    allFingerprintTemplates  TEXT,
    dataPath                 TEXT,
    user                     INTEGER NOT NULL,
    dateAdded                DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    isUploaded               BOOLEAN NOT NULL DEFAULT FALSE,
    isViewed                 BOOLEAN NOT NULL DEFAULT FALSE,
    dateUploaded             DATETIME,
    hasProblem               BOOLEAN DEFAULT FALSE,
    notes                    TEXT,
    FOREIGN KEY (instituteId) REFERENCES Institutes(id),
    FOREIGN KEY (user) REFERENCES Users(id)
);


CREATE TABLE IF NOT EXISTS Contacts (
    id          INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    personId    INTEGER NOT NULL,
    label       VARCHAR(25) NOT NULL ,
    contact     VARCHAR(250) NOT NULL ,
    UNIQUE KEY `contact` (`personId`,`label`,`contact`),
    FOREIGN KEY (personId) REFERENCES Subscribers(id)
);

CREATE TABLE IF NOT EXISTS Identifications
(
    id              INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    subscriberId    INTEGER NOT NULL,
    datetime        DATETIME NOT NULL,
    isIdentified    BOOLEAN NOT NULL ,
    username        VARCHAR(25),
    notes           TEXT
);