SELECT DISTINCT violationid
FROM public.codeviolation 
	INNER JOIN public.cecase ON (cecase.caseid = codeviolation.cecase_caseid)
	INNER JOIN public.property ON (cecase.property_propertyid = property.propertyid)
	LEFT OUTER JOIN 
		(	SELECT codeviolation_violationid, citation.citationid, citation.dateofrecord
			FROM public.citationviolation 
				INNER JOIN public.citation ON (citationviolation.citation_citationid = citation.citationid)
				INNER JOIN public.citationstatus on (citationstatus.statusid = citation.status_statusid)
			WHERE citationstatus.editsforbidden = TRUE	
		) AS citv ON (codeviolation.violationid = citv.codeviolation_violationid)
	LEFT OUTER JOIN 
		(
			SELECT codeviolation_violationid, sentdate
			FROM noticeofviolationcodeviolation
				INNER JOIN public.noticeofviolation ON (noticeofviolationcodeviolation.noticeofviolation_noticeid = noticeofviolation.noticeid)
			WHERE noticeofviolation.sentdate IS NOT NULL
		) AS novcv ON (codeviolation.violationid = novcv.codeviolation_violationid)
WHERE violationid IS NOT NULL
AND novcv.sentdate IS NOT NULL
AND citv.citationid IS NOT NULL;