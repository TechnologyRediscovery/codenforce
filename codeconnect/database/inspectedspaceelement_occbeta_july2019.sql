SELECT occinspectedspaceelement.inspectedspaceelementid
     FROM occinspectedspaceelement INNER JOIN occinspectedspace ON (occinspectedspaceelement.inspectedspace_inspectedspaceid = occinspectedspace.inspectedspaceid)
     INNER JOIN occspaceelement ON (occspaceelement.spaceelementid = occinspectedspaceelement.spaceelement_elementid)
     INNER JOIN occspace ON (occspaceelement.space_id = occspace.spaceid)
     WHERE occinspectedspace.inspectedspaceid=0;


 SELECT occinspectedspaceelement.inspectedspaceelementid
     FROM occinspectedspaceelement INNER JOIN occinspectedspace ON (occinspectedspaceelement.inspectedspace_inspectedspaceid = occinspectedspace.inspectedspaceid)
     WHERE occinspectedspace.inspectedspaceid=0;