
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

ALTER TABLE occinspection ADD COLUMN effectivedate TIMESTAMP WITH TIME ZONE;


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

ALTER TABLE public.occinspectedspaceelementphotodoc DROP CONSTRAINT IF EXISTS inspchklstspele_elementid_cv_fk;
ALTER TABLE public.occinspectedspaceelementphotodoc 
	ADD CONSTRAINT occinspectedspaceelementphotodoc_photodocid_fk 
	FOREIGN KEY (photodoc_photodocid)
	REFERENCES photodoc (photodocid);

ALTER TABLE public.occinspectedspaceelementphotodoc DROP CONSTRAINT IF EXISTS inspchklstspele_elementid_phdoc_fk;
ALTER TABLE public.occinspectedspaceelementphotodoc 
	ADD CONSTRAINT occinspectedspaceelementphotodoc_inspectedele_fk 
	FOREIGN KEY (inspchklstspele_elementid)
	REFERENCES occinspectedspaceelement (inspectedspaceelementid);

ALTER TABLE occspaceelement ADD COLUMN required BOOLEAN DEFAULT true;
ALTER TABLE occspace ADD COLUMN description TEXT;

ALTER TABLE occinspectedspaceelement ADD COLUMN overriderequiredflagnotinspected_userid INTEGER 
	CONSTRAINT occinspectedspaceelement_overridereq_userid_fk 
	REFERENCES login (userid);

ALTER TABLE muniprofile ADD COLUMN minimumuserrankforinspectionoverrides INTEGER DEFAULT 3;

ALTER TABLE public.occinspectedspaceelementphotodoc RENAME inspchklstspele_elementid  TO inspectedspaceelement_elementid;

ALTER TABLE public.occinspectedspaceelement ADD COLUMN spaceelement_elementid INTEGER NOT NULL
	CONSTRAINT occinspectedspaceelement_elementid_fk 
	REFERENCES occspaceelement (spaceelementid);


ALTER TABLE public.occinspectedspaceelement RENAME lastinspectedbyts  TO lastinspectedts;
ALTER TABLE public.occinspectedspaceelement ADD COLUMN required BOOLEAN DEFAULT TRUE;

ALTER TABLE public.propertyunit ADD COLUMN active boolean default true;

ALTER TABLE codeviolationseverityclass RENAME TO intensityclass;

DROP TABLE public.datasource CASCADE;

ALTER TABLE genlog RENAME TO log;
ALTER TABLE genlogcategory RENAME TO logcategory;

DROP TABLE personsource CASCADE;

ALTER TABLE person ADD COLUMN bobsource_sourceid INTEGER CONSTRAINT person_bobsourceid_fk REFERENCES bobsource (sourceid);
ALTER TABLE property ADD COLUMN bobsource_sourceid INTEGER CONSTRAINT property_bobsourceid_fk REFERENCES bobsource (sourceid);


ALTER TABLE property ADD COLUMN unfitdatestart TIMESTAMP WITH TIME ZONE;
ALTER TABLE property ADD COLUMN unfitdatestop TIMESTAMP WITH TIME ZONE;
ALTER TABLE property ADD COLUMN unfitby_userid INTEGER 
	CONSTRAINT property_unfitby_userid_fk 
	REFERENCES login (userid);

ALTER TABLE property ADD COLUMN abandoneddatestart TIMESTAMP WITH TIME ZONE;
ALTER TABLE property ADD COLUMN abandoneddatestop TIMESTAMP WITH TIME ZONE;
ALTER TABLE property ADD COLUMN abandonedby_userid INTEGER CONSTRAINT property_abandoned_userid_fk REFERENCES login (userid);

ALTER TABLE property ADD COLUMN vacantdatestart TIMESTAMP WITH TIME ZONE;
ALTER TABLE property ADD COLUMN vacantdatestop TIMESTAMP WITH TIME ZONE;
ALTER TABLE property ADD COLUMN vacantbu_userid INTEGER CONSTRAINT property_vacant_userid_fk REFERENCES login (userid);

ALTER TABLE property ADD COLUMN condition_intensityclassid INTEGER CONSTRAINT property_conditionintensityclass_classid_fk REFERENCES intensityclass (classid);
ALTER TABLE property ADD COLUMN landbankprospect_intensityclassid INTEGER CONSTRAINT property_landbankprospect_classid_fk REFERENCES intensityclass (classid);
ALTER TABLE property ADD COLUMN landbankheld BOOLEAN DEFAULT FALSE;

ALTER TABLE property DROP COLUMN vacant;
ALTER TABLE property ADD COLUMN active BOOLEAN DEFAULT TRUE;

ALTER TABLE property DROP COLUMN datasource;

ALTER TABLE propertyunit ADD COLUMN condition_intensityclassid INTEGER CONSTRAINT propunit_conditionintensityclass_classid_fk REFERENCES intensityclass (classid);
ALTER TABLE property ADD COLUMN nonaddressable BOOLEAN DEFAULT FALSE;




CREATE SEQUENCE IF NOT EXISTS propertystatusid_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;

CREATE TABLE public.propertystatus
(
	statusid 						INTEGER NOT NULL DEFAULT nextval('propertystatusid_seq') PRIMARY KEY,
	title 							text,
	description 					text,
	userdeployable					boolean DEFAULT true,
	minimumuserranktoassign 		integer DEFAULT 2,
	minimumuserranktoremove 		integer DEFAULT 2,
	muni_municode 					integer CONSTRAINT propertystatus_municode_fk REFERENCES municipality (municode),
	active 							boolean DEFAULT TRUE

);

ALTER TABLE property DROP COLUMN status_statusid;


ALTER TABLE property ADD COLUMN status_statusid INTEGER CONSTRAINT property_statusid_fk REFERENCES propertystatus (statusid);

ALTER TABLE property DROP COLUMN propertyusetype;
ALTER TABLE property ADD COLUMN usetype_typeid INTEGER CONSTRAINT property_propertyusetypeid_fk  REFERENCES propertyusetype (propertyusetypeid);

ALTER TABLE propertyusetype ADD COLUMN zoneclass TEXT;

ALTER TABLE property RENAME vacantbu_userid TO vacantby_userid;

ALTER TABLE property DROP COLUMN status_statusid;

CREATE SEQUENCE IF NOT EXISTS propertyotherid_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;

CREATE TABLE public.propertyotherid
(
	otheridid 				INTEGER NOT NULL DEFAULT nextval('propertyotherid_seq') PRIMARY KEY,
	property_propid 		INTEGER NOT NULL CONSTRAINT propertyotherid_propid_fk REFERENCES property (propertyid),
	otheraddress 			text,
	otheraddressnotes 		text,
	otheraddresslastupdated TIMESTAMP WITH TIME ZONE,
	otherlotandblock 		text,
	otherlotandblocknotes 	text,
	otherlotandblocklastupdated 	TIMESTAMP WITH TIME ZONE,
	otherparcelid 			text,
	otherparcelidnotes 		text,
	otherparceladdresslastupdated 	TIMESTAMP WITH TIME ZONE
);

ALTER TABLE propertyunit ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.propertyunit RENAME rentalintentstartdate  TO rentalintentdatestart;
ALTER TABLE public.propertyunit RENAME rentalintentstopdate  TO rentalintentdatestop;
ALTER TABLE public.property DROP COLUMN apartmentno;

ALTER TABLE public.occlocationdescription RENAME TO occlocationdescriptor;
ALTER TABLE public.occlocationdescriptor ADD COLUMN buildingfloorno INTEGER; 

ALTER TABLE public.occinspectedspaceelement ADD COLUMN failureseverity_intensityclassid INTEGER 
	CONSTRAINT occinspectedspaceele_intenclassid_fk
	REFERENCES intensityclass (classid);

ALTER TABLE occperiodtype ADD COLUMN inspectable BOOLEAN DEFAULT TRUE;

ALTER TABLE munilogin ADD COLUMN recorddeactivatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE munilogin ADD COLUMN userrole role;

ALTER TABLE login DROP COLUMN activitystartdate;
ALTER TABLE login DROP COLUMN activitystopdate;
ALTER TABLE login DROP COLUMN accesspermitted;
ALTER TABLE login DROP COLUMN enforcementofficial;

ALTER TABLE public.munilogin DROP CONSTRAINT loginmuni_pkey;

CREATE SEQUENCE IF NOT EXISTS munilogin_recordid_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;

ALTER TABLE munilogin ADD COLUMN muniloginrecordid INTEGER DEFAULT nextval('munilogin_recordid_seq') PRIMARY KEY;
ALTER TABLE munilogin ADD COLUMN recordcreatedts TIMESTAMP WITH TIME ZONE DEFAULT now();
ALTER TABLE munilogin RENAME activitystartdate TO accessgranteddatestart;
ALTER TABLE munilogin RENAME activitystopdate TO accessgranteddatestop;

ALTER TABLE login DROP COLUMN userrole;

ALTER TABLE login DROP COLUMN badgenumber;
ALTER TABLE login DROP COLUMN orinumber;

ALTER TABLE munilogin ADD COLUMN badgenumber text;
ALTER TABLE munilogin ADD COLUMN orinumber text;

-- REALEASED DON'T ADD TO ME!

INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (19, 'database/patches/dbpatch_beta19.sql', '07-09-2019', 'ecd', 'municipality facelift and others');

