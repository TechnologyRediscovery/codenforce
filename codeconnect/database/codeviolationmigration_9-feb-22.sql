
-- step 1: copy violations to the same case
BEGIN;

INSERT INTO public.codeviolation(
            codesetelement_elementid, cecase_caseid, dateofrecord, 
            entrytimestamp, stipulatedcompliancedate, actualcompliancedate, 
            penalty, description, notes, legacyimport, compliancetimestamp, 
            complianceuser, severity_classid, createdby, compliancetfexpiry_proposalid, 
            lastupdatedts, lastupdated_userid, active, compliancenote, nullifiedts, 
            nullifiedby, bobsource_sourceid)

SELECT codesetelement_elementid, cecase_caseid, dateofrecord, 
       entrytimestamp, stipulatedcompliancedate, actualcompliancedate, 
       penalty, description, notes, legacyimport, compliancetimestamp, 
       complianceuser, severity_classid, createdby, compliancetfexpiry_proposalid, 
       lastupdatedts, lastupdated_userid, active, compliancenote, nullifiedts, 
       nullifiedby, bobsource_sourceid
  FROM public.codeviolation WHERE cecase_caseid=58570 AND actualcompliancedate IS NULL;

  COMMIT;




-- step 2: Update copied violations
BEGIN;

UPDATE codeviolation set cecase_caseid = 59637, entrytimestamp=now(), stipulatedcompliancedate='2022-03-09'  WHERE violationid >= 7412 and violationid <= 7419;

COMMIT;