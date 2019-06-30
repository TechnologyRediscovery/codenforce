--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.17
-- Dumped by pg_dump version 11.3 (Ubuntu 11.3-1.pgdg16.04+1)

-- Started on 2019-06-29 18:26:09 EDT

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

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 294 (class 1259 OID 106169)
-- Name: eventrule; Type: TABLE; Schema: public; Owner: sylvia
--

CREATE TABLE public.eventrule (
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
    description text
);


ALTER TABLE public.eventrule OWNER TO sylvia;

--
-- TOC entry 2573 (class 0 OID 106169)
-- Dependencies: 294
-- Data for Name: eventrule; Type: TABLE DATA; Schema: public; Owner: sylvia
--

INSERT INTO public.eventrule (ruleid, title, targetcasephase, requiredcurrentcasephase, forbiddencurrentcasephase, requiredextanteventtype, forbiddenextanteventtype, requiredextanteventcat, forbiddenextanteventcat, triggeredeventcat, active, mandatory, treatreqphaseasthreshold, treatforbidphaseasthreshold, rejectrulehostifrulefails, description) VALUES (1000, 'condemation', 'Closed', NULL, 'Closed', NULL, NULL, 122, 128, 128, true, false, false, false, true, 'Checks that a case is not closed and case has not previously condemned this property');
INSERT INTO public.eventrule (ruleid, title, targetcasephase, requiredcurrentcasephase, forbiddencurrentcasephase, requiredextanteventtype, forbiddenextanteventtype, requiredextanteventcat, forbiddenextanteventcat, triggeredeventcat, active, mandatory, treatreqphaseasthreshold, treatforbidphaseasthreshold, rejectrulehostifrulefails, description) VALUES (1001, 'issue citation', 'AwaitingHearingDate', 'InitialComplianceTimeframe', 'Closed', NULL, NULL, NULL, NULL, 124, true, true, true, false, true, 'rides with the citation issuance objects to manage citation-related updates of casephase; case must be in a compliance
            window (and not closed); writes in an officer action: issue citation event; requests the scheduling of a hearing event');
INSERT INTO public.eventrule (ruleid, title, targetcasephase, requiredcurrentcasephase, forbiddencurrentcasephase, requiredextanteventtype, forbiddenextanteventtype, requiredextanteventcat, forbiddenextanteventcat, triggeredeventcat, active, mandatory, treatreqphaseasthreshold, treatforbidphaseasthreshold, rejectrulehostifrulefails, description) VALUES (1002, 'schedule a hearing', 'HearingPreparation', 'AwaitingHearingDate', 'Closed', NULL, NULL, 124, NULL, 136, true, false, true, false, false, 'to schedule a hearing, the case must have at least a single citation and not be closed; triggers ');
INSERT INTO public.eventrule (ruleid, title, targetcasephase, requiredcurrentcasephase, forbiddencurrentcasephase, requiredextanteventtype, forbiddenextanteventtype, requiredextanteventcat, forbiddenextanteventcat, triggeredeventcat, active, mandatory, treatreqphaseasthreshold, treatforbidphaseasthreshold, rejectrulehostifrulefails, description) VALUES (1003, 'hearing outcome: new timeframe', 'InitialPostHearingComplianceTimeframe', 'HearingPreparation', 'Closed', NULL, NULL, 120, NULL, 113, true, false, true, false, false, 'be sure to edit each code violation for which the defendant was given an extended compliance timeframe by updated 
            the compliance timeframe expiry; your change will be logged along with the violation');
INSERT INTO public.eventrule (ruleid, title, targetcasephase, requiredcurrentcasephase, forbiddencurrentcasephase, requiredextanteventtype, forbiddenextanteventtype, requiredextanteventcat, forbiddenextanteventcat, triggeredeventcat, active, mandatory, treatreqphaseasthreshold, treatforbidphaseasthreshold, rejectrulehostifrulefails, description) VALUES (1004, 'hearing outcome: penalty stands', 'Closed', 'HearingPreparation', 'Closed', NULL, NULL, 132, NULL, 126, true, true, true, false, false, 'coordinates a case outcome with a penalty that is undreduced by magistrate');
INSERT INTO public.eventrule (ruleid, title, targetcasephase, requiredcurrentcasephase, forbiddencurrentcasephase, requiredextanteventtype, forbiddenextanteventtype, requiredextanteventcat, forbiddenextanteventcat, triggeredeventcat, active, mandatory, treatreqphaseasthreshold, treatforbidphaseasthreshold, rejectrulehostifrulefails, description) VALUES (1005, 'lock and queue notice of violation', 'NoticeDelivery', 'PrelimInvestigationPending', 'InitialComplianceTimeframe', NULL, NULL, NULL, NULL, 121, true, true, false, true, false, 'created when a NOV is locked and queued and requests mailing');
INSERT INTO public.eventrule (ruleid, title, targetcasephase, requiredcurrentcasephase, forbiddencurrentcasephase, requiredextanteventtype, forbiddenextanteventtype, requiredextanteventcat, forbiddenextanteventcat, triggeredeventcat, active, mandatory, treatreqphaseasthreshold, treatforbidphaseasthreshold, rejectrulehostifrulefails, description) VALUES (1006, 'mailed notice of violation', 'InitialComplianceTimeframe', 'NoticeDelivery', 'InitialComplianceTimeframe', NULL, NULL, NULL, NULL, NULL, true, true, false, true, false, 'transitions case to its initial compliance timeframe as soon as a notice of violation is marked as mailed');


--
-- TOC entry 2455 (class 2606 OID 106177)
-- Name: eventrule phasechangerule_pk; Type: CONSTRAINT; Schema: public; Owner: sylvia
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT phasechangerule_pk PRIMARY KEY (ruleid);


--
-- TOC entry 2457 (class 2606 OID 106183)
-- Name: eventrule phasechangerule_forbiddenevcat_fk; Type: FK CONSTRAINT; Schema: public; Owner: sylvia
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT phasechangerule_forbiddenevcat_fk FOREIGN KEY (forbiddenextanteventcat) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 2456 (class 2606 OID 106178)
-- Name: eventrule phasechangerule_reqevcat_fk; Type: FK CONSTRAINT; Schema: public; Owner: sylvia
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT phasechangerule_reqevcat_fk FOREIGN KEY (requiredextanteventcat) REFERENCES public.eventcategory(categoryid);


--
-- TOC entry 2458 (class 2606 OID 106193)
-- Name: eventrule phasechangerule_triggeredevcat_fk; Type: FK CONSTRAINT; Schema: public; Owner: sylvia
--

ALTER TABLE ONLY public.eventrule
    ADD CONSTRAINT phasechangerule_triggeredevcat_fk FOREIGN KEY (triggeredeventcat) REFERENCES public.eventcategory(categoryid);


-- Completed on 2019-06-29 18:26:09 EDT

--
-- PostgreSQL database dump complete
--

