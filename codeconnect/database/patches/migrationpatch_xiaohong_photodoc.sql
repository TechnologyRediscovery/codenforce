-- Updating database table for migration data from wilkins

CREATE SEQUENCE IF NOT EXISTS photodoctype_typeid_seq
    START WITH 3
    INCREMENT BY 1
    MINVALUE 3
    NO MAXVALUE
    CACHE 1;

ALTER TABLE public.photodoctype ADD column donotstoretype BOOLEAN;

ALTER TABLE public.photodoctype ALTER COLUMN typeid set default nextval('photodoctype_typeid_seq');

CREATE SEQUENCE IF NOT EXISTS akapropertyid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.akaproperty
(
    akapropertyid              INTEGER DEFAULT nextval('akapropertyid_seq') NOT NULL,
    property_propertyid        INTEGER NOT NULL,
    akaaddress                 text NOT NULL,
    CONSTRAINT akapropertyid_pk PRIMARY KEY (akapropertyid)
);

ALTER TABLE akaproperty ADD CONSTRAINT property_propertyid_fk FOREIGN KEY (property_propertyid) REFERENCES property (propertyid) ;