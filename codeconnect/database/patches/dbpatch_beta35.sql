-- ****************************************************************************
-- PATCH 35
-- "Early Jan 2021" humization changes

-- *************


INSERT INTO public.icon(
            iconid, name, styleclass, fontawesome, materialicons)
    VALUES (32, 'nullified', 'violation-nullified', 'fa fa-window-close-o', 'close');





ALTER TABLE public.codeviolation ADD COLUMN bobsource_sourceid INTEGER CONSTRAINT codeviolation_bobsource_fk REFERENCES bobsource (sourceid);




CREATE TABLE public.cecasephotodoc
(
  photodoc_photodocid integer NOT NULL,
  cecase_caseid integer NOT NULL,
  CONSTRAINT cecasephotodoc_pk PRIMARY KEY ( photodoc_photodocid, cecase_caseid),
  CONSTRAINT cecaseiolationphotodoc_cv_fk FOREIGN KEY (cecase_caseid)
      REFERENCES public.cecase (caseid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cecasephotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid)
      REFERENCES public.photodoc (photodocid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.codeviolationphotodoc
  OWNER TO sylvia;

  --NOTE: this file should not all be ran at once. Run this half first, then follow the comment's directions.
ALTER TABLE photodoctype
RENAME TO blobtype;

CREATE SEQUENCE IF NOT EXISTS blobbytes_seq
INCREMENT 1
START 10;



CREATE TABLE public.blobbytes
(
  bytesid integer NOT NULL DEFAULT nextval('blobbytes_seq'::regclass),
  createdts timestamp with time zone,
  blob bytea,
  uploadedby_userid integer,
  filename text,
  CONSTRAINT blobbytes_pk PRIMARY KEY (bytesid),
  CONSTRAINT blobbytes_uploadedby_fk FOREIGN KEY (uploadedby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.blobbytes
  OWNER TO sylvia;





--This statement should copy some of photodoc's columns into blob

INSERT INTO blobbytes(uploaddate, blobtype_typeid, blob)
SELECT photodocdate, photodoctype_typeid, photodocblob FROM photodoc;

--DOUBLE CHECK THE VALUES REALLY DID COPY CORRECTLY BEFORE RUNNING THE REST OF THIS FILE

ALTER TABLE public.photodoc ADD COLUMN blobbytes_bytesid integer;

--Attach the two tables. The where clauses match blobs with their bytes.

UPDATE photodoc
SET blobbytes_bytesid = blobbytes.bytesid
FROM blobbytes
WHERE photodoc.photodocblob = blobbytes.blob 
    AND photodoc.photodocdate = blobbytes.uploaddate;

--In order to make sure the two tables connected correctly, it's best to do a SELECT statement on photodoc and ORDER BY blobbytes_bytesid.
--For some reason the bytes aren't always copied in order, so it might appear as if certain rows are skipped even if they're not.
--On my copy of the live DB, all rows were properly copied and attached.

--This commented out ALTER TABLE should be implemented at some point, 
--but on my machine certain rows couldn't be linked with their bytes.
--It seems as if rows without filenames are the only ones having the problem.
--I'm leaving this note here and the statement because I think my DB
--didn't get all the files when I downloaded the brain into it.

--ALTER TABLE public.photodoc ALTER COLUMN blobbytes_bytesid SET NOT NULL;

ALTER TABLE public.photodoc
  ADD CONSTRAINT photodoc_blobbytes_fk FOREIGN KEY (blobbytes_bytesid)
      REFERENCES public.blobbytes (bytesid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

--MAKE SURE THAT THE TWO TABLES ARE LINKED TO THE RIGHT BLOBS

--drop redundant columns

ALTER TABLE photodoc
DROP COLUMN photodocdate,
DROP COLUMN photodoctype_typeid,
DROP COLUMN photodocblob;



--Let's add metadata to the blobbytes table
ALTER TABLE public.blobbytes ADD COLUMN metadatamap bytea;



--We need to be able to filter blobs by muni

ALTER TABLE public.photodoc ADD COLUMN muni_municode integer;

ALTER TABLE public.photodoc
  ADD CONSTRAINT photodoc_municode_fk FOREIGN KEY (muni_municode)
      REFERENCES public.municipality (municode) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;

--We will now get the muni code by linking tables together until we find one.

--action request.muni

UPDATE photodoc
SET muni_municode = ceactionrequest.muni_municode
FROM ceactionrequestphotodoc, ceactionrequest
WHERE photodoc.photodocid = ceactionrequestphotodoc.photodoc_photodocid 
    AND ceactionrequestphotodoc.ceactionrequest_requestid = ceactionrequest.requestid;

--codeviolation -> cecase -> property.muni

UPDATE photodoc
SET muni_municode = property.municipality_municode
FROM codeviolationphotodoc, codeviolation, cecase, property
WHERE photodoc.photodocid = codeviolationphotodoc.photodoc_photodocid 
    AND codeviolationphotodoc.codeviolation_violationid = codeviolation.violationid
    AND codeviolation.cecase_caseid = cecase.caseid
    AND cecase.property_propertyid = property.propertyid
    AND property.municipality_municode IS NOT NULL;

--Municipality.muni

UPDATE photodoc
SET muni_municode = muniphotodoc.muni_municode
FROM muniphotodoc
WHERE photodoc.photodocid = muniphotodoc.photodoc_photodocid;

--occinspectedspaceelement -> occinspectedspace -> occinspection -> occperiod -> propertyunit -> property.muni

UPDATE photodoc
SET muni_municode = property.municipality_municode
FROM occinspectedspaceelementphotodoc, occinspectedspaceelement, occinspectedspace, occinspection, occperiod, propertyunit, property
WHERE photodoc.photodocid = occinspectedspaceelementphotodoc.photodoc_photodocid 
    AND occinspectedspaceelementphotodoc.inspectedspaceelement_elementid = occinspectedspaceelement.inspectedspaceelementid
    AND occinspectedspaceelement.inspectedspace_inspectedspaceid = occinspectedspace.inspectedspaceid
    AND occinspectedspace.occinspection_inspectionid = occinspection.inspectionid
    AND occinspection.occperiod_periodid = occperiod.periodid
    AND occperiod.propertyunit_unitid = propertyunit.unitid
    AND propertyunit.property_propertyid = property.propertyid
    AND property.municipality_municode IS NOT NULL;

--occperiod -> propertyunit -> property.muni

UPDATE photodoc
SET muni_municode = property.municipality_municode
FROM occperiodphotodoc, occperiod, propertyunit, property
WHERE photodoc.photodocid = occperiodphotodoc.photodoc_photodocid 
    AND occperiodphotodoc.occperiod_periodid = occperiod.periodid
    AND occperiod.propertyunit_unitid = propertyunit.unitid
    AND propertyunit.property_propertyid = property.propertyid
    AND property.municipality_municode IS NOT NULL;

--property.muni

UPDATE photodoc
SET muni_municode = property.municipality_municode
FROM propertyphotodoc, property
WHERE photodoc.photodocid = propertyphotodoc.photodoc_photodocid 
    AND propertyphotodoc.property_propertyid = property.propertyid
    AND property.municipality_municode IS NOT NULL;


--pdfdoc table

--Uses the photodoc sequence on purpose, as we don't want there to be any collisions between PDF blobs and Photo blobs.

-- CREATE TABLE public.pdfdoc
-- (
--   pdfdocid integer NOT NULL DEFAULT nextval('photodoc_photodocid_seq'::regclass),
--   pdfdocdescription character varying(100),
--   pdfdoccommitted boolean DEFAULT true,
--   blobbytes_bytesid integer,
--   muni_municode integer,
--   CONSTRAINT pdfdoc_pk PRIMARY KEY (pdfdocid),
--   CONSTRAINT pdfdoc_blobbytes_fk FOREIGN KEY (blobbytes_bytesid)
--       REFERENCES public.blobbytes (bytesid) MATCH SIMPLE
--       ON UPDATE NO ACTION ON DELETE NO ACTION,
--   CONSTRAINT pdfdoc_municode_fk FOREIGN KEY (muni_municode)
--       REFERENCES public.municipality (municode) MATCH SIMPLE
--       ON UPDATE NO ACTION ON DELETE NO ACTION
-- )
-- WITH (
--   OIDS=FALSE
-- );
-- ALTER TABLE public.photodoc
--   OWNER TO sylvia;

-- New linking tables

CREATE TABLE public.ceactionrequestpdfdoc
(
  pdfdoc_pdfdocid integer NOT NULL,
  ceactionrequest_requestid integer NOT NULL,
  CONSTRAINT ceactionrequestpdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, ceactionrequest_requestid),
  CONSTRAINT ceactionrequestpdfdoc_cear_fk FOREIGN KEY (ceactionrequest_requestid)
      REFERENCES public.ceactionrequest (requestid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT ceactionrequestpdfdoc_pdfdoc_fk FOREIGN KEY (pdfdoc_pdfdocid)
      REFERENCES public.pdfdoc (pdfdocid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.ceactionrequestpdfdoc
  OWNER TO sylvia;


CREATE TABLE public.codeviolationpdfdoc
(
  pdfdoc_pdfdocid integer NOT NULL,
  codeviolation_violationid integer NOT NULL,
  CONSTRAINT codeviolationpdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, codeviolation_violationid),
  CONSTRAINT codeviolationpdfdoc_cv_fk FOREIGN KEY (codeviolation_violationid)
      REFERENCES public.codeviolation (violationid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT codeviolationpdfdoc_pdfdoc_fk FOREIGN KEY (pdfdoc_pdfdocid)
      REFERENCES public.pdfdoc (pdfdocid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.codeviolationpdfdoc
  OWNER TO sylvia;


CREATE TABLE public.munipdfdoc
(
  pdfdoc_pdfdocid integer NOT NULL,
  muni_municode integer NOT NULL,
  CONSTRAINT munipdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, muni_municode),
  CONSTRAINT munipdfdoc_muni_fk FOREIGN KEY (muni_municode)
      REFERENCES public.municipality (municode) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT munipdfdoc_pdid_fk FOREIGN KEY (pdfdoc_pdfdocid)
      REFERENCES public.pdfdoc (pdfdocid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.munipdfdoc
  OWNER TO sylvia;


CREATE TABLE public.occinspectedspaceelementpdfdoc
(
  pdfdoc_pdfdocid integer NOT NULL,
  inspectedspaceelement_elementid integer NOT NULL,
  CONSTRAINT inspchklstspelepdf_pk PRIMARY KEY (pdfdoc_pdfdocid, inspectedspaceelement_elementid),
  CONSTRAINT occinspectedspaceelementpdfdoc_inspectedele_fk FOREIGN KEY (inspectedspaceelement_elementid)
      REFERENCES public.occinspectedspaceelement (inspectedspaceelementid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occinspectedspaceelementpdfdoc_pdfdocid_fk FOREIGN KEY (pdfdoc_pdfdocid)
      REFERENCES public.pdfdoc (pdfdocid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.occinspectedspaceelementpdfdoc
  OWNER TO sylvia;


CREATE TABLE public.occperiodpdfdoc
(
  pdfdoc_pdfdocid integer NOT NULL,
  occperiod_periodid integer NOT NULL,
  CONSTRAINT occperiodpdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, occperiod_periodid),
  CONSTRAINT occperiodpdfdoc__occperiod_fk FOREIGN KEY (occperiod_periodid)
      REFERENCES public.occperiod (periodid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occperiodpdfdoc_pdfdoc_fk FOREIGN KEY (pdfdoc_pdfdocid)
      REFERENCES public.pdfdoc (pdfdocid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.occperiodpdfdoc
  OWNER TO sylvia;


-- CREATE TABLE public.parcelpdfdoc
-- (
--   pdfdoc_pdfdocid integer NOT NULL,
--   parcel_parcelkey integer NOT NULL,
--   CONSTRAINT parcelpdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, parcel_parcelkey),
--   CONSTRAINT parcelpdfdoc_cv_fk FOREIGN KEY (parcel_parcelkey)
--       REFERENCES public.parcel (parcelkey) MATCH SIMPLE
--       ON UPDATE NO ACTION ON DELETE NO ACTION,
--   CONSTRAINT parcelpdfdoc_phdoc_fk FOREIGN KEY (pdfdoc_pdfdocid)
--       REFERENCES public.pdfdoc (pdfdocid) MATCH SIMPLE
--       ON UPDATE NO ACTION ON DELETE NO ACTION
-- )
-- WITH (
--   OIDS=FALSE
-- );
-- ALTER TABLE public.parcelpdfdoc
--   OWNER TO sylvia;


CREATE TABLE public.parcelphotodoc
(
  photodoc_photodocid integer NOT NULL,
  parcel_parcelkey integer NOT NULL,
  CONSTRAINT parcelphotodoc_pk PRIMARY KEY (photodoc_photodocid, parcel_parcelkey),
  CONSTRAINT parcelphotodoc_cv_fk FOREIGN KEY (parcel_parcelkey)
      REFERENCES public.parcel (parcelkey) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT parcelphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid)
      REFERENCES public.photodoc (photodocid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.parcelphotodoc
  OWNER TO sylvia;


-- CREATE TABLE public.propertypdfdoc
-- (
--   pdfdoc_pdfdocid integer NOT NULL,
--   property_propertyid integer NOT NULL,
--   CONSTRAINT propertypdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, property_propertyid),
--   CONSTRAINT propertypdfdoc_pdid_fk FOREIGN KEY (pdfdoc_pdfdocid)
--       REFERENCES public.pdfdoc (pdfdocid) MATCH SIMPLE
--       ON UPDATE NO ACTION ON DELETE NO ACTION,
--   CONSTRAINT propertypdfdoc_prop_fk FOREIGN KEY (property_propertyid)
--       REFERENCES public.property (propertyid) MATCH SIMPLE
--       ON UPDATE NO ACTION ON DELETE NO ACTION
-- )
-- WITH (
--   OIDS=FALSE
-- );
-- ALTER TABLE public.propertypdfdoc
--   OWNER TO sylvia;


-- RUN ON LOCAL BRAIN UP TO HERE`


ALTER TABLE noticeofviolation ADD COLUMN notifyingofficer_userid INTEGER 
    CONSTRAINT nov_notifyingofficer_fk 
    REFERENCES login (userid);




CREATE OR REPLACE FUNCTION generateRand(low INT, high INT)
    RETURNS INT AS
$$
   BEGIN
      RETURN floor(random() * (high-low + 1) + low);
   END
$$ language 'plpgsql' STRICT;

--Alright, let's get started
-- RUN ON REMOTE UP TO HERE EXCEPT for blob migration

ALTER TABLE public.occpermitapplication ADD COLUMN applicationpubliccc integer;

UPDATE public.occpermitapplication SET applicationpubliccc = generateRand(100000,999999) WHERE applicationid > 0;

ALTER TABLE public.occpermitapplication ALTER COLUMN applicationpubliccc SET NOT NULL;

ALTER TABLE public.occpermitapplication ADD COLUMN paccenabled boolean;
ALTER TABLE public.occpermitapplication ALTER COLUMN paccenabled SET DEFAULT true;

ALTER TABLE public.occpermitapplication ADD COLUMN allowuplinkaccess boolean;
ALTER TABLE public.occpermitapplication ALTER COLUMN allowuplinkaccess SET DEFAULT true;



-- NOTE on new brain SpringBreak2022: 
-- NOTICE:  constraint "photodoc_blobbytes_fk" of relation "photodoc" does not exist, skipping
-- Query returned successfully with no result in 42 msec.

ALTER TABLE photodoc DROP CONSTRAINT IF EXISTS photodoc_blobbytes_fk;


--IF datepublished IS NULL the patch is still open and receiving changes
-- Published during new brain SB22
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (35, 'database/patches/dbpatch_beta35.sql','03-10-2022', 'ecd', 'Early JAN 2021 changes');
