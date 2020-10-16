-- ****************************************************************************
-- PATCH 31
-- "MID SEP 2020" launch changes

-- ****************************************************************************



-- ****************************************************************************
-- THIS ALTER TYPE COMMAND MUST BE EXECUTED ATOMICALLY
-- ALTER TYPE eventtype ADD VALUE 'Court' AFTER 'Workflow';

-- ****************************************************************************

--run on server UP TO HERE

INSERT INTO public.intensityclass(
            classid, title, muni_municode, numericrating, schemaname, active, 
            icon_iconid)
    VALUES (DEFAULT, 'Minor; isolated; primarily cosmetic', 999, 1, 'violationseverity', TRUE, 
            10);


INSERT INTO public.intensityclass(
            classid, title, muni_municode, numericrating, schemaname, active, 
            icon_iconid)
    VALUES (DEFAULT, 'Moderate; symptomatic of larger issues', 999, 2, 'violationseverity', TRUE, 
            10);

INSERT INTO public.intensityclass(
            classid, title, muni_municode, numericrating, schemaname, active, 
            icon_iconid)
    VALUES (DEFAULT, 'Severe; structural or functional deficit', 999, 3, 'violationseverity', TRUE, 
            10);

INSERT INTO public.intensityclass(
            classid, title, muni_municode, numericrating, schemaname, active, 
            icon_iconid)
    VALUES (DEFAULT, 'Extreme; presents immediate human safety hazard', 999, 4, 'violationseverity', TRUE, 
            10);

ALTER TABLE codeviolation ADD COLUMN compliancenote TEXT;

ALTER TABLE noticeofviolation ADD COLUMN active BOOLEAN DEFAULT TRUE;

-- FROM SNAPPER's file called ParcelNotInWprdcData.sql

INSERT INTO public.eventcategory(
     categoryid, categorytype,
     title,
     description,
     notifymonitors, hidable, icon_iconid, relativeorderwithintype, relativeorderglobal,
     hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins,
     active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
 VALUES
    (308, 'PropertyInfoCase'::eventtype,
        'DifferentMunicode',
        'Documents a change in a Parcel''s municode (compared to the Allegheny County Real Estate Portal)',
        TRUE, TRUE, NULL, 0, 0,
        NULL, NULL, 1,
        TRUE, 7, 3, 7),
    (309, 'PropertyInfoCase'::eventtype,
        'NotInRealEstatePortal',
        'Documents when a parcel is in the CodeNForce database but the Allegheny County Real Estate Portal''s corresponding page is blank',
        TRUE, TRUE, NULL, 0, 0,
        NULL, NULL, 1,
        TRUE, 7, 3, 7);


--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (31, 'database/patches/dbpatch_beta31.sql', '09-24-2020' 'ecd', 'mid sep 2020 changes');
