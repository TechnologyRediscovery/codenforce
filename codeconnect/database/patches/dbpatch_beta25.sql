ALTER TABLE public.actionrqstissuetype RENAME TO ceactionrequestissuetype;

ALTER TABLE public.ceactionrequestissuetype ADD COLUMN intensity_classid INTEGER 
	CONSTRAINT ceactionrequestissuetype_intensity_fk REFERENCES public.intensityclass (classid);

ALTER TABLE public.ceactionrequestissuetype ADD COLUMN active BOOLEAN DEFAULT TRUE;