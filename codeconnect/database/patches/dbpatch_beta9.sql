
ALTER TABLE citationstatus ADD COLUMN editsallowed BOOLEAN DEFAULT TRUE;
ALTER TABLE municipality ADD COLUMN defaultcourtentity INTEGER CONSTRAINT muni_defcourtentity_fk REFERENCES courtentity (entityid);


BEGIN;
INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (9, 'database/patches/dbpatch_beta9.sql', '03-29-2019', 'ecd', 'citation updates, among others');

COMMIT;
