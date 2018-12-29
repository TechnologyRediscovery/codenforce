--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.12
-- Dumped by pg_dump version 9.5.12

-- Started on 2018-03-28 22:44:08 EDT

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2294 (class 0 OID 51946)
-- Dependencies: 204
-- Data for Name: ceeventcategory; Type: TABLE DATA; Schema: public; Owner: sylvia
--

INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (100, 'Communication', 'Communicating with somebody', 'Phone calls, emails, etc.');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (101, 'Communication', 'Internal Case Note Added', 'A member of the COG staff attached a note to this case');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (102, 'Communication', 'Municipal Case Note Added', 'A staff member at the relevant municipality added a case note');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (103, 'Communication', 'Public Case Note Added', 'A member of the public used a case access code to attach a note to this case');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (104, 'Origination', 'Online Action Request Form', 'An action request form was completed by some entity (public or internal)');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (105, 'Origination', 'Visual Observation of Violation', 'A code officer detected a code violation during a visual inspection of a property');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (106, 'Origination', 'Call from Municipality', 'A municipal staff person called a code officer and requested an investigation');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (111, 'PhaseChange', 'Change in Case Phase', 'System generated case transition event');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (112, 'Closing', 'Compliance', 'All code violations associated with this case have been corrected');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (113, 'Timeline', 'Compliance Timeframe end', 'The compliance timeframe for a violation on this case has expired');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (117, 'Notice', 'Code Violation Update', 'Non-compliance related record update on a Code Violation');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (107, 'Communication', 'Email', 'Email sent or received');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (121, 'Action', 'Deploy Notice of Violation', 'Case officer has created a notivce of violation letter and requested that it be printed and mailed');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (122, 'Action', 'Notice of Violation Sent', 'A deployed notice of violation enters the post system');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (123, 'Action', 'Property Inspection', 'In-person inspection of a property to which a code enforcement case has been attached and code violations noted');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (124, 'Action', 'Citation Issued', 'A citation has been issued');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (120, 'Meeting', 'Municipal Magistrate Hearing', 'Hearing held with the municipal magistrate concerning a citation issued for this case');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (125, 'Meeting', 'Hearing scheduled', 'Notice received by the COG that a hearing has been scheduled with a municipal magistrate');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (126, 'Closing', 'Penalities Paid', 'Property owner paid imposed penalities for citations issued');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (127, 'Closing', 'Demolition of property', 'Property of concern was demolished');
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description) VALUES (128, 'Closing', 'Condemned', 'Property of Concern was declared condemned');


-- Completed on 2018-03-28 22:44:08 EDT

--
-- PostgreSQL database dump complete
--

