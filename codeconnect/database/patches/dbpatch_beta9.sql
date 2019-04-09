
ALTER TABLE citationstatus ADD COLUMN editsallowed BOOLEAN DEFAULT TRUE;
ALTER TABLE municipality ADD COLUMN defaultcourtentity INTEGER CONSTRAINT muni_defcourtentity_fk REFERENCES courtentity (entityid);
ALTER TABLE loginmuni ADD COLUMN default BOOLEAN DEFAULT FALSE;
ALTER TABLE ceevent ADD COLUMN directrequesttodefaultmuniceo boolean DEFAULT false;





BEGIN;
INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (9, 'database/patches/dbpatch_beta9.sql', '03-29-2019', 'ecd', 'citation updates, among others');

COMMIT;
