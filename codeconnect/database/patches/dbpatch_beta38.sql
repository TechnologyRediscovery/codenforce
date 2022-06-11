
-- PUBLISHED and run on remote SB22
-- DB Patch 38: Final clean up tweaks for alpha launch SB22

--  >>>>>>>>>>>> UNCOMMENT AND RUN CREATE TYPE  ALONE <<<<<<<<<<<<<<
--  >>>>>>>>>>>> UNCOMMENT AND RUN CREATE TYPE  ALONE <<<<<<<<<<<<<<
-- ****************************************************************************
-- RUN THIS CREATE TYPE BY ITSELF!
-- Run on remote SB22
--CREATE TYPE systemdomain AS ENUM ('CodeEnforcement','Occupancy','Universal');
--ALTER TYPE linkedobjectroleschema ADD VALUE 'CitationDocketHuman';

-- ****************************************************************************



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





ALTER TABLE blobtype ADD COLUMN contenttypestring TEXT;
ALTER TABLE blobtype ADD COLUMN browserviewable BOOLEAN DEFAULT FALSE;
ALTER TABLE blobtype ADD COLUMN notes TEXT;
ALTER TABLE blobtype ADD COLUMN fileextensionsarr TEXT[];

ALTER TABLE public.photodoc ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.photodoc ADD COLUMN lastupdatedby_userid INTEGER 
	CONSTRAINT photodoc_lastupdatedby_fk REFERENCES login (userid);

ALTER TABLE public.photodoc ADD COLUMN deactivatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.photodoc ADD COLUMN deactivatedby_userid INTEGER 
	CONSTRAINT photodoc_deactivatedby_fk REFERENCES login (userid);


INSERT INTO public.blobtype(
            typeid, typetitle, icon_iconid, contenttypestring, browserviewable, 
            notes, fileextensionsarr)
    VALUES (200, 'PDF', 10, 'application/pdf', TRUE, 
            NULL, '{"pdf", "PDF"}');

INSERT INTO public.blobtype(
            typeid, typetitle, icon_iconid, contenttypestring, browserviewable, 
            notes, fileextensionsarr)
    VALUES (201, 'JPEG image', 10, 'image/jpeg', TRUE, 
            NULL, '{"jpg", "JPEG", "JPG"}');

INSERT INTO public.blobtype(
            typeid, typetitle, icon_iconid, contenttypestring, browserviewable, 
            notes, fileextensionsarr)
    VALUES (202, 'PNG image', 10, 'image/png', TRUE, 
            NULL, '{"PNG", "png"}');

INSERT INTO public.blobtype(
            typeid, typetitle, icon_iconid, contenttypestring, browserviewable, 
            notes, fileextensionsarr)
    VALUES (203, 'Microsoft Corporation word document docx', 10, 'proprietary', FALSE, 
            NULL, '{"docx", "DOCX"}');




--ALTER TABLE occchecklistspacetypeelement DROP COLUMN codeelement_elementid; -- replaced by FK to codesetelement!



ALTER TABLE linkedobjectrole RENAME COLUMN lorschema_schemaid TO lorschema;




ALTER TABLE occinspectiondetermination ADD COLUMN domain systemdomain;
ALTER TABLE occinspectiondetermination ADD COLUMN requiremigrationtoce BOOLEAN;


ALTER TABLE noticeofviolation ADD COLUMN fixednotifyingofficername text;
ALTER TABLE noticeofviolation ADD COLUMN fixednotifyingofficertitle text;
ALTER TABLE noticeofviolation ADD COLUMN fixednotifyingofficerphone text;
ALTER TABLE noticeofviolation ADD COLUMN fixednotifyingofficeremail text;
ALTER TABLE noticeofviolation ADD COLUMN notifyingofficer_humanid integer 
    CONSTRAINT nov_notifyingofficer_humanid_fk 
    REFERENCES public.human (humanid) ;

ALTER TABLE contactemail ADD COLUMN priority INTEGER DEFAULT 1;
ALTER TABLE contactphone ADD COLUMN priority INTEGER DEFAULT 1;
ALTER TABLE humanmailingaddress ADD COLUMN priority INTEGER DEFAULT 1;
ALTER TABLE parcelmailingaddress ADD COLUMN priority INTEGER DEFAULT 1;



-- RUN LOCALLY TO HERE

-- don't need these since photodoc has a type assocaited with it that can be any file type

-- DROP TABLE public.ceactionrequestpdfdoc;
-- DROP TABLE public.codeviolationpdfdoc;
-- DROP TABLE public.parcelpdfdoc;



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (38, 'database/patches/dbpatch_beta38.sql','03-10-2022', 'ecd', 'Final cleanup stuff for DB upgrade for humanui SB22');

-- Output on SB22 upgrade
--Query returned successfully: one row affected, 82 msec execution time.