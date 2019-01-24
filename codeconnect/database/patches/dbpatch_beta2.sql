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
	entrytimestamp timestamp with time zone NOT NULL DEFAULT now()  
  ) ;

COMMIT;

