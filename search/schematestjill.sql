--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.25
-- Dumped by pg_dump version 10.19 (Ubuntu 10.19-0ubuntu0.18.04.1)

-- Started on 2022-01-28 20:23:47 EST

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
-- TOC entry 14 (class 2615 OID 20172)
-- Name: tiger; Type: SCHEMA; Schema: -; Owner: changeme
--

CREATE SCHEMA tiger;


ALTER SCHEMA tiger OWNER TO changeme;

--
-- TOC entry 15 (class 2615 OID 20441)
-- Name: tiger_data; Type: SCHEMA; Schema: -; Owner: changeme
--

CREATE SCHEMA tiger_data;


ALTER SCHEMA tiger_data OWNER TO changeme;

--
-- TOC entry 13 (class 2615 OID 20019)
-- Name: topology; Type: SCHEMA; Schema: -; Owner: changeme
--

CREATE SCHEMA topology;


ALTER SCHEMA topology OWNER TO changeme;

--
-- TOC entry 6187 (class 0 OID 0)
-- Dependencies: 13
-- Name: SCHEMA topology; Type: COMMENT; Schema: -; Owner: changeme
--

COMMENT ON SCHEMA topology IS 'PostGIS Topology schema';


--
-- TOC entry 1 (class 3079 OID 12361)
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- TOC entry 6188 (class 0 OID 0)
-- Dependencies: 1
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


--
-- TOC entry 3 (class 3079 OID 20161)
-- Name: fuzzystrmatch; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS fuzzystrmatch WITH SCHEMA public;


--
-- TOC entry 6189 (class 0 OID 0)
-- Dependencies: 3
-- Name: EXTENSION fuzzystrmatch; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION fuzzystrmatch IS 'determine similarities and distance between strings';


--
-- TOC entry 2 (class 3079 OID 20624)
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 6190 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- TOC entry 4 (class 3079 OID 18493)
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- TOC entry 6191 (class 0 OID 0)
-- Dependencies: 4
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


--
-- TOC entry 6 (class 3079 OID 20173)
-- Name: postgis_tiger_geocoder; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis_tiger_geocoder WITH SCHEMA tiger;


--
-- TOC entry 6192 (class 0 OID 0)
-- Dependencies: 6
-- Name: EXTENSION postgis_tiger_geocoder; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis_tiger_geocoder IS 'PostGIS tiger geocoder and reverse geocoder';


--
-- TOC entry 5 (class 3079 OID 20020)
-- Name: postgis_topology; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis_topology WITH SCHEMA topology;


--
-- TOC entry 6193 (class 0 OID 0)
-- Dependencies: 5
-- Name: EXTENSION postgis_topology; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis_topology IS 'PostGIS topology spatial types and functions';


--
-- TOC entry 2511 (class 1247 OID 20662)
-- Name: casephase; Type: TYPE; Schema: public; Owner: changeme
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


ALTER TYPE public.casephase OWNER TO changeme;

--
-- TOC entry 2514 (class 1247 OID 20686)
-- Name: citationviolationstatus; Type: TYPE; Schema: public; Owner: changeme
--

CREATE TYPE public.citationviolationstatus AS ENUM (
    'Pending',
    'Guilty',
    'Dismissed',
    'Compliance',
    'Deemed Invalid'
);


ALTER TYPE public.citationviolationstatus OWNER TO changeme;

--
-- TOC entry 2517 (class 1247 OID 20698)
-- Name: eventtype; Type: TYPE; Schema: public; Owner: changeme
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
    'Court'
);


ALTER TYPE public.eventtype OWNER TO changeme;

--
-- TOC entry 2520 (class 1247 OID 20732)
-- Name: linkedobjectroleschema; Type: TYPE; Schema: public; Owner: changeme
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
    'ParcelMailingaddress'
);


ALTER TYPE public.linkedobjectroleschema OWNER TO changeme;

--
-- TOC entry 2523 (class 1247 OID 20752)
-- Name: occapplicationstatus; Type: TYPE; Schema: public; Owner: changeme
--

CREATE TYPE public.occapplicationstatus AS ENUM (
    'Waiting',
    'NewUnit',
    'OldUnit',
    'Rejected',
    'Invalid'
);


ALTER TYPE public.occapplicationstatus OWNER TO changeme;

--
-- TOC entry 2526 (class 1247 OID 20764)
-- Name: occinspectionphototype; Type: TYPE; Schema: public; Owner: changeme
--

CREATE TYPE public.occinspectionphototype AS ENUM (
    'PassDocumentation',
    'FailDocumentation',
    'GeneralDocumentation',
    'Other',
    'Unused'
);


ALTER TYPE public.occinspectionphototype OWNER TO changeme;

--
-- TOC entry 2529 (class 1247 OID 20776)
-- Name: persontype; Type: TYPE; Schema: public; Owner: changeme
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


ALTER TYPE public.persontype OWNER TO changeme;

--
-- TOC entry 2532 (class 1247 OID 20818)
-- Name: requeststatusenum; Type: TYPE; Schema: public; Owner: changeme
--

CREATE TYPE public.requeststatusenum AS ENUM (
    'AwaitingReview',
    'UnderInvestigation',
    'NoViolationFound',
    'CitationFiled',
    'Resolved'
);


ALTER TYPE public.requeststatusenum OWNER TO changeme;

--
-- TOC entry 2535 (class 1247 OID 20830)
-- Name: role; Type: TYPE; Schema: public; Owner: changeme
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


ALTER TYPE public.role OWNER TO changeme;

--
-- TOC entry 1918 (class 1255 OID 20847)
-- Name: cnf_injectstaticnovdata(integer); Type: FUNCTION; Schema: public; Owner: changeme
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


ALTER FUNCTION public.cnf_injectstaticnovdata(targetmunicode integer) OWNER TO changeme;

--
-- TOC entry 1919 (class 1255 OID 20848)
-- Name: cnf_parsezipcode(text); Type: FUNCTION; Schema: public; Owner: changeme
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


ALTER FUNCTION public.cnf_parsezipcode(zipraw text) OWNER TO changeme;

--
-- TOC entry 1920 (class 1255 OID 20849)
-- Name: copycleartextpswds(); Type: FUNCTION; Schema: public; Owner: changeme
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


ALTER FUNCTION public.copycleartextpswds() OWNER TO changeme;

--
-- TOC entry 260 (class 1259 OID 20850)
-- Name: person_personidseq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.person_personidseq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.person_personidseq OWNER TO changeme;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 261 (class 1259 OID 20852)
-- Name: person; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.person OWNER TO changeme;

--
-- TOC entry 1921 (class 1255 OID 20866)
-- Name: createghostperson(public.person, integer); Type: FUNCTION; Schema: public; Owner: changeme
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


ALTER FUNCTION public.createghostperson(person_row public.person, userid integer) OWNER TO changeme;

--
-- TOC entry 1922 (class 1255 OID 20867)
-- Name: extractbuildingno(text); Type: FUNCTION; Schema: public; Owner: changeme
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


ALTER FUNCTION public.extractbuildingno(addr text) OWNER TO changeme;

--
-- TOC entry 1923 (class 1255 OID 20868)
-- Name: extractstreet(text); Type: FUNCTION; Schema: public; Owner: changeme
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


ALTER FUNCTION public.extractstreet(addr text) OWNER TO changeme;

--
-- TOC entry 1924 (class 1255 OID 20869)
-- Name: hashpasswords(); Type: FUNCTION; Schema: public; Owner: changeme
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


ALTER FUNCTION public.hashpasswords() OWNER TO changeme;

--
-- TOC entry 1925 (class 1255 OID 20870)
-- Name: migratepersontohuman(integer, integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: changeme
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


ALTER FUNCTION public.migratepersontohuman(creationrobotuser integer, defaultsource integer, municodetarget integer, parcel_human_lorid integer, human_mailing_lorid integer) OWNER TO changeme;

--
-- TOC entry 1926 (class 1255 OID 20872)
-- Name: migratepropertytoparcel(integer, integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: changeme
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


ALTER FUNCTION public.migratepropertytoparcel(creationrobotuser integer, defaultsource integer, cityid integer, municodetarget integer, parceladdr_lorid integer) OWNER TO changeme;

--
-- TOC entry 1927 (class 1255 OID 20874)
-- Name: resetsequences(integer); Type: FUNCTION; Schema: public; Owner: changeme
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


ALTER FUNCTION public.resetsequences(incr integer) OWNER TO changeme;

--
-- TOC entry 1928 (class 1255 OID 20875)
-- Name: unifyspacechars(text); Type: FUNCTION; Schema: public; Owner: changeme
--

CREATE FUNCTION public.unifyspacechars(chaostext text) RETURNS text
    LANGUAGE plpgsql
    AS $$

	BEGIN
		RETURN regexp_replace(chaostext, '[\s\u180e\u200B\u200C\u200D\u2060\uFEFF\u00a0]',' ','g'); 
	END;
$$;


ALTER FUNCTION public.unifyspacechars(chaostext text) OWNER TO changeme;

--
-- TOC entry 1929 (class 1255 OID 20876)
-- Name: unifyspacesandtrim(text); Type: FUNCTION; Schema: public; Owner: changeme
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


ALTER FUNCTION public.unifyspacesandtrim(chaostext text) OWNER TO changeme;

--
-- TOC entry 262 (class 1259 OID 20877)
-- Name: actionrqstissuetype_issuetypeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.actionrqstissuetype_issuetypeid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.actionrqstissuetype_issuetypeid_seq OWNER TO changeme;

--
-- TOC entry 263 (class 1259 OID 20879)
-- Name: blobbytes_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.blobbytes_seq
    START WITH 10
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.blobbytes_seq OWNER TO changeme;

--
-- TOC entry 264 (class 1259 OID 20881)
-- Name: blobbytes; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.blobbytes (
    bytesid integer DEFAULT nextval('public.blobbytes_seq'::regclass) NOT NULL,
    createdts timestamp with time zone,
    blob bytea,
    uploadedby_userid integer,
    filename text
);


ALTER TABLE public.blobbytes OWNER TO changeme;

--
-- TOC entry 265 (class 1259 OID 20888)
-- Name: blobtype; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.blobtype (
    typeid integer NOT NULL,
    typetitle text,
    icon_iconid integer
);


ALTER TABLE public.blobtype OWNER TO changeme;

--
-- TOC entry 266 (class 1259 OID 20894)
-- Name: blockcategory_categoryid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.blockcategory_categoryid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.blockcategory_categoryid_seq OWNER TO changeme;

--
-- TOC entry 267 (class 1259 OID 20896)
-- Name: bobsourceid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.bobsourceid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bobsourceid_seq OWNER TO changeme;

--
-- TOC entry 268 (class 1259 OID 20898)
-- Name: bobsource; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.bobsource OWNER TO changeme;

--
-- TOC entry 269 (class 1259 OID 20907)
-- Name: ceactionrequest_requestid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.ceactionrequest_requestid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ceactionrequest_requestid_seq OWNER TO changeme;

--
-- TOC entry 270 (class 1259 OID 20909)
-- Name: ceactionrequest; Type: TABLE; Schema: public; Owner: changeme
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
    active boolean DEFAULT true
);


ALTER TABLE public.ceactionrequest OWNER TO changeme;

--
-- TOC entry 271 (class 1259 OID 20920)
-- Name: ceactionrequestissuetype; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.ceactionrequestissuetype OWNER TO changeme;

--
-- TOC entry 272 (class 1259 OID 20928)
-- Name: ceactionrequestpdfdoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.ceactionrequestpdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    ceactionrequest_requestid integer NOT NULL
);


ALTER TABLE public.ceactionrequestpdfdoc OWNER TO changeme;

--
-- TOC entry 273 (class 1259 OID 20931)
-- Name: ceactionrequestphotodoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.ceactionrequestphotodoc (
    photodoc_photodocid integer NOT NULL,
    ceactionrequest_requestid integer NOT NULL
);


ALTER TABLE public.ceactionrequestphotodoc OWNER TO changeme;

--
-- TOC entry 274 (class 1259 OID 20934)
-- Name: ceactionrequeststatus; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.ceactionrequeststatus (
    statusid integer NOT NULL,
    title text,
    description text,
    icon_iconid integer
);


ALTER TABLE public.ceactionrequeststatus OWNER TO changeme;

--
-- TOC entry 275 (class 1259 OID 20940)
-- Name: cecase_caseid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.cecase_caseid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cecase_caseid_seq OWNER TO changeme;

--
-- TOC entry 276 (class 1259 OID 20942)
-- Name: cecase; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.cecase (
    caseid integer DEFAULT nextval('public.cecase_caseid_seq'::regclass) NOT NULL,
    cecasepubliccc integer NOT NULL,
    property_propertyid integer NOT NULL,
    propertyunit_unitid integer,
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
    lastupdatedts timestamp with time zone
);


ALTER TABLE public.cecase OWNER TO changeme;

--
-- TOC entry 6194 (class 0 OID 0)
-- Dependencies: 276
-- Name: TABLE cecase; Type: COMMENT; Schema: public; Owner: changeme
--

COMMENT ON TABLE public.cecase IS 'I can comment here and see there!';


--
-- TOC entry 6195 (class 0 OID 0)
-- Dependencies: 276
-- Name: COLUMN cecase.casename; Type: COMMENT; Schema: public; Owner: changeme
--

COMMENT ON COLUMN public.cecase.casename IS 'Column Comment';


--
-- TOC entry 277 (class 1259 OID 20953)
-- Name: cecasephasechangerule_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.cecasephasechangerule_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cecasephasechangerule_seq OWNER TO changeme;

--
-- TOC entry 278 (class 1259 OID 20955)
-- Name: cecasephotodoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.cecasephotodoc (
    photodoc_photodocid integer NOT NULL,
    cecase_caseid integer NOT NULL
);


ALTER TABLE public.cecasephotodoc OWNER TO changeme;

--
-- TOC entry 279 (class 1259 OID 20958)
-- Name: cecasestatusicon; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.cecasestatusicon (
    iconid integer NOT NULL,
    status public.casephase NOT NULL
);


ALTER TABLE public.cecasestatusicon OWNER TO changeme;

--
-- TOC entry 280 (class 1259 OID 20961)
-- Name: ceevent_eventid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.ceevent_eventid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ceevent_eventid_seq OWNER TO changeme;

--
-- TOC entry 281 (class 1259 OID 20963)
-- Name: ceeventcategory_categoryid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.ceeventcategory_categoryid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ceeventcategory_categoryid_seq OWNER TO changeme;

--
-- TOC entry 282 (class 1259 OID 20965)
-- Name: ceeventproposal_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.ceeventproposal_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ceeventproposal_seq OWNER TO changeme;

--
-- TOC entry 283 (class 1259 OID 20967)
-- Name: ceeventproposalimplementation_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.ceeventproposalimplementation_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ceeventproposalimplementation_seq OWNER TO changeme;

--
-- TOC entry 284 (class 1259 OID 20969)
-- Name: checklist_checklistid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.checklist_checklistid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.checklist_checklistid_seq OWNER TO changeme;

--
-- TOC entry 285 (class 1259 OID 20971)
-- Name: chkliststiceid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.chkliststiceid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.chkliststiceid_seq OWNER TO changeme;

--
-- TOC entry 286 (class 1259 OID 20973)
-- Name: choice_choiceid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.choice_choiceid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.choice_choiceid_seq OWNER TO changeme;

--
-- TOC entry 287 (class 1259 OID 20975)
-- Name: choice; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.choice OWNER TO changeme;

--
-- TOC entry 288 (class 1259 OID 20987)
-- Name: eventproposal_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.eventproposal_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.eventproposal_seq OWNER TO changeme;

--
-- TOC entry 289 (class 1259 OID 20989)
-- Name: choicedirective; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.choicedirective OWNER TO changeme;

--
-- TOC entry 290 (class 1259 OID 21015)
-- Name: choicedirectivechoice; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.choicedirectivechoice (
    choice_choiceid integer NOT NULL,
    directive_directiveid integer NOT NULL
);


ALTER TABLE public.choicedirectivechoice OWNER TO changeme;

--
-- TOC entry 291 (class 1259 OID 21018)
-- Name: choicedirectivedirectiveset; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.choicedirectivedirectiveset (
    directiveset_setid integer NOT NULL,
    directive_dirid integer NOT NULL
);


ALTER TABLE public.choicedirectivedirectiveset OWNER TO changeme;

--
-- TOC entry 292 (class 1259 OID 21021)
-- Name: choicedirectivesetid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.choicedirectivesetid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.choicedirectivesetid_seq OWNER TO changeme;

--
-- TOC entry 293 (class 1259 OID 21023)
-- Name: choicedirectiveset; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.choicedirectiveset (
    directivesetid integer DEFAULT nextval('public.choicedirectivesetid_seq'::regclass) NOT NULL,
    title text,
    description text
);


ALTER TABLE public.choicedirectiveset OWNER TO changeme;

--
-- TOC entry 294 (class 1259 OID 21030)
-- Name: choiceproposal; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.choiceproposal OWNER TO changeme;

--
-- TOC entry 295 (class 1259 OID 21039)
-- Name: citation_citationid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.citation_citationid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citation_citationid_seq OWNER TO changeme;

--
-- TOC entry 296 (class 1259 OID 21041)
-- Name: citation; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.citation (
    citationid integer DEFAULT nextval('public.citation_citationid_seq'::regclass) NOT NULL,
    citationno text,
    status_statusid integer NOT NULL,
    origin_courtentity_entityid integer NOT NULL,
    login_userid integer NOT NULL,
    dateofrecord timestamp with time zone NOT NULL,
    transtimestamp timestamp with time zone NOT NULL,
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


ALTER TABLE public.citation OWNER TO changeme;

--
-- TOC entry 297 (class 1259 OID 21049)
-- Name: citationcitationstatus_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.citationcitationstatus_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationcitationstatus_seq OWNER TO changeme;

--
-- TOC entry 298 (class 1259 OID 21051)
-- Name: citationcitationstatus; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.citationcitationstatus OWNER TO changeme;

--
-- TOC entry 299 (class 1259 OID 21058)
-- Name: citationdockethuman_linkid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.citationdockethuman_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationdockethuman_linkid_seq OWNER TO changeme;

--
-- TOC entry 300 (class 1259 OID 21060)
-- Name: citationdocketno_docketid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.citationdocketno_docketid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationdocketno_docketid_seq OWNER TO changeme;

--
-- TOC entry 301 (class 1259 OID 21062)
-- Name: citationdocketno; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.citationdocketno OWNER TO changeme;

--
-- TOC entry 302 (class 1259 OID 21069)
-- Name: citationdocketnohuman; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.citationdocketnohuman (
    linkid integer DEFAULT nextval('public.citationdockethuman_linkid_seq'::regclass) NOT NULL,
    docketno_docketid integer NOT NULL,
    citationhuman_linkid integer NOT NULL,
    notes text
);


ALTER TABLE public.citationdocketnohuman OWNER TO changeme;

--
-- TOC entry 303 (class 1259 OID 21076)
-- Name: citationevent; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.citationevent (
    citation_citationid integer NOT NULL,
    event_eventid integer NOT NULL
);


ALTER TABLE public.citationevent OWNER TO changeme;

--
-- TOC entry 304 (class 1259 OID 21079)
-- Name: citationfilingtype_typeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.citationfilingtype_typeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationfilingtype_typeid_seq OWNER TO changeme;

--
-- TOC entry 305 (class 1259 OID 21081)
-- Name: citationfilingtype; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.citationfilingtype (
    typeid integer DEFAULT nextval('public.citationfilingtype_typeid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    muni_municode integer NOT NULL,
    active boolean DEFAULT true
);


ALTER TABLE public.citationfilingtype OWNER TO changeme;

--
-- TOC entry 306 (class 1259 OID 21089)
-- Name: citationhuman_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.citationhuman_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationhuman_seq OWNER TO changeme;

--
-- TOC entry 307 (class 1259 OID 21091)
-- Name: citationhuman; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.citationhuman OWNER TO changeme;

--
-- TOC entry 308 (class 1259 OID 21098)
-- Name: citationphotodoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.citationphotodoc (
    photodoc_photodocid integer NOT NULL,
    citation_citationid integer NOT NULL
);


ALTER TABLE public.citationphotodoc OWNER TO changeme;

--
-- TOC entry 309 (class 1259 OID 21101)
-- Name: citationstatus_statusid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.citationstatus_statusid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationstatus_statusid_seq OWNER TO changeme;

--
-- TOC entry 310 (class 1259 OID 21103)
-- Name: citationstatus; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.citationstatus (
    statusid integer DEFAULT nextval('public.citationstatus_statusid_seq'::regclass) NOT NULL,
    statusname text NOT NULL,
    description text NOT NULL,
    icon_iconid integer,
    editsforbidden boolean DEFAULT true,
    eventrule_ruleid integer,
    courtentity_entityid integer
);


ALTER TABLE public.citationstatus OWNER TO changeme;

--
-- TOC entry 311 (class 1259 OID 21111)
-- Name: citationviolation_cvid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.citationviolation_cvid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationviolation_cvid_seq OWNER TO changeme;

--
-- TOC entry 312 (class 1259 OID 21113)
-- Name: citationviolation; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.citationviolation OWNER TO changeme;

--
-- TOC entry 313 (class 1259 OID 21120)
-- Name: codeelement_elementid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.codeelement_elementid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codeelement_elementid_seq OWNER TO changeme;

--
-- TOC entry 314 (class 1259 OID 21122)
-- Name: codeelement; Type: TABLE; Schema: public; Owner: changeme
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
    createdts timestamp with time zone
);


ALTER TABLE public.codeelement OWNER TO changeme;

--
-- TOC entry 315 (class 1259 OID 21130)
-- Name: codeelementguide_id_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.codeelementguide_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codeelementguide_id_seq OWNER TO changeme;

--
-- TOC entry 316 (class 1259 OID 21132)
-- Name: codeelementguide; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.codeelementguide OWNER TO changeme;

--
-- TOC entry 317 (class 1259 OID 21139)
-- Name: codeelementinjectedvalue_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.codeelementinjectedvalue_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codeelementinjectedvalue_seq OWNER TO changeme;

--
-- TOC entry 318 (class 1259 OID 21141)
-- Name: codeelementinjectedvalue; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.codeelementinjectedvalue OWNER TO changeme;

--
-- TOC entry 319 (class 1259 OID 21149)
-- Name: codeset_codesetid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.codeset_codesetid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codeset_codesetid_seq OWNER TO changeme;

--
-- TOC entry 320 (class 1259 OID 21151)
-- Name: codeset; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.codeset (
    codesetid integer DEFAULT nextval('public.codeset_codesetid_seq'::regclass) NOT NULL,
    name text,
    description text,
    municipality_municode integer,
    active boolean DEFAULT true
);


ALTER TABLE public.codeset OWNER TO changeme;

--
-- TOC entry 321 (class 1259 OID 21159)
-- Name: codesetelement_elementid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.codesetelement_elementid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codesetelement_elementid_seq OWNER TO changeme;

--
-- TOC entry 322 (class 1259 OID 21161)
-- Name: codesetelement; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.codesetelement OWNER TO changeme;

--
-- TOC entry 323 (class 1259 OID 21168)
-- Name: codesetelementclass_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.codesetelementclass_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codesetelementclass_seq OWNER TO changeme;

--
-- TOC entry 324 (class 1259 OID 21170)
-- Name: codesource_sourceid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.codesource_sourceid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codesource_sourceid_seq OWNER TO changeme;

--
-- TOC entry 325 (class 1259 OID 21172)
-- Name: codesource; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.codesource OWNER TO changeme;

--
-- TOC entry 326 (class 1259 OID 21180)
-- Name: codeviolation_violationid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.codeviolation_violationid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codeviolation_violationid_seq OWNER TO changeme;

--
-- TOC entry 327 (class 1259 OID 21182)
-- Name: codeviolation; Type: TABLE; Schema: public; Owner: changeme
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
    bobsource_sourceid integer
);


ALTER TABLE public.codeviolation OWNER TO changeme;

--
-- TOC entry 6196 (class 0 OID 0)
-- Dependencies: 327
-- Name: TABLE codeviolation; Type: COMMENT; Schema: public; Owner: changeme
--

COMMENT ON TABLE public.codeviolation IS 'save commets';


--
-- TOC entry 328 (class 1259 OID 21191)
-- Name: codeviolationpdfdoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.codeviolationpdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    codeviolation_violationid integer NOT NULL
);


ALTER TABLE public.codeviolationpdfdoc OWNER TO changeme;

--
-- TOC entry 329 (class 1259 OID 21194)
-- Name: codeviolationphotodoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.codeviolationphotodoc (
    photodoc_photodocid integer NOT NULL,
    codeviolation_violationid integer NOT NULL
);


ALTER TABLE public.codeviolationphotodoc OWNER TO changeme;

--
-- TOC entry 330 (class 1259 OID 21197)
-- Name: codeviolationseverityclass_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.codeviolationseverityclass_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codeviolationseverityclass_seq OWNER TO changeme;

--
-- TOC entry 331 (class 1259 OID 21199)
-- Name: coglog_logeentryid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.coglog_logeentryid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.coglog_logeentryid_seq OWNER TO changeme;

--
-- TOC entry 332 (class 1259 OID 21201)
-- Name: contactemail_emailid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.contactemail_emailid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.contactemail_emailid_seq OWNER TO changeme;

--
-- TOC entry 333 (class 1259 OID 21203)
-- Name: contactemail; Type: TABLE; Schema: public; Owner: changeme
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
    notes text
);


ALTER TABLE public.contactemail OWNER TO changeme;

--
-- TOC entry 334 (class 1259 OID 21210)
-- Name: contactphone_phoneid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.contactphone_phoneid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.contactphone_phoneid_seq OWNER TO changeme;

--
-- TOC entry 335 (class 1259 OID 21212)
-- Name: contactphone; Type: TABLE; Schema: public; Owner: changeme
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
    notes text
);


ALTER TABLE public.contactphone OWNER TO changeme;

--
-- TOC entry 336 (class 1259 OID 21219)
-- Name: contactphonetype_typeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.contactphonetype_typeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.contactphonetype_typeid_seq OWNER TO changeme;

--
-- TOC entry 337 (class 1259 OID 21221)
-- Name: contactphonetype; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.contactphonetype (
    phonetypeid integer DEFAULT nextval('public.contactphonetype_typeid_seq'::regclass) NOT NULL,
    title text,
    createdts timestamp with time zone,
    deactivatedts timestamp with time zone
);


ALTER TABLE public.contactphonetype OWNER TO changeme;

--
-- TOC entry 338 (class 1259 OID 21228)
-- Name: courtentity_entityid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.courtentity_entityid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.courtentity_entityid_seq OWNER TO changeme;

--
-- TOC entry 339 (class 1259 OID 21230)
-- Name: courtentity; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.courtentity OWNER TO changeme;

--
-- TOC entry 340 (class 1259 OID 21237)
-- Name: dbpatch; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.dbpatch (
    patchnum integer NOT NULL,
    patchfilename text,
    datepublished timestamp without time zone,
    patchauthor text,
    notes text
);


ALTER TABLE public.dbpatch OWNER TO changeme;

--
-- TOC entry 341 (class 1259 OID 21243)
-- Name: event; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.event (
    eventid integer DEFAULT nextval('public.ceevent_eventid_seq'::regclass) NOT NULL,
    category_catid integer NOT NULL,
    cecase_caseid integer,
    creationts timestamp with time zone,
    eventdescription text,
    creator_userid integer NOT NULL,
    active boolean DEFAULT true,
    notes text,
    occperiod_periodid integer,
    timestart timestamp with time zone,
    timeend timestamp with time zone,
    lastupdatedby_userid integer,
    lastupdatedts timestamp with time zone
);


ALTER TABLE public.event OWNER TO changeme;

--
-- TOC entry 342 (class 1259 OID 21251)
-- Name: eventcategory; Type: TABLE; Schema: public; Owner: changeme
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
    userrankminimumtoupdate integer DEFAULT 3
);


ALTER TABLE public.eventcategory OWNER TO changeme;

--
-- TOC entry 343 (class 1259 OID 21266)
-- Name: eventhuman_linkid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.eventhuman_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.eventhuman_linkid_seq OWNER TO changeme;

--
-- TOC entry 344 (class 1259 OID 21268)
-- Name: eventhuman; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.eventhuman OWNER TO changeme;

--
-- TOC entry 345 (class 1259 OID 21275)
-- Name: eventrule; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.eventrule OWNER TO changeme;

--
-- TOC entry 346 (class 1259 OID 21294)
-- Name: eventruleimpl_impid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.eventruleimpl_impid_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.eventruleimpl_impid_seq OWNER TO changeme;

--
-- TOC entry 347 (class 1259 OID 21296)
-- Name: eventruleimpl; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.eventruleimpl OWNER TO changeme;

--
-- TOC entry 348 (class 1259 OID 21303)
-- Name: eventruleruleset; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.eventruleruleset (
    ruleset_rulesetid integer NOT NULL,
    eventrule_ruleid integer NOT NULL
);


ALTER TABLE public.eventruleruleset OWNER TO changeme;

--
-- TOC entry 349 (class 1259 OID 21306)
-- Name: eventrulesetid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.eventrulesetid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.eventrulesetid_seq OWNER TO changeme;

--
-- TOC entry 350 (class 1259 OID 21308)
-- Name: eventruleset; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.eventruleset (
    rulesetid integer DEFAULT nextval('public.eventrulesetid_seq'::regclass) NOT NULL,
    title text,
    description text
);


ALTER TABLE public.eventruleset OWNER TO changeme;

--
-- TOC entry 351 (class 1259 OID 21315)
-- Name: human_humanid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.human_humanid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.human_humanid_seq OWNER TO changeme;

--
-- TOC entry 352 (class 1259 OID 21317)
-- Name: human; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.human OWNER TO changeme;

--
-- TOC entry 353 (class 1259 OID 21326)
-- Name: humancecase_linkid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.humancecase_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humancecase_linkid_seq OWNER TO changeme;

--
-- TOC entry 354 (class 1259 OID 21328)
-- Name: humancecase; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.humancecase OWNER TO changeme;

--
-- TOC entry 355 (class 1259 OID 21335)
-- Name: humanmailing_linkid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.humanmailing_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humanmailing_linkid_seq OWNER TO changeme;

--
-- TOC entry 356 (class 1259 OID 21337)
-- Name: humanmailing_roleid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.humanmailing_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humanmailing_roleid_seq OWNER TO changeme;

--
-- TOC entry 357 (class 1259 OID 21339)
-- Name: humanmailingaddress; Type: TABLE; Schema: public; Owner: changeme
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
    linkedobjectrole_lorid integer
);


ALTER TABLE public.humanmailingaddress OWNER TO changeme;

--
-- TOC entry 358 (class 1259 OID 21346)
-- Name: humanmuni_linkid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.humanmuni_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humanmuni_linkid_seq OWNER TO changeme;

--
-- TOC entry 359 (class 1259 OID 21348)
-- Name: humanmuni; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.humanmuni OWNER TO changeme;

--
-- TOC entry 360 (class 1259 OID 21355)
-- Name: humanoccperiod_linkid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.humanoccperiod_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humanoccperiod_linkid_seq OWNER TO changeme;

--
-- TOC entry 361 (class 1259 OID 21357)
-- Name: humanoccperiod; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.humanoccperiod OWNER TO changeme;

--
-- TOC entry 362 (class 1259 OID 21364)
-- Name: humanparcel_linkid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.humanparcel_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humanparcel_linkid_seq OWNER TO changeme;

--
-- TOC entry 363 (class 1259 OID 21366)
-- Name: humanparcel; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.humanparcel OWNER TO changeme;

--
-- TOC entry 364 (class 1259 OID 21373)
-- Name: humanparcelrole_roleid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.humanparcelrole_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humanparcelrole_roleid_seq OWNER TO changeme;

--
-- TOC entry 365 (class 1259 OID 21375)
-- Name: parcelunithuman_linkid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.parcelunithuman_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelunithuman_linkid_seq OWNER TO changeme;

--
-- TOC entry 366 (class 1259 OID 21377)
-- Name: humanparcelunit; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.humanparcelunit OWNER TO changeme;

--
-- TOC entry 367 (class 1259 OID 21384)
-- Name: iconid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.iconid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.iconid_seq OWNER TO changeme;

--
-- TOC entry 368 (class 1259 OID 21386)
-- Name: icon; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.icon (
    iconid integer DEFAULT nextval('public.iconid_seq'::regclass) NOT NULL,
    name text,
    styleclass text,
    fontawesome text,
    materialicons text,
    active boolean DEFAULT true
);


ALTER TABLE public.icon OWNER TO changeme;

--
-- TOC entry 369 (class 1259 OID 21394)
-- Name: improvementid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.improvementid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.improvementid_seq OWNER TO changeme;

--
-- TOC entry 370 (class 1259 OID 21396)
-- Name: improvementstatus; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.improvementstatus (
    statusid integer NOT NULL,
    statustitle text,
    statusdescription text,
    icon_iconid integer
);


ALTER TABLE public.improvementstatus OWNER TO changeme;

--
-- TOC entry 371 (class 1259 OID 21402)
-- Name: improvementsuggestion; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.improvementsuggestion OWNER TO changeme;

--
-- TOC entry 372 (class 1259 OID 21409)
-- Name: improvementtype; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.improvementtype (
    typeid integer NOT NULL,
    typetitle text,
    typedescription text
);


ALTER TABLE public.improvementtype OWNER TO changeme;

--
-- TOC entry 373 (class 1259 OID 21415)
-- Name: inspectedspacetypeelement_inspectedstelid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.inspectedspacetypeelement_inspectedstelid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.inspectedspacetypeelement_inspectedstelid_seq OWNER TO changeme;

--
-- TOC entry 374 (class 1259 OID 21417)
-- Name: intensityclass; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.intensityclass OWNER TO changeme;

--
-- TOC entry 375 (class 1259 OID 21425)
-- Name: linkedobjectrole_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.linkedobjectrole_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.linkedobjectrole_seq OWNER TO changeme;

--
-- TOC entry 376 (class 1259 OID 21427)
-- Name: linkedobjectrole; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.linkedobjectrole (
    lorid integer DEFAULT nextval('public.linkedobjectrole_seq'::regclass) NOT NULL,
    lorschema_schemaid public.linkedobjectroleschema NOT NULL,
    title text NOT NULL,
    description text,
    createdts timestamp with time zone DEFAULT now() NOT NULL,
    deactivatedts timestamp with time zone,
    notes text
);


ALTER TABLE public.linkedobjectrole OWNER TO changeme;

--
-- TOC entry 377 (class 1259 OID 21435)
-- Name: linkedobjectroleschema_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.linkedobjectroleschema_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.linkedobjectroleschema_seq OWNER TO changeme;

--
-- TOC entry 378 (class 1259 OID 21437)
-- Name: listitemchange_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.listitemchange_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.listitemchange_seq OWNER TO changeme;

--
-- TOC entry 379 (class 1259 OID 21439)
-- Name: listchangerequest; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.listchangerequest (
    changeid integer DEFAULT nextval('public.listitemchange_seq'::regclass) NOT NULL,
    changetext text
);


ALTER TABLE public.listchangerequest OWNER TO changeme;

--
-- TOC entry 380 (class 1259 OID 21446)
-- Name: locationdescription_id_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.locationdescription_id_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.locationdescription_id_seq OWNER TO changeme;

--
-- TOC entry 381 (class 1259 OID 21448)
-- Name: log; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.log OWNER TO changeme;

--
-- TOC entry 382 (class 1259 OID 21457)
-- Name: logcategory; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.logcategory (
    catid integer NOT NULL,
    title text,
    description text
);


ALTER TABLE public.logcategory OWNER TO changeme;

--
-- TOC entry 383 (class 1259 OID 21463)
-- Name: login_userid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.login_userid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.login_userid_seq OWNER TO changeme;

--
-- TOC entry 384 (class 1259 OID 21465)
-- Name: login; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.login (
    userid integer DEFAULT nextval('public.login_userid_seq'::regclass) NOT NULL,
    username text NOT NULL,
    password text,
    notes text,
    personlink integer,
    pswdlastupdated timestamp with time zone,
    forcepasswordreset timestamp with time zone,
    createdby integer,
    createdts timestamp with time zone,
    nologinvirtualonly boolean DEFAULT false,
    deactivatedts timestamp with time zone,
    deactivated_userid integer,
    lastupdatedts timestamp with time zone DEFAULT now(),
    userrole public.role,
    homemuni integer
);


ALTER TABLE public.login OWNER TO changeme;

--
-- TOC entry 385 (class 1259 OID 21474)
-- Name: logincredentialexercise_ex_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.logincredentialexercise_ex_seq
    START WITH 2777
    INCREMENT BY 7
    MINVALUE 2777
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.logincredentialexercise_ex_seq OWNER TO changeme;

--
-- TOC entry 386 (class 1259 OID 21476)
-- Name: munilogin_recordid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.munilogin_recordid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.munilogin_recordid_seq OWNER TO changeme;

--
-- TOC entry 387 (class 1259 OID 21478)
-- Name: loginmuniauthperiod; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.loginmuniauthperiod (
    muniauthperiodid integer DEFAULT nextval('public.munilogin_recordid_seq'::regclass) NOT NULL,
    muni_municode integer NOT NULL,
    authuser_userid integer NOT NULL,
    accessgranteddatestart timestamp with time zone DEFAULT '1970-01-01 05:00:00+00'::timestamp with time zone NOT NULL,
    accessgranteddatestop timestamp with time zone DEFAULT '1970-01-01 05:00:00+00'::timestamp with time zone NOT NULL,
    recorddeactivatedts timestamp with time zone,
    authorizedrole public.role,
    createdts timestamp with time zone,
    createdby_userid integer NOT NULL,
    notes text,
    supportassignedby integer,
    assignmentrank integer DEFAULT 1
);


ALTER TABLE public.loginmuniauthperiod OWNER TO changeme;

--
-- TOC entry 388 (class 1259 OID 21488)
-- Name: loginmuniauthperiodsession_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.loginmuniauthperiodsession_seq
    START WITH 2777
    INCREMENT BY 7
    MINVALUE 2777
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.loginmuniauthperiodsession_seq OWNER TO changeme;

--
-- TOC entry 389 (class 1259 OID 21490)
-- Name: loginmuniauthperiodlog; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.loginmuniauthperiodlog OWNER TO changeme;

--
-- TOC entry 390 (class 1259 OID 21497)
-- Name: loginmuniauthperiodlog_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.loginmuniauthperiodlog_seq
    START WITH 2777
    INCREMENT BY 7
    MINVALUE 2777
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.loginmuniauthperiodlog_seq OWNER TO changeme;

--
-- TOC entry 391 (class 1259 OID 21499)
-- Name: loginobjecthistory_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.loginobjecthistory_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.loginobjecthistory_seq OWNER TO changeme;

--
-- TOC entry 392 (class 1259 OID 21501)
-- Name: loginobjecthistory; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.loginobjecthistory OWNER TO changeme;

--
-- TOC entry 393 (class 1259 OID 21506)
-- Name: mailingaddress_addressid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.mailingaddress_addressid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mailingaddress_addressid_seq OWNER TO changeme;

--
-- TOC entry 394 (class 1259 OID 21508)
-- Name: mailingaddress; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.mailingaddress (
    addressid integer DEFAULT nextval('public.mailingaddress_addressid_seq'::regclass) NOT NULL,
    bldgno text NOT NULL,
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
    notes text
);


ALTER TABLE public.mailingaddress OWNER TO changeme;

--
-- TOC entry 395 (class 1259 OID 21515)
-- Name: mailingcitystatezip; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.mailingcitystatezip (
    id integer NOT NULL,
    zip_code character(5),
    sid integer,
    state_abbr character(2),
    city character varying(30),
    list_type_id integer,
    list_type character varying(10),
    default_state character(2),
    default_city character varying(30),
    default_type character varying(10)
);


ALTER TABLE public.mailingcitystatezip OWNER TO changeme;

--
-- TOC entry 396 (class 1259 OID 21518)
-- Name: mailingstreet_streetid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.mailingstreet_streetid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mailingstreet_streetid_seq OWNER TO changeme;

--
-- TOC entry 397 (class 1259 OID 21520)
-- Name: mailingstreet; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.mailingstreet (
    streetid integer DEFAULT nextval('public.mailingstreet_streetid_seq'::regclass) NOT NULL,
    name text NOT NULL,
    namevariantsarr text[],
    citystatezip_cszipid integer NOT NULL,
    notes text,
    pobox boolean DEFAULT false,
    createdts timestamp with time zone DEFAULT now()
);


ALTER TABLE public.mailingstreet OWNER TO changeme;

--
-- TOC entry 398 (class 1259 OID 21529)
-- Name: moneycecasefeeassignedid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.moneycecasefeeassignedid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.moneycecasefeeassignedid_seq OWNER TO changeme;

--
-- TOC entry 399 (class 1259 OID 21531)
-- Name: moneycecasefeeassigned; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.moneycecasefeeassigned (
    cecaseassignedfeeid integer DEFAULT nextval('public.moneycecasefeeassignedid_seq'::regclass) NOT NULL,
    moneyfeeassigned_assignedid integer NOT NULL,
    cecase_caseid integer NOT NULL,
    assignedby_userid integer,
    assignedbyts timestamp with time zone,
    waivedby_userid integer,
    lastmodifiedts timestamp with time zone,
    reduceby money,
    reduceby_userid integer,
    notes text,
    fee_feeid integer NOT NULL,
    codesetelement_elementid integer NOT NULL
);


ALTER TABLE public.moneycecasefeeassigned OWNER TO changeme;

--
-- TOC entry 400 (class 1259 OID 21538)
-- Name: moneycecasefeepayment; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.moneycecasefeepayment (
    payment_paymentid integer NOT NULL,
    cecaseassignedfee_id integer NOT NULL
);


ALTER TABLE public.moneycecasefeepayment OWNER TO changeme;

--
-- TOC entry 401 (class 1259 OID 21541)
-- Name: moneycodesetelementfee; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.moneycodesetelementfee (
    fee_feeid integer NOT NULL,
    codesetelement_elementid integer NOT NULL,
    active boolean,
    autoassign boolean
);


ALTER TABLE public.moneycodesetelementfee OWNER TO changeme;

--
-- TOC entry 402 (class 1259 OID 21544)
-- Name: moneycodesetelementfeeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.moneycodesetelementfeeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.moneycodesetelementfeeid_seq OWNER TO changeme;

--
-- TOC entry 403 (class 1259 OID 21546)
-- Name: occinspectionfee_feeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occinspectionfee_feeid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occinspectionfee_feeid_seq OWNER TO changeme;

--
-- TOC entry 404 (class 1259 OID 21548)
-- Name: moneyfee; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.moneyfee (
    feeid integer DEFAULT nextval('public.occinspectionfee_feeid_seq'::regclass) NOT NULL,
    muni_municode integer NOT NULL,
    feename text NOT NULL,
    feeamount money NOT NULL,
    effectivedate timestamp with time zone NOT NULL,
    expirydate timestamp with time zone NOT NULL,
    notes text
);


ALTER TABLE public.moneyfee OWNER TO changeme;

--
-- TOC entry 405 (class 1259 OID 21555)
-- Name: moneyfeeassignedid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.moneyfeeassignedid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.moneyfeeassignedid_seq OWNER TO changeme;

--
-- TOC entry 406 (class 1259 OID 21557)
-- Name: moneyoccperiodfeeassignedid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.moneyoccperiodfeeassignedid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.moneyoccperiodfeeassignedid_seq OWNER TO changeme;

--
-- TOC entry 407 (class 1259 OID 21559)
-- Name: moneyoccperiodfeeassigned; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.moneyoccperiodfeeassigned (
    moneyoccperassignedfeeid integer DEFAULT nextval('public.moneyoccperiodfeeassignedid_seq'::regclass) NOT NULL,
    moneyfeeassigned_assignedid integer NOT NULL,
    occperiod_periodid integer NOT NULL,
    assignedby_userid integer,
    assignedbyts timestamp with time zone,
    waivedby_userid integer,
    lastmodifiedts timestamp with time zone,
    reduceby money,
    reduceby_userid integer,
    notes text,
    fee_feeid integer NOT NULL,
    occperiodtype_typeid integer NOT NULL
);


ALTER TABLE public.moneyoccperiodfeeassigned OWNER TO changeme;

--
-- TOC entry 408 (class 1259 OID 21566)
-- Name: moneyoccperiodfeepayment; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.moneyoccperiodfeepayment (
    payment_paymentid integer NOT NULL,
    occperiodassignedfee_id integer NOT NULL
);


ALTER TABLE public.moneyoccperiodfeepayment OWNER TO changeme;

--
-- TOC entry 409 (class 1259 OID 21569)
-- Name: moneyoccperiodtypefee; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.moneyoccperiodtypefee (
    fee_feeid integer NOT NULL,
    occperiodtype_typeid integer NOT NULL,
    active boolean,
    autoassign boolean
);


ALTER TABLE public.moneyoccperiodtypefee OWNER TO changeme;

--
-- TOC entry 410 (class 1259 OID 21572)
-- Name: moneyoccperiodtypefeeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.moneyoccperiodtypefeeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.moneyoccperiodtypefeeid_seq OWNER TO changeme;

--
-- TOC entry 411 (class 1259 OID 21574)
-- Name: payment_paymentid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.payment_paymentid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.payment_paymentid_seq OWNER TO changeme;

--
-- TOC entry 412 (class 1259 OID 21576)
-- Name: moneypayment; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.moneypayment (
    paymentid integer DEFAULT nextval('public.payment_paymentid_seq'::regclass) NOT NULL,
    paymenttype_typeid integer NOT NULL,
    datereceived timestamp with time zone NOT NULL,
    datedeposited timestamp with time zone NOT NULL,
    amount money NOT NULL,
    payer_personid integer NOT NULL,
    referencenum text,
    checkno integer,
    cleared boolean DEFAULT false,
    notes text,
    recordedby_userid integer,
    entrytimestamp timestamp with time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.moneypayment OWNER TO changeme;

--
-- TOC entry 413 (class 1259 OID 21585)
-- Name: paymenttype_typeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.paymenttype_typeid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.paymenttype_typeid_seq OWNER TO changeme;

--
-- TOC entry 414 (class 1259 OID 21587)
-- Name: moneypaymenttype; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.moneypaymenttype (
    typeid integer DEFAULT nextval('public.paymenttype_typeid_seq'::regclass) NOT NULL,
    pmttypetitle text NOT NULL
);


ALTER TABLE public.moneypaymenttype OWNER TO changeme;

--
-- TOC entry 415 (class 1259 OID 21594)
-- Name: muni_muniprofile_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.muni_muniprofile_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.muni_muniprofile_seq OWNER TO changeme;

--
-- TOC entry 416 (class 1259 OID 21596)
-- Name: municipality; Type: TABLE; Schema: public; Owner: changeme
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
    office_propertyid integer,
    notes text,
    lastupdatedts timestamp with time zone,
    lastupdated_userid integer,
    primarystaffcontact_userid integer,
    defaultoccperiod integer
);


ALTER TABLE public.municipality OWNER TO changeme;

--
-- TOC entry 417 (class 1259 OID 21609)
-- Name: municitystatezip; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.municitystatezip (
    muni_municode integer NOT NULL,
    citystatezip_id integer NOT NULL
);


ALTER TABLE public.municitystatezip OWNER TO changeme;

--
-- TOC entry 418 (class 1259 OID 21612)
-- Name: municourtentity; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.municourtentity (
    muni_municode integer NOT NULL,
    courtentity_entityid integer NOT NULL,
    relativeorder integer
);


ALTER TABLE public.municourtentity OWNER TO changeme;

--
-- TOC entry 419 (class 1259 OID 21615)
-- Name: munilogin; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.munilogin (
    muni_municode integer NOT NULL,
    userid integer NOT NULL,
    defaultmuni boolean DEFAULT false,
    accessgranteddatestart timestamp with time zone DEFAULT '1970-01-01 05:00:00+00'::timestamp with time zone NOT NULL,
    accessgranteddatestop timestamp with time zone DEFAULT '1970-01-01 05:00:00+00'::timestamp with time zone NOT NULL,
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


ALTER TABLE public.munilogin OWNER TO changeme;

--
-- TOC entry 420 (class 1259 OID 21626)
-- Name: munipdfdoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.munipdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    muni_municode integer NOT NULL
);


ALTER TABLE public.munipdfdoc OWNER TO changeme;

--
-- TOC entry 421 (class 1259 OID 21629)
-- Name: muniphotodoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.muniphotodoc (
    photodoc_photodocid integer NOT NULL,
    muni_municode integer NOT NULL
);


ALTER TABLE public.muniphotodoc OWNER TO changeme;

--
-- TOC entry 422 (class 1259 OID 21632)
-- Name: muniprofile; Type: TABLE; Schema: public; Owner: changeme
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
    novfollowupdefaultdays integer DEFAULT 20
);


ALTER TABLE public.muniprofile OWNER TO changeme;

--
-- TOC entry 423 (class 1259 OID 21643)
-- Name: muniprofileeventruleset; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.muniprofileeventruleset (
    muniprofile_profileid integer NOT NULL,
    ruleset_setid integer NOT NULL,
    cedefault boolean DEFAULT true
);


ALTER TABLE public.muniprofileeventruleset OWNER TO changeme;

--
-- TOC entry 424 (class 1259 OID 21647)
-- Name: muniprofileoccperiodtype; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.muniprofileoccperiodtype (
    muniprofile_profileid integer NOT NULL,
    occperiodtype_typeid integer NOT NULL
);


ALTER TABLE public.muniprofileoccperiodtype OWNER TO changeme;

--
-- TOC entry 425 (class 1259 OID 21650)
-- Name: noticeofviolation_noticeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.noticeofviolation_noticeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.noticeofviolation_noticeid_seq OWNER TO changeme;

--
-- TOC entry 426 (class 1259 OID 21652)
-- Name: noticeofviolation; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.noticeofviolation (
    noticeid integer DEFAULT nextval('public.noticeofviolation_noticeid_seq'::regclass) NOT NULL,
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
    printstyle_styleid integer,
    active boolean DEFAULT true,
    followupevent_eventid integer,
    notifyingofficer_userid integer,
    recipient_humanid integer,
    recipient_mailing integer,
    fixedrecipientxferts timestamp with time zone DEFAULT now(),
    fixedrecipientname text,
    fixedrecipientbldgno text,
    fixedrecipientstreet text,
    fixedrecipientcity text,
    fixedrecipientstate text,
    fixedrecipientzip text
);


ALTER TABLE public.noticeofviolation OWNER TO changeme;

--
-- TOC entry 427 (class 1259 OID 21661)
-- Name: noticeofviolationcodeviolation; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.noticeofviolationcodeviolation (
    noticeofviolation_noticeid integer NOT NULL,
    codeviolation_violationid integer NOT NULL,
    includeordtext boolean DEFAULT true,
    includehumanfriendlyordtext boolean DEFAULT false,
    includeviolationphoto boolean DEFAULT false
);


ALTER TABLE public.noticeofviolationcodeviolation OWNER TO changeme;

--
-- TOC entry 428 (class 1259 OID 21667)
-- Name: occchecklist; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.occchecklist OWNER TO changeme;

--
-- TOC entry 429 (class 1259 OID 21675)
-- Name: occchecklist_photorequirement_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occchecklist_photorequirement_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occchecklist_photorequirement_seq OWNER TO changeme;

--
-- TOC entry 430 (class 1259 OID 21677)
-- Name: occchecklistphotorequirement; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occchecklistphotorequirement (
    occchecklist_checklistid integer NOT NULL,
    occphotorequirement_reqid integer NOT NULL
);


ALTER TABLE public.occchecklistphotorequirement OWNER TO changeme;

--
-- TOC entry 431 (class 1259 OID 21680)
-- Name: occchecklistspacetype; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occchecklistspacetype (
    checklistspacetypeid integer DEFAULT nextval('public.chkliststiceid_seq'::regclass) NOT NULL,
    checklist_id integer NOT NULL,
    required boolean,
    spacetype_typeid integer NOT NULL,
    notes text
);


ALTER TABLE public.occchecklistspacetype OWNER TO changeme;

--
-- TOC entry 432 (class 1259 OID 21687)
-- Name: spaceelement_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.spaceelement_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.spaceelement_seq OWNER TO changeme;

--
-- TOC entry 433 (class 1259 OID 21689)
-- Name: occchecklistspacetypeelement; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occchecklistspacetypeelement (
    spaceelementid integer DEFAULT nextval('public.spaceelement_seq'::regclass) NOT NULL,
    codeelement_id integer,
    required boolean DEFAULT true,
    checklistspacetype_typeid integer
);


ALTER TABLE public.occchecklistspacetypeelement OWNER TO changeme;

--
-- TOC entry 434 (class 1259 OID 21694)
-- Name: occevent_eventid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occevent_eventid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occevent_eventid_seq OWNER TO changeme;

--
-- TOC entry 435 (class 1259 OID 21696)
-- Name: occeventproposalimplementation_id_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occeventproposalimplementation_id_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occeventproposalimplementation_id_seq OWNER TO changeme;

--
-- TOC entry 436 (class 1259 OID 21698)
-- Name: occinspectedspace_pk_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occinspectedspace_pk_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occinspectedspace_pk_seq OWNER TO changeme;

--
-- TOC entry 437 (class 1259 OID 21700)
-- Name: occinspectedspace; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occinspectedspace (
    inspectedspaceid integer DEFAULT nextval('public.occinspectedspace_pk_seq'::regclass) NOT NULL,
    occinspection_inspectionid integer NOT NULL,
    occlocationdescription_descid integer NOT NULL,
    addedtochecklistby_userid integer NOT NULL,
    addedtochecklistts timestamp with time zone,
    occchecklistspacetype_chklstspctypid integer
);


ALTER TABLE public.occinspectedspace OWNER TO changeme;

--
-- TOC entry 438 (class 1259 OID 21704)
-- Name: occinspectedspaceelement; Type: TABLE; Schema: public; Owner: changeme
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
    migratetocecaseonfail boolean DEFAULT true
);


ALTER TABLE public.occinspectedspaceelement OWNER TO changeme;

--
-- TOC entry 439 (class 1259 OID 21712)
-- Name: occinspectedspaceelementphotodoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occinspectedspaceelementphotodoc (
    photodoc_photodocid integer NOT NULL,
    inspectedspaceelement_elementid integer NOT NULL,
    phototype public.occinspectionphototype
);


ALTER TABLE public.occinspectedspaceelementphotodoc OWNER TO changeme;

--
-- TOC entry 440 (class 1259 OID 21715)
-- Name: occupancyinspectionid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occupancyinspectionid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occupancyinspectionid_seq OWNER TO changeme;

--
-- TOC entry 441 (class 1259 OID 21717)
-- Name: occinspection; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occinspection (
    inspectionid integer DEFAULT nextval('public.occupancyinspectionid_seq'::regclass) NOT NULL,
    occperiod_periodid integer NOT NULL,
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
    cause_causeid integer
);


ALTER TABLE public.occinspection OWNER TO changeme;

--
-- TOC entry 442 (class 1259 OID 21725)
-- Name: occinspection_determination_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occinspection_determination_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occinspection_determination_seq OWNER TO changeme;

--
-- TOC entry 443 (class 1259 OID 21727)
-- Name: occinspectioncause_causeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occinspectioncause_causeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occinspectioncause_causeid_seq OWNER TO changeme;

--
-- TOC entry 444 (class 1259 OID 21729)
-- Name: occinspectioncause; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occinspectioncause (
    causeid integer DEFAULT nextval('public.occinspectioncause_causeid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    notes text,
    active boolean
);


ALTER TABLE public.occinspectioncause OWNER TO changeme;

--
-- TOC entry 445 (class 1259 OID 21736)
-- Name: occinspectiondetermination; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occinspectiondetermination (
    determinationid integer DEFAULT nextval('public.occinspection_determination_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    notes text,
    eventcat_catid integer,
    active boolean
);


ALTER TABLE public.occinspectiondetermination OWNER TO changeme;

--
-- TOC entry 446 (class 1259 OID 21743)
-- Name: occinspectionphotodoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occinspectionphotodoc (
    photodoc_photodocid integer NOT NULL,
    inspection_inspectionid integer NOT NULL,
    photorequirement_requirementid integer
);


ALTER TABLE public.occinspectionphotodoc OWNER TO changeme;

--
-- TOC entry 447 (class 1259 OID 21746)
-- Name: occinspectionpropertystatus; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occinspectionpropertystatus (
    occinspection_inspectionid integer NOT NULL,
    propertystatus_statusid integer NOT NULL,
    notes text
);


ALTER TABLE public.occinspectionpropertystatus OWNER TO changeme;

--
-- TOC entry 448 (class 1259 OID 21752)
-- Name: occlocationdescriptor; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occlocationdescriptor (
    locationdescriptionid integer DEFAULT nextval('public.locationdescription_id_seq'::regclass) NOT NULL,
    description text,
    buildingfloorno integer
);


ALTER TABLE public.occlocationdescriptor OWNER TO changeme;

--
-- TOC entry 449 (class 1259 OID 21759)
-- Name: occperiodid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occperiodid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occperiodid_seq OWNER TO changeme;

--
-- TOC entry 450 (class 1259 OID 21761)
-- Name: occperiod; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occperiod (
    periodid integer DEFAULT nextval('public.occperiodid_seq'::regclass) NOT NULL,
    source_sourceid integer NOT NULL,
    propertyunit_unitid integer,
    createdts timestamp with time zone,
    type_typeid integer NOT NULL,
    typecertifiedby_userid integer,
    typecertifiedts timestamp with time zone,
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
    active boolean DEFAULT true,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer
);


ALTER TABLE public.occperiod OWNER TO changeme;

--
-- TOC entry 451 (class 1259 OID 21770)
-- Name: occperiodeventrule; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.occperiodeventrule OWNER TO changeme;

--
-- TOC entry 452 (class 1259 OID 21774)
-- Name: occperiodeventrule_operid_pk_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occperiodeventrule_operid_pk_seq
    START WITH 100
    INCREMENT BY 10
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occperiodeventrule_operid_pk_seq OWNER TO changeme;

--
-- TOC entry 453 (class 1259 OID 21776)
-- Name: occperiodpdfdoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occperiodpdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    occperiod_periodid integer NOT NULL
);


ALTER TABLE public.occperiodpdfdoc OWNER TO changeme;

--
-- TOC entry 454 (class 1259 OID 21779)
-- Name: occperiodpermitapplication; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occperiodpermitapplication (
    occperiod_periodid integer NOT NULL,
    occpermitapp_applicationid integer NOT NULL
);


ALTER TABLE public.occperiodpermitapplication OWNER TO changeme;

--
-- TOC entry 455 (class 1259 OID 21782)
-- Name: occperiodphotodoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occperiodphotodoc (
    photodoc_photodocid integer NOT NULL,
    occperiod_periodid integer NOT NULL
);


ALTER TABLE public.occperiodphotodoc OWNER TO changeme;

--
-- TOC entry 456 (class 1259 OID 21785)
-- Name: occperiodtypeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occperiodtypeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occperiodtypeid_seq OWNER TO changeme;

--
-- TOC entry 457 (class 1259 OID 21787)
-- Name: occperiodtype; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occperiodtype (
    typeid integer DEFAULT nextval('public.occperiodtypeid_seq'::regclass) NOT NULL,
    muni_municode integer NOT NULL,
    title text NOT NULL,
    authorizeduses text,
    description text,
    userassignable boolean DEFAULT true,
    permittable boolean DEFAULT true,
    startdaterequired boolean DEFAULT true,
    enddaterequired boolean DEFAULT true,
    passedinspectionrequired boolean DEFAULT true,
    rentalcompatible boolean DEFAULT true,
    active boolean DEFAULT true,
    allowthirdpartyinspection boolean,
    optionalpersontypes public.persontype[],
    requiredpersontypes public.persontype[],
    commercial boolean DEFAULT false,
    requirepersontypeentrycheck boolean DEFAULT false,
    defaultpermitvalidityperioddays integer,
    occchecklist_checklistlistid integer,
    asynchronousinspectionvalidityperiod boolean DEFAULT false,
    defaultinspectionvalidityperiod integer,
    eventruleset_setid integer,
    inspectable boolean DEFAULT true,
    permittitle text,
    permittitlesub text
);


ALTER TABLE public.occperiodtype OWNER TO changeme;

--
-- TOC entry 458 (class 1259 OID 21805)
-- Name: occupancypermit_permitid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occupancypermit_permitid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occupancypermit_permitid_seq OWNER TO changeme;

--
-- TOC entry 459 (class 1259 OID 21807)
-- Name: occpermit; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occpermit (
    permitid integer DEFAULT nextval('public.occupancypermit_permitid_seq'::regclass) NOT NULL,
    occperiod_periodid integer NOT NULL,
    referenceno text,
    issuedto_personid integer NOT NULL,
    issuedby_userid integer,
    dateissued timestamp with time zone NOT NULL,
    permitadditionaltext text,
    notes text
);


ALTER TABLE public.occpermit OWNER TO changeme;

--
-- TOC entry 460 (class 1259 OID 21814)
-- Name: occpermitapp_appid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occpermitapp_appid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occpermitapp_appid_seq OWNER TO changeme;

--
-- TOC entry 461 (class 1259 OID 21816)
-- Name: occpermitapplication; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.occpermitapplication OWNER TO changeme;

--
-- TOC entry 462 (class 1259 OID 21823)
-- Name: occpermitapplicationhuman; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occpermitapplicationhuman (
    occpermitapplication_applicationid integer NOT NULL,
    human_humanid integer NOT NULL,
    applicant boolean,
    preferredcontact boolean,
    applicationpersontype public.persontype DEFAULT 'Other'::public.persontype NOT NULL,
    active boolean
);


ALTER TABLE public.occpermitapplicationhuman OWNER TO changeme;

--
-- TOC entry 463 (class 1259 OID 21827)
-- Name: occpermitpublicreason_reasonid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occpermitpublicreason_reasonid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occpermitpublicreason_reasonid_seq OWNER TO changeme;

--
-- TOC entry 464 (class 1259 OID 21829)
-- Name: occpermitapplicationreason; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.occpermitapplicationreason OWNER TO changeme;

--
-- TOC entry 465 (class 1259 OID 21837)
-- Name: occphotorequirement; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occphotorequirement (
    requirementid integer DEFAULT nextval('public.occchecklist_photorequirement_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    notes text,
    required boolean,
    active boolean
);


ALTER TABLE public.occphotorequirement OWNER TO changeme;

--
-- TOC entry 466 (class 1259 OID 21844)
-- Name: spacetype_spacetypeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.spacetype_spacetypeid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.spacetype_spacetypeid_seq OWNER TO changeme;

--
-- TOC entry 467 (class 1259 OID 21846)
-- Name: occspacetype; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.occspacetype (
    spacetypeid integer DEFAULT nextval('public.spacetype_spacetypeid_seq'::regclass) NOT NULL,
    spacetitle text NOT NULL,
    description text NOT NULL,
    required boolean DEFAULT false
);


ALTER TABLE public.occspacetype OWNER TO changeme;

--
-- TOC entry 468 (class 1259 OID 21854)
-- Name: occupancyinspectionstatusid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occupancyinspectionstatusid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occupancyinspectionstatusid_seq OWNER TO changeme;

--
-- TOC entry 469 (class 1259 OID 21856)
-- Name: occupancypermittype_typeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.occupancypermittype_typeid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occupancypermittype_typeid_seq OWNER TO changeme;

--
-- TOC entry 470 (class 1259 OID 21858)
-- Name: parcel_parcelkey_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.parcel_parcelkey_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcel_parcelkey_seq OWNER TO changeme;

--
-- TOC entry 471 (class 1259 OID 21860)
-- Name: parcel; Type: TABLE; Schema: public; Owner: changeme
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
    lotandblock text
);


ALTER TABLE public.parcel OWNER TO changeme;

--
-- TOC entry 472 (class 1259 OID 21867)
-- Name: parcelhumanrole_roleid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.parcelhumanrole_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelhumanrole_roleid_seq OWNER TO changeme;

--
-- TOC entry 473 (class 1259 OID 21869)
-- Name: parcelinfo_infoid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.parcelinfo_infoid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelinfo_infoid_seq OWNER TO changeme;

--
-- TOC entry 474 (class 1259 OID 21871)
-- Name: parcelinfo; Type: TABLE; Schema: public; Owner: changeme
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
    deactivatedby_userid integer
);


ALTER TABLE public.parcelinfo OWNER TO changeme;

--
-- TOC entry 475 (class 1259 OID 21881)
-- Name: parcelmailing_linkid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.parcelmailing_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelmailing_linkid_seq OWNER TO changeme;

--
-- TOC entry 476 (class 1259 OID 21883)
-- Name: parcelmailingaddress; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.parcelmailingaddress (
    mailingparcel_parcelid integer,
    mailingparcel_mailingid integer,
    source_sourceid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text,
    linkid integer DEFAULT nextval('public.parcelmailing_linkid_seq'::regclass) NOT NULL,
    linkedobjectrole_lorid integer
);


ALTER TABLE public.parcelmailingaddress OWNER TO changeme;

--
-- TOC entry 477 (class 1259 OID 21890)
-- Name: parcelmailingaddressid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.parcelmailingaddressid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelmailingaddressid_seq OWNER TO changeme;

--
-- TOC entry 478 (class 1259 OID 21892)
-- Name: parcelmigrationlog_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.parcelmigrationlog_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelmigrationlog_seq OWNER TO changeme;

--
-- TOC entry 479 (class 1259 OID 21894)
-- Name: parcelmigrationlog; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.parcelmigrationlog (
    logentryid integer DEFAULT nextval('public.parcelmigrationlog_seq'::regclass) NOT NULL,
    property_id integer,
    parcel_id integer,
    error_code integer,
    notes text,
    ts timestamp with time zone NOT NULL
);


ALTER TABLE public.parcelmigrationlog OWNER TO changeme;

--
-- TOC entry 480 (class 1259 OID 21901)
-- Name: parcelmigrationlogerrorcode; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.parcelmigrationlogerrorcode (
    code integer NOT NULL,
    descr text NOT NULL,
    fatal boolean DEFAULT true
);


ALTER TABLE public.parcelmigrationlogerrorcode OWNER TO changeme;

--
-- TOC entry 481 (class 1259 OID 21908)
-- Name: parcelpdfdoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.parcelpdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    parcel_parcelkey integer NOT NULL
);


ALTER TABLE public.parcelpdfdoc OWNER TO changeme;

--
-- TOC entry 482 (class 1259 OID 21911)
-- Name: parcelphotodoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.parcelphotodoc (
    photodoc_photodocid integer NOT NULL,
    parcel_parcelkey integer NOT NULL
);


ALTER TABLE public.parcelphotodoc OWNER TO changeme;

--
-- TOC entry 483 (class 1259 OID 21914)
-- Name: parcelunit_unitid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.parcelunit_unitid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelunit_unitid_seq OWNER TO changeme;

--
-- TOC entry 484 (class 1259 OID 21916)
-- Name: parcelunit; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.parcelunit (
    unitid integer DEFAULT nextval('public.parcelunit_unitid_seq'::regclass) NOT NULL,
    unitnumber text NOT NULL,
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


ALTER TABLE public.parcelunit OWNER TO changeme;

--
-- TOC entry 485 (class 1259 OID 21923)
-- Name: photodoc_photodocid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.photodoc_photodocid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.photodoc_photodocid_seq OWNER TO changeme;

--
-- TOC entry 486 (class 1259 OID 21925)
-- Name: pdfdoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.pdfdoc (
    pdfdocid integer DEFAULT nextval('public.photodoc_photodocid_seq'::regclass) NOT NULL,
    pdfdocdescription character varying(100),
    pdfdoccommitted boolean DEFAULT true,
    blobbytes_bytesid integer,
    muni_municode integer
);


ALTER TABLE public.pdfdoc OWNER TO changeme;

--
-- TOC entry 487 (class 1259 OID 21930)
-- Name: person_clone_merge_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.person_clone_merge_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.person_clone_merge_seq OWNER TO changeme;

--
-- TOC entry 488 (class 1259 OID 21932)
-- Name: propertunit_unitid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.propertunit_unitid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertunit_unitid_seq OWNER TO changeme;

--
-- TOC entry 489 (class 1259 OID 21934)
-- Name: personchange; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.personchange OWNER TO changeme;

--
-- TOC entry 490 (class 1259 OID 21942)
-- Name: personchangeid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.personchangeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personchangeid_seq OWNER TO changeme;

--
-- TOC entry 491 (class 1259 OID 21944)
-- Name: personhumanmigrationlog_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.personhumanmigrationlog_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personhumanmigrationlog_seq OWNER TO changeme;

--
-- TOC entry 492 (class 1259 OID 21946)
-- Name: personhumanmigrationlog; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.personhumanmigrationlog (
    logentryid integer DEFAULT nextval('public.personhumanmigrationlog_seq'::regclass) NOT NULL,
    human_humanid integer,
    person_personid integer,
    error_code integer,
    notes text,
    ts timestamp with time zone
);


ALTER TABLE public.personhumanmigrationlog OWNER TO changeme;

--
-- TOC entry 493 (class 1259 OID 21953)
-- Name: personhumanmigrationlogerrorcode; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.personhumanmigrationlogerrorcode (
    code integer NOT NULL,
    descr text NOT NULL,
    fatal boolean DEFAULT true
);


ALTER TABLE public.personhumanmigrationlogerrorcode OWNER TO changeme;

--
-- TOC entry 494 (class 1259 OID 21960)
-- Name: personmergehistory; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.personmergehistory (
    mergeid integer DEFAULT nextval('public.person_clone_merge_seq'::regclass) NOT NULL,
    mergetarget_personid integer,
    mergesource_personid integer,
    mergby_userid integer,
    mergetimestamp timestamp with time zone,
    mergenotes text
);


ALTER TABLE public.personmergehistory OWNER TO changeme;

--
-- TOC entry 495 (class 1259 OID 21967)
-- Name: personmunilink; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.personmunilink (
    muni_municode integer NOT NULL,
    person_personid integer NOT NULL,
    defaultmuni boolean DEFAULT false
);


ALTER TABLE public.personmunilink OWNER TO changeme;

--
-- TOC entry 496 (class 1259 OID 21971)
-- Name: personsource_sourceid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.personsource_sourceid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personsource_sourceid_seq OWNER TO changeme;

--
-- TOC entry 497 (class 1259 OID 21973)
-- Name: photodoc; Type: TABLE; Schema: public; Owner: changeme
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
    createdts timestamp with time zone DEFAULT now()
);


ALTER TABLE public.photodoc OWNER TO changeme;

--
-- TOC entry 498 (class 1259 OID 21982)
-- Name: printstyle_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.printstyle_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.printstyle_seq OWNER TO changeme;

--
-- TOC entry 499 (class 1259 OID 21984)
-- Name: printstyle; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.printstyle OWNER TO changeme;

--
-- TOC entry 500 (class 1259 OID 21992)
-- Name: propertyid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.propertyid_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertyid_seq OWNER TO changeme;

--
-- TOC entry 501 (class 1259 OID 21994)
-- Name: property; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.property OWNER TO changeme;

--
-- TOC entry 502 (class 1259 OID 22005)
-- Name: propertyexternaldata_extdataid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.propertyexternaldata_extdataid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertyexternaldata_extdataid_seq OWNER TO changeme;

--
-- TOC entry 503 (class 1259 OID 22007)
-- Name: propertyexternaldata; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.propertyexternaldata OWNER TO changeme;

--
-- TOC entry 504 (class 1259 OID 22014)
-- Name: propertyotherid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.propertyotherid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertyotherid_seq OWNER TO changeme;

--
-- TOC entry 505 (class 1259 OID 22016)
-- Name: propertyotherid; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.propertyotherid OWNER TO changeme;

--
-- TOC entry 506 (class 1259 OID 22023)
-- Name: propertypdfdoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.propertypdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    property_propertyid integer NOT NULL
);


ALTER TABLE public.propertypdfdoc OWNER TO changeme;

--
-- TOC entry 507 (class 1259 OID 22026)
-- Name: propertyperson; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.propertyperson (
    property_propertyid integer NOT NULL,
    person_personid integer NOT NULL,
    creationts timestamp with time zone
);


ALTER TABLE public.propertyperson OWNER TO changeme;

--
-- TOC entry 508 (class 1259 OID 22029)
-- Name: propertyphotodoc; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.propertyphotodoc (
    photodoc_photodocid integer NOT NULL,
    property_propertyid integer NOT NULL
);


ALTER TABLE public.propertyphotodoc OWNER TO changeme;

--
-- TOC entry 509 (class 1259 OID 22032)
-- Name: propertystatusid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.propertystatusid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertystatusid_seq OWNER TO changeme;

--
-- TOC entry 510 (class 1259 OID 22034)
-- Name: propertystatus; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.propertystatus OWNER TO changeme;

--
-- TOC entry 511 (class 1259 OID 22045)
-- Name: propertyunit; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.propertyunit OWNER TO changeme;

--
-- TOC entry 512 (class 1259 OID 22053)
-- Name: propertyunitchange; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.propertyunitchange OWNER TO changeme;

--
-- TOC entry 513 (class 1259 OID 22061)
-- Name: propertyusetype_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.propertyusetype_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertyusetype_seq OWNER TO changeme;

--
-- TOC entry 514 (class 1259 OID 22063)
-- Name: propertyusetype; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.propertyusetype (
    propertyusetypeid integer DEFAULT nextval('public.propertyusetype_seq'::regclass) NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(100),
    icon_iconid integer,
    zoneclass text,
    active boolean DEFAULT true
);


ALTER TABLE public.propertyusetype OWNER TO changeme;

--
-- TOC entry 515 (class 1259 OID 22071)
-- Name: propevent_eventid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.propevent_eventid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propevent_eventid_seq OWNER TO changeme;

--
-- TOC entry 516 (class 1259 OID 22073)
-- Name: spaceid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.spaceid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.spaceid_seq OWNER TO changeme;

--
-- TOC entry 517 (class 1259 OID 22075)
-- Name: taxstatus_taxstatusid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.taxstatus_taxstatusid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.taxstatus_taxstatusid_seq OWNER TO changeme;

--
-- TOC entry 518 (class 1259 OID 22077)
-- Name: taxstatus; Type: TABLE; Schema: public; Owner: changeme
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


ALTER TABLE public.taxstatus OWNER TO changeme;

--
-- TOC entry 6197 (class 0 OID 0)
-- Dependencies: 518
-- Name: TABLE taxstatus; Type: COMMENT; Schema: public; Owner: changeme
--

COMMENT ON TABLE public.taxstatus IS 'Scraped data from Allegheny County http://www2.alleghenycounty.us/RealEstate/. Description valid as of August 2020';


--
-- TOC entry 519 (class 1259 OID 22084)
-- Name: textblock_blockid_seq; Type: SEQUENCE; Schema: public; Owner: changeme
--

CREATE SEQUENCE public.textblock_blockid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.textblock_blockid_seq OWNER TO changeme;

--
-- TOC entry 520 (class 1259 OID 22086)
-- Name: textblock; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.textblock (
    blockid integer DEFAULT nextval('public.textblock_blockid_seq'::regclass) NOT NULL,
    blockcategory_catid integer NOT NULL,
    muni_municode integer NOT NULL,
    blockname text NOT NULL,
    blocktext text NOT NULL,
    placementorderdefault integer,
    injectabletemplate boolean DEFAULT false
);


ALTER TABLE public.textblock OWNER TO changeme;

--
-- TOC entry 521 (class 1259 OID 22094)
-- Name: textblockcategory; Type: TABLE; Schema: public; Owner: changeme
--

CREATE TABLE public.textblockcategory (
    categoryid integer DEFAULT nextval('public.blockcategory_categoryid_seq'::regclass) NOT NULL,
    categorytitle text NOT NULL,
    icon_iconid integer,
    muni_municode integer
);


ALTER TABLE public.textblockcategory OWNER TO changeme;

--
-- TOC entry 5056 (class 2606 OID 23150)
-- Name: ceactionrequestissuetype actionrqstissuetype_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequestissuetype
    ADD CONSTRAINT actionrqstissuetype_pk PRIMARY KEY (issuetypeid);


--
-- TOC entry 5047 (class 2606 OID 23152)
-- Name: blobbytes blobbytes_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.blobbytes
    ADD CONSTRAINT blobbytes_pk PRIMARY KEY (bytesid);


--
-- TOC entry 5339 (class 2606 OID 23154)
-- Name: textblockcategory blockcategory_catid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.textblockcategory
    ADD CONSTRAINT blockcategory_catid_pk PRIMARY KEY (categoryid);


--
-- TOC entry 5052 (class 2606 OID 23156)
-- Name: bobsource bobsource_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.bobsource
    ADD CONSTRAINT bobsource_pk PRIMARY KEY (sourceid);


--
-- TOC entry 5054 (class 2606 OID 23158)
-- Name: ceactionrequest ceactionrequest_requestid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_requestid_pk PRIMARY KEY (requestid);


--
-- TOC entry 5058 (class 2606 OID 23160)
-- Name: ceactionrequestpdfdoc ceactionrequestpdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequestpdfdoc
    ADD CONSTRAINT ceactionrequestpdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, ceactionrequest_requestid);


--
-- TOC entry 5060 (class 2606 OID 23162)
-- Name: ceactionrequestphotodoc ceactionrequestphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequestphotodoc
    ADD CONSTRAINT ceactionrequestphotodoc_pk PRIMARY KEY (photodoc_photodocid, ceactionrequest_requestid);


--
-- TOC entry 5062 (class 2606 OID 23164)
-- Name: ceactionrequeststatus ceactionrequeststatus_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequeststatus
    ADD CONSTRAINT ceactionrequeststatus_pkey PRIMARY KEY (statusid);


--
-- TOC entry 5064 (class 2606 OID 23166)
-- Name: cecase cecase_caseid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_caseid_pk PRIMARY KEY (caseid);


--
-- TOC entry 5067 (class 2606 OID 23168)
-- Name: cecasephotodoc cecasephotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.cecasephotodoc
    ADD CONSTRAINT cecasephotodoc_pk PRIMARY KEY (photodoc_photodocid, cecase_caseid);


--
-- TOC entry 5069 (class 2606 OID 23170)
-- Name: cecasestatusicon cecasestatusicon_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.cecasestatusicon
    ADD CONSTRAINT cecasestatusicon_pk PRIMARY KEY (iconid, status);


--
-- TOC entry 5130 (class 2606 OID 23172)
-- Name: event ceevent_eventid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT ceevent_eventid_pk PRIMARY KEY (eventid);


--
-- TOC entry 5132 (class 2606 OID 23174)
-- Name: eventcategory ceeventcategory_categoryid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventcategory
    ADD CONSTRAINT ceeventcategory_categoryid_pk PRIMARY KEY (categoryid);


--
-- TOC entry 5081 (class 2606 OID 23176)
-- Name: choiceproposal ceeventproposalresponse_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventproposalresponse_pk PRIMARY KEY (proposalid);


--
-- TOC entry 5236 (class 2606 OID 23178)
-- Name: occchecklistspacetype chkliststiceid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occchecklistspacetype
    ADD CONSTRAINT chkliststiceid_pk PRIMARY KEY (checklistspacetypeid);


--
-- TOC entry 5071 (class 2606 OID 23180)
-- Name: choice choice_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choice
    ADD CONSTRAINT choice_pkey PRIMARY KEY (choiceid);


--
-- TOC entry 5077 (class 2606 OID 23182)
-- Name: choicedirectivedirectiveset choicedirdirset_comp_pf; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choicedirectivedirectiveset
    ADD CONSTRAINT choicedirdirset_comp_pf PRIMARY KEY (directiveset_setid, directive_dirid);


--
-- TOC entry 5079 (class 2606 OID 23184)
-- Name: choicedirectiveset choiceproposalset_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choicedirectiveset
    ADD CONSTRAINT choiceproposalset_pkey PRIMARY KEY (directivesetid);


--
-- TOC entry 5083 (class 2606 OID 23186)
-- Name: citation citation_citationid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT citation_citationid_pk PRIMARY KEY (citationid);


--
-- TOC entry 5086 (class 2606 OID 23188)
-- Name: citationcitationstatus citationcitationstatus_id_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT citationcitationstatus_id_pk PRIMARY KEY (citationstatusid);


--
-- TOC entry 5088 (class 2606 OID 23190)
-- Name: citationdocketno citationdocketno_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationdocketno
    ADD CONSTRAINT citationdocketno_pkey PRIMARY KEY (docketid);


--
-- TOC entry 5090 (class 2606 OID 23192)
-- Name: citationdocketnohuman citationdocketnohuman_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationdocketnohuman
    ADD CONSTRAINT citationdocketnohuman_pkey PRIMARY KEY (linkid);


--
-- TOC entry 5092 (class 2606 OID 23194)
-- Name: citationfilingtype citationfilingtype_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationfilingtype
    ADD CONSTRAINT citationfilingtype_pkey PRIMARY KEY (typeid);


--
-- TOC entry 5096 (class 2606 OID 23196)
-- Name: citationphotodoc citationphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationphotodoc
    ADD CONSTRAINT citationphotodoc_pk PRIMARY KEY (photodoc_photodocid, citation_citationid);


--
-- TOC entry 5098 (class 2606 OID 23198)
-- Name: citationstatus citationstatus_statusid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationstatus
    ADD CONSTRAINT citationstatus_statusid_pk PRIMARY KEY (statusid);


--
-- TOC entry 5100 (class 2606 OID 23200)
-- Name: citationviolation citationviolation_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citationviolation_pkey PRIMARY KEY (citationviolationid);


--
-- TOC entry 5102 (class 2606 OID 23202)
-- Name: codeelement codeelement_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeelement
    ADD CONSTRAINT codeelement_pk PRIMARY KEY (elementid);


--
-- TOC entry 5106 (class 2606 OID 23204)
-- Name: codeelementinjectedvalue codeelementinjectedvalue_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeelementinjectedvalue
    ADD CONSTRAINT codeelementinjectedvalue_pk PRIMARY KEY (injectedvalueid);


--
-- TOC entry 5104 (class 2606 OID 23206)
-- Name: codeelementguide codeelementtype_cvtypeid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeelementguide
    ADD CONSTRAINT codeelementtype_cvtypeid_pk PRIMARY KEY (guideentryid);


--
-- TOC entry 5108 (class 2606 OID 23208)
-- Name: codeset codeset_codesetid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeset
    ADD CONSTRAINT codeset_codesetid_pk PRIMARY KEY (codesetid);


--
-- TOC entry 5110 (class 2606 OID 23210)
-- Name: codesetelement codesetelement_codesetelementid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codesetelement_codesetelementid_pk PRIMARY KEY (codesetelementid);


--
-- TOC entry 5112 (class 2606 OID 23212)
-- Name: codesource codesource_sourceid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codesource
    ADD CONSTRAINT codesource_sourceid_pk PRIMARY KEY (sourceid);


--
-- TOC entry 5114 (class 2606 OID 23214)
-- Name: codeviolation codeviolation_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_pk PRIMARY KEY (violationid);


--
-- TOC entry 5116 (class 2606 OID 23216)
-- Name: codeviolationpdfdoc codeviolationpdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolationpdfdoc
    ADD CONSTRAINT codeviolationpdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, codeviolation_violationid);


--
-- TOC entry 5118 (class 2606 OID 23218)
-- Name: codeviolationphotodoc codeviolationphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolationphotodoc
    ADD CONSTRAINT codeviolationphotodoc_pk PRIMARY KEY (photodoc_photodocid, codeviolation_violationid);


--
-- TOC entry 5166 (class 2606 OID 23220)
-- Name: intensityclass codeviolationseverityclass_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.intensityclass
    ADD CONSTRAINT codeviolationseverityclass_pk PRIMARY KEY (classid);


--
-- TOC entry 5172 (class 2606 OID 23222)
-- Name: log coglog_logentryid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.log
    ADD CONSTRAINT coglog_logentryid_pk PRIMARY KEY (logentryid);


--
-- TOC entry 5120 (class 2606 OID 23224)
-- Name: contactemail contactemail_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactemail
    ADD CONSTRAINT contactemail_pkey PRIMARY KEY (emailid);


--
-- TOC entry 5122 (class 2606 OID 23226)
-- Name: contactphone contactphone_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT contactphone_pkey PRIMARY KEY (phoneid);


--
-- TOC entry 5124 (class 2606 OID 23228)
-- Name: contactphonetype contactphonetype_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactphonetype
    ADD CONSTRAINT contactphonetype_pkey PRIMARY KEY (phonetypeid);


--
-- TOC entry 5126 (class 2606 OID 23230)
-- Name: courtentity courtentity_entityid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.courtentity
    ADD CONSTRAINT courtentity_entityid_pk PRIMARY KEY (entityid);


--
-- TOC entry 5128 (class 2606 OID 23232)
-- Name: dbpatch dbpatch_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.dbpatch
    ADD CONSTRAINT dbpatch_pk PRIMARY KEY (patchnum);


--
-- TOC entry 5138 (class 2606 OID 23234)
-- Name: eventruleimpl erimplid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimplid_pk PRIMARY KEY (erimplid);


--
-- TOC entry 5134 (class 2606 OID 23236)
-- Name: eventhuman eventhuman_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_pkey PRIMARY KEY (linkid);


--
-- TOC entry 5075 (class 2606 OID 23238)
-- Name: choicedirectivechoice eventpropchoice_comppk_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choicedirectivechoice
    ADD CONSTRAINT eventpropchoice_comppk_pk PRIMARY KEY (choice_choiceid, directive_directiveid);


--
-- TOC entry 5073 (class 2606 OID 23240)
-- Name: choicedirective eventproposal_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choicedirective
    ADD CONSTRAINT eventproposal_pk PRIMARY KEY (directiveid);


--
-- TOC entry 5136 (class 2606 OID 23242)
-- Name: eventrule eventrule_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT eventrule_pkey PRIMARY KEY (ruleid);


--
-- TOC entry 5140 (class 2606 OID 23244)
-- Name: eventruleruleset eventruleset_comp_pf; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventruleruleset
    ADD CONSTRAINT eventruleset_comp_pf PRIMARY KEY (ruleset_rulesetid, eventrule_ruleid);


--
-- TOC entry 5142 (class 2606 OID 23246)
-- Name: eventruleset eventruleset_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventruleset
    ADD CONSTRAINT eventruleset_pkey PRIMARY KEY (rulesetid);


--
-- TOC entry 5175 (class 2606 OID 23248)
-- Name: logcategory genlogcategory_catid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.logcategory
    ADD CONSTRAINT genlogcategory_catid_pk PRIMARY KEY (catid);


--
-- TOC entry 5183 (class 2606 OID 23250)
-- Name: loginobjecthistory historyentryid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT historyentryid_pk PRIMARY KEY (historyentryid);


--
-- TOC entry 5144 (class 2606 OID 23252)
-- Name: human human_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_pkey PRIMARY KEY (humanid);


--
-- TOC entry 5146 (class 2606 OID 23254)
-- Name: humancecase humancecase_linkid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_linkid_pk PRIMARY KEY (linkid);


--
-- TOC entry 5094 (class 2606 OID 23256)
-- Name: citationhuman humancitation_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_pkey PRIMARY KEY (linkid);


--
-- TOC entry 5148 (class 2606 OID 23258)
-- Name: humanmailingaddress humanmailingaddress_linkid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailingaddress_linkid_pk PRIMARY KEY (linkid);


--
-- TOC entry 5150 (class 2606 OID 23260)
-- Name: humanmuni humanmuni_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_pkey PRIMARY KEY (linkid);


--
-- TOC entry 5152 (class 2606 OID 23262)
-- Name: humanoccperiod humanoccperiod_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanoccperiod_pkey PRIMARY KEY (linkid);


--
-- TOC entry 5154 (class 2606 OID 23264)
-- Name: humanparcel humanparcel_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT humanparcel_pkey PRIMARY KEY (linkid);


--
-- TOC entry 5156 (class 2606 OID 23266)
-- Name: humanparcelunit humanparcelunit_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT humanparcelunit_pkey PRIMARY KEY (linkid);


--
-- TOC entry 5158 (class 2606 OID 23268)
-- Name: icon iconid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.icon
    ADD CONSTRAINT iconid_pk PRIMARY KEY (iconid);


--
-- TOC entry 5160 (class 2606 OID 23270)
-- Name: improvementstatus improvementstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.improvementstatus
    ADD CONSTRAINT improvementstatus_pkey PRIMARY KEY (statusid);


--
-- TOC entry 5164 (class 2606 OID 23272)
-- Name: improvementtype improvementtype_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.improvementtype
    ADD CONSTRAINT improvementtype_pkey PRIMARY KEY (typeid);


--
-- TOC entry 5244 (class 2606 OID 23274)
-- Name: occinspectedspaceelementphotodoc inspchklstspele_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspaceelementphotodoc
    ADD CONSTRAINT inspchklstspele_pk PRIMARY KEY (photodoc_photodocid, inspectedspaceelement_elementid);


--
-- TOC entry 5242 (class 2606 OID 23276)
-- Name: occinspectedspaceelement inspectedspacetypeice_inspectedsticeid; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT inspectedspacetypeice_inspectedsticeid PRIMARY KEY (inspectedspaceelementid);


--
-- TOC entry 5232 (class 2606 OID 23278)
-- Name: occchecklist inspectionchecklist_checklistid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occchecklist
    ADD CONSTRAINT inspectionchecklist_checklistid_pk PRIMARY KEY (checklistid);


--
-- TOC entry 5168 (class 2606 OID 23280)
-- Name: linkedobjectrole linkedobjectrole_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.linkedobjectrole
    ADD CONSTRAINT linkedobjectrole_pkey PRIMARY KEY (lorid);


--
-- TOC entry 5170 (class 2606 OID 23282)
-- Name: listchangerequest listitemchange_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.listchangerequest
    ADD CONSTRAINT listitemchange_pkey PRIMARY KEY (changeid);


--
-- TOC entry 5256 (class 2606 OID 23284)
-- Name: occlocationdescriptor locationdescription_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occlocationdescriptor
    ADD CONSTRAINT locationdescription_pkey PRIMARY KEY (locationdescriptionid);


--
-- TOC entry 5177 (class 2606 OID 23286)
-- Name: login login_pk2; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_pk2 PRIMARY KEY (userid);


--
-- TOC entry 5181 (class 2606 OID 23288)
-- Name: loginmuniauthperiodlog logincredentialex_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginmuniauthperiodlog
    ADD CONSTRAINT logincredentialex_pk PRIMARY KEY (authperiodlogentryid);


--
-- TOC entry 5179 (class 2606 OID 23290)
-- Name: loginmuniauthperiod loginmuniauthperiod_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginmuniauthperiod
    ADD CONSTRAINT loginmuniauthperiod_pkey PRIMARY KEY (muniauthperiodid);


--
-- TOC entry 5185 (class 2606 OID 23292)
-- Name: mailingaddress mailingaddress_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.mailingaddress
    ADD CONSTRAINT mailingaddress_pkey PRIMARY KEY (addressid);


--
-- TOC entry 5187 (class 2606 OID 23294)
-- Name: mailingcitystatezip mailingcitystatezip_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.mailingcitystatezip
    ADD CONSTRAINT mailingcitystatezip_pkey PRIMARY KEY (id);


--
-- TOC entry 5189 (class 2606 OID 23296)
-- Name: mailingstreet mailingstreet_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.mailingstreet
    ADD CONSTRAINT mailingstreet_pkey PRIMARY KEY (streetid);


--
-- TOC entry 5191 (class 2606 OID 23298)
-- Name: moneycecasefeeassigned moneycecasefeeassigned_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneycecasefeeassigned
    ADD CONSTRAINT moneycecasefeeassigned_pkey PRIMARY KEY (cecaseassignedfeeid);


--
-- TOC entry 5193 (class 2606 OID 23300)
-- Name: moneycecasefeepayment moneycecasefeepayment_comp_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneycecasefeepayment
    ADD CONSTRAINT moneycecasefeepayment_comp_pk PRIMARY KEY (payment_paymentid, cecaseassignedfee_id);


--
-- TOC entry 5195 (class 2606 OID 23302)
-- Name: moneycodesetelementfee moneycodesetelementfee_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneycodesetelementfee
    ADD CONSTRAINT moneycodesetelementfee_pkey PRIMARY KEY (fee_feeid, codesetelement_elementid);


--
-- TOC entry 5199 (class 2606 OID 23304)
-- Name: moneyoccperiodfeeassigned moneyoccperiodfeeassigned_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyoccperiodfeeassigned
    ADD CONSTRAINT moneyoccperiodfeeassigned_pkey PRIMARY KEY (moneyoccperassignedfeeid);


--
-- TOC entry 5201 (class 2606 OID 23306)
-- Name: moneyoccperiodfeepayment moneyoccperiodfeepayment_comp_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyoccperiodfeepayment
    ADD CONSTRAINT moneyoccperiodfeepayment_comp_pk PRIMARY KEY (payment_paymentid, occperiodassignedfee_id);


--
-- TOC entry 5203 (class 2606 OID 23308)
-- Name: moneyoccperiodtypefee moneyoccperiodtypefee_comp_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyoccperiodtypefee
    ADD CONSTRAINT moneyoccperiodtypefee_comp_pk PRIMARY KEY (fee_feeid, occperiodtype_typeid);


--
-- TOC entry 5209 (class 2606 OID 23310)
-- Name: municipality municipality_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT municipality_pk PRIMARY KEY (municode);


--
-- TOC entry 5211 (class 2606 OID 23312)
-- Name: municitystatezip municitystatezip_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municitystatezip
    ADD CONSTRAINT municitystatezip_pk PRIMARY KEY (muni_municode, citystatezip_id);


--
-- TOC entry 5213 (class 2606 OID 23314)
-- Name: municourtentity municourtentity_comp_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municourtentity
    ADD CONSTRAINT municourtentity_comp_pk PRIMARY KEY (muni_municode, courtentity_entityid);


--
-- TOC entry 5215 (class 2606 OID 23316)
-- Name: munilogin munilogin_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.munilogin
    ADD CONSTRAINT munilogin_pkey PRIMARY KEY (muniloginrecordid);


--
-- TOC entry 5217 (class 2606 OID 23318)
-- Name: munipdfdoc munipdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.munipdfdoc
    ADD CONSTRAINT munipdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, muni_municode);


--
-- TOC entry 5219 (class 2606 OID 23320)
-- Name: muniphotodoc muniphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.muniphotodoc
    ADD CONSTRAINT muniphotodoc_pk PRIMARY KEY (photodoc_photodocid, muni_municode);


--
-- TOC entry 5221 (class 2606 OID 23322)
-- Name: muniprofile muniprofile_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.muniprofile
    ADD CONSTRAINT muniprofile_pkey PRIMARY KEY (profileid);


--
-- TOC entry 5223 (class 2606 OID 23324)
-- Name: muniprofileeventruleset muniprofileeventruleset_comp_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.muniprofileeventruleset
    ADD CONSTRAINT muniprofileeventruleset_comp_pk PRIMARY KEY (muniprofile_profileid, ruleset_setid);


--
-- TOC entry 5225 (class 2606 OID 23326)
-- Name: muniprofileoccperiodtype muniprofileoccperiodtype_comp_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.muniprofileoccperiodtype
    ADD CONSTRAINT muniprofileoccperiodtype_comp_pk PRIMARY KEY (muniprofile_profileid, occperiodtype_typeid);


--
-- TOC entry 5230 (class 2606 OID 23328)
-- Name: noticeofviolationcodeviolation noticeofviolationcodeviolation_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolationcodeviolation
    ADD CONSTRAINT noticeofviolationcodeviolation_pk PRIMARY KEY (noticeofviolation_noticeid, codeviolation_violationid);


--
-- TOC entry 5228 (class 2606 OID 23330)
-- Name: noticeofviolation noticeviolation_noticeid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT noticeviolation_noticeid_pk PRIMARY KEY (noticeid);


--
-- TOC entry 5234 (class 2606 OID 23332)
-- Name: occchecklistphotorequirement occchecklistphotorequirement_pk_comp; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occchecklistphotorequirement
    ADD CONSTRAINT occchecklistphotorequirement_pk_comp PRIMARY KEY (occchecklist_checklistid, occphotorequirement_reqid);


--
-- TOC entry 5197 (class 2606 OID 23334)
-- Name: moneyfee occinspecfee_feeid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyfee
    ADD CONSTRAINT occinspecfee_feeid_pk PRIMARY KEY (feeid);


--
-- TOC entry 5240 (class 2606 OID 23336)
-- Name: occinspectedspace occinspectedspace_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspace
    ADD CONSTRAINT occinspectedspace_pkey PRIMARY KEY (inspectedspaceid);


--
-- TOC entry 5246 (class 2606 OID 23338)
-- Name: occinspection occinspection_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_pk PRIMARY KEY (inspectionid);


--
-- TOC entry 5248 (class 2606 OID 23340)
-- Name: occinspectioncause occinspectioncause_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectioncause
    ADD CONSTRAINT occinspectioncause_pkey PRIMARY KEY (causeid);


--
-- TOC entry 5250 (class 2606 OID 23342)
-- Name: occinspectiondetermination occinspectiondetermination_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectiondetermination
    ADD CONSTRAINT occinspectiondetermination_pkey PRIMARY KEY (determinationid);


--
-- TOC entry 5252 (class 2606 OID 23344)
-- Name: occinspectionphotodoc occinspectionphotodoc_pk_comp; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectionphotodoc
    ADD CONSTRAINT occinspectionphotodoc_pk_comp PRIMARY KEY (photodoc_photodocid, inspection_inspectionid);


--
-- TOC entry 5254 (class 2606 OID 23346)
-- Name: occinspectionpropertystatus occinspectionpropstatuspk_comp; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectionpropertystatus
    ADD CONSTRAINT occinspectionpropstatuspk_comp PRIMARY KEY (occinspection_inspectionid, propertystatus_statusid);


--
-- TOC entry 5258 (class 2606 OID 23348)
-- Name: occperiod occperiod_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_pk PRIMARY KEY (periodid);


--
-- TOC entry 5260 (class 2606 OID 23350)
-- Name: occperiodeventrule occperiodeventrule_comp_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodeventrule
    ADD CONSTRAINT occperiodeventrule_comp_pk PRIMARY KEY (occperiod_periodid, eventrule_ruleid);


--
-- TOC entry 5262 (class 2606 OID 23352)
-- Name: occperiodpdfdoc occperiodpdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodpdfdoc
    ADD CONSTRAINT occperiodpdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, occperiod_periodid);


--
-- TOC entry 5264 (class 2606 OID 23354)
-- Name: occperiodpermitapplication occperiodpermitapp_comp_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodpermitapplication
    ADD CONSTRAINT occperiodpermitapp_comp_pk PRIMARY KEY (occperiod_periodid, occpermitapp_applicationid);


--
-- TOC entry 5266 (class 2606 OID 23356)
-- Name: occperiodphotodoc occperiodphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodphotodoc
    ADD CONSTRAINT occperiodphotodoc_pk PRIMARY KEY (photodoc_photodocid, occperiod_periodid);


--
-- TOC entry 5268 (class 2606 OID 23358)
-- Name: occperiodtype occperiodtype_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodtype
    ADD CONSTRAINT occperiodtype_pk PRIMARY KEY (typeid);


--
-- TOC entry 5270 (class 2606 OID 23360)
-- Name: occpermit occpermit_permitid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occpermit_permitid_pk PRIMARY KEY (permitid);


--
-- TOC entry 5272 (class 2606 OID 23362)
-- Name: occpermitapplication occpermitapp_applicationid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occpermitapplication
    ADD CONSTRAINT occpermitapp_applicationid_pk PRIMARY KEY (applicationid);


--
-- TOC entry 5274 (class 2606 OID 23364)
-- Name: occpermitapplicationhuman occpermitapplicationhuman_comp_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occpermitapplicationhuman
    ADD CONSTRAINT occpermitapplicationhuman_comp_pk PRIMARY KEY (occpermitapplication_applicationid, human_humanid);


--
-- TOC entry 5276 (class 2606 OID 23366)
-- Name: occpermitapplicationreason occpermitreason_reasonid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occpermitapplicationreason
    ADD CONSTRAINT occpermitreason_reasonid_pk PRIMARY KEY (reasonid);


--
-- TOC entry 5278 (class 2606 OID 23368)
-- Name: occphotorequirement occphotorequirement_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occphotorequirement
    ADD CONSTRAINT occphotorequirement_pkey PRIMARY KEY (requirementid);


--
-- TOC entry 5312 (class 2606 OID 23370)
-- Name: printstyle paramsid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.printstyle
    ADD CONSTRAINT paramsid_pk PRIMARY KEY (styleid);


--
-- TOC entry 5282 (class 2606 OID 23372)
-- Name: parcel parcel_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_pkey PRIMARY KEY (parcelkey);


--
-- TOC entry 5284 (class 2606 OID 23374)
-- Name: parcelinfo parcelinfo_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_pkey PRIMARY KEY (parcelinfoid);


--
-- TOC entry 5286 (class 2606 OID 23376)
-- Name: parcelmailingaddress parcelmailingaddress_linkid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT parcelmailingaddress_linkid_pk PRIMARY KEY (linkid);


--
-- TOC entry 5288 (class 2606 OID 23378)
-- Name: parcelmigrationlog parcelmigrationlog_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelmigrationlog
    ADD CONSTRAINT parcelmigrationlog_pkey PRIMARY KEY (logentryid);


--
-- TOC entry 5290 (class 2606 OID 23380)
-- Name: parcelmigrationlogerrorcode parcelmigrationlogerrorcode_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelmigrationlogerrorcode
    ADD CONSTRAINT parcelmigrationlogerrorcode_pkey PRIMARY KEY (code);


--
-- TOC entry 5292 (class 2606 OID 23382)
-- Name: parcelpdfdoc parcelpdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelpdfdoc
    ADD CONSTRAINT parcelpdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, parcel_parcelkey);


--
-- TOC entry 5294 (class 2606 OID 23384)
-- Name: parcelphotodoc parcelphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelphotodoc
    ADD CONSTRAINT parcelphotodoc_pk PRIMARY KEY (photodoc_photodocid, parcel_parcelkey);


--
-- TOC entry 5296 (class 2606 OID 23386)
-- Name: parcelunit parcelunit_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_pkey PRIMARY KEY (unitid);


--
-- TOC entry 5205 (class 2606 OID 23388)
-- Name: moneypayment payment_paymentid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneypayment
    ADD CONSTRAINT payment_paymentid_pk PRIMARY KEY (paymentid);


--
-- TOC entry 5298 (class 2606 OID 23390)
-- Name: pdfdoc pdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.pdfdoc
    ADD CONSTRAINT pdfdoc_pk PRIMARY KEY (pdfdocid);


--
-- TOC entry 5300 (class 2606 OID 23392)
-- Name: personchange personchangeid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personchange
    ADD CONSTRAINT personchangeid_pk PRIMARY KEY (personchangeid);


--
-- TOC entry 5302 (class 2606 OID 23394)
-- Name: personhumanmigrationlog personhumanmigrationlog_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personhumanmigrationlog
    ADD CONSTRAINT personhumanmigrationlog_pkey PRIMARY KEY (logentryid);


--
-- TOC entry 5304 (class 2606 OID 23396)
-- Name: personhumanmigrationlogerrorcode personhumanmigrationlogerrorcode_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personhumanmigrationlogerrorcode
    ADD CONSTRAINT personhumanmigrationlogerrorcode_pkey PRIMARY KEY (code);


--
-- TOC entry 5045 (class 2606 OID 23398)
-- Name: person personid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT personid_pk PRIMARY KEY (personid);


--
-- TOC entry 5306 (class 2606 OID 23400)
-- Name: personmergehistory personmerge; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personmergehistory
    ADD CONSTRAINT personmerge PRIMARY KEY (mergeid);


--
-- TOC entry 5308 (class 2606 OID 23402)
-- Name: personmunilink personmuni; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personmunilink
    ADD CONSTRAINT personmuni PRIMARY KEY (muni_municode, person_personid);


--
-- TOC entry 5310 (class 2606 OID 23404)
-- Name: photodoc photodoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.photodoc
    ADD CONSTRAINT photodoc_pk PRIMARY KEY (photodocid);


--
-- TOC entry 5050 (class 2606 OID 23406)
-- Name: blobtype photodoctype_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.blobtype
    ADD CONSTRAINT photodoctype_pkey PRIMARY KEY (typeid);


--
-- TOC entry 5207 (class 2606 OID 23408)
-- Name: moneypaymenttype pmttype_typeid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneypaymenttype
    ADD CONSTRAINT pmttype_typeid_pk PRIMARY KEY (typeid);


--
-- TOC entry 5315 (class 2606 OID 23410)
-- Name: property property_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_pk PRIMARY KEY (propertyid);


--
-- TOC entry 5317 (class 2606 OID 23412)
-- Name: propertyexternaldata propertyexternaldata_extdataid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyexternaldata
    ADD CONSTRAINT propertyexternaldata_extdataid_pk PRIMARY KEY (extdataid);


--
-- TOC entry 5319 (class 2606 OID 23414)
-- Name: propertyotherid propertyotherid_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyotherid
    ADD CONSTRAINT propertyotherid_pkey PRIMARY KEY (otheridid);


--
-- TOC entry 5321 (class 2606 OID 23416)
-- Name: propertypdfdoc propertypdfdoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertypdfdoc
    ADD CONSTRAINT propertypdfdoc_pk PRIMARY KEY (pdfdoc_pdfdocid, property_propertyid);


--
-- TOC entry 5323 (class 2606 OID 23418)
-- Name: propertyperson propertyperson_propid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyperson
    ADD CONSTRAINT propertyperson_propid_pk PRIMARY KEY (property_propertyid, person_personid);


--
-- TOC entry 5325 (class 2606 OID 23420)
-- Name: propertyphotodoc propertyphotodoc_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyphotodoc
    ADD CONSTRAINT propertyphotodoc_pk PRIMARY KEY (photodoc_photodocid, property_propertyid);


--
-- TOC entry 5327 (class 2606 OID 23422)
-- Name: propertystatus propertystatus_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertystatus
    ADD CONSTRAINT propertystatus_pkey PRIMARY KEY (statusid);


--
-- TOC entry 5333 (class 2606 OID 23424)
-- Name: propertyusetype propertyusetype_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyusetype
    ADD CONSTRAINT propertyusetype_pk PRIMARY KEY (propertyusetypeid);


--
-- TOC entry 5238 (class 2606 OID 23426)
-- Name: occchecklistspacetypeelement spaceelementid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occchecklistspacetypeelement
    ADD CONSTRAINT spaceelementid_pk PRIMARY KEY (spaceelementid);


--
-- TOC entry 5280 (class 2606 OID 23428)
-- Name: occspacetype spacetype_spacetypeid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occspacetype
    ADD CONSTRAINT spacetype_spacetypeid_pk PRIMARY KEY (spacetypeid);


--
-- TOC entry 5162 (class 2606 OID 23430)
-- Name: improvementsuggestion systemimprovements_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.improvementsuggestion
    ADD CONSTRAINT systemimprovements_pkey PRIMARY KEY (improvementid);


--
-- TOC entry 5335 (class 2606 OID 23432)
-- Name: taxstatus taxstatus_pkey; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.taxstatus
    ADD CONSTRAINT taxstatus_pkey PRIMARY KEY (taxstatusid);


--
-- TOC entry 5337 (class 2606 OID 23434)
-- Name: textblock textblock_blockid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.textblock
    ADD CONSTRAINT textblock_blockid_pk PRIMARY KEY (blockid);


--
-- TOC entry 5331 (class 2606 OID 23436)
-- Name: propertyunitchange unitchangeid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyunitchange
    ADD CONSTRAINT unitchangeid_pk PRIMARY KEY (unitchangeid);


--
-- TOC entry 5329 (class 2606 OID 23438)
-- Name: propertyunit unitid_pk; Type: CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyunit
    ADD CONSTRAINT unitid_pk PRIMARY KEY (unitid);


--
-- TOC entry 5048 (class 1259 OID 23439)
-- Name: fki_blobbytes_uploadedby_fk; Type: INDEX; Schema: public; Owner: changeme
--

CREATE INDEX fki_blobbytes_uploadedby_fk ON public.blobbytes USING btree (uploadedby_userid);


--
-- TOC entry 5065 (class 1259 OID 23440)
-- Name: fki_cecase_login_userid_fk; Type: INDEX; Schema: public; Owner: changeme
--

CREATE INDEX fki_cecase_login_userid_fk ON public.cecase USING btree (login_userid);


--
-- TOC entry 5084 (class 1259 OID 23441)
-- Name: fki_citation_userid_fk; Type: INDEX; Schema: public; Owner: changeme
--

CREATE INDEX fki_citation_userid_fk ON public.citation USING btree (login_userid);


--
-- TOC entry 5173 (class 1259 OID 23442)
-- Name: fki_genlogcategory_catid_fk; Type: INDEX; Schema: public; Owner: changeme
--

CREATE INDEX fki_genlogcategory_catid_fk ON public.log USING btree (category);


--
-- TOC entry 5226 (class 1259 OID 23443)
-- Name: fki_noticeOfViolation_recipient_fk; Type: INDEX; Schema: public; Owner: changeme
--

CREATE INDEX "fki_noticeOfViolation_recipient_fk" ON public.noticeofviolation USING btree (personid_recipient);


--
-- TOC entry 5313 (class 1259 OID 23444)
-- Name: property_address_idx; Type: INDEX; Schema: public; Owner: changeme
--

CREATE INDEX property_address_idx ON public.property USING btree (address);


--
-- TOC entry 5361 (class 2606 OID 23445)
-- Name: ceactionrequestissuetype acrreqisstype_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequestissuetype
    ADD CONSTRAINT acrreqisstype_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5348 (class 2606 OID 23450)
-- Name: blobbytes blobbytes_uploadedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.blobbytes
    ADD CONSTRAINT blobbytes_uploadedby_fk FOREIGN KEY (uploadedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5793 (class 2606 OID 23455)
-- Name: textblock blockcategory_catid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.textblock
    ADD CONSTRAINT blockcategory_catid_fk FOREIGN KEY (blockcategory_catid) REFERENCES public.textblockcategory(categoryid);


--
-- TOC entry 5351 (class 2606 OID 23460)
-- Name: bobsource bobsource_creator_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.bobsource
    ADD CONSTRAINT bobsource_creator_userid_fk FOREIGN KEY (creator) REFERENCES public.login(userid);


--
-- TOC entry 5350 (class 2606 OID 23465)
-- Name: bobsource bobsource_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.bobsource
    ADD CONSTRAINT bobsource_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5352 (class 2606 OID 23470)
-- Name: ceactionrequest ceactionreq_usersub_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionreq_usersub_fk FOREIGN KEY (usersubmitter_userid) REFERENCES public.login(userid);


--
-- TOC entry 5353 (class 2606 OID 23475)
-- Name: ceactionrequest ceactionrequest_caseattachment_userid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_caseattachment_userid_fkey FOREIGN KEY (caseattachment_userid) REFERENCES public.login(userid);


--
-- TOC entry 5354 (class 2606 OID 23480)
-- Name: ceactionrequest ceactionrequest_caseid; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_caseid FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 5355 (class 2606 OID 23485)
-- Name: ceactionrequest ceactionrequest_issuetypeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_issuetypeid_fk FOREIGN KEY (issuetype_issuetypeid) REFERENCES public.ceactionrequestissuetype(issuetypeid);


--
-- TOC entry 5356 (class 2606 OID 23490)
-- Name: ceactionrequest ceactionrequest_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5357 (class 2606 OID 23495)
-- Name: ceactionrequest ceactionrequest_prop_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_prop_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 5358 (class 2606 OID 23500)
-- Name: ceactionrequest ceactionrequest_requestorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT ceactionrequest_requestorid_fk FOREIGN KEY (actrequestor_requestorid) REFERENCES public.person(personid);


--
-- TOC entry 5360 (class 2606 OID 23505)
-- Name: ceactionrequestissuetype ceactionrequestissuetype_intensity_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequestissuetype
    ADD CONSTRAINT ceactionrequestissuetype_intensity_fk FOREIGN KEY (intensity_classid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 5363 (class 2606 OID 23510)
-- Name: ceactionrequestpdfdoc ceactionrequestpdfdoc_cear_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequestpdfdoc
    ADD CONSTRAINT ceactionrequestpdfdoc_cear_fk FOREIGN KEY (ceactionrequest_requestid) REFERENCES public.ceactionrequest(requestid);


--
-- TOC entry 5362 (class 2606 OID 23515)
-- Name: ceactionrequestpdfdoc ceactionrequestpdfdoc_pdfdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequestpdfdoc
    ADD CONSTRAINT ceactionrequestpdfdoc_pdfdoc_fk FOREIGN KEY (pdfdoc_pdfdocid) REFERENCES public.pdfdoc(pdfdocid);


--
-- TOC entry 5365 (class 2606 OID 23520)
-- Name: ceactionrequestphotodoc ceactionrequestphotodoc_cear_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequestphotodoc
    ADD CONSTRAINT ceactionrequestphotodoc_cear_fk FOREIGN KEY (ceactionrequest_requestid) REFERENCES public.ceactionrequest(requestid);


--
-- TOC entry 5364 (class 2606 OID 23525)
-- Name: ceactionrequestphotodoc ceactionrequestphotodoc_phdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequestphotodoc
    ADD CONSTRAINT ceactionrequestphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 5367 (class 2606 OID 23530)
-- Name: cecase cecase_bobsourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_bobsourceid_fk FOREIGN KEY (bobsource_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5368 (class 2606 OID 23535)
-- Name: cecase cecase_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5369 (class 2606 OID 23540)
-- Name: cecase cecase_login_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_login_userid_fk FOREIGN KEY (login_userid) REFERENCES public.login(userid);


--
-- TOC entry 5370 (class 2606 OID 23545)
-- Name: cecase cecase_personid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_personid_fk FOREIGN KEY (personinfocase_personid) REFERENCES public.person(personid);


--
-- TOC entry 5371 (class 2606 OID 23550)
-- Name: cecase cecase_propertyid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_propertyid_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 5372 (class 2606 OID 23555)
-- Name: cecase cecase_unitid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.cecase
    ADD CONSTRAINT cecase_unitid_fk FOREIGN KEY (propertyunit_unitid) REFERENCES public.propertyunit(unitid);


--
-- TOC entry 5374 (class 2606 OID 23560)
-- Name: cecasephotodoc cecaseiolationphotodoc_cv_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.cecasephotodoc
    ADD CONSTRAINT cecaseiolationphotodoc_cv_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 5373 (class 2606 OID 23565)
-- Name: cecasephotodoc cecasephotodoc_photodocid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.cecasephotodoc
    ADD CONSTRAINT cecasephotodoc_photodocid_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 5472 (class 2606 OID 23570)
-- Name: event ceevent_cecaseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT ceevent_cecaseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 5473 (class 2606 OID 23575)
-- Name: event ceevent_ceeventcategory_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT ceevent_ceeventcategory_fk FOREIGN KEY (category_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 5474 (class 2606 OID 23580)
-- Name: event ceevent_login_userid; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT ceevent_login_userid FOREIGN KEY (creator_userid) REFERENCES public.login(userid);


--
-- TOC entry 5475 (class 2606 OID 23585)
-- Name: event ceevent_occperiodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT ceevent_occperiodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5478 (class 2606 OID 23590)
-- Name: eventcategory ceeventcat_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventcategory
    ADD CONSTRAINT ceeventcat_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 5383 (class 2606 OID 23595)
-- Name: choiceproposal ceeventpropimp_genevent_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventpropimp_genevent_fk FOREIGN KEY (generatingevent_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 5384 (class 2606 OID 23600)
-- Name: choiceproposal ceeventpropimp_initiator_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventpropimp_initiator_fk FOREIGN KEY (initiator_userid) REFERENCES public.login(userid);


--
-- TOC entry 5385 (class 2606 OID 23605)
-- Name: choiceproposal ceeventpropimp_propid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventpropimp_propid_fk FOREIGN KEY (directive_directiveid) REFERENCES public.choicedirective(directiveid);


--
-- TOC entry 5386 (class 2606 OID 23610)
-- Name: choiceproposal ceeventpropimp_resev_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventpropimp_resev_fk FOREIGN KEY (responseevent_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 5387 (class 2606 OID 23615)
-- Name: choiceproposal ceeventpropimp_responderactual_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventpropimp_responderactual_fk FOREIGN KEY (responderactual_userid) REFERENCES public.login(userid);


--
-- TOC entry 5388 (class 2606 OID 23620)
-- Name: choiceproposal ceeventpropimp_responderintended_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT ceeventpropimp_responderintended_fk FOREIGN KEY (responderintended_userid) REFERENCES public.login(userid);


--
-- TOC entry 5382 (class 2606 OID 23625)
-- Name: choicedirectivedirectiveset choicedirdirset_dirid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choicedirectivedirectiveset
    ADD CONSTRAINT choicedirdirset_dirid_fk FOREIGN KEY (directive_dirid) REFERENCES public.choicedirective(directiveid);


--
-- TOC entry 5381 (class 2606 OID 23630)
-- Name: choicedirectivedirectiveset choicedirdirset_dirsetid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choicedirectivedirectiveset
    ADD CONSTRAINT choicedirdirset_dirsetid_fk FOREIGN KEY (directiveset_setid) REFERENCES public.choicedirectiveset(directivesetid);


--
-- TOC entry 5389 (class 2606 OID 23635)
-- Name: choiceproposal choiceproposal_cecaseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT choiceproposal_cecaseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 5390 (class 2606 OID 23640)
-- Name: choiceproposal choiceproposal_chosenchoiceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT choiceproposal_chosenchoiceid_fk FOREIGN KEY (chosen_choiceid) REFERENCES public.choice(choiceid);


--
-- TOC entry 5391 (class 2606 OID 23645)
-- Name: choiceproposal choiceproposal_occperiodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choiceproposal
    ADD CONSTRAINT choiceproposal_occperiodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5392 (class 2606 OID 23650)
-- Name: citation citation_citationstatusid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT citation_citationstatusid_fk FOREIGN KEY (status_statusid) REFERENCES public.citationstatus(statusid);


--
-- TOC entry 5393 (class 2606 OID 23655)
-- Name: citation citation_courtentity_entityid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT citation_courtentity_entityid_fk FOREIGN KEY (origin_courtentity_entityid) REFERENCES public.courtentity(entityid);


--
-- TOC entry 5394 (class 2606 OID 23660)
-- Name: citation citation_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT citation_userid_fk FOREIGN KEY (login_userid) REFERENCES public.login(userid);


--
-- TOC entry 5399 (class 2606 OID 23665)
-- Name: citationcitationstatus citationcitationstatus_courtentityid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT citationcitationstatus_courtentityid_fk FOREIGN KEY (courtentity_entityid) REFERENCES public.courtentity(entityid);


--
-- TOC entry 5400 (class 2606 OID 23670)
-- Name: citationcitationstatus citationcitstatus_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT citationcitstatus_fk FOREIGN KEY (citationstatus_statusid) REFERENCES public.citationstatus(statusid);


--
-- TOC entry 5405 (class 2606 OID 23675)
-- Name: citationdocketno citationdocketno_citationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationdocketno
    ADD CONSTRAINT citationdocketno_citationid_fk FOREIGN KEY (citation_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 5406 (class 2606 OID 23680)
-- Name: citationdocketno citationdocketno_courtentityid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationdocketno
    ADD CONSTRAINT citationdocketno_courtentityid_fk FOREIGN KEY (courtentity_entityid) REFERENCES public.courtentity(entityid);


--
-- TOC entry 5411 (class 2606 OID 23685)
-- Name: citationdocketnohuman citationdocketnohuman_docketid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationdocketnohuman
    ADD CONSTRAINT citationdocketnohuman_docketid_fk FOREIGN KEY (docketno_docketid) REFERENCES public.citationdocketno(docketid);


--
-- TOC entry 5410 (class 2606 OID 23690)
-- Name: citationdocketnohuman citationdocketnohuman_humanlinkid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationdocketnohuman
    ADD CONSTRAINT citationdocketnohuman_humanlinkid_fk FOREIGN KEY (citationhuman_linkid) REFERENCES public.citationhuman(linkid);


--
-- TOC entry 5401 (class 2606 OID 23695)
-- Name: citationcitationstatus citationevent_citationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT citationevent_citationid_fk FOREIGN KEY (citation_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 5413 (class 2606 OID 23700)
-- Name: citationevent citationevent_citationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationevent
    ADD CONSTRAINT citationevent_citationid_fk FOREIGN KEY (citation_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 5412 (class 2606 OID 23705)
-- Name: citationevent citationevent_eventid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationevent
    ADD CONSTRAINT citationevent_eventid_fk FOREIGN KEY (event_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 5414 (class 2606 OID 23710)
-- Name: citationfilingtype citationfilingtype_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationfilingtype
    ADD CONSTRAINT citationfilingtype_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5395 (class 2606 OID 23715)
-- Name: citation citationfilitytype_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT citationfilitytype_typeid_fk FOREIGN KEY (filingtype_typeid) REFERENCES public.citationfilingtype(typeid);


--
-- TOC entry 5415 (class 2606 OID 23720)
-- Name: citationhuman citationhuman_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT citationhuman_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 5423 (class 2606 OID 23725)
-- Name: citationphotodoc citationphotodoc_cit_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationphotodoc
    ADD CONSTRAINT citationphotodoc_cit_fk FOREIGN KEY (citation_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 5422 (class 2606 OID 23730)
-- Name: citationphotodoc citationphotodoc_phdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationphotodoc
    ADD CONSTRAINT citationphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 5426 (class 2606 OID 23735)
-- Name: citationstatus citationstatus_courtentityid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationstatus
    ADD CONSTRAINT citationstatus_courtentityid_fk FOREIGN KEY (courtentity_entityid) REFERENCES public.courtentity(entityid);


--
-- TOC entry 5425 (class 2606 OID 23740)
-- Name: citationstatus citationstatus_eventruleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationstatus
    ADD CONSTRAINT citationstatus_eventruleid_fk FOREIGN KEY (eventrule_ruleid) REFERENCES public.eventrule(ruleid);


--
-- TOC entry 5424 (class 2606 OID 23745)
-- Name: citationstatus citationstatus_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationstatus
    ADD CONSTRAINT citationstatus_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 5427 (class 2606 OID 23750)
-- Name: citationviolation citationviol_citationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citationviol_citationid_fk FOREIGN KEY (citation_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 5428 (class 2606 OID 23755)
-- Name: citationviolation citationviolation_violationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citationviolation_violationid_fk FOREIGN KEY (codeviolation_violationid) REFERENCES public.codeviolation(violationid);


--
-- TOC entry 5429 (class 2606 OID 23760)
-- Name: citationviolation citviol_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citviol_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5430 (class 2606 OID 23765)
-- Name: citationviolation citviol_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citviol_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5431 (class 2606 OID 23770)
-- Name: citationviolation citviol_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citviol_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5432 (class 2606 OID 23775)
-- Name: citationviolation citviol_source_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationviolation
    ADD CONSTRAINT citviol_source_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5646 (class 2606 OID 23780)
-- Name: occchecklistspacetype cklist_spacetypeice_checklistid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occchecklistspacetype
    ADD CONSTRAINT cklist_spacetypeice_checklistid_fk FOREIGN KEY (checklist_id) REFERENCES public.occchecklist(checklistid);


--
-- TOC entry 5433 (class 2606 OID 23785)
-- Name: codeelement codeelement_codesource_sourceid; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeelement
    ADD CONSTRAINT codeelement_codesource_sourceid FOREIGN KEY (codesource_sourceid) REFERENCES public.codesource(sourceid);


--
-- TOC entry 5434 (class 2606 OID 23790)
-- Name: codeelement codeelement_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeelement
    ADD CONSTRAINT codeelement_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5435 (class 2606 OID 23795)
-- Name: codeelement codeelement_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeelement
    ADD CONSTRAINT codeelement_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5436 (class 2606 OID 23800)
-- Name: codeelement codeelement_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeelement
    ADD CONSTRAINT codeelement_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5648 (class 2606 OID 23805)
-- Name: occchecklistspacetypeelement codelementid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occchecklistspacetypeelement
    ADD CONSTRAINT codelementid_fk FOREIGN KEY (codeelement_id) REFERENCES public.codeelement(elementid);


--
-- TOC entry 5442 (class 2606 OID 23810)
-- Name: codesetelement codeseetelement_elementid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codeseetelement_elementid_fk FOREIGN KEY (codelement_elementid) REFERENCES public.codeelement(elementid);


--
-- TOC entry 5443 (class 2606 OID 23815)
-- Name: codesetelement codeseetelement_setid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codeseetelement_setid_fk FOREIGN KEY (codeset_codesetid) REFERENCES public.codeset(codesetid);


--
-- TOC entry 5441 (class 2606 OID 23820)
-- Name: codeset codeset_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeset
    ADD CONSTRAINT codeset_municode_fk FOREIGN KEY (municipality_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5444 (class 2606 OID 23825)
-- Name: codesetelement codesetele_severityclassdefault_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codesetele_severityclassdefault_classid_fk FOREIGN KEY (defaultseverityclass_classid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 5445 (class 2606 OID 23830)
-- Name: codesetelement codesetelement_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codesetelement_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5446 (class 2606 OID 23835)
-- Name: codesetelement codesetelement_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codesetelement_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5447 (class 2606 OID 23840)
-- Name: codesetelement codesetelement_feeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codesetelement_feeid_fk FOREIGN KEY (fee_feeid) REFERENCES public.moneyfee(feeid);


--
-- TOC entry 5448 (class 2606 OID 23845)
-- Name: codesetelement codesetelement_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codesetelement
    ADD CONSTRAINT codesetelement_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5449 (class 2606 OID 23850)
-- Name: codeviolation codeviolation_bobsource_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_bobsource_fk FOREIGN KEY (bobsource_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5450 (class 2606 OID 23855)
-- Name: codeviolation codeviolation_caseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_caseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 5451 (class 2606 OID 23860)
-- Name: codeviolation codeviolation_cdsetel_elementid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_cdsetel_elementid_fk FOREIGN KEY (codesetelement_elementid) REFERENCES public.codesetelement(codesetelementid);


--
-- TOC entry 5452 (class 2606 OID 23865)
-- Name: codeviolation codeviolation_complianceofficer_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_complianceofficer_fk FOREIGN KEY (complianceuser) REFERENCES public.login(userid);


--
-- TOC entry 5453 (class 2606 OID 23870)
-- Name: codeviolation codeviolation_lastupdatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_lastupdatedby_fk FOREIGN KEY (lastupdated_userid) REFERENCES public.login(userid);


--
-- TOC entry 5454 (class 2606 OID 23875)
-- Name: codeviolation codeviolation_nullifiedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_nullifiedby_fk FOREIGN KEY (nullifiedby) REFERENCES public.login(userid);


--
-- TOC entry 5455 (class 2606 OID 23880)
-- Name: codeviolation codeviolation_tfexpiry_proposalid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolation_tfexpiry_proposalid_fk FOREIGN KEY (compliancetfexpiry_proposalid) REFERENCES public.choiceproposal(proposalid);


--
-- TOC entry 5640 (class 2606 OID 23885)
-- Name: noticeofviolationcodeviolation codeviolation_violationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolationcodeviolation
    ADD CONSTRAINT codeviolation_violationid_fk FOREIGN KEY (codeviolation_violationid) REFERENCES public.codeviolation(violationid);


--
-- TOC entry 5459 (class 2606 OID 23890)
-- Name: codeviolationpdfdoc codeviolationpdfdoc_cv_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolationpdfdoc
    ADD CONSTRAINT codeviolationpdfdoc_cv_fk FOREIGN KEY (codeviolation_violationid) REFERENCES public.codeviolation(violationid);


--
-- TOC entry 5458 (class 2606 OID 23895)
-- Name: codeviolationpdfdoc codeviolationpdfdoc_pdfdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolationpdfdoc
    ADD CONSTRAINT codeviolationpdfdoc_pdfdoc_fk FOREIGN KEY (pdfdoc_pdfdocid) REFERENCES public.pdfdoc(pdfdocid);


--
-- TOC entry 5461 (class 2606 OID 23900)
-- Name: codeviolationphotodoc codeviolationphotodoc_cv_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolationphotodoc
    ADD CONSTRAINT codeviolationphotodoc_cv_fk FOREIGN KEY (codeviolation_violationid) REFERENCES public.codeviolation(violationid);


--
-- TOC entry 5460 (class 2606 OID 23905)
-- Name: codeviolationphotodoc codeviolationphotodoc_phdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolationphotodoc
    ADD CONSTRAINT codeviolationphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 5456 (class 2606 OID 23910)
-- Name: codeviolation codeviolationseverityclass_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT codeviolationseverityclass_fk FOREIGN KEY (severity_classid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 5466 (class 2606 OID 23915)
-- Name: contactphone contactemail_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT contactemail_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5462 (class 2606 OID 23920)
-- Name: contactemail contactemail_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactemail
    ADD CONSTRAINT contactemail_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5467 (class 2606 OID 23925)
-- Name: contactphone contactphone_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT contactphone_typeid_fk FOREIGN KEY (phonetype_typeid) REFERENCES public.contactphonetype(phonetypeid);


--
-- TOC entry 5552 (class 2606 OID 23930)
-- Name: intensityclass cvclass_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.intensityclass
    ADD CONSTRAINT cvclass_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5551 (class 2606 OID 23935)
-- Name: intensityclass cvclass_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.intensityclass
    ADD CONSTRAINT cvclass_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 5407 (class 2606 OID 23940)
-- Name: citationdocketno docketno_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationdocketno
    ADD CONSTRAINT docketno_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5408 (class 2606 OID 23945)
-- Name: citationdocketno docketno_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationdocketno
    ADD CONSTRAINT docketno_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5409 (class 2606 OID 23950)
-- Name: citationdocketno docketno_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationdocketno
    ADD CONSTRAINT docketno_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5463 (class 2606 OID 23955)
-- Name: contactemail email_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactemail
    ADD CONSTRAINT email_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5464 (class 2606 OID 23960)
-- Name: contactemail email_deactivated_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactemail
    ADD CONSTRAINT email_deactivated_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5465 (class 2606 OID 23965)
-- Name: contactemail email_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactemail
    ADD CONSTRAINT email_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5491 (class 2606 OID 23970)
-- Name: eventruleimpl erimpl_caseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_caseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 5492 (class 2606 OID 23975)
-- Name: eventruleimpl erimpl_deacby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_deacby_userid_fk FOREIGN KEY (deacby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5493 (class 2606 OID 23980)
-- Name: eventruleimpl erimpl_implby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_implby_userid_fk FOREIGN KEY (implby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5494 (class 2606 OID 23985)
-- Name: eventruleimpl erimpl_occperiodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_occperiodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5495 (class 2606 OID 23990)
-- Name: eventruleimpl erimpl_passoverrideby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_passoverrideby_userid_fk FOREIGN KEY (passoverrideby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5496 (class 2606 OID 23995)
-- Name: eventruleimpl erimpl_triggeredevent_eventid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_triggeredevent_eventid_fk FOREIGN KEY (triggeredevent_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 5497 (class 2606 OID 24000)
-- Name: eventruleimpl erimpl_waivedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventruleimpl
    ADD CONSTRAINT erimpl_waivedby_userid_fk FOREIGN KEY (waivedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5476 (class 2606 OID 24005)
-- Name: event event_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.event
    ADD CONSTRAINT event_createdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5477 (class 2606 OID 24010)
-- Name: eventcategory eventcategory_proposal_propid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventcategory
    ADD CONSTRAINT eventcategory_proposal_propid_fk FOREIGN KEY (directive_directiveid) REFERENCES public.choicedirective(directiveid);


--
-- TOC entry 5377 (class 2606 OID 24015)
-- Name: choice eventchoice_eventcatid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choice
    ADD CONSTRAINT eventchoice_eventcatid_fk FOREIGN KEY (eventcat_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 5376 (class 2606 OID 24020)
-- Name: choice eventchoice_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choice
    ADD CONSTRAINT eventchoice_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 5378 (class 2606 OID 24025)
-- Name: choicedirective eventchoice_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choicedirective
    ADD CONSTRAINT eventchoice_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 5375 (class 2606 OID 24030)
-- Name: choice eventchoice_ruleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choice
    ADD CONSTRAINT eventchoice_ruleid_fk FOREIGN KEY (eventrule_ruleid) REFERENCES public.eventrule(ruleid);


--
-- TOC entry 5479 (class 2606 OID 24035)
-- Name: eventhuman eventhuman_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5480 (class 2606 OID 24040)
-- Name: eventhuman eventhuman_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5481 (class 2606 OID 24045)
-- Name: eventhuman eventhuman_eventid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_eventid_fk FOREIGN KEY (event_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 5482 (class 2606 OID 24050)
-- Name: eventhuman eventhuman_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5483 (class 2606 OID 24055)
-- Name: eventhuman eventhuman_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 5484 (class 2606 OID 24060)
-- Name: eventhuman eventhuman_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT eventhuman_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5380 (class 2606 OID 24065)
-- Name: choicedirectivechoice eventpopchoice_choiceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choicedirectivechoice
    ADD CONSTRAINT eventpopchoice_choiceid_fk FOREIGN KEY (choice_choiceid) REFERENCES public.choice(choiceid);


--
-- TOC entry 5379 (class 2606 OID 24070)
-- Name: choicedirectivechoice eventpropchoice_proposalid; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.choicedirectivechoice
    ADD CONSTRAINT eventpropchoice_proposalid FOREIGN KEY (directive_directiveid) REFERENCES public.choicedirective(directiveid);


--
-- TOC entry 5486 (class 2606 OID 24075)
-- Name: eventrule eventrule_directive_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT eventrule_directive_id_fk FOREIGN KEY (promptingdirective_directiveid) REFERENCES public.choicedirective(directiveid);


--
-- TOC entry 5487 (class 2606 OID 24080)
-- Name: eventrule eventrule_forbiddeneventcatid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT eventrule_forbiddeneventcatid_fk FOREIGN KEY (forbiddeneventcat_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 5488 (class 2606 OID 24085)
-- Name: eventrule eventrule_requiredeventcatid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT eventrule_requiredeventcatid_fk FOREIGN KEY (requiredeventcat_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 5489 (class 2606 OID 24090)
-- Name: eventrule eventrule_triggfaileventcatid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT eventrule_triggfaileventcatid_fk FOREIGN KEY (triggeredeventcatonfail) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 5490 (class 2606 OID 24095)
-- Name: eventrule eventrule_triggpasseventcatid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT eventrule_triggpasseventcatid_fk FOREIGN KEY (triggeredeventcatonpass) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 5499 (class 2606 OID 24100)
-- Name: eventruleruleset evruleevruleset_ruleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventruleruleset
    ADD CONSTRAINT evruleevruleset_ruleid_fk FOREIGN KEY (eventrule_ruleid) REFERENCES public.eventrule(ruleid);


--
-- TOC entry 5498 (class 2606 OID 24105)
-- Name: eventruleruleset evruleevruleset_setid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventruleruleset
    ADD CONSTRAINT evruleevruleset_setid_fk FOREIGN KEY (ruleset_rulesetid) REFERENCES public.eventruleset(rulesetid);


--
-- TOC entry 5554 (class 2606 OID 24110)
-- Name: log genlog_user_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.log
    ADD CONSTRAINT genlog_user_userid_fk FOREIGN KEY (user_userid) REFERENCES public.login(userid);


--
-- TOC entry 5553 (class 2606 OID 24115)
-- Name: log genlogcategory_catid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.log
    ADD CONSTRAINT genlogcategory_catid_fk FOREIGN KEY (category) REFERENCES public.logcategory(catid);


--
-- TOC entry 5437 (class 2606 OID 24120)
-- Name: codeelement guideentryid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeelement
    ADD CONSTRAINT guideentryid_fk FOREIGN KEY (guideentryid) REFERENCES public.codeelementguide(guideentryid);


--
-- TOC entry 5568 (class 2606 OID 24125)
-- Name: loginobjecthistory hist_ceactionrequest_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_ceactionrequest_fk FOREIGN KEY (ceactionrequest_requestid) REFERENCES public.ceactionrequest(requestid);


--
-- TOC entry 5569 (class 2606 OID 24130)
-- Name: loginobjecthistory hist_cecase_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_cecase_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 5570 (class 2606 OID 24135)
-- Name: loginobjecthistory hist_event_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_event_fk FOREIGN KEY (ceevent_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 5571 (class 2606 OID 24140)
-- Name: loginobjecthistory hist_occapp_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_occapp_fk FOREIGN KEY (occapp_appid) REFERENCES public.occpermitapplication(applicationid);


--
-- TOC entry 5572 (class 2606 OID 24145)
-- Name: loginobjecthistory hist_person_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_person_fk FOREIGN KEY (person_personid) REFERENCES public.person(personid);


--
-- TOC entry 5573 (class 2606 OID 24150)
-- Name: loginobjecthistory hist_prop_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_prop_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 5574 (class 2606 OID 24155)
-- Name: loginobjecthistory hist_user_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT hist_user_fk FOREIGN KEY (login_userid) REFERENCES public.login(userid);


--
-- TOC entry 5500 (class 2606 OID 24160)
-- Name: human human_clone_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_clone_humanid_fk FOREIGN KEY (cloneof_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5501 (class 2606 OID 24165)
-- Name: human human_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5396 (class 2606 OID 24170)
-- Name: citation human_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT human_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5402 (class 2606 OID 24175)
-- Name: citationcitationstatus human_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT human_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5502 (class 2606 OID 24180)
-- Name: human human_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5397 (class 2606 OID 24185)
-- Name: citation human_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT human_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5403 (class 2606 OID 24190)
-- Name: citationcitationstatus human_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT human_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5503 (class 2606 OID 24195)
-- Name: human human_deceasedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_deceasedby_userid_fk FOREIGN KEY (deceasedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5504 (class 2606 OID 24200)
-- Name: human human_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5398 (class 2606 OID 24205)
-- Name: citation human_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citation
    ADD CONSTRAINT human_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5404 (class 2606 OID 24210)
-- Name: citationcitationstatus human_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationcitationstatus
    ADD CONSTRAINT human_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5680 (class 2606 OID 24215)
-- Name: occperiod human_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT human_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5505 (class 2606 OID 24220)
-- Name: human human_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.human
    ADD CONSTRAINT human_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5506 (class 2606 OID 24225)
-- Name: humancecase humancecase_caseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_caseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 5507 (class 2606 OID 24230)
-- Name: humancecase humancecase_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5508 (class 2606 OID 24235)
-- Name: humancecase humancecase_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5509 (class 2606 OID 24240)
-- Name: humancecase humancecase_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5510 (class 2606 OID 24245)
-- Name: humancecase humancecase_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5511 (class 2606 OID 24250)
-- Name: humancecase humancecase_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 5512 (class 2606 OID 24255)
-- Name: humancecase humancecase_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humancecase
    ADD CONSTRAINT humancecase_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5416 (class 2606 OID 24260)
-- Name: citationhuman humancitation_citationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_citationid_fk FOREIGN KEY (citation_citationid) REFERENCES public.citation(citationid);


--
-- TOC entry 5417 (class 2606 OID 24265)
-- Name: citationhuman humancitation_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5418 (class 2606 OID 24270)
-- Name: citationhuman humancitation_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5419 (class 2606 OID 24275)
-- Name: citationhuman humancitation_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5420 (class 2606 OID 24280)
-- Name: citationhuman humancitation_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5421 (class 2606 OID 24285)
-- Name: citationhuman humancitation_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.citationhuman
    ADD CONSTRAINT humancitation_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5513 (class 2606 OID 24290)
-- Name: humanmailingaddress humanmailing_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailing_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5514 (class 2606 OID 24295)
-- Name: humanmailingaddress humanmailing_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailing_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5515 (class 2606 OID 24300)
-- Name: humanmailingaddress humanmailing_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailing_humanid_fk FOREIGN KEY (humanmailing_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5516 (class 2606 OID 24305)
-- Name: humanmailingaddress humanmailing_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailing_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5517 (class 2606 OID 24310)
-- Name: humanmailingaddress humanmailing_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailing_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5518 (class 2606 OID 24315)
-- Name: humanmailingaddress humanmailingaddress_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmailingaddress
    ADD CONSTRAINT humanmailingaddress_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 5519 (class 2606 OID 24320)
-- Name: humanmuni humanmuni_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5526 (class 2606 OID 24325)
-- Name: humanoccperiod humanmuni_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanmuni_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5520 (class 2606 OID 24330)
-- Name: humanmuni humanmuni_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5527 (class 2606 OID 24335)
-- Name: humanoccperiod humanmuni_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanmuni_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5521 (class 2606 OID 24340)
-- Name: humanmuni humanmuni_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5528 (class 2606 OID 24345)
-- Name: humanoccperiod humanmuni_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanmuni_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5522 (class 2606 OID 24350)
-- Name: humanmuni humanmuni_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 5523 (class 2606 OID 24355)
-- Name: humanmuni humanmuni_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5524 (class 2606 OID 24360)
-- Name: humanmuni humanmuni_muniid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_muniid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5529 (class 2606 OID 24365)
-- Name: humanoccperiod humanmuni_muniid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanmuni_muniid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5525 (class 2606 OID 24370)
-- Name: humanmuni humanmuni_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanmuni
    ADD CONSTRAINT humanmuni_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5530 (class 2606 OID 24375)
-- Name: humanoccperiod humanoccperiod_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanoccperiod_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 5531 (class 2606 OID 24380)
-- Name: humanoccperiod humanoccperiod_periodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanoccperiod_periodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5532 (class 2606 OID 24385)
-- Name: humanoccperiod humanoccperiod_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanoccperiod
    ADD CONSTRAINT humanoccperiod_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5533 (class 2606 OID 24390)
-- Name: humanparcel humanparcel_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT humanparcel_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 5540 (class 2606 OID 24395)
-- Name: humanparcelunit humanparcelunit_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT humanparcelunit_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5541 (class 2606 OID 24400)
-- Name: humanparcelunit humanparcelunit_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT humanparcelunit_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5542 (class 2606 OID 24405)
-- Name: humanparcelunit humanparcelunit_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT humanparcelunit_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5543 (class 2606 OID 24410)
-- Name: humanparcelunit humanparcelunit_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT humanparcelunit_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 5544 (class 2606 OID 24415)
-- Name: humanparcelunit humanparcelunit_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT humanparcelunit_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5366 (class 2606 OID 24420)
-- Name: ceactionrequeststatus icon_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequeststatus
    ADD CONSTRAINT icon_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 5547 (class 2606 OID 24425)
-- Name: improvementstatus improvementstatus_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.improvementstatus
    ADD CONSTRAINT improvementstatus_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 5550 (class 2606 OID 24430)
-- Name: improvementsuggestion imptstatus_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.improvementsuggestion
    ADD CONSTRAINT imptstatus_fk FOREIGN KEY (statusid) REFERENCES public.improvementstatus(statusid);


--
-- TOC entry 5549 (class 2606 OID 24435)
-- Name: improvementsuggestion imptype_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.improvementsuggestion
    ADD CONSTRAINT imptype_fk FOREIGN KEY (improvementtypeid) REFERENCES public.improvementtype(typeid);


--
-- TOC entry 5440 (class 2606 OID 24440)
-- Name: codeelementinjectedvalue injectedvalue_codeset_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeelementinjectedvalue
    ADD CONSTRAINT injectedvalue_codeset_fk FOREIGN KEY (codeset_codesetid) REFERENCES public.codeset(codesetid);


--
-- TOC entry 5439 (class 2606 OID 24445)
-- Name: codeelementinjectedvalue injectedvalue_element_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeelementinjectedvalue
    ADD CONSTRAINT injectedvalue_element_fk FOREIGN KEY (codelement_eleid) REFERENCES public.codeelement(elementid);


--
-- TOC entry 5653 (class 2606 OID 24450)
-- Name: occinspectedspaceelement inspecchecklistspaceele_locdescid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT inspecchecklistspaceele_locdescid_fk FOREIGN KEY (locationdescription_id) REFERENCES public.occlocationdescriptor(locationdescriptionid);


--
-- TOC entry 5642 (class 2606 OID 24455)
-- Name: occchecklist inspectionchecklist_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occchecklist
    ADD CONSTRAINT inspectionchecklist_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5555 (class 2606 OID 24460)
-- Name: login login_createdby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_createdby_fk FOREIGN KEY (createdby) REFERENCES public.login(userid);


--
-- TOC entry 5556 (class 2606 OID 24465)
-- Name: login login_decatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_decatedby_userid_fk FOREIGN KEY (deactivated_userid) REFERENCES public.login(userid);


--
-- TOC entry 5557 (class 2606 OID 24470)
-- Name: login login_homemuni_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_homemuni_fk FOREIGN KEY (homemuni) REFERENCES public.municipality(municode);


--
-- TOC entry 5558 (class 2606 OID 24475)
-- Name: login login_personlink_personid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.login
    ADD CONSTRAINT login_personlink_personid_fk FOREIGN KEY (personlink) REFERENCES public.person(personid);


--
-- TOC entry 5563 (class 2606 OID 24480)
-- Name: loginmuniauthperiodlog logincredentialex_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginmuniauthperiodlog
    ADD CONSTRAINT logincredentialex_muni_fk FOREIGN KEY (audit_muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5564 (class 2606 OID 24485)
-- Name: loginmuniauthperiodlog logincredentialex_periodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginmuniauthperiodlog
    ADD CONSTRAINT logincredentialex_periodid_fk FOREIGN KEY (authperiod_periodid) REFERENCES public.loginmuniauthperiod(muniauthperiodid);


--
-- TOC entry 5565 (class 2606 OID 24490)
-- Name: loginmuniauthperiodlog logincredentialex_usercredentialid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginmuniauthperiodlog
    ADD CONSTRAINT logincredentialex_usercredentialid_fk FOREIGN KEY (audit_usercredential_userid) REFERENCES public.login(userid);


--
-- TOC entry 5566 (class 2606 OID 24495)
-- Name: loginmuniauthperiodlog logincredentialex_usersessionid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginmuniauthperiodlog
    ADD CONSTRAINT logincredentialex_usersessionid_fk FOREIGN KEY (audit_usersession_userid) REFERENCES public.login(userid);


--
-- TOC entry 5618 (class 2606 OID 24500)
-- Name: munilogin loginmuni_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.munilogin
    ADD CONSTRAINT loginmuni_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5617 (class 2606 OID 24505)
-- Name: munilogin loginmuni_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.munilogin
    ADD CONSTRAINT loginmuni_userid_fk FOREIGN KEY (userid) REFERENCES public.login(userid);


--
-- TOC entry 5559 (class 2606 OID 24510)
-- Name: loginmuniauthperiod loginmuniauthperiod_creator_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginmuniauthperiod
    ADD CONSTRAINT loginmuniauthperiod_creator_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5560 (class 2606 OID 24515)
-- Name: loginmuniauthperiod loginmuniauthperiod_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginmuniauthperiod
    ADD CONSTRAINT loginmuniauthperiod_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5561 (class 2606 OID 24520)
-- Name: loginmuniauthperiod loginmuniauthperiod_supportassignedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginmuniauthperiod
    ADD CONSTRAINT loginmuniauthperiod_supportassignedby_userid_fk FOREIGN KEY (supportassignedby) REFERENCES public.login(userid);


--
-- TOC entry 5562 (class 2606 OID 24525)
-- Name: loginmuniauthperiod loginmuniauthperiod_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginmuniauthperiod
    ADD CONSTRAINT loginmuniauthperiod_userid_fk FOREIGN KEY (authuser_userid) REFERENCES public.login(userid);


--
-- TOC entry 5567 (class 2606 OID 24530)
-- Name: loginmuniauthperiodlog loginmuniauthperiodsession_disputedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginmuniauthperiodlog
    ADD CONSTRAINT loginmuniauthperiodsession_disputedby_userid_fk FOREIGN KEY (disputedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5575 (class 2606 OID 24535)
-- Name: loginobjecthistory loginobjecthistory_occperiod_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.loginobjecthistory
    ADD CONSTRAINT loginobjecthistory_occperiod_id_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5576 (class 2606 OID 24540)
-- Name: mailingaddress mailingaddress_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.mailingaddress
    ADD CONSTRAINT mailingaddress_createdby_userid_fk FOREIGN KEY (verifiedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5577 (class 2606 OID 24545)
-- Name: mailingaddress mailingaddress_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.mailingaddress
    ADD CONSTRAINT mailingaddress_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5578 (class 2606 OID 24550)
-- Name: mailingaddress mailingaddress_streetid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.mailingaddress
    ADD CONSTRAINT mailingaddress_streetid_fk FOREIGN KEY (street_streetid) REFERENCES public.mailingstreet(streetid);


--
-- TOC entry 5579 (class 2606 OID 24555)
-- Name: mailingaddress mailingaddress_verifiedsourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.mailingaddress
    ADD CONSTRAINT mailingaddress_verifiedsourceid_fk FOREIGN KEY (verifiedsource_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5726 (class 2606 OID 24560)
-- Name: parcelmailingaddress mailingaddressparcel_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT mailingaddressparcel_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5727 (class 2606 OID 24565)
-- Name: parcelmailingaddress mailingaddressparcel_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT mailingaddressparcel_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5728 (class 2606 OID 24570)
-- Name: parcelmailingaddress mailingaddressparcel_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT mailingaddressparcel_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5729 (class 2606 OID 24575)
-- Name: parcelmailingaddress mailingparcel_parcelid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT mailingparcel_parcelid_fk FOREIGN KEY (mailingparcel_parcelid) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 5730 (class 2606 OID 24580)
-- Name: parcelmailingaddress mailingparcel_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT mailingparcel_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5580 (class 2606 OID 24585)
-- Name: mailingstreet mailingstreet_citystatezip_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.mailingstreet
    ADD CONSTRAINT mailingstreet_citystatezip_fk FOREIGN KEY (citystatezip_cszipid) REFERENCES public.mailingcitystatezip(id);


--
-- TOC entry 5581 (class 2606 OID 24590)
-- Name: moneycecasefeeassigned moneycecasefeeassigned_assignedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneycecasefeeassigned
    ADD CONSTRAINT moneycecasefeeassigned_assignedby_fk FOREIGN KEY (assignedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5582 (class 2606 OID 24595)
-- Name: moneycecasefeeassigned moneycecasefeeassigned_caseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneycecasefeeassigned
    ADD CONSTRAINT moneycecasefeeassigned_caseid_fk FOREIGN KEY (cecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 5583 (class 2606 OID 24600)
-- Name: moneycecasefeeassigned moneycecasefeeassigned_feeid_occtypeid_comp_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneycecasefeeassigned
    ADD CONSTRAINT moneycecasefeeassigned_feeid_occtypeid_comp_fk FOREIGN KEY (fee_feeid, codesetelement_elementid) REFERENCES public.moneycodesetelementfee(fee_feeid, codesetelement_elementid);


--
-- TOC entry 5584 (class 2606 OID 24605)
-- Name: moneycecasefeeassigned moneycecasefeeassigned_reducedbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneycecasefeeassigned
    ADD CONSTRAINT moneycecasefeeassigned_reducedbyuserid_fk FOREIGN KEY (reduceby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5585 (class 2606 OID 24610)
-- Name: moneycecasefeeassigned moneycecasefeeassigned_wavedbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneycecasefeeassigned
    ADD CONSTRAINT moneycecasefeeassigned_wavedbyuserid_fk FOREIGN KEY (waivedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5587 (class 2606 OID 24615)
-- Name: moneycecasefeepayment moneycecasefeepayment_occperassignedfee_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneycecasefeepayment
    ADD CONSTRAINT moneycecasefeepayment_occperassignedfee_fk FOREIGN KEY (cecaseassignedfee_id) REFERENCES public.moneycecasefeeassigned(cecaseassignedfeeid);


--
-- TOC entry 5586 (class 2606 OID 24620)
-- Name: moneycecasefeepayment moneycecasefeepayment_paymentid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneycecasefeepayment
    ADD CONSTRAINT moneycecasefeepayment_paymentid_fk FOREIGN KEY (payment_paymentid) REFERENCES public.moneypayment(paymentid);


--
-- TOC entry 5589 (class 2606 OID 24625)
-- Name: moneycodesetelementfee moneycodesetelefee_cdseteleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneycodesetelementfee
    ADD CONSTRAINT moneycodesetelefee_cdseteleid_fk FOREIGN KEY (codesetelement_elementid) REFERENCES public.codesetelement(codesetelementid);


--
-- TOC entry 5588 (class 2606 OID 24630)
-- Name: moneycodesetelementfee moneycodesetelefee_feeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneycodesetelementfee
    ADD CONSTRAINT moneycodesetelefee_feeid_fk FOREIGN KEY (fee_feeid) REFERENCES public.moneyfee(feeid);


--
-- TOC entry 5591 (class 2606 OID 24635)
-- Name: moneyoccperiodfeeassigned moneyoccperiodfeeassigned; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyoccperiodfeeassigned
    ADD CONSTRAINT moneyoccperiodfeeassigned FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5592 (class 2606 OID 24640)
-- Name: moneyoccperiodfeeassigned moneyoccperiodfeeassigned_assignedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyoccperiodfeeassigned
    ADD CONSTRAINT moneyoccperiodfeeassigned_assignedby_fk FOREIGN KEY (assignedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5593 (class 2606 OID 24645)
-- Name: moneyoccperiodfeeassigned moneyoccperiodfeeassigned_feeid_occtypeid_comp_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyoccperiodfeeassigned
    ADD CONSTRAINT moneyoccperiodfeeassigned_feeid_occtypeid_comp_fk FOREIGN KEY (fee_feeid, occperiodtype_typeid) REFERENCES public.moneyoccperiodtypefee(fee_feeid, occperiodtype_typeid);


--
-- TOC entry 5594 (class 2606 OID 24650)
-- Name: moneyoccperiodfeeassigned moneyoccperiodfeeassigned_reducedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyoccperiodfeeassigned
    ADD CONSTRAINT moneyoccperiodfeeassigned_reducedby_fk FOREIGN KEY (reduceby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5595 (class 2606 OID 24655)
-- Name: moneyoccperiodfeeassigned moneyoccperiodfeeassigned_waivedbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyoccperiodfeeassigned
    ADD CONSTRAINT moneyoccperiodfeeassigned_waivedbyuserid_fk FOREIGN KEY (waivedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5597 (class 2606 OID 24660)
-- Name: moneyoccperiodfeepayment moneyoccperiodfeepayment_paymentid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyoccperiodfeepayment
    ADD CONSTRAINT moneyoccperiodfeepayment_paymentid_fk FOREIGN KEY (payment_paymentid) REFERENCES public.moneypayment(paymentid);


--
-- TOC entry 5599 (class 2606 OID 24665)
-- Name: moneyoccperiodtypefee moneyoccperiodtypefee_feeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyoccperiodtypefee
    ADD CONSTRAINT moneyoccperiodtypefee_feeid_fk FOREIGN KEY (fee_feeid) REFERENCES public.moneyfee(feeid);


--
-- TOC entry 5598 (class 2606 OID 24670)
-- Name: moneyoccperiodtypefee moneyoccperiodtypefee_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyoccperiodtypefee
    ADD CONSTRAINT moneyoccperiodtypefee_typeid_fk FOREIGN KEY (occperiodtype_typeid) REFERENCES public.occperiodtype(typeid);


--
-- TOC entry 5596 (class 2606 OID 24675)
-- Name: moneyoccperiodfeepayment moneyoccpermittypefeepayment_occperassignedfee_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyoccperiodfeepayment
    ADD CONSTRAINT moneyoccpermittypefeepayment_occperassignedfee_fk FOREIGN KEY (occperiodassignedfee_id) REFERENCES public.moneyoccperiodfeeassigned(moneyoccperassignedfeeid);


--
-- TOC entry 5603 (class 2606 OID 24680)
-- Name: municipality muni_defaultcodeset_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_defaultcodeset_fk FOREIGN KEY (defaultcodeset) REFERENCES public.codeset(codesetid);


--
-- TOC entry 5604 (class 2606 OID 24685)
-- Name: municipality muni_defoccperiod_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_defoccperiod_fk FOREIGN KEY (defaultoccperiod) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5605 (class 2606 OID 24690)
-- Name: municipality muni_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_lastupdatedby_userid_fk FOREIGN KEY (lastupdated_userid) REFERENCES public.login(userid);


--
-- TOC entry 5606 (class 2606 OID 24695)
-- Name: municipality muni_manageruserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_manageruserid_fk FOREIGN KEY (munimanager_userid) REFERENCES public.login(userid);


--
-- TOC entry 5590 (class 2606 OID 24700)
-- Name: moneyfee muni_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneyfee
    ADD CONSTRAINT muni_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5792 (class 2606 OID 24705)
-- Name: textblock muni_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.textblock
    ADD CONSTRAINT muni_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5607 (class 2606 OID 24710)
-- Name: municipality muni_munipropid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_munipropid_fk FOREIGN KEY (office_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 5608 (class 2606 OID 24715)
-- Name: municipality muni_occpermitissuingcodesource_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_occpermitissuingcodesource_sourceid_fk FOREIGN KEY (occpermitissuingsource_sourceid) REFERENCES public.codesource(sourceid);


--
-- TOC entry 5609 (class 2606 OID 24720)
-- Name: municipality muni_printstyleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_printstyleid_fk FOREIGN KEY (novprintstyle_styleid) REFERENCES public.printstyle(styleid);


--
-- TOC entry 5610 (class 2606 OID 24725)
-- Name: municipality muni_profileid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_profileid_fk FOREIGN KEY (profile_profileid) REFERENCES public.muniprofile(profileid);


--
-- TOC entry 5611 (class 2606 OID 24730)
-- Name: municipality muni_staffcontact_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municipality
    ADD CONSTRAINT muni_staffcontact_userid_fk FOREIGN KEY (primarystaffcontact_userid) REFERENCES public.login(userid);


--
-- TOC entry 5340 (class 2606 OID 24735)
-- Name: person municipality_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT municipality_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5613 (class 2606 OID 24740)
-- Name: municitystatezip municitystatezip_cszip_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municitystatezip
    ADD CONSTRAINT municitystatezip_cszip_fk FOREIGN KEY (citystatezip_id) REFERENCES public.mailingcitystatezip(id);


--
-- TOC entry 5612 (class 2606 OID 24745)
-- Name: municitystatezip municitystatezip_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municitystatezip
    ADD CONSTRAINT municitystatezip_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5615 (class 2606 OID 24750)
-- Name: municourtentity municourtentity_courtid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municourtentity
    ADD CONSTRAINT municourtentity_courtid_fk FOREIGN KEY (courtentity_entityid) REFERENCES public.courtentity(entityid);


--
-- TOC entry 5614 (class 2606 OID 24755)
-- Name: municourtentity municourtentity_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.municourtentity
    ADD CONSTRAINT municourtentity_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5616 (class 2606 OID 24760)
-- Name: munilogin munilogin_defcaseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.munilogin
    ADD CONSTRAINT munilogin_defcaseid_fk FOREIGN KEY (defaultcecase_caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 5620 (class 2606 OID 24765)
-- Name: munipdfdoc munipdfdoc_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.munipdfdoc
    ADD CONSTRAINT munipdfdoc_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5619 (class 2606 OID 24770)
-- Name: munipdfdoc munipdfdoc_pdid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.munipdfdoc
    ADD CONSTRAINT munipdfdoc_pdid_fk FOREIGN KEY (pdfdoc_pdfdocid) REFERENCES public.pdfdoc(pdfdocid);


--
-- TOC entry 5622 (class 2606 OID 24775)
-- Name: muniphotodoc muniphotodoc_muni_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.muniphotodoc
    ADD CONSTRAINT muniphotodoc_muni_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5621 (class 2606 OID 24780)
-- Name: muniphotodoc muniphotodoc_pdid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.muniphotodoc
    ADD CONSTRAINT muniphotodoc_pdid_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 5623 (class 2606 OID 24785)
-- Name: muniprofile muniprofile_lastupdateduserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.muniprofile
    ADD CONSTRAINT muniprofile_lastupdateduserid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5625 (class 2606 OID 24790)
-- Name: muniprofileeventruleset muniprofileeventruleset_profileid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.muniprofileeventruleset
    ADD CONSTRAINT muniprofileeventruleset_profileid_fk FOREIGN KEY (muniprofile_profileid) REFERENCES public.muniprofile(profileid);


--
-- TOC entry 5624 (class 2606 OID 24795)
-- Name: muniprofileeventruleset muniprofileeventruleset_setid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.muniprofileeventruleset
    ADD CONSTRAINT muniprofileeventruleset_setid_fk FOREIGN KEY (ruleset_setid) REFERENCES public.eventruleset(rulesetid);


--
-- TOC entry 5627 (class 2606 OID 24800)
-- Name: muniprofileoccperiodtype muniprofileoccperiodtype_profileid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.muniprofileoccperiodtype
    ADD CONSTRAINT muniprofileoccperiodtype_profileid_fk FOREIGN KEY (muniprofile_profileid) REFERENCES public.muniprofile(profileid);


--
-- TOC entry 5626 (class 2606 OID 24805)
-- Name: muniprofileoccperiodtype muniprofileoccperiodtype_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.muniprofileoccperiodtype
    ADD CONSTRAINT muniprofileoccperiodtype_typeid_fk FOREIGN KEY (occperiodtype_typeid) REFERENCES public.occperiodtype(typeid);


--
-- TOC entry 5628 (class 2606 OID 24810)
-- Name: noticeofviolation noticeOfViolation_recipient_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT "noticeOfViolation_recipient_fk" FOREIGN KEY (personid_recipient) REFERENCES public.person(personid);


--
-- TOC entry 5639 (class 2606 OID 24815)
-- Name: noticeofviolationcodeviolation noticeofviolation_noticeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolationcodeviolation
    ADD CONSTRAINT noticeofviolation_noticeid_fk FOREIGN KEY (noticeofviolation_noticeid) REFERENCES public.noticeofviolation(noticeid);


--
-- TOC entry 5629 (class 2606 OID 24820)
-- Name: noticeofviolation noticeofviolationcaseid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT noticeofviolationcaseid_fk FOREIGN KEY (caseid) REFERENCES public.cecase(caseid);


--
-- TOC entry 5630 (class 2606 OID 24825)
-- Name: noticeofviolation nov_creationby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_creationby_userid_fk FOREIGN KEY (creationby) REFERENCES public.login(userid);


--
-- TOC entry 5631 (class 2606 OID 24830)
-- Name: noticeofviolation nov_followup_eventid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_followup_eventid_fk FOREIGN KEY (followupevent_eventid) REFERENCES public.event(eventid);


--
-- TOC entry 5632 (class 2606 OID 24835)
-- Name: noticeofviolation nov_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_humanid_fk FOREIGN KEY (recipient_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5633 (class 2606 OID 24840)
-- Name: noticeofviolation nov_lockedandqueuedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_lockedandqueuedby_userid_fk FOREIGN KEY (lockedandqueuedformailingby) REFERENCES public.login(userid);


--
-- TOC entry 5634 (class 2606 OID 24845)
-- Name: noticeofviolation nov_mailing_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_mailing_fk FOREIGN KEY (recipient_mailing) REFERENCES public.humanmailingaddress(linkid);


--
-- TOC entry 5635 (class 2606 OID 24850)
-- Name: noticeofviolation nov_notifyingofficer_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_notifyingofficer_fk FOREIGN KEY (notifyingofficer_userid) REFERENCES public.login(userid);


--
-- TOC entry 5636 (class 2606 OID 24855)
-- Name: noticeofviolation nov_printstyleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_printstyleid_fk FOREIGN KEY (printstyle_styleid) REFERENCES public.printstyle(styleid);


--
-- TOC entry 5637 (class 2606 OID 24860)
-- Name: noticeofviolation nov_returnedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_returnedby_fk FOREIGN KEY (lockedandqueuedformailingby) REFERENCES public.login(userid);


--
-- TOC entry 5638 (class 2606 OID 24865)
-- Name: noticeofviolation nov_sentby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.noticeofviolation
    ADD CONSTRAINT nov_sentby_fk FOREIGN KEY (sentby) REFERENCES public.login(userid);


--
-- TOC entry 5644 (class 2606 OID 24870)
-- Name: occchecklistphotorequirement occchecklistphotorequirement_checklist_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occchecklistphotorequirement
    ADD CONSTRAINT occchecklistphotorequirement_checklist_fk FOREIGN KEY (occphotorequirement_reqid) REFERENCES public.occchecklist(checklistid);


--
-- TOC entry 5643 (class 2606 OID 24875)
-- Name: occchecklistphotorequirement occchecklistphotorequirement_requirement_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occchecklistphotorequirement
    ADD CONSTRAINT occchecklistphotorequirement_requirement_fk FOREIGN KEY (occchecklist_checklistid) REFERENCES public.occphotorequirement(requirementid);


--
-- TOC entry 5645 (class 2606 OID 24880)
-- Name: occchecklistspacetype occchecklistspacetype_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occchecklistspacetype
    ADD CONSTRAINT occchecklistspacetype_typeid_fk FOREIGN KEY (spacetype_typeid) REFERENCES public.occspacetype(spacetypeid);


--
-- TOC entry 5647 (class 2606 OID 24885)
-- Name: occchecklistspacetypeelement occchecklistspacetypeelement_checklistspacetype_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occchecklistspacetypeelement
    ADD CONSTRAINT occchecklistspacetypeelement_checklistspacetype_typeid_fk FOREIGN KEY (checklistspacetype_typeid) REFERENCES public.occchecklistspacetype(checklistspacetypeid);


--
-- TOC entry 5662 (class 2606 OID 24890)
-- Name: occinspection occinpection_determinationby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinpection_determinationby_fk FOREIGN KEY (determinationby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5677 (class 2606 OID 24895)
-- Name: occinspectionphotodoc occinpectionphotodoc_requirement_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectionphotodoc
    ADD CONSTRAINT occinpectionphotodoc_requirement_fk FOREIGN KEY (photorequirement_requirementid) REFERENCES public.occphotorequirement(requirementid);


--
-- TOC entry 5641 (class 2606 OID 24900)
-- Name: occchecklist occinspecchecklist_codesourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occchecklist
    ADD CONSTRAINT occinspecchecklist_codesourceid_fk FOREIGN KEY (governingcodesource_sourceid) REFERENCES public.codesource(sourceid);


--
-- TOC entry 5654 (class 2606 OID 24905)
-- Name: occinspectedspaceelement occinspectedchklstspel_complianceby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedchklstspel_complianceby_userid_fk FOREIGN KEY (compliancegrantedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5655 (class 2606 OID 24910)
-- Name: occinspectedspaceelement occinspectedchklstspel_lastinspecby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedchklstspel_lastinspecby_userid_fk FOREIGN KEY (lastinspectedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5649 (class 2606 OID 24915)
-- Name: occinspectedspace occinspectedspace_chklstspctypid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspace
    ADD CONSTRAINT occinspectedspace_chklstspctypid_fk FOREIGN KEY (occchecklistspacetype_chklstspctypid) REFERENCES public.occchecklistspacetype(checklistspacetypeid);


--
-- TOC entry 5650 (class 2606 OID 24920)
-- Name: occinspectedspace occinspectedspace_inspecid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspace
    ADD CONSTRAINT occinspectedspace_inspecid_fk FOREIGN KEY (occinspection_inspectionid) REFERENCES public.occinspection(inspectionid);


--
-- TOC entry 5651 (class 2606 OID 24925)
-- Name: occinspectedspace occinspectedspace_lastbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspace
    ADD CONSTRAINT occinspectedspace_lastbyuserid_fk FOREIGN KEY (addedtochecklistby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5652 (class 2606 OID 24930)
-- Name: occinspectedspace occinspectedspace_locid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspace
    ADD CONSTRAINT occinspectedspace_locid_fk FOREIGN KEY (occlocationdescription_descid) REFERENCES public.occlocationdescriptor(locationdescriptionid);


--
-- TOC entry 5656 (class 2606 OID 24935)
-- Name: occinspectedspaceelement occinspectedspaceele_intenclassid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedspaceele_intenclassid_fk FOREIGN KEY (failureseverity_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 5657 (class 2606 OID 24940)
-- Name: occinspectedspaceelement occinspectedspaceelement_elementid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedspaceelement_elementid_fk FOREIGN KEY (occchecklistspacetypeelement_elementid) REFERENCES public.occchecklistspacetypeelement(spaceelementid);


--
-- TOC entry 5658 (class 2606 OID 24945)
-- Name: occinspectedspaceelement occinspectedspaceelement_overridereq_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedspaceelement_overridereq_userid_fk FOREIGN KEY (overriderequiredflagnotinspected_userid) REFERENCES public.login(userid);


--
-- TOC entry 5659 (class 2606 OID 24950)
-- Name: occinspectedspaceelement occinspectedspaceelement_spaceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspaceelement
    ADD CONSTRAINT occinspectedspaceelement_spaceid_fk FOREIGN KEY (inspectedspace_inspectedspaceid) REFERENCES public.occinspectedspace(inspectedspaceid);


--
-- TOC entry 5661 (class 2606 OID 24955)
-- Name: occinspectedspaceelementphotodoc occinspectedspaceelementphotodoc_inspectedele_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspaceelementphotodoc
    ADD CONSTRAINT occinspectedspaceelementphotodoc_inspectedele_fk FOREIGN KEY (inspectedspaceelement_elementid) REFERENCES public.occinspectedspaceelement(inspectedspaceelementid);


--
-- TOC entry 5660 (class 2606 OID 24960)
-- Name: occinspectedspaceelementphotodoc occinspectedspaceelementphotodoc_photodocid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectedspaceelementphotodoc
    ADD CONSTRAINT occinspectedspaceelementphotodoc_photodocid_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 5663 (class 2606 OID 24965)
-- Name: occinspection occinspection_cause_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_cause_fk FOREIGN KEY (cause_causeid) REFERENCES public.occinspectioncause(causeid);


--
-- TOC entry 5664 (class 2606 OID 24970)
-- Name: occinspection occinspection_checklistid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_checklistid_fk FOREIGN KEY (occchecklist_checklistlistid) REFERENCES public.occchecklist(checklistid);


--
-- TOC entry 5665 (class 2606 OID 24975)
-- Name: occinspection occinspection_creationby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_creationby_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5666 (class 2606 OID 24980)
-- Name: occinspection occinspection_deactivatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_deactivatedby_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5667 (class 2606 OID 24985)
-- Name: occinspection occinspection_determination_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_determination_fk FOREIGN KEY (determination_detid) REFERENCES public.occinspectiondetermination(determinationid);


--
-- TOC entry 5668 (class 2606 OID 24990)
-- Name: occinspection occinspection_followupto_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_followupto_fk FOREIGN KEY (followupto_inspectionid) REFERENCES public.occinspection(inspectionid);


--
-- TOC entry 5669 (class 2606 OID 24995)
-- Name: occinspection occinspection_inspector_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_inspector_userid_fk FOREIGN KEY (inspector_userid) REFERENCES public.login(userid);


--
-- TOC entry 5670 (class 2606 OID 25000)
-- Name: occinspection occinspection_lastupdatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_lastupdatedby_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5671 (class 2606 OID 25005)
-- Name: occinspection occinspection_periodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_periodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5672 (class 2606 OID 25010)
-- Name: occinspection occinspection_thirdpartyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspection_thirdpartyuserid_fk FOREIGN KEY (thirdpartyinspector_personid) REFERENCES public.person(personid);


--
-- TOC entry 5674 (class 2606 OID 25015)
-- Name: occinspectiondetermination occinspectiondetermination_eventcat_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectiondetermination
    ADD CONSTRAINT occinspectiondetermination_eventcat_fk FOREIGN KEY (eventcat_catid) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 5676 (class 2606 OID 25020)
-- Name: occinspectionphotodoc occinspectionphotodoc_inspection_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectionphotodoc
    ADD CONSTRAINT occinspectionphotodoc_inspection_fk FOREIGN KEY (inspection_inspectionid) REFERENCES public.occinspection(inspectionid);


--
-- TOC entry 5675 (class 2606 OID 25025)
-- Name: occinspectionphotodoc occinspectionphotodoc_phtodoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectionphotodoc
    ADD CONSTRAINT occinspectionphotodoc_phtodoc_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 5679 (class 2606 OID 25030)
-- Name: occinspectionpropertystatus occinspectionpropertystatus_inspection_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectionpropertystatus
    ADD CONSTRAINT occinspectionpropertystatus_inspection_fk FOREIGN KEY (occinspection_inspectionid) REFERENCES public.occinspection(inspectionid);


--
-- TOC entry 5678 (class 2606 OID 25035)
-- Name: occinspectionpropertystatus occinspectionpropertystatus_status_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspectionpropertystatus
    ADD CONSTRAINT occinspectionpropertystatus_status_fk FOREIGN KEY (propertystatus_statusid) REFERENCES public.propertystatus(statusid);


--
-- TOC entry 5673 (class 2606 OID 25040)
-- Name: occinspection occinspectionthirdpartyapprovalby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occinspection
    ADD CONSTRAINT occinspectionthirdpartyapprovalby_fk FOREIGN KEY (thirdpartyinspectorapprovalby) REFERENCES public.login(userid);


--
-- TOC entry 5681 (class 2606 OID 25045)
-- Name: occperiod occperiod_authby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_authby_userid_fk FOREIGN KEY (authorizedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5682 (class 2606 OID 25050)
-- Name: occperiod occperiod_createdbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_createdbyuserid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5683 (class 2606 OID 25055)
-- Name: occperiod occperiod_endcert_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_endcert_userid_fk FOREIGN KEY (enddatecertifiedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5684 (class 2606 OID 25060)
-- Name: occperiod occperiod_mngr_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_mngr_userid_fk FOREIGN KEY (manager_userid) REFERENCES public.login(userid);


--
-- TOC entry 5685 (class 2606 OID 25065)
-- Name: occperiod occperiod_periodtypid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_periodtypid_fk FOREIGN KEY (type_typeid) REFERENCES public.occperiodtype(typeid);


--
-- TOC entry 5686 (class 2606 OID 25070)
-- Name: occperiod occperiod_propunit_unitid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_propunit_unitid_fk FOREIGN KEY (propertyunit_unitid) REFERENCES public.propertyunit(unitid);


--
-- TOC entry 5687 (class 2606 OID 25075)
-- Name: occperiod occperiod_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5688 (class 2606 OID 25080)
-- Name: occperiod occperiod_startcert_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_startcert_userid_fk FOREIGN KEY (startdatecertifiedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5704 (class 2606 OID 25085)
-- Name: occpermit occperiod_startcert_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occperiod_startcert_userid_fk FOREIGN KEY (issuedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5689 (class 2606 OID 25090)
-- Name: occperiod occperiod_typecert_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiod
    ADD CONSTRAINT occperiod_typecert_userid_fk FOREIGN KEY (typecertifiedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5692 (class 2606 OID 25095)
-- Name: occperiodeventrule occperiodeventrule2_attachedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodeventrule
    ADD CONSTRAINT occperiodeventrule2_attachedby_userid_fk FOREIGN KEY (attachedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5691 (class 2606 OID 25100)
-- Name: occperiodeventrule occperiodeventrule_op_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodeventrule
    ADD CONSTRAINT occperiodeventrule_op_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5690 (class 2606 OID 25105)
-- Name: occperiodeventrule occperiodeventrule_ruleid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodeventrule
    ADD CONSTRAINT occperiodeventrule_ruleid_fk FOREIGN KEY (eventrule_ruleid) REFERENCES public.eventrule(ruleid);


--
-- TOC entry 5694 (class 2606 OID 25110)
-- Name: occperiodpdfdoc occperiodpdfdoc__occperiod_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodpdfdoc
    ADD CONSTRAINT occperiodpdfdoc__occperiod_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5693 (class 2606 OID 25115)
-- Name: occperiodpdfdoc occperiodpdfdoc_pdfdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodpdfdoc
    ADD CONSTRAINT occperiodpdfdoc_pdfdoc_fk FOREIGN KEY (pdfdoc_pdfdocid) REFERENCES public.pdfdoc(pdfdocid);


--
-- TOC entry 5696 (class 2606 OID 25120)
-- Name: occperiodpermitapplication occperiodpermitapp_appid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodpermitapplication
    ADD CONSTRAINT occperiodpermitapp_appid_fk FOREIGN KEY (occpermitapp_applicationid) REFERENCES public.occpermitapplication(applicationid);


--
-- TOC entry 5695 (class 2606 OID 25125)
-- Name: occperiodpermitapplication occperiodpermitapp_periodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodpermitapplication
    ADD CONSTRAINT occperiodpermitapp_periodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5698 (class 2606 OID 25130)
-- Name: occperiodphotodoc occperiodphotodoc__occperiod_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodphotodoc
    ADD CONSTRAINT occperiodphotodoc__occperiod_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5697 (class 2606 OID 25135)
-- Name: occperiodphotodoc occperiodphotodoc_phdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodphotodoc
    ADD CONSTRAINT occperiodphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 5701 (class 2606 OID 25140)
-- Name: occperiodtype occperiodtype_checklistid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodtype
    ADD CONSTRAINT occperiodtype_checklistid_fk FOREIGN KEY (occchecklist_checklistlistid) REFERENCES public.occchecklist(checklistid);


--
-- TOC entry 5700 (class 2606 OID 25145)
-- Name: occperiodtype occperiodtype_eventrulesetid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodtype
    ADD CONSTRAINT occperiodtype_eventrulesetid_fk FOREIGN KEY (eventruleset_setid) REFERENCES public.eventruleset(rulesetid);


--
-- TOC entry 5699 (class 2606 OID 25150)
-- Name: occperiodtype occperiodtype_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occperiodtype
    ADD CONSTRAINT occperiodtype_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5703 (class 2606 OID 25155)
-- Name: occpermit occpermit_issuedto_personid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occpermit_issuedto_personid_fk FOREIGN KEY (issuedto_personid) REFERENCES public.person(personid);


--
-- TOC entry 5702 (class 2606 OID 25160)
-- Name: occpermit occpermit_periodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occpermit
    ADD CONSTRAINT occpermit_periodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5706 (class 2606 OID 25165)
-- Name: occpermitapplication occpermitapp_periodid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occpermitapplication
    ADD CONSTRAINT occpermitapp_periodid_fk FOREIGN KEY (occperiod_periodid) REFERENCES public.occperiod(periodid);


--
-- TOC entry 5708 (class 2606 OID 25170)
-- Name: occpermitapplicationhuman occpermitapplicationhuman_applicationid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occpermitapplicationhuman
    ADD CONSTRAINT occpermitapplicationhuman_applicationid_fk FOREIGN KEY (occpermitapplication_applicationid) REFERENCES public.occpermitapplication(applicationid);


--
-- TOC entry 5707 (class 2606 OID 25175)
-- Name: occpermitapplicationhuman occpermitapplicationhuman_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occpermitapplicationhuman
    ADD CONSTRAINT occpermitapplicationhuman_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5709 (class 2606 OID 25180)
-- Name: occpermitapplicationreason occpermitapprsn_pertype_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occpermitapplicationreason
    ADD CONSTRAINT occpermitapprsn_pertype_fk FOREIGN KEY (periodtypeproposal_periodid) REFERENCES public.occperiodtype(typeid);


--
-- TOC entry 5715 (class 2606 OID 25185)
-- Name: parcelinfo parcel_abandoned_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_abandoned_userid_fk FOREIGN KEY (abandonedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5716 (class 2606 OID 25190)
-- Name: parcelinfo parcel_bobsourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_bobsourceid_fk FOREIGN KEY (bobsource_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5717 (class 2606 OID 25195)
-- Name: parcelinfo parcel_conditionintensityclass_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_conditionintensityclass_classid_fk FOREIGN KEY (condition_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 5710 (class 2606 OID 25200)
-- Name: parcel parcel_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5711 (class 2606 OID 25205)
-- Name: parcel parcel_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5718 (class 2606 OID 25210)
-- Name: parcelinfo parcel_landbankprospect_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_landbankprospect_classid_fk FOREIGN KEY (landbankprospect_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 5712 (class 2606 OID 25215)
-- Name: parcel parcel_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5713 (class 2606 OID 25220)
-- Name: parcel parcel_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5534 (class 2606 OID 25225)
-- Name: humanparcel parcel_parcelkey_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT parcel_parcelkey_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 5719 (class 2606 OID 25230)
-- Name: parcelinfo parcel_parcelusetypeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_parcelusetypeid_fk FOREIGN KEY (usetype_typeid) REFERENCES public.propertyusetype(propertyusetypeid);


--
-- TOC entry 5714 (class 2606 OID 25235)
-- Name: parcel parcel_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcel
    ADD CONSTRAINT parcel_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5535 (class 2606 OID 25240)
-- Name: humanparcel parcel_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT parcel_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5720 (class 2606 OID 25245)
-- Name: parcelinfo parcel_unfitby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_unfitby_userid_fk FOREIGN KEY (unfitby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5721 (class 2606 OID 25250)
-- Name: parcelinfo parcel_vacant_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcel_vacant_userid_fk FOREIGN KEY (vacantby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5536 (class 2606 OID 25255)
-- Name: humanparcel parcelhuman_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT parcelhuman_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5537 (class 2606 OID 25260)
-- Name: humanparcel parcelhuman_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT parcelhuman_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5538 (class 2606 OID 25265)
-- Name: humanparcel parcelhuman_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT parcelhuman_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5539 (class 2606 OID 25270)
-- Name: humanparcel parcelhuman_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcel
    ADD CONSTRAINT parcelhuman_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5722 (class 2606 OID 25275)
-- Name: parcelinfo parcelinfo_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5723 (class 2606 OID 25280)
-- Name: parcelinfo parcelinfo_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5724 (class 2606 OID 25285)
-- Name: parcelinfo parcelinfo_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5725 (class 2606 OID 25290)
-- Name: parcelinfo parcelinfo_parcelkey_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelinfo
    ADD CONSTRAINT parcelinfo_parcelkey_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 5731 (class 2606 OID 25295)
-- Name: parcelmailingaddress parcelmailing_lorid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelmailingaddress
    ADD CONSTRAINT parcelmailing_lorid_fk FOREIGN KEY (linkedobjectrole_lorid) REFERENCES public.linkedobjectrole(lorid);


--
-- TOC entry 5734 (class 2606 OID 25300)
-- Name: parcelmigrationlog parcelmigration_errorcode; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelmigrationlog
    ADD CONSTRAINT parcelmigration_errorcode FOREIGN KEY (error_code) REFERENCES public.parcelmigrationlogerrorcode(code);


--
-- TOC entry 5733 (class 2606 OID 25305)
-- Name: parcelmigrationlog parcelmigrationlog_parcelid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelmigrationlog
    ADD CONSTRAINT parcelmigrationlog_parcelid_fk FOREIGN KEY (parcel_id) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 5732 (class 2606 OID 25310)
-- Name: parcelmigrationlog parcelmigrationlog_propid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelmigrationlog
    ADD CONSTRAINT parcelmigrationlog_propid_fk FOREIGN KEY (property_id) REFERENCES public.property(propertyid);


--
-- TOC entry 5736 (class 2606 OID 25315)
-- Name: parcelpdfdoc parcelpdfdoc_cv_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelpdfdoc
    ADD CONSTRAINT parcelpdfdoc_cv_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 5735 (class 2606 OID 25320)
-- Name: parcelpdfdoc parcelpdfdoc_phdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelpdfdoc
    ADD CONSTRAINT parcelpdfdoc_phdoc_fk FOREIGN KEY (pdfdoc_pdfdocid) REFERENCES public.pdfdoc(pdfdocid);


--
-- TOC entry 5738 (class 2606 OID 25325)
-- Name: parcelphotodoc parcelphotodoc_cv_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelphotodoc
    ADD CONSTRAINT parcelphotodoc_cv_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 5737 (class 2606 OID 25330)
-- Name: parcelphotodoc parcelphotodoc_phdoc_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelphotodoc
    ADD CONSTRAINT parcelphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 5739 (class 2606 OID 25335)
-- Name: parcelunit parcelunit_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5740 (class 2606 OID 25340)
-- Name: parcelunit parcelunit_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5741 (class 2606 OID 25345)
-- Name: parcelunit parcelunit_lastupdatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5742 (class 2606 OID 25350)
-- Name: parcelunit parcelunit_loc_locationdescriptionid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_loc_locationdescriptionid_fk FOREIGN KEY (location_occlocationdescriptor) REFERENCES public.occlocationdescriptor(locationdescriptionid);


--
-- TOC entry 5743 (class 2606 OID 25355)
-- Name: parcelunit parcelunit_parcelkey_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_parcelkey_fk FOREIGN KEY (parcel_parcelkey) REFERENCES public.parcel(parcelkey);


--
-- TOC entry 5744 (class 2606 OID 25360)
-- Name: parcelunit parcelunit_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT parcelunit_sourceid_fk FOREIGN KEY (source_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5545 (class 2606 OID 25365)
-- Name: humanparcelunit parcelunithuman_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT parcelunithuman_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5485 (class 2606 OID 25370)
-- Name: eventhuman parcelunithuman_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.eventhuman
    ADD CONSTRAINT parcelunithuman_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5546 (class 2606 OID 25375)
-- Name: humanparcelunit parcelunithuman_unitid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.humanparcelunit
    ADD CONSTRAINT parcelunithuman_unitid_fk FOREIGN KEY (parcelunit_unitid) REFERENCES public.parcelunit(unitid);


--
-- TOC entry 5602 (class 2606 OID 25380)
-- Name: moneypayment payerid_person_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneypayment
    ADD CONSTRAINT payerid_person_fk FOREIGN KEY (payer_personid) REFERENCES public.person(personid);


--
-- TOC entry 5601 (class 2606 OID 25385)
-- Name: moneypayment payment_paymenttypeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneypayment
    ADD CONSTRAINT payment_paymenttypeid_fk FOREIGN KEY (paymenttype_typeid) REFERENCES public.moneypaymenttype(typeid);


--
-- TOC entry 5600 (class 2606 OID 25390)
-- Name: moneypayment payment_recordedby_login_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.moneypayment
    ADD CONSTRAINT payment_recordedby_login_fk FOREIGN KEY (recordedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5747 (class 2606 OID 25395)
-- Name: pdfdoc pdfdoc_blobbytes_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.pdfdoc
    ADD CONSTRAINT pdfdoc_blobbytes_fk FOREIGN KEY (blobbytes_bytesid) REFERENCES public.blobbytes(bytesid);


--
-- TOC entry 5746 (class 2606 OID 25400)
-- Name: pdfdoc pdfdoc_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.pdfdoc
    ADD CONSTRAINT pdfdoc_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5341 (class 2606 OID 25405)
-- Name: person person_clone_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_clone_fk FOREIGN KEY (clonedby) REFERENCES public.login(userid);


--
-- TOC entry 5342 (class 2606 OID 25410)
-- Name: person person_clone_person_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_clone_person_fk FOREIGN KEY (cloneof) REFERENCES public.person(personid);


--
-- TOC entry 5343 (class 2606 OID 25415)
-- Name: person person_creator_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_creator_fk FOREIGN KEY (creator) REFERENCES public.login(userid);


--
-- TOC entry 5344 (class 2606 OID 25420)
-- Name: person person_ghost_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_ghost_fk FOREIGN KEY (ghostby) REFERENCES public.login(userid);


--
-- TOC entry 5345 (class 2606 OID 25425)
-- Name: person person_ghost_person_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_ghost_person_fk FOREIGN KEY (ghostof) REFERENCES public.person(personid);


--
-- TOC entry 5346 (class 2606 OID 25430)
-- Name: person person_sourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_sourceid_fk FOREIGN KEY (sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5347 (class 2606 OID 25435)
-- Name: person person_userlink_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.person
    ADD CONSTRAINT person_userlink_userid_fk FOREIGN KEY (userlink) REFERENCES public.login(userid);


--
-- TOC entry 5748 (class 2606 OID 25440)
-- Name: personchange personchange_approvedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personchange
    ADD CONSTRAINT personchange_approvedby_fk FOREIGN KEY (approvedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5749 (class 2606 OID 25445)
-- Name: personchange personchange_changedbypersonid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personchange
    ADD CONSTRAINT personchange_changedbypersonid_fk FOREIGN KEY (changedby_personid) REFERENCES public.person(personid);


--
-- TOC entry 5750 (class 2606 OID 25450)
-- Name: personchange personchange_changedbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personchange
    ADD CONSTRAINT personchange_changedbyuserid_fk FOREIGN KEY (changedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5751 (class 2606 OID 25455)
-- Name: personchange personchange_personpersonid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personchange
    ADD CONSTRAINT personchange_personpersonid_fk FOREIGN KEY (person_personid) REFERENCES public.person(personid);


--
-- TOC entry 5754 (class 2606 OID 25460)
-- Name: personhumanmigrationlog personhumanmigrationlog_code_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personhumanmigrationlog
    ADD CONSTRAINT personhumanmigrationlog_code_fk FOREIGN KEY (error_code) REFERENCES public.personhumanmigrationlogerrorcode(code);


--
-- TOC entry 5753 (class 2606 OID 25465)
-- Name: personhumanmigrationlog personhumanmigrationlog_humanid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personhumanmigrationlog
    ADD CONSTRAINT personhumanmigrationlog_humanid_fk FOREIGN KEY (human_humanid) REFERENCES public.human(humanid);


--
-- TOC entry 5752 (class 2606 OID 25470)
-- Name: personhumanmigrationlog personhumanmigrationlog_personid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personhumanmigrationlog
    ADD CONSTRAINT personhumanmigrationlog_personid_fk FOREIGN KEY (person_personid) REFERENCES public.person(personid);


--
-- TOC entry 5757 (class 2606 OID 25475)
-- Name: personmergehistory personmerge_source_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personmergehistory
    ADD CONSTRAINT personmerge_source_fk FOREIGN KEY (mergesource_personid) REFERENCES public.person(personid);


--
-- TOC entry 5756 (class 2606 OID 25480)
-- Name: personmergehistory personmerge_target_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personmergehistory
    ADD CONSTRAINT personmerge_target_fk FOREIGN KEY (mergetarget_personid) REFERENCES public.person(personid);


--
-- TOC entry 5755 (class 2606 OID 25485)
-- Name: personmergehistory personmerge_user_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personmergehistory
    ADD CONSTRAINT personmerge_user_fk FOREIGN KEY (mergby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5759 (class 2606 OID 25490)
-- Name: personmunilink personmuni_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personmunilink
    ADD CONSTRAINT personmuni_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5758 (class 2606 OID 25495)
-- Name: personmunilink personmuni_personid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.personmunilink
    ADD CONSTRAINT personmuni_personid_fk FOREIGN KEY (person_personid) REFERENCES public.person(personid);


--
-- TOC entry 5468 (class 2606 OID 25500)
-- Name: contactphone phone_createdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT phone_createdby_userid_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5469 (class 2606 OID 25505)
-- Name: contactphone phone_deactivatedby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT phone_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5470 (class 2606 OID 25510)
-- Name: contactphone phone_disconnected_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT phone_disconnected_userid_fk FOREIGN KEY (disconnect_userid) REFERENCES public.login(userid);


--
-- TOC entry 5471 (class 2606 OID 25515)
-- Name: contactphone phone_lastupdatdby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.contactphone
    ADD CONSTRAINT phone_lastupdatdby_userid_fk FOREIGN KEY (lastupdatedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5760 (class 2606 OID 25520)
-- Name: photodoc photodoc_blobbytes_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.photodoc
    ADD CONSTRAINT photodoc_blobbytes_fk FOREIGN KEY (blobbytes_bytesid) REFERENCES public.blobbytes(bytesid);


--
-- TOC entry 5761 (class 2606 OID 25525)
-- Name: photodoc photodoc_blobtype_typeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.photodoc
    ADD CONSTRAINT photodoc_blobtype_typeid_fk FOREIGN KEY (blobtype_typeid) REFERENCES public.blobtype(typeid);


--
-- TOC entry 5762 (class 2606 OID 25530)
-- Name: photodoc photodoc_createdby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.photodoc
    ADD CONSTRAINT photodoc_createdby_fk FOREIGN KEY (createdby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5763 (class 2606 OID 25535)
-- Name: photodoc photodoc_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.photodoc
    ADD CONSTRAINT photodoc_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5349 (class 2606 OID 25540)
-- Name: blobtype photodoctype_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.blobtype
    ADD CONSTRAINT photodoctype_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 5764 (class 2606 OID 25545)
-- Name: printstyle printstyle_headerimage_pdid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.printstyle
    ADD CONSTRAINT printstyle_headerimage_pdid_fk FOREIGN KEY (headerimage_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 5765 (class 2606 OID 25550)
-- Name: property property_abandoned_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_abandoned_userid_fk FOREIGN KEY (abandonedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5766 (class 2606 OID 25555)
-- Name: property property_bobsourceid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_bobsourceid_fk FOREIGN KEY (bobsource_sourceid) REFERENCES public.bobsource(sourceid);


--
-- TOC entry 5767 (class 2606 OID 25560)
-- Name: property property_conditionintensityclass_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_conditionintensityclass_classid_fk FOREIGN KEY (condition_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 5768 (class 2606 OID 25565)
-- Name: property property_landbankprospect_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_landbankprospect_classid_fk FOREIGN KEY (landbankprospect_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 5769 (class 2606 OID 25570)
-- Name: property property_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_municode_fk FOREIGN KEY (municipality_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5770 (class 2606 OID 25575)
-- Name: property property_propertyusetypeid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_propertyusetypeid_fk FOREIGN KEY (usetype_typeid) REFERENCES public.propertyusetype(propertyusetypeid);


--
-- TOC entry 5771 (class 2606 OID 25580)
-- Name: property property_unfitby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_unfitby_userid_fk FOREIGN KEY (unfitby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5772 (class 2606 OID 25585)
-- Name: property property_updatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_updatedby_fk FOREIGN KEY (lastupdatedby) REFERENCES public.login(userid);


--
-- TOC entry 5773 (class 2606 OID 25590)
-- Name: property property_vacant_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.property
    ADD CONSTRAINT property_vacant_userid_fk FOREIGN KEY (vacantby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5775 (class 2606 OID 25595)
-- Name: propertyexternaldata propertyexternaldata_propid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyexternaldata
    ADD CONSTRAINT propertyexternaldata_propid_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 5774 (class 2606 OID 25600)
-- Name: propertyexternaldata propertyexternaldata_taxstatus_taxstatusid_fkey; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyexternaldata
    ADD CONSTRAINT propertyexternaldata_taxstatus_taxstatusid_fkey FOREIGN KEY (taxstatus_taxstatusid) REFERENCES public.taxstatus(taxstatusid);


--
-- TOC entry 5776 (class 2606 OID 25605)
-- Name: propertyotherid propertyotherid_propid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyotherid
    ADD CONSTRAINT propertyotherid_propid_fk FOREIGN KEY (property_propid) REFERENCES public.property(propertyid);


--
-- TOC entry 5778 (class 2606 OID 25610)
-- Name: propertypdfdoc propertypdfdoc_pdid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertypdfdoc
    ADD CONSTRAINT propertypdfdoc_pdid_fk FOREIGN KEY (pdfdoc_pdfdocid) REFERENCES public.pdfdoc(pdfdocid);


--
-- TOC entry 5777 (class 2606 OID 25615)
-- Name: propertypdfdoc propertypdfdoc_prop_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertypdfdoc
    ADD CONSTRAINT propertypdfdoc_prop_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 5780 (class 2606 OID 25620)
-- Name: propertyperson propertyperson_personid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyperson
    ADD CONSTRAINT propertyperson_personid_fk FOREIGN KEY (person_personid) REFERENCES public.person(personid);


--
-- TOC entry 5779 (class 2606 OID 25625)
-- Name: propertyperson propertyperson_propid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyperson
    ADD CONSTRAINT propertyperson_propid_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 5782 (class 2606 OID 25630)
-- Name: propertyphotodoc propertyphotodoc_pdid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyphotodoc
    ADD CONSTRAINT propertyphotodoc_pdid_fk FOREIGN KEY (photodoc_photodocid) REFERENCES public.photodoc(photodocid);


--
-- TOC entry 5781 (class 2606 OID 25635)
-- Name: propertyphotodoc propertyphotodoc_prop_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyphotodoc
    ADD CONSTRAINT propertyphotodoc_prop_fk FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 5783 (class 2606 OID 25640)
-- Name: propertystatus propertystatus_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertystatus
    ADD CONSTRAINT propertystatus_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5786 (class 2606 OID 25645)
-- Name: propertyunit propertyunit_propertyid; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyunit
    ADD CONSTRAINT propertyunit_propertyid FOREIGN KEY (property_propertyid) REFERENCES public.property(propertyid);


--
-- TOC entry 5787 (class 2606 OID 25650)
-- Name: propertyunitchange propertyunitchange_approvedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyunitchange
    ADD CONSTRAINT propertyunitchange_approvedby_fk FOREIGN KEY (approvedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5788 (class 2606 OID 25655)
-- Name: propertyunitchange propertyunitchange_changedbypersonid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyunitchange
    ADD CONSTRAINT propertyunitchange_changedbypersonid_fk FOREIGN KEY (changedby_personid) REFERENCES public.person(personid);


--
-- TOC entry 5789 (class 2606 OID 25660)
-- Name: propertyunitchange propertyunitchange_changedbyuserid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyunitchange
    ADD CONSTRAINT propertyunitchange_changedbyuserid_fk FOREIGN KEY (changedby_userid) REFERENCES public.login(userid);


--
-- TOC entry 5790 (class 2606 OID 25665)
-- Name: propertyunitchange propertyunitchange_unitid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyunitchange
    ADD CONSTRAINT propertyunitchange_unitid_fk FOREIGN KEY (propertyunit_unitid) REFERENCES public.propertyunit(unitid);


--
-- TOC entry 5791 (class 2606 OID 25670)
-- Name: propertyusetype propertyusetype_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyusetype
    ADD CONSTRAINT propertyusetype_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 5785 (class 2606 OID 25675)
-- Name: propertyunit propunit_conditionintensityclass_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyunit
    ADD CONSTRAINT propunit_conditionintensityclass_classid_fk FOREIGN KEY (condition_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 5745 (class 2606 OID 25680)
-- Name: parcelunit propunit_conditionintensityclass_classid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.parcelunit
    ADD CONSTRAINT propunit_conditionintensityclass_classid_fk FOREIGN KEY (condition_intensityclassid) REFERENCES public.intensityclass(classid);


--
-- TOC entry 5784 (class 2606 OID 25685)
-- Name: propertyunit propunit_rentalintentupdatedby_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.propertyunit
    ADD CONSTRAINT propunit_rentalintentupdatedby_fk FOREIGN KEY (rentalintentlastupdatedby_userid) REFERENCES public.eventrule(ruleid);


--
-- TOC entry 5705 (class 2606 OID 25690)
-- Name: occpermitapplication reason_reasonid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.occpermitapplication
    ADD CONSTRAINT reason_reasonid_fk FOREIGN KEY (reason_reasonid) REFERENCES public.occpermitapplicationreason(reasonid);


--
-- TOC entry 5359 (class 2606 OID 25695)
-- Name: ceactionrequest status_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.ceactionrequest
    ADD CONSTRAINT status_id_fk FOREIGN KEY (status_id) REFERENCES public.ceactionrequeststatus(statusid);


--
-- TOC entry 5548 (class 2606 OID 25700)
-- Name: improvementsuggestion submitter_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.improvementsuggestion
    ADD CONSTRAINT submitter_fk FOREIGN KEY (submitterid) REFERENCES public.login(userid);


--
-- TOC entry 5795 (class 2606 OID 25705)
-- Name: textblockcategory textblockcategory_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.textblockcategory
    ADD CONSTRAINT textblockcategory_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 5438 (class 2606 OID 25710)
-- Name: codeelementguide textblockcategory_iconid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeelementguide
    ADD CONSTRAINT textblockcategory_iconid_fk FOREIGN KEY (icon_iconid) REFERENCES public.icon(iconid);


--
-- TOC entry 5794 (class 2606 OID 25715)
-- Name: textblockcategory textblockcategory_municode_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.textblockcategory
    ADD CONSTRAINT textblockcategory_municode_fk FOREIGN KEY (muni_municode) REFERENCES public.municipality(municode);


--
-- TOC entry 5457 (class 2606 OID 25720)
-- Name: codeviolation violation_creationby_userid_fk; Type: FK CONSTRAINT; Schema: public; Owner: changeme
--

ALTER TABLE ONLY public.codeviolation
    ADD CONSTRAINT violation_creationby_userid_fk FOREIGN KEY (createdby) REFERENCES public.login(userid);


--
-- TOC entry 6186 (class 0 OID 0)
-- Dependencies: 11
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: changeme
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM changeme;
GRANT ALL ON SCHEMA public TO changeme;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2022-01-28 20:24:02 EST

--
-- PostgreSQL database dump complete
--

