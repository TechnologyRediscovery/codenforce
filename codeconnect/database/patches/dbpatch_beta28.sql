-- ****************************************************************************
-- PATCH 27
-- Mid-july launch changes
-- Person revisions: adding type of link to propertyperson
-- 
-- 
-- ****************************************************************************





--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (28, 'database/patches/dbpatch_beta28.sql', NULL, 'ecd', 'mid-july-launch');