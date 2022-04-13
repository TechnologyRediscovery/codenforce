ALTER TABLE mailingcitystatezip
ADD source_sourceid INTEGER
    CONSTRAINT mailingcitystatezip_sourceid_fk REFERENCES bobsource,
lastupdatedts        TIMESTAMP WITH TIME ZONE
lastupdatedby_userid INTEGER
    CONSTRAINT mailingcitystatezip_lastupdatedby_userid_fk REFERENCES login,
deactivatedts        TIMESTAMP WITH TIME ZONE,
deactivatedby_userid INTEGER
    CONSTRAINT mailingcitystatezip_deactivatedby_userid REFERENCES login;

