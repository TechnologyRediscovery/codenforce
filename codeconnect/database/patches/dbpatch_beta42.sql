-- PATCH 42



ALTER TABLE public.parcelinfo ADD COLUMN landbankprospectstart TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.parcelinfo ADD COLUMN landbankprospectstop TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.parcelinfo ADD COLUMN landbankprospectnotes TEXT;
ALTER TABLE public.parcelinfo ADD COLUMN landbankacqcandidatestart TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.parcelinfo ADD COLUMN landbankacqcandidatestop TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.parcelinfo ADD COLUMN landbankacqcandidatenotes TEXT;
ALTER TABLE public.parcelinfo ADD COLUMN landbankpursuingstart TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.parcelinfo ADD COLUMN landbankpursuingstop TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.parcelinfo ADD COLUMN landbankpursuingnotes TEXT;
ALTER TABLE public.parcelinfo ADD COLUMN landbankownedstart TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.parcelinfo ADD COLUMN landbankownedstop TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.parcelinfo ADD COLUMN landbankownednotes TEXT;


ALTER TABLE public.parcelinfo ADD COLUMN landbankprospectstartby_userid INTEGER CONSTRAINT parcelinfo_landbankprospectstart_userid_fk REFERENCES login (userid);
ALTER TABLE public.parcelinfo ADD COLUMN landbankprospectstopby_userid INTEGER CONSTRAINT parcelinfo_landbankprospectstop_userid_fk REFERENCES login (userid);
ALTER TABLE public.parcelinfo ADD COLUMN landbankacqcandidatestartby_userid INTEGER CONSTRAINT parcelinfo_landbankacqcandidatestart_userid_fk REFERENCES login (userid);
ALTER TABLE public.parcelinfo ADD COLUMN landbankacqcandidatestopby_userid INTEGER CONSTRAINT parcelinfo_landbankacqcandidatestop_userid_fk REFERENCES login (userid);
ALTER TABLE public.parcelinfo ADD COLUMN landbankpursuingstartby_userid INTEGER CONSTRAINT parcelinfo_landbankpursuingstart_userid_fk REFERENCES login (userid);
ALTER TABLE public.parcelinfo ADD COLUMN landbankpursuingstopby_userid INTEGER CONSTRAINT parcelinfo_landbankpursuingstop_userid_fk REFERENCES login (userid);
ALTER TABLE public.parcelinfo ADD COLUMN landbankownedstartby_userid INTEGER CONSTRAINT parcelinfo_landbankownedstart_userid_fk REFERENCES login (userid);
ALTER TABLE public.parcelinfo ADD COLUMN landbankownedstopby_userid INTEGER CONSTRAINT parcelinfo_landbankownedstop_userid_fk REFERENCES login (userid);




-- TEST
-- SELECT * FROM public.event WHERE cecase_caseid IN (SELECT caseid FROM public.cecase WHERE propertyinfocase = TRUE);
-- SELECT * FROM public.cecase WHERE proeprtyinfocase = TRUE;

-- live server execution results
DELETE FROM public.event WHERE cecase_caseid IN (SELECT caseid FROM public.cecase WHERE propertyinfocase = TRUE);
--Query returned successfully: 16236 rows affected, 02:21 minutes execution time.
DELETE FROM public.cecase WHERE proeprtyinfocase = TRUE;
--



ALTER TABLE public.muniprofile ADD COLUMN priorityparamdeadlineadminbufferdays INTEGER DEFAULT 10;
ALTER TABLE public.muniprofile ADD COLUMN priorityparamnoletterbufferdays INTEGER DEFAULT 3;
ALTER TABLE public.muniprofile ADD COLUMN priorityparamprioritizeletterfollowupbuffer BOOLEAN DEFAULT FALSE;
ALTER TABLE public.muniprofile ADD COLUMN priorityparamalloweventcatgreenbuffers BOOLEAN DEFAULT TRUE;







CREATE TABLE public.cecasepin
(
    cecase_caseid INTEGER CONSTRAINT cecasepin_caseid_fk REFERENCES cecase (caseid),
    pinnedby_userid INTEGER CONSTRAINT cecasepin_userid_fk REFERENCES login (userid),
    createdts TIMESTAMP WITH TIME ZONE DEFAULT now(),
    deactivatedts TIMESTAMP WITH TIME ZONE,
    CONSTRAINT cecasepin_pk PRIMARY KEY (cecase_caseid, pinnedby_userid)
);



CREATE TABLE public.occperiodpin
(
    occperiod_periodid INTEGER CONSTRAINT occperiodpin_caseid_fk REFERENCES occperiod (periodid),
    pinnedby_userid INTEGER CONSTRAINT occperiodpin_userid_fk REFERENCES login (userid),
    createdts TIMESTAMP WITH TIME ZONE DEFAULT now(),
    deactivatedts TIMESTAMP WITH TIME ZONE,
    CONSTRAINT occperiodpin_pk PRIMARY KEY (occperiod_periodid, pinnedby_userid)
);





ALTER TABLE public.eventcategory ADD COLUMN prioritygreenbufferdays INTEGER DEFAULT 0;

ALTER TABLE public.municipality ADD COLUMN defaultheaderimage_photodocid INTEGER CONSTRAINT muni_defheader_fk REFERENCES public.photodoc (photodocid);
ALTER TABLE public.municipality ADD COLUMN defaultheaderimageheightpx INTEGER DEFAULT 250;


ALTER TYPE eventtype ADD VALUE 'OccupancyOrigination';
ALTER TYPE eventtype ADD VALUE 'OccupancyClosing';
ALTER TYPE eventtype ADD VALUE 'Inspection';

ALTER TABLE public.event ADD COLUMN parcel_parcelkey INTEGER CONSTRAINT event_parcelkey_fk REFERENCES parcel (parcelkey);


ALTER TABLE public.event ADD COLUMN human_humanid INTEGER 
    CONSTRAINT event_humanid_fk REFERENCES human (humanid);

CREATE SEQUENCE public.occinspectiondispatch_dispatchid_seq
  INCREMENT 1
  MINVALUE 100
  MAXVALUE 9223372036854775807
  START 100
  CACHE 1;
ALTER TABLE public.occinspectiondispatch_dispatchid_seq
  OWNER TO sylvia;


CREATE TABLE IF NOT EXISTS public.occinspectiondispatch
(
    dispatchid integer NOT NULL DEFAULT nextval('occinspectiondispatch_dispatchid_seq'::regclass),
    createdby_userid integer NOT NULL,
    creationts timestamp with time zone NOT NULL,
    dispatchnotes text COLLATE pg_catalog."default",
    inspection_inspectionid integer NOT NULL,
    retrievalts timestamp with time zone,
    retrievedby_userid integer,
    synchronizationts timestamp with time zone,
    synchronizationnotes text COLLATE pg_catalog."default",
    municipality_municode integer NOT NULL,
    municipalityname text COLLATE pg_catalog."default",
    CONSTRAINT occinspectiondispatch_pkey PRIMARY KEY (dispatchid),
    CONSTRAINT occinspectiondispatch_createdby_userid_fk FOREIGN KEY (createdby_userid)
        REFERENCES public.login (userid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT occinspectiondispatch_municode_fk FOREIGN KEY (municipality_municode)
        REFERENCES public.municipality (municode) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT occinspectiondispatch_occinspectionid_fk FOREIGN KEY (inspection_inspectionid)
        REFERENCES public.occinspection (inspectionid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT occinspectiondispatch_retrievedby_userid_fk FOREIGN KEY (retrievedby_userid)
        REFERENCES public.login (userid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.occinspectiondispatch
    OWNER to sylvia;

ALTER TABLE public.occinspectiondispatch ADD COLUMN deactivatedts TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occinspectiondispatch ADD COLUMN deactivatedby_userid INTEGER CONSTRAINT occinspectiondispatch_deacbyuserid_fk 
    REFERENCES login (userid);


ALTER TABLE public.occinspectiondispatch ADD COLUMN lastupdatedts  TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.occinspectiondispatch ADD COLUMN lastupdatedby_userid    INTEGER CONSTRAINT occinspectiondispatch_lastupdatdby_userid_fk 
    REFERENCES login (userid);

ALTER TABLE public.occinspectiondispatch DROP COLUMN municipality_municode;
ALTER TABLE public.occinspectiondispatch DROP COLUMN municipalityname;



CREATE TABLE IF NOT EXISTS public.loginphotodocs
(
    user_userid     INTEGER NOT NULL CONSTRAINT loginphotodoc_userid_fk REFERENCES public.login (userid),
    photodoc_id     INTEGER NOT NULL CONSTRAINT loginphotodoc_photodoc_fk REFERENCES public.photodoc (photodocid),
    CONSTRAINT loginphotodoc PRIMARY KEY (user_userid, photodoc_id)
);

ALTER TABLE public.login ADD COLUMN signature_photodocid INTEGER REFERENCES public.photodoc (photodocid);


ALTER TABLE public.occpermit ADD COLUMN staticsignature_photodocid INTEGER CONSTRAINT occpermit_sig_photodocid_fk REFERENCES public.photodoc (photodocid);
ALTER TABLE public.noticeofviolation ADD COLUMN fixedissuingofficersig_photodocid INTEGER CONSTRAINT nov_sig_photodocid_fk REFERENCES public.photodoc (photodocid);


ALTER TABLE public.noticeofviolation ADD COLUMN includestipcompdate BOOLEAN DEFAULT TRUE;
ALTER TABLE public.noticeofviolationtype ADD COLUMN includestipcompdate BOOLEAN DEFAULT TRUE;


ALTER TABLE public.noticeofviolation ALTER COLUMN includestipcompdate SET DEFAULT false;
ALTER TABLE public.noticeofviolationtype ALTER COLUMN includestipcompdate SET DEFAULT false;

--******************************* REMOTE CURSOR HERE  ******************************* 
--******************************* LOCAL CURSOR HERE  ******************************* 



INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (42, 'database/patches/dbpatch_beta42.sql', '10-13-2022', 'ecd', 'associated with release v0.875');