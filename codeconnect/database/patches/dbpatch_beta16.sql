
--***************************************
--  add reasons and their type proposal mappings here
--***************************************
--***************************************


-- TODO

ALTER TABLE public.occpermitapplicationreason
   ALTER COLUMN periodtypeproposal_periodid SET NOT NULL;





INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (13, 'database/patches/dbpatch_beta13.sql', '05-05-2019', 'ecd', 'occ permit application revisions');

