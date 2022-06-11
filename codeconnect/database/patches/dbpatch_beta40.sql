
-- DB Patch 40
-- MOPPING up occ inspection and getting transfers working




ALTER TABLE codeviolation ADD COLUMN transferredts TIMESTAMP WITH TIME ZONE;
ALTER TABLE codeviolation ADD COLUMN transferredby_userid INTEGER
	CONSTRAINT codeviolation_transferredbyuserid_fk REFERENCES login (userid);
ALTER TABLE codeviolation ADD COLUMN transferredtocecase_caseid INTEGER
	CONSTRAINT codeviolation_transferredtocecase_fk REFERENCES cecase (caseid);

ALTER TABLE occinspectedspaceelement ADD COLUMN transferredts TIMESTAMP WITH TIME ZONE;
ALTER TABLE occinspectedspaceelement ADD COLUMN transferredby_userid INTEGER
	CONSTRAINT occinspectedspaceelement_transferredbyuserid_fk REFERENCES login (userid);
ALTER TABLE occinspectedspaceelement ADD COLUMN transferredtocecase_caseid INTEGER
	CONSTRAINT occinspectedspaceelement_transferredtocecase_fk REFERENCES cecase (caseid);


INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors, 
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal, 
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins, 
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (311, CAST('Origination' AS eventtype), 'Generic case origination event', 'Placeholder category to signal a case opening', FALSE, 
            TRUE, 10, 0, 0, 
            'This generic event was created by the case auditor if it discovered a case without an origination event', NULL, 15, 
            TRUE, 5, 0 , 5);


INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors, 
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal, 
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins, 
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (312, CAST('Closing' AS eventtype), 'Generic case closing event', 'Placeholder category to signal a case closing', FALSE, 
            TRUE, 10, 0, 0, 
            'This generic event was created by the case auditor if it discovered a case without a closing event', NULL, 15, 
            TRUE, 5, 0 , 5);


ALTER TABLE public.municipality ADD COLUMN officeparcel_parcelid INTEGER
	CONSTRAINT municipality_parcelid_fk REFERENCES parcel (parcelkey);



--- SNAPPER UPDATES ---------------- -- HAVEN"T RUN ON SERVER SNAPPER


--BEGIN; -- not needed during 11-MAY-2022 updates
-- New Linked object role

INSERT INTO linkedobjectrole (lorid, lorschema, title, description, createdts, deactivatedts, notes) VALUES
	(233, 'ParcelMailingaddress', 'owner mailing address', null, now(), null, null), 
	(234, 'ParcelMailingaddress', 'mortgage mailing address', null, now(), null, null);

--DONE local system-check remote ECD
-- ALTER TABLE public.parcelmailingaddress ADD CONSTRAINT parcelmailingaddress_mailingaddressid_fk
--     FOREIGN KEY (mailingparcel_mailingid) REFERENCES mailingaddress (addressid);
-- ALTER TABLE public.parcelmailingaddress RENAME COLUMN mailingparcel_parcelid  TO parcel_parcelkey;
-- ALTER TABLE public.parcelmailingaddress RENAME COLUMN mailingparcel_mailingid TO mailingaddress_addressid;

ALTER TABLE mailingaddress
	ADD COLUMN attention text,
  ADD COLUMN secondary text;

-- Add citystatezip metadata
ALTER TABLE mailingcitystatezip
	ADD COLUMN createdts TIMESTAMP WITH TIME ZONE,
  	ADD COLUMN createdby_userid INTEGER
  	CONSTRAINT mailingcitystatezip_created_by_userid_fk REFERENCES login,
	ADD source_sourceid INTEGER
    CONSTRAINT mailingcitystatezip_sourceid_fk REFERENCES bobsource,
	ADD lastupdatedts TIMESTAMP WITH TIME ZONE,
	ADD lastupdatedby_userid INTEGER
    CONSTRAINT mailingcitystatezip_lastupdatedby_userid_fk REFERENCES login,
	ADD deactivatedts TIMESTAMP WITH TIME ZONE,
	ADD deactivatedby_userid INTEGER
    CONSTRAINT mailingcitystatezip_deactivatedby_userid REFERENCES login;


-- Add default id
CREATE SEQUENCE mailingcitystatezip_id_seq START 300000;
ALTER TABLE mailingcitystatezip ALTER column id SET DEFAULT nextval('public.mailingcitystatezip_id_seq'::regclass);

-- Ease mailing constraints
ALTER TABLE mailingaddress
	ALTER COLUMN bldgno DROP not null;
UPDATE mailingstreet SET pobox=false WHERE pobox IS NULL;
ALTER TABLE mailingstreet
	ALTER COLUMN pobox SET NOT NULL;

-- The human_id_seq is currently set to 100 when a developer loads a fresh copy of the database
--  This causes id collisions when inserting new humans.
--  This hacky solution fixes that.
--  Note: other tables likely share this same problem. We can deal with it when we run into it.
-- DIDN'T run with snappers on 11-MAY-2022
-- select setval('public.human_humanid_seq'::regclass, max(humanid) + 30000) from human;


--- END SNAPPER UPDATES ----------------





ALTER TABLE parcel ADD COLUMN broadview_photodocid INTEGER 
	CONSTRAINT parcel_broadview_photodicid_fk REFERENCES photodoc (photodocid);



ALTER TABLE photodoc ADD COLUMN dateofrecord TIMESTAMP WITH TIME ZONE DEFAULT now();
ALTER TABLE photodoc ADD COLUMN courtdocument boolean DEFAULT TRUE;



-- Changes for occupancy permitting
ALTER TABLE public.occpermit ADD COLUMN finalizedts TIMESTAMP WITH TIME ZONE;

ALTER TABLE public.occpermit ADD COLUMN finalizedby_userid INTEGER 
	CONSTRAINT occpermit_finalizedby_userid_fk REFERENCES login (userid);

ALTER TABLE public.occpermit ADD COLUMN statictitle TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticmuniaddress TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticpropertyinfo TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticownerseller TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticcolumnlink TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticbuyertenant TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticproposeduse TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticusecode TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticpropclass TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticdateofapplication TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occpermit ADD COLUMN staticinitialinspection TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occpermit ADD COLUMN staticreinspectiondate TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occpermit ADD COLUMN staticfinalinspection TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occpermit ADD COLUMN staticdateofissue TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occpermit ADD COLUMN staticofficername TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticissuedundercodesourceid TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticstipulations TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticcomments TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticmanager TEXT;
ALTER TABLE public.occpermit ADD COLUMN statictenants TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticleaseterm TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticleasestatus TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticpaymentstatus TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticnotice TEXT;
ALTER TABLE public.occpermit ADD COLUMN staticconstructiontype TEXT;


ALTER TABLE public.occpermit ADD COLUMN createdts TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occpermit ADD COLUMN createdby_userid INTEGER 
	CONSTRAINT occpermit_createdby_fk REFERENCES login (userid);

ALTER TABLE public.occpermit ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occpermit ADD COLUMN lastupdatedby_userid INTEGER 
	CONSTRAINT occpermit_lastupdatedby_fk REFERENCES login (userid);



ALTER TABLE public.occpermit ADD COLUMN deactivatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occpermit ADD COLUMN deactivatedby_userid INTEGER 
	CONSTRAINT occpermit_deactivatedby_fk REFERENCES login (userid);

ALTER TABLE public.occpermit DROP COLUMN issuedto_personid;
ALTER TABLE public.occpermit DROP COLUMN issuedby_userid;
ALTER TABLE public.occpermit DROP COLUMN dateissued;
ALTER TABLE public.occpermit RENAME COLUMN permitadditionaltext TO staticpermitadditionaltext ;





INSERT INTO public.textblockcategory(
            categoryid, categorytitle, icon_iconid, muni_municode)
    VALUES (200, 'Occ Permit Stipulations', NULL, 999);

INSERT INTO public.textblockcategory(
            categoryid, categorytitle, icon_iconid, muni_municode)
    VALUES (201, 'Occ Permit Notices', NULL, 999);

INSERT INTO public.textblockcategory(
            categoryid, categorytitle, icon_iconid, muni_municode)
    VALUES (202, 'Occ Permit Comments', NULL, 999);


INSERT INTO public.textblock(
            blockid, blockcategory_catid, muni_municode, blockname, blocktext, 
            placementorderdefault, injectabletemplate)
    VALUES (DEFAULT, 201, 999, 'Non-transferrable', 'This Occupancy permit is issued to the listed OWNER only and is not transferable to another Buyer. The Occupancy Permit is issued as/for the authorized use listed above only for the LISTED tenant(s)/occupant(s), Buyers and is not transferable to another tenant(s)/occupant(s) or Buyers. Should ANY information included on this Occupancy permit change a NEW Certificate of Occupancy will need to be obtained.', 
            1, NULL);

INSERT INTO public.textblock(
            blockid, blockcategory_catid, muni_municode, blockname, blocktext, 
            placementorderdefault, injectabletemplate)
    VALUES (DEFAULT, 200, 999, 'Construction', 'All permits are required when required for construction purposes to remedy noted violations as described in FIR', 
            1, NULL);



INSERT INTO public.textblock(
            blockid, blockcategory_catid, muni_municode, blockname, blocktext, 
            placementorderdefault, injectabletemplate)
    VALUES (DEFAULT, 201, 999, 'Non-transferrable-TCO','This Occupancy permit is issued to the listed Buyer only and is not transferable to another Buyer. The Occupancy Permit is issued as a Temporary Occupancy Permit (TCO) to CLOSE. The TCO will expire in 30 days of the issue date. No habitability or occupation of the subject property is permitted. Should ANY information included on this Occupancy permit change a NEW Certificate of Occupancy will need to be obtained. REFER TO THE FIELD INSPECTION REPORT FOR DETAILS OF INSPECTION FINDINGS. Violations may result in an NOV for compliance, and a re-inspection will be required. TCO will expire on the date listed above.' , 
            1, NULL);




ALTER TABLE public.occpermit ADD COLUMN nullifiedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occpermit ADD COLUMN nullifiedby_userid INTEGER
	CONSTRAINT occpermit_nullifiedby_userid REFERENCES login (userid);

ALTER TABLE public.occinspectiondetermination ADD COLUMN qualifiesaspassed boolean DEFAULT FALSE;


ALTER TABLE occperiodtype ADD COLUMN expires BOOLEAN DEFAULT FALSE;



ALTER TABLE occperiodtype RENAME TO occpermittype;
ALTER TABLE public.occpermit ADD COLUMN staticdateexpiry timestamp with time zone;
ALTER TABLE public.occpermit ADD COLUMN permittype_typeid integer;
ALTER TABLE public.occpermit
  ADD CONSTRAINT occpermit_typeid_fk FOREIGN KEY (permittype_typeid)
      REFERENCES public.occpermittype (typeid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;


DROP TABLE public.moneycecasefeepayment CASCADE;
DROP TABLE public.moneycecasefeeassigned CASCADE;

DROP TABLE public.moneycodesetelementfee CASCADE;
 
 DROP TABLE public.moneyoccperiodfeeassigned CASCADE;
 DROP TABLE public.moneyoccperiodfeepayment CASCADE;
 DROP TABLE public.moneyoccperiodtypefee CASCADE;
 DROP TABLE public.moneypayment CASCADE;
 DROP TABLE public.moneyfee CASCADE;

--  NOTICE:  drop cascades to constraint moneyoccpermittypefeepayment_occperassignedfee_fk on table moneyoccperiodfeepayment
-- NOTICE:  drop cascades to 2 other objects
-- DETAIL:  drop cascades to constraint codesetelement_feeid_fk on table codesetelement
-- drop cascades to constraint muniprofilefee__feeid_fk on table muniprofilefee
-- Query returned successfully with no result in 184 msec.




-- ******************************* run on LIVE DEPLOYED system up to here *******************************




CREATE TYPE transactiontype AS ENUM ('charge','payment', 'adjustment');


CREATE SEQUENCE public.moneyledger_transactionid_seq
  INCREMENT 1
  MINVALUE 100
  MAXVALUE 9223372036854775807
  START 101
  CACHE 1;
ALTER TABLE public.citationfilingtype_typeid_seq
  OWNER TO sylvia;



CREATE TABLE public.moneyledger
(
	transactionid integer NOT NULL DEFAULT nextval('moneyledger_transactionid_seq'::regclass),
	cecase_caseid integer CONSTRAINT moneyledger_caseid_fk REFERENCES cecase (caseid),
	occperiod_periodid integer CONSTRAINT moneyledger_occperiod_fk REFERENCES occperiod (periodid),
	transtype transactiontype NOT NULL,
	amount 			money NOT NULL,
	dateofrecord 			TIMESTAMP WITH TIME ZONE,
	createdts               TIMESTAMP WITH TIME ZONE,
	createdby_userid        INTEGER CONSTRAINT moneyledger_createdby_userid_fk REFERENCES login (userid),     
	lastupdatedts           TIMESTAMP WITH TIME ZONE,
	lastupdatedby_userid    INTEGER CONSTRAINT moneyledger_lastupdatdby_userid_fk REFERENCES login (userid),
	deactivatedts           TIMESTAMP WITH TIME ZONE,
	deactivatedby_userid    INTEGER CONSTRAINT moneyledger_deactivatedby_userid_fk REFERENCES login (userid),    
    notes text,


);


CREATE TYPE chargetype AS ENUM ('fee','fine');

CREATE TABLE public.chargeschedule
(
	feeid integer NOT NULL DEFAULT nextval('occinspectionfee_feeid_seq'::regclass),
	ctype chargetype NOT NULL,
	muni_municode integer NOT NULL,
	chargename text NOT NULL,
	chargeamount money NOT NULL,
	governingordinance_eceid integer NOT NULL CONSTRAINT chargeschedule_ord_fk REFERENCES codesetelement (codesetelementid),
	effectivedate timestamp with time zone NOT NULL,
	expirydate timestamp with time zone NOT NULL,
	createdts               TIMESTAMP WITH TIME ZONE,
	createdby_userid        INTEGER CONSTRAINT chargeschedule_createdby_userid_fk REFERENCES login (userid),     
	lastupdatedts           TIMESTAMP WITH TIME ZONE,
	lastupdatedby_userid    INTEGER CONSTRAINT chargeschedule_lastupdatdby_userid_fk REFERENCES login (userid),
	deactivatedts           TIMESTAMP WITH TIME ZONE,
	deactivatedby_userid    INTEGER CONSTRAINT chargeschedule_deactivatedby_userid_fk REFERENCES login (userid),    
	notes text,
  
  CONSTRAINT occinspecfee_feeid_pk PRIMARY KEY (feeid),
  CONSTRAINT muni_municode_fk FOREIGN KEY (muni_municode)
      REFERENCES public.municipality (municode) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)


-- ******************************* run on LOCAL TEST system up to here *******************************


CREATE TABLE public.moneychargeledger
(



);





-- WIP fields
ALTER TABLE public.login DROP CONSTRAINT login_personlink_personid_fk;
ALTER TABLE public.login
  ADD CONSTRAINT login_humanlink_humanid_fk FOREIGN KEY (personlink)
      REFERENCES public.human (humanid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;


ALTER TABLE public.cecase RENAME COLUMN login_userid TO manager_userid;
ALTER TABLE public.cecase ADD COLUMN createdby_userid INTEGER
	CONSTRAINT cecase_createdby_userid_fk REFERENCES login (userid);
ALTER TABLE public.cecase ADD COLUMN createdts TIMESTAMP WITH TIME ZONE DEFAULT now();
ALTER TABLE public.cecase ADD COLUMN deactivatedby_userid INTEGER
	CONSTRAINT cecase_deactivatedby_userid_fk REFERENCES login (userid);
ALTER TABLE public.cecase ADD COLUMN deactivatedts TIMESTAMP WITH TIME ZONE DEFAULT now();
UPDATE TABLE public.cecase SET createdby_userid = manager_userid;


-- finish me
ALTER TABLE occchecklist ADD COLUMN inspectionspecific INTEGER
    CONSTRAINT occchecklist_inspspecific_fk REFERENCES 


ALTER TABLE public.municipality DROP COLUMN office_propertyid;







-- EXTRA STUFF 
CREATE OR REPLACE FUNCTION public.cnf_nov_udpatestaticsendersigfields(targetmunicode INTEGER)
    RETURNS INTEGER AS
$BODY$
    DECLARE
        nov_rec RECORD;
        pers_rec RECORD;
        fullname TEXT;
        fixedname TEXT;
        nov_count INTEGER;
    BEGIN
        nov_count := 0;
        FOR nov_rec IN SELECT noticeid, notifyingofficer_userid FROM public.noticeofviolation 
            INNER JOIN public.cecase ON (noticeofviolation.caseid = cecase.caseid)
            INNER JOIN public.property ON (cecase.property_propertyid = property.propertyid)
            WHERE municipality_municode = targetmunicode AND notifyingofficer_userid IS NOT NULL

            LOOP -- over NOVs by MUNI
                SELECT personid, fname, lname, jobtitle, phonework, email 
                    FROM public.login 
                    LEFT OUTER JOIN public.person ON (login.personlink = person.personid) 
                    WHERE userid = nov_rec.notifyingofficer_userid INTO pers_rec;

                RAISE NOTICE 'WRITING FIXED SENDER ID % INTO NOV ID %', nov_rec.notifyingofficer_userid, nov_rec.noticeid;
                fullname := pers_rec.fname || ' ' || pers_rec.lname;

                EXECUTE format('UPDATE noticeofviolation SET 
                    fixednotifyingofficername = %L,
                    fixednotifyingofficertitle = %L,
                    fixednotifyingofficerphone = %L,
                    fixednotifyingofficeremail = %L,
                    notifyingofficer_humanid = %L WHERE noticeid=%L;',
                    fullname,
                    pers_rec.jobtitle,
                    pers_rec.phonework,
                    pers_rec.email,
                    pers_rec.personid,
                    nov_rec.noticeid);
                nov_count := nov_count + 1;
                RAISE NOTICE 'UPDATE SUCCESS! Count: % ', nov_count;
            END LOOP; -- loop over NOVs by MUNI
        RETURN nov_count;
    END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;




-- extra gunk - not run remotely


DROP TABLE humanmailingrole;

-- TODO: Remove personid_recipient on NOV after full migration to human and mailing address
-- todo clean up old citation stuff


-- TODO: Remove the codeelment_id column of occchecklistspacetypeelement
-- after the refactor 


CREATE SEQUENCE IF NOT EXISTS occperiodlease_leaseid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.occperiodlease
    (
        leaseid             INTEGER PRIMARY KEY DEFAULT nextval('parcelunithumanlease_leaseid_seq'),
        datestart           DATE,
        dateend             DATE,
        signeddate          DATE,
        monthlyrent         MONEY,
        -- finish me??
        --leasor_humanid      INTEGER CONSTRAINT parcelhumanlease 
    
    );


INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (40, 'database/patches/dbpatch_beta40.sql', NULL, 'ecd', '');