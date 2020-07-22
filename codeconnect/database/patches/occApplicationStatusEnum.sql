CREATE TYPE occapplicationstatus AS ENUM ('Waiting','NewOccPeriod','OldOccPeriod','Rejected','Invalid');

ALTER TABLE occpermitapplication
ADD status occapplicationstatus;