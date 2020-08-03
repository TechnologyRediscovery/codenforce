-- ****************************************************************************
-- PATCH 28
-- "Mid-july" launch changes
-- Person revisions: adding type of link to propertyperson
-- 
-- EXTRA RUN ON SERVER
-- ALTER TABLE public.codeviolation ADD COLUMN lastupdatedts timestamp with time zone;
-- ALTER TABLE public.codeviolation ADD COLUMN lastupdated_userid integer;
-- ALTER TABLE public.codeviolation ADD COLUMN active boolean;
-- ALTER TABLE public.codeviolation ALTER COLUMN active SET DEFAULT true;
-- ALTER TABLE public.codeviolation
--   ADD CONSTRAINT codeviolation_lastupdatedby_fk FOREIGN KEY (lastupdated_userid)
--       REFERENCES public.login (userid) MATCH SIMPLE
--       ON UPDATE NO ACTION ON DELETE NO ACTION;

-- ****************************************************************************

INSERT INTO public.icon(
            iconid, name, styleclass, fontawesome, materialicons)
    VALUES (100, 'note', 'cnf-note', 'fa fa-sticky-note-o', 'note');


INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors, 
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal, 
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins, 
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (221, 'PropertyInfoCase'::eventtype, 'Property note', 'General update on property; not for case or period-specific deets', FALSE, 
            TRUE, 100, 0, 0,
            NULL, NULL,1, 
            TRUE, 3, 3, 5);

ALTER TABLE cecase DROP COLUMN casephase;

ALTER TABLE public.cecase ADD COLUMN lastupdatedby_userid INTEGER CONSTRAINT cecase_lastupdatedby_userid_fk REFERENCES login (userid);
ALTER TABLE public.cecase ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE;




ALTER TABLE public.login ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE DEFAULT now();
ALTER TABLE public.login DROP COLUMN pswdcleartext;
ALTER TABLE public.login DROP COLUMN active;


INSERT INTO public.intensityclass(
            classid, title, muni_municode, numericrating, schemaname, active, 
            icon_iconid)
    VALUES (DEFAULT, 'Well-Kept', 999, 1, 'propcondition', TRUE, 
            10);


INSERT INTO public.intensityclass(
            classid, title, muni_municode, numericrating, schemaname, active, 
            icon_iconid)
    VALUES (DEFAULT, 'Weathered', 999, 2, 'propcondition', TRUE, 
            10);


INSERT INTO public.intensityclass(
            classid, title, muni_municode, numericrating, schemaname, active, 
            icon_iconid)
    VALUES (DEFAULT, 'Deterioriated', 999, 3, 'propcondition', TRUE, 
            10);

INSERT INTO public.intensityclass(
            classid, title, muni_municode, numericrating, schemaname, active, 
            icon_iconid)
    VALUES (DEFAULT, 'Structurally unsound', 999, 5, 'propcondition', TRUE, 
            10);

INSERT INTO public.intensityclass(
            classid, title, muni_municode, numericrating, schemaname, active, 
            icon_iconid)
    VALUES (DEFAULT, 'Optimal location', 999, 1, 'landbankprospect', TRUE, 
            10);

INSERT INTO public.intensityclass(
            classid, title, muni_municode, numericrating, schemaname, active, 
            icon_iconid)
    VALUES (DEFAULT, 'Poor location', 999, 2, 'landbankprospect', TRUE, 
            10);

INSERT INTO public.intensityclass(
            classid, title, muni_municode, numericrating, schemaname, active, 
            icon_iconid)
    VALUES (DEFAULT, 'Solid location, severe damage', 999, 3, 'landbankprospect', TRUE, 
            10);

INSERT INTO public.intensityclass(
            classid, title, muni_municode, numericrating, schemaname, active, 
            icon_iconid)
    VALUES (DEFAULT, 'Poor location, Desirable condition', 999, 4, 'landbankprospect', TRUE, 
            10);





ALTER TABLE person
    ADD COLUMN
    rawname         text;
ALTER TABLE person
    ADD COLUMN
    cleanname       text;
ALTER TABLE person
    ADD COLUMN
    multientity     bool;
ALTER TABLE propertyunit
    ADD COLUMN
    rental          bool;


INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors,
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal,
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins,
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (300, 'PropertyInfoCase'::eventtype, 'NewParcelid', 'A new parcel was added to the database', TRUE,
            TRUE, NULL, 0, 0,
            NULL, NULL, 1,
            TRUE, 7, 3, 7);

INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors,
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal,
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins,
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (301, 'PropertyInfoCase'::eventtype, 'DifferentOwner', 'Documents a change in a parcel''s owner', TRUE,
            TRUE, NULL, 0, 0,
            NULL, NULL, 1,
            TRUE, 7, 3, 7);

INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors,
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal,
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins,
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (302, 'PropertyInfoCase'::eventtype, 'DifferentStreet', 'Documents a change in a parcel''s street', TRUE,
            TRUE, NULL, 0, 0,
            NULL, NULL, 1,
            TRUE, 7, 3, 7);

INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors,
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal,
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins,
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (303, 'PropertyInfoCase'::eventtype, 'DifferentCityStateZip', 'Documents a change in a parcel''s city, state, or zipcode', TRUE,
            TRUE, NULL, 0, 0,
            NULL, NULL, 1,
            TRUE, 7, 3, 7);

INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors,
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal,
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins,
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (304, 'PropertyInfoCase'::eventtype, 'DifferentLivingArea', 'Documents a change in a parcel''s living area', TRUE,
            TRUE, NULL, 0, 0,
            NULL, NULL, 1,
            TRUE, 7, 3, 7);

INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors,
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal,
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins,
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (305, 'PropertyInfoCase'::eventtype, 'DifferentCondition', 'Documents a change in a parcel''s condition', TRUE,
            TRUE, NULL, 0, 0,
            NULL, NULL, 1,
            TRUE, 7, 3, 7);

INSERT INTO public.eventcategory(
            categoryid, categorytype, title, description, notifymonitors,
            hidable, icon_iconid, relativeorderwithintype, relativeorderglobal,
            hosteventdescriptionsuggtext, directive_directiveid, defaultdurationmins,
            active, userrankminimumtoenact, userrankminimumtoview, userrankminimumtoupdate)
    VALUES (306, 'PropertyInfoCase'::eventtype, 'DifferentTaxStats', 'Documents a change in a parcel''s tax status', TRUE,
            TRUE, NULL, 0, 0,
            NULL, NULL, 1,
            TRUE, 7, 3, 7);


CREATE OR REPLACE FUNCTION resetsequences(incr integer default 3)
RETURNS void as $$
declare
    propertyid_ integer;
    unitid_     integer;
    caseid_     integer;
    personid_   integer;
    cecaseid_   integer;
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
    cecaseid_ := MAX(caseid) FROM cecase;
        cecaseid_ = cecaseid_ + incr;
        PERFORM setval('cecase_caseid_seq', cecaseid_);
END;
$$ LANGUAGE plpgsql;

-- -- Original code to reset sequences, kept for posterity
--ALTER SEQUENCE public.propertyid_seq
--    START WITH 1000000
--    RESTART;
--ALTER SEQUENCE public.propertunit_unitid_seq    -- This sequence is misspelled. How easy is it to fix?
--    START WITH 15000
--    RESTART;
--ALTER SEQUENCE cecase_caseid_seq
--    START WITH 21200
--    RESTART;
--ALTER SEQUENCE person_personidseq
--    START WITH 120000
--    RESTART;

--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (28, 'database/patches/dbpatch_beta28.sql', '08-03-2020', 'ecd', 'mid-july-launch');
--  LAUNCH DAY PATCH! (07/15/2020)
--      Added miscellaneous columns.
--      Added event category's.
--      Added function resetsequences to dynamically avoid id collisions.
--  AUTHOR: SNAPPER VIBES
-- ****************************************************************************
