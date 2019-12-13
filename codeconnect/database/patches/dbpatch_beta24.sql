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
