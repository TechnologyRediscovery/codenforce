/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.occupancy.application;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.ChoiceCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class OccPeriodWorkflowBB extends BackingBeanUtils{

    private OccPeriodDataHeavy currentOccPeriod;
    
    private boolean periodStartDateNull;
    private boolean periodEndDateNull;
    
    private String formNoteText;
    private String formProposalRejectionReason;
    
    private List<OccPeriodType> occPeriodTypeList;
    private OccPeriodType selectedOccPeriodType;
    
    private List<PropertyUnit> propertyUnitCandidateList;
    private PropertyUnit selectedPropertyUnit;
    
    private User selectedManager;
    
     // proposals
    private ProposalOccPeriod currentProposal;
    private List<ViewOptionsProposalsEnum> proposalsViewOptions;
    private ViewOptionsProposalsEnum selectedProposalsViewOption;
    
    // rules
    private EventRuleAbstract currentEventRuleAbstract;
    private List<EventRuleSet> eventRuleSetList;
    private EventRuleSet selectedEventRuleSet;
    
    
    private List<ViewOptionsEventRulesEnum> rulesViewOptions;
    private ViewOptionsEventRulesEnum selectedRulesViewOption;

    /**
     * Creates a new instance of OccPeriodWorkflowBB
     */
    public OccPeriodWorkflowBB() {
    }
    
    
    @PostConstruct
    public void initBean(){
        EventCoordinator ec = getEventCoordinator();
        PropertyIntegrator pi = getPropertyIntegrator();
        
        periodEndDateNull = false;
        periodStartDateNull = false;
        
        proposalsViewOptions = Arrays.asList(ViewOptionsProposalsEnum.values());
        selectedProposalsViewOption = ViewOptionsProposalsEnum.VIEW_ALL;
        currentEventRuleAbstract = ec.rules_getInitializedEventRuleAbstract();
        occPeriodTypeList = getSessionBean().getSessMuni().getProfile().getOccPeriodTypeList();
        
        
        rulesViewOptions = Arrays.asList(ViewOptionsEventRulesEnum.values());
        selectedRulesViewOption = ViewOptionsEventRulesEnum.VIEW_ALL;
        try {
            propertyUnitCandidateList = pi.getPropertyUnitList(getSessionBean().getSessProperty());
            eventRuleSetList = ec.rules_getEventRuleSetList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
    }

    
    public void rejectProposal(){
        // TODO: Finish my guts
        
    }
    
    public void proposals_initiateViewPropMetadata(ProposalOccPeriod p){
        System.out.println("OccInspectionBB.proposals_viewPropMetadata");
        setCurrentProposal(p);
    }
    
    public void proposal_reject(Proposal p){
        ChoiceCoordinator choiceCoord = getChoiceCoordinator();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(p.getNotes());
            sb.append("\n*** Proposal Rejection Reason ***");
            sb.append(getFormNoteText());
            p.setNotes(sb.toString());
            
            choiceCoord.rejectProposal(p, getCurrentOccPeriod(), getSessionBean().getSessUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            "Proposal id " + p.getProposalID() + " has been rejected!", ""));
        } catch (IntegrationException | AuthorizationException | BObStatusException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            ex.getMessage(), ""));
        }
    }
    
    public void proposals_makeChoice(Choice choice, Proposal p){
        OccupancyCoordinator oc = getOccupancyCoordinator();
         System.out.println("OccInspectionBB.proposals_makeChoice");
        try {
            if(p instanceof ProposalOccPeriod){
                oc.evaluateProposal(p, 
                                        choice, getCurrentOccPeriod(), 
                                        getSessionBean().getSessUser());
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                "You just chose choice ID " + choice.getChoiceID() + " proposed in proposal ID " + p.getProposalID(), ""));
            }
            
        } catch (EventException | AuthorizationException | BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            ex.getMessage(), ""));
        }
        reloadCurrentOccPeriodDataHeavy();
    }    
    
    public void proposals_clearProposal(Proposal p){
        ChoiceCoordinator cc = getChoiceCoordinator();
         System.out.println("OccInspectionBB.clearChoice");
        try {
            cc.clearProposalEvaluation(p, getSessionBean().getSessUser());
            
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            ex.getMessage(), ""));
        }
        reloadCurrentOccPeriodDataHeavy();
    }    
    
    public void reloadCurrentOccPeriodDataHeavy(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            currentOccPeriod = oc.assembleOccPeriodDataHeavy(currentOccPeriod, getSessionBean().getSessUser().getMyCredential());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Reloaded occ period ID " + currentOccPeriod.getPeriodID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Unable to reload occ period", ""));
        }
        
    }
    
       
     public void certifyOccPeriodField(ActionEvent ev){
         
         OccupancyCoordinator oc = getOccupancyCoordinator();
         FacesContext context = getFacesContext();
         String field = context.getExternalContext().getRequestParameterMap().get("fieldtocertify");
         String certifymode = context.getExternalContext().getRequestParameterMap().get("certifymode");
         
         System.out.println("OccInspectionBB.certifuyOccPeriodField | field: " + field + " | mode: " + certifymode);
         
         User u = getSessionBean().getSessUser();
         LocalDateTime now = LocalDateTime.now();
         
         switch(field){
            case "authorization":
                currentOccPeriod.setAuthorizedBy(u);
                currentOccPeriod.setAuthorizedTS(now);
                if(certifymode.equals("withdraw")){
                    currentOccPeriod.setAuthorizedBy(null);
                    currentOccPeriod.setAuthorizedTS(null);
                }
                break;
            case "occperiodtype":
                currentOccPeriod.setPeriodTypeCertifiedBy(u);
                currentOccPeriod.setPeriodTypeCertifiedTS(now);
                if(certifymode.equals("withdraw")){
                    currentOccPeriod.setPeriodTypeCertifiedBy(null);
                    currentOccPeriod.setPeriodTypeCertifiedTS(null);
                }
                break;
            case "startdate":
                if(periodStartDateNull){
                    currentOccPeriod.setStartDate(null);
                }
                currentOccPeriod.setStartDateCertifiedBy(u);
                currentOccPeriod.setStartDateCertifiedTS(now);
                if(certifymode.equals("withdraw")){
                    currentOccPeriod.setStartDateCertifiedBy(null);
                    currentOccPeriod.setStartDateCertifiedTS(null);
                }
                break;
            case "enddate":
                if(periodEndDateNull){
                    currentOccPeriod.setEndDate(null);
                }
                currentOccPeriod.setEndDateCertifiedBy(u);
                currentOccPeriod.setEndDateCertifiedTS(now);
                if(certifymode.equals("withdraw")){
                    currentOccPeriod.setEndDateCertifiedBy(null);
                    currentOccPeriod.setEndDateCertifiedTS(null);
                }
                break;
            default:
                getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Error! Unable to certify field", ""));
         }
         
        try {
            oc.updateOccPeriod(currentOccPeriod, u);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Successfully udpated field status!", ""));
        } catch (IntegrationException | BObStatusException ex) {
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
        reloadCurrentOccPeriodDataHeavy();
         
     }
     
     
     public void authorizeOccPeriod(ActionEvent ev){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.authorizeOccPeriod(currentOccPeriod, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_INFO,
               "Success! Occupancy period ID " + currentOccPeriod.getPeriodID() 
                       + " is now authorized and permits can be generated.", ""));
        } catch (AuthorizationException | BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
               new FacesMessage(FacesMessage.SEVERITY_ERROR,
               ex.getMessage(), ""));
        }
     }
       /**
      * utility pass through method to be called when loading Occperiod advanced settings
      * @param ev 
      */
     public void updateOccPeriodInitialize(ActionEvent ev){
         
     }
     
     public void updateOccPeriodCommit(){
        OccupancyCoordinator oc = getOccupancyCoordinator();
        if(selectedManager != null){
            currentOccPeriod.setManager(selectedManager);
        }
        
        if(selectedOccPeriodType != null){
            currentOccPeriod.setType(selectedOccPeriodType);
        }
        
        try {
            oc.updateOccPeriod(currentOccPeriod, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Update successful on OccPeriod ID: " + currentOccPeriod.getPeriodID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
     }
     public void updatePeriodPropUnit(){
         OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
            oc.updateOccPeriodPropUnit(currentOccPeriod, getSelectedPropertyUnit());
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                "The current occupancy period has been assigned to property unit ID " + getSelectedPropertyUnit().getUnitID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                ex.getMessage(), ""));
        }
        reloadCurrentOccPeriodDataHeavy();
     }
     
    
    
     /**
     * @return the currentProposal
     */
    public ProposalOccPeriod getCurrentProposal() {
        return currentProposal;
    }

    /**
     * @return the proposalsViewOptions
     */
    public List<ViewOptionsProposalsEnum> getProposalsViewOptions() {
        return proposalsViewOptions;
    }

    /**
     * @return the selectedProposalsViewOption
     */
    public ViewOptionsProposalsEnum getSelectedProposalsViewOption() {
        return selectedProposalsViewOption;
    }

    /**
     * @return the currentEventRuleAbstract
     */
    public EventRuleAbstract getCurrentEventRuleAbstract() {
        return currentEventRuleAbstract;
    }

    /**
     * @return the eventRuleSetList
     */
    public List<EventRuleSet> getEventRuleSetList() {
        return eventRuleSetList;
    }

    /**
     * @return the selectedEventRuleSet
     */
    public EventRuleSet getSelectedEventRuleSet() {
        return selectedEventRuleSet;
    }


    
    /**
     * @return the rulesViewOptions
     */
    public List<ViewOptionsEventRulesEnum> getRulesViewOptions() {
        return rulesViewOptions;
    }

    /**
     * @return the selectedRulesViewOption
     */
    public ViewOptionsEventRulesEnum getSelectedRulesViewOption() {
        return selectedRulesViewOption;
    }

    /**
     * @param currentProposal the currentProposal to set
     */
    public void setCurrentProposal(ProposalOccPeriod currentProposal) {
        this.currentProposal = currentProposal;
    }

    /**
     * @param proposalsViewOptions the proposalsViewOptions to set
     */
    public void setProposalsViewOptions(List<ViewOptionsProposalsEnum> proposalsViewOptions) {
        this.proposalsViewOptions = proposalsViewOptions;
    }

    /**
     * @param selectedProposalsViewOption the selectedProposalsViewOption to set
     */
    public void setSelectedProposalsViewOption(ViewOptionsProposalsEnum selectedProposalsViewOption) {
        this.selectedProposalsViewOption = selectedProposalsViewOption;
    }

    /**
     * @param currentEventRuleAbstract the currentEventRuleAbstract to set
     */
    public void setCurrentEventRuleAbstract(EventRuleAbstract currentEventRuleAbstract) {
        this.currentEventRuleAbstract = currentEventRuleAbstract;
    }

    /**
     * @param eventRuleSetList the eventRuleSetList to set
     */
    public void setEventRuleSetList(List<EventRuleSet> eventRuleSetList) {
        this.eventRuleSetList = eventRuleSetList;
    }

    /**
     * @param selectedEventRuleSet the selectedEventRuleSet to set
     */
    public void setSelectedEventRuleSet(EventRuleSet selectedEventRuleSet) {
        this.selectedEventRuleSet = selectedEventRuleSet;
    }

    /**
     * @param rulesViewOptions the rulesViewOptions to set
     */
    public void setRulesViewOptions(List<ViewOptionsEventRulesEnum> rulesViewOptions) {
        this.rulesViewOptions = rulesViewOptions;
    }

    /**
     * @param selectedRulesViewOption the selectedRulesViewOption to set
     */
    public void setSelectedRulesViewOption(ViewOptionsEventRulesEnum selectedRulesViewOption) {
        this.selectedRulesViewOption = selectedRulesViewOption;
    }

    /**
     * @return the currentOccPeriod
     */
    public OccPeriodDataHeavy getCurrentOccPeriod() {
        return currentOccPeriod;
    }

    /**
     * @return the periodStartDateNull
     */
    public boolean isPeriodStartDateNull() {
        return periodStartDateNull;
    }

    /**
     * @return the periodEndDateNull
     */
    public boolean isPeriodEndDateNull() {
        return periodEndDateNull;
    }

    /**
     * @param currentOccPeriod the currentOccPeriod to set
     */
    public void setCurrentOccPeriod(OccPeriodDataHeavy currentOccPeriod) {
        this.currentOccPeriod = currentOccPeriod;
    }

    /**
     * @param periodStartDateNull the periodStartDateNull to set
     */
    public void setPeriodStartDateNull(boolean periodStartDateNull) {
        this.periodStartDateNull = periodStartDateNull;
    }

    /**
     * @param periodEndDateNull the periodEndDateNull to set
     */
    public void setPeriodEndDateNull(boolean periodEndDateNull) {
        this.periodEndDateNull = periodEndDateNull;
    }

    /**
     * @return the formNoteText
     */
    public String getFormNoteText() {
        return formNoteText;
    }

    /**
     * @return the formProposalRejectionReason
     */
    public String getFormProposalRejectionReason() {
        return formProposalRejectionReason;
    }

    /**
     * @param formNoteText the formNoteText to set
     */
    public void setFormNoteText(String formNoteText) {
        this.formNoteText = formNoteText;
    }

    /**
     * @param formProposalRejectionReason the formProposalRejectionReason to set
     */
    public void setFormProposalRejectionReason(String formProposalRejectionReason) {
        this.formProposalRejectionReason = formProposalRejectionReason;
    }

    /**
     * @return the selectedOccPeriodType
     */
    public OccPeriodType getSelectedOccPeriodType() {
        return selectedOccPeriodType;
    }

    /**
     * @param selectedOccPeriodType the selectedOccPeriodType to set
     */
    public void setSelectedOccPeriodType(OccPeriodType selectedOccPeriodType) {
        this.selectedOccPeriodType = selectedOccPeriodType;
    }

    /**
     * @return the selectedManager
     */
    public User getSelectedManager() {
        return selectedManager;
    }

    /**
     * @param selectedManager the selectedManager to set
     */
    public void setSelectedManager(User selectedManager) {
        this.selectedManager = selectedManager;
    }

    /**
     * @return the occPeriodTypeList
     */
    public List<OccPeriodType> getOccPeriodTypeList() {
        return occPeriodTypeList;
    }

    /**
     * @param occPeriodTypeList the occPeriodTypeList to set
     */
    public void setOccPeriodTypeList(List<OccPeriodType> occPeriodTypeList) {
        this.occPeriodTypeList = occPeriodTypeList;
    }

    /**
     * @return the propertyUnitCandidateList
     */
    public List<PropertyUnit> getPropertyUnitCandidateList() {
        return propertyUnitCandidateList;
    }

    /**
     * @return the selectedPropertyUnit
     */
    public PropertyUnit getSelectedPropertyUnit() {
        return selectedPropertyUnit;
    }

    /**
     * @param propertyUnitCandidateList the propertyUnitCandidateList to set
     */
    public void setPropertyUnitCandidateList(List<PropertyUnit> propertyUnitCandidateList) {
        this.propertyUnitCandidateList = propertyUnitCandidateList;
    }

    /**
     * @param selectedPropertyUnit the selectedPropertyUnit to set
     */
    public void setSelectedPropertyUnit(PropertyUnit selectedPropertyUnit) {
        this.selectedPropertyUnit = selectedPropertyUnit;
    }
}
