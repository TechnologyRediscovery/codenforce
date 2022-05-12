--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.19
-- Dumped by pg_dump version 11.5 (Ubuntu 11.5-1.pgdg16.04+1)

-- Started on 2022-02-25 12:16:15 EST

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
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 4423 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- TOC entry 1134 (class 1247 OID 65164)
-- Name: casephase; Type: TYPE; Schema: public; Owner: sylvia
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


ALTER TYPE public.casephase OWNER TO sylvia;

--
-- TOC entry 1426 (class 1247 OID 163769)
-- Name: citationviolationstatus; Type: TYPE; Schema: public; Owner: sylvia
--

CREATE TYPE public.citationviolationstatus AS ENUM (
    'Pending',
    'Guilty',
    'Dismissed',
    'Compliance',
    'Deemed Invalid'
);


ALTER TYPE public.citationviolationstatus OWNER TO sylvia;

--
-- TOC entry 1144 (class 1247 OID 65186)
-- Name: eventtype; Type: TYPE; Schema: public; Owner: sylvia
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


ALTER TYPE public.eventtype OWNER TO sylvia;

--
-- TOC entry 1443 (class 1247 OID 163945)
-- Name: linkedobjectroleschema; Type: TYPE; Schema: public; Owner: sylvia
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


ALTER TYPE public.linkedobjectroleschema OWNER TO sylvia;

--
-- TOC entry 1117 (class 1247 OID 144062)
-- Name: occapplicationstatus; Type: TYPE; Schema: public; Owner: sylvia
--

CREATE TYPE public.occapplicationstatus AS ENUM (
    'Waiting',
    'NewUnit',
    'OldUnit',
    'Rejected',
    'Invalid'
);


ALTER TYPE public.occapplicationstatus OWNER TO sylvia;

--
-- TOC entry 1503 (class 1247 OID 172682)
-- Name: occinspectionphototype; Type: TYPE; Schema: public; Owner: sylvia
--

CREATE TYPE public.occinspectionphototype AS ENUM (
    'PassDocumentation',
    'FailDocumentation',
    'GeneralDocumentation',
    'Other',
    'Unused'
);


ALTER TYPE public.occinspectionphototype OWNER TO sylvia;

--
-- TOC entry 1128 (class 1247 OID 65125)
-- Name: persontype; Type: TYPE; Schema: public; Owner: sylvia
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


ALTER TYPE public.persontype OWNER TO sylvia;

--
-- TOC entry 892 (class 1247 OID 29478)
-- Name: requeststatusenum; Type: TYPE; Schema: public; Owner: sylvia
--

CREATE TYPE public.requeststatusenum AS ENUM (
    'AwaitingReview',
    'UnderInvestigation',
    'NoViolationFound',
    'CitationFiled',
    'Resolved'
);


ALTER TYPE public.requeststatusenum OWNER TO sylvia;

--
-- TOC entry 1131 (class 1247 OID 65148)
-- Name: role; Type: TYPE; Schema: public; Owner: sylvia
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


ALTER TYPE public.role OWNER TO sylvia;

--
-- TOC entry 1546 (class 1247 OID 180800)
-- Name: systemdomain; Type: TYPE; Schema: public; Owner: sylvia
--

CREATE TYPE public.systemdomain AS ENUM (
    'CodeEnforcement',
    'Occupancy',
    'Universal'
);


ALTER TYPE public.systemdomain OWNER TO sylvia;

--
-- TOC entry 505 (class 1255 OID 173009)
-- Name: cnf_injectstaticnovdata(integer); Type: FUNCTION; Schema: public; Owner: sylvia
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


ALTER FUNCTION public.cnf_injectstaticnovdata(targetmunicode integer) OWNER TO sylvia;

--
-- TOC entry 504 (class 1255 OID 172981)
-- Name: cnf_parsezipcode(text); Type: FUNCTION; Schema: public; Owner: sylvia
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


ALTER FUNCTION public.cnf_parsezipcode(zipraw text) OWNER TO sylvia;

--
-- TOC entry 495 (class 1255 OID 110889)
-- Name: copycleartextpswds(); Type: FUNCTION; Schema: public; Owner: sylvia
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


ALTER FUNCTION public.copycleartextpswds() OWNER TO sylvia;

--
-- TOC entry 184 (class 1259 OID 65205)
-- Name: person_personidseq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.person_personidseq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.person_personidseq OWNER TO sylvia;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 185 (class 1259 OID 65207)
-- Name: person; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.person OWNER TO sylvia;

--
-- TOC entry 475 (class 1255 OID 106362)
-- Name: createghostperson(public.person, integer); Type: FUNCTION; Schema: public; Owner: sylvia
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


ALTER FUNCTION public.createghostperson(person_row public.person, userid integer) OWNER TO sylvia;

--
-- TOC entry 498 (class 1255 OID 172416)
-- Name: extractbuildingno(text); Type: FUNCTION; Schema: public; Owner: sylvia
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


ALTER FUNCTION public.extractbuildingno(addr text) OWNER TO sylvia;

--
-- TOC entry 502 (class 1255 OID 172418)
-- Name: extractstreet(text); Type: FUNCTION; Schema: public; Owner: sylvia
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


ALTER FUNCTION public.extractstreet(addr text) OWNER TO sylvia;

--
-- TOC entry 496 (class 1255 OID 110891)
-- Name: hashpasswords(); Type: FUNCTION; Schema: public; Owner: sylvia
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


ALTER FUNCTION public.hashpasswords() OWNER TO sylvia;

--
-- TOC entry 500 (class 1255 OID 172788)
-- Name: migratepersontohuman(integer, integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: sylvia
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


ALTER FUNCTION public.migratepersontohuman(creationrobotuser integer, defaultsource integer, municodetarget integer, parcel_human_lorid integer, human_mailing_lorid integer) OWNER TO sylvia;

--
-- TOC entry 499 (class 1255 OID 172797)
-- Name: migratepropertytoparcel(integer, integer, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: sylvia
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


ALTER FUNCTION public.migratepropertytoparcel(creationrobotuser integer, defaultsource integer, cityid integer, municodetarget integer, parceladdr_lorid integer) OWNER TO sylvia;

--
-- TOC entry 497 (class 1255 OID 144039)
-- Name: resetsequences(integer); Type: FUNCTION; Schema: public; Owner: sylvia
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


ALTER FUNCTION public.resetsequences(incr integer) OWNER TO sylvia;

--
-- TOC entry 501 (class 1255 OID 172972)
-- Name: unifyspacechars(text); Type: FUNCTION; Schema: public; Owner: sylvia
--

CREATE FUNCTION public.unifyspacechars(chaostext text) RETURNS text
    LANGUAGE plpgsql
    AS $$

	BEGIN
		RETURN regexp_replace(chaostext, '[\s\u180e\u200B\u200C\u200D\u2060\uFEFF\u00a0]',' ','g'); 
	END;
$$;


ALTER FUNCTION public.unifyspacechars(chaostext text) OWNER TO sylvia;

--
-- TOC entry 503 (class 1255 OID 172973)
-- Name: unifyspacesandtrim(text); Type: FUNCTION; Schema: public; Owner: sylvia
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


ALTER FUNCTION public.unifyspacesandtrim(chaostext text) OWNER TO sylvia;

--
-- TOC entry 186 (class 1259 OID 65224)
-- Name: actionrqstissuetype_issuetypeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.actionrqstissuetype_issuetypeid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.actionrqstissuetype_issuetypeid_seq OWNER TO sylvia;

--
-- TOC entry 391 (class 1259 OID 155173)
-- Name: blobbytes_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.blobbytes_seq
    START WITH 10
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.blobbytes_seq OWNER TO sylvia;

--
-- TOC entry 392 (class 1259 OID 155175)
-- Name: blobbytes; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.blobbytes (
    bytesid integer DEFAULT nextval('public.blobbytes_seq'::regclass) NOT NULL,
    createdts timestamp with time zone,
    blob bytea,
    uploadedby_userid integer,
    filename text
);


ALTER TABLE public.blobbytes OWNER TO sylvia;

--
-- TOC entry 274 (class 1259 OID 95398)
-- Name: blobtype; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.blobtype OWNER TO sylvia;

--
-- TOC entry 248 (class 1259 OID 66105)
-- Name: blockcategory_categoryid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.blockcategory_categoryid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.blockcategory_categoryid_seq OWNER TO sylvia;

--
-- TOC entry 299 (class 1259 OID 106743)
-- Name: bobsourceid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.bobsourceid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.bobsourceid_seq OWNER TO sylvia;

--
-- TOC entry 300 (class 1259 OID 106745)
-- Name: bobsource; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.bobsource OWNER TO sylvia;

--
-- TOC entry 196 (class 1259 OID 65329)
-- Name: ceactionrequest_requestid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.ceactionrequest_requestid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ceactionrequest_requestid_seq OWNER TO sylvia;

--
-- TOC entry 197 (class 1259 OID 65331)
-- Name: ceactionrequest; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.ceactionrequest OWNER TO sylvia;

--
-- TOC entry 187 (class 1259 OID 65226)
-- Name: ceactionrequestissuetype; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.ceactionrequestissuetype OWNER TO sylvia;

--
-- TOC entry 395 (class 1259 OID 155294)
-- Name: ceactionrequestpdfdoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.ceactionrequestpdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    ceactionrequest_requestid integer NOT NULL
);


ALTER TABLE public.ceactionrequestpdfdoc OWNER TO sylvia;

--
-- TOC entry 277 (class 1259 OID 95447)
-- Name: ceactionrequestphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.ceactionrequestphotodoc (
    photodoc_photodocid integer NOT NULL,
    ceactionrequest_requestid integer NOT NULL
);


ALTER TABLE public.ceactionrequestphotodoc OWNER TO sylvia;

--
-- TOC entry 269 (class 1259 OID 74852)
-- Name: ceactionrequeststatus; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.ceactionrequeststatus (
    statusid integer NOT NULL,
    title text,
    description text,
    icon_iconid integer
);


ALTER TABLE public.ceactionrequeststatus OWNER TO sylvia;

--
-- TOC entry 204 (class 1259 OID 65436)
-- Name: cecase_caseid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.cecase_caseid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cecase_caseid_seq OWNER TO sylvia;

--
-- TOC entry 205 (class 1259 OID 65438)
-- Name: cecase; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.cecase OWNER TO sylvia;

--
-- TOC entry 4424 (class 0 OID 0)
-- Dependencies: 205
-- Name: TABLE cecase; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON TABLE public.cecase IS 'I can comment here and see there!';


--
-- TOC entry 4425 (class 0 OID 0)
-- Dependencies: 205
-- Name: COLUMN cecase.casename; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON COLUMN public.cecase.casename IS 'Column Comment';


--
-- TOC entry 288 (class 1259 OID 106167)
-- Name: cecasephasechangerule_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.cecasephasechangerule_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cecasephasechangerule_seq OWNER TO sylvia;

--
-- TOC entry 393 (class 1259 OID 155212)
-- Name: cecasephotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.cecasephotodoc (
    photodoc_photodocid integer NOT NULL,
    cecase_caseid integer NOT NULL
);


ALTER TABLE public.cecasephotodoc OWNER TO sylvia;

--
-- TOC entry 284 (class 1259 OID 103945)
-- Name: cecasestatusicon; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.cecasestatusicon (
    iconid integer NOT NULL,
    status public.casephase NOT NULL
);


ALTER TABLE public.cecasestatusicon OWNER TO sylvia;

--
-- TOC entry 206 (class 1259 OID 65467)
-- Name: ceevent_eventid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.ceevent_eventid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ceevent_eventid_seq OWNER TO sylvia;

--
-- TOC entry 202 (class 1259 OID 65418)
-- Name: ceeventcategory_categoryid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.ceeventcategory_categoryid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ceeventcategory_categoryid_seq OWNER TO sylvia;

--
-- TOC entry 296 (class 1259 OID 106585)
-- Name: ceeventproposal_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.ceeventproposal_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ceeventproposal_seq OWNER TO sylvia;

--
-- TOC entry 297 (class 1259 OID 106695)
-- Name: ceeventproposalimplementation_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.ceeventproposalimplementation_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.ceeventproposalimplementation_seq OWNER TO sylvia;

--
-- TOC entry 227 (class 1259 OID 65759)
-- Name: checklist_checklistid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.checklist_checklistid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.checklist_checklistid_seq OWNER TO sylvia;

--
-- TOC entry 229 (class 1259 OID 65770)
-- Name: chkliststiceid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.chkliststiceid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.chkliststiceid_seq OWNER TO sylvia;

--
-- TOC entry 313 (class 1259 OID 107352)
-- Name: choice_choiceid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.choice_choiceid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.choice_choiceid_seq OWNER TO sylvia;

--
-- TOC entry 314 (class 1259 OID 107354)
-- Name: choice; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.choice OWNER TO sylvia;

--
-- TOC entry 310 (class 1259 OID 107235)
-- Name: eventproposal_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.eventproposal_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.eventproposal_seq OWNER TO sylvia;

--
-- TOC entry 311 (class 1259 OID 107237)
-- Name: choicedirective; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.choicedirective OWNER TO sylvia;

--
-- TOC entry 315 (class 1259 OID 107378)
-- Name: choicedirectivechoice; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.choicedirectivechoice (
    choice_choiceid integer NOT NULL,
    directive_directiveid integer NOT NULL
);


ALTER TABLE public.choicedirectivechoice OWNER TO sylvia;

--
-- TOC entry 337 (class 1259 OID 109086)
-- Name: choicedirectivedirectiveset; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.choicedirectivedirectiveset (
    directiveset_setid integer NOT NULL,
    directive_dirid integer NOT NULL
);


ALTER TABLE public.choicedirectivedirectiveset OWNER TO sylvia;

--
-- TOC entry 332 (class 1259 OID 108885)
-- Name: choicedirectivesetid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.choicedirectivesetid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.choicedirectivesetid_seq OWNER TO sylvia;

--
-- TOC entry 333 (class 1259 OID 108887)
-- Name: choicedirectiveset; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.choicedirectiveset (
    directivesetid integer DEFAULT nextval('public.choicedirectivesetid_seq'::regclass) NOT NULL,
    title text,
    description text
);


ALTER TABLE public.choicedirectiveset OWNER TO sylvia;

--
-- TOC entry 298 (class 1259 OID 106697)
-- Name: choiceproposal; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.choiceproposal OWNER TO sylvia;

--
-- TOC entry 216 (class 1259 OID 65594)
-- Name: citation_citationid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.citation_citationid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citation_citationid_seq OWNER TO sylvia;

--
-- TOC entry 247 (class 1259 OID 66080)
-- Name: citation; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.citation OWNER TO sylvia;

--
-- TOC entry 405 (class 1259 OID 163825)
-- Name: citationcitationstatus_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.citationcitationstatus_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationcitationstatus_seq OWNER TO sylvia;

--
-- TOC entry 406 (class 1259 OID 163830)
-- Name: citationcitationstatus; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.citationcitationstatus OWNER TO sylvia;

--
-- TOC entry 440 (class 1259 OID 173067)
-- Name: citationdockethuman_linkid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.citationdockethuman_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationdockethuman_linkid_seq OWNER TO sylvia;

--
-- TOC entry 438 (class 1259 OID 173019)
-- Name: citationdocketno_docketid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.citationdocketno_docketid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationdocketno_docketid_seq OWNER TO sylvia;

--
-- TOC entry 439 (class 1259 OID 173021)
-- Name: citationdocketno; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.citationdocketno OWNER TO sylvia;

--
-- TOC entry 441 (class 1259 OID 173069)
-- Name: citationdocketnohuman; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.citationdocketnohuman (
    linkid integer DEFAULT nextval('public.citationdockethuman_linkid_seq'::regclass) NOT NULL,
    docketno_docketid integer NOT NULL,
    citationhuman_linkid integer NOT NULL,
    notes text
);


ALTER TABLE public.citationdocketnohuman OWNER TO sylvia;

--
-- TOC entry 408 (class 1259 OID 163902)
-- Name: citationevent; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.citationevent (
    citation_citationid integer NOT NULL,
    event_eventid integer NOT NULL
);


ALTER TABLE public.citationevent OWNER TO sylvia;

--
-- TOC entry 442 (class 1259 OID 173088)
-- Name: citationfilingtype_typeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.citationfilingtype_typeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationfilingtype_typeid_seq OWNER TO sylvia;

--
-- TOC entry 443 (class 1259 OID 173090)
-- Name: citationfilingtype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.citationfilingtype (
    typeid integer DEFAULT nextval('public.citationfilingtype_typeid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    muni_municode integer NOT NULL,
    active boolean DEFAULT true
);


ALTER TABLE public.citationfilingtype OWNER TO sylvia;

--
-- TOC entry 403 (class 1259 OID 163727)
-- Name: citationhuman_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.citationhuman_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationhuman_seq OWNER TO sylvia;

--
-- TOC entry 404 (class 1259 OID 163729)
-- Name: citationhuman; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.citationhuman OWNER TO sylvia;

--
-- TOC entry 407 (class 1259 OID 163864)
-- Name: citationphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.citationphotodoc (
    photodoc_photodocid integer NOT NULL,
    citation_citationid integer NOT NULL
);


ALTER TABLE public.citationphotodoc OWNER TO sylvia;

--
-- TOC entry 243 (class 1259 OID 65996)
-- Name: citationstatus_statusid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.citationstatus_statusid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationstatus_statusid_seq OWNER TO sylvia;

--
-- TOC entry 244 (class 1259 OID 65998)
-- Name: citationstatus; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.citationstatus OWNER TO sylvia;

--
-- TOC entry 245 (class 1259 OID 66032)
-- Name: citationviolation_cvid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.citationviolation_cvid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.citationviolation_cvid_seq OWNER TO sylvia;

--
-- TOC entry 246 (class 1259 OID 66034)
-- Name: citationviolation; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.citationviolation OWNER TO sylvia;

--
-- TOC entry 210 (class 1259 OID 65534)
-- Name: codeelement_elementid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.codeelement_elementid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codeelement_elementid_seq OWNER TO sylvia;

--
-- TOC entry 211 (class 1259 OID 65536)
-- Name: codeelement; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.codeelement OWNER TO sylvia;

--
-- TOC entry 263 (class 1259 OID 66626)
-- Name: codeelementguide_id_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.codeelementguide_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codeelementguide_id_seq OWNER TO sylvia;

--
-- TOC entry 264 (class 1259 OID 66628)
-- Name: codeelementguide; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.codeelementguide OWNER TO sylvia;

--
-- TOC entry 360 (class 1259 OID 144173)
-- Name: codeelementinjectedvalue_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.codeelementinjectedvalue_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codeelementinjectedvalue_seq OWNER TO sylvia;

--
-- TOC entry 361 (class 1259 OID 144188)
-- Name: codeelementinjectedvalue; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.codeelementinjectedvalue OWNER TO sylvia;

--
-- TOC entry 198 (class 1259 OID 65362)
-- Name: codeset_codesetid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.codeset_codesetid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codeset_codesetid_seq OWNER TO sylvia;

--
-- TOC entry 199 (class 1259 OID 65364)
-- Name: codeset; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.codeset (
    codesetid integer DEFAULT nextval('public.codeset_codesetid_seq'::regclass) NOT NULL,
    name text,
    description text,
    municipality_municode integer,
    active boolean DEFAULT true
);


ALTER TABLE public.codeset OWNER TO sylvia;

--
-- TOC entry 212 (class 1259 OID 65557)
-- Name: codesetelement_elementid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.codesetelement_elementid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codesetelement_elementid_seq OWNER TO sylvia;

--
-- TOC entry 213 (class 1259 OID 65559)
-- Name: codesetelement; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.codesetelement OWNER TO sylvia;

--
-- TOC entry 287 (class 1259 OID 105013)
-- Name: codesetelementclass_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.codesetelementclass_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codesetelementclass_seq OWNER TO sylvia;

--
-- TOC entry 208 (class 1259 OID 65514)
-- Name: codesource_sourceid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.codesource_sourceid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codesource_sourceid_seq OWNER TO sylvia;

--
-- TOC entry 209 (class 1259 OID 65516)
-- Name: codesource; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.codesource OWNER TO sylvia;

--
-- TOC entry 217 (class 1259 OID 65621)
-- Name: codeviolation_violationid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.codeviolation_violationid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codeviolation_violationid_seq OWNER TO sylvia;

--
-- TOC entry 218 (class 1259 OID 65623)
-- Name: codeviolation; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.codeviolation OWNER TO sylvia;

--
-- TOC entry 4426 (class 0 OID 0)
-- Dependencies: 218
-- Name: TABLE codeviolation; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON TABLE public.codeviolation IS 'save commets';


--
-- TOC entry 396 (class 1259 OID 155309)
-- Name: codeviolationpdfdoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.codeviolationpdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    codeviolation_violationid integer NOT NULL
);


ALTER TABLE public.codeviolationpdfdoc OWNER TO sylvia;

--
-- TOC entry 275 (class 1259 OID 95411)
-- Name: codeviolationphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.codeviolationphotodoc (
    photodoc_photodocid integer NOT NULL,
    codeviolation_violationid integer NOT NULL
);


ALTER TABLE public.codeviolationphotodoc OWNER TO sylvia;

--
-- TOC entry 285 (class 1259 OID 104986)
-- Name: codeviolationseverityclass_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.codeviolationseverityclass_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.codeviolationseverityclass_seq OWNER TO sylvia;

--
-- TOC entry 201 (class 1259 OID 65401)
-- Name: coglog_logeentryid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.coglog_logeentryid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.coglog_logeentryid_seq OWNER TO sylvia;

--
-- TOC entry 371 (class 1259 OID 154320)
-- Name: contactemail_emailid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.contactemail_emailid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.contactemail_emailid_seq OWNER TO sylvia;

--
-- TOC entry 372 (class 1259 OID 154322)
-- Name: contactemail; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.contactemail OWNER TO sylvia;

--
-- TOC entry 369 (class 1259 OID 154279)
-- Name: contactphone_phoneid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.contactphone_phoneid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.contactphone_phoneid_seq OWNER TO sylvia;

--
-- TOC entry 370 (class 1259 OID 154281)
-- Name: contactphone; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.contactphone OWNER TO sylvia;

--
-- TOC entry 367 (class 1259 OID 154268)
-- Name: contactphonetype_typeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.contactphonetype_typeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.contactphonetype_typeid_seq OWNER TO sylvia;

--
-- TOC entry 368 (class 1259 OID 154270)
-- Name: contactphonetype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.contactphonetype (
    phonetypeid integer DEFAULT nextval('public.contactphonetype_typeid_seq'::regclass) NOT NULL,
    title text,
    createdts timestamp with time zone,
    deactivatedts timestamp with time zone
);


ALTER TABLE public.contactphonetype OWNER TO sylvia;

--
-- TOC entry 214 (class 1259 OID 65578)
-- Name: courtentity_entityid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.courtentity_entityid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.courtentity_entityid_seq OWNER TO sylvia;

--
-- TOC entry 215 (class 1259 OID 65580)
-- Name: courtentity; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.courtentity OWNER TO sylvia;

--
-- TOC entry 280 (class 1259 OID 95613)
-- Name: dbpatch; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.dbpatch (
    patchnum integer NOT NULL,
    patchfilename text,
    datepublished timestamp without time zone,
    patchauthor text,
    notes text
);


ALTER TABLE public.dbpatch OWNER TO sylvia;

--
-- TOC entry 207 (class 1259 OID 65469)
-- Name: event; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.event OWNER TO sylvia;

--
-- TOC entry 203 (class 1259 OID 65420)
-- Name: eventcategory; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.eventcategory OWNER TO sylvia;

--
-- TOC entry 412 (class 1259 OID 163993)
-- Name: eventhuman_linkid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.eventhuman_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.eventhuman_linkid_seq OWNER TO sylvia;

--
-- TOC entry 413 (class 1259 OID 163995)
-- Name: eventhuman; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.eventhuman OWNER TO sylvia;

--
-- TOC entry 312 (class 1259 OID 107307)
-- Name: eventrule; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.eventrule OWNER TO sylvia;

--
-- TOC entry 354 (class 1259 OID 143957)
-- Name: eventruleimpl_impid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.eventruleimpl_impid_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.eventruleimpl_impid_seq OWNER TO sylvia;

--
-- TOC entry 355 (class 1259 OID 143959)
-- Name: eventruleimpl; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.eventruleimpl OWNER TO sylvia;

--
-- TOC entry 331 (class 1259 OID 108870)
-- Name: eventruleruleset; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.eventruleruleset (
    ruleset_rulesetid integer NOT NULL,
    eventrule_ruleid integer NOT NULL
);


ALTER TABLE public.eventruleruleset OWNER TO sylvia;

--
-- TOC entry 329 (class 1259 OID 108859)
-- Name: eventrulesetid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.eventrulesetid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.eventrulesetid_seq OWNER TO sylvia;

--
-- TOC entry 330 (class 1259 OID 108861)
-- Name: eventruleset; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.eventruleset (
    rulesetid integer DEFAULT nextval('public.eventrulesetid_seq'::regclass) NOT NULL,
    title text,
    description text
);


ALTER TABLE public.eventruleset OWNER TO sylvia;

--
-- TOC entry 364 (class 1259 OID 154163)
-- Name: human_humanid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.human_humanid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.human_humanid_seq OWNER TO sylvia;

--
-- TOC entry 365 (class 1259 OID 154165)
-- Name: human; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.human OWNER TO sylvia;

--
-- TOC entry 415 (class 1259 OID 164069)
-- Name: humancecase_linkid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.humancecase_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humancecase_linkid_seq OWNER TO sylvia;

--
-- TOC entry 388 (class 1259 OID 155005)
-- Name: humancecase; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.humancecase OWNER TO sylvia;

--
-- TOC entry 416 (class 1259 OID 164141)
-- Name: humanmailing_linkid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.humanmailing_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humanmailing_linkid_seq OWNER TO sylvia;

--
-- TOC entry 374 (class 1259 OID 154413)
-- Name: humanmailing_roleid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.humanmailing_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humanmailing_roleid_seq OWNER TO sylvia;

--
-- TOC entry 390 (class 1259 OID 155080)
-- Name: humanmailingaddress; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.humanmailingaddress OWNER TO sylvia;

--
-- TOC entry 377 (class 1259 OID 154693)
-- Name: humanmuni_linkid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.humanmuni_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humanmuni_linkid_seq OWNER TO sylvia;

--
-- TOC entry 385 (class 1259 OID 154940)
-- Name: humanmuni; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.humanmuni OWNER TO sylvia;

--
-- TOC entry 386 (class 1259 OID 154974)
-- Name: humanoccperiod_linkid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.humanoccperiod_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humanoccperiod_linkid_seq OWNER TO sylvia;

--
-- TOC entry 387 (class 1259 OID 154976)
-- Name: humanoccperiod; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.humanoccperiod OWNER TO sylvia;

--
-- TOC entry 383 (class 1259 OID 154894)
-- Name: humanparcel_linkid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.humanparcel_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humanparcel_linkid_seq OWNER TO sylvia;

--
-- TOC entry 384 (class 1259 OID 154896)
-- Name: humanparcel; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.humanparcel OWNER TO sylvia;

--
-- TOC entry 382 (class 1259 OID 154878)
-- Name: humanparcelrole_roleid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.humanparcelrole_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.humanparcelrole_roleid_seq OWNER TO sylvia;

--
-- TOC entry 381 (class 1259 OID 154842)
-- Name: parcelunithuman_linkid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.parcelunithuman_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelunithuman_linkid_seq OWNER TO sylvia;

--
-- TOC entry 389 (class 1259 OID 155041)
-- Name: humanparcelunit; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.humanparcelunit OWNER TO sylvia;

--
-- TOC entry 282 (class 1259 OID 103894)
-- Name: iconid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.iconid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.iconid_seq OWNER TO sylvia;

--
-- TOC entry 283 (class 1259 OID 103896)
-- Name: icon; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.icon (
    iconid integer DEFAULT nextval('public.iconid_seq'::regclass) NOT NULL,
    name text,
    styleclass text,
    fontawesome text,
    materialicons text,
    active boolean DEFAULT true
);


ALTER TABLE public.icon OWNER TO sylvia;

--
-- TOC entry 259 (class 1259 OID 66585)
-- Name: improvementid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.improvementid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.improvementid_seq OWNER TO sylvia;

--
-- TOC entry 258 (class 1259 OID 66577)
-- Name: improvementstatus; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.improvementstatus (
    statusid integer NOT NULL,
    statustitle text,
    statusdescription text,
    icon_iconid integer
);


ALTER TABLE public.improvementstatus OWNER TO sylvia;

--
-- TOC entry 260 (class 1259 OID 66587)
-- Name: improvementsuggestion; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.improvementsuggestion OWNER TO sylvia;

--
-- TOC entry 257 (class 1259 OID 66569)
-- Name: improvementtype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.improvementtype (
    typeid integer NOT NULL,
    typetitle text,
    typedescription text
);


ALTER TABLE public.improvementtype OWNER TO sylvia;

--
-- TOC entry 253 (class 1259 OID 66244)
-- Name: inspectedspacetypeelement_inspectedstelid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.inspectedspacetypeelement_inspectedstelid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.inspectedspacetypeelement_inspectedstelid_seq OWNER TO sylvia;

--
-- TOC entry 286 (class 1259 OID 104988)
-- Name: intensityclass; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.intensityclass OWNER TO sylvia;

--
-- TOC entry 410 (class 1259 OID 163927)
-- Name: linkedobjectrole_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.linkedobjectrole_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.linkedobjectrole_seq OWNER TO sylvia;

--
-- TOC entry 411 (class 1259 OID 163983)
-- Name: linkedobjectrole; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.linkedobjectrole OWNER TO sylvia;

--
-- TOC entry 409 (class 1259 OID 163915)
-- Name: linkedobjectroleschema_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.linkedobjectroleschema_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.linkedobjectroleschema_seq OWNER TO sylvia;

--
-- TOC entry 261 (class 1259 OID 66611)
-- Name: listitemchange_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.listitemchange_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.listitemchange_seq OWNER TO sylvia;

--
-- TOC entry 262 (class 1259 OID 66613)
-- Name: listchangerequest; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.listchangerequest (
    changeid integer DEFAULT nextval('public.listitemchange_seq'::regclass) NOT NULL,
    changetext text
);


ALTER TABLE public.listchangerequest OWNER TO sylvia;

--
-- TOC entry 267 (class 1259 OID 74790)
-- Name: locationdescription_id_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.locationdescription_id_seq
    START WITH 100
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.locationdescription_id_seq OWNER TO sylvia;

--
-- TOC entry 271 (class 1259 OID 87094)
-- Name: log; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.log OWNER TO sylvia;

--
-- TOC entry 272 (class 1259 OID 87112)
-- Name: logcategory; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.logcategory (
    catid integer NOT NULL,
    title text,
    description text
);


ALTER TABLE public.logcategory OWNER TO sylvia;

--
-- TOC entry 200 (class 1259 OID 65378)
-- Name: login_userid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.login_userid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.login_userid_seq OWNER TO sylvia;

--
-- TOC entry 252 (class 1259 OID 66179)
-- Name: login; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.login OWNER TO sylvia;

--
-- TOC entry 349 (class 1259 OID 110900)
-- Name: logincredentialexercise_ex_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.logincredentialexercise_ex_seq
    START WITH 2777
    INCREMENT BY 7
    MINVALUE 2777
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.logincredentialexercise_ex_seq OWNER TO sylvia;

--
-- TOC entry 346 (class 1259 OID 110641)
-- Name: munilogin_recordid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.munilogin_recordid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.munilogin_recordid_seq OWNER TO sylvia;

--
-- TOC entry 348 (class 1259 OID 110789)
-- Name: loginmuniauthperiod; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.loginmuniauthperiod OWNER TO sylvia;

--
-- TOC entry 350 (class 1259 OID 110927)
-- Name: loginmuniauthperiodsession_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.loginmuniauthperiodsession_seq
    START WITH 2777
    INCREMENT BY 7
    MINVALUE 2777
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.loginmuniauthperiodsession_seq OWNER TO sylvia;

--
-- TOC entry 352 (class 1259 OID 110995)
-- Name: loginmuniauthperiodlog; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.loginmuniauthperiodlog OWNER TO sylvia;

--
-- TOC entry 351 (class 1259 OID 110993)
-- Name: loginmuniauthperiodlog_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.loginmuniauthperiodlog_seq
    START WITH 2777
    INCREMENT BY 7
    MINVALUE 2777
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.loginmuniauthperiodlog_seq OWNER TO sylvia;

--
-- TOC entry 278 (class 1259 OID 95476)
-- Name: loginobjecthistory_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.loginobjecthistory_seq
    START WITH 1000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.loginobjecthistory_seq OWNER TO sylvia;

--
-- TOC entry 279 (class 1259 OID 95525)
-- Name: loginobjecthistory; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.loginobjecthistory OWNER TO sylvia;

--
-- TOC entry 373 (class 1259 OID 154351)
-- Name: mailingaddress_addressid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.mailingaddress_addressid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mailingaddress_addressid_seq OWNER TO sylvia;

--
-- TOC entry 419 (class 1259 OID 164333)
-- Name: mailingaddress; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.mailingaddress OWNER TO sylvia;

--
-- TOC entry 421 (class 1259 OID 172525)
-- Name: mailingcitystatezip; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.mailingcitystatezip OWNER TO sylvia;

--
-- TOC entry 418 (class 1259 OID 164283)
-- Name: mailingstreet_streetid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.mailingstreet_streetid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.mailingstreet_streetid_seq OWNER TO sylvia;

--
-- TOC entry 420 (class 1259 OID 172506)
-- Name: mailingstreet; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.mailingstreet (
    streetid integer DEFAULT nextval('public.mailingstreet_streetid_seq'::regclass) NOT NULL,
    name text NOT NULL,
    namevariantsarr text[],
    citystatezip_cszipid integer NOT NULL,
    notes text,
    pobox boolean DEFAULT false,
    createdts timestamp with time zone DEFAULT now(),
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer
);


ALTER TABLE public.mailingstreet OWNER TO sylvia;

--
-- TOC entry 320 (class 1259 OID 108390)
-- Name: moneycecasefeeassignedid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.moneycecasefeeassignedid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.moneycecasefeeassignedid_seq OWNER TO sylvia;

--
-- TOC entry 321 (class 1259 OID 108392)
-- Name: moneycecasefeeassigned; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.moneycecasefeeassigned OWNER TO sylvia;

--
-- TOC entry 327 (class 1259 OID 108471)
-- Name: moneycecasefeepayment; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.moneycecasefeepayment (
    payment_paymentid integer NOT NULL,
    cecaseassignedfee_id integer NOT NULL
);


ALTER TABLE public.moneycecasefeepayment OWNER TO sylvia;

--
-- TOC entry 324 (class 1259 OID 108426)
-- Name: moneycodesetelementfee; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.moneycodesetelementfee (
    fee_feeid integer NOT NULL,
    codesetelement_elementid integer NOT NULL,
    active boolean,
    autoassign boolean
);


ALTER TABLE public.moneycodesetelementfee OWNER TO sylvia;

--
-- TOC entry 317 (class 1259 OID 107593)
-- Name: moneycodesetelementfeeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.moneycodesetelementfeeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.moneycodesetelementfeeid_seq OWNER TO sylvia;

--
-- TOC entry 231 (class 1259 OID 65810)
-- Name: occinspectionfee_feeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occinspectionfee_feeid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occinspectionfee_feeid_seq OWNER TO sylvia;

--
-- TOC entry 232 (class 1259 OID 65812)
-- Name: moneyfee; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.moneyfee OWNER TO sylvia;

--
-- TOC entry 319 (class 1259 OID 108364)
-- Name: moneyfeeassignedid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.moneyfeeassignedid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.moneyfeeassignedid_seq OWNER TO sylvia;

--
-- TOC entry 322 (class 1259 OID 108408)
-- Name: moneyoccperiodfeeassignedid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.moneyoccperiodfeeassignedid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.moneyoccperiodfeeassignedid_seq OWNER TO sylvia;

--
-- TOC entry 323 (class 1259 OID 108410)
-- Name: moneyoccperiodfeeassigned; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.moneyoccperiodfeeassigned OWNER TO sylvia;

--
-- TOC entry 326 (class 1259 OID 108456)
-- Name: moneyoccperiodfeepayment; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.moneyoccperiodfeepayment (
    payment_paymentid integer NOT NULL,
    occperiodassignedfee_id integer NOT NULL
);


ALTER TABLE public.moneyoccperiodfeepayment OWNER TO sylvia;

--
-- TOC entry 325 (class 1259 OID 108441)
-- Name: moneyoccperiodtypefee; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.moneyoccperiodtypefee (
    fee_feeid integer NOT NULL,
    occperiodtype_typeid integer NOT NULL,
    active boolean,
    autoassign boolean
);


ALTER TABLE public.moneyoccperiodtypefee OWNER TO sylvia;

--
-- TOC entry 318 (class 1259 OID 107629)
-- Name: moneyoccperiodtypefeeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.moneyoccperiodtypefeeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.moneyoccperiodtypefeeid_seq OWNER TO sylvia;

--
-- TOC entry 241 (class 1259 OID 65930)
-- Name: payment_paymentid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.payment_paymentid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.payment_paymentid_seq OWNER TO sylvia;

--
-- TOC entry 242 (class 1259 OID 65932)
-- Name: moneypayment; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.moneypayment OWNER TO sylvia;

--
-- TOC entry 239 (class 1259 OID 65919)
-- Name: paymenttype_typeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.paymenttype_typeid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.paymenttype_typeid_seq OWNER TO sylvia;

--
-- TOC entry 240 (class 1259 OID 65921)
-- Name: moneypaymenttype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.moneypaymenttype (
    typeid integer DEFAULT nextval('public.paymenttype_typeid_seq'::regclass) NOT NULL,
    pmttypetitle text NOT NULL
);


ALTER TABLE public.moneypaymenttype OWNER TO sylvia;

--
-- TOC entry 328 (class 1259 OID 108624)
-- Name: muni_muniprofile_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.muni_muniprofile_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.muni_muniprofile_seq OWNER TO sylvia;

--
-- TOC entry 183 (class 1259 OID 65115)
-- Name: municipality; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.municipality OWNER TO sylvia;

--
-- TOC entry 422 (class 1259 OID 172537)
-- Name: municitystatezip; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.municitystatezip (
    muni_municode integer NOT NULL,
    citystatezip_id integer NOT NULL
);


ALTER TABLE public.municitystatezip OWNER TO sylvia;

--
-- TOC entry 335 (class 1259 OID 108968)
-- Name: municourtentity; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.municourtentity (
    muni_municode integer NOT NULL,
    courtentity_entityid integer NOT NULL,
    relativeorder integer
);


ALTER TABLE public.municourtentity OWNER TO sylvia;

--
-- TOC entry 270 (class 1259 OID 87079)
-- Name: munilogin; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.munilogin OWNER TO sylvia;

--
-- TOC entry 397 (class 1259 OID 155324)
-- Name: munipdfdoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.munipdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    muni_municode integer NOT NULL
);


ALTER TABLE public.munipdfdoc OWNER TO sylvia;

--
-- TOC entry 293 (class 1259 OID 106434)
-- Name: muniphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.muniphotodoc (
    photodoc_photodocid integer NOT NULL,
    muni_municode integer NOT NULL
);


ALTER TABLE public.muniphotodoc OWNER TO sylvia;

--
-- TOC entry 334 (class 1259 OID 108911)
-- Name: muniprofile; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.muniprofile OWNER TO sylvia;

--
-- TOC entry 338 (class 1259 OID 110237)
-- Name: muniprofileeventruleset; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.muniprofileeventruleset (
    muniprofile_profileid integer NOT NULL,
    ruleset_setid integer NOT NULL,
    cedefault boolean DEFAULT true
);


ALTER TABLE public.muniprofileeventruleset OWNER TO sylvia;

--
-- TOC entry 339 (class 1259 OID 110252)
-- Name: muniprofileoccperiodtype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.muniprofileoccperiodtype (
    muniprofile_profileid integer NOT NULL,
    occperiodtype_typeid integer NOT NULL
);


ALTER TABLE public.muniprofileoccperiodtype OWNER TO sylvia;

--
-- TOC entry 182 (class 1259 OID 52232)
-- Name: noticeofviolation_noticeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.noticeofviolation_noticeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.noticeofviolation_noticeid_seq OWNER TO sylvia;

--
-- TOC entry 222 (class 1259 OID 65673)
-- Name: noticeofviolation; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.noticeofviolation OWNER TO sylvia;

--
-- TOC entry 292 (class 1259 OID 106363)
-- Name: noticeofviolationcodeviolation; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.noticeofviolationcodeviolation (
    noticeofviolation_noticeid integer NOT NULL,
    codeviolation_violationid integer NOT NULL,
    includeordtext boolean DEFAULT true,
    includehumanfriendlyordtext boolean DEFAULT false,
    includeviolationphoto boolean DEFAULT false
);


ALTER TABLE public.noticeofviolationcodeviolation OWNER TO sylvia;

--
-- TOC entry 228 (class 1259 OID 65761)
-- Name: occchecklist; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.occchecklist OWNER TO sylvia;

--
-- TOC entry 428 (class 1259 OID 172650)
-- Name: occchecklist_photorequirement_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occchecklist_photorequirement_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occchecklist_photorequirement_seq OWNER TO sylvia;

--
-- TOC entry 431 (class 1259 OID 172713)
-- Name: occchecklistphotorequirement; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occchecklistphotorequirement (
    occchecklist_checklistid integer NOT NULL,
    occphotorequirement_reqid integer NOT NULL
);


ALTER TABLE public.occchecklistphotorequirement OWNER TO sylvia;

--
-- TOC entry 230 (class 1259 OID 65772)
-- Name: occchecklistspacetype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occchecklistspacetype (
    checklistspacetypeid integer DEFAULT nextval('public.chkliststiceid_seq'::regclass) NOT NULL,
    checklist_id integer NOT NULL,
    required boolean,
    spacetype_typeid integer NOT NULL,
    notes text
);


ALTER TABLE public.occchecklistspacetype OWNER TO sylvia;

--
-- TOC entry 226 (class 1259 OID 65739)
-- Name: spaceelement_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.spaceelement_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.spaceelement_seq OWNER TO sylvia;

--
-- TOC entry 266 (class 1259 OID 74771)
-- Name: occchecklistspacetypeelement; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occchecklistspacetypeelement (
    spaceelementid integer DEFAULT nextval('public.spaceelement_seq'::regclass) NOT NULL,
    required boolean DEFAULT true,
    checklistspacetype_typeid integer,
    notes text,
    codesetelement_seteleid integer
);


ALTER TABLE public.occchecklistspacetypeelement OWNER TO sylvia;

--
-- TOC entry 306 (class 1259 OID 106936)
-- Name: occevent_eventid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occevent_eventid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occevent_eventid_seq OWNER TO sylvia;

--
-- TOC entry 307 (class 1259 OID 106984)
-- Name: occeventproposalimplementation_id_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occeventproposalimplementation_id_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occeventproposalimplementation_id_seq OWNER TO sylvia;

--
-- TOC entry 340 (class 1259 OID 110397)
-- Name: occinspectedspace_pk_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occinspectedspace_pk_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occinspectedspace_pk_seq OWNER TO sylvia;

--
-- TOC entry 341 (class 1259 OID 110399)
-- Name: occinspectedspace; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occinspectedspace (
    inspectedspaceid integer DEFAULT nextval('public.occinspectedspace_pk_seq'::regclass) NOT NULL,
    occinspection_inspectionid integer NOT NULL,
    occlocationdescription_descid integer NOT NULL,
    addedtochecklistby_userid integer NOT NULL,
    addedtochecklistts timestamp with time zone,
    occchecklistspacetype_chklstspctypid integer
);


ALTER TABLE public.occinspectedspace OWNER TO sylvia;

--
-- TOC entry 254 (class 1259 OID 66246)
-- Name: occinspectedspaceelement; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.occinspectedspaceelement OWNER TO sylvia;

--
-- TOC entry 276 (class 1259 OID 95426)
-- Name: occinspectedspaceelementphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occinspectedspaceelementphotodoc (
    photodoc_photodocid integer NOT NULL,
    inspectedspaceelement_elementid integer NOT NULL,
    phototype public.occinspectionphototype
);


ALTER TABLE public.occinspectedspaceelementphotodoc OWNER TO sylvia;

--
-- TOC entry 223 (class 1259 OID 65687)
-- Name: occupancyinspectionid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occupancyinspectionid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occupancyinspectionid_seq OWNER TO sylvia;

--
-- TOC entry 305 (class 1259 OID 106901)
-- Name: occinspection; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.occinspection OWNER TO sylvia;

--
-- TOC entry 426 (class 1259 OID 172624)
-- Name: occinspection_determination_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occinspection_determination_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occinspection_determination_seq OWNER TO sylvia;

--
-- TOC entry 424 (class 1259 OID 172613)
-- Name: occinspectioncause_causeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occinspectioncause_causeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occinspectioncause_causeid_seq OWNER TO sylvia;

--
-- TOC entry 425 (class 1259 OID 172615)
-- Name: occinspectioncause; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occinspectioncause (
    causeid integer DEFAULT nextval('public.occinspectioncause_causeid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    notes text,
    active boolean
);


ALTER TABLE public.occinspectioncause OWNER TO sylvia;

--
-- TOC entry 427 (class 1259 OID 172626)
-- Name: occinspectiondetermination; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occinspectiondetermination (
    determinationid integer DEFAULT nextval('public.occinspection_determination_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    notes text,
    eventcat_catid integer,
    active boolean
);


ALTER TABLE public.occinspectiondetermination OWNER TO sylvia;

--
-- TOC entry 444 (class 1259 OID 180871)
-- Name: occinspectiondispatch_dispatchid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occinspectiondispatch_dispatchid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occinspectiondispatch_dispatchid_seq OWNER TO sylvia;

--
-- TOC entry 445 (class 1259 OID 180897)
-- Name: occinspectiondispatch; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occinspectiondispatch (
    dispatchid integer DEFAULT nextval('public.occinspectiondispatch_dispatchid_seq'::regclass) NOT NULL,
    createdby_userid integer NOT NULL,
    creationts timestamp with time zone NOT NULL,
    dispatchnotes text,
    inspection_inspectionid integer NOT NULL,
    retrievalts timestamp with time zone,
    retrievedby_userid integer,
    synchronizationts timestamp with time zone,
    synchronizationnotes text,
    municipality_municode integer NOT NULL,
    municipalityname text
);


ALTER TABLE public.occinspectiondispatch OWNER TO sylvia;

--
-- TOC entry 430 (class 1259 OID 172661)
-- Name: occinspectionphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occinspectionphotodoc (
    photodoc_photodocid integer NOT NULL,
    inspection_inspectionid integer NOT NULL,
    photorequirement_requirementid integer
);


ALTER TABLE public.occinspectionphotodoc OWNER TO sylvia;

--
-- TOC entry 423 (class 1259 OID 172595)
-- Name: occinspectionpropertystatus; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occinspectionpropertystatus (
    occinspection_inspectionid integer NOT NULL,
    propertystatus_statusid integer NOT NULL,
    notes text
);


ALTER TABLE public.occinspectionpropertystatus OWNER TO sylvia;

--
-- TOC entry 268 (class 1259 OID 74792)
-- Name: occlocationdescriptor; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occlocationdescriptor (
    locationdescriptionid integer DEFAULT nextval('public.locationdescription_id_seq'::regclass) NOT NULL,
    description text,
    buildingfloorno integer
);


ALTER TABLE public.occlocationdescriptor OWNER TO sylvia;

--
-- TOC entry 301 (class 1259 OID 106806)
-- Name: occperiodid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occperiodid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occperiodid_seq OWNER TO sylvia;

--
-- TOC entry 302 (class 1259 OID 106808)
-- Name: occperiod; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.occperiod OWNER TO sylvia;

--
-- TOC entry 353 (class 1259 OID 143827)
-- Name: occperiodeventrule; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.occperiodeventrule OWNER TO sylvia;

--
-- TOC entry 347 (class 1259 OID 110730)
-- Name: occperiodeventrule_operid_pk_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occperiodeventrule_operid_pk_seq
    START WITH 100
    INCREMENT BY 10
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occperiodeventrule_operid_pk_seq OWNER TO sylvia;

--
-- TOC entry 398 (class 1259 OID 155354)
-- Name: occperiodpdfdoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occperiodpdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    occperiod_periodid integer NOT NULL
);


ALTER TABLE public.occperiodpdfdoc OWNER TO sylvia;

--
-- TOC entry 316 (class 1259 OID 107421)
-- Name: occperiodpermitapplication; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occperiodpermitapplication (
    occperiod_periodid integer NOT NULL,
    occpermitapp_applicationid integer NOT NULL
);


ALTER TABLE public.occperiodpermitapplication OWNER TO sylvia;

--
-- TOC entry 308 (class 1259 OID 107163)
-- Name: occperiodphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occperiodphotodoc (
    photodoc_photodocid integer NOT NULL,
    occperiod_periodid integer NOT NULL
);


ALTER TABLE public.occperiodphotodoc OWNER TO sylvia;

--
-- TOC entry 303 (class 1259 OID 106872)
-- Name: occperiodtypeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occperiodtypeid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occperiodtypeid_seq OWNER TO sylvia;

--
-- TOC entry 304 (class 1259 OID 106874)
-- Name: occperiodtype; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.occperiodtype OWNER TO sylvia;

--
-- TOC entry 234 (class 1259 OID 65842)
-- Name: occupancypermit_permitid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occupancypermit_permitid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occupancypermit_permitid_seq OWNER TO sylvia;

--
-- TOC entry 309 (class 1259 OID 107211)
-- Name: occpermit; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.occpermit OWNER TO sylvia;

--
-- TOC entry 237 (class 1259 OID 65880)
-- Name: occpermitapp_appid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occpermitapp_appid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occpermitapp_appid_seq OWNER TO sylvia;

--
-- TOC entry 238 (class 1259 OID 65882)
-- Name: occpermitapplication; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.occpermitapplication OWNER TO sylvia;

--
-- TOC entry 414 (class 1259 OID 164038)
-- Name: occpermitapplicationhuman; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occpermitapplicationhuman (
    occpermitapplication_applicationid integer NOT NULL,
    human_humanid integer NOT NULL,
    applicant boolean,
    preferredcontact boolean,
    applicationpersontype public.persontype DEFAULT 'Other'::public.persontype NOT NULL,
    active boolean
);


ALTER TABLE public.occpermitapplicationhuman OWNER TO sylvia;

--
-- TOC entry 235 (class 1259 OID 65868)
-- Name: occpermitpublicreason_reasonid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occpermitpublicreason_reasonid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occpermitpublicreason_reasonid_seq OWNER TO sylvia;

--
-- TOC entry 236 (class 1259 OID 65870)
-- Name: occpermitapplicationreason; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.occpermitapplicationreason OWNER TO sylvia;

--
-- TOC entry 429 (class 1259 OID 172652)
-- Name: occphotorequirement; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occphotorequirement (
    requirementid integer DEFAULT nextval('public.occchecklist_photorequirement_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    notes text,
    required boolean,
    active boolean
);


ALTER TABLE public.occphotorequirement OWNER TO sylvia;

--
-- TOC entry 224 (class 1259 OID 65728)
-- Name: spacetype_spacetypeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.spacetype_spacetypeid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.spacetype_spacetypeid_seq OWNER TO sylvia;

--
-- TOC entry 225 (class 1259 OID 65730)
-- Name: occspacetype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occspacetype (
    spacetypeid integer DEFAULT nextval('public.spacetype_spacetypeid_seq'::regclass) NOT NULL,
    spacetitle text NOT NULL,
    description text NOT NULL
);


ALTER TABLE public.occspacetype OWNER TO sylvia;

--
-- TOC entry 256 (class 1259 OID 66542)
-- Name: occupancyinspectionstatusid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occupancyinspectionstatusid_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occupancyinspectionstatusid_seq OWNER TO sylvia;

--
-- TOC entry 233 (class 1259 OID 65826)
-- Name: occupancypermittype_typeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.occupancypermittype_typeid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.occupancypermittype_typeid_seq OWNER TO sylvia;

--
-- TOC entry 362 (class 1259 OID 154132)
-- Name: parcel_parcelkey_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.parcel_parcelkey_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcel_parcelkey_seq OWNER TO sylvia;

--
-- TOC entry 363 (class 1259 OID 154134)
-- Name: parcel; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.parcel OWNER TO sylvia;

--
-- TOC entry 366 (class 1259 OID 154211)
-- Name: parcelhumanrole_roleid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.parcelhumanrole_roleid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelhumanrole_roleid_seq OWNER TO sylvia;

--
-- TOC entry 375 (class 1259 OID 154605)
-- Name: parcelinfo_infoid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.parcelinfo_infoid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelinfo_infoid_seq OWNER TO sylvia;

--
-- TOC entry 376 (class 1259 OID 154607)
-- Name: parcelinfo; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.parcelinfo OWNER TO sylvia;

--
-- TOC entry 417 (class 1259 OID 164153)
-- Name: parcelmailing_linkid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.parcelmailing_linkid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelmailing_linkid_seq OWNER TO sylvia;

--
-- TOC entry 378 (class 1259 OID 154752)
-- Name: parcelmailingaddress; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.parcelmailingaddress OWNER TO sylvia;

--
-- TOC entry 402 (class 1259 OID 163679)
-- Name: parcelmailingaddressid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.parcelmailingaddressid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelmailingaddressid_seq OWNER TO sylvia;

--
-- TOC entry 436 (class 1259 OID 172839)
-- Name: parcelmigrationlog_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.parcelmigrationlog_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelmigrationlog_seq OWNER TO sylvia;

--
-- TOC entry 437 (class 1259 OID 172841)
-- Name: parcelmigrationlog; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.parcelmigrationlog (
    logentryid integer DEFAULT nextval('public.parcelmigrationlog_seq'::regclass) NOT NULL,
    property_id integer,
    parcel_id integer,
    error_code integer,
    notes text,
    ts timestamp with time zone NOT NULL
);


ALTER TABLE public.parcelmigrationlog OWNER TO sylvia;

--
-- TOC entry 435 (class 1259 OID 172830)
-- Name: parcelmigrationlogerrorcode; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.parcelmigrationlogerrorcode (
    code integer NOT NULL,
    descr text NOT NULL,
    fatal boolean DEFAULT true
);


ALTER TABLE public.parcelmigrationlogerrorcode OWNER TO sylvia;

--
-- TOC entry 399 (class 1259 OID 155369)
-- Name: parcelpdfdoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.parcelpdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    parcel_parcelkey integer NOT NULL
);


ALTER TABLE public.parcelpdfdoc OWNER TO sylvia;

--
-- TOC entry 400 (class 1259 OID 155384)
-- Name: parcelphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.parcelphotodoc (
    photodoc_photodocid integer NOT NULL,
    parcel_parcelkey integer NOT NULL
);


ALTER TABLE public.parcelphotodoc OWNER TO sylvia;

--
-- TOC entry 379 (class 1259 OID 154801)
-- Name: parcelunit_unitid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.parcelunit_unitid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.parcelunit_unitid_seq OWNER TO sylvia;

--
-- TOC entry 380 (class 1259 OID 154803)
-- Name: parcelunit; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.parcelunit OWNER TO sylvia;

--
-- TOC entry 219 (class 1259 OID 65647)
-- Name: photodoc_photodocid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.photodoc_photodocid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.photodoc_photodocid_seq OWNER TO sylvia;

--
-- TOC entry 394 (class 1259 OID 155277)
-- Name: pdfdoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.pdfdoc (
    pdfdocid integer DEFAULT nextval('public.photodoc_photodocid_seq'::regclass) NOT NULL,
    pdfdocdescription character varying(100),
    pdfdoccommitted boolean DEFAULT true,
    blobbytes_bytesid integer,
    muni_municode integer
);


ALTER TABLE public.pdfdoc OWNER TO sylvia;

--
-- TOC entry 289 (class 1259 OID 106285)
-- Name: person_clone_merge_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.person_clone_merge_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.person_clone_merge_seq OWNER TO sylvia;

--
-- TOC entry 193 (class 1259 OID 65283)
-- Name: propertunit_unitid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.propertunit_unitid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertunit_unitid_seq OWNER TO sylvia;

--
-- TOC entry 357 (class 1259 OID 144091)
-- Name: personchange; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.personchange OWNER TO sylvia;

--
-- TOC entry 356 (class 1259 OID 144089)
-- Name: personchangeid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.personchangeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personchangeid_seq OWNER TO sylvia;

--
-- TOC entry 433 (class 1259 OID 172742)
-- Name: personhumanmigrationlog_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.personhumanmigrationlog_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personhumanmigrationlog_seq OWNER TO sylvia;

--
-- TOC entry 434 (class 1259 OID 172744)
-- Name: personhumanmigrationlog; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.personhumanmigrationlog (
    logentryid integer DEFAULT nextval('public.personhumanmigrationlog_seq'::regclass) NOT NULL,
    human_humanid integer,
    person_personid integer,
    error_code integer,
    notes text,
    ts timestamp with time zone
);


ALTER TABLE public.personhumanmigrationlog OWNER TO sylvia;

--
-- TOC entry 432 (class 1259 OID 172733)
-- Name: personhumanmigrationlogerrorcode; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.personhumanmigrationlogerrorcode (
    code integer NOT NULL,
    descr text NOT NULL,
    fatal boolean DEFAULT true
);


ALTER TABLE public.personhumanmigrationlogerrorcode OWNER TO sylvia;

--
-- TOC entry 290 (class 1259 OID 106315)
-- Name: personmergehistory; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.personmergehistory (
    mergeid integer DEFAULT nextval('public.person_clone_merge_seq'::regclass) NOT NULL,
    mergetarget_personid integer,
    mergesource_personid integer,
    mergby_userid integer,
    mergetimestamp timestamp with time zone,
    mergenotes text
);


ALTER TABLE public.personmergehistory OWNER TO sylvia;

--
-- TOC entry 291 (class 1259 OID 106344)
-- Name: personmunilink; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.personmunilink (
    muni_municode integer NOT NULL,
    person_personid integer NOT NULL,
    defaultmuni boolean DEFAULT false
);


ALTER TABLE public.personmunilink OWNER TO sylvia;

--
-- TOC entry 273 (class 1259 OID 87147)
-- Name: personsource_sourceid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.personsource_sourceid_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personsource_sourceid_seq OWNER TO sylvia;

--
-- TOC entry 220 (class 1259 OID 65649)
-- Name: photodoc; Type: TABLE; Schema: public; Owner: sylvia
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
    deactivatedby_userid integer
);


ALTER TABLE public.photodoc OWNER TO sylvia;

--
-- TOC entry 294 (class 1259 OID 106455)
-- Name: printstyle_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.printstyle_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.printstyle_seq OWNER TO sylvia;

--
-- TOC entry 295 (class 1259 OID 106472)
-- Name: printstyle; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.printstyle OWNER TO sylvia;

--
-- TOC entry 281 (class 1259 OID 103877)
-- Name: propertyid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.propertyid_seq
    START WITH 10000
    INCREMENT BY 1
    MINVALUE 10000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertyid_seq OWNER TO sylvia;

--
-- TOC entry 190 (class 1259 OID 65248)
-- Name: property; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.property OWNER TO sylvia;

--
-- TOC entry 191 (class 1259 OID 65267)
-- Name: propertyexternaldata_extdataid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.propertyexternaldata_extdataid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertyexternaldata_extdataid_seq OWNER TO sylvia;

--
-- TOC entry 192 (class 1259 OID 65269)
-- Name: propertyexternaldata; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.propertyexternaldata OWNER TO sylvia;

--
-- TOC entry 344 (class 1259 OID 110612)
-- Name: propertyotherid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.propertyotherid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertyotherid_seq OWNER TO sylvia;

--
-- TOC entry 345 (class 1259 OID 110614)
-- Name: propertyotherid; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.propertyotherid OWNER TO sylvia;

--
-- TOC entry 401 (class 1259 OID 155399)
-- Name: propertypdfdoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.propertypdfdoc (
    pdfdoc_pdfdocid integer NOT NULL,
    property_propertyid integer NOT NULL
);


ALTER TABLE public.propertypdfdoc OWNER TO sylvia;

--
-- TOC entry 195 (class 1259 OID 65299)
-- Name: propertyperson; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.propertyperson (
    property_propertyid integer NOT NULL,
    person_personid integer NOT NULL,
    creationts timestamp with time zone
);


ALTER TABLE public.propertyperson OWNER TO sylvia;

--
-- TOC entry 221 (class 1259 OID 65658)
-- Name: propertyphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.propertyphotodoc (
    photodoc_photodocid integer NOT NULL,
    property_propertyid integer NOT NULL
);


ALTER TABLE public.propertyphotodoc OWNER TO sylvia;

--
-- TOC entry 342 (class 1259 OID 110574)
-- Name: propertystatusid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.propertystatusid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertystatusid_seq OWNER TO sylvia;

--
-- TOC entry 343 (class 1259 OID 110576)
-- Name: propertystatus; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.propertystatus OWNER TO sylvia;

--
-- TOC entry 194 (class 1259 OID 65285)
-- Name: propertyunit; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.propertyunit OWNER TO sylvia;

--
-- TOC entry 336 (class 1259 OID 109023)
-- Name: propertyunitchange; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.propertyunitchange OWNER TO sylvia;

--
-- TOC entry 188 (class 1259 OID 65240)
-- Name: propertyusetype_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.propertyusetype_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertyusetype_seq OWNER TO sylvia;

--
-- TOC entry 189 (class 1259 OID 65242)
-- Name: propertyusetype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.propertyusetype (
    propertyusetypeid integer DEFAULT nextval('public.propertyusetype_seq'::regclass) NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(100),
    icon_iconid integer,
    zoneclass text,
    active boolean DEFAULT true
);


ALTER TABLE public.propertyusetype OWNER TO sylvia;

--
-- TOC entry 255 (class 1259 OID 66299)
-- Name: propevent_eventid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.propevent_eventid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propevent_eventid_seq OWNER TO sylvia;

--
-- TOC entry 265 (class 1259 OID 74755)
-- Name: spaceid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.spaceid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.spaceid_seq OWNER TO sylvia;

--
-- TOC entry 358 (class 1259 OID 144121)
-- Name: taxstatus_taxstatusid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.taxstatus_taxstatusid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.taxstatus_taxstatusid_seq OWNER TO sylvia;

--
-- TOC entry 359 (class 1259 OID 144123)
-- Name: taxstatus; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.taxstatus OWNER TO sylvia;

--
-- TOC entry 4427 (class 0 OID 0)
-- Dependencies: 359
-- Name: TABLE taxstatus; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON TABLE public.taxstatus IS 'Scraped data from Allegheny County http://www2.alleghenycounty.us/RealEstate/. Description valid as of August 2020';


--
-- TOC entry 249 (class 1259 OID 66107)
-- Name: textblock_blockid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.textblock_blockid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.textblock_blockid_seq OWNER TO sylvia;

--
-- TOC entry 251 (class 1259 OID 66145)
-- Name: textblock; Type: TABLE; Schema: public; Owner: sylvia
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


ALTER TABLE public.textblock OWNER TO sylvia;

--
-- TOC entry 250 (class 1259 OID 66109)
-- Name: textblockcategory; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.textblockcategory (
    categoryid integer DEFAULT nextval('public.blockcategory_categoryid_seq'::regclass) NOT NULL,
    categorytitle text NOT NULL,
    icon_iconid integer,
    muni_municode integer
);


ALTER TABLE public.textblockcategory OWNER TO sylvia;

--
-- TOC entry 4422 (class 0 OID 0)
-- Dependencies: 8
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2022-02-25 12:16:17 EST

--
-- PostgreSQL database dump complete
--

