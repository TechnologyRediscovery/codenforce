BEGIN;

/*Add applicant flag for case when applicant is not a required or optional person on 
an occupancy permit application */

ALTER TABLE occpermitapplicationperson
ADD isapplicant boolean;

-- Add optionalpersontypes to occpermitapplicationreason

ALTER TABLE occpermitapplicationreason
ADD optionalpersontypes persontype[];

-- Populate optionalpersontypes 

UPDATE occpermitapplicationreason
SET optionalpersontypes = '{Manager, Tenant}'
WHERE reasonid = '1';

UPDATE occpermitapplicationreason
SET optionalpersontypes = '{Manager}'
WHERE reasonid = '3';

-- Update requiredpersontypes (effectively removing Tenant as a requiredpersontype for a rental)

UPDATE occpermitapplicationreason
SET requiredpersontypes = '{Owner}'
WHERE reasonid = '1';

COMMIT;

