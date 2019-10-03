
-- create sequences for auxillary tables

-- Table actionrequestor 

CREATE SEQUENCE actionrequestor_requestorid_seq;
ALTER TABLE public.actionrequestor ALTER COLUMN requestorid SET DEFAULT nextval('actionrequestor_requestorid_seq');

INSERT INTO public.actionrequestor(
            requestorid, requestorfname, requestorlname, phone, notes, actionrequestortype_typeid)
    VALUES (DEFAULT, 'Liz', 'Kozub', NULL, 'Code enforcement admin at TCVCOG', 1);

-- table actionrequestortype 

CREATE SEQUENCE actionrequestortype_typeid_seq;
ALTER TABLE public.actionrequestortype ALTER COLUMN typeid SET DEFAULT nextval('actionrequestortype_typeid_seq');

INSERT INTO public.actionrequestortype(
            typeid, typename, roledescription)
    VALUES (DEFAULT, 'COG staff', 'COG Staff member--not a code enforcement officer');

INSERT INTO public.actionrequestortype(
            typeid, typename, roledescription)
    VALUES (DEFAULT, 'Municipality staff', 'Non-COG staff who work for a municipality served by the CE team');

INSERT INTO public.actionrequestortype(
            typeid, typename, roledescription)
    VALUES (DEFAULT, 'CE Officer', 'Code enforcement officer who works for the TCVCOG');

INSERT INTO public.actionrequestortype(
		typeid, typename, roledescription)
	VALUES (DEFAULT, 'Citizen', 'Member of the public at large, not employed by a govt agency');

INSERT INTO public.actionrequestortype(
            typeid, typename, roledescription)
    VALUES (DEFAULT, 'Law Enforcement', 'Memver of law enforcement');

    INSERT INTO public.actionrequestortype(
            typeid, typename, roledescription)
    VALUES (DEFAULT, 'Councilperson', 'Sitting member of a town council of a municipality served by the TCVCOG');

-- table public.occpermitreason

ALTER TABLE public.occpermitreason ALTER COLUMN reasonDescription TYPE varchar (150);

CREATE SEQUENCE occpermitreason_reasonid_seq;
ALTER TABLE public.occpermitreason ALTER COLUMN reasonid SET DEFAULT nextval('occpermitreason_reasonid_seq');

INSERT INTO public.occpermitreason(
            reasonid, reasonname, reasondescription)
    VALUES (DEFAULT, 'New Tenant', 'A new tentant is moving in to a previously inspected unit');

INSERT INTO public.occpermitreason(
            reasonid, reasonname, reasondescription)
    VALUES (DEFAULT, 'New rental unit', 'A previously unrented unit has been converted into a rental');

INSERT INTO public.occpermitreason(
            reasonid, reasonname, reasondescription)
    VALUES (DEFAULT, 'Property Sale', 'A property sale is underway');

-- table public.inspectionStatus

CREATE SEQUENCE inspectionStatus_statusID_seq;
ALTER TABLE public.inspectionstatus ALTER COLUMN stausID SET DEFAULT nextval('inspectionStatus_statusID_seq');

INSERT INTO public.inspectionstatus(
            stausid, statusname, statusdescription)
    VALUES (DEFAULT, 'awaiting first inspection scheduling', 'inspection requested but not yet conducted');

INSERT INTO public.inspectionstatus(
            stausid, statusname, statusdescription)
    VALUES (DEFAULT, 'inspection scheduled', 'inspection requested but not yet conducted');

INSERT INTO public.inspectionstatus(
            stausid, statusname, statusdescription)
    VALUES (DEFAULT, 'passed inspection - permit from COG pending', 'unit passed inspection after one or more  inspections and permit has not yet been issued by COG');

INSERT INTO public.inspectionstatus(
            stausid, statusname, statusdescription)
    VALUES (DEFAULT, 'passed inspection - permit from COG issued', 'unit passed inspection after one or more inspections and permit has been isused');

INSERT INTO public.inspectionstatus(
            stausid, statusname, statusdescription)
    VALUES (DEFAULT, 'passed inspection - permit from muni pending', 'unit passed inspection after one or more inspections and municipality must issue permit');

INSERT INTO public.inspectionstatus(
            stausid, statusname, statusdescription)
    VALUES (DEFAULT, 'passed inspection - permit from muni issued', 'unit passed inspection after one or more inspections and municipality has issued permit');

INSERT INTO public.inspectionstatus(
            stausid, statusname, statusdescription)
    VALUES (DEFAULT, 'failed inspection - awaiting follow-up inspection', 'unit failed inspection after one or more failures');
-- Select * from inspectionstatus;

-- table pulic.paymenttpe

CREATE SEQUENCE paymenttype_typeid_seq;
ALTER TABLE public.paymenttype ALTER COLUMN typeid SET DEFAULT nextval('paymenttype_typeid_seq');

INSERT INTO public.paymenttype(
            typeid, name)
    VALUES (DEFAULT, 'check');

INSERT INTO public.paymenttype(
            typeid, name)
    VALUES (DEFAULT, 'money order');

INSERT INTO public.paymenttype(
            typeid, name)
    VALUES (DEFAULT, 'cash');

INSERT INTO public.paymenttype(
            typeid, name)
    VALUES (DEFAULT, 'credit card');

-- table actionrqstissuetype

CREATE SEQUENCE actionrqstissuetype_issueTypeID_seq;
ALTER TABLE public.actionrqstissuetype ALTER COLUMN issueTypeID SET DEFAULT nextval('actionrqstissuetype_issueTypeID_seq');

INSERT INTO public.actionrqstissuetype(
            issuetypeid, typename, typedescription, notes)
    VALUES (DEFAULT, 'Overgrown grass or weeds', 'residence has weeds or grass that are overgrown', NULL);

INSERT INTO public.actionrqstissuetype(
            issuetypeid, typename, typedescription, notes)
    VALUES (DEFAULT, 'Improperly stored trash or waste', 'trash or waste exists outside of appropriate cans or receptacles', NULL);

INSERT INTO public.actionrqstissuetype(
            issuetypeid, typename, typedescription, notes)
    VALUES (DEFAULT, 'Abandoned vehicles', 'A vehicle is improperly stored on the street or on the property', NULL);

-- table codeviolationtype

CREATE SEQUENCE codeviolationtype_codeViolationTypeID_seq;
ALTER TABLE public.codeViolationType ALTER COLUMN codeViolationTypeID SET DEFAULT nextval('codeviolationtype_codeViolationTypeID_seq');

INSERT INTO public.codeviolationtype(
            codeviolationtypeid, typename, typedescription)
    VALUES (DEFAULT, 'Rubbish', 'waste or trash outside property');

INSERT INTO public.codeviolationtype(
            codeviolationtypeid, typename, typedescription)
    VALUES (DEFAULT, 'Property Care', 'high grass, weeds, overgrown shrubs');

INSERT INTO public.codeviolationtype(
            codeviolationtypeid, typename, typedescription)
    VALUES (DEFAULT, 'Unsafe Living Conditions', 'Any number of electrical or carpentry-related issues');

INSERT INTO public.codeviolationtype(
            codeviolationtypeid, typename, typedescription)
    VALUES (DEFAULT, 'delapidated buildings', 'run down or junked building structure');

INSERT INTO public.codeviolationtype(
            codeviolationtypeid, typename, typedescription)
    VALUES (DEFAULT, 'Improper vehicle storage', 'vehicles stored on property in plain sight and unisghtly');


-- codeenfeventtype

CREATE SEQUENCE codeenfeventtype_codeenfeventtypeid_seq;
ALTER TABLE public.codeenfeventtype ALTER COLUMN codeenfeventtypeid SET DEFAULT nextval('codeenfeventtype_codeenfeventtypeid_seq');

INSERT INTO public.codeenfeventtype(
            codeenfeventtypeid, typename, typedescription)
    VALUES (DEFAULT, 'letter', 'a letter was sent to occupant and or property owner');

INSERT INTO public.codeenfeventtype(
            codeenfeventtypeid, typename, typedescription)
    VALUES (DEFAULT, 'first visit to property - no answer', 'a visit was made to the property in question and nobody answered the door');

INSERT INTO public.codeenfeventtype(
            codeenfeventtypeid, typename, typedescription)
    VALUES (DEFAULT, 'first visit - conversation occured', 'a covnersation was had with the occupant');

INSERT INTO public.codeenfeventtype(
            codeenfeventtypeid, typename, typedescription)
    VALUES (DEFAULT, 'follow-up call made to occupant', 'a call was made to occupants or owners concerning the violation');

INSERT INTO public.codeenfeventtype(
            codeenfeventtypeid, typename, typedescription)
    VALUES (DEFAULT, 'follow-up visit made - no ansswer', 'a follow-up visit was made to the property but nobody answered the door');

INSERT INTO public.codeenfeventtype(
            codeenfeventtypeid, typename, typedescription)
    VALUES (DEFAULT, 'follow-up visit made - conversation occurred', 'a follow-up visit was made to the property but nobody answered the door');

INSERT INTO public.codeenfeventtype(
            codeenfeventtypeid, typename, typedescription)
    VALUES (DEFAULT, 'case referred to magistrate', 'after unsuccesful resolution through citations, the case referred to a magistrate for arbitration');

-- table codeenfcasestatus

CREATE SEQUENCE codeenfcasestatus_ceeventstatusid_seq;
ALTER TABLE public.codeenfcasestatus ALTER COLUMN ceeventstatusid SET DEFAULT nextval('codeenfcasestatus_ceeventstatusid_seq');


INSERT INTO public.codeenfcasestatus(
            ceeventstatusid, statusname, statusdescription)
    VALUES (DEFAULT, 'open', 'a code officer has deemed issue a violation');

INSERT INTO public.codeenfcasestatus(
            ceeventstatusid, statusname, statusdescription)
    VALUES (DEFAULT, 'closed', 'applicable violations have been remedied');

INSERT INTO public.codeenfcasestatus(
            ceeventstatusid, statusname, statusdescription)
    VALUES (DEFAULT, 'under active investigation', 'code enforcement staff have this prop under invesiation');

INSERT INTO public.codeenfcasestatus(
            ceeventstatusid, statusname, statusdescription)
    VALUES (DEFAULT, 'active compliance window', '');

INSERT INTO public.codeenfcasestatus(
            ceeventstatusid, statusname, statusdescription)
    VALUES (DEFAULT, 'stalled - unable to contact and violations continue to exist', '');

INSERT INTO public.codeenfcasestatus(
            ceeventstatusid, statusname, statusdescription)
    VALUES (DEFAULT, 'exploded', 'kaboom');




CREATE SEQUENCE codeofficer_officerid_seq;
ALTER TABLE public.codeofficer ALTER COLUMN officerid SET DEFAULT nextval('codeofficer_officerid_seq');

INSERT INTO public.codeofficer(
            officerid, firstname, lastname, hiredate, workenddate, roledescription)
    VALUES (DEFAULT, 'Gary', 'J', NULL, NULL, '');

INSERT INTO public.codeofficer(
            officerid, firstname, lastname, hiredate, workenddate, roledescription)
    VALUES (DEFAULT, 'Jesse', 'J', NULL, NULL, '');

INSERT INTO public.codeofficer(
            officerid, firstname, lastname, hiredate, workenddate, roledescription)
    VALUES (DEFAULT, 'Nick', 'J', NULL, NULL, '');

INSERT INTO public.codeofficer(
            officerid, firstname, lastname, hiredate, workenddate, roledescription)
    VALUES (DEFAULT, 'Dror', 'J', NULL, NULL, '');































