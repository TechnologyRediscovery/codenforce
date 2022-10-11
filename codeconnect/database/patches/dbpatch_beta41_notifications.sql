-- DB Patch 41:Notifications (e.g. special events)







-- REMOTE CURSOR


-- event cat changes
-- snooze rank floor
-- process rank floor

-- need a table for event review that has a user, timestamp, and link to event, and notes, perhaps re-review? event trigger?
-- An event emitter is an object that carries an event category and an eventholder that can receive a given event
-- we could trigger event reviews, but creating an event that get batch assigned that requires a certain floor for processing, 
-- and processing requires creating an event of a certain category, and that category could be a special one for further review.
-- include a flag for allow category substitution within type on the event category



CREATE TYPE eventemissionenum AS ENUM (
	'NOTICE_OF_VIOLATION_SENT',
    'NOTICE_OF_VIOLATION_FOLLOWUP',
    'NOTICE_OF_VIOLATION_RETURNED',
    'TRANSACTION',
    'CITATION_HEARING');


CREATE SEQUENCE IF NOT EXISTS eventemission_emissionid_seq 
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


CREATE TABLE public.eventemission
(
	emissionid INTEGER PRIMARY KEY DEFAULT nextval('eventemission_emissionid_seq'),
	emissionenum eventemissionenum NOT NULL,
	emissionts TIMESTAMP WITH TIME ZONE NOT NULL,
	event_eventid INTEGER NOT NULL CONSTRAINT eventemission_eventid_fk REFERENCES event (eventid),
	emittedby_userid INTEGER NOT NULL CONSTRAINT eventemission_emitteruser_fk REFERENCES login (userid),
	emitter_novid INTEGER CONSTRAINT eventemission_novid_fk REFERENCES noticeofviolation (noticeid),
	emitter_citationid INTEGER CONSTRAINT emitter_citation_fk REFERENCES citation (citationid),
	deactivatedts TIMESTAMP WITH TIME ZONE,
	deactivatedby_userid INTEGER CONSTRAINT eventemission_deactivatedby_fk REFERENCES login (userid),
	emissionresponsets TIMESTAMP WITH TIME ZONE,
	emissionresponse_userid INTEGER CONSTRAINT eventemission_response_fk REFERENCES login (userid),
	notes TEXT
); 

ALTER TABLE event RENAME COLUMN creationts TO createdts;
ALTER TABLE event RENAME COLUMN creator_userid TO createdby_userid;

ALTER TABLE public.event ADD COLUMN deactivatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.event ADD COLUMN deactivatedby_userid INTEGER CONSTRAINT event_deactivatedby_fk REFERENCES login (userid);


UPDATE public.event SET deactivatedts = now() WHERE active=FALSE;


ALTER TABLE public.citationstatus ADD COLUMN terminalstatus BOOLEAN DEFAULT FALSE;

-- 




INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (41, 'database/patches/dbpatch_beta41_notifications.sql', '07-15-2022', 'ecd', '');