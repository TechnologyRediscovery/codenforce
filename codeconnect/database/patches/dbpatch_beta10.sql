


ALTER TABLE citationstatus RENAME COLUMN editsallowed TO editsforbidden;


CREATE SEQUENCE IF NOT EXISTS cecasephasechangerule_seq
	    START WITH 10
	    INCREMENT BY 1
	    MINVALUE 10
	    NO MAXVALUE
	    CACHE 1;

CREATE TABLE cecasephasechangerule
(
	ruleid 						INTEGER DEFAULT nextval('cecasephasechangerule_seq') 
									CONSTRAINT phasechangerule_pk PRIMARY KEY,
	title 						TEXT,
	targetcasephase				casephase,
	requiredcurrentcasephase	casephase,
	forbiddencurrentcasephase	casephase,
	requiredextanteventtype		ceeventtype,
	forbiddenextanteventtype	ceeventtype,
	requiredextanteventcat		INTEGER CONSTRAINT phasechangerule_reqevcat_fk REFERENCES ceeventcategory (categoryid),
	forbiddenextanteventcat		INTEGER CONSTRAINT phasechangerule_forbiddenevcat_fk REFERENCES ceeventcategory (categoryid)
);

ALTER TABLE cecasephasechangerule ADD COLUMN triggeredeventcat INTEGER 
	CONSTRAINT phasechangerule_triggeredevcat_fk REFERENCES ceeventcategory (categoryid);

ALTER TABLE cecasephasechangerule ADD COLUMN active BOOLEAN DEFAULT TRUE;
ALTER TABLE cecasephasechangerule ADD COLUMN mandatory BOOLEAN DEFAULT FALSE;
ALTER TABLE cecasephasechangerule ADD COLUMN treatreqphaseasthreshold BOOLEAN DEFAULT FALSE;
ALTER TABLE cecasephasechangerule ADD COLUMN treatforbidphaseasthreshold BOOLEAN DEFAULT FALSE;
ALTER TABLE cecasephasechangerule ADD COLUMN rejectrulehostifrulefails BOOLEAN DEFAULT TRUE;
ALTER TABLE cecasephasechangerule ADD COLUMN description text;

ALTER TABLE cecasephasechangerule ADD COLUMN triggeredeventcatreqcat INTEGER 
	CONSTRAINT phasechangerule_triggeredevcatreqcat_fk REFERENCES ceeventcategory (categoryid);


ALTER TABLE ceeventcategory ADD COLUMN phasechangerule_ruleid INTEGER 
	CONSTRAINT ceeventcat_phasechange_fk REFERENCES cecasephasechangerule (ruleid);

ALTER TABLE citationstatus ADD COLUMN phasechangerule_ruleid INTEGER 
	CONSTRAINT citationstatus_phasechangerule_fk REFERENCES cecasephasechangerule (ruleid);


INSERT INTO public.cecasephasechangerule(
            ruleid, title, targetcasephase, requiredcurrentcasephase, forbiddencurrentcasephase, 
            requiredextanteventtype, forbiddenextanteventtype, requiredextanteventcat, 
            forbiddenextanteventcat, triggeredeventcat, active, mandatory,
            treatreqphaseasthreshold, treatforbidphaseasthreshold, rejectrulehostifrulefails, 
            description, triggeredeventcatreqcat)
    VALUES (1000, 'condemation', 'Closed'::casephase, NULL , 'Closed'::casephase, 
            NULL, NULL, 122, 
            128, 128, TRUE, FALSE
            FALSE, FALSE, TRUE,
            'Checks that a case is not closed and case has not previously condemned this property', NULL);

INSERT INTO public.cecasephasechangerule(
            ruleid, title, targetcasephase, requiredcurrentcasephase, forbiddencurrentcasephase, 
            requiredextanteventtype, forbiddenextanteventtype, requiredextanteventcat, 
            forbiddenextanteventcat, triggeredeventcat, active, mandatory, 
            treatreqphaseasthreshold, treatforbidphaseasthreshold, rejectrulehostifrulefails,
            description, triggeredeventcatreqcat)
    VALUES (1001, 'issue citation', 'AwaitingHearingDate'::casephase, 'InitialComplianceTimeframe'::casephase, 'Closed'::casephase, 
            NULL, NULL, NULL, 
            NULL, 124, TRUE, TRUE,  -- creates an "Issue citation" event
            TRUE, FALSE, TRUE, 
            'rides with the citation issuance objects to manage citation-related updates of casephase; case must be in a compliance
            window (and not closed); writes in an officer action: issue citation event; requests the scheduling of a hearing event', 
            136); -- requests a case timeline event when hearing gets scheduled



INSERT INTO public.cecasephasechangerule(
            ruleid, title, targetcasephase, requiredcurrentcasephase, forbiddencurrentcasephase, 
            requiredextanteventtype, forbiddenextanteventtype, requiredextanteventcat, 
            forbiddenextanteventcat, triggeredeventcat, active, mandatory, 
            treatreqphaseasthreshold, treatforbidphaseasthreshold, rejectrulehostifrulefails, 
            description, triggeredeventcatreqcat)
    VALUES (1002, 'schedule a hearing', 'HearingPreparation'::casephase, 'AwaitingHearingDate'::casephase, 'Closed'::casephase, 
            NULL, NULL, 124, -- requires that a citation exist on the case
            NULL, 135, TRUE, FALSE, -- triggers a property inspection
            TRUE, FALSE, FALSE, 
            'to schedule a hearing, the case must have at least a single citation and not be closed; triggers ',
            NULL); -- prop inspection isn't chained to anything







INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (10, 'database/patches/dbpatch_beta10.sql', '04-16-2019', 'ecd', 'case reports and citation lifecycle');

