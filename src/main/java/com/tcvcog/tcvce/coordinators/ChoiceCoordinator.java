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
import com.tcvcog.tcvce.application.interfaces.IFace_ProposalDriven;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Directive;
import com.tcvcog.tcvce.entities.Event;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.ProposalCECase;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.integration.ChoiceIntegrator;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Iterator;
import com.tcvcog.tcvce.entities.IFace_Openable;
import com.tcvcog.tcvce.entities.IFace_Proposable;

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
    
    public CECase configureProposals(CECase cse, UserAuthorized u) throws EventException, AuthorizationException{
        if(cse.getProposalList() != null && u != null){
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
        }
        return cse;
    }
    
    /**
     * Extracts the List&lt;Proposal&gt; from 
     * @param oPeriod
     * @param u
     * @return
     * @throws EventException
     * @throws AuthorizationException 
     */
    public OccPeriodDataHeavy configureProposals(OccPeriodDataHeavy oPeriod, UserAuthorized u) throws EventException, AuthorizationException{
        if(oPeriod != null && u != null){
            if(oPeriod.getProposalList() != null){
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
            }
        }
        return oPeriod;
    }
    
    /**
     * Coordinator internal logic container for setting switches on Proposals
     * @param proposal
     * @param u
     * @return 
     */
    private Proposal configureProposal( Proposal proposal, 
                                        UserAuthorized u){
        
        if(proposal != null && u != null){

            // start by  setting the most restrictive rights and then relax them as authorization
            // status allows
            proposal.setHidden(true);
            proposal.setReadOnlyCurrentUser(true);

            // hide inactives and exit
            if(!proposal.isActive()){
                return proposal;
            }

            if(proposal.getActivatesOn() != null && proposal.getExpiresOn() != null){
                if(proposal.getActivatesOn().isBefore(LocalDateTime.now()) && proposal.getExpiresOn().isAfter((LocalDateTime.now()))){
                    proposal.setHidden(false);

                }
            }
            if(u.getRole().getRank() >= proposal.getDirective().getMinimumRequiredUserRankToView()){
                proposal.setHidden(false);
                if(u.getRole().getRank() >= proposal.getDirective().getMinimumRequiredUserRankToEvaluate()){
                    proposal.setReadOnlyCurrentUser(false);
                }
            }
            configureChoiceList(proposal, u);
        }
        return proposal;
    }
    
    public Proposal configureChoiceList(Proposal proposal, UserAuthorized u){
        if(proposal != null && u != null){
            if(proposal.getDirective().getChoiceList() != null){
                Iterator<IFace_Proposable> iter = proposal.getDirective().getChoiceList().iterator();
                while(iter.hasNext()){
                    IFace_Proposable p = iter.next();
                    configureChoice(p, u);
                }
            }
        }
        return proposal;
    }
    
    private IFace_Proposable configureChoice(IFace_Proposable choice, UserAuthorized u){
        if(choice != null && u != null){
            choice.setHidden(true);
            choice.setCanChoose(false);

            // hide inactives and exit
            if(!choice.isActive()){
                return choice;
            }
             if(u.getRole().getRank() >= choice.getMinimumRequiredUserRankToView()){
                    choice.setHidden(false);
                    if(u.getRole().getRank() >= choice.getMinimumRequiredUserRankToChoose()){
                        choice.setCanChoose(true);
                    }
            }
        }
        return choice;
    }
    
    public boolean determineProposalEvaluatability( Proposal proposal,
                                                    IFace_Proposable chosen, 
                                                    User u){
        if(proposal == null || chosen == null || u== null){
            return false;
        }
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
        if(proposal.getActivatesOn() != null && proposal.getExpiresOn() != null){
            if(!(proposal.getActivatesOn().isBefore(LocalDateTime.now()) 
                    && proposal.getExpiresOn().isAfter((LocalDateTime.now())))){
                return false;
            }
            
        }
        return true;
    }
    
    public void recordProposalEvaluation(Proposal p) throws IntegrationException{
        ChoiceIntegrator ci = getChoiceIntegrator();
        p.setHidden(true);
        ci.recordProposalEvaluation(p);
    }
    
    /**
     * Takes in a Directive object and an OccPeriod or CECase and 
     * implements that directive by assigning it via a Proposal given sensible initial values
     * @param dir Extracted from the Event to be implemented
     * @param propDriven which in beta v.0.9 are CECase and OccPeriod objects
     * @param ev 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void implementDirective(Directive dir, IFace_ProposalDriven propDriven, Event ev) throws IntegrationException{
        ChoiceIntegrator ci = getChoiceIntegrator();
        Proposal pr = new Proposal();
        pr.setDirective(dir);
        if(dir.isActive()){
            pr.setActive(true);
            pr.setActivatesOn(LocalDateTime.now());
            pr.setHidden(false);
            pr.setProposalRejected(false);
            pr.setOrder(0);
        } else {
            return;
        }
              
        
        if(ev != null){
            pr.setGeneratingEvent(ev);
            pr.setGeneratingEventID(ev.getEventID());
        }
        
        if(propDriven instanceof OccPeriod){
            OccPeriod op = (OccPeriod) propDriven;
            if(!op.isOpen() && !dir.isApplyToClosedBOBs()){
                return;
            }
            ProposalOccPeriod pop = new ProposalOccPeriod(pr);
            pop.setOccperiodID(op.getPeriodID());
            pop.setPeriod(op);
            ci.insertProposal(pop);
            
        } else if(propDriven instanceof CECase){
            CECase cse = (CECase) propDriven;
            if(!cse.isOpen() && !dir.isApplyToClosedBOBs()){
                return;
            }
            ProposalCECase pcec = new ProposalCECase(pr);
            pcec.setCeCase(cse);
            pcec.setCeCaseID(cse.getCaseID());
            ci.insertProposal(pcec);
        }
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
     * @throws BObStatusException if the directive is required for bob close and if it is open.
     * This method does not allow evaluation of a required proposal after BOB is closed. 
     * If this occurs, there's a bug somewhere in the entitylifecycle that anybody could have closed this bob
     */
    public void rejectProposal(Proposal p, IFace_Openable bob, UserAuthorized u) throws IntegrationException, AuthorizationException, BObStatusException{
        ChoiceIntegrator ci = getChoiceIntegrator();
        if(u.getRole().getRank() >= p.getDirective().getMinimumRequiredUserRankToEvaluate()){
            if(!p.getDirective().isRequiredEvaluationForBOBClose() && bob.isOpen()){
                // configure our proposal for rejection
                p.setProposalRejected(true);
                p.setResponderActual(u);
                p.setResponseTS(LocalDateTime.now());
                p.setHidden(true);
                // send the updates to the integrator
                ci.updateProposal(p);
            } else {
                throw new BObStatusException("Evaluating this proposal is required. This setting can be overriden by an administrator.");
            }
        } else {
            throw new AuthorizationException("You do not have sufficient privileges to reject this propsoal");
        }
    }
    
    public void clearProposalEvaluation(Proposal p, UserAuthorized u) throws IntegrationException, BObStatusException{
        ChoiceIntegrator ci = getChoiceIntegrator();
        if(p.isReadOnlyCurrentUser()){
            throw new BObStatusException("User cannot clear a proposal they cannot evaluate");
        }
        p.setResponseTS(null);
        p.setResponderActual(null);
        p.setResponseEvent(null);
        p.setProposalRejected(false);
        ci.updateProposal(p);
    }
}