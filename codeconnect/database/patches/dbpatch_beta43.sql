-- LAST PATCH in the first generation database patch series
-- When jumping into 1.0.0, you'll need a database patched through this last patch
-- Start with a database version dbv.0.43.0 then you can stay current by applying high fidelity
-- patches beginning with cnfdbv.1.x.y

ALTER TABLE public.codeelement ADD COLUMN subsubsectitle TEXT;


-- REMOTE CURSOR
-- LOCAL CURSOR 




INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (43, 'database/patches/dbpatch_beta43.sql', '2022-10-31', 'ecd', '');