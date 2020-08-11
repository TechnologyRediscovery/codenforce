BEGIN;

-- Personchange id sequence

CREATE SEQUENCE IF NOT EXISTS personchangeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

COMMIT;

BEGIN;

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
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.personchange
  OWNER TO sylvia;

COMMIT;