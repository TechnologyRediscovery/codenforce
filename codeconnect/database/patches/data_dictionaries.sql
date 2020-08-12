-- Certain columns from the WPRDC API contain both an abbreviation and a description column
-- Rather than duplicating values, our abbreviation column will be a foreign key pointing to a description table
-- See https://data.wprdc.org/dataset/property-assessments/resource/43a275de-4745-446b-8ba0-b9389568e568

-- Fixes condition (was incorrectly labelled as text)
ALTER TABLE propertyexternaldata
    ALTER COLUMN condition type smallint
    USING condition::smallint;


CREATE TABLE datadict_condition(
    id          smallint,
    name        text,
    description 	text);

COMMENT ON TABLE datadict_condition
    IS 'Code for the overall physical condition or state of repair of a structure, relative to its age.';


-- TODO: Get in touch with the WPRDC and see if these whack orderings of values is actually correct.
-- If it IS correct, we should use record["CONDITIONDESC"] instead, and reorder the columns into an easy to sort method.
INSERT INTO datadict_condition(
    id, name, description
) VALUES
    (1, 'Excellent', 'Outstanding maintenance.'),
    (7, 'Very Good', 'High degree of upkeep')
    (2, 'Good', 'Above ordinary maintenance.'),
    (3, 'Average', 'Ordinary maintenance, shows normal wear and tear.'),
    (4, 'Fair', 'Sound but with noticeable deffered maintenance'),
    (5, 'Poor', 'Structural deterioration caused by chronic deferred maintenance.'),
    (8, 'Very Poor', 'Barely livable'),
    (6, 'Unsound', 'Not suitable for habitation')
;





7-Very good -High degree of upkeep.  8-Very poor -Barely livable. 6-Unsound -Not suitable for habitation.
)


