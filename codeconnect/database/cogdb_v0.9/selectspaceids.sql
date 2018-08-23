SELECT DISTINCT spaceid
FROM inspectedchecklistspaceelement INNER JOIN checklistspaceelement ON (checklistspaceelementid = checklistspaceelement_id)
	INNER JOIN spaceelement ON (spaceelement_id = spaceelementid)
	INNER JOIN space ON (space_id = spaceid)
WHERE occupancyinspection_id=101;