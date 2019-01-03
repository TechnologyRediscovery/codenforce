# Adding additional columns to postgres tables

First draft tables should be as complete as possible before getting wired up to business objects in Java. Adding additional columns becomes necessary as the data object specifications and uses evolve over time.

## Enumerating a needs list
This tutorial documents a series of changes to the `person` table that were identified when building the `SearchParamsPerson` object:

### name blob flag
When adding data from county listings, the entire name is squished into one line in a listing on the HTML page. The parsing algo initially created during the test import process was only accurate in splicing first and last about 80% of the time. The simpler way to preserve accuracy is not to parse them at all: just store them as a blob in the last name. 

If the last name is actually a blob that contains potentially a first name and/or a middle name or initial, we'll turn on the `nameBlob` switch.

#### what do we mean by a 'flag'?
It's an on/off switch, usually implemented with a `boolean` data type in almost every system you'll use. We raise a flag by toggling the variable to true. 

The downfall of creating flagging variables is not maintaining them at some undefined point on some undefined subset of records--an act which potentially eroding the value of the whole column (since we're talking semantics here, we might also say: field or variable or member).

In the case of a name blob flag, we'll set this column's value to true if we've written more than just a last name into the `lname` column. This can be important in mailing, in that we'd never use a blobed last name field in a reverse order name print by last name. Or at least we'd want to think about what to do if the goal is to print a list alphabetized by Person last names. Perhaps i'll include them all together at the start or end of the list, etc.

### business entity flag
The county stores humans and business owners of a property in the same field without an obvious flag. So this flag field is more of a convenience in that it will default to false, and at some undefined point in the future, our scraper might start raising this flag (i.e. setting its value to true) when some sort of regexp pattern is matched or a parsing algo decides they've got a business name on its hands.

### need for separate mailing address 
County records provide the mailing address for tax purposes in a field on their property listing. We will store this data, too, in a separate set of four mailing address fields (java entity version):
	private String mailing_address_street;
	private String mailing_address_city;

	private String mailing_address_zip;
	private String mailing_address_state;

If we are storing a person's mailing address that may or may not be the same as their residence, we will look to the corresponding flag declared with the following on the Person class:

	private boolean mailingSameAsResidence;

### tracking origination data
It's easy to dump contact info into database fields. It's harder to maintain that data's integrity over years and potentially a dozen or more changes without meta-data. Central meta-data in Person management is origin information--namely where the Person came from, who ushered that data into CC, and when--which we'll capture in these four new member variables and corresponding database table fields:

    private int sourceID;
    private String sourceTitle;
    private User creator;
    private LocalDateTime dateCreated;

#### several approaches to utility fields like `source`
CC uses a few ways to store categorical information about various objects, such as the phase of a CECase, the type of `Person`, etc. The most robust way is to define an entire entity in Java, that encapsulates an entire record in the corresponding utility table in the database: 

* unique identifier (i.e. its primary key field value)
* title (i.e. name)
* description

If we ever want the user to be able to manipulate these categories, creating the full object and its corresponding integration class methods for retrieving and updating is the only way to keep from going mad. CEActionRequestStatus is an excellent example. 

##### too much overhead in the custom type?
The management overhead for this custom type is worth its weight in gold since we can now make List objects of these, send them out to drop down selection boxes in the view. Since the primary key of the record that created each type is inside the object itself, we can slap them on a CEActionRequest object during the workflow and the integration process with the database is nice and smooth.

In the case of a Person origin, the user will NEVER change the origin field--so foregoing creating a custom type and just storing the id as an `int` type and title as `String` keeps complexity at a minimum.

## Database update scripts
We have data in our table that is there to stay which will drive our database update process into a series of phases that basically follows the pattern of creating a structure, populating, and then locking down the contents with database constraints.

1. create the `personsource` utility table
2. populate `personsource` with at least the default value that we'll store in the corresponding field in `person`
3. add new columns to `person` table
4. update existing `person` records with default values for any of our new fields. 
5. check our work on the test postgres db
6. run scripts on the deployed postgres db

### utility table
Our lil' utility table only has two columns. We create a sequence to auto-fill the primary key field.

	BEGIN;
	CREATE SEQUENCE IF NOT EXISTS personSource_sourceID_seq
		START WITH 10
		INCREMENT BY 1
		MINVALUE 10
		NO MAXVALUE
		CACHE 1;

	CREATE TABLE personsource
	(
		sourceid    INTEGER DEFAULT nextval('personSource_sourceID_seq') CONSTRAINT personSource_sourceIDID_pk PRIMARY KEY,
		title       text NOT NULL
	) ;

### populating utility table
Most database management packages will create a template INSERT script for you. In pgadminIII I `right-click the table name -- scripts -- INSERT script`

The sql:

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

Note that I created the unknown source with a hard-coded id of 9, which is under my auto-generating sequence. The next three are actual sources whose ids are auto-generated. By hard-coding the 9 into the script, I can confidently also hard code the value into a `.properties` file which my java can read from when it needs to know a default row identifier to use when inserting a person whose source is unknown. The actual users I assume the customizer of the entire system will address and will need to look up those IDs on their own.

This kind of scheme is somewhat tinkery in finding the balance between writing db scripts that are too brittle and writing ones that we can confidently run and wire up to stuff in JSF without getting a bunch of key errors on db writes.

### adding columns to `person`
After reviewing the [postgres documentation on data manipulation](https://www.postgresql.org/docs/10/ddl-alter.html#DDL-ALTER-ADDING-A-COLUMN), these column additions were written and tested. 

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

Note the syntax for foreign keys: when declared along with the `ADD COLUMN` the foreign key command is implied with `REFERENCES`.

### committing your work
It's good practice to not auto-commit your SQL commands. It will only take one unintended auto-commit when you're working on the production system to burn into your mind the value of being able to check your work before making it permanent. 

In this tutorial, the "auto-commit" option in pgadminIII was unchecked and I gave the 

	BEGIN;

and the 

	COMMIT;

commands on my own. If I had made a mistake, I could always 

	ROLLBACK;

This is [called working in transactions](https://www.postgresql.org/docs/8.3/tutorial-transactions.html), and, in postgres, the documentation notes that this process is always underway even if we don't tinker with it ourselves.

### checking and running on deployed database
All is well in the `person` table, and the scripts are declared correct. To make these changes on the deployed database, I first need to tunnel into the admin port with this shell command:

	$ ssh -f edarsow@cogdrop -L 20000:localhost:5432 -N

This connects my local port `20000` to my VPS's port `5432` which is what the postgres daemon listens on for admin commands. To the deployed postgres system, these commands appear to be coming from localhost, not remotely. I'm relying on ssh to do the authentication and encrypting for me--which assumes that the VPS has not been compromised.

If you've never used ssh for port forwarding, read the manual page first with:

	$ man ssh

Using the default man viewer in Ubuntu 16.04, search for the `-f` flag in the documentation by entering `/` followed by the search key `-f` and then `enter`. You can repeat the search for the same term with only the `/`. This is all documented in the manual's manual accessed with:

	$ man man

## Updating `Person` POJO and `PersonIntegrator`
With new columns in the database, we need member variables on the plain-old-java-object (aka "business objects") to store the data in the database for use by our java code. This requires an [integration class][1] method that extracts the data from the db and injects it into our `Person` objects.

[1]: https://github.com/TechnologyRediscovery/codeconnect/blob/master/src/main/java/com/tcvcog/tcvce/integration/PersonIntegrator.java "Person Integrator"
