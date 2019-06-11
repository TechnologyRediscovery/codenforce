BEGIN;

/*Add applicant flag for case when applicant is not a required or optional person on 
an occupancy permit application */

ALTER TABLE occpermitapplicationperson
ADD COLUMN applicant boolean,
ADD COLUMN preferredcontact boolean,

-- What should our default value be here? This table is probably empty in production, so this may be nonissue
ADD COLUMN applicationpersontype persontype DEFAULT 'LegacyOwner' NOT NULL;


-- Add optionalpersontypes to occpermitapplicationreason

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (13, 'database/patches/dbpatch_beta13.sql', '05-30-2019', 'ecd', 'final clean ups');

