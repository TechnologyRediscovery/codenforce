SELECT inspectedspaceelementid, notes, locationdescription_id, lastinspectedby_userid, 
       lastinspectedts, compliancegrantedby_userid, compliancegrantedts, 
       inspectedspace_inspectedspaceid, overriderequiredflagnotinspected_userid, 
       spaceelement_elementid, occinspectedspaceelement.required, failureseverity_intensityclassid, occspaceelement.codeelement_id
  FROM public.occinspectedspaceelement INNER JOIN public.occspaceelement ON (spaceelement_elementid = spaceelementid)
  WHERE inspectedspaceelementid>0;
