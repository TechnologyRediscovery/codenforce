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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class ViolationAddBB extends BackingBeanUtils implements Serializable {
    
    private CodeViolation currentViolation;
    private Date dateOfRecord;
    private Date stipulatedComplianceDate;
    private double penalty;
    private String description;
    private String notes;
    
   
    
    /**
     * Creates a new instance of ViolationAdd
     */
    public ViolationAddBB() {
    }
    
    public String addViolation(){
        
        ViolationCoordinator vc = getViolationCoordinator();
        
        
        currentViolation.setStipulatedComplianceDate(getStipulatedComplianceDate()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        currentViolation.setDateOfRecord(getDateOfRecord()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        currentViolation.setPenalty(penalty);
        currentViolation.setDescription(description);
        currentViolation.setNotes(notes);
        
        try {
             vc.addNewCodeViolation(currentViolation);
             // if update succeds without throwing an error, then proceed to
             // giving the event coordinator info for an update event
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Success! Violation added.", ""));
            return "caseViolations";
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to edit violation.", 
                                "This is a system-level error that msut be corrected by an administrator, Sorry!"));
            
        } catch (ViolationException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            ex.getMessage(), "Stipulated compliance date must be in the future; please revise the stipulated compliance date."));
        }
        return "";
        
    }
    

   

    /**
     * @return the penalty
     */
    public double getPenalty() {
        penalty = currentViolation.getCodeViolated().getNormPenalty();
        return penalty;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
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
     * @return the currentViolation
     */
    public CodeViolation getCurrentViolation() {
        
        currentViolation = getSessionBean().getActiveCodeViolation();
        return currentViolation;
    }

    /**
     * @param currentViolation the currentViolation to set
     */
    public void setCurrentViolation(CodeViolation currentViolation) {
        this.currentViolation = currentViolation;
    }

    /**
     * @return the stipulatedComplianceDate
     */
    public Date getStipulatedComplianceDate() {
        stipulatedComplianceDate = java.util.Date.from(
                currentViolation.getStipulatedComplianceDate()
                        .atZone(ZoneId.systemDefault()).toInstant());
        return stipulatedComplianceDate;
    }

    /**
     * @param stipulatedComplianceDate the stipulatedComplianceDate to set
     */
    public void setStipulatedComplianceDate(Date stipulatedComplianceDate) {
        this.stipulatedComplianceDate = stipulatedComplianceDate;
    }

    /**
     * @return the dateOfRecord
     */
    public Date getDateOfRecord() {
        dateOfRecord = java.util.Date.from(
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
        return dateOfRecord;
    }

    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(Date dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }

  
    
}
