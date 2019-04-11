--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.16
-- Dumped by pg_dump version 11.2 (Ubuntu 11.2-1.pgdg16.04+1)

-- Started on 2019-04-09 13:43:47 EDT

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2497 (class 0 OID 103945)
-- Dependencies: 294
-- Data for Name: cecasestatusicon; Type: TABLE DATA; Schema: public; Owner: sylvia
--

INSERT INTO public.cecasestatusicon (iconid, status) VALUES (10, 'PrelimInvestigationPending');
INSERT INTO public.cecasestatusicon (iconid, status) VALUES (10, 'NoticeDelivery');
INSERT INTO public.cecasestatusicon (iconid, status) VALUES (11, 'InitialComplianceTimeframe');
INSERT INTO public.cecasestatusicon (iconid, status) VALUES (11, 'SecondaryComplianceTimeframe');
INSERT INTO public.cecasestatusicon (iconid, status) VALUES (12, 'AwaitingHearingDate');
INSERT INTO public.cecasestatusicon (iconid, status) VALUES (12, 'HearingPreparation');
INSERT INTO public.cecasestatusicon (iconid, status) VALUES (12, 'InitialPostHearingComplianceTimeframe');
INSERT INTO public.cecasestatusicon (iconid, status) VALUES (12, 'SecondaryPostHearingComplianceTimeframe');
INSERT INTO public.cecasestatusicon (iconid, status) VALUES (14, 'InactiveHolding');
INSERT INTO public.cecasestatusicon (iconid, status) VALUES (13, 'Closed');
INSERT INTO public.cecasestatusicon (iconid, status) VALUES (14, 'LegacyImported');


-- Completed on 2019-04-09 13:43:47 EDT

--
-- PostgreSQL database dump complete
--

