
ALTER TABLE occperiodtype ADD COLUMN permittitle TEXT;
ALTER TABLE occperiodtype ADD COLUMN permittitlesub TEXT;

ALTER TABLE choiceproposal ADD COLUMN chosen_choiceid INTEGER CONSTRAINT choiceproposal_chosenchoiceid_fk REFERENCES public.choice (choiceid);

INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (21, 'database/patches/dbpatch_beta20.sql', '07-23-2019', 'ecd', 'occ beta final over');