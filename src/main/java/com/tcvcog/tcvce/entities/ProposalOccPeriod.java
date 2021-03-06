/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor. 
 */
package com.tcvcog.tcvce.entities;

/**
 *
 * Mapped to database table choiceproposal
 * 
 * @author sylvia
 */
public class ProposalOccPeriod extends Proposal {
    
    public ProposalOccPeriod(Proposal pr, int occPeriodID){
        this.hostObjectID = occPeriodID;
        
        this.proposalID  = pr.proposalID;
        this.directive = pr.directive;

        this.generatingEventID = pr.generatingEventID;
        this.readOnlyCurrentUser = pr.readOnlyCurrentUser;

        this.initiator = pr.initiator;
        this.responderIntended = pr.responderIntended;
        this.responderActual = pr.responderActual;

        this.activatesOn = pr.activatesOn;

        this.expiresOn = pr.expiresOn;

        this.responseTS = pr.responseTS;

        this.active = pr.active;
        this.hidden = pr.hidden;

        this.generatingEvent = pr.generatingEvent;
        this.evaluationEvent = pr.evaluationEvent;

        this.notes = pr.notes;
        this.proposalRejected = pr.proposalRejected;

        this.order = pr.order;
    }

       
    
    
}
