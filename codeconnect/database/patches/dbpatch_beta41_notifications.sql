-- DB Patch 41:Notifications






-- LOCAL CURSOR

-- REMOTE CURSOR


-- event cat changes
-- snooze rank floor
-- process rank floor

-- need a table for event review that has a user, timestamp, and link to event, and notes, perhaps re-review? event trigger?
-- An event emitter is an object that carries an event category and an eventholder that can receive a given event
-- we could trigger event reviews, but creating an event that get batch assigned that requires a certain floor for processing, 
-- and processing requires creating an event of a certin category, and that category could be a special one for further review.
-- include a flag for allow category substitution within type on the event category












INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (41, 'database/patches/dbpatch_beta41_notifications.sql', NULL, 'ecd', '');