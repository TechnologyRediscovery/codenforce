-- ****************************************************************************
-- PATCH 35
-- "Early Jan 2021" humization changes

-- *************


INSERT INTO public.icon(
            iconid, name, styleclass, fontawesome, materialicons)
    VALUES (32, 'nullified', 'violation-nullified', 'fa fa-window-close-o', 'close');


-- RUN ON LOCAL BRAIN UP TO HERE
-- RUN ON REMOTE UP TO HERE

--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (35, 'database/patches/dbpatch_beta35.sql',NULL, 'ecd', 'Early JAN 2021 changes');
