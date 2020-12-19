-- ****************************************************************************
-- PATCH 33
-- "LATE NOV/DEC 2020" launch changes

-- *************

ALTER TABLE public.codeviolation
    ADD COLUMN nullifiedts TIMESTAMP WITH TIME ZONE;

ALTER TABLE public.codeviolation
    ADD COLUMN nullifiedby INTEGER CONSTRAINT codeviolation_nullifiedby_fk REFERENCES login(userid);




ALTER TABLE public.codelement
    ADD COLUMN ordsubsubsectitle TEXT;

ALTER TABLE public.codelement
    ADD COLUMN ordsubsubsecnum TEXT;

ALTER TABLE public.codeelement DROP COLUMN lastupdated_userid;
ALTER TABLE public.codeelement DROP COLUMN isactive;

ALTER TABLE public.codeelement
    ADD COLUMN createdby_userid        INTEGER CONSTRAINT codeelement_createdby_userid_fk REFERENCES login (userid),     
    ADD COLUMN lastupdatedby_userid    INTEGER CONSTRAINT codeelement_lastupdatdby_userid_fk REFERENCES login (userid),
    ADD COLUMN deactivatedts           TIMESTAMP WITH TIME ZONE,
    ADD COLUMN deactivatedby_userid    INTEGER CONSTRAINT codeelement_deactivatedby_userid_fk REFERENCES login (userid);
 

 ALTER TABLE public.codeelement
    ADD COLUMN createdts TIMESTAMP WITH TIME ZONE;
 
 ALTER TABLE public.codeelement DROP COLUMN creator_userid;


 ALTER TABLE public.codeelement DROP COLUMN datecreated;
 ALTER TABLE public.codeelement DROP COLUMN datecreated;



ALTER TABLE public.codesetelement
        ADD COLUMN createdts               TIMESTAMP WITH TIME ZONE,
        ADD COLUMN createdby_userid        INTEGER CONSTRAINT codesetelement_createdby_userid_fk REFERENCES login (userid),     
        ADD COLUMN lastupdatedts           TIMESTAMP WITH TIME ZONE,
        ADD COLUMN lastupdatedby_userid    INTEGER CONSTRAINT codesetelement_lastupdatdby_userid_fk REFERENCES login (userid),
        ADD COLUMN deactivatedts           TIMESTAMP WITH TIME ZONE,
        ADD COLUMN deactivatedby_userid    INTEGER CONSTRAINT codesetelement_deactivatedby_userid_fk REFERENCES login (userid);

ALTER TABLE codeset ADD COLUMN active BOOLEAN DEFAULT TRUE;

-- RUN ON REMOTE TO HERE
-- RUN LOCALLY TO HERE
ALTER TABLE textblock 
    ADD COLUMN injectabletemplate BOOLEAN DEFAULT FALSE;

ALTER TABLE noticeofviolation 
    ADD COLUMN injectviolations BOOLEAN DEFAULT FALSE;

ALTER TABLE noticeofviolation 
    DROP COLUMN injectviolations;

ALTER TABLE noticeofviolation
    ADD COLUMN followupevent_eventid INTEGER CONSTRAINT nov_followup_eventid_fk REFERENCES event (eventid);

ALTER TABLE muniprofile 
    ADD COLUMN novfollowupdefaultdays INTEGER DEFAULT 20;

INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors, 
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal, 
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins, 
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (223, 'Timeline'::eventtype, 'Followup on NOV' , 'NOV compliance window has expired', FALSE, 
            TRUE, 10, 1, 1, 
            'Follow up on notice of violation', NULL, 30, 
            TRUE, 4, 4, 4);


INSERT INTO public.textblockcategory(
            categoryid, categorytitle, icon_iconid, muni_municode)
    VALUES (101, "Injectable Template", 10, 999);





--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (33, 'database/patches/dbpatch_beta33.sql','12-14-2020', 'ecd', 'NOV/DEC 2020 changes');
