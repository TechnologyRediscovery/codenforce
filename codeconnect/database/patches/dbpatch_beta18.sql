


ALTER TABLE occperiodtype DROP COLUMN fee_feeid CASCADE;

ALTER TABLE occperiodtype RENAME COLUMN completedinspectionrequired TO passedinspectionrequired;

ALTER TABLE occperiodtype ADD COLUMN defaultvalidityperioddays INTEGER;

-- occupancy permit applications CANNOT BE FOR MORE THAN ONE UNIT
ALTER TABLE occpermitapplication DROP COLUMN multiunit;

ALTER TABLE occpermitapplication DROP COLUMN occupancyinspection_id;

ALTER TABLE occpermitapplication ADD COLUMN occperiod_periodid INTEGER CONSTRAINT occpermitapp_periodid_fk REFERENCES occperiod (periodid);

-- Need to turn on NOT NULL for occpermitapplications's occ period fk pointer
-- ALTER TABLE occpermitapplication ALTER COLUMN occperiod_periodid ADD CONSTRAINT NOT NULL; 

--
--  MAJOR EVENT OVERHAUL: INTRODUCTION OF CHOICES!
--

ALTER TABLE eventchoice RENAME TO choice;
ALTER TABLE eventproposal RENAME TO choiceproposal;
ALTER TABLE eventproposalimplementation RENAME TO choiceproposalimplementation;
ALTER TABLE eventproposalchoice RENAME TO choiceproposalchoice;

ALTER TABLE choiceproposal ADD COLUMN directproposaltodefaultmuniadmin BOOLEAN DEFAULT false;
ALTER TABLE choice ADD COLUMN worflowpagetriggerconstantvar text;

ALTER TABLE occperiodtype ADD COLUMN occchecklist_checklistlistid INTEGER CONSTRAINT occperiodtype_checklistid_fk REFERENCES occchecklist (checklistid);
ALTER TABLE occinspection ADD COLUMN occchecklist_checklistlistid INTEGER CONSTRAINT occinspection_checklistid_fk REFERENCES occchecklist (checklistid);

--
--
-- 	MAJOR MUNICPALITY SETTINGS UPGRADE FOR BETA 0.9
--
--

ALTER TABLE loginmuni RENAME TO munilogin;

ALTER TABLE munilogin ADD COLUMN  activitystartdate timestamp with time zone DEFAULT '01-01-1970' NOT NULL;
ALTER TABLE munilogin ADD COLUMN activitystopdate timestamp with time zone DEFAULT '01-01-1970' NOT NULL;
-- ALTER TABLE public.login DROP COLUMN activitystartdate;
-- ALTER TABLE public.login DROP COLUMN activitystopdate;



CREATE SEQUENCE IF NOT EXISTS muni_muniprofile_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;





-- start below here



CREATE SEQUENCE IF NOT EXISTS eventrulesetid_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;

CREATE TABLE public.eventruleset
(
	rulesetid 						INTEGER NOT NULL DEFAULT nextval('eventrulesetid_seq') PRIMARY KEY,
	title 							text,
	description 					text
);


CREATE TABLE public.eventruleruleset
(
	ruleset_rulesetid 				INTEGER NOT NULL CONSTRAINT evruleevruleset_setid_fk REFERENCES eventruleset (rulesetid),
	eventrule_ruleid 				INTEGER NOT NULL CONSTRAINT evruleevruleset_ruleid_fk REFERENCES eventrule (ruleid),
	CONSTRAINT eventruleset_comp_pf PRIMARY KEY (ruleset_rulesetid, eventrule_ruleid)

);


CREATE SEQUENCE IF NOT EXISTS choiceproposalsetid_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;

CREATE TABLE public.choiceproposalset
(
	proposalsetid 					INTEGER NOT NULL DEFAULT nextval('choiceproposalsetid_seq') PRIMARY KEY,
	title 							text,
	description 					text
);


CREATE TABLE public.choiceproppropset
(
	proposalset_setid 				INTEGER NOT NULL CONSTRAINT choicepropchoicepropset_setid_fk REFERENCES choiceproposalset (proposalsetid),
	choiceproposal_propid 	 				INTEGER NOT NULL CONSTRAINT choicepropchoicepropset_prop_fk REFERENCES choiceproposal (proposalid),
	CONSTRAINT choiceproppropset_comp_pf PRIMARY KEY (proposalset_setid, choiceproposal_propid)

);




ALTER TABLE munilogin ADD COLUMN codeofficerstartdate TIMESTAMP WITH TIME ZONE;
ALTER TABLE munilogin ADD COLUMN codeofficerstopdate TIMESTAMP WITH TIME ZONE;

ALTER TABLE munilogin ADD COLUMN staffstartdate TIMESTAMP WITH TIME ZONE;
ALTER TABLE munilogin ADD COLUMN staffstopdate TIMESTAMP WITH TIME ZONE;

ALTER TABLE munilogin ADD COLUMN sysadminstartdate TIMESTAMP WITH TIME ZONE;
ALTER TABLE munilogin ADD COLUMN sysadminstopdate TIMESTAMP WITH TIME ZONE;

ALTER TABLE munilogin ADD COLUMN supportstartdate TIMESTAMP WITH TIME ZONE;
ALTER TABLE munilogin ADD COLUMN supportstopdate TIMESTAMP WITH TIME ZONE;



DROP TABLE IF EXISTS muniprofile CASCADE;

CREATE TABLE public.muniprofile
  (
	
  	profileid 						INTEGER NOT NULL DEFAULT nextval('muni_muniprofile_seq') PRIMARY KEY,
  	title 							text NOT NULL,
  	description 					text,
  	lastupdatedts					TIMESTAMP WITH TIME ZONE,
  	lastupdatedby_userid 			INTEGER CONSTRAINT muniprofile_lastupdateduserid_fk REFERENCES login (userid),
  	notes							text,
  	ceruleset 						INTEGER NOT NULL CONSTRAINT muniprofile_cerulesetid_fk REFERENCES eventruleset (rulesetid),
  	occresidentialruleset 			INTEGER NOT NULL CONSTRAINT muniprofile_occresrulesetid_fk REFERENCES eventruleset (rulesetid),
  	occcommercialruleset 			INTEGER NOT NULL CONSTRAINT muniprofile_occcommercialrulesetid_fk REFERENCES eventruleset (rulesetid)
);


ALTER TABLE municipality 

  	ADD COLUMN enablecodeenformcent 			boolean DEFAULT true,
  	ADD COLUMN enableoccupancy 					boolean DEFAULT true,
  	ADD COLUMN enablepublicceactionreqsub 		boolean DEFAULT true,
  	ADD COLUMN enablepublicceactionreqinfo 		boolean DEFAULT true,
  	ADD COLUMN enablepublicoccpermitapp			boolean DEFAULT false,
  	ADD COLUMN enablepublicoccinspectodo		boolean DEFAULT true,
  	ADD COLUMN munimanager_userid 				INTEGER CONSTRAINT muni_manageruserid_fk REFERENCES login (userid),
  	ADD COLUMN office_propertyid 				INTEGER CONSTRAINT muni_munipropid_fk 	 REFERENCES property (propertyid),
    ADD COLUMN profile_profileid      INTEGER CONSTRAINT muni_profileid_fk REFERENCES muniprofile (profileid);


CREATE TABLE public.municourtentity
(
	muni_municode 								INTEGER NOT NULL CONSTRAINT municourtentity_municode_fk REFERENCES municipality (municode),
	courtentity_entityid 						INTEGER NOT NULL CONSTRAINT municourtentity_courtid_fk REFERENCES courtentity (entityid),
	CONSTRAINT municourtentity_comp_pk 	PRIMARY KEY (muni_municode, courtentity_entityid)
);

ALTER TABLE courtentity DROP COLUMN muni_municode CASCADE;

ALTER TABLE eventcategory RENAME COLUMN relativeorderacrossallevents TO relativeorderglobal;

ALTER TABLE eventrule ADD COLUMN notes TEXT;

DROP TABLE propertyevent;

ALTER TABLE cecase ADD COLUMN personinfocase_personid INTEGER CONSTRAINT cecase_personid_fk REFERENCES person (personid);

ALTER TABLE cecase ADD COLUMN bobsource_sourceid INTEGER CONSTRAINT cecase_bobsourceid_fk REFERENCES bobsource (sourceid);

ALTER TABLE public.occpermitapplication ADD COLUMN rentalintent boolean;

/* new propertyunitchange table*/

CREATE TABLE public.propertyunitchange
(
  unitchangeid integer NOT NULL DEFAULT nextval('propertunit_unitid_seq'::regclass),
  propertyunit_unitid integer CONSTRAINT propertyunitchange_unitid_fk REFERENCES propertyunit (unitid),
  unitnumber text,
  otherknownaddress text,
  rentalintent boolean,
  removed boolean,
  added boolean,
  entryts timestamp with time zone,
  approvedondate timestamp with time zone,
  approvedby_userid integer CONSTRAINT propertyunitchange_approvedby_fk REFERENCES login (userid),
  changedby_userid INTEGER CONSTRAINT propertyunitchange_changedbyuserid_fk REFERENCES login (userid),
  changedby_personid INTEGER CONSTRAINT propertyunitchange_changedbypersonid_fk REFERENCES person (personid),
  active boolean DEFAULT true,
  notes text,
  CONSTRAINT unitchangeid_pk PRIMARY KEY (unitchangeid)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE property DROP COLUMN containsrentalunits;

ALTER TABLE propertyunit RENAME COLUMN rental TO rentalintent;


ALTER TABLE choiceproposal RENAME TO choicedirective;
ALTER TABLE choiceproposalimplementation RENAME TO choiceproposal;
ALTER TABLE choiceproposal RENAME COLUMN implementationid TO proposalid;
ALTER TABLE choiceproposal RENAME COLUMN proposal_propid TO directive_directiveid;
ALTER TABLE choicedirective RENAME COLUMN proposalid TO directiveid;

ALTER TABLE choicedirective RENAME COLUMN directproposaltodefaultmuniceo TO directtodefaultmuniceo;
ALTER TABLE choicedirective RENAME COLUMN directproposaltodefaultmunistaffer TO directtodefaultmunistaffer;
ALTER TABLE choicedirective RENAME COLUMN directproposaltodeveloper TO directtodeveloper;
ALTER TABLE choicedirective ADD COLUMN directtomunisysadmin BOOLEAN DEFAULT false;

ALTER TABLE choiceproposalset RENAME TO choicedirectiveset;
ALTER TABLE choiceproposalchoice RENAME TO choicedirectivechoice;


DROP TABLE public.choiceproppropset;

ALTER TABLE choicedirectiveset RENAME COLUMN proposalsetid TO directivesetid;
CREATE TABLE public.choicedirectivedirectiveset
(
	directiveset_setid 						INTEGER NOT NULL CONSTRAINT choicedirdirset_dirsetid_fk REFERENCES choicedirectiveset (directivesetid),
	directive_dirid 	 					INTEGER NOT NULL CONSTRAINT choicedirdirset_dirid_fk REFERENCES choicedirective (directiveid),
	CONSTRAINT choicedirdirset_comp_pf PRIMARY KEY (directiveset_setid, directive_dirid)

);

ALTER TABLE choicedirectivechoice RENAME COLUMN eventproposal_proposalid TO directive_directiveid;

ALTER SEQUENCE choiceproposalsetid_seq RENAME TO choicedirectivesetid_seq;

ALTER TABLE eventcategory RENAME COLUMN proposal_propid TO directive_directiveid;

-- Cleanup on occ
ALTER TABLE occspace ADD COLUMN required BOOLEAN default false;

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (18, 'database/patches/dbpatch_beta18.sql', '07-04-2019', 'ecd', 'tweaks during occupancy beta integration');

