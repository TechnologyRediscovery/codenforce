
-- database patch for creating the database infrastructure to allow
-- offile mobile completion of occupancy inspection 

-- thanks to CHEN&CHEN!!

CREATE SEQUENCE IF NOT EXISTS occinspectiondispatch_dispatchid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.occinspectiondispatch
(
	dispatchid 			INTEGER PRIMARY KEY DEFAULT nextval('occinspectiondispatch_dispatchid_seq'),
	createdby_userid	INTEGER NOT NULL CONSTRAINT occinspectiondispatch_createdby_userid_fk REFERENCES login (userid),
	creationts 			TIMESTAMP WITH TIME ZONE NOT NULL,
	dispatchnotes		TEXT,
	inspection_inspectionID INTEGER NOT NULL CONSTRAINT occinspectiondispatch_occinspectionid_fk REFERENCES occinspection (inspectionid),
	retrievalts			TIMESTAMP WITH TIME ZONE,
	retrievedby_userid	INTEGER CONSTRAINT occinspectiondispatch_retrievedby_userid_fk REFERENCES login (userid),
	synchronizationts 	TIMESTAMP WITH TIME ZONE,
	synchronizationnotes TEXT,
	municipality_municode 	INTEGER NOT NULL CONSTRAINT occinspectiondispatch_municode_fk REFERENCES municipality (municode),
	municipalityname	TEXT
);

