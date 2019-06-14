
-- have not wired this new column up yet!
ALTER TABLE ceactionrequest ADD COLUMN usersubmitter_userid INTEGER;
ALTER TABLE ceactionrequest ADD CONSTRAINT ceactionreq_usersub_fk FOREIGN KEY (usersubmitter_userid) REFERENCES login (userid);

-- add stipulated compliance that freeze in time when added to a notice of violation

ALTER TYPE ceeventtype ADD VALUE IF NOT EXISTS 'Citation' AFTER 'Compliance';

CREATE SEQUENCE IF NOT EXISTS occperiodid_seq
	START WITH 1000
	INCREMENT BY 1 
	MINVALUE 1000
	NO MAXVALUE 
	CACHE 1;

CREATE TABLE public.occperiod
(

	periodid 						INTEGER DEFAULT nextval('occperiodid_seq') NOT NULL,
	propertyunit_unitid
	startdate						TIMESTAMP WITH TIME ZONE,
	startdatecertifiedby_userid
	startdatecertifiedts
	enddate
	enddatecertifiedby_userid
	enddatecterifiedts
	manager_userid
	

) ;


-- Has not been run on remote server

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (14, 'database/patches/dbpatch_beta13.sql', '', 'ecd', 'final clean ups');

