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

-- ******** RUN LOCALLY UP TO HERE ******** 

DROP TABLE public.eventperson;


CREATE TABLE public.eventhuman
(
  human_humanid integer,
  event_eventid integer,
  role TEXT,
  source_sourceid integer,
  createdts timestamp with time zone,
  createdby_userid integer,
  lastupdatedts timestamp with time zone,
  lastupdatedby_userid integer,
  deactivatedts timestamp with time zone,
  deactivatedby_userid integer,
  notes text,

  CONSTRAINT eventhuman_comppk PRIMARY KEY (human_humanid, event_eventid),
  
  CONSTRAINT eventhuman_humanid_fk FOREIGN KEY (human_humanid)
      REFERENCES public.human (humanid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  
  CONSTRAINT eventhuman_eventid_fk FOREIGN KEY (event_eventid)
  		REFERENCES public.event (eventid) MATCH SIMPLE
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



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (36, 'database/patches/dbpatch_beta36.sql',NULL, 'ecd', 'Citatation facelift');


