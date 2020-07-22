/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
import com.tcvcog.tcvce.coordinators.DataCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;
import org.primefaces.model.chart.DonutChartModel;

/**
 *
 * @author sylvia
 */
public class CECaseViolationsBB 
        extends     BackingBeanUtils
        implements  Serializable {

    private CECaseDataHeavy currentCase;
    
    private List<CodeViolation> selectedViolations;
    private CodeViolation selectedViolation;
    private int newViolationCodeBookEleID;

    
    private DonutChartModel violationDonut;
    

    /**
     * Creates a new instance of CECaseViolationsBB
     */
    public CECaseViolationsBB() {
    }
 
    @PostConstruct
    public void initBean() {
        SessionBean sb = getSessionBean();
        currentCase = sb.getSessCECase();
       
    }
    
    
    public String addViolation() {
        getSessionBean().setSessCECase(getCurrentCase());
        return "violationSelectElement";
    }
      
    /**
     * Attempts to deactivate a code violation
     * @param cv 
     */
    public void removeViolation(CodeViolation cv){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.deactivateCodeViolation(cv, getSessionBean().getSessUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Violation id " + cv.getViolationID() + " removed from case!", ""));
            
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
            
        }
        
        
    }
    
    
    /**
     * TODO: Fix this ; logged in github issue
     * Attempts to change the DB link between a code violation and a code element
     * @param ae
     * @throws BObStatusException 
     */
    public void updateViolationCodeBookLink(ActionEvent ae) throws BObStatusException {
        CaseCoordinator cc = getCaseCoordinator();
        CodeIntegrator ci = getCodeIntegrator();
        
        try {
            EnforcableCodeElement ece = ci.getEnforcableCodeElement(newViolationCodeBookEleID);
            if (ece != null) {
                selectedViolation.setViolatedEnfElement(ece);
//                cc.updateCodeViolation(selectedViolation);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Success: Updated Violation with new CodeBook linking", ""));
                currentCase = cc.assembleCECaseDataHeavy(cc.getCECase(currentCase.getCaseID()), getSessionBean().getSessUser());
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to find CodeBook Entry by this ID, sorry. Please try again.", ""));
            }
        } catch (IntegrationException | SearchException ex) {
            System.out.println(ex);
        }

    }


  

    public void recordCompliance(CodeViolation cv) {
        EventCoordinator ec = getEventCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        setSelectedViolation(cv);
        // build event details package
        EventCnF e = null;
        try {
            getSelectedViolation().setComplianceUser(getSessionBean().getSessUser());
            e = ec.generateViolationComplianceEvent(getSelectedViolation());
            e.setUserCreator(getSessionBean().getSessUser());
            e.setTimeStart(LocalDateTime.now());
            cv.setActualComplianceDate(LocalDateTime.now());
            cc.recordCompliance(cv, getSessionBean().getSessUser());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        // the user is then shown the add event dialog, and when the
        // event is added to the case, the CaseCoordinator will
        // set the date of record on the violation to match that chosen
        // for the event
//        selectedEvent = e;

    }

//    Procedural vs. OO code
    public String editViolation(CodeViolation cv) {
            getSessionBean().setSessCodeViolation(cv);
//            positionCurrentCaseAtHeadOfQueue();
            return "violationEdit";
    }
 
    
    
   private void generateViolationDonut(){
       DataCoordinator dc = getDataCoordinator();
       /*
       
       if(caseList != null && caseList.size() >= 1){
           DonutChartModel d = new DonutChartModel();
           d.addCircle(dc.computeViolationFrequencyStringMap(caseList));
           
           d.setTitle("Violation frequency: all listed cases");
           d.setLegendPosition("s");
           d.setShowDataLabels(true);
           d.setShowDatatip(true);
//           d.setSeriesColors(sytleClassClosed);
            violationDonut = d;
          
       }
   */
       
       
   }

    /**
     * @return the currentCase
     */
    public CECaseDataHeavy getCurrentCase() {
        return currentCase;
    }

    /**
     * @return the selectedViolations
     */
    public List<CodeViolation> getSelectedViolations() {
        return selectedViolations;
    }

    /**
     * @return the selectedViolation
     */
    public CodeViolation getSelectedViolation() {
        return selectedViolation;
    }

    /**
     * @return the newViolationCodeBookEleID
     */
    public int getNewViolationCodeBookEleID() {
        return newViolationCodeBookEleID;
    }

    /**
     * @return the violationDonut
     */
    public DonutChartModel getViolationDonut() {
        return violationDonut;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECaseDataHeavy currentCase) {
        this.currentCase = currentCase;
    }

    /**
     * @param selectedViolations the selectedViolations to set
     */
    public void setSelectedViolations(List<CodeViolation> selectedViolations) {
        this.selectedViolations = selectedViolations;
    }

    /**
     * @param selectedViolation the selectedViolation to set
     */
    public void setSelectedViolation(CodeViolation selectedViolation) {
        this.selectedViolation = selectedViolation;
    }

    /**
     * @param newViolationCodeBookEleID the newViolationCodeBookEleID to set
     */
    public void setNewViolationCodeBookEleID(int newViolationCodeBookEleID) {
        this.newViolationCodeBookEleID = newViolationCodeBookEleID;
    }

    /**
     * @param violationDonut the violationDonut to set
     */
    public void setViolationDonut(DonutChartModel violationDonut) {
        this.violationDonut = violationDonut;
    }
}
