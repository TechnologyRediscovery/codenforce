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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CitationStatus;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.CourtEntity;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.CitationIntegrator;
import com.tcvcog.tcvce.integration.CourtEntityIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class CitationBB extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of CitationBB
     */
    public CitationBB() {
        
    }
    
    
    private Citation currentCitation;
    
    private String citationIdLabel;
    
    private ArrayList<CitationStatus> citationStatusList;
    private CitationStatus formCitationStatus;
    private String formCitationNumber;
    private CourtEntity formCourtEntity;
    private ArrayList<CourtEntity> courtEntityList;
    
    private java.util.Date formDateOfRecord;
    private boolean formIsActive;
    private String formNotes;
    
    private ArrayList<CodeViolation> violationList;
    
    public String updateCitation(){
        System.out.println("CitationBB.updateCitation");
        CaseCoordinator cc = getCaseCoordinator();
        
        Citation c= getSessionBean().getActiveCitation();
        c.setStatus(formCitationStatus);
        c.setCitationNo(formCitationNumber);
        c.setOrigin_courtentity(formCourtEntity);
        c.setUserOwner(getFacesUser());
        c.setDateOfRecord(formDateOfRecord.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        c.setIsActive(formIsActive);
        c.setNotes(formNotes);
        try {
            cc.updateCitation(c);
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Unable to issue citation due to a database integration error", ""));
            System.out.println(ex);
        }
        
        
        return "";
    }
    
    public String issueCitation(){
        System.out.println("CitationBB.IssueCitation");
        CaseCoordinator cc = getCaseCoordinator();
        
        Citation c = currentCitation;
        c.setStatus(formCitationStatus);
        c.setCitationNo(formCitationNumber);
        c.setOrigin_courtentity(formCourtEntity);
        c.setUserOwner(getFacesUser());
        c.setDateOfRecord(formDateOfRecord.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime());
        c.setIsActive(formIsActive);
        c.setNotes(formNotes);
        try {
            cc.issueCitation(c);
              
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO, 
                       "New citation added to database!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Unable to issue citation due to a database integration error", ""));
            System.out.println(ex);
            return "";
        }
        return "caseProfile";
    }
    
    public String saveButDoNotIssueCitation(){
        // if requested feature
        return "";
    }
    
    public String deleteCitation(){
        return "";
    }

    /**
     * @return the currentCitation
     */
    public Citation getCurrentCitation() {
        
        currentCitation = getSessionBean().getActiveCitation();
        
        
        
        
        return currentCitation;
    }

    /**
     * @return the formCitationNumber
     */
    public String getFormCitationNumber() {
        formCitationNumber = currentCitation.getCitationNo();
        return formCitationNumber;
    }

    /**
     * @return the formCourtEntity
     */
    public CourtEntity getFormCourtEntity() {
        formCourtEntity = currentCitation.getOrigin_courtentity();
        return formCourtEntity;
    }

    /**
     * @return the formDateOfRecord
     */
    public java.util.Date getFormDateOfRecord() {
        if(currentCitation.getDateOfRecord() != null){
            formDateOfRecord = java.util.Date.from(currentCitation.getDateOfRecord()
                .atZone(ZoneId.systemDefault()).toInstant());
            
            return formDateOfRecord;
            
        } else {
            LocalDateTime ldtNow = LocalDateTime.now();
            formDateOfRecord = java.util.Date.from(ldtNow
                    .atZone(ZoneId.systemDefault()).toInstant());
            
        }
        return formDateOfRecord;
    }

    /**
     * @return the formIsActive
     */
    public boolean isFormIsActive() {
        formIsActive = currentCitation.isIsActive();
        return formIsActive;
    }

    /**
     * @return the formNotes
     */
    public String getFormNotes() {
        formNotes = currentCitation.getNotes();
                
        return formNotes;
    }

    /**
     * @return the violationList
     */
    public ArrayList<CodeViolation> getViolationList() {
        violationList = currentCitation.getViolationList();
        return violationList;
    }

    /**
     * @param currentCitation the currentCitation to set
     */
    public void setCurrentCitation(Citation currentCitation) {
        this.currentCitation = currentCitation;
    }

    /**
     * @param formCitationNumber the formCitationNumber to set
     */
    public void setFormCitationNumber(String formCitationNumber) {
        this.formCitationNumber = formCitationNumber;
    }

    /**
     * @param formCourtEntity the formCourtEntity to set
     */
    public void setFormCourtEntity(CourtEntity formCourtEntity) {
        this.formCourtEntity = formCourtEntity;
    }

    /**
     * @param formDateOfRecord the formDateOfRecord to set
     */
    public void setFormDateOfRecord(java.util.Date formDateOfRecord) {
        this.formDateOfRecord = formDateOfRecord;
    }

    /**
     * @param formIsActive the formIsActive to set
     */
    public void setFormIsActive(boolean formIsActive) {
        this.formIsActive = formIsActive;
    }

    /**
     * @param formNotes the formNotes to set
     */
    public void setFormNotes(String formNotes) {
        this.formNotes = formNotes;
    }

    /**
     * @param violationList the violationList to set
     */
    public void setViolationList(ArrayList<CodeViolation> violationList) {
        this.violationList = violationList;
    }

    /**
     * @return the formCitationStatus
     */
    public CitationStatus getFormCitationStatus() {
        return formCitationStatus;
    }

    /**
     * @param formCitationStatus the formCitationStatus to set
     */
    public void setFormCitationStatus(CitationStatus formCitationStatus) {
        this.formCitationStatus = formCitationStatus;
    }

    /**
     * @return the CitationStatusList
     */
    public ArrayList<CitationStatus> getCitationStatusList() {
        CitationIntegrator citInt = getCitationIntegrator();
        try {
            citationStatusList = citInt.getFullCitationStatusList();
        } catch (IntegrationException ex) {
            // do nothing
            System.out.println(ex);
        }
        return citationStatusList;
    }

    /**
     * @param CitationStatusList the CitationStatusList to set
     */
    public void setCitationStatusList(ArrayList<CitationStatus> citationStatusList) {
        this.citationStatusList = citationStatusList;
    }

    /**
     * @return the citationIdLabel
     */
    public String getCitationIdLabel() {
        
        if(currentCitation.getCitationNo() != null){
            citationIdLabel = String.valueOf(currentCitation.getCitationID());
        } else {
            citationIdLabel = "[citation not yet issued]";
        }
        
        return citationIdLabel;
    }

    /**
     * @param citationIdLabel the citationIdLabel to set
     */
    public void setCitationIdLabel(String citationIdLabel) {
        this.citationIdLabel = citationIdLabel;
    }

    /**
     * @return the courtEntityList
     */
    public ArrayList<CourtEntity> getCourtEntityList() {
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        try {
            courtEntityList = cei.getCourtEntityList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            // no notice to user
        }
        return courtEntityList;
    }

    /**
     * @param courtEntityList the courtEntityList to set
     */
    public void setCourtEntityList(ArrayList<CourtEntity> courtEntityList) {
        this.courtEntityList = courtEntityList;
    }

    
    
    
    
    
    
    
}
