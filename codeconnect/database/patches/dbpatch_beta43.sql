-- REMOTE CURSOR

ALTER TABLE public.codeelement ADD COLUMN subsubsectitle TEXT;

--- LOCAL CURSOR 


INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (43, 'database/patches/dbpatch_beta43.sql', '', 'ecd', '');