CREATE FUNCTION generateuserperson(login_row login) RETURNS integer AS $$

DECLARE
	newpersonid integer;

BEGIN
       

	INSERT INTO public.person(
		    personid, persontype, muni_municode, fname, lname, jobtitle, 
		    phonecell, phonehome, phonework, email, address_street, address_city, 
		    address_state, address_zip, notes, lastupdated, expirydate, isactive, 
		    isunder18, humanverifiedby, compositelname, sourceid, creator, 
		    businessentity, mailing_address_street, mailing_address_city, 
		    mailing_address_zip, mailing_address_state, useseparatemailingaddr, 
		    expirynotes, creationtimestamp, canexpire, userlink)
	    VALUES (DEFAULT, 'User'::persontype, login_row.muni_municode, login_row.fname, login_row.lname, login_row.worktitle, 
		    login_row.phonecell, login_row.phonehome, login_row.phonework, login_row.email, login_rowaddress_street, login_row.address_city, 
		    login_row.address_state, login_row.address_zip, login_row.notes, now(), '01-01-2021', TRUE, 
		    FALSE, 100, FALSE, 13 , 100, 
		    FALSE, NULL, NULL, 
		    NULL, NULL, FALSE, 
		    NULL, now(), FALSE, login_row.userid);

	    newpersonid :=currval('person_personidseq');

	    

	    RETURN newpersonid;


END;


$$ LANGUAGE plpgsql;

