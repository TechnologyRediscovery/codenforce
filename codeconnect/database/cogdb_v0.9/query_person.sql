SELECT DISTINCT	personid 	FROM 	public.person 
				LEFT OUTER JOIN public.propertyperson ON (person.personid = propertyperson.person_personid)
				LEFT OUTER JOIN public.occperiodperson ON (person.personid = occperiodperson.person_personid)
				LEFT OUTER JOIN public.eventperson ON (person.personid = eventperson.person_personid)
				LEFT OUTER JOIN public.citationperson ON (person.personid = citationperson.person_personid)
				LEFT OUTER JOIN public.personmergehistory ON (person.personid = personmergehistory.mergetarget_personid)
				LEFT OUTER JOIN public.personmunilink ON (person.personid = personmunilink.person_personid);