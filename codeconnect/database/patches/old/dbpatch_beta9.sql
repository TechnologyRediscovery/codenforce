
ALTER TABLE citationstatus ADD COLUMN editsallowed BOOLEAN DEFAULT TRUE;
ALTER TABLE municipality ADD COLUMN defaultcourtentity INTEGER CONSTRAINT muni_defcourtentity_fk REFERENCES courtentity (entityid);
ALTER TABLE loginmuni ADD COLUMN defaultmuni BOOLEAN DEFAULT FALSE;
ALTER TABLE ceevent ADD COLUMN directrequesttodefaultmuniceo boolean DEFAULT false;

-- Add icon ids to ceeeventcategories


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

-- add foreign keys to 

--- ceevent categories

ALTER TYPE persontype ADD VALUE IF NOT EXISTS 'User' BEFORE 'CogStaff'; 


BEGIN;
INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (9, 'database/patches/dbpatch_beta9.sql', '03-29-2019', 'ecd', 'citation updates, among others');

COMMIT;
