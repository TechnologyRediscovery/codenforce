BEGIN;


CREATE TABLE occpermitapplicationperson
(
	permitapp_applicationid 	INTEGER CONSTRAINT occpermitappperson_appid_fk REFERENCES occupancypermitapplication (applicationid),
	person_personid				INTEGER CONSTRAINT occpermitappperson_personid_fk REFERENCES person (personid)
);

ALTER TABLE occpermitapplicationperson ADD CONSTRAINT occpermitappperson_comp_pk PRIMARY KEY (permitapp_applicationid, person_personid);
ALTER TABLE occupancypermitapplication DROP COLUMN newoccupant_personid;

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (3, '/database/patches/dbpatch_beta3.sql', '01-31-2019', 'ecd', 'Update to occ permit application');

COMMIT;