--
-- THE OFFICIAL PRE_RELEASE database 0.9.43 schema release
-- This is the database onto which patches starting in the released generation begin
-- starting with cnfdbv.1.x.y

-- Dumped from database version 9.5.19
-- Dumped by pg_dump version 11.5 (Ubuntu 11.5-1.pgdg16.04+1)

-- Started on 2022-10-31 11:56:03 EDT

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 4626 (class 1262 OID 17422)
-- Name: cogdb; Type: DATABASE; Schema: -; Owner: -
--

CREATE DATABASE cogdb WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = 'en_US.UTF-8' LC_CTYPE = 'en_US.UTF-8';


\connect cogdb

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2 (class 3079 OID 110817)
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 4627 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- TOC entry 1148 (class 1247 OID 65164)
-- Name: casephase; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.casephase AS ENUM (
    'PrelimInvestigationPending',
    'NoticeDelivery',
    'InitialComplianceTimeframe',
    'SecondaryComplianceTimeframe',
    'AwaitingHearingDate',
    'HearingPreparation',
    'InitialPostHearingComplianceTimeframe',
    'SecondaryPostHearingComplianceTimeframe',
    'InactiveHolding',
    'Closed',
    'LegacyImported'
);


--
-- TOC entry 1555 (class 1247 OID 189537)
-- Name: chargetype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.chargetype AS ENUM (
    'fee',
    'fine',
    'FINE',
    'FEE'
);


--
-- TOC entry 1406 (class 1247 OID 163769)
-- Name: citationviolationstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.citationviolationstatus AS ENUM (
    'Pending',
    'Guilty',
    'Dismissed',
    'Compliance',
    'Deemed Invalid',
    'FILED',
    'AWAITING_PLEA',
    'CONTINUED',
    'GUILTY',
    'NO_CONTEST',
    'DISMISSED',
    'COMPLIANCE',
    'INVALID',
    'WITHDRAWN',
    'NOT_GUILTY',
    'OTHER'
);


--
-- TOC entry 1602 (class 1247 OID 205984)
-- Name: eventemissionenum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.eventemissionenum AS ENUM (
    'NOTICE_OF_VIOLATION_SENT',
    'NOTICE_OF_VIOLATION_FOLLOWUP',
    'NOTICE_OF_VIOLATION_RETURNED',
    'TRANSACTION',
    'CITATION_HEARING'
);


--
-- TOC entry 1158 (class 1247 OID 65186)
-- Name: eventtype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.eventtype AS ENUM (
    'Origination',
    'Action',
    'CaseAdmin',
    'PhaseChange',
    'Closing',
    'Timeline',
    'Communication',
    'Meeting',
    'Notice',
    'Custom',
    'Compliance',
    'Citation',
    'Occupancy',
    'PropertyInfoCase',
    'Workflow',
    'Court',
    'Accounting',
    'OccupancyOrigination',
    'OccupancyClosing',
    'Inspection'
);


--
-- TOC entry 1422 (class 1247 OID 163945)
-- Name: linkedobjectroleschema; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.linkedobjectroleschema AS ENUM (
    'OccApplicationHuman',
    'CECaseHuman',
    'OccPeriodHuman',
    'ParcelHuman',
    'ParcelUnitHuman',
    'CitationHuman',
    'EventHuman',
    'MailingaddressHuman',
    'ParcelMailingaddress',
    'CitationDocketHuman'
);


--
-- TOC entry 1131 (class 1247 OID 144062)
-- Name: occapplicationstatus; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.occapplicationstatus AS ENUM (
    'Waiting',
    'NewUnit',
    'OldUnit',
    'Rejected',
    'Invalid'
);


--
-- TOC entry 1478 (class 1247 OID 172682)
-- Name: occinspectionphototype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.occinspectionphototype AS ENUM (
    'PassDocumentation',
    'FailDocumentation',
    'GeneralDocumentation',
    'Other',
    'Unused'
);


--
-- TOC entry 1142 (class 1247 OID 65125)
-- Name: persontype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.persontype AS ENUM (
    'user',
    'User',
    'CogStaff',
    'NonCogOfficial',
    'MuniStaff',
    'Tenant',
    'OwnerOccupant',
    'OwnerNonOccupant',
    'FutureOwner',
    'Owner',
    'Manager',
    'ElectedOfficial',
    'Public',
    'LawEnforcement',
    'Other',
    'ownercntylookup',
    'LegacyOwner',
    'LegacyAgent',
    'LegacyImported',
    'Workflow'
);


--
-- TOC entry 912 (class 1247 OID 29478)
-- Name: requeststatusenum; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.requeststatusenum AS ENUM (
    'AwaitingReview',
    'UnderInvestigation',
    'NoViolationFound',
    'CitationFiled',
    'Resolved'
);


--
-- TOC entry 1145 (class 1247 OID 65148)
-- Name: role; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.role AS ENUM (
    'Developer',
    'SysAdmin',
    'CogStaff',
    'EnforcementOfficial',
    'MuniStaff',
    'MuniReader',
    'Public',
    'User'
);


--
-- TOC entry 1521 (class 1247 OID 180800)
-- Name: systemdomain; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.systemdomain AS ENUM (
    'CodeEnforcement',
    'Occupancy',
    'Universal'
);


--
-- TOC entry 1551 (class 1247 OID 189491)
-- Name: transactiontype; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE public.transactiontype AS ENUM (
    'charge',
    'payment',
    'adjustment',
    'CHARGE',
    'PAYMENT',
    'ADJUSTMENT'
);


--
-- TOC entry 522 (class 1255 OID 173009)
-- Name: cnf_injectstaticnovdata(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.cnf_injectstaticnovdata(targetmunicode integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
  	DECLARE
  		nov_rec RECORD;
  		pers_rec RECORD;
  		fullname TEXT;
  		fixedname TEXT;
  		fixedaddr TEXT;
  		fixedcity TEXT;
  		fixedstate TEXT;
  		fixedzip TEXT;
  		nov_count INTEGER;
	BEGIN
		nov_count := 0;
		FOR nov_rec IN SELECT noticeid, personid_recipient FROM public.noticeofviolation 
			INNER JOIN public.cecase ON (noticeofviolation.caseid = cecase.caseid)
			INNER JOIN public.property ON (cecase.property_propertyid = property.propertyid)
			WHERE municipality_municode = targetmunicode

			LOOP -- over NOVs by MUNI
				SELECT personid, fname, lname, address_street, address_city, 
       				address_state, address_zip FROM public.person WHERE personid = nov_rec.personid_recipient INTO pers_rec;

   				RAISE NOTICE 'WRITING FIXED RECIPIENT ID % INTO NOV ID %', nov_rec.personid_recipient, nov_rec.noticeid;
   				fullname := pers_rec.fname || ' ' || pers_rec.lname;

   				EXECUTE format('UPDATE noticeofviolation SET 
   					fixedrecipientxferts = now(), 
   					fixedrecipientname = %L,
   					fixedrecipientstreet = %L,
				    fixedrecipientcity = %L,
				    fixedrecipientstate = %L,
				    fixedrecipientzip = %L WHERE noticeid = %L;',
				    fullname,
				    pers_rec.address_street,
				    pers_rec.address_city,
				    pers_rec.address_state,
				    pers_rec.address_zip,
				    nov_rec.noticeid);
   				nov_count := nov_count + 1;
   				RAISE NOTICE 'UPDATE SUCCESS! Count: % ', nov_count;
			END LOOP; -- loop over NOVs by MUNI
		RETURN nov_count;
	END;
$$;


--
-- TOC entry 523 (class 1255 OID 180990)
-- Name: cnf_nov_injectstaticsendersigfields(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.cnf_nov_injectstaticsendersigfields(targetmunicode integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
                    notifyingofficer_humanid = %L;',
                    fullname,
                    pers_rec.jobtitle,
                    pers_rec.phonework,
                    pers_rec.email,
                    pers_rec.personid);
                nov_count := nov_count + 1;
                RAISE NOTICE 'UPDATE SUCCESS! Count: % ', nov_count;
            END LOOP; -- loop over NOVs by MUNI
        RETURN nov_count;
    END;
$$;


--
-- TOC entry 524 (class 1255 OID 180993)
-- Name: cnf_nov_udpatestaticsendersigfields(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.cnf_nov_udpatestaticsendersigfields(targetmunicode integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
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
$$;


--
-- TOC entry 521 (class 1255 OID 172981)
-- Name: cnf_parsezipcode(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.cnf_parsezipcode(zipraw text) RETURNS text
    LANGUAGE plpgsql
    AS $$
  	DECLARE
  		cleanzip TEXT;

	BEGIN
		cleanzip := substring(zipraw FROM '(\d{5})');
		IF cleanzip IS NOT NULL
			THEN
				RETURN cleanzip;
			ELSE
				RETURN '';
		END IF;
	END;
$$;


--
-- TOC entry 525 (class 1255 OID 197687)
-- Name: cnf_sha1(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.cnf_sha1(text) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT
    AS $_$

	SELECT encode(digest($1, 'sha1'), 'hex')

$_$;


--
-- TOC entry 512 (class 1255 OID 110889)
-- Name: copycleartextpswds(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.copycleartextpswds() RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
 userrow RECORD;
BEGIN
 RAISE NOTICE 'starting transfer...';
 FOR userrow IN SELECT password, userid
 FROM login LOOP
	EXECUTE format('UPDATE login SET pswdcleartext = %L WHERE userid = %L ', userrow.password, userrow.userid); 
	END LOOP;
RETURN 1;
END;
$$;


--
-- TOC entry 184 (class 1259 OID 65205)
-- Name: person_personidseq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.person_personidseq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 185 (class 1259 OID 65207)
-- Name: person; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.person (
    personid integer DEFAULT nextval('public.person_personidseq'::regclass) NOT NULL,
    persontype public.persontype,
    muni_municode integer NOT NULL,
    fname text,
    lname text NOT NULL,
    jobtitle text,
    phonecell text,
    phonehome text,
    phonework text,
    email text,
    address_street text,
    address_city text,
    address_state text DEFAULT 'PA'::text,
    address_zip text,
    notes text,
    lastupdated timestamp with time zone,
    expirydate timestamp with time zone,
    isactive boolean DEFAULT true,
    isunder18 boolean DEFAULT false,
    humanverifiedby integer,
    compositelname boolean DEFAULT false,
    sourceid integer,
    creator integer,
    businessentity boolean DEFAULT false,
    mailing_address_street text,
    mailing_address_city text,
    mailing_address_zip text,
    mailing_address_state text,
    useseparatemailingaddr boolean DEFAULT false,
    expirynotes text,
    creationtimestamp timestamp with time zone,
    canexpire boolean DEFAULT false,
    userlink integer,
    mailing_address_thirdline text,
    ghostof integer,
    ghostby integer,
    ghosttimestamp timestamp with time zone,
    cloneof integer,
    clonedby integer,
    clonetimestamp timestamp with time zone,
    referenceperson boolean,
    rawname text,
    cleanname text,
    multientity boolean
);


--
-- TOC entry 492 (class 1255 OID 106362)
-- Name: createghostperson(public.person, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.createghostperson(person_row public.person, userid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$

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

$$;


--
-- TOC entry 515 (class 1255 OID 172416)
-- Name: extractbuildingno(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.extractbuildingno(addr text) RETURNS text
    LANGUAGE plpgsql
    AS $$
	DECLARE
	 	extractedbldg TEXT;

	BEGIN
		IF addr ILIKE '%PO BOX%'
			THEN 
				extractedbldg := substring(addr from '[Pp][Oo]\s[Bb][Oo][Xx]\s\d+');
			ELSE
				IF addr ILIKE '%/%'
				THEN -- we've got a XX 1/2 street
					extractedbldg := substring(addr from '\d+\W\d/\d');
				ELSE 
					extractedbldg := substring(addr from '\d+');
				END IF; -- fractions
		END IF; -- PO boxes

		RETURN unifyspacesandtrim(extractedbldg);
	END;
$$;


--
-- TOC entry 519 (class 1255 OID 172418)
-- Name: extractstreet(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.extractstreet(addr text) RETURNS text
    LANGUAGE plpgsql
    AS $$
	DECLARE
	 	extractedstreet TEXT;
	 	re_matches RECORD;
	 	validationstring TEXT;

	BEGIN
		IF addr ILIKE '%PO BOX%'
			THEN
				RETURN 'PO BOX';
			ELSE
				IF addr ILIKE '%/%'
					THEN -- we've got a XX 1/2 street
						extractedstreet := substring(addr from '\d+\W\d/\d\W?(.*)');
					ELSE 
						IF addr ILIKE'%-%'
							THEN --we've got a range of some sort
								SELECT regexp_matches(addr, '\w-\w') INTO re_matches;
								IF NOT FOUND
									THEN -- we've got an address range and not a unit range
										extractedstreet := substring(addr from '\d+\W?-\d+\W(.*)');
									ELSE -- we've likely got a unit range, so skip
										RAISE NOTICE 'found unit range in address; skipping: %', addr;
										RETURN NULL;
								END IF;
							ELSE -- no address range
								extractedstreet := substring(addr from '\d+\W(.*)');
						END IF; -- range check
				END IF; -- fraction check
		END IF; -- box box
		validationstring := unifyspacesandtrim(extractedstreet);
		-- check work for null, and empty strings and single spaces
		IF validationstring IS NOT NULL AND validationstring <> '' AND validationstring <> ' '
			THEN
				RETURN validationstring;
			ELSE
				RETURN NULL;
		END IF; --validation
	END;
$$;


--
-- TOC entry 513 (class 1255 OID 110891)
-- Name: hashpasswords(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.hashpasswords() RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
 userrow RECORD;
BEGIN
 RAISE NOTICE 'starting transfer...';
 FOR userrow IN SELECT pswdcleartext, userid
 FROM login LOOP
	EXECUTE format('UPDATE login SET password = encode(digest(%L, ''md5''), ''base64'') WHERE userid = %L ', userrow.pswdcleartext, userrow.userid); 
	END LOOP;
RETURN 1;
END;
$$;


--
-- TOC entry 517 (class 1255 OID 172788)
-- Name: migratepersontohuman(integer, integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.migratepersontohuman(creationrobotuser integer, defaultsource integer, municodetarget integer, parcel_human_lorid integer, human_mailing_lorid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
	DECLARE
	 	pr RECORD;
	 	prop_pers_rec RECORD;
	 	fullname TEXT;
	 	fresh_human_id INTEGER;
	 	current_human_id INTEGER;
	 	deacts TIMESTAMP WITH TIME ZONE;
	 	deacuser INTEGER;
	 	human_rec_count INTEGER;
	 	parcel_rec RECORD;
	 	mailing_rec RECORD;
	 	zip_parsed TEXT;
	 	citystatezip_rec RECORD;
	 	street_freshstreetname TEXT;
	 	street_freshid INTEGER;
	 	address_freshid INTEGER;
	 	pers_newaddr_street TEXT;
	 	pers_newaddr_bldgno TEXT;
	 	human_dupid INTEGER;

	BEGIN
		RAISE NOTICE '**** BEGIN PERSON TO HUMAN MIGRATION ****';

		human_rec_count := 0;

		FOR pr IN SELECT personid, persontype, muni_municode, fname, lname, jobtitle, 
		       phonecell, phonehome, phonework, email, address_street, address_city, 
		       address_state, address_zip, notes, lastupdated, expirydate, isactive, 
		       isunder18, humanverifiedby, compositelname, sourceid, creator, 
		       businessentity, mailing_address_street, mailing_address_city, 
		       mailing_address_zip, mailing_address_state, useseparatemailingaddr, 
		       expirynotes, creationtimestamp, canexpire, userlink, mailing_address_thirdline, 
		       ghostof, ghostby, ghosttimestamp, cloneof, clonedby, clonetimestamp, 
		       referenceperson, rawname, cleanname, multientity
		  			FROM public.person WHERE muni_municode = municodetarget
		 
			LOOP -- over legacy person records
				RAISE NOTICE 'ITERATION: personid: %, lname: %', pr.personid, pr.lname;
				
				IF (pr.lname IS NULL OR pr.lname = '') AND (pr.fname IS NULL OR pr.fname = '')
					THEN

						RAISE NOTICE 'found null or empty last AND first name; skipping person and LOGGING;';
						EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
																            logentryid, human_humanid, person_personid, error_code, notes, ts)
																    VALUES (DEFAULT, NULL, %L, %L, ''EMTPY NAME'', now());',
																    pr.personid, 4 
																);
						CONTINUE;
				END IF;

				--concat name
				IF pr.fname IS NOT NULL
					THEN
						fullname := unifyspacesandtrim(pr.fname) || ' ' || unifyspacesandtrim(pr.lname); 
					ELSE 
						fullname := unifyspacesandtrim(pr.lname);
				END IF;

				RAISE NOTICE 'FULL name for personid % is %', pr.personid, fullname;

				-- Check for duplicate person in human table
				SELECT humanid INTO human_dupid FROM human WHERE unifyspacesandtrim(human.name) = fullname;

				RAISE NOTICE 'DUP CHECK: HUMAN_DUPID: % ', human_dupid;

				IF human_dupid IS NULL OR human_dupid = 0 -- no duplicate based on name only
					THEN  -- go ahead and write our new human records
						RAISE NOTICE 'NO DUP FOUND FOR %; writing new human', fullname;
						-- check for deactivation
						IF NOT pr.isactive 
							THEN 
								deacts := now();
								deacuser := 99; -- the cogbot
							ELSE
								deacts := NULL;
								deacuser := NULL;
						END IF;

						-- our new humanid is our old person id
						fresh_human_id := pr.personid;

						EXECUTE format('INSERT INTO public.human(
			            humanid, name, dob, under18, jobtitle, businessentity, multihuman, 
			            source_sourceid, deceaseddate, deceasedby_userid, cloneof_humanid, 
			            createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, 
			            deactivatedts, deactivatedby_userid, notes)
						    VALUES (%L, %L, NULL, %L, %L, FALSE, %L, 
						            %L, NULL, NULL, NULL, 
						            now(), %L, now(), %L, 
						            %L, %L, %L);', 
					               fresh_human_id, fullname, pr.isunder18, unifyspacesandtrim(pr.jobtitle), pr.multientity,
					               pr.sourceid, 
					               pr.creator, pr.creator, 
					               deacts, deacuser, unifyspacesandtrim(pr.notes)

			            ); 

			            RAISE NOTICE 'Fresh human record ID: %', fresh_human_id;
			            -- Now move our cursor to our new fresh human
			            current_human_id := fresh_human_id; -- NOTE if we have a dupe, current_human_id is not updated

			            human_rec_count := human_rec_count + 1;

						-- DEAL with "None" in phone columns, and empty strings

						IF pr.phonecell IS NOT NULL AND pr.phonecell <> '' AND pr.phonecell <> 'None'			
							THEN
								--RAISE NOTICE 'PHONE CELL FOUND: %', pr.phonecell;
								EXECUTE format('INSERT INTO public.contactphone(
											            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, 
											            disconnectts, disconnect_userid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, 100, 
											            NULL, NULL, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration JUL-2021'');', 
											            		fresh_human_id, unifyspacesandtrim(pr.phonecell), 
											            					creationrobotuser,
								            					creationrobotuser);
							ELSE
								--RAISE NOTICE 'NO VALID PHONE CELL FOUND; NOT WRITING RECORD';
								NULL; -- don't write any phone records
						END IF;


						IF pr.phonehome IS NOT NULL AND pr.phonehome <> '' AND pr.phonehome <> 'None'			
							THEN
								--RAISE NOTICE 'PHONE HOME FOUND: %', pr.phonehome;
								EXECUTE format('INSERT INTO public.contactphone(
											            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, 
											            disconnectts, disconnect_userid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, 101, 
											            NULL, NULL, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration JUL-2021'');', 
											            		fresh_human_id, unifyspacesandtrim(pr.phonehome), 
											            					creationrobotuser,
								            					creationrobotuser);
							ELSE -- duplicate
								--RAISE NOTICE 'NO VALID PHONE HOME FOUND; NOT WRITING RECORD';
								NULL; -- don't write any records
						END IF;


						IF pr.phonework IS NOT NULL AND pr.phonework <> '' AND pr.phonework <> 'None'			
							THEN
								--RAISE NOTICE 'PHONE WORK FOUND: %', pr.phonework;
								EXECUTE format('INSERT INTO public.contactphone(
											            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, 
											            disconnectts, disconnect_userid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, 102, 
											            NULL, NULL, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration JUL-2021'');', 
											            		fresh_human_id, unifyspacesandtrim(pr.phonework), 
											            					creationrobotuser,
								            					creationrobotuser);
							ELSE
								--RAISE NOTICE 'NO VALID PHONE WORK FOUND; NOT WRITING RECORD';
								NULL; -- don't write any records
						END IF;
						
						IF pr.email IS NOT NULL AND pr.email <> '' AND pr.email <> 'None'			
						
							THEN
								--RAISE NOTICE 'EMAIL: WRITING RECORD % ', pr.email;
								EXECUTE format('INSERT INTO public.contactemail(
									            emailid, human_humanid, emailaddress, bouncets, createdts, createdby_userid, 
									            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
									            notes)
									    VALUES (DEFAULT, %L, %L, NULL, now(), %L, 
									            now(), %L, NULL, NULL, 
									            ''Created during person to human migration JUL-2021'');', 
							            		fresh_human_id, unifyspacesandtrim(pr.email), creationrobotuser,
				            					creationrobotuser);
							ELSE
								--RAISE NOTICE 'EMAIL: NO VALID EMAIL FOUND, SKIPPING';
								NULL; -- don't write any records
						END IF;

					ELSE  -- Record duplicate person found, don't write new humans, just write to log
						current_human_id := human_dupid;
						RAISE NOTICE 'DUPLICATE HUMAN FOUND: logging and using already inserted humanid % ', current_human_id  ;
						EXECUTE format('INSERT INTO public.personhumanmigrationlog(
									            logentryid, human_humanid, person_personid, error_code, notes, ts)
									    VALUES (DEFAULT, NULL, NULL, 2, %L, now());',
								    		 'DUPLICATE RECORD FOR fullname ' || fullname || ' With Person ID: ' || pr.personid
									    );

				END IF; -- over duplicate check of person records

				-- use existing property-person links to connect our new human to existing parcels
				FOR prop_pers_rec IN SELECT property_propertyid, person_personid, creationts
									 	 FROM public.propertyperson
									 	 WHERE person_personid = pr.personid
			 		LOOP -- begin iterating over property person records
			 			
			 			-- Link this human to an existing parcel

			 			SELECT parcelkey INTO parcel_rec FROM parcel WHERE parcelkey = prop_pers_rec.property_propertyid;
			 			IF FOUND
				 			THEN -- link human and parcel
				 			-- NOTE that we used the old property PK as the new parcel PK so this linking should be straightforward

				 				EXECUTE format('INSERT INTO public.humanparcel(
								                       linkid, human_humanid, parcel_parcelkey, source_sourceid, createdts, 
											            createdby_userid, lastupdatedts, lastupdatedby_userid, deactivatedts, 
											            deactivatedby_userid, notes, linkedobjectrole_lorid)
												    VALUES (DEFAULT, %L, %L, %L, now(), 
												    		%L, now(), %L, NULL,
												            NULL, ''Created during person-human migration AUG 2021'', %L);',
										            		current_human_id, parcel_rec.parcelkey, defaultsource, 
								            				creationrobotuser, creationrobotuser,
								            				parcel_human_lorid
					            				);
				 				RAISE NOTICE 'Linked parcel with PK % to Human with PK %', parcel_rec.parcelkey, current_human_id;

				 			-- If no parcel exists, write record to migrationlog table
				 			ELSE
				 				RAISE NOTICE 'No parcel found to link';
				 				-- EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
									-- 				            logentryid, human_humanid, person_personid, error_code, notes, ts)
									-- 				    VALUES (DEFAULT, NULL, %L, %L, %L, now());',
									-- 				     pr.personid, 5, 
									-- 				    'UNABLE TO LINK PARCEL ' ||  parcel_rec.parcelkey || ' TO HUMAN ' || current_human_id
									-- 				);
		 				END IF;

			 			-- Next, check if this person's address is one of those linked any of the parcels to which he/she is connected, if so, do nothing
			 			-- but if their address is not associated with parcel in our current muni, then make a new record in the mailingaddress
			 			-- family and connect this fresh_human to a fresh address

			 			SELECT  addressid INTO mailing_rec
							FROM public.mailingaddress 
							INNER JOIN public.parcelmailingaddress 
								ON (mailingparcel_mailingid = addressid)
							WHERE mailingparcel_parcelid = parcel_rec.parcelkey
								AND bldgno ILIKE extractbuildingno(pr.address_street); -- NOTE: We're playing fast and loose
								-- with address matching: if the building numbers are the same, we assume it's the same address
								-- and don't write a new mailingaddress record. EDGE cases of folks having a separate mailing
								-- whose building numbers is exactly the same as their linked parcel are not addressed.
								-- THE PO box parsing process potentially makes this fraught so beware!

						IF FOUND
							THEN -- we've already got a link between the person and that person's address
								RAISE NOTICE 'Fresh human has legacy address already linked to a parcel: moving on';
								NULL;
							ELSE 	-- we don't have a mailing address that matches the person record's mailing address, 
									-- So write a new address and link it to our fresh human

								zip_parsed := cnf_parsezipcode(pr.address_zip);								
								SELECT id 
									FROM mailingcitystatezip 
									WHERE zip_code = zip_parsed AND list_type_id = 1 
									INTO citystatezip_rec;


								IF FOUND
									THEN --we've got a real zip code to attach to our new street
									RAISE NOTICE 'Fresh human has address with legitimite ZIP %', citystatezip_rec.id;
									street_freshstreetname := extractstreet(pr.address_street);
									RAISE NOTICE 'Extracted street from fresh address: %|<-SPACE CHECK', street_freshstreetname;

									IF street_freshstreetname IS NOT NULL
										THEN -- we have successfully extracted a street name

											SELECT streetid INTO street_freshid 
												FROM mailingstreet 
												WHERE citystatezip_cszipid = citystatezip_rec.id
													AND mailingstreet.name ILIKE street_freshstreetname;

											IF street_freshid IS NULL OR street_freshid = 0
												THEN -- no existing street with this same zip, so write new street

													-- Write new street and address records	
													EXECUTE format('
														INSERT INTO public.mailingstreet(
													            streetid, name, namevariantsarr, citystatezip_cszipid, notes, 
													            pobox)
													    VALUES (DEFAULT, %L, NULL, %L, %L, 
												    	        NULL);',
												    	        street_freshstreetname, citystatezip_rec.id, 'Migration pers-hum AUG-2021; Raw addr: '|| pr.address_street
									    	        );

									    	        -- Now get our fresh street ID for writing our building Number
									    	        SELECT currval('mailingstreet_streetid_seq') INTO street_freshid;
							    	        END IF; -- possibly write new street
							    	        -- with a street PK, we're ready to write to the mailingaddress base table
							    	        EXECUTE format('
							    	        	INSERT INTO public.mailingaddress(
											            addressid, bldgno, street_streetid, verifiedts, verifiedby_userid, 
											            verifiedsource_sourceid, source_sourceid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, NULL, 
											            NULL, %L, now(), %L, 
											            now(), %L, NULL, NULL, 
											            %L);',
											            extractbuildingno(pr.address_street), street_freshid,
											            defaultsource, creationrobotuser,
											            creationrobotuser,
											            'Migration AUG-2021: Raw addr: ' || pr.address_street
							    	        	);

							    	        SELECT currval('mailingaddress_addressid_seq') INTO address_freshid;

							    	        -- Now that we know the ID of the fresh mailing, we can link our fresh human via the old personid
							    	        -- to this new address we just made and got an ID for
							    	        EXECUTE format('INSERT INTO public.humanmailingaddress(
														            humanmailing_humanid, humanmailing_addressid, source_sourceid, 
														            createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, 
														            deactivatedts, deactivatedby_userid, notes, linkid, linkedobjectrole_lorid)
														    VALUES (%L, %L, %L, 
														            now(), %L, now(), %L, 
														            NULL, NULL, ''Created during person-human migration AUG 2021'', DEFAULT, %L);',
														            current_human_id, address_freshid, defaultsource,
														            creationrobotuser, creationrobotuser, human_mailing_lorid);
						    	        ELSE -- could not extract street
						    	        	RAISE NOTICE 'COULD NOT EXTRACT STREET; SKIPPING NEW ADDRESS INSERT ';
											EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
															            logentryid, human_humanid, person_personid, error_code, notes, ts)
															    VALUES (DEFAULT, %L, %L, %L, %L, now());',
															    current_human_id, pr.personid, 6,
															    'Street parsing failure on: ' ||  pr.address_street
															);
										END IF;


									ELSE -- malformed ZIP on legacy person, meaning we cannot write a new address
									RAISE NOTICE 'ZIP NOT FOUND: % ', zip_parsed;
										EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
														            logentryid, human_humanid, person_personid, error_code, notes, ts)
														    VALUES (DEFAULT, %L, %L, %L, %L, now());',
														    current_human_id, pr.personid, 3, 
														    'Zip not found in master file: ' || pr.address_zip
														);
								END IF; -- end check for legitimate zipcode found on old person address
						END IF; -- end check for existing address connection between person's parcel and ONE of that parcel's addresses
		 		END LOOP; -- end iteration over property-person records 

				RAISE NOTICE 'END PERSON RECORD';
		END LOOP; -- over legacy person table records

		RETURN human_rec_count;
	END;
$$;


--
-- TOC entry 516 (class 1255 OID 172797)
-- Name: migratepropertytoparcel(integer, integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.migratepropertytoparcel(creationrobotuser integer, defaultsource integer, cityid integer, municodetarget integer, parceladdr_lorid integer) RETURNS integer
    LANGUAGE plpgsql
    AS $$
	DECLARE
	 	pr RECORD;
	 	deacts TIMESTAMP WITH TIME ZONE;
	 	deacuser INTEGER;
	 	extractedbldg TEXT;
	 	extractedstreet TEXT;
	 	addr_range TEXT;
	 	addr_range_start TEXT;
	 	addr_range_end TEXT;
	 	addr_range_start_no INTEGER;
	 	addr_range_end_no INTEGER;
	 	addr_range_cursor INTEGER;
	 	addr_range_arr TEXT[];
	 	bldgno TEXT;
	 	maid INTEGER; -- mailing address ID
	 	current_street_id INTEGER;
	 	buildingcount INTEGER;

	BEGIN
	buildingcount := 0;
		RAISE NOTICE 'starting property migration...';
		FOR pr IN SELECT 		propertyid, municipality_municode, parid, lotandblock, address, 
						       usegroup, constructiontype, countycode, notes, addr_city, addr_state, 
						       addr_zip, ownercode, propclass, lastupdated, lastupdatedby, locationdescription, 
						       bobsource_sourceid, unfitdatestart, unfitdatestop, unfitby_userid, 
						       abandoneddatestart, abandoneddatestop, abandonedby_userid, vacantdatestart, 
						       vacantdatestop, vacantby_userid, condition_intensityclassid, 
						       landbankprospect_intensityclassid, landbankheld, active, nonaddressable, 
						       usetype_typeid, creationts
						  FROM public.property WHERE municipality_municode=municodetarget
		 
		LOOP -- over properties in legacy table
			RAISE NOTICE 'LOOP ITERATION: %; propertyid %; parcel id  %', pr.address, pr.propertyid, pr.parid; 
			-- clear our iterables
			addr_range_arr := ARRAY[]::text[];
			addr_range := NULL;
			-- check for deactivation
			IF NOT pr.active 
			THEN 
				deacts := now();
				deacuser := creationrobotuser; -- the cogbot
			ELSE
				deacts := NULL;
				deacuser := NULL;
			END IF;

			-- TRY manual string concatenation method

			EXECUTE 'INSERT INTO public.parcel(
						            parcelkey, parcelidcnty, source_sourceid, createdts, createdby_userid, 
						            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
						            notes, muni_municode, lotandblock)  VALUES ('
					|| pr.propertyid
					|| ',' 
					|| quote_nullable(unifyspacesandtrim(pr.parid))
					|| ',' 
					|| quote_nullable(pr.bobsource_sourceid)
					|| ',' 
					|| quote_nullable(pr.creationts)
					|| ',' 
					|| creationrobotuser
					|| ',' 
	                || quote_nullable(pr.lastupdated)
					|| ',' 
	                || quote_nullable(pr.lastupdatedby)
					|| ',' 
	                || quote_nullable(deacts)
					|| ',' 
	                || quote_nullable(deacuser)
					|| ',' 
	              	|| quote_nullable(unifyspacesandtrim(pr.notes))
					|| ',' 
	              	|| pr.municipality_municode
					|| ',' 
	              	|| quote_nullable(pr.lotandblock) 	
					|| ');';

			EXECUTE format('INSERT INTO public.parcelinfo(
					            parcelinfoid, parcel_parcelkey, usegroup, constructiontype, countycode, 
					            notes, ownercode, propclass, locationdescription, bobsource_sourceid, 
					            unfitdatestart, unfitdatestop, unfitby_userid, abandoneddatestart, 
					            abandoneddatestop, abandonedby_userid, vacantdatestart, vacantdatestop, 
					            vacantby_userid, condition_intensityclassid, landbankprospect_intensityclassid, 
					            landbankheld, nonaddressable, usetype_typeid, createdts, createdby_userid, 
					            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid)
					    VALUES (DEFAULT, %L, %L, %L, %L, 
					            NULL, %L, %L, NULL, %L, 
					            %L, %L, %L, %L, 
					            %L, %L, %L, %L, 
					            %L, %L, %L, 
					            %L, %L, %L, %L, %L, 
					            %L, %L, %L, %L);',
				            pr.propertyid, pr.usegroup, pr.constructiontype, pr.countycode,
				            pr.ownercode, pr.propclass, pr.bobsource_sourceid,
				            pr.unfitdatestart, pr.unfitdatestop, pr.unfitby_userid, pr.abandoneddatestart, 
			            	pr.abandoneddatestop, pr.abandonedby_userid, pr.vacantdatestart, pr.vacantdatestop, 
			            	pr.vacantby_userid, pr.condition_intensityclassid, pr.landbankprospect_intensityclassid,
			            	pr.landbankheld, pr.nonaddressable, pr.usetype_typeid, pr.creationts, creationrobotuser,
			            	pr.creationts, creationrobotuser, deacts, deacuser
						);

			-- parse address into street and bldgno 
			extractedstreet := extractstreet(pr.address);
			-- See if street is in the table already, if so, get its ID
			
			SELECT streetid INTO current_street_id
				FROM public.mailingstreet 
				WHERE name ILIKE '%'||extractedstreet||'%'
					AND citystatezip_cszipid = cityid; -- only look for existing street matches within the zip of this muni
			
			RAISE NOTICE 'PropID: %; Has street % been found? Street ID: %', pr.address, extractedstreet, current_street_id;
			IF extractedstreet IS NOT NULL
				THEN
					IF FOUND
					THEN -- we have an existing street

						NULL; -- we'll use the current_street_id for all the address writes

					ELSE -- we don't have a record of this street, so write it and grab its ID
						-- write street into mailingstreet
						EXECUTE format('INSERT INTO public.mailingstreet(
										            	streetid, name, namevariantsarr, citystatezip_cszipid, notes, 
		            									pobox)
										    VALUES (DEFAULT, %L, NULL, %L, %L, 
										            NULL);',
										            		  unifyspacesandtrim(extractedstreet), cityid, 'MIGRATION AUG-2021; Raw addr: ' || pr.address );
						-- fetch fresh street id
						SELECT currval('mailingstreet_streetid_seq') INTO current_street_id;
					END IF;
					
					-- extract addresses with a - in there somewhere
					addr_range := substring(pr.address from '\d+\W?-\d+');
					RAISE NOTICE 'FOUND RANGE ADDRESS: %', addr_range;

					IF 
						addr_range IS NOT NULL
					THEN -- build range
						addr_range_start := substring(addr_range from '\d+');
						addr_range_end := substring(addr_range from '\d+\W?-(\d+)');
						addr_range_start_no := CAST (addr_range_start AS INTEGER);
						addr_range_end_no := CAST (addr_range_end AS INTEGER);
						addr_range_cursor := addr_range_start_no;
						WHILE  
							addr_range_cursor <= addr_range_end_no
						LOOP
							addr_range_arr := array_append(addr_range_arr, addr_range_cursor::text);
							RAISE NOTICE 'ADDR RANGE CURSOR VAL: %; ARRAY STATUS: % ', addr_range_cursor, addr_range_arr;
							-- step up by 2 building nos per even/odd numbering schema
							addr_range_cursor := addr_range_cursor + 2; 

						END LOOP;

					ELSE -- NORMAL building no
						extractedbldg := extractbuildingno(pr.address);
						RAISE NOTICE 'FOUND NORMAL BUILDING pr.address: %, extracted no: %', pr.address, extractedbldg;
						addr_range_arr := array_append(addr_range_arr, extractedbldg);
						
					END IF;

					RAISE NOTICE 'INSERTING MAILING ADDRESSES ARRAY: % ', addr_range_arr;

					FOREACH bldgno IN ARRAY addr_range_arr
					LOOP -- over each address in the array
						RAISE NOTICE 'INSERTING BLDG NO: %', bldgno;
						EXECUTE format('INSERT INTO public.mailingaddress(
									            addressid, bldgno, street_streetid, verifiedts, verifiedby_userid, 
									            verifiedsource_sourceid, source_sourceid, createdts, createdby_userid, 
									            lastupdatedts, lastupdatedby_userid, notes)
									    VALUES (DEFAULT, %L, %L, NULL, NULL, 
									            NULL, %L, %L, %L, 
									            %L, %L, %L);',
									                      unifyspacesandtrim(bldgno), current_street_id,
							                     defaultsource, now(), creationrobotuser,
							                    now(), creationrobotuser, 'MIGRATION AUG-2021; Raw Addr: ' || pr.address);
						buildingcount := buildingcount + 1;

						-- get our fresh mailing address ID
						SELECT currval('mailingaddress_addressid_seq') INTO maid;

						-- Connect our current parcel with each
						EXECUTE format('INSERT INTO public.parcelmailingaddress(
										            mailingparcel_parcelid, mailingparcel_mailingid, source_sourceid, 
										            createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, 
										            deactivatedts, deactivatedby_userid, notes, linkid, linkedobjectrole_lorid)
										    VALUES (%L, %L, %L, 
										            now(), %L, now(), %L, 
										            NULL, NULL, ''Created during parcel migration JUL-21'', DEFAULT, %L);',
									            	pr.propertyid, maid, defaultsource,
									            	creationrobotuser, creationrobotuser, 
		            								parceladdr_lorid);
						RAISE NOTICE 'LinkID complete: % ', maid;
					END LOOP; -- over each building Number extracted from the original property address 
				ELSE -- we don't have a well formed street
					EXECUTE format('INSERT INTO public.parcelmigrationlog(
								            logentryid, property_id, parcel_id, error_code, notes, ts)
								    VALUES (DEFAULT, %L, NULL, 1, ''IMPROPERLY FORMED ADDRESS'', now());',
								    pr.propertyid);
				END IF; -- check of malformed property
		END LOOP; -- over properties in the legacy table
		RETURN buildingcount;
	END;
$$;


--
-- TOC entry 514 (class 1255 OID 144039)
-- Name: resetsequences(integer); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.resetsequences(incr integer DEFAULT 3) RETURNS void
    LANGUAGE plpgsql
    AS $$
declare
    propertyid_ integer;
    unitid_     integer;
    caseid_     integer;
    personid_   integer;
BEGIN
    propertyid_ := MAX(propertyid) FROM property;
        propertyid_ = propertyid_ + incr;
        PERFORM setval('propertyid_seq', propertyid_);
    unitid_ := MAX(unitid) FROM propertyunit;
        unitid_ = unitid_ + incr;
        PERFORM setval('propertunit_unitid_seq', unitid_);
    caseid_ := MAX(caseid) FROM cecase;
        caseid_ = caseid_ + incr;
        PERFORM setval('cecase_caseid_seq', unitid_);
    personid_ := MAX(personid) FROM person;
        personid_ = personid_ + incr;
        PERFORM setval('person_personidseq', personid_);
END;
$$;


--
-- TOC entry 518 (class 1255 OID 172972)
-- Name: unifyspacechars(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.unifyspacechars(chaostext text) RETURNS text
    LANGUAGE plpgsql
    AS $$

	BEGIN
		RETURN regexp_replace(chaostext, '[\s\u180e\u200B\u200C\u200D\u2060\uFEFF\u00a0]',' ','g'); 
	END;
$$;


--
-- TOC entry 520 (class 1255 OID 172973)
-- Name: unifyspacesandtrim(text); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.unifyspacesandtrim(chaostext text) RETURNS text
    LANGUAGE plpgsql
    AS $_$

	BEGIN
		-- start by replacing all spaces of any kind with latin 0020
		-- then trim that normal space from the end and beginning
		RETURN regexp_replace(
					regexp_replace(
						regexp_replace(chaostext, '[\s\u180e\u200B\u200C\u200D\u2060\uFEFF\u00a0]',' ','g'), 
						'\s+$',''),
						'^\s+','');
	END;
$_$;


--
-- TOC entry 186 (class 1259 OID 65224)
-- Name: actionrqstissuetype_issuetypeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.actionrqstissuetype_issuetypeid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 382 (class 1259 OID 155173)
-- Name: blobbytes_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.blobbytes_seq
    START WITH 10
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 383 (class 1259 OID 155175)
-- Name: blobbytes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.blobbytes (
    bytesid integer DEFAULT nextval('public.blobbytes_seq'::regclass) NOT NULL,
    createdts timestamp with time zone,
    blob bytea,
    uploadedby_userid integer,
    filename text
);


--
-- TOC entry 271 (class 1259 OID 95398)
-- Name: blobtype; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.blobtype (
    typeid integer NOT NULL,
    typetitle text,
    icon_iconid integer,
    contenttypestring text,
    browserviewable boolean DEFAULT false,
    notes text,
    fileextensionsarr text[]
);


--
-- TOC entry 245 (class 1259 OID 66105)
-- Name: blockcategory_categoryid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.blockcategory_categoryid_seq
    START WITH 8000
    INCREMENT BY 1
    MINVALUE 8000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 296 (class 1259 OID 106743)
-- Name: bobsourceid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.bobsourceid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 297 (class 1259 OID 106745)
-- Name: bobsource; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.bobsource (
    sourceid integer DEFAULT nextval('public.bobsourceid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    creator integer,
    muni_municode integer NOT NULL,
    userattributable boolean DEFAULT true,
    active boolean DEFAULT true,
    notes text
);


--
-- TOC entry 196 (class 1259 OID 65329)
-- Name: ceactionrequest_requestid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ceactionrequest_requestid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 197 (class 1259 OID 65331)
-- Name: ceactionrequest; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ceactionrequest (
    requestid integer DEFAULT nextval('public.ceactionrequest_requestid_seq'::regclass) NOT NULL,
    requestpubliccc integer,
    muni_municode integer NOT NULL,
    property_propertyid integer,
    issuetype_issuetypeid integer NOT NULL,
    actrequestor_requestorid integer NOT NULL,
    cecase_caseid integer,
    submittedtimestamp timestamp with time zone NOT NULL,
    dateofrecord timestamp with time zone NOT NULL,
    notataddress boolean,
    addressofconcern text,
    requestdescription text NOT NULL,
    isurgent boolean DEFAULT false,
    anonymityrequested boolean DEFAULT false,
    coginternalnotes text,
    muniinternalnotes text,
    publicexternalnotes text,
    status_id integer NOT NULL,
    caseattachmenttimestamp timestamp with time zone,
    paccenabled boolean DEFAULT true,
    caseattachment_userid integer,
    usersubmitter_userid integer,
    active boolean DEFAULT true,
    parcel_parcelkey integer
);


--
-- TOC entry 187 (class 1259 OID 65226)
-- Name: ceactionrequestissuetype; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ceactionrequestissuetype (
    issuetypeid integer DEFAULT nextval('public.actionrqstissuetype_issuetypeid_seq'::regclass) NOT NULL,
    typename text,
    typedescription text,
    muni_municode integer,
    notes text,
    intensity_classid integer,
    active boolean DEFAULT true
);


--
-- TOC entry 386 (class 1259 OID 155294)
-- Name: ceactionrequestpdfdoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ceactionrequestpdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    ceactionrequest_requestid integer NOT NULL
);


--
-- TOC entry 274 (class 1259 OID 95447)
-- Name: ceactionrequestphotodoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ceactionrequestphotodoc (
    photodoc_photodocid integer NOT NULL,
    ceactionrequest_requestid integer NOT NULL
);


--
-- TOC entry 266 (class 1259 OID 74852)
-- Name: ceactionrequeststatus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ceactionrequeststatus (
    statusid integer NOT NULL,
    title text,
    description text,
    icon_iconid integer
);


--
-- TOC entry 204 (class 1259 OID 65436)
-- Name: cecase_caseid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cecase_caseid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 205 (class 1259 OID 65438)
-- Name: cecase; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cecase (
    caseid integer DEFAULT nextval('public.cecase_caseid_seq'::regclass) NOT NULL,
    cecasepubliccc integer NOT NULL,
    property_propertyid integer,
    login_userid integer,
    casename text,
    originationdate timestamp with time zone,
    closingdate timestamp with time zone,
    creationtimestamp timestamp with time zone,
    notes text,
    paccenabled boolean DEFAULT true,
    allowuplinkaccess boolean DEFAULT true,
    propertyinfocase boolean DEFAULT false,
    personinfocase_personid integer,
    bobsource_sourceid integer,
    active boolean DEFAULT true,
    lastupdatedby_userid integer,
    lastupdatedts timestamp with time zone,
    parcelunit_unitid integer,
    parcel_parcelkey integer
);


--
-- TOC entry 4628 (class 0 OID 0)
-- Dependencies: 205
-- Name: TABLE cecase; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.cecase IS 'I can comment here and see there!';


--
-- TOC entry 4629 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN cecase.casename; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON COLUMN public.cecase.casename IS 'Column Comment';


--
-- TOC entry 285 (class 1259 OID 106167)
-- Name: cecasephasechangerule_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cecasephasechangerule_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 384 (class 1259 OID 155212)
-- Name: cecasephotodoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cecasephotodoc (
    photodoc_photodocid integer NOT NULL,
    cecase_caseid integer NOT NULL
);


--
-- TOC entry 460 (class 1259 OID 206175)
-- Name: cecasepin; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cecasepin (
    cecase_caseid integer NOT NULL,
    pinnedby_userid integer NOT NULL,
    createdts timestamp with time zone DEFAULT now(),
    deactivatedts timestamp with time zone
);


--
-- TOC entry 281 (class 1259 OID 103945)
-- Name: cecasestatusicon; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cecasestatusicon (
    iconid integer NOT NULL,
    status public.casephase NOT NULL
);


--
-- TOC entry 206 (class 1259 OID 65467)
-- Name: ceevent_eventid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ceevent_eventid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 202 (class 1259 OID 65418)
-- Name: ceeventcategory_categoryid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ceeventcategory_categoryid_seq
    START WITH 100000
    INCREMENT BY 1
    MINVALUE 100000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 293 (class 1259 OID 106585)
-- Name: ceeventproposal_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ceeventproposal_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 294 (class 1259 OID 106695)
-- Name: ceeventproposalimplementation_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ceeventproposalimplementation_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 227 (class 1259 OID 65759)
-- Name: checklist_checklistid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.checklist_checklistid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 229 (class 1259 OID 65770)
-- Name: chkliststiceid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.chkliststiceid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 310 (class 1259 OID 107352)
-- Name: choice_choiceid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.choice_choiceid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 311 (class 1259 OID 107354)
-- Name: choice; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.choice (
    choiceid integer DEFAULT nextval('public.choice_choiceid_seq'::regclass) NOT NULL,
    title text,
    description text,
    eventcat_catid integer,
    addeventcat boolean DEFAULT true,
    eventrule_ruleid integer,
    addeventrule boolean DEFAULT true,
    relativeorder integer NOT NULL,
    active boolean DEFAULT true,
    minimumrequireduserranktoview integer DEFAULT 3,
    minimumrequireduserranktochoose integer DEFAULT 3,
    icon_iconid integer,
    worflowpagetriggerconstantvar text
);


--
-- TOC entry 307 (class 1259 OID 107235)
-- Name: eventproposal_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.eventproposal_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 308 (class 1259 OID 107237)
-- Name: choicedirective; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.choicedirective (
    directiveid integer DEFAULT nextval('public.eventproposal_seq'::regclass) NOT NULL,
    title text,
    overalldescription text,
    creator_userid integer,
    directtodefaultmuniceo boolean DEFAULT true,
    directtodefaultmunistaffer boolean DEFAULT false,
    directtodeveloper boolean DEFAULT false,
    executechoiceiflonewolf boolean DEFAULT false,
    applytoclosedentities boolean DEFAULT true,
    instantiatemultiple boolean DEFAULT true,
    inactivategeneventoneval boolean DEFAULT false,
    maintainreldatewindow boolean DEFAULT true,
    autoinactivateonbobclose boolean DEFAULT true,
    autoinactiveongeneventinactivation boolean DEFAULT true,
    minimumrequireduserranktoview integer DEFAULT 3,
    minimumrequireduserranktoevaluate integer DEFAULT 3,
    active boolean DEFAULT true,
    icon_iconid integer,
    relativeorder integer DEFAULT 1,
    directtomunisysadmin boolean DEFAULT false,
    requiredevaluationforbobclose boolean DEFAULT true,
    forcehideprecedingproposals boolean DEFAULT false,
    forcehidetrailingproposals boolean DEFAULT false,
    refusetobehidden boolean DEFAULT false
);


--
-- TOC entry 312 (class 1259 OID 107378)
-- Name: choicedirectivechoice; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.choicedirectivechoice (
    choice_choiceid integer NOT NULL,
    directive_directiveid integer NOT NULL
);


--
-- TOC entry 328 (class 1259 OID 109086)
-- Name: choicedirectivedirectiveset; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.choicedirectivedirectiveset (
    directiveset_setid integer NOT NULL,
    directive_dirid integer NOT NULL
);


--
-- TOC entry 323 (class 1259 OID 108885)
-- Name: choicedirectivesetid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.choicedirectivesetid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 324 (class 1259 OID 108887)
-- Name: choicedirectiveset; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.choicedirectiveset (
    directivesetid integer DEFAULT nextval('public.choicedirectivesetid_seq'::regclass) NOT NULL,
    title text,
    description text
);


--
-- TOC entry 295 (class 1259 OID 106697)
-- Name: choiceproposal; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.choiceproposal (
    proposalid integer DEFAULT nextval('public.ceeventproposalimplementation_seq'::regclass) NOT NULL,
    directive_directiveid integer,
    generatingevent_eventid integer,
    initiator_userid integer,
    responderintended_userid integer,
    activateson timestamp with time zone,
    expireson timestamp with time zone,
    responderactual_userid integer,
    rejectproposal boolean DEFAULT false,
    responsetimestamp timestamp with time zone,
    responseevent_eventid integer,
    active boolean DEFAULT true,
    notes text,
    relativeorder integer,
    occperiod_periodid integer,
    cecase_caseid integer,
    chosen_choiceid integer
);


--
-- TOC entry 216 (class 1259 OID 65594)
-- Name: citation_citationid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.citation_citationid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 244 (class 1259 OID 66080)
-- Name: citation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.citation (
    citationid integer DEFAULT nextval('public.citation_citationid_seq'::regclass) NOT NULL,
    citationno text,
    status_statusid integer,
    origin_courtentity_entityid integer NOT NULL,
    login_userid integer NOT NULL,
    dateofrecord timestamp with time zone NOT NULL,
    isactive boolean DEFAULT true,
    notes text,
    officialtext text,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    filingtype_typeid integer
);


--
-- TOC entry 396 (class 1259 OID 163825)
-- Name: citationcitationstatus_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.citationcitationstatus_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 397 (class 1259 OID 163830)
-- Name: citationcitationstatus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.citationcitationstatus (
    citationstatusid integer DEFAULT nextval('public.citationcitationstatus_seq'::regclass) NOT NULL,
    citation_citationid integer NOT NULL,
    citationstatus_statusid integer NOT NULL,
    dateofrecord timestamp with time zone NOT NULL,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    courtentity_entityid integer
);


--
-- TOC entry 431 (class 1259 OID 173067)
-- Name: citationdockethuman_linkid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.citationdockethuman_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 429 (class 1259 OID 173019)
-- Name: citationdocketno_docketid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.citationdocketno_docketid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 430 (class 1259 OID 173021)
-- Name: citationdocketno; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.citationdocketno (
    docketid integer DEFAULT nextval('public.citationdocketno_docketid_seq'::regclass) NOT NULL,
    docketno text NOT NULL,
    dateofrecord timestamp with time zone NOT NULL,
    courtentity_entityid integer NOT NULL,
    createdts timestamp with time zone NOT NULL,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    citation_citationid integer NOT NULL
);


--
-- TOC entry 432 (class 1259 OID 173069)
-- Name: citationdocketnohuman; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.citationdocketnohuman (
    linkid integer DEFAULT nextval('public.citationdockethuman_linkid_seq'::regclass) NOT NULL,
    docketno_docketid integer NOT NULL,
    citationhuman_linkid integer NOT NULL,
    notes text
);


--
-- TOC entry 399 (class 1259 OID 163902)
-- Name: citationevent; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.citationevent (
    citation_citationid integer NOT NULL,
    event_eventid integer NOT NULL
);


--
-- TOC entry 433 (class 1259 OID 173088)
-- Name: citationfilingtype_typeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.citationfilingtype_typeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 434 (class 1259 OID 173090)
-- Name: citationfilingtype; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.citationfilingtype (
    typeid integer DEFAULT nextval('public.citationfilingtype_typeid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    muni_municode integer NOT NULL,
    active boolean DEFAULT true
);


--
-- TOC entry 394 (class 1259 OID 163727)
-- Name: citationhuman_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.citationhuman_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 395 (class 1259 OID 163729)
-- Name: citationhuman; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.citationhuman (
    linkid integer DEFAULT nextval('public.citationhuman_seq'::regclass) NOT NULL,
    human_humanid integer,
    citation_citationid integer,
    source_sourceid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    linkedobjectrole_lorid integer
);


--
-- TOC entry 398 (class 1259 OID 163864)
-- Name: citationphotodoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.citationphotodoc (
    photodoc_photodocid integer NOT NULL,
    citation_citationid integer NOT NULL
);


--
-- TOC entry 240 (class 1259 OID 65996)
-- Name: citationstatus_statusid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.citationstatus_statusid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 241 (class 1259 OID 65998)
-- Name: citationstatus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.citationstatus (
    statusid integer DEFAULT nextval('public.citationstatus_statusid_seq'::regclass) NOT NULL,
    statusname text NOT NULL,
    description text NOT NULL,
    icon_iconid integer,
    editsforbidden boolean DEFAULT true,
    eventrule_ruleid integer,
    courtentity_entityid integer,
    displayorder integer DEFAULT 1,
    terminalstatus boolean DEFAULT false
);


--
-- TOC entry 242 (class 1259 OID 66032)
-- Name: citationviolation_cvid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.citationviolation_cvid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 243 (class 1259 OID 66034)
-- Name: citationviolation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.citationviolation (
    citationviolationid integer DEFAULT nextval('public.citationviolation_cvid_seq'::regclass) NOT NULL,
    citation_citationid integer NOT NULL,
    codeviolation_violationid integer NOT NULL,
    createdts timestamp with time zone,
    lastupdatedts timestamp with time zone,
    deactivatedts timestamp with time zone,
    status public.citationviolationstatus,
    createdby_userid integer,
    lastupdatedby_userid integer,
    deactivatedby_userid integer,
    notes text,
    source_sourceid integer
);


--
-- TOC entry 210 (class 1259 OID 65534)
-- Name: codeelement_elementid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.codeelement_elementid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 211 (class 1259 OID 65536)
-- Name: codeelement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.codeelement (
    elementid integer DEFAULT nextval('public.codeelement_elementid_seq'::regclass) NOT NULL,
    codesource_sourceid integer NOT NULL,
    ordchapterno integer NOT NULL,
    ordchaptertitle text,
    ordsecnum text,
    ordsectitle text,
    ordsubsecnum text,
    ordsubsectitle text,
    ordtechnicaltext text NOT NULL,
    ordhumanfriendlytext text,
    resourceurl text,
    guideentryid integer,
    notes text,
    legacyid integer,
    ordsubsubsecnum text,
    useinjectedvalues boolean,
    lastupdatedts timestamp with time zone DEFAULT now(),
    createdby_userid integer,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    createdts timestamp with time zone,
    subsubsectitle text
);


--
-- TOC entry 260 (class 1259 OID 66626)
-- Name: codeelementguide_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.codeelementguide_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 261 (class 1259 OID 66628)
-- Name: codeelementguide; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.codeelementguide (
    guideentryid integer DEFAULT nextval('public.codeelementguide_id_seq'::regclass) NOT NULL,
    category text NOT NULL,
    subcategory text,
    description text,
    enforcementguidelines text,
    inspectionguidelines text,
    priority boolean,
    icon_iconid integer
);


--
-- TOC entry 351 (class 1259 OID 144173)
-- Name: codeelementinjectedvalue_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.codeelementinjectedvalue_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 352 (class 1259 OID 144188)
-- Name: codeelementinjectedvalue; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.codeelementinjectedvalue (
    injectedvalueid integer DEFAULT nextval('public.codeelementinjectedvalue_seq'::regclass) NOT NULL,
    value text NOT NULL,
    injectionorder integer,
    codelement_eleid integer NOT NULL,
    codeset_codesetid integer NOT NULL,
    creationts timestamp with time zone,
    notes text,
    active boolean DEFAULT true
);


--
-- TOC entry 198 (class 1259 OID 65362)
-- Name: codeset_codesetid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.codeset_codesetid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 199 (class 1259 OID 65364)
-- Name: codeset; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.codeset (
    codesetid integer DEFAULT nextval('public.codeset_codesetid_seq'::regclass) NOT NULL,
    name text,
    description text,
    municipality_municode integer,
    active boolean DEFAULT true
);


--
-- TOC entry 212 (class 1259 OID 65557)
-- Name: codesetelement_elementid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.codesetelement_elementid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 213 (class 1259 OID 65559)
-- Name: codesetelement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.codesetelement (
    codesetelementid integer DEFAULT nextval('public.codesetelement_elementid_seq'::regclass) NOT NULL,
    codeset_codesetid integer NOT NULL,
    codelement_elementid integer NOT NULL,
    elementmaxpenalty numeric,
    elementminpenalty numeric,
    elementnormpenalty numeric NOT NULL,
    penaltynotes text,
    normdaystocomply integer NOT NULL,
    daystocomplynotes text,
    munispecificnotes text,
    defaultseverityclass_classid integer,
    fee_feeid integer,
    defaultviolationdescription text,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer
);


--
-- TOC entry 284 (class 1259 OID 105013)
-- Name: codesetelementclass_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.codesetelementclass_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 208 (class 1259 OID 65514)
-- Name: codesource_sourceid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.codesource_sourceid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 209 (class 1259 OID 65516)
-- Name: codesource; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.codesource (
    sourceid integer DEFAULT nextval('public.codesource_sourceid_seq'::regclass) NOT NULL,
    name text NOT NULL,
    year integer NOT NULL,
    description text,
    isactive boolean DEFAULT true,
    url text,
    notes text
);


--
-- TOC entry 217 (class 1259 OID 65621)
-- Name: codeviolation_violationid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.codeviolation_violationid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 218 (class 1259 OID 65623)
-- Name: codeviolation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.codeviolation (
    violationid integer DEFAULT nextval('public.codeviolation_violationid_seq'::regclass) NOT NULL,
    codesetelement_elementid integer NOT NULL,
    cecase_caseid integer NOT NULL,
    dateofrecord timestamp with time zone,
    entrytimestamp timestamp with time zone NOT NULL,
    stipulatedcompliancedate timestamp with time zone NOT NULL,
    actualcompliancedate timestamp with time zone,
    penalty numeric NOT NULL,
    description text NOT NULL,
    notes text,
    legacyimport boolean DEFAULT false,
    compliancetimestamp timestamp with time zone,
    complianceuser integer,
    severity_classid integer,
    createdby integer,
    compliancetfexpiry_proposalid integer,
    lastupdatedts timestamp with time zone,
    lastupdated_userid integer,
    active boolean DEFAULT true,
    compliancenote text,
    nullifiedts timestamp with time zone,
    nullifiedby integer,
    bobsource_sourceid integer,
    transferredts timestamp with time zone,
    transferredby_userid integer,
    transferredtocecase_caseid integer
);


--
-- TOC entry 4630 (class 0 OID 0)
-- Dependencies: 218
-- Name: TABLE codeviolation; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.codeviolation IS 'save commets';


--
-- TOC entry 387 (class 1259 OID 155309)
-- Name: codeviolationpdfdoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.codeviolationpdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    codeviolation_violationid integer NOT NULL
);


--
-- TOC entry 272 (class 1259 OID 95411)
-- Name: codeviolationphotodoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.codeviolationphotodoc (
    photodoc_photodocid integer NOT NULL,
    codeviolation_violationid integer NOT NULL
);


--
-- TOC entry 282 (class 1259 OID 104986)
-- Name: codeviolationseverityclass_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.codeviolationseverityclass_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 201 (class 1259 OID 65401)
-- Name: coglog_logeentryid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.coglog_logeentryid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 362 (class 1259 OID 154320)
-- Name: contactemail_emailid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.contactemail_emailid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 363 (class 1259 OID 154322)
-- Name: contactemail; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.contactemail (
    emailid integer DEFAULT nextval('public.contactemail_emailid_seq'::regclass) NOT NULL,
    human_humanid integer NOT NULL,
    emailaddress text NOT NULL,
    bouncets timestamp with time zone,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    priority integer DEFAULT 1
);


--
-- TOC entry 360 (class 1259 OID 154279)
-- Name: contactphone_phoneid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.contactphone_phoneid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 361 (class 1259 OID 154281)
-- Name: contactphone; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.contactphone (
    phoneid integer DEFAULT nextval('public.contactphone_phoneid_seq'::regclass) NOT NULL,
    human_humanid integer NOT NULL,
    phonenumber text NOT NULL,
    phoneext integer,
    phonetype_typeid integer,
    disconnectts timestamp with time zone,
    disconnect_userid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    priority integer DEFAULT 1
);


--
-- TOC entry 358 (class 1259 OID 154268)
-- Name: contactphonetype_typeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.contactphonetype_typeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 359 (class 1259 OID 154270)
-- Name: contactphonetype; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.contactphonetype (
    phonetypeid integer DEFAULT nextval('public.contactphonetype_typeid_seq'::regclass) NOT NULL,
    title text,
    createdts timestamp with time zone,
    deactivatedts timestamp with time zone
);


--
-- TOC entry 214 (class 1259 OID 65578)
-- Name: courtentity_entityid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.courtentity_entityid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 215 (class 1259 OID 65580)
-- Name: courtentity; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.courtentity (
    entityid integer DEFAULT nextval('public.courtentity_entityid_seq'::regclass) NOT NULL,
    entityofficialnum text,
    jurisdictionlevel text NOT NULL,
    name text NOT NULL,
    address_street text NOT NULL,
    address_city text NOT NULL,
    address_zip text NOT NULL,
    address_state text NOT NULL,
    county text,
    phone text,
    url text,
    notes text,
    judgename text
);


--
-- TOC entry 277 (class 1259 OID 95613)
-- Name: dbpatch; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.dbpatch (
    patchnum integer NOT NULL,
    patchfilename text,
    datepublished timestamp without time zone,
    patchauthor text,
    notes text
);


--
-- TOC entry 207 (class 1259 OID 65469)
-- Name: event; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.event (
    eventid integer DEFAULT nextval('public.ceevent_eventid_seq'::regclass) NOT NULL,
    category_catid integer NOT NULL,
    cecase_caseid integer,
    createdts timestamp with time zone,
    eventdescription text,
    createdby_userid integer NOT NULL,
    active boolean DEFAULT true,
    notes text,
    occperiod_periodid integer,
    timestart timestamp with time zone,
    timeend timestamp with time zone,
    lastupdatedby_userid integer,
    lastupdatedts timestamp with time zone,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    parcel_parcelkey integer
);


--
-- TOC entry 203 (class 1259 OID 65420)
-- Name: eventcategory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.eventcategory (
    categoryid integer DEFAULT nextval('public.ceeventcategory_categoryid_seq'::regclass) NOT NULL,
    categorytype public.eventtype NOT NULL,
    title text,
    description text,
    notifymonitors boolean DEFAULT false,
    hidable boolean DEFAULT false,
    icon_iconid integer,
    relativeorderwithintype integer DEFAULT 0,
    relativeorderglobal integer DEFAULT 0,
    hosteventdescriptionsuggtext text,
    directive_directiveid integer,
    defaultdurationmins integer,
    active boolean DEFAULT true,
    userrankminimumtoenact integer DEFAULT 3,
    userrankminimumtoview integer DEFAULT 3,
    userrankminimumtoupdate integer DEFAULT 3,
    rolefloorenact public.role,
    rolefloorview public.role,
    rolefloorupdate public.role,
    prioritygreenbufferdays integer DEFAULT 0
);


--
-- TOC entry 458 (class 1259 OID 206029)
-- Name: eventemission_emissionid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.eventemission_emissionid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 459 (class 1259 OID 206065)
-- Name: eventemission; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.eventemission (
    emissionid integer DEFAULT nextval('public.eventemission_emissionid_seq'::regclass) NOT NULL,
    emissionenum public.eventemissionenum NOT NULL,
    emissionts timestamp with time zone NOT NULL,
    event_eventid integer NOT NULL,
    emittedby_userid integer NOT NULL,
    emitter_novid integer,
    emitter_citationid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    emissionresponsets timestamp with time zone,
    emissionresponse_userid integer,
    notes text
);


--
-- TOC entry 403 (class 1259 OID 163993)
-- Name: eventhuman_linkid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.eventhuman_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 404 (class 1259 OID 163995)
-- Name: eventhuman; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.eventhuman (
    linkid integer DEFAULT nextval('public.eventhuman_linkid_seq'::regclass) NOT NULL,
    event_eventid integer NOT NULL,
    human_humanid integer NOT NULL,
    linkedobjectrole_lorid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    source_sourceid integer
);


--
-- TOC entry 309 (class 1259 OID 107307)
-- Name: eventrule; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.eventrule (
    ruleid integer DEFAULT nextval('public.cecasephasechangerule_seq'::regclass) NOT NULL,
    title text,
    description text,
    requiredeventtype public.eventtype,
    forbiddeneventtype public.eventtype,
    requiredeventcat_catid integer,
    requiredeventcatupperboundtypeintorder boolean DEFAULT false,
    requiredeventcatupperboundglobalorder boolean DEFAULT false,
    forbiddeneventcat_catid integer,
    forbiddeneventcatupperboundtypeintorder boolean DEFAULT false,
    forbiddeneventcatupperboundglobalorder boolean DEFAULT false,
    mandatorypassreqtocloseentity boolean DEFAULT true,
    autoremoveonentityclose boolean DEFAULT true,
    triggeredeventcatonpass integer,
    triggeredeventcatonfail integer,
    active boolean DEFAULT true,
    notes text,
    requiredeventcatthresholdtypeintorder integer,
    forbiddeneventcatthresholdtypeintorder integer,
    requiredeventcatthresholdglobalorder integer,
    forbiddeneventcatthresholdglobalorder integer,
    promptingdirective_directiveid integer,
    userrankmintoconfigure integer DEFAULT 3,
    userrankmintoimplement integer DEFAULT 3,
    userrankmintowaive integer DEFAULT 3,
    userrankmintooverride integer DEFAULT 3,
    userrankmintodeactivate integer DEFAULT 3
);


--
-- TOC entry 345 (class 1259 OID 143957)
-- Name: eventruleimpl_impid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.eventruleimpl_impid_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 346 (class 1259 OID 143959)
-- Name: eventruleimpl; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.eventruleimpl (
    erimplid integer DEFAULT nextval('public.eventruleimpl_impid_seq'::regclass) NOT NULL,
    eventrule_ruleid integer NOT NULL,
    cecase_caseid integer,
    occperiod_periodid integer,
    implts timestamp with time zone,
    implby_userid integer,
    lastevaluatedts timestamp with time zone,
    passedrulets timestamp with time zone,
    triggeredevent_eventid integer,
    waivedts timestamp with time zone,
    waivedby_userid integer,
    passoverridets timestamp with time zone,
    passoverrideby_userid integer,
    deacts timestamp with time zone,
    deacby_userid integer,
    notes text
);


--
-- TOC entry 322 (class 1259 OID 108870)
-- Name: eventruleruleset; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.eventruleruleset (
    ruleset_rulesetid integer NOT NULL,
    eventrule_ruleid integer NOT NULL
);


--
-- TOC entry 320 (class 1259 OID 108859)
-- Name: eventrulesetid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.eventrulesetid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 321 (class 1259 OID 108861)
-- Name: eventruleset; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.eventruleset (
    rulesetid integer DEFAULT nextval('public.eventrulesetid_seq'::regclass) NOT NULL,
    title text,
    description text
);


--
-- TOC entry 355 (class 1259 OID 154163)
-- Name: human_humanid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.human_humanid_seq
    START WITH 500000
    INCREMENT BY 1
    MINVALUE 500000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 356 (class 1259 OID 154165)
-- Name: human; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.human (
    humanid integer DEFAULT nextval('public.human_humanid_seq'::regclass) NOT NULL,
    name text NOT NULL,
    dob date,
    under18 boolean,
    jobtitle text,
    businessentity boolean DEFAULT false,
    multihuman boolean DEFAULT false,
    source_sourceid integer,
    deceaseddate date,
    deceasedby_userid integer,
    cloneof_humanid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text
);


--
-- TOC entry 406 (class 1259 OID 164069)
-- Name: humancecase_linkid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.humancecase_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 379 (class 1259 OID 155005)
-- Name: humancecase; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.humancecase (
    cecase_caseid integer NOT NULL,
    human_humanid integer NOT NULL,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    linkedobjectrole_lorid integer,
    source_sourceid integer,
    linkid integer DEFAULT nextval('public.humancecase_linkid_seq'::regclass) NOT NULL
);


--
-- TOC entry 407 (class 1259 OID 164141)
-- Name: humanmailing_linkid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.humanmailing_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 365 (class 1259 OID 154413)
-- Name: humanmailing_roleid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.humanmailing_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 381 (class 1259 OID 155080)
-- Name: humanmailingaddress; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.humanmailingaddress (
    humanmailing_humanid integer,
    humanmailing_addressid integer,
    source_sourceid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    linkid integer DEFAULT nextval('public.humanmailing_linkid_seq'::regclass) NOT NULL,
    linkedobjectrole_lorid integer,
    priority integer DEFAULT 1
);


--
-- TOC entry 368 (class 1259 OID 154693)
-- Name: humanmuni_linkid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.humanmuni_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 376 (class 1259 OID 154940)
-- Name: humanmuni; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.humanmuni (
    linkid integer DEFAULT nextval('public.humanmuni_linkid_seq'::regclass) NOT NULL,
    human_humanid integer,
    muni_municode integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    linkedobjectrole_lorid integer,
    source_sourceid integer
);


--
-- TOC entry 377 (class 1259 OID 154974)
-- Name: humanoccperiod_linkid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.humanoccperiod_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 378 (class 1259 OID 154976)
-- Name: humanoccperiod; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.humanoccperiod (
    linkid integer DEFAULT nextval('public.humanoccperiod_linkid_seq'::regclass) NOT NULL,
    human_humanid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    occperiod_periodid integer,
    linkedobjectrole_lorid integer,
    source_sourceid integer
);


--
-- TOC entry 374 (class 1259 OID 154894)
-- Name: humanparcel_linkid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.humanparcel_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 375 (class 1259 OID 154896)
-- Name: humanparcel; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.humanparcel (
    linkid integer DEFAULT nextval('public.humanparcel_linkid_seq'::regclass) NOT NULL,
    human_humanid integer,
    parcel_parcelkey integer,
    source_sourceid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    linkedobjectrole_lorid integer
);


--
-- TOC entry 373 (class 1259 OID 154878)
-- Name: humanparcelrole_roleid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.humanparcelrole_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 372 (class 1259 OID 154842)
-- Name: parcelunithuman_linkid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.parcelunithuman_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 380 (class 1259 OID 155041)
-- Name: humanparcelunit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.humanparcelunit (
    linkid integer DEFAULT nextval('public.parcelunithuman_linkid_seq'::regclass) NOT NULL,
    parcelunit_unitid integer NOT NULL,
    human_humanid integer NOT NULL,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    linkedobjectrole_lorid integer,
    source_sourceid integer
);


--
-- TOC entry 279 (class 1259 OID 103894)
-- Name: iconid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.iconid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 280 (class 1259 OID 103896)
-- Name: icon; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.icon (
    iconid integer DEFAULT nextval('public.iconid_seq'::regclass) NOT NULL,
    name text,
    styleclass text,
    fontawesome text,
    materialicons text,
    deactivatedts timestamp with time zone
);


--
-- TOC entry 256 (class 1259 OID 66585)
-- Name: improvementid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.improvementid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 255 (class 1259 OID 66577)
-- Name: improvementstatus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.improvementstatus (
    statusid integer NOT NULL,
    statustitle text,
    statusdescription text,
    icon_iconid integer
);


--
-- TOC entry 257 (class 1259 OID 66587)
-- Name: improvementsuggestion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.improvementsuggestion (
    improvementid integer DEFAULT nextval('public.improvementid_seq'::regclass) NOT NULL,
    improvementtypeid integer NOT NULL,
    improvementsuggestiontext text NOT NULL,
    improvementreply text,
    statusid integer NOT NULL,
    submitterid integer NOT NULL,
    submissiontimestamp timestamp with time zone
);


--
-- TOC entry 254 (class 1259 OID 66569)
-- Name: improvementtype; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.improvementtype (
    typeid integer NOT NULL,
    typetitle text,
    typedescription text
);


--
-- TOC entry 250 (class 1259 OID 66244)
-- Name: inspectedspacetypeelement_inspectedstelid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.inspectedspacetypeelement_inspectedstelid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 283 (class 1259 OID 104988)
-- Name: intensityclass; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.intensityclass (
    classid integer DEFAULT nextval('public.codeviolationseverityclass_seq'::regclass) NOT NULL,
    title text,
    muni_municode integer,
    numericrating integer,
    schemaname text,
    active boolean DEFAULT true,
    icon_iconid integer
);


--
-- TOC entry 401 (class 1259 OID 163927)
-- Name: linkedobjectrole_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.linkedobjectrole_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 402 (class 1259 OID 163983)
-- Name: linkedobjectrole; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.linkedobjectrole (
    lorid integer DEFAULT nextval('public.linkedobjectrole_seq'::regclass) NOT NULL,
    lorschema public.linkedobjectroleschema NOT NULL,
    title text NOT NULL,
    description text,
    createdts timestamp with time zone DEFAULT now() NOT NULL,
    deactivatedts timestamp with time zone,
    notes text
);


--
-- TOC entry 400 (class 1259 OID 163915)
-- Name: linkedobjectroleschema_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.linkedobjectroleschema_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 258 (class 1259 OID 66611)
-- Name: listitemchange_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.listitemchange_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 259 (class 1259 OID 66613)
-- Name: listchangerequest; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.listchangerequest (
    changeid integer DEFAULT nextval('public.listitemchange_seq'::regclass) NOT NULL,
    changetext text
);


--
-- TOC entry 264 (class 1259 OID 74790)
-- Name: locationdescription_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.locationdescription_id_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 268 (class 1259 OID 87094)
-- Name: log; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.log (
    logentryid integer DEFAULT nextval('public.coglog_logeentryid_seq'::regclass) NOT NULL,
    timeofentry timestamp with time zone DEFAULT now(),
    user_userid integer,
    notes text,
    error boolean DEFAULT false,
    category integer,
    credsig text,
    subsys text,
    severity text
);


--
-- TOC entry 269 (class 1259 OID 87112)
-- Name: logcategory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.logcategory (
    catid integer NOT NULL,
    title text,
    description text
);


--
-- TOC entry 200 (class 1259 OID 65378)
-- Name: login_userid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.login_userid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 249 (class 1259 OID 66179)
-- Name: login; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.login (
    userid integer DEFAULT nextval('public.login_userid_seq'::regclass) NOT NULL,
    username text NOT NULL,
    password text,
    notes text,
    xarchivepersonlink integer,
    pswdlastupdated timestamp with time zone,
    forcepasswordreset timestamp with time zone,
    createdby_userid integer,
    createdts timestamp with time zone,
    nologinvirtualonly boolean DEFAULT false,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    lastupdatedts timestamp with time zone DEFAULT now(),
    userrole public.role,
    homemuni integer,
    humanlink_humanid integer,
    lastupdatedby_userid integer,
    forcepasswordresetby_userid integer,
    signature_photodocid integer
);


--
-- TOC entry 340 (class 1259 OID 110900)
-- Name: logincredentialexercise_ex_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.logincredentialexercise_ex_seq
    START WITH 2777
    INCREMENT BY 7
    MINVALUE 2777
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 337 (class 1259 OID 110641)
-- Name: munilogin_recordid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.munilogin_recordid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 339 (class 1259 OID 110789)
-- Name: loginmuniauthperiod; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.loginmuniauthperiod (
    muniauthperiodid integer DEFAULT nextval('public.munilogin_recordid_seq'::regclass) NOT NULL,
    muni_municode integer NOT NULL,
    authuser_userid integer NOT NULL,
    accessgranteddatestart timestamp with time zone DEFAULT '1970-01-01 00:00:00-05'::timestamp with time zone NOT NULL,
    accessgranteddatestop timestamp with time zone DEFAULT '1970-01-01 00:00:00-05'::timestamp with time zone NOT NULL,
    recorddeactivatedts timestamp with time zone,
    authorizedrole public.role,
    createdts timestamp with time zone,
    createdby_userid integer NOT NULL,
    notes text,
    supportassignedby integer,
    assignmentrank integer DEFAULT 1,
    oathts timestamp with time zone,
    oathcourtentity_entityid integer
);


--
-- TOC entry 341 (class 1259 OID 110927)
-- Name: loginmuniauthperiodsession_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.loginmuniauthperiodsession_seq
    START WITH 2777
    INCREMENT BY 7
    MINVALUE 2777
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 343 (class 1259 OID 110995)
-- Name: loginmuniauthperiodlog; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.loginmuniauthperiodlog (
    authperiodlogentryid integer DEFAULT nextval('public.loginmuniauthperiodsession_seq'::regclass) NOT NULL,
    authperiod_periodid integer NOT NULL,
    category text,
    entryts timestamp with time zone,
    entrydateofrecord timestamp with time zone,
    disputedby_userid integer,
    disputedts timestamp with time zone,
    notes text,
    cookie_jsessionid text,
    header_remoteaddr text,
    header_useragent text,
    header_dateraw text,
    header_date timestamp with time zone,
    header_cachectl text,
    audit_usersession_userid integer NOT NULL,
    audit_usercredential_userid integer NOT NULL,
    audit_muni_municode integer NOT NULL
);


--
-- TOC entry 342 (class 1259 OID 110993)
-- Name: loginmuniauthperiodlog_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.loginmuniauthperiodlog_seq
    START WITH 2777
    INCREMENT BY 7
    MINVALUE 2777
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 275 (class 1259 OID 95476)
-- Name: loginobjecthistory_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.loginobjecthistory_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 276 (class 1259 OID 95525)
-- Name: loginobjecthistory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.loginobjecthistory (
    historyentryid integer DEFAULT nextval('public.loginobjecthistory_seq'::regclass) NOT NULL,
    login_userid integer,
    person_personid integer,
    property_propertyid integer,
    ceactionrequest_requestid integer,
    cecase_caseid integer,
    ceevent_eventid integer,
    occapp_appid integer,
    occpermit_permitid integer,
    entrytimestamp timestamp with time zone DEFAULT now() NOT NULL,
    occperiod_periodid integer
);


--
-- TOC entry 462 (class 1259 OID 206332)
-- Name: loginphotodocs; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.loginphotodocs (
    user_userid integer NOT NULL,
    photodoc_id integer NOT NULL
);


--
-- TOC entry 455 (class 1259 OID 197696)
-- Name: loginsigvault; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.loginsigvault (
    sigsha1hash text NOT NULL,
    writets timestamp with time zone DEFAULT now()
);


--
-- TOC entry 364 (class 1259 OID 154351)
-- Name: mailingaddress_addressid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.mailingaddress_addressid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 410 (class 1259 OID 164333)
-- Name: mailingaddress; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mailingaddress (
    addressid integer DEFAULT nextval('public.mailingaddress_addressid_seq'::regclass) NOT NULL,
    bldgno text,
    street_streetid integer NOT NULL,
    verifiedts timestamp with time zone,
    verifiedby_userid integer,
    verifiedsource_sourceid integer,
    source_sourceid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    attention text,
    secondary text
);


--
-- TOC entry 440 (class 1259 OID 181202)
-- Name: mailingcitystatezip_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.mailingcitystatezip_id_seq
    START WITH 300000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 412 (class 1259 OID 172525)
-- Name: mailingcitystatezip; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mailingcitystatezip (
    id integer DEFAULT nextval('public.mailingcitystatezip_id_seq'::regclass) NOT NULL,
    zip_code character(5),
    sid integer,
    state_abbr character(2),
    city character varying(30),
    list_type_id integer,
    list_type character varying(10),
    default_state character(2),
    default_city character varying(30),
    default_type character varying(10),
    createdts timestamp with time zone,
    createdby_userid integer,
    source_sourceid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer
);


--
-- TOC entry 409 (class 1259 OID 164283)
-- Name: mailingstreet_streetid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.mailingstreet_streetid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 411 (class 1259 OID 172506)
-- Name: mailingstreet; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mailingstreet (
    streetid integer DEFAULT nextval('public.mailingstreet_streetid_seq'::regclass) NOT NULL,
    name text NOT NULL,
    namevariantsarr text[],
    citystatezip_cszipid integer NOT NULL,
    notes text,
    pobox boolean DEFAULT false NOT NULL,
    createdts timestamp with time zone DEFAULT now(),
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer
);


--
-- TOC entry 317 (class 1259 OID 108390)
-- Name: moneycecasefeeassignedid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.moneycecasefeeassignedid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 448 (class 1259 OID 190480)
-- Name: moneychargeoccpermittype; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.moneychargeoccpermittype (
    permittype_id integer NOT NULL,
    charge_id integer NOT NULL,
    requireattachment boolean,
    createdts timestamp with time zone NOT NULL,
    createdby_userid integer NOT NULL,
    lastupdatedts timestamp with time zone NOT NULL,
    lastupdatedby_userid integer NOT NULL,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text
);


--
-- TOC entry 231 (class 1259 OID 65810)
-- Name: occinspectionfee_feeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occinspectionfee_feeid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 444 (class 1259 OID 190297)
-- Name: moneychargeschedule; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.moneychargeschedule (
    chargeid integer DEFAULT nextval('public.occinspectionfee_feeid_seq'::regclass) NOT NULL,
    chgtype public.chargetype NOT NULL,
    muni_municode integer NOT NULL,
    chargename text NOT NULL,
    description text,
    chargeamount money NOT NULL,
    governingordinance_eceid integer NOT NULL,
    effectivedate timestamp with time zone NOT NULL,
    expirydate timestamp with time zone NOT NULL,
    minranktoassign public.role,
    minranktodeactivate public.role,
    eventcatwhenposted integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text
);


--
-- TOC entry 314 (class 1259 OID 107593)
-- Name: moneycodesetelementfeeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.moneycodesetelementfeeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 316 (class 1259 OID 108364)
-- Name: moneyfeeassignedid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.moneyfeeassignedid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 442 (class 1259 OID 189497)
-- Name: moneyledger_transactionid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.moneyledger_transactionid_seq
    START WITH 101
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 446 (class 1259 OID 190351)
-- Name: moneyledger; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.moneyledger (
    transactionid integer DEFAULT nextval('public.moneyledger_transactionid_seq'::regclass) NOT NULL,
    cecase_caseid integer,
    occperiod_periodid integer,
    transtype public.transactiontype NOT NULL,
    amount money NOT NULL,
    dateofrecord timestamp with time zone NOT NULL,
    source_id integer,
    event_eventid integer,
    lockedts timestamp with time zone,
    lockedby_userid integer NOT NULL,
    createdts timestamp with time zone NOT NULL,
    createdby_userid integer NOT NULL,
    lastupdatedts timestamp with time zone NOT NULL,
    lastupdatedby_userid integer NOT NULL,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text
);


--
-- TOC entry 447 (class 1259 OID 190415)
-- Name: moneyledgercharge; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.moneyledgercharge (
    transaction_id integer NOT NULL,
    charge_id integer NOT NULL,
    createdts timestamp with time zone NOT NULL,
    createdby_userid integer NOT NULL,
    lastupdatedts timestamp with time zone NOT NULL,
    lastupdatedby_userid integer NOT NULL,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text
);


--
-- TOC entry 443 (class 1259 OID 189575)
-- Name: moneyledgercharge_mclid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.moneyledgercharge_mclid_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 318 (class 1259 OID 108408)
-- Name: moneyoccperiodfeeassignedid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.moneyoccperiodfeeassignedid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 315 (class 1259 OID 107629)
-- Name: moneyoccperiodtypefeeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.moneyoccperiodtypefeeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 449 (class 1259 OID 190533)
-- Name: moneypmtmetadatacheck_checkid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.moneypmtmetadatacheck_checkid_seq
    START WITH 100001
    INCREMENT BY 1
    MINVALUE 100000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 450 (class 1259 OID 190535)
-- Name: moneypmtmetadatacheck; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.moneypmtmetadatacheck (
    checkid integer DEFAULT nextval('public.moneypmtmetadatacheck_checkid_seq'::regclass) NOT NULL,
    transaction_id integer NOT NULL,
    checkno integer NOT NULL,
    bankname text,
    mailingaddress_addressid integer NOT NULL,
    createdts timestamp with time zone NOT NULL,
    createdby_userid integer NOT NULL,
    lastupdatedts timestamp with time zone NOT NULL,
    lastupdatedby_userid integer NOT NULL,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text
);


--
-- TOC entry 451 (class 1259 OID 190610)
-- Name: moneypmtmetadatamunicipay_municipayrecordid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.moneypmtmetadatamunicipay_municipayrecordid_seq
    START WITH 100001
    INCREMENT BY 1
    MINVALUE 100000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 452 (class 1259 OID 190612)
-- Name: moneypmtmetadatamunicipay; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.moneypmtmetadatamunicipay (
    recordid integer DEFAULT nextval('public.moneypmtmetadatamunicipay_municipayrecordid_seq'::regclass) NOT NULL,
    transaction_id integer NOT NULL,
    municipayrefno text,
    municipayreply text,
    createdts timestamp with time zone NOT NULL,
    createdby_userid integer NOT NULL,
    lastupdatedts timestamp with time zone NOT NULL,
    lastupdatedby_userid integer NOT NULL,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text
);


--
-- TOC entry 453 (class 1259 OID 190652)
-- Name: moneytransactionhuman_linkid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.moneytransactionhuman_linkid_seq
    START WITH 1001
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 454 (class 1259 OID 190654)
-- Name: moneytransactionhuman; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.moneytransactionhuman (
    linkid integer DEFAULT nextval('public.moneytransactionhuman_linkid_seq'::regclass) NOT NULL,
    human_humanid integer NOT NULL,
    transaction_id integer NOT NULL,
    linkedobjectrole_lorid integer NOT NULL,
    createdts timestamp with time zone NOT NULL,
    createdby_userid integer NOT NULL,
    lastupdatedts timestamp with time zone NOT NULL,
    lastupdatedby_userid integer NOT NULL,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text
);


--
-- TOC entry 238 (class 1259 OID 65919)
-- Name: paymenttype_typeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.paymenttype_typeid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 445 (class 1259 OID 190336)
-- Name: moneytransactionsource; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.moneytransactionsource (
    sourceid integer DEFAULT nextval('public.paymenttype_typeid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    notes text,
    humanassignable boolean DEFAULT true,
    eventcatwhenposted integer,
    applicabletype_typeid public.transactiontype,
    active boolean DEFAULT true,
    trxpathenumliteral text,
    muni_municode integer
);


--
-- TOC entry 319 (class 1259 OID 108624)
-- Name: muni_muniprofile_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.muni_muniprofile_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 183 (class 1259 OID 65115)
-- Name: municipality; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.municipality (
    municode integer NOT NULL,
    muniname text NOT NULL,
    address_street text,
    address_city text,
    address_state text DEFAULT 'PA'::text,
    address_zip text,
    phone text,
    fax text,
    email text,
    population integer,
    activeinprogram boolean,
    defaultcodeset integer NOT NULL,
    occpermitissuingsource_sourceid integer NOT NULL,
    novprintstyle_styleid integer,
    profile_profileid integer,
    enablecodeenforcement boolean DEFAULT true,
    enableoccupancy boolean DEFAULT true,
    enablepublicceactionreqsub boolean DEFAULT true,
    enablepublicceactionreqinfo boolean DEFAULT true,
    enablepublicoccpermitapp boolean DEFAULT false,
    enablepublicoccinspectodo boolean DEFAULT true,
    munimanager_userid integer,
    notes text,
    lastupdatedts timestamp with time zone,
    lastupdated_userid integer,
    primarystaffcontact_userid integer,
    defaultoccperiod integer,
    officeparcel_parcelid integer,
    defaultheaderimage_photodocid integer,
    defaultheaderimageheightpx integer DEFAULT 250
);


--
-- TOC entry 413 (class 1259 OID 172537)
-- Name: municitystatezip; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.municitystatezip (
    muni_municode integer NOT NULL,
    citystatezip_id integer NOT NULL
);


--
-- TOC entry 326 (class 1259 OID 108968)
-- Name: municourtentity; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.municourtentity (
    muni_municode integer NOT NULL,
    courtentity_entityid integer NOT NULL,
    relativeorder integer
);


--
-- TOC entry 267 (class 1259 OID 87079)
-- Name: munilogin; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.munilogin (
    muni_municode integer NOT NULL,
    userid integer NOT NULL,
    defaultmuni boolean DEFAULT false,
    accessgranteddatestart timestamp with time zone DEFAULT '1970-01-01 00:00:00-05'::timestamp with time zone NOT NULL,
    accessgranteddatestop timestamp with time zone DEFAULT '1970-01-01 00:00:00-05'::timestamp with time zone NOT NULL,
    codeofficerstartdate timestamp with time zone,
    codeofficerstopdate timestamp with time zone,
    staffstartdate timestamp with time zone,
    staffstopdate timestamp with time zone,
    sysadminstartdate timestamp with time zone,
    sysadminstopdate timestamp with time zone,
    supportstartdate timestamp with time zone,
    supportstopdate timestamp with time zone,
    codeofficerassignmentorder integer,
    staffassignmentorder integer,
    sysadminassignmentorder integer,
    supportassignmentorder integer,
    bypasscodeofficerassignmentorder integer,
    bypassstaffassignmentorder integer,
    bypasssysadminassignmentorder integer,
    bypasssupportassignmentorder integer,
    recorddeactivatedts timestamp with time zone,
    userrole public.role,
    muniloginrecordid integer DEFAULT nextval('public.munilogin_recordid_seq'::regclass) NOT NULL,
    recordcreatedts timestamp with time zone DEFAULT now(),
    badgenumber text,
    orinumber text,
    defaultcecase_caseid integer
);


--
-- TOC entry 388 (class 1259 OID 155324)
-- Name: munipdfdoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.munipdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    muni_municode integer NOT NULL
);


--
-- TOC entry 290 (class 1259 OID 106434)
-- Name: muniphotodoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.muniphotodoc (
    photodoc_photodocid integer NOT NULL,
    muni_municode integer NOT NULL
);


--
-- TOC entry 325 (class 1259 OID 108911)
-- Name: muniprofile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.muniprofile (
    profileid integer DEFAULT nextval('public.muni_muniprofile_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    notes text,
    continuousoccupancybufferdays integer DEFAULT 0,
    minimumuserranktodeclarerentalintent integer DEFAULT 3,
    minimumuserrankforinspectionoverrides integer DEFAULT 3,
    novfollowupdefaultdays integer DEFAULT 20,
    priorityparamdeadlineadminbufferdays integer DEFAULT 10,
    priorityparamnoletterbufferdays integer DEFAULT 3,
    priorityparamprioritizeletterfollowupbuffer boolean DEFAULT false,
    priorityparamalloweventcatgreenbuffers boolean DEFAULT true
);


--
-- TOC entry 329 (class 1259 OID 110237)
-- Name: muniprofileeventruleset; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.muniprofileeventruleset (
    muniprofile_profileid integer NOT NULL,
    ruleset_setid integer NOT NULL,
    cedefault boolean DEFAULT true
);


--
-- TOC entry 441 (class 1259 OID 189475)
-- Name: muniprofilefee; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.muniprofilefee (
    fee_feeid integer NOT NULL,
    muniprofile_profileid integer NOT NULL,
    active boolean
);


--
-- TOC entry 330 (class 1259 OID 110252)
-- Name: muniprofileoccperiodtype; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.muniprofileoccperiodtype (
    muniprofile_profileid integer NOT NULL,
    occperiodtype_typeid integer NOT NULL
);


--
-- TOC entry 182 (class 1259 OID 52232)
-- Name: noticeofviolation_noticeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.noticeofviolation_noticeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 222 (class 1259 OID 65673)
-- Name: noticeofviolation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.noticeofviolation (
    noticeid integer DEFAULT nextval('public.noticeofviolation_noticeid_seq'::regclass) NOT NULL,
    caseid integer NOT NULL,
    lettertextbeforeviolations text,
    creationtimestamp timestamp with time zone NOT NULL,
    dateofrecord timestamp with time zone NOT NULL,
    sentdate timestamp with time zone,
    returneddate timestamp with time zone,
    personid_recipient integer,
    lettertextafterviolations text,
    lockedandqueuedformailingdate timestamp with time zone,
    lockedandqueuedformailingby integer,
    sentby integer,
    returnedby integer,
    notes text,
    creationby integer,
    printstyle_styleid integer,
    active boolean DEFAULT true,
    followupevent_eventid integer,
    notifyingofficer_userid integer,
    recipient_humanid integer,
    recipient_mailing integer,
    fixedrecipientxferts timestamp with time zone,
    fixedrecipientname text,
    fixedrecipientbldgno text,
    fixedrecipientstreet text,
    fixedrecipientcity text,
    fixedrecipientstate text,
    fixedrecipientzip text,
    fixednotifyingofficername text,
    fixednotifyingofficertitle text,
    fixednotifyingofficerphone text,
    fixednotifyingofficeremail text,
    notifyingofficer_humanid integer,
    fixedheader_photodocid integer,
    letter_typeid integer,
    fixedissuingofficersig_photodocid integer,
    includestipcompdate boolean DEFAULT false
);


--
-- TOC entry 289 (class 1259 OID 106363)
-- Name: noticeofviolationcodeviolation; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.noticeofviolationcodeviolation (
    noticeofviolation_noticeid integer NOT NULL,
    codeviolation_violationid integer NOT NULL,
    includeordtext boolean DEFAULT true,
    includehumanfriendlyordtext boolean DEFAULT false,
    includeviolationphoto boolean DEFAULT false
);


--
-- TOC entry 456 (class 1259 OID 205874)
-- Name: noticeofviolationtype_novtypeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.noticeofviolationtype_novtypeid_seq
    START WITH 101
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 457 (class 1259 OID 205922)
-- Name: noticeofviolationtype; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.noticeofviolationtype (
    novtypeid integer DEFAULT nextval('public.noticeofviolationtype_novtypeid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    eventcatsent_catid integer,
    eventcatfollowup_catid integer,
    eventcatreturned_catid integer,
    followupwindowdays integer DEFAULT 20,
    headerimage_photodocid integer,
    textblockcategory_catid integer,
    muni_municode integer,
    courtdocument boolean DEFAULT true,
    injectviolations boolean DEFAULT true,
    deactivatedts timestamp with time zone,
    printstyle_styleid integer,
    includestipcompdate boolean DEFAULT false
);


--
-- TOC entry 228 (class 1259 OID 65761)
-- Name: occchecklist; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occchecklist (
    checklistid integer DEFAULT nextval('public.checklist_checklistid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text NOT NULL,
    muni_municode integer NOT NULL,
    active boolean DEFAULT true,
    governingcodesource_sourceid integer,
    createdts timestamp with time zone
);


--
-- TOC entry 419 (class 1259 OID 172650)
-- Name: occchecklist_photorequirement_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occchecklist_photorequirement_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 422 (class 1259 OID 172713)
-- Name: occchecklistphotorequirement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occchecklistphotorequirement (
    occchecklist_checklistid integer NOT NULL,
    occphotorequirement_reqid integer NOT NULL
);


--
-- TOC entry 230 (class 1259 OID 65772)
-- Name: occchecklistspacetype; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occchecklistspacetype (
    checklistspacetypeid integer DEFAULT nextval('public.chkliststiceid_seq'::regclass) NOT NULL,
    checklist_id integer NOT NULL,
    required boolean,
    spacetype_typeid integer NOT NULL,
    notes text
);


--
-- TOC entry 226 (class 1259 OID 65739)
-- Name: spaceelement_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.spaceelement_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 263 (class 1259 OID 74771)
-- Name: occchecklistspacetypeelement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occchecklistspacetypeelement (
    spaceelementid integer DEFAULT nextval('public.spaceelement_seq'::regclass) NOT NULL,
    required boolean DEFAULT true,
    checklistspacetype_typeid integer,
    notes text,
    codesetelement_seteleid integer
);


--
-- TOC entry 303 (class 1259 OID 106936)
-- Name: occevent_eventid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occevent_eventid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 304 (class 1259 OID 106984)
-- Name: occeventproposalimplementation_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occeventproposalimplementation_id_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 331 (class 1259 OID 110397)
-- Name: occinspectedspace_pk_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occinspectedspace_pk_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 332 (class 1259 OID 110399)
-- Name: occinspectedspace; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occinspectedspace (
    inspectedspaceid integer DEFAULT nextval('public.occinspectedspace_pk_seq'::regclass) NOT NULL,
    occinspection_inspectionid integer NOT NULL,
    occlocationdescription_descid integer NOT NULL,
    addedtochecklistby_userid integer NOT NULL,
    addedtochecklistts timestamp with time zone,
    occchecklistspacetype_chklstspctypid integer
);


--
-- TOC entry 251 (class 1259 OID 66246)
-- Name: occinspectedspaceelement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occinspectedspaceelement (
    inspectedspaceelementid integer DEFAULT nextval('public.inspectedspacetypeelement_inspectedstelid_seq'::regclass) NOT NULL,
    notes text,
    locationdescription_id integer,
    lastinspectedby_userid integer,
    lastinspectedts timestamp with time zone,
    compliancegrantedby_userid integer,
    compliancegrantedts timestamp with time zone,
    inspectedspace_inspectedspaceid integer NOT NULL,
    overriderequiredflagnotinspected_userid integer,
    occchecklistspacetypeelement_elementid integer NOT NULL,
    failureseverity_intensityclassid integer,
    migratetocecaseonfail boolean DEFAULT true,
    transferredts timestamp with time zone,
    transferredby_userid integer,
    transferredtocecase_caseid integer
);


--
-- TOC entry 273 (class 1259 OID 95426)
-- Name: occinspectedspaceelementphotodoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occinspectedspaceelementphotodoc (
    photodoc_photodocid integer NOT NULL,
    inspectedspaceelement_elementid integer NOT NULL,
    phototype public.occinspectionphototype
);


--
-- TOC entry 223 (class 1259 OID 65687)
-- Name: occupancyinspectionid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occupancyinspectionid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 302 (class 1259 OID 106901)
-- Name: occinspection; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occinspection (
    inspectionid integer DEFAULT nextval('public.occupancyinspectionid_seq'::regclass) NOT NULL,
    occperiod_periodid integer,
    inspector_userid integer NOT NULL,
    publicaccesscc integer,
    enablepacc boolean DEFAULT false,
    notespreinspection text,
    thirdpartyinspector_personid integer,
    thirdpartyinspectorapprovalts timestamp with time zone,
    thirdpartyinspectorapprovalby integer,
    maxoccupantsallowed integer NOT NULL,
    numbedrooms integer,
    numbathrooms integer,
    occchecklist_checklistlistid integer,
    effectivedate timestamp with time zone,
    createdts timestamp with time zone,
    followupto_inspectionid integer,
    timestart timestamp with time zone,
    timeend timestamp with time zone,
    determination_detid integer,
    determinationby_userid integer,
    determinationts timestamp with time zone,
    remarks text,
    generalcomments text,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    cause_causeid integer,
    cecase_caseid integer
);


--
-- TOC entry 417 (class 1259 OID 172624)
-- Name: occinspection_determination_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occinspection_determination_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 415 (class 1259 OID 172613)
-- Name: occinspectioncause_causeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occinspectioncause_causeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 416 (class 1259 OID 172615)
-- Name: occinspectioncause; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occinspectioncause (
    causeid integer DEFAULT nextval('public.occinspectioncause_causeid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    notes text,
    active boolean
);


--
-- TOC entry 418 (class 1259 OID 172626)
-- Name: occinspectiondetermination; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occinspectiondetermination (
    determinationid integer DEFAULT nextval('public.occinspection_determination_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    notes text,
    eventcat_catid integer,
    active boolean,
    qualifiesaspassed boolean DEFAULT false
);


--
-- TOC entry 435 (class 1259 OID 180871)
-- Name: occinspectiondispatch_dispatchid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occinspectiondispatch_dispatchid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 436 (class 1259 OID 180897)
-- Name: occinspectiondispatch; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occinspectiondispatch (
    dispatchid integer DEFAULT nextval('public.occinspectiondispatch_dispatchid_seq'::regclass) NOT NULL,
    createdby_userid integer NOT NULL,
    createdts timestamp with time zone NOT NULL,
    dispatchnotes text,
    inspection_inspectionid integer NOT NULL,
    retrievalts timestamp with time zone,
    retrievedby_userid integer,
    synchronizationts timestamp with time zone,
    synchronizationnotes text,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer NOT NULL
);


--
-- TOC entry 421 (class 1259 OID 172661)
-- Name: occinspectionphotodoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occinspectionphotodoc (
    photodoc_photodocid integer NOT NULL,
    inspection_inspectionid integer NOT NULL,
    photorequirement_requirementid integer
);


--
-- TOC entry 414 (class 1259 OID 172595)
-- Name: occinspectionpropertystatus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occinspectionpropertystatus (
    occinspection_inspectionid integer NOT NULL,
    propertystatus_statusid integer NOT NULL,
    notes text
);


--
-- TOC entry 437 (class 1259 OID 181088)
-- Name: occinspectionrequirement_requirementid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occinspectionrequirement_requirementid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 438 (class 1259 OID 181090)
-- Name: occinspectionrequirement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occinspectionrequirement (
    requirementid integer DEFAULT nextval('public.occinspectionrequirement_requirementid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    active boolean DEFAULT true
);


--
-- TOC entry 439 (class 1259 OID 181100)
-- Name: occinspectionrequirementassigned; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occinspectionrequirementassigned (
    occrequirement_requirementid integer NOT NULL,
    occinspection_inspectionid integer NOT NULL,
    assignedby integer,
    assigneddate timestamp with time zone,
    assignednotes text,
    fulfilledby integer,
    fulfilleddate timestamp with time zone,
    fulfillednotes text,
    notes text
);


--
-- TOC entry 265 (class 1259 OID 74792)
-- Name: occlocationdescriptor; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occlocationdescriptor (
    locationdescriptionid integer DEFAULT nextval('public.locationdescription_id_seq'::regclass) NOT NULL,
    description text,
    buildingfloorno integer
);


--
-- TOC entry 298 (class 1259 OID 106806)
-- Name: occperiodid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occperiodid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 299 (class 1259 OID 106808)
-- Name: occperiod; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occperiod (
    periodid integer DEFAULT nextval('public.occperiodid_seq'::regclass) NOT NULL,
    source_sourceid integer NOT NULL,
    createdts timestamp with time zone,
    startdate timestamp with time zone,
    startdatecertifiedby_userid integer,
    startdatecertifiedts timestamp with time zone,
    enddate timestamp with time zone,
    enddatecertifiedby_userid integer,
    enddatecterifiedts timestamp with time zone,
    manager_userid integer,
    authorizationts timestamp with time zone,
    authorizedby_userid integer,
    notes text,
    createdby_userid integer NOT NULL,
    overrideperiodtypeconfig boolean DEFAULT false,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    parcelunit_unitid integer NOT NULL,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer
);


--
-- TOC entry 344 (class 1259 OID 143827)
-- Name: occperiodeventrule; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occperiodeventrule (
    occperiod_periodid integer NOT NULL,
    eventrule_ruleid integer NOT NULL,
    attachedts timestamp with time zone,
    attachedby_userid integer,
    lastevaluatedts timestamp with time zone,
    passedrulets timestamp with time zone,
    passedrule_eventid integer,
    active boolean DEFAULT true
);


--
-- TOC entry 338 (class 1259 OID 110730)
-- Name: occperiodeventrule_operid_pk_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occperiodeventrule_operid_pk_seq
    START WITH 100
    INCREMENT BY 10
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 389 (class 1259 OID 155354)
-- Name: occperiodpdfdoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occperiodpdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    occperiod_periodid integer NOT NULL
);


--
-- TOC entry 313 (class 1259 OID 107421)
-- Name: occperiodpermitapplication; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occperiodpermitapplication (
    occperiod_periodid integer NOT NULL,
    occpermitapp_applicationid integer NOT NULL
);


--
-- TOC entry 305 (class 1259 OID 107163)
-- Name: occperiodphotodoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occperiodphotodoc (
    photodoc_photodocid integer NOT NULL,
    occperiod_periodid integer NOT NULL
);


--
-- TOC entry 461 (class 1259 OID 206191)
-- Name: occperiodpin; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occperiodpin (
    occperiod_periodid integer NOT NULL,
    pinnedby_userid integer NOT NULL,
    createdts timestamp with time zone DEFAULT now(),
    deactivatedts timestamp with time zone
);


--
-- TOC entry 300 (class 1259 OID 106872)
-- Name: occperiodtypeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occperiodtypeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 233 (class 1259 OID 65842)
-- Name: occupancypermit_permitid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occupancypermit_permitid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 306 (class 1259 OID 107211)
-- Name: occpermit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occpermit (
    permitid integer DEFAULT nextval('public.occupancypermit_permitid_seq'::regclass) NOT NULL,
    occperiod_periodid integer NOT NULL,
    referenceno text,
    staticpermitadditionaltext text,
    notes text,
    finalizedts timestamp with time zone,
    finalizedby_userid integer,
    statictitle text,
    staticmuniaddress text,
    staticpropertyinfo text,
    staticownerseller text,
    staticcolumnlink text,
    staticbuyertenant text,
    staticproposeduse text,
    staticusecode text,
    staticpropclass text,
    staticdateofapplication timestamp with time zone,
    staticinitialinspection timestamp with time zone,
    staticreinspectiondate timestamp with time zone,
    staticfinalinspection timestamp with time zone,
    staticdateofissue timestamp with time zone,
    staticofficername text,
    staticissuedundercodesourceid text,
    staticstipulations text,
    staticcomments text,
    staticmanager text,
    statictenants text,
    staticleaseterm text,
    staticleasestatus text,
    staticpaymentstatus text,
    staticnotice text,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    staticconstructiontype text,
    nullifiedts timestamp with time zone,
    nullifiedby_userid integer,
    staticdateexpiry timestamp with time zone,
    permittype_typeid integer,
    staticsignature_photodocid integer
);


--
-- TOC entry 236 (class 1259 OID 65880)
-- Name: occpermitapp_appid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occpermitapp_appid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 237 (class 1259 OID 65882)
-- Name: occpermitapplication; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occpermitapplication (
    applicationid integer DEFAULT nextval('public.occpermitapp_appid_seq'::regclass) NOT NULL,
    reason_reasonid integer NOT NULL,
    submissiontimestamp timestamp with time zone NOT NULL,
    submitternotes text,
    internalnotes text,
    propertyunitid text,
    declaredtotaladults integer,
    declaredtotalyouth integer,
    occperiod_periodid integer,
    rentalintent boolean,
    status public.occapplicationstatus,
    externalnotes text
);


--
-- TOC entry 405 (class 1259 OID 164038)
-- Name: occpermitapplicationhuman; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occpermitapplicationhuman (
    occpermitapplication_applicationid integer NOT NULL,
    human_humanid integer NOT NULL,
    applicant boolean,
    preferredcontact boolean,
    applicationpersontype public.persontype DEFAULT 'Other'::public.persontype NOT NULL,
    active boolean
);


--
-- TOC entry 234 (class 1259 OID 65868)
-- Name: occpermitpublicreason_reasonid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occpermitpublicreason_reasonid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 235 (class 1259 OID 65870)
-- Name: occpermitapplicationreason; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occpermitapplicationreason (
    reasonid integer DEFAULT nextval('public.occpermitpublicreason_reasonid_seq'::regclass) NOT NULL,
    reasontitle text NOT NULL,
    reasondescription text NOT NULL,
    activereason boolean DEFAULT true,
    requiredpersontypes public.persontype[],
    optionalpersontypes public.persontype[],
    humanfriendlydescription text,
    periodtypeproposal_periodid integer
);


--
-- TOC entry 301 (class 1259 OID 106874)
-- Name: occpermittype; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occpermittype (
    typeid integer DEFAULT nextval('public.occperiodtypeid_seq'::regclass) NOT NULL,
    muni_municode integer NOT NULL,
    title text NOT NULL,
    authorizeduses text,
    description text,
    userassignable boolean DEFAULT true,
    permittable boolean DEFAULT true,
    requireinspectionpass boolean DEFAULT true,
    requireleaselink boolean DEFAULT true,
    active boolean DEFAULT true,
    allowthirdpartyinspection boolean,
    commercial boolean DEFAULT false,
    defaultpermitvalidityperioddays integer,
    eventruleset_setid integer,
    permittitle text,
    permittitlesub text,
    expires boolean DEFAULT false,
    requiremanager boolean,
    requiretenant boolean,
    requirezerobalance boolean
);


--
-- TOC entry 420 (class 1259 OID 172652)
-- Name: occphotorequirement; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occphotorequirement (
    requirementid integer DEFAULT nextval('public.occchecklist_photorequirement_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    notes text,
    required boolean,
    active boolean
);


--
-- TOC entry 224 (class 1259 OID 65728)
-- Name: spacetype_spacetypeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.spacetype_spacetypeid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 225 (class 1259 OID 65730)
-- Name: occspacetype; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.occspacetype (
    spacetypeid integer DEFAULT nextval('public.spacetype_spacetypeid_seq'::regclass) NOT NULL,
    spacetitle text NOT NULL,
    description text NOT NULL
);


--
-- TOC entry 253 (class 1259 OID 66542)
-- Name: occupancyinspectionstatusid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occupancyinspectionstatusid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 232 (class 1259 OID 65826)
-- Name: occupancypermittype_typeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.occupancypermittype_typeid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 353 (class 1259 OID 154132)
-- Name: parcel_parcelkey_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.parcel_parcelkey_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 354 (class 1259 OID 154134)
-- Name: parcel; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.parcel (
    parcelkey integer DEFAULT nextval('public.parcel_parcelkey_seq'::regclass) NOT NULL,
    parcelidcnty text,
    source_sourceid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    muni_municode integer NOT NULL,
    lotandblock text,
    broadview_photodocid integer
);


--
-- TOC entry 357 (class 1259 OID 154211)
-- Name: parcelhumanrole_roleid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.parcelhumanrole_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 366 (class 1259 OID 154605)
-- Name: parcelinfo_infoid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.parcelinfo_infoid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 367 (class 1259 OID 154607)
-- Name: parcelinfo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.parcelinfo (
    parcelinfoid integer DEFAULT nextval('public.parcelinfo_infoid_seq'::regclass) NOT NULL,
    parcel_parcelkey integer,
    usegroup text,
    constructiontype text,
    countycode text DEFAULT '02'::text,
    notes text,
    ownercode text,
    propclass text,
    locationdescription text,
    bobsource_sourceid integer,
    unfitdatestart timestamp with time zone,
    unfitdatestop timestamp with time zone,
    unfitby_userid integer,
    abandoneddatestart timestamp with time zone,
    abandoneddatestop timestamp with time zone,
    abandonedby_userid integer,
    vacantdatestart timestamp with time zone,
    vacantdatestop timestamp with time zone,
    vacantby_userid integer,
    condition_intensityclassid integer,
    landbankprospect_intensityclassid integer,
    landbankheld boolean DEFAULT false,
    nonaddressable boolean DEFAULT false,
    usetype_typeid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    landbankprospectstart timestamp with time zone,
    landbankprospectstop timestamp with time zone,
    landbankprospectnotes text,
    landbankacqcandidatestart timestamp with time zone,
    landbankacqcandidatestop timestamp with time zone,
    landbankacqcandidatenotes text,
    landbankpursuingstart timestamp with time zone,
    landbankpursuingstop timestamp with time zone,
    landbankpursuingnotes text,
    landbankownedstart timestamp with time zone,
    landbankownedstop timestamp with time zone,
    landbankownednotes text,
    landbankprospectstartby_userid integer,
    landbankprospectstopby_userid integer,
    landbankacqcandidatestartby_userid integer,
    landbankacqcandidatestopby_userid integer,
    landbankpursuingstartby_userid integer,
    landbankpursuingstopby_userid integer,
    landbankownedstartby_userid integer,
    landbankownedstopby_userid integer
);


--
-- TOC entry 408 (class 1259 OID 164153)
-- Name: parcelmailing_linkid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.parcelmailing_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 369 (class 1259 OID 154752)
-- Name: parcelmailingaddress; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.parcelmailingaddress (
    parcel_parcelkey integer,
    mailingaddress_addressid integer,
    source_sourceid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    linkid integer DEFAULT nextval('public.parcelmailing_linkid_seq'::regclass) NOT NULL,
    linkedobjectrole_lorid integer,
    priority integer DEFAULT 1
);


--
-- TOC entry 393 (class 1259 OID 163679)
-- Name: parcelmailingaddressid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.parcelmailingaddressid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 427 (class 1259 OID 172839)
-- Name: parcelmigrationlog_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.parcelmigrationlog_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 428 (class 1259 OID 172841)
-- Name: parcelmigrationlog; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.parcelmigrationlog (
    logentryid integer DEFAULT nextval('public.parcelmigrationlog_seq'::regclass) NOT NULL,
    property_id integer,
    parcel_id integer,
    error_code integer,
    notes text,
    ts timestamp with time zone NOT NULL
);


--
-- TOC entry 426 (class 1259 OID 172830)
-- Name: parcelmigrationlogerrorcode; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.parcelmigrationlogerrorcode (
    code integer NOT NULL,
    descr text NOT NULL,
    fatal boolean DEFAULT true
);


--
-- TOC entry 390 (class 1259 OID 155369)
-- Name: parcelpdfdoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.parcelpdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    parcel_parcelkey integer NOT NULL
);


--
-- TOC entry 391 (class 1259 OID 155384)
-- Name: parcelphotodoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.parcelphotodoc (
    photodoc_photodocid integer NOT NULL,
    parcel_parcelkey integer NOT NULL
);


--
-- TOC entry 370 (class 1259 OID 154801)
-- Name: parcelunit_unitid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.parcelunit_unitid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 371 (class 1259 OID 154803)
-- Name: parcelunit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.parcelunit (
    unitid integer DEFAULT nextval('public.parcelunit_unitid_seq'::regclass) NOT NULL,
    unitnumber text DEFAULT 'DEFAULT'::text NOT NULL,
    parcel_parcelkey integer NOT NULL,
    rentalintentdatestart timestamp with time zone,
    rentalintentdatestop timestamp with time zone,
    rentalnotes text,
    condition_intensityclassid integer,
    source_sourceid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    location_occlocationdescriptor integer,
    address_parcelmailingid integer
);


--
-- TOC entry 239 (class 1259 OID 65930)
-- Name: payment_paymentid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.payment_paymentid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 219 (class 1259 OID 65647)
-- Name: photodoc_photodocid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.photodoc_photodocid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 385 (class 1259 OID 155277)
-- Name: pdfdoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pdfdoc (
    pdfdocid integer DEFAULT nextval('public.photodoc_photodocid_seq'::regclass) NOT NULL,
    pdfdocdescription character varying(100),
    pdfdoccommitted boolean DEFAULT true,
    blobbytes_bytesid integer,
    muni_municode integer
);


--
-- TOC entry 286 (class 1259 OID 106285)
-- Name: person_clone_merge_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.person_clone_merge_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 193 (class 1259 OID 65283)
-- Name: propertunit_unitid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.propertunit_unitid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 348 (class 1259 OID 144091)
-- Name: personchange; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.personchange (
    personchangeid integer DEFAULT nextval('public.propertunit_unitid_seq'::regclass) NOT NULL,
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
    active boolean DEFAULT true
);


--
-- TOC entry 347 (class 1259 OID 144089)
-- Name: personchangeid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.personchangeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 424 (class 1259 OID 172742)
-- Name: personhumanmigrationlog_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.personhumanmigrationlog_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 425 (class 1259 OID 172744)
-- Name: personhumanmigrationlog; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.personhumanmigrationlog (
    logentryid integer DEFAULT nextval('public.personhumanmigrationlog_seq'::regclass) NOT NULL,
    human_humanid integer,
    person_personid integer,
    error_code integer,
    notes text,
    ts timestamp with time zone
);


--
-- TOC entry 423 (class 1259 OID 172733)
-- Name: personhumanmigrationlogerrorcode; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.personhumanmigrationlogerrorcode (
    code integer NOT NULL,
    descr text NOT NULL,
    fatal boolean DEFAULT true
);


--
-- TOC entry 287 (class 1259 OID 106315)
-- Name: personmergehistory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.personmergehistory (
    mergeid integer DEFAULT nextval('public.person_clone_merge_seq'::regclass) NOT NULL,
    mergetarget_personid integer,
    mergesource_personid integer,
    mergby_userid integer,
    mergetimestamp timestamp with time zone,
    mergenotes text
);


--
-- TOC entry 288 (class 1259 OID 106344)
-- Name: personmunilink; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.personmunilink (
    muni_municode integer NOT NULL,
    person_personid integer NOT NULL,
    defaultmuni boolean DEFAULT false
);


--
-- TOC entry 270 (class 1259 OID 87147)
-- Name: personsource_sourceid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.personsource_sourceid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 220 (class 1259 OID 65649)
-- Name: photodoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.photodoc (
    photodocid integer DEFAULT nextval('public.photodoc_photodocid_seq'::regclass) NOT NULL,
    photodocdescription character varying(100),
    photodoccommitted boolean DEFAULT true,
    blobbytes_bytesid integer,
    muni_municode integer,
    blobtype_typeid integer,
    metadatamap bytea,
    title text,
    createdby_userid integer,
    createdts timestamp with time zone DEFAULT now(),
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    dateofrecord timestamp with time zone DEFAULT now(),
    courtdocument boolean DEFAULT true
);


--
-- TOC entry 291 (class 1259 OID 106455)
-- Name: printstyle_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.printstyle_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 292 (class 1259 OID 106472)
-- Name: printstyle; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.printstyle (
    styleid integer DEFAULT nextval('public.printstyle_seq'::regclass) NOT NULL,
    description text,
    headerimage_photodocid integer,
    headerheight integer,
    novtopmargin integer,
    novaddresseleftmargin integer,
    novaddressetopmargin integer,
    browserheadfootenabled boolean DEFAULT false,
    novtexttopmargin integer
);


--
-- TOC entry 278 (class 1259 OID 103877)
-- Name: propertyid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.propertyid_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 190 (class 1259 OID 65248)
-- Name: property; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.property (
    propertyid integer DEFAULT nextval('public.propertyid_seq'::regclass) NOT NULL,
    municipality_municode integer,
    parid text NOT NULL,
    lotandblock text,
    address text NOT NULL,
    usegroup text,
    constructiontype text,
    countycode text DEFAULT '02'::text,
    notes text,
    addr_city text,
    addr_state text,
    addr_zip text,
    ownercode text,
    propclass text,
    lastupdated timestamp with time zone,
    lastupdatedby integer,
    locationdescription text,
    bobsource_sourceid integer,
    unfitdatestart timestamp with time zone,
    unfitdatestop timestamp with time zone,
    unfitby_userid integer,
    abandoneddatestart timestamp with time zone,
    abandoneddatestop timestamp with time zone,
    abandonedby_userid integer,
    vacantdatestart timestamp with time zone,
    vacantdatestop timestamp with time zone,
    vacantby_userid integer,
    condition_intensityclassid integer,
    landbankprospect_intensityclassid integer,
    landbankheld boolean DEFAULT false,
    active boolean DEFAULT true,
    nonaddressable boolean DEFAULT false,
    usetype_typeid integer,
    creationts timestamp with time zone
);


--
-- TOC entry 191 (class 1259 OID 65267)
-- Name: propertyexternaldata_extdataid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.propertyexternaldata_extdataid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 192 (class 1259 OID 65269)
-- Name: propertyexternaldata; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.propertyexternaldata (
    extdataid integer DEFAULT nextval('public.propertyexternaldata_extdataid_seq'::regclass) NOT NULL,
    property_propertyid integer NOT NULL,
    ownername text,
    ownerphone text,
    address_street text,
    address_citystatezip text,
    address_city text,
    address_state text,
    address_zip text,
    saleprice numeric,
    saleyear integer,
    assessedlandvalue numeric,
    assessedbuildingvalue numeric,
    assessmentyear integer,
    usecode text,
    yearbuilt integer,
    livingarea integer,
    condition text,
    notes text,
    lastupdated timestamp with time zone,
    taxcode character(1),
    taxstatus_taxstatusid integer
);


--
-- TOC entry 335 (class 1259 OID 110612)
-- Name: propertyotherid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.propertyotherid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 336 (class 1259 OID 110614)
-- Name: propertyotherid; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.propertyotherid (
    otheridid integer DEFAULT nextval('public.propertyotherid_seq'::regclass) NOT NULL,
    property_propid integer NOT NULL,
    otheraddress text,
    otheraddressnotes text,
    otheraddresslastupdated timestamp with time zone,
    otherlotandblock text,
    otherlotandblocknotes text,
    otherlotandblocklastupdated timestamp with time zone,
    otherparcelid text,
    otherparcelidnotes text,
    otherparceladdresslastupdated timestamp with time zone
);


--
-- TOC entry 392 (class 1259 OID 155399)
-- Name: propertypdfdoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.propertypdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    property_propertyid integer NOT NULL
);


--
-- TOC entry 195 (class 1259 OID 65299)
-- Name: propertyperson; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.propertyperson (
    property_propertyid integer NOT NULL,
    person_personid integer NOT NULL,
    creationts timestamp with time zone
);


--
-- TOC entry 221 (class 1259 OID 65658)
-- Name: propertyphotodoc; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.propertyphotodoc (
    photodoc_photodocid integer NOT NULL,
    property_propertyid integer NOT NULL
);


--
-- TOC entry 333 (class 1259 OID 110574)
-- Name: propertystatusid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.propertystatusid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 334 (class 1259 OID 110576)
-- Name: propertystatus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.propertystatus (
    statusid integer DEFAULT nextval('public.propertystatusid_seq'::regclass) NOT NULL,
    title text,
    description text,
    userdeployable boolean DEFAULT true,
    minimumuserranktoassign integer DEFAULT 2,
    minimumuserranktoremove integer DEFAULT 2,
    muni_municode integer,
    active boolean DEFAULT true
);


--
-- TOC entry 194 (class 1259 OID 65285)
-- Name: propertyunit; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.propertyunit (
    unitid integer DEFAULT nextval('public.propertunit_unitid_seq'::regclass) NOT NULL,
    unitnumber text,
    property_propertyid integer NOT NULL,
    otherknownaddress text,
    notes text,
    rentalintentdatestart timestamp with time zone,
    rentalintentdatestop timestamp with time zone,
    rentalintentlastupdatedby_userid integer,
    rentalnotes text,
    active boolean DEFAULT true,
    condition_intensityclassid integer,
    lastupdatedts timestamp with time zone,
    rental boolean
);


--
-- TOC entry 327 (class 1259 OID 109023)
-- Name: propertyunitchange; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.propertyunitchange (
    unitchangeid integer DEFAULT nextval('public.propertunit_unitid_seq'::regclass) NOT NULL,
    propertyunit_unitid integer,
    unitnumber text,
    otherknownaddress text,
    removed boolean,
    added boolean,
    entryts timestamp with time zone,
    approvedondate timestamp with time zone,
    approvedby_userid integer,
    changedby_userid integer,
    changedby_personid integer,
    active boolean DEFAULT true,
    notes text,
    rentalnotes text
);


--
-- TOC entry 188 (class 1259 OID 65240)
-- Name: propertyusetype_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.propertyusetype_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 189 (class 1259 OID 65242)
-- Name: propertyusetype; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.propertyusetype (
    propertyusetypeid integer DEFAULT nextval('public.propertyusetype_seq'::regclass) NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(100),
    icon_iconid integer,
    zoneclass text,
    deactivatedts timestamp with time zone
);


--
-- TOC entry 252 (class 1259 OID 66299)
-- Name: propevent_eventid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.propevent_eventid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 262 (class 1259 OID 74755)
-- Name: spaceid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.spaceid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 349 (class 1259 OID 144121)
-- Name: taxstatus_taxstatusid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.taxstatus_taxstatusid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 350 (class 1259 OID 144123)
-- Name: taxstatus; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.taxstatus (
    taxstatusid integer DEFAULT nextval('public.taxstatus_taxstatusid_seq'::regclass) NOT NULL,
    year integer,
    paidstatus text,
    tax numeric,
    penalty numeric,
    interest numeric,
    total numeric,
    datepaid date
);


--
-- TOC entry 4631 (class 0 OID 0)
-- Dependencies: 350
-- Name: TABLE taxstatus; Type: COMMENT; Schema: public; Owner: -
--

COMMENT ON TABLE public.taxstatus IS 'Scraped data from Allegheny County http://www2.alleghenycounty.us/RealEstate/. Description valid as of August 2020';


--
-- TOC entry 246 (class 1259 OID 66107)
-- Name: textblock_blockid_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.textblock_blockid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 248 (class 1259 OID 66145)
-- Name: textblock; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.textblock (
    blockid integer DEFAULT nextval('public.textblock_blockid_seq'::regclass) NOT NULL,
    blockcategory_catid integer NOT NULL,
    muni_municode integer NOT NULL,
    blockname text NOT NULL,
    blocktext text NOT NULL,
    placementorderdefault integer,
    injectabletemplate boolean DEFAULT false,
    deactivatedts timestamp with time zone
);


--
-- TOC entry 247 (class 1259 OID 66109)
-- Name: textblockcategory; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.textblockcategory (
    categoryid integer DEFAULT nextval('public.blockcategory_categoryid_seq'::regclass) NOT NULL,
    categorytitle text NOT NULL,
    icon_iconid integer,
    muni_municode integer,
    deactivatedts timestamp with time zone
);


--
-- TOC entry 3372 (class 2606 OID 65234)
-- Name: ceactionrequestissuetype actionrqstissuetype_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequestissuetype
    ADD CONSTRAINT actionrqstissuetype_pk PRIMARY KEY (issuetypeid);


--
-- TOC entry 3579 (class 2606 OID 155183)
-- Name: blobbytes blobbytes_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blobbytes
    ADD CONSTRAINT blobbytes_pk PRIMARY KEY (bytesid);


--
-- TOC entry 3430 (class 2606 OID 66117)
-- Name: textblockcategory blockcategory_catid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.textblockcategory
    ADD CONSTRAINT blockcategory_catid_pk PRIMARY KEY (categoryid);


--
-- TOC entry 3491 (class 2606 OID 106755)
-- Name: bobsource bobsource_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bobsource
    ADD CONSTRAINT bobsource_pk PRIMARY KEY (sourceid);


--
-- TOC entry 3385 (class 2606 OID 65341)
-- Name: ceactionrequest ceactionrequest_requestid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_requestid_pk PRIMARY KEY (requestid);


--
-- TOC entry 3586 (class 2606 OID 155298)
-- Name: ceactionrequestpdfdoc ceactionrequestpdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequestpdfdoc
    ADD CONSTRAINT ceactionrequestpdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, ceactionrequest_requestid);


--
-- TOC entry 3467 (class 2606 OID 95451)
-- Name: ceactionrequestphotodoc ceactionrequestphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequestphotodoc
    ADD CONSTRAINT ceactionrequestphotodoc_pk PRIMARY KEY (photodoc_photodocid, ceactionrequest_requestid);


--
-- TOC entry 3452 (class 2606 OID 74859)
-- Name: ceactionrequeststatus ceactionrequeststatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequeststatus
    ADD CONSTRAINT ceactionrequeststatus_pkey PRIMARY KEY (statusid);


--
-- TOC entry 3391 (class 2606 OID 65446)
-- Name: cecase cecase_caseid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_caseid_pk PRIMARY KEY (caseid);


--
-- TOC entry 3582 (class 2606 OID 155216)
-- Name: cecasephotodoc cecasephotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecasephotodoc
    ADD CONSTRAINT cecasephotodoc_pk PRIMARY KEY (photodoc_photodocid, cecase_caseid);


--
-- TOC entry 3676 (class 2606 OID 206180)
-- Name: cecasepin cecasepin_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecasepin
    ADD CONSTRAINT cecasepin_pk PRIMARY KEY (cecase_caseid, pinnedby_userid);


--
-- TOC entry 3475 (class 2606 OID 103949)
-- Name: cecasestatusicon cecasestatusicon_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecasestatusicon
    ADD CONSTRAINT cecasestatusicon_pk PRIMARY KEY (iconid, status);


--
-- TOC entry 3394 (class 2606 OID 65483)
-- Name: event ceevent_eventid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT ceevent_eventid_pk PRIMARY KEY (eventid);


--
-- TOC entry 3389 (class 2606 OID 65435)
-- Name: eventcategory ceeventcategory_categoryid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventcategory
    ADD CONSTRAINT ceeventcategory_categoryid_pk PRIMARY KEY (categoryid);


--
-- TOC entry 3489 (class 2606 OID 106707)
-- Name: choiceproposal ceeventproposalresponse_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventproposalresponse_pk PRIMARY KEY (proposalid);


--
-- TOC entry 3417 (class 2606 OID 65777)
-- Name: occchecklistspacetype chkliststiceid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occchecklistspacetype
    ADD CONSTRAINT chkliststiceid_pk PRIMARY KEY (checklistspacetypeid);


--
-- TOC entry 3507 (class 2606 OID 107367)
-- Name: choice choice_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choice
    ADD CONSTRAINT choice_pkey PRIMARY KEY (choiceid);


--
-- TOC entry 3525 (class 2606 OID 109090)
-- Name: choicedirectivedirectiveset choicedirdirset_comp_pf; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choicedirectivedirectiveset
    ADD CONSTRAINT choicedirdirset_comp_pf PRIMARY KEY (directiveset_setid, directive_dirid);


--
-- TOC entry 3517 (class 2606 OID 108895)
-- Name: choicedirectiveset choiceproposalset_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choicedirectiveset
    ADD CONSTRAINT choiceproposalset_pkey PRIMARY KEY (directivesetid);


--
-- TOC entry 3427 (class 2606 OID 66089)
-- Name: citation citation_citationid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT citation_citationid_pk PRIMARY KEY (citationid);


--
-- TOC entry 3602 (class 2606 OID 163838)
-- Name: citationcitationstatus citationcitationstatus_id_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT citationcitationstatus_id_pk PRIMARY KEY (citationstatusid);


--
-- TOC entry 3640 (class 2606 OID 173028)
-- Name: citationdocketno citationdocketno_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationdocketno
    ADD CONSTRAINT citationdocketno_pkey PRIMARY KEY (docketid);


--
-- TOC entry 3642 (class 2606 OID 173077)
-- Name: citationdocketnohuman citationdocketnohuman_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationdocketnohuman
    ADD CONSTRAINT citationdocketnohuman_pkey PRIMARY KEY (linkid);


--
-- TOC entry 3644 (class 2606 OID 173098)
-- Name: citationfilingtype citationfilingtype_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationfilingtype
    ADD CONSTRAINT citationfilingtype_pkey PRIMARY KEY (typeid);


--
-- TOC entry 3604 (class 2606 OID 163868)
-- Name: citationphotodoc citationphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationphotodoc
    ADD CONSTRAINT citationphotodoc_pk PRIMARY KEY (photodoc_photodocid, citation_citationid);


--
-- TOC entry 3423 (class 2606 OID 66006)
-- Name: citationstatus citationstatus_statusid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationstatus
    ADD CONSTRAINT citationstatus_statusid_pk PRIMARY KEY (statusid);


--
-- TOC entry 3425 (class 2606 OID 66039)
-- Name: citationviolation citationviolation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citationviolation_pkey PRIMARY KEY (citationviolationid);


--
-- TOC entry 3398 (class 2606 OID 65546)
-- Name: codeelement codeelement_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeelement
    ADD CONSTRAINT codeelement_pk PRIMARY KEY (elementid);


--
-- TOC entry 3549 (class 2606 OID 144197)
-- Name: codeelementinjectedvalue codeelementinjectedvalue_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeelementinjectedvalue
    ADD CONSTRAINT codeelementinjectedvalue_pk PRIMARY KEY (injectedvalueid);


--
-- TOC entry 3446 (class 2606 OID 66636)
-- Name: codeelementguide codeelementtype_cvtypeid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeelementguide
    ADD CONSTRAINT codeelementtype_cvtypeid_pk PRIMARY KEY (guideentryid);


--
-- TOC entry 3387 (class 2606 OID 65372)
-- Name: codeset codeset_codesetid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeset
    ADD CONSTRAINT codeset_codesetid_pk PRIMARY KEY (codesetid);


--
-- TOC entry 3400 (class 2606 OID 65567)
-- Name: codesetelement codesetelement_codesetelementid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codesetelement_codesetelementid_pk PRIMARY KEY (codesetelementid);


--
-- TOC entry 3396 (class 2606 OID 65525)
-- Name: codesource codesource_sourceid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codesource
    ADD CONSTRAINT codesource_sourceid_pk PRIMARY KEY (sourceid);


--
-- TOC entry 3404 (class 2606 OID 65631)
-- Name: codeviolation codeviolation_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_pk PRIMARY KEY (violationid);


--
-- TOC entry 3588 (class 2606 OID 155313)
-- Name: codeviolationpdfdoc codeviolationpdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolationpdfdoc
    ADD CONSTRAINT codeviolationpdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, codeviolation_violationid);


--
-- TOC entry 3463 (class 2606 OID 95415)
-- Name: codeviolationphotodoc codeviolationphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolationphotodoc
    ADD CONSTRAINT codeviolationphotodoc_pk PRIMARY KEY (photodoc_photodocid, codeviolation_violationid);


--
-- TOC entry 3477 (class 2606 OID 104997)
-- Name: intensityclass codeviolationseverityclass_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.intensityclass
    ADD CONSTRAINT codeviolationseverityclass_pk PRIMARY KEY (classid);


--
-- TOC entry 3456 (class 2606 OID 87106)
-- Name: log coglog_logentryid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.log
    ADD CONSTRAINT coglog_logentryid_pk PRIMARY KEY (logentryid);


--
-- TOC entry 3559 (class 2606 OID 154330)
-- Name: contactemail contactemail_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactemail
    ADD CONSTRAINT contactemail_pkey PRIMARY KEY (emailid);


--
-- TOC entry 3557 (class 2606 OID 154289)
-- Name: contactphone contactphone_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT contactphone_pkey PRIMARY KEY (phoneid);


--
-- TOC entry 3555 (class 2606 OID 154278)
-- Name: contactphonetype contactphonetype_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactphonetype
    ADD CONSTRAINT contactphonetype_pkey PRIMARY KEY (phonetypeid);


--
-- TOC entry 3402 (class 2606 OID 65588)
-- Name: courtentity courtentity_entityid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.courtentity
    ADD CONSTRAINT courtentity_entityid_pk PRIMARY KEY (entityid);


--
-- TOC entry 3471 (class 2606 OID 95620)
-- Name: dbpatch dbpatch_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.dbpatch
    ADD CONSTRAINT dbpatch_pk PRIMARY KEY (patchnum);


--
-- TOC entry 3543 (class 2606 OID 143967)
-- Name: eventruleimpl erimplid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimplid_pk PRIMARY KEY (erimplid);


--
-- TOC entry 3674 (class 2606 OID 206073)
-- Name: eventemission eventemission_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventemission
    ADD CONSTRAINT eventemission_pkey PRIMARY KEY (emissionid);


--
-- TOC entry 3608 (class 2606 OID 164003)
-- Name: eventhuman eventhuman_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_pkey PRIMARY KEY (linkid);


--
-- TOC entry 3509 (class 2606 OID 107382)
-- Name: choicedirectivechoice eventpropchoice_comppk_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choicedirectivechoice
    ADD CONSTRAINT eventpropchoice_comppk_pk PRIMARY KEY (choice_choiceid, directive_directiveid);


--
-- TOC entry 3503 (class 2606 OID 107258)
-- Name: choicedirective eventproposal_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choicedirective
    ADD CONSTRAINT eventproposal_pk PRIMARY KEY (directiveid);


--
-- TOC entry 3505 (class 2606 OID 107326)
-- Name: eventrule eventrule_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT eventrule_pkey PRIMARY KEY (ruleid);


--
-- TOC entry 3515 (class 2606 OID 108874)
-- Name: eventruleruleset eventruleset_comp_pf; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventruleruleset
    ADD CONSTRAINT eventruleset_comp_pf PRIMARY KEY (ruleset_rulesetid, eventrule_ruleid);


--
-- TOC entry 3513 (class 2606 OID 108869)
-- Name: eventruleset eventruleset_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventruleset
    ADD CONSTRAINT eventruleset_pkey PRIMARY KEY (rulesetid);


--
-- TOC entry 3459 (class 2606 OID 87119)
-- Name: logcategory genlogcategory_catid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.logcategory
    ADD CONSTRAINT genlogcategory_catid_pk PRIMARY KEY (catid);


--
-- TOC entry 3469 (class 2606 OID 95531)
-- Name: loginobjecthistory historyentryid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT historyentryid_pk PRIMARY KEY (historyentryid);


--
-- TOC entry 3553 (class 2606 OID 154175)
-- Name: human human_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_pkey PRIMARY KEY (humanid);


--
-- TOC entry 3573 (class 2606 OID 164073)
-- Name: humancecase humancecase_linkid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_linkid_pk PRIMARY KEY (linkid);


--
-- TOC entry 3600 (class 2606 OID 163737)
-- Name: citationhuman humancitation_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_pkey PRIMARY KEY (linkid);


--
-- TOC entry 3577 (class 2606 OID 164145)
-- Name: humanmailingaddress humanmailingaddress_linkid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailingaddress_linkid_pk PRIMARY KEY (linkid);


--
-- TOC entry 3569 (class 2606 OID 154948)
-- Name: humanmuni humanmuni_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_pkey PRIMARY KEY (linkid);


--
-- TOC entry 3571 (class 2606 OID 154984)
-- Name: humanoccperiod humanoccperiod_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanoccperiod_pkey PRIMARY KEY (linkid);


--
-- TOC entry 3567 (class 2606 OID 154904)
-- Name: humanparcel humanparcel_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT humanparcel_pkey PRIMARY KEY (linkid);


--
-- TOC entry 3575 (class 2606 OID 155049)
-- Name: humanparcelunit humanparcelunit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT humanparcelunit_pkey PRIMARY KEY (linkid);


--
-- TOC entry 3473 (class 2606 OID 103904)
-- Name: icon iconid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.icon
    ADD CONSTRAINT iconid_pk PRIMARY KEY (iconid);


--
-- TOC entry 3440 (class 2606 OID 66584)
-- Name: improvementstatus improvementstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.improvementstatus
    ADD CONSTRAINT improvementstatus_pkey PRIMARY KEY (statusid);


--
-- TOC entry 3438 (class 2606 OID 66576)
-- Name: improvementtype improvementtype_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.improvementtype
    ADD CONSTRAINT improvementtype_pkey PRIMARY KEY (typeid);


--
-- TOC entry 3465 (class 2606 OID 95430)
-- Name: occinspectedspaceelementphotodoc inspchklstspele_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelementphotodoc
    ADD CONSTRAINT inspchklstspele_pk PRIMARY KEY (photodoc_photodocid, inspectedspaceelement_elementid);


--
-- TOC entry 3436 (class 2606 OID 66255)
-- Name: occinspectedspaceelement inspectedspacetypeice_inspectedsticeid; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT inspectedspacetypeice_inspectedsticeid PRIMARY KEY (inspectedspaceelementid);


--
-- TOC entry 3415 (class 2606 OID 65769)
-- Name: occchecklist inspectionchecklist_checklistid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occchecklist
    ADD CONSTRAINT inspectionchecklist_checklistid_pk PRIMARY KEY (checklistid);


--
-- TOC entry 3606 (class 2606 OID 163992)
-- Name: linkedobjectrole linkedobjectrole_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.linkedobjectrole
    ADD CONSTRAINT linkedobjectrole_pkey PRIMARY KEY (lorid);


--
-- TOC entry 3444 (class 2606 OID 66621)
-- Name: listchangerequest listitemchange_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.listchangerequest
    ADD CONSTRAINT listitemchange_pkey PRIMARY KEY (changeid);


--
-- TOC entry 3450 (class 2606 OID 74800)
-- Name: occlocationdescriptor locationdescription_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occlocationdescriptor
    ADD CONSTRAINT locationdescription_pkey PRIMARY KEY (locationdescriptionid);


--
-- TOC entry 3434 (class 2606 OID 66189)
-- Name: login login_pk2; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_pk2 PRIMARY KEY (userid);


--
-- TOC entry 3539 (class 2606 OID 111003)
-- Name: loginmuniauthperiodlog logincredentialex_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginmuniauthperiodlog
    ADD CONSTRAINT logincredentialex_pk PRIMARY KEY (authperiodlogentryid);


--
-- TOC entry 3537 (class 2606 OID 110800)
-- Name: loginmuniauthperiod loginmuniauthperiod_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginmuniauthperiod
    ADD CONSTRAINT loginmuniauthperiod_pkey PRIMARY KEY (muniauthperiodid);


--
-- TOC entry 3680 (class 2606 OID 206336)
-- Name: loginphotodocs loginphotodoc; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginphotodocs
    ADD CONSTRAINT loginphotodoc PRIMARY KEY (user_userid, photodoc_id);


--
-- TOC entry 3670 (class 2606 OID 197704)
-- Name: loginsigvault loginsigvault_hash_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginsigvault
    ADD CONSTRAINT loginsigvault_hash_pk PRIMARY KEY (sigsha1hash);


--
-- TOC entry 3612 (class 2606 OID 164341)
-- Name: mailingaddress mailingaddress_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingaddress
    ADD CONSTRAINT mailingaddress_pkey PRIMARY KEY (addressid);


--
-- TOC entry 3616 (class 2606 OID 172529)
-- Name: mailingcitystatezip mailingcitystatezip_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingcitystatezip
    ADD CONSTRAINT mailingcitystatezip_pkey PRIMARY KEY (id);


--
-- TOC entry 3614 (class 2606 OID 172514)
-- Name: mailingstreet mailingstreet_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingstreet
    ADD CONSTRAINT mailingstreet_pkey PRIMARY KEY (streetid);


--
-- TOC entry 3662 (class 2606 OID 190487)
-- Name: moneychargeoccpermittype moneychargeoccpermittype_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeoccpermittype
    ADD CONSTRAINT moneychargeoccpermittype_pk PRIMARY KEY (permittype_id, charge_id);


--
-- TOC entry 3658 (class 2606 OID 190359)
-- Name: moneyledger moneyledger_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledger
    ADD CONSTRAINT moneyledger_pkey PRIMARY KEY (transactionid);


--
-- TOC entry 3660 (class 2606 OID 190422)
-- Name: moneyledgercharge moneyledgercharge_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledgercharge
    ADD CONSTRAINT moneyledgercharge_pk PRIMARY KEY (transaction_id, charge_id);


--
-- TOC entry 3664 (class 2606 OID 190543)
-- Name: moneypmtmetadatacheck moneypmtmetadatacheck_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneypmtmetadatacheck
    ADD CONSTRAINT moneypmtmetadatacheck_pkey PRIMARY KEY (checkid);


--
-- TOC entry 3666 (class 2606 OID 190620)
-- Name: moneypmtmetadatamunicipay moneypmtmetadatamunicipay_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneypmtmetadatamunicipay
    ADD CONSTRAINT moneypmtmetadatamunicipay_pk PRIMARY KEY (recordid);


--
-- TOC entry 3668 (class 2606 OID 190662)
-- Name: moneytransactionhuman moneytransactionhuman_linkid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneytransactionhuman
    ADD CONSTRAINT moneytransactionhuman_linkid_pk PRIMARY KEY (linkid);


--
-- TOC entry 3368 (class 2606 OID 65123)
-- Name: municipality municipality_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT municipality_pk PRIMARY KEY (municode);


--
-- TOC entry 3618 (class 2606 OID 172541)
-- Name: municitystatezip municitystatezip_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municitystatezip
    ADD CONSTRAINT municitystatezip_pk PRIMARY KEY (muni_municode, citystatezip_id);


--
-- TOC entry 3521 (class 2606 OID 108972)
-- Name: municourtentity municourtentity_comp_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municourtentity
    ADD CONSTRAINT municourtentity_comp_pk PRIMARY KEY (muni_municode, courtentity_entityid);


--
-- TOC entry 3454 (class 2606 OID 110645)
-- Name: munilogin munilogin_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.munilogin
    ADD CONSTRAINT munilogin_pkey PRIMARY KEY (muniloginrecordid);


--
-- TOC entry 3590 (class 2606 OID 155328)
-- Name: munipdfdoc munipdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.munipdfdoc
    ADD CONSTRAINT munipdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, muni_municode);


--
-- TOC entry 3485 (class 2606 OID 106438)
-- Name: muniphotodoc muniphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniphotodoc
    ADD CONSTRAINT muniphotodoc_pk PRIMARY KEY (photodoc_photodocid, muni_municode);


--
-- TOC entry 3519 (class 2606 OID 108919)
-- Name: muniprofile muniprofile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniprofile
    ADD CONSTRAINT muniprofile_pkey PRIMARY KEY (profileid);


--
-- TOC entry 3527 (class 2606 OID 110241)
-- Name: muniprofileeventruleset muniprofileeventruleset_comp_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniprofileeventruleset
    ADD CONSTRAINT muniprofileeventruleset_comp_pk PRIMARY KEY (muniprofile_profileid, ruleset_setid);


--
-- TOC entry 3652 (class 2606 OID 189479)
-- Name: muniprofilefee muniprofilefee_composite_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniprofilefee
    ADD CONSTRAINT muniprofilefee_composite_pk PRIMARY KEY (fee_feeid, muniprofile_profileid);


--
-- TOC entry 3529 (class 2606 OID 110256)
-- Name: muniprofileoccperiodtype muniprofileoccperiodtype_comp_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniprofileoccperiodtype
    ADD CONSTRAINT muniprofileoccperiodtype_comp_pk PRIMARY KEY (muniprofile_profileid, occperiodtype_typeid);


--
-- TOC entry 3483 (class 2606 OID 106370)
-- Name: noticeofviolationcodeviolation noticeofviolationcodeviolation_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolationcodeviolation
    ADD CONSTRAINT noticeofviolationcodeviolation_pk PRIMARY KEY (noticeofviolation_noticeid, codeviolation_violationid);


--
-- TOC entry 3672 (class 2606 OID 205933)
-- Name: noticeofviolationtype noticeofviolationtype_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolationtype
    ADD CONSTRAINT noticeofviolationtype_pkey PRIMARY KEY (novtypeid);


--
-- TOC entry 3411 (class 2606 OID 65681)
-- Name: noticeofviolation noticeviolation_noticeid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT noticeviolation_noticeid_pk PRIMARY KEY (noticeid);


--
-- TOC entry 3630 (class 2606 OID 172717)
-- Name: occchecklistphotorequirement occchecklistphotorequirement_pk_comp; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occchecklistphotorequirement
    ADD CONSTRAINT occchecklistphotorequirement_pk_comp PRIMARY KEY (occchecklist_checklistid, occphotorequirement_reqid);


--
-- TOC entry 3654 (class 2606 OID 190305)
-- Name: moneychargeschedule occinspecfee_feeid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeschedule
    ADD CONSTRAINT occinspecfee_feeid_pk PRIMARY KEY (chargeid);


--
-- TOC entry 3531 (class 2606 OID 110404)
-- Name: occinspectedspace occinspectedspace_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspace
    ADD CONSTRAINT occinspectedspace_pkey PRIMARY KEY (inspectedspaceid);


--
-- TOC entry 3497 (class 2606 OID 106910)
-- Name: occinspection occinspection_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_pk PRIMARY KEY (inspectionid);


--
-- TOC entry 3622 (class 2606 OID 172623)
-- Name: occinspectioncause occinspectioncause_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectioncause
    ADD CONSTRAINT occinspectioncause_pkey PRIMARY KEY (causeid);


--
-- TOC entry 3624 (class 2606 OID 172634)
-- Name: occinspectiondetermination occinspectiondetermination_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectiondetermination
    ADD CONSTRAINT occinspectiondetermination_pkey PRIMARY KEY (determinationid);


--
-- TOC entry 3646 (class 2606 OID 180905)
-- Name: occinspectiondispatch occinspectiondispatch_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectiondispatch
    ADD CONSTRAINT occinspectiondispatch_pkey PRIMARY KEY (dispatchid);


--
-- TOC entry 3628 (class 2606 OID 172665)
-- Name: occinspectionphotodoc occinspectionphotodoc_pk_comp; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionphotodoc
    ADD CONSTRAINT occinspectionphotodoc_pk_comp PRIMARY KEY (photodoc_photodocid, inspection_inspectionid);


--
-- TOC entry 3620 (class 2606 OID 172602)
-- Name: occinspectionpropertystatus occinspectionpropstatuspk_comp; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionpropertystatus
    ADD CONSTRAINT occinspectionpropstatuspk_comp PRIMARY KEY (occinspection_inspectionid, propertystatus_statusid);


--
-- TOC entry 3648 (class 2606 OID 181099)
-- Name: occinspectionrequirement occinspectionrequirement_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionrequirement
    ADD CONSTRAINT occinspectionrequirement_pkey PRIMARY KEY (requirementid);


--
-- TOC entry 3650 (class 2606 OID 181107)
-- Name: occinspectionrequirementassigned occinspectionrequirementassigned_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionrequirementassigned
    ADD CONSTRAINT occinspectionrequirementassigned_pk PRIMARY KEY (occrequirement_requirementid, occinspection_inspectionid);


--
-- TOC entry 3493 (class 2606 OID 106816)
-- Name: occperiod occperiod_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_pk PRIMARY KEY (periodid);


--
-- TOC entry 3541 (class 2606 OID 143832)
-- Name: occperiodeventrule occperiodeventrule_comp_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodeventrule
    ADD CONSTRAINT occperiodeventrule_comp_pk PRIMARY KEY (occperiod_periodid, eventrule_ruleid);


--
-- TOC entry 3592 (class 2606 OID 155358)
-- Name: occperiodpdfdoc occperiodpdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodpdfdoc
    ADD CONSTRAINT occperiodpdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, occperiod_periodid);


--
-- TOC entry 3511 (class 2606 OID 107425)
-- Name: occperiodpermitapplication occperiodpermitapp_comp_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodpermitapplication
    ADD CONSTRAINT occperiodpermitapp_comp_pk PRIMARY KEY (occperiod_periodid, occpermitapp_applicationid);


--
-- TOC entry 3499 (class 2606 OID 107167)
-- Name: occperiodphotodoc occperiodphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodphotodoc
    ADD CONSTRAINT occperiodphotodoc_pk PRIMARY KEY (photodoc_photodocid, occperiod_periodid);


--
-- TOC entry 3678 (class 2606 OID 206196)
-- Name: occperiodpin occperiodpin_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodpin
    ADD CONSTRAINT occperiodpin_pk PRIMARY KEY (occperiod_periodid, pinnedby_userid);


--
-- TOC entry 3495 (class 2606 OID 106889)
-- Name: occpermittype occperiodtype_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermittype
    ADD CONSTRAINT occperiodtype_pk PRIMARY KEY (typeid);


--
-- TOC entry 3501 (class 2606 OID 107219)
-- Name: occpermit occpermit_permitid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occpermit_permitid_pk PRIMARY KEY (permitid);


--
-- TOC entry 3421 (class 2606 OID 66273)
-- Name: occpermitapplication occpermitapp_applicationid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermitapplication
    ADD CONSTRAINT occpermitapp_applicationid_pk PRIMARY KEY (applicationid);


--
-- TOC entry 3610 (class 2606 OID 164043)
-- Name: occpermitapplicationhuman occpermitapplicationhuman_comp_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermitapplicationhuman
    ADD CONSTRAINT occpermitapplicationhuman_comp_pk PRIMARY KEY (occpermitapplication_applicationid, human_humanid);


--
-- TOC entry 3419 (class 2606 OID 65879)
-- Name: occpermitapplicationreason occpermitreason_reasonid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermitapplicationreason
    ADD CONSTRAINT occpermitreason_reasonid_pk PRIMARY KEY (reasonid);


--
-- TOC entry 3626 (class 2606 OID 172660)
-- Name: occphotorequirement occphotorequirement_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occphotorequirement
    ADD CONSTRAINT occphotorequirement_pkey PRIMARY KEY (requirementid);


--
-- TOC entry 3487 (class 2606 OID 106481)
-- Name: printstyle paramsid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.printstyle
    ADD CONSTRAINT paramsid_pk PRIMARY KEY (styleid);


--
-- TOC entry 3551 (class 2606 OID 154142)
-- Name: parcel parcel_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_pkey PRIMARY KEY (parcelkey);


--
-- TOC entry 3561 (class 2606 OID 154618)
-- Name: parcelinfo parcelinfo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_pkey PRIMARY KEY (parcelinfoid);


--
-- TOC entry 3563 (class 2606 OID 164157)
-- Name: parcelmailingaddress parcelmailingaddress_linkid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT parcelmailingaddress_linkid_pk PRIMARY KEY (linkid);


--
-- TOC entry 3638 (class 2606 OID 172849)
-- Name: parcelmigrationlog parcelmigrationlog_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmigrationlog
    ADD CONSTRAINT parcelmigrationlog_pkey PRIMARY KEY (logentryid);


--
-- TOC entry 3636 (class 2606 OID 172838)
-- Name: parcelmigrationlogerrorcode parcelmigrationlogerrorcode_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmigrationlogerrorcode
    ADD CONSTRAINT parcelmigrationlogerrorcode_pkey PRIMARY KEY (code);


--
-- TOC entry 3594 (class 2606 OID 155373)
-- Name: parcelpdfdoc parcelpdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelpdfdoc
    ADD CONSTRAINT parcelpdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, parcel_parcelkey);


--
-- TOC entry 3596 (class 2606 OID 155388)
-- Name: parcelphotodoc parcelphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelphotodoc
    ADD CONSTRAINT parcelphotodoc_pk PRIMARY KEY (photodoc_photodocid, parcel_parcelkey);


--
-- TOC entry 3565 (class 2606 OID 154811)
-- Name: parcelunit parcelunit_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_pkey PRIMARY KEY (unitid);


--
-- TOC entry 3584 (class 2606 OID 155283)
-- Name: pdfdoc pdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdfdoc
    ADD CONSTRAINT pdfdoc_pk PRIMARY KEY (pdfdocid);


--
-- TOC entry 3545 (class 2606 OID 144100)
-- Name: personchange personchangeid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personchange
    ADD CONSTRAINT personchangeid_pk PRIMARY KEY (personchangeid);


--
-- TOC entry 3634 (class 2606 OID 172752)
-- Name: personhumanmigrationlog personhumanmigrationlog_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personhumanmigrationlog
    ADD CONSTRAINT personhumanmigrationlog_pkey PRIMARY KEY (logentryid);


--
-- TOC entry 3632 (class 2606 OID 172741)
-- Name: personhumanmigrationlogerrorcode personhumanmigrationlogerrorcode_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personhumanmigrationlogerrorcode
    ADD CONSTRAINT personhumanmigrationlogerrorcode_pkey PRIMARY KEY (code);


--
-- TOC entry 3370 (class 2606 OID 65218)
-- Name: person personid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT personid_pk PRIMARY KEY (personid);


--
-- TOC entry 3479 (class 2606 OID 106323)
-- Name: personmergehistory personmerge; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personmergehistory
    ADD CONSTRAINT personmerge PRIMARY KEY (mergeid);


--
-- TOC entry 3481 (class 2606 OID 106349)
-- Name: personmunilink personmuni; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personmunilink
    ADD CONSTRAINT personmuni PRIMARY KEY (muni_municode, person_personid);


--
-- TOC entry 3406 (class 2606 OID 65657)
-- Name: photodoc photodoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.photodoc
    ADD CONSTRAINT photodoc_pk PRIMARY KEY (photodocid);


--
-- TOC entry 3461 (class 2606 OID 95405)
-- Name: blobtype photodoctype_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blobtype
    ADD CONSTRAINT photodoctype_pkey PRIMARY KEY (typeid);


--
-- TOC entry 3656 (class 2606 OID 190345)
-- Name: moneytransactionsource pmttype_typeid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneytransactionsource
    ADD CONSTRAINT pmttype_typeid_pk PRIMARY KEY (sourceid);


--
-- TOC entry 3377 (class 2606 OID 65256)
-- Name: property property_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_pk PRIMARY KEY (propertyid);


--
-- TOC entry 3379 (class 2606 OID 65277)
-- Name: propertyexternaldata propertyexternaldata_extdataid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyexternaldata
    ADD CONSTRAINT propertyexternaldata_extdataid_pk PRIMARY KEY (extdataid);


--
-- TOC entry 3535 (class 2606 OID 110622)
-- Name: propertyotherid propertyotherid_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyotherid
    ADD CONSTRAINT propertyotherid_pkey PRIMARY KEY (otheridid);


--
-- TOC entry 3598 (class 2606 OID 155403)
-- Name: propertypdfdoc propertypdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertypdfdoc
    ADD CONSTRAINT propertypdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, property_propertyid);


--
-- TOC entry 3383 (class 2606 OID 65303)
-- Name: propertyperson propertyperson_propid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyperson
    ADD CONSTRAINT propertyperson_propid_pk PRIMARY KEY (property_propertyid, person_personid);


--
-- TOC entry 3408 (class 2606 OID 65662)
-- Name: propertyphotodoc propertyphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyphotodoc
    ADD CONSTRAINT propertyphotodoc_pk PRIMARY KEY (photodoc_photodocid, property_propertyid);


--
-- TOC entry 3533 (class 2606 OID 110588)
-- Name: propertystatus propertystatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertystatus
    ADD CONSTRAINT propertystatus_pkey PRIMARY KEY (statusid);


--
-- TOC entry 3374 (class 2606 OID 65247)
-- Name: propertyusetype propertyusetype_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyusetype
    ADD CONSTRAINT propertyusetype_pk PRIMARY KEY (propertyusetypeid);


--
-- TOC entry 3448 (class 2606 OID 74779)
-- Name: occchecklistspacetypeelement spaceelementid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occchecklistspacetypeelement
    ADD CONSTRAINT spaceelementid_pk PRIMARY KEY (spaceelementid);


--
-- TOC entry 3413 (class 2606 OID 65738)
-- Name: occspacetype spacetype_spacetypeid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occspacetype
    ADD CONSTRAINT spacetype_spacetypeid_pk PRIMARY KEY (spacetypeid);


--
-- TOC entry 3442 (class 2606 OID 66595)
-- Name: improvementsuggestion systemimprovements_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.improvementsuggestion
    ADD CONSTRAINT systemimprovements_pkey PRIMARY KEY (improvementid);


--
-- TOC entry 3547 (class 2606 OID 144130)
-- Name: taxstatus taxstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.taxstatus
    ADD CONSTRAINT taxstatus_pkey PRIMARY KEY (taxstatusid);


--
-- TOC entry 3432 (class 2606 OID 66153)
-- Name: textblock textblock_blockid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.textblock
    ADD CONSTRAINT textblock_blockid_pk PRIMARY KEY (blockid);


--
-- TOC entry 3523 (class 2606 OID 109032)
-- Name: propertyunitchange unitchangeid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyunitchange
    ADD CONSTRAINT unitchangeid_pk PRIMARY KEY (unitchangeid);


--
-- TOC entry 3381 (class 2606 OID 65293)
-- Name: propertyunit unitid_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyunit
    ADD CONSTRAINT unitid_pk PRIMARY KEY (unitid);


--
-- TOC entry 3580 (class 1259 OID 155445)
-- Name: fki_blobbytes_uploadedby_fk; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX fki_blobbytes_uploadedby_fk ON public.blobbytes USING btree (uploadedby_userid);


--
-- TOC entry 3392 (class 1259 OID 66220)
-- Name: fki_cecase_login_userid_fk; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX fki_cecase_login_userid_fk ON public.cecase USING btree (login_userid);


--
-- TOC entry 3428 (class 1259 OID 66226)
-- Name: fki_citation_userid_fk; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX fki_citation_userid_fk ON public.citation USING btree (login_userid);


--
-- TOC entry 3457 (class 1259 OID 87125)
-- Name: fki_genlogcategory_catid_fk; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX fki_genlogcategory_catid_fk ON public.log USING btree (category);


--
-- TOC entry 3409 (class 1259 OID 66169)
-- Name: fki_noticeOfViolation_recipient_fk; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX "fki_noticeOfViolation_recipient_fk" ON public.noticeofviolation USING btree (personid_recipient);


--
-- TOC entry 3375 (class 1259 OID 78886)
-- Name: property_address_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX property_address_idx ON public.property USING btree (address);


--
-- TOC entry 3700 (class 2606 OID 65235)
-- Name: ceactionrequestissuetype acrreqisstype_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequestissuetype
    ADD CONSTRAINT acrreqisstype_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 4075 (class 2606 OID 155440)
-- Name: blobbytes blobbytes_uploadedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blobbytes
    ADD CONSTRAINT blobbytes_uploadedby_fk FOREIGN KEY (uploadedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3815 (class 2606 OID 66154)
-- Name: textblock blockcategory_catid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.textblock
    ADD CONSTRAINT blockcategory_catid_fk FOREIGN KEY (blockcategory_catid) REFERENCES public.textblockcategory(categoryid);


--
-- TOC entry 3883 (class 2606 OID 106756)
-- Name: bobsource bobsource_creator_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bobsource
    ADD CONSTRAINT bobsource_creator_userid_fk FOREIGN KEY (creator) REFERENCES public.login(userid);


--
-- TOC entry 3882 (class 2606 OID 106761)
-- Name: bobsource bobsource_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bobsource
    ADD CONSTRAINT bobsource_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3718 (class 2606 OID 106498)
-- Name: ceactionrequest ceactionreq_usersub_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionreq_usersub_fk FOREIGN KEY (usersubmitter_userid) REFERENCES public.login(userid);


--
-- TOC entry 3725 (class 2606 OID 87126)
-- Name: ceactionrequest ceactionrequest_caseattachment_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_caseattachment_userid_fkey FOREIGN KEY (caseattachment_userid) REFERENCES public.login(userid);


--
-- TOC entry 3723 (class 2606 OID 65462)
-- Name: ceactionrequest ceactionrequest_caseid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_caseid FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 3720 (class 2606 OID 65347)
-- Name: ceactionrequest ceactionrequest_issuetypeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_issuetypeid_fk FOREIGN KEY (issuetype_issuetypeid) REFERENCES public.ceactionrequestissuetype(issuetypeid);


--
-- TOC entry 3721 (class 2606 OID 65352)
-- Name: ceactionrequest ceactionrequest_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3722 (class 2606 OID 65357)
-- Name: ceactionrequest ceactionrequest_prop_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_prop_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 3719 (class 2606 OID 65342)
-- Name: ceactionrequest ceactionrequest_requestorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_requestorid_fk FOREIGN KEY (actrequestor_requestorid) REFERENCES public.person(personid);


--
-- TOC entry 3699 (class 2606 OID 143793)
-- Name: ceactionrequestissuetype ceactionrequestissuetype_intensity_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequestissuetype
    ADD CONSTRAINT ceactionrequestissuetype_intensity_fk FOREIGN KEY (intensity_classid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 4081 (class 2606 OID 155299)
-- Name: ceactionrequestpdfdoc ceactionrequestpdfdoc_cear_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequestpdfdoc
    ADD CONSTRAINT ceactionrequestpdfdoc_cear_fk FOREIGN KEY (ceactionrequest_requestid) REFERENCES public.ceactionrequest(requestid);


--
-- TOC entry 4080 (class 2606 OID 155304)
-- Name: ceactionrequestpdfdoc ceactionrequestpdfdoc_pdfdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequestpdfdoc
    ADD CONSTRAINT ceactionrequestpdfdoc_pdfdoc_fk FOREIGN KEY (pdfdoc_pdfdocid) REFERENCES public.pdfdoc(pdfdocid);


--
-- TOC entry 3851 (class 2606 OID 95457)
-- Name: ceactionrequestphotodoc ceactionrequestphotodoc_cear_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequestphotodoc
    ADD CONSTRAINT ceactionrequestphotodoc_cear_fk FOREIGN KEY (ceactionrequest_requestid) REFERENCES public.ceactionrequest(requestid);


--
-- TOC entry 3852 (class 2606 OID 95452)
-- Name: ceactionrequestphotodoc ceactionrequestphotodoc_phdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequestphotodoc
    ADD CONSTRAINT ceactionrequestphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 3733 (class 2606 OID 108988)
-- Name: cecase cecase_bobsourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_bobsourceid_fk FOREIGN KEY (bobsource_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 3734 (class 2606 OID 144040)
-- Name: cecase cecase_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3731 (class 2606 OID 66215)
-- Name: cecase cecase_login_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_login_userid_fk FOREIGN KEY (login_userid) REFERENCES public.login(userid);


--
-- TOC entry 3736 (class 2606 OID 181059)
-- Name: cecase cecase_parcelkey_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_parcelkey_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 3726 (class 2606 OID 181064)
-- Name: ceactionrequest cecase_parcelkey_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT cecase_parcelkey_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 3735 (class 2606 OID 181054)
-- Name: cecase cecase_parcelunitid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_parcelunitid_fk FOREIGN KEY (parcelunit_unitid) REFERENCES public.parcelunit(unitid);


--
-- TOC entry 3732 (class 2606 OID 108983)
-- Name: cecase cecase_personid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_personid_fk FOREIGN KEY (personinfocase_personid) REFERENCES public.person(personid);


--
-- TOC entry 3730 (class 2606 OID 65447)
-- Name: cecase cecase_propertyid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_propertyid_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 4077 (class 2606 OID 155217)
-- Name: cecasephotodoc cecaseiolationphotodoc_cv_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecasephotodoc
    ADD CONSTRAINT cecaseiolationphotodoc_cv_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 4076 (class 2606 OID 163674)
-- Name: cecasephotodoc cecasephotodoc_photodocid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecasephotodoc
    ADD CONSTRAINT cecasephotodoc_photodocid_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 4221 (class 2606 OID 206181)
-- Name: cecasepin cecasepin_caseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecasepin
    ADD CONSTRAINT cecasepin_caseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 4220 (class 2606 OID 206186)
-- Name: cecasepin cecasepin_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cecasepin
    ADD CONSTRAINT cecasepin_userid_fk FOREIGN KEY (pinnedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3738 (class 2606 OID 65489)
-- Name: event ceevent_cecaseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT ceevent_cecaseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 3737 (class 2606 OID 65484)
-- Name: event ceevent_ceeventcategory_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT ceevent_ceeventcategory_fk FOREIGN KEY (category_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 3739 (class 2606 OID 66205)
-- Name: event ceevent_login_userid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT ceevent_login_userid FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3740 (class 2606 OID 143788)
-- Name: event ceevent_occperiodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT ceevent_occperiodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 3729 (class 2606 OID 103905)
-- Name: eventcategory ceeventcat_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventcategory
    ADD CONSTRAINT ceeventcat_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 3874 (class 2606 OID 106713)
-- Name: choiceproposal ceeventpropimp_genevent_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventpropimp_genevent_fk FOREIGN KEY (generatingevent_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 3875 (class 2606 OID 106718)
-- Name: choiceproposal ceeventpropimp_initiator_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventpropimp_initiator_fk FOREIGN KEY (initiator_userid) REFERENCES public.login(userid);


--
-- TOC entry 3873 (class 2606 OID 107264)
-- Name: choiceproposal ceeventpropimp_propid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventpropimp_propid_fk FOREIGN KEY (directive_directiveid) REFERENCES public.choicedirective(directiveid);


--
-- TOC entry 3878 (class 2606 OID 106733)
-- Name: choiceproposal ceeventpropimp_resev_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventpropimp_resev_fk FOREIGN KEY (responseevent_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 3877 (class 2606 OID 106728)
-- Name: choiceproposal ceeventpropimp_responderactual_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventpropimp_responderactual_fk FOREIGN KEY (responderactual_userid) REFERENCES public.login(userid);


--
-- TOC entry 3876 (class 2606 OID 106723)
-- Name: choiceproposal ceeventpropimp_responderintended_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventpropimp_responderintended_fk FOREIGN KEY (responderintended_userid) REFERENCES public.login(userid);


--
-- TOC entry 3940 (class 2606 OID 109096)
-- Name: choicedirectivedirectiveset choicedirdirset_dirid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choicedirectivedirectiveset
    ADD CONSTRAINT choicedirdirset_dirid_fk FOREIGN KEY (directive_dirid) REFERENCES public.choicedirective(directiveid);


--
-- TOC entry 3941 (class 2606 OID 109091)
-- Name: choicedirectivedirectiveset choicedirdirset_dirsetid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choicedirectivedirectiveset
    ADD CONSTRAINT choicedirdirset_dirsetid_fk FOREIGN KEY (directiveset_setid) REFERENCES public.choicedirectiveset(directivesetid);


--
-- TOC entry 3881 (class 2606 OID 110323)
-- Name: choiceproposal choiceproposal_cecaseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT choiceproposal_cecaseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 3879 (class 2606 OID 110685)
-- Name: choiceproposal choiceproposal_chosenchoiceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT choiceproposal_chosenchoiceid_fk FOREIGN KEY (chosen_choiceid) REFERENCES public.choice(choiceid);


--
-- TOC entry 3880 (class 2606 OID 110318)
-- Name: choiceproposal choiceproposal_occperiodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT choiceproposal_occperiodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 3805 (class 2606 OID 66090)
-- Name: citation citation_citationstatusid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT citation_citationstatusid_fk FOREIGN KEY (status_statusid) REFERENCES public.citationstatus(statusid);


--
-- TOC entry 3806 (class 2606 OID 66095)
-- Name: citation citation_courtentity_entityid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT citation_courtentity_entityid_fk FOREIGN KEY (origin_courtentity_entityid) REFERENCES public.courtentity(entityid);


--
-- TOC entry 3807 (class 2606 OID 66221)
-- Name: citation citation_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT citation_userid_fk FOREIGN KEY (login_userid) REFERENCES public.login(userid);


--
-- TOC entry 4106 (class 2606 OID 173146)
-- Name: citationcitationstatus citationcitationstatus_courtentityid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT citationcitationstatus_courtentityid_fk FOREIGN KEY (courtentity_entityid) REFERENCES public.courtentity(entityid);


--
-- TOC entry 4102 (class 2606 OID 163844)
-- Name: citationcitationstatus citationcitstatus_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT citationcitstatus_fk FOREIGN KEY (citationstatus_statusid) REFERENCES public.citationstatus(statusid);


--
-- TOC entry 4152 (class 2606 OID 173117)
-- Name: citationdocketno citationdocketno_citationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationdocketno
    ADD CONSTRAINT citationdocketno_citationid_fk FOREIGN KEY (citation_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 4148 (class 2606 OID 173029)
-- Name: citationdocketno citationdocketno_courtentityid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationdocketno
    ADD CONSTRAINT citationdocketno_courtentityid_fk FOREIGN KEY (courtentity_entityid) REFERENCES public.courtentity(entityid);


--
-- TOC entry 4154 (class 2606 OID 173078)
-- Name: citationdocketnohuman citationdocketnohuman_docketid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationdocketnohuman
    ADD CONSTRAINT citationdocketnohuman_docketid_fk FOREIGN KEY (docketno_docketid) REFERENCES public.citationdocketno(docketid);


--
-- TOC entry 4153 (class 2606 OID 173083)
-- Name: citationdocketnohuman citationdocketnohuman_humanlinkid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationdocketnohuman
    ADD CONSTRAINT citationdocketnohuman_humanlinkid_fk FOREIGN KEY (citationhuman_linkid) REFERENCES public.citationhuman(linkid);


--
-- TOC entry 4101 (class 2606 OID 163839)
-- Name: citationcitationstatus citationevent_citationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT citationevent_citationid_fk FOREIGN KEY (citation_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 4110 (class 2606 OID 163905)
-- Name: citationevent citationevent_citationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationevent
    ADD CONSTRAINT citationevent_citationid_fk FOREIGN KEY (citation_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 4109 (class 2606 OID 163910)
-- Name: citationevent citationevent_eventid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationevent
    ADD CONSTRAINT citationevent_eventid_fk FOREIGN KEY (event_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 4155 (class 2606 OID 173099)
-- Name: citationfilingtype citationfilingtype_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationfilingtype
    ADD CONSTRAINT citationfilingtype_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3811 (class 2606 OID 173104)
-- Name: citation citationfilitytype_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT citationfilitytype_typeid_fk FOREIGN KEY (filingtype_typeid) REFERENCES public.citationfilingtype(typeid);


--
-- TOC entry 4100 (class 2606 OID 164054)
-- Name: citationhuman citationhuman_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT citationhuman_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 4108 (class 2606 OID 163869)
-- Name: citationphotodoc citationphotodoc_cit_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationphotodoc
    ADD CONSTRAINT citationphotodoc_cit_fk FOREIGN KEY (citation_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 4107 (class 2606 OID 163874)
-- Name: citationphotodoc citationphotodoc_phdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationphotodoc
    ADD CONSTRAINT citationphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 3796 (class 2606 OID 173014)
-- Name: citationstatus citationstatus_courtentityid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationstatus
    ADD CONSTRAINT citationstatus_courtentityid_fk FOREIGN KEY (courtentity_entityid) REFERENCES public.courtentity(entityid);


--
-- TOC entry 3797 (class 2606 OID 107347)
-- Name: citationstatus citationstatus_eventruleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationstatus
    ADD CONSTRAINT citationstatus_eventruleid_fk FOREIGN KEY (eventrule_ruleid) REFERENCES public.eventrule(ruleid);


--
-- TOC entry 3798 (class 2606 OID 103915)
-- Name: citationstatus citationstatus_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationstatus
    ADD CONSTRAINT citationstatus_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 3801 (class 2606 OID 164219)
-- Name: citationviolation citationviol_citationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citationviol_citationid_fk FOREIGN KEY (citation_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 3799 (class 2606 OID 66045)
-- Name: citationviolation citationviolation_violationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citationviolation_violationid_fk FOREIGN KEY (codeviolation_violationid) REFERENCES public.codeviolation(violationid);


--
-- TOC entry 3800 (class 2606 OID 163879)
-- Name: citationviolation citviol_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citviol_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3803 (class 2606 OID 163889)
-- Name: citationviolation citviol_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citviol_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3802 (class 2606 OID 163884)
-- Name: citationviolation citviol_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citviol_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3804 (class 2606 OID 163897)
-- Name: citationviolation citviol_source_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citviol_source_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 3792 (class 2606 OID 65778)
-- Name: occchecklistspacetype cklist_spacetypeice_checklistid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occchecklistspacetype
    ADD CONSTRAINT cklist_spacetypeice_checklistid_fk FOREIGN KEY (checklist_id) REFERENCES public.occchecklist(checklistid);


--
-- TOC entry 3744 (class 2606 OID 65552)
-- Name: codeelement codeelement_codesource_sourceid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeelement
    ADD CONSTRAINT codeelement_codesource_sourceid FOREIGN KEY (codesource_sourceid) REFERENCES public.codesource(sourceid);


--
-- TOC entry 3746 (class 2606 OID 155135)
-- Name: codeelement codeelement_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeelement
    ADD CONSTRAINT codeelement_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3748 (class 2606 OID 155145)
-- Name: codeelement codeelement_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeelement
    ADD CONSTRAINT codeelement_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3747 (class 2606 OID 155140)
-- Name: codeelement codeelement_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeelement
    ADD CONSTRAINT codeelement_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3750 (class 2606 OID 65573)
-- Name: codesetelement codeseetelement_elementid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codeseetelement_elementid_fk FOREIGN KEY (codelement_elementid) REFERENCES public.codeelement(elementid);


--
-- TOC entry 3749 (class 2606 OID 65568)
-- Name: codesetelement codeseetelement_setid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codeseetelement_setid_fk FOREIGN KEY (codeset_codesetid) REFERENCES public.codeset(codesetid);


--
-- TOC entry 3727 (class 2606 OID 65373)
-- Name: codeset codeset_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeset
    ADD CONSTRAINT codeset_municode_fk FOREIGN KEY (municipality_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3751 (class 2606 OID 107137)
-- Name: codesetelement codesetele_severityclassdefault_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codesetele_severityclassdefault_classid_fk FOREIGN KEY (defaultseverityclass_classid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 3752 (class 2606 OID 155150)
-- Name: codesetelement codesetelement_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codesetelement_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3754 (class 2606 OID 155160)
-- Name: codesetelement codesetelement_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codesetelement_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3753 (class 2606 OID 155155)
-- Name: codesetelement codesetelement_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codesetelement_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3763 (class 2606 OID 155207)
-- Name: codeviolation codeviolation_bobsource_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_bobsource_fk FOREIGN KEY (bobsource_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 3756 (class 2606 OID 65637)
-- Name: codeviolation codeviolation_caseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_caseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 3755 (class 2606 OID 65632)
-- Name: codeviolation codeviolation_cdsetel_elementid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_cdsetel_elementid_fk FOREIGN KEY (codesetelement_elementid) REFERENCES public.codesetelement(codesetelementid);


--
-- TOC entry 3757 (class 2606 OID 93664)
-- Name: codeviolation codeviolation_complianceofficer_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_complianceofficer_fk FOREIGN KEY (complianceuser) REFERENCES public.login(userid);


--
-- TOC entry 3761 (class 2606 OID 144008)
-- Name: codeviolation codeviolation_lastupdatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_lastupdatedby_fk FOREIGN KEY (lastupdated_userid) REFERENCES public.login(userid);


--
-- TOC entry 3762 (class 2606 OID 144250)
-- Name: codeviolation codeviolation_nullifiedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_nullifiedby_fk FOREIGN KEY (nullifiedby) REFERENCES public.login(userid);


--
-- TOC entry 3760 (class 2606 OID 144003)
-- Name: codeviolation codeviolation_tfexpiry_proposalid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_tfexpiry_proposalid_fk FOREIGN KEY (compliancetfexpiry_proposalid) REFERENCES public.choiceproposal(proposalid);


--
-- TOC entry 3764 (class 2606 OID 181128)
-- Name: codeviolation codeviolation_transferredbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_transferredbyuserid_fk FOREIGN KEY (transferredby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3765 (class 2606 OID 181133)
-- Name: codeviolation codeviolation_transferredtocecase_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_transferredtocecase_fk FOREIGN KEY (transferredtocecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 3869 (class 2606 OID 106371)
-- Name: noticeofviolationcodeviolation codeviolation_violationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolationcodeviolation
    ADD CONSTRAINT codeviolation_violationid_fk FOREIGN KEY (codeviolation_violationid) REFERENCES public.codeviolation(violationid);


--
-- TOC entry 4083 (class 2606 OID 155314)
-- Name: codeviolationpdfdoc codeviolationpdfdoc_cv_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolationpdfdoc
    ADD CONSTRAINT codeviolationpdfdoc_cv_fk FOREIGN KEY (codeviolation_violationid) REFERENCES public.codeviolation(violationid);


--
-- TOC entry 4082 (class 2606 OID 155319)
-- Name: codeviolationpdfdoc codeviolationpdfdoc_pdfdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolationpdfdoc
    ADD CONSTRAINT codeviolationpdfdoc_pdfdoc_fk FOREIGN KEY (pdfdoc_pdfdocid) REFERENCES public.pdfdoc(pdfdocid);


--
-- TOC entry 3847 (class 2606 OID 95421)
-- Name: codeviolationphotodoc codeviolationphotodoc_cv_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolationphotodoc
    ADD CONSTRAINT codeviolationphotodoc_cv_fk FOREIGN KEY (codeviolation_violationid) REFERENCES public.codeviolation(violationid);


--
-- TOC entry 3848 (class 2606 OID 95416)
-- Name: codeviolationphotodoc codeviolationphotodoc_phdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolationphotodoc
    ADD CONSTRAINT codeviolationphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 3758 (class 2606 OID 105008)
-- Name: codeviolation codeviolationseverityclass_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolationseverityclass_fk FOREIGN KEY (severity_classid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 3990 (class 2606 OID 154290)
-- Name: contactphone contactemail_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT contactemail_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 3999 (class 2606 OID 154331)
-- Name: contactemail contactemail_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactemail
    ADD CONSTRAINT contactemail_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 3991 (class 2606 OID 154295)
-- Name: contactphone contactphone_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT contactphone_typeid_fk FOREIGN KEY (phonetype_typeid) REFERENCES public.contactphonetype(phonetypeid);


--
-- TOC entry 3862 (class 2606 OID 104998)
-- Name: intensityclass cvclass_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.intensityclass
    ADD CONSTRAINT cvclass_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3861 (class 2606 OID 105003)
-- Name: intensityclass cvclass_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.intensityclass
    ADD CONSTRAINT cvclass_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 4149 (class 2606 OID 173034)
-- Name: citationdocketno docketno_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationdocketno
    ADD CONSTRAINT docketno_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4126 (class 2606 OID 173166)
-- Name: mailingstreet docketno_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingstreet
    ADD CONSTRAINT docketno_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4151 (class 2606 OID 173044)
-- Name: citationdocketno docketno_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationdocketno
    ADD CONSTRAINT docketno_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4124 (class 2606 OID 173176)
-- Name: mailingstreet docketno_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingstreet
    ADD CONSTRAINT docketno_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4150 (class 2606 OID 173039)
-- Name: citationdocketno docketno_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationdocketno
    ADD CONSTRAINT docketno_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4125 (class 2606 OID 173171)
-- Name: mailingstreet docketno_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingstreet
    ADD CONSTRAINT docketno_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3998 (class 2606 OID 154336)
-- Name: contactemail email_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactemail
    ADD CONSTRAINT email_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3996 (class 2606 OID 154346)
-- Name: contactemail email_deactivated_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactemail
    ADD CONSTRAINT email_deactivated_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3997 (class 2606 OID 154341)
-- Name: contactemail email_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactemail
    ADD CONSTRAINT email_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4216 (class 2606 OID 206089)
-- Name: eventemission emitter_citation_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventemission
    ADD CONSTRAINT emitter_citation_fk FOREIGN KEY (emitter_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 3965 (class 2606 OID 143968)
-- Name: eventruleimpl erimpl_caseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_caseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 3971 (class 2606 OID 143998)
-- Name: eventruleimpl erimpl_deacby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_deacby_userid_fk FOREIGN KEY (deacby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3967 (class 2606 OID 143978)
-- Name: eventruleimpl erimpl_implby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_implby_userid_fk FOREIGN KEY (implby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3966 (class 2606 OID 143973)
-- Name: eventruleimpl erimpl_occperiodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_occperiodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 3970 (class 2606 OID 143993)
-- Name: eventruleimpl erimpl_passoverrideby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_passoverrideby_userid_fk FOREIGN KEY (passoverrideby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3968 (class 2606 OID 143983)
-- Name: eventruleimpl erimpl_triggeredevent_eventid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_triggeredevent_eventid_fk FOREIGN KEY (triggeredevent_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 3969 (class 2606 OID 143988)
-- Name: eventruleimpl erimpl_waivedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_waivedby_userid_fk FOREIGN KEY (waivedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3741 (class 2606 OID 143891)
-- Name: event event_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_createdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3742 (class 2606 OID 206104)
-- Name: event event_deactivatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_deactivatedby_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3743 (class 2606 OID 206266)
-- Name: event event_parcelkey_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_parcelkey_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 3728 (class 2606 OID 107408)
-- Name: eventcategory eventcategory_proposal_propid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventcategory
    ADD CONSTRAINT eventcategory_proposal_propid_fk FOREIGN KEY (directive_directiveid) REFERENCES public.choicedirective(directiveid);


--
-- TOC entry 3926 (class 2606 OID 107368)
-- Name: choice eventchoice_eventcatid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choice
    ADD CONSTRAINT eventchoice_eventcatid_fk FOREIGN KEY (eventcat_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 3924 (class 2606 OID 107436)
-- Name: choice eventchoice_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choice
    ADD CONSTRAINT eventchoice_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 3918 (class 2606 OID 107441)
-- Name: choicedirective eventchoice_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choicedirective
    ADD CONSTRAINT eventchoice_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 3925 (class 2606 OID 107373)
-- Name: choice eventchoice_ruleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choice
    ADD CONSTRAINT eventchoice_ruleid_fk FOREIGN KEY (eventrule_ruleid) REFERENCES public.eventrule(ruleid);


--
-- TOC entry 4215 (class 2606 OID 206094)
-- Name: eventemission eventemission_deactivatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventemission
    ADD CONSTRAINT eventemission_deactivatedby_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4218 (class 2606 OID 206079)
-- Name: eventemission eventemission_emitteruser_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventemission
    ADD CONSTRAINT eventemission_emitteruser_fk FOREIGN KEY (emittedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4219 (class 2606 OID 206074)
-- Name: eventemission eventemission_eventid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventemission
    ADD CONSTRAINT eventemission_eventid_fk FOREIGN KEY (event_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 4217 (class 2606 OID 206084)
-- Name: eventemission eventemission_novid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventemission
    ADD CONSTRAINT eventemission_novid_fk FOREIGN KEY (emitter_novid) REFERENCES public.noticeofviolation(noticeid);


--
-- TOC entry 4214 (class 2606 OID 206099)
-- Name: eventemission eventemission_response_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventemission
    ADD CONSTRAINT eventemission_response_fk FOREIGN KEY (emissionresponse_userid) REFERENCES public.login(userid);


--
-- TOC entry 4114 (class 2606 OID 164019)
-- Name: eventhuman eventhuman_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4116 (class 2606 OID 164029)
-- Name: eventhuman eventhuman_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4111 (class 2606 OID 164004)
-- Name: eventhuman eventhuman_eventid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_eventid_fk FOREIGN KEY (event_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 4115 (class 2606 OID 164024)
-- Name: eventhuman eventhuman_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4113 (class 2606 OID 164014)
-- Name: eventhuman eventhuman_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 4117 (class 2606 OID 164121)
-- Name: eventhuman eventhuman_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 3928 (class 2606 OID 107383)
-- Name: choicedirectivechoice eventpopchoice_choiceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choicedirectivechoice
    ADD CONSTRAINT eventpopchoice_choiceid_fk FOREIGN KEY (choice_choiceid) REFERENCES public.choice(choiceid);


--
-- TOC entry 3927 (class 2606 OID 107388)
-- Name: choicedirectivechoice eventpropchoice_proposalid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.choicedirectivechoice
    ADD CONSTRAINT eventpropchoice_proposalid FOREIGN KEY (directive_directiveid) REFERENCES public.choicedirective(directiveid);


--
-- TOC entry 3923 (class 2606 OID 110725)
-- Name: eventrule eventrule_directive_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT eventrule_directive_id_fk FOREIGN KEY (promptingdirective_directiveid) REFERENCES public.choicedirective(directiveid);


--
-- TOC entry 3920 (class 2606 OID 107332)
-- Name: eventrule eventrule_forbiddeneventcatid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT eventrule_forbiddeneventcatid_fk FOREIGN KEY (forbiddeneventcat_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 3919 (class 2606 OID 107327)
-- Name: eventrule eventrule_requiredeventcatid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT eventrule_requiredeventcatid_fk FOREIGN KEY (requiredeventcat_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 3922 (class 2606 OID 107342)
-- Name: eventrule eventrule_triggfaileventcatid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT eventrule_triggfaileventcatid_fk FOREIGN KEY (triggeredeventcatonfail) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 3921 (class 2606 OID 107337)
-- Name: eventrule eventrule_triggpasseventcatid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT eventrule_triggpasseventcatid_fk FOREIGN KEY (triggeredeventcatonpass) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 3931 (class 2606 OID 108880)
-- Name: eventruleruleset evruleevruleset_ruleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventruleruleset
    ADD CONSTRAINT evruleevruleset_ruleid_fk FOREIGN KEY (eventrule_ruleid) REFERENCES public.eventrule(ruleid);


--
-- TOC entry 3932 (class 2606 OID 108875)
-- Name: eventruleruleset evruleevruleset_setid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventruleruleset
    ADD CONSTRAINT evruleevruleset_setid_fk FOREIGN KEY (ruleset_rulesetid) REFERENCES public.eventruleset(rulesetid);


--
-- TOC entry 3845 (class 2606 OID 87107)
-- Name: log genlog_user_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.log
    ADD CONSTRAINT genlog_user_userid_fk FOREIGN KEY (user_userid) REFERENCES public.login(userid);


--
-- TOC entry 3844 (class 2606 OID 87120)
-- Name: log genlogcategory_catid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.log
    ADD CONSTRAINT genlogcategory_catid_fk FOREIGN KEY (category) REFERENCES public.logcategory(catid);


--
-- TOC entry 3745 (class 2606 OID 74785)
-- Name: codeelement guideentryid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeelement
    ADD CONSTRAINT guideentryid_fk FOREIGN KEY (guideentryid) REFERENCES public.codeelementguide(guideentryid);


--
-- TOC entry 3856 (class 2606 OID 95547)
-- Name: loginobjecthistory hist_ceactionrequest_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_ceactionrequest_fk FOREIGN KEY (ceactionrequest_requestid) REFERENCES public.ceactionrequest(requestid);


--
-- TOC entry 3857 (class 2606 OID 95552)
-- Name: loginobjecthistory hist_cecase_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_cecase_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 3858 (class 2606 OID 95557)
-- Name: loginobjecthistory hist_event_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_event_fk FOREIGN KEY (ceevent_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 3859 (class 2606 OID 95562)
-- Name: loginobjecthistory hist_occapp_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_occapp_fk FOREIGN KEY (occapp_appid) REFERENCES public.occpermitapplication(applicationid);


--
-- TOC entry 3854 (class 2606 OID 95537)
-- Name: loginobjecthistory hist_person_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_person_fk FOREIGN KEY (person_personid) REFERENCES public.person(personid);


--
-- TOC entry 3855 (class 2606 OID 95542)
-- Name: loginobjecthistory hist_prop_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_prop_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 3853 (class 2606 OID 95532)
-- Name: loginobjecthistory hist_user_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_user_fk FOREIGN KEY (login_userid) REFERENCES public.login(userid);


--
-- TOC entry 3986 (class 2606 OID 154191)
-- Name: human human_clone_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_clone_humanid_fk FOREIGN KEY (cloneof_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 3987 (class 2606 OID 154196)
-- Name: human human_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3808 (class 2606 OID 163696)
-- Name: citation human_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT human_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4103 (class 2606 OID 163849)
-- Name: citationcitationstatus human_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT human_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3989 (class 2606 OID 154206)
-- Name: human human_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3810 (class 2606 OID 163706)
-- Name: citation human_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT human_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4105 (class 2606 OID 163859)
-- Name: citationcitationstatus human_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT human_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3985 (class 2606 OID 154186)
-- Name: human human_deceasedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_deceasedby_userid_fk FOREIGN KEY (deceasedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3988 (class 2606 OID 154201)
-- Name: human human_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3809 (class 2606 OID 163701)
-- Name: citation human_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT human_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4104 (class 2606 OID 163854)
-- Name: citationcitationstatus human_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT human_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3890 (class 2606 OID 164362)
-- Name: occperiod human_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT human_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3984 (class 2606 OID 154181)
-- Name: human human_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4055 (class 2606 OID 155011)
-- Name: humancecase humancecase_caseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_caseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 4057 (class 2606 OID 155021)
-- Name: humancecase humancecase_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4059 (class 2606 OID 155031)
-- Name: humancecase humancecase_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4056 (class 2606 OID 155016)
-- Name: humancecase humancecase_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 4058 (class 2606 OID 155026)
-- Name: humancecase humancecase_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4061 (class 2606 OID 164059)
-- Name: humancecase humancecase_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 4060 (class 2606 OID 164064)
-- Name: humancecase humancecase_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4095 (class 2606 OID 163743)
-- Name: citationhuman humancitation_citationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_citationid_fk FOREIGN KEY (citation_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 4097 (class 2606 OID 163753)
-- Name: citationhuman humancitation_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4098 (class 2606 OID 163758)
-- Name: citationhuman humancitation_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4094 (class 2606 OID 163738)
-- Name: citationhuman humancitation_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 4099 (class 2606 OID 163763)
-- Name: citationhuman humancitation_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4096 (class 2606 OID 163748)
-- Name: citationhuman humancitation_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4071 (class 2606 OID 155106)
-- Name: humanmailingaddress humanmailing_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailing_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4073 (class 2606 OID 155116)
-- Name: humanmailingaddress humanmailing_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailing_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4069 (class 2606 OID 155086)
-- Name: humanmailingaddress humanmailing_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailing_humanid_fk FOREIGN KEY (humanmailing_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 4072 (class 2606 OID 155111)
-- Name: humanmailingaddress humanmailing_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailing_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4070 (class 2606 OID 155096)
-- Name: humanmailingaddress humanmailing_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailing_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4074 (class 2606 OID 172885)
-- Name: humanmailingaddress humanmailingaddress_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailingaddress_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 4043 (class 2606 OID 154959)
-- Name: humanmuni humanmuni_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4049 (class 2606 OID 154990)
-- Name: humanoccperiod humanmuni_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanmuni_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4045 (class 2606 OID 154969)
-- Name: humanmuni humanmuni_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4051 (class 2606 OID 155000)
-- Name: humanoccperiod humanmuni_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanmuni_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4044 (class 2606 OID 154964)
-- Name: humanmuni humanmuni_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4050 (class 2606 OID 154995)
-- Name: humanoccperiod humanmuni_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanmuni_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4046 (class 2606 OID 164171)
-- Name: humanmuni humanmuni_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 4042 (class 2606 OID 154954)
-- Name: humanmuni humanmuni_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 4041 (class 2606 OID 154949)
-- Name: humanmuni humanmuni_muniid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_muniid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 4048 (class 2606 OID 154985)
-- Name: humanoccperiod humanmuni_muniid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanmuni_muniid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 4047 (class 2606 OID 164176)
-- Name: humanmuni humanmuni_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4053 (class 2606 OID 164096)
-- Name: humanoccperiod humanoccperiod_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanoccperiod_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 4052 (class 2606 OID 164091)
-- Name: humanoccperiod humanoccperiod_periodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanoccperiod_periodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 4054 (class 2606 OID 164101)
-- Name: humanoccperiod humanoccperiod_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanoccperiod_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4040 (class 2606 OID 164106)
-- Name: humanparcel humanparcel_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT humanparcel_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 4064 (class 2606 OID 155065)
-- Name: humanparcelunit humanparcelunit_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT humanparcelunit_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4066 (class 2606 OID 155075)
-- Name: humanparcelunit humanparcelunit_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT humanparcelunit_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4065 (class 2606 OID 155070)
-- Name: humanparcelunit humanparcelunit_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT humanparcelunit_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4067 (class 2606 OID 164111)
-- Name: humanparcelunit humanparcelunit_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT humanparcelunit_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 4068 (class 2606 OID 164116)
-- Name: humanparcelunit humanparcelunit_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT humanparcelunit_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 3840 (class 2606 OID 103910)
-- Name: ceactionrequeststatus icon_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequeststatus
    ADD CONSTRAINT icon_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 3833 (class 2606 OID 103920)
-- Name: improvementstatus improvementstatus_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.improvementstatus
    ADD CONSTRAINT improvementstatus_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 3835 (class 2606 OID 66601)
-- Name: improvementsuggestion imptstatus_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.improvementsuggestion
    ADD CONSTRAINT imptstatus_fk FOREIGN KEY (statusid) REFERENCES public.improvementstatus(statusid);


--
-- TOC entry 3836 (class 2606 OID 66596)
-- Name: improvementsuggestion imptype_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.improvementsuggestion
    ADD CONSTRAINT imptype_fk FOREIGN KEY (improvementtypeid) REFERENCES public.improvementtype(typeid);


--
-- TOC entry 3976 (class 2606 OID 144203)
-- Name: codeelementinjectedvalue injectedvalue_codeset_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeelementinjectedvalue
    ADD CONSTRAINT injectedvalue_codeset_fk FOREIGN KEY (codeset_codesetid) REFERENCES public.codeset(codesetid);


--
-- TOC entry 3977 (class 2606 OID 144198)
-- Name: codeelementinjectedvalue injectedvalue_element_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeelementinjectedvalue
    ADD CONSTRAINT injectedvalue_element_fk FOREIGN KEY (codelement_eleid) REFERENCES public.codeelement(elementid);


--
-- TOC entry 3824 (class 2606 OID 74822)
-- Name: occinspectedspaceelement inspecchecklistspaceele_locdescid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT inspecchecklistspaceele_locdescid_fk FOREIGN KEY (locationdescription_id) REFERENCES public.occlocationdescriptor(locationdescriptionid);


--
-- TOC entry 3790 (class 2606 OID 66239)
-- Name: occchecklist inspectionchecklist_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occchecklist
    ADD CONSTRAINT inspectionchecklist_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3822 (class 2606 OID 110871)
-- Name: login login_createdby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_createdby_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3821 (class 2606 OID 119194)
-- Name: login login_decatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_decatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3817 (class 2606 OID 197710)
-- Name: login login_forcepasswordresetby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_forcepasswordresetby_userid_fk FOREIGN KEY (forcepasswordresetby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3820 (class 2606 OID 144055)
-- Name: login login_homemuni_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_homemuni_fk FOREIGN KEY (homemuni) REFERENCES public.municipality(municode);


--
-- TOC entry 3819 (class 2606 OID 197682)
-- Name: login login_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_humanid_fk FOREIGN KEY (humanlink_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 3818 (class 2606 OID 197705)
-- Name: login login_lastupdated_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_lastupdated_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3823 (class 2606 OID 95608)
-- Name: login login_personlink_personid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_personlink_personid_fk FOREIGN KEY (xarchivepersonlink) REFERENCES public.person(personid);


--
-- TOC entry 3816 (class 2606 OID 206308)
-- Name: login login_signature_photodocid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_signature_photodocid_fkey FOREIGN KEY (signature_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 3961 (class 2606 OID 111024)
-- Name: loginmuniauthperiodlog logincredentialex_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginmuniauthperiodlog
    ADD CONSTRAINT logincredentialex_muni_fk FOREIGN KEY (audit_muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3957 (class 2606 OID 111004)
-- Name: loginmuniauthperiodlog logincredentialex_periodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginmuniauthperiodlog
    ADD CONSTRAINT logincredentialex_periodid_fk FOREIGN KEY (authperiod_periodid) REFERENCES public.loginmuniauthperiod(muniauthperiodid);


--
-- TOC entry 3960 (class 2606 OID 111019)
-- Name: loginmuniauthperiodlog logincredentialex_usercredentialid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginmuniauthperiodlog
    ADD CONSTRAINT logincredentialex_usercredentialid_fk FOREIGN KEY (audit_usercredential_userid) REFERENCES public.login(userid);


--
-- TOC entry 3959 (class 2606 OID 111014)
-- Name: loginmuniauthperiodlog logincredentialex_usersessionid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginmuniauthperiodlog
    ADD CONSTRAINT logincredentialex_usersessionid_fk FOREIGN KEY (audit_usersession_userid) REFERENCES public.login(userid);


--
-- TOC entry 3843 (class 2606 OID 87084)
-- Name: munilogin loginmuni_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.munilogin
    ADD CONSTRAINT loginmuni_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3842 (class 2606 OID 87089)
-- Name: munilogin loginmuni_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.munilogin
    ADD CONSTRAINT loginmuni_userid_fk FOREIGN KEY (userid) REFERENCES public.login(userid);


--
-- TOC entry 3954 (class 2606 OID 110811)
-- Name: loginmuniauthperiod loginmuniauthperiod_creator_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginmuniauthperiod
    ADD CONSTRAINT loginmuniauthperiod_creator_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3952 (class 2606 OID 110801)
-- Name: loginmuniauthperiod loginmuniauthperiod_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginmuniauthperiod
    ADD CONSTRAINT loginmuniauthperiod_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3956 (class 2606 OID 180794)
-- Name: loginmuniauthperiod loginmuniauthperiod_oathcourtentityid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginmuniauthperiod
    ADD CONSTRAINT loginmuniauthperiod_oathcourtentityid_fk FOREIGN KEY (oathcourtentity_entityid) REFERENCES public.courtentity(entityid);


--
-- TOC entry 3955 (class 2606 OID 110884)
-- Name: loginmuniauthperiod loginmuniauthperiod_supportassignedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginmuniauthperiod
    ADD CONSTRAINT loginmuniauthperiod_supportassignedby_userid_fk FOREIGN KEY (supportassignedby) REFERENCES public.login(userid);


--
-- TOC entry 3953 (class 2606 OID 110806)
-- Name: loginmuniauthperiod loginmuniauthperiod_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginmuniauthperiod
    ADD CONSTRAINT loginmuniauthperiod_userid_fk FOREIGN KEY (authuser_userid) REFERENCES public.login(userid);


--
-- TOC entry 3958 (class 2606 OID 111009)
-- Name: loginmuniauthperiodlog loginmuniauthperiodsession_disputedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginmuniauthperiodlog
    ADD CONSTRAINT loginmuniauthperiodsession_disputedby_userid_fk FOREIGN KEY (disputedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3860 (class 2606 OID 138077)
-- Name: loginobjecthistory loginobjecthistory_occperiod_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT loginobjecthistory_occperiod_id_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 4224 (class 2606 OID 206342)
-- Name: loginphotodocs loginphotodoc_photodoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginphotodocs
    ADD CONSTRAINT loginphotodoc_photodoc_fk FOREIGN KEY (photodoc_id) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 4225 (class 2606 OID 206337)
-- Name: loginphotodocs loginphotodoc_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.loginphotodocs
    ADD CONSTRAINT loginphotodoc_userid_fk FOREIGN KEY (user_userid) REFERENCES public.login(userid);


--
-- TOC entry 4123 (class 2606 OID 164347)
-- Name: mailingaddress mailingaddress_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingaddress
    ADD CONSTRAINT mailingaddress_createdby_userid_fk FOREIGN KEY (verifiedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4121 (class 2606 OID 164357)
-- Name: mailingaddress mailingaddress_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingaddress
    ADD CONSTRAINT mailingaddress_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4120 (class 2606 OID 172520)
-- Name: mailingaddress mailingaddress_streetid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingaddress
    ADD CONSTRAINT mailingaddress_streetid_fk FOREIGN KEY (street_streetid) REFERENCES public.mailingstreet(streetid);


--
-- TOC entry 4122 (class 2606 OID 164352)
-- Name: mailingaddress mailingaddress_verifiedsourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingaddress
    ADD CONSTRAINT mailingaddress_verifiedsourceid_fk FOREIGN KEY (verifiedsource_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4021 (class 2606 OID 154773)
-- Name: parcelmailingaddress mailingaddressparcel_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT mailingaddressparcel_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4023 (class 2606 OID 154783)
-- Name: parcelmailingaddress mailingaddressparcel_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT mailingaddressparcel_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4022 (class 2606 OID 154778)
-- Name: parcelmailingaddress mailingaddressparcel_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT mailingaddressparcel_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4131 (class 2606 OID 181182)
-- Name: mailingcitystatezip mailingcitystatezip_created_by_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingcitystatezip
    ADD CONSTRAINT mailingcitystatezip_created_by_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4128 (class 2606 OID 181197)
-- Name: mailingcitystatezip mailingcitystatezip_deactivatedby_userid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingcitystatezip
    ADD CONSTRAINT mailingcitystatezip_deactivatedby_userid FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4129 (class 2606 OID 181192)
-- Name: mailingcitystatezip mailingcitystatezip_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingcitystatezip
    ADD CONSTRAINT mailingcitystatezip_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4130 (class 2606 OID 181187)
-- Name: mailingcitystatezip mailingcitystatezip_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingcitystatezip
    ADD CONSTRAINT mailingcitystatezip_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4019 (class 2606 OID 154758)
-- Name: parcelmailingaddress mailingparcel_parcelid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT mailingparcel_parcelid_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 4020 (class 2606 OID 154768)
-- Name: parcelmailingaddress mailingparcel_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT mailingparcel_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4127 (class 2606 OID 172532)
-- Name: mailingstreet mailingstreet_citystatezip_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mailingstreet
    ADD CONSTRAINT mailingstreet_citystatezip_fk FOREIGN KEY (citystatezip_cszipid) REFERENCES public.mailingcitystatezip(id);


--
-- TOC entry 4190 (class 2606 OID 190493)
-- Name: moneychargeoccpermittype moneychargepermittype_chargeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeoccpermittype
    ADD CONSTRAINT moneychargepermittype_chargeid_fk FOREIGN KEY (charge_id) REFERENCES public.moneychargeschedule(chargeid);


--
-- TOC entry 4191 (class 2606 OID 190488)
-- Name: moneychargeoccpermittype moneychargepermittype_permitid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeoccpermittype
    ADD CONSTRAINT moneychargepermittype_permitid_fk FOREIGN KEY (permittype_id) REFERENCES public.occpermittype(typeid);


--
-- TOC entry 4168 (class 2606 OID 190316)
-- Name: moneychargeschedule moneychargeschedule_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeschedule
    ADD CONSTRAINT moneychargeschedule_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4170 (class 2606 OID 190326)
-- Name: moneychargeschedule moneychargeschedule_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeschedule
    ADD CONSTRAINT moneychargeschedule_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4169 (class 2606 OID 190321)
-- Name: moneychargeschedule moneychargeschedule_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeschedule
    ADD CONSTRAINT moneychargeschedule_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4166 (class 2606 OID 190306)
-- Name: moneychargeschedule moneychargeschedule_ord_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeschedule
    ADD CONSTRAINT moneychargeschedule_ord_fk FOREIGN KEY (governingordinance_eceid) REFERENCES public.codesetelement(codesetelementid);


--
-- TOC entry 4167 (class 2606 OID 190311)
-- Name: moneychargeschedule moneychargeschedule_postingeventcat_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeschedule
    ADD CONSTRAINT moneychargeschedule_postingeventcat_fk FOREIGN KEY (eventcatwhenposted) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 4174 (class 2606 OID 190360)
-- Name: moneyledger moneyledger_caseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledger
    ADD CONSTRAINT moneyledger_caseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 4179 (class 2606 OID 190400)
-- Name: moneyledger moneyledger_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledger
    ADD CONSTRAINT moneyledger_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4184 (class 2606 OID 190433)
-- Name: moneyledgercharge moneyledger_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledgercharge
    ADD CONSTRAINT moneyledger_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4189 (class 2606 OID 190498)
-- Name: moneychargeoccpermittype moneyledger_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeoccpermittype
    ADD CONSTRAINT moneyledger_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4199 (class 2606 OID 190626)
-- Name: moneypmtmetadatamunicipay moneyledger_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneypmtmetadatamunicipay
    ADD CONSTRAINT moneyledger_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4181 (class 2606 OID 190410)
-- Name: moneyledger moneyledger_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledger
    ADD CONSTRAINT moneyledger_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4186 (class 2606 OID 190443)
-- Name: moneyledgercharge moneyledger_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledgercharge
    ADD CONSTRAINT moneyledger_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4187 (class 2606 OID 190508)
-- Name: moneychargeoccpermittype moneyledger_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeoccpermittype
    ADD CONSTRAINT moneyledger_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4197 (class 2606 OID 190636)
-- Name: moneypmtmetadatamunicipay moneyledger_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneypmtmetadatamunicipay
    ADD CONSTRAINT moneyledger_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4177 (class 2606 OID 190380)
-- Name: moneyledger moneyledger_event_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledger
    ADD CONSTRAINT moneyledger_event_fk FOREIGN KEY (event_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 4180 (class 2606 OID 190405)
-- Name: moneyledger moneyledger_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledger
    ADD CONSTRAINT moneyledger_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4185 (class 2606 OID 190438)
-- Name: moneyledgercharge moneyledger_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledgercharge
    ADD CONSTRAINT moneyledger_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4188 (class 2606 OID 190503)
-- Name: moneychargeoccpermittype moneyledger_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeoccpermittype
    ADD CONSTRAINT moneyledger_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4198 (class 2606 OID 190631)
-- Name: moneypmtmetadatamunicipay moneyledger_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneypmtmetadatamunicipay
    ADD CONSTRAINT moneyledger_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4178 (class 2606 OID 190395)
-- Name: moneyledger moneyledger_lockedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledger
    ADD CONSTRAINT moneyledger_lockedby_userid_fk FOREIGN KEY (lockedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4175 (class 2606 OID 190365)
-- Name: moneyledger moneyledger_occperiod_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledger
    ADD CONSTRAINT moneyledger_occperiod_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 4176 (class 2606 OID 190375)
-- Name: moneyledger moneyledger_transsource_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledger
    ADD CONSTRAINT moneyledger_transsource_fk FOREIGN KEY (source_id) REFERENCES public.moneytransactionsource(sourceid);


--
-- TOC entry 4183 (class 2606 OID 190428)
-- Name: moneyledgercharge moneyledgercharge_chargeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledgercharge
    ADD CONSTRAINT moneyledgercharge_chargeid_fk FOREIGN KEY (charge_id) REFERENCES public.moneychargeschedule(chargeid);


--
-- TOC entry 4195 (class 2606 OID 190549)
-- Name: moneypmtmetadatacheck moneypmtmetadatacheck_addressid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneypmtmetadatacheck
    ADD CONSTRAINT moneypmtmetadatacheck_addressid_fk FOREIGN KEY (mailingaddress_addressid) REFERENCES public.mailingaddress(addressid);


--
-- TOC entry 4194 (class 2606 OID 190554)
-- Name: moneypmtmetadatacheck moneypmtmetadatacheck_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneypmtmetadatacheck
    ADD CONSTRAINT moneypmtmetadatacheck_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4192 (class 2606 OID 190564)
-- Name: moneypmtmetadatacheck moneypmtmetadatacheck_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneypmtmetadatacheck
    ADD CONSTRAINT moneypmtmetadatacheck_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4193 (class 2606 OID 190559)
-- Name: moneypmtmetadatacheck moneypmtmetadatacheck_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneypmtmetadatacheck
    ADD CONSTRAINT moneypmtmetadatacheck_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4196 (class 2606 OID 190544)
-- Name: moneypmtmetadatacheck moneypmtmetadatacheck_transid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneypmtmetadatacheck
    ADD CONSTRAINT moneypmtmetadatacheck_transid_fk FOREIGN KEY (transaction_id) REFERENCES public.moneyledger(transactionid);


--
-- TOC entry 4200 (class 2606 OID 190621)
-- Name: moneypmtmetadatamunicipay moneypmtmetadatamunicipay_transid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneypmtmetadatamunicipay
    ADD CONSTRAINT moneypmtmetadatamunicipay_transid_fk FOREIGN KEY (transaction_id) REFERENCES public.moneyledger(transactionid);


--
-- TOC entry 4203 (class 2606 OID 190678)
-- Name: moneytransactionhuman moneytransactionhuman_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneytransactionhuman
    ADD CONSTRAINT moneytransactionhuman_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4201 (class 2606 OID 190688)
-- Name: moneytransactionhuman moneytransactionhuman_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneytransactionhuman
    ADD CONSTRAINT moneytransactionhuman_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4206 (class 2606 OID 190663)
-- Name: moneytransactionhuman moneytransactionhuman_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneytransactionhuman
    ADD CONSTRAINT moneytransactionhuman_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 4202 (class 2606 OID 190683)
-- Name: moneytransactionhuman moneytransactionhuman_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneytransactionhuman
    ADD CONSTRAINT moneytransactionhuman_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4204 (class 2606 OID 190673)
-- Name: moneytransactionhuman moneytransactionhuman_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneytransactionhuman
    ADD CONSTRAINT moneytransactionhuman_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 4205 (class 2606 OID 190668)
-- Name: moneytransactionhuman moneytransactionhuman_transid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneytransactionhuman
    ADD CONSTRAINT moneytransactionhuman_transid_fk FOREIGN KEY (transaction_id) REFERENCES public.moneyledger(transactionid);


--
-- TOC entry 4173 (class 2606 OID 190346)
-- Name: moneytransactionsource moneytransactionsource_eventcat_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneytransactionsource
    ADD CONSTRAINT moneytransactionsource_eventcat_fk FOREIGN KEY (eventcatwhenposted) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 4172 (class 2606 OID 190703)
-- Name: moneytransactionsource moneytransactionsource_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneytransactionsource
    ADD CONSTRAINT moneytransactionsource_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 4182 (class 2606 OID 190423)
-- Name: moneyledgercharge monleyledgercharge_transid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneyledgercharge
    ADD CONSTRAINT monleyledgercharge_transid_fk FOREIGN KEY (transaction_id) REFERENCES public.moneyledger(transactionid);


--
-- TOC entry 3681 (class 2606 OID 74719)
-- Name: municipality muni_defaultcodeset_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_defaultcodeset_fk FOREIGN KEY (defaultcodeset) REFERENCES public.codeset(codesetid);


--
-- TOC entry 3689 (class 2606 OID 206247)
-- Name: municipality muni_defheader_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_defheader_fk FOREIGN KEY (defaultheaderimage_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 3686 (class 2606 OID 143816)
-- Name: municipality muni_defoccperiod_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_defoccperiod_fk FOREIGN KEY (defaultoccperiod) REFERENCES public.occperiod(periodid);


--
-- TOC entry 3687 (class 2606 OID 109110)
-- Name: municipality muni_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_lastupdatedby_userid_fk FOREIGN KEY (lastupdated_userid) REFERENCES public.login(userid);


--
-- TOC entry 3685 (class 2606 OID 108951)
-- Name: municipality muni_manageruserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_manageruserid_fk FOREIGN KEY (munimanager_userid) REFERENCES public.login(userid);


--
-- TOC entry 3814 (class 2606 OID 66159)
-- Name: textblock muni_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.textblock
    ADD CONSTRAINT muni_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 4171 (class 2606 OID 190331)
-- Name: moneychargeschedule muni_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.moneychargeschedule
    ADD CONSTRAINT muni_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3682 (class 2606 OID 74833)
-- Name: municipality muni_occpermitissuingcodesource_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_occpermitissuingcodesource_sourceid_fk FOREIGN KEY (occpermitissuingsource_sourceid) REFERENCES public.codesource(sourceid);


--
-- TOC entry 3683 (class 2606 OID 106487)
-- Name: municipality muni_printstyleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_printstyleid_fk FOREIGN KEY (novprintstyle_styleid) REFERENCES public.printstyle(styleid);


--
-- TOC entry 3684 (class 2606 OID 108940)
-- Name: municipality muni_profileid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_profileid_fk FOREIGN KEY (profile_profileid) REFERENCES public.muniprofile(profileid);


--
-- TOC entry 3688 (class 2606 OID 109115)
-- Name: municipality muni_staffcontact_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_staffcontact_userid_fk FOREIGN KEY (primarystaffcontact_userid) REFERENCES public.login(userid);


--
-- TOC entry 3691 (class 2606 OID 65219)
-- Name: person municipality_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT municipality_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3690 (class 2606 OID 181162)
-- Name: municipality municipality_parcelid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT municipality_parcelid_fk FOREIGN KEY (officeparcel_parcelid) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 4132 (class 2606 OID 172547)
-- Name: municitystatezip municitystatezip_cszip_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municitystatezip
    ADD CONSTRAINT municitystatezip_cszip_fk FOREIGN KEY (citystatezip_id) REFERENCES public.mailingcitystatezip(id);


--
-- TOC entry 4133 (class 2606 OID 172542)
-- Name: municitystatezip municitystatezip_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municitystatezip
    ADD CONSTRAINT municitystatezip_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3934 (class 2606 OID 108978)
-- Name: municourtentity municourtentity_courtid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municourtentity
    ADD CONSTRAINT municourtentity_courtid_fk FOREIGN KEY (courtentity_entityid) REFERENCES public.courtentity(entityid);


--
-- TOC entry 3935 (class 2606 OID 108973)
-- Name: municourtentity municourtentity_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.municourtentity
    ADD CONSTRAINT municourtentity_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3841 (class 2606 OID 110664)
-- Name: munilogin munilogin_defcaseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.munilogin
    ADD CONSTRAINT munilogin_defcaseid_fk FOREIGN KEY (defaultcecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 4085 (class 2606 OID 155329)
-- Name: munipdfdoc munipdfdoc_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.munipdfdoc
    ADD CONSTRAINT munipdfdoc_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 4084 (class 2606 OID 155334)
-- Name: munipdfdoc munipdfdoc_pdid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.munipdfdoc
    ADD CONSTRAINT munipdfdoc_pdid_fk FOREIGN KEY (pdfdoc_pdfdocid) REFERENCES public.pdfdoc(pdfdocid);


--
-- TOC entry 3870 (class 2606 OID 106444)
-- Name: muniphotodoc muniphotodoc_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniphotodoc
    ADD CONSTRAINT muniphotodoc_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3871 (class 2606 OID 106439)
-- Name: muniphotodoc muniphotodoc_pdid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniphotodoc
    ADD CONSTRAINT muniphotodoc_pdid_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 3933 (class 2606 OID 108920)
-- Name: muniprofile muniprofile_lastupdateduserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniprofile
    ADD CONSTRAINT muniprofile_lastupdateduserid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3943 (class 2606 OID 110242)
-- Name: muniprofileeventruleset muniprofileeventruleset_profileid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniprofileeventruleset
    ADD CONSTRAINT muniprofileeventruleset_profileid_fk FOREIGN KEY (muniprofile_profileid) REFERENCES public.muniprofile(profileid);


--
-- TOC entry 3942 (class 2606 OID 110247)
-- Name: muniprofileeventruleset muniprofileeventruleset_setid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniprofileeventruleset
    ADD CONSTRAINT muniprofileeventruleset_setid_fk FOREIGN KEY (ruleset_setid) REFERENCES public.eventruleset(rulesetid);


--
-- TOC entry 4165 (class 2606 OID 189485)
-- Name: muniprofilefee muniprofilefee_profileid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniprofilefee
    ADD CONSTRAINT muniprofilefee_profileid_fk FOREIGN KEY (muniprofile_profileid) REFERENCES public.muniprofile(profileid);


--
-- TOC entry 3945 (class 2606 OID 110257)
-- Name: muniprofileoccperiodtype muniprofileoccperiodtype_profileid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniprofileoccperiodtype
    ADD CONSTRAINT muniprofileoccperiodtype_profileid_fk FOREIGN KEY (muniprofile_profileid) REFERENCES public.muniprofile(profileid);


--
-- TOC entry 3944 (class 2606 OID 110262)
-- Name: muniprofileoccperiodtype muniprofileoccperiodtype_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.muniprofileoccperiodtype
    ADD CONSTRAINT muniprofileoccperiodtype_typeid_fk FOREIGN KEY (occperiodtype_typeid) REFERENCES public.occpermittype(typeid);


--
-- TOC entry 3775 (class 2606 OID 66164)
-- Name: noticeofviolation noticeOfViolation_recipient_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT "noticeOfViolation_recipient_fk" FOREIGN KEY (personid_recipient) REFERENCES public.person(personid);


--
-- TOC entry 3868 (class 2606 OID 106376)
-- Name: noticeofviolationcodeviolation noticeofviolation_noticeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolationcodeviolation
    ADD CONSTRAINT noticeofviolation_noticeid_fk FOREIGN KEY (noticeofviolation_noticeid) REFERENCES public.noticeofviolation(noticeid);


--
-- TOC entry 3782 (class 2606 OID 205917)
-- Name: noticeofviolation noticeofviolation_photodocid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT noticeofviolation_photodocid_fk FOREIGN KEY (fixedheader_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 3774 (class 2606 OID 65682)
-- Name: noticeofviolation noticeofviolationcaseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT noticeofviolationcaseid_fk FOREIGN KEY (caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 4209 (class 2606 OID 205954)
-- Name: noticeofviolationtype noticeofviolationtype_blockcatid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolationtype
    ADD CONSTRAINT noticeofviolationtype_blockcatid_fk FOREIGN KEY (textblockcategory_catid) REFERENCES public.textblockcategory(categoryid);


--
-- TOC entry 4212 (class 2606 OID 205939)
-- Name: noticeofviolationtype noticeofviolationtype_eventcatfollowup_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolationtype
    ADD CONSTRAINT noticeofviolationtype_eventcatfollowup_fk FOREIGN KEY (eventcatfollowup_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 4211 (class 2606 OID 205944)
-- Name: noticeofviolationtype noticeofviolationtype_eventcatreturned_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolationtype
    ADD CONSTRAINT noticeofviolationtype_eventcatreturned_fk FOREIGN KEY (eventcatreturned_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 4213 (class 2606 OID 205934)
-- Name: noticeofviolationtype noticeofviolationtype_eventcatsent_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolationtype
    ADD CONSTRAINT noticeofviolationtype_eventcatsent_fk FOREIGN KEY (eventcatsent_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 4210 (class 2606 OID 205949)
-- Name: noticeofviolationtype noticeofviolationtype_headerimage_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolationtype
    ADD CONSTRAINT noticeofviolationtype_headerimage_fk FOREIGN KEY (headerimage_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 4208 (class 2606 OID 205959)
-- Name: noticeofviolationtype noticeofviolationtype_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolationtype
    ADD CONSTRAINT noticeofviolationtype_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3783 (class 2606 OID 205964)
-- Name: noticeofviolation noticeofviolationtype_novtypeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT noticeofviolationtype_novtypeid_fk FOREIGN KEY (letter_typeid) REFERENCES public.noticeofviolationtype(novtypeid);


--
-- TOC entry 4207 (class 2606 OID 205969)
-- Name: noticeofviolationtype noticeofviolationtype_printstyle_styleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolationtype
    ADD CONSTRAINT noticeofviolationtype_printstyle_styleid_fk FOREIGN KEY (printstyle_styleid) REFERENCES public.printstyle(styleid);


--
-- TOC entry 3776 (class 2606 OID 106406)
-- Name: noticeofviolation nov_creationby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_creationby_userid_fk FOREIGN KEY (creationby) REFERENCES public.login(userid);


--
-- TOC entry 3784 (class 2606 OID 152471)
-- Name: noticeofviolation nov_followup_eventid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_followup_eventid_fk FOREIGN KEY (followupevent_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 3786 (class 2606 OID 164181)
-- Name: noticeofviolation nov_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_humanid_fk FOREIGN KEY (recipient_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 3779 (class 2606 OID 106421)
-- Name: noticeofviolation nov_lockedandqueuedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_lockedandqueuedby_userid_fk FOREIGN KEY (lockedandqueuedformailingby) REFERENCES public.login(userid);


--
-- TOC entry 3788 (class 2606 OID 180981)
-- Name: noticeofviolation nov_mailing_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_mailing_fk FOREIGN KEY (recipient_mailing) REFERENCES public.mailingaddress(addressid);


--
-- TOC entry 3781 (class 2606 OID 155472)
-- Name: noticeofviolation nov_notifyingofficer_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_notifyingofficer_fk FOREIGN KEY (notifyingofficer_userid) REFERENCES public.login(userid);


--
-- TOC entry 3787 (class 2606 OID 180956)
-- Name: noticeofviolation nov_notifyingofficer_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_notifyingofficer_humanid_fk FOREIGN KEY (notifyingofficer_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 3780 (class 2606 OID 106492)
-- Name: noticeofviolation nov_printstyleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_printstyleid_fk FOREIGN KEY (printstyle_styleid) REFERENCES public.printstyle(styleid);


--
-- TOC entry 3777 (class 2606 OID 106411)
-- Name: noticeofviolation nov_returnedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_returnedby_fk FOREIGN KEY (lockedandqueuedformailingby) REFERENCES public.login(userid);


--
-- TOC entry 3778 (class 2606 OID 106416)
-- Name: noticeofviolation nov_sentby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_sentby_fk FOREIGN KEY (sentby) REFERENCES public.login(userid);


--
-- TOC entry 3785 (class 2606 OID 214479)
-- Name: noticeofviolation nov_sig_photodocid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_sig_photodocid_fk FOREIGN KEY (fixedissuingofficersig_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 4141 (class 2606 OID 172718)
-- Name: occchecklistphotorequirement occchecklistphotorequirement_checklist_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occchecklistphotorequirement
    ADD CONSTRAINT occchecklistphotorequirement_checklist_fk FOREIGN KEY (occphotorequirement_reqid) REFERENCES public.occchecklist(checklistid);


--
-- TOC entry 4140 (class 2606 OID 172723)
-- Name: occchecklistphotorequirement occchecklistphotorequirement_requirement_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occchecklistphotorequirement
    ADD CONSTRAINT occchecklistphotorequirement_requirement_fk FOREIGN KEY (occchecklist_checklistid) REFERENCES public.occphotorequirement(requirementid);


--
-- TOC entry 3791 (class 2606 OID 110328)
-- Name: occchecklistspacetype occchecklistspacetype_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occchecklistspacetype
    ADD CONSTRAINT occchecklistspacetype_typeid_fk FOREIGN KEY (spacetype_typeid) REFERENCES public.occspacetype(spacetypeid);


--
-- TOC entry 3839 (class 2606 OID 172708)
-- Name: occchecklistspacetypeelement occchecklistspacetypeelement_checklistspacetype_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occchecklistspacetypeelement
    ADD CONSTRAINT occchecklistspacetypeelement_checklistspacetype_typeid_fk FOREIGN KEY (checklistspacetype_typeid) REFERENCES public.occchecklistspacetype(checklistspacetypeid);


--
-- TOC entry 3838 (class 2606 OID 180789)
-- Name: occchecklistspacetypeelement occchecklistspacetypeelement_seteleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occchecklistspacetypeelement
    ADD CONSTRAINT occchecklistspacetypeelement_seteleid_fk FOREIGN KEY (codesetelement_seteleid) REFERENCES public.codesetelement(codesetelementid);


--
-- TOC entry 3902 (class 2606 OID 172645)
-- Name: occinspection occinpection_determinationby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinpection_determinationby_fk FOREIGN KEY (determinationby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4137 (class 2606 OID 172676)
-- Name: occinspectionphotodoc occinpectionphotodoc_requirement_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionphotodoc
    ADD CONSTRAINT occinpectionphotodoc_requirement_fk FOREIGN KEY (photorequirement_requirementid) REFERENCES public.occphotorequirement(requirementid);


--
-- TOC entry 3789 (class 2606 OID 106867)
-- Name: occchecklist occinspecchecklist_codesourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occchecklist
    ADD CONSTRAINT occinspecchecklist_codesourceid_fk FOREIGN KEY (governingcodesource_sourceid) REFERENCES public.codesource(sourceid);


--
-- TOC entry 3826 (class 2606 OID 107132)
-- Name: occinspectedspaceelement occinspectedchklstspel_complianceby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedchklstspel_complianceby_userid_fk FOREIGN KEY (compliancegrantedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3825 (class 2606 OID 107127)
-- Name: occinspectedspaceelement occinspectedchklstspel_lastinspecby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedchklstspel_lastinspecby_userid_fk FOREIGN KEY (lastinspectedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3946 (class 2606 OID 172577)
-- Name: occinspectedspace occinspectedspace_chklstspctypid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspace
    ADD CONSTRAINT occinspectedspace_chklstspctypid_fk FOREIGN KEY (occchecklistspacetype_chklstspctypid) REFERENCES public.occchecklistspacetype(checklistspacetypeid);


--
-- TOC entry 3949 (class 2606 OID 110410)
-- Name: occinspectedspace occinspectedspace_inspecid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspace
    ADD CONSTRAINT occinspectedspace_inspecid_fk FOREIGN KEY (occinspection_inspectionid) REFERENCES public.occinspection(inspectionid);


--
-- TOC entry 3947 (class 2606 OID 110420)
-- Name: occinspectedspace occinspectedspace_lastbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspace
    ADD CONSTRAINT occinspectedspace_lastbyuserid_fk FOREIGN KEY (addedtochecklistby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3948 (class 2606 OID 110415)
-- Name: occinspectedspace occinspectedspace_locid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspace
    ADD CONSTRAINT occinspectedspace_locid_fk FOREIGN KEY (occlocationdescription_descid) REFERENCES public.occlocationdescriptor(locationdescriptionid);


--
-- TOC entry 3827 (class 2606 OID 110628)
-- Name: occinspectedspaceelement occinspectedspaceele_intenclassid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedspaceele_intenclassid_fk FOREIGN KEY (failureseverity_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 3829 (class 2606 OID 110458)
-- Name: occinspectedspaceelement occinspectedspaceelement_elementid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedspaceelement_elementid_fk FOREIGN KEY (occchecklistspacetypeelement_elementid) REFERENCES public.occchecklistspacetypeelement(spaceelementid);


--
-- TOC entry 3830 (class 2606 OID 110440)
-- Name: occinspectedspaceelement occinspectedspaceelement_overridereq_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedspaceelement_overridereq_userid_fk FOREIGN KEY (overriderequiredflagnotinspected_userid) REFERENCES public.login(userid);


--
-- TOC entry 3828 (class 2606 OID 110425)
-- Name: occinspectedspaceelement occinspectedspaceelement_spaceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedspaceelement_spaceid_fk FOREIGN KEY (inspectedspace_inspectedspaceid) REFERENCES public.occinspectedspace(inspectedspaceid);


--
-- TOC entry 3831 (class 2606 OID 181138)
-- Name: occinspectedspaceelement occinspectedspaceelement_transferredbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedspaceelement_transferredbyuserid_fk FOREIGN KEY (transferredby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3832 (class 2606 OID 181143)
-- Name: occinspectedspaceelement occinspectedspaceelement_transferredtocecase_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedspaceelement_transferredtocecase_fk FOREIGN KEY (transferredtocecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 3849 (class 2606 OID 110453)
-- Name: occinspectedspaceelementphotodoc occinspectedspaceelementphotodoc_inspectedele_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelementphotodoc
    ADD CONSTRAINT occinspectedspaceelementphotodoc_inspectedele_fk FOREIGN KEY (inspectedspaceelement_elementid) REFERENCES public.occinspectedspaceelement(inspectedspaceelementid);


--
-- TOC entry 3850 (class 2606 OID 110430)
-- Name: occinspectedspaceelementphotodoc occinspectedspaceelementphotodoc_photodocid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectedspaceelementphotodoc
    ADD CONSTRAINT occinspectedspaceelementphotodoc_photodocid_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 3906 (class 2606 OID 172728)
-- Name: occinspection occinspection_cause_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_cause_fk FOREIGN KEY (cause_causeid) REFERENCES public.occinspectioncause(causeid);


--
-- TOC entry 3907 (class 2606 OID 181074)
-- Name: occinspection occinspection_cecaseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_cecaseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 3895 (class 2606 OID 108606)
-- Name: occinspection occinspection_checklistid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_checklistid_fk FOREIGN KEY (occchecklist_checklistlistid) REFERENCES public.occchecklist(checklistid);


--
-- TOC entry 3904 (class 2606 OID 172698)
-- Name: occinspection occinspection_creationby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_creationby_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3903 (class 2606 OID 172693)
-- Name: occinspection occinspection_deactivatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_deactivatedby_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3901 (class 2606 OID 172640)
-- Name: occinspection occinspection_determination_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_determination_fk FOREIGN KEY (determination_detid) REFERENCES public.occinspectiondetermination(determinationid);


--
-- TOC entry 3900 (class 2606 OID 172590)
-- Name: occinspection occinspection_followupto_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_followupto_fk FOREIGN KEY (followupto_inspectionid) REFERENCES public.occinspection(inspectionid);


--
-- TOC entry 3897 (class 2606 OID 106916)
-- Name: occinspection occinspection_inspector_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_inspector_userid_fk FOREIGN KEY (inspector_userid) REFERENCES public.login(userid);


--
-- TOC entry 3905 (class 2606 OID 172703)
-- Name: occinspection occinspection_lastupdatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_lastupdatedby_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3896 (class 2606 OID 106911)
-- Name: occinspection occinspection_periodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_periodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 3898 (class 2606 OID 107073)
-- Name: occinspection occinspection_thirdpartyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_thirdpartyuserid_fk FOREIGN KEY (thirdpartyinspector_personid) REFERENCES public.person(personid);


--
-- TOC entry 4136 (class 2606 OID 172635)
-- Name: occinspectiondetermination occinspectiondetermination_eventcat_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectiondetermination
    ADD CONSTRAINT occinspectiondetermination_eventcat_fk FOREIGN KEY (eventcat_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 4160 (class 2606 OID 180906)
-- Name: occinspectiondispatch occinspectiondispatch_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectiondispatch
    ADD CONSTRAINT occinspectiondispatch_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4157 (class 2606 OID 206274)
-- Name: occinspectiondispatch occinspectiondispatch_deacbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectiondispatch
    ADD CONSTRAINT occinspectiondispatch_deacbyuserid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4156 (class 2606 OID 206279)
-- Name: occinspectiondispatch occinspectiondispatch_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectiondispatch
    ADD CONSTRAINT occinspectiondispatch_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4159 (class 2606 OID 180911)
-- Name: occinspectiondispatch occinspectiondispatch_occinspectionid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectiondispatch
    ADD CONSTRAINT occinspectiondispatch_occinspectionid_fk FOREIGN KEY (inspection_inspectionid) REFERENCES public.occinspection(inspectionid);


--
-- TOC entry 4158 (class 2606 OID 180916)
-- Name: occinspectiondispatch occinspectiondispatch_retrievedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectiondispatch
    ADD CONSTRAINT occinspectiondispatch_retrievedby_userid_fk FOREIGN KEY (retrievedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4139 (class 2606 OID 172666)
-- Name: occinspectionphotodoc occinspectionphotodoc_inspection_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionphotodoc
    ADD CONSTRAINT occinspectionphotodoc_inspection_fk FOREIGN KEY (inspection_inspectionid) REFERENCES public.occinspection(inspectionid);


--
-- TOC entry 4138 (class 2606 OID 172671)
-- Name: occinspectionphotodoc occinspectionphotodoc_phtodoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionphotodoc
    ADD CONSTRAINT occinspectionphotodoc_phtodoc_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 4135 (class 2606 OID 172603)
-- Name: occinspectionpropertystatus occinspectionpropertystatus_inspection_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionpropertystatus
    ADD CONSTRAINT occinspectionpropertystatus_inspection_fk FOREIGN KEY (occinspection_inspectionid) REFERENCES public.occinspection(inspectionid);


--
-- TOC entry 4134 (class 2606 OID 172608)
-- Name: occinspectionpropertystatus occinspectionpropertystatus_status_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionpropertystatus
    ADD CONSTRAINT occinspectionpropertystatus_status_fk FOREIGN KEY (propertystatus_statusid) REFERENCES public.propertystatus(statusid);


--
-- TOC entry 4162 (class 2606 OID 181118)
-- Name: occinspectionrequirementassigned occinspectionrequirementassigned_assignedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionrequirementassigned
    ADD CONSTRAINT occinspectionrequirementassigned_assignedby_userid_fk FOREIGN KEY (assignedby) REFERENCES public.login(userid);


--
-- TOC entry 4161 (class 2606 OID 181123)
-- Name: occinspectionrequirementassigned occinspectionrequirementassigned_fulfilledby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionrequirementassigned
    ADD CONSTRAINT occinspectionrequirementassigned_fulfilledby_userid_fk FOREIGN KEY (fulfilledby) REFERENCES public.login(userid);


--
-- TOC entry 4163 (class 2606 OID 181113)
-- Name: occinspectionrequirementassigned occinspectionrequirementassigned_inspectionid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionrequirementassigned
    ADD CONSTRAINT occinspectionrequirementassigned_inspectionid_fk FOREIGN KEY (occinspection_inspectionid) REFERENCES public.occinspection(inspectionid);


--
-- TOC entry 4164 (class 2606 OID 181108)
-- Name: occinspectionrequirementassigned occinspectionrequirementassigned_requirementid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspectionrequirementassigned
    ADD CONSTRAINT occinspectionrequirementassigned_requirementid_fk FOREIGN KEY (occrequirement_requirementid) REFERENCES public.occinspectionrequirement(requirementid);


--
-- TOC entry 3899 (class 2606 OID 107078)
-- Name: occinspection occinspectionthirdpartyapprovalby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspectionthirdpartyapprovalby_fk FOREIGN KEY (thirdpartyinspectorapprovalby) REFERENCES public.login(userid);


--
-- TOC entry 3888 (class 2606 OID 106852)
-- Name: occperiod occperiod_authby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_authby_userid_fk FOREIGN KEY (authorizedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3889 (class 2606 OID 107413)
-- Name: occperiod occperiod_createdbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_createdbyuserid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3892 (class 2606 OID 181080)
-- Name: occperiod occperiod_deactivatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_deactivatedby_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3886 (class 2606 OID 106842)
-- Name: occperiod occperiod_endcert_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_endcert_userid_fk FOREIGN KEY (enddatecertifiedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3887 (class 2606 OID 106847)
-- Name: occperiod occperiod_mngr_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_mngr_userid_fk FOREIGN KEY (manager_userid) REFERENCES public.login(userid);


--
-- TOC entry 3891 (class 2606 OID 181048)
-- Name: occperiod occperiod_parcelunitid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_parcelunitid_fk FOREIGN KEY (parcelunit_unitid) REFERENCES public.parcelunit(unitid);


--
-- TOC entry 3884 (class 2606 OID 106817)
-- Name: occperiod occperiod_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 3885 (class 2606 OID 106837)
-- Name: occperiod occperiod_startcert_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_startcert_userid_fk FOREIGN KEY (startdatecertifiedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3962 (class 2606 OID 143843)
-- Name: occperiodeventrule occperiodeventrule2_attachedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodeventrule
    ADD CONSTRAINT occperiodeventrule2_attachedby_userid_fk FOREIGN KEY (attachedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3964 (class 2606 OID 143833)
-- Name: occperiodeventrule occperiodeventrule_op_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodeventrule
    ADD CONSTRAINT occperiodeventrule_op_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 3963 (class 2606 OID 143838)
-- Name: occperiodeventrule occperiodeventrule_ruleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodeventrule
    ADD CONSTRAINT occperiodeventrule_ruleid_fk FOREIGN KEY (eventrule_ruleid) REFERENCES public.eventrule(ruleid);


--
-- TOC entry 4087 (class 2606 OID 155359)
-- Name: occperiodpdfdoc occperiodpdfdoc__occperiod_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodpdfdoc
    ADD CONSTRAINT occperiodpdfdoc__occperiod_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 4086 (class 2606 OID 155364)
-- Name: occperiodpdfdoc occperiodpdfdoc_pdfdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodpdfdoc
    ADD CONSTRAINT occperiodpdfdoc_pdfdoc_fk FOREIGN KEY (pdfdoc_pdfdocid) REFERENCES public.pdfdoc(pdfdocid);


--
-- TOC entry 3929 (class 2606 OID 107431)
-- Name: occperiodpermitapplication occperiodpermitapp_appid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodpermitapplication
    ADD CONSTRAINT occperiodpermitapp_appid_fk FOREIGN KEY (occpermitapp_applicationid) REFERENCES public.occpermitapplication(applicationid);


--
-- TOC entry 3930 (class 2606 OID 107426)
-- Name: occperiodpermitapplication occperiodpermitapp_periodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodpermitapplication
    ADD CONSTRAINT occperiodpermitapp_periodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 3909 (class 2606 OID 107168)
-- Name: occperiodphotodoc occperiodphotodoc__occperiod_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodphotodoc
    ADD CONSTRAINT occperiodphotodoc__occperiod_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 3908 (class 2606 OID 107173)
-- Name: occperiodphotodoc occperiodphotodoc_phdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodphotodoc
    ADD CONSTRAINT occperiodphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 4223 (class 2606 OID 206197)
-- Name: occperiodpin occperiodpin_caseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodpin
    ADD CONSTRAINT occperiodpin_caseid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 4222 (class 2606 OID 206202)
-- Name: occperiodpin occperiodpin_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occperiodpin
    ADD CONSTRAINT occperiodpin_userid_fk FOREIGN KEY (pinnedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3893 (class 2606 OID 110199)
-- Name: occpermittype occperiodtype_eventrulesetid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermittype
    ADD CONSTRAINT occperiodtype_eventrulesetid_fk FOREIGN KEY (eventruleset_setid) REFERENCES public.eventruleset(rulesetid);


--
-- TOC entry 3894 (class 2606 OID 106890)
-- Name: occpermittype occperiodtype_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermittype
    ADD CONSTRAINT occperiodtype_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3913 (class 2606 OID 181236)
-- Name: occpermit occpermit_createdby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occpermit_createdby_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3915 (class 2606 OID 181246)
-- Name: occpermit occpermit_deactivatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occpermit_deactivatedby_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3912 (class 2606 OID 181231)
-- Name: occpermit occpermit_finalizedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occpermit_finalizedby_userid_fk FOREIGN KEY (finalizedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3914 (class 2606 OID 181241)
-- Name: occpermit occpermit_lastupdatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occpermit_lastupdatedby_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3916 (class 2606 OID 181264)
-- Name: occpermit occpermit_nullifiedby_userid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occpermit_nullifiedby_userid FOREIGN KEY (nullifiedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3910 (class 2606 OID 107220)
-- Name: occpermit occpermit_periodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occpermit_periodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 3911 (class 2606 OID 206347)
-- Name: occpermit occpermit_sig_photodocid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occpermit_sig_photodocid_fk FOREIGN KEY (staticsignature_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 3917 (class 2606 OID 189470)
-- Name: occpermit occpermit_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occpermit_typeid_fk FOREIGN KEY (permittype_typeid) REFERENCES public.occpermittype(typeid);


--
-- TOC entry 3794 (class 2606 OID 108547)
-- Name: occpermitapplication occpermitapp_periodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermitapplication
    ADD CONSTRAINT occpermitapp_periodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 4119 (class 2606 OID 164044)
-- Name: occpermitapplicationhuman occpermitapplicationhuman_applicationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermitapplicationhuman
    ADD CONSTRAINT occpermitapplicationhuman_applicationid_fk FOREIGN KEY (occpermitapplication_applicationid) REFERENCES public.occpermitapplication(applicationid);


--
-- TOC entry 4118 (class 2606 OID 164049)
-- Name: occpermitapplicationhuman occpermitapplicationhuman_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermitapplicationhuman
    ADD CONSTRAINT occpermitapplicationhuman_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 3793 (class 2606 OID 107068)
-- Name: occpermitapplicationreason occpermitapprsn_pertype_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermitapplicationreason
    ADD CONSTRAINT occpermitapprsn_pertype_fk FOREIGN KEY (periodtypeproposal_periodid) REFERENCES public.occpermittype(typeid);


--
-- TOC entry 4012 (class 2606 OID 154639)
-- Name: parcelinfo parcel_abandoned_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_abandoned_userid_fk FOREIGN KEY (abandonedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4013 (class 2606 OID 154644)
-- Name: parcelinfo parcel_bobsourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_bobsourceid_fk FOREIGN KEY (bobsource_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 3983 (class 2606 OID 181206)
-- Name: parcel parcel_broadview_photodicid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_broadview_photodicid_fk FOREIGN KEY (broadview_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 4014 (class 2606 OID 154649)
-- Name: parcelinfo parcel_conditionintensityclass_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_conditionintensityclass_classid_fk FOREIGN KEY (condition_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 3979 (class 2606 OID 154148)
-- Name: parcel parcel_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3981 (class 2606 OID 154158)
-- Name: parcel parcel_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4015 (class 2606 OID 154654)
-- Name: parcelinfo parcel_landbankprospect_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_landbankprospect_classid_fk FOREIGN KEY (landbankprospect_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 3980 (class 2606 OID 154153)
-- Name: parcel parcel_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3982 (class 2606 OID 154674)
-- Name: parcel parcel_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 4035 (class 2606 OID 154910)
-- Name: humanparcel parcel_parcelkey_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT parcel_parcelkey_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 4016 (class 2606 OID 154659)
-- Name: parcelinfo parcel_parcelusetypeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_parcelusetypeid_fk FOREIGN KEY (usetype_typeid) REFERENCES public.propertyusetype(propertyusetypeid);


--
-- TOC entry 3978 (class 2606 OID 154143)
-- Name: parcel parcel_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4036 (class 2606 OID 154915)
-- Name: humanparcel parcel_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT parcel_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4017 (class 2606 OID 154664)
-- Name: parcelinfo parcel_unfitby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_unfitby_userid_fk FOREIGN KEY (unfitby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4018 (class 2606 OID 154669)
-- Name: parcelinfo parcel_vacant_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_vacant_userid_fk FOREIGN KEY (vacantby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4037 (class 2606 OID 154925)
-- Name: humanparcel parcelhuman_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT parcelhuman_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4039 (class 2606 OID 154935)
-- Name: humanparcel parcelhuman_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT parcelhuman_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4034 (class 2606 OID 154905)
-- Name: humanparcel parcelhuman_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT parcelhuman_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 4038 (class 2606 OID 154930)
-- Name: humanparcel parcelhuman_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT parcelhuman_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4009 (class 2606 OID 154624)
-- Name: parcelinfo parcelinfo_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4011 (class 2606 OID 154634)
-- Name: parcelinfo parcelinfo_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4002 (class 2606 OID 206141)
-- Name: parcelinfo parcelinfo_landbankacqcandidatestart_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_landbankacqcandidatestart_userid_fk FOREIGN KEY (landbankacqcandidatestartby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4003 (class 2606 OID 206146)
-- Name: parcelinfo parcelinfo_landbankacqcandidatestop_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_landbankacqcandidatestop_userid_fk FOREIGN KEY (landbankacqcandidatestopby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4006 (class 2606 OID 206161)
-- Name: parcelinfo parcelinfo_landbankownedstart_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_landbankownedstart_userid_fk FOREIGN KEY (landbankownedstartby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4007 (class 2606 OID 206166)
-- Name: parcelinfo parcelinfo_landbankownedstop_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_landbankownedstop_userid_fk FOREIGN KEY (landbankownedstopby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4000 (class 2606 OID 206131)
-- Name: parcelinfo parcelinfo_landbankprospectstart_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_landbankprospectstart_userid_fk FOREIGN KEY (landbankprospectstartby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4001 (class 2606 OID 206136)
-- Name: parcelinfo parcelinfo_landbankprospectstop_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_landbankprospectstop_userid_fk FOREIGN KEY (landbankprospectstopby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4004 (class 2606 OID 206151)
-- Name: parcelinfo parcelinfo_landbankpursuingstart_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_landbankpursuingstart_userid_fk FOREIGN KEY (landbankpursuingstartby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4005 (class 2606 OID 206156)
-- Name: parcelinfo parcelinfo_landbankpursuingstop_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_landbankpursuingstop_userid_fk FOREIGN KEY (landbankpursuingstopby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4010 (class 2606 OID 154629)
-- Name: parcelinfo parcelinfo_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4008 (class 2606 OID 154619)
-- Name: parcelinfo parcelinfo_parcelkey_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_parcelkey_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 4024 (class 2606 OID 164166)
-- Name: parcelmailingaddress parcelmailing_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT parcelmailing_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 4025 (class 2606 OID 181069)
-- Name: parcelmailingaddress parcelmailingaddress_mailingaddressid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT parcelmailingaddress_mailingaddressid_fk FOREIGN KEY (mailingaddress_addressid) REFERENCES public.mailingaddress(addressid);


--
-- TOC entry 4145 (class 2606 OID 172860)
-- Name: parcelmigrationlog parcelmigration_errorcode; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmigrationlog
    ADD CONSTRAINT parcelmigration_errorcode FOREIGN KEY (error_code) REFERENCES public.parcelmigrationlogerrorcode(code);


--
-- TOC entry 4146 (class 2606 OID 172855)
-- Name: parcelmigrationlog parcelmigrationlog_parcelid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmigrationlog
    ADD CONSTRAINT parcelmigrationlog_parcelid_fk FOREIGN KEY (parcel_id) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 4147 (class 2606 OID 172850)
-- Name: parcelmigrationlog parcelmigrationlog_propid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelmigrationlog
    ADD CONSTRAINT parcelmigrationlog_propid_fk FOREIGN KEY (property_id) REFERENCES public.property(propertyid);


--
-- TOC entry 4089 (class 2606 OID 155374)
-- Name: parcelpdfdoc parcelpdfdoc_cv_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelpdfdoc
    ADD CONSTRAINT parcelpdfdoc_cv_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 4088 (class 2606 OID 155379)
-- Name: parcelpdfdoc parcelpdfdoc_phdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelpdfdoc
    ADD CONSTRAINT parcelpdfdoc_phdoc_fk FOREIGN KEY (pdfdoc_pdfdocid) REFERENCES public.pdfdoc(pdfdocid);


--
-- TOC entry 4091 (class 2606 OID 155389)
-- Name: parcelphotodoc parcelphotodoc_cv_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelphotodoc
    ADD CONSTRAINT parcelphotodoc_cv_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 4090 (class 2606 OID 155394)
-- Name: parcelphotodoc parcelphotodoc_phdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelphotodoc
    ADD CONSTRAINT parcelphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 4028 (class 2606 OID 154822)
-- Name: parcelunit parcelunit_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4030 (class 2606 OID 154832)
-- Name: parcelunit parcelunit_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4029 (class 2606 OID 154827)
-- Name: parcelunit parcelunit_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 4032 (class 2606 OID 155036)
-- Name: parcelunit parcelunit_loc_locationdescriptionid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_loc_locationdescriptionid_fk FOREIGN KEY (location_occlocationdescriptor) REFERENCES public.occlocationdescriptor(locationdescriptionid);


--
-- TOC entry 4026 (class 2606 OID 154812)
-- Name: parcelunit parcelunit_parcelkey_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_parcelkey_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 4033 (class 2606 OID 180971)
-- Name: parcelunit parcelunit_parcelmailingaddress_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_parcelmailingaddress_fk FOREIGN KEY (address_parcelmailingid) REFERENCES public.parcelmailingaddress(linkid);


--
-- TOC entry 4027 (class 2606 OID 154817)
-- Name: parcelunit parcelunit_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 4063 (class 2606 OID 155055)
-- Name: humanparcelunit parcelunithuman_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT parcelunithuman_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 4112 (class 2606 OID 164009)
-- Name: eventhuman parcelunithuman_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT parcelunithuman_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 4062 (class 2606 OID 155050)
-- Name: humanparcelunit parcelunithuman_unitid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT parcelunithuman_unitid_fk FOREIGN KEY (parcelunit_unitid) REFERENCES public.parcelunit(unitid);


--
-- TOC entry 4079 (class 2606 OID 155284)
-- Name: pdfdoc pdfdoc_blobbytes_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdfdoc
    ADD CONSTRAINT pdfdoc_blobbytes_fk FOREIGN KEY (blobbytes_bytesid) REFERENCES public.blobbytes(bytesid);


--
-- TOC entry 4078 (class 2606 OID 155289)
-- Name: pdfdoc pdfdoc_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pdfdoc
    ADD CONSTRAINT pdfdoc_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3697 (class 2606 OID 106265)
-- Name: person person_clone_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_clone_fk FOREIGN KEY (clonedby) REFERENCES public.login(userid);


--
-- TOC entry 3696 (class 2606 OID 106260)
-- Name: person person_clone_person_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_clone_person_fk FOREIGN KEY (cloneof) REFERENCES public.person(personid);


--
-- TOC entry 3692 (class 2606 OID 87163)
-- Name: person person_creator_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_creator_fk FOREIGN KEY (creator) REFERENCES public.login(userid);


--
-- TOC entry 3695 (class 2606 OID 106255)
-- Name: person person_ghost_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_ghost_fk FOREIGN KEY (ghostby) REFERENCES public.login(userid);


--
-- TOC entry 3694 (class 2606 OID 106250)
-- Name: person person_ghost_person_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_ghost_person_fk FOREIGN KEY (ghostof) REFERENCES public.person(personid);


--
-- TOC entry 3698 (class 2606 OID 143811)
-- Name: person person_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_sourceid_fk FOREIGN KEY (sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 3693 (class 2606 OID 95603)
-- Name: person person_userlink_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_userlink_userid_fk FOREIGN KEY (userlink) REFERENCES public.login(userid);


--
-- TOC entry 3975 (class 2606 OID 144101)
-- Name: personchange personchange_approvedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personchange
    ADD CONSTRAINT personchange_approvedby_fk FOREIGN KEY (approvedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3974 (class 2606 OID 144106)
-- Name: personchange personchange_changedbypersonid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personchange
    ADD CONSTRAINT personchange_changedbypersonid_fk FOREIGN KEY (changedby_personid) REFERENCES public.person(personid);


--
-- TOC entry 3973 (class 2606 OID 144111)
-- Name: personchange personchange_changedbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personchange
    ADD CONSTRAINT personchange_changedbyuserid_fk FOREIGN KEY (changedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3972 (class 2606 OID 144116)
-- Name: personchange personchange_personpersonid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personchange
    ADD CONSTRAINT personchange_personpersonid_fk FOREIGN KEY (person_personid) REFERENCES public.person(personid);


--
-- TOC entry 4142 (class 2606 OID 172763)
-- Name: personhumanmigrationlog personhumanmigrationlog_code_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personhumanmigrationlog
    ADD CONSTRAINT personhumanmigrationlog_code_fk FOREIGN KEY (error_code) REFERENCES public.personhumanmigrationlogerrorcode(code);


--
-- TOC entry 4144 (class 2606 OID 172753)
-- Name: personhumanmigrationlog personhumanmigrationlog_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personhumanmigrationlog
    ADD CONSTRAINT personhumanmigrationlog_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 4143 (class 2606 OID 172758)
-- Name: personhumanmigrationlog personhumanmigrationlog_personid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personhumanmigrationlog
    ADD CONSTRAINT personhumanmigrationlog_personid_fk FOREIGN KEY (person_personid) REFERENCES public.person(personid);


--
-- TOC entry 3864 (class 2606 OID 106329)
-- Name: personmergehistory personmerge_source_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personmergehistory
    ADD CONSTRAINT personmerge_source_fk FOREIGN KEY (mergesource_personid) REFERENCES public.person(personid);


--
-- TOC entry 3865 (class 2606 OID 106324)
-- Name: personmergehistory personmerge_target_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personmergehistory
    ADD CONSTRAINT personmerge_target_fk FOREIGN KEY (mergetarget_personid) REFERENCES public.person(personid);


--
-- TOC entry 3863 (class 2606 OID 106334)
-- Name: personmergehistory personmerge_user_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personmergehistory
    ADD CONSTRAINT personmerge_user_fk FOREIGN KEY (mergby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3867 (class 2606 OID 106350)
-- Name: personmunilink personmuni_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personmunilink
    ADD CONSTRAINT personmuni_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3866 (class 2606 OID 106355)
-- Name: personmunilink personmuni_personid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.personmunilink
    ADD CONSTRAINT personmuni_personid_fk FOREIGN KEY (person_personid) REFERENCES public.person(personid);


--
-- TOC entry 3993 (class 2606 OID 154305)
-- Name: contactphone phone_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT phone_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3995 (class 2606 OID 154315)
-- Name: contactphone phone_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT phone_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3992 (class 2606 OID 154300)
-- Name: contactphone phone_disconnected_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT phone_disconnected_userid_fk FOREIGN KEY (disconnect_userid) REFERENCES public.login(userid);


--
-- TOC entry 3994 (class 2606 OID 154310)
-- Name: contactphone phone_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT phone_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3766 (class 2606 OID 155190)
-- Name: photodoc photodoc_blobbytes_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.photodoc
    ADD CONSTRAINT photodoc_blobbytes_fk FOREIGN KEY (blobbytes_bytesid) REFERENCES public.blobbytes(bytesid);


--
-- TOC entry 3768 (class 2606 OID 155420)
-- Name: photodoc photodoc_blobtype_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.photodoc
    ADD CONSTRAINT photodoc_blobtype_typeid_fk FOREIGN KEY (blobtype_typeid) REFERENCES public.blobtype(typeid);


--
-- TOC entry 3769 (class 2606 OID 155446)
-- Name: photodoc photodoc_createdby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.photodoc
    ADD CONSTRAINT photodoc_createdby_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3771 (class 2606 OID 180826)
-- Name: photodoc photodoc_deactivatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.photodoc
    ADD CONSTRAINT photodoc_deactivatedby_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3770 (class 2606 OID 180821)
-- Name: photodoc photodoc_lastupdatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.photodoc
    ADD CONSTRAINT photodoc_lastupdatedby_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3767 (class 2606 OID 155195)
-- Name: photodoc photodoc_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.photodoc
    ADD CONSTRAINT photodoc_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3846 (class 2606 OID 103930)
-- Name: blobtype photodoctype_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.blobtype
    ADD CONSTRAINT photodoctype_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 3872 (class 2606 OID 106482)
-- Name: printstyle printstyle_headerimage_pdid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.printstyle
    ADD CONSTRAINT printstyle_headerimage_pdid_fk FOREIGN KEY (headerimage_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 3707 (class 2606 OID 110500)
-- Name: property property_abandoned_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_abandoned_userid_fk FOREIGN KEY (abandonedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3705 (class 2606 OID 110490)
-- Name: property property_bobsourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_bobsourceid_fk FOREIGN KEY (bobsource_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 3709 (class 2606 OID 110510)
-- Name: property property_conditionintensityclass_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_conditionintensityclass_classid_fk FOREIGN KEY (condition_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 3710 (class 2606 OID 110515)
-- Name: property property_landbankprospect_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_landbankprospect_classid_fk FOREIGN KEY (landbankprospect_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 3702 (class 2606 OID 65257)
-- Name: property property_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_municode_fk FOREIGN KEY (municipality_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3704 (class 2606 OID 110604)
-- Name: property property_propertyusetypeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_propertyusetypeid_fk FOREIGN KEY (usetype_typeid) REFERENCES public.propertyusetype(propertyusetypeid);


--
-- TOC entry 3706 (class 2606 OID 110495)
-- Name: property property_unfitby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_unfitby_userid_fk FOREIGN KEY (unfitby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3703 (class 2606 OID 66368)
-- Name: property property_updatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_updatedby_fk FOREIGN KEY (lastupdatedby) REFERENCES public.login(userid);


--
-- TOC entry 3708 (class 2606 OID 110505)
-- Name: property property_vacant_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_vacant_userid_fk FOREIGN KEY (vacantby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3712 (class 2606 OID 65278)
-- Name: propertyexternaldata propertyexternaldata_propid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyexternaldata
    ADD CONSTRAINT propertyexternaldata_propid_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 3711 (class 2606 OID 144132)
-- Name: propertyexternaldata propertyexternaldata_taxstatus_taxstatusid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyexternaldata
    ADD CONSTRAINT propertyexternaldata_taxstatus_taxstatusid_fkey FOREIGN KEY (taxstatus_taxstatusid) REFERENCES public.taxstatus(taxstatusid);


--
-- TOC entry 3951 (class 2606 OID 110623)
-- Name: propertyotherid propertyotherid_propid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyotherid
    ADD CONSTRAINT propertyotherid_propid_fk FOREIGN KEY (property_propid) REFERENCES public.property(propertyid);


--
-- TOC entry 4093 (class 2606 OID 155404)
-- Name: propertypdfdoc propertypdfdoc_pdid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertypdfdoc
    ADD CONSTRAINT propertypdfdoc_pdid_fk FOREIGN KEY (pdfdoc_pdfdocid) REFERENCES public.pdfdoc(pdfdocid);


--
-- TOC entry 4092 (class 2606 OID 155409)
-- Name: propertypdfdoc propertypdfdoc_prop_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertypdfdoc
    ADD CONSTRAINT propertypdfdoc_prop_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 3716 (class 2606 OID 65309)
-- Name: propertyperson propertyperson_personid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyperson
    ADD CONSTRAINT propertyperson_personid_fk FOREIGN KEY (person_personid) REFERENCES public.person(personid);


--
-- TOC entry 3717 (class 2606 OID 65304)
-- Name: propertyperson propertyperson_propid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyperson
    ADD CONSTRAINT propertyperson_propid_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 3773 (class 2606 OID 65663)
-- Name: propertyphotodoc propertyphotodoc_pdid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyphotodoc
    ADD CONSTRAINT propertyphotodoc_pdid_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 3772 (class 2606 OID 65668)
-- Name: propertyphotodoc propertyphotodoc_prop_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyphotodoc
    ADD CONSTRAINT propertyphotodoc_prop_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 3950 (class 2606 OID 110589)
-- Name: propertystatus propertystatus_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertystatus
    ADD CONSTRAINT propertystatus_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3715 (class 2606 OID 65294)
-- Name: propertyunit propertyunit_propertyid; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyunit
    ADD CONSTRAINT propertyunit_propertyid FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 3938 (class 2606 OID 109038)
-- Name: propertyunitchange propertyunitchange_approvedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyunitchange
    ADD CONSTRAINT propertyunitchange_approvedby_fk FOREIGN KEY (approvedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3936 (class 2606 OID 109048)
-- Name: propertyunitchange propertyunitchange_changedbypersonid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyunitchange
    ADD CONSTRAINT propertyunitchange_changedbypersonid_fk FOREIGN KEY (changedby_personid) REFERENCES public.person(personid);


--
-- TOC entry 3937 (class 2606 OID 109043)
-- Name: propertyunitchange propertyunitchange_changedbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyunitchange
    ADD CONSTRAINT propertyunitchange_changedbyuserid_fk FOREIGN KEY (changedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 3939 (class 2606 OID 109033)
-- Name: propertyunitchange propertyunitchange_unitid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyunitchange
    ADD CONSTRAINT propertyunitchange_unitid_fk FOREIGN KEY (propertyunit_unitid) REFERENCES public.propertyunit(unitid);


--
-- TOC entry 3701 (class 2606 OID 103935)
-- Name: propertyusetype propertyusetype_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyusetype
    ADD CONSTRAINT propertyusetype_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 3713 (class 2606 OID 110545)
-- Name: propertyunit propunit_conditionintensityclass_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyunit
    ADD CONSTRAINT propunit_conditionintensityclass_classid_fk FOREIGN KEY (condition_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 4031 (class 2606 OID 154837)
-- Name: parcelunit propunit_conditionintensityclass_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT propunit_conditionintensityclass_classid_fk FOREIGN KEY (condition_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 3714 (class 2606 OID 110178)
-- Name: propertyunit propunit_rentalintentupdatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.propertyunit
    ADD CONSTRAINT propunit_rentalintentupdatedby_fk FOREIGN KEY (rentalintentlastupdatedby_userid) REFERENCES public.eventrule(ruleid);


--
-- TOC entry 3795 (class 2606 OID 65889)
-- Name: occpermitapplication reason_reasonid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.occpermitapplication
    ADD CONSTRAINT reason_reasonid_fk FOREIGN KEY (reason_reasonid) REFERENCES public.occpermitapplicationreason(reasonid);


--
-- TOC entry 3724 (class 2606 OID 74860)
-- Name: ceactionrequest status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT status_id_fk FOREIGN KEY (status_id) REFERENCES public.ceactionrequeststatus(statusid);


--
-- TOC entry 3834 (class 2606 OID 66606)
-- Name: improvementsuggestion submitter_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.improvementsuggestion
    ADD CONSTRAINT submitter_fk FOREIGN KEY (submitterid) REFERENCES public.login(userid);


--
-- TOC entry 3813 (class 2606 OID 103940)
-- Name: textblockcategory textblockcategory_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.textblockcategory
    ADD CONSTRAINT textblockcategory_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 3837 (class 2606 OID 105056)
-- Name: codeelementguide textblockcategory_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeelementguide
    ADD CONSTRAINT textblockcategory_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 3812 (class 2606 OID 144245)
-- Name: textblockcategory textblockcategory_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.textblockcategory
    ADD CONSTRAINT textblockcategory_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 3759 (class 2606 OID 106401)
-- Name: codeviolation violation_creationby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT violation_creationby_userid_fk FOREIGN KEY (createdby) REFERENCES public.login(userid);


-- Completed on 2022-10-31 11:56:06 EDT

--
-- PostgreSQL database dump complete
--

