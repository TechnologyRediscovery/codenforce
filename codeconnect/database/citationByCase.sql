SELECT citationid, citationno, origin_courtentity_entityid, cecase_caseid, 
       citation.login_userid, dateofrecord, transtimestamp, isactive, citation.notes
  FROM public.citation 	INNER JOIN public.cecase ON cecase.caseid = citation.cecase_caseid
  WHERE cecase.caseID=1002; 
