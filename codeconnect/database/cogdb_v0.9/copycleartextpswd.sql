CREATE OR REPLACE FUNCTION public.copycleartextpswds()
  RETURNS integer AS
$BODY$
DECLARE
 userrow RECORD;
BEGIN
 RAISE NOTICE 'starting transfer...';
 FOR userrow IN SELECT password, userid
 FROM login LOOP
	EXECUTE format('UPDATE login SET pswdcleartext = %L WHERE userid = %L ', userrow.password, userrow.userid); 
	END LOOP;
RETURN 1;
END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;


