
-- DB Patch 40
-- MOPPING up occ inspection and getting transfers working




ALTER TABLE codeviolation ADD COLUMN transferredts TIMESTAMP WITH TIME ZONE;
ALTER TABLE codeviolation ADD COLUMN transferredby_userid INTEGER
	CONSTRAINT codeviolation_transferredbyuserid_fk REFERENCES login (userid);
ALTER TABLE codeviolation ADD COLUMN transferredtocecase_caseid INTEGER
	CONSTRAINT codeviolation_transferredtocecase_fk REFERENCES cecase (caseid);

ALTER TABLE occinspectedspaceelement ADD COLUMN transferredts TIMESTAMP WITH TIME ZONE;
ALTER TABLE occinspectedspaceelement ADD COLUMN transferredby_userid INTEGER
	CONSTRAINT occinspectedspaceelement_transferredbyuserid_fk REFERENCES login (userid);
ALTER TABLE occinspectedspaceelement ADD COLUMN transferredtocecase_caseid INTEGER
	CONSTRAINT occinspectedspaceelement_transferredtocecase_fk REFERENCES cecase (caseid);


INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors, 
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal, 
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins, 
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (311, CAST('Origination' AS eventtype), 'Generic case origination event', 'Placeholder category to signal a case opening', FALSE, 
            TRUE, 10, 0, 0, 
            'This generic event was created by the case auditor if it discovered a case without an origination event', NULL, 15, 
            TRUE, 5, 0 , 5);


INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors, 
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal, 
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins, 
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (312, CAST('Closing' AS eventtype), 'Generic case closing event', 'Placeholder category to signal a case closing', FALSE, 
            TRUE, 10, 0, 0, 
            'This generic event was created by the case auditor if it discovered a case without a closing event', NULL, 15, 
            TRUE, 5, 0 , 5);


ALTER TABLE public.municipality ADD COLUMN officeparcel_parcelid INTEGER
	CONSTRAINT municipality_parcelid_fk REFERENCES parcel (parcelkey);


-- ******************************* run on LIVE DEPLOYED system up to here *******************************
-- ******************************* run on LOCAL TEST system up to here *******************************


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
    VALUES (40, 'database/patches/dbpatch_beta40.sql', NULL, 'ecd', '');