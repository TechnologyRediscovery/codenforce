-- TURTLE CREEK VALLEY COUNCIL OF GOVERNMENTS CODE ENFORCEMENT DATABASE
-- AUTHOR: SNAPPER VIBES
-- PROPERTY ADDED TO DATABASE EVENT

-- munideplouable is false, as new properties should only be inserted using insert and update scripts.
-- Change this script if that is not the case.


insert into eventcategory(
    categoryid, categorytype, title, description,
    userdeployable, munideployable, publicdeployable, notifycasemonitors,
    hidable, relativeorderwithintype, relativeorderglobal
)
values(
    221, 'CaseAdmin', 'New Property', 'A new property was inserted into the database',
    true, false, false, true,
    true, 0, 0
)
