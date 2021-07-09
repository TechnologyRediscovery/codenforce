-- ****************************************************************************
-- PATCH 34.1
-- JURPLEL occ 

-- *************
ALTER TABLE public.occperiod 
ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE,
ADD COLUMN lastupdatedby_userid INTEGER CONSTRAINT human_lastupdatdby_userid_fk REFERENCES login (userid);

-- RUN ON REMOTE UP TO HERE

-- RUN ON LOCAL BRAIN UP TO HERE`


--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (34.1, 'database/patches/dbpatch_beta34.1.sql','07-09-2021', 'JURPLEL', 'OCC CHANGES');
