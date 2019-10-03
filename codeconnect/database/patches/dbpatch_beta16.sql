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

CREATE SEQUENCE IF NOT EXISTS moneyfeeassignedid_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;

  CREATE TABLE public.moneyfeeassigned
  (
	
  	moneyfeeassignedid 				INTEGER NOT NULL DEFAULT nextval('moneyfeeassignedid_seq') PRIMARY KEY,
	assignedby_userid 				INTEGER CONSTRAINT moneycodesetelementfee_assignedby_fk REFERENCES login (userid),
  	assignedbyts 					TIMESTAMP WITH TIME ZONE,
  	waivedby_userid		 			INTEGER CONSTRAINT moneycodesetelementfee_wavedbyuserid_fk REFERENCES login (userid),
  	lastmodifiedts 					TIMESTAMP WITH TIME ZONE,
  	reduceby 						MONEY,
  	reduceby_userid					INTEGER CONSTRAINT moneycodesetelementfee_reducedbyuserid_fk REFERENCES login (userid),
  	notes 							text
);



CREATE SEQUENCE IF NOT EXISTS moneycecasefeeassignedid_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;


CREATE TABLE public.moneycecasefeeassigned
(
  cecaseassignedfeeid 				INTEGER NOT NULL DEFAULT nextval('moneycecasefeeassignedid_seq') PRIMARY KEY,
  moneyfeeassigned_assignedid		INTEGER NOT NULL CONSTRAINT moneyfeeassignedcodesetelement_assigned_fk REFERENCES moneyfeeassigned (moneyfeeassignedid),		
  codesetelement_elementid 			INTEGER NOT NULL CONSTRAINT moneycodesetelefee_cdseteleid_fk REFERENCES codesetelement (codesetelementid)
);


-- added manually by nathan on his system
ALTER TABLE moneycecasefeeassigned DROP COLUMN codesetelement_elementid;
-- added manually by nathan on his system
ALTER TABLE moneycecasefeeassigned ADD COLUMN cecase_caseid INTEGER NOT NULL CONSTRAINT moneycecasefeeassigned_caseid_fk REFERENCES cecase (caseid);
-- added manually by nathan on his system
ALTER TABLE moneyfeeassigned ADD COLUMN fee_feeid INTEGER NOT NULL CONSTRAINT moneyassignedfee_feeid_fk REFERENCES moneyfee (feeid);


CREATE SEQUENCE IF NOT EXISTS moneyoccperiodfeeassignedid_seq
  START WITH 1000
  INCREMENT BY 1 
  MINVALUE 1000
  NO MAXVALUE 
  CACHE 1;


CREATE TABLE public.moneyoccperiodfeeassigned
(
	moneyoccperassignedfeeid 		INTEGER NOT NULL DEFAULT nextval('moneyoccperiodfeeassignedid_seq') PRIMARY KEY,
  moneyfeeassigned_assignedid		INTEGER NOT NULL CONSTRAINT moneyfeeassignedcodesetelement_assigned_fk REFERENCES moneyfeeassigned (moneyfeeassignedid),		
  occperiod_periodid  				INTEGER NOT NULL CONSTRAINT moneyoccperiodfeeassigned REFERENCES occperiod (periodid)
  
);

CREATE TABLE public.moneycodesetelementfee
(
  
  fee_feeid integer NOT NULL,
  codesetelement_elementid integer NOT NULL,
  CONSTRAINT moneycodesetelementfee_pkey PRIMARY KEY (fee_feeid, codesetelement_elementid),
  CONSTRAINT moneycodesetelefee_cdseteleid_fk FOREIGN KEY (codesetelement_elementid)
      REFERENCES public.codesetelement (codesetelementid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT moneycodesetelefee_feeid_fk FOREIGN KEY (fee_feeid)
      REFERENCES public.moneyfee (feeid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


CREATE TABLE public.moneyoccperiodtypefee
(
  fee_feeid 						INTEGER NOT NULL CONSTRAINT moneyoccperiodtypefee_feeid_fk REFERENCES moneyfee (feeid),		
  occperiodtype_typeid 				INTEGER NOT NULL CONSTRAINT moneyoccperiodtypefee_typeid_fk REFERENCES occperiodtype (typeid),
  CONSTRAINT moneyoccperiodtypefee_comp_pk PRIMARY KEY (fee_feeid, occperiodtype_typeid)
);





CREATE TABLE public.moneyoccperiodfeepayment
(
	payment_paymentid 					INTEGER NOT NULL CONSTRAINT moneyoccperiodfeepayment_paymentid_fk REFERENCES moneypayment (paymentid),
	occperiodassignedfee_id 			INTEGER NOT NULL CONSTRAINT moneyoccpermittypefeepayment_occperassignedfee_fk REFERENCES moneyoccperiodfeeassigned (moneyoccperassignedfeeid),
	CONSTRAINT moneyoccperiodfeepayment_comp_pk PRIMARY KEY (payment_paymentid, occperiodassignedfee_id)


);



CREATE TABLE public.moneycecasefeepayment
(
	payment_paymentid 					INTEGER NOT NULL CONSTRAINT moneycecasefeepayment_paymentid_fk REFERENCES moneypayment (paymentid),
	cecaseassignedfee_id 			INTEGER NOT NULL CONSTRAINT moneycecasefeepayment_occperassignedfee_fk REFERENCES moneycecasefeeassigned (cecaseassignedfeeid),
	CONSTRAINT moneycecasefeepayment_comp_pk PRIMARY KEY (payment_paymentid, cecaseassignedfee_id)

);


--- END MONEY


DROP TABLE occeventproposalimplementation;
ALTER TABLE ceeventproposalimplementation RENAME TO eventproposalimplementation;

CREATE TABLE public.cecaserule
(
	cecase_caseid 				INTEGER NOT NULL CONSTRAINT cecaserule_caseid_fk REFERENCES cecase (caseid),
	eventrule_ruleid			INTEGER NOT NULL CONSTRAINT cecaserule_ruleid_fk REFERENCES eventrule (ruleid),
	CONSTRAINT cecaserule_comp_pk PRIMARY KEY (cecase_caseid, eventrule_ruleid)
);


ALTER TABLE public.eventproposalimplementation RENAME expiredorinactive TO active;
ALTER TABLE public.eventproposalimplementation
   ALTER COLUMN active SET DEFAULT true;



-- reference:
-- ALTER TABLE public.occinspectedchecklistspaceelement ADD COLUMN lastinspectedby_userid INTEGER CONSTRAINT occinspectedchklstspel_lastinspecby_userid_fk REFERENCES public.login (userid);




--***************************************
--  add reasons and their type proposal mappings here
--***************************************
--***************************************




-- TODO
-- run this after proposals
-- ALTER TABLE public.occpermitapplicationreason
--    ALTER COLUMN periodtypeproposal_periodid SET NOT NULL;





INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (16, 'database/patches/dbpatch_beta16.sql', '07-02-2019', 'ecd', 'occ permit application revisions');

