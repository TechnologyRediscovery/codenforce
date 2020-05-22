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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.WorkflowCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Choice;
import com.tcvcog.tcvce.entities.EventRuleAbstract;
import com.tcvcog.tcvce.entities.EventRuleSet;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class WorkflowBB extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of ChoiceProposalBB
     */
    public WorkflowBB() {
    }
    
    private IFace_EventRuleGoverned currentERG;
    
    private Proposal currentProposal;
    
    private String formProposalRejectionReason;
    
    // view config
    private List<ViewOptionsProposalsEnum> proposalsViewOptions;
    private ViewOptionsProposalsEnum selectedProposalsViewOption;
    
    private List<ViewOptionsEventRulesEnum> rulesViewOptions;
    private ViewOptionsEventRulesEnum selectedRulesViewOption;
    
    
    @PostConstruct
    public void initBean(){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        
        setProposalsViewOptions(Arrays.asList(ViewOptionsProposalsEnum.values()));
        setSelectedProposalsViewOption(ViewOptionsProposalsEnum.VIEW_ALL);
//        currentEventRuleAbstract = wc.rules_getInitializedEventRuleAbstract();
        
        setRulesViewOptions(Arrays.asList(ViewOptionsEventRulesEnum.values()));
        setSelectedRulesViewOption(ViewOptionsEventRulesEnum.VIEW_ALL);
        
    }
    
    
    
    public void proposals_initiateViewPropMetadata(Proposal p){
        System.out.println("OccInspectionBB.proposals_viewPropMetadata");
        currentProposal = p;
    }
    
    public void proposal_reject(Proposal p){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(p.getNotes());
            sb.append("\n*** Proposal Rejection Reason ***");
            sb.append(formProposalRejectionReason);
            p.setNotes(sb.toString());
            
            wc.rejectProposal(p, currentERG, getSessionBean().getSessUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            "Proposal id " + p.getProposalID() + " has been rejected!", ""));
        } catch (IntegrationException | AuthorizationException | BObStatusException ex) {
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            ex.getMessage(), ""));
        }
    }
    
    public void proposals_makeChoice(Choice choice, Proposal p){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        try {
            if(p instanceof ProposalOccPeriod){
                wc.evaluateProposal(p, 
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
    }    
    
    public void proposals_clearProposal(Proposal p){
        WorkflowCoordinator cc = getWorkflowCoordinator();
         System.out.println("OccInspectionBB.clearChoice");
        try {
            cc.clearProposalEvaluation(p, getSessionBean().getSessUser());
            
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
            ex.getMessage(), ""));
        }
    }  

    /**
     * @return the formProposalRejectionReason
     */
    public String getFormProposalRejectionReason() {
        return formProposalRejectionReason;
    }

    /**
     * @return the currentProposal
     */
    public Proposal getCurrentProposal() {
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
     * @param formProposalRejectionReason the formProposalRejectionReason to set
     */
    public void setFormProposalRejectionReason(String formProposalRejectionReason) {
        this.formProposalRejectionReason = formProposalRejectionReason;
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
     * @return the currentERG
     */
    public IFace_EventRuleGoverned getCurrentERG() {
        return currentERG;
    }

    /**
     * @param currentERG the currentERG to set
     */
    public void setCurrentERG(IFace_EventRuleGoverned currentERG) {
        this.currentERG = currentERG;
    }
    
    
}
