-- AUTHOR: SNAPPER VIBES
-- TURTLE CREEK VALLEY COUNCIL OF GOVERNMENTS CODE ENFORCEMENT DATABASE
-- id_base replacement script

create sequence personid_seq
    start with 101; -- We start at a high number to avoid collisions with test data
alter table public.person
    alter personid set default nextval('personid_seq');

create sequence propertyid_seq
    start with 200000;
alter table property
    alter propertyid set default nextval('propertyid_seq');


