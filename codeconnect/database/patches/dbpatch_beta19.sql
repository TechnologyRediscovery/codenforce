
ALTER TABLE municipality DROP COLUMN managername;
ALTER TABLE municipality DROP COLUMN managerphone;
ALTER TABLE municipality DROP COLUMN defaultcourtentity;
ALTER TABLE municipality DROP COLUMN defaultcodeofficeruser;
ALTER TABLE municipality ADD COLUMN primarystaffcontact_userid integer CONSTRAINT muni_staffcontact_userid_fk REFERENCES login (userid);
ALTER TABLE municipality ADD COLUMN notes text;
ALTER TABLE municipality ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE municipality ADD COLUMN lastupdated_userid INTEGER CONSTRAINT muni_lastupdatedby_userid_fk REFERENCES login (userid);

ALTER TABLE municourtentity ADD COLUMN relativeorder INTEGER;
ALTER TABLE munilogin ADD COLUMN codeofficerassignmentorder INTEGER;
ALTER TABLE munilogin ADD COLUMN staffassignmentorder INTEGER;
ALTER TABLE munilogin ADD COLUMN sysadminassignmentorder INTEGER;
ALTER TABLE munilogin ADD COLUMN supportassignmentorder INTEGER;

ALTER TABLE munilogin ADD COLUMN bypasscodeofficerassignmentorder INTEGER;
ALTER TABLE munilogin ADD COLUMN bypassstaffassignmentorder INTEGER;
ALTER TABLE munilogin ADD COLUMN bypasssysadminassignmentorder INTEGER;
ALTER TABLE munilogin ADD COLUMN bypasssupportassignmentorder INTEGER;

ALTER TABLE choicedirective ADD COLUMN requiredevaluationforbobclose BOOLEAN DEFAULT true;

ALTER TABLE occevent ADD COLUMN triggeringeventrule_ruleid INTEGER CONSTRAINT occevent_eventruleid_fk REFERENCES eventrule (ruleid);
ALTER TABLE occevent ADD COLUMN triggeringeventrulepassed BOOLEAN DEFAULT false;

ALTER TABLE ceevent ADD COLUMN triggeringeventrule_ruleid INTEGER CONSTRAINT ceevent_eventruleid_fk REFERENCES eventrule (ruleid);
ALTER TABLE ceevent ADD COLUMN triggeringeventrulepassed BOOLEAN DEFAULT false;

ALTER TABLE occperiodtype RENAME COLUMN defaultvalidityperioddays TO defaultpermitvalidityperioddays;
ALTER TABLE occperiodtype ADD COLUMN asynchronousinspectionvalidityperiod BOOLEAN DEFAULT false;
ALTER TABLE occperiodtype ADD COLUMN defaultinspectionvalidityperiod INTEGER;

ALTER TABLE propertyunit ADD COLUMN rentalintentstartdate TIMESTAMP WITH TIME ZONE;
ALTER TABLE propertyunit ADD COLUMN rentalintentstopdate TIMESTAMP WITH TIME ZONE;
ALTER TABLE propertyunit ADD COLUMN rentalintentlastupdatedby_userid INTEGER CONSTRAINT propunit_rentalintentupdatedby_fk REFERENCES eventrule (ruleid);

ALTER TABLE muniprofile ADD COLUMN continuousoccupancybufferdays INTEGER DEFAULT 0;
ALTER TABLE muniprofile ADD COLUMN minusrranktodeclarerentalintent INTEGER DEFAULT 3;





INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (19, 'database/patches/dbpatch_beta19.sql', '07-09-2019', 'ecd', 'municipality facelift and others');

