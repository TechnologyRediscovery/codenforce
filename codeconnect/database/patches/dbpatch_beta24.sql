-- ****************************************************************************
-- First patch post re-braining
-- 
-- 
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

-- not yet run locally or remote
ALTER TABLE ceevent RENAME TO event;

ALTER TABLE cecaseeventrule RENAME TO eventruleimpl;
ALTER TABLE ceeventperson RENAME TO eventperson;

DROP TABLE occperiodeventrule CASCADE;
DROP TABLE occevent CASCADE;
DROP TABLE occeventperson CASCADE;

ALTER TABLE public.choiceproposal DROP COLUMN generatingevent_occeventid;
ALTER TABLE public.choiceproposal DROP COLUMN responseevent_occeventid;

ALTER TABLE DROP COLUMN dateofrecord;

ALTER TABLE event ADD COLUMN timestart TIMESTAMP WITH TIME ZONE;
ALTER TABLE event ADD COLUMN timeend TIMESTAMP WITH TIME ZONE;

ALTER TABLE eventcategory ADD COLUMN defaultdurationmins INTEGER;



