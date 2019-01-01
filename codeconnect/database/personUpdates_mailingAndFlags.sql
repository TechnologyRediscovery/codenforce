BEGIN;
CREATE SEQUENCE IF NOT EXISTS personSource_sourceID_seq
    START WITH 10
    INCREMENT BY 1
    MINVALUE 10
    NO MAXVALUE
    CACHE 1;

CREATE TABLE personsource
(
    sourceid                        INTEGER DEFAULT nextval('personSource_sourceID_seq') CONSTRAINT personSource_sourceIDID_pk PRIMARY KEY,
    title                           text NOT NULL
) ;

INSERT INTO public.personsource(
            sourceid, title)
    VALUES (9, 'unknown');

INSERT INTO public.personsource(
            sourceid, title)
    VALUES (DEFAULT, 'Staff');

INSERT INTO public.personsource(
            sourceid, title)
    VALUES (DEFAULT, 'Public user');

INSERT INTO public.personsource(
            sourceid, title)
    VALUES (DEFAULT, 'County');




ALTER TABLE person ADD COLUMN sourceid INTEGER CONSTRAINT person_source_sourceid_fk REFERENCES personsource (sourceid);
ALTER TABLE person ADD COLUMN creator  INTEGER CONSTRAINT person_creator_fk REFERENCES login (userid);
ALTER TABLE person ADD COLUMN businessentity BOOLEAN DEFAULT FALSE;
ALTER TABLE person ADD COLUMN addressofresidence BOOLEAN DEFAULT TRUE;
ALTER TABLE person ADD COLUMN mailing_address_street TEXT;
ALTER TABLE person ADD COLUMN mailing_address_city TEXT; 
ALTER TABLE person ADD COLUMN mailing_address_zip TEXT;
ALTER TABLE person ADD COLUMN mailing_address_state TEXT;
ALTER TABLE person ADD COLUMN mailingsameasresidence BOOLEAN default TRUE;
ALTER TABLE person ADD COLUMN expirynotes TEXT;






COMMIT;
