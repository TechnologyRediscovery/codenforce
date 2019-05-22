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
import com.tcvcog.tcvce.integration.CitationIntegrator;
import com.tcvcog.tcvce.integration.CourtEntityIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class CitationBB extends BackingBeanUtils implements Serializable{

    
    private Citation currentCitation;
    private CECase currentCase;
    
    private List<CitationStatus> citationStatusList;
    private List<CourtEntity> courtEntityList;
    
    private boolean issueCitationDisabled;
    private boolean updateCitationDisabled;
    
    private List<CodeViolation> removedViolationList;
    private String citationEditEventDescription;
    
    
    /**
     * Creates a new instance of CitationBB
     */
    public CitationBB() {
        
    }
    
    @PostConstruct
    public void initBean(){
        CitationIntegrator citInt = getCitationIntegrator();
        CourtEntityIntegrator cei = getCourtEntityIntegrator();
        
        try {
            citationStatusList = citInt.getCitationStatusList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        try {
            courtEntityList = cei.getCourtEntityList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        
        Citation c = getSessionBean().getActiveCitation();
        if(c != null){
            currentCitation = c;
        } else {
            CECase ceCase = getSessionBean().getcECaseQueue().get(0);
            currentCitation = new Citation();
            currentCitation.setCeCaseNoLists(ceCase);
            currentCitation.setDateOfRecord(LocalDateTime.now());
            currentCitation.setUserOwner(getSessionBean().getFacesUser());
            currentCitation.setIsActive(true);
            currentCitation.setOrigin_courtentity(getSessionBean().getActiveMuni().getDefaultCourtEntity());
            List<CodeViolation> l = new ArrayList<>();
            for(CodeViolation v: ceCase.getViolationList()){
                if(v.getActualComplianceDate() == null){
                    l.add(v);
                }
            }
            currentCitation.setViolationList(l);
            removedViolationList = new ArrayList<>();
        }
        
    }
    
    
    public void removeViolationFromCitation(CodeViolation v){
        currentCitation.getViolationList().remove(v);
        removedViolationList.add(v);
    }
    
    public void returnViolation(CodeViolation v){
        currentCitation.getViolationList().add(v);
        removedViolationList.remove(v);
    }
    
    public String updateCitation(){
        System.out.println("CitationBB.updateCitation");
        CaseCoordinator cc = getCaseCoordinator();
        
        
        try {
            cc.updateCitation(currentCitation);
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Unable to issue citation due to a database integration error", ""));
            System.out.println(ex);
        }
        return "ceCases";
    }
    
    public String insertCitation(){
        System.out.println("CitationBB.IssueCitation");
        CaseCoordinator cc = getCaseCoordinator();
        
        Citation c = currentCitation;
        c.setUserOwner(getFacesUser());
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
   
   
    /**
     * @return the currentCitation
     */
    public Citation getCurrentCitation() {
        return currentCitation;
    }

    

    /**
     * @return the removedViolationList
     */
    public List<CodeViolation> getRemovedViolationList() {
        
        return removedViolationList;
    }

    /**
     * @param currentCitation the currentCitation to set
     */
    public void setCurrentCitation(Citation currentCitation) {
        this.currentCitation = currentCitation;
    }

   

    /**
     * @param violationList the removedViolationList to set
     */
    public void setViolationList(ArrayList<CodeViolation> violationList) {
        this.removedViolationList = violationList;
    }

   

    /**
     * @return the CitationStatusList
     */
    public List<CitationStatus> getCitationStatusList() {
        
        return citationStatusList;
    }

    /**
     * @param citationStatusList
     */
    public void setCitationStatusList(List<CitationStatus> citationStatusList) {
        this.citationStatusList = citationStatusList;
    }

    
    /**
     * @return the courtEntityList
     */
    public List<CourtEntity> getCourtEntityList() {
        return courtEntityList;
    }

    /**
     * @param courtEntityList the courtEntityList to set
     */
    public void setCourtEntityList(ArrayList<CourtEntity> courtEntityList) {
        this.courtEntityList = courtEntityList;
    }

    /**
     * @return the issueCitationDisabled
     */
    public boolean isIssueCitationDisabled() {
        issueCitationDisabled = currentCitation.getCitationNo() != null;
        return issueCitationDisabled;
    }

    /**
     * @return the updateCitationDisabled
     */
    public boolean isUpdateCitationDisabled() {
        updateCitationDisabled = currentCitation.getCitationNo() == null;
        return updateCitationDisabled;
    }

    /**
     * @param issueCitationDisabled the issueCitationDisabled to set
     */
    public void setIssueCitationDisabled(boolean issueCitationDisabled) {
        this.issueCitationDisabled = issueCitationDisabled;
    }

    /**
     * @param updateCitationDisabled the updateCitationDisabled to set
     */
    public void setUpdateCitationDisabled(boolean updateCitationDisabled) {
        this.updateCitationDisabled = updateCitationDisabled;
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
     * @return the citationEditEventDescription
     */
    public String getCitationEditEventDescription() {
        return citationEditEventDescription;
    }

    /**
     * @param citationEditEventDescription the citationEditEventDescription to set
     */
    public void setCitationEditEventDescription(String citationEditEventDescription) {
        this.citationEditEventDescription = citationEditEventDescription;
    }

    
    
    
    
    
    
    
}
