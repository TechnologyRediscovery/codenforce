BEGIN;
ALTER TABLE codeviolation ADD COLUMN compliancetimestamp TIMESTAMP WITH TIME ZONE;
ALTER TABLE codeviolation ADD COLUMN complianceuser INTEGER CONSTRAINT codeviolation_complianceofficer_fk REFERENCES login (userid);
ALTER TABLE codeviolation ADD COLUMN compliancetfevent INTEGER CONSTRAINT codeviolation_compliancetfevent_fk REFERENCES ceevent (eventid);
COMMIT;