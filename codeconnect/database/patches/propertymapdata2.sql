CREATE OR REPLACE VIEW public.propertymapdata2
 AS
 SELECT p.parid,
    p.propertyid,
    p.lotandblock,
    s.propertyho,
    s.propertyad,
    p.usegroup,
    s.propertyci,
    s.propertyzi,
    COALESCE(p.ownercode, 'NONE'::text) AS ownercode,
    p.lastupdated,
    p.landbankheld,
    p.active,
    p.address,
    propertyexternaldata.address_citystatezip,
    s.geom,
    count(c.caseid) AS casecount,
    COALESCE(c.casename, 'NONE'::text) AS casename
   FROM spatialdata8 s
     FULL JOIN property p ON s.pin::text = p.parid
     FULL JOIN cecase c ON c.property_propertyid = p.propertyid
     FULL JOIN propertyexternaldata ON propertyexternaldata.property_propertyid = p.propertyid
  GROUP BY p.parid, p.propertyid, p.lotandblock, s.propertyho, s.propertyad, p.usegroup, s.propertyci, s.propertyzi, p.ownercode, p.lastupdated, p.landbankheld, p.active, p.address, propertyexternaldata.address_citystatezip, s.geom, c.caseid, c.casename;
