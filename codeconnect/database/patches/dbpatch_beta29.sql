-- ****************************************************************************
-- PATCH 29
-- "Early August 2020" launch changes

-- ****************************************************************************


ALTER TABLE login ADD COLUMN homemuni INTEGER CONSTRAINT login_homemuni_fk REFERENCES municipality(municode);

ALTER TABLE citation ADD COLUMN officialtext TEXT;

-- FROM NADGIT
-- ****************************************************************************
-- RUN THIS CREATE TYPE BY ITSELF!
-- CREATE TYPE occapplicationstatus AS ENUM ('Waiting','NewUnit','OldUnit','Rejected','Invalid');

-- ****************************************************************************


ALTER TABLE occpermitapplication
ADD status occapplicationstatus;

-- We have to make sure all existing applications have a status

UPDATE occpermitapplication
SET status = 'Waiting'::occapplicationstatus
WHERE applicationid >= 0;

-- A bonus! The externalnotes field!

ALTER TABLE occpermitapplication
ADD externalnotes text;

--Also, here's the occapplicationpersons table

CREATE TABLE public.occpermitapplicationperson
(
  occpermitapplication_applicationid integer NOT NULL,
  person_personid integer NOT NULL,
  applicant boolean,
  preferredcontact boolean,
  applicationpersontype persontype NOT NULL DEFAULT 'Other'::persontype,
  active boolean,
  CONSTRAINT occpermitapplicationperson_comp_pk PRIMARY KEY (occpermitapplication_applicationid, person_personid),
  CONSTRAINT occpermitapplicationperson_applicationid_fk FOREIGN KEY (occpermitapplication_applicationid)
      REFERENCES public.occpermitapplication (applicationid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occpermitapplicationperson_personid_fk FOREIGN KEY (person_personid)
      REFERENCES public.person (personid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


-- Personchange id sequence

CREATE SEQUENCE IF NOT EXISTS personchangeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;



--Person change table

CREATE TABLE public.personchange
(
  personchangeid integer NOT NULL DEFAULT nextval('propertunit_unitid_seq'::regclass),
  person_personid integer,
  firstname text,
  lastname text,
  compositelastname boolean,
  phonecell text,
  phonehome text,
  phonework text,
  email text,
  addressstreet text,
  addresscity text,
  addresszip text,
  addressstate text,
  useseparatemailingaddress boolean,
  mailingaddressstreet text,
  mailingaddresthirdline text,
  mailingaddresscity text,
  mailingaddresszip text,
  mailingaddressstate text,
  removed boolean,
  added boolean,
  entryts timestamp with time zone,
  approvedondate timestamp with time zone,
  approvedby_userid integer,
  changedby_userid integer,
  changedby_personid integer,
  active boolean DEFAULT true,
  CONSTRAINT personchangeid_pk PRIMARY KEY (personchangeid),
  CONSTRAINT personchange_approvedby_fk FOREIGN KEY (approvedby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT personchange_changedbypersonid_fk FOREIGN KEY (changedby_personid)
      REFERENCES public.person (personid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT personchange_changedbyuserid_fk FOREIGN KEY (changedby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT personchange_personpersonid_fk FOREIGN KEY (person_personid)
      REFERENCES public.person (personid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

alter table propertyunitchange drop column rentalintent;

alter table propertyunitchange add column rentalnotes text;


--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (29, 'database/patches/dbpatch_beta29.sql', '08-11-2020', 'ecd', 'early august changes');
