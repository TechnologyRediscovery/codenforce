
-- have not wired this new column up yet!
ALTER TABLE ceactionrequest ADD COLUMN usersubmitter_userid INTEGER;
ALTER TABLE ceactionrequest ADD CONSTRAINT ceactionreq_usersub_fk FOREIGN KEY (usersubmitter_userid) REFERENCES login (userid);

-- add stipulated compliance that freeze in time when added to a notice of violation




INSERT INTO public.dbpatch(
            patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (13, 'database/patches/dbpatch_beta13.sql', '', 'ecd', 'final clean ups');

