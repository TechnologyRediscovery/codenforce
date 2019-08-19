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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Openable;
import com.tcvcog.tcvce.entities.Proposable;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.ProposalCECase;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserWithAccessData;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.integration.ChoiceIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Iterator;

/**
 *
 * @author sylvia
 */
public class ChoiceCoordinator extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of ChoiceCoordinator
     */
    public ChoiceCoordinator() {
    }
    
    public CECase configureProposals(CECase cse, User u) throws EventException, AuthorizationException{
        Iterator<Proposal> iter = cse.getProposalList().iterator();
        while(iter.hasNext()){
            Proposal p = iter.next();
            configureProposal(p, u);
//            if(p.getDirective().getChoiceList().size() == 1
//                    &&
//                p.getDirective().isExecuteChoiceIfLoneWolf()){
//                    processProposalEvaluation(p, p.getDirective().getChoiceList().get(0), cse, u);
//            }
        }
        
        
        return cse;
    }
    
    public OccPeriod configureProposals(OccPeriod oPeriod, User u) throws EventException, AuthorizationException{
        Iterator<Proposal> iter = oPeriod.getProposalList().iterator();
        while(iter.hasNext()){
            Proposal p = iter.next();
            configureProposal(p, u);
//            if(p.getDirective().getChoiceList().size() == 1
//                    &&
//                p.getDirective().isExecuteChoiceIfLoneWolf()){
//                    evaluateProposal(p, p.getDirective().getChoiceList().get(0), oPeriod, u);
//            }
        }
        
        return oPeriod;
    }
    
    private Proposal configureProposal( Proposal proposal, 
                                        User u){
        
        // start by  setting the most restrictive rights and then relax them as authorization
        // status allows
        proposal.setHidden(true);
        proposal.setReadOnlyCurrentUser(true);

        // hide inactives and exit
        if(!proposal.isActive()){
            return proposal;
        }
        
        if(proposal.getActivatesOn().isBefore(LocalDateTime.now()) && proposal.getExpiresOn().isAfter((LocalDateTime.now()))){
            if(u.getRoleType().getRank() >= proposal.getDirective().getMinimumRequiredUserRankToView()){
                proposal.setHidden(false);
                if(u.getRoleType().getRank() >= proposal.getDirective().getMinimumRequiredUserRankToEvaluate()){
                    proposal.setReadOnlyCurrentUser(false);
                }
                // this will only execute if we are unhidden
                configureChoiceList(proposal, u);
            }
        }
        
        return proposal;
    }
    
    
    public Proposal configureChoiceList(Proposal proposal, User u){
        Iterator<Proposable> iter = proposal.getDirective().getChoiceList().iterator();
        while(iter.hasNext()){
            Proposable p = iter.next();
            configureChoice(p, u);
        }
        return proposal;
    }
    
    private Proposable configureChoice(Proposable choice, User u){
        choice.setHidden(true);
        choice.setCanChoose(false);
        
        // hide inactives and exit
        if(!choice.isActive()){
            return choice;
        }
        
         if(u.getRoleType().getRank() >= choice.getMinimumRequiredUserRankToView()){
                choice.setHidden(false);
                if(u.getRoleType().getRank() >= choice.getMinimumRequiredUserRankToChoose()){
                    choice.setCanChoose(true);
                }
            }
        return choice;
    }
    
    public boolean determineProposalEvaluatability( Proposal proposal,
                                                    Proposable chosen, 
                                                    User u){
        
        // our proposal must contain our desired choice
        if(!proposal.getDirective().getChoiceList().contains(chosen)){
            return false;
        }
        // we must be allowed to choose the choice
        if(!chosen.isCanChoose()){
            return false;
        }
        if(!proposal.isActive()){
            return false;
        }
        if(!(proposal.getActivatesOn().isBefore(LocalDateTime.now()) 
                && proposal.getExpiresOn().isAfter((LocalDateTime.now())))){
            return false;
        }
        return true;
    }
    
    public void recordProposalEvaluation(Proposal p) throws IntegrationException{
        ChoiceIntegrator ci = getChoiceIntegrator();
        p.setHidden(true);
        ci.recordProposalEvaluation(p);
    }
    
    /**
     * Processes requests to reject a proposal by checking user rank, required status, 
     * and the CECase's or OccPeriod's open/closed status
     * @param p to be rejected
     * @param bob this interface allows you to ask the object if it's open or closed. For Occbeta, this is only
     * OccPeriod and CECase objects
     * @param u the current session user
     * @throws IntegrationException
     * @throws AuthorizationException
     * @throws CaseLifecycleException if the directive is required for bob close and if it is open.
     * This method does not allow evaluation of a required proposal after BOB is closed. 
     * If this occurs, there's a bug somewhere in the entitylifecycle that anybody could have closed this bob
     */
    public void rejectProposal(Proposal p, Openable bob, User u) throws IntegrationException, AuthorizationException, CaseLifecycleException{
        ChoiceIntegrator ci = getChoiceIntegrator();
        if(u.getRoleType().getRank() >= p.getDirective().getMinimumRequiredUserRankToEvaluate()){
            if(!p.getDirective().isRequiredEvaluationForBOBClose() && bob.isOpen()){
                // configure our proposal for rejection
                p.setProposalRejected(true);
                p.setResponderActual(u);
                p.setResponseTimestamp(LocalDateTime.now());
                p.setHidden(true);
                // send the updates to the integrator
                ci.updateProposal(p);
            } else {
                throw new CaseLifecycleException("Evaluating this proposal is required. This setting can be overriden by an administrator.");
            }
        } else {
            throw new AuthorizationException("You do not have sufficient privileges to reject this propsoal");
        }
    }
}
