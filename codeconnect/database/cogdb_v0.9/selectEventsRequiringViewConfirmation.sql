SELECT eventid, propertyid, caseid, categoryid, categorytype, activeevent, dateofrecord, municipality_municode
FROM ceevent 	INNER JOIN ceeventcategory ON (ceeventcategory_catid = categoryid)
		INNER JOIN cecase ON (cecase_caseid = caseid)
		INNER JOIN property on (property_propertyid = propertyid)
WHERE categorytype = CAST ('Timeline' AS ceeventtype)
		AND dateofrecord >= now()
		AND activeevent = TRUE
		AND ceevent.requiresviewconfirmation = TRUE
		AND hidden = FALSE
		AND viewconfirmedby IS NULL
		AND municipality_municode = 999;