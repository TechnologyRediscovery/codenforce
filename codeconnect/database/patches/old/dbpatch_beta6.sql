
BEGIN;

ALTER TABLE ceevent ADD COLUMN assignedto_login_userid INTEGER CONSTRAINT ceevent_assignedto_fk REFERENCES login (userid);
-- ALTER TABLE ceevent RENAME COLUMN viewrequestedby_personid TO viewrequestedby_userid;

ALTER TABLE login DROP COLUMN fname;
ALTER TABLE login DROP COLUMN lname;
ALTER TABLE login DROP COLUMN worktitle;
ALTER TABLE login DROP COLUMN phonecell;
ALTER TABLE login DROP COLUMN phonehome; 
ALTER TABLE login DROP COLUMN phonework; 
ALTER TABLE login DROP COLUMN email;
ALTER TABLE login DROP COLUMN address_street;
ALTER TABLE login DROP COLUMN address_city; 
ALTER TABLE login DROP COLUMN address_zip;
ALTER TABLE login DROP COLUMN address_state;

ALTER SEQUENCE IF EXISTS login_userid_seq
	MINVALUE 1000
	START WITH 1000
	RESTART;

ALTER TABLE municipality ADD COLUMN defaultcodeofficeruser INTEGER CONSTRAINT municipality_defaultceo_fk REFERENCES login (userid);

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (6, '/database/patches/dbpatch_beta6.sql', '03-07-2019', 'ecd', 'CEEventsUpdate');

-- correction to previous patch record
UPDATE public.dbpatch SET patchfilename='/database/patches/dbpatch_beta5.sql' WHERE patchnum=5;


COMMIT;