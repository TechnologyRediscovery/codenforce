-- UPGRADE PATCH #1
-- PARCEL AND HUMANIZATION

-- These function calls work with functions current through DB patch dbpatch_beta37.sql

-- We have three municipalities to migrate from property and person to parcel and human:
-- COGLAND ID 999
-- East McKeesport 821
-- Chalfant 814


INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (100, 'ParcelMailingaddress', 'contains', 'standard mailing address on parcel', '2021-07-13 12:14:31.377882-04', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (101, 'ParcelHuman', 'associated with', 'default parcel-human role', '2021-08-05 15:47:10.920067-04', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (102, 'MailingaddressHuman', 'gets mail at', 'default human-mailing role', '2021-08-05 15:48:00.373826-04', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (103, 'MailingaddressHuman', 'owns', 'property owner', '2022-02-18 16:45:47.278684-05', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (104, 'OccApplicationHuman', 'applicant', 'the person completing the application', '2022-02-23 11:20:29.20346-05', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (105, 'CECaseHuman', 'associated with', 'default code enforcement case - human role', '2022-02-23 11:20:53.55565-05', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (106, 'OccPeriodHuman', 'associated with', 'default occupnacy period - person association', '2022-02-23 11:21:20.252497-05', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (107, 'ParcelUnitHuman', 'associated with', 'default parcel unit - person role', '2022-02-23 11:22:22.756677-05', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (108, 'CitationHuman', 'associated with', 'default citation - person role', '2022-02-23 11:22:37.84544-05', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (109, 'EventHuman', 'associated with ', 'default event - person role', '2022-02-23 11:22:51.325155-05', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (110, 'CitationHuman', 'defendant', 'defendant of the citation', '2022-02-23 11:23:25.965335-05', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (111, 'CitationHuman', 'witness - defense', 'defense witness', '2022-02-23 11:23:41.220574-05', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (112, 'CitationHuman', 'witness - code officer', 'plaintiff witness', '2022-02-23 11:24:01.037725-05', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (113, 'ParcelUnitHuman', 'tenant', 'tenant of the parcel unit', '2022-02-23 11:24:54.230676-05', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (114, 'ParcelUnitHuman', 'manager', 'manager of the parcel unit', '2022-02-23 11:25:11.207171-05', NULL, NULL);
INSERT INTO public.linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES (115, 'CitationDocketHuman', 'associated with', 'default citation docket - person role', '2022-03-10 17:13:14.849026-05', NULL, NULL);


-- this killed the old parcelmailing and also killed parcelunit's FK, which we restore after we
-- get the good copy of this table
DROP TABLE public.parcelmailingaddress CASCADE;

CREATE TABLE public.parcelmailingaddress
(
  mailingparcel_parcelid integer,
  mailingparcel_mailingid integer,
  source_sourceid integer,
  createdts timestamp with time zone,
  createdby_userid integer,
  lastupdatedts timestamp with time zone,
  lastupdatedby_userid integer,
  deactivatedts timestamp with time zone,
  deactivatedby_userid integer,
  notes text,
  linkid integer NOT NULL DEFAULT nextval('parcelmailing_linkid_seq'::regclass),
  linkedobjectrole_lorid integer,
  priority integer DEFAULT 1,
  CONSTRAINT parcelmailingaddress_linkid_pk PRIMARY KEY (linkid),
  CONSTRAINT mailingaddressparcel_createdby_userid_fk FOREIGN KEY (createdby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT mailingaddressparcel_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT mailingaddressparcel_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT mailingparcel_parcelid_fk FOREIGN KEY (mailingparcel_parcelid)
      REFERENCES public.parcel (parcelkey) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT mailingparcel_sourceid_fk FOREIGN KEY (source_sourceid)
      REFERENCES public.bobsource (sourceid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT parcelmailing_lorid_fk FOREIGN KEY (linkedobjectrole_lorid)
      REFERENCES public.linkedobjectrole (lorid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.parcelmailingaddress
  OWNER TO sylvia;


ALTER TABLE public.parcelunit ADD 
CONSTRAINT parcelunit_parcelmailingaddress_fk FOREIGN KEY (address_parcelmailingid) REFERENCES parcelmailingaddress (linkid);

-- migratepropertytoparcel(creationrobotuser INTEGER,
-- 														  defaultsource INTEGER,
-- 													  	  cityid INTEGER,
-- 													  	  municodetarget INTEGER,
-- 												  	  	  parceladdr_lorid INTEGER	);
SELECT migratepropertytoparcel(						     99, -- sylvia
														 14, -- special MAR22 migration source
													  	 14707, -- recognized for Swissavle, PA with default name Pittsburgh
													  	 999, -- cog land
												  	  	 100	); -- standard parcel mailing address link

-- clean up from the cogland new propety made with the new UI
DELETE FROM parcelmailingaddress WHERE mailingparcel_parcelid = 1007024;
DELETE FROM parcelinfo WHERE parcel_parcelkey = 1007024;
DELETE from parcel where parcelkey = 1007024;

-- HUMAN MAILINGADDRESS didn't get the lastupdatedts and user fields

 ALTER TABLE public.humanmailingaddress ADD COLUMN lastupdatedts timestamp with time zone;
 ALTER TABLE public.humanmailingaddress ADD COLUMN lastupdatedby_userid INTEGER;

ALTER TABLE public.humanmailingaddress
  ADD CONSTRAINT mailingaddressparcel_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;


SELECT 							migratepersontohuman(		99, --sylvia
															14, -- SB22 source
														  	999, -- cogland
														  	101, --default parcelhuman role
														  	102) -- default "gets mail at" role

-- NOW FOR CHALFANT

SELECT migratepropertytoparcel(						     99, -- sylvia
														 14, -- special MAR22 migration source
													  	 14592, -- Recognized for chalfant 15112 EAST PITTSBURGH
													  	 814, -- chalfant
												  	  	 100	); -- standard parcel mailing address link
-- 434 records out

SELECT 							migratepersontohuman(		99, --sylvia
															14, -- SB22 source
														  	814, -- chalfant
														  	101, --default parcelhuman role
														  	102); -- default "gets mail at" role

-- 473 records out



SELECT migratepropertytoparcel(						     99, -- sylvia
														 14, -- special MAR22 migration source
													  	 14504, -- Recognized for East McKeesport 15035
													  	 821, -- EMB
												  	  	 100	); -- standard parcel mailing address link
-- 1051 records 1:19 seconds

SELECT 							migratepersontohuman(		99, --sylvia
															14, -- SB22 source
														  	821, -- EMB
														  	101, --default parcelhuman role
														  	102); -- default "gets mail at" role
-- if you undertake a partial execution of the persontohuman function, 
-- the records have to all be cleared to avoid duplicate key errors
DELETE from contactphone WHERE createdts > '2022-03-11 00:00:00';
DELETE from contactemail WHERE createdts > '2022-03-11 00:00:00';
DELETE from humanmailingaddress WHERE createdts > '2022-03-11 00:00:00';
DELETE from personhumanmigrationlog WHERE ts > '2022-03-11 00:00:00';
DELETE from humanparcel WHERE createdts > '2022-03-11 00:00:00';
DELETE from human where createdts > '2022-03-11 00:00:00';


-- manual parcel insertion for the municipality property, which is needed for the session init


INSERT INTO public.parcel(
            parcelkey, parcelidcnty, source_sourceid, createdts, createdby_userid, 
            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
            notes, muni_municode, lotandblock)
    VALUES (DEFAULT, '0374-L-00229-0000-01', 9, now(), 100, 
            now(), 100, NULL, NULL, 
            'manually entered SB22', 814, '374-L-229');


INSERT INTO public.parcel(
            parcelkey, parcelidcnty, source_sourceid, createdts, createdby_userid, 
            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
            notes, muni_municode, lotandblock)
    VALUES (DEFAULT, '0547-C-00192-0000-00', 9, now(), 100, 
            now(), 100, NULL, NULL, 
            'manually entered SB22', 821, '547-C-192');


--somehow occspacetype didn't get made, either; Or rather, it got renamed, and didn't get remade
CREATE TABLE public.occspacetype
(
  spacetypeid integer NOT NULL DEFAULT nextval('spacetype_spacetypeid_seq'::regclass),
  spacetitle text NOT NULL,
  description text NOT NULL,
  CONSTRAINT spacetype_spacetypeid_resurrectedtable_pk PRIMARY KEY (spacetypeid)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE public.occspacetype
  OWNER TO sylvia;

INSERT INTO public.occspacetype VALUES (10, 'Exterior', 'outside the house');
INSERT INTO public.occspacetype VALUES (11, 'Living', 'A space in which living occurs');
INSERT INTO public.occspacetype VALUES (12, 'Utility', 'HVAC, water, etc.');
INSERT INTO public.occspacetype VALUES (13, 'Unfinished', 'Non-habitable, non-utility');
INSERT INTO public.occspacetype VALUES (14, 'Stairways', 'Up and down spaces');
INSERT INTO public.occspacetype VALUES (16, 'Entryway', 'enter and exit');
INSERT INTO public.occspacetype VALUES (15, 'Whole House', 'Contains all inspectable ordinances');
INSERT INTO public.occspacetype VALUES (17, 'Pool', 'above or below ground');
INSERT INTO public.occspacetype VALUES (18, 'Storage Shed', 'No utilities');


ALTER TABLE public.occchecklistspacetype DROP CONSTRAINT occchecklistspacetype_typeid_fk;

ALTER TABLE public.occchecklistspacetype
  ADD CONSTRAINT occchecklistspacetype_typeid_fk FOREIGN KEY (spacetype_typeid)
      REFERENCES public.occspacetype (spacetypeid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

-- jumped human IDs to be super high


ALTER SEQUENCE public.human_humanid_seq RESTART 500000;
ALTER SEQUENCE public.human_humanid_seq START 500000;
ALTER SEQUENCE public.human_humanid_seq MINVALUE 500000;


-- any mailing address will do, then link it later to the person and the property
ALTER TABLE public.noticeofviolation
  ADD CONSTRAINT nov_mailing_fk FOREIGN KEY (recipient_mailing)
      REFERENCES public.mailingaddress (addressid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;