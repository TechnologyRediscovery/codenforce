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


import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.Photograph;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import org.primefaces.event.FileUploadEvent;

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
    private List<Photograph> photoList;
    private Photograph selectedPhoto;
    
    /**
     * Creates a new instance of ViolationAdd
     */
    public ViolationAddBB() {
        
    }
    
    @PostConstruct
    public void initBean(){
        currentViolation = getSessionBean().getActiveCodeViolation();
    }
    
    public void updateDescription(Photograph photo){
        ImageServices is = getImageServices();
        try {
            is.updatePhotoDescription(photo);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
    }
    
    public void handlePhotoUpload(FileUploadEvent ev){
        if(this.currentViolation == null){
            this.currentViolation = getSessionBean().getActiveCodeViolation();
        }
        if(ev == null){
            System.out.println("ViolationAddBB.handlePhotoUpload | event: null");
            return;
        }
        if(this.currentViolation.getPhotoList() == null){
            this.currentViolation.setPhotoList(new ArrayList<Integer>());
        }
        if(this.photoList == null){
            this.photoList = new ArrayList<>();
        }
        
        ImageServices is = getImageServices();
        Photograph ph = new Photograph();
        ph.setPhotoBytes(ev.getFile().getContents());
        ph.setDescription("no description");
        ph.setTypeID(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("photoTypeId")));
        ph.setTimeStamp(LocalDateTime.now());
        
        try {
            this.currentViolation.getPhotoList().add(is.storePhotograph(ph));
        } catch (IntegrationException ex) {
            System.out.println("ViolationAddBB.handlePhotoUpload | upload failed!\n" + ex);
            return;
        }
        this.getPhotoList().add(ph);
    }
    
    public String addViolation(){
        
        
        CaseCoordinator cc = getCaseCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        
        currentViolation.setStipulatedComplianceDate(getStipulatedComplianceDate()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        currentViolation.setDateOfRecord(getDateOfRecord()
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        currentViolation.setPenalty(penalty);
        currentViolation.setDescription(description);
        currentViolation.setNotes(notes);
       
        
        try {
             cc.attachViolationToCaseAndInsertTimeFrameEvent(currentViolation, 
                     getSessionBean().getcECaseQueue().get(0));
             getSessionBean().setSessionCECase(ci.getCECase(currentViolation.getCeCaseID()));
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Success! Violation added.", ""));
            return "ceCases";
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to edit violation.", 
                                "This is a system-level error that msut be corrected by an administrator, Sorry!"));
            
        } catch (ViolationException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), "Stipulated compliance date must "
                                + "be in the future; please revise the stipulated compliance date."));
        } catch (CaseLifecyleException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), "To preserve data integrity, this "
                                + "case's phase restrictions forbid attaching new code violations."));
        }
        return "";
        
    }
    
    public String addViolationWithPhotos(){
        CaseIntegrator ci = getCaseIntegrator();
        CaseCoordinator cc = getCaseCoordinator();
        
        currentViolation.setStipulatedComplianceDate(stipulatedComplianceDate
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        currentViolation.setDateOfRecord(dateOfRecord
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
        currentViolation.setPenalty(penalty);
        currentViolation.setDescription(description);
        currentViolation.setNotes(notes);
        
        
        try {
             currentViolation.setViolationID(cc.attachViolationToCaseAndInsertTimeFrameEvent(currentViolation, getSessionBean().getSessionCECase()));
             getSessionBean().setActiveCodeViolation(currentViolation);
             getSessionBean().setSessionCECase(ci.getCECase(currentViolation.getCeCaseID()));
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Success! Violation added.", ""));
            return "violationPhotos";
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to edit violation.", 
                                "This is a system-level error that must be corrected by an administrator, Sorry!"));
            
        } catch (ViolationException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            ex.getMessage(), "Stipulated compliance date must be in the future; please revise the stipulated compliance date."));
        } catch (CaseLifecyleException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), "To preserve data integrity, this "
                                + "case's phase restrictions forbid attaching new code violations."));
        }
        return "";
        
    }
    
    public String photosConfirm(){
        if(this.currentViolation == null){
            this.currentViolation = getSessionBean().getActiveCodeViolation();
        }
        if(this.getPhotoList() == null  ||  this.getPhotoList().isEmpty()){
            getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "No uploaded photos to commit.", 
                                "Use the 'Return to case home without commiting photos' button bellow if you have no photos to upload."));
            return "";
        }
        
        ImageServices is = getImageServices();
        
        for(Photograph photo : this.getPhotoList()){
            
            try { 
                // commit and link
                is.commitPhotograph(photo.getPhotoID());
                is.linkPhotoToCodeViolation(photo.getPhotoID(), currentViolation.getViolationID());
                
            } catch (IntegrationException ex) {
                System.out.println(ex.toString());
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                                "INTEGRATION ERROR: Unable write request into the database, our apologies!", 
                                "Please call your municipal office and report your concern by phone."));
                    return "";
            }
        }
        return "ceHome";
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

    /**
     * @return the photoList
     */
    public List<Photograph> getPhotoList() {
        return photoList;
    }

    /**
     * @param photoList the photoList to set
     */
    public void setPhotoList(List<Photograph> photoList) {
        this.photoList = photoList;
    }

    /**
     * @return the selectedPhoto
     */
    public Photograph getSelectedPhoto() {
        return selectedPhoto;
    }

    /**
     * @param selectedPhoto the selectedPhoto to set
     */
    public void setSelectedPhoto(Photograph selectedPhoto) {
        this.selectedPhoto = selectedPhoto;
    }

  
    
}
