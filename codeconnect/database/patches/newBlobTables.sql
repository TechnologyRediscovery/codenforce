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


--This ALTER TABLE should be implemented at some point, 
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

--Let's make the metadata table

CREATE SEQUENCE IF NOT EXISTS blobmetadata_seq
INCREMENT 1
START 10;

CREATE TABLE public.blobmetadata
(
  metadataid integer NOT NULL DEFAULT nextval('blobmetadata_seq'::regclass),
  blobtype_typeid integer NOT NULL,
  metadatamap bytea,
  CONSTRAINT blobmetadata_pk PRIMARY KEY (metadataid),
  CONSTRAINT blobmetadata_blobtype_fk FOREIGN KEY (blobtype_typeid)
      REFERENCES public.blobtype (typeid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.blobbytes
  OWNER TO sylvia;

--link blobbytes to the metadata

ALTER TABLE blobbytes ADD COLUMN blobmetadata_metadataid integer;

ALTER TABLE blobbytes ADD CONSTRAINT blobbytes_blobmetadata_fk FOREIGN KEY (blobmetadata_metadataid)
      REFERENCES public.blobmetadata (metadataid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;