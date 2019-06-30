--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.16
-- Dumped by pg_dump version 11.2 (Ubuntu 11.2-1.pgdg16.04+1)

-- Started on 2019-04-09 13:34:16 EDT

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
-- TOC entry 2509 (class 0 OID 65420)
-- Dependencies: 203
-- Data for Name: ceeventcategory; Type: TABLE DATA; Schema: public; Owner: sylvia
--

INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (111, 'PhaseChange', 'Change in Case Phase', 'System generated case transition event', true, false, false, false, false, false, NULL, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (117, 'CaseAdmin', 'Code Violation Update', 'Non-compliance related record update on a Code Violation', true, false, false, false, false, false, NULL, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (122, 'CaseAdmin', 'Notice of Violation Mailed', 'A queued notice of violation has been physically put into the outgoing mail bin', false, false, false, false, false, true, NULL, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (121, 'CaseAdmin', 'Notice of violation queued', 'Case officer has created a notice of violation letter and queued it for mailing', false, false, false, false, false, true, NULL, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (131, 'CaseAdmin', 'Notice of violation returned', 'A previously mailed notice of violation letter was returned to sender', false, false, false, true, false, true, NULL, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (132, 'CaseAdmin', 'Code violation added to case', 'Code officer attached a code violation to this case', false, false, false, true, false, true, NULL, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (124, 'CaseAdmin', 'Citation Issued', 'A citation has been issued', false, false, false, false, false, true, NULL, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (134, 'CaseAdmin', 'Citation updated', 'Code officer updated citation info', false, false, false, true, false, true, NULL, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (104, 'Origination', 'Online Action Request Form', 'An action request form was completed by some entity (public or internal)', true, false, false, false, false, false, 18, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (105, 'Origination', 'Visual Observation of Violation', 'A code officer detected a code violation during a visual inspection of a property', true, false, false, false, false, false, 18, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (106, 'Origination', 'Call from Municipality', 'A municipal staff person called a code officer and requested an investigation', true, false, false, false, false, false, 18, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (107, 'Communication', 'Email', 'Email sent or received', true, false, false, false, false, false, 16, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (112, 'Closing', 'Compliance', 'All code violations associated with this case have been corrected', false, false, false, true, false, false, 19, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (127, 'Closing', 'Demolition of property', 'Property of concern was demolished', true, false, false, false, false, false, 19, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (204, 'Closing', 'No violations found', 'On-site inspection conducted and no code violations observed', true, false, false, true, false, false, 19, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (207, 'Custom', 'Legacy import', 'Data imported from Legacy MS Access database', false, false, false, false, false, false, 14, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (208, 'Custom', 'Payment of QOL ticket', 'Recived payment of QOL at COG Office', true, false, false, false, false, false, 14, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (126, 'Closing', 'Penalities Paid', 'Property owner paid imposed penalities for citations issued', true, true, false, true, false, false, 19, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (130, 'PhaseChange', 'Manual override of case phase', 'Case officer triggered manul override of standard case protocol', true, false, false, false, false, false, 14, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (135, 'Action', 'Inspect property', 'Code officer visited property at any point in case lifecycle', true, false, false, true, true, true, 23, true);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (101, 'Communication', 'Case note: Officers only', 'A member of the COG staff attached a note to this case', true, false, false, false, false, false, 16, true);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (102, 'Communication', 'Case note: general, non-public', 'A staff member at the relevant municipality added a case note', true, false, false, false, false, false, 16, true);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (100, 'Communication', 'Phone call', 'made or received a phone call with anybody regarding this case', false, true, false, false, false, true, 17, true);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (103, 'Communication', 'Case note: general, publicly viewable', 'A member of the public used a case access code to attach a note to this case', true, false, false, false, false, false, 16, true);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (128, 'Closing', 'Condemned', 'Property of Concern was declared condemned', true, false, false, false, false, false, 19, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (209, 'Origination', 'Action request connection', 'A CE action request is connected to this CE case', false, false, false, false, false, true, 18, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (201, 'Origination', 'Fire Hazard', 'Undersized extension cords nailed to wall', false, true, true, true, false, true, 18, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (138, 'Timeline', 'Scheduled Meeting', 'Marks the scheduled calendar date of any non-court meeting related to this case', true, true, false, true, false, true, 20, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (136, 'Timeline', 'Scheduled Hearing', 'Marks the scheduled calendar date of a court hearing', true, true, false, true, false, true, 20, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (113, 'Timeline', 'End of compliance timeframe', 'The compliance timeframe for a violation on this case has expired', true, false, false, false, false, true, 20, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (140, 'Communication', 'Post: sent or received', 'Wrote and mailed case-related info, or received mail related to case', true, true, false, false, false, true, 16, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (137, 'Communication', 'In-person conversation', 'Converation in person between any persons about this case', true, true, false, false, false, true, 21, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (139, 'Meeting', 'Attend meeting', 'Attending a non-court meeting related to this case', true, true, false, false, false, true, 21, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (129, 'Compliance', 'Compliance achieved for one or more code violation', 'Case officer has inspected a site and verified that one or more existing code violations have been remedied', true, false, false, true, false, false, 22, false);
INSERT INTO public.ceeventcategory (categoryid, categorytype, title, description, userdeployable, munideployable, publicdeployable, notifycasemonitors, casephasechangetrigger, hidable, icon_iconid, requestable) VALUES (120, 'Action', 'Attend court hearing', 'Hearing held with the municipal magistrate concerning a citation issued for this case', true, false, false, true, false, false, 23, true);


--
-- TOC entry 2510 (class 0 OID 103896)
-- Dependencies: 293
-- Data for Name: icon; Type: TABLE DATA; Schema: public; Owner: sylvia
--

INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (10, 'investigation', 'mced-status-investigation', 'fa fa-search-plus', 'image_search');
INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (11, 'enforcement', 'mced-status-enforcement', 'fa fa-hourglass', 'hourglass_full');
INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (14, 'holding', 'mced-status-holding', 'fa fa-hotel', 'weekend');
INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (12, 'citation', 'mced-status-citation', 'fa fa-gavel', 'gavel');
INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (13, 'closed', 'mced-status-closed', 'fa fa-calendar-check-o', 'event_available');
INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (16, 'email', 'mced-event-email', 'fa fa-keyboard-o', 'keyboard');
INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (17, 'phone', 'mced-event-phone', 'fa fa-phone', 'phone');
INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (18, 'newcase', 'mced-event-opencase', 'fa fa-folder-open', 'create_new_folder');
INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (19, 'closecase', 'mced-event-closecase', 'fa fa-window-close', 'meeting_room');
INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (20, 'date', 'mced-event-date', 'fa fa-calendar', 'date_range');
INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (21, 'face', 'mced-event-person', 'fa fa-smile-o', 'face');
INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (22, 'compliance', 'mced-event-compliance', 'fa fa-check-square', 'check_box');
INSERT INTO public.icon (iconid, name, styleclass, fontawesome, materialicons) VALUES (23, 'action', 'mced-event-action', 'fa fa-blind', 'accessibility');


-- Completed on 2019-04-09 13:34:17 EDT

--
-- PostgreSQL database dump complete
--

