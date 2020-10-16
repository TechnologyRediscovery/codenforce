-- ****************************************************************************
-- PATCH 32
-- "MID AUG 2020" launch changes

-- ****************************************************************************

ALTER TABLE public.codeelement
   ADD COLUMN ordsubsubsecnum text;

ALTER TABLE public.codeelement
    ADD COLUMN useinjectedvalues boolean;



CREATE SEQUENCE codeelementinjectedvalue_seq
    INCREMENT 1
    START 1000
    MINVALUE 1000;

CREATE TABLE codeelementinjectedvalue(
    injectedvalueid         integer NOT NULL 
                            CONSTRAINT codeelementinjectedvalue_pk PRIMARY KEY 
                            DEFAULT nextval('codeelementinjectedvalue_seq'),
    value                   TEXT NOT NULL,
    injectionorder          integer,
    codelement_eleid        integer NOT NULL CONSTRAINT injectedvalue_element_fk REFERENCES codeelement(elementid),
    codeset_codesetid       integer NOT NULL CONSTRAINT injectedvalue_codeset_fk REFERENCES codeset(codesetid),
    creationts              TIMESTAMP WITH TIME ZONE,
    notes                   TEXT,
    active                  boolean DEFAULT TRUE
);






--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (32, 'database/patches/dbpatch_beta32.sql',null 'ecd', 'mid oct 2020 changes');
