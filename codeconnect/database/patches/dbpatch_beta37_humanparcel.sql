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

INSERT INTO public.mailingzipcode(zipcodeid, zipcode, state_stateid)
	VALUES (88, 99999, 100 );


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


INSERT INTO public.mailingcity(
            cityid, name, namevariantsarr, zipcode_zipcodeid)
    VALUES (9, 'COGCity', NULL, 88);





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



-- Manually clear the -1/2 addresses and the R/W street names

CREATE OR REPLACE FUNCTION public.extractbuildingno(addr TEXT	)
  	RETURNS TEXT AS

$BODY$
	DECLARE
	 	extractedbldg TEXT;

	BEGIN
		IF addr ILIKE '%/%'
		THEN -- we've got a XX 1/2 street
			extractedbldg := substring(addr from '\d+\W\d/\d');
		ELSE 
			extractedbldg := substring(addr from '\d+');
		END IF;


		RETURN trim(both from extractedbldg);
	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;




CREATE OR REPLACE FUNCTION public.extractstreet(addr TEXT	)
  	RETURNS TEXT AS

$BODY$
	DECLARE
	 	extractedstreet TEXT;

	BEGIN
		IF addr ILIKE '%/%'
		THEN -- we've got a XX 1/2 street
			extractedstreet := substring(addr from '\d+\W\d/\d(.*)');
		ELSE 
			extractedstreet := substring(addr from '\d+\W(.*)');

		END IF;
		RETURN trim(both from extractedstreet);
		RETURN 1;
	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;




CREATE OR REPLACE FUNCTION public.migratepropertytoparcel(creationrobotuser INTEGER,
														  defaultsource INTEGER,
													  	  cityid INTEGER,
													  	  municodetarget INTEGER,
												  	  	  parceladdr_lorid INTEGER	)
  	RETURNS integer AS

$BODY$
	DECLARE
	 	pr RECORD;
	 	deacts TIMESTAMP WITH TIME ZONE;
	 	deacuser INTEGER;
	 	extractedbldg TEXT;
	 	extractedstreet TEXT;
	 	addr_range TEXT;
	 	addr_range_start TEXT;
	 	addr_range_end TEXT;
	 	addr_range_start_no INTEGER;
	 	addr_range_end_no INTEGER;
	 	addr_range_cursor INTEGER;
	 	addr_range_arr TEXT[];
	 	bldgno INTEGER;
	 	maid INTEGER; -- mailing address ID
	 	current_street_id INTEGER;
	 	buildingcount INTEGER;

	BEGIN
	buildingcount := 0;
		RAISE NOTICE 'starting property migration...';
		FOR pr IN SELECT 		propertyid, municipality_municode, parid, lotandblock, address, 
						       usegroup, constructiontype, countycode, notes, addr_city, addr_state, 
						       addr_zip, ownercode, propclass, lastupdated, lastupdatedby, locationdescription, 
						       bobsource_sourceid, unfitdatestart, unfitdatestop, unfitby_userid, 
						       abandoneddatestart, abandoneddatestop, abandonedby_userid, vacantdatestart, 
						       vacantdatestop, vacantby_userid, condition_intensityclassid, 
						       landbankprospect_intensityclassid, landbankheld, active, nonaddressable, 
						       usetype_typeid, creationts
						  FROM public.property WHERE municipality_municode=municodetarget
		 
		LOOP -- over properties in legacy table
			RAISE NOTICE 'LOOP ITERATION: %; propertyid %; parcel id  %', pr.address, pr.propertyid, pr.parid; 
			-- clear our iterables
			addr_range_arr := ARRAY[]::text[];
			addr_range := NULL;
			-- check for deactivation
			IF NOT pr.active 
			THEN 
				deacts := now();
				deacuser := creationrobotuser; -- the cogbot
			ELSE
				deacts := NULL;
				deacuser := NULL;
			END IF;

						
			-- TRY manual string concatenation method

			EXECUTE 'INSERT INTO public.parcel(
						            parcelkey, parcelidcnty, source_sourceid, createdts, createdby_userid, 
						            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
						            notes, muni_municode, lotandblock)  VALUES ('
					|| pr.propertyid
					|| ',' 
					|| quote_nullable(pr.parid)
					|| ',' 
					|| quote_nullable(pr.bobsource_sourceid)
					|| ',' 
					|| quote_nullable(pr.creationts)
					|| ',' 
					|| creationrobotuser
					|| ',' 
	                || quote_nullable(pr.lastupdated)
					|| ',' 
	                || quote_nullable(pr.lastupdatedby)
					|| ',' 
	                || quote_nullable(deacts)
					|| ',' 
	                || quote_nullable(deacuser)
					|| ',' 
	              	|| quote_nullable(pr.notes)
					|| ',' 
	              	|| pr.municipality_municode
					|| ',' 
	              	|| quote_nullable(pr.lotandblock) 	
					|| ');';
	           

		

			EXECUTE format('INSERT INTO public.parcelinfo(
					            parcelinfoid, parcel_parcelkey, usegroup, constructiontype, countycode, 
					            notes, ownercode, propclass, locationdescription, bobsource_sourceid, 
					            unfitdatestart, unfitdatestop, unfitby_userid, abandoneddatestart, 
					            abandoneddatestop, abandonedby_userid, vacantdatestart, vacantdatestop, 
					            vacantby_userid, condition_intensityclassid, landbankprospect_intensityclassid, 
					            landbankheld, nonaddressable, usetype_typeid, createdts, createdby_userid, 
					            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid)
					    VALUES (DEFAULT, %L, %L, %L, %L, 
					            NULL, %L, %L, NULL, %L, 
					            %L, %L, %L, %L, 
					            %L, %L, %L, %L, 
					            %L, %L, %L, 
					            %L, %L, %L, %L, %L, 
					            %L, %L, %L, %L);',
				            pr.propertyid, pr.usegroup, pr.constructiontype, pr.countycode,
				            pr.ownercode, pr.propclass, pr.bobsource_sourceid,
				            pr.unfitdatestart, pr.unfitdatestop, pr.unfitby_userid, pr.abandoneddatestart, 
			            	pr.abandoneddatestop, pr.abandonedby_userid, pr.vacantdatestart, pr.vacantdatestop, 
			            	pr.vacantby_userid, pr.condition_intensityclassid, pr.landbankprospect_intensityclassid,
			            	pr.landbankheld, pr.nonaddressable, pr.usetype_typeid, pr.creationts, creationrobotuser,
			            	pr.creationts, creationrobotuser, deacts, deacuser
						);

			-- parse address into street and bldgno 
			extractedstreet := extractstreet(pr.address);
			-- See if street is in the table already, if so, get its ID
			
			SELECT streetid FROM public.mailingstreet WHERE name ILIKE extractedstreet INTO current_street_id;
			
			RAISE NOTICE 'PropID: %; Has street % been found? Street ID: %', pr.address, extractedstreet, current_street_id;

			IF FOUND
			THEN -- we have an existing street
				NULL; -- we'll use the current_street_id for all the address writes

			ELSE -- we don't have a record of this street, so write it and grab its ID
				-- write street into mailingstreet
				EXECUTE format('INSERT INTO public.mailingstreet(
								            streetid, name, namevariantsarr, muni_municode, city_cityid, 
								            notes)
								    VALUES (DEFAULT, %L, NULL, %L, %L, 
								            NULL);',
								            		  extractedstreet, municodetarget, cityid);
				-- fetch fresh street id
				SELECT currval('mailingstreet_streetid_seq') INTO current_street_id;
			END IF;
			
		
			
			-- extract addresses with a - in there somewhere
			addr_range := substring(pr.address from '\d+\W?-\d+');
			RAISE NOTICE 'FOUND RANGE ADDRESS: %', addr_range;

			IF 
				addr_range IS NOT NULL
			THEN -- build range
				addr_range_start := substring(addr_range from '\d+');
				addr_range_end := substring(addr_range from '\d+\W?-(\d+)');
				addr_range_start_no := CAST (addr_range_start AS INTEGER);
				addr_range_end_no := CAST (addr_range_end AS INTEGER);
				addr_range_cursor := addr_range_start_no;
				WHILE  
					addr_range_cursor <= addr_range_end_no
				LOOP
					addr_range_arr := array_append(addr_range_arr, addr_range_cursor::text);
					RAISE NOTICE 'ADDR RANGE CURSOR VAL: %; ARRAY STATUS: % ', addr_range_cursor, addr_range_arr;
					-- step up by 2 building nos per even/odd numbering schema
					addr_range_cursor := addr_range_cursor + 2; 

				END LOOP;

			ELSE -- NORMAL building no
				extractedbldg := extractbuildingno(pr.address);
				RAISE NOTICE 'FOUND NORMAL BUILDING pr.address: %, extracted no: %', pr.address, extractedbldg;
				addr_range_arr := array_append(addr_range_arr, extractedbldg);
				
			END IF;

			RAISE NOTICE 'INSERTING MAILING ADDRESSES ARRAY: % ', addr_range_arr;

			FOREACH bldgno IN ARRAY addr_range_arr
			LOOP -- over each address in the array
				RAISE NOTICE 'INSERTING BLDG NO: %', bldgno;
				EXECUTE format('INSERT INTO public.mailingaddress(
							            addressid, bldgno, street_streetid, verifiedts, verifiedby_userid, 
							            verifiedsource_sourceid, source_sourceid, createdts, createdby_userid, 
							            lastupdatedts, lastupdatedby_userid, notes)
							    VALUES (DEFAULT, %L, %L, NULL, NULL, 
							            NULL, %L, %L, %L, 
							            %L, %L, ''Created during parcel migration JUL-21'');',
							                     bldgno, current_street_id,
					                     defaultsource, now(), creationrobotuser,
					                    now(), creationrobotuser);
				buildingcount := buildingcount + 1;

				-- get our fresh mailing address ID
				SELECT currval('mailingaddress_addressid_seq') INTO maid;

				-- Connect our current parcel with each
				EXECUTE format('INSERT INTO public.parcelmailingaddress(
								            mailingparcel_parcelid, mailingparcel_mailingid, source_sourceid, 
								            createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, 
								            deactivatedts, deactivatedby_userid, notes, linkid, linkedobjectrole_lorid)
								    VALUES (%L, %L, %L, 
								            now(), %L, now(), %L, 
								            NULL, NULL, ''Created during parcel migration JUL-21'', DEFAULT, %L);',

							            	pr.propertyid, maid, defaultsource,
							            	creationrobotuser, creationrobotuser, 
            								parceladdr_lorid);
				RAISE NOTICE 'LinkID complete: % ', maid;

			END LOOP; -- over each building Number extracted from the original property address 
		END LOOP; -- over properties in the legacy table
		RETURN buildingcount;
	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;



INSERT INTO public.contactphonetype(
            phonetypeid, title, createdts, deactivatedts)
    VALUES (DEFAULT, 'Mobile', now(), NULL);

INSERT INTO public.contactphonetype(
            phonetypeid, title, createdts, deactivatedts)
    VALUES (DEFAULT, 'Home', now(), NULL);

INSERT INTO public.contactphonetype(
            phonetypeid, title, createdts, deactivatedts)
    VALUES (DEFAULT, 'Work', now(), NULL);

INSERT INTO public.contactphonetype(
            phonetypeid, title, createdts, deactivatedts)
    VALUES (DEFAULT, 'Other', now(), NULL);



-- HUMAN MIGRATION STUFF

CREATE TABLE public.personhumanmigrationlogerrorcode
(
	code 	INTEGER PRIMARY KEY,
	descr 	TEXT NOT NULL,
	fatal 	BOOLEAN DEFAULT TRUE
);

INSERT INTO public.personhumanmigrationlogerrorcode(
		code, descr, fatal)
	VALUES (1, 'Cannot find matching address for linking', TRUE);

INSERT INTO public.personhumanmigrationlogerrorcode(
		code, descr, fatal)
	VALUES (2, 'Found duplicate person; skipping new human insert', FALSE);

INSERT INTO public.personhumanmigrationlogerrorcode(
		code, descr, fatal)
	VALUES (3, 'Malformed zipcode in legacy person record', FALSE);



CREATE SEQUENCE IF NOT EXISTS personhumanmigrationlog_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.personhumanmigrationlog
(
	logentryid 		INTEGER DEFAULT nextval('personhumanmigrationlog_seq') PRIMARY KEY,
	human_humanid  	INTEGER CONSTRAINT personhumanmigrationlog_humanid_fk REFERENCES human (humanid),
	person_personid 	INTEGER CONSTRAINT personhumanmigrationlog_personid_fk REFERENCES person (personid),
	error_code 		INTEGER CONSTRAINT personhumanmigrationlog_code_fk 	REFERENCES personhumanmigrationlogerrorcode (code),
	notes 			TEXT,
	ts 				TIMESTAMP WITH TIME ZONE NOT NULL
);


CREATE OR REPLACE FUNCTION public.migratepersontohuman(creationrobotuser INTEGER,
														  defaultsource INTEGER,
													  	  municodetarget INTEGER,
													  	  parcel_human_lorid INTEGER,
													  	  human_mailing_lorid INTEGER)
  	RETURNS integer AS

$BODY$
	DECLARE
	 	pr RECORD;
	 	prop_pers_rec RECORD;
	 	fullname TEXT;
	 	fresh_human_id INTEGER;
	 	deacts TIMESTAMP WITH TIME ZONE;
	 	deacuser INTEGER;
	 	human_rec_count INTEGER;
	 	parcel_rec RECORD;
	 	mailing_rec RECORD;
	 	citystatezip_rec RECORD;
	 	street_freshid INTEGER;
	 	address_freshid INTEGER;
	 	pers_newaddr_street TEXT;
	 	pers_newaddr_bldgno TEXT;
	 	human_dupid INTEGER;

	BEGIN
		RAISE NOTICE '**** BEGIN PERSON TO HUMAN MIGRATION ****';

		human_rec_count := 0;

		FOR pr IN SELECT personid, persontype, muni_municode, fname, lname, jobtitle, 
		       phonecell, phonehome, phonework, email, address_street, address_city, 
		       address_state, address_zip, notes, lastupdated, expirydate, isactive, 
		       isunder18, humanverifiedby, compositelname, sourceid, creator, 
		       businessentity, mailing_address_street, mailing_address_city, 
		       mailing_address_zip, mailing_address_state, useseparatemailingaddr, 
		       expirynotes, creationtimestamp, canexpire, userlink, mailing_address_thirdline, 
		       ghostof, ghostby, ghosttimestamp, cloneof, clonedby, clonetimestamp, 
		       referenceperson, rawname, cleanname, multientity
		  			FROM public.person WHERE muni_municode = municodetarget
		 
			LOOP -- over legacy person records
				RAISE NOTICE 'ITERATION: personid: %, lname: %', pr.personid, pr.lname;
				
				IF (pr.lname IS NULL OR pr.lname = '') AND (pr.fname IS NULL OR pr.fname = '')
					THEN
						RAISE NOTICE 'found null or empty last AND first name; skipping person';
						CONTINUE;
				END IF;

				--concat name
				IF pr.fname IS NOT NULL
					THEN
						fullname := pr.fname || ' ' || pr.lname; 
					ELSE 
						fullname := pr.lname;
				END IF;

				RAISE NOTICE 'FULL name for personid % is %', pr.personid, fullname;

				SELECT humanid INTO human_dupid FROM human WHERE name ILIKE fullname;

				IF NOT FOUND -- no duplicate based on name only
					THEN  -- go ahead and write our new human records
						RAISE NOTICE 'NO DUP FOUND FOR %; writing new human', fullname;
						-- check for deactivation
						IF NOT pr.isactive 
							THEN 
								deacts := now();
								deacuser := 99; -- the cogbot
							ELSE
								deacts := NULL;
								deacuser := NULL;
						END IF;

						-- our new humanid is our old person id
						fresh_human_id := pr.personid;

						EXECUTE format('INSERT INTO public.human(
			            humanid, name, dob, under18, jobtitle, businessentity, multihuman, 
			            source_sourceid, deceaseddate, deceasedby_userid, cloneof_humanid, 
			            createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, 
			            deactivatedts, deactivatedby_userid, notes)
						    VALUES (%L, %L, NULL, %L, %L, FALSE, %L, 
						            %L, NULL, NULL, NULL, 
						            %L, %L, %L, %L, 
						            %L, %L, %L);', 
					               fresh_human_id, fullname, pr.isunder18, pr.jobtitle, pr.multientity,
					               pr.sourceid, 
					               pr.creationtimestamp, pr.creator, pr.lastupdated, pr.creator, 
					               deacts, deacuser, pr.notes

			            ); 

			            human_rec_count := human_rec_count + 1;

						-- DEAL with "None" in phone columns, and empty strings

						IF pr.phonecell IS NOT NULL AND pr.phonecell <> '' AND pr.phonecell <> 'None'			
							THEN
								EXECUTE format('INSERT INTO public.contactphone(
											            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, 
											            disconnectts, disconnect_userid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, 100, 
											            NULL, NULL, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration JUL-2021'');', 
											            		fresh_human_id, pr.phonecell, 
											            					creationrobotuser,
								            					creationrobotuser);
							ELSE
								NULL; -- don't write any records
						END IF;


						IF pr.phonehome IS NOT NULL AND pr.phonehome <> '' AND pr.phonehome <> 'None'			
							THEN
								EXECUTE format('INSERT INTO public.contactphone(
											            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, 
											            disconnectts, disconnect_userid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, 101, 
											            NULL, NULL, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration JUL-2021'');', 
											            		fresh_human_id, pr.phonehome, 
											            					creationrobotuser,
								            					creationrobotuser);
							ELSE
								NULL; -- don't write any records
						END IF;


						IF pr.phonework IS NOT NULL AND pr.phonework <> '' AND pr.phonework <> 'None'			
							THEN
								EXECUTE format('INSERT INTO public.contactphone(
											            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, 
											            disconnectts, disconnect_userid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, 102, 
											            NULL, NULL, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration JUL-2021'');', 
											            		fresh_human_id, pr.phonework, 
											            					creationrobotuser,
								            					creationrobotuser);
							ELSE
								NULL; -- don't write any records
						END IF;
						
						IF pr.email IS NOT NULL AND pr.email <> '' AND pr.email <> 'None'			
						
							THEN
								EXECUTE format('INSERT INTO public.contactemail(
									            emailid, human_humanid, emailaddress, bouncets, createdts, createdby_userid, 
									            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
									            notes)
									    VALUES (DEFAULT, %L, %L, NULL, now(), %L, 
									            now(), %L, NULL, NULL, 
									            ''Created during person to human migration JUL-2021'');
												);', 
							            		fresh_human_id, pr.email, creationrobotuser,
				            					creationrobotuser);
							ELSE
								NULL; -- don't write any records
						END IF;


						-- use existing property-person links to connect our new human to existing parcels
						FOR prop_pers_rec IN SELECT property_propertyid, person_personid, creationts
											 	 FROM public.propertyperson
											 	 WHERE person_personid = pr.personid
					 		LOOP -- begin iterating over property person records
					 			
					 			-- Link this human to an existing parcel

					 			SELECT parcelkey INTO parcel_rec FROM parcel WHERE parcelkey = prop_pers_rec.property_propertyid;
					 			IF FOUND
						 			THEN -- link human and parcel
						 			-- NOTE that we used the old property PK as the new parcel PK so this linking should be straightforward

						 				EXECUTE format('INSERT INTO public.parcelhuman(
														            human_humanid, parcel_parcelkey, source_sourceid, role_roleid, 
														            createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, 
														            deactivatedts, deactivatedby_userid, notes)
														    VALUES (%L, %L, %L, %L, 
														            now(), %L, now(), %L, 
														            NULL, NULL, ''Created during person-human migration AUG 2021'');',
												            		fresh_human_id, parcel_rec.parcelkey, defaultsource, parcel_human_lorid,
										            				creationrobotuser, creationrobotuser
							            				);
						 				RAISE NOTICE 'Linked parcel with PK % to Human with PK %', parcel_rec.parcelkey, fresh_human_id;

						 			-- If no parcel exists, write record to migrationlog table
						 			ELSE
						 				NULL;
				 				END IF;
					 		END LOOP; -- end iteration over property-person records 

				 			-- Next, check if this person's address is one of those linked any of the parcels to which he/she is connected, if so, do nothing
				 			-- but if their address is not associated with parcel in our current muni, then make a new record in the mailingaddress
				 			-- family and connect this fresh_human to a fresh address

				 			SELECT  addressid INTO mailing_rec
								FROM public.mailingaddress 
								INNER JOIN public.parcelmailingaddress 
									ON (mailingparcel_mailingid = addressid)
								WHERE mailingparcel_parcelid = parcel_rec.parcelkey
									AND bldgno ILIKE extractbuildingno(pr.address_street); -- NOTE: We're playing fast and loose
									-- with address matching: if the building numbers are the same, we assume it's the same address
									-- and don't write a new mailingaddress record. EDGE cases of folks having a separate mailing
									-- whose building numbers is exactly the same as their linked parcel are not addressed.

							IF FOUND
								THEN -- we've already got a link between the person and that person's address
									RAISE NOTICE 'Fresh human has legacy address already linked to a parcel: moving on';
									NULL;
								ELSE 	-- we don't have a mailing address that matches the person record's mailing address, 
										-- So write a new address and link it to our fresh human

									SELECT id INTO citystatezip_rec
										FROM mailingcitystatezip WHERE zip_code = pr.address_zip AND list_type_id = 1;

									IF FOUND
										THEN --we've got a real zip code to attach to our new street
										RAISE NOTICE 'Fresh human has address with legimite ZIP %', citystatezip_rec.id;
											-- Write new street and address records
											EXECUTE format('
												INSERT INTO public.mailingstreet(
											            streetid, name, namevariantsarr, citystatezip_cszipid, notes, 
											            pobox)
											    VALUES (DEFAULT, %L, NULL, %L, ''Created during pers-human migration AUG-2021'', 
										    	        NULL);',
										    	        extractstreet(pr.address_street), citystatezip_rec.id
							    	        );

							    	        -- Now get our fresh street ID for writing our building Number
							    	        SELECT currval('mailingstreet_streetid_seq') INTO street_freshid;

							    	        -- with a street PK, we're ready to write to the mailingaddress base table
							    	        EXECUTE format('
							    	        	INSERT INTO public.mailingaddress(
											            addressid, bldgno, street_streetid, verifiedts, verifiedby_userid, 
											            verifiedsource_sourceid, source_sourceid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, NULL 
											            NULL, %L, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration'');',
											            extractbuildingno(pr.address_street), street_freshid,
											            defaultsource, creationrobotuser,
											            creationrobotuser
							    	        	);

							    	        SELECT currval('mailingaddress_addressid_seq') INTO address_freshid;

							    	        -- Now that we know the ID of the fresh mailing, we can link our fresh human via the old personid
							    	        -- to this new address we just got
							    	        EXECUTE format('INSERT INTO public.humanmailingaddress(
														            humanmailing_humanid, humanmailing_addressid, source_sourceid, 
														            role_roleid, createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, 
														            deactivatedts, deactivatedby_userid, notes, linkid)
														    VALUES (%L, %L, %L, 
														            %L, now(), %L, now(), %L, 
														            NULL, NULL, ''Created during person-human migration AUG 2021'', DEFAULT);',
														            pr.personid, address_freshid, defaultsource,
														            human_mailing_lorid, creationrobotuser, creationrobotuser);

										ELSE -- malformed ZIP on legacy person, meaning we cannot write a new address
											EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
															            logentryid, human_humanid, person_personid, error_code, notes, ts)
															    VALUES (DEFAULT, %L, %L, %L, ''ZIP NOT FOUND FROM PERSON ENTRY'', now());',
															    pr.personid, pr.personid, 3 
															);
									END IF; -- end check for legitimate zipcode found on old person address
							END IF; -- end check for existing address connection between person's parcel and ONE of that parcel's addresses
					ELSE  -- Record duplicate person found, don't write new humans, just write to log
						RAISE NOTICE 'Fresh human has address with MALFORMED ZIP: logging';
						EXECUTE format('INSERT INTO public.personhumanmigrationlog(
									            logentryid, human_humanid, person_personid, error_code, notes, ts)
									    VALUES (DEFAULT, NULL, %L, 2, ''DUP PERSON; SKIPPING NEW HUMAN'', now());',
								    		pr.personid
									    );
				END IF; -- over duplicate check of person records
				-- TODO: deal with ghosts in current person table which are what
				-- NOVs point to: extract their data and inject into new fixed fields!!

				RAISE NOTICE 'END PERSON RECORD';
		END LOOP; -- over legacy person table records

		RETURN human_rec_count;
	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;






--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (37, 'database/patches/dbpatch_beta37.sql',NULL, 'ecd', 'Human and Parcel migration');

