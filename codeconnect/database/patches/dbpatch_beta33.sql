-- ****************************************************************************
-- PATCH 33
-- "LATE OCT 2020" launch changes

-- *************

ALTER TABLE public.codeviolation
    ADD COLUMN nullifiedts TIMESTAMP WITH TIME ZONE;

ALTER TABLE public.codeviolation
    ADD COLUMN nullifiedby INTEGER CONSTRAINT codeviolation_nullifiedby_fk REFERENCES login(userid);


-- RUN ON REMOTE TO HERE
-- RUN LOCALLY TO HERE

ALTER TABLE textblock 
    ADD COLUMN injectabletemplate BOOLEAN DEFAULT FALSE;

ALTER TABLE noticeofviolation 
    ADD COLUMN injectviolations BOOLEAN DEFAULT FALSE;


INSERT INTO public.textblockcategory(
            categoryid, categorytitle, icon_iconid, muni_municode)
    VALUES (101, "Injectable Template", 10, 999);


-- COMMENTS FROM SNAPPER
-- Person column Ghosts and clones don't seem to make much sense
-- ECD: Ghost (static copy never to change for archival purposes)
-- Serializing a record to a single cell or separate table, instead 
-- of the more complicated recursive foreign key to a copy record
-- Collapse first and last name
-- Dealing with edge case: single phsical location with same street
-- name but different suffix (apple st. and apple ave)

-- design criteria
-- A single parcel can contain multiple mailing unit (i.e. 1114, 1116, 1118 

-- Largo Way, also had 1115-1121 Morrell ave)
-- Accommodtate a single address (930 Punta Gorda) that contains multiple

-- units (which are also mailing units) but their distinction is 
-- by unit number (not street address), suffix of street address

-- A single person (A legal person entity) can be associated with multiple
-- mailing addresses, each of which could be in a different role (i.e. owner
-- owner of parcel at address X, receives tax mailing at address Y, 
-- and lives at address Z)
-- Needs: Person deceased date

-- ASSUMPTION: an address listed as 1115-1121 would be interpolated only as 
-- the odds: 1115, 1117, 1119, 1121. SEAN: Is this accurate?

-- TODO: defined workflow for pyparcelupdate info update by month (step-by-step, 
-- get data from where, do what with it, store the result where?, 
-- compare to what, made determination, etc.)
-- This will involve clones since changes are written to clones, not
-- the "real" person
-- TODO: Deal with the WPRDC dump that stores the second
-- part of an addrss range in field called PROPERTYFRACTION

CREATE TABLE parcel;
parcelid
-- code enf case is still based on a parcel

CREATE TABLE parcelperson;
FK parcel
FK person
TIMESTAMP
role

CREATE TABLE person;
name
dob
under18
recordexpirydate
deceaseddate
-- Clone defined; a person record with a non-null FK 
-- to a nother person record
FK personclone
-- TODO: document cloning


CREATE TABLE mailingaddressperson
FK to mailingaddress
FK to person
associationrole
creationts

CREATE TABLE mailingaddressparcel
FK to parcel
FK to mailingaddress
creationts

CREATE TABLE mailingaddress;
-- current
address1
address2
city
state
zip

-- option 2
housenum
streetname
unitno
city
state
zip

-- four fields in the WPRDC API, map to "mailingaddress"
changenoticeaddress1
changenoticeaddress2
changenoticeaddress3
changenoticeaddress4

-- optionbrainstorm
FK to street


CREATE TABLE parcelunit;
FK parcel
unitno
intenttorent
--TODO: The field Use Code can contain APART:x-y UNITS


-- this is closesed to current propertyunit (occ is based on one of these)



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (33, 'database/patches/dbpatch_beta33.sql',NULL, 'ecd', 'LATE oct 2020 changes');
