SELECT codeviolation.violationid FROM public.citationviolation 	
	INNER JOIN public.citation ON citation.citationid = citationviolation.citation_citationid
	INNER JOIN public.codeviolation on codeviolation.violationid = citationviolation.codeviolation_violationid
	WHERE citation.citationid=667;