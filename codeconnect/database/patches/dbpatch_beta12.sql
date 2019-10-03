

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


--ALTER TABLE citationviolation ADD CONSTRAINT citationviolation_violationid_fk FOREIGN KEY (codeviolation_violationid)
--      REFERENCES public.codeviolation (violationid);

ALTER TABLE citationviolation ADD CONSTRAINT citationviolation_citation_fk FOREIGN KEY (citation_citationid)
      REFERENCES public.citation (citationid);

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
  creationby integer,
  CONSTRAINT noticeviolation_noticeid_pk PRIMARY KEY (noticeid),

  CONSTRAINT noticeofviolationcaseid_fk FOREIGN KEY (caseid)
      REFERENCES public.cecase (caseid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,

  CONSTRAINT nov_lockedandqueued_fk FOREIGN KEY (personid_recipient)
      REFERENCES person (personid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,

  CONSTRAINT nov_creationby_userid_fk FOREIGN KEY (creationby)
      REFERENCES login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION, 

  CONSTRAINT nov_returnedby_fk FOREIGN KEY (lockedandqueuedformailingby)
      REFERENCES login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,

  CONSTRAINT nov_sentby_fk FOREIGN KEY (sentby)
      REFERENCES login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,

  CONSTRAINT nov_lockedandqueuedby_userid_fk FOREIGN KEY (lockedandqueuedformailingby)
      REFERENCES login (userid) MATCH SIMPLE
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

ALTER TABLE codeviolation ADD COLUMN createdby INTEGER;
ALTER TABLE codeviolation ADD CONSTRAINT violation_creationby_userid_fk FOREIGN KEY ( createdby ) REFERENCES login ( userid ) ;


CREATE TABLE muniphotodoc
  (
    photodoc_photodocid INTEGER NOT NULL ,
    muni_municode INTEGER NOT NULL
  ) ;

ALTER TABLE muniphotodoc ADD CONSTRAINT muniphotodoc_pk PRIMARY KEY ( photoDoc_photoDocID, muni_municode ) ;
ALTER TABLE muniphotodoc ADD CONSTRAINT muniphotodoc_pdid_fk FOREIGN KEY (photodoc_photoDocID) REFERENCES photodoc (photoDocID);
ALTER TABLE muniphotodoc ADD CONSTRAINT muniphotodoc_muni_fk FOREIGN KEY (muni_municode) REFERENCES municipality (municode);

-- NOVs need a header image
ALTER TABLE noticeofviolation ADD COLUMN headerimage_pdid INTEGER;
ALTER TABLE noticeofviolation ADD CONSTRAINT nov_headerimage_fk FOREIGN KEY (headerimage_pdid) references photodoc (photodocid); 



CREATE SEQUENCE IF NOT EXISTS printstyle_seq
	    START WITH 1000
	    INCREMENT BY 1
	    MINVALUE 1000
	    NO MAXVALUE
	    CACHE 1;

DROP TABLE printstyle;

CREATE TABLE printstyle
(
	styleid							INTEGER DEFAULT nextval('printstyle_seq') 
									CONSTRAINT paramsid_pk PRIMARY KEY,
	description						TEXT,
	headerimage_photodocid			INTEGER CONSTRAINT printstyle_headerimage_pdid_fk REFERENCES photodoc (photoDocID),
	headerheight					INTEGER,
	novtopmargin 					INTEGER,
	novaddresseleftmargin			INTEGER,
	novaddressetopmargin			INTEGER,
	browserheadfootenabled			BOOLEAN DEFAULT FALSE,
	novtexttopmargin				INTEGER
	
);


ALTER TABLE municipality ADD COLUMN novprintstyle_styleid INTEGER;
ALTER TABLE municipality ADD CONSTRAINT muni_printstyleid_fk FOREIGN KEY (novprintstyle_styleid) REFERENCES printstyle (styleid);

ALTER TABLE noticeofviolation ADD COLUMN printstyle_styleid INTEGER;
ALTER TABLE noticeofviolation ADD CONSTRAINT nov_printstyleid_fk FOREIGN KEY (printstyle_styleid) REFERENCES printstyle (styleid);

ALTER TABLE noticeofviolation DROP COLUMN headerimage_pdid;

ALTER SEQUENCE IF EXISTS person_personidseq RESTART WITH 110000;

-- from Matt Scott work
-- need on both servers 
ALTER TABLE person ADD COLUMN rawtext TEXT;

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (12, 'database/patches/dbpatch_beta12.sql', '05-20-2019', 'ecd', 'ghost and clone creator functions');

