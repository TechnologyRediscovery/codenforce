-- ****************************************************************************
-- PATCH 26
-- 
-- 
-- 
-- 
-- ****************************************************************************

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
ALTER TABLE eventcategory SET active = TRUE;

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

ALTER TABLE public.event ADD COLUMN target_userid INTEGER CONSTRAINT event_target_userid_fk REFERENCES login (userid);

ALTER TABLE public.eventcategory ADD COLUMN userrankminimumtoupdate INTEGER;
ALTER TABLE public.eventcategory ALTER COLUMN userrankminimumtoupdate SET DEFAULT 3;

ALTER TABLE public.eventcategory RENAME COLUMN notifycasemonitors TO notifymonitors;
ALTER TABLE public.event RENAME COLUMN activeevent TO active;
--- RUN LOCALLY UP TO HERE

ALTER TABLE public.eventperson ADD COLUMN roledescr TEXT;



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (26, 'database/patches/dbpatch_beta26.sql', NULL, 'ecd', 'various changes');