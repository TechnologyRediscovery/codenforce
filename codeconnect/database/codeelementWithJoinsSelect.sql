SELECT elementid, codeelementtype_cdeltypeid, codesource_sourceid, ordchapterno, 
       ordchaptertitle, ordsecnum, ordsectitle, ordsubsecnum, ordsubsectitle, 
       ordtechnicaltext, ordhumanfriendlytext, defaultpenalty, isactive, 
       isenforcementpriority, resourceurl, inspectiontips, datecreated
       FROM public.codeelement INNER JOIN public.codeelement

elementid, codeelementtype.cdelTypeID as codeelementtypeid, codeelementtype.name as codeelementtypename, 
	codesource_sourceid, codesource.name as codesourcename, codesource.year as codesourceyear, 
	ordchapterno, 
	ordchaptertitle, ordsecnum, ordsectitle, 
	ordsubsecnum, ordsubsectitle, ordtechnicaltext, 
	ordhumanfriendlytext, defaultpenalty, codeelement.isactive, 
	isenforcementpriority, defaultdaystocomply, resourceurl, 
	inspectiontips, datecreated
  FROM public.codeelement 	INNER JOIN public.codeelementtype ON cdeltypeid = codeelementtype_cdelTypeID 
				INNER JOIN public.codesource ON sourceid = codesource_sourceID
  WHERE codesource_sourceid = 10;
