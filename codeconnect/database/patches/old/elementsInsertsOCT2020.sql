INSERT INTO public.codeelement(
            elementid, 
            codesource_sourceid, 
            ordchapterno, 
            ordchaptertitle, 
            ordsecnum, 
            ordsectitle, 
            ordsubsecnum, 
            ordsubsectitle, 
            ordtechnicaltext, 
            ordhumanfriendlytext, 
            isactive, 
            resourceurl, 
            datecreated, 
            guideentryid, 
            notes, 
            legacyid)
    VALUES (DEFAULT, 
    	29, 
    	'1', 
    	'Carbon Monoxide Alarm Standards Act', 
        '4', 
        'Carbon monoxide alarm requirements', 
        '4b', 
        'Multifamily dwellings', 
        'Each apartment in a multifamily dwelling, which uses a fossil
fuel-burning heater or appliance, fireplace, or an attached garage, must have an
operational, centrally located and approved carbon monoxide alarm installed in the
vicinity of the bedrooms and the fossil fuel-burning heater or fireplace within 18
months of the effective date of this act.', 
		NULL, 
        TRUE, 
        'https://www.legis.state.pa.us/WU01/LI/LI/US/PDF/2013/0/0121..PDF', 
        now(), 
        NULL, 
        NULL, 
        NULL);

    INSERT INTO public.codeelement(
            elementid, 
            codesource_sourceid, 
            ordchapterno, 
            ordchaptertitle, 
            ordsecnum, 
            ordsectitle, 
            ordsubsecnum, 
            ordsubsectitle, 
            ordtechnicaltext, 
            ordhumanfriendlytext, 
            isactive, 
            resourceurl, 
            datecreated, 
            guideentryid, 
            notes, 
            legacyid)
    VALUES (DEFAULT, 
    	29, 
    	'1', 
    	'Carbon Monoxide Alarm Standards Act', 
        '5', 
        'Carbon monoxide alarm requirements in rental properties', 
        '5a', 
        'Owner responsibilities', 
        'The owner of a multifamily dwelling having a fossil fuelburning heater or appliance, fireplace or an attached garage used for rental purposes
and required to be equipped with one or more approved carbon monoxide alarms
shall:<br />
(1) Provide and install an operational, centrally located and approved carbon
monoxide alarm in the vicinity of the bedrooms and the fossil fuel-burning
heater or fireplace.<br />
(2) Replace, in accordance with this act, any approved carbon monoxide alarm that
has been stolen, removed, found missing or rendered inoperable during a prior
occupancy of the rental property and which has not been replaced by the prior<br />
occupant before the commencement of a new occupancy of the rental property.
(3) Ensure that the batteries in each approved carbon monoxide alarm are in
operating condition at the time the new occupant takes residence in the rental
property.', 
		NULL, 
        TRUE, 
        'https://www.legis.state.pa.us/WU01/LI/LI/US/PDF/2013/0/0121..PDF', 
        now(), 
        NULL, 
        NULL, 
        NULL);



    INSERT INTO public.codeelement(
            elementid, 
            codesource_sourceid, 
            ordchapterno, 
            ordchaptertitle, 
            ordsecnum, 
            ordsectitle, 
            ordsubsecnum, 
            ordsubsectitle, 
            ordtechnicaltext, 
            ordhumanfriendlytext, 
            isactive, 
            resourceurl, 
            datecreated, 
            guideentryid, 
            notes, 
            legacyid)
    VALUES (DEFAULT, 
    	29, 
    	'1', 
    	'Carbon Monoxide Alarm Standards Act', 
        '5', 
        'Carbon monoxide alarm requirements in rental properties', 
        '5b', 
        'Maintenance, repair or replacement', 
        'Except as provided in subsection (a), the owner
of a multifamily dwelling used for rental purposes is not responsible for the
maintenance, repair or replacement of an approved carbon monoxide alarm or the
care and replacement of batteries while the building is occupied. Responsibility for
maintenance and repair of carbon monoxide alarms shall revert to the owner of the
building upon vacancy of the rental property.', 
		NULL, 
        TRUE, 
        'https://www.legis.state.pa.us/WU01/LI/LI/US/PDF/2013/0/0121..PDF', 
        now(), 
        NULL, 
        NULL, 
        NULL);




    INSERT INTO public.codeelement(
            elementid, 
            codesource_sourceid, 
            ordchapterno, 
            ordchaptertitle, 
            ordsecnum, 
            ordsectitle, 
            ordsubsecnum, 
            ordsubsectitle, 
            ordtechnicaltext, 
            ordhumanfriendlytext, 
            isactive, 
            resourceurl, 
            datecreated, 
            guideentryid, 
            notes, 
            legacyid)
    VALUES (DEFAULT, 
    	29, 
    	'1', 
    	'Carbon Monoxide Alarm Standards Act', 
        '5', 
        'Carbon monoxide alarm requirements in rental properties', 
        '5c', 
        'Occupant responsibilities', 
        'The occupant of each multifamily dwelling used for rental
purposes in which an operational and approved carbon monoxide alarm has been
provided must:<br />
(1) Keep and maintain the device in good repair.<br />
(2) Test the device.<br />
(3) Replace batteries as needed.<br />
(4) Replace any device that is stolen, removed, missing or rendered inoperable during
the occupancy of the building.<br />
(5) Notify the owner or the authorized agent of the owner in writing of any deficiencies
pertaining to the approved carbon monoxide alarm.', 
		NULL, 
        TRUE, 
        'https://www.legis.state.pa.us/WU01/LI/LI/US/PDF/2013/0/0121..PDF', 
        now(), 
        NULL, 
        NULL, 
        NULL);





    INSERT INTO public.codeelement(
            elementid, 
            codesource_sourceid, 
            ordchapterno, 
            ordchaptertitle, 
            ordsecnum, 
            ordsectitle, 
            ordsubsecnum, 
            ordsubsectitle, 
            ordtechnicaltext, 
            ordhumanfriendlytext, 
            isactive, 
            resourceurl, 
            datecreated, 
            guideentryid, 
            notes, 
            legacyid)
    VALUES (DEFAULT, 
    	29, 
    	'1', 
    	'Carbon Monoxide Alarm Standards Act', 
        '6', 
        'Enforcement', 
    	NULL, 
        NULL, 
        'Willful failure to install or maintain in operating condition any approved carbon monoxide
alarm required by this act is a summary offense punishable by a fine of up to $50', 
		NULL, 
        TRUE, 
        'https://www.legis.state.pa.us/WU01/LI/LI/US/PDF/2013/0/0121..PDF', 
        now(), 
        NULL, 
        NULL, 
        NULL);


967
971
970
972
973
974


INSERT INTO public.codesetelement(
            codesetelementid, codeset_codesetid, codelement_elementid, elementmaxpenalty, 
            elementminpenalty, elementnormpenalty, penaltynotes, normdaystocomply, 
            daystocomplynotes, munispecificnotes, defaultseverityclass_classid, 
            fee_feeid)
    VALUES (DEFAULT, 18, 967 , 50.0, 
            0, 50, NULL, 30, 
            NULL, NULL, NULL, 
            NULL);
