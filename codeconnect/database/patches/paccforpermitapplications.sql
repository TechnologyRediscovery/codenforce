--We'll need a function to generate PACC numbers for the new PACC column on occpermitapplication 

CREATE OR REPLACE FUNCTION generateRand(low INT, high INT)
	RETURNS INT AS
$$
   BEGIN
      RETURN floor(random() * (high-low + 1) + low);
   END
$$ language 'plpgsql' STRICT;

--Alright, let's get started

ALTER TABLE public.occpermitapplication ADD COLUMN applicationpubliccc integer;

UPDATE public.occpermitapplication SET applicationpubliccc = generateRand(100000,999999) WHERE applicationid > 0;

ALTER TABLE public.occpermitapplication ALTER COLUMN applicationpubliccc SET NOT NULL;

ALTER TABLE public.occpermitapplication ADD COLUMN paccenabled boolean;
ALTER TABLE public.occpermitapplication ALTER COLUMN paccenabled SET DEFAULT true;

ALTER TABLE public.occpermitapplication ADD COLUMN allowuplinkaccess boolean;
ALTER TABLE public.occpermitapplication ALTER COLUMN allowuplinkaccess SET DEFAULT true;