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



import com.tcvcog.tcvce.coordinators.BlobCoordinator;
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.integration.BlobIntegrator;
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
    private CECase currentCase;
    private Date dateOfRecord;
    private Date stipulatedComplianceDate;
    private double penalty;
    private String description;
    private String notes;
    private List<Blob> blobList;
    private Blob selectedBlob;
    
    /**
     * Creates a new instance of ViolationAdd
     */
    public ViolationAddBB() {
        
    }
    
    @PostConstruct
    public void initBean(){
        currentViolation = getSessionBean().getSessionCodeViolation();
        currentCase = getSessionBean().getSessionCECase();
    }
    
    public void handlePhotoUpload(FileUploadEvent ev){
        if(this.currentViolation == null){
            this.currentViolation = getSessionBean().getSessionCodeViolation();
        }
        if(ev == null){
            System.out.println("ViolationAddBB.handlePhotoUpload | event: null");
            return;
        }
        if(this.currentViolation.getBlobIDList() == null){
            this.currentViolation.setBlobIDList(new ArrayList<Integer>());
        }
        if(this.blobList == null){
            this.blobList = new ArrayList<>();
        }
        
        BlobIntegrator blobi = getBlobIntegrator();
        Blob blob = getBlobCoordinator().getNewBlob();
        blob.setBytes(ev.getFile().getContents());
        blob.setType(BlobType.PHOTO); // TODO: extract type from context somehow
        
        try {
            this.currentViolation.getBlobIDList().add(blobi.storeBlob(blob));
        } catch (IntegrationException ex) {
            System.out.println("ViolationAddBB.handlePhotoUpload | upload failed!\n" + ex);
            return;
        } catch (BlobException ex) {
            System.out.println(ex);
            return;
        }
        this.getBlobList().add(blob);
    }
    
    public String addViolation(){
        
        
        CaseCoordinator cc = getCaseCoordinator();
        
        try {
             cc.attachViolationToCaseAndInsertTimeFrameEvent(currentViolation, currentCase);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Success! Violation added.", ""));
             getSessionBean().getSessionBean().setSessionCECase(currentCase);
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
        } catch (CaseLifecycleException ex) {
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
        
        try {
             currentViolation.setViolationID(cc.attachViolationToCaseAndInsertTimeFrameEvent(currentViolation, currentCase));
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
        } catch (CaseLifecycleException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), "To preserve data integrity, this "
                                + "case's phase restrictions forbid attaching new code violations."));
        }
        return "";
        
    }
    
    public String photosConfirm(){
        /*  TODO: this obviously
        
        if(this.currentViolation == null){
            this.currentViolation = getSessionBean().getSessionCodeViolation();
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
        */
        return "ceHome";
    }
   


    /**
     * @return the currentViolation
     */
    public CodeViolation getCurrentViolation() {
        
        currentViolation = getSessionBean().getSessionCodeViolation();
        return currentViolation;
    }

    /**
     * @param currentViolation the currentViolation to set
     */
    public void setCurrentViolation(CodeViolation currentViolation) {
        this.currentViolation = currentViolation;
    }

    /**
     * @return the photoList
     */
    public List<Blob> getBlobList() {
        return this.blobList;
    }

    /**
     * @param blobList
     */
    public void setBlobList(List<Blob> blobList) {
        this.blobList = blobList;
    }

    /**
     * @return the selectedBlob
     */
    public Blob getSelectedPhoto() {
        return getSelectedBlob();
    }

    /**
     * @param selectedBlob
     */
    public void setSelectedPhoto(Blob selectedBlob) {
        this.setSelectedBlob(selectedBlob);
    }

    /**
     * @return the currentCase
     */
    public CECase getCurrentCase() {
        return currentCase;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECase currentCase) {
        this.currentCase = currentCase;
    }

    /**
     * @return the penalty
     */
    public double getPenalty() {
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
     * @return the selectedBlob
     */
    public Blob getSelectedBlob() {
        return selectedBlob;
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
     * @param selectedBlob the selectedBlob to set
     */
    public void setSelectedBlob(Blob selectedBlob) {
        this.selectedBlob = selectedBlob;
    }

    /**
     * @return the dateOfRecord
     */
    public Date getDateOfRecord() {
        return dateOfRecord;
    }

    /**
     * @return the stipulatedComplianceDate
     */
    public Date getStipulatedComplianceDate() {
        return stipulatedComplianceDate;
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

  
    
}
