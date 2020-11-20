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

-- A single person (A legal person entity) can be associated with multiple
-- mailing addresses, each of which could be in a different role (i.e. owner
-- owner of parcel at address X, receives tax mailing at address Y, 
-- and lives at address Z)
-- Needs: Person deceased date

-- ASSUMPTION: an address listed as 1115-1121 would be interpolated only as 
-- the odds: 1115, 1117, 1119, 1121. SEAN: Is this accurate?

-- TODO: defined workflow for pyparcelupdate info update by month (step-by-step, 
-- get data from where, do what with it, store the result where?, 
-- compare to what, made determination, etc.)
-- This will involve clones since changes are written to clones, not
-- the "real" person
-- TODO: Deal with the WPRDC dump that stores the second
-- part of an addrss range in field called PROPERTYFRACTION

CREATE TABLE public.parcel(
    parcelkey     INTEGER,
    parcelidcnty    text,
    parcellab       text,
    lastupdatedts   TIMESTAMP WITH TIME ZONE,
    lastupdatedby_userid    INTEGER CONSTRAINT parcel_lastupdated_fk REFERENCES,
    deactivatedts   TIMESTAMP WITH TIME ZONE,
    notes           text
);


-- code enf case is still based on a parcel

CREATE TABLE public.parcelperson
    (
        person_personid     INTEGER CONSTRAINT parcelperson_personid_fk REFERENCES person (personid),
        parcel_parcelkey    INTEGER CONSTRAINT parcel_parcelkey_fk REFERENCES parcel (parcelkey),
        role_roleid         INTEGER CONSTRAINT parcelperson_roleid_fk   REFERENCES parcelpersonrole (roleid)
    );  


CREATE SEQUENCE IF NOT EXISTS parcelpersonrole_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.parcelpersonrole
    (
        roleid              INTEGER PRIMARY KEY DEFAULT nextval('parcelpersonrole_roleid_seq'),
        title               TEXT NOT NULL,
        description         TEXT,
        muni_municode       INTEGER CONSTRAINT parcelpersonrole_municode_fk REFERENCES municipality (municode),
        deactivatedts       TIMESTAMP WITH TIME ZONE

    ); 

CREATE TABLE public.personnorm
    (
        personid                INTEGER PRIMARY KEY DEFAULT nextval('person_personidseq'),
        name                    TEXT,
        dob                     DATE,
        under18                 BOOLEAN,
        activationstartdt       DATE,
        activationstartnotes    TEXT,
        activationstopdt        DATE,
        activationstopnotes     TEXT,
        deceaseddate            DATE,
        deceasedby_userid       INTEGER CONSTRAINT person_deceasedby_userid_fk REFERENCES login (userid),
        cloneof_personid        INTEGER CONSTRAINT person_clone_personid_fk REFERENCES person (personid),
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER CONSTRAINT person_deceasedby_userid_fk REFERENCES login (userid),     
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT person_lastupdatdby_userid_fk REFERENCES login (userid),
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER CONSTRAINT person_deceasedby_userid_fk REFERENCES login (userid),           
        notes                   TEXT
    );



CREATE SEQUENCE IF NOT EXISTS contactphone_phoneid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.contactphone
    (
        phoneid             INTEGER PRIMARY KEY DEFAULT nextval('contactphone_phoneid_seq'),
        person_personid         INTEGER NOT NULL CONSTRAINT contactemail_personid_fk REFERENCES personnorm (personid),
        phonenumber         TEXT NOT NULL,
        phoneext            INTEGER,
        disconnectts        TIMESTAMP WITH TIME ZONE,
        disconnect_userid       INTEGER CONSTRAINT person_deceasedby_userid_fk REFERENCES login (userid),    
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER CONSTRAINT person_deceasedby_userid_fk REFERENCES login (userid),     
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT person_lastupdatdby_userid_fk REFERENCES login (userid),
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER CONSTRAINT person_deceasedby_userid_fk REFERENCES login (userid),           
        notes               TEXT
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
        person_personid         INTEGER NOT NULL CONSTRAINT contactemail_personid_fk REFERENCES personnorm (personid),
        emailaddress            TEXT NOT NULL,
        bouncets                TIMESTAMP WITH TIME ZONE,
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER CONSTRAINT person_deceasedby_userid_fk REFERENCES login (userid),     
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT person_lastupdatdby_userid_fk REFERENCES login (userid),
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER CONSTRAINT person_deceasedby_userid_fk REFERENCES login (userid),           
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
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT mailingaddress_lastupdated_userid REFERENCES login (userid),
        notes                   TEXT,
        verifiedts              TIMESTAMP WITH TIME ZONE,
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER CONSTRAINT createdby_XXX_userid_fk REFERENCES login (userid),     
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT lastupdatedby_XXX_userid_fk REFERENCES login (userid),
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER CONSTRAINT deactivatedby_XXX_userid_fk REFERENCES login (userid),           

    );



CREATE TABLE public.personmailingaddress
    (
        personmailing_personid          INTEGER CONSTRAINT personmailing_personid_fk REFERENCES person (personid),
        personmailing_addressid         INTEGER CONSTRAINT personmailing_addressid_fk REFERENCES address (addressid),
        createdts                       TIMESTAMP WITH TIME ZONE,
        createdby_userid                INTEGER CONSTRAINT createdby_XXX_userid_fk REFERENCES login (userid),     
        deactivatedts                   TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid            INTEGER CONSTRAINT deactivatedby_XXX_userid_fk REFERENCES login (userid),           


    );

CREATE SEQUENCE IF NOT EXISTS personmailing_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.personmailingrole
    (
        roleid              INTEGER PRIMARY KEY DEFAULT nextval('personmailing_roleid_seq'),
        title               TEXT NOT NULL,
        createdts          TIMESTAMP WITH TIME ZONE,
        description         TEXT,
        muni_municode       INTEGER CONSTRAINT personmailing_municode_fk REFERENCES municipality (municode),
        deactivatedts       TIMESTAMP WITH TIME ZONE,
        notes

    ); 


CREATE TABLE public.mailingaddressparcel
    (
        mailingparcel_parcelid      INTEGER CONSTRAINT mailingparcel_parcelid_fk REFERENCES parcel (parcelkey),
        mailingparcel_mailingid     INTEGER CONSTRAINT mailingparcel_mailingid_fk REFERENCES mailingaddress (addressid),
        createdts                   TIMESTAMP WITH TIME ZONE,
        deactivatedts               TIMESTAMP WITH TIME ZONE,
        notes                       TEXT

    );


CREATE TABLE parcelunit
    (
          unitid integer NOT NULL DEFAULT nextval('propertunit_unitid_seq'::regclass),
          unitnumber text,
          parcel_parcelkey          INTEGER NOT NULL CONSTRAINT parcelunit_parcelkey_fk REFERENCES parcel (parcelkey),
          notes text,
          rentalintentdatestart timestamp with time zone,
          rentalintentdatestop timestamp with time zone,
          rentalintentlastupdatedby_userid integer,
          rentalnotes text,
          condition_intensityclassid integer,
          lastupdatedts timestamp with time zone,
          rental boolean,
          CONSTRAINT unitid_pk PRIMARY KEY (unitid),
          CONSTRAINT propunit_conditionintensityclass_classid_fk FOREIGN KEY (condition_intensityclassid)
              REFERENCES public.intensityclass (classid) MATCH SIMPLE
              ON UPDATE NO ACTION ON DELETE NO ACTION,
          CONSTRAINT propunit_rentalintentupdatedby_fk FOREIGN KEY (rentalintentlastupdatedby_userid)
              REFERENCES public.eventrule (ruleid) MATCH SIMPLE
              ON UPDATE NO ACTION ON DELETE NO ACTION
)
    );

FK parcel
unitno
intenttorent 
--TODO: The field Use Code can contain APART:x-y UNITS


-- this is closesed to current propertyunit (occ is based on one of these)



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (33, 'database/patches/dbpatch_beta33.sql',NULL, 'ecd', 'LATE oct 2020 changes');
