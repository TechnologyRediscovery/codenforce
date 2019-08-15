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
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Proposable;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.ProposalCECase;
import com.tcvcog.tcvce.entities.ProposalOccPeriod;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserWithAccessData;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
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
            if(p.getDirective().getChoiceList().size() == 1
                    &&
                p.getDirective().isExecuteChoiceIfLoneWolf()){
                    processProposalEvaluation(p, p.getDirective().getChoiceList().get(0), cse, u);
            }
        }
        
        
        return cse;
    }
    
    public OccPeriod configureProposals(OccPeriod oPeriod, User u) throws EventException, AuthorizationException{
        Iterator<Proposal> iter = oPeriod.getProposalList().iterator();
        while(iter.hasNext()){
            Proposal p = iter.next();
            configureProposal(p, u);
            if(p.getDirective().getChoiceList().size() == 1
                    &&
                p.getDirective().isExecuteChoiceIfLoneWolf()){
                    processProposalEvaluation(p, p.getDirective().getChoiceList().get(0), oPeriod, u);
            }
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
    
    
    
    public CECase processProposalEvaluation(    Proposal proposal, 
                                                Proposable chosen, 
                                                CECase cse, 
                                                User u) 
                                                throws EventException, AuthorizationException{
        

        // first make sure that the given Proposable is in the Proposal
        if(!proposal.getDirective().getChoiceList().contains(chosen)){
            throw new EventException("The identified chosen Proposable is not contained inside the given Proposal");
        }
        // check authorization
        configureChoice(chosen, u);
        if(!chosen.isCanChoose()){
            throw new AuthorizationException("You do not have permission to select this Choice");
        }
        
        
        
        
        return cse;
    }
    
    public OccPeriod processProposalEvaluation( Proposal proposal, 
                                                Proposable chosen, 
                                                OccPeriod oPeriod, 
                                                User u) throws EventException, AuthorizationException{
                                            
        OccupancyIntegrator oi = getOccupancyIntegrator();
        
        // first make sure that the given Proposable is in the Proposal
        if(!proposal.getDirective().getChoiceList().contains(chosen)){
            throw new EventException("The identified chosen Proposable is not contained inside the given Proposal");
        }
        // check authorization
        configureChoice(chosen, u);
        if(!chosen.isCanChoose()){
            throw new AuthorizationException("You do not have permission to select this Choice");
        }
        
        
        
        return oPeriod;
        
    }
    
    
    
    
}
