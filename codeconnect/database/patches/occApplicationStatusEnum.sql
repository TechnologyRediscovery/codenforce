CREATE TYPE occapplicationstatus AS ENUM ('Waiting','NewUnit','OldUnit','Rejected','Invalid');

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
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.occpermitapplicationperson
  OWNER TO sylvia;
