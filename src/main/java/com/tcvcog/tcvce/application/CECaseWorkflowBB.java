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
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.CaseStage;
import com.tcvcog.tcvce.entities.Choice;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.ProposalCECase;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECaseList;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public  class CECaseWorkflowBB
        extends BackingBeanUtils
        implements Serializable{
    
    private CECaseDataHeavy currentCase;

    private ReportConfigCECase reportCECase;
    
    
    private CasePhase nextPhase;
    private CasePhase[] casePhaseList;
    private CasePhase selectedCasePhase;
    private CaseStage[] caseStageArray;

    
    private String styleClassStatusIcon;

    private String styleClassInvestigation;
    private String styleClassEnforcement;
    private String sytleClassCitation;
    private String sytleClassClosed;
    private String styleClassActionRequestIcon;
    
    
    /**
     * Creates a new instance of CECaseWorkflowBB
     */
    public CECaseWorkflowBB() {
    }
    
    @PostConstruct
    public void initBean() {
        CaseCoordinator caseCoord = getCaseCoordinator();
        SessionBean sb = getSessionBean();
        currentCase = sb.getSessionCECase();
        
        ReportConfigCECase rpt = getSessionBean().getReportConfigCECase();
        if (rpt != null) {
            rpt.setTitle("Code Enforcement Case Summary");
            reportCECase = rpt;
        }
    }

    public void makeChoice(Choice choice, Proposal p){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            if(p instanceof ProposalCECase){
                cc.evaluateProposal(p, choice, currentCase, getSessionBean().getSessionUser());
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "You just chose choice ID " + choice.getChoiceID() + " proposed in proposal ID " + p.getProposalID(), ""));
            }
            
        } catch (EventException | AuthorizationException | BObStatusException | IntegrationException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            ex.getMessage(), ""));
        } 
    }    
    
    
    public String takeNextAction() {
//        EventCnF e = getEventForTriggeringCasePhaseAdvancement();
        return "eventAdd";
    }
    
    
    
    public void overrideCasePhase(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        try {
            cc.manuallyChangeCasePhase(currentCase, getSelectedCasePhase());
            currentCase = ci.getCECase(currentCase.getCaseID());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Updated case phase; please refresh case", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to write case phase changes to DB",
                            "This error must be corrected by a system administrator, sorry"));
        } catch (BObStatusException | ViolationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Unable to change case phase due to a case lifecycle exception",
                            "Please check with your system administrator"));

        }
    }

    
    /**
     * @return the currentCase's phase
     */
    public CasePhase getCurrentCasePhase() {
        return currentCase.getCasePhase();
    }

  

    public void initiatePhaseOverride(ActionEvent ev) {
        System.out.println("CaseProfileBB.initiatePhaseOverride");
        // do nothing
    }
    
    
    
    public String generateReportCECase() {
        CaseCoordinator cc = getCaseCoordinator();

        reportCECase.setCse(currentCase);

        reportCECase.setCreator(getSessionBean().getSessionUser());
        reportCECase.setMuni(getSessionBean().getSessionMuni());
        reportCECase.setGenerationTimestamp(LocalDateTime.now());

        try {
            reportCECase = cc.transformCECaseForReport(reportCECase);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        } catch (BObStatusException ex) {
            System.out.println(ex);
        }

        getSessionBean().setReportConfigCECase(reportCECase);
        // this is for use by the report header to have a super class with only
        // the basic info. reportingBB exposes it to the faces page
        getSessionBean().setSessionReport(reportCECase);
        // force our reportingBB to choose the right bundle
        getSessionBean().setReportConfigCECaseList(null);

        return "reportCECase";
    }

    public void prepareReportCECase(ActionEvent ev) {
        CaseCoordinator cc = getCaseCoordinator();
        reportCECase = cc.getDefaultReportConfigCECase(currentCase);
        System.out.println("CaseProfileBB.prepareReportCECase | reportConfigOb: " + reportCECase);

    }
    
    /**
     * @return the styleClassInvestigation
     */
    public String getStyleClassInvestigation() {
        String style = null;
        
        if (currentCase != null && currentCase.getCasePhase().getCaseStage() == CaseStage.Investigation) {
            style = currentCase.getCasePhaseIcon().getStyleClass();
            
        } else {
            style = Constants.STYLE_CLASS_INACTIVE_CASE_PHASE;
        }
        styleClassInvestigation = style;
        return styleClassInvestigation;
    }

    /**
     * @return the styleClassEnforcement
     */
    public String getStyleClassEnforcement() {
        String style = null;
        if (currentCase != null && currentCase.getCasePhase().getCaseStage() == CaseStage.Enforcement) {
            style = currentCase.getCasePhaseIcon().getStyleClass();
            
        } else {
            style = Constants.STYLE_CLASS_INACTIVE_CASE_PHASE;
        }
        styleClassEnforcement = style;
        return styleClassEnforcement;
    }

    /**
     * @return the sytleClassCitation
     */
    public String getSytleClassCitation() {
        String style = null;
        if (currentCase != null && currentCase.getCasePhase().getCaseStage() == CaseStage.Citation) {
            style = currentCase.getCasePhaseIcon().getStyleClass();
            
        } else {
            style = Constants.STYLE_CLASS_INACTIVE_CASE_PHASE;
        }
        sytleClassCitation = style;
        return sytleClassCitation;
    }

    /**
     * @return the sytleClassClosed
     */
    public String getSytleClassClosed() {
        String style = null;
        if (currentCase != null && currentCase.getCasePhase().getCaseStage() == CaseStage.Closed) {
            style = currentCase.getCasePhaseIcon().getStyleClass();
            
        } else {
            style = Constants.STYLE_CLASS_INACTIVE_CASE_PHASE;
        }
        sytleClassClosed = style;
        return sytleClassClosed;
    }
    
    
    
    
    /**
     * @return the currentCase
     */
    public CECaseDataHeavy getCurrentCase() {
        return currentCase;
    }

    /**
     * @return the reportCECase
     */
    public ReportConfigCECase getReportCECase() {
        return reportCECase;
    }


    /**
     * @return the styleClassStatusIcon
     */
    public String getStyleClassStatusIcon() {
          if(currentCase != null && currentCase.getCasePhaseIcon() != null){
            styleClassStatusIcon = currentCase.getCasePhaseIcon().getStyleClass();
        }
        return styleClassStatusIcon;
    }

  

    /**
     * @return the styleClassActionRequestIcon
     */
    public String getStyleClassActionRequestIcon() {
        return styleClassActionRequestIcon;
    }

    /**
     * @param currentCase the currentCase to set
     */
    public void setCurrentCase(CECaseDataHeavy currentCase) {
        this.currentCase = currentCase;
    }

    /**
     * @param reportCECase the reportCECase to set
     */
    public void setReportCECase(ReportConfigCECase reportCECase) {
        this.reportCECase = reportCECase;
    }

    /**
     * @param styleClassStatusIcon the styleClassStatusIcon to set
     */
    public void setStyleClassStatusIcon(String styleClassStatusIcon) {
        this.styleClassStatusIcon = styleClassStatusIcon;
    }

    /**
     * @param styleClassInvestigation the styleClassInvestigation to set
     */
    public void setStyleClassInvestigation(String styleClassInvestigation) {
        this.styleClassInvestigation = styleClassInvestigation;
    }

    /**
     * @param styleClassEnforcement the styleClassEnforcement to set
     */
    public void setStyleClassEnforcement(String styleClassEnforcement) {
        this.styleClassEnforcement = styleClassEnforcement;
    }

    /**
     * @param sytleClassCitation the sytleClassCitation to set
     */
    public void setSytleClassCitation(String sytleClassCitation) {
        this.sytleClassCitation = sytleClassCitation;
    }

    /**
     * @param sytleClassClosed the sytleClassClosed to set
     */
    public void setSytleClassClosed(String sytleClassClosed) {
        this.sytleClassClosed = sytleClassClosed;
    }

    /**
     * @param styleClassActionRequestIcon the styleClassActionRequestIcon to set
     */
    public void setStyleClassActionRequestIcon(String styleClassActionRequestIcon) {
        this.styleClassActionRequestIcon = styleClassActionRequestIcon;
    }

    /**
     * @return the nextPhase
     */
    public CasePhase getNextPhase() {
        return nextPhase;
    }

    /**
     * @return the casePhaseList
     */
    public CasePhase[] getCasePhaseList() {
         casePhaseList = (CasePhase.values());
        return casePhaseList;
    }

    /**
     * @return the selectedCasePhase
     */
    public CasePhase getSelectedCasePhase() {
        return selectedCasePhase;
    }

    /**
     * @return the caseStageArray
     */
    public CaseStage[] getCaseStageArray() {
        return caseStageArray;
    }

    /**
     * @param nextPhase the nextPhase to set
     */
    public void setNextPhase(CasePhase nextPhase) {
        this.nextPhase = nextPhase;
    }

    /**
     * @param casePhaseList the casePhaseList to set
     */
    public void setCasePhaseList(CasePhase[] casePhaseList) {
        this.casePhaseList = casePhaseList;
    }

    /**
     * @param selectedCasePhase the selectedCasePhase to set
     */
    public void setSelectedCasePhase(CasePhase selectedCasePhase) {
        this.selectedCasePhase = selectedCasePhase;
    }

    /**
     * @param caseStageArray the caseStageArray to set
     */
    public void setCaseStageArray(CaseStage[] caseStageArray) {
        this.caseStageArray = caseStageArray;
    }
    
    
    
    
    
}
