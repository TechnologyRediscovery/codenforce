ALTER TABLE public.occperiod 
ADD COLUMN lastupdatedts TIMESTAMP WITH TIME ZONE,
ADD COLUMN lastupdatedby_userid INTEGER CONSTRAINT human_lastupdatdby_userid_fk REFERENCES login (userid);