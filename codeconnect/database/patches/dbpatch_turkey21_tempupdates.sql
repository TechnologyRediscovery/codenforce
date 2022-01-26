INSERT INTO public.icon(
            iconid, name, styleclass, fontawesome, materialicons)
    VALUES (33, 'final review', 'final-review', 'fa fa-clipboard', 'pending_actions');

UPDATE public.icon SET materialicons='Door_Back' WHERE iconid=13;

-- we need to key to enforcable code element not a code element itself
ALTER TABLE occchecklistspacetypeelement ADD COLUMN codesetelement_seteleid INTEGER 
	CONSTRAINT occchecklistspacetypeelement_seteleid_fk REFERENCES codesetelement (codesetelementid);

-- TODO: Remove the codeelment_id column of occchecklistspacetypeelement
-- after the refactor 


-- How do we tell which users are code officers? It's not enough to just have ranks, 
-- since some CEOs might be ranked dev, but not all devs are sworn offiers
-- solution: add an oathdate and oath court entity to UMAPs then 
-- we can pull valid umaps with oath dates to get users for assignment
-- to code officer stuff
ALTER TABLE loginmuniauthperiod ADD COLUMN oathts TIMESTAMP WITH TIME ZONE;
ALTER TABLE loginmuniauthperiod ADD COLUMN oathcourtentity_entityid INTEGER
	CONSTRAINT loginmuniauthperiod_oathcourtentityid_fk REFERENCES courtentity (entityid);



-- ****************************************************************************
-- RUN THIS CREATE TYPE BY ITSELF!
CREATE TYPE systemdomain AS ENUM ('CodeEnforcement','Occupancy','Universal');

-- ****************************************************************************


-- RUN LOCALLY TO HERE

ALTER TABLE occinspectiondetermination ADD COLUMN domain systemdomain;
ALTER TABLE occinspectiondetermination ADD COLUMN requiremigrationtoce BOOLEAN;




CREATE SEQUENCE IF NOT EXISTS occceelementmigrationlog_migrationid_seq
    START WITH 1000
    INCREMENT BY 1
    MINVALUE 1000
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.occceelementmigrationlog
(
	migrationid 						INTEGER PRIMARY KEY DEFAULT nextval('occceelementmigrationlog_migrationid_seq'),
	inspectionitem_ocsteleid			INTEGER NOT NULL CONSTRAINT occceelementmigrationlog_ocstelid_fk REFERENCES occchecklistspacetypeelement (spacelementid),
	codeviolation_codeviolatoinid		INTEGER NOT NULL CONSTRAINT occceelementmigrationlog_violationid_fk REFERENCES codeviolatoin (violationid),
	createdby_userid					INTEGER NOT NULL CONSTRAINT occceelementmigrationlog_createdbyuserid_fk REFERENCES login (userid),
	createdts							TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
	batchid								INTEGER NOT NULL,
	deactivatedts						TIMESTAMP WITH TIME ZONE
);