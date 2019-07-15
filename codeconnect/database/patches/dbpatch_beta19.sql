
ALTER TABLE municipality DROP COLUMN managername;
ALTER TABLE municipality DROP COLUMN managerphone;
ALTER TABLE municipality DROP COLUMN defaultcourtentity;
ALTER TABLE municipality DROP COLUMN defaultcodeofficeruser;
ALTER TABLE municipality ADD COLUMN primarystaffcontact_userid integer CONSTRAINT muni_staffcontact_userid_fk REFERENCES login (userid);
ALTER TABLE municipality ADD COLUMN notes text;
ALTER TABLE municipality ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE municipality ADD COLUMN lastupdated_userid INTEGER CONSTRAINT muni_lastupdatedby_userid_fk REFERENCES login (userid);

ALTER TABLE municourtentity ADD COLUMN relativeorder INTEGER;
ALTER TABLE munilogin ADD COLUMN codeofficerassignmentorder INTEGER;
ALTER TABLE munilogin ADD COLUMN staffassignmentorder INTEGER;
ALTER TABLE munilogin ADD COLUMN sysadminassignmentorder INTEGER;
ALTER TABLE munilogin ADD COLUMN supportassignmentorder INTEGER;

ALTER TABLE munilogin ADD COLUMN bypasscodeofficerassignmentorder INTEGER;
ALTER TABLE munilogin ADD COLUMN bypassstaffassignmentorder INTEGER;
ALTER TABLE munilogin ADD COLUMN bypasssysadminassignmentorder INTEGER;
ALTER TABLE munilogin ADD COLUMN bypasssupportassignmentorder INTEGER;

ALTER TABLE choicedirective ADD COLUMN requiredevaluationforbobclose BOOLEAN DEFAULT true;

ALTER TABLE occperiodtype RENAME COLUMN defaultvalidityperioddays TO defaultpermitvalidityperioddays;
ALTER TABLE occperiodtype ADD COLUMN asynchronousinspectionvalidityperiod BOOLEAN DEFAULT false;
ALTER TABLE occperiodtype ADD COLUMN defaultinspectionvalidityperiod INTEGER;

ALTER TABLE propertyunit ADD COLUMN rentalintentstartdate TIMESTAMP WITH TIME ZONE;
ALTER TABLE propertyunit ADD COLUMN rentalintentstopdate TIMESTAMP WITH TIME ZONE;
ALTER TABLE propertyunit ADD COLUMN rentalintentlastupdatedby_userid INTEGER CONSTRAINT propunit_rentalintentupdatedby_fk REFERENCES eventrule (ruleid);

ALTER TABLE muniprofile ADD COLUMN continuousoccupancybufferdays INTEGER DEFAULT 0;
ALTER TABLE muniprofile ADD COLUMN minimumuserranktodeclarerentalintent INTEGER DEFAULT 3;

ALTER TABLE propertyunit DROP COLUMN rentalintent;
ALTER TABLE propertyunit ADD COLUMN rentalnotes TEXT;

ALTER TABLE occinspeection ADD COLUMN effectivedate TIMESTAMP WITH TIME ZONE;





CREATE TABLE public.muniprofileeventruleset
(
	muniprofile_profileid 					INTEGER NOT NULL CONSTRAINT muniprofileeventruleset_profileid_fk REFERENCES muniprofile (profileid),
	ruleset_setid 							INTEGER NOT NULL CONSTRAINT muniprofileeventruleset_setid_fk REFERENCES eventruleset (rulesetid),
	CONSTRAINT muniprofileeventruleset_comp_pk PRIMARY KEY (muniprofile_profileid, ruleset_setid)
);

DROP TABLE occperiodtypeeventrule;

ALTER TABLE occperiodtype ADD COLUMN eventruleset_setid INTEGER CONSTRAINT occperiodtype_eventrulesetid_fk REFERENCES eventruleset (rulesetid);

CREATE TABLE public.occperiodeventrule
(
	occperiod_periodid 				INTEGER NOT NULL CONSTRAINT occperiodeventrule_periodid_fk REFERENCES occperiod (periodid),
	eventrule_ruleid 				INTEGER NOT NULL CONSTRAINT occperiodeventrule_eventruleid_fk REFERENCES eventrule (ruleid),
	attachedts 						TIMESTAMP WITH TIME ZONE,
	attachedby_userid 				INTEGER CONSTRAINT occperiodeventrule_attachedby_userid_Fk REFERENCES login (userid),
	lastevaluatedts 				TIMESTAMP WITH TIME ZONE,
	passedrulets 					TIMESTAMP WITH TIME ZONE,
	passedrule_eventid 				INTEGER NOT NULL CONSTRAINT occperiodeventrule_eventid_fk REFERENCES occevent (eventid)
 
);

ALTER TABLE CECASERULE 
	ADD COLUMN attachedts 						TIMESTAMP WITH TIME ZONE,
	ADD COLUMN attachedby_userid 				INTEGER CONSTRAINT occperiodeventrule_attachedby_userid_Fk REFERENCES login (userid),
	ADD COLUMN lastevaluatedts 					TIMESTAMP WITH TIME ZONE,
	ADD COLUMN passedrulets 					TIMESTAMP WITH TIME ZONE,
	ADD COLUMN passedrule_eventid 				INTEGER NOT NULL CONSTRAINT occperiodeventrule_eventid_fk REFERENCES occevent (eventid);


ALTER TABLE muniprofile DROP COLUMN ceruleset;
ALTER TABLE muniprofile DROP COLUMN occresidentialruleset;
ALTER TABLE muniprofile DROP COLUMN occcommercialruleset;

CREATE TABLE public.muniprofileoccperiodtype
(

	muniprofile_profileid 						INTEGER NOT NULL CONSTRAINT muniprofileoccperiodtype_profileid_fk 	REFERENCES muniprofile (profileid),
	occperiodtype_typeid 						INTEGER NOT NULL CONSTRAINT muniprofileoccperiodtype_typeid_fk 		REFERENCES occperiodtype (typeid),
	CONSTRAINT muniprofileoccperiodtype_comp_pk 	PRIMARY KEY (muniprofile_profileid, occperiodtype_typeid)

);

ALTER TABLE eventrule DROP COLUMN requiredeventcatthresholdtypeintorder;
ALTER TABLE eventrule DROP COLUMN forbiddeneventcatthresholdtypeintorder;
ALTER TABLE eventrule DROP COLUMN requiredeventcatthresholdglobalorder;
ALTER TABLE eventrule DROP COLUMN forbiddeneventcatthresholdglobalorder;
ALTER TABLE eventrule ADD COLUMN requiredeventcatthresholdtypeintorder INTEGER;
ALTER TABLE eventrule ADD COLUMN forbiddeneventcatthresholdtypeintorder INTEGER;
ALTER TABLE eventrule ADD COLUMN requiredeventcatthresholdglobalorder INTEGER;
ALTER TABLE eventrule ADD COLUMN forbiddeneventcatthresholdglobalorder INTEGER;



ALTER TABLE choicedirective ADD COLUMN forcehideprecedingproposals BOOLEAN DEFAULT FALSE;
ALTER TABLE choicedirective ADD COLUMN forcehidetrailingproposals BOOLEAN DEFAULT FALSE;
ALTER TABLE choicedirective ADD COLUMN refusetobehidden BOOLEAN DEFAULT FALSE;

ALTER TABLE choiceproposal ADD COLUMN hidden BOOLEAN DEFAULT false;
ALTER TABLE choiceproposal ADD COLUMN generatingevent_occeventid INTEGER CONSTRAINT choiceproposal_genocceventid_fk REFERENCES occevent (eventid);
ALTER TABLE choiceproposal ADD COLUMN responseevent_occeventid INTEGER CONSTRAINT choiceproposal_resocceventid_fk REFERENCES occevent (eventid);

ALTER TABLE choiceproposal RENAME COLUMN generatingevent_eventid TO generatingevent_cecaseeventid;
ALTER TABLE choiceproposal RENAME COLUMN responseevent_eventid TO responseevent_cecaseeventid;

ALTER TABLE choicedirective DROP COLUMN directproposaltodefaultmuniadmin;

ALTER TABLE choiceproposal ADD COLUMN occperiod_periodid INTEGER CONSTRAINT choiceproposal_occperiodid_fk REFERENCES occperiod (periodid);
ALTER TABLE choiceproposal ADD COLUMN cecase_caseid INTEGER CONSTRAINT choiceproposal_cecaseid_fk REFERENCES cecase (caseid);

ALTER TABLE occchecklistspaceelement RENAME checklistspaceelementid TO checklistspacetypeid;
ALTER TABLE occchecklistspaceelement RENAME TO occchecklistspacetype;
ALTER TABLE occchecklistspacetype ADD COLUMN spacetype_typeid INTEGER NOT NULL CONSTRAINT occchecklistspacetype_typeid_fk REFERENCES occspacetype (spacetypeid);

ALTER TABLE occchecklistspacetype ADD COLUMN overridespacetyperequired BOOLEAN DEFAULT false;
ALTER TABLE occchecklistspacetype ADD COLUMN overridespacetyperequiredvalue BOOLEAN DEFAULT false;
ALTER TABLE occchecklistspacetype ADD COLUMN overridespacetyperequireallspaces BOOLEAN DEFAULT false;

ALTER TABLE occchecklistspacetype DROP COLUMN spaceelement_id;

ALTER TABLE occinspectedchecklistspaceelement RENAME TO occinspectedspaceelement;


CREATE SEQUENCE IF NOT EXISTS occinspectedspace_pk_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;

CREATE TABLE public.occinspectedspace
(
	inspectedspaceid 				INTEGER NOT NULL DEFAULT nextval('occinspectedspace_pk_seq') PRIMARY KEY,
	occspace_spaceid 				INTEGER NOT NULL CONSTRAINT occinspectedspace_spaceid_fk REFERENCES occspace (spaceid),
	occinspection_inspectionid 		INTEGER NOT NULL CONSTRAINT occinspectedspace_inspecid_fk REFERENCES occinspection (inspectionid),
	occlocationdescription_descid	INTEGER NOT NULL CONSTRAINT occinspectedspace_locid_fk 	REFERENCES occlocationdescription (locationdescriptionid),
	lastinspectedby_userid	 		INTEGER NOT NULL CONSTRAINT	occinspectedspace_lastbyuserid_fk REFERENCES login (userid),
	lastinspectedts 				TIMESTAMP WITH TIME ZONE
);

ALTER TABLE occinspectedspaceelement ADD COLUMN inspectedspace_inspectedspaceid INTEGER NOT NULL 
	CONSTRAINT occinspectedspaceelement_spaceid_fk 
	REFERENCES occinspectedspace (inspectedspaceid);

ALTER TABLE occinspectedchecklistspaceelementphotodoc RENAME TO occinspectedspaceelementphotodoc;

ALTER TABLE public.occinspectedspaceelement DROP COLUMN occupancyinspection_id;
ALTER TABLE public.occinspectedspaceelement DROP COLUMN checklistspaceelement_id;

ALTER TABLE public.occinspectedspaceelement RENAME inspectedchecklistspaceelementid TO inspectedspaceelementid;

INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (19, 'database/patches/dbpatch_beta19.sql', '07-09-2019', 'ecd', 'municipality facelift and others');

