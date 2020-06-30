-- ****************************************************************************
-- PATCH 26
-- 
-- Mostly event subsystem overhaul changes;
-- DON'T FORGET TO MANUALLY RUN THE ALTER TYPE .... STATEMENT IN THE COMMENT
-- BLOCK DIRECTLY BELOW THIS HEADER MESSAGE
-- 
-- 
-- ****************************************************************************


-- **********************************************************************
-- **********************************************************************
-- **********************************************************************
-- **********************************************************************
--
--
--
-- NOTE NOTE NOTE: THIS STATEMENT MUST BE RUN INDEPENDENTLY AT THE START OF THE SCRIPT
-- ALTER TYPE eventtype ADD VALUE IF NOT EXISTS 'Workflow'; 
--
--
--
-- **********************************************************************
-- **********************************************************************
-- **********************************************************************
-- **********************************************************************
-- **********************************************************************
-- **********************************************************************
-- **********************************************************************

ALTER TABLE occperiod ADD COLUMN active boolean DEFAULT true;
ALTER TABLE ceactionrequest ADD COLUMN active boolean DEFAULT true;
ALTER TABLE cecase ADD COLUMN active boolean DEFAULT true;


-- From NADGIT
ALTER TABLE public.moneycodesetelementfee ADD COLUMN active boolean;
ALTER TABLE public.moneycodesetelementfee ADD COLUMN autoassign boolean;

-- ****************************************************************************
-- Run on production server up to this point 
--							  0				 		
--                           000
--                  	  000000000
--						0000000000000
--						     000 
--							 000 	
-- ****************************************************************************

INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, userdeployable, 
            munideployable, publicdeployable, notifycasemonitors, hidable, 
            icon_iconid, relativeorderwithintype, relativeorderglobal, hosteventdescriptionsuggtext, 
            directive_directiveid, defaultdurationmins)
    VALUES (220, 'PropertyInfoCase'::eventtype, 'Property info related', 'Related to scraping of data from county', FALSE, 
            FALSE, FALSE, TRUE, TRUE, 
            NULL, 0, 0, FALSE, 
            NULL, 0);

-- WATCH OUT FOR ONES FROM SNAPPERS

ALTER TABLE eventcategory ADD COLUMN active BOOLEAN DEFAULT true;
UPDATE eventcategory SET active = true;

ALTER TABLE public.eventcategory ADD COLUMN userrankminimumtoenact INTEGER;
ALTER TABLE public.eventcategory ALTER COLUMN userrankminimumtoenact SET DEFAULT 3;
ALTER TABLE public.eventcategory ADD COLUMN userrankminimumtoview INTEGER;
ALTER TABLE public.eventcategory ALTER COLUMN userrankminimumtoview SET DEFAULT 3;


ALTER TABLE public.eventcategory DROP COLUMN userdeployable;
ALTER TABLE public.eventcategory DROP COLUMN munideployable;
ALTER TABLE public.eventcategory DROP COLUMN publicdeployable;

ALTER TABLE public.event DROP COLUMN disclosetomunicipality;
ALTER TABLE public.event DROP COLUMN disclosetopublic;
ALTER TABLE public.event RENAME COLUMN eventtimestamp TO creationts;
ALTER TABLE public.event RENAME COLUMN owner_userid TO creator_userid;

ALTER TABLE public.event ADD COLUMN lastupdatedby_userid INTEGER CONSTRAINT event_createdby_userid_fk REFERENCES login (userid);
ALTER TABLE public.event ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE;

ALTER TABLE public.eventcategory ADD COLUMN userrankminimumtoupdate INTEGER;
ALTER TABLE public.eventcategory ALTER COLUMN userrankminimumtoupdate SET DEFAULT 3;

ALTER TABLE public.eventcategory RENAME COLUMN notifycasemonitors TO notifymonitors;
ALTER TABLE public.event RENAME COLUMN activeevent TO active;

-- Event rules and such overhaul
DROP TABLE public.eventruleimpl CASCADE;


ALTER TABLE public.eventrule ADD COLUMN userrankmintoconfigure INTEGER DEFAULT 3;
ALTER TABLE public.eventrule ADD COLUMN userrankmintoimplement INTEGER DEFAULT 3;
ALTER TABLE public.eventrule ADD COLUMN userrankmintowaive INTEGER DEFAULT 3;
ALTER TABLE public.eventrule ADD COLUMN userrankmintooverride INTEGER DEFAULT 3;
ALTER TABLE public.eventrule ADD COLUMN userrankmintodeactivate INTEGER DEFAULT 3;


CREATE SEQUENCE IF NOT EXISTS eventruleimpl_impid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.eventruleimpl
(
	erimplid			INTEGER NOT NULL DEFAULT nextval('eventruleimpl_impid_seq') CONSTRAINT erimplid_pk PRIMARY KEY,
	eventrule_ruleid 	INTEGER NOT NULL,
	cecase_caseid 		INTEGER CONSTRAINT erimpl_caseid_fk REFERENCES cecase (caseid),
	occperiod_periodid  INTEGER CONSTRAINT erimpl_occperiodid_fk REFERENCES occperiod (periodid),
	implts 				TIMESTAMP WITH TIME ZONE,
	implby_userid		INTEGER CONSTRAINT erimpl_implby_userid_fk REFERENCES login (userid),
	lastevaluatedts 	TIMESTAMP WITH TIME ZONE,
	passedrulets 		TIMESTAMP WITH TIME ZONE,
	triggeredevent_eventid INTEGER CONSTRAINT erimpl_triggeredevent_eventid_FK REFERENCES event (eventid),
	waivedts			TIMESTAMP WITH TIME ZONE,
	waivedby_userid 	INTEGER CONSTRAINT erimpl_waivedby_userid_fk REFERENCES login (userid),
	passoverridets			TIMESTAMP WITH TIME ZONE,
	passoverrideby_userid 	INTEGER CONSTRAINT erimpl_passoverrideby_userid_fk REFERENCES login (userid),
	deacts			TIMESTAMP WITH TIME ZONE,
	deacby_userid 	INTEGER CONSTRAINT erimpl_deacby_userid_fk REFERENCES login (userid),
	notes 			TEXT
);

ALTER TABLE public.codeviolation DROP COLUMN compliancetfevent;

ALTER TABLE public.codeviolation ADD COLUMN compliancetfexpiry_proposalid INTEGER 
	CONSTRAINT codeviolation_tfexpiry_proposalid_fk 
	REFERENCES choiceproposal (proposalid);



INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors, 
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal, 
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins, 
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (222, 'Workflow'::eventtype, 'Workflow choice made', 'Documents a user selecting or the auto-selection of a choice in a proposal', FALSE, 
            TRUE, NULL, 0, 0, 
            NULL, NULL, 1, 
            TRUE, 7, 3, 7);





--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (26, 'database/patches/dbpatch_beta26.sql', '29-JUN-2020', 'ecd', 'various changes');