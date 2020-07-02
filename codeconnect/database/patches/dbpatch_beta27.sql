-- ****************************************************************************
-- PATCH 27
-- Public user setup
-- 
-- 
-- ****************************************************************************

INSERT INTO public.person(
            personid, persontype, muni_municode, fname, lname, jobtitle, 
            phonecell, phonehome, phonework, email, address_street, address_city, 
            address_state, address_zip, notes, lastupdated, expirydate, isactive, 
            isunder18, humanverifiedby, compositelname, sourceid, creator, 
            businessentity, mailing_address_street, mailing_address_city, 
            mailing_address_zip, mailing_address_state, useseparatemailingaddr, 
            expirynotes, creationtimestamp, canexpire, userlink, mailing_address_thirdline, 
            ghostof, ghostby, ghosttimestamp, cloneof, clonedby, clonetimestamp, 
            referenceperson)
    VALUES (98, 'Public'::persontype , 999, 'Public', 'User', 'Public User', 
            NULL, NULL, NULL, NULL, '2209 S BRADDOCK AVE', 'SWISSVALE', 
            'PA', '15218', 'Used for all non-authenticated public users', now(), '01-01-2100', TRUE, 
            FALSE, 99, FALSE, 10, 99, 
            FALSE, NULL, NULL, 
            NULL, NULL, FALSE, 
            NULL, now(), FALSE, NULL, NULL, 
            NULL, NULL, NULL, NULL, NULL, NULL, 
            NULL);

INSERT INTO public.login(
            userid, username, password, notes, personlink, pswdlastupdated, 
            active, forcepasswordreset, createdby, createdts, nologinvirtualonly, 
            pswdcleartext, userrole, deactivatedts, deactivated_userid)
    VALUES (98, 'publicuser', NULL, 'Represents non-authenticated public user', 98, NULL, 
            TRUE, NULL, 99, now(), TRUE, 
            NULL, 'Public'::role , NULL, NULL);

INSERT INTO public.loginmuniauthperiod(
            muniauthperiodid, muni_municode, authuser_userid, accessgranteddatestart, 
            accessgranteddatestop, recorddeactivatedts, authorizedrole, createdts, 
            createdby_userid, notes, supportassignedby, assignmentrank)
    VALUES (98, 999, 98, '01-07-2020', 
            '01-01-2100', NULL, 'Public'::role, now(), 
            99, 'Public user UMAP', NULL, 1);



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (27, 'database/patches/dbpatch_beta27.sql', '02-JUL-2020', 'ecd', 'public user');