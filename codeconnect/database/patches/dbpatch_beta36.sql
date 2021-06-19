-- ****************************************************************************
-- PATCH 36
-- "CIATATION FACELIFT"

-- *************



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (36, 'database/patches/dbpatch_beta36.sql',NULL, 'ecd', 'Citatation facelift');


