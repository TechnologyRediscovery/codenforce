-- ****************************************************************************
-- PATCH 26
-- 
-- 
-- 
-- 
-- ****************************************************************************

ALTER TABLE occperiod ADD COLUMN active boolean DEFAULT true;
ALTER TABLE ceactionrequest ADD COLUMN active boolean DEFAULT true;
ALTER TABLE cecase ADD COLUMN active boolean DEFAULT true;


-- From NADGIT
ALTER TABLE public.moneycodesetelementfee ADD COLUMN active boolean;
ALTER TABLE public.moneycodesetelementfee ADD COLUMN autoassign boolean;

-- Run on production server up to this point 


--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (26, 'database/patches/dbpatch_beta26.sql', NULL, 'ecd', 'various changes');