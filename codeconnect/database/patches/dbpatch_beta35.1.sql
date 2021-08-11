-- Occupancy inspection refactor for inspeection UI by JURPLEL


-- ****************************************************************************
-- RUN THIS CREATE TYPE BY ITSELF!
-- CREATE TYPE occinspectionphototype AS ENUM ('PassDocumentation','FailDocumentation','GeneralDocumentation','Other','Unused');

-- ****************************************************************************



ALTER TABLE occspacetype RENAME TO occhecklistspacetype;

ALTER TABLE occspaceelement RENAME TO occchecklistspacetypeelement;

ALTER TABLE public.occchecklistspacetypeelement
	ADD COLUMN checklistspacetype_typeid INTEGER 
	CONSTRAINT occchecklistspacetypeelement_checklistspacetype_typeid_fk REFERENCES public.occchecklistspacetype (checklistspacetypeid);

ALTER TABLE public.occchecklistspacetypeelement DROP COLUMN space_id;

-- replaced with occspacetypeelement
DROP TABLE public.occspace CASCADE;

ALTER TABLE occchecklist ADD COLUMN createdts TIMESTAMP WITH TIME ZONE;


-- overly complex logic:drop 
ALTER TABLE public.occchecklistspacetype
	DROP COLUMN overridespacetyperequired;

-- overly complex logic:drop 
ALTER TABLE public.occchecklistspacetype
	DROP COLUMN overridespacetyperequiredvalue;

-- overly complex logic:drop 
ALTER TABLE public.occchecklistspacetype
	DROP COLUMN overridespacetyperequireallspaces;

ALTER TABLE public.occchecklistspacetype
	ADD COLUMN notes TEXT;

ALTER TABLE public.occinspectedspace
	DROP COLUMN occspace_spaceid;

ALTER TABLE public.occinspectedspace
	ADD COLUMN occchecklistspacetype_chklstspctypid INTEGER 
	CONSTRAINT occinspectedspace_chklstspctypid_fk REFERENCES public.occchecklistspacetype (checklistspacetypeid);

ALTER TABLE public.occinspectedspaceelement
	ADD COLUMN migratetocecaseonfail BOOLEAN DEFAULT TRUE;

ALTER TABLE public.occinspectedspaceelement
	RENAME COLUMN spaceelement_elementid TO occchecklistspacetypeelement_elementid;




ALTER TABLE public.occinspection 
	ADD COLUMN followupto_inspectionid INTEGER 
	CONSTRAINT occinspection_followupto_fk REFERENCES occinspection (inspectionid);

ALTER TABLE public.occinspection 
	ADD COLUMN deactivatedts TIMESTAMP WITH TIME ZONE;

ALTER TABLE public.occinspection 
	ADD COLUMN deactivatedts TIMESTAMP WITH TIME ZONE;

ALTER TABLE public.occinspection 
	ADD COLUMN deactivatedby_userid INTEGER 
	CONSTRAINT occinspection_deactivatedby_fk REFERENCES login (userid);



ALTER TABLE public.occinspection
	RENAME COLUMN notes TO notespreinspection;

ALTER TABLE public.occinspection ADD COLUMN timestart TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occinspection ADD COLUMN timeend TIMESTAMP WITH TIME ZONE;

-- replaced with standard deactivatedts and deactivatedby_userid
ALTER TABLE public.occinspection DROP COLUMN active;
ALTER TABLE public.occinspection ADD COLUMN createdby_userid INTEGER 
	CONSTRAINT occinspection_creationby_fk REFERENCES login (userid);

ALTER TABLE public.occinspection ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occinspection ADD COLUMN lastupdatedby_userid INTEGER 
	CONSTRAINT occinspection_lastupdatedby_fk REFERENCES login (userid);



CREATE TABLE public.occinspectionpropertystatus
(
	occinspection_inspectionid INTEGER NOT NULL,
	propertystatus_statusid 	INTEGER NOT NULL,
	notes 						TEXT,
	CONSTRAINT occinspectionpropstatuspk_comp PRIMARY KEY (occinspection_inspectionid, propertystatus_statusid),
	CONSTRAINT occinspectionpropertystatus_inspection_fk FOREIGN KEY (occinspection_inspectionid) 
		REFERENCES occinspection (inspectionid),
	CONSTRAINT occinspectionpropertystatus_status_fk FOREIGN KEY (propertystatus_statusid)
		REFERENCES propertystatus (statusid)
);


CREATE SEQUENCE IF NOT EXISTS occinspectioncause_causeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.occinspectioncause
(
	causeid 		INTEGER PRIMARY KEY DEFAULT nextval('occinspectioncause_causeid_seq'),
	title 			TEXT NOT NULL,
	description 	TEXT,
	notes 			TEXT,
	active 			BOOLEAN DEFAULT true
);

ALTER TABLE public.occinspection 
	ADD COLUMN cause_causeid INTEGER 
	CONSTRAINT occinspection_cause_fk REFERENCES occinspectioncause (causeid);


CREATE SEQUENCE IF NOT EXISTS occinspection_determination_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.occinspectiondetermination
(
	determinationid 		INTEGER PRIMARY KEY DEFAULT nextval('occinspection_determination_seq'),
	title 			TEXT NOT NULL,
	description 	TEXT,
	notes 			TEXT,
	eventcat_catid 	INTEGER CONSTRAINT occinspectiondetermination_eventcat_fk REFERENCES eventcategory (categoryid),
	active 			BOOLEAN DEFAULT true
);

ALTER TABLE public.occinspection 
	ADD COLUMN determination_detid INTEGER 
	CONSTRAINT occinspection_determination_fk REFERENCES occinspectiondetermination (determinationid);

ALTER TABLE public.occinspection 
	ADD COLUMN determinationby_userid INTEGER
	CONSTRAINT occinpection_determinationby_fk REFERENCES login (userid);

ALTER TABLE public.occinspection
	ADD COLUMN determinationts TIMESTAMP WITH TIME ZONE;




ALTER TABLE public.occinspection DROP COLUMN passedinspection_userid;
ALTER TABLE public.occinspection DROP COLUMN passedinspectionts;

ALTER TABLE public.occinspection ADD COLUMN remarks TEXT;


ALTER TABLE public.occinspection ADD COLUMN generalcomments TEXT;


CREATE SEQUENCE IF NOT EXISTS occchecklist_photorequirement_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.occphotorequirement
(
	requirementid 		INTEGER PRIMARY KEY DEFAULT nextval('occchecklist_photorequirement_seq'),
	title 			TEXT NOT NULL,
	description 	TEXT,
	notes 			TEXT,
	required 		BOOLEAN,
	active 			BOOLEAN DEFAULT true
);

CREATE TABLE public.occchecklistphotorequirement
(
	occchecklist_checklistid 	INTEGER NOT NULL,
	occphotorequirement_reqid 	INTEGER NOT NULL,
	CONSTRAINT occchecklistphotorequirement_pk_comp PRIMARY KEY (occchecklist_checklistid, occphotorequirement_reqid),
	CONSTRAINT occchecklistphotorequirement_checklist_fk FOREIGN KEY (occphotorequirement_reqid)
		REFERENCES public.occchecklist (checklistid),
	CONSTRAINT occchecklistphotorequirement_requirement_fk FOREIGN KEY (occchecklist_checklistid)
		REFERENCES public.occphotorequirement (requirementid)

);

CREATE TABLE public.occinspectionphotodoc
(
  photodoc_photodocid integer NOT NULL,
  inspection_inspectionid integer NOT NULL,
  photorequirement_requirementid INTEGER,
  CONSTRAINT occinspectionphotodoc_pk_comp PRIMARY KEY (photodoc_photodocid, inspection_inspectionid),
  CONSTRAINT occinspectionphotodoc_inspection_fk FOREIGN KEY (inspection_inspectionid)
      REFERENCES public.occinspection (inspectionid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occinspectionphotodoc_phtodoc_fk FOREIGN KEY (photodoc_photodocid)
      REFERENCES public.photodoc (photodocid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occinpectionphotodoc_requirement_fk FOREIGN KEY (photorequirement_requirementid)
  		REFERENCES public.occphotorequirement (requirementid)
);

DROP TABLE public.occinspectedspaceelementpdfdoc;

ALTER TABLE public.occinspectedspaceelementphotodoc ADD COLUMN phototype occinspectionphototype;

-- RUN LOCALLY UP TO HERE

--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (351, 'database/patches/dbpatch_beta35.1.sql', '07-19-2021', 'ecd', 'Occ refactor: collapse occspace and occspacetype, revise occinspection');
