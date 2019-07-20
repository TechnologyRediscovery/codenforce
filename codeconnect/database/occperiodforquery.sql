SELECT DISTINCT occperiod.periodid
FROM occperiod INNER JOIN occperiodtype ON (type_typeid = typeid)
INNER JOIN propertyunit ON (occperiod.propertyunit_unitid = unitid)
INNER JOIN property ON (propertyunit.property_propertyid = property.propertyid)
RIGHT OUTER JOIN occinspection ON (occinspection.occperiod_periodid = periodid)
RIGHT OUTER JOIN occpermit ON (occpermit.occperiod_periodid = periodid) WHERE occperiod.periodid IS NOT NULL;