-- Deletes Forest Hills data
delete from cecase
    using property
    where cecase.property_propertyid = property.propertyid
    and property.municipality_municode = 828;
delete from propertyperson
    using person
    where propertyperson.person_personid = person.personid
    and person.muni_municode = 828;
delete from propertyunit
    using property
    where propertyunit.property_propertyid = property.propertyid
	and property.municipality_municode = 828;
delete from public.property where municipality_municode = 828;
delete from public.person where muni_municode = 828;

