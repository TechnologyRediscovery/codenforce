--Deletes Forest Hills data
delete from public.propertyperson where property_propertyid >= 200000 and property_propertyid < 210000;
delete from public.propertyunit where property_propertyid >= 200000 and property_propertyid < 210000;
delete from public.property where propertyid >= 200000 and propertyid < 210000;
delete from public.person where personid >= 101 and personid < 10100;

--delete from property where municipality_municode = 828;


-- -- Legacy deletion script
--delete from public.propertyperson where property_propertyid >= 210000 and property_propertyid < 220000;
--delete from public.propertyunit where property_propertyid >= 210000 and property_propertyid < 220000;
--delete from public.property where propertyid >= 210000 and propertyid < 220000;
--delete from public.person where personid >= 120000 and personid < 130000;