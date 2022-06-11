# Patches for Michael Faux's PUT and icon unification
ALTER TABLE public.propertyusetype ADD COLUMN active BOOLEAN DEFAULT TRUE;

ALTER TABLE public.icon ADD COLUMN active BOOLEAN DEFAULT TRUE;