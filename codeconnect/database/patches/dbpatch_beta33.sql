-- ****************************************************************************
-- PATCH 33
-- "LATE OCT 2020" launch changes

-- *************

ALTER TABLE public.codeviolation
    ADD COLUMN nullifiedts TIMESTAMP WITH TIME ZONE;

ALTER TABLE public.codeviolation
    ADD COLUMN nullifiedby INTEGER CONSTRAINT codeviolation_nullifiedby_fk REFERENCES login(userid);


-- RUN ON REMOTE TO HERE
-- RUN LOCALLY TO HERE





--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (33, 'database/patches/dbpatch_beta33.sql',NULL, 'ecd', 'LATE oct 2020 changes');
