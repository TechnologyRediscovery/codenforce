
BEGIN;

-- Property id sequence

CREATE SEQUENCE IF NOT EXISTS propertyid_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    NO MAXVALUE
    CACHE 1;

COMMIT;

BEGIN;

ALTER TABLE property ALTER COLUMN propertyid SET DEFAULT nextval('propertyid_seq');

COMMIT;

-- add updated persons in all the users



BEGIN;
-- update login entries with default person

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (7, '/database/patches/dbpatch_beta7.sql', '03-07-2019', 'ecd', 'CEEventsUpdate');

COMMIT;