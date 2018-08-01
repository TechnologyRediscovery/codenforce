/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.ViolationCoordinator;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EventCase;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.Date;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class ViolationEditBB extends BackingBeanUtils implements Serializable{

    
    private CodeViolation currentViolation;
    private Date dateOfRecord;
    private Date stipulatedComplianceDate;
    private double penalty;
    private String description;
    private String notes;
    
    // violation update event fields
    private boolean formDiscloseToMuni;
    private boolean formDiscloseToPublic;
    private String formEventNotes;
    
    /**
     * Creates a new instance of ViolationEditBB
     */
    public ViolationEditBB() {
    }
    
    public String editViolation(){
       ViolationCoordinator violationCoordinator = getViolationCoordinator();
       
       EventCoordinator eventCoordinator = getEventCoordinator();
       currentViolation = getSessionBean().getActiveCodeViolation();
       CECase ceCase = getSessionBean().getActiveCase();
       EventCase event = new EventCase();
        
        currentViolation.setStipulatedComplianceDate(getStipulatedComplianceDate()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        currentViolation.setDateOfRecord(getDateOfRecord()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        currentViolation.setPenalty(penalty);
        currentViolation.setDescription(description);
        currentViolation.setNotes(notes);
        
        // load up edit event data
        event.setNotes(getFormEventNotes());
        event.setDiscloseToMunicipality(formDiscloseToMuni);
        event.setDiscloseToPublic(formDiscloseToPublic);
        
        try {
             violationCoordinator.updateCodeViolation(currentViolation);
             
             // if update succeeds without throwing an error, then generate an
             // update violation event
             eventCoordinator.generateAndInsertCodeViolationUpdateEvent(ceCase, currentViolation, event);

             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Success! Violation updated and notice event generated", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to edit violation in the database", 
                                "This is a system-level error that msut be corrected by an administrator, Sorry!"));
            
        } catch (ViolationException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            ex.getMessage(), "Please revise the stipulated compliance date"));
             
        } catch (EventException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), "Unable to generate automated event to log violation update"));
        }
        
            return "caseViolations";
    }

    /**
     * @return the currentViolation
     */
    public CodeViolation getCurrentViolation() {
       
       currentViolation = getSessionBean().getActiveCodeViolation();
        return currentViolation;
    }
    
    public String backToCaseManager(){
        return "caseProfile";
    }

    /**
     * @return the dateOfRecord
     */
    public Date getDateOfRecord() {
        dateOfRecord = Date.from(currentViolation
                .getDateOfRecord().atZone(ZoneId.systemDefault()).toInstant());
        return dateOfRecord;
    }

    /**
     * @return the stipulatedComplianceDate
     */
    public Date getStipulatedComplianceDate() {
        stipulatedComplianceDate = Date.from(currentViolation
                .getStipulatedComplianceDate().atZone(ZoneId.systemDefault()).toInstant());
        return stipulatedComplianceDate;
    }

    /**
     * @return the penalty
     */
    public double getPenalty() {
        penalty = currentViolation.getPenalty();
        return penalty;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        description = currentViolation.getDescription();
        return description;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        notes = currentViolation.getNotes();
        return notes;
    }

   

    /**
     * @param currentViolation the currentViolation to set
     */
    public void setCurrentViolation(CodeViolation currentViolation) {
        this.currentViolation = currentViolation;
    }

    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(Date dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }

    /**
     * @param stipulatedComplianceDate the stipulatedComplianceDate to set
     */
    public void setStipulatedComplianceDate(Date stipulatedComplianceDate) {
        this.stipulatedComplianceDate = stipulatedComplianceDate;
    }

    /**
     * @param penalty the penalty to set
     */
    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    

    /**
     * @return the formDiscloseToMuni
     */
    public boolean isFormDiscloseToMuni() {
        return formDiscloseToMuni;
    }

    /**
     * @return the formDiscloseToPublic
     */
    public boolean isFormDiscloseToPublic() {
        return formDiscloseToPublic;
    }


    /**
     * @param formDiscloseToMuni the formDiscloseToMuni to set
     */
    public void setFormDiscloseToMuni(boolean formDiscloseToMuni) {
        this.formDiscloseToMuni = formDiscloseToMuni;
    }

    /**
     * @param formDiscloseToPublic the formDiscloseToPublic to set
     */
    public void setFormDiscloseToPublic(boolean formDiscloseToPublic) {
        this.formDiscloseToPublic = formDiscloseToPublic;
    }

    /**
     * @return the formEventNotes
     */
    public String getFormEventNotes() {
        return formEventNotes;
    }

    /**
     * @param formEventNotes the formEventNotes to set
     */
    public void setFormEventNotes(String formEventNotes) {
        this.formEventNotes = formEventNotes;
    }
    
}
