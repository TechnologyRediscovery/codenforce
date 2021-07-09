-- ****************************************************************************
-- PATCH 37
-- "Human and parcel migration from person and property"

-- *************


CREATE SEQUENCE IF NOT EXISTS mailingstate_stateid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.mailingstate
(
	stateid 		INTEGER DEFAULT nextval('mailingstate_stateid_seq') PRIMARY KEY ,
	statemalecode 	INTEGER,
	postalabbrev	TEXT NOT NULL,
	name 			TEXT,
	namevariantsarr TEXT[]

);



CREATE SEQUENCE IF NOT EXISTS mailingzipcode_zipcodeid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.mailingzipcode
(
	zipcodeid 		INTEGER DEFAULT nextval('mailingzipcode_zipcodeid_seq') PRIMARY KEY ,
	zipcode  		TEXT NOT NULL,
	state_stateid 	INTEGER CONSTRAINT mailingzipcode_stateid_fk REFERENCES mailingstate (stateid)
);


CREATE SEQUENCE IF NOT EXISTS mailingcity_cityid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.mailingcity
(
	cityid 	 		INTEGER DEFAULT nextval('mailingcity_cityid_seq') PRIMARY KEY ,
	name 			TEXT NOT NULL,
	namevariantsarr TEXT[],
	zipcode_zipcodeid INTEGER CONSTRAINT mailingcity_zipcodeid_fk REFERENCES mailingzipcode (zipcodeid)

);



CREATE SEQUENCE IF NOT EXISTS mailingstreet_streetid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.mailingstreet
(
	streetid 		INTEGER DEFAULT nextval('mailingstreet_streetid_seq') PRIMARY KEY ,
	name 			TEXT NOT NULL,
	namevariantsarr TEXT[],
	muni_municode   INTEGER CONSTRAINT mailingstreet_muni_fk REFERENCES municipality (municode),
    city_cityid             INTEGER NOT NULL CONSTRAINT mailingstreet_cityid_fk REFERENCES mailingcity (cityid),
    notes			TEXT
	

);


DROP TABLE public.mailingaddress CASCADE;


CREATE TABLE mailingaddress
    (
        addressid               INTEGER PRIMARY KEY DEFAULT nextval('mailingaddress_addressid_seq'),
        bldgno	                TEXT NOT NULL,
        unitno 					TEXT,
        street_streetid         INTEGER NOT NULL CONSTRAINT mailingaddress_streetid_fk REFERENCES mailingstreet (streetid),
        verifiedts              TIMESTAMP WITH TIME ZONE,
        verifiedby_userid       INTEGER CONSTRAINT mailingaddress_createdby_userid_fk REFERENCES login (userid),     
        verifiedsource_sourceid INTEGER CONSTRAINT mailingaddress_verifiedsourceid_fk REFERENCES public.bobsource (sourceid),
        source_sourceid         INTEGER CONSTRAINT mailingaddress_sourceid_fk REFERENCES public.bobsource (sourceid),
        createdts               TIMESTAMP WITH TIME ZONE,
        createdby_userid        INTEGER, 
        lastupdatedts           TIMESTAMP WITH TIME ZONE,
        lastupdatedby_userid    INTEGER ,
        deactivatedts           TIMESTAMP WITH TIME ZONE,
        deactivatedby_userid    INTEGER ,
        notes                   TEXT

    );


INSERT INTO public.mailingstate(
            stateid, statemalecode, postalabbrev, name, namevariantsarr)
    VALUES (DEFAULT, NULL, 'PA','Pennsylviania', NULL);






CREATE OR REPLACE FUNCTION public.migratepropertytoparcel(creationrobotuser INTEGER)
  	RETURNS integer AS

$BODY$
	DECLARE
	 	pr RECORD;
	 	deacts TIMESTAMP WITH TIME ZONE;
	 	deacuser INTEGER;
	BEGIN
		RAISE NOTICE 'starting property migration...';
		FOR pr IN SELECT 		propertyid, municipality_municode, parid, lotandblock, address, 
						       usegroup, constructiontype, countycode, notes, addr_city, addr_state, 
						       addr_zip, ownercode, propclass, lastupdated, lastupdatedby, locationdescription, 
						       bobsource_sourceid, unfitdatestart, unfitdatestop, unfitby_userid, 
						       abandoneddatestart, abandoneddatestop, abandonedby_userid, vacantdatestart, 
						       vacantdatestop, vacantby_userid, condition_intensityclassid, 
						       landbankprospect_intensityclassid, landbankheld, active, nonaddressable, 
						       usetype_typeid, creationts
						  FROM public.property

		 
		LOOP
			
			-- check for deactivation
			IF NOT pr.active 
			THEN 
				deacts := now();
				deacuser := creationrobotuser; -- the cogbot
			ELSE
				deacts := NULL;
				deacuser := NULL;
			END IF;

			EXECUTE format('INSERT INTO public.parcel(
						            parcelkey, parcelidcnty, source_sourceid, createdts, createdby_userid, 
						            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
						            notes, muni_municode, lotandblock)
						    VALUES (%I, %I, %I, %I, %I, 
						            %I, %I, %I, %I, 
						            %I, %I, %I);
							);', 
		              pr.propertyid, pr.parid, pr.bobsource_sourceid, pr.creationts, creationrobotuser,
		              pr.lastupdated, pr.lastupdatedby, deacts, deacuser,
	              	  pr.notes, pr.municipality_municode, pr.lotandblock 	

	            ); 


			EXECUTE format('INSERT INTO public.parcelinfo(
					            parcelinfoid, parcel_parcelkey, usegroup, constructiontype, countycode, 
					            notes, ownercode, propclass, locationdescription, bobsource_sourceid, 
					            unfitdatestart, unfitdatestop, unfitby_userid, abandoneddatestart, 
					            abandoneddatestop, abandonedby_userid, vacantdatestart, vacantdatestop, 
					            vacantby_userid, condition_intensityclassid, landbankprospect_intensityclassid, 
					            landbankheld, nonaddressable, usetype_typeid, createdts, createdby_userid, 
					            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid)
					    VALUES (DEFAULT, %I, %I, %I, %I, 
					            NULL, %I, %I, NULL, %I, 
					            %I, %I, %I, %I, 
					            %I, %I, %I, %I, 
					            %I, %I, %I, 
					            %I, %I, %I, %I, %I, 
					            %I, %I, %I, %I);',
				            pr.propertyid, pr.usegroup, pr.constructiontype, pr.countycode
				            pr.ownercode, pr.propclass, pr.bobsource_sourceid
				            pr.unfitdatestart, pr.unfitdatestop, pr.unfitby_userid, pr.abandoneddatestart, 
			            	pr.abandoneddatestop, pr.abandonedby_userid, pr.vacantdatestart, pr.vacantdatestop, 
			            	pr.vacantby_userid, pr.condition_intensityclassid, pr.landbankprospect_intensityclassid
			            	pr.landbankheld, pr.nonaddressable, pr.usetype_typeid, pr.creationts, creationrobotuser
			            	pr.creationts, creationrobotuser, deacts, deacuser
						);


			-- DEAL with "None" in phone columns, and empty strings

			EXECUTE format('INSERT INTO public.mailingaddress(
            addressid, addressnum, street, unitno, city, state, zipcode, 
            pobox, verifiedts, source_sourceid, createdts, createdby_userid, 
            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
            notes)
			    VALUES (%I, %I, %I, %I, %I, %I, %I, 
			            %I, %I, %I, %I, %I, 
			            %I, %I, %I, %I, 
			            %I);
			'


			);



			-- TODO: deal with ghosts in current person table which are what
			-- NOVs point to: extract their data and inject into new fixed fields!!
		END LOOP;
		RETURN 1;
	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.migratepersontohuman()
  OWNER TO sylvia;



CREATE OR REPLACE FUNCTION public.migratepersontohuman()
  	RETURNS integer AS

$BODY$
	DECLARE
	 	pr RECORD;
	 	fullname TEXT;
	 	deacts TIMESTAMP WITH TIME ZONE;
	 	deacuser INTEGER;
	BEGIN
		RAISE NOTICE 'starting migration...';
		FOR pr IN SELECT personid, persontype, muni_municode, fname, lname, jobtitle, 
		       phonecell, phonehome, phonework, email, address_street, address_city, 
		       address_state, address_zip, notes, lastupdated, expirydate, isactive, 
		       isunder18, humanverifiedby, compositelname, sourceid, creator, 
		       businessentity, mailing_address_street, mailing_address_city, 
		       mailing_address_zip, mailing_address_state, useseparatemailingaddr, 
		       expirynotes, creationtimestamp, canexpire, userlink, mailing_address_thirdline, 
		       ghostof, ghostby, ghosttimestamp, cloneof, clonedby, clonetimestamp, 
		       referenceperson, rawname, cleanname, multientity
		  			FROM public.person
		 
		LOOP
			--concat name
			IF pr.fname IS NOT NULL
			THEN
				fullname := pr.fname || ' ' || pr.lname; 
			ELSE 
				fullname = pr.lname;
			END IF;

			-- check for deactivation
			IF NOT pr.isactive 
			THEN 
				deacts := now();
				deacuser := 99; -- the cogbot
			ELSE
				deacts := NULL;
				deacuser := NULL;
			END IF;

			EXECUTE format('INSERT INTO public.human(
            humanid, name, dob, under18, jobtitle, businessentity, multihuman, 
            source_sourceid, deceaseddate, deceasedby_userid, cloneof_humanid, 
            createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, 
            deactivatedts, deactivatedby_userid, notes)
			    VALUES (%I, %I, NULL, %I, %I, %I, %I, 
			            %I, NULL, NULL, NULL, 
			            %I, %I, %I, %I, 
			            %I, %I, %I);', 
		               pr.personid, fullname, pr.isunder18, pr.jobtitle, pr.multientity,
		               pr.sourceid, 
		               pr.creationtimestamp, pr.creator, pr.lastupdated, pr.creator, 
		               deacts, deacuser, pr.notes

	            ); 

			-- DEAL with "None" in phone columns, and empty strings

			EXECUTE format('INSERT INTO public.mailingaddress(
            addressid, addressnum, street, unitno, city, state, zipcode, 
            pobox, verifiedts, source_sourceid, createdts, createdby_userid, 
            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
            notes)
			    VALUES (%I, %I, %I, %I, %I, %I, %I, 
			            %I, %I, %I, %I, %I, 
			            %I, %I, %I, %I, 
			            %I);
			'


			);



			-- TODO: deal with ghosts in current person table which are what
			-- NOVs point to: extract their data and inject into new fixed fields!!
		END LOOP;
		RETURN 1;
	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.migratepersontohuman()
  OWNER TO sylvia;





--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (36, 'database/patches/dbpatch_beta37.sql',NULL, 'ecd', 'Human and Parcel migration');

