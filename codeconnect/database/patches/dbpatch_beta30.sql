-- ****************************************************************************
-- PATCH 29
-- "Early August 2020" launch changes

-- ****************************************************************************



-- ****************************************************************************
-- THIS ALTER TYPE COMMAND MUST BE EXECUTED ATOMICALLY

-- ALTER TYPE eventtype ADD VALUE 'Court' AFTER 'Workflow';

-- ****************************************************************************



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (30, 'database/patches/dbpatch_beta30.sql', NULL 'ecd', 'mid august 2020 changes');
