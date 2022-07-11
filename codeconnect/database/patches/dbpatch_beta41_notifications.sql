-- DB Patch 41:Notifications (e.g. special events)






-- LOCAL CURSOR

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









-- drop these columns and check integration methods/objects for compat
-- DETAIL:  drop cascades to constraint codesetelement_feeid_fk on table codesetelement
-- drop cascades to constraint muniprofilefee__feeid_fk on table muniprofilefee
-- Query returned successfully with no result in 184 msec.
DROP TABLE public.muniprofilefee;




-- |^|^|^|^|^|^|^|^|^|^|^|^|^|^|^|^|^ END GRAND TRANSACTION REVAMP |^|^|^|^|^|^|^|^|^|^|^|^|^|^|^|^|^








-- WIP fields
ALTER TABLE public.login DROP CONSTRAINT login_personlink_personid_fk;
ALTER TABLE public.login
  ADD CONSTRAINT login_humanlink_humanid_fk FOREIGN KEY (personlink)
      REFERENCES public.human (humanid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;


ALTER TABLE public.cecase RENAME COLUMN login_userid TO manager_userid;
ALTER TABLE public.cecase ADD COLUMN createdby_userid INTEGER
	CONSTRAINT cecase_createdby_userid_fk REFERENCES login (userid);
ALTER TABLE public.cecase ADD COLUMN createdts TIMESTAMP WITH TIME ZONE DEFAULT now();
ALTER TABLE public.cecase ADD COLUMN deactivatedby_userid INTEGER
	CONSTRAINT cecase_deactivatedby_userid_fk REFERENCES login (userid);
ALTER TABLE public.cecase ADD COLUMN deactivatedts TIMESTAMP WITH TIME ZONE DEFAULT now();
UPDATE TABLE public.cecase SET createdby_userid = manager_userid;


-- finish me
ALTER TABLE occchecklist ADD COLUMN inspectionspecific INTEGER
    CONSTRAINT occchecklist_inspspecific_fk REFERENCES 


ALTER TABLE public.municipality DROP COLUMN office_propertyid;







-- EXTRA STUFF 
CREATE OR REPLACE FUNCTION public.cnf_nov_udpatestaticsendersigfields(targetmunicode INTEGER)
    RETURNS INTEGER AS
$BODY$
    DECLARE
        nov_rec RECORD;
        pers_rec RECORD;
        fullname TEXT;
        fixedname TEXT;
        nov_count INTEGER;
    BEGIN
        nov_count := 0;
        FOR nov_rec IN SELECT noticeid, notifyingofficer_userid FROM public.noticeofviolation 
            INNER JOIN public.cecase ON (noticeofviolation.caseid = cecase.caseid)
            INNER JOIN public.property ON (cecase.property_propertyid = property.propertyid)
            WHERE municipality_municode = targetmunicode AND notifyingofficer_userid IS NOT NULL

            LOOP -- over NOVs by MUNI
                SELECT personid, fname, lname, jobtitle, phonework, email 
                    FROM public.login 
                    LEFT OUTER JOIN public.person ON (login.personlink = person.personid) 
                    WHERE userid = nov_rec.notifyingofficer_userid INTO pers_rec;

                RAISE NOTICE 'WRITING FIXED SENDER ID % INTO NOV ID %', nov_rec.notifyingofficer_userid, nov_rec.noticeid;
                fullname := pers_rec.fname || ' ' || pers_rec.lname;

                EXECUTE format('UPDATE noticeofviolation SET 
                    fixednotifyingofficername = %L,
                    fixednotifyingofficertitle = %L,
                    fixednotifyingofficerphone = %L,
                    fixednotifyingofficeremail = %L,
                    notifyingofficer_humanid = %L WHERE noticeid=%L;',
                    fullname,
                    pers_rec.jobtitle,
                    pers_rec.phonework,
                    pers_rec.email,
                    pers_rec.personid,
                    nov_rec.noticeid);
                nov_count := nov_count + 1;
                RAISE NOTICE 'UPDATE SUCCESS! Count: % ', nov_count;
            END LOOP; -- loop over NOVs by MUNI
        RETURN nov_count;
    END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;




-- extra gunk - not run remotely


DROP TABLE humanmailingrole;

-- TODO: Remove personid_recipient on NOV after full migration to human and mailing address
-- todo clean up old citation stuff


-- TODO: Remove the codeelment_id column of occchecklistspacetypeelement
-- after the refactor 


CREATE SEQUENCE IF NOT EXISTS occperiodlease_leaseid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.occperiodlease
    (
        leaseid             INTEGER PRIMARY KEY DEFAULT nextval('parcelunithumanlease_leaseid_seq'),
        datestart           DATE,
        dateend             DATE,
        signeddate          DATE,
        monthlyrent         MONEY,
        -- finish me??
        --leasor_humanid      INTEGER CONSTRAINT parcelhumanlease 
    
    );









INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (41, 'database/patches/dbpatch_beta41_notifications.sql', NULL, 'ecd', '');