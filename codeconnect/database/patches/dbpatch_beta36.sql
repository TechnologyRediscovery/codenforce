-- ****************************************************************************
-- PATCH 36
-- "CIATATION FACELIFT"

-- *************



ALTER TABLE public.citation ADD COLUMN createdts               TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.citation ADD COLUMN createdby_userid        INTEGER CONSTRAINT human_createdby_userid_fk REFERENCES login (userid);         
ALTER TABLE public.citation ADD COLUMN lastupdatedts           TIMESTAMP WITH TIME ZONE;         
ALTER TABLE public.citation ADD COLUMN lastupdatedby_userid    INTEGER CONSTRAINT human_lastupdatdby_userid_fk REFERENCES login (userid);        
ALTER TABLE public.citation ADD COLUMN deactivatedts           TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.citation ADD COLUMN deactivatedby_userid    INTEGER CONSTRAINT human_deactivatedby_userid_fk REFERENCES login (userid);         

ALTER TABLE public.citationperson ADD COLUMN createdts               TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.citationperson ADD COLUMN createdby_userid        INTEGER CONSTRAINT human_createdby_userid_fk REFERENCES login (userid);         
ALTER TABLE public.citationperson ADD COLUMN lastupdatedts           TIMESTAMP WITH TIME ZONE;         
ALTER TABLE public.citationperson ADD COLUMN lastupdatedby_userid    INTEGER CONSTRAINT human_lastupdatdby_userid_fk REFERENCES login (userid);        
ALTER TABLE public.citationperson ADD COLUMN deactivatedts           TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.citationperson ADD COLUMN deactivatedby_userid    INTEGER CONSTRAINT human_deactivatedby_userid_fk REFERENCES login (userid);         


ALTER TABLE public.citationviolation ADD COLUMN createdts               TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.citationviolation ADD COLUMN createdby_userid        INTEGER CONSTRAINT human_createdby_userid_fk REFERENCES login (userid);         
ALTER TABLE public.citationviolation ADD COLUMN lastupdatedts           TIMESTAMP WITH TIME ZONE;         
ALTER TABLE public.citationviolation ADD COLUMN lastupdatedby_userid    INTEGER CONSTRAINT human_lastupdatdby_userid_fk REFERENCES login (userid);        
ALTER TABLE public.citationviolation ADD COLUMN deactivatedts           TIMESTAMP WITH TIME ZONE;
ALTER TABLE public.citationviolation ADD COLUMN deactivatedby_userid    INTEGER CONSTRAINT human_deactivatedby_userid_fk REFERENCES login (userid);  









CREATE TABLE public.citationevent 
(

	citation_citationid 	INTEGER NOT NULL CONSTRAINT citationevent_citationid_fk REFERENCES citation (citationid),
	event_eventid 			INTEGER NOT NULL CONSTRAINT citationevent_eventid_fk REFERENCES event (eventid),
	createdts               TIMESTAMP WITH TIME ZONE,
	createdby_userid        INTEGER CONSTRAINT human_createdby_userid_fk REFERENCES login (userid),         
	lastupdatedts           TIMESTAMP WITH TIME ZONE,         
	lastupdatedby_userid    INTEGER CONSTRAINT human_lastupdatdby_userid_fk REFERENCES login (userid),        
	deactivatedts           TIMESTAMP WITH TIME ZONE,
	deactivatedby_userid    INTEGER CONSTRAINT human_deactivatedby_userid_fk REFERENCES login (userid),  
	notes					TEXT
);

CREATE TABLE public.citationcitationstatus
(


)



--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (36, 'database/patches/dbpatch_beta36.sql',NULL, 'ecd', 'Citatation facelift');


