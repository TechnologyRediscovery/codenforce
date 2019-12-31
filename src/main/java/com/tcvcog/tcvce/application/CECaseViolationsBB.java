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
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EnforcableCodeElement;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CodeIntegrator;
import com.tcvcog.tcvce.integration.ViolationIntegrator;
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

    private CECase currentCase;
    
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
        currentCase = sb.getSessionCECase();
       
    }
    
    
    public String addViolation() {
        getSessionBean().setSessionCECase(getCurrentCase());
        return "violationSelectElement";
    }
      
    public void removeViolation(CodeViolation cv){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            cc.deleteViolation(cv);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Violation id " + cv.getViolationID() + " removed from case!", ""));
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(), ""));
            
        }
        
        
    }
    
    
    public void deleteViolation(ActionEvent e) {
        CaseCoordinator cc = getCaseCoordinator();

        try {
            cc.deleteViolation(selectedViolations.get(0));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Unable to delete selected violation",
                        "It has probably been added to a Notice of Violation"
                                + "already and cannot be removed."));
        }
    }
    
    
    public void updateViolationsCodeBookLink(ActionEvent ae) throws BObStatusException {
        CaseIntegrator casei = getCaseIntegrator();
        try {
            ViolationIntegrator cvi = getCodeViolationIntegrator();
            CodeIntegrator ci = getCodeIntegrator();
            EnforcableCodeElement ece = ci.getEnforcableCodeElement(newViolationCodeBookEleID);
            if (ece != null) {
                selectedViolation.setViolatedEnfElement(ece);
                cvi.updateCodeViolation(selectedViolation);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Success: Updated Violation with new CodeBook linking", ""));
                currentCase = casei.getCECase(currentCase.getCaseID());
            } else {
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Unable to find CodeBook Entry by this ID, sorry. Please try again.", ""));
            }
        } catch (IntegrationException ex) {
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
            getSelectedViolation().setComplianceUser(getSessionBean().getSessionUser());
            e = ec.generateViolationComplianceEvent(getSelectedViolation());
            e.setOwner(getSessionBean().getSessionUser());
            e.setDateOfRecord(LocalDateTime.now());
            cv.setActualComplianceDate(LocalDateTime.now());
            cc.recordCompliance(cv, getSessionBean().getSessionUser());
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
            getSessionBean().setSessionCodeViolation(cv);
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
    public CECase getCurrentCase() {
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
    public void setCurrentCase(CECase currentCase) {
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
