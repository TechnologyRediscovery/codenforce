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
import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.coordinators.WorkflowCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodType;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class WorkflowBB extends BackingBeanUtils{


     private IFace_EventRuleGoverned currentERG;
    
    private Proposal currentProposal;
    private int formEventRuleIDToAdd;
    
    private String formProposalRejectionReason;
    
    // view config
    private List<ViewOptionsProposalsEnum> proposalsViewOptions;
    private ViewOptionsProposalsEnum selectedProposalsViewOption;
    
    private List<ViewOptionsEventRulesEnum> rulesViewOptions;
    private ViewOptionsEventRulesEnum selectedRulesViewOption;
    

    /**
     * Creates a new instance of OccPeriodWorkflowBB
     */
    public WorkflowBB() {
    }
    
    
    @PostConstruct
    public void initBean(){
       
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

    /**
     * Primary entry point for initiating the cascade of impacts from making a choice
     * @param choice
     * @param p 
     */
    public void proposals_makeChoice(Choice choice, Proposal p){
        WorkflowCoordinator wc = getWorkflowCoordinator();
        EventCoordinator ec = getEventCoordinator();
        List<EventCnF> evDoneList = null;
        StringBuilder sb = new StringBuilder();
        
        try {
            evDoneList = wc.evaluateProposal(p, choice, currentERG, getSessionBean().getSessUser());
            if(evDoneList != null && !evDoneList.isEmpty()){
                sb.append("Upon ");
                sb.append(String.valueOf(p.getProposalID()));
                sb.append(" the following events were added to ");
                sb.append(currentERG.discloseEventDomain().getTitle());
                sb.append(" With ID: " );
                sb.append(currentERG.getBObID());
                sb.append(": ");
            }
            sb.append(ec.buildEventInfoMessage(evDoneList));

            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                sb.toString(), ""));
            } catch (BObStatusException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                ex.getMessage(), ""));
        }
    }    
    
    /**
     * Passes the task of removing previous proposal evaluation to the
     * WorkflowCoordinator
     * @param p 
     */
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

      
}
