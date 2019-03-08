
BEGIN;

ALTER TABLE ceevent ADD COLUMN assignedto_login_userid INTEGER CONSTRAINT ceevent_assignedto_fk REFERENCES login (userid);


INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (6, '/database/patches/dbpatch_beta6.sql', '03-07-2019', 'ecd', 'CEEventsUpdate');

-- correction to previous patch record
UPDATE public.dbpatch SET patchfilename='/database/patches/dbpatch_beta5.sql' WHERE patchnum=5;


COMMIT;