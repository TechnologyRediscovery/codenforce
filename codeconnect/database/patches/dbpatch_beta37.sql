-- ****************************************************************************
-- PATCH 37
-- "Human and parcel migration from person and property"

-- *************

CREATE SEQUENCE IF NOT EXISTS mailingstreet_streetid_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

-- the local version of mailingstreet on SB22 is this, since the one in the patch wasn't keyed to citystatezip
CREATE TABLE public.mailingstreet
(
  streetid integer NOT NULL DEFAULT nextval('mailingstreet_streetid_seq'::regclass),
  name text NOT NULL,
  namevariantsarr text[],
  citystatezip_cszipid integer NOT NULL,
  notes text,
  pobox boolean DEFAULT false,
  createdts timestamp with time zone DEFAULT now(),
  createdby_userid integer,
  lastupdatedts timestamp with time zone,
  lastupdatedby_userid integer,
  deactivatedts timestamp with time zone,
  deactivatedby_userid integer,
  CONSTRAINT mailingstreet_pkey PRIMARY KEY (streetid),
  CONSTRAINT docketno_createdby_userid_fk FOREIGN KEY (createdby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT docketno_deactivatedby_userid_fk FOREIGN KEY (deactivatedby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT docketno_lastupdatedby_userid_fk FOREIGN KEY (lastupdatedby_userid)
      REFERENCES public.login (userid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT mailingstreet_citystatezip_fk FOREIGN KEY (citystatezip_cszipid)
      REFERENCES public.mailingcitystatezip (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.mailingstreet
  OWNER TO sylvia;



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




-- Manually clear the -1/2 addresses and the R/W street names

CREATE OR REPLACE FUNCTION public.extractbuildingno(addr TEXT	)
  	RETURNS TEXT AS

$BODY$
	DECLARE
	 	extractedbldg TEXT;

	BEGIN
		IF addr ILIKE '%PO BOX%'
			THEN 
				extractedbldg := substring(addr from '[Pp][Oo]\s[Bb][Oo][Xx]\s\d+');
			ELSE
				IF addr ILIKE '%/%'
				THEN -- we've got a XX 1/2 street
					extractedbldg := substring(addr from '\d+\W\d/\d');
				ELSE 
					extractedbldg := substring(addr from '\d+');
				END IF; -- fractions
		END IF; -- PO boxes

		RETURN unifyspacesandtrim(extractedbldg);
	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;




CREATE OR REPLACE FUNCTION public.extractstreet(addr TEXT	)
  	RETURNS TEXT AS

$BODY$
	DECLARE
	 	extractedstreet TEXT;
	 	re_matches RECORD;
	 	validationstring TEXT;

	BEGIN
		IF addr ILIKE '%PO BOX%'
			THEN
				RETURN 'PO BOX';
			ELSE
				IF addr ILIKE '%/%'
					THEN -- we've got a XX 1/2 street
						extractedstreet := substring(addr from '\d+\W\d/\d\W?(.*)');
					ELSE 
						IF addr ILIKE'%-%'
							THEN --we've got a range of some sort
								SELECT regexp_matches(addr, '\w-\w') INTO re_matches;
								IF NOT FOUND
									THEN -- we've got an address range and not a unit range
										extractedstreet := substring(addr from '\d+\W?-\d+\W(.*)');
									ELSE -- we've likely got a unit range, so skip
										RAISE NOTICE 'found unit range in address; skipping: %', addr;
										RETURN NULL;
								END IF;
							ELSE -- no address range
								extractedstreet := substring(addr from '\d+\W(.*)');
						END IF; -- range check
				END IF; -- fraction check
		END IF; -- box box
		validationstring := unifyspacesandtrim(extractedstreet);
		-- check work for null, and empty strings and single spaces
		IF validationstring IS NOT NULL AND validationstring <> '' AND validationstring <> ' '
			THEN
				RETURN validationstring;
			ELSE
				RETURN NULL;
		END IF; --validation
	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;



CREATE TABLE public.parcelmigrationlogerrorcode
(
	code 	INTEGER PRIMARY KEY,
	descr 	TEXT NOT NULL,
	fatal 	BOOLEAN DEFAULT TRUE
);

INSERT INTO public.parcelmigrationlogerrorcode(
		code, descr, fatal)
	VALUES (1, 'Invalid address', TRUE);

CREATE SEQUENCE IF NOT EXISTS parcelmigrationlog_seq
    START WITH 100
    INCREMENT BY 1
    MINVALUE 100
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.parcelmigrationlog
(
	logentryid 		INTEGER DEFAULT nextval('parcelmigrationlog_seq') PRIMARY KEY,
	property_id 		INTEGER CONSTRAINT parcelmigrationlog_propid_fk REFERENCES property (propertyid),
	parcel_id 			INTEGER CONSTRAINT parcelmigrationlog_parcelid_fk REFERENCES parcel (parcelkey),
	error_code 		INTEGER CONSTRAINT parcelmigration_errorcode	REFERENCES parcelmigrationlogerrorcode (code),
	notes 			TEXT,
	ts 				TIMESTAMP WITH TIME ZONE NOT NULL
);


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
	 	bldgno TEXT;
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
					|| quote_nullable(unifyspacesandtrim(pr.parid))
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
	              	|| quote_nullable(unifyspacesandtrim(pr.notes))
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
			
			SELECT streetid INTO current_street_id
				FROM public.mailingstreet 
				WHERE name ILIKE '%'||extractedstreet||'%'
					AND citystatezip_cszipid = cityid; -- only look for existing street matches within the zip of this muni
			
			RAISE NOTICE 'PropID: %; Has street % been found? Street ID: %', pr.address, extractedstreet, current_street_id;
			IF extractedstreet IS NOT NULL
				THEN
					IF FOUND
					THEN -- we have an existing street

						NULL; -- we'll use the current_street_id for all the address writes

					ELSE -- we don't have a record of this street, so write it and grab its ID
						-- write street into mailingstreet
						EXECUTE format('INSERT INTO public.mailingstreet(
										            	streetid, name, namevariantsarr, citystatezip_cszipid, notes, 
		            									pobox)
										    VALUES (DEFAULT, %L, NULL, %L, %L, 
										            NULL);',
										            		  unifyspacesandtrim(extractedstreet), cityid, 'MIGRATION AUG-2021; Raw addr: ' || pr.address );
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
									            %L, %L, %L);',
									                      unifyspacesandtrim(bldgno), current_street_id,
							                     defaultsource, now(), creationrobotuser,
							                    now(), creationrobotuser, 'MIGRATION AUG-2021; Raw Addr: ' || pr.address);
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
				ELSE -- we don't have a well formed street
					EXECUTE format('INSERT INTO public.parcelmigrationlog(
								            logentryid, property_id, parcel_id, error_code, notes, ts)
								    VALUES (DEFAULT, %L, NULL, 1, ''IMPROPERLY FORMED ADDRESS'', now());',
								    pr.propertyid);
				END IF; -- check of malformed property
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

INSERT INTO public.personhumanmigrationlogerrorcode(
		code, descr, fatal)
	VALUES (4, 'Person without first and last name', TRUE);

INSERT INTO public.personhumanmigrationlogerrorcode(
		code, descr, fatal)
	VALUES (5, 'Human - parcel link failure', TRUE);

INSERT INTO public.personhumanmigrationlogerrorcode(
		code, descr, fatal)
	VALUES (6, 'Non-parcel address error: cannot parse street', TRUE);

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


-- role_roleid didn't exist on live system
--ALTER TABLE public.humanmailingaddress DROP COLUMN role_roleid;
ALTER TABLE public.humanmailingaddress ADD COLUMN linkedobjectrole_lorid INTEGER 
	CONSTRAINT humanmailingaddress_lorid_fk REFERENCES linkedobjectrole (lorid);



CREATE OR REPLACE FUNCTION public.migratepersontohuman(		creationrobotuser INTEGER,
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
	 	current_human_id INTEGER;
	 	deacts TIMESTAMP WITH TIME ZONE;
	 	deacuser INTEGER;
	 	human_rec_count INTEGER;
	 	parcel_rec RECORD;
	 	mailing_rec RECORD;
	 	zip_parsed TEXT;
	 	citystatezip_rec RECORD;
	 	street_freshstreetname TEXT;
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

						RAISE NOTICE 'found null or empty last AND first name; skipping person and LOGGING;';
						EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
																            logentryid, human_humanid, person_personid, error_code, notes, ts)
																    VALUES (DEFAULT, NULL, %L, %L, ''EMTPY NAME'', now());',
																    pr.personid, 4 
																);
						CONTINUE;
				END IF;

				--concat name
				IF pr.fname IS NOT NULL
					THEN
						fullname := unifyspacesandtrim(pr.fname) || ' ' || unifyspacesandtrim(pr.lname); 
					ELSE 
						fullname := unifyspacesandtrim(pr.lname);
				END IF;

				RAISE NOTICE 'FULL name for personid % is %', pr.personid, fullname;

				-- Check for duplicate person in human table
				SELECT humanid INTO human_dupid FROM human WHERE unifyspacesandtrim(human.name) = fullname;

				RAISE NOTICE 'DUP CHECK: HUMAN_DUPID: % ', human_dupid;

				IF human_dupid IS NULL OR human_dupid = 0 -- no duplicate based on name only
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
						            now(), %L, now(), %L, 
						            %L, %L, %L);', 
					               fresh_human_id, fullname, pr.isunder18, unifyspacesandtrim(pr.jobtitle), pr.multientity,
					               pr.sourceid, 
					               pr.creator, pr.creator, 
					               deacts, deacuser, unifyspacesandtrim(pr.notes)

			            ); 

			            RAISE NOTICE 'Fresh human record ID: %', fresh_human_id;
			            -- Now move our cursor to our new fresh human
			            current_human_id := fresh_human_id; -- NOTE if we have a dupe, current_human_id is not updated

			            human_rec_count := human_rec_count + 1;

						-- DEAL with "None" in phone columns, and empty strings

						IF pr.phonecell IS NOT NULL AND pr.phonecell <> '' AND pr.phonecell <> 'None'			
							THEN
								--RAISE NOTICE 'PHONE CELL FOUND: %', pr.phonecell;
								EXECUTE format('INSERT INTO public.contactphone(
											            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, 
											            disconnectts, disconnect_userid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, 100, 
											            NULL, NULL, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration JUL-2021'');', 
											            		fresh_human_id, unifyspacesandtrim(pr.phonecell), 
											            					creationrobotuser,
								            					creationrobotuser);
							ELSE
								--RAISE NOTICE 'NO VALID PHONE CELL FOUND; NOT WRITING RECORD';
								NULL; -- don't write any phone records
						END IF;


						IF pr.phonehome IS NOT NULL AND pr.phonehome <> '' AND pr.phonehome <> 'None'			
							THEN
								--RAISE NOTICE 'PHONE HOME FOUND: %', pr.phonehome;
								EXECUTE format('INSERT INTO public.contactphone(
											            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, 
											            disconnectts, disconnect_userid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, 101, 
											            NULL, NULL, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration JUL-2021'');', 
											            		fresh_human_id, unifyspacesandtrim(pr.phonehome), 
											            					creationrobotuser,
								            					creationrobotuser);
							ELSE -- duplicate
								--RAISE NOTICE 'NO VALID PHONE HOME FOUND; NOT WRITING RECORD';
								NULL; -- don't write any records
						END IF;


						IF pr.phonework IS NOT NULL AND pr.phonework <> '' AND pr.phonework <> 'None'			
							THEN
								--RAISE NOTICE 'PHONE WORK FOUND: %', pr.phonework;
								EXECUTE format('INSERT INTO public.contactphone(
											            phoneid, human_humanid, phonenumber, phoneext, phonetype_typeid, 
											            disconnectts, disconnect_userid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, 102, 
											            NULL, NULL, now(), %L, 
											            now(), %L, NULL, NULL, 
											            ''Created during person-human migration JUL-2021'');', 
											            		fresh_human_id, unifyspacesandtrim(pr.phonework), 
											            					creationrobotuser,
								            					creationrobotuser);
							ELSE
								--RAISE NOTICE 'NO VALID PHONE WORK FOUND; NOT WRITING RECORD';
								NULL; -- don't write any records
						END IF;
						
						IF pr.email IS NOT NULL AND pr.email <> '' AND pr.email <> 'None'			
						
							THEN
								--RAISE NOTICE 'EMAIL: WRITING RECORD % ', pr.email;
								EXECUTE format('INSERT INTO public.contactemail(
									            emailid, human_humanid, emailaddress, bouncets, createdts, createdby_userid, 
									            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
									            notes)
									    VALUES (DEFAULT, %L, %L, NULL, now(), %L, 
									            now(), %L, NULL, NULL, 
									            ''Created during person to human migration JUL-2021'');', 
							            		fresh_human_id, unifyspacesandtrim(pr.email), creationrobotuser,
				            					creationrobotuser);
							ELSE
								--RAISE NOTICE 'EMAIL: NO VALID EMAIL FOUND, SKIPPING';
								NULL; -- don't write any records
						END IF;

					ELSE  -- Record duplicate person found, don't write new humans, just write to log
						current_human_id := human_dupid;
						RAISE NOTICE 'DUPLICATE HUMAN FOUND: logging and using already inserted humanid % ', current_human_id  ;
						EXECUTE format('INSERT INTO public.personhumanmigrationlog(
									            logentryid, human_humanid, person_personid, error_code, notes, ts)
									    VALUES (DEFAULT, NULL, NULL, 2, %L, now());',
								    		 'DUPLICATE RECORD FOR fullname ' || fullname || ' With Person ID: ' || pr.personid
									    );

				END IF; -- over duplicate check of person records

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

				 				EXECUTE format('INSERT INTO public.humanparcel(
								                       linkid, human_humanid, parcel_parcelkey, source_sourceid, createdts, 
											            createdby_userid, lastupdatedts, lastupdatedby_userid, deactivatedts, 
											            deactivatedby_userid, notes, linkedobjectrole_lorid)
												    VALUES (DEFAULT, %L, %L, %L, now(), 
												    		%L, now(), %L, NULL,
												            NULL, ''Created during person-human migration AUG 2021'', %L);',
										            		current_human_id, parcel_rec.parcelkey, defaultsource, 
								            				creationrobotuser, creationrobotuser,
								            				parcel_human_lorid
					            				);
				 				RAISE NOTICE 'Linked parcel with PK % to Human with PK %', parcel_rec.parcelkey, current_human_id;

				 			-- If no parcel exists, write record to migrationlog table
				 			ELSE
				 				RAISE NOTICE 'No parcel found to link';
				 				-- EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
									-- 				            logentryid, human_humanid, person_personid, error_code, notes, ts)
									-- 				    VALUES (DEFAULT, NULL, %L, %L, %L, now());',
									-- 				     pr.personid, 5, 
									-- 				    'UNABLE TO LINK PARCEL ' ||  parcel_rec.parcelkey || ' TO HUMAN ' || current_human_id
									-- 				);
		 				END IF;

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
								-- THE PO box parsing process potentially makes this fraught so beware!

						IF FOUND
							THEN -- we've already got a link between the person and that person's address
								RAISE NOTICE 'Fresh human has legacy address already linked to a parcel: moving on';
								NULL;
							ELSE 	-- we don't have a mailing address that matches the person record's mailing address, 
									-- So write a new address and link it to our fresh human

								zip_parsed := cnf_parsezipcode(pr.address_zip);								
								SELECT id 
									FROM mailingcitystatezip 
									WHERE zip_code = zip_parsed AND list_type_id = 1 
									INTO citystatezip_rec;


								IF FOUND
									THEN --we've got a real zip code to attach to our new street
									RAISE NOTICE 'Fresh human has address with legitimite ZIP %', citystatezip_rec.id;
									street_freshstreetname := extractstreet(pr.address_street);
									RAISE NOTICE 'Extracted street from fresh address: %|<-SPACE CHECK', street_freshstreetname;

									IF street_freshstreetname IS NOT NULL
										THEN -- we have successfully extracted a street name

											SELECT streetid INTO street_freshid 
												FROM mailingstreet 
												WHERE citystatezip_cszipid = citystatezip_rec.id
													AND mailingstreet.name ILIKE street_freshstreetname;

											IF street_freshid IS NULL OR street_freshid = 0
												THEN -- no existing street with this same zip, so write new street

													-- Write new street and address records	
													EXECUTE format('
														INSERT INTO public.mailingstreet(
													            streetid, name, namevariantsarr, citystatezip_cszipid, notes, 
													            pobox)
													    VALUES (DEFAULT, %L, NULL, %L, %L, 
												    	        NULL);',
												    	        street_freshstreetname, citystatezip_rec.id, 'Migration pers-hum AUG-2021; Raw addr: '|| pr.address_street
									    	        );

									    	        -- Now get our fresh street ID for writing our building Number
									    	        SELECT currval('mailingstreet_streetid_seq') INTO street_freshid;
							    	        END IF; -- possibly write new street
							    	        -- with a street PK, we're ready to write to the mailingaddress base table
							    	        EXECUTE format('
							    	        	INSERT INTO public.mailingaddress(
											            addressid, bldgno, street_streetid, verifiedts, verifiedby_userid, 
											            verifiedsource_sourceid, source_sourceid, createdts, createdby_userid, 
											            lastupdatedts, lastupdatedby_userid, deactivatedts, deactivatedby_userid, 
											            notes)
											    VALUES (DEFAULT, %L, %L, NULL, NULL, 
											            NULL, %L, now(), %L, 
											            now(), %L, NULL, NULL, 
											            %L);',
											            extractbuildingno(pr.address_street), street_freshid,
											            defaultsource, creationrobotuser,
											            creationrobotuser,
											            'Migration AUG-2021: Raw addr: ' || pr.address_street
							    	        	);

							    	        SELECT currval('mailingaddress_addressid_seq') INTO address_freshid;

							    	        -- Now that we know the ID of the fresh mailing, we can link our fresh human via the old personid
							    	        -- to this new address we just made and got an ID for
							    	        EXECUTE format('INSERT INTO public.humanmailingaddress(
														            humanmailing_humanid, humanmailing_addressid, source_sourceid, 
														            createdts, createdby_userid, lastupdatedts, lastupdatedby_userid, 
														            deactivatedts, deactivatedby_userid, notes, linkid, linkedobjectrole_lorid)
														    VALUES (%L, %L, %L, 
														            now(), %L, now(), %L, 
														            NULL, NULL, ''Created during person-human migration AUG 2021'', DEFAULT, %L);',
														            current_human_id, address_freshid, defaultsource,
														            creationrobotuser, creationrobotuser, human_mailing_lorid);
						    	        ELSE -- could not extract street
						    	        	RAISE NOTICE 'COULD NOT EXTRACT STREET; SKIPPING NEW ADDRESS INSERT ';
											EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
															            logentryid, human_humanid, person_personid, error_code, notes, ts)
															    VALUES (DEFAULT, %L, %L, %L, %L, now());',
															    current_human_id, pr.personid, 6,
															    'Street parsing failure on: ' ||  pr.address_street
															);
										END IF;


									ELSE -- malformed ZIP on legacy person, meaning we cannot write a new address
									RAISE NOTICE 'ZIP NOT FOUND: % ', zip_parsed;
										EXECUTE format ('INSERT INTO public.personhumanmigrationlog(
														            logentryid, human_humanid, person_personid, error_code, notes, ts)
														    VALUES (DEFAULT, %L, %L, %L, %L, now());',
														    current_human_id, pr.personid, 3, 
														    'Zip not found in master file: ' || pr.address_zip
														);
								END IF; -- end check for legitimate zipcode found on old person address
						END IF; -- end check for existing address connection between person's parcel and ONE of that parcel's addresses
		 		END LOOP; -- end iteration over property-person records 

				RAISE NOTICE 'END PERSON RECORD';
		END LOOP; -- over legacy person table records

		RETURN human_rec_count;
	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;



CREATE OR REPLACE FUNCTION public.unifyspacesandtrim(chaostext TEXT)
  	RETURNS TEXT AS

$BODY$

	BEGIN
		-- start by replacing all spaces of any kind with latin 0020
		-- then trim that normal space from the end and beginning
		RETURN regexp_replace(
					regexp_replace(
						regexp_replace(chaostext, '[\s\u180e\u200B\u200C\u200D\u2060\uFEFF\u00a0]',' ','g'), 
						'\s+$',''),
						'^\s+','');
	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;




CREATE OR REPLACE FUNCTION public.cnf_parsezipcode(zipraw TEXT)
  	RETURNS TEXT AS
$BODY$
  	DECLARE
  		cleanzip TEXT;

	BEGIN
		cleanzip := substring(zipraw FROM '(\d{5})');
		IF cleanzip IS NOT NULL
			THEN
				RETURN cleanzip;
			ELSE
				RETURN '';
		END IF;
	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;



-- This puppy takes notices of violation that are sent to actual person records
-- and extracts the person record data at the time of this function
-- and writes them to static fields on the actual NOV record, allowing a perfect
-- archive of who recieved the letter and to where that letter was mailed.
-- the old version used so-called ghosts, which were copies of person records
-- and then the NOV was keyed to the ghost, which, in theory, would never change
-- since gosts are dead, and don't change after death.

CREATE OR REPLACE FUNCTION public.cnf_injectstaticnovdata(targetmunicode INTEGER)
  	RETURNS INTEGER AS
$BODY$
  	DECLARE
  		nov_rec RECORD;
  		pers_rec RECORD;
  		fullname TEXT;
  		fixedname TEXT;
  		fixedaddr TEXT;
  		fixedcity TEXT;
  		fixedstate TEXT;
  		fixedzip TEXT;
  		nov_count INTEGER;
	BEGIN
		nov_count := 0;
		FOR nov_rec IN SELECT noticeid, personid_recipient FROM public.noticeofviolation 
			INNER JOIN public.cecase ON (noticeofviolation.caseid = cecase.caseid)
			INNER JOIN public.property ON (cecase.property_propertyid = property.propertyid)
			WHERE municipality_municode = targetmunicode

			LOOP -- over NOVs by MUNI
				SELECT personid, fname, lname, address_street, address_city, 
       				address_state, address_zip FROM public.person WHERE personid = nov_rec.personid_recipient INTO pers_rec;

   				RAISE NOTICE 'WRITING FIXED RECIPIENT ID % INTO NOV ID %', nov_rec.personid_recipient, nov_rec.noticeid;
   				fullname := pers_rec.fname || ' ' || pers_rec.lname;

   				EXECUTE format('UPDATE noticeofviolation SET 
   					fixedrecipientxferts = now(), 
   					fixedrecipientname = %L,
   					fixedrecipientstreet = %L,
				    fixedrecipientcity = %L,
				    fixedrecipientstate = %L,
				    fixedrecipientzip = %L WHERE noticeid = %L;',
				    fullname,
				    pers_rec.address_street,
				    pers_rec.address_city,
				    pers_rec.address_state,
				    pers_rec.address_zip,
				    nov_rec.noticeid);
   				nov_count := nov_count + 1;
   				RAISE NOTICE 'UPDATE SUCCESS! Count: % ', nov_count;
			END LOOP; -- loop over NOVs by MUNI
		RETURN nov_count;
	END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


ALTER TABLE citationviolation RENAME COLUMN linksource to source_sourceid;

ALTER TABLE citationcitationstatus ADD COLUMN courtentity_entityid INTEGER CONSTRAINT citationcitationstatus_courtentityid_fk REFERENCES courtentity (entityid);

--IF datepublished IS NULL the patch is still open and receiving changes
INSERT INTO public.dbpatch(patchnum, patchfilename, datepublished, patchauthor, notes)
    VALUES (37, 'database/patches/dbpatch_beta37.sql','03-10-2022', 'ecd', 'Human and Parcel migration');


-- output from live sysetm SB22
-- NOTICE:  drop cascades to 3 other objects
-- DETAIL:  drop cascades to constraint mailingparcel_mailingid_fk on table parcelmailingaddress
-- drop cascades to constraint humanmailing_addressid_fk on table humanmailingaddress
-- drop cascades to constraint mailingparcel_mailingid_fk on table mailingaddressparcel
-- Query returned successfully: one row affected, 145 msec execution time.

