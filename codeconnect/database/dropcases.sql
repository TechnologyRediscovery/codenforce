CREATE OR REPLACE FUNCTION public.dropcecase(caseid int)
  RETURNS integer AS
$BODY$

DECLARE
	successfuldrop BOOLEAN;

BEGIN

FOR 
       

END;


$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION public.generateuserperson(login)
  OWNER TO sylvia;




ALTER TABLE public.codeviolation
  ADD CONSTRAINT codeviolation_caseid_fk FOREIGN KEY (cecase_caseid)
      REFERENCES public.cecase (caseid) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;