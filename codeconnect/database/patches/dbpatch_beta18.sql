


ALTER TABLE occperiodtype DROP COLUMN fee_feeid CASCADE;

ALTER TABLE occperiodtype RENAME COLUMN completedinspectionrequired TO passedinspectionrequired;

ALTER TABLE occperiodtype ADD COLUMN defaultvalidityperioddays INTEGER;

-- occupancy permit applications CANNOT BE FOR MORE THAN ONE UNIT
ALTER TABLE occpermitapplication DROP COLUMN multiunit;


INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (18, 'database/patches/dbpatch_beta18.sql', '07-04-2019', 'ecd', 'tweaks during occupancy beta integration');

