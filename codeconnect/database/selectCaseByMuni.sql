SELECT 
  cecase.caseid, 
  municipality.municode
FROM 
  public.cecase, 
  public.property, 
  public.municipality
WHERE 
  cecase.property_propertyid = property.propertyid AND
  property.municipality_municode = municipality.municode AND
  municipality.municode = 999;
