-- ****************************************************************************
-- PATCH 29
-- "Early August 2020" launch changes

-- ****************************************************************************

ALTER TABLE login ADD COLUMN homemuni INTEGER CONSTRAINT login_homemuni_fk REFERENCES municipality(municode);

--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (29, 'database/patches/dbpatch_beta29.sql', NULL, 'ecd', 'early august changes');
