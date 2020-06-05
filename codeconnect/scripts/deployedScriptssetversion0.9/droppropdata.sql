-- -- Deletes Forest Hills data
delete from public.propertyperson where property_propertyid >= 210000 and property_propertyid < 220000;
delete from public.propertyunit where property_propertyid >= 210000 and property_propertyid < 220000;
delete from public.property where propertyid >= 210000 and propertyid < 220000;
delete from public.person where personid >= 120000 and personid < 130000;