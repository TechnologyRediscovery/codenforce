﻿-- Deletes Forest Hills data
delete from propertyperson
    using person
    where propertyperson.person_personid = person.personid
    and person.muni_municode = 828;
delete from propertyunit
    using property
    where propertyunit.property_propertyid = property.propertyid
	and municipality_municode = 828;
delete from public.property where municipality_municode = 828;it
delete from public.person where muni_municode = 828;