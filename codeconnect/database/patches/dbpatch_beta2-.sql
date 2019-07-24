
ALTER TABLE PERSON DROP COLUMN IF EXISTS bobsource_sourceid;

INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (20, 'database/patches/dbpatch_beta20.sql', '07-23-2019', 'ecd', 'occ beta final over');

