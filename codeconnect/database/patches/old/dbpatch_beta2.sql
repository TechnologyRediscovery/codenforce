BEGIN;

ALTER TABLE cecase ADD COLUMN propertyinfocase BOOLEAN;

ALTER TABLE ceevent ADD COLUMN viewrequestedby_userid INTEGER;
ALTER TABLE ceevent ADD CONSTRAINT ceevent_viewrequestedby_fk FOREIGN KEY (viewrequestedby_userid)
    REFERENCES public.login (userid);
ALTER TABLE ceevent ADD COLUMN viewnotes text;

CREATE SEQUENCE IF NOT EXISTS loginobjecthistory_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


CREATE TABLE loginobjecthistory
  (
    historyentryid  			INTEGER DEFAULT nextval('loginobjecthistory_seq') CONSTRAINT historyentryid_pk PRIMARY KEY,
    login_userid				INTEGER CONSTRAINT hist_user_fk REFERENCES login (userid),
    person_personid				INTEGER CONSTRAINT hist_person_fk REFERENCES person (personid),
    property_propertyid			INTEGER CONSTRAINT hist_prop_fk REFERENCES property (propertyid),
    ceactionrequest_requestid	INTEGER CONSTRAINT hist_ceactionrequest_fk REFERENCES ceactionrequest (requestid),
    cecase_caseid				INTEGER CONSTRAINT hist_cecase_fk REFERENCES cecase (caseid),
    ceevent_eventid				INTEGER CONSTRAINT hist_event_fk REFERENCES ceevent (eventid),
    occapp_appid				INTEGER CONSTRAINT hist_occapp_fk REFERENCES occupancypermitapplication (applicationid),
    occinspec_inspecid			INTEGER CONSTRAINT his_occinspec_fk REFERENCES occupancyinspection (inspectionid),
    occpermit_permitid			INTEGER CONSTRAINT his_occpermit_fk REFERENCES occupancypermit (permitid),
	entrytimestamp 				timestamp with time zone NOT NULL DEFAULT now()  
  ) ;

ALTER TABLE person RENAME COLUMN mailingsameasresidence TO useseparatemailingaddr;
ALTER TABLE person ADD COLUMN canexpire BOOLEAN DEFAULT FALSE;
ALTER TABLE person DROP COLUMN addressofresidence;
ALTER TABLE person ADD COLUMN userlink INTEGER;
ALTER TABLE person ADD CONSTRAINT person_userlink_userid_fk FOREIGN KEY (userlink) REFERENCES login (userid);

ALTER TABLE login ADD COLUMN personlink INTEGER;
ALTER TABLE login ADD CONSTRAINT login_personlink_personid_fk FOREIGN KEY (personlink) REFERENCES person (personid);

UPDATE person SET useseparatemailingaddr = FALSE;
ALTER TABLE person ALTER COLUMN useseparatemailingaddr SET DEFAULT FALSE;

ALTER TABLE photodoc ADD COLUMN photodoccommitted BOOLEAN DEFAULT TRUE;



CREATE TABLE dbpatch
(
	patchnum				INTEGER CONSTRAINT dbpatch_pk PRIMARY KEY,
	patchfilename			text,
	datepublished			timestamp,
	patchauthor				text,
	notes					text

);


INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (1, '/database/patches/dbpatch_beta1.sql', '01-01-2019', 'ecd', 'to be patched to db pull as of team of 3 in dec 2018');
INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (2, '/database/patches/dbpatch_beta2.sql', '01-24-2019', 'ecd', 'and this very table is born!');




COMMIT;

-- SEALED with export of the data dictionary 