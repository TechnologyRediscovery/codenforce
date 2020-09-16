-- ****************************************************************************
-- PATCH 30
-- "MID August 2020" launch changes

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



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (30, 'database/patches/dbpatch_beta30.sql', '08-25-2020' 'ecd', 'mid august 2020 changes');
