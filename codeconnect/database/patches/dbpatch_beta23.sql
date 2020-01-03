-- ****************************************************************************
-- Patch README:
-- Patch sealed for the re-braining of the team in December 2019 for beta
-- testing
-- Corresponds with zeronine.sql for re-braining

-- DO THIS FIRST
-- >>>>>>>>>>>>>>>>>>> UNCOMMENT and RUN THIS LINE ON ITS OWN <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

-- ALTER TYPE role ADD VALUE IF NOT EXISTS 'User' AFTER 'Public';

-- >>>>>>>>>>>>>>>>>>> Keep it commented out in this script since type updates must be performed independently <<<<<<<<<<<<<<<<<<<<<


ALTER TABLE login DROP COLUMN userrole;
ALTER TABLE login ADD COLUMN userrole role DEFAULT 'User'::role ;
UPDATE login SET userrole = 'User'::role;

ALTER TABLE occperiodeventrule ADD CONSTRAINT occperiodeventrule_pk PRIMARY KEY (occperiod_periodid, eventrule_ruleid);

ALTER TABLE cecaserule RENAME TO cecaseeventrule;

ALTER TABLE occperiodeventrule ADD COLUMN active BOOLEAN DEFAULT TRUE;
ALTER TABLE cecaseeventrule ADD COLUMN active BOOLEAN DEFAULT TRUE; 

ALTER TABLE public.occperiodeventrule
   ALTER COLUMN passedrule_eventid DROP NOT NULL;

ALTER TABLE public.cecaseeventrule
   ALTER COLUMN passedrule_eventid DROP NOT NULL;

ALTER TABLE muniprofileeventruleset ADD COLUMN cedefault BOOLEAN DEFAULT TRUE;

ALTER TABLE choiceproposal DROP COLUMN hidden;

ALTER TABLE public.eventrule DROP COLUMN promptingproposal_proposalid;

ALTER TABLE public.eventrule ADD COLUMN promptingdirective_directiveid INTEGER CONSTRAINT eventrule_directive_id_FK REFERENCES choicedirective (directiveid);

ALTER TABLE public.occperiodeventrule DROP CONSTRAINT occperiodeventrule_pk;



CREATE SEQUENCE IF NOT EXISTS occperiodeventrule_operid_pk_seq 
    START WITH 100
    INCREMENT BY 10
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.occperiodeventrule ADD COLUMN occperiodeventruleid INTEGER DEFAULT nextval('occperiodeventrule_operid_pk_seq');
ALTER TABLE public.occperiodeventrule ADD CONSTRAINT occperiodeventrule_pk PRIMARY KEY (occperiodeventruleid);

ALTER TABLE public.choicedirective ADD COLUMN relativeorder INTEGER DEFAULT 1;



DROP TABLE IF EXISTS public.choiceproposal;

CREATE TABLE public.choiceproposal
(
  proposalid integer NOT NULL DEFAULT nextval('ceeventproposalimplementation_seq'::regclass),
  directive_directiveid integer,
  generatingevent_cecaseeventid integer,
  initiator_userid integer,
  responderintended_userid integer,
  activateson timestamp with time zone,
  expireson timestamp with time zone,
  responderactual_userid integer,
  rejectproposal boolean DEFAULT false,
  responsetimestamp timestamp with time zone,
  responseevent_cecaseeventid integer,
  active boolean DEFAULT true,
  notes text,
  relativeorder integer,
  generatingevent_occeventid integer,
  responseevent_occeventid integer,
  occperiod_periodid integer,
  cecase_caseid integer,
  chosen_choiceid integer,
  CONSTRAINT ceeventproposalresponse_pk PRIMARY KEY (proposalid),
  CONSTRAINT ceeventpropimp_genevent_fk FOREIGN KEY (generatingevent_cecaseeventid)
      REFERENCES public.ceevent (eventid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT ceeventpropimp_initiator_fk FOREIGN KEY (initiator_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT ceeventpropimp_propid_fk FOREIGN KEY (directive_directiveid)
      REFERENCES public.choicedirective (directiveid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT ceeventpropimp_resev_fk FOREIGN KEY (responseevent_cecaseeventid)
      REFERENCES public.ceevent (eventid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT ceeventpropimp_responderactual_fk FOREIGN KEY (responderactual_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT ceeventpropimp_responderintended_fk FOREIGN KEY (responderintended_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT choiceproposal_cecaseid_fk FOREIGN KEY (cecase_caseid)
      REFERENCES public.cecase (caseid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT choiceproposal_chosenchoiceid_fk FOREIGN KEY (chosen_choiceid)
      REFERENCES public.choice (choiceid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT choiceproposal_genocceventid_fk FOREIGN KEY (generatingevent_occeventid)
      REFERENCES public.occevent (eventid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT choiceproposal_occperiodid_fk FOREIGN KEY (occperiod_periodid)
      REFERENCES public.occperiod (periodid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT choiceproposal_resocceventid_fk FOREIGN KEY (responseevent_occeventid)
      REFERENCES public.occevent (eventid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

DROP TABLE IF EXISTS loginmuniauthperiod;

CREATE TABLE public.loginmuniauthperiod
(
  muniauthperiodid integer NOT NULL DEFAULT nextval('munilogin_recordid_seq'::regclass),
  muni_municode integer NOT NULL,
  authuser_userid integer NOT NULL,
  defaultmuni boolean DEFAULT false,
  accessgranteddatestart timestamp with time zone NOT NULL DEFAULT '1970-01-01 00:00:00-05'::timestamp with time zone,
  accessgranteddatestop timestamp with time zone NOT NULL DEFAULT '1970-01-01 00:00:00-05'::timestamp with time zone,
  recorddeactivatedts timestamp with time zone,
  authorizedrole role,
  createdts timestamp with time zone,
  createdby_userid INTEGER NOT NULL,
  notes text,
  CONSTRAINT loginmuniauthperiod_pkey PRIMARY KEY (muniauthperiodid),
  CONSTRAINT loginmuniauthperiod_municode_fk FOREIGN KEY (muni_municode)
      REFERENCES public.municipality (municode) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT loginmuniauthperiod_userid_fk FOREIGN KEY (authuser_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
   CONSTRAINT loginmuniauthperiod_creator_userid_fk FOREIGN KEY (createdby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

ALTER TABLE login ADD COLUMN pswdlastupdated TIMESTAMP WITH TIME ZONE;
ALTER TABLE login ADD COLUMN active BOOLEAN DEFAULT TRUE;
ALTER TABLE login ADD COLUMN forcepasswordreset TIMESTAMP WITH TIME ZONE;
ALTER TABLE login ADD COLUMN createdby INTEGER CONSTRAINT login_createdby_fk REFERENCES login (userid);
ALTER TABLE login ADD COLUMN createdts TIMESTAMP WITH TIME ZONE;
ALTER TABLE login ADD COLUMN nologinvirtualonly BOOLEAN DEFAULT FALSE;

ALTER TABLE public.loginmuniauthperiod ADD COLUMN supportassignedby INTEGER CONSTRAINT loginmuniauthperiod_supportassignedby_userid_fk REFERENCES login (userid);

ALTER TABLE public.login ADD COLUMN userrole role DEFAULT 'User'::role;

ALTER TABLE public.login
   ALTER COLUMN password DROP NOT NULL;

ALTER TABLE public.login ADD COLUMN pswdcleartext TEXT;
ALTER TABLE public.loginmuniauthperiod ADD COLUMN rank INTEGER DEFAULT 1;
ALTER TABLE public.loginmuniauthperiod RENAME COLUMN rank to assignmentrank;

ALTER TABLE public.loginmuniauthperiod DROP COLUMN defaultmuni;

-- REMOTE SERVER RUN CURSOR


CREATE SEQUENCE IF NOT EXISTS loginmuniauthperiodlog_seq
  START WITH 2777
  INCREMENT BY 7
  MINVALUE 2777
  NO MAXVALUE 
  CACHE 1;

CREATE TABLE public.loginmuniauthperiodlog
(
  authperiodlogentryid      INTEGER NOT NULL CONSTRAINT logincredentialex_pk PRIMARY KEY DEFAULT nextval('loginmuniauthperiodsession_seq'),
  authperiod_periodid       INTEGER NOT NULL CONSTRAINT logincredentialex_periodid_fk REFERENCES loginmuniauthperiod (muniauthperiodid),
  category                  TEXT,
  entryts                   TIMESTAMP WITH TIME ZONE,
  entrydateofecord          TIMESTAMP WITH TIME ZONE,
  disputedby_userid         INTEGER CONSTRAINT loginmuniauthperiodsession_disputedby_userid_fk REFERENCES login (userid),
  disputedts                TIMESTAMP WITH TIME ZONE,
  notes                     TEXT,
  cookie_jsessionid         TEXT,               
  header_remoteaddr         TEXT,
  header_useragent          TEXT,
  header_dateraw            TEXT,
  header_date               TIMESTAMP WITH TIME ZONE,
  header_cachectl           TEXT,
  audit_usersession_userid        INTEGER NOT NULL CONSTRAINT logincredentialex_usersessionid_fk REFERENCES login (userid),
  audit_usercredential_userid     INTEGER NOT NULL CONSTRAINT logincredentialex_usercredentialid_fk REFERENCES login (userid),
  audit_muni_municode             INTEGER NOT NULL CONSTRAINT logincredentialex_muni_fk REFERENCES municipality (municode)
);

ALTER TABLE LOGIN ADD COLUMN deactivatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE LOGIN ADD COLUMN deactivated_userid INTEGER CONSTRAINT login_decatedby_userid_fk REFERENCES login (userid);


-- From Nathan

ALTER TABLE moneyoccperiodtypefee ADD COLUMN active boolean;
ALTER TABLE moneyoccperiodtypefee ADD COLUMN autoassign boolean;



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (23, 'database/patches/dbpatch_beta23.sql', NULL, 'ecd', 'occ beta tweaking');