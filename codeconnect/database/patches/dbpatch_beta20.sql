
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



INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (20, 'database/patches/dbpatch_beta20.sql', '07-23-2019', 'ecd', 'occ beta final over');


update munilogin set codeofficerstartdate='2000-01-01';
update munilogin set staffstartdate='2000-01-01';
update munilogin set sysadminstartdate='2000-01-01';
update munilogin set supportstartdate='2000-01-01';
