SELECT inspectedspaceelementid, notes, locationdescription_id, occinspectedspaceelement.lastinspectedby_userid,
                   lastinspectedbyts, compliancegrantedby_userid, compliancegrantedts,
                   inspectedspace_inspectedspaceid, overriderequiredflagnotinspected_userid,
                   spaceelement_elementid
             FROM occinspectedspaceelement INNER JOIN occinspectedspace ON (inspectedspace_inspectedspaceid = inspectedspaceid)
             INNER JOIN occspaceelement ON (spaceelementid = spaceelement_elementid)
             INNER JOIN occspace ON (space_id = spaceid)
             WHERE inspectedspaceid=0;