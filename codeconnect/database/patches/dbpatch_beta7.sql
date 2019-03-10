
BEGIN;

-- Property id sequence

-- update login entries with default person

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (7, '/database/patches/dbpatch_beta7.sql', '03-07-2019', 'ecd', 'CEEventsUpdate');

COMMIT;