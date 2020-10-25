-- ****************************************************************************
-- PATCH 32
-- "MID OCT 2020" launch changes

-- ****************************************************************************

ALTER TABLE public.codeelement
   ADD COLUMN ordsubsubsecnum text;

ALTER TABLE public.codeelement
    ADD COLUMN useinjectedvalues boolean;

ALTER TABLE public.codeelement ADD COLUMN creator_userid integer;

ALTER TABLE public.codeelement ADD COLUMN lastupdatedts timestamp with time zone;
ALTER TABLE public.codeelement ALTER COLUMN lastupdatedts SET DEFAULT now();
ALTER TABLE public.codeelement ADD COLUMN lastupdated_userid integer;
ALTER TABLE public.codeelement
  ADD CONSTRAINT codeelement_creator_fk FOREIGN KEY (creator_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
ALTER TABLE public.codeelement
  ADD CONSTRAINT codeelement_lastupdatedby_fk FOREIGN KEY (lastupdated_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;




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

ALTER TABLE public.codesetelement ADD COLUMN defaultviolationdescription TEXT;

ALTER TABLE public.textblock ADD COLUMN placementorderdefault INTEGER;

ALTER TABLE public.textblockcategory ADD COLUMN  
    muni_municode integer CONSTRAINT textblockcategory_municode_fk REFERENCES municipality (municode);


-- From Snapper
-- RUN on both DBs as of 16 OCT
ALTER TABLE propertyperson
ADD COLUMN creationts timestamp with time zone;



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (32, 'database/patches/dbpatch_beta32.sql','10-16-2020' 'ecd', 'mid oct 2020 changes');
