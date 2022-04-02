
-- PATCH 38: Trimmings from major brain upgrade SB22



CREATE SEQUENCE public.citationfilingtype_typeid_seq
  INCREMENT 1
  MINVALUE 100
  MAXVALUE 9223372036854775807
  START 101
  CACHE 1;
ALTER TABLE public.citationfilingtype_typeid_seq
  OWNER TO sylvia;



CREATE TABLE public.citationfilingtype
(
  typeid integer NOT NULL DEFAULT nextval('citationfilingtype_typeid_seq'::regclass),
  title text NOT NULL,
  description text,
  muni_municode integer NOT NULL,
  active boolean DEFAULT true,
  CONSTRAINT citationfilingtype_pkey PRIMARY KEY (typeid),
  CONSTRAINT citationfilingtype_muni_fk FOREIGN KEY (muni_municode)
      REFERENCES public.municipality (municode) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.citationfilingtype
  OWNER TO sylvia;

ALTER TABLE  citation ADD COLUMN filingtype_typeid INTEGER CONSTRAINT citation_filingtype_fk REFERENCES citationfilingtype (typeid);




INSERT INTO public.citationfilingtype VALUES (100, 'Private Criminal Complaint', NULL, 999, true);
INSERT INTO public.citationfilingtype VALUES (101, 'Non-Traffic Citation', NULL, 999, true);

-- this didn't get in the scripts either
ALTER TABLE occinspection rename column creationts to createdts;


ALTER TABLE public.citationdocketno ADD COLUMN citation_citationid integer;
ALTER TABLE public.citationdocketno
  ADD CONSTRAINT citationdocketno_citationid_fk FOREIGN KEY (citation_citationid)
      REFERENCES public.citation (citationid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;


ALTER TABLE occchecklistspacetypeelement ADD COLUMN notes text;

--ALTER TYPE linkedobjectroleschema ADD VALUE 'CitationDocketHuman';


ALTER TYPE citationviolationstatus ADD VALUE 'FILED';
ALTER TYPE citationviolationstatus ADD VALUE 'AWAITING_PLEA';
ALTER TYPE citationviolationstatus ADD VALUE 'CONTINUED';
ALTER TYPE citationviolationstatus ADD VALUE 'GUILTY';
ALTER TYPE citationviolationstatus ADD VALUE 'NO_CONTEST';
ALTER TYPE citationviolationstatus ADD VALUE 'DISMISSED';
ALTER TYPE citationviolationstatus ADD VALUE 'COMPLIANCE';
ALTER TYPE citationviolationstatus ADD VALUE 'INVALID';
ALTER TYPE citationviolationstatus ADD VALUE 'WITHDRAWN';
ALTER TYPE citationviolationstatus ADD VALUE 'NOT_GUILTY';
ALTER TYPE citationviolationstatus ADD VALUE 'OTHER';





UPDATE noticeofviolation SET notifyingofficer_userid = creationby WHERE notifyingofficer_userid IS NULL;
ALTER TABLE citationstatus ADD COLUMN displayorder INTEGER DEFAULT 1;
ALTER TABLE public.parcelunit ADD COLUMN location_occlocationdescriptor integer;

ALTER TABLE public.parcelunit
  ADD CONSTRAINT parcelunit_loc_locationdescriptionid_fk FOREIGN KEY (location_occlocationdescriptor)
      REFERENCES public.occlocationdescriptor (locationdescriptionid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE public.occperiod ADD COLUMN parcelunit_unitid INTEGER 
    CONSTRAINT occperiod_parcelunitid_fk REFERENCES parcelunit (unitid);







-- DOES NOT RUN ON LIVE SYSTEM, dropping all occ periods insteaad
-- UPDATE pubic.occperiod SET parcelunit_unitid = propertyunit_unitid;

DELETE FROM occinspectedspaceelementphotodoc;
DELETE FROM occinspectedspaceelement;
DELETE FROM occinspectedspace;
DELETE FROM occinspectionphotodoc;
DELETE FROM occinspection;
DELETE FROM occperiodeventrule;
DELETE FROM choiceproposal WHERE occperiod_periodid IS NOT NULL;
DELETE FROM loginobjecthistory WHERE occperiod_periodid IS NOT NULL;
DELETE FROM eventhuman WHERE event_eventid IN (SELECT eventid FROM event WHERE occperiod_periodid IS NOT NULL);
DELETE FROM public.event WHERE occperiod_periodid IS NOT NULL;
DELETE FROM occpermitapplication;
DELETE FROM occperiodphotodoc;
DELETE FROM occperiod;

ALTER TABLE public.occperiod DROP COLUMN propertyunit_unitid;


ALTER TABLE public.occperiod ADD COLUMN deactivatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occperiod ADD COLUMN deactivatedby_userid INTEGER 
    CONSTRAINT occperiod_deactivatedby_fk REFERENCES login (userid);

ALTER TABLE public.occperiod
   ALTER COLUMN parcelunit_unitid SET NOT NULL;

ALTER TABLE public.occperiod DROP COLUMN active;

ALTER TABLE public.parcelmailingaddress ADD CONSTRAINT parcelmailingaddress_mailingaddressid_fk
    FOREIGN KEY (mailingparcel_mailingid) REFERENCES mailingaddress (addressid);

ALTER TABLE public.parcelmailingaddress RENAME COLUMN mailingparcel_parcelid TO parcel_parcelkey;
ALTER TABLE public.parcelmailingaddress RENAME COLUMN mailingparcel_mailingid TO mailingaddress_addressid;

ALTER TABLE public.cecase ADD COLUMN parcel_parcelkey INTEGER
    CONSTRAINT cecase_parcelkey_fk REFERENCES parcel (parcelkey);

ALTER TABLE public.ceactionrequest ADD COLUMN parcel_parcelkey INTEGER
    CONSTRAINT cecase_parcelkey_fk REFERENCES parcel (parcelkey);

-- now copy the propertyid into the parcelID column as long as it exists in parcel. What to do with the nulls?
UPDATE public.cecase SET parcel_parcelkey = property_propertyid
WHERE property_propertyid IN (SELECT parcelkey from parcel);

ALTER TABLE public.cecase
   ALTER COLUMN property_propertyid DROP NOT NULL;

ALTER TABLE public.cecase ADD COLUMN parcelunit_unitid INTEGER 
    CONSTRAINT cecase_parcelunitid_fk REFERENCES parcelunit (unitid);

ALTER TABLE public.cecase DROP COLUMN propertyunit_unitid;



ALTER TABLE public.parcelunit
   ALTER COLUMN unitnumber SET DEFAULT 'DEFAULT';

-- make all the default units by parcel
INSERT INTO public.parcelunit (parcel_parcelkey)
    SELECT parcelkey FROM public.parcel;

UPDATE public.parcelunit SET createdts = now(), lastupdatedts = now(), createdby_userid=99, lastupdatedby_userid=99;
UPDATE public.parcelunit SET source_sourceid=8;


ALTER TABLE citation DROP COLUMN active;

-- CROSS-APPLICABILITY OF OCC INSPECTIONS with CE case side

ALTER TABLE occinspection ADD COLUMN cecase_caseid INTEGER 
    CONSTRAINT occinspection_cecaseid_fk REFERENCES cecase (caseid);

ALTER TABLE public.occinspection
   ALTER COLUMN occperiod_periodid DROP NOT NULL;

-- ******************************* run on REMOTE system up to here *******************************

-- ******************************* run on LOCAL TEST system up to here *******************************






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
    VALUES (39, 'database/patches/dbpatch_beta39.sql',NULL, 'ecd', 'Post SB upgrade');