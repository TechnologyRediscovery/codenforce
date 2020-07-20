-- ****************************************************************************
--  LAUNCH DAY PATCH! (07/15/2020)
--      Added miscellaneous columns.
--      Added event category's.
--      Added function resetsequences to dynamically avoid id collisions.
--  AUTHOR: SNAPPER VIBES
-- ****************************************************************************


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
select resetsequences();

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
