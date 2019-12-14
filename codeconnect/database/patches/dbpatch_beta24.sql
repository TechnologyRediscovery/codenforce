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