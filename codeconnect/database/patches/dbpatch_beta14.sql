
-- have not wired this new column up yet!
ALTER TABLE ceactionrequest ADD COLUMN usersubmitter_userid INTEGER;
ALTER TABLE ceactionrequest ADD CONSTRAINT ceactionreq_usersub_fk FOREIGN KEY (usersubmitter_userid) REFERENCES login (userid);

-- add stipulated compliance that freeze in time when added to a notice of violation

ALTER TYPE ceeventtype ADD VALUE IF NOT EXISTS 'Citation' AFTER 'Compliance';



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
	creator_userid 					INTEGER,
	choice1eventcat_catid			INTEGER CONSTRAINT ceeventproposal_choice1_catid_fk REFERENCES ceeventcategory (categoryid),
	choice1description 				text,
	choice2eventcat_catid			INTEGER CONSTRAINT ceeventproposal_choice2_catid_fk REFERENCES ceeventcategory (categoryid),
	choice2description 				text,
	choice3eventcat_catid			INTEGER CONSTRAINT ceeventproposal_choice3_catid_fk REFERENCES ceeventcategory (categoryid),
	choice3description 				text,
	directproposaltodefaultmuniceo 	boolean DEFAULT true,
 	directproposaltodefaultmunistaffer boolean DEFAULT false,
 	directproposaltodeveloper		boolean DEFAULT false,
 	active	 						BOOLEAN DEFAULT true

) ;


CREATE SEQUENCE IF NOT EXISTS ceeventproposalimplementation_seq
	START WITH 1000
	INCREMENT BY 1 
	MINVALUE 1000
	NO MAXVALUE 
	CACHE 1;

CREATE TABLE public.ceeventproposalimplementation
(
	implementationid				INTEGER DEFAULT nextval('ceeventproposalimplementation_seq') NOT NULL  CONSTRAINT ceeventproposalresponse_pk PRIMARY KEY,
	proposal_propid 				INTEGER CONSTRAINT ceeventpropimp_propid_fk REFERENCES ceeventproposal (proposalid),
	generatingevent_eventid			INTEGER CONSTRAINT ceeventpropimp_genevent_fk REFERENCES ceevent (eventid),
	initiator_userid				INTEGER CONSTRAINT ceeventpropimp_initiator_fk REFERENCES login (userid),
	responderintended_userid 		INTEGER CONSTRAINT ceeventpropimp_responderintended_fk REFERENCES login (userid),
	responderactual_userid 			INTEGER CONSTRAINT ceeventpropimp_responderactual_fk REFERENCES login (userid),
	rejectproposal 					boolean DEFAULT false,
	responsetimestamp 				timestamp with time zone,
	responseevent_eventid 			INTEGER CONSTRAINT ceeventpropimp_resev_fk REFERENCES ceevent (eventid),
	active							BOOLEAN DEFAULT true,
	notes 							text
) ;


ALTER TABLE ceevent DROP COLUMN responsetimestamp; 
ALTER TABLE ceevent DROP COLUMN actionrequestedby_userid;
ALTER TABLE ceevent DROP COLUMN respondernotes;
ALTER TABLE ceevent DROP COLUMN responderintended_userid;
ALTER TABLE ceevent DROP COLUMN requestedeventcat_catid;
ALTER TABLE ceevent DROP COLUMN responseevent_eventid;
ALTER TABLE ceevent DROP COLUMN rejeecteventrequest; 
ALTER TABLE ceevent DROP COLUMN responderactual_userid;

ALTER TABLE ceeventcategory ADD COLUMN proposal_propid INTEGER CONSTRAINT ceeventcat_proposal_propid_fk REFERENCES ceeventproposal (proposalid), ;

-- Has not been run on remote server

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (14, 'database/patches/dbpatch_beta14.sql', '', 'ecd', 'final clean ups');

