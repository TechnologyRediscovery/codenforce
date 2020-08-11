
-- SEALED as of 19AUG19

ALTER TABLE occperiodtype ADD COLUMN permittitle TEXT;
ALTER TABLE occperiodtype ADD COLUMN permittitlesub TEXT;

ALTER TABLE choiceproposal ADD COLUMN chosen_choiceid INTEGER CONSTRAINT choiceproposal_chosenchoiceid_fk REFERENCES public.choice (choiceid);

ALTER TABLE occinspection ADD COLUMN active BOOLEAN DEFAULT TRUE;
ALTER TABLE occinspection ADD COLUMN creationts TIMESTAMP WITH TIME ZONE;

ALTER TABLE occinspectedspace RENAME COLUMN lastinspectedby_userid TO addedtochecklistby_userid;
ALTER TABLE occinspectedspace RENAME COLUMN lastinspectedts TO addedtochecklistts;

INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (21, 'database/patches/dbpatch_beta21.sql', '07-23-2019', 'ecd', 'occ beta final over');