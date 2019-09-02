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
    akaaddress                 text NOT NULL

);
ALTER TABLE akaproperty ADD CONSTRAINT akapropertyid_pk PRIMARY KEY ( akapropertyid  ) ;

ALTER TABLE akaproperty ADD CONSTRAINT property_propertyid_fk FOREIGN KEY ( property_propertyid ) REFERENCES property ( propertyid ) ;



