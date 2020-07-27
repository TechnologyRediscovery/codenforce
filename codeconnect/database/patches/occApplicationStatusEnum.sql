CREATE TYPE occapplicationstatus AS ENUM ('Waiting','NewOccPeriod','OldOccPeriod','Rejected','Invalid');

ALTER TABLE occpermitapplication
ADD status occapplicationstatus;

-- We have to make sure all existing applications have a status

UPDATE occpermitapplication
SET status = 'Waiting'::occapplicationstatus
WHERE applicationid >= 0;

-- A bonus! The externalnotes field!

ALTER TABLE occpermitapplication
ADD externalnotes text;