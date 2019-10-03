# Building occupancy inspection infrastructure

## Details of the application table
applicationid - auto-generated on table insert with DEFAULT

multiUnit  - use an on/off switch on the GUI to select multi-unit or not

reason_reasonid - this is a FK field to the public.occpermitapplicationreason table

submissiontimestamp - use the now() postgresFunction 

currentowner_personid - The next four fields are all Foreign Keys into the Person table

contatperson_personid - person fk
 
newoccupant_personid - person fk

newowner_personid - person fk

occupancyinspection_id - FK to occupancyinspection table - added after payment is done

submitternotes - blank textarea box for user to add extra comments

internalnotes - used by code officers on the back end

propertyunitid - 