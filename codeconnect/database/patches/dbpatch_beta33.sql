-- ****************************************************************************
-- PATCH 33
-- "LATE OCT 2020" launch changes

-- *************

ALTER TABLE public.codeviolation
    ADD COLUMN nullifiedts TIMESTAMP WITH TIME ZONE;

ALTER TABLE public.codeviolation
    ADD COLUMN nullifiedby INTEGER CONSTRAINT codeviolation_nullifiedby_fk REFERENCES login(userid);


-- RUN ON REMOTE TO HERE
-- RUN LOCALLY TO HERE

ALTER TABLE textblock 
    ADD COLUMN injectabletemplate BOOLEAN DEFAULT FALSE;

ALTER TABLE noticeofviolation 
    ADD COLUMN injectviolations BOOLEAN DEFAULT FALSE;

ALTER TABLE noticeofviolation 
    DROP COLUMN injectviolations;

ALTER TABLE noticeofviolation
    ADD COLUMN followupevent_eventid INTEGER CONSTRAINT nov_followup_eventid_fk REFERENCES event (eventid);

ALTER TABLE muniprofile 
    ADD COLUMN novfollowupdefaultdays INTEGER DEFAULT 20;

INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors, 
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal, 
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins, 
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (223, 'Timeline'::eventtype, 'Followup on NOV' , 'NOV compliance window has expired', FALSE, 
            TRUE, 10, 1, 1, 
            'Follow up on notice of violation', NULL, 30, 
            TRUE, 4, 4, 4);


INSERT INTO public.textblockcategory(
            categoryid, categorytitle, icon_iconid, muni_municode)
    VALUES (101, "Injectable Template", 10, 999);


-- COMMENTS FROM SNAPPER
-- Person column Ghosts and clones don't seem to make much sense
-- ECD: Ghost (static copy never to change for archival purposes)
-- Serializing a record to a single cell or separate table, instead 
-- of the more complicated recursive foreign key to a copy record
-- Collapse first and last name
-- Dealing with edge case: single phsical location with same street
-- name but different suffix (apple st. and apple ave)

-- design criteria
-- A single parcel can contain multiple mailing unit (i.e. 1114, 1116, 1118 

-- Largo Way, also had 1115-1121 Morrell ave)
-- Accommodtate a single address (930 Punta Gorda) that contains multiple

-- units (which are also mailing units) but their distinction is 
-- by unit number (not street address), suffix of street address

-- A single human (A legal human entity) can be associated with multiple
-- mailing addresses, each of which could be in a different role (i.e. owner
-- owner of parcel at address X, receives tax mailing at address Y, 
-- and lives at address Z)
-- Needs: human deceased date

-- ASSUMPTION: an address listed as 1115-1121 would be interpolated only as 
-- the odds: 1115, 1117, 1119, 1121. SEAN: Is this accurate?

-- TODO: defined workflow for pyparcelupdate info update by month (step-by-step, 
-- get data from where, do what with it, store the result where?, 
-- compare to what, made determination, etc.)
-- This will involve clones since changes are written to clones, not
-- the "real" person
-- TODO: Deal with the WPRDC dump that stores the second
-- part of an addrss range in field called PROPERTYFRACTION


CREATE SEQUENCE IF NOT EXISTS parcel_parcelkey_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.parcel
    (
        parcelkey               INTEGER PRIMARY KEY DEFAULT nextval('parcel_parcelkey_seq'),
        parcelidcnty            TEXT,
        muni_municode           INTEGER NOT NULL CONSTRAINT parcel_municode_fk REFERENCES municipality (municode),
        source_sourceid         INTEGER CONSTRAINT parcel_sourceid_fk REFERENCES public.bobsource (sourceid),
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER CONSTRAINT parcel_createdby_userid_fk REFERENCES login (userid),     
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT parcel_lastupdatdby_userid_fk REFERENCES login (userid),
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER CONSTRAINT parcel_deactivatedby_userid_fk REFERENCES login (userid),    
        notes                   TEXT
    );
    


CREATE SEQUENCE IF NOT EXISTS parcelinfo_infoid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.parcelinfo
    (
        parcelinfoid            INTEGER PRIMARY KEY DEFAULT nextval('parcelinfo_infoid_seq'),
        parcel_parcelkey        INTEGER CONSTRAINT parcelinfo_parcelkey_fk REFERENCES parcel (parcelkey),
      usegroup text,
      constructiontype text,
      countycode text DEFAULT '02'::text,
      notes text,
      ownercode text,
      propclass text,
      locationdescription text,
      bobsource_sourceid integer,
      unfitdatestart timestamp with time zone,
      unfitdatestop timestamp with time zone,
      unfitby_userid integer,
      abandoneddatestart timestamp with time zone,
      abandoneddatestop timestamp with time zone,
      abandonedby_userid integer,
      vacantdatestart timestamp with time zone,
      vacantdatestop timestamp with time zone,
      vacantby_userid integer,
      condition_intensityclassid integer,
      landbankprospect_intensityclassid integer,
      landbankheld boolean DEFAULT false,
      nonaddressable boolean DEFAULT false,
      usetype_typeid integer,
    createdts               TIMESTAMP WITH TIME ZONE,
    createdby_userid        INTEGER CONSTRAINT parcelinfo_createdby_userid_fk REFERENCES login (userid),     
    lastupdatedts           TIMESTAMP WITH TIME ZONE,
    lastupdatedby_userid    INTEGER CONSTRAINT parcelinfo_lastupdatdby_userid_fk REFERENCES login (userid),
    deactivatedts           TIMESTAMP WITH TIME ZONE,
    deactivatedby_userid    INTEGER CONSTRAINT parcelinfo_deactivatedby_userid_fk REFERENCES login (userid),    
  
  CONSTRAINT parcel_abandoned_userid_fk FOREIGN KEY (abandonedby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT parcel_bobsourceid_fk FOREIGN KEY (bobsource_sourceid)
      REFERENCES public.bobsource (sourceid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT parcel_conditionintensityclass_classid_fk FOREIGN KEY (condition_intensityclassid)
      REFERENCES public.intensityclass (classid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT parcel_landbankprospect_classid_fk FOREIGN KEY (landbankprospect_intensityclassid)
      REFERENCES public.intensityclass (classid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT parcel_parcelusetypeid_fk FOREIGN KEY (usetype_typeid)
      REFERENCES public.propertyusetype (propertyusetypeid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT parcel_unfitby_userid_fk FOREIGN KEY (unfitby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT parcel_vacant_userid_fk FOREIGN KEY (vacantby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
    
    );


CREATE SEQUENCE IF NOT EXISTS human_humanid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.human
    (
        humanid                 INTEGER PRIMARY KEY DEFAULT nextval('human_humanid_seq'),
        name                    TEXT NOT NULL,
        dob                     DATE,
        under18                 BOOLEAN,
        jobtitle                TEXT,
        businessentity          BOOLEAN DEFAULT FALSE,
        multihuman              BOOLEAN DEFAULT FALSE,
        source_sourceid         INTEGER CONSTRAINT human_sourceid_fk REFERENCES public.bobsource (sourceid),
        deceaseddate            DATE,
        deceasedby_userid       INTEGER CONSTRAINT human_deceasedby_userid_fk REFERENCES login (userid),
        cloneof_humanid         INTEGER CONSTRAINT human_clone_humanid_fk REFERENCES human (humanid),
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER CONSTRAINT human_createdby_userid_fk REFERENCES login (userid),     
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT human_lastupdatdby_userid_fk REFERENCES login (userid),
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER CONSTRAINT human_deactivatedby_userid_fk REFERENCES login (userid),           
        notes                   TEXT
    );


CREATE SEQUENCE IF NOT EXISTS parcelhumanrole_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.parcelhumanrole
    (
        roleid              INTEGER PRIMARY KEY DEFAULT nextval('parcelhumanrole_roleid_seq'),
        title               TEXT NOT NULL,
        description         TEXT,
        muni_municode       INTEGER CONSTRAINT parcelhumanrole_municode_fk REFERENCES municipality (municode),
        deactivatedts       TIMESTAMP WITH TIME ZONE

    ); 

-- code enf case is still based on a parcel

CREATE TABLE public.parcelhuman
    (
        human_humanid           INTEGER CONSTRAINT parcelhuman_humanid_fk REFERENCES human (humanid),
        parcel_parcelkey        INTEGER CONSTRAINT parcel_parcelkey_fk REFERENCES parcel (parcelkey),
        source_sourceid         INTEGER CONSTRAINT parcel_sourceid_fk REFERENCES public.bobsource (sourceid),
        role_roleid             INTEGER CONSTRAINT parcelhuman_roleid_fk   REFERENCES parcelhumanrole (roleid),
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER CONSTRAINT parcelhuman_createdby_userid_fk REFERENCES login (userid),     
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT parcelhuman_lastupdatdby_userid_fk REFERENCES login (userid),
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER CONSTRAINT parcelhuman_deactivatedby_userid_fk REFERENCES login (userid),
        notes                   TEXT    
    );  



CREATE SEQUENCE IF NOT EXISTS humanmuni_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.humanmuni
(
    linkid              INTEGER PRIMARY KEY DEFAULT nextval('humanmuni_linkid_seq'),
    persontype          persontype NOT NULL,
    human_humanid       INTEGER CONSTRAINT humanmuni_muniid_fk REFERENCES human (humanid),
    muni_municode       INTEGER CONSTRAINT humanmuni_municode_fk REFERENCES municipality (municode),
    createdts               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    deactivatedts           TIMESTAMP WITH TIME ZONE,
    notes                TEXT

);

CREATE SEQUENCE IF NOT EXISTS contactphonetype_typeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.contactphonetype 
    (
        phonetypeid           INTEGER PRIMARY KEY DEFAULT nextval('contactphonetype_typeid_seq'),
        title                   TEXT,
        createdts               TIMESTAMP WITH TIME ZONE,
        deactivatedts           TIMESTAMP WITH TIME ZONE

    );

CREATE SEQUENCE IF NOT EXISTS contactphone_phoneid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.contactphone
    (
        phoneid                 INTEGER PRIMARY KEY DEFAULT nextval('contactphone_phoneid_seq'),
        human_humanid           INTEGER NOT NULL CONSTRAINT contactemail_humanid_fk REFERENCES human (humanid),
        phonenumber             TEXT NOT NULL,
        phoneext                INTEGER,
        phonetype_typeid        INTEGER CONSTRAINT contactphone_typeid_fk REFERENCES contactphonetype (phonetypeid),
        disconnectts            TIMESTAMP WITH TIME ZONE,
        disconnect_userid       INTEGER CONSTRAINT phone_disconnected_userid_fk REFERENCES login (userid),    
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER CONSTRAINT phone_createdby_userid_fk REFERENCES login (userid),     
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT phone_lastupdatdby_userid_fk REFERENCES login (userid),
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER CONSTRAINT phone_deactivatedby_userid_fk REFERENCES login (userid),           
        notes                   TEXT
    );

CREATE SEQUENCE IF NOT EXISTS contactemail_emailid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.contactemail
    (
        emailid                 INTEGER PRIMARY KEY DEFAULT nextval('contactemail_emailid_seq'),
        human_humanid           INTEGER NOT NULL CONSTRAINT contactemail_humanid_fk REFERENCES human (humanid),
        emailaddress            TEXT NOT NULL,
        bouncets                TIMESTAMP WITH TIME ZONE,
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER CONSTRAINT email_createdby_userid_fk REFERENCES login (userid),     
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT email_lastupdatdby_userid_fk REFERENCES login (userid),
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER CONSTRAINT email_deactivated_userid_fk REFERENCES login (userid),           
        notes                   TEXT
    );





CREATE SEQUENCE IF NOT EXISTS mailingaddress_addressid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE mailingaddress
    (
        addressid               INTEGER PRIMARY KEY DEFAULT nextval('mailingaddress_addressid_seq'),
        addressnum              TEXT,
        street                  TEXT,
        unitno                  TEXT,
        city                    TEXT,
        state                   TEXT,
        zipcode                 TEXT,
        pobox                   INTEGER,
        verifiedts              TIMESTAMP WITH TIME ZONE,
        source_sourceid         INTEGER CONSTRAINT mailingaddress_sourceid_fk REFERENCES public.bobsource (sourceid),
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER CONSTRAINT mailingaddress_createdby_userid_fk REFERENCES login (userid),     
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT mailingaddress_lastupdatedby_userid_fk REFERENCES login (userid),
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER CONSTRAINT mailingaddress_deactivatedby_userid_fk REFERENCES login (userid),           
        notes                   TEXT

    );



CREATE TABLE public.humanmailingaddress
    (
        humanmailing_humanid          INTEGER CONSTRAINT humanmailing_humanid_fk REFERENCES human (humanid),
        humanmailing_addressid         INTEGER CONSTRAINT humanmailing_addressid_fk REFERENCES mailingaddress (addressid),
        source_sourceid                 INTEGER CONSTRAINT humanmailing_sourceid_fk REFERENCES public.bobsource (sourceid),
        createdts                       TIMESTAMP WITH TIME ZONE,
        createdby_userid                INTEGER CONSTRAINT humanmailing_createdby_userid_fk REFERENCES login (userid),     
        deactivatedts                   TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid            INTEGER CONSTRAINT humanmailing_deactivatedby_userid_fk REFERENCES login (userid),  
        notes                           TEXT         


    );

CREATE SEQUENCE IF NOT EXISTS humanmailing_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.humanmailingrole
    (
        roleid              INTEGER PRIMARY KEY DEFAULT nextval('humanmailing_roleid_seq'),
        title               TEXT NOT NULL,
        createdts          TIMESTAMP WITH TIME ZONE,
        description         TEXT,
        muni_municode       INTEGER CONSTRAINT humanmailing_municode_fk REFERENCES municipality (municode),
        deactivatedts       TIMESTAMP WITH TIME ZONE,
        notes               TEXT

    ); 


CREATE TABLE public.mailingaddressparcel
    (
        mailingparcel_parcelid      INTEGER CONSTRAINT mailingparcel_parcelid_fk REFERENCES parcel (parcelkey),
        mailingparcel_mailingid     INTEGER CONSTRAINT mailingparcel_mailingid_fk REFERENCES mailingaddress (addressid),
        source_sourceid             INTEGER CONSTRAINT mailingparcel_sourceid_fk REFERENCES public.bobsource (sourceid),
        createdts                   TIMESTAMP WITH TIME ZONE,
        deactivatedts               TIMESTAMP WITH TIME ZONE,
        notes                       TEXT

    );


CREATE TABLE parcelunit
    (
          unitid                    integer NOT NULL DEFAULT nextval('propertunit_unitid_seq'::regclass),
          unitnumber                text NOT NULL,
          parcel_parcelkey          INTEGER NOT NULL CONSTRAINT parcelunit_parcelkey_fk REFERENCES parcel (parcelkey),
          rentalintentdatestart     timestamp with time zone,
          rentalintentdatestop      timestamp with time zone,
          rentalnotes               text,
          condition_intensityclassid integer,
          source_sourceid           INTEGER CONSTRAINT parcelunit_sourceid_fk REFERENCES public.bobsource (sourceid),
          createdts                 TIMESTAMP WITH TIME ZONE,
          createdby_userid          INTEGER CONSTRAINT parcelunit_createdby_userid_fk REFERENCES login (userid),     
          lastupdatedts             TIMESTAMP WITH TIME ZONE,
          lastupdatedby_userid      INTEGER CONSTRAINT parcelunit_lastupdatedby_userid_fk REFERENCES login (userid),
          deactivatedts             TIMESTAMP WITH TIME ZONE,
          deactivatedby_userid      INTEGER CONSTRAINT parcelunit_deactivatedby_userid_fk REFERENCES login (userid),      
          notes                     text,

          CONSTRAINT propunit_conditionintensityclass_classid_fk FOREIGN KEY (condition_intensityclassid)
              REFERENCES public.intensityclass (classid) MATCH SIMPLE
              ON UPDATE NO ACTION ON DELETE NO ACTION
    );


CREATE SEQUENCE IF NOT EXISTS parcelunithuman_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.parcelunithuman
    (
        linkid                  INTEGER PRIMARY KEY DEFAULT nextval('parcelunithuman_linkid_seq'),
        parcelunit_unitid       INTEGER NOT NULL CONSTRAINT parcelunithuman_unitid_fk REFERENCES parcelunit (unitid),
        human_humanid           INTEGER NOT NULL CONSTRAINT parcelunithuman_humanid_fk REFERENCES human (humanid),
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER CONSTRAINT parcelunithuman_createdby_userid_fk REFERENCES login (userid),     
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT parcelunithuman_lastupdatedby_userid_fk REFERENCES login (userid),
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER CONSTRAINT parcelunithuman_deactivatedby_userid_fk REFERENCES login (userid),   
        notes                   TEXT
    );



--TODO: The field Use Code can contain APART:x-y UNITS


-- this is closesed to current propertyunit (occ is based on one of these)



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (33, 'database/patches/dbpatch_beta33.sql',NULL, 'ecd', 'LATE oct 2020 changes');
