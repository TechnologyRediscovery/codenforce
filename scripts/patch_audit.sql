#syntax=docker/dockerfile:1
#FROM ubuntu:16.04
#docker run --rm -P --name codenforce_db
#sudo docker ps
#sudo docker run codenforce_db
#psql -h localhost -p 5432 -d cogdb  -U changeme --password
# Create Trigger to keep track of the audits and operations performed
#Create the Patch audit table
$ cogdb=# CREATE TABLE patch_audit (
	pa_id SERIAL  varchar(2) PRIMARY KEY,
	pa_operation  varchar(1),
        pa_version    varchar(5),
        pa_timestamp  date,
	pa_user       varchar(20),
	pa_object     varchar(40),
	pa_return_cd  varchar(10),
	pa_return_exp varchar(10)
); 
#Create function to populate the Patch Audit File
$ cogdb=# CREATE OR REPLACE FUNCTION process_patch_audit() RETURNS TRIGGER AS $patchaudit$
    BEGIN
	Return_cd = '0000'
	Return_excep = 'NO FAILURE'
        exception when others then
           get stacked diagnostics
           v_state = returned_sqlstate,  
           Return_cd = RETURNED_SQLSTATE,
           Return_excep = MESSAGE_TEXT
        END;
        RETURN;
        IF(TG_TG = 'DELETE') THEN 
           INSERT INTO PATCH_AUDIT SELECT 'D', now(), user, OLD.*;
        ELSE(TG_TG = 'UPDATE') THEN
            INSERT INTO PATCH_AUDIT SELECT 'U', now(), user, NEW.*;
        ELSE(TG_TG = 'DELETE') THEN
            INSERT INTO PATCH_AUDIT SELECT 'I'. now(), user, NEW.*;
        END IF;
        RETURN NULL;
   END; 
$patchaudit$ LANGUAGE plpgsql;
$ cogdb=# CREATE EVENT TRIGGER PATCH_AUDIT ON ddl_command  
   EXECUTE FUNCTION process_patch_audit();

$ cogdb=# ALTER TABLE occspacetype RENAME TO occhecklistspacetype;

$ cogdb=# ALTER TABLE occspaceelement RENAME TO occchecklistspacetypeelement;

$ cogdb=# ALTER TABLE public.occchecklistspacetypeelement
	ADD COLUMN checklistspacetype_typeid INTEGER 
	CONSTRAINT occchecklistspacetypeelement_checklistspacetype_typeid_fk REFERENCES public.occchecklistspacetype (checklistspacetypeid);

$ cogdb=# ALTER TABLE public.occchecklistspacetypeelement DROP COLUMN space_id;

-- replaced with occspacetypeelement
$ cogdb=# DROP TABLE public.occspace CASCADE;

ALTER TABLE occchecklist ADD COLUMN createdts TIMESTAMP WITH TIME ZONE;


-- overly complex logic:drop 
$ cogdb=# ALTER TABLE public.occchecklistspacetype
	DROP COLUMN overridespacetyperequired;

-- overly complex logic:drop 
$ cogdb=# ALTER TABLE public.occchecklistspacetype
	DROP COLUMN overridespacetyperequiredvalue;

-- overly complex logic:drop 
$ cogdb=# ALTER TABLE public.occchecklistspacetype
	DROP COLUMN overridespacetyperequireallspaces;

$ cogdb=# ALTER TABLE public.occchecklistspacetype
	ADD COLUMN notes TEXT;

$ cogdb=# ALTER TABLE public.occinspectedspace
	DROP COLUMN occspace_spaceid;

$ cogdb=# ALTER TABLE public.occinspectedspace
	ADD COLUMN occchecklistspacetype_chklstspctypid INTEGER 
	CONSTRAINT occinspectedspace_chklstspctypid_fk REFERENCES public.occchecklistspacetype (checklistspacetypeid);

$ cogdb=# ALTER TABLE public.occinspectedspaceelement
	ADD COLUMN migratetocecaseonfail BOOLEAN DEFAULT TRUE;

$ cogdb=# ALTER TABLE public.occinspectedspaceelement
	RENAME COLUMN spaceelement_elementid TO occchecklistspacetypeelement_elementid;

$ cogdb=# ALTER TABLE public.occinspection 
	ADD COLUMN followupto_inspectionid INTEGER 
	CONSTRAINT occinspection_followupto_fk REFERENCES occinspection (inspectionid);

$ cogdb=# ALTER TABLE public.occinspection 
	ADD COLUMN deactivatedts TIMESTAMP WITH TIME ZONE;

$ cogdb=# ALTER TABLE public.occinspection 
	ADD COLUMN deactivatedts TIMESTAMP WITH TIME ZONE;

$ cogdb=# ALTER TABLE public.occinspection 
	ADD COLUMN deactivatedby_userid INTEGER 
	CONSTRAINT occinspection_deactivatedby_fk REFERENCES login (userid);

$ cogdb=# ALTER TABLE public.occinspection
	RENAME COLUMN notes TO notespreinspection;

$ cogdb=# ALTER TABLE public.occinspection ADD COLUMN timestart TIMESTAMP WITH TIME ZONE;
$ cogdb=# ALTER TABLE public.occinspection ADD COLUMN timeend TIMESTAMP WITH TIME ZONE;

# replaced with standard deactivatedts and deactivatedby_userid
$ cogdb=# ALTER TABLE public.occinspection DROP COLUMN active;
$ cogdb=# ALTER TABLE public.occinspection ADD COLUMN createdby_userid INTEGER 
	CONSTRAINT occinspection_creationby_fk REFERENCES login (userid);

$ cogdb=# ALTER TABLE public.occinspection ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE;
$ cogdb=# ALTER TABLE public.occinspection ADD COLUMN lastupdatedby_userid INTEGER 
	CONSTRAINT occinspection_lastupdatedby_fk REFERENCES login (userid);



$ cogdb=# CREATE TABLE public.occinspectionpropertystatus
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


$ cogdb=# CREATE SEQUENCE IF NOT EXISTS occinspectioncause_causeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

$ cogdb=# CREATE TABLE public.occinspectioncause
(
	causeid 		INTEGER PRIMARY KEY DEFAULT nextval('occinspectioncause_causeid_seq'),
	title 			TEXT NOT NULL,
	description 	TEXT,
	notes 			TEXT,
	active 			BOOLEAN DEFAULT true
);

$ cogdb=# ALTER TABLE public.occinspection 
	ADD COLUMN cause_causeid INTEGER 
	CONSTRAINT occinspection_cause_fk REFERENCES occinspectioncause (causeid);


$ cogdb=# CREATE SEQUENCE IF NOT EXISTS occinspection_determination_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

$ cogdb=# CREATE TABLE public.occinspectiondetermination
(
	determinationid 		INTEGER PRIMARY KEY DEFAULT nextval('occinspection_determination_seq'),
	title 			TEXT NOT NULL,
	description 	TEXT,
	notes 			TEXT,
	eventcat_catid 	INTEGER CONSTRAINT occinspectiondetermination_eventcat_fk REFERENCES eventcategory (categoryid),
	active 			BOOLEAN DEFAULT true
);

$ cogdb=# ALTER TABLE public.occinspection 
	ADD COLUMN determination_detid INTEGER 
	CONSTRAINT occinspection_determination_fk REFERENCES occinspectiondetermination (determinationid);

$ cogdb=# ALTER TABLE public.occinspection 
	ADD COLUMN determinationby_userid INTEGER
	CONSTRAINT occinpection_determinationby_fk REFERENCES login (userid);

$ cogdb=# ALTER TABLE public.occinspection
	ADD COLUMN determinationts TIMESTAMP WITH TIME ZONE;




$ cogdb=# ALTER TABLE public.occinspection DROP COLUMN passedinspection_userid;
$ cogdb=# ALTER TABLE public.occinspection DROP COLUMN passedinspectionts;

$ cogdb=# ALTER TABLE public.occinspection ADD COLUMN remarks TEXT;


$ cogdb=# ALTER TABLE public.occinspection ADD COLUMN generalcomments TEXT;


$ cogdb=# CREATE SEQUENCE IF NOT EXISTS occchecklist_photorequirement_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

$ cogdb=# CREATE TABLE public.occphotorequirement
(
	requirementid 		INTEGER PRIMARY KEY DEFAULT nextval('occchecklist_photorequirement_seq'),
	title 			TEXT NOT NULL,
	description 	TEXT,
	notes 			TEXT,
	required 		BOOLEAN,
	active 			BOOLEAN DEFAULT true
);

$ cogdb=# CREATE TABLE public.occchecklistphotorequirement
(
	occchecklist_checklistid 	INTEGER NOT NULL,
	occphotorequirement_reqid 	INTEGER NOT NULL,
	CONSTRAINT occchecklistphotorequirement_pk_comp PRIMARY KEY (occchecklist_checklistid, occphotorequirement_reqid),
	CONSTRAINT occchecklistphotorequirement_checklist_fk FOREIGN KEY (occphotorequirement_reqid)
		REFERENCES public.occchecklist (checklistid),
	CONSTRAINT occchecklistphotorequirement_requirement_fk FOREIGN KEY (occchecklist_checklistid)
		REFERENCES public.occphotorequirement (requirementid)

);

$ cogdb=# CREATE TABLE public.occinspectionphotodoc
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

$ cogdb=# DROP TABLE public.occinspectedspaceelementpdfdoc;

$ cogdb=# ALTER TABLE public.occinspectedspaceelementphotodoc ADD COLUMN phototype occinspectionphototype;

-- RUN LOCALLY UP TO HERE

--IF datepublished IS NULL the patch is still open and receiving changes
$ cogdb=# INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (351, 'database/patches/dbpatch_beta35.1.sql', '07-19-2021', 'ecd', 'Occ refactor: collapse occspace and occspacetype, revise occinspection');
$ cogdb=# CREATE TABLE IF NOT EXISTS patch_audit_archive (pa_arc_id SERIAL  varchar(2) PRIMARY KEY,
	pa_arc_operation  varchar(1),
        pa_arc_version    varchar(5),
        pa_arc_timestamp  date,
	pa_arc_user       varchar(20),
	pa_arc_object     varchar(40),
	pa_arc_return_cd  varchar(10),
	pa_arc_return_exp varchar(10)
); 
$ cogdb=# select * into patch_audit_archive from patch_audit;
sudo docker-compose down
#WORKDIR /code
#ENV FLASK_APP=app.py
#ENV FLASK_RUN_HOST=0.0.0.0
#RUN apk add --no-cache gcc musl-dev linux-headers
#COPY requirements.txt requirements.txt
#RUN pip install -r requirements.txt
#EXPOSE 5000
#COPY . .
#CMD ["flask", "run"]
