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

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (15, 'database/patches/dbpatch_beta15.sql', '', 'ecd', 'occupancy adjustments');

