
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

-- *******************************
-- run on remote system up to here
-- *******************************

ALTER TABLE citation DROP COLUMN active;


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