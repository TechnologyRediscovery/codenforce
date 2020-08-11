
-- This must be run all by itself and THEN COMMENTED OUT for running the rest of the patch
ALTER TYPE ceeventtype ADD VALUE IF NOT EXISTS 'Citation' AFTER 'Compliance';


-- have not wired this new column up yet!
ALTER TABLE ceactionrequest ADD COLUMN usersubmitter_userid INTEGER;
ALTER TABLE ceactionrequest ADD CONSTRAINT ceactionreq_usersub_fk FOREIGN KEY (usersubmitter_userid) REFERENCES login (userid);

-- add stipulated compliance that freeze in time when added to a notice of violation



-- proposals have choices and those choices can point to rules, but a category cannot point to a rule, only a proposal


ALTER TABLE public.ceeventcategory RENAME TO eventcategory;

-- already deprecated
DROP TYPE public.ceeventclass;


ALTER TYPE public.ceeventtype
  RENAME TO "eventtype";


ALTER TABLE public.eventcategory ADD COLUMN relativeorderwithintype INTEGER default 0;
ALTER TABLE public.eventcategory ADD COLUMN relativeorderacrossallevents INTEGER default 0;
ALTER TABLE public.eventcategory ADD COLUMN hosteventdescriptionsuggtext  TEXT;


CREATE SEQUENCE IF NOT EXISTS eventproposal_seq
  START WITH 10
  INCREMENT BY 1 
  MINVALUE 10
  NO MAXVALUE 
  CACHE 1;

CREATE TABLE public.eventproposal
(
  proposalid                INTEGER DEFAULT nextval('eventproposal_seq') NOT NULL CONSTRAINT eventproposal_pk PRIMARY KEY,
  title                     TEXT,
  overalldescription        text,
  creator_userid            INTEGER,
  directproposaltodefaultmuniceo  boolean DEFAULT true,
  directproposaltodefaultmunistaffer boolean DEFAULT false,
  directproposaltodeveloper   boolean DEFAULT false,
  executechoiceiflonewolf     boolean DEFAULT false,
  applytoclosedentities       boolean DEFAULT true,
  instantiatemultiple                             boolean DEFAULT true,
  inactivategeneventoneval                        boolean DEFAULT false,
  maintainreldatewindow                           boolean DEFAULT true,
  autoinactivateonbobclose                         boolean DEFAULT true,
  autoinactiveongeneventinactivation               boolean DEFAULT true,
  minimumrequireduserranktoview                    INTEGER DEFAULT 3,
  minimumrequireduserranktoevaluate               INTEGER DEFAULT 3,
  active                                          BOOLEAN DEFAULT true

) ;

ALTER TABLE eventcategory ADD COLUMN proposal_propid INTEGER;

ALTER TABLE public.eventcategory
  ADD CONSTRAINT ceeventcat_proposal_fk FOREIGN KEY (proposal_propid)
      REFERENCES public.eventproposal (proposalid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;






CREATE TABLE public.eventrule
(
  ruleid                                          integer NOT NULL DEFAULT nextval('cecasephasechangerule_seq'::regclass) PRIMARY KEY,
  title                                           text,
  description                                     text,
  requiredeventtype                               eventtype,
  forbiddeneventtype                              eventtype,
  requiredeventcat_catid                          integer CONSTRAINT eventrule_requiredeventcatid_fk REFERENCES public.eventcategory (categoryid),  
  requiredeventcatthresholdtypeintorder           BOOLEAN DEFAULT false,
  requiredeventcatupperboundtypeintorder          BOOLEAN DEFAULT false,
  requiredeventcatthresholdglobalorder            BOOLEAN DEFAULT false,
  requiredeventcatupperboundglobalorder           BOOLEAN DEFAULT false,
  forbiddeneventcat_catid                         integer CONSTRAINT eventrule_forbiddeneventcatid_fk REFERENCES public.eventcategory (categoryid),
  forbiddeneventcatthresholdtypeintorder          BOOLEAN DEFAULT false,
  forbiddeneventcatupperboundtypeintorder         BOOLEAN DEFAULT false,
  forbiddeneventcatthresholdglobalorder           BOOLEAN DEFAULT false,
  forbiddeneventcatupperboundglobalorder          BOOLEAN DEFAULT false,
  mandatorypassreqtocloseentity                   boolean DEFAULT true,
  autoremoveonentityclose                         boolean DEFAULT true,
  promptingproposal_proposalid                    integer,
  triggeredeventcatonpass                         INTEGER CONSTRAINT eventrule_triggpasseventcatid_fk REFERENCES public.eventcategory (categoryid),
  triggeredeventcatonfail                         INTEGER CONSTRAINT eventrule_triggfaileventcatid_fk REFERENCES public.eventcategory (categoryid),
  active                                          boolean DEFAULT true
);

ALTER TABLE public.citationstatus RENAME COLUMN phasechangerule_ruleid TO eventrule_ruleid;

ALTER TABLE public.citationstatus
  ADD CONSTRAINT citationstatus_eventruleid_fk FOREIGN KEY (eventrule_ruleid)
      REFERENCES public.eventrule (ruleid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;




CREATE SEQUENCE IF NOT EXISTS choice_choiceid_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;


CREATE TABLE public.choice
(
  choiceid                          INTEGER NOT NULL PRIMARY KEY DEFAULT nextval('choice_choiceid_seq'),
  title                             TEXT,
  description                       TEXT,
  eventcat_catid                    INTEGER CONSTRAINT eventchoice_eventcatid_fk REFERENCES eventcategory (categoryid),
  addeventcat                       BOOLEAN DEFAULT true,
  eventrule_ruleid                  INTEGER CONSTRAINT eventchoice_ruleid_fk REFERENCES eventrule (ruleid),
  addeventrule                      BOOLEAN DEFAULT true,
  relativeorder                     INTEGER NOT NULL,
  active                            BOOLEAN DEFAULT true,
  minimumrequireduserranktoview     INTEGER DEFAULT 3,
  minimumrequireduserranktochoose   INTEGER DEFAULT 3

);


CREATE TABLE public.eventproposalchoice
(
  choice_choiceid       INTEGER NOT NULL, 
  eventproposal_proposalid  INTEGER NOT NULL,
  CONSTRAINT eventpropchoice_comppk_pk PRIMARY KEY (choice_choiceid, eventproposal_proposalid),
  CONSTRAINT eventpopchoice_choiceid_fk FOREIGN KEY (choice_choiceid)
    REFERENCES choice (choiceid),
  CONSTRAINT eventpropchoice_proposalid FOREIGN KEY (eventproposal_proposalid)
    REFERENCES eventproposal (proposalid)

);


CREATE SEQUENCE IF NOT EXISTS occevent_eventid_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;

CREATE TABLE public.occevent
(
  eventid integer NOT NULL DEFAULT nextval('occevent_eventid_seq'::regclass),
  category_catid integer NOT NULL,
  occperiod_periodid INTEGER NOT NULL CONSTRAINT occevent_periodid_fk REFERENCES public.occperiod (periodid),
  dateofrecord timestamp with time zone,
  eventtimestamp timestamp with time zone,
  eventdescription text,
  owner_userid integer NOT NULL,
  disclosetomunicipality boolean DEFAULT true,
  disclosetopublic boolean DEFAULT false,
  activeevent boolean DEFAULT true,
  hidden boolean DEFAULT false,
  notes text,
  CONSTRAINT occevent_eventid_pk PRIMARY KEY (eventid),
  CONSTRAINT occevent_eventcategory_fk FOREIGN KEY (category_catid)
      REFERENCES public.eventcategory (categoryid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occevent_login_userid FOREIGN KEY (owner_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);





CREATE SEQUENCE IF NOT EXISTS occeventproposalimplementation_id_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;


CREATE TABLE public.occeventproposalimplementation
(
  implementationid integer NOT NULL DEFAULT nextval('occeventproposalimplementation_id_seq'::regclass),
  proposal_propid integer,
  generatingevent_eventid integer,
  initiator_userid integer,
  responderintended_userid integer,
  activateson timestamp with time zone,
  expireson timestamp with time zone,
  responderactual_userid integer,
  chosen_choiceid integer,
  rejectproposal boolean DEFAULT false,
  responsetimestamp timestamp with time zone,
  responseevent_eventid integer,
  active boolean DEFAULT true,
  notes text,
  CONSTRAINT occeventproposalresponse_pk PRIMARY KEY (implementationid),
  CONSTRAINT occeventpropimp_genevent_fk FOREIGN KEY (generatingevent_eventid)
      REFERENCES public.occevent (eventid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventpropimp_initiator_fk FOREIGN KEY (initiator_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventprop_propid_fk FOREIGN KEY (proposal_propid)
      REFERENCES public.eventproposal (proposalid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventpropimp_respev_fk FOREIGN KEY (responseevent_eventid)
      REFERENCES public.occevent (eventid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventpropimp_responderactual_fk FOREIGN KEY (responderactual_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventpropimp_responderintended_fk FOREIGN KEY (responderintended_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventpropimp_choice_fk FOREIGN KEY (chosen_choiceid)
      REFERENCES choice (choiceid)
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);



CREATE TABLE public.occeventperson
(
  occevent_eventid integer NOT NULL,
  person_personid integer NOT NULL,
  CONSTRAINT occevent_eventid PRIMARY KEY (occevent_eventid, person_personid),
  CONSTRAINT occeventperson_occevent_eventid_fk FOREIGN KEY (occevent_eventid)
      REFERENCES public.ceevent (eventid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occeventperson_person_personid_fk FOREIGN KEY (person_personid)
      REFERENCES public.person (personid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);


CREATE SEQUENCE IF NOT EXISTS ceeventproposalimplementation_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;

CREATE TABLE public.ceeventproposalimplementation
(
  implementationid        INTEGER DEFAULT nextval('ceeventproposalimplementation_seq') NOT NULL  CONSTRAINT ceeventproposalresponse_pk PRIMARY KEY,
  proposal_propid         INTEGER CONSTRAINT ceeventpropimp_propid_fk REFERENCES eventproposal (proposalid),
  generatingevent_eventid     INTEGER CONSTRAINT ceeventpropimp_genevent_fk REFERENCES ceevent (eventid),
  initiator_userid        INTEGER CONSTRAINT ceeventpropimp_initiator_fk REFERENCES login (userid),
  responderintended_userid    INTEGER CONSTRAINT ceeventpropimp_responderintended_fk REFERENCES login (userid),
  responderactual_userid      INTEGER CONSTRAINT ceeventpropimp_responderactual_fk REFERENCES login (userid),
  rejectproposal          boolean DEFAULT false,
  responsetimestamp         timestamp with time zone,
  responseevent_eventid       INTEGER CONSTRAINT ceeventpropimp_resev_fk REFERENCES ceevent (eventid),
  active              BOOLEAN DEFAULT true,
  notes               text
) ;



ALTER TABLE ceevent DROP COLUMN responsetimestamp; 
ALTER TABLE ceevent DROP COLUMN actionrequestedby_userid;
ALTER TABLE ceevent DROP COLUMN respondernotes;
ALTER TABLE ceevent DROP COLUMN responderintended_userid;
ALTER TABLE ceevent DROP COLUMN requestedeventcat_catid;
ALTER TABLE ceevent DROP COLUMN responseevent_eventid;
ALTER TABLE ceevent DROP COLUMN rejeecteventrequest; 
ALTER TABLE ceevent DROP COLUMN responderactual_userid;


-- Has not been run on remote server


CREATE TABLE public.occperiodtypeeventrule
(
  eventrule_ruleid            INTEGER NOT NULL CONSTRAINT occperiodeventrule_ruleid_fk REFERENCES eventrule (ruleid),
  occperiodtype_typeid          INTEGER NOT NULL CONSTRAINT occperiodeventrule_periodid_fk REFERENCES occperiodtype (typeid),
  CONSTRAINT occperiodtypeeventrule_pk_comp PRIMARY KEY (eventrule_ruleid, occperiodtype_typeid)
);




INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (15, 'database/patches/dbpatch_beta14.sql', '06-29-2019', 'ecd', 'event and proposals: major overhaul');

