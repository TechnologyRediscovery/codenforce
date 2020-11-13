--NOTE: this file should not all be ran at once. Run this half first, then follow the comment's directions.
ALTER TABLE photodoctype
RENAME TO blobtype;

CREATE SEQUENCE IF NOT EXISTS blobbytes_seq
INCREMENT 1
START 10;

CREATE TABLE public.blobbytes
(
  bytesid integer NOT NULL DEFAULT nextval('blobbytes_seq'::regclass),
  uploaddate timestamp with time zone,
  blobtype_typeid integer NOT NULL,
  blob bytea,
  uploadpersonid integer,
  filename text,
  CONSTRAINT blobbytes_pk PRIMARY KEY (bytesid),
  CONSTRAINT blobytes_blobtype_fk FOREIGN KEY (blobtype_typeid)
      REFERENCES public.blobtype (typeid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.blobbytes
  OWNER TO sylvia;

--This statement should copy some of photodoc's columns into blob

INSERT INTO blobbytes(uploaddate, blobtype_typeid, blob, uploadpersonid, filename)
SELECT photodocdate, photodoctype_typeid, photodocblob, photodocuploadpersonid, photodocfilename FROM photodoc;

--DOUBLE CHECK THE VALUES REALLY DID COPY CORRECTLY BEFORE RUNNING THE REST OF THIS FILE

ALTER TABLE public.photodoc ADD COLUMN blobbytes_bytesid integer;

--Attach the two tables. The where clauses match blobs with their bytes.

UPDATE photodoc
SET blobbytes_bytesid = blobbytes.bytesid
FROM blobbytes
WHERE photodoc.photodocblob = blobbytes.blob 
	AND photodoc.photodocdate = blobbytes.uploaddate
	AND photodoc.photodocfilename = blobbytes.filename;


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
DROP COLUMN photodocblob,
DROP COLUMN photodocuploadpersonid,
DROP COLUMN photodocfilename;

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