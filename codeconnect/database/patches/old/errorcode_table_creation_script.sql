-- AUTHOR: SNAPPER VIBES
-- TURTLE CREEK VALLEY COUNCIL OF GOVERNMENTS CODE ENFORCEMENT DATABASE
-- ERRORCODE TABLE CREATION SCRIPT


CREATE TABLE errorcode(
    errorcode           INTEGER NOT NULL PRIMARY KEY,
    errorname           text NOT NULL,
    errordescription    text,
    icon_iconid         INTEGER REFERENCES icon (iconid)
);


--ALTER TABLE improvementstatus RENAME TO issuestatus;

CREATE TABLE errorperson(
    person_personid     INTEGER NOT NULL REFERENCES person (personid),
    errorcode           INTEGER NOT NULL REFERENCES errorcode (errorcode),
    createdts           TIMESTAMP WITH TIME ZONE NOT NULL,
    issuestatus_statusid    INTEGER NOT NULL REFERENCES improvementstatus (statusid) DEFAULT 1,
    notes               TEXT
);


CREATE TABLE errorproperty(
    property_parid      INTEGER NOT NULL REFERENCES property (propertyid),
    errorcode           INTEGER NOT NULL REFERENCES errorcode (errorcode),
    createdts           TIMESTAMP WITH TIME ZONE NOT NULL,
    issuestatus_statusid    INTEGER NOT NULL REFERENCES improvementstatus (statusid) DEFAULT 1,
    notes               TEXT
);
