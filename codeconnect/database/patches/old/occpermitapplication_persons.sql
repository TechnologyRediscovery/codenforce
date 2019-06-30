-- Already integrated into patch cycle

BEGIN;

/*Add applicant flag for case when applicant is not a required or optional person on 
an occupancy permit application */

ALTER TABLE occpermitapplicationperson
ADD COLUMN applicant boolean,
ADD COLUMN preferredcontact boolean,

-- What should our default value be here? This table is probably empty in production, so this may be nonissue
ADD COLUMN applicationpersontype persontype DEFAULT 'LegacyOwner' NOT NULL;


-- Add optionalpersontypes to occpermitapplicationreason

ALTER TABLE occpermitapplicationreason
ADD COLUMN optionalpersontypes persontype[],
ADD COLUMN humanfriendlydescription text;

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


-- Add person friendly descriptions

UPDATE occpermitapplicationreason
SET personfriendlydescription = description.descriptiontext
FROM (VALUES 
	(1, 'An owner must be added to an occupancy permit application for a new rental property. Optionally, a property manager and the tenants who will be renting the property can also be added.'),
	(2, 'Both the current owner and future owner must be added to an occupancy permit application that is being created due to the sale of a property.'),
	(3, 'An owner, plus all incoming tenants must be added to an occupancy permit application created due to a change in tenants. Optionally, a property manager can also be added.'),
	(4, 'An owner must be added to an occupancy permit application in the case of change of use.')
	) as description(id, descriptiontext)
WHERE description.id = occpermitapplicationreason.reasonid;

ALTER TABLE occupancypermitapplication
DROP COLUMN currentowner_personid,
DROP COLUMN contactperson_personid,
DROP COLUMN newowner_personid;

COMMIT;

