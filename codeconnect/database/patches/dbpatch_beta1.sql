BEGIN;

ALTER TABLE codeviolation ADD COLUMN compliancetimestamp TIMESTAMP WITH TIME ZONE;
ALTER TABLE codeviolation ADD COLUMN complianceuser INTEGER CONSTRAINT codeviolation_complianceofficer_fk REFERENCES login (userid);
ALTER TABLE codeviolation ADD COLUMN compliancetfevent INTEGER CONSTRAINT codeviolation_compliancetfevent_fk REFERENCES ceevent (eventid);

ALTER TABLE occupancypermitapplication RENAME COLUMN "multiUnit" TO multiunit;
ALTER TABLE occupancypermitapplication RENAME COLUMN contatperson_personid TO contactperson_personid;


CREATE TABLE photodoctype
(
    typeid                  INTEGER PRIMARY KEY,
    typeTitle               text
) ;


INSERT INTO public.photodoctype(
            typeid, typetitle)
    VALUES (1, 'photo');
INSERT INTO public.photodoctype(
            typeid, typetitle)
    VALUES (2, 'pdf');

ALTER TABLE photodoc ADD CONSTRAINT photodoc_photodoctype_fk FOREIGN KEY ( photodoctype_typeid) REFERENCES photodoctype (typeid) ;

CREATE TABLE public.codeviolationphotodoc
(
  photodoc_photodocid integer NOT NULL,
  codeviolation_violationid integer NOT NULL,
  CONSTRAINT codeviolationphotodoc_pk PRIMARY KEY (photodoc_photodocid, codeviolation_violationid),
  CONSTRAINT codeviolationphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid)
      REFERENCES public.photodoc (photodocid),
  CONSTRAINT codeviolationphotodoc_cv_fk FOREIGN KEY (codeviolation_violationid)
      REFERENCES public.codeviolation (violationid) 
);

CREATE TABLE public.inspectedchecklistspaceelementphotodoc
(
  photodoc_photodocid integer NOT NULL,
  inspchklstspele_elementid integer NOT NULL,
  CONSTRAINT inspchklstspele_pk PRIMARY KEY (photodoc_photodocid, inspchklstspele_elementid),
  CONSTRAINT inspchklstspele_elementid_phdoc_fk FOREIGN KEY (photodoc_photodocid)
      REFERENCES public.photodoc (photodocid),
  CONSTRAINT inspchklstspele_elementid_cv_fk FOREIGN KEY (inspchklstspele_elementid)
      REFERENCES public.codeviolation (violationid) 
);

CREATE TABLE public.ceactionrequestphotodoc
(
  photodoc_photodocid integer NOT NULL,
  ceactionrequest_requestid integer NOT NULL,
  CONSTRAINT ceactionrequestphotodoc_pk PRIMARY KEY (photodoc_photodocid, ceactionrequest_requestid),
  CONSTRAINT ceactionrequestphotodoc_phdoc_fk FOREIGN KEY (photodoc_photodocid)
      REFERENCES public.photodoc (photodocid),
  CONSTRAINT ceactionrequestphotodoc_cear_fk FOREIGN KEY (ceactionrequest_requestid)
      REFERENCES public.ceactionrequest (requestid) 
);

COMMIT;