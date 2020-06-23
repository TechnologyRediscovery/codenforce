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
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.CasePhaseEnum;
import com.tcvcog.tcvce.entities.CaseStageEnum;
import com.tcvcog.tcvce.entities.reports.ReportConfigCECase;
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
    
    private EventCnF selectedEvent;
    
    private CasePhaseEnum nextPhase;
    private CasePhaseEnum[] casePhaseList;
    private CasePhaseEnum selectedCasePhase;
    private CaseStageEnum[] caseStageArray;

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
        currentCase = sb.getSessCECase();
        
        ReportConfigCECase rpt = getSessionBean().getReportConfigCECase();
        if (rpt != null) {
            rpt.setTitle("Code Enforcement Case Summary");
            reportCECase = rpt;
        }
    }

    
    /**
     * @return the currentCase's phase
     */
    public CasePhaseEnum getCurrentCasePhase() {
        return currentCase.getCasePhase();
    }
    
    public void initiateCaseUpdate(ActionEvent ev){
        
    }
    
    public void refreshCurrentCase(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        try {
            currentCase = cc.assembleCECaseDataHeavy(currentCase, getSessionBean().getSessUser().getMyCredential());
        } catch (BObStatusException  | IntegrationException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Could not refresh current case", ""));
        }
        
    }
    
    public void updateCase(ActionEvent ev){
        CaseCoordinator cc = getCaseCoordinator();
        
        try {
            cc.updateCECaseMetadata(currentCase);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "Case metadata updated", ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                "Could not update case metadata, sorry! This error must be corrected by an administrator", ""));
            
        }
        
        
    }

  

    public void initiatePhaseOverride(ActionEvent ev) {
        System.out.println("CaseProfileBB.initiatePhaseOverride");
        // do nothing
    }
    
    
    public void overrideCasePhase(ActionEvent ev){
        System.out.println("Not implemented yet;");
    }
    
    public String exploreProperty(ActionEvent ev){
        PropertyCoordinator pc = getPropertyCoordinator();
        try {
            getSessionBean().setSessProperty(pc.assemblePropertyDataHeavy(currentCase.getProperty(), getSessionBean().getSessUser().getMyCredential()));
        } catch (IntegrationException |BObStatusException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Could not load property data heavy; reloaded page", ""));
            return "";
        }
        return "propertyInfo";
        
    }
    
    
    
    public String generateReportCECase() {
        CaseCoordinator cc = getCaseCoordinator();

        reportCECase.setCse(currentCase);

        reportCECase.setCreator(getSessionBean().getSessUser());
        reportCECase.setMuni(getSessionBean().getSessMuni());
        reportCECase.setGenerationTimestamp(LocalDateTime.now());

        try {
            reportCECase = cc.transformCECaseForReport(reportCECase);
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Could not generate report, sorry!", ""));
        }

        getSessionBean().setReportConfigCECase(reportCECase);
        // this is for use by the report header to have a super class with only
        // the basic info. reportingBB exposes it to the faces page
        getSessionBean().setSessReport(reportCECase);
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
        
        if (currentCase != null && currentCase.getCasePhase().getCaseStage() == CaseStageEnum.Investigation) {
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
        if (currentCase != null && currentCase.getCasePhase().getCaseStage() == CaseStageEnum.Enforcement) {
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
        if (currentCase != null && currentCase.getCasePhase().getCaseStage() == CaseStageEnum.Citation) {
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
        if (currentCase != null && currentCase.getCasePhase().getCaseStage() == CaseStageEnum.Closed) {
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
    public CasePhaseEnum getNextPhase() {
        return nextPhase;
    }

    /**
     * @return the casePhaseList
     */
    public CasePhaseEnum[] getCasePhaseList() {
         casePhaseList = (CasePhaseEnum.values());
        return casePhaseList;
    }

    /**
     * @return the selectedCasePhase
     */
    public CasePhaseEnum getSelectedCasePhase() {
        return selectedCasePhase;
    }

    /**
     * @return the caseStageArray
     */
    public CaseStageEnum[] getCaseStageArray() {
        return caseStageArray;
    }

    /**
     * @param nextPhase the nextPhase to set
     */
    public void setNextPhase(CasePhaseEnum nextPhase) {
        this.nextPhase = nextPhase;
    }

    /**
     * @param casePhaseList the casePhaseList to set
     */
    public void setCasePhaseList(CasePhaseEnum[] casePhaseList) {
        this.casePhaseList = casePhaseList;
    }

    /**
     * @param selectedCasePhase the selectedCasePhase to set
     */
    public void setSelectedCasePhase(CasePhaseEnum selectedCasePhase) {
        this.selectedCasePhase = selectedCasePhase;
    }

    /**
     * @param caseStageArray the caseStageArray to set
     */
    public void setCaseStageArray(CaseStageEnum[] caseStageArray) {
        this.caseStageArray = caseStageArray;
    }

    /**
     * @return the selectedEvent
     */
    public EventCnF getSelectedEvent() {
        return selectedEvent;
    }

    /**
     * @param selectedEvent the selectedEvent to set
     */
    public void setSelectedEvent(EventCnF selectedEvent) {
        this.selectedEvent = selectedEvent;
    }
    
    
    
    
    
}
