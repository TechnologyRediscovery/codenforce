-- SEALED AS OF 5-JUL-2019 BY SYLVIA

-- RUN THIS ALONE
ALTER TYPE eventtype ADD VALUE IF NOT EXISTS 'Occupancy' AFTER 'Citation';

-- THEN CONTINUE
ALTER TABLE PERSON DROP COLUMN IF EXISTS bobsource_sourceid;

ALTER TABLE login ADD COLUMN userrole TEXT;
update login set userrole='Developer';

-- us for testing only!!!


-- update munilogin set codeofficerstopdate='2020-01-01';
-- update munilogin set staffstopdate='2020-01-01';
-- update munilogin set sysadminstopdate='2020-01-01';
-- update munilogin set supportstopdate='2020-01-01';
-- update munilogin set codeofficerstartdate='2000-01-01';
-- update munilogin set staffstartdate='2000-01-01';
-- update munilogin set sysadminstartdate='2000-01-01';
-- update munilogin set supportstartdate='2000-01-01';


ALTER TABLE public.municipality RENAME enablecodeenformcent  TO enablecodeenforcement;

ALTER TABLE courtentity ADD COLUMN judgename text;

ALTER TABLE choiceproposal DROP COLUMN hidden;

ALTER TABLE munilogin ADD COLUMN defaultcecase_caseid INTEGER CONSTRAINT munilogin_defcaseid_fk REFERENCES cecase (caseid);

ALTER TABLE occperiod DROP COLUMN overrideperiodtypeconfig;
ALTER TABLE occperiod ADD COLUMN overrideperiodtypeconfig BOOLEAN DEFAULT FALSE;

ALTER TABLE public.occperiod DROP CONSTRAINT occperiod_periodtype_typeid_fk;

ALTER TABLE public.occperiod ADD CONSTRAINT occperiod_periodtypid_fk FOREIGN KEY (type_typeid) REFERENCES occperiodtype (typeid);

ALTER TABLE public.occinspection
   ALTER COLUMN passedinspection_userid DROP NOT NULL;

ALTER TABLE public.occinspection DROP CONSTRAINT occinspection_maxocc_userid_fk;

update munilogin set codeofficerstartdate='2000-01-01';
update munilogin set staffstartdate='2000-01-01';
update munilogin set sysadminstartdate='2000-01-01';
update munilogin set supportstartdate='2000-01-01';

INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (20, 'database/patches/dbpatch_beta20.sql', '07-23-2019', 'ecd', 'occ beta final over');
