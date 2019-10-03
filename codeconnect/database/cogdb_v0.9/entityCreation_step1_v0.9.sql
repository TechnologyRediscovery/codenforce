-- AUTHOR: ERIC DARSOW
-- TURTLE CREEK VALLEY COUNCIL OF GOVERNMENTS CODE ENFORCEMENT DATABASE
-- PRIMARY DATABASE CREATION SCRIPT
-- COPATIBLE WITH POSTGRESQL V.10

--BEGIN;

-- NOTE that starting on 7 Jan 2018, this DDL script is NO LONGER
-- CURRENT for our design. Rather, changes are made directly in postgres
-- and the creation script can be exported. Use this as reference



CREATE TABLE municipality
  (
    muniCode             INTEGER NOT NULL,
    muniName             text NOT NULL ,
    -- need municipal title here, too
    address_street       text ,
    address_city         text ,
    address_state        text DEFAULT 'PA',
    address_zip          text ,
    phone                text ,
    fax                  text ,
    email                text ,   
    managerName          text ,
    managerPhone         text ,
    population           INTEGER,
    activeInProgram      boolean
  ) ;

ALTER TABLE municipality ADD CONSTRAINT municipality_pk PRIMARY KEY ( muniCode ) ;


CREATE TYPE personType AS ENUM (
    'CogStaff',
    'NonCogOfficial', 
    'MuniStaff',
    'Tenant',
    'OwnerOccupant',
    'OwnerNonOccupant',
    'Manager',
    'ElectedOfficial',
    'Public',
    'LawEnforcement',
    'Other'
    'ownerCntyLookup'
) ;

CREATE TYPE role AS ENUM (
    'Developer',
    'SysAdmin',
    'CogStaff',
    'EnforcementOfficial',
    'MuniStaff',
    'MuniReader',
    'Public'

) ;

CREATE TYPE casephase as ENUM
(
    'PrelimInvestigationPending',
    'NoticeDelivery',
    'InitialComplianceTimeframe',
    'SecondaryComplianceTimeframe',
    'AwaitingHearingDate',
    'HearingPreparation',
    'InitialPostHearingComplianceTimeframe',
    'SecondaryPostHearingComplianceTimeframe',
    'InactiveHolding',
    'Closed'
) ;

-- This event type enum is used by the case manager to determine how to process various event creation events
-- PhaseChange events require special processing on the case to determine the appropriate case status
-- to place the case after the event has been logged

CREATE TYPE ceEventType as ENUM
(
    'Origination',
    'Action',
    'PhaseChange',
    'Closing',
    'Timeline',
    'Communication',
    'Meeting',
    'Notice',
    'Custom',
    'Compliance'
) ;



CREATE SEQUENCE IF NOT EXISTS person_personIDSeq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

-- Storing various kinds of persons who aren't users in the system: see login for system users
CREATE TABLE person
(
    personID                   INTEGER DEFAULT nextval('person_personIDSeq') NOT NULL,
    personType                 personType,
    muni_muniCode              INTEGER NOT NULL,
    fName                      text NOT NULL,
    lName                      text NOT NULL,
    jobTitle                   text , 
    phoneCell                  text ,
    phoneHome                  text ,
    phoneWork                  text ,
    email                      text ,
    address_street             text ,
    address_city               text ,
    address_state              text DEFAULT 'PA',
    address_zip                text ,
    notes                      text,
    lastUpdated                TIMESTAMP WITH TIME ZONE,
    expiryDate                 TIMESTAMP WITH TIME ZONE,
    isActive                   boolean DEFAULT TRUE,
    isUnder18                  boolean DEFAULT FALSE,
    humanVerifiedby            INTEGER;


) ;

ALTER TABLE person ADD CONSTRAINT personID_pk PRIMARY KEY ( personID ) ;

ALTER TABLE person ADD CONSTRAINT municipality_fk FOREIGN KEY ( muni_muniCode ) REFERENCES municipality ( muniCode ) ;

ALTER TABLE person ADD CONSTRAINT verifier_fk FOREIGN KEY ( humanVerifiedby ) REFERENCES person ( personID ) ;



CREATE SEQUENCE IF NOT EXISTS actionrqstissuetype_issueTypeID_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE actionrqstissuetype
  (
    issueTypeID     INTEGER DEFAULT nextval('actionrqstissuetype_issueTypeID_seq') NOT NULL ,
    typeName        text ,
    typeDescription text ,
    muni_muniCode   INTEGER,
    notes           text
  ) ;

ALTER TABLE actionrqstissuetype ADD CONSTRAINT actionrqstissuetype_pk PRIMARY KEY ( issueTypeID ) ;

ALTER TABLE actionrqstissuetype ADD CONSTRAINT acrreqisstype_muniCode_fk FOREIGN KEY ( muni_muniCode ) REFERENCES municipality ( muniCode ) ;


--****************************************************************
--****************************PROPERTY TABLES ********************
--****************************************************************


CREATE SEQUENCE IF NOT EXISTS propertyusetype_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


CREATE TABLE propertyusetype
  (
    propertyUseTypeID       INTEGER DEFAULT nextval('propertyusetype_seq') NOT NULL ,
    name                    VARCHAR (50) NOT NULL ,
    description             VARCHAR (100)
  ) ;

ALTER TABLE propertyusetype ADD CONSTRAINT propertyusetype_pk PRIMARY KEY ( propertyUseTypeID ) ;


-- THE CENTRAL TABLE for the system: property. Note that properties don't have an autogenerated ID number
-- since this should exist outside the system and be transferred in. If there is conflict in these IDs, we've 
-- got to sort that out in t he data source
-- propertyusetype_propertyuseid
-- propertyUseType_UseID


CREATE TABLE property
  (
    propertyID                      INTEGER NOT NULL,
    municipality_muniCode           INTEGER, --fk
    parID                           text NOT NULL,
    lotAndBlock                     text NOT NULL,
    address                         text NOT NULL,
    -- changed during python period
    propertyUseType                 text,
    useGroup                        text,
    constructionType                text,
    countyCode                      text DEFAULT '02',
    apartmentNo                     INTEGER,  -- deprecated: keep for transfer from access db
    notes                           text,
    addr_city                       text,
    addr_state                      text,
    addr_zip                        text,
    ownercode                       text,
    propclass                       text,
    lastUpdated                     TIMESTAMP WITH TIME ZONE,
    lastUpdatedBy                   INTEGER,
    locationdescription             text
  ) ;

ALTER TABLE property ADD CONSTRAINT property_pk PRIMARY KEY ( propertyID ) ;

ALTER TABLE property ADD CONSTRAINT property_muniCode_fk FOREIGN KEY ( municipality_muniCode ) REFERENCES municipality (muniCode) ;

ALTER TABLE property ADD CONSTRAINT property_updatedBy_fk FOREIGN KEY ( lastUpdatedBy ) REFERENCES login (userid)) ;



-- not using a lookup table for these yet since there are "like" 200 values
-- ALTER TABLE property ADD CONSTRAINT property_propUseTypeID_fk FOREIGN KEY (propertyUseType_UseID) REFERENCES propertyusetype (propertyUseTypeID) ;


CREATE SEQUENCE IF NOT EXISTS propertyexternaldata_extDataID_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

CREATE TABLE propertyexternaldata
(
    extDataID                       INTEGER DEFAULT nextval('propertyexternaldata_extDataID_seq') NOT NULL,
    property_propertyID             INTEGER NOT NULL, -- fk
    salePrice                       NUMERIC,
    saleYear                        INTEGER,
    assessedLandValue               NUMERIC,
    assessedBuildingValue           NUMERIC,
    assessmentYear                  INTEGER,
    useCode                         text,
    yearBuilt                       INTEGER,
    livingArea                      INTEGER,
    condition                       text,
    taxStatus                       text ,
    taxStatusYear                   INTEGER,
    notes                           text,
    lastUpdated                     TIMESTAMP WITH TIME ZONE
) ;

ALTER TABLE propertyexternaldata ADD CONSTRAINT propertyexternaldata_extDataID_pk PRIMARY KEY (extDataID) ;

ALTER TABLE propertyexternaldata ADD CONSTRAINT propertyexternaldata_propID_fk FOREIGN KEY (property_propertyID) REFERENCES property ( propertyID );


CREATE SEQUENCE IF NOT EXISTS propattributeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;



CREATE TABLE propertyattribute
(
    attributeid                     INTEGER DEFAULT nextval('propattributeid_seq') PRIMARY KEY,
    property_propertyID             INTEGER NOT NULL CONSTRAINT propattr_propertyid_fk REFERENCES property (propertyID),
    propattrnameID                  INTEGER NOT NULL CONSTRAINT propattrname_attrid_fk REFERENCES propertyattributename (attrNameID),
    attrTextValue                   text,
    attrBooleanValue                boolean,
    requiresViewConfirmation        INTEGER CONSTRAINT propattr_requiresview_fk REFERENCES login (userid),
    viewConfirmed                   INTEGER CONSTRAINT propattr_viewconfirmed_fk REFERENCES login (userid),
    viewConfirmedTimeStamp          TIMESTAMP WITH TIME ZONE,
    notes                           text
);


CREATE SEQUENCE IF NOT EXISTS propattrnameid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;

CREATE TABLE propertyattributename
(
    attrNameID                      INTEGER DEFAULT nextval('propattrnameid_seq') PRIMARY KEY,
    attrName                        text,
    valueisboolean                  boolean,
    description                     text

);

CREATE SEQUENCE IF NOT EXISTS proplegacydata_dataid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

CREATE TABLE propertylegacydata
(
    dataid                          INTEGER DEFAULT nextval('proplegacydata_dataid_seq') PRIMARY KEY,
    parcelid                        character varying(30) CONSTRAINT prop_parcelid_fk REFERENCES property (parID)

);

CREATE SEQUENCE IF NOT EXISTS propertunit_unitID_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

-- property units are created on a by-property basis for use in CE and occ inspections 
-- A person of type tenant must have an associated unit for properties with multiple units
-- and this behavior is enforced by a required person types evaluated by the person coordinator

CREATE TABLE propertyunit
(
    unitID                          INTEGER DEFAULT nextval('propertunit_unitID_seq') NOT NULL,
    property_propertyID             INTEGER NOT NULL,
    unitNumber                      text,
    rental                          boolean,
    otherKnownAddress               text,
    notes                           text
) ;


ALTER TABLE propertyunit ADD CONSTRAINT unitID_pk PRIMARY KEY ( unitID );

ALTER TABLE propertyunit ADD CONSTRAINT propertyunit_propertyID FOREIGN KEY (property_propertyID) REFERENCES property (propertyID);

-- Bridge table between property and person to facilitate a many-to-many relationship (inward to outward crow's feet)
CREATE TABLE propertyperson
(
    property_propertyID            INTEGER NOT NULL, 
    person_personID                INTEGER NOT NULL

) ;

-- composite primary key
ALTER TABLE propertyperson ADD CONSTRAINT propertyperson_propID_pk PRIMARY KEY (property_propertyID, person_personID) ;

ALTER TABLE propertyperson add constraint propertyperson_propID_fk FOREIGN KEY (property_propertyID) REFERENCES property (propertyID) ;

ALTER TABLE propertyperson ADD CONSTRAINT propertyperson_personID_fk FOREIGN KEY (person_personID) REFERENCES person (personID) ;


-- bridge table between propertyunit and person
CREATE TABLE propertyunitperson
(
    propertyUnit_unitID             INTEGER NOT NULL,
    person_personID                 INTEGER NOT NULL
) ;

-- composite primary key
ALTER TABLE propertyunitperson ADD CONSTRAINT propertyunitperson_unitID_pk PRIMARY KEY ( propertyUnit_unitID, person_personID ) ;

ALTER TABLE propertyunitperson ADD CONSTRAINT propertyunitperson_unitID_fk FOREIGN KEY ( propertyUnit_unitID ) REFERENCES propertyunit (unitID) ;

ALTER TABLE propertyunitperson ADD CONSTRAINT propertyunitperson_personID_fk FOREIGN KEY ( person_personID ) REFERENCES person ( personID ) ;

-- ACTION REQUEST INFRASTRUCTURE


CREATE SEQUENCE IF NOT EXISTS ceactionrequest_requestid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

-- DB generator of seemingly inconsistent public control codes
-- Current implementation creates this in the server before inserting
-- CREATE SEQUENCE IF NOT EXISTS ceactionrequest__requestPublicc_seq
--     START WITH 1963
--     INCREMENT BY 27
--     MINVALUE 1963
--     NO MAXVALUE
--     CACHE 1;

-- Code enforcement action request stores data related to any submitted action request
-- Filling this out will generate an event 
-- NOTE: It is the job of the case table to know about the existence of a ceactionrequest entry

CREATE TABLE ceactionrequest
  (
    requestID                INTEGER DEFAULT nextval('ceactionrequest_requestid_seq') NOT NULL ,
    requestPublicCC          INTEGER , --DEFAULT nextval('ceactionrequest__requestPublicc_seq'),
    muni_muniCode            INTEGER NOT NULL ,
    property_propertyID      INTEGER ,
    issueType_issueTypeID    INTEGER NOT NULL ,
    actRequestor_requestorID INTEGER NOT NULL,
    cecase_caseID            INTEGER ,
    submittedTimestamp       TIMESTAMP WITH TIME ZONE NOT NULL ,
    dateOfRecord             TIMESTAMP WITH TIME ZONE NOT NULL ,
    notataddress             boolean ,  -- should read "atKnownAddress!!!!!!!!!!!! inserted as true = we've got a property attached"
    addressOfConcern         text ,  -- need to change to "location of conern" to not be confused with the property's address
    requestDescription       text NOT NULL ,
    isUrgent                 boolean DEFAULT FALSE ,
    anonymityRequested       boolean DEFAULT FALSE ,
    cogInternalNotes         text,
    muniInternalNotes        text,
    publicExternalNotes      text
  ) ;

ALTER TABLE ceactionrequest ADD CONSTRAINT ceactionrequest_requestID_pk PRIMARY KEY ( requestID ) ;

ALTER TABLE ceactionrequest ADD CONSTRAINT ceactionrequest_requestorID_fk FOREIGN KEY ( actrequestor_requestorID ) REFERENCES person ( personID ) ;

ALTER TABLE ceactionrequest ADD CONSTRAINT ceactionrequest_issueTypeID_fk FOREIGN KEY ( issueType_issueTypeID ) REFERENCES actionRqstIssueType ( issueTypeID ) ;

ALTER TABLE ceactionrequest ADD CONSTRAINT ceactionrequest_muni_fk FOREIGN KEY ( muni_muniCode ) REFERENCES municipality ( muniCode ) ;

ALTER TABLE ceactionrequest ADD CONSTRAINT ceactionrequest_prop_fk FOREIGN KEY ( property_propertyID ) REFERENCES property (propertyID) ;



-- *****************************************************************************


CREATE SEQUENCE IF NOT EXISTS codeset_codeSetID_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


CREATE TABLE codeset
(
    codeSetID                   INTEGER DEFAULT nextval('codeset_codeSetID_seq') NOT NULL,
    name                        text,
    description                 text,
    municipality_muniCode       INTEGER -- foreign key from municipality
) ;

ALTER TABLE codeset ADD CONSTRAINT codeset_codeSetID_pk PRIMARY KEY ( codeSetID) ;

ALTER TABLE codeset ADD CONSTRAINT codeset_muniCode_fk FOREIGN KEY (municipality_muniCode) REFERENCES municipality (muniCode) ;



CREATE SEQUENCE IF NOT EXISTS login_userid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


CREATE TABLE login
  (
    userID                     INTEGER DEFAULT nextval('login_userid_seq') NOT NULL ,
    userRole                   role NOT NULL,
    username                   text NOT NULL,
    password                   text NOT NULL,
    muni_muniCode              INTEGER NOT NULL, --Foreign Key from municipality
    defaultCodeSet             INTEGER,
    fName                      text NOT NULL ,
    lName                      text NOT NULL,
    workTitle                  text NOT NULL, 
    phoneCell                  text ,
    phoneHome                  text ,
    phoneWork                  text NOT NULL,
    email                      text NOT NULL,
    address_street             text ,
    address_city               text ,
    address_zip                text ,
    address_state              text DEFAULT 'PA',
    notes                      text,
    activityStartDate          TIMESTAMP WITH TIME ZONE NOT NULL, -- these could be used for tracking employee status
    activityStopDate           TIMESTAMP WITH TIME ZONE NOT NULL,
    accessPermitted            boolean DEFAULT TRUE,
    enforcementOfficial        boolean,
    badgeNumber                text,
    oriNumber                  text

  ) ;

ALTER TABLE login ADD CONSTRAINT login_pk PRIMARY KEY ( userid ) ;

ALTER TABLE login ADD CONSTRAINT login_muniCode_fk FOREIGN KEY ( muni_muniCode ) REFERENCES municipality ( muniCode ) ;

ALTER TABLE login ADD CONSTRAINT login_defaultCodeSet_fk FOREIGN KEY (defaultCodeSet) REFERENCES codeset (codeSetID) ;
 

CREATE SEQUENCE IF NOT EXISTS coglog_logeEntryID_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE coglog
(
    logEntryID              INTEGER DEFAULT nextval('coglog_logeEntryID_seq') NOT NULL,
    timeOfEntry             TIMESTAMP WITH TIME ZONE DEFAULT current_timestamp,
    user_userID             INTEGER ,-- fk 
    sessionID               INTEGER, 
    category                text,
    notes                   text                           

) ;

ALTER TABLE coglog ADD CONSTRAINT coglog_logentryID_pk PRIMARY KEY (logentryID) ;

ALTER TABLE coglog ADD CONSTRAINT coglog_user_userID_fk FOREIGN KEY (user_userID) REFERENCES login ( userID) ;


--*************** EVENTS ************************


-- Note that user created events will have IDs starting at 200. Pre-created events get manually assigned 
-- IDs under 200

CREATE SEQUENCE IF NOT EXISTS ceeventcategory_categoryID_seq
    START WITH 200
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE ceeventcategory 
(
    categoryID                   INTEGER DEFAULT nextval('ceeventcategory_categoryID_seq') NOT NULL,
    categoryType                 ceEventType NOT NULL,
    title                        text,
    description                  text,
    userDeployable               boolean DEFAULT TRUE,
    muniDeployable               boolean DEFAULT FALSE,
    publicDeployable             boolean DEFAULT FALSE,
    requiresViewConfirmation     boolean DEFAULT FALSE,
    notifyCaseMonitors           boolean DEFAULT FALSE,
    casePhaseChangeTrigger       boolean DEFAULT FALSE,
    hidable                      boolean DEFAULT FALSE


);

ALTER TABLE ceeventcategory ADD CONSTRAINT ceeventcategory_categoryID_pk PRIMARY KEY (categoryID);


CREATE SEQUENCE IF NOT EXISTS cecase_caseID_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

CREATE TABLE cecase 
(
    caseID                          INTEGER DEFAULT nextval('cecase_caseID_seq') NOT NULL,
    ceCasePublicCC                  INTEGER NOT NULL,
    property_propertyID             INTEGER NOT NULL,
    propertyunit_unitID             INTEGER , -- propertyunit foriegn key
    login_userID                    INTEGER , -- the case owner
    caseName                        text , -- this is a human-friendly case title
    casePhase                       casephase NOT NULL , -- the central case flow tracking field
    originationDate                 TIMESTAMP WITH TIME ZONE, -- inherited from the one event of type 'Origination'
    closingdate                     TIMESTAMP WITH TIME ZONE , -- inerited from the one event of type 'Closing'
    creationTimestamp               TIMESTAMP WITH TIME ZONE , -- added manually in test system
    notes                           text

) ;

ALTER TABLE cecase ADD CONSTRAINT cecase_caseID_pk PRIMARY KEY (caseID);

ALTER TABLE cecase ADD CONSTRAINT cecase_propertyID_fk FOREIGN KEY ( property_propertyID) REFERENCES property (propertyID) ;

-- I hope this doesn't create an unwanted cycle in the system. May need some debugging
ALTER TABLE cecase ADD CONSTRAINT cecase_unitID_fk FOREIGN KEY ( propertyunit_unitID ) REFERENCES propertyunit (unitID) ;

ALTER TABLE cecase ADD CONSTRAINT cecase_login_userID_fk FOREIGN KEY (login_userid) REFERENCES login (userID) ;

-- added after cecase to allow ddl script to run without error
ALTER TABLE ceactionrequest ADD CONSTRAINT ceactionrequest_caseID FOREIGN KEY (cecase_caseID) REFERENCES cecase (caseID);



CREATE SEQUENCE IF NOT EXISTS ceevent_eventID_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


-- events have names and event names have types that are used for processing events
CREATE TABLE ceevent
  (
    eventID                     INTEGER DEFAULT nextval('ceevent_eventID_seq') NOT NULL ,
    ceeventCategory_catID       INTEGER NOT NULL , -- fk from ceeventcateogry
    cecase_caseID               INTEGER NOT NULL , -- fk from cecase
    dateOfRecord                TIMESTAMP WITH TIME ZONE ,
    eventTimeStamp              TIMESTAMP WITH TIME ZONE ,
    eventDescription            text ,
    login_userid                INTEGER NOT NULL, --fk from login
    discloseToMunicipality      boolean DEFAULT TRUE,
    discloseToPublic            boolean DEFAULT FALSE, 
    activeEvent                 boolean DEFAULT TRUE, 
    requiresViewConfirmation    boolean DEFAULT FALSE,
    viewConfirmed               boolean DEFAULT FALSE,
    hidden                      boolean DEFAULT FALSE,
    notes                       text

  ) ;

ALTER TABLE ceevent ADD CONSTRAINT ceevent_eventID_pk PRIMARY KEY (eventID);

ALTER TABLE ceevent ADD CONSTRAINT ceevent_ceeventcategory_fk FOREIGN KEY ( ceeventCategory_catID ) REFERENCES ceeventcategory (categoryID) ;

ALTER TABLE ceevent ADD CONSTRAINT ceevent_ceCaseID_fk FOREIGN KEY (cecase_caseID) REFERENCES cecase (caseID) ;

ALTER TABLE ceevent ADD CONSTRAINT ceevent_login_userID FOREIGN KEY ( login_userid ) REFERENCES login (userID) ;

-- bridge table to allow many-to-many relationship between case and person
CREATE TABLE ceeventperson 
(
    ceevent_eventID             INTEGER NOT NULL ,
    person_personID             INTEGER NOT NULL
) ;

ALTER TABLE ceeventperson ADD CONSTRAINT ceeventperson_pk PRIMARY KEY (ceevent_eventID, person_personID) ;

ALTER TABLE ceeventperson ADD CONSTRAINT ceeventperson_ceevent_eventID_fk FOREIGN KEY ( ceevent_eventID ) REFERENCES ceevent ( eventID ) ;

ALTER TABLE ceeventperson ADD CONSTRAINT ceeventperson_person_personID_fk FOREIGN KEY ( person_personID ) REFERENCES person ( personID ) ;


CREATE SEQUENCE IF NOT EXISTS propevent_eventID_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

CREATE TABLE propertyevent
  (

    eventID                     INTEGER DEFAULT nextval('propevent_eventID_seq') CONSTRAINT propevent_eventID_pk PRIMARY KEY,
    ceeventCategory_catID       INTEGER NOT NULL CONSTRAINT propevent_ceeventcategory_fk REFERENCES ceeventcategory (categoryID),
    property_propertyID         INTEGER NOT NULL CONSTRAINT propevent_propertyid_fk REFERENCES property (propertyID),
    dateOfRecord                TIMESTAMP WITH TIME ZONE ,
    eventTimeStamp              TIMESTAMP WITH TIME ZONE ,
    eventDescription            text ,
    login_userid                INTEGER NOT NULL CONSTRAINT propevent_login_userID REFERENCES login (userID),
    discloseToMunicipality      boolean DEFAULT TRUE,
    discloseToPublic            boolean DEFAULT FALSE, 
    activeEvent                 boolean DEFAULT TRUE, 
    hidden                      boolean DEFAULT FALSE,
    notes                       text

  ) ;


-- ********************* VIOLATION CORNER *****************************************



CREATE SEQUENCE IF NOT EXISTS codesource_sourceID_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;

-- Stores information related to the origin of a set of municpal codes, such as the IPMC
CREATE TABLE codesource 
(
    sourceID        INTEGER DEFAULT nextval('codesource_sourceID_seq' ) NOT NULL,
    name            text NOT NULL,
    year            INTEGER NOT NULL,
    description     text,
    isActive        boolean DEFAULT TRUE,
    URL             text,
    notes           text

) ; 

ALTER TABLE codesource ADD CONSTRAINT codesource_sourceID_pk PRIMARY KEY ( sourceID);

CREATE SEQUENCE IF NOT EXISTS codeelementguide_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.codeelementguide
(
  guideEntryID integer NOT NULL DEFAULT nextval('codeelementguide_id_seq'::regclass),
  category text NOT NULL,
  subcategory text,
  description text,
  enforcementGuidelines text,
  inspectionGuidelines text,
  priority boolean,
  CONSTRAINT codeelementtype_cvtypeid_pk PRIMARY KEY (guideEntryID)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.codeelementguide
  OWNER TO sylvia;


CREATE SEQUENCE IF NOT EXISTS codeelement_elementID_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

-- stores data on individual code violations extracted from municipal code and adopted national/international code standards

CREATE TABLE codeelement
  (
    elementID                   INTEGER DEFAULT nextval('codeelement_elementID_seq') NOT NULL ,
    codeelementtype_cdelTypeID  INTEGER NOT NULL , -- fk from codeelementtype
    codesource_sourceID         INTEGER NOT NULL, -- fk from codesource
    ordchapterNo                INTEGER NOT NULL ,
    ordchapterTitle             text,
    ordSecNum                   text,
    ordsecTitle                 text,
    ordSubSecNum                text,
    ordSubSecTitle              text,
    ordTechnicalText            text NOT NULL,
    ordHumanFriendlyText        text,
    defaultPenalty              NUMERIC, -- fees, etc.
    isActive                    boolean DEFAULT TRUE ,
    isEnforcementPriority       boolean DEFAULT FALSE,
    resourceURL                 text ,
    inspectionTips              text ,
    dateCreated                 TIMESTAMP WITH TIME ZONE
  ) ;

ALTER TABLE codeelement ADD CONSTRAINT codeelement_pk PRIMARY KEY ( elementID ) ;

ALTER TABLE codeelement ADD CONSTRAINT codeelement_cdeltype_cdelTypeID_fk FOREIGN KEY (codeelementtype_cdelTypeID) REFERENCES codeelementtype (cdelTypeID) ;

ALTER TABLE codeelement ADD CONSTRAINT codeelement_codesource_sourceID FOREIGN KEY (codesource_sourceID) REFERENCES codesource (sourceID) ;

CREATE SEQUENCE IF NOT EXISTS codesetelement_elementid_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;


-- bridging table to allow for codeset and codelement many-many relationships
CREATE TABLE codesetelement
(
    codeSetElementID            INTEGER DEFAULT nextval('codesetelement_elementid_seq') NOT NULL, --pk
    codeset_codeSetID           INTEGER NOT NULL, --fk
    codelement_elementID        INTEGER NOT NULL, --fk
    elementMaxPenalty           NUMERIC,
    elementMinPenalty           NUMERIC,
    elementNormPenalty          NUMERIC NOT NULL,
    penaltyNotes                text,
    normDaysToComply            INTEGER NOT NULL,
    daysToComplyNotes           text
) ;

ALTER TABLE codesetelement ADD CONSTRAINT codesetelement_codeSetElementID_pk PRIMARY KEY ( codeSetElementID ) ;

ALTER TABLE codesetelement ADD CONSTRAINT codeseetelement_setID_fk FOREIGN KEY (codeset_codeSetID) REFERENCES codeset (codeSetID) ;

ALTER TABLE codesetelement ADD CONSTRAINT codeseetelement_elementID_fk FOREIGN KEY (codelement_elementID) REFERENCES codeelement (elementID) ;



CREATE SEQUENCE IF NOT EXISTS courtentity_entityID_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE courtentity
(
    entityID                   INTEGER DEFAULT nextval('courtentity_entityID_seq') NOT NULL,
    entityOfficialNum          text ,
    jurisdictionLevel          text NOT NULL,
    muni_muniCode              INTEGER , --fk
    name                       text NOT NULL ,
    address_street             text NOT NULL,
    address_city               text NOT NULL,
    address_zip                text NOT NULL,
    address_state              text NOT NULL,
    county                     text ,
    phone                      text ,
    URL                        text ,
    notes                      text 
) ;

ALTER TABLE courtentity ADD CONSTRAINT courtentity_entityID_pk PRIMARY KEY (entityID) ;

ALTER TABLE courtentity ADD CONSTRAINT courtentity_muniCode_fk FOREIGN KEY (muni_muniCode) REFERENCES municipality (muniCode);
    

CREATE SEQUENCE IF NOT EXISTS citationStatus_statusID_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE citationstatus
(
    statusID                INTEGER DEFAULT nextval('citationStatus_statusID_seq') CONSTRAINT citationStatus_statusID_pk PRIMARY KEY,
    statusName              text NOT NULL,
    description             text NOT NULL,

) ;

CREATE SEQUENCE IF NOT EXISTS citation_citationID_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

CREATE TABLE citation
(
    citationID                      INTEGER DEFAULT nextval('citation_citationID_seq') NOT NULL CONSTRAINT citation_citationID_pk PRIMARY KEY, 
    citationNo                      text, --collaborvely created with munis
    status_statusID                 INTEGER NOT NULL CONSTRAINT citation_citationStatusID_fk REFERENCES citationstatus (statusID), -- fx
    origin_courtentity_entityID     INTEGER NOT NULL CONSTRAINT citation_courtentity_entityID_fk REFERENCES courtentity ( entityID ) , --fk
    login_userID                    INTEGER NOT NULL CONSTRAINT citation_login_userID_login_fk REFERENCES login (userid), --fk
    dateOfRecord                    TIMESTAMP WITH TIME ZONE NOT NULL,
    transTimeStamp                  TIMESTAMP WITH TIME ZONE NOT NULL,
    isActive                        boolean DEFAULT TRUE,
    notes                           text
) ;


CREATE SEQUENCE IF NOT EXISTS citationviolation_cvid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

CREATE TABLE citationviolation
(
    citationviolationID             INTEGER DEFAULT nextval('citationviolation_cvid_seq') PRIMARY KEY,
    citation_citationID             INTEGER NOT NULL CONSTRAINT citationviolation_citationID_fk REFERENCES citation (citationID),
    codeviolation_violationID       INTEGER NOT NULL CONSTRAINT citationviolation_violationID_fk REFERENCES codeviolation (violationID)

) ;

-- Stores information related to which code sections are violated in each cecase item
-- this is a briding table to facilitate many-to-many relationships between case and codeelement
-- with some extra attributes to flesh out the association
CREATE SEQUENCE IF NOT EXISTS codeviolation_violationID_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

CREATE TABLE codeviolation
  (
    violationID                 INTEGER DEFAULT nextval('codeviolation_violationID_seq') NOT NULL,
    codeSetElement_elementID    INTEGER NOT NULL , -- foreign key from codeelement
    cecase_caseID               INTEGER NOT NULL , -- foreign key from cecase
    dateOfRecord                TIMESTAMP WITH TIME ZONE,
    entryTimeStamp              TIMESTAMP WITH TIME ZONE NOT NULL,
    stipulatedComplianceDate    TIMESTAMP WITH TIME ZONE NOT NULL, -- auto generated base on the default compliance timeframe for each violation
    actualCompliancDate         TIMESTAMP WITH TIME ZONE , -- entered when a violationComplianceEvent is generated
    penalty                     NUMERIC NOT NULL,
    description                 text NOT NULL,
    notes                       text
    -- needs a timestamp field
  ) ;

ALTER TABLE codeviolation ADD CONSTRAINT codeviolation_pk PRIMARY KEY ( violationID ) ;

ALTER TABLE codeviolation ADD CONSTRAINT codeviolation_cdsetel_elementID_fk FOREIGN KEY (codeSetElement_elementID) REFERENCES codesetelement ( codeSetElementID ) ;

ALTER TABLE codeviolation ADD CONSTRAINT codeviolation_caseID_fk FOREIGN KEY (cecase_caseID) REFERENCES cecase (caseID) ;

ALTER TABLE codeviolation ADD CONSTRAINT codeviolation_citationID_fk FOREIGN KEY ( citation_citationID ) REFERENCES citation (citationID) ;


-- Photos and document blob tables and connectors

-- Linking table between property and its related blobs


CREATE SEQUENCE IF NOT EXISTS photodoc_photoDocID_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

CREATE TABLE photodoc
  (
    photoDocID          INTEGER DEFAULT nextval('photodoc_photoDocID_seq') NOT NULL ,
    photoDocDescription VARCHAR (100) ,
    photoDocDate        TIMESTAMP WITH TIME ZONE ,
    photoDocType_typeID INTEGER NOT NULL ,
    photoDocBlob        bytea
  ) ;
ALTER TABLE photoDoc ADD CONSTRAINT photoDoc_pk PRIMARY KEY ( photoDocID ) ;


CREATE TABLE propertyphotodoc
  (
    photodoc_photoDocID INTEGER NOT NULL ,
    property_propertyID INTEGER NOT NULL
  ) ;

ALTER TABLE propertyPhotoDoc ADD CONSTRAINT propertyPhotoDoc_pk PRIMARY KEY ( photoDoc_photoDocID, property_propertyID ) ;

ALTER TABLE propertyphotodoc ADD CONSTRAINT propertyphotodoc_pdid_fk FOREIGN KEY (photodoc_photoDocID) REFERENCES photodoc (photoDocID);

ALTER TABLE propertyphotodoc ADD CONSTRAINT propertyphotodoc_prop_fk FOREIGN KEY (property_propertyID) REFERENCES property (propertyID);




CREATE SEQUENCE IF NOT EXISTS blockcategory_categoryid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE textblockcategory 
(
    categoryID              INTEGER DEFAULT nextval('blockcategory_categoryid_seq') NOT NULL CONSTRAINT blockCategory_catID_pk PRIMARY KEY,
    cateogryTitle           text NOT NULL
)



CREATE SEQUENCE IF NOT EXISTS textblock_blockid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE textblock
(
    blockID                     INTEGER DEFAULT nextval('textblock_blockid_seq') NOT NULL CONSTRAINT textblock_blockID_pk PRIMARY KEY,
    blockCategory_catID         INTEGER NOT NULL CONSTRAINT blockCategory_catID_FK REFERENCES blockcategory (categoryid),
    muni_muniCode               INTEGER NOT NULL CONSTRAINT muni_muniCode_fk REFERENCES municipality (muniCode),
    blockName                   text NOT NULL,
    blockText                   text NOT NULL
)

CREATE SEQUENCE IF NOT EXISTS noticeofviolation_noticeID_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


CREATE TABLE noticeofviolation
  (
    noticeID                    INTEGER DEFAULT nextval('noticeofviolation_noticeID_seq') NOT NULL,
    caseID                      INTEGER NOT NULL,
    personid_recipient          INTEGER NOT NULL CONSTRAINT noticeOfViolation_recipient_fk REFERENCES person (personID),
    letterText                  text,
    insertionTimeStamp          TIMESTAMP WITH TIME ZONE NOT NULL,
    dateOfRecord                TIMESTAMP WITH TIME ZONE NOT NULL,
    requestToSend               boolean,
    letterSent                  boolean,
    letterSendDate              TIMESTAMP WITH TIME ZONE,
    letterReturnedDate          TIMESTAMP WITH TIME ZONE

  ) ;

ALTER TABLE noticeofviolation ADD CONSTRAINT noticeViolation_noticeID_pk PRIMARY KEY ( noticeID ) ;

ALTER TABLE noticeofviolation ADD CONSTRAINT noticeOfViolationCaseID_fk FOREIGN KEY (caseID) REFERENCES cecase (caseID) ;


-- ****************  OCCUPANCY INSPECTIONS  **************************

CREATE SEQUENCE IF NOT EXISTS occupancyinspectionID_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

CREATE TABLE occupancyinspection
(
    inspectionID                    INTEGER DEFAULT nextval('occupancyinspectionID_seq') NOT NULL,
    propertyUnitID                  INTEGER NOT NULL, --fk 
    status                          INTEGER NOT NULL CONSTRAINT occInspec_status_statusid_fk REFERENCES occupancyinspectionstatus (statusid),
    login_userID                    INTEGER NOT NULL, --fk
    inspectionCreationTimestamp     TIMESTAMP WITH TIME ZONE,
    firstInspectionDate             TIMESTAMP WITH TIME ZONE,
    firstInspectionPass             boolean DEFAULT FALSE,
    secondInspectionDate            TIMESTAMP WITH TIME ZONE,
    secondInspectionPass            boolean DEFAULT FALSE,
    assignedFee                     INTEGER NOT NULL,
    totalFeePaid                    boolean DEFAULT FALSE,
    notes                           text,
    publicaccesscc                  INTEGER,
    enablepacc                      boolean DEFAULT TRUE,
    muniauthgrantedby               INTEGER CONSTRAINT occInspec_muniauthgrantedby_fk REFERENCES login (userID),
    muniauthtimestamp               TIMESTAMP WITH TIME ZONE,
    muniauthnotes                   text


) ;

ALTER TABLE occupancyinspection ADD CONSTRAINT occInspec_inspectionID PRIMARY KEY (inspectionID);

ALTER TABLE occupancyinspection ADD CONSTRAINT occInspec_login_userID_fk FOREIGN KEY (login_userID) REFERENCES login (userid);

ALTER TABLE occupancyinspection ADD CONSTRAINT occInspec_propUnit_fk FOREIGN KEY (propertyUnitID) REFERENCES propertyunit (unitID);

ALTER TABLE occupancyinspection ADD CONSTRAINT occInspec_assignedFee_fk FOREIGN KEY (assignedFee) REFERENCES occinspectionfee (feeID);



CREATE SEQUENCE IF NOT EXISTS occupancyinspectionstatusID_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE occupancyinspectionstatus
(
    statusid                        INTEGER DEFAULT nextval('occupancyinspectionstatusID_seq') PRIMARY KEY,
    title                           text,
    description                     text

) ;


CREATE SEQUENCE IF NOT EXISTS spacetype_spacetypeid_seq 
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


CREATE TABLE spacetype
(
    spaceTypeID                     INTEGER DEFAULT nextval('spacetype_spacetypeid_seq') NOT NULL,
    spaceTitle                      text NOT NULL,
    description                     text NOT NULL
) ;

ALTER TABLE spacetype ADD CONSTRAINT spacetype_spaceTypeID_pk PRIMARY KEY (spaceTypeID) ;

CREATE SEQUENCE IF NOT EXISTS spacetypeice_spacetypeiceid_seq 
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


CREATE TABLE spacetypeice 
(
    spaceTypeIceID                  INTEGER NOT NULL PRIMARY KEY,
    spaceTypeID                     INTEGER NOT NULL, --fk
    codeelement_eleid                INTEGER NOT NULL, --fk
    notes                           text

) ;

ALTER TABLE spacetypeice ADD CONSTRAINT spaceType_typeID_fk FOREIGN KEY ( spaceTypeID ) REFERENCES spacetype (spaceTypeID) ;

ALTER TABLE spacetypeice ADD CONSTRAINT inspectablecodelementid_fk FOREIGN KEY (codeelement_eleid)  REFERENCES codeelement (elementid);

CREATE SEQUENCE IF NOT EXISTS checklist_checklistID_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;

CREATE TABLE inspectionchecklist
(
    checklistID                         INTEGER DEFAULT nextval('checklist_checklistID_seq') NOT NULL,
    title                               text NOT NULL,
    description                         text NOT NULL,
    muni_muniCode                       INTEGER NOT NULL CONSTRAINT inspectionChecklist_muni_fk REFERENCES municipality (muniCode);
) ;

ALTER TABLE inspectionchecklist ADD CONSTRAINT inspectionchecklist_checklistID_pk PRIMARY KEY (checklistID);

CREATE SEQUENCE IF NOT EXISTS chklistSTICEID_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


CREATE TABLE checklistspacetypeice
(
  chkliststiceid                        integer NOT NULL DEFAULT nextval('chkliststiceid_seq'::regclass),
  chklist_checklistid                   integer NOT NULL,
  spacetypeice_typeid                   integer NOT NULL,
  required                              boolean,
  
  CONSTRAINT chkliststiceid_pk PRIMARY KEY (chkliststiceid),

  CONSTRAINT cklist_spacetypeice_checklistid_fk FOREIGN KEY (chklist_checklistid)
      REFERENCES public.inspectionchecklist (checklistid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,

  CONSTRAINT cklist_spacetypeice_fk FOREIGN KEY (spacetypeice_typeid)
      REFERENCES public.spacetypeice (spacetypeiceid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


CREATE SEQUENCE IF NOT EXISTS inspectedspacetypeelement_inspectedstelid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE inspectedspacetypeelement
(
    inspectedstelid                             INTEGER DEFAULT nextval('inspectedspacetypeelement_inspectedstelid_seq') CONSTRAINT inspectedspacetypeice_inspectedsticeID PRIMARY KEY,
    inspection_inspectionID                     INTEGER NOT NULL CONSTRAINT inspectedSTICE_inspection_inspectionID_fk REFERENCES occupancyinspection (inspectionID),
    chklistSTICE_ID                             INTEGER NOT NULL CONSTRAINT inspectedSTICE_chklistSTICE_ID_fk REFERENCES checklistspacetypeice (chklistSTICEID),
    compliance                                  boolean default FALSE,
    complianceDate                              TIMESTAMP WITH TIME ZONE,
    notes                                       text
) ;


CREATE SEQUENCE IF NOT EXISTS occinspectionfee_feeID_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;

-- for Adam
CREATE TABLE occinspectionfee
(
    feeID                           INTEGER DEFAULT nextval('occinspectionfee_feeID_seq') CONSTRAINT occinspecfee_feeID_pk PRIMARY KEY,
    muni_muniCode                   INTEGER NOT NULL CONSTRAINT muni_muniCode_fk REFERENCES municipality (muniCode),
    feeName                         text NOT NULL,
    feeAmount                       MONEY NOT NULL,
    effectiveDate                   TIMESTAMP WITH TIME ZONE NOT NULL,
    expiryDate                      TIMESTAMP WITH TIME ZONE NOT NULL,
    notes                           text

) ;

CREATE SEQUENCE IF NOT EXISTS occupancyPermitType_typeID_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;

-- for Adam
CREATE TABLE occpermittype
(
    typeID                          INTEGER DEFAULT nextval('occupancyPermitType_typeID_seq') CONSTRAINT occpermittype_typeID_pk PRIMARY KEY,
    muni_muniCode                   INTEGER NOT NULL,
    typeName                        text NOT NULL,
    typeDescription                 text NOT NULL,
    defaultValidityLengthDays       INTEGER,
    requiresPassedInspection        BOOLEAN DEFAULT TRUE

) ;

ALTER TABLE occpermittype ADD CONSTRAINT occpermittype_muniCode_fk FOREIGN KEY (muni_muniCode) REFERENCES municipality (muniCode) ;

CREATE SEQUENCE IF NOT EXISTS occupancyPermit_permitID_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;

-- for Adam
CREATE TABLE occupancypermit
(
    permitID                        INTEGER DEFAULT nextval('occupancyPermit_permitID_seq') CONSTRAINT occpermit_permitID_pk PRIMARY KEY,
    referenceNo                     text,
    occInspec_inspectionID          INTEGER NOT NULL CONSTRAINT occpermit_inspectionID_fk REFERENCES occupancyinspection (inspectionID) ,
    permitType                      INTEGER NOT NULL CONSTRAINT occpermit_permitType_fk REFERENCES occpermittype,
    dateIssued                      TIMESTAMP WITH TIME ZONE NOT NULL,
    dateExpires                     TIMESTAMP WITH TIME ZONE,
    issuedUnder                     INTEGER NOT NULL CONSTRAINT codesource_sourceID_fk REFERENCES codesource,
    specialConditions               text,
    notes                           text

) ;




CREATE SEQUENCE IF NOT EXISTS occpermitpublicreason_reasonID_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;

CREATE TABLE occpermitapplicationreason
(
    reasonID                        INTEGER DEFAULT nextval('occpermitpublicreason_reasonID_seq') CONSTRAINT occPermitReason_reasonID_pk PRIMARY KEY,
    reasonTitle                     text NOT NULL,
    reasonDescription               text NOT NULL,
    activeReason                    boolean DEFAULT TRUE
) ;



CREATE SEQUENCE IF NOT EXISTS occPermitApp_appID_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


CREATE TABLE occupancypermitapplication
(
    applicationID                   INTEGER DEFAULT nextval('occPermitApp_appID_seq') CONSTRAINT occpermitapp_applicationid_pk PRIMARY KEY,
    -- note that this is unverified and thus cannot be inserted into the system by the applicant. 
    -- code officer needs to check this manually against existing units and add this unit as needed when processing the application
    propertyUnitID                  text, 
    unitConnected                   boolean DEFAULT TRUE,
    reason_reasonID                 INTEGER NOT NULL CONSTRAINT reason_reasonID_fk REFERENCES occpermitapplicationreason (reasonID),
    submissionTimestamp             TIMESTAMP WITH TIME ZONE NOT NULL, 
    currentOwner_personID           INTEGER NOT NULL CONSTRAINT currOwner_personID_fk REFERENCES person (personID),
    contatPerson_personID           INTEGER NOT NULL CONSTRAINT contactPerson_personID_fk REFERENCES person (personID),
    newOccupant_personID            INTEGER CONSTRAINT newOccupant_personID_fk REFERENCES person (personID),
    newOwner_personID               INTEGER CONSTRAINT newOwner_personID_fk REFERENCES person (personID),
    occInspec_inspectionID          INTEGER CONSTRAINT occInspec_inspecID_fk REFERENCES occupancyinspection (inspectionID),
    numOccsUnder18                  INTEGER NOT NULL,
    submitterNotes                  text,
    internalnotes                   text


) ;

CREATE SEQUENCE IF NOT EXISTS paymentType_typeID_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;

-- for Adam
CREATE TABLE paymenttype
(
    typeId                          INTEGER DEFAULT nextval('paymentType_typeID_seq') CONSTRAINT pmttype_typeID_pk PRIMARY KEY ,
    pmtTypeTitle                    text NOT NULL
) ;


CREATE SEQUENCE IF NOT EXISTS payment_paymentID_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


-- payment
CREATE TABLE payment
  (
    paymentId              INTEGER DEFAULT nextval('payment_paymentID_seq') CONSTRAINT payment_paymentID_pk PRIMARY KEY ,
    occInspec_inspectionID INTEGER NOT NULL CONSTRAINT occInspection_inspectionID_fk REFERENCES occupancyinspection (inspectionID) ,
    paymentType_typeId     INTEGER NOT NULL CONSTRAINT payment_paymenttypeID_fk REFERENCES paymenttype (typeId),
    dateReceived           TIMESTAMP WITH TIME ZONE NOT NULL, 
    dateDeposited          TIMESTAMP WITH TIME ZONE NOT NULL,
    amount                 MONEY NOT NULL,
    payerID                INTEGER NOT NULL CONSTRAINT payerID_person_fk REFERENCES person, -- personFK
    referenceNum           text,
    checkno                INTEGER,
    cleared                boolean DEFAULT FALSE,
    notes                  text,
    recordedby             integer CONSTRAINT payment_recordedby_login_fk FOREIGN KEY (recordedby) REFERENCES public.login (userid) MATCH SIMPLE
                                    ON UPDATE NO ACTION ON DELETE NO ACTION,
    entrytimestamp         timestamp with time zone NOT NULL DEFAULT now(),
  ) ;


CREATE TABLE improvementtype
(
    typeid                  INTEGER PRIMARY KEY,
    typeTitle               text,
    typeDescription         text

) ;

CREATE TABLE improvementStatus
(
    statusID                INTEGER PRIMARY KEY,
    statusTitle             text,
    statusDescription       text

) ;


CREATE SEQUENCE IF NOT EXISTS improvementID_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


CREATE TABLE improvementsuggestion
(
    improvementID               INTEGER DEFAULT nextval('improvementID_seq') PRIMARY KEY,
    improvementTypeID           INTEGER NOT NULL CONSTRAINT imptype_fk REFERENCES improvementType (typeid),
    improvementSuggestionText   text NOT NULL,
    improvementReply            text,
    statusID                    INTEGER NOT NULL CONSTRAINT imptstatus_fk REFERENCES improvementStatus (statusID),
    submitterID                 INTEGER NOT NULL CONSTRAINT submitter_fk REFERENCES login (userid),
    submissionTimestamp         TIMESTAMP WITH TIME ZONE
);


CREATE SEQUENCE IF NOT EXISTS listitemchange_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;



CREATE TABLE listchangerequest
(
    changeID                    INTEGER DEFAULT nextval('listitemchange_seq') PRIMARY KEY,
    changeText                  text

) ;

CREATE SEQUENCE IF NOT EXISTS spaceid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


CREATE TABLE space
(
    spaceID                     INTEGER DEFAULT nextval('spaceID_seq') PRIMARY KEY,
    name                        text NOT NULL,
    spaceType                   INTEGER NOT NULL CONSTRAINT spacetype_fk REFERENCES spacetype (spacetypeid)
);



-- COMMIT;

