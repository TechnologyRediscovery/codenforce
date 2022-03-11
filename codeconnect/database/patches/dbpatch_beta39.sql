
-- PATCH 38: Trimmings from major brain upgrade SB22



-- run on remote system up to here

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


-- extra gunk


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