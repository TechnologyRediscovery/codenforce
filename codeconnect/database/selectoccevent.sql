SELECT eventid, category_catid, occperiod_periodid, dateofrecord, eventtimestamp,  
                               eventdescription, owner_userid, disclosetomunicipality, disclosetopublic,  
                               activeevent, hidden, occevent.notes, property_propertyid, propertyunit_unitid, municipality_municode
	FROM public.occevent 
	INNER JOIN public.occperiod ON (occperiod_periodid = periodid)
	INNER JOIN public.propertyunit ON (propertyunit_unitid = unitid) 
	INNER JOIN public.property on (property_propertyid = propertyid)  
	WHERE eventid = 1003;