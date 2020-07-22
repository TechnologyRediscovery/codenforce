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
import com.tcvcog.tcvce.domain.BlobException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.Blob;
import com.tcvcog.tcvce.entities.BlobType;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CodeViolation;
//import com.tcvcog.tcvce.entities.Photograph;
import com.tcvcog.tcvce.integration.BlobIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import org.primefaces.event.FileUploadEvent;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class ViolationAddBB extends BackingBeanUtils implements Serializable {
    
    private CodeViolation currentViolation;
    private CECaseDataHeavy currentCase;
    private List<Blob> blobList;
    
    /**
     * Creates a new instance of ViolationAdd
     */
    public ViolationAddBB() {
        
    }
    
    @PostConstruct
    public void initBean(){
        currentViolation = getSessionBean().getSessCodeViolation();
        currentCase = getSessionBean().getSessCECase();
    }
    
    public void handlePhotoUpload(FileUploadEvent ev){
        if(this.currentViolation == null){
            this.currentViolation = getSessionBean().getSessCodeViolation();
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
             cc.attachViolationToCaseAndInsertTimeFrameEvent(currentViolation, currentCase, getSessionBean().getSessUser());
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Success! Violation added.", ""));
             getSessionBean().getSessionBean().setSessCECase(currentCase);
            return "ceCases";
        } catch (IntegrationException | SearchException ex) {
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
        } catch (BObStatusException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), "To preserve data integrity, this "
                                + "case's phase restrictions forbid attaching new code violations."));
        } catch (EventException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), "Violation event exception"));
        }
        return "";
        
    }
    
    public String addViolationWithPhotos(){
        CaseIntegrator ci = getCaseIntegrator();
        CaseCoordinator cc = getCaseCoordinator();
        
        try {
             currentViolation.setViolationID(cc.attachViolationToCaseAndInsertTimeFrameEvent(currentViolation, currentCase, getSessionBean().getSessUser()));
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Success! Violation added.", ""));
            return "violationPhotos";
        } catch (IntegrationException | SearchException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Unable to edit violation.", 
                                "This is a system-level error that must be corrected by an administrator, Sorry!"));
            
        } catch (ViolationException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, 
                            ex.getMessage(), "Stipulated compliance date must be in the future; please revise the stipulated compliance date."));
        } catch (BObStatusException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), "To preserve data integrity, this "
                                + "case's phase restrictions forbid attaching new code violations."));
        } catch (EventException ex) {
             System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            ex.getMessage(), "Violation event exception"));
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
        
        currentViolation = getSessionBean().getSessCodeViolation();
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
     * @return the currentCase
     */
    public CECaseDataHeavy getCurrentCase() {
        return currentCase;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECaseDataHeavy currentCase) {
        this.currentCase = currentCase;
    }

  
    
}
