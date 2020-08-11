-- LOCKED AS OF 23AUG19	

ALTER TABLE ceevent RENAME COLUMN ceeventcategory_catid TO category_catid;

INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (22, 'database/patches/dbpatch_beta22.sql', '08-20-2019', 'ecd', 'occ beta tweaking');