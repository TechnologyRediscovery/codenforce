SELECT DISTINCT ON (citationid) citation.citationid, codeviolation.cecase_caseID FROM public.citationviolation 	
	INNER JOIN public.citation ON citation.citationid = citationviolation.citation_citationid
	INNER JOIN public.codeviolation on codeviolation.violationid = citationviolation.codeviolation_violationid
	INNER JOIN public.cecase ON cecase.caseid = codeviolation.cecase_caseID
	WHERE codeviolation.cecase_caseID = 1005;