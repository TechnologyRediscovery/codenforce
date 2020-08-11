

CREATE SEQUENCE IF NOT EXISTS bobsourceid_seq
	START WITH 10
	INCREMENT BY 1 
	MINVALUE 10
	NO MAXVALUE 
	CACHE 1;

CREATE TABLE public.bobsource 
(
	sourceid 							INTEGER DEFAULT nextval('bobsourceid_seq') NOT NULL CONSTRAINT bobsource_pk PRIMARY KEY,
	title 								TEXT NOT NULL,
	description 						TEXT,
	creator 							INTEGER CONSTRAINT bobsource_creator_userid_fk REFERENCES public.login (userid),
	muni_municode 						INTEGER NOT NULL CONSTRAINT bobsource_municode_fk REFERENCES public.municipality (municode),
	userattributable 					BOOLEAN DEFAULT true,
	active 								BOOLEAN DEFAULT true,
	notes 								TEXT
) ;


-- BEGIN MAJOR OCCUPANCY OVERHAUL AND FINAL BETA IMPLEMENTATION PUSH CHANGES ---


ALTER TABLE public.checklist RENAME TO occchecklist;
ALTER TABLE public.checklistspaceelement RENAME TO occchecklistspaceelement;


ALTER TABLE public.occupancypermitapplication ADD COLUMN declaredtotaladults integer;
ALTER TABLE public.occupancypermitapplication ADD COLUMN declaredtotalyouth integer;

ALTER TABLE public.ceeventcategory RENAME phasechangerule_ruleid  TO eventrule_ruleid;

ALTER TABLE public.ceeventcategory
  RENAME CONSTRAINT ceeventcat_phasechange_fk TO ceeventcat_ruleid_fk;



DROP TABLE public.occupancyinspectionstatus CASCADE;

-- this will also drop a FK on the loginobjecthistory

-- Since we want to be able to assocaite an occupancy permit with a permitting code source (like IPMC 2015), 
-- we had the FK on the permit to just poop it out onto the printed page. Instead, move it to the checklist itself


ALTER TABLE public.occchecklist ADD COLUMN governingcodesource_sourceid INTEGER CONSTRAINT occchecklist_codesourceid_fk REFERENCES codesource (sourceid);


DROP TABLE occpermittype CASCADE;


CREATE SEQUENCE IF NOT EXISTS occperiodtypeid_seq
	START WITH 1000
	INCREMENT BY 1 
	MINVALUE 1000
	NO MAXVALUE 
	CACHE 1;

CREATE TABLE public.occperiodtype
(
	typeid 							INTEGER DEFAULT nextval('occperiodtypeid_seq') NOT NULL CONSTRAINT occperiodtype_pk PRIMARY KEY,
	muni_municode						INTEGER NOT NULL CONSTRAINT occperiodtype_municode_fk REFERENCES public.municipality (municode),
	title 								TEXT NOT NULL,
	authorizeduses 						TEXT,
	description 						TEXT,
	userassignable 						BOOLEAN DEFAULT true,
	permittable 						         BOOLEAN DEFAULT true,
	startdaterequired 					BOOLEAN DEFAULT true,
	enddaterequired 					BOOLEAN DEFAULT true,
	completedinspectionrequired 		BOOLEAN DEFAULT true,
	rentalcompatible 					BOOLEAN DEFAULT true,
  commercial                BOOLEAN DEFAULT false,				
	active 								BOOLEAN DEFAULT true,
  allowthirdpartyinspection       BOOLEAN DEFAULT false,
  requiredpersontypes             persontype[],
  optionalpersontypes             persontype[],
  fee_feeid                       INTEGER NOT NULL CONSTRAINT occinspection_feeid_fk REFERENCES public.occinspectionfee (feeID),
  requirepersontypeentrycheck     boolean DEFAULT false


) ;


-- Only used for test buld of DB; they are included in formal table def above
-- ALTER TABLE occperiodtype ADD COLUMN requirepersontypeentrycheck     boolean DEFAULT false;
-- ALTER TABLE occperiodtype ADD COLUMN optionalpersontypes             persontype[];
-- ALTER TABLE occperiodtype ADD COLUMN requiredpersontypes             persontype[];

ALTER TABLE occpermitapplicationreason ADD COLUMN periodtypeproposal_periodid INTEGER CONSTRAINT occpermitapprsn_pertype_fk REFERENCES public.occperiodtype (typeid);



CREATE SEQUENCE IF NOT EXISTS occperiodid_seq
	START WITH 1000
	INCREMENT BY 1 
	MINVALUE 1000
	NO MAXVALUE 
	CACHE 1;

CREATE TABLE public.occperiod
(
	periodid 							INTEGER DEFAULT nextval('occperiodid_seq') NOT NULL CONSTRAINT occperiod_pk PRIMARY KEY,
	source_sourceid 					INTEGER NOT NULL CONSTRAINT occperiod_sourceid_fk REFERENCES public.bobsource (sourceid),
	propertyunit_unitid 				INTEGER CONSTRAINT occperiod_propunit_unitid_fk REFERENCES public.propertyunit (unitid),
	createdts	 						TIMESTAMP WITH TIME ZONE,
	type_typeid 						INTEGER NOT NULL CONSTRAINT occperiod_periodtype_typeid_fk REFERENCES public.occperiod (periodid),
	typecertifiedby_userid 				INTEGER CONSTRAINT occperiod_typecert_userid_fk REFERENCES public.login (userid),
	typecertifiedts 					TIMESTAMP WITH TIME ZONE,
	startdate							TIMESTAMP WITH TIME ZONE,
	startdatecertifiedby_userid			INTEGER CONSTRAINT occperiod_startcert_userid_fk REFERENCES public.login (userid),
	startdatecertifiedts				TIMESTAMP WITH TIME ZONE,
	enddate 							TIMESTAMP WITH TIME ZONE,
	enddatecertifiedby_userid 			INTEGER CONSTRAINT occperiod_endcert_userid_fk REFERENCES public.login (userid),
	enddatecterifiedts 					TIMESTAMP WITH TIME ZONE,
	manager_userid 						INTEGER CONSTRAINT occperiod_mngr_userid_fk REFERENCES public.login (userid),
	authorizationts 					TIMESTAMP WITH TIME ZONE,
	authorizedby_userid 				INTEGER CONSTRAINT occperiod_authby_userid_fk REFERENCES public.login (userid),
	overrideperiodtypeconfig			INTEGER CONSTRAINT occperiod_overridetype_userid_fk REFERENCES public.login (userid),
	notes 								TEXT


) ;

DROP TABLE occupancypermit CASCADE;

CREATE TABLE public.occpermit
(
  permitid                INTEGER NOT NULL DEFAULT nextval('occupancypermit_permitid_seq'::regclass) CONSTRAINT occpermit_permitid_pk PRIMARY KEY,
  occperiod_periodid          INTEGER NOT NULL CONSTRAINT occpermit_periodid_fk REFERENCES public.occperiod (periodid),
  referenceno               TEXT,
  issuedto_personid           INTEGER NOT NULL CONSTRAINT occpermit_issuedto_personid_fk REFERENCES public.person (personid),
  issuedby_userid             INTEGER CONSTRAINT occperiod_startcert_userid_fk REFERENCES public.login (userid), 
  dateissued              TIMESTAMP WITH TIME ZONE NOT NULL,
  permitadditionaltext          TEXT,
  notes                 TEXT
) ;



CREATE TABLE public.occperiodphotodoc
(
  photodoc_photodocid integer NOT NULL,
  occperiod_periodid integer NOT NULL,
  CONSTRAINT occperiodphotodoc_pk PRIMARY KEY (photodoc_photodocid, occperiod_periodid),
  CONSTRAINT occperiodphotodoc__occperiod_fk FOREIGN KEY (occperiod_periodid)
      REFERENCES public.occperiod (periodid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occperiodphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid)
      REFERENCES public.photodoc (photodocid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);


DROP TABLE public.occupancyinspection CASCADE;

CREATE TABLE public.occinspection
(
    inspectionID                    INTEGER DEFAULT nextval('occupancyinspectionID_seq') NOT NULL CONSTRAINT occinspection_pk PRIMARY KEY,
    occperiod_periodid 				INTEGER NOT NULL CONSTRAINT occinspection_periodid_fk REFERENCES public.occperiod (periodid),
    inspector_userid 				INTEGER NOT NULL CONSTRAINT occinspection_inspector_userid_fk REFERENCES public.login (userid),
    passedinspection_userid   INTEGER NOT NULL CONSTRAINT occinspection_pass_userid_fk REFERENCES public.login (userid),
    maxoccupantsallowed   INTEGER NOT NULL CONSTRAINT occinspection_maxocc_userid_fk REFERENCES public.login (userid),
    publicaccesscc                  INTEGER,
    enablepacc                      BOOLEAN DEFAULT FALSE,
    notes                           TEXT,
    thirdpartyinspector_personid integer CONSTRAINT occinspection_thirdpartyuserid_fk REFERENCES public.person (personid),
    thirdpartyinspectorapprovalts timestamp with time zone,
    thirdpartyinspectorapprovalby INTEGER CONSTRAINT occinspectionthirdpartyapprovalby_fk REFERENCES public.login (userid)
) ;

ALTER TABLE public.occinspection ADD COLUMN numbedrooms integer;
ALTER TABLE public.occinspection ADD COLUMN numbathrooms integer;

-- Drop all rows
DELETE FROM payment;
ALTER TABLE payment ADD COLUMN occperiod_periodid INTEGER NOT NULL CONSTRAINT payment_occperiodid_fk REFERENCES occperiod (periodid);



ALTER TABLE public.space RENAME TO occspace;
ALTER TABLE public.spaceelement RENAME TO occspaceelement;
ALTER TABLE public.spacetype RENAME TO occspacetype;

ALTER TABLE public.inspectedchecklistspaceelement RENAME TO occinspectedchecklistspaceelement;
ALTER TABLE public.inspectedchecklistspaceelementphotodoc RENAME TO occinspectedchecklistspaceelementphotodoc;
ALTER TABLE public.locationdescription RENAME TO occlocationdescription;

ALTER TABLE public.occinspectedchecklistspaceelement DROP COLUMN inspected;
ALTER TABLE public.occinspectedchecklistspaceelement DROP COLUMN compliancedate;
ALTER TABLE public.occinspectedchecklistspaceelement ADD COLUMN lastinspectedby_userid INTEGER CONSTRAINT occinspectedchklstspel_lastinspecby_userid_fk REFERENCES public.login (userid);
ALTER TABLE public.occinspectedchecklistspaceelement ADD COLUMN lastinspectedbyts TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occinspectedchecklistspaceelement ADD COLUMN compliancegrantedby_userid INTEGER CONSTRAINT occinspectedchklstspel_complianceby_userid_fk REFERENCES public.login (userid);
ALTER TABLE public.occinspectedchecklistspaceelement ADD COLUMN compliancegrantedts TIMESTAMP WITH TIME ZONE;

ALTER TABLE public.occspacetype ADD COLUMN required boolean;
ALTER TABLE public.occspacetype ALTER COLUMN required SET DEFAULT false;

-- deprecated after creation of severity class
DROP TABLE codesetelementclass CASCADE;

ALTER TABLE codesetelement RENAME COLUMN class_classid TO defaultseverityclass_classid; 
ALTER TABLE codesetelement 
  ADD CONSTRAINT codesetele_severityclassdefault_classid_fk 
  FOREIGN KEY (defaultseverityclass_classid) 
  REFERENCES codeviolationseverityclass (classid);

ALTER TABLE codesetelement ADD COLUMN fee_feeid INTEGER CONSTRAINT codesetelement_feeid_fk REFERENCES occinspectionfee (feeid);

DROP TABLE occpermitapplicationperson CASCADE;
-- DROP TABLE occperiodperson CASCADE;

CREATE TABLE public.occperiodperson
(
  period_periodid integer NOT NULL,
  person_personid integer NOT NULL,
  applicant boolean,
  preferredcontact boolean,
  applicationpersontype persontype NOT NULL DEFAULT 'Other'::persontype,
  CONSTRAINT occperiodperson_comp_pk PRIMARY KEY (period_periodid, person_personid),
  CONSTRAINT occperiodperson_periodid_fk FOREIGN KEY (period_periodid)
      REFERENCES public.occperiod (periodid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occperiodperson_personid_fk FOREIGN KEY (person_personid)
      REFERENCES public.person (personid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);



INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (14, 'database/patches/dbpatch_beta15.sql', '06-29-2019', 'ecd', 'occupancy adjustments');

