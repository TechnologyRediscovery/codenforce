--codeviolation

UPDATE codeviolation SET notes = 'this is a test 514v1' WHERE violationid = 1026; 
SELECT caseid,cecase.lastupdatedts, cecase.lastupdatedby_userid FROM cecase INNER JOIN codeviolation ON  (caseid = cecase_caseid and violationid = 1026);

--test citationviolation

UPDATE citationviolation SET notes = 'this is a test 514v1' WHERE citationviolationid = 9; 
SELECT cecase_caseid FROM codeviolation INNER JOIN citationviolation ON (violationid = codeviolation_violationid) and citationviolationid = 9;
SELECT caseid, lastupdatedby_userid, lastupdatedts FROM cecase where caseid = 1011;

--test citation

UPDATE citation SET notes = 'this is a test 514v1' WHERE citationid = 1035; 
SELECT cecase_caseid, cecase.lastupdatedby_userid, cecase.lastupdatedts FROM cecase INNER JOIN codeviolation ON (caseid = cecase_caseid) INNER JOIN citationviolation ON (violationid = codeviolation_violationid ) WHERE citation_citationid = 1035;

--citationdocketno
UPDATE citationdocketno SET notes = 'this is a test 514v1' WHERE docketid = 102; 
SELECT cecase_caseid,lastupdatedts,lastupdatedby_userid  FROM codeviolation INNER JOIN citationdocketno ON (citation_citationid = citation_citationid and docketid=102);
SELECT cecase_caseid, cecase.lastupdatedby_userid, cecase.lastupdatedts FROM cecase INNER JOIN codeviolation ON (caseid = cecase_caseid) INNER JOIN citationviolation ON citation_citationid = citation_citationid INNER JOIN citationdocketno ON citationviolation.citation_citationid = citationdocketno.citation_citationid and docketid=102;

--citationdocketno

UPDATE citationdocketno SET notes = 'this is a test 514v1' WHERE docketid = 102; 
SELECT cecase_caseid, cecase.lastupdatedts, cecase.lastupdatedby_userid FROM cecase INNER JOIN codeviolation ON (caseid = cecase_caseid) INNER JOIN citationviolation ON(codeviolation_violationid = violationid) INNER JOIN citationdocketno ON (citationviolation.citation_citationid = citationdocketno.citation_citationid) and (docketid=102);

--citationdocketnohuman

UPDATE citationdocketnohuman SET notes = 'this is a test 514v2' WHERE linkid = 1026;  
SELECT cecase_caseid, cecase.lastupdatedts, cecase.lastupdatedby_userid FROM cecase INNER JOIN codeviolation ON (caseid = cecase_caseid) INNER JOIN citationviolation ON(codeviolation_violationid = violationid) INNER JOIN citationdocketno ON (citationviolation.citation_citationid = citationdocketno.citation_citationid)INNER JOIN citationdocketnohuman ON (citationdocketno.docketid = citationdocketnohuman.docketno_docketid) and (docketid=102);


--citationcitationstatus 

UPDATE citationcitationstatus SET notes = 'test of new trigr' WHERE citation_citationid = 1061;
SELECT cecase_caseid, lastupdatedts, lastupdatedby_userid FROM citationcitationstatus INNER JOIN citationdocketno ON (citation_citationid = citation_citationid and docketid=102);
SELECT cecase_caseid, cecase.lastupdatedby_userid, cecase.lastupdatedts FROM cecase INNER JOIN codeviolation ON (caseid = cecase_caseid) INNER JOIN citationviolation ON citation_citationid = citation_citationid INNER JOIN citationcitationstatus ON citationviolation.citation_citationid = citationcitationstatus.citation_citationid and citationcitationstatus.citation_citationid=1061;

--occpermit

UPDATE occpermit SET notes = 'this is a test 514v1' WHERE permitid = 1044; 
SELECT occperiod.periodid, lastupdatedts,lastupdatedby_userid FROM occpermit INNER JOIN occperiod ON (occperiod_periodid = periodid) WHERE permitid = 1044;

--occperiod

UPDATE occperiod SET notes = 'this is a test 514v1' WHERE periodid = 1059; 
SELECT occperiod.periodid, lastupdatedts,lastupdatedby_userid FROM occpermit INNER JOIN occperiod ON (occperiod_periodid = periodid) WHERE permitid = 1059;

--occperiodpermitapplication

UPDATE occperiodpermitapplication SET occpermitapp_applicationid = '1059' WHERE occpermitapp_applicationid = 1011; 
SELECT occperiod.periodid, lastupdatedts,lastupdatedby_userid FROM occperiodpermitapplication INNER JOIN occperiod ON (occperiod_periodid = periodid) WHERE (occpermitapp_applicationid = 1011);


--occperiodphotodoc

UPDATE occperiodphotodoc SET photodoc_photodocid = 1020 WHERE occperiod_periodid= 1059;
SELECT occperiod.periodid, lastupdatedts,lastupdatedby_userid FROM occperiod  INNER JOIN occperiodphotodoc ON (periodid = occperiod_periodid) WHERE photodoc_photodocid = 1020;

--occinspection

UPDATE occinspection SET notespreinspection = 'this is a test 514v1' WHERE inspectionid = 82;
SELECT occperiod.periodid, occperiod.lastupdatedts, occperiod.lastupdatedby_userid FROM occperiod INNER JOIN occinspection ON (periodid = occperiod_periodid) WHERE inspectionid = 82;

--UPDATE citation SET notes SET notes = 'this is a test 513v1' WHERE citationid = 1044;      

--contactemail 
UPDATE contactemail SET notes = 'this is a test 513v2parcel' WHERE emailid = 104;
SELECT human_humanid, human.lastupdatedts, human.lastupdatedby_userid FROM contactemail INNER JOIN human ON (human_humanid = humanid and emailid = 104); 

--contactemail  
UPDATE contactphone SET notes = 'this is a test 513v2parcel' WHERE phoneid = 116;
SELECT human_humanid, human.lastupdatedts, human.lastupdatedby_userid FROM contactphone INNER JOIN human ON (human_humanid = humanid and phoneid = 116);

--humanmuni
UPDATE humanmuni SET notes = 'this is a test 513v2parcel' WHERE linkid = 100;
SELECT human_humanid, human.lastupdatedts, human.lastupdatedby_userid FROM humanmuni     INNER JOIN human ON  (human_humanid = humanid and linkid = 100);


--humancecase
UPDATE humancecase SET notes = 'this is a test 513v2 parcel' WHERE cecase_caseid = 1044;
SELECT human_humanid, human.lastupdatedts, human.lastupdatedby_userid FROM humancecase  INNER JOIN human ON (human_humanid = humanid  and cecase_caseid = 1044);


--humanparcel
UPDATE humanparcel SET notes = 'this is a test 513v2' WHERE linkid = 61988; 
SELECT human_humanid, human.lastupdatedts, human.lastupdatedby_userid FROM humanparcel  INNER JOIN human ON  (human_humanid = humanid and linkid = 61988);

--humanoccperiod
UPDATE humanoccperiod SET notes = 'this is a test 513v2 parcel' WHERE linkid = 1044;
SELECT human_humanid, human.lastupdatedts, human.lastupdatedby_userid FROM humanoccperiod INNER JOIN human ON  (human_humanid = humanid and linkid = 1044);

--humanparcelunit
UPDATE humanparcelunit SET notes = 'this is a test 513v2 parcel' WHERE linkid = 103; 
SELECT human_humanid, human.lastupdatedts, human.lastupdatedby_userid FROM humanparcel  INNER JOIN human ON  (human_humanid = humanid and linkid = 61988);


--parcelinfo-
UPDATE parcelinfo SET notes = 'this is a test 513v1' WHERE parcelinfoid = 29171;
SELECT parcel_parcelkey FROM parcelinfo INNER JOIN parcel ON  (parcel_parcelkey = parcelkey and parcelinfoid = 29171); 

--parcelunit
UPDATE parcelunit SET notes = 'this is a test 513v1' WHERE unitid = 1044; 
SELECT parcelkey, parcel.lastupdatedts, parcel.lastupdatedby_userid  FROM parcelunit INNER JOIN parcel ON  (parcel_parcelkey  = parcelkey and unitid = 1044); 

--humanparcel
UPDATE humanparcel  SET notes = 'this is a test 513v1' WHERE linkid = 61987;
SELECT parcel_parcelkey,parcel.lastupdatedts, parcel.lastupdatedby_userid FROM humanparcel INNER JOIN parcel ON (parcel_parcelkey = parcelkey and linkid = 61987);

--parcelphotodoc
UPDATE parcelphotodoc SET photdoc_photodocid = 98 WHERE parcel_parcelkey = 1044; 
SELECT parcel_parcelkey, parcel.lastupdatedts, parcel.lastupdatedby_userid FROM parcelphotodoc INNER JOIN parcel ON (parcel_parcelkey = parcelkey and parcel_parcelkey = 1044);

--parcelmailingaddress
UPDATE parcelmailingaddress SET notes = 'this is a test 513v1' WHERE mailingaddress_addressid = 69947;
SELECT parcel.parcelkey, parcel.lastupdatedts, parcel.lastupdatedby_userid FROM parcel INNER JOIN parcelmailingaddress ON parcelkey = parcel_parcelkey and mailingaddress_addressid = 69947;

--parcel 
UPDATE parcel SET notes = 'this is a test 513v1' WHERE parcelkey = 2387;
SELECT lastupdatedts, lastupdatedby_userid FROM parcel WHERE parcelkey = 2387; 
   

 











SELECT occperiod.periodid, occperiod.lastupdatedts, occperiod.lastupdatedby_userid FROM occperiod INNER JOIN occinspection ON (periodid = occperiod_periodid) WHERE inspectionid = 82;
 
