
-- note yet released: created while working with Noah on property stuff
BEGIN;

ALTER TABLE property ADD COLUMN multiunit boolean DEFAULT FALSE;


INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (5, '/database/patches/dbpatch_beta4.sql', '02-28-2019', 'ecd', 'Changes to property');


COMMIT;