

CREATE OR REPLACE FUNCTION public.createghostperson(
    person_row person,
    userid integer)
  RETURNS integer AS
$BODY$

DECLARE
	newpersonid integer;

BEGIN
       

	INSERT INTO public.person(
		    personid, persontype, muni_municode, fname, lname, jobtitle, 
		    phonecell, phonehome, phonework, email, address_street, address_city, 
		    address_state, address_zip, notes, lastupdated, expirydate, isactive, 
		    isunder18, humanverifiedby, compositelname, sourceid, creator, 
		    businessentity, mailing_address_street, mailing_address_city, 
		    mailing_address_zip, mailing_address_state, useseparatemailingaddr, 
		    expirynotes, creationtimestamp, canexpire, userlink, mailing_address_thirdline, 
            ghostof, ghostby, ghosttimestamp, cloneof, clonedby, clonetimestamp, 
            referenceperson)
	    VALUES (DEFAULT, person_row.persontype, person_row.muni_municode, person_row.fname, person_row.lname, person_row.jobtitle, 
		    person_row.phonecell, person_row.phonehome, person_row.phonework, person_row.email, person_row.address_street, person_row.address_city, 
		    person_row.address_state, person_row.address_zip, person_row.notes, now(), NULL, TRUE, 
		    person_row.isunder18, NULL, person_row.compositelname, person_row.sourceid , person_row.creator, 
		    person_row.businessentity, person_row.mailing_address_street, person_row.mailing_address_city, 
		    person_row.mailing_address_zip, person_row.mailing_address_state, person_row.useseparatemailingaddr, 
		    person_row.expirynotes, person_row.creationtimestamp, person_row.canexpire, person_row.userlink, person_row.mailing_address_thirdline,
		    person_row.personid, userid, now(), NULL, NULL, NULL,
		    NULL);

	    newpersonid :=currval('person_personidseq');

	    RETURN newpersonid;

END;

$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;




CREATE OR REPLACE FUNCTION public.createcloneperson(person_row person, userid integer)
  RETURNS integer AS
$BODY$

DECLARE
	newpersonid integer;

BEGIN
       

	INSERT INTO public.person(
		    personid, persontype, muni_municode, fname, lname, jobtitle, 
		    phonecell, phonehome, phonework, email, address_street, address_city, 
		    address_state, address_zip, notes, lastupdated, expirydate, isactive, 
		    isunder18, humanverifiedby, compositelname, sourceid, creator, 
		    businessentity, mailing_address_street, mailing_address_city, 
		    mailing_address_zip, mailing_address_state, useseparatemailingaddr, 
		    expirynotes, creationtimestamp, canexpire, userlink, mailing_address_thirdline, 
            ghostof, ghostby, ghosttimestamp, cloneof, clonedby, clonetimestamp, 
            referenceperson)
	    VALUES (DEFAULT, person_row.persontype, person_row.muni_municode, person_row.fname, person_row.lname, person_row.jobtitle, 
		    person_row.phonecell, person_row.phonehome, person_row.phonework, person_row.email, person_row.address_street, person_row.address_city, 
		    person_row.address_state, person_row.address_zip, person_row.notes, now(), NULL, TRUE, 
		    person_row.isunder18, NULL, person_row.compositelname, person_row.sourceid , person_row.creator, 
		    person_row.businessentity, person_row.mailing_address_street, person_row.mailing_address_city, 
		    person_row.mailing_address_zip, person_row.mailing_address_state, person_row.useseparatemailingaddr, 
		    person_row.expirynotes, person_row.creationtimestamp, person_row.canexpire, person_row.userlink, person_row.mailing_address_thirdline,
		    NULL, NULL, NULL, person_row.personid, userid, now(),
		    FALSE);

	    newpersonid :=currval('person_personidseq');

	    RETURN newpersonid;

END;
$BODY$
  LANGUAGE plpgsql VOLATILE;

  -- Forgot FK in citationviolation table
ALTER TABLE citationviolation ADD CONSTRAINT citationviolation_violationid_fk FOREIGN KEY (codeviolation_violationid)
      REFERENCES public.codeviolation (violationid);

 -- Facelift of NOVs
 CREATE TABLE public.noticeofviolationcodeviolation
(
  	noticeofviolation_noticeid integer NOT NULL,
  	codeviolation_violationid integer NOT NULL,
	includeordtext BOOLEAN DEFAULT TRUE,
	includehumanfriendlyordtext BOOLEAN DEFAULT FALSE,
	includeviolationphoto BOOLEAN DEFAULT FALSE,

  CONSTRAINT noticeofviolationcodeviolation_pk PRIMARY KEY (noticeofviolation_noticeid, codeviolation_violationid),
  CONSTRAINT codeviolation_violationid_fk FOREIGN KEY (codeviolation_violationid)
      REFERENCES public.codeviolation (violationid),
  CONSTRAINT noticeofviolation_noticeid_fk FOREIGN KEY (noticeofviolation_noticeid)
      REFERENCES public.noticeofviolation (noticeid)
);


DROP TABLE public.noticeofviolation;

CREATE TABLE public.noticeofviolation
(
  noticeid integer NOT NULL DEFAULT nextval('noticeofviolation_noticeid_seq'::regclass),
  caseid integer NOT NULL,
  lettertextbeforeviolations text,
  creationtimestamp timestamp with time zone NOT NULL,
  dateofrecord timestamp with time zone NOT NULL,
  sentdate timestamp with time zone,
  returneddate timestamp with time zone,
  personid_recipient integer NOT NULL,
  lettertextafterviolations text,
  lockedandqueuedformailingdate timestamp with time zone,
  lockedandqueuedformailingby integer,
  sentby integer,
  returnedby integer,
  notes text,
  CONSTRAINT noticeviolation_noticeid_pk PRIMARY KEY (noticeid),
  CONSTRAINT "noticeOfViolation_recipient_fk" FOREIGN KEY (personid_recipient)
      REFERENCES public.person (personid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT noticeofviolationcaseid_fk FOREIGN KEY (caseid)
      REFERENCES public.cecase (caseid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT nov_lockedandqueued_fk FOREIGN KEY (personid_recipient)
      REFERENCES public.person (personid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT nov_returnedby_fk FOREIGN KEY (personid_recipient)
      REFERENCES public.person (personid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT nov_sentby_fk FOREIGN KEY (personid_recipient)
      REFERENCES public.person (personid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.noticeofviolation
  OWNER TO sylvia;

-- Index: public."fki_noticeOfViolation_recipient_fk"

-- DROP INDEX public."fki_noticeOfViolation_recipient_fk";

CREATE INDEX "fki_noticeOfViolation_recipient_fk"
  ON public.noticeofviolation
  USING btree
  (personid_recipient);


INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (12, 'database/patches/dbpatch_beta12.sql', '05-20-2019', 'ecd', 'ghost and clone creator functions');

