INSERT INTO public.municipality(
            municode, muniname, address_street, address_city, address_state, 
            address_zip, phone, fax, email, population, activeinprogram, 
            defaultcodeset, occpermitissuingsource_sourceid, novprintstyle_styleid, 
            profile_profileid, enablecodeenforcement, enableoccupancy, enablepublicceactionreqsub, 
            enablepublicceactionreqinfo, enablepublicoccpermitapp, enablepublicoccinspectodo, 
            munimanager_userid, office_propertyid, notes, lastupdatedts, 
            lastupdated_userid, primarystaffcontact_userid, defaultoccperiod)
    VALUES (808, 'Braddock', '415 6th Stree', 'Braddock', 'PA', 
            '15104', '4122711018', NULL, 'braddockborough@comcast.net', 2114, TRUE, 
            10, 10, 1000, 
            1000, TRUE, TRUE, TRUE,
            FALSE, FALSE, FALSE, 
            99, 173134, 'created for overview zoom call with Paul', now(), 
            99, 99, 1006);
