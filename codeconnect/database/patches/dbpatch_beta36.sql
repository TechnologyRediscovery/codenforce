-- ****************************************************************************
-- PATCH 36
-- "CIATATION FACELIFT"

-- *************

-- RUN ON REMOTE UP TO HERE



-- CONTINUE HUMANIZATION UPDATES



CREATE SEQUENCE IF NOT EXISTS parcelmailingaddressid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelmailingaddress ADD COLUMN parcelmailingid INTEGER 
	DEFAULT nextval('parcelmailingaddressid_seq') 
	CONSTRAINT parcelmailingaddressid_pk PRIMARY KEY;

ALTER TABLE public.parcel ADD COLUMN lotandblock TEXT;



ALTER TABLE public.parcelunit ADD COLUMN address_parcelmailingid INTEGER
	CONSTRAINT parcelunit_parcelmailing_fk REFERENCES parcelmailingaddress (parcelmailingid);



-- This brings citation and its linked objects up to par with the stadnard linked entity fields

ALTER TABLE public.citation ADD COLUMN docketno				TEXT;
ALTER TABLE public.citation ADD COLUMN createdts               TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.citation ADD COLUMN createdby_userid        INTEGER CONSTRAINT human_createdby_userid_fk REFERENCES login (userid);         
ALTER TABLE public.citation ADD COLUMN lastupdatedts           TIMESTAMP WITH TIME ZONE;         
ALTER TABLE public.citation ADD COLUMN lastupdatedby_userid    INTEGER CONSTRAINT human_lastupdatdby_userid_fk REFERENCES login (userid);        
ALTER TABLE public.citation ADD COLUMN deactivatedts           TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.citation ADD COLUMN deactivatedby_userid    INTEGER CONSTRAINT human_deactivatedby_userid_fk REFERENCES login (userid);  



DROP TABLE public.citationperson;


CREATE SEQUENCE IF NOT EXISTS citationhuman_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.citationhuman
(
  linkid integer NOT NULL DEFAULT nextval('citationhuman_seq'::regclass),
  human_humanid integer,
  citation_citationid integer,
  role TEXT,
  source_sourceid integer,
  createdts timestamp with time zone,
  createdby_userid integer,
  lastupdatedts timestamp with time zone,
  lastupdatedby_userid integer,
  deactivatedts timestamp with time zone,
  deactivatedby_userid integer,
  notes text,

  CONSTRAINT humancitation_pkey PRIMARY KEY (linkid),
  
  CONSTRAINT humancitation_humanid_fk FOREIGN KEY (human_humanid)
      REFERENCES public.human (humanid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  
  CONSTRAINT humancitation_citationid_fk FOREIGN KEY (citation_citationid)
  		REFERENCES public.citation (citationid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,

  CONSTRAINT humancitation_sourceid_fk FOREIGN KEY (source_sourceid)
      REFERENCES public.bobsource (sourceid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,

  CONSTRAINT humancitation_createdby_userid_fk FOREIGN KEY (createdby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,

  CONSTRAINT humancitation_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,

  CONSTRAINT humancitation_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
) ;


-- ***********************************************
-- RUN THIS CREATE TYPE BY ITSELF
-- ***********************************************
-- CREATE TYPE citationviolationstatus AS ENUM ('Pending','Guilty','Dismissed','Compliance', 'Deemed Invalid');
-- ***********************************************

ALTER TABLE public.citationviolation ADD COLUMN createdts               TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.citationviolation ADD COLUMN lastupdatedts           TIMESTAMP WITH TIME ZONE;         
ALTER TABLE public.citationviolation ADD COLUMN deactivatedts           TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.citationviolation ADD COLUMN createdby_userid        INTEGER CONSTRAINT citviol_createdby_userid_fk REFERENCES login (userid);         
ALTER TABLE public.citationviolation ADD COLUMN lastupdatedby_userid    INTEGER CONSTRAINT citviol_lastupdatdby_userid_fk REFERENCES login (userid);        
ALTER TABLE public.citationviolation ADD COLUMN deactivatedby_userid    INTEGER CONSTRAINT citviol_deactivatedby_userid_fk REFERENCES login (userid);  

ALTER TABLE public.citationviolation ADD COLUMN status                  citationviolationstatus;
ALTER TABLE public.citationviolation ADD COLUMN linknotes               TEXT;
ALTER TABLE public.citationviolation ADD COLUMN linksource 				INTEGER CONSTRAINT citviol_source_fk REFERENCES bobsource (sourceid);




CREATE TABLE public.citationevent 
(

	citation_citationid 	INTEGER NOT NULL CONSTRAINT citationevent_citationid_fk REFERENCES citation (citationid),
	event_eventid 			INTEGER NOT NULL CONSTRAINT citationevent_eventid_fk REFERENCES event (eventid)
);


CREATE SEQUENCE IF NOT EXISTS citationcitationstatus_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.citationcitationstatus
(

	citationstatusid INTEGER DEFAULT nextval('citationcitationstatus_seq') CONSTRAINT citationcitationstatus_id_pk PRIMARY KEY,
	citation_citationid INTEGER NOT NULL CONSTRAINT citationevent_citationid_fk REFERENCES citation (citationid),
	citationstatus_statusid INTEGER NOT NULL CONSTRAINT citationcitstatus_fk REFERENCES citationstatus (statusid),
	dateofrecord TIMESTAMP WITH TIME ZONE NOT NULL,
	createdts               TIMESTAMP WITH TIME ZONE,
	createdby_userid        INTEGER CONSTRAINT human_createdby_userid_fk REFERENCES login (userid),         
	lastupdatedts           TIMESTAMP WITH TIME ZONE,         
	lastupdatedby_userid    INTEGER CONSTRAINT human_lastupdatdby_userid_fk REFERENCES login (userid),        
	deactivatedts           TIMESTAMP WITH TIME ZONE,
	deactivatedby_userid    INTEGER CONSTRAINT human_deactivatedby_userid_fk REFERENCES login (userid),  
	notes					TEXT
);



CREATE TABLE public.citationphotodoc
(
	photodoc_photodocid integer NOT NULL,
	  citation_citationid integer NOT NULL,
	  CONSTRAINT citationphotodoc_pk PRIMARY KEY (photodoc_photodocid, citation_citationid),
	  CONSTRAINT citationphotodoc_cit_fk FOREIGN KEY (citation_citationid)
	      REFERENCES public.citation (citationid) MATCH SIMPLE
	      ON UPDATE NO ACTION ON DELETE NO ACTION,
	  CONSTRAINT citationphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid)
	      REFERENCES public.photodoc (photodocid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);



CREATE TYPE linkedobjectroleschema AS ENUM ('OccApplicationHuman', 'CECaseHuman', 'OccPeriodHuman', 'ParcelHuman', 'ParcelUnitHuman', 'CitationHuman', 'EventHuman', 'MailingaddressHuman', 'ParcelMailingaddress');

CREATE SEQUENCE IF NOT EXISTS linkedobjectrole_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.linkedobjectrole
(
	lorid 			INTEGER NOT NULL DEFAULT nextval('linkedobjectrole_seq') PRIMARY KEY,
	lorschema_schemaid 	linkedobjectroleschema NOT NULL,
	title 			TEXT NOT NULL,
	description     TEXT,
	createdts       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	deactivatedts   TIMESTAMP WITH TIME ZONE,
	notes			TEXT
);


DROP TABLE public.eventperson;


CREATE SEQUENCE IF NOT EXISTS eventhuman_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.eventhuman
    (
        linkid                  INTEGER PRIMARY KEY DEFAULT nextval('eventhuman_linkid_seq'),
        event_eventid 			INTEGER NOT NULL CONSTRAINT eventhuman_eventid_fk REFERENCES event (eventid),
        human_humanid           INTEGER NOT NULL CONSTRAINT parcelunithuman_humanid_fk REFERENCES human (humanid),
        linkedobjectrole_lorid  INTEGER CONSTRAINT eventhuman_lorid_fk REFERENCES linkedobjectrole (lorid),
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER CONSTRAINT eventhuman_createdby_userid_fk REFERENCES login (userid),     
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER CONSTRAINT eventhuman_lastupdatedby_userid_fk REFERENCES login (userid),
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER CONSTRAINT eventhuman_deactivatedby_userid_fk REFERENCES login (userid),   
        notes                   TEXT
    );

DROP TABLE CASCADE occpermitapplicationperson ;



CREATE TABLE public.occpermitapplicationhuman
(
  occpermitapplication_applicationid integer NOT NULL,
  human_humanid integer NOT NULL,
  applicant boolean,
  preferredcontact boolean,
  applicationpersontype persontype NOT NULL DEFAULT 'Other'::persontype,
  active boolean,
  CONSTRAINT occpermitapplicationhuman_comp_pk PRIMARY KEY (occpermitapplication_applicationid, human_humanid),
  CONSTRAINT occpermitapplicationhuman_applicationid_fk FOREIGN KEY (occpermitapplication_applicationid)
      REFERENCES public.occpermitapplication (applicationid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occpermitapplicationhuman_humanid_fk FOREIGN KEY (human_humanid)
      REFERENCES public.human (humanid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
) ;

ALTER TABLE citationhuman ADD COLUMN linkedobjectrole_lorid INTEGER 
    CONSTRAINT citationhuman_lorid_fk 
    REFERENCES linkedobjectrole (lorid);  



  -- OCCAPPLICATIONHUMAN ("","OccApplicationHuman"), 

  -- JURPLEL can update this table????



  --   CECASEHUMAN ("humancecase", "CECaseHuman"), 
  --   Unification of link structure
ALTER TABLE humancecase ADD COLUMN linkedobjectrole_lorid INTEGER 
    CONSTRAINT humancecase_lorid_fk 
    REFERENCES linkedobjectrole (lorid);  

ALTER TABLE humancecase ADD COLUMN source_sourceid
    CONSTRAINT humancecase_sourceid_fk
    REFERENCES bobsource (sourceid);


CREATE SEQUENCE IF NOT EXISTS humancecase_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

ALTER TABLE humancecase ADD COLUMN linkid INTEGER NOT NULL 
    CONSTRAINT humancecase_linkid_pk PRIMARY KEY 
    DEFAULT nextval('humancecase_linkid_seq');


  --   OCCPERIODHUMAN ("humanoccperiod","OccPeriodHuman"), 
DROP TABLE occperiodperson CASCADE;

ALTER TABLE humanoccperiod DROP COLUMN persontype;

ALTER TABLE humanoccperiod ADD COLUMN occperiod_periodid INTEGER
    CONSTRAINT humanoccperiod_periodid_fk
    REFERENCES occperiod (periodid);

ALTER TABLE humanoccperiod ADD COLUMN linkedobjectrole_lorid INTEGER 
    CONSTRAINT humanoccperiod_lorid_fk 
    REFERENCES linkedobjectrole (lorid);  


ALTER TABLE humanoccperiod ADD COLUMN source_sourceid INTEGER
    CONSTRAINT humanoccperiod_sourceid_fk
    REFERENCES bobsource (sourceid);

  --   PARCELHUMAN ("humanparcel","ParcelHuman"), 

ALTER TABLE humanparcel DROP COLUMN role_roleid;

ALTER TABLE humanparcel ADD COLUMN linkedobjectrole_lorid INTEGER 
    CONSTRAINT humanparcel_lorid_fk 
    REFERENCES linkedobjectrole (lorid);  




  --   PARCELUNITHUMAN ("humanparcelunit","ParcelUnitHuman"), 
ALTER TABLE humanparcelunit DROP COLUMN role_roleid;

ALTER TABLE humanparcelunit ADD COLUMN linkedobjectrole_lorid INTEGER 
    CONSTRAINT humanparcelunit_lorid_fk 
    REFERENCES linkedobjectrole (lorid);  

ALTER TABLE humanparcelunit ADD COLUMN source_sourceid INTEGER
    CONSTRAINT humanparcelunit_sourceid_fk
    REFERENCES bobsource (sourceid);


  --   CITATIONHUMAN ("citationhuman","CitationHuman"), 

 -- GOOD SHAPE!!!


  --   EVENTHUMAN ("eventhuman","EventHuman"), 

  ALTER TABLE eventhuman ADD COLUMN source_sourceid INTEGER
    CONSTRAINT eventhuman_sourceid_fk
    REFERENCES bobsource (sourceid);

  --   MAILINGADDRESSHUMAN ("humanmailingaddress","MailingaddressHuman"), 

CREATE SEQUENCE IF NOT EXISTS humanmailing_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

ALTER TABLE humanmailingaddress ADD COLUMN linkid INTEGER NOT NULL 
    CONSTRAINT humanmailingaddress_linkid_pk PRIMARY KEY 
    DEFAULT nextval('humanmailing_linkid_seq');
  --   PARCELMAILINGADDRESS  ("parcelmailingaddress","ParcelMailingaddress");
  
CREATE SEQUENCE IF NOT EXISTS parcelmailing_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

ALTER TABLE parcelmailingaddress ADD COLUMN linkid INTEGER NOT NULL 
    CONSTRAINT parcelmailingaddress_linkid_pk PRIMARY KEY 
    DEFAULT nextval('parcelmailing_linkid_seq');
  

ALTER TABLE public.parcelmailingaddress DROP COLUMN parcelmailingid;
-- ******** RUN LOCALLY UP TO HERE ******** 

ALTER TABLE parcelmailingaddress ADD COLUMN linkedobjectrole_lorid INTEGER 
    CONSTRAINT parcelmailing_lorid_fk 
    REFERENCES linkedobjectrole (lorid);  

  
CREATE SEQUENCE IF NOT EXISTS humancontacted_contactconfigid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;



CREATE TABLE public.humancontacted 
(
    contactconfigid INTEGER DEFAULT nextval('humancontacted_contactconfigid_seq') PRIMARY KEY,
    humanmailing_linkid INTEGER CONSTRAINT humancontacted_mailing_fk REFERENCES humanmailingaddress (linkid),
    contactphone_phoneid INTEGER CONSTRAINT humancontacted_phoneid_fk REFERENCES contactphone (phoneid),
    contactemail_emailid INTEGER CONSTRAINT humancontacted_emailid_fk REFERENCES contactemail (emailid) 
);




--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (36, 'database/patches/dbpatch_beta36.sql',NULL, 'ecd', 'Citatation facelift');


