ROLLBACK;
BEGIN;

SELECT migratepropertytoparcel(	99, -- cogbot
				13, -- code source
				14504, -- zip code 15035 default city record
				821, --emb
				100); -- parcelAddr LOR
COMMIT;						
ROLLBACK;

BEGIN;

SELECT migratepersontohuman( 	99, -- cogbot
				13, -- code source (migration)
				821, --EMB
				101, --parcelhuman LOR
				102); --MAHuman LOR
				
COMMIT;
ROLLBACK;		

BEGIN;

SELECT cnf_injectstaticnovdata(999); --cogland

ROLLBACK;
COMMIT;


	