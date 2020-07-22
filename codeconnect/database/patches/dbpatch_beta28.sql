-- ****************************************************************************
-- PATCH 28
-- "Mid-july" launch changes
-- Person revisions: adding type of link to propertyperson
-- 
-- 
-- ****************************************************************************

INSERT INTO public.icon(
            iconid, name, styleclass, fontawesome, materialicons)
    VALUES (100, 'note', 'cnf-note', 'fa fa-sticky-note-o', 'note');


INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors, 
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal, 
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins, 
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (221, 'PropertyInfoCase'::eventtype, 'Property note', 'General update on property; not for case or period-specific deets', FALSE, 
            TRUE, 100, 0, 0,
            NULL, NULL,1, 
            TRUE, 3, 3, 5);

ALTER TABLE cecase DROP COLUMN casephase;

ALTER TABLE public.cecase ADD COLUMN lastupdatedby_userid INTEGER CONSTRAINT cecase_lastupdatedby_userid_fk REFERENCES login (userid);
ALTER TABLE public.cecase ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE;





--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (28, 'database/patches/dbpatch_beta28.sql', NULL, 'ecd', 'mid-july-launch');