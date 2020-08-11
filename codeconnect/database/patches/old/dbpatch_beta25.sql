-- ****************************************************************************
-- First patch post re-braining
-- 
-- closed as of January 15

-- RELEASE NOTE: You'll probably need to add a BObSource with an ID of 9 if you got a pre-built
-- brain from Ellen. 
-- 
-- ****************************************************************************



ALTER TABLE public.actionrqstissuetype RENAME TO ceactionrequestissuetype;

ALTER TABLE public.ceactionrequestissuetype ADD COLUMN intensity_classid INTEGER 
	CONSTRAINT ceactionrequestissuetype_intensity_fk REFERENCES public.intensityclass (classid);

ALTER TABLE public.ceactionrequestissuetype ADD COLUMN active BOOLEAN DEFAULT TRUE;

ALTER TABLE public.person ADD CONSTRAINT person_sourceid_fk FOREIGN KEY (sourceid) REFERENCES bobsource (sourceid);

ALTER TABLE personmergehistory RENAME COLUMN mergesource TO mergesource_personid;

ALTER TABLE municipality ADD COLUMN  defaultoccperiod INTEGER CONSTRAINT muni_defoccperiod_fk REFERENCES occperiod (periodid);

ALTER TABLE choiceproposal RENAME COLUMN generatingevent_cecaseeventid TO generatingevent_eventid;

ALTER TABLE choiceproposal RENAME COLUMN responseevent_cecaseeventid TO responseevent_eventid;




CREATE TABLE public.occperiodeventrule
(
  occperiod_periodid integer NOT NULL,
  eventrule_ruleid integer NOT NULL,
  attachedts timestamp with time zone,
  attachedby_userid integer,
  lastevaluatedts timestamp with time zone,
  passedrulets timestamp with time zone,
  passedrule_eventid integer,
  active boolean DEFAULT true,
  CONSTRAINT occperiodeventrule_comp_pk PRIMARY KEY (occperiod_periodid, eventrule_ruleid),
  CONSTRAINT occperiodeventrule_op_fk FOREIGN KEY (occperiod_periodid)
      REFERENCES public.occperiod (periodid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occperiodeventrule_ruleid_fk FOREIGN KEY (eventrule_ruleid)
      REFERENCES public.eventrule (ruleid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT occperiodeventrule2_attachedby_userid_fk FOREIGN KEY (attachedby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);


--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (25, 'database/patches/dbpatch_beta25.sql', NULL, 'ecd', 'various changes');