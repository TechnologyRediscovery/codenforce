
ALTER TABLE citationstatus ADD COLUMN editsallowed BOOLEAN DEFAULT TRUE;
ALTER TABLE municipality ADD COLUMN defaultcourtentity INTEGER CONSTRAINT muni_defcourtentity_fk REFERENCES courtentity (entityid);
ALTER TABLE loginmuni ADD COLUMN default BOOLEAN DEFAULT FALSE;
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


-- add foreign keys to 

--- ceevent categories

ALTER TYPE persontype ADD VALUE IF NOT EXISTS 'User' BEFORE 'CogStaff'S; 






BEGIN;
INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (9, 'database/patches/dbpatch_beta9.sql', '03-29-2019', 'ecd', 'citation updates, among others');

COMMIT;
