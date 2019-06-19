
-- have not wired this new column up yet!
ALTER TABLE ceactionrequest ADD COLUMN usersubmitter_userid INTEGER;
ALTER TABLE ceactionrequest ADD CONSTRAINT ceactionreq_usersub_fk FOREIGN KEY (usersubmitter_userid) REFERENCES login (userid);

-- add stipulated compliance that freeze in time when added to a notice of violation

ALTER TYPE ceeventtype ADD VALUE IF NOT EXISTS 'Citation' AFTER 'Compliance';

CREATE SEQUENCE IF NOT EXISTS occperiodid_seq
	START WITH 1000
	INCREMENT BY 1 
	MINVALUE 1000
	NO MAXVALUE 
	CACHE 1;

CREATE TABLE public.occperiod
(
	periodid 						INTEGER DEFAULT nextval('occperiodid_seq') NOT NULL,
	propertyunit_unitid
	startdate						TIMESTAMP WITH TIME ZONE,
	startdatecertifiedby_userid
	startdatecertifiedts
	enddate
	enddatecertifiedby_userid
	enddatecterifiedts
	manager_userid
) ;


CREATE SEQUENCE IF NOT EXISTS ceeventproposal_seq
	START WITH 10
	INCREMENT BY 1 
	MINVALUE 10
	NO MAXVALUE 
	CACHE 1;

CREATE TABLE public.ceeventproposal
(
	proposalid 						INTEGER DEFAULT nextval('ceeventproposal_seq') NOT NULL CONSTRAINT ceeventproposal_pk PRIMARY KEY,
	title							TEXT,
	overalldescription				text,
	creator_userid 					integer,
	choice1eventcat_catid			integer CONSTRAINT ceeventproposal_choice1_catid_fk REFERENCES ceeventcategory (categoryid),
	choice1description 				text,
	choice2eventcat_catid			integer CONSTRAINT ceeventproposal_choice2_catid_fk REFERENCES ceeventcategory (categoryid),
	choice2description 				text,
	choice3eventcat_catid			integer CONSTRAINT ceeventproposal_choice3_catid_fk REFERENCES ceeventcategory (categoryid),
	choice3description 				text,
	directproposaltodefaultmuniceo 	boolean DEFAULT true,
 	directproposaltodefaultmunistaffer boolean DEFAULT false

) ;


CREATE SEQUENCE IF NOT EXISTS ceeventproposalresponse_seq
	START WITH 1000
	INCREMENT BY 1 
	MINVALUE 1000
	NO MAXVALUE 
	CACHE 1;

CREATE TABLE public.ceeventproposalresponse
(
	responseid 						INTEGER DEFAULT nextval('ceeventproposalresponse_seq') NOT NULL  CONSTRAINT ceeventproposalresponse_pk PRIMARY KEY,
	initiator 						INTEGER CONSTRAINT ceeventpropres_initiator_fk REFERENCES login (userid),
	proposal_proposalid 				integer CONSTRAINT ceeventpropres_prop_fk REFERENCES ceeventproposal (proposalid),
	responseevent_eventid 			integer CONSTRAINT ceeventpropres_resev_fk REFERENCES ceevent (eventid),
	responderintended_userid 		integer CONSTRAINT ceeventpropres_responderintended_fk REFERENCES login (userid),
	responder_userid 				integer CONSTRAINT ceeventpropres_responderactual_fk REFERENCES login (userid),
	responsetimestamp 				timestamp with time zone,
	notes 							text,
	rejectproposal 					boolean DEFAULT false
) ;

ALTER TABLE ceeventproposal DROP COLUMN creator_userid;


ALTER TABLE ceevent DROP COLUMN responsetimestamp; 
ALTER TABLE ceevent DROP COLUMN actionrequestedby_userid;
ALTER TABLE ceevent DROP COLUMN respondernotes;
ALTER TABLE ceevent DROP COLUMN responderintended_userid;
ALTER TABLE ceevent DROP COLUMN requestedeventcat_catid;
ALTER TABLE ceevent DROP COLUMN responseevent_eventid;
ALTER TABLE ceevent DROP COLUMN rejeecteventrequest; 
ALTER TABLE ceevent DROP COLUMN responderactual_userid;

ALTER TABLE ceeventcategory ADD COLUMN proposal_propid INTEGER CONSTRAINT ceeventcat_proposal_propid_fk REFERENCES ceeventproposal (proposalid);

-- Has not been run on remote server

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (14, 'database/patches/dbpatch_beta13.sql', '', 'ecd', 'final clean ups');

