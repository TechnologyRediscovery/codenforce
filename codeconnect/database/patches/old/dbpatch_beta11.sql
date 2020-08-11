

ALTER TABLE person ADD COLUMN ghostof INTEGER CONSTRAINT person_ghost_person_fk REFERENCES person (personid);
ALTER TABLE person ADD COLUMN ghostby INTEGER CONSTRAINT person_ghost_fk REFERENCES login (userid);
ALTER TABLE person ADD COLUMN ghosttimestamp TIMESTAMP WITH TIME ZONE;

ALTER TABLE person ADD COLUMN cloneof INTEGER CONSTRAINT person_clone_person_fk REFERENCES person (personid);
ALTER TABLE person ADD COLUMN clonedby INTEGER CONSTRAINT person_clone_fk REFERENCES login (userid);
ALTER TABLE person ADD COLUMN clonetimestamp TIMESTAMP WITH TIME ZONE;

ALTER TABLE person ADD COLUMN referenceperson BOOLEAN;


CREATE TABLE citationperson 
(
    citation_citationid             INTEGER NOT NULL ,
    person_personid             	INTEGER NOT NULL
) ;

ALTER TABLE citationperson ADD CONSTRAINT citationperson_pk PRIMARY KEY (citation_citationid, person_personID) ;
ALTER TABLE citationperson ADD CONSTRAINT citationperson_citation_eventID_fk FOREIGN KEY ( citation_citationid ) REFERENCES citation ( citationid ) ;
ALTER TABLE citationperson ADD CONSTRAINT citationperson_person_personID_fk FOREIGN KEY ( person_personID ) REFERENCES person ( personID ) ;



CREATE SEQUENCE IF NOT EXISTS person_clone_merge_seq
	    START WITH 1000
	    INCREMENT BY 1
	    MINVALUE 1000
	    NO MAXVALUE
	    CACHE 1;

CREATE TABLE personmergehistory
(
	mergeid						INTEGER DEFAULT nextval('person_clone_merge_seq') 
									CONSTRAINT personmerge PRIMARY KEY,
	mergetarget_personid		INTEGER CONSTRAINT personmerge_target_fk REFERENCES person (personid),
	mergesource					INTEGER CONSTRAINT personmerge_source_fk REFERENCES person (personid),
	mergby_userid				INTEGER CONSTRAINT personmerge_user_fk REFERENCES login (userid),
	mergetimestamp				TIMESTAMP WITH TIME ZONE,
	mergenotes					TEXT
);


INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (11, 'database/patches/dbpatch_beta11.sql', '05-19-2019', 'ecd', 'person revision; ghosts and clones');

