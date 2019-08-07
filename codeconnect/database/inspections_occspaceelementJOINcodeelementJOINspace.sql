-- InspectedCodeElements need to provide a key to this critical table when inserting
SELECT spaceelementid, space_id, occspace.name, codeelement_id, codesource_sourceid, ordchapterno, ordchaptertitle, 
       ordsecnum, ordsectitle, ordsubsecnum, ordsubsectitle, occspaceelement.required
  FROM public.occspaceelement INNER JOIN codeelement ON (elementid = codeelement_id)
  INNER JOIN occspace ON (spaceid = space_id);
