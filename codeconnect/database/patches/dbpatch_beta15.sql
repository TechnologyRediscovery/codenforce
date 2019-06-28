
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


DROP TABLE public.occupancyinspectionstatus;



-- this will also drop a FK on the loginobjecthistory
DROP TABLE occupancypermit CASCADE;

CREATE TABLE public.occpermit
(
  permitid 								INTEGER NOT NULL DEFAULT nextval('occupancypermit_permitid_seq'::regclass) CONSTRAINT occpermit_permitid_pk PRIMARY KEY,
  occperiod_periodid 					INTEGER NOT NULL CONSTRAINT occpermit_periodid_fk REFERENCES public.occperiod (periodid),
  referenceno 							TEXT,
  permittype_typeid  					INTEGER CONSTRAINT occpermit_permittype_fk REFERENCES public.occpermittype (typeid),
  issuedto_personid 					INTEGER NOT NULL CONSTRAINT occpermit_issuedto_personid_fk REFERENCES public.person (personid),
  issuedby_userid 						INTEGER CONSTRAINT occperiod_startcert_userid_fk REFERENCES public.login (userid), 
  dateissued 							TIMESTAMP WITH TIME ZONE NOT NULL,
  permitadditionaltext 					TEXT,
  notes 								TEXT
) ;

-- Since we want to be able to assocaite an occupancy permit with a permitting code source (like IPMC 2015), 
-- we had the FK on the permit to just poop it out onto the printed page. Instead, move it to the checklist itself

ALTER TABLE public.occinspecchecklist RENAME TO occchecklist;
ALTER TABLE public.occchecklist ADD COLUMN governingcodesource_sourceid INTEGER CONSTRAINT occinspecchecklist_codesourceid_fk REFERENCES codesource (sourceid);
ALTER TABLE public.eventrule DROP CONSTRAINT phasechangerule_triggeredevcatreqcat_fk;
ALTER TABLE public.eventrule DROP COLUMN triggeredeventproposal;

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
	ruletopassforpermitissuance 		 INTEGER CONSTRAINT occperiodtype_passedruleforopermit_ruleid_fk REFERENCES public.eventrule (ruleid),
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

-- ALTER TABLE occperiodtype ADD COLUMN requirepersontypeentrycheck     boolean DEFAULT false;

-- Only used for test buld of DB; they are included in formal table def above
-- ALTER TABLE occperiodtype ADD COLUMN optionalpersontypes             persontype[];
-- ALTER TABLE occperiodtype ADD COLUMN requiredpersontypes             persontype[];

ALTER TABLE occpermitapplicationreason ADD COLUMN periodtypeproposal_periodid INTEGER CONSTRAINT occpermitapprsn_pertype_fk REFERENCES public.occperiodtype (typeid);

--***************************************
--  add reasons and their type proposal mappings here
--***************************************
--***************************************
--***************************************
--***************************************
--***************************************
--***************************************
--***************************************
--***************************************
--***************************************
--***************************************
--***************************************
--***************************************
--***************************************
--***************************************


-- TODO

ALTER TABLE public.occpermitapplicationreason
   ALTER COLUMN periodtypeproposal_periodid SET NOT NULL;


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
	notes 								TEXT,


) ;

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
    notes                           TEXT
    thirdpartyinspector_personid integer CONSTRAINT occinspection_thirdpartyuserid_fk REFERENCES public.person (personid),
    thirdpartyinspectorapprovalts timestamp with time zone,
    thirdpartyinspectorapprovalby INTEGER CONSTRAINT occinspectionthirdpartyapprovalby_fk REFERENCES public.login (userid)
) ;

-- Drop all rows
DELETE FROM payment;
ALTER TABLE payment ADD CONSTRAINT payment_occinspectionid_fk FOREIGN KEY (occinspec_inspectionid) REFERENCES occinspection (inspectionid);


ALTER TABLE public.ceeventcategory RENAME TO eventcategory;
ALTER TABLE public.cecasephasechangerule RENAME TO eventrule;
ALTER TABLE public.ceeventproposal RENAME TO eventproposal;


CREATE SEQUENCE IF NOT EXISTS occevent_eventid_seq
	START WITH 1000
	INCREMENT BY 1 
	MINVALUE 1000
	NO MAXVALUE 
	CACHE 1;

CREATE TABLE public.occevent
(
  eventid integer NOT NULL DEFAULT nextval('occevent_eventid_seq'::regclass),
  category_catid integer NOT NULL,
  occperiod_periodid INTEGER NOT NULL CONSTRAINT occevent_periodid_fk REFERENCES public.occperiod (periodid),
  dateofrecord timestamp with time zone,
  eventtimestamp timestamp with time zone,
  eventdescription text,
  owner_userid integer NOT NULL,
  disclosetomunicipality boolean DEFAULT true,
  disclosetopublic boolean DEFAULT false,
  activeevent boolean DEFAULT true,
  hidden boolean DEFAULT false,
  notes text,
  CONSTRAINT occevent_eventid_pk PRIMARY KEY (eventid),
  CONSTRAINT occevent_eventcategory_fk FOREIGN KEY (category_catid)
      REFERENCES public.eventcategory (categoryid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occevent_login_userid FOREIGN KEY (owner_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);


CREATE SEQUENCE IF NOT EXISTS occeventproposalimplementation_id_seq
	START WITH 1000
	INCREMENT BY 1 
	MINVALUE 1000
	NO MAXVALUE 
	CACHE 1;


CREATE TABLE public.occeventproposalimplementation
(
  implementationid integer NOT NULL DEFAULT nextval('occeventproposalimplementation_id_seq'::regclass),
  proposal_propid integer,
  generatingevent_eventid integer,
  initiator_userid integer,
  responderintended_userid integer,
  activateson timestamp with time zone,
  expireson timestamp with time zone,
  responderactual_userid integer,
  rejectproposal boolean DEFAULT false,
  responsetimestamp timestamp with time zone,
  responseevent_eventid integer,
  expiredorinactive boolean DEFAULT false,
  notes text,
  CONSTRAINT occeventproposalresponse_pk PRIMARY KEY (implementationid),
  CONSTRAINT occeventpropimp_genevent_fk FOREIGN KEY (generatingevent_eventid)
      REFERENCES public.occevent (eventid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventpropimp_initiator_fk FOREIGN KEY (initiator_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventprop_propid_fk FOREIGN KEY (proposal_propid)
      REFERENCES public.eventproposal (proposalid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventpropimp_respev_fk FOREIGN KEY (responseevent_eventid)
      REFERENCES public.occevent (eventid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventpropimp_responderactual_fk FOREIGN KEY (responderactual_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventpropimp_responderintended_fk FOREIGN KEY (responderintended_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);


CREATE TABLE public.occeventperson
(
  occevent_eventid integer NOT NULL,
  person_personid integer NOT NULL,
  CONSTRAINT occevent_eventid PRIMARY KEY (occevent_eventid, person_personid),
  CONSTRAINT occeventperson_occevent_eventid_fk FOREIGN KEY (occevent_eventid)
      REFERENCES public.ceevent (eventid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventperson_person_personid_fk FOREIGN KEY (person_personid)
      REFERENCES public.person (personid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);


ALTER TABLE public.space RENAME TO occspace;
ALTER TABLE public.spaceelement RENAME TO occspaceelement;
ALTER TABLE public.spacetype RENAME TO occspacetype;

ALTER TABLE public.inspectedchecklistspaceelement RENAME TO occinspectedchecklistspaceelement;
ALTER TABLE public.inspectedchecklistspaceelementphotodoc RENAME TO occinspectedchecklistspaceelementphotodoc;
ALTER TABLE public.locationdescription RENAME TO occlocationdescription;




INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (15, 'database/patches/dbpatch_beta15.sql', '', 'ecd', 'occupancy adjustments');

