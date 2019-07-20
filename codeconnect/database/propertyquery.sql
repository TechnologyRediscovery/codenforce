SELECT DISTINCT propertyid
	FROM property LEFT OUTER JOIN propertyexternaldata ON (property.propertyid = propertyexternaldata.property_propertyid)
	LEFT OUTER JOIN propertyusetype ON (property.usetype_typeid = propertyusetype.propertyusetypeid)
	LEFT OUTER JOIN propertystatus ON (property.status_statusid = propertystatus.statusid)
	WHERE propertyid IS NOT NULL;