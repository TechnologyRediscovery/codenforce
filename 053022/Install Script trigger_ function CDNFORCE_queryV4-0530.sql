-- Function: trigger_upd_occperiod_via_occinspection

CREATE OR REPLACE FUNCTION trigger_upd_cecase_occperiod_via_occinspection
RETURNS TRIGGER AS $$
DECLARE			
	temp_violationid integer; 		
	temp_cecase_caseid integer;	
        temp_user integer;    
			
 
    BEGIN
	
        -- 	   RETURN(SELECT CAST ( current_user AS text ))
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN
        IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
        END IF;
	        UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = OLD.occperiod_periodid;
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN
        IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
        END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  =  NEW.occperiod_periodid;
			      RETURN NEW;		
             
        ELSIF 
        (TG_OP = 'UPDATE') THEN
        IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
        END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = NEW.occperiod_periodid;
			      RETURN NEW;	
     	END IF;	   
    END;  
$$  LANGUAGE plpgsql;  
-- Trigger: trigger_upd_occperiod_via_occinspection

-- DROP TRIGGER IF EXISTS trigger_upd_occperiod_via_occinspection ON public.occinspection;

CREATE TRIGGER trigger_upd_occperiod_via_occinspection
    BEFORE INSERT OR DELETE OR UPDATE 
    ON public.occinspection
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_occperiod_via_occinspection();

COMMENT ON TRIGGER trigger_upd_occperiod_via_occinspection ON public.occinspection
    IS 'This trigger will update the cecase userid and last update date when action is taken on the occinspection table (update,delete,insert). ';


-- Function: trg_upd_cecase_via_codeviolation
CREATE OR REPLACE FUNCTION trigger_upd_cecase_via_codeviolation()
RETURNS TRIGGER AS $$
DECLARE			
	temp_violationid integer; 		
	temp_cecase_caseid integer;	
        temp_user integer;    
			
    BEGIN			
        --			
        --Update the cecase lastupdate and user when codeviolation record is updated,delete,or inserted.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
            IF OLD.lastupdated_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
	        END IF;
	        UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE caseid = OLD.cecase_caseid;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
	        IF OLD.lastupdated_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
	        END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE caseid = NEW.cecase_caseid;
				   RETURN NEW;
                   
        ELSIF 
        (TG_OP = 'UPDATE') THEN	
            IF OLD.lastupdated_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
	        END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE caseid = NEW.cecase_caseid;		
				   RETURN NEW;
        END IF;	   
    END;  

$$  LANGUAGE plpgsql;     
-- Trigger: trg_upd_cecase_via_codeviolation

DROP TRIGGER IF EXISTS trigger_upd_cecase_via_codeviolation ON public.codeviolation;

CREATE TRIGGER trigger_upd_cecase_via_codeviolation
    BEFORE INSERT OR DELETE OR UPDATE 
    ON public.codeviolation
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_cecase_via_codeviolation();

--**************************************************
CREATE OR REPLACE FUNCTION trigger_upd_cecase_via_citationviolation()
RETURNS TRIGGER AS $$
DECLARE			
	temp_violationid integer; 		
	temp_cecase_caseid integer;	
    temp_user integer; 
 
    BEGIN			
        --			
        --Update the cecase lastupdatedts and user when citationviolation record is updated,insert or deleted.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
		   END IF;
	       UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE caseid  =  (SELECT cecase_cecaseid FROM codeviolation WHERE violationid = OLD.codeviolation_violationid );
			      RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
		    END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user 
            WHERE caseid  =  (SELECT cecase_cecaseid FROM codeviolation WHERE violationid = NEW.codeviolation_violationid);
				   RETURN NEW;
                   
        ELSIF 
        (TG_OP = 'UPDATE') THEN	
            IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
		    END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE caseid  = (SELECT cecase_caseid FROM codeviolation WHERE violationid = NEW.codeviolation_violationid);
				   RETURN NEW;
        END IF;	   
    END;
  
  
$$  LANGUAGE plpgsql;

 -- Trigger: trigger_upd_cecase_via_citationviolation

DROP TRIGGER IF EXISTS trigger_upd_cecase_via_citationviolation ON public.citationviolation;

CREATE TRIGGER trigger_upd_cecase_via_citationviolation
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.citationviolation
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_cecase_via_citationviolation();   
--*************************************************
-- Function: trg_upd_cecase_via_citation
CREATE OR REPLACE FUNCTION trigger_upd_cecase_via_citation()
RETURNS TRIGGER AS $$
DECLARE			
	temp_violationid integer; 		
	temp_cecase_caseid integer;	
        temp_user integer; 
 
    BEGIN			
        --			
        --Update the cecase lastupdate and user when citation record is updated.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
		   END IF;
	       UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE caseid IN (SELECT codeviolation.cecase_caseid FROM codeviolation WHERE violationid IN (SELECT codeviolation_violationid FROM citationviolation WHERE citation_citationid = OLD.citationid ));
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
		    END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE caseid IN (SELECT codeviolation.cecase_caseid FROM codeviolation WHERE violationid IN (SELECT codeviolation_violationid FROM citationviolation WHERE citation_citationid = NEW.citationid ));
			      RETURN NEW;	
        ELSIF 
        (TG_OP = 'UPDATE') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
		    END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE caseid IN (SELECT codeviolation.cecase_caseid FROM codeviolation WHERE violationid IN (SELECT codeviolation_violationid FROM citationviolation WHERE citation_citationid = NEW.citationid ));
			      RETURN NEW;	
            	
        END IF;	   
    END;  
$$  LANGUAGE plpgsql;    
-- Trigger: trigger_up_cecase_via_citation

DROP TRIGGER IF EXISTS trigger_up_cecase_via_citation ON public.citation;

CREATE TRIGGER trigger_up_cecase_via_citation
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.citation
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_cecase_via_citation();
--***********************************************
-- Function: trg_upd_cecase_via_citationcitationstatus
CREATE FUNCTION trigger_upd_cecase_via_citationcitationstatus()
RETURNS TRIGGER AS $$
DECLARE			
    temp_violationid integer; 		
    temp_cecase_caseid integer;	
    temp_user integer; 
 
    BEGIN			
        --			
        --Update the cecase lastupdate and user when citationcitationstatus record is has action taken on it (upd,del,insert)	
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
		   END IF;
	       UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = OLD.lastupdated_userid
           WHERE caseid IN (SELECT codeviolation.cecase_caseid FROM codeviolation WHERE violationid IN (SELECT codeviolation_violationid FROM citationviolation WHERE citation_citationid = OLD.citation_citationid ));
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
		    END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = NEW.lastupdated_userid 
            WHERE caseid IN (SELECT codeviolation.cecase_caseid FROM codeviolation WHERE violationid IN (SELECT codeviolation_violationid FROM citationviolation WHERE citation_citationid = NEW.citation_citationid ));
			      RETURN NEW;	
        ELSIF 
        (TG_OP = 'UPDATE') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
		    END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE caseid IN (SELECT codeviolation.cecase_caseid FROM codeviolation WHERE violationid IN (SELECT codeviolation_violationid FROM citationviolation WHERE citation_citationid = NEW.citation_citationid ));
			      RETURN NEW;	            	
        END IF;	   
    END;  


$$  LANGUAGE plpgsql;
-- Trigger: trigger_upd_cecase_via_citationcitationstatus

DROP TRIGGER IF EXISTS trigger_upd_cecase_via_citationcitationstatus ON public.citationcitationstatus;

CREATE TRIGGER trigger_upd_cecase_via_citationcitationstatus
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.citationcitationstatus
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_cecase_via_citationcitationstatus();

COMMENT ON TRIGGER trigger_upd_cecase_via_citationcitationstatus ON public.citationcitationstatus
    IS 'Update the cecase when action taken on the citationcitationstatus.';    
--***********************************************
-- Function: trg_upd_cecase_via_citationdocketno
CREATE OR REPLACE FUNCTION trigger_upd_cecase_via_citationdocketno()
RETURNS TRIGGER AS $$

DECLARE
    temp_violationid integer; 		
	temp_cecase_caseid integer;	
        temp_user integer; 

    BEGIN			
        --			
        --Update the cecase lastupdate and user when citationdocketno record is has action taken on it -(upd,del,or insert).		
        -- 	   RETURN(SELECT CAST (current_user AS text));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
		   END IF;
	       UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE caseid IN (SELECT cecase_caseid FROM codeviolation WHERE violationid IN (SELECT codeviolation_violationid FROM citationviolation WHERE citation_citationid  = OLD.citation_citationid));
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
		    END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE caseid IN (SELECT cecase_caseid FROM codeviolation WHERE violationid IN (SELECT codeviolation_violationid FROM citationviolation WHERE citation_citationid  = NEW.citation_citationid));
			      RETURN NEW;	
        ELSIF 
        (TG_OP = 'UPDATE') THEN
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
		    END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE caseid IN (SELECT cecase_caseid FROM codeviolation WHERE violationid IN (SELECT codeviolation_violationid FROM citationviolation WHERE citation_citationid  = NEW.citation_citationid));
			      RETURN NEW;	           	
        END IF;	   
    END;  
 
$$  LANGUAGE plpgsql;    
-- Trigger: trigger_upd_cecase_via_citationdocketno

DROP TRIGGER IF EXISTS trigger_upd_cecase_via_citationdocketno ON public.citationdocketno;

CREATE TRIGGER trigger_upd_cecase_via_citationdocketno
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.citationdocketno
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_cecase_via_citationdocketno();

COMMENT ON TRIGGER trigger_upd_cecase_via_citationdocketno ON public.citationdocketno
    IS 'Update the cecase when action taken on citationdocketno (upd,del,insert)';
--***********************************************   
-- Function: trg_upd_cecase_via_citationdocketnohuman
CREATE OR REPLACE FUNCTION trigger_upd_cecase_via_citationdocketnohuman()
RETURNS TRIGGER AS $$
DECLARE			
	temp_violationid integer; 		
	temp_cecase_caseid integer;	
        temp_user integer; 
 
    BEGIN			
        --			
        --Update the cecase lastupdate and user when citationdocketnohuman record is updated.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
		   END IF;
	       UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = OLD.lastupdated_userid
           WHERE caseid IN (SELECT codeviolation.cecase_caseid FROM codeviolation WHERE violationid IN (SELECT codeviolation_violationid FROM citationviolation WHERE citation_citationid IN (SELECT citation_citationid FROM citationdocketno WHERE docketid = OLD.docketno_docketid)));
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
		    END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = NEW.lastupdated_userid 
             WHERE caseid IN (SELECT codeviolation.cecase_caseid FROM codeviolation WHERE violationid IN (SELECT codeviolation_violationid FROM citationviolation WHERE citation_citationid IN (SELECT citation_citationid FROM citationdocketno WHERE docketid = NEW.docketno_docketid)));
			      RETURN NEW;	
        ELSIF 
        (TG_OP = 'UPDATE') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
		    END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE caseid IN (SELECT codeviolation.cecase_caseid FROM codeviolation WHERE violationid IN (SELECT codeviolation_violationid FROM citationviolation WHERE citation_citationid IN (SELECT citation_citationid FROM citationdocketno WHERE docketid = NEW.docketno_docketid)));
			      RETURN NEW;		
            	
        END IF;	   
    END;  

$$  LANGUAGE plpgsql;
-- Trigger: trigger_upd_cecase_via_citationdocketnohuman

DROP TRIGGER IF EXISTS trigger_upd_cecase_via_citationdocketnohuman ON public.citationdocketnohuman;

CREATE TRIGGER trigger_upd_cecase_via_citationdocketnohuman
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.citationdocketnohuman
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_cecase_via_citationdocketnohuman();

COMMENT ON TRIGGER trigger_upd_cecase_via_citationdocketnohuman ON public.citationdocketnohuman
    IS 'This is the table trigger that will update the cecase when an action is taken on ti citationdocketnohuman table (add,delete,update).';  
--***********************************************    
-- Function: trg_upd_cecase_via_occinspection 
CREATE OR REPLACE FUNCTION trigger_upd_cecase_via_occinspection() 
RETURNS TRIGGER AS $$
DECLARE			
	temp_periodid integer; 		
        temp_user integer; 
 
    BEGIN
	
        -- 	   RETURN(SELECT CAST ( current_user AS text ))
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN
        IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
        END IF;
	        UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = OLD.occperiod_periodid;
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN
        IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
        END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  =  NEW.occperiod_periodid;
			      RETURN NEW;		
             
        ELSIF 
        (TG_OP = 'UPDATE') THEN
        IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
        END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = NEW.occperiod_periodid;
			      RETURN NEW;	
     	END IF;	   
    END;  

$$  LANGUAGE plpgsql;
-- Trigger: trigger_upd_cecase_via_occinspection 

DROP TRIGGER IF EXISTS trigger_upd_cecase_via_occinspection ON public.occinspection;

CREATE TRIGGER trigger_upd_cecase_via_occinspection
    BEFORE INSERT OR DELETE OR UPDATE 
    ON public.occinspection
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_cecase_via_occinspection();

COMMENT ON TRIGGER trigger_upd_cecase_via_occinspection ON public.occinspection
    IS 'This trigger will update the cecase userid and last update date when action is taken on the occinspection table (update,delete,insert). ';
--****************************************
CREATE OR REPLACE FUNCTION trigger_upd_cecase_via_occinspectedspace() 
RETURNS TRIGGER AS $$
DECLARE			
    temp_violationid integer; 		
    temp_cecase_caseid integer;	
    temp_user integer; 
 
    BEGIN			
        --			
        --Update the cecase lastupdate and user when occinspectedspace record is updated.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN			
        IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
        END IF;
	       UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE propertyunit_unitid IN (SELECT propertyunit_unitid FROM occupancyperiod WHERE occperiod_periodid IN (SELECT occperiod_periodid FROM occinspection WHERE occperiod_periodid = OLD.occperiod_periodid));
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN
         IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
        END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
             WHERE propertyunit_unitid IN (SELECT propertyunit_unitid FROM occupancyperiod WHERE occperiod_periodid IN (SELECT occperiod_periodid FROM occinspection WHERE occperiod_periodid = NEW.occperiod_periodid));
			      RETURN NEW;	
        ELSIF 
        (TG_OP = 'UPDATE') THEN	
         IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
        END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE propertyunit_unitid IN (SELECT propertyunit_unitid FROM occupancyperiod WHERE occperiod_periodid IN (SELECT occperiod_periodid FROM occinspection WHERE occperiod_periodid = NEW.occperiod_periodid));
			      RETURN NEW; 		
     	END IF;	   
    END;  

$$  LANGUAGE plpgsql;   
-- Trigger: trigger_upd_cecase_via_occinspectedspace

DROP TRIGGER IF EXISTS trigger_upd_cecase_via_occinspectedspace ON public.occinspectedspace;

CREATE TRIGGER trigger_upd_cecase_via_occinspectedspace
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.occinspectedspace
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_cecase_via_occinspectedspace();

COMMENT ON TRIGGER trigger_upd_cecase_via_occinspectedspace ON public.occinspectedspace
    IS 'This trigger will update the cecase user and lastupdate date when action taken on the occinspectedspace.  ';
--****************************************
-- Function: trigger_up_cecase_via_occinspectedspaceelement
CREATE OR REPLACE FUNCTION trigger_upd_cecase_via_occinspectedspaceelement()
RETURNS TRIGGER AS $$
DECLARE			
	temp_violationid integer; 		
	temp_cecase_caseid integer;	
    temp_user integer; 
 
    BEGIN			
        --			
        --Update the cecase lastupdate and user when occinspectspaceelement table has action taken on it (upd,del,insert).		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 
         IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
        END IF;
	       UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE propertyunit_unitid IN (SELECT propertyunit_unitid FROM occupancyperiod WHERE occperiod_periodid IN (SELECT occperiod_periodid FROM occinspection WHERE occperiod_periodid IN (SELECT inpectedspacedid FROM occinspectedspace WHERE inspectedspaceid  = OLD.inspectedspace_inspectedspacedid)));
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
         IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
         END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
              WHERE propertyunit_unitid IN (SELECT propertyunit_unitid FROM occupancyperiod WHERE occperiod_periodid IN (SELECT occperiod_periodid FROM occinspection WHERE occperiod_periodid IN (SELECT inpectedspacedid FROM occinspectedspace WHERE inspectedspaceid  = NEW.inspectedspace_inspectedspacedid)));
			      RETURN NEW;
        ELSIF 
        (TG_OP = 'UPDATE') THEN	
         IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
         END IF;  
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE propertyunit_unitid IN (SELECT propertyunit_unitid FROM occupancyperiod WHERE occperiod_periodid IN (SELECT occperiod_periodid FROM occinspection WHERE occperiod_periodid IN (SELECT inpectedspacedid FROM occinspectedspace WHERE inspectedspaceid  = NEW.inspectedspace_inspectedspacedid)));
			      RETURN NEW;		
     	END IF;	   
    END;  
$$  LANGUAGE plpgsql; 

-- Trigger: trigger_up_cecase_via_occinspectedspaceelement

DROP TRIGGER IF EXISTS trigger_up_cecase_via_occinspectedspaceelement ON public.occinspectedspaceelement;

CREATE TRIGGER trigger_upd_cecase_via_occinspectedspaceelement
    BEFORE INSERT OR DELETE OR UPDATE 
    ON public.occinspectedspaceelement
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_cecase_via_occinspectedspaceelement();
--****************************************
-- Function: trigger_upd_cecase_via_occinspectionphotodoc
CREATE OR REPLACE FUNCTION trigger_upd_cecase_via_occinspectionphotodoc()
RETURNS TRIGGER AS $$
DECLARE			
	temp_violationid integer; 		
	temp_cecase_caseid integer;	
        temp_user integer; 
 
    BEGIN			
        --			
        --Update the cecase lastupdate and user when occinspectionphotodoc is updated.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN
            IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
            END IF;
	        UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE propertyunit_unitid IN (SELECT propertyunit_unitid FROM occperiod WHERE periodid IN (SELECT occperiod_periodid FROM occinspection WHERE inspectionid =  OLD.inspection_inspectionid));
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE propertyunit_unitid IN (SELECT propertyunit_unitid FROM occperiod WHERE periodid IN (SELECT occperiod_periodid FROM occinspection WHERE inspectionid =  NEW.inspection_inspectionid));
			      RETURN NEW;	
             
        ELSIF 
        (TG_OP = 'UPDATE') THEN
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE propertyunit_unitid IN (SELECT propertyunit_unitid FROM occperiod WHERE periodid IN (SELECT occperiod_periodid FROM occinspection WHERE inspectionid =  NEW.inspection_inspectionid));
			      RETURN NEW;
     	END IF;	   
    END;  

$$  LANGUAGE plpgsql; 
-- Trigger: trigger_upd_cecase_via_occinspectionphotodoc

DROP TRIGGER IF EXISTS trigger_upd_cecase_via_occinspectionphotodoc ON public.occinspectionphotodoc;

CREATE TRIGGER trigger_upd_cecase_via_occinspectionphotodoc
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.occinspectionphotodoc
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_cecase_via_occinspectionphotodoc();

COMMENT ON TRIGGER trigger_upd_cecase_via_occinspectionphotodoc ON public.occinspectionphotodoc
    IS 'Update the cecase when action is taken on the occinspectionphotodoc (upd,del,insert). ';
--****************************************
--function: trigger_upd_occperiod_via_occpermit()
CREATE OR REPLACE FUNCTION trigger_upd_occperiod_via_occpermit()
RETURNS TRIGGER AS $$
DECLARE			
	temp_periodid integer; 		
    temp_user integer; 
 
    BEGIN
        -- Update the occperiod lastupdate and user when occpermit table has a updated,deleted or insert.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 	
            IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
            END IF;
	        UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = OLD.occperiod_periodid;
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  =  NEW.occperiod_periodid;
			      RETURN NEW;		
             
        ELSIF 
        (TG_OP = 'UPDATE') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = NEW.occperiod_periodid;
			      RETURN NEW;	
     	END IF;	   
    END; 

$$  LANGUAGE plpgsql;  
--Trigger: trigger_upd_occperiod_via_occpermit()

DROP TRIGGER IF EXISTS "trigger_upd_occperiod_via_occpermit()" ON public.occpermit;

CREATE TRIGGER trigger_upd_occperiod_via_occpermit
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.occpermit
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_occperiod_via_occpermit();

COMMENT ON TRIGGER trigger_upd_occperiod_via_occpermit ON public.occpermit
    IS 'Update the occperiod user and last update dt when the occpermit has action taken on it. (insert,upd,delete)';
--**********************************************************
-- Function: trigger_upd_occperiod_via_occpermitapplication
CREATE OR REPLACE FUNCTION trigger_upd_occperiod_via_occpermitapplication()
RETURNS TRIGGER AS $$
DECLARE			
	temp_periodid integer; 		
        temp_user integer; 
 
    BEGIN

        -- Update the occperiod lastupdate and user when occpermitapplication table is has action taken.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN
            IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
            END IF;
	        UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = OLD.occperiod_periodid;
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  =  NEW.occperiod_periodid;
			      RETURN NEW;		
             
        ELSIF 
        (TG_OP = 'UPDATE') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = NEW.occperiod_periodid;
			      RETURN NEW;	
     	END IF;	   
    END;  
$$  LANGUAGE plpgsql;  
-- Trigger: trigger_upd_occperiod_via_occpermitapplication

DROP TRIGGER IF EXISTS trigger_upd_occperiod_via_occpermitapplication ON public.occpermitapplication;

CREATE TRIGGER trigger_upd_occperiod_via_occpermitapplication
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.occpermitapplication
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_occperiod_via_occpermitapplication();

COMMENT ON TRIGGER trigger_upd_occperiod_via_occpermitapplication ON public.occpermitapplication
    IS 'Update the occperiod when the occpermitapplication has action taken (upd,insert,or delete).';
--**********************************************************
-- Function: trigger_upd_occperiod_via_occperiodphotodoc
CREATE FUNCTION trigger_upd_occperiod_via_occperiodphotodoc()
RETURNS TRIGGER AS $$
DECLARE			
	temp_periodid integer; 		
    temp_user integer; 
    BEGIN

        -- Update the occperiod lastupdate and user when occperiodohotodoc table is has action taken.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 
--            IF OLD.lastupdatedby_userid IS NOT null THEN
--              temp_user = OLD.lastupdated_userid;
--            END IF;
	        UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = OLD.occperiod_periodid;
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
--            IF NEW.lastupdatedby_userid IS NOT null THEN
--               temp_user = NEW.lastupdatedby_userid;
--            END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  =  NEW.occperiod_periodid;
			      RETURN NEW;		
             
        ELSIF 
        (TG_OP = 'UPDATE') THEN	
--            IF NEW.lastupdatedby_userid IS NOT null THEN
--               temp_user = NEW.lastupdatedby_userid;
--            END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = NEW.occperiod_periodid;
			      RETURN NEW;	
     	END IF;	   
    END;      
$$  LANGUAGE plpgsql;  
-- Trigger: trigger_upd_occperiod_via_occperiodphotodoc

DROP TRIGGER IF EXISTS trigger_upd_occperiod_via_occperiodphotodoc ON public.occperiodphotodoc;

CREATE TRIGGER trigger_upd_occperiod_via_occperiodphotodoc
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.occperiodphotodoc
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_occperiod_via_occperiodphotodoc();

COMMENT ON TRIGGER trigger_upd_occperiod_via_occperiodphotodoc ON public.occperiodphotodoc
    IS 'Update the occperiod when  the occperiodphotodoc table has action taken on it- (upd,del,ins).';
--**********************************************************
-- Function: trigger_upd_cecase_via_occinspection 
CREATE OR REPLACE FUNCTION trigger_upd_cecase_via_occinspection()
RETURNS TRIGGER AS $$
-- Update the cecase lastupdate and user when occinspecton table is has action taken.	
DECLARE			
	temp_periodid integer; 		
        temp_user integer; 
 
    BEGIN
	
        -- 	   RETURN(SELECT CAST ( current_user AS text ))
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN
        IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
        END IF;
	        UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = OLD.occperiod_periodid;
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN
        IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
        END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  =  NEW.occperiod_periodid;
			      RETURN NEW;		
             
        ELSIF 
        (TG_OP = 'UPDATE') THEN
        IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
        END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = NEW.occperiod_periodid;
			      RETURN NEW;	
     	END IF;	   
    END;  

$$  LANGUAGE plpgsql;  
-- Trigger: trigger_upd_cecase_via_occinspection 

-- Trigger: upd_cecase_via_occinspection

-- DROP TRIGGER IF EXISTS upd_cecase_via_occinspection ON public.occinspection;

CREATE TRIGGER upd_cecase_via_occinspection
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.occinspection
    FOR EACH ROW
    EXECUTE PROCEDURE public.upd_cecase_via_occinspection();

COMMENT ON TRIGGER upd_cecase_via_occinspection ON public.occinspection
    IS 'Update cecase via the occinspection having action taken on it.';
--**********************************************************
-- Function: trigger_upd_occperiod_via_occperiodeventrule
CREATE FUNCTION trigger_upd_occperiod_via_occperiodeventrule()
RETURNS TRIGGER AS $$
DECLARE			
	temp_periodid integer; 		
        temp_user integer; 
 
    BEGIN

        -- Update the occperiod lastupdate and user when occperiodeventrule table is has action taken.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 
            IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
            END IF;
	        UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = OLD.occperiod_periodid;
			      RETURN OLD;	
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  =  NEW.occperiod_periodid;
			      RETURN NEW;		
             
        ELSIF 
        (TG_OP = 'UPDATE') THEN
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid  = NEW.occperiod_periodid;
			      RETURN NEW;	
     	END IF;	   
    END;  

$$  LANGUAGE plpgsql;  
-- Trigger: trigger_upd_occperiod_via_occperiodeventrule

DROP TRIGGER IF EXISTS trigger_upd_occperiod_via_occperiodeventrule ON public.occperiodeventrule;

CREATE TRIGGER trigger_upd_occperiod_via_occperiodeventrule
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.occperiodeventrule
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_occperiod_via_occperiodeventrule();

COMMENT ON TRIGGER trigger_upd_occperiod_via_occperiodeventrule ON public.occperiodeventrule
    IS 'Update the occperiod when the occperiodeventrule table has an action taken on it. (insert,upd,delete)';

--**********************************************************
-- Function- triggger_upd_occperiod_via_event    
CREATE OR REPLACE FUNCTION trigger_upd_occperiod_via_event()
RETURNS TRIGGER AS $$
DECLARE			
	temp_periodid integer; 		
        temp_user integer; 
		
    BEGIN	
 
        --Update the  occperiod lastupdate and user in occperiod when event table is updated.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
            END IF;
	       UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE  periodid = OLD.occperiod_periodid;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
              WHERE  periodid = NEW.occperiod_periodid;
				   RETURN NEW; 
				    
        ELSEIF 
        (TG_OP = 'UPDATE') THEN
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE occperiod SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE periodid = NEW.occperiod_periodid;
				   RETURN NEW;
        END IF;
     END;

$$  LANGUAGE plpgsql;  

-- Trigger: triggger_upd_occperiod_via_event

DROP TRIGGER IF EXISTS triggger_upd_occperiod_via_event ON public.event;

CREATE TRIGGER triggger_upd_occperiod_via_event
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.event
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_occperiod_via_event();

COMMENT ON TRIGGER triggger_upd_occperiod_via_event ON public.event
    IS 'This trigger will update the event table when occperiod has action take on it (insert,upd,delete).';

--**********************************************************
-- Function: trigger_upd_parcel_via_parcelinfo
CREATE OR REPLACE FUNCTION trigger_upd_parcel_via_parcelinfo()
RETURNS TRIGGER AS $$
DECLARE					
	temp_parcelkey integer;	
        temp_user integer; 

    			
    BEGIN			
        --			
        --Update the parcel lastupdate and user when parcelinfo table is has action taken.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
            END IF;
	       UPDATE parcel SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE parcelkey = OLD.parcel_parcelkey;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
            END IF;
            UPDATE parcel SET lastupdatedts = now(),lastupdatedby_userid = temp_user 
            WHERE parcelkey = NEW.parcel_parcelkey;
				   RETURN NEW;
        ELSIF (TG_OP = 'UPDATE') THEN
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
           END IF;
           UPDATE parcel SET lastupdatedts = now(),lastupdatedby_userid = temp_user 
            WHERE parcelkey = NEW.parcel_parcelkey;
				   RETURN NEW;
        END IF;	   
    END;  

$$  LANGUAGE plpgsql;      

-- Trigger: trigger_upd_parcel_via_parcelinfo

DROP TRIGGER IF EXISTS trigger_upd_parcel_via_parcelinfo ON public.parcelinfo;

CREATE TRIGGER trigger_upd_parcel_via_parcelinfo
    BEFORE INSERT OR DELETE OR UPDATE OF parcelinfoid, parcel_parcelkey, usegroup, constructiontype, countycode, notes, ownercode, propclass, locationdescription, bobsource_sourceid, unfitdatestart, unfitdatestop, unfitby_userid, abandoneddatestart, abandoneddatestop, abandonedby_userid, vacantdatestart, vacantdatestop, vacantby_userid, condition_intensityclassid, landbankprospect_intensityclassid, landbankheld, nonaddressable, usetype_typeid, createdts
    ON public.parcelinfo
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_parcel_via_parcelinfo();

COMMENT ON TRIGGER trigger_upd_parcel_via_parcelinfo ON public.parcelinfo
    IS 'This trigger will call function to update parcel if action taken on parcelinfo.  ';

--**********************************************************
-- Function: trigger_upd_parcel_via_parcelunit
CREATE OR REPLACE FUNCTION trigger_upd_parcel_via_parcelunit()
RETURNS TRIGGER AS $$
DECLARE					
    temp_parcelkey integer;	
    temp_user integer; 
    			
    BEGIN			
        --			
        --Update the parcel lastupdate and user when parcelunit table is has action taken.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
            IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
            END IF;
	       UPDATE parcel SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE parcelkey = OLD.parcel_parcelkey;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
            END IF;
            UPDATE parcel SET lastupdatedts = now(),lastupdatedby_userid = temp_user 
            WHERE parcelkey = NEW.parcel_parcelkey;
				   RETURN NEW;
        ELSIF (TG_OP = 'UPDATE') THEN
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
           END IF;
           UPDATE parcel SET lastupdatedts = now(),lastupdatedby_userid = temp_user 
            WHERE parcelkey = NEW.parcel_parcelkey;
				   RETURN NEW;
        END IF;	   
    END;  

 $$  LANGUAGE plpgsql;   

-- Trigger: trigger_upd_parcel_via_parcelunit

DROP TRIGGER IF EXISTS trigger_upd_parcel_via_parcelunit ON public.parcelunit;

CREATE TRIGGER trigger_upd_parcel_via_parcelunit
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.parcelunit
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_parcel_via_parcelunit();

COMMENT ON TRIGGER trigger_upd_parcel_via_parcelunit ON public.parcelunit
    IS 'Update the parcel when the parcelunit table has action taken on it (update,delete,or insert). ';
--********************************************************** 
-- Function: trigger_upd_human_via_humanparcel 
CREATE OR REPLACE FUNCTION trigger_upd_human_via_humanparcel()
RETURNS TRIGGER AS $$ 
DECLARE					
	temp_humanid integer;	
        temp_user integer; 

    			
    BEGIN			
        --			
        --Update the human lastupdate and user when humanoccperiod table is has action taken.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
           END IF;
	       UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = OLD.lastupdatedby_userid
           WHERE humanid = OLD.human_humanid;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
            END IF;
            UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = NEW.lastupdatedby_userid 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        ELSIF (TG_OP = 'UPDATE') THEN
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
           END IF;
           UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = NEW.lastupdatedby_userid 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        END IF;	   
    END; 
 
$$  LANGUAGE plpgsql;   
-- Trigger: trigger_upd_human_via_humanparcel

DROP TRIGGER IF EXISTS trigger_upd_human_via_humanparcel ON public.humanparcel;

CREATE TRIGGER trigger_upd_human_via_humanparcel
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.humanparcel
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_human_via_humanparcel();

COMMENT ON TRIGGER trigger_upd_human_via_humanparcel ON public.humanparcel
    IS 'update the human table user and lastupd date when action is taken on humanparcel. (add,upd,del)';     
--********************************************************** 
CREATE OR REPLACE FUNCTION trigger_upd_parcel_via_parcelmailingaddress()
RETURNS TRIGGER AS $$ 
DECLARE			
	temp_parcelkey integer; 		
        temp_user integer; 
 
    BEGIN			
        --			
        --Update the parcel lastupdate and user when parcelmailingaddress table has action taken add,delete, or update.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
           END IF;
	       UPDATE parcel SET lastupdatedts = now(),lastupdatedby_userid = OLD.lastupdatedby_userid
            WHERE parcelkey = OLD.parcel_parcelkey;
			      RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE parcel SET lastupdatedts = now(),lastupdatedby_userid = OLD.lastupdatedby_userid
            WHERE parcelkey = NEW.parcel_parcelkey;
			      RETURN NEW;
                   
        ELSIF 
        (TG_OP = 'UPDATE') THEN	
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
           END IF;
           UPDATE parcel SET lastupdatedts = now(),lastupdatedby_userid = OLD.lastupdatedby_userid
            WHERE parcelkey = NEW.parcel_parcelkey;
			      RETURN NEW;
        END IF;	   
    END; 

$$  LANGUAGE plpgsql;
 -- Trigger: trigger_upd_parcel_via_ parcelmailingaddress

DROP TRIGGER IF EXISTS "trigger_upd_parcel_via_ parcelmailingaddress" ON public.parcelmailingaddress;

CREATE TRIGGER "trigger_upd_parcel_via_ parcelmailingaddress"
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.parcelmailingaddress
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_parcel_via_parcelmailingaddress();

COMMENT ON TRIGGER "trigger_upd_parcel_via_ parcelmailingaddress" ON public.parcelmailingaddress
    IS 'Update the parcel user and last upd date when the parcelamailingaddress table is updated';
--********************************************************** 
-- Function: trigger_upd_parcel_via_cecase
CREATE OR REPLACE FUNCTION trigger_upd_parcel_via_cecase()
RETURNS TRIGGER AS $$ 
DECLARE			
	temp_violationid integer; 		
	temp_cecase_caseid integer;	
         temp_user integer;   
			
    BEGIN			
        --			
        --Update the parcel lastupdate and user when cecase record is update by (ins,del, or upd)		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
        IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
        END IF;   
	    UPDATE parcel SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE parcelkey = OLD.parcel_parcelkey;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
           END IF;
           UPDATE parcel SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE parcelkey = NEW.parcel_parcelkey;
				   RETURN NEW;
        ELSEIF 
        (TG_OP = 'UPDATE') THEN	
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
           END IF;
           UPDATE parcel SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE parcelkey = NEW.parcel_parcelkey;
				   RETURN NEW;          
        END IF;	  
    END; 

$$  LANGUAGE plpgsql;
-- Trigger: trigger_upd_parcel_via_cecase

DROP TRIGGER IF EXISTS trigger_upd_parcel_via_cecase ON public.cecase;

CREATE TRIGGER trigger_upd_parcel_via_cecase
    BEFORE INSERT OR DELETE OR UPDATE 
    ON public.cecase
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_parcel_via_cecase();
--**********************************************************
-- Occperiod to update parcel  test and Add in
--**********************************************************  
-- function: trigger_upd_human_via_contactemail
CREATE OR REPLACE FUNCTION trigger_upd_human_via_contactemail()
RETURNS TRIGGER AS $$   
DECLARE					
	temp_humanid integer;	
    temp_user integer; 

    			
    BEGIN			
        --			
        --Update the human lastupdate and user when contactemail table is has action taken.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
           END IF;
	       UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = OLD.lastupdatedby_userid
           WHERE humanid = OLD.human_humanid;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
            END IF;
            UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = NEW.lastupdatedby_userid 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        ELSIF (TG_OP = 'UPDATE') THEN
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
           END IF;
           UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = NEW.lastupdatedby_userid 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        END IF;	   
    END;  

$$  LANGUAGE plpgsql;
-- Trigger: trigger_upd_human_via_contactemail

DROP TRIGGER IF EXISTS trigger_upd_human_via_contactemail ON public.contactemail;

CREATE TRIGGER trigger_upd_human_via_contactemail
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.contactemail
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_human_via_contactemail();
 --**********************************************************       
-- Function: trigger_upd_human_via_contactphone
CREATE OR REPLACE FUNCTION trigger_upd_human_via_contactphone()
RETURNS TRIGGER AS $$   
DECLARE					
    temp_humanid integer;	
    temp_user integer; 

    			
    BEGIN			
        --			
        --Update the human lastupdate and user when human table is has action taken on contactphone table.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
           END IF;
	       UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = OLD.lastupdatedby_userid
           WHERE humanid = OLD.human_humanid;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
            END IF;
            UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = NEW.lastupdatedby_userid 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        ELSIF (TG_OP = 'UPDATE') THEN
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
           END IF;
           UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = NEW.lastupdatedby_userid 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        END IF;	   
    END;
$$  LANGUAGE plpgsql;  
-- Trigger: trigger_upd_human_via_contactphone

DROP TRIGGER IF EXISTS trigger_upd_human_via_contactphone ON public.contactphone;

CREATE TRIGGER trigger_upd_human_via_contactphone
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.contactphone
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_human_via_contactphone();

COMMENT ON TRIGGER trigger_upd_human_via_contactphone ON public.contactphone
    IS 'This function will update the human table if the human contactphone table is updated. ';
--**********************************************************  
-- Function: trigger_upd_human_via_humanuni 
CREATE OR REPLACE FUNCTION trigger_upd_human_via_humanuni()
RETURNS TRIGGER AS $$  
DECLARE					
    temp_humanid integer;	
    temp_user integer; 

    			
    BEGIN			
        --			
        --Update the human lastupdate and user when humanuni table is has action taken.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
            END IF;
	       UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE humanid = OLD.human_humanid;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
            END IF;
            UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = temp_user 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        ELSIF (TG_OP = 'UPDATE') THEN
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
           END IF;
           UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = temp_user 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        END IF;	   
    END;  

$$  LANGUAGE plpgsql; 

-- Trigger: trigger_upd_human_via_humanuni

DROP TRIGGER IF EXISTS trigger_upd_human_via_humanuni ON public.humanmuni;

CREATE TRIGGER trigger_upd_human_via_humanuni
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.humanmuni
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_human_via_humanuni();

COMMENT ON TRIGGER trigger_upd_human_via_humanuni ON public.humanmuni
    IS 'This trigger will update the human table userid and last update date when action taken on the humanuni table (insert,upd,delete)';   
--********************************************************** 
-- Function: trigger_upd_human_via_humancecase
CREATE OR REPLACE FUNCTION trigger_upd_human_via_humancecase()
RETURNS TRIGGER AS $$  
DECLARE					
	temp_humanid integer;	
        temp_user integer; 

    			
    BEGIN			
        --			
        --Update the human lastupdate and user when humancase table is has action taken.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
           END IF;
	       UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE humanid = OLD.human_humanid;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = temp_user 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        ELSIF (TG_OP = 'UPDATE') THEN
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
           END IF;
           UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = temp_user
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        END IF;	   
    END;  
   
$$  LANGUAGE plpgsql;  

-- Trigger: trigger_upd_human_via_humancecase

DROP TRIGGER IF EXISTS trigger_upd_human_via_humancecase ON public.humancecase;

CREATE TRIGGER trigger_upd_human_via_humancecase
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.humancecase
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_human_via_humancecase();

--********************************************************** 
CREATE OR REPLACE FUNCTION trigger_upd_human_via_humanparcel()
RETURNS TRIGGER AS $$  
DECLARE					
	temp_humanid integer;	
        temp_user integer; 

    			
    BEGIN			
        --			
        --Update the human lastupdate and user when humanoccperiod table is has action taken.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
           END IF;
	       UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = OLD.lastupdatedby_userid
           WHERE humanid = OLD.human_humanid;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
            END IF;
            UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = NEW.lastupdatedby_userid 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        ELSIF (TG_OP = 'UPDATE') THEN
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdatedby_userid;
           END IF;
           UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = NEW.lastupdatedby_userid 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        END IF;	   
    END; 
 
 $$  LANGUAGE plpgsql;    
    
-- Trigger: trigger_upd_human_via_humanparcel

DROP TRIGGER IF EXISTS trigger_upd_human_via_humanparcel ON public.humanparcel;

CREATE TRIGGER trigger_upd_human_via_humanparcel
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.humanparcel
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_human_via_humanparcel();

COMMENT ON TRIGGER trigger_upd_human_via_humanparcel ON public.humanparcel
    IS 'update the human table user and lastupd date when action is taken on humanparcel. (add,upd,del)';

--********************************************************** 
-- Function: trigger_upd_human_via_humanoccperiod
CREATE OR REPLACE FUNCTION trigger_upd_human_via_humanoccperiod()
RETURNS TRIGGER AS $$  
DECLARE					
	temp_humanid integer;	
        temp_user integer; 

    			
    BEGIN			
        --			
        --Update the human lastupdate and user when humanoccperiod table is has action taken.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
           END IF;
	       UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE humanid = OLD.human_humanid;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
            END IF;
            UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = temp_user 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        ELSIF (TG_OP = 'UPDATE') THEN
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
           END IF;
           UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = temp_user 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
 $$  LANGUAGE plpgsql; 
-- Trigger: trigger_upd_human_via_humanoccperiod

DROP TRIGGER IF EXISTS trigger_upd_human_via_humanoccperiod ON public.humanoccperiod;

CREATE TRIGGER trigger_upd_human_via_humanoccperiod
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.humanoccperiod
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_human_via_humanoccperiod();

COMMENT ON TRIGGER trigger_upd_human_via_humanoccperiod ON public.humanoccperiod
    IS 'Update the human table based on humanoccperiod. ';
--********************************************************** 
-- Function: trigger_upd_human_via_humanparcelunit
CREATE OR REPLACE FUNCTION trigger_upd_human_via_humanparcelunit()
RETURNS TRIGGER AS $$  
DECLARE					
    temp_humanid integer;	
    temp_user integer; 

    			
    BEGIN			
        --			
        --Update the human lastupdate and user when humanparcelunit table is has action taken.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
            END IF;
	       UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = OLD.lastupdatedby_userid
           WHERE humanid = OLD.human_humanid;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
            IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
            END IF;
            UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = NEW.lastupdatedby_userid 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        ELSIF (TG_OP = 'UPDATE') THEN
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdatedby_userid;
            END IF;
           UPDATE human SET lastupdatedts = now(),lastupdatedby_userid = NEW.lastupdatedby_userid 
            WHERE humanid = NEW.human_humanid;
				   RETURN NEW;
        END IF;	   
    END;  
  
 $$  LANGUAGE plpgsql;
-- Trigger: trigger_upd_human_via_humanparcelunit

DROP TRIGGER IF EXISTS trigger_upd_human_via_humanparcelunit ON public.humanparcelunit;

CREATE TRIGGER trigger_upd_human_via_humanparcelunit
    AFTER INSERT OR DELETE OR UPDATE 
    ON public.humanparcelunit
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_human_via_humanparcelunit();

COMMENT ON TRIGGER trigger_upd_human_via_humanparcelunit ON public.humanparcelunit
    IS 'This function updates the human table userid and lastupdate dt when action is taken on humanparcelunit table (insert,del,upd). '; 
--**********************************************************   
-- Function: trigger_upd_cecase_via_event
CREATE OR REPLACE FUNCTION trigger_upd_cecase_via_event()
RETURNS TRIGGER AS $$  

  DECLARE			
	temp_periodid integer; 		
        temp_user integer; 
		
    BEGIN	
 
        --Update the  lastupdate and user in cecase when event table is updated the field caseid or periodid.		
        -- 	   RETURN(SELECT CAST ( current_user AS text ));
        temp_user = 99;
        IF (TG_OP = 'DELETE') THEN 			
           IF OLD.lastupdatedby_userid IS NOT null THEN
               temp_user = OLD.lastupdated_userid;
        END IF;
	       UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE  caseid = OLD.cecase_caseid;
				   RETURN OLD;
        ELSEIF 			
        (TG_OP = 'INSERT') THEN	
           IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
           END IF;
           UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
           WHERE  caseid = NEW.cecase_caseid;
				   RETURN NEW;
        ELSEIF 			  
				    
         (TG_OP = 'UPDATE') THEN
            IF NEW.lastupdatedby_userid IS NOT null THEN
               temp_user = NEW.lastupdated_userid;
	    END IF;
            UPDATE cecase SET lastupdatedts = now(),lastupdatedby_userid = temp_user
             WHERE  caseid = NEW.cecase_caseid;
				   RETURN NEW; 			
        END IF;
     END;

$$  LANGUAGE plpgsql;

-- Trigger: trigger_upd_cecase_via_event

DROP TRIGGER IF EXISTS trigger_upd_cecase_via_event ON public.event;

CREATE TRIGGER trigger_upd_cecase_via_event
    AFTER INSERT OR DELETE OR UPDATE OF cecase_caseid, occperiod_periodid
    ON public.event
    FOR EACH ROW
    EXECUTE PROCEDURE public.trigger_upd_cecase_via_event();

COMMENT ON TRIGGER trigger_upd_cecase_via_event ON public.event
    IS 'This trigger update the cecase user and upd date when action is taken on the event table.';
--**********************************************************

   



          

  