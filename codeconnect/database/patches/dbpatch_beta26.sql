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

-- Run on production server up to this point 

INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, userdeployable, 
            munideployable, publicdeployable, notifycasemonitors, hidable, 
            icon_iconid, relativeorderwithintype, relativeorderglobal, hosteventdescriptionsuggtext, 
            directive_directiveid, defaultdurationmins)
    VALUES (220, 'PropertyInfoCase'::eventtype, 'Property info related', 'Related to scraping of data from county', FALSE, 
            FALSE, FALSE, TRUE, TRUE, 
            NULL, 0, 0, FALSE, 
            NULL, 0);


--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (26, 'database/patches/dbpatch_beta26.sql', NULL, 'ecd', 'various changes');