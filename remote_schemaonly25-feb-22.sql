--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.25
-- Dumped by pg_dump version 11.5 (Ubuntu 11.5-1.pgdg16.04+1)

-- Started on 2022-02-25 12:14:00 EST

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
-- TOC entry 3 (class 3079 OID 38806)
-- Name: pgcrypto; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;


--
-- TOC entry 5281 (class 0 OID 0)
-- Dependencies: 3
-- Name: EXTENSION pgcrypto; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION pgcrypto IS 'cryptographic functions';


--
-- TOC entry 2 (class 3079 OID 39500)
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- TOC entry 5282 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry, geography, and raster spatial types and functions';


--
-- TOC entry 1933 (class 1247 OID 20484)
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
    'LegacyImported',
    'CountySiteImport'
);


ALTER TYPE public.casephase OWNER TO sylvia;

--
-- TOC entry 1992 (class 1247 OID 20516)
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
-- TOC entry 2456 (class 1247 OID 40883)
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
-- TOC entry 1942 (class 1247 OID 20538)
-- Name: persontype; Type: TYPE; Schema: public; Owner: sylvia
--

CREATE TYPE public.persontype AS ENUM (
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
    'LegacyAgent',
    'LegacyOwner'
);


ALTER TYPE public.persontype OWNER TO sylvia;

--
-- TOC entry 1945 (class 1247 OID 20564)
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
-- TOC entry 1948 (class 1247 OID 20576)
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
-- TOC entry 418 (class 1255 OID 31227)
-- Name: build_code_guide(); Type: FUNCTION; Schema: public; Owner: sylvia
--

CREATE FUNCTION public.build_code_guide() RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
 elementrow RECORD;
BEGIN
 RAISE NOTICE 'starting transfer...';
 FOR elementrow IN SELECT ordsectitle, ordsubsectitle, ordhumanfriendlytext 
 FROM codeelement WHERE codesource_sourceid = 10 LOOP
	EXECUTE format('INSERT INTO codeelementguide (category, subcategory, inspectionguidelines) VALUES (%L, %L, %L)', elementrow.ordsectitle, elementrow.ordsubsectitle, elementrow.ordhumanfriendlytext); 
	END LOOP;
RETURN 1;
END;
$$;


ALTER FUNCTION public.build_code_guide() OWNER TO sylvia;

--
-- TOC entry 1577 (class 1255 OID 42136)
-- Name: cast_array_to_array_literal(anyarray); Type: FUNCTION; Schema: public; Owner: sylvia
--

CREATE FUNCTION public.cast_array_to_array_literal(value anyarray) RETURNS text
    LANGUAGE plpgsql
    AS $$
	BEGIN
		RETURN value::TEXT;
	END;
	$$;


ALTER FUNCTION public.cast_array_to_array_literal(value anyarray) OWNER TO sylvia;

--
-- TOC entry 434 (class 1255 OID 38804)
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
-- TOC entry 248 (class 1259 OID 20937)
-- Name: person_personidseq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.person_personidseq
    START WITH 120000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.person_personidseq OWNER TO sylvia;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 249 (class 1259 OID 20939)
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
-- TOC entry 490 (class 1255 OID 34346)
-- Name: createcloneperson(public.person, integer); Type: FUNCTION; Schema: public; Owner: sylvia
--

CREATE FUNCTION public.createcloneperson(person_row public.person, userid integer) RETURNS integer
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
		    NULL, NULL, NULL, person_row.personid, userid, now(),
		    FALSE);

	    newpersonid :=currval('person_personidseq');

	    RETURN newpersonid;

END;
$$;


ALTER FUNCTION public.createcloneperson(person_row public.person, userid integer) OWNER TO sylvia;

--
-- TOC entry 462 (class 1255 OID 34196)
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
-- TOC entry 230 (class 1259 OID 20822)
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
-- TOC entry 231 (class 1259 OID 20824)
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
    userrole public.role,
    lastupdatedts timestamp with time zone DEFAULT now(),
    homemuni integer
);


ALTER TABLE public.login OWNER TO sylvia;

--
-- TOC entry 436 (class 1255 OID 31535)
-- Name: generateuserperson(public.login); Type: FUNCTION; Schema: public; Owner: sylvia
--

CREATE FUNCTION public.generateuserperson(login_row public.login) RETURNS integer
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
		    expirynotes, creationtimestamp, canexpire, userlink)
	    VALUES (DEFAULT, 'User'::persontype, login_row.muni_municode, login_row.fname, login_row.lname, login_row.worktitle, 
		    login_row.phonecell, login_row.phonehome, login_row.phonework, login_row.email, login_row.address_street, login_row.address_city, 
		    login_row.address_state, login_row.address_zip, login_row.notes, now(), '01-01-2021', TRUE, 
		    FALSE, 100, FALSE, 13 , 100, 
		    FALSE, NULL, NULL, 
		    NULL, NULL, FALSE, 
		    NULL, now(), FALSE, login_row.userid);

	    newpersonid :=currval('person_personidseq');

	    UPDATE public.login SET personlink=newpersonid WHERE login.userid = login_row.userid;

	    RETURN newpersonid;

END;


$$;


ALTER FUNCTION public.generateuserperson(login_row public.login) OWNER TO sylvia;

--
-- TOC entry 438 (class 1255 OID 38805)
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
-- TOC entry 419 (class 1255 OID 31237)
-- Name: populate_element_ids(); Type: FUNCTION; Schema: public; Owner: sylvia
--

CREATE FUNCTION public.populate_element_ids() RETURNS integer
    LANGUAGE plpgsql
    AS $$
DECLARE
 guiderow RECORD;
BEGIN
 RAISE NOTICE 'starting transfer...';
 FOR guiderow IN SELECT guideentryid, category, subcategory 
 FROM codeelementguide LOOP
	EXECUTE format('UPDATE codeelement SET guideentryid = %L WHERE ordsectitle = %L AND ordsubsectitle = %L ', guiderow.guideentryid, guiderow.category, guiderow.subcategory); 
	END LOOP;
RETURN 1;
END;
$$;


ALTER FUNCTION public.populate_element_ids() OWNER TO sylvia;

--
-- TOC entry 183 (class 1259 OID 20591)
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
-- TOC entry 407 (class 1259 OID 42546)
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
-- TOC entry 408 (class 1259 OID 42562)
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
-- TOC entry 275 (class 1259 OID 31298)
-- Name: blobtype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.blobtype (
    typeid integer NOT NULL,
    typetitle text,
    icon_iconid integer
);


ALTER TABLE public.blobtype OWNER TO sylvia;

--
-- TOC entry 185 (class 1259 OID 20600)
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
-- TOC entry 299 (class 1259 OID 35628)
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
-- TOC entry 300 (class 1259 OID 35630)
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
-- TOC entry 188 (class 1259 OID 20613)
-- Name: cecase_caseid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.cecase_caseid_seq
    START WITH 21200
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.cecase_caseid_seq OWNER TO sylvia;

--
-- TOC entry 189 (class 1259 OID 20615)
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
    allowuplinkaccess boolean,
    propertyinfocase boolean,
    personinfocase_personid integer,
    bobsource_sourceid integer,
    active boolean DEFAULT true,
    lastupdatedby_userid integer,
    lastupdatedts timestamp with time zone
);


ALTER TABLE public.cecase OWNER TO sylvia;

--
-- TOC entry 215 (class 1259 OID 20745)
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
-- TOC entry 216 (class 1259 OID 20747)
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
-- TOC entry 378 (class 1259 OID 40965)
-- Name: casedata; Type: VIEW; Schema: public; Owner: sylvia
--

CREATE VIEW public.casedata AS
 SELECT ce.property_propertyid AS propertyid,
    ce.caseid,
    co.violationid,
    ce.casename,
    co.description,
    ce.originationdate,
    ce.closingdate,
    co.entrytimestamp,
    co.stipulatedcompliancedate,
    co.actualcompliancedate
   FROM (public.cecase ce
     FULL JOIN public.codeviolation co ON ((ce.caseid = co.cecase_caseid)))
  GROUP BY ce.property_propertyid, ce.caseid, co.violationid, ce.casename, co.description, ce.originationdate, ce.closingdate, co.entrytimestamp, co.stipulatedcompliancedate, co.actualcompliancedate;


ALTER TABLE public.casedata OWNER TO sylvia;

--
-- TOC entry 186 (class 1259 OID 20602)
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
-- TOC entry 187 (class 1259 OID 20604)
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
-- TOC entry 184 (class 1259 OID 20593)
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
-- TOC entry 278 (class 1259 OID 31341)
-- Name: ceactionrequestphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.ceactionrequestphotodoc (
    photodoc_photodocid integer NOT NULL,
    ceactionrequest_requestid integer NOT NULL
);


ALTER TABLE public.ceactionrequestphotodoc OWNER TO sylvia;

--
-- TOC entry 270 (class 1259 OID 21549)
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
-- TOC entry 289 (class 1259 OID 34197)
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
-- TOC entry 290 (class 1259 OID 34199)
-- Name: cecasephasechangerule; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.cecasephasechangerule (
    ruleid integer DEFAULT nextval('public.cecasephasechangerule_seq'::regclass) NOT NULL,
    title text,
    targetcasephase public.casephase,
    requiredcurrentcasephase public.casephase,
    forbiddencurrentcasephase public.casephase,
    requiredextanteventtype public.eventtype,
    forbiddenextanteventtype public.eventtype,
    requiredextanteventcat integer,
    forbiddenextanteventcat integer,
    triggeredeventcat integer,
    active boolean DEFAULT true,
    mandatory boolean DEFAULT false,
    treatreqphaseasthreshold boolean DEFAULT false,
    treatforbidphaseasthreshold boolean DEFAULT false,
    rejectrulehostifrulefails boolean DEFAULT true,
    description text,
    triggeredeventcatreqcat integer
);


ALTER TABLE public.cecasephasechangerule OWNER TO sylvia;

--
-- TOC entry 409 (class 1259 OID 42637)
-- Name: cecasephotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.cecasephotodoc (
    photodoc_photodocid integer NOT NULL,
    cecase_caseid integer NOT NULL
);


ALTER TABLE public.cecasephotodoc OWNER TO sylvia;

--
-- TOC entry 285 (class 1259 OID 31624)
-- Name: cecasestatusicon; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.cecasestatusicon (
    iconid integer NOT NULL,
    status public.casephase NOT NULL
);


ALTER TABLE public.cecasestatusicon OWNER TO sylvia;

--
-- TOC entry 190 (class 1259 OID 20622)
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
-- TOC entry 192 (class 1259 OID 20637)
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
-- TOC entry 317 (class 1259 OID 37389)
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
-- TOC entry 195 (class 1259 OID 20656)
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
-- TOC entry 197 (class 1259 OID 20666)
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
-- TOC entry 312 (class 1259 OID 37255)
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
-- TOC entry 313 (class 1259 OID 37257)
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
-- TOC entry 309 (class 1259 OID 37181)
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
-- TOC entry 310 (class 1259 OID 37183)
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
    directtomunisysadmin boolean DEFAULT false,
    requiredevaluationforbobclose boolean DEFAULT true,
    forcehideprecedingproposals boolean DEFAULT false,
    forcehidetrailingproposals boolean DEFAULT false,
    refusetobehidden boolean DEFAULT false,
    relativeorder integer DEFAULT 1
);


ALTER TABLE public.choicedirective OWNER TO sylvia;

--
-- TOC entry 314 (class 1259 OID 37281)
-- Name: choicedirectivechoice; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.choicedirectivechoice (
    choice_choiceid integer NOT NULL,
    directive_directiveid integer NOT NULL
);


ALTER TABLE public.choicedirectivechoice OWNER TO sylvia;

--
-- TOC entry 337 (class 1259 OID 38072)
-- Name: choicedirectivedirectiveset; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.choicedirectivedirectiveset (
    directiveset_setid integer NOT NULL,
    directive_dirid integer NOT NULL
);


ALTER TABLE public.choicedirectivedirectiveset OWNER TO sylvia;

--
-- TOC entry 332 (class 1259 OID 37926)
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
-- TOC entry 333 (class 1259 OID 37928)
-- Name: choicedirectiveset; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.choicedirectiveset (
    directivesetid integer DEFAULT nextval('public.choicedirectivesetid_seq'::regclass) NOT NULL,
    title text,
    description text
);


ALTER TABLE public.choicedirectiveset OWNER TO sylvia;

--
-- TOC entry 348 (class 1259 OID 38648)
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
-- TOC entry 199 (class 1259 OID 20672)
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
-- TOC entry 200 (class 1259 OID 20674)
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
    officialtext text
);


ALTER TABLE public.citation OWNER TO sylvia;

--
-- TOC entry 291 (class 1259 OID 34298)
-- Name: citationperson; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.citationperson (
    citation_citationid integer NOT NULL,
    person_personid integer NOT NULL
);


ALTER TABLE public.citationperson OWNER TO sylvia;

--
-- TOC entry 201 (class 1259 OID 20682)
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
-- TOC entry 202 (class 1259 OID 20684)
-- Name: citationstatus; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.citationstatus (
    statusid integer DEFAULT nextval('public.citationstatus_statusid_seq'::regclass) NOT NULL,
    statusname text NOT NULL,
    description text NOT NULL,
    icon_iconid integer,
    editsforbidden boolean DEFAULT true,
    eventrule_ruleid integer
);


ALTER TABLE public.citationstatus OWNER TO sylvia;

--
-- TOC entry 203 (class 1259 OID 20691)
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
-- TOC entry 204 (class 1259 OID 20693)
-- Name: citationviolation; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.citationviolation (
    citationviolationid integer DEFAULT nextval('public.citationviolation_cvid_seq'::regclass) NOT NULL,
    citation_citationid integer NOT NULL,
    codeviolation_violationid integer NOT NULL
);


ALTER TABLE public.citationviolation OWNER TO sylvia;

--
-- TOC entry 205 (class 1259 OID 20697)
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
-- TOC entry 206 (class 1259 OID 20699)
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
-- TOC entry 207 (class 1259 OID 20708)
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
-- TOC entry 208 (class 1259 OID 20710)
-- Name: codeelementguide; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.codeelementguide (
    guideentryid integer DEFAULT nextval('public.codeelementguide_id_seq'::regclass) NOT NULL,
    category text NOT NULL,
    subcategory text,
    description text,
    enforcementguidelines text,
    inspectionguidelines text,
    priority boolean
);


ALTER TABLE public.codeelementguide OWNER TO sylvia;

--
-- TOC entry 380 (class 1259 OID 41075)
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
-- TOC entry 381 (class 1259 OID 41077)
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
-- TOC entry 209 (class 1259 OID 20717)
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
-- TOC entry 210 (class 1259 OID 20719)
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
-- TOC entry 211 (class 1259 OID 20726)
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
-- TOC entry 212 (class 1259 OID 20728)
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
-- TOC entry 288 (class 1259 OID 32892)
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
-- TOC entry 213 (class 1259 OID 20735)
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
-- TOC entry 214 (class 1259 OID 20737)
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
-- TOC entry 276 (class 1259 OID 31311)
-- Name: codeviolationphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.codeviolationphotodoc (
    photodoc_photodocid integer NOT NULL,
    codeviolation_violationid integer NOT NULL
);


ALTER TABLE public.codeviolationphotodoc OWNER TO sylvia;

--
-- TOC entry 286 (class 1259 OID 32865)
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
-- TOC entry 217 (class 1259 OID 20754)
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
-- TOC entry 393 (class 1259 OID 41857)
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
-- TOC entry 394 (class 1259 OID 41859)
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
-- TOC entry 391 (class 1259 OID 41816)
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
-- TOC entry 392 (class 1259 OID 41818)
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
-- TOC entry 389 (class 1259 OID 41805)
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
-- TOC entry 390 (class 1259 OID 41807)
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
-- TOC entry 218 (class 1259 OID 20764)
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
-- TOC entry 219 (class 1259 OID 20766)
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
-- TOC entry 281 (class 1259 OID 31482)
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
-- TOC entry 191 (class 1259 OID 20624)
-- Name: event; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.event (
    eventid integer DEFAULT nextval('public.ceevent_eventid_seq'::regclass) NOT NULL,
    category_catid integer NOT NULL,
    cecase_caseid integer NOT NULL,
    creationts timestamp with time zone,
    eventdescription text,
    creator_userid integer NOT NULL,
    active boolean DEFAULT true,
    requiresviewconfirmation boolean DEFAULT false,
    notes text,
    requestedevent_eventid integer,
    directrequesttodefaultmuniceo boolean DEFAULT false,
    occperiod_periodid integer,
    timestart timestamp with time zone,
    timeend timestamp with time zone,
    lastupdatedby_userid integer,
    lastupdatedts timestamp with time zone
);


ALTER TABLE public.event OWNER TO sylvia;

--
-- TOC entry 5283 (class 0 OID 0)
-- Dependencies: 191
-- Name: TABLE event; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON TABLE public.event IS 'Table: Code enforcement event

Represents any one of several dozen possible actions that can occur during the lifetime of a code enforcement case, such as adding a violation to the case, the code officer making a phone call to a property manager, or the closing of the case after compliance has been achieved.

Events are the main form of documentation of a code enforcement case. Individual column heades have been commented with detailed explanations.';


--
-- TOC entry 5284 (class 0 OID 0)
-- Dependencies: 191
-- Name: COLUMN event.eventid; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON COLUMN public.event.eventid IS 'Code enforcement event primary key';


--
-- TOC entry 5285 (class 0 OID 0)
-- Dependencies: 191
-- Name: COLUMN event.category_catid; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON COLUMN public.event.category_catid IS 'Foreign key to code enforcement event category table. Events are categorized on two levels: event category (which lives in a DB table) and even type, which lives in a postgres custom type. By keying to the category, the integrator in Java will construct an EventCategory object which contains the Java enum EventType.';


--
-- TOC entry 5286 (class 0 OID 0)
-- Dependencies: 191
-- Name: COLUMN event.cecase_caseid; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON COLUMN public.event.cecase_caseid IS 'Foreign key to the code enforcement case to which this event is attached. Events know about their "host" case and not visa versa.';


--
-- TOC entry 5287 (class 0 OID 0)
-- Dependencies: 191
-- Name: COLUMN event.creationts; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON COLUMN public.event.creationts IS 'The DB-generated time stamp at the moment of event insertion. NOT user editable.';


--
-- TOC entry 5288 (class 0 OID 0)
-- Dependencies: 191
-- Name: COLUMN event.eventdescription; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON COLUMN public.event.eventdescription IS 'The official description of the event which is user-editable and is the primary mechanism for passing information to the user about this event. Descriptions can be 1-3 sentences. Longer info should be stored in the Event notes.';


--
-- TOC entry 5289 (class 0 OID 0)
-- Dependencies: 191
-- Name: COLUMN event.creator_userid; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON COLUMN public.event.creator_userid IS 'FK to login table to connect the event to a User. The owner of the event is usually the creator, but in the case of automatically generated events, the owner is Sylvia, the COG Bot. In some cases, an event that requests that another event be completed may be given an owner corresponding to the User to whom the request is directed, not the actual creator of the event in question.';


--
-- TOC entry 5290 (class 0 OID 0)
-- Dependencies: 191
-- Name: COLUMN event.active; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON COLUMN public.event.active IS 'We don''t delete events, generally, but we can make them inactive. An inactive event is akin to a deleted/removed event but can, obviously, still be resurrected if needed.';


--
-- TOC entry 5291 (class 0 OID 0)
-- Dependencies: 191
-- Name: COLUMN event.requiresviewconfirmation; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON COLUMN public.event.requiresviewconfirmation IS 'DEPRECATED - holdover from the first iteration of action requests';


--
-- TOC entry 5292 (class 0 OID 0)
-- Dependencies: 191
-- Name: COLUMN event.notes; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON COLUMN public.event.notes IS 'User-editable field for capturing additional informatoin about the event which cannot be included in the event description. Use Notes after description is full since Notes are not displayed by default in many event views.';


--
-- TOC entry 5293 (class 0 OID 0)
-- Dependencies: 191
-- Name: COLUMN event.requestedevent_eventid; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON COLUMN public.event.requestedevent_eventid IS 'DEPRECATED - part of old action request infrastructure';


--
-- TOC entry 5294 (class 0 OID 0)
-- Dependencies: 191
-- Name: COLUMN event.directrequesttodefaultmuniceo; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON COLUMN public.event.directrequesttodefaultmuniceo IS 'Used when an event requests an action but that action doesn''t need to be directed at a particular user. Switching this flag to true will tell the routing system: "I don''t care which user responds, as long as they are a code officer with authority in this event''s municipality." This is the default behavior for new events which request an action.';


--
-- TOC entry 193 (class 1259 OID 20639)
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
    requestable boolean DEFAULT false,
    eventrule_ruleid integer,
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
-- TOC entry 194 (class 1259 OID 20653)
-- Name: eventperson; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.eventperson (
    ceevent_eventid integer NOT NULL,
    person_personid integer NOT NULL
);


ALTER TABLE public.eventperson OWNER TO sylvia;

--
-- TOC entry 311 (class 1259 OID 37210)
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
-- TOC entry 355 (class 1259 OID 39195)
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
-- TOC entry 356 (class 1259 OID 39197)
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
-- TOC entry 331 (class 1259 OID 37911)
-- Name: eventruleruleset; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.eventruleruleset (
    ruleset_rulesetid integer NOT NULL,
    eventrule_ruleid integer NOT NULL
);


ALTER TABLE public.eventruleruleset OWNER TO sylvia;

--
-- TOC entry 329 (class 1259 OID 37900)
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
-- TOC entry 330 (class 1259 OID 37902)
-- Name: eventruleset; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.eventruleset (
    rulesetid integer DEFAULT nextval('public.eventrulesetid_seq'::regclass) NOT NULL,
    title text,
    description text
);


ALTER TABLE public.eventruleset OWNER TO sylvia;

--
-- TOC entry 403 (class 1259 OID 42107)
-- Name: externalrealestateportal_id_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.externalrealestateportal_id_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.externalrealestateportal_id_seq OWNER TO sylvia;

--
-- TOC entry 406 (class 1259 OID 42122)
-- Name: externalrealestateportal; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.externalrealestateportal (
    id integer DEFAULT nextval('public.externalrealestateportal_id_seq'::regclass) NOT NULL,
    parcel text NOT NULL,
    creationts timestamp without time zone,
    lastupdatedts timestamp without time zone,
    parcelid text,
    propertyaddress text,
    municipality text,
    ownername text,
    ownermailing text
);


ALTER TABLE public.externalrealestateportal OWNER TO sylvia;

--
-- TOC entry 404 (class 1259 OID 42109)
-- Name: externaltaxstatus_id_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.externaltaxstatus_id_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.externaltaxstatus_id_seq OWNER TO sylvia;

--
-- TOC entry 402 (class 1259 OID 42105)
-- Name: externalwprdc_id_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.externalwprdc_id_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.externalwprdc_id_seq OWNER TO sylvia;

--
-- TOC entry 405 (class 1259 OID 42111)
-- Name: externalwprdc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.externalwprdc (
    id integer DEFAULT nextval('public.externalwprdc_id_seq'::regclass) NOT NULL,
    parcel text NOT NULL,
    creationts timestamp without time zone,
    lastupdatedts timestamp without time zone,
    parid character varying(30),
    propertyhousenum character varying(10),
    propertyfraction character varying(6),
    propertyaddress character varying(80),
    propertycity character varying(50),
    propertystate character varying(50),
    propertyunit character varying(30),
    propertyzip character varying(10),
    municode character varying(5),
    munidesc character varying(50),
    schoolcode character varying(30),
    schooldesc character varying(50),
    legal1 character varying(60),
    legal2 character varying(60),
    legal3 character varying(60),
    neighcode character varying(8),
    neighdesc character varying(50),
    taxcode character varying(1),
    taxdesc character varying(50),
    taxsubcode character varying(1),
    taxsubcode_desc character varying(50),
    ownercode character varying(3),
    ownerdesc character varying(50),
    class character varying(2),
    classdesc character varying(50),
    usecode character varying(4),
    usedesc character varying(50),
    lotarea double precision,
    homesteadflag character varying(6),
    cleangreen character varying(3),
    farmsteadflag character varying(6),
    abatementflag character varying(6),
    recorddate character varying(10),
    saledate character varying(10),
    saleprice double precision,
    salecode character varying(2),
    saledesc character varying(50),
    deedbook character varying(8),
    deedpage character varying(8),
    prevsaledate character varying(10),
    prevsaleprice double precision,
    prevsaledate2 character varying(10),
    prevsaleprice2 double precision,
    changenoticeaddress1 character varying(100),
    changenoticeaddress2 character varying(100),
    changenoticeaddress3 character varying(100),
    changenoticeaddress4 character varying(100),
    countybuilding double precision,
    countyland double precision,
    countytotal double precision,
    countyexemptbldg double precision,
    localbuilding double precision,
    localland double precision,
    localtotal double precision,
    fairmarketbuilding double precision,
    fairmarketland double precision,
    fairmarkettotal double precision,
    style character varying(2),
    styledesc character varying(50),
    stories character varying(3),
    yearblt double precision,
    exteriorfinish character varying(2),
    extfinish_desc character varying(50),
    roof character varying(20),
    roofdesc character varying(50),
    basement character varying(1),
    basementdesc character varying(50),
    grade character varying(3),
    gradedesc character varying(50),
    condition character varying(2),
    conditiondesc character varying(50),
    cdu character varying(2),
    cdudesc character varying(50),
    totalrooms double precision,
    bedrooms double precision,
    fullbaths double precision,
    halfbaths double precision,
    heatingcooling character varying(1),
    heatingcoolingdesc character varying(50),
    fireplaces double precision,
    bsmtgarage character varying(1),
    finishedlivingarea double precision,
    cardnumber double precision,
    alt_id character varying(30),
    taxyear double precision,
    asofdate text
);


ALTER TABLE public.externalwprdc OWNER TO sylvia;

--
-- TOC entry 384 (class 1259 OID 41700)
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
-- TOC entry 385 (class 1259 OID 41702)
-- Name: human; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.human (
    humanid integer DEFAULT nextval('public.human_humanid_seq'::regclass) NOT NULL,
    name text NOT NULL,
    dob date,
    under18 boolean,
    jobtitle text,
    muni_municode integer,
    businessentity boolean DEFAULT false,
    multihuman boolean DEFAULT false,
    source_sourceid integer,
    activationstartdate date,
    activationstartnotes text,
    activationstopdate date,
    activationstopnotes text,
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
-- TOC entry 397 (class 1259 OID 41950)
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
-- TOC entry 401 (class 1259 OID 42024)
-- Name: humanmailingaddress; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.humanmailingaddress (
    humanmailing_humanid integer,
    humanmailing_addressid integer,
    role_humanmailingroleid integer,
    source_sourceid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text
);


ALTER TABLE public.humanmailingaddress OWNER TO sylvia;

--
-- TOC entry 398 (class 1259 OID 41952)
-- Name: humanmailingrole; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.humanmailingrole (
    roleid integer DEFAULT nextval('public.humanmailing_roleid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    createdts timestamp with time zone,
    description text,
    muni_municode integer,
    deactivatedts timestamp with time zone,
    notes text
);


ALTER TABLE public.humanmailingrole OWNER TO sylvia;

--
-- TOC entry 283 (class 1259 OID 31573)
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
-- TOC entry 284 (class 1259 OID 31575)
-- Name: icon; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.icon (
    iconid integer DEFAULT nextval('public.iconid_seq'::regclass) NOT NULL,
    name text,
    styleclass text,
    fontawesome text,
    materialicons text
);


ALTER TABLE public.icon OWNER TO sylvia;

--
-- TOC entry 220 (class 1259 OID 20773)
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
-- TOC entry 221 (class 1259 OID 20775)
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
-- TOC entry 222 (class 1259 OID 20781)
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
-- TOC entry 223 (class 1259 OID 20788)
-- Name: improvementtype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.improvementtype (
    typeid integer NOT NULL,
    typetitle text,
    typedescription text
);


ALTER TABLE public.improvementtype OWNER TO sylvia;

--
-- TOC entry 224 (class 1259 OID 20794)
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
-- TOC entry 287 (class 1259 OID 32867)
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
-- TOC entry 226 (class 1259 OID 20804)
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
-- TOC entry 227 (class 1259 OID 20806)
-- Name: listchangerequest; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.listchangerequest (
    changeid integer DEFAULT nextval('public.listitemchange_seq'::regclass) NOT NULL,
    changetext text
);


ALTER TABLE public.listchangerequest OWNER TO sylvia;

--
-- TOC entry 228 (class 1259 OID 20813)
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
-- TOC entry 272 (class 1259 OID 23122)
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
-- TOC entry 273 (class 1259 OID 23140)
-- Name: logcategory; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.logcategory (
    catid integer NOT NULL,
    title text,
    description text
);


ALTER TABLE public.logcategory OWNER TO sylvia;

--
-- TOC entry 350 (class 1259 OID 38868)
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
-- TOC entry 346 (class 1259 OID 38496)
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
-- TOC entry 349 (class 1259 OID 38741)
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
    assignmentrank integer DEFAULT 1
);


ALTER TABLE public.loginmuniauthperiod OWNER TO sylvia;

--
-- TOC entry 351 (class 1259 OID 38902)
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
-- TOC entry 352 (class 1259 OID 38904)
-- Name: loginmuniauthperiodlog; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.loginmuniauthperiodlog (
    authperiodlogentryid integer DEFAULT nextval('public.loginmuniauthperiodlog_seq'::regclass) NOT NULL,
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
-- TOC entry 279 (class 1259 OID 31362)
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
-- TOC entry 280 (class 1259 OID 31411)
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
-- TOC entry 395 (class 1259 OID 41888)
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
-- TOC entry 396 (class 1259 OID 41890)
-- Name: mailingaddress; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.mailingaddress (
    addressid integer DEFAULT nextval('public.mailingaddress_addressid_seq'::regclass) NOT NULL,
    addressnum text,
    street text,
    unitno text,
    city text,
    state text,
    zipcode text,
    pobox integer,
    verifiedts timestamp with time zone,
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
-- TOC entry 320 (class 1259 OID 37696)
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
-- TOC entry 321 (class 1259 OID 37698)
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
-- TOC entry 327 (class 1259 OID 37787)
-- Name: moneycecasefeepayment; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.moneycecasefeepayment (
    payment_paymentid integer NOT NULL,
    cecaseassignedfee_id integer NOT NULL
);


ALTER TABLE public.moneycecasefeepayment OWNER TO sylvia;

--
-- TOC entry 324 (class 1259 OID 37742)
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
-- TOC entry 234 (class 1259 OID 20849)
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
-- TOC entry 235 (class 1259 OID 20851)
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
-- TOC entry 319 (class 1259 OID 37670)
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
-- TOC entry 322 (class 1259 OID 37724)
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
-- TOC entry 323 (class 1259 OID 37726)
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
-- TOC entry 326 (class 1259 OID 37772)
-- Name: moneyoccperiodfeepayment; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.moneyoccperiodfeepayment (
    payment_paymentid integer NOT NULL,
    occperiodassignedfee_id integer NOT NULL
);


ALTER TABLE public.moneyoccperiodfeepayment OWNER TO sylvia;

--
-- TOC entry 325 (class 1259 OID 37757)
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
-- TOC entry 244 (class 1259 OID 20917)
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
-- TOC entry 245 (class 1259 OID 20919)
-- Name: moneypayment; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.moneypayment (
    paymentid integer DEFAULT nextval('public.payment_paymentid_seq'::regclass) NOT NULL,
    occinspec_inspectionid integer NOT NULL,
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
-- TOC entry 246 (class 1259 OID 20928)
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
-- TOC entry 247 (class 1259 OID 20930)
-- Name: moneypaymenttype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.moneypaymenttype (
    typeid integer DEFAULT nextval('public.paymenttype_typeid_seq'::regclass) NOT NULL,
    pmttypetitle text NOT NULL
);


ALTER TABLE public.moneypaymenttype OWNER TO sylvia;

--
-- TOC entry 328 (class 1259 OID 37898)
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
-- TOC entry 232 (class 1259 OID 20833)
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
    enablecodeenforcement boolean DEFAULT true,
    enableoccupancy boolean DEFAULT true,
    enablepublicceactionreqsub boolean DEFAULT true,
    enablepublicceactionreqinfo boolean DEFAULT true,
    enablepublicoccpermitapp boolean DEFAULT false,
    enablepublicoccinspectodo boolean DEFAULT true,
    munimanager_userid integer,
    office_propertyid integer,
    profile_profileid integer,
    primarystaffcontact_userid integer,
    notes text,
    lastupdatedts timestamp with time zone,
    lastupdated_userid integer,
    defaultoccperiod integer
);


ALTER TABLE public.municipality OWNER TO sylvia;

--
-- TOC entry 335 (class 1259 OID 38009)
-- Name: municourtentity; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.municourtentity (
    muni_municode integer NOT NULL,
    courtentity_entityid integer NOT NULL,
    relativeorder integer
);


ALTER TABLE public.municourtentity OWNER TO sylvia;

--
-- TOC entry 271 (class 1259 OID 23107)
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
-- TOC entry 296 (class 1259 OID 34435)
-- Name: muniphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.muniphotodoc (
    photodoc_photodocid integer NOT NULL,
    muni_municode integer NOT NULL
);


ALTER TABLE public.muniphotodoc OWNER TO sylvia;

--
-- TOC entry 334 (class 1259 OID 37952)
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
-- TOC entry 338 (class 1259 OID 38142)
-- Name: muniprofileeventruleset; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.muniprofileeventruleset (
    muniprofile_profileid integer NOT NULL,
    ruleset_setid integer NOT NULL
);


ALTER TABLE public.muniprofileeventruleset OWNER TO sylvia;

--
-- TOC entry 339 (class 1259 OID 38195)
-- Name: muniprofileoccperiodtype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.muniprofileoccperiodtype (
    muniprofile_profileid integer NOT NULL,
    occperiodtype_typeid integer NOT NULL
);


ALTER TABLE public.muniprofileoccperiodtype OWNER TO sylvia;

--
-- TOC entry 233 (class 1259 OID 20840)
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
-- TOC entry 294 (class 1259 OID 34375)
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
    injectviolations boolean DEFAULT false,
    followupevent_eventid integer,
    notifyingofficer_userid integer
);


ALTER TABLE public.noticeofviolation OWNER TO sylvia;

--
-- TOC entry 295 (class 1259 OID 34414)
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
-- TOC entry 196 (class 1259 OID 20658)
-- Name: occchecklist; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occchecklist (
    checklistid integer DEFAULT nextval('public.checklist_checklistid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text NOT NULL,
    muni_municode integer NOT NULL,
    active boolean DEFAULT true,
    governingcodesource_sourceid integer
);


ALTER TABLE public.occchecklist OWNER TO sylvia;

--
-- TOC entry 198 (class 1259 OID 20668)
-- Name: occchecklistspacetype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occchecklistspacetype (
    checklistspacetypeid integer DEFAULT nextval('public.chkliststiceid_seq'::regclass) NOT NULL,
    checklist_id integer NOT NULL,
    required boolean,
    spacetype_typeid integer NOT NULL,
    overridespacetyperequired boolean DEFAULT false,
    overridespacetyperequiredvalue boolean DEFAULT false,
    overridespacetyperequireallspaces boolean DEFAULT false
);


ALTER TABLE public.occchecklistspacetype OWNER TO sylvia;

--
-- TOC entry 315 (class 1259 OID 37296)
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
-- TOC entry 316 (class 1259 OID 37326)
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
-- TOC entry 340 (class 1259 OID 38285)
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
-- TOC entry 341 (class 1259 OID 38287)
-- Name: occinspectedspace; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occinspectedspace (
    inspectedspaceid integer DEFAULT nextval('public.occinspectedspace_pk_seq'::regclass) NOT NULL,
    occspace_spaceid integer NOT NULL,
    occinspection_inspectionid integer NOT NULL,
    occlocationdescription_descid integer NOT NULL,
    addedtochecklistby_userid integer NOT NULL,
    addedtochecklistts timestamp with time zone
);


ALTER TABLE public.occinspectedspace OWNER TO sylvia;

--
-- TOC entry 225 (class 1259 OID 20796)
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
    spaceelement_elementid integer NOT NULL,
    required boolean DEFAULT true,
    failureseverity_intensityclassid integer
);


ALTER TABLE public.occinspectedspaceelement OWNER TO sylvia;

--
-- TOC entry 277 (class 1259 OID 31326)
-- Name: occinspectedspaceelementphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occinspectedspaceelementphotodoc (
    photodoc_photodocid integer NOT NULL,
    inspectedspaceelement_elementid integer NOT NULL
);


ALTER TABLE public.occinspectedspaceelementphotodoc OWNER TO sylvia;

--
-- TOC entry 240 (class 1259 OID 20879)
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
-- TOC entry 307 (class 1259 OID 35787)
-- Name: occinspection; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occinspection (
    inspectionid integer DEFAULT nextval('public.occupancyinspectionid_seq'::regclass) NOT NULL,
    occperiod_periodid integer NOT NULL,
    inspector_userid integer NOT NULL,
    passedinspection_userid integer,
    maxoccupantsallowed integer NOT NULL,
    publicaccesscc integer,
    enablepacc boolean DEFAULT false,
    notes text,
    thirdpartyinspector_personid integer,
    thirdpartyinspectorapprovalts timestamp with time zone,
    thirdpartyinspectorapprovalby integer,
    numbedrooms integer,
    numbathrooms integer,
    passedinspectionts timestamp with time zone,
    occchecklist_checklistlistid integer,
    effectivedate timestamp with time zone,
    active boolean DEFAULT true,
    creationts timestamp with time zone
);


ALTER TABLE public.occinspection OWNER TO sylvia;

--
-- TOC entry 229 (class 1259 OID 20815)
-- Name: occlocationdescriptor; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occlocationdescriptor (
    locationdescriptionid integer DEFAULT nextval('public.locationdescription_id_seq'::regclass) NOT NULL,
    description text,
    buildingfloorno integer
);


ALTER TABLE public.occlocationdescriptor OWNER TO sylvia;

--
-- TOC entry 303 (class 1259 OID 35692)
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
-- TOC entry 304 (class 1259 OID 35694)
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
-- TOC entry 353 (class 1259 OID 39074)
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
-- TOC entry 347 (class 1259 OID 38566)
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
-- TOC entry 318 (class 1259 OID 37638)
-- Name: occperiodpermitapplication; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occperiodpermitapplication (
    occperiod_periodid integer NOT NULL,
    occpermitapp_applicationid integer NOT NULL
);


ALTER TABLE public.occperiodpermitapplication OWNER TO sylvia;

--
-- TOC entry 308 (class 1259 OID 35853)
-- Name: occperiodperson; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occperiodperson (
    period_periodid integer NOT NULL,
    person_personid integer NOT NULL,
    applicant boolean,
    preferredcontact boolean,
    applicationpersontype public.persontype DEFAULT 'Other'::public.persontype NOT NULL
);


ALTER TABLE public.occperiodperson OWNER TO sylvia;

--
-- TOC entry 306 (class 1259 OID 35772)
-- Name: occperiodphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occperiodphotodoc (
    photodoc_photodocid integer NOT NULL,
    occperiod_periodid integer NOT NULL
);


ALTER TABLE public.occperiodphotodoc OWNER TO sylvia;

--
-- TOC entry 301 (class 1259 OID 35656)
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
-- TOC entry 302 (class 1259 OID 35658)
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
    commercial boolean DEFAULT false,
    active boolean DEFAULT true,
    allowthirdpartyinspection boolean DEFAULT false,
    requiredpersontypes public.persontype[],
    optionalpersontypes public.persontype[],
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
-- TOC entry 242 (class 1259 OID 20900)
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
-- TOC entry 305 (class 1259 OID 35748)
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
-- TOC entry 236 (class 1259 OID 20858)
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
-- TOC entry 243 (class 1259 OID 20909)
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
-- TOC entry 374 (class 1259 OID 40893)
-- Name: occpermitapplicationperson; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occpermitapplicationperson (
    occpermitapplication_applicationid integer NOT NULL,
    person_personid integer NOT NULL,
    applicant boolean,
    preferredcontact boolean,
    applicationpersontype public.persontype DEFAULT 'Other'::public.persontype NOT NULL,
    active boolean
);


ALTER TABLE public.occpermitapplicationperson OWNER TO sylvia;

--
-- TOC entry 237 (class 1259 OID 20860)
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
-- TOC entry 238 (class 1259 OID 20862)
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
-- TOC entry 261 (class 1259 OID 21011)
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
-- TOC entry 262 (class 1259 OID 21013)
-- Name: occspace; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occspace (
    spaceid integer DEFAULT nextval('public.spaceid_seq'::regclass) NOT NULL,
    name text NOT NULL,
    spacetype_id integer NOT NULL,
    required boolean DEFAULT false,
    description text
);


ALTER TABLE public.occspace OWNER TO sylvia;

--
-- TOC entry 263 (class 1259 OID 21020)
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
-- TOC entry 264 (class 1259 OID 21022)
-- Name: occspaceelement; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occspaceelement (
    spaceelementid integer DEFAULT nextval('public.spaceelement_seq'::regclass) NOT NULL,
    space_id integer NOT NULL,
    codeelement_id integer,
    required boolean DEFAULT true
);


ALTER TABLE public.occspaceelement OWNER TO sylvia;

--
-- TOC entry 265 (class 1259 OID 21026)
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
-- TOC entry 266 (class 1259 OID 21028)
-- Name: occspacetype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.occspacetype (
    spacetypeid integer DEFAULT nextval('public.spacetype_spacetypeid_seq'::regclass) NOT NULL,
    spacetitle text NOT NULL,
    description text NOT NULL,
    required boolean DEFAULT false
);


ALTER TABLE public.occspacetype OWNER TO sylvia;

--
-- TOC entry 241 (class 1259 OID 20891)
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
-- TOC entry 239 (class 1259 OID 20870)
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
-- TOC entry 382 (class 1259 OID 41669)
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
-- TOC entry 383 (class 1259 OID 41671)
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
    muni_municode integer NOT NULL
);


ALTER TABLE public.parcel OWNER TO sylvia;

--
-- TOC entry 388 (class 1259 OID 41764)
-- Name: parcelhuman; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.parcelhuman (
    human_humanid integer,
    parcel_parcelkey integer,
    source_sourceid integer,
    role_roleid integer,
    createdts timestamp with time zone,
    createdby_userid integer,
    lastupdatedts timestamp with time zone,
    lastupdatedby_userid integer,
    deactivatedts timestamp with time zone,
    deactivatedby_userid integer,
    notes text
);


ALTER TABLE public.parcelhuman OWNER TO sylvia;

--
-- TOC entry 386 (class 1259 OID 41748)
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
-- TOC entry 387 (class 1259 OID 41750)
-- Name: parcelhumanrole; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.parcelhumanrole (
    roleid integer DEFAULT nextval('public.parcelhumanrole_roleid_seq'::regclass) NOT NULL,
    title text NOT NULL,
    description text,
    muni_municode integer,
    deactivatedts timestamp with time zone
);


ALTER TABLE public.parcelhumanrole OWNER TO sylvia;

--
-- TOC entry 399 (class 1259 OID 41966)
-- Name: parcelmailingaddress; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.parcelmailingaddress (
    mailingparcel_parcelid integer,
    mailingparcel_mailingid integer,
    source_sourceid integer,
    createdts timestamp with time zone,
    deactivatedts timestamp with time zone,
    notes text
);


ALTER TABLE public.parcelmailingaddress OWNER TO sylvia;

--
-- TOC entry 252 (class 1259 OID 20958)
-- Name: propertunit_unitid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.propertunit_unitid_seq
    START WITH 15000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertunit_unitid_seq OWNER TO sylvia;

--
-- TOC entry 400 (class 1259 OID 41987)
-- Name: parcelunit; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.parcelunit (
    unitid integer DEFAULT nextval('public.propertunit_unitid_seq'::regclass) NOT NULL,
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
    notes text
);


ALTER TABLE public.parcelunit OWNER TO sylvia;

--
-- TOC entry 292 (class 1259 OID 34313)
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
-- TOC entry 376 (class 1259 OID 40911)
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
-- TOC entry 375 (class 1259 OID 40909)
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
-- TOC entry 293 (class 1259 OID 34315)
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
-- TOC entry 354 (class 1259 OID 39095)
-- Name: personmunilink; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.personmunilink (
    muni_municode integer NOT NULL,
    person_personid integer NOT NULL,
    defaultmuni boolean DEFAULT false
);


ALTER TABLE public.personmunilink OWNER TO sylvia;

--
-- TOC entry 274 (class 1259 OID 31242)
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
-- TOC entry 250 (class 1259 OID 20949)
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
-- TOC entry 251 (class 1259 OID 20951)
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
    createdts timestamp with time zone DEFAULT now(),
    createdby_userid integer
);


ALTER TABLE public.photodoc OWNER TO sylvia;

--
-- TOC entry 297 (class 1259 OID 34458)
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
-- TOC entry 298 (class 1259 OID 34475)
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
-- TOC entry 282 (class 1259 OID 31527)
-- Name: propertyid_seq; Type: SEQUENCE; Schema: public; Owner: sylvia
--

CREATE SEQUENCE public.propertyid_seq
    START WITH 1000000
    INCREMENT BY 1
    MINVALUE 10000
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.propertyid_seq OWNER TO sylvia;

--
-- TOC entry 253 (class 1259 OID 20960)
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
    multiunit boolean DEFAULT false,
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
-- TOC entry 255 (class 1259 OID 20980)
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
-- TOC entry 256 (class 1259 OID 20982)
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
-- TOC entry 377 (class 1259 OID 40943)
-- Name: spatialdata8; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.spatialdata8 (
    gid integer NOT NULL,
    cartodb_id smallint,
    propertyad character varying(23),
    pin character varying(16),
    propertyci character varying(16),
    municode_a character varying(3),
    propertyho character varying(5),
    propertyfr character varying(5),
    propertyzi character varying(5),
    propertyun character varying(8),
    propertyst character varying(2),
    geom public.geometry(MultiPolygon)
);


ALTER TABLE public.spatialdata8 OWNER TO sylvia;

--
-- TOC entry 379 (class 1259 OID 40970)
-- Name: propertymapdata; Type: VIEW; Schema: public; Owner: sylvia
--

CREATE VIEW public.propertymapdata AS
 SELECT p.parid,
    p.propertyid,
    p.lotandblock,
    s.propertyho,
    s.propertyad,
    p.usegroup,
    s.propertyci,
    s.propertyzi,
    COALESCE(p.ownercode, 'NONE'::text) AS ownercode,
    p.lastupdated,
    p.landbankheld,
    p.active,
    s.geom,
    count(c.caseid) AS casecount,
    COALESCE(c.casename, 'NONE'::text) AS casename
   FROM ((public.spatialdata8 s
     FULL JOIN public.property p ON (((s.pin)::text = p.parid)))
     FULL JOIN public.cecase c ON ((c.property_propertyid = p.propertyid)))
  GROUP BY p.parid, p.propertyid, p.lotandblock, s.propertyho, s.propertyad, p.usegroup, s.propertyci, s.propertyzi, p.ownercode, p.lastupdated, p.landbankheld, p.active, s.geom, c.caseid, c.casename;


ALTER TABLE public.propertymapdata OWNER TO sylvia;

--
-- TOC entry 344 (class 1259 OID 38467)
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
-- TOC entry 345 (class 1259 OID 38469)
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
-- TOC entry 257 (class 1259 OID 20989)
-- Name: propertyperson; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.propertyperson (
    property_propertyid integer NOT NULL,
    person_personid integer NOT NULL,
    creationts timestamp with time zone
);


ALTER TABLE public.propertyperson OWNER TO sylvia;

--
-- TOC entry 410 (class 1259 OID 42652)
-- Name: propertyphotodoc; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.propertyphotodoc (
    photodoc_photodocid integer NOT NULL,
    property_propertyid integer NOT NULL
);


ALTER TABLE public.propertyphotodoc OWNER TO sylvia;

--
-- TOC entry 342 (class 1259 OID 38434)
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
-- TOC entry 343 (class 1259 OID 38436)
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
-- TOC entry 258 (class 1259 OID 20995)
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
-- TOC entry 336 (class 1259 OID 38034)
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
-- TOC entry 259 (class 1259 OID 21005)
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
-- TOC entry 260 (class 1259 OID 21007)
-- Name: propertyusetype; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.propertyusetype (
    propertyusetypeid integer DEFAULT nextval('public.propertyusetype_seq'::regclass) NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(100),
    icon_iconid integer,
    zoneclass text
);


ALTER TABLE public.propertyusetype OWNER TO sylvia;

--
-- TOC entry 254 (class 1259 OID 20967)
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
-- TOC entry 357 (class 1259 OID 39448)
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
-- TOC entry 358 (class 1259 OID 39450)
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
-- TOC entry 5295 (class 0 OID 0)
-- Dependencies: 358
-- Name: TABLE taxstatus; Type: COMMENT; Schema: public; Owner: sylvia
--

COMMENT ON TABLE public.taxstatus IS 'Scraped data from Allegheny County http://www2.alleghenycounty.us/RealEstate/. Description valid as of August 2020';


--
-- TOC entry 267 (class 1259 OID 21035)
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
-- TOC entry 268 (class 1259 OID 21037)
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
-- TOC entry 269 (class 1259 OID 21044)
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
-- TOC entry 5280 (class 0 OID 0)
-- Dependencies: 9
-- Name: SCHEMA public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2022-02-25 12:14:26 EST

--
-- PostgreSQL database dump complete
--

