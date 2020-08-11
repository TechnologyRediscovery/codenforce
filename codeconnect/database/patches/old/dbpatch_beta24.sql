-- ****************************************************************************
-- First patch post re-braining
-- 
-- closed!!!
-- 
-- 
-- 
-- 
-- ****************************************************************************



ALTER TABLE loginobjecthistory ADD COLUMN occperiod_periodid INTEGER CONSTRAINT loginobjecthistory_occperiod_id_fk REFERENCES occperiod (periodid);
ALTER TABLE public.loginobjecthistory DROP COLUMN occinspec_inspecid;

ALTER TABLE ceevent DROP COLUMN hidden;
ALTER TABLE occevent DROP COLUMN hidden;

-- Bring back occ period IDs on events and use the same table.
ALTER TABLE ceevent ADD COLUMN occperiod_periodid INTEGER CONSTRAINT ceevent_occperiodid_fk REFERENCES occperiod (periodid);

ALTER TABLE ceevent RENAME TO event;

-- not yet run locally or remote
ALTER TABLE cecaseeventrule RENAME TO eventruleimpl;
ALTER TABLE ceeventperson RENAME TO eventperson;

DROP TABLE occperiodeventrule CASCADE;
DROP TABLE occevent CASCADE;
DROP TABLE occeventperson CASCADE;

ALTER TABLE public.choiceproposal DROP COLUMN generatingevent_occeventid;
ALTER TABLE public.choiceproposal DROP COLUMN responseevent_occeventid;



-- What is this?
ALTER TABLE event DROP COLUMN dateofrecord;

ALTER TABLE event ADD COLUMN timestart TIMESTAMP WITH TIME ZONE;
ALTER TABLE event ADD COLUMN timeend TIMESTAMP WITH TIME ZONE;

ALTER TABLE eventcategory ADD COLUMN defaultdurationmins INTEGER;

-- LOG REVISIONS 

ALTER TABLE log DROP COLUMN sessionid;
ALTER TABLE log DROP COLUMN reqview;
ALTER TABLE log DROP COLUMN viewed;

ALTER TABLE log ADD COLUMN credsig TEXT;
ALTER TABLE log ADD COLUMN subsys TEXT;
ALTER TABLE log ADD COLUMN severity TEXT;


ALTER TABLE property ADD COLUMN creationts TIMESTAMP WITH TIME ZONE;

-- close




--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (24, 'database/patches/dbpatch_beta24.sql', NULL, 'ecd', 'various changes');
