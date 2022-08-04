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
--******************************* REMOTE CURSOR HERE  ******************************* 
--******************************* LOCAL CURSOR HERE  ******************************* 


INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (42, 'database/patches/dbpatch_beta42.sql', NULL, 'ecd', '');