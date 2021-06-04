select * from codeviolation 
INNER JOIN CECASE on cecase.caseid=codeviolation.cecase_caseid
INNER JOIN property on cecase.property_propertyid = property.propertyid
INNER JOIN municipality on property.municipality_municode = municipality.municode
where cecase_caseid = 19499 
OR cecase_caseid = 34481 
OR cecase_caseid = 36883
OR cecase_caseid =58233
OR cecase_caseid =58234
OR cecase_caseid =58235
OR cecase_caseid =58236
OR cecase_caseid =58675
OR cecase_caseid =58232
OR cecase_caseid =58229
ORDER BY property.address;



\COPY (select * from codeviolation INNER JOIN CECASE on cecase.caseid=codeviolation.cecase_caseid
INNER JOIN property on cecase.property_propertyid = property.propertyid
INNER JOIN municipality on property.municipality_municode = municipality.municode
where cecase_caseid = 19499 
OR cecase_caseid = 34481 
OR cecase_caseid = 36883
OR cecase_caseid =58233
OR cecase_caseid =58234
OR cecase_caseid =58235
OR cecase_caseid =58236
OR cecase_caseid =58675
OR cecase_caseid =58232
OR cecase_caseid =58229
ORDER BY property.address;
) TO '/home/edarsow/vbone.csv' DELIMITER ',' CSV HEADER;

