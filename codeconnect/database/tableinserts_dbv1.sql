

-- *********** INSESRT STATEMENTS FOR  ACTION REQUEST ISSUE TYPE=**********************

INSERT INTO public.actionrqstissuetype(issuetypeid, typename, typedescription, notes)
    VALUES (DEFAULT, 'Tall grass or excessive weeds', 'tall grass', NULL);

INSERT INTO public.actionrqstissuetype(issuetypeid, typename, typedescription, notes)
    VALUES (DEFAULT, 'Excessive trash', 'too much garbage', NULL);

INSERT INTO public.actionrqstissuetype(issuetypeid, typename, typedescription, notes)
    VALUES (DEFAULT, 'Rodent or bug infestation', 'rats, roaches, etc.', NULL);

INSERT INTO public.actionrqstissuetype(issuetypeid, typename, typedescription, notes)
    VALUES (DEFAULT, 'Inappropriate Signage', 'Signs or advertisements that violate local ordinances', NULL);

INSERT INTO public.actionrqstissuetype(issuetypeid, typename, typedescription, notes)
    VALUES (DEFAULT, 'Signs of abandonment', 'Indicators exist that property has been vacated and left unattended', NULL);

INSERT INTO public.actionrqstissuetype(issuetypeid, typename, typedescription, notes)
    VALUES (DEFAULT, 'Unsightly material storage', 'Trash, appliances, abandoned vehicles, etc.', NULL);

INSERT INTO public.actionrqstissuetype(issuetypeid, typename, typedescription, notes)
    VALUES (DEFAULT, 'other / not listed', 'User described reason for action request', NULL);
-- defaults start at 1

-- fixes any sample insert properties to be registered in COG Land
update property set municipality_muniCode = 999;

-- Sample user
INSERT INTO public.login(
            userid, userrole, username, password, muni_municode, fname, lname, 
            worktitle, phonecell, phonehome, phonework, email, address_street, 
            address_city, address_zip, address_state, notes, activitystartdate, activitystopdate, 
            accesspermitted)
    VALUES (DEFAULT, 'Developer', 'ecd', '1234', 999, 'Eric', 'Darsow', 
            'System Administrator', '4129239907', '4128943020', NULL, 'ericdarsow@gmail.com', '2209 S Braddock Ave', 
            'Pittsburgh', '15218', 'PA', 'System Creator', now() , NULL, 
            DEFAULT);
-- first default 100


INSERT INTO public.person(
            personid, persontype, muni_municode, fname, lname, jobtitle, 
            phonecell, phonehome, phonework, email, address_street, address_city, 
            address_state, address_zip, notes, lastupdated, expirydate, isactive, 
            isunder18)
    VALUES (DEFAULT, 'CogStaff', 999, 'Eric', 'Darsow', 'System Designer', 
            '4129239907', '4128943020', NULL, 'developer@tcvcog.com', '2209 S BRADDOCK AVE', 'Swissvale', 
            'PA', '15218', 'System Creator and Designer', now(), NULL, TRUE, 
            FALSE);
-- first default is 100


INSERT INTO public.ceactionrequest(
            requestid, requestpubliccc, muni_municode, property_propertyid, 
            issuetype_issuetypeid, actrequestor_requestorid, submittedtimestamp, 
            dateofrecord, notataddress, addressofconcern, requestdescription, 
            isurgent, anonymityRequested, coginternalnotes, muniinternalnotes, publicexternalnotes)
    VALUES (DEFAULT, 123456, 999, 11997, 
            1, 100, now(), 
            now(), FALSE, '123 Forth', 'My neighbor is breeding goats on their front lawn and now I sneeze a lot', 
            FALSE, FALSE, 'Let''s not get out in front of this one', 'A very important issue well deal with promtply', 'We are checking on farm animal breeding regulations. Stay Tuned.');
-- first default ID val is 101

INSERT INTO public.person(
            personid, persontype, muni_municode, fname, lname, jobtitle, 
            phonecell, phonehome, phonework, email, address_street, address_city, 
            address_state, address_zip, notes, lastupdated, expirydate, isactive, 
            isunder18)
    VALUES (DEFAULT, 'OwnerOccupant', '999', 'Delores', 'Yearling', 'Professional Tenant', 
            '1231231234', '1231231234', '1231231234', 'delo@res.com', '1234 Fifth', 'COG Land', 
            'PA', '15222', 'Fictitious Person', now(), NULL, TRUE, 
            FALSE);
-- probably 1002


INSERT INTO public.cecase(
            caseid, ceCasePublicCC, property_propertyid, propertyunit_unitid, login_userid, 
            casename, casephase, originationdate, closingdate, notes)
    VALUES (DEFAULT, 12345 ,11997, NULL, 100, 
            'Goat Breeding', 'PrelimInvestigationPending', now(), NULL, 'Checking into legality of goat breeding in COG Land. Municipal Records are a mess, however, and this is proving challenging');
-- first default ID val is 1000

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Communication', 'Communicating with somebody', 'Phone calls, emails, etc.');
-- first default ID val is 100

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Communication', 'Internal Case Note Added', 'A member of the COG staff attached a note to this case');

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Communication', 'Municipal Case Note Added', 'A staff member at the relevant municipality added a case note');

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Communication', 'Public Case Note Added', 'A member of the public used a case access code to attach a note to this case');


INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Origination', 'Online Action Request Form', 'An action request form was completed by some entity (public or internal)');

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Origination', 'Visual Observation of Violation', 'A code officer detected a code violation during a visual inspection of a property');

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Origination', 'Call from Municipality', 'A municipal staff person called a code officer and requested an investigation');


INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Action', 'Email', 'Email sent or received');

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Action', 'In-Person conversation', 'Talking to somebody about the case in person');

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Action', 'Post', 'Sending or receiving a post letter');

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Action', 'Phone Call', 'Initiating or receiving a phone call');


INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'PhaseChange', 'Change in Case Phase', 'System generated case transition event');


INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Closing', 'Compliance', 'All code violations associated with this case have been corrected');

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Timeline', 'Compliance Timeframe end', 'The compliance timeframe for a violation on this case has expired');

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Meeting', 'Court Hearing', 'A court hearing at any jurisdiction');

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (DEFAULT, 'Custom', 'Note', 'Case note of unspecified content');

INSERT INTO public.ceeventcategory(
            categoryid, categorytype, title, description)
    VALUES (150, 'Notice', 'Code Violation Update', 'A code violation in this case was updated');


INSERT INTO public.ceevent(
            eventid, ceeventcategory_catid, cecase_caseid, dateofrecord, 
            eventtimestamp, eventdescription, login_userid, disclosetomunicipality, 
            disclosetopublic, activeevent, notes)
    VALUES (DEFAULT, 100, 1000, now(), 
            now(), 'Called animal control office of COG Land and the person who answered started laughing at my question about goat breeding on front lawns. Looking for other sources', 100 , TRUE, 
            TRUE, TRUE, 'We may need to develop relationships with some rural townships to gain access to farm animal code experts.');
-- first default ID val is 1000


INSERT INTO public.courtentity(
            entityid, entityofficialnum, jurisdictionlevel, muni_municode, 
            name, address_street, address_city, address_zip, address_state, 
            county, phone, url, notes)
    VALUES (DEFAULT, '1234-56789', 'Municipal', 999, 
            'COG Land Supreme Court', '2700 MONREOVILL BLVD', 'Monroeville', '15221', 'PA', 
            'Allegheny', '1231231234', 'tcvcog.com', 'A sample court entity');

-- first default ID val is 100

INSERT INTO public.enforcementofficial(
            officialid, login_userid, badgenumber, orinumber, notes)
    VALUES (DEFAULT, 100, '9999-6666', 'ORI12345', 'SYSTEM ALERT: This is likely an imposter code enforcement official!');
-- first val is 100


INSERT INTO public.codesource(
            sourceid, name, year, description, isactive, url, notes)
    VALUES (DEFAULT, 'IPMC', 2015, 'International Property Maintenance Code', TRUE, 'http://www.cityoffortmeade.com/document_center/2012_International_Property_Maintenance_Code.pdf', 'Sample Code Source');
--default first val is 10

INSERT INTO public.codeset(
            codesetid, name, description, municipality_municode)
    VALUES (DEFAULT, 'COG Land Municpal Code', 'Test Code Set for COG Land', 999);
--default first val is 10

INSERT INTO public.codeelementtype(
            cdeltypeid, name, description)
    VALUES (DEFAULT, 'Exterior Regulations', 'Regulations Concerning the exterior of a dwelling structure on a property');
--id 1000

INSERT INTO public.codeelementtype(
            cdeltypeid, name, description)
    VALUES (DEFAULT, 'Interior Regulations', 'Regulations Concerning the interior of a dwelling structure on a property');
--id 1001

INSERT INTO public.codeelement(
            elementid, codeelementtype_cdeltypeid, codesource_sourceid, ordchapterno, 
            ordchaptertitle, ordsecnum, ordsectitle, ordsubsecnum, ordsubsectitle, 
            ordtechnicaltext, ordhumanfriendlytext, defaultpenalty, isactive, 
            isenforcementpriority, resourceurl, inspectiontips, dateCreated)
    VALUES (DEFAULT, 1001, 10, 5, 
            'Plumping facilities and fixutre requirements', '502', 'Required Facilities', '502.1', 'Dwelling Units', 
            'Every dwelling unit shall contain its own bathtub or shower, lavatory, water closet and kitchen sink which shall be maintained in a sanitary, safe working condition', 'All dwelling units need a bathroom and kitchen with basic amenities', 3.5, TRUE, 
            TRUE, 'www.kitchen.com', 'Ask property owner to see the ''bathroom'' and ''kitchen''', now());
--Default is 100


INSERT INTO public.codesetelement(
            codesetelementid, codeset_codesetid, codelement_elementid, elementmaxpenalty, 
            elementminpenalty, elementnormpenalty, penaltynotes, normdaystocomply, 
            daystocomplynotes)
    VALUES (DEFAULT, 10, 100, 1000, 
            100, 600, 'Usually reduced by magistrate to 400', 30, 
            'Start with 30 but folks usually take longer');

INSERT INTO public.codeviolation(
            violationid, codesetelement_elementid, cecase_caseid, citation_citationid, 
            dateofcitation, dateofrecord, entrytimestamp, stipulatedcompliancedate, 
            actualcompliancdate, penalty, description, notes)
    VALUES (DEFAULT, 1, 1000, NULL, 
            '2018-01-20 14:49:06.219519-05', '2018-01-20 14:49:06.219519-05', '2018-01-20 14:49:06.219519-05', '2018-02-20 14:49:06.219519-05', 
            NULL, 100, 'No bathroom, only bucket', 'We need a policy on buckets');


INSERT INTO public.coglog(
            logentryid, timeofentry, user_userid, sessionid, category, notes)
    VALUES (DEFAULT, now(), 100, 12341, 'Christopher won the lottery!', 'Check into legal rights to earnings');

INSERT INTO public.propertyperson(
            property_propertyid, person_personid)
    VALUES (11997, 101);

INSERT INTO public.citation(
            citationid, citationno, origin_courtentity_entityid, cecase_caseid, enforcementofficial_officialID, 
            dateofrecord, transtimestamp, isactive, notes)
    VALUES (DEFAULT, '999-1000',100 ,1000 ,100,
            now(), now(), TRUE, 'Sample citation to demonstrate system functionality.');
-- first default id val is 1000


INSERT INTO public.propertyunit(
            unitid, unitnumber, property_propertyid, notes)
    VALUES (DEFAULT, '1A', 11997, 'Basement');

INSERT INTO public.propertyunit(
            unitid, unitnumber, property_propertyid, notes)
    VALUES (DEFAULT, '1B', 11997, 'Main Floor');

INSERT INTO public.propertyunitperson(
            propertyunit_unitid, person_personid)
    VALUES (1000, 100 );

INSERT INTO public.propertyunitperson(
            propertyunit_unitid, person_personid)
    VALUES (1001, 101 );

INSERT INTO public.propertyusetype(
            propertyusetypeid, name, description)
    VALUES (DEFAULT, 'Multi-Unit', 'Property contains 2 or more dwelling units');
INSERT INTO public.propertyusetype(
            propertyusetypeid, name, description)
    VALUES (DEFAULT, 'Single Family', 'Property contains 1 dwelling unit');
INSERT INTO public.propertyusetype(
            propertyusetypeid, name, description)
    VALUES (DEFAULT, 'Duplex', 'Property is house-like and contains 2 dwelling units');
INSERT INTO public.propertyusetype(
            propertyusetypeid, name, description)
    VALUES (DEFAULT, 'Condominium', 'Please define Condominium');