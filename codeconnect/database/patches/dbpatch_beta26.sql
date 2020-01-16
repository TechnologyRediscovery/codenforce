-- ****************************************************************************
-- PATCH 26
-- 
-- 
-- 
-- 
-- ****************************************************************************



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (26, 'database/patches/dbpatch_beta26.sql', NULL, 'ecd', 'various changes');