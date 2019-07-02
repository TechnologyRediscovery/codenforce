-- STATUS: Unreleased
-- STATUS: Prototype db: WIP
-- STATUS: Beta db: null


ALTER TABLE public.occupancypermitapplication
  RENAME TO occpermitapplication;

ALTER TABLE occperiod ADD COLUMN createdby_userid INTEGER NOT NULL CONSTRAINT occperiod_createdbyuserid_fk REFERENCES login (userid);


CREATE TABLE public.occperiodpermitapplication
(
  occperiod_periodid integer NOT NULL,
  occpermitapp_applicationid integer NOT NULL,

  CONSTRAINT occperiodpermitapp_comp_pk PRIMARY KEY (occperiod_periodid, occpermitapp_applicationid),
  CONSTRAINT occperiodpermitapp_periodid_fk FOREIGN KEY (occperiod_periodid)
      REFERENCES public.occperiod (periodid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occperiodpermitapp_appid_fk FOREIGN KEY (occpermitapp_applicationid)
      REFERENCES public.occpermitapplication (applicationid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

DROP TABLE propertyunitperson CASCADE;

ALTER TABLE choice RENAME TO eventchoice;

ALTER TABLE eventchoice ADD COLUMN icon_iconid INTEGER CONSTRAINT eventchoice_iconid_fk REFERENCES icon (iconid);
ALTER TABLE eventproposal ADD COLUMN icon_iconid INTEGER CONSTRAINT eventchoice_iconid_fk REFERENCES icon (iconid);

ALTER TABLE occinspection ADD COLUMN passedinspectionts TIMESTAMP WITH TIME ZONE;

-- MONEY MONEY MONEY MONEY MONEY


ALTER TABLE occinspectionfee RENAME TO moneyfee;
ALTER TABLE public.payment
  RENAME TO moneypayment;
ALTER TABLE paymenttype RENAME TO moneypaymenttype;




ALTER TABLE public.moneypayment DROP COLUMN occperiod_periodid;
ALTER TABLE public.moneypayment RENAME COLUMN payerid TO payer_personid;
ALTER TABLE public.moneypayment RENAME COLUMN recordedby TO recordedby_userid;
ALTER TABLE public.moneypayment ALTER COLUMN amount TYPE MONEY;




CREATE SEQUENCE IF NOT EXISTS moneycodesetelementfeeid_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;


CREATE TABLE public.moneycodesetelementfee
(
  codesetelementfeeid				INTEGER NOT NULL DEFAULT nextval('moneycodesetelementfeeid_seq'::regclass) PRIMARY KEY,
  assignedby_userid 				INTEGER CONSTRAINT moneycodesetelementfee_assignedby_fk REFERENCES login (userid),
  assignedbyts 						TIMESTAMP WITH TIME ZONE,
  fee_feeid 						INTEGER NOT NULL CONSTRAINT moneycodesetelefee_feeid_fk REFERENCES moneyfee (feeid),		
  codesetelement_elementid 			INTEGER NOT NULL CONSTRAINT moneycodesetelefee_cdseteleid_fk REFERENCES codesetelement (codesetelementid),
  waivedby_userid		 			INTEGER CONSTRAINT moneycodesetelementfee_wavedbyuserid_fk REFERENCES login (userid),
  lastmodifiedts 					TIMESTAMP WITH TIME ZONE,
  reduceby 							MONEY,
  reduceby_userid					INTEGER CONSTRAINT moneycodesetelementfee_reducedbyuserid_fk REFERENCES login (userid),
  notes 							text
);


CREATE SEQUENCE IF NOT EXISTS moneyoccperiodtypefeeid_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;


CREATE TABLE public.moneyoccperiodtypefee
(
  occperiodtypefeeid				INTEGER NOT NULL DEFAULT nextval('moneyoccperiodtypefeeid_seq'::regclass) PRIMARY KEY,
  assignedby_userid 				INTEGER CONSTRAINT moneyoccperiodtypefee_assignedby_fk REFERENCES login (userid),
  assignedbyts 						TIMESTAMP WITH TIME ZONE,
  fee_feeid 						INTEGER NOT NULL CONSTRAINT moneyoccperiodtypefee_feeid_fk REFERENCES moneyfee (feeid),		
  occperiodtype_typeid 				INTEGER NOT NULL CONSTRAINT moneyoccperiodtypefee_typeid_fk REFERENCES occperiodtype (typeid),
  waivedby_userid		 			INTEGER CONSTRAINT moneyoccperiodtypefee_wavedbyuserid_fk REFERENCES login (userid),
  lastmodifiedts 					TIMESTAMP WITH TIME ZONE,
  reduceby 							MONEY,
  reduceby_userid					INTEGER CONSTRAINT moneyoccperiodtypefee_reducedbyuserid_fk REFERENCES login (userid),
  notes 							text
);



CREATE TABLE public.moneyoccpermittypefeepayment
(
	occperiod_periodid 				INTEGER NOT NULL,
	payment_paymentid 				INTEGER NOT NULL,
	occperiodtypefee_pertypefeeid	INTEGER NOT NULL,
	CONSTRAINT moneyoccpermittypefeepayment_comp_pk PRIMARY KEY (occperiod_periodid, payment_paymentid, occperiodtypefee_pertypefeeid)
);



CREATE TABLE public.moneycodesetelementfeepayment
(
	codesetelementfee_elefeeid 		INTEGER NOT NULL,
	payment_paymentid 				INTEGER NOT NULL,
	codeviolation_violationid 		INTEGER NOT NULL,
	CONSTRAINT moneycodesetelementfeepayment_comp_pk PRIMARY KEY (codesetelementfee_elefeeid, payment_paymentid, codeviolation_violationid)
);



--- END MONEY





-- reference:
-- ALTER TABLE public.occinspectedchecklistspaceelement ADD COLUMN lastinspectedby_userid INTEGER CONSTRAINT occinspectedchklstspel_lastinspecby_userid_fk REFERENCES public.login (userid);




--***************************************
--  add reasons and their type proposal mappings here
--***************************************
--***************************************




-- TODO
-- run this after proposals
ALTER TABLE public.occpermitapplicationreason
   ALTER COLUMN periodtypeproposal_periodid SET NOT NULL;





INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (13, 'database/patches/dbpatch_beta13.sql', '05-05-2019', 'ecd', 'occ permit application revisions');

