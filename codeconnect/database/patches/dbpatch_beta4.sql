
ALTER TYPE persontype ADD VALUE IF NOT EXISTS 'FutureOwner' AFTER 'OwnerNonOccupant';
ALTER TYPE persontype ADD VALUE IF NOT EXISTS 'Owner' AFTER 'FutureOwner';

BEGIN;

ALTER TABLE occpermitapplicationreason ADD COLUMN requiredpersontypes persontype[];

INSERT INTO public.occpermitapplicationreason(
            reasonid, reasontitle, reasondescription, activereason)
    VALUES (4, 'Change of use', 'Zoning change', TRUE);

UPDATE occpermitapplicationreason SET requiredpersontypes = ARRAY['Owner'::persontype, 'Tenant'::persontype] WHERE reasonid = 1;
UPDATE occpermitapplicationreason SET requiredpersontypes = ARRAY['FutureOwner'::persontype, 'Owner'::persontype] WHERE reasonid = 2;
UPDATE occpermitapplicationreason SET requiredpersontypes = ARRAY['Owner'::persontype, 'Tenant'::persontype] WHERE reasonid = 3;
UPDATE occpermitapplicationreason SET requiredpersontypes = ARRAY['Owner'::persontype] WHERE reasonid = 4;

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (4, '/database/patches/dbpatch_beta4.sql', '02-26-2019', 'ecd', 'Update related to occ applications');

COMMIT;