

-- Need a separate category for events generated with dedicated case management buttons, like mailing a NOV

ALTER TYPE ceeventtype ADD VALUE IF NOT EXISTS 'CaseAdmin' AFTER 'Action';


BEGIN;


UPDATE public.ceeventcategory
   SET categorytype=CAST('Communication' AS ceeventtype), title='Phone call', description='made or received a phone call with anybody regarding this case', userdeployable=FALSE, 
       munideployable=TRUE, publicdeployable=FALSE, requiresviewconfirmation=FALSE, 
       notifycasemonitors=FALSE, casephasechangetrigger=FALSE, hidable=TRUE
 WHERE categoryid=100;

-- Code violation update event category
UPDATE public.ceeventcategory
   SET categorytype=CAST('CaseAdmin' AS ceeventtype)
 WHERE categoryid=117;

-- Notice violation queued
UPDATE public.ceeventcategory
   SET categorytype=CAST('CaseAdmin' AS ceeventtype), title='Notice of violation queued', userdeployable=FALSE, 
       munideployable=FALSE, publicdeployable=FALSE, requiresviewconfirmation=FALSE, 
       notifycasemonitors=FALSE, casephasechangetrigger=FALSE, hidable=TRUE
 WHERE categoryid=121;

 -- Notice violation mailed
 UPDATE public.ceeventcategory
   SET categorytype=CAST('CaseAdmin' AS ceeventtype), userdeployable=FALSE, 
       munideployable=FALSE, publicdeployable=FALSE, requiresviewconfirmation=FALSE, 
       notifycasemonitors=FALSE, casephasechangetrigger=FALSE, hidable=TRUE
 WHERE categoryid=122;

 -- Notice violation returned
 INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description, userdeployable, 
            munideployable, publicdeployable, requiresviewconfirmation, notifycasemonitors, 
            casephasechangetrigger, hidable)
    VALUES (131, CAST('CaseAdmin' as ceeventtype), 'Notice of violation returned', 'A previously mailed notice of violation letter was returned to sender', TRUE, 
            TRUE, FALSE, TRUE, TRUE, 
            FALSE, TRUE);

 -- Citation issued
 UPDATE public.ceeventcategory
   SET categorytype=CAST('CaseAdmin' AS ceeventtype), userdeployable=FALSE, 
       munideployable=FALSE, publicdeployable=FALSE, requiresviewconfirmation=FALSE, 
       notifycasemonitors=FALSE, casephasechangetrigger=FALSE, hidable=TRUE
 WHERE categoryid=124;


 -- Notice violation returned
 INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description, userdeployable, 
            munideployable, publicdeployable, requiresviewconfirmation, notifycasemonitors, 
            casephasechangetrigger, hidable)
    VALUES (131, CAST('CaseAdmin' as ceeventtype), 'Notice of violation returned', 'A previously mailed notice of violation letter was returned to sender', TRUE, 
            TRUE, FALSE, TRUE, TRUE, 
            FALSE, TRUE);

 -- NOV returned typo fix
 UPDATE public.ceeventcategory
   SET userdeployable=FALSE, munideployable=FALSE, publicdeployable=FALSE
 WHERE categoryid=131;

 -- Code violation added
 INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description,
            userdeployable, munideployable, publicdeployable, requiresviewconfirmation, notifycasemonitors, 
            casephasechangetrigger, hidable)
    VALUES (132, CAST('CaseAdmin' as ceeventtype), 'Code violation added to case', 'Code officer attached a code violation to this case', 
    	FALSE, FALSE, FALSE, TRUE, TRUE, 
            FALSE, TRUE);


 -- Code violation added
 INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description,
            userdeployable, munideployable, publicdeployable, requiresviewconfirmation, notifycasemonitors, 
            casephasechangetrigger, hidable)
    VALUES (134, CAST('CaseAdmin' as ceeventtype), 'Citation updated', 'Code officer updated citation info', 
    	FALSE, FALSE, FALSE, TRUE, TRUE, 
            FALSE, TRUE);

-- Generalize attend hearing
 UPDATE public.ceeventcategory
   SET title='Attend court hearing'
 WHERE categoryid=120;

 INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description,
            userdeployable, munideployable, publicdeployable, requiresviewconfirmation, notifycasemonitors, 
            casephasechangetrigger, hidable)
    VALUES (135, CAST('Action' as ceeventtype), 'Property Inspection', 'Code officer visited property at any point in case lifecycle', 
    	TRUE, FALSE, FALSE, TRUE, TRUE, 
            TRUE, TRUE);

 INSERT INTO public.ceeventcategory(
        categoryid, categorytype, title, description,
        userdeployable, munideployable, publicdeployable, requiresviewconfirmation, notifycasemonitors, 
        casephasechangetrigger, hidable)
VALUES (136, CAST('Timeline' as ceeventtype), 'Scheduled Hearing', 'Marks the scheduled calendar date of a court hearing', 
	TRUE, TRUE, FALSE, TRUE, TRUE, 
        TRUE, TRUE);

 INSERT INTO public.ceeventcategory(
        categoryid, categorytype, title, description,
        userdeployable, munideployable, publicdeployable, requiresviewconfirmation, notifycasemonitors, 
        casephasechangetrigger, hidable)
VALUES (137, CAST('Communication' as ceeventtype), 'In-person conversation', 'Converation in person between any persons about this case', 
	TRUE, TRUE, FALSE, FALSE, FALSE, 
        FALSE, TRUE);

 INSERT INTO public.ceeventcategory(
        categoryid, categorytype, title, description,
        userdeployable, munideployable, publicdeployable, requiresviewconfirmation, notifycasemonitors, 
        casephasechangetrigger, hidable)
VALUES (138, CAST('Timeline' as ceeventtype), 'Scheduled Meeting', 'Marks the scheduled calendar date of any non-court meeting related to this case', 
	TRUE, TRUE, FALSE, TRUE, TRUE, 
        FALSE, TRUE);

 INSERT INTO public.ceeventcategory(
        categoryid, categorytype, title, description,
        userdeployable, munideployable, publicdeployable, requiresviewconfirmation, notifycasemonitors, 
        casephasechangetrigger, hidable)
VALUES (139, CAST('Meeting' as ceeventtype), 'Attend meeting', 'Attending a non-court meeting related to this case', 
	TRUE, TRUE, FALSE, FALSE, FALSE, 
        FALSE, TRUE);



-- Revise title
 UPDATE public.ceeventcategory
   SET title='Inspect property'
 WHERE categoryid=135;

  UPDATE public.ceeventcategory
   SET title='Case note: Officers only'
 WHERE categoryid=101;

 UPDATE public.ceeventcategory
   SET title='Case note: general, non-public'
 WHERE categoryid=102;

 UPDATE public.ceeventcategory
   SET title='Case note: general, publicly viewable'
 WHERE categoryid=103;



-- Initial investigation
DELETE FROM ceeventcategory WHERE categoryid=202;
-- log scheduled hearing
DELETE FROM ceeventcategory WHERE categoryid=125;
-- post-hearing prop inspec
DELETE FROM ceeventcategory WHERE categoryid=203;
-- pre-hearing prop inspection
DELETE FROM ceeventcategory WHERE categoryid=123;


COMMIT;

ALTER TYPE ceeventtype ADD VALUE IF NOT EXISTS 'PropertyInfoCase' AFTER 'Compliance';


BEGIN;
-- update login entries with default person

INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (8, 'database/patches/dbpatch_beta8.sql', '03-15-2019', 'ecd', 'new event type and system tracking');

COMMIT;