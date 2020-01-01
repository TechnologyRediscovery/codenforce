/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor. 
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * Mapped to database table choiceproposal
 * 
 * @author sylvia
 */
public class ProposalCECase 
        extends Proposal{
    
  
    private int ceCaseID;
    private CECase ceCase;
    
  
     public ProposalCECase(Proposal pr){
        
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
        this.responseEvent = pr.responseEvent;

        this.notes = pr.notes;
        this.proposalRejected = pr.proposalRejected;

        this.order = pr.order;
    }

    /**
     * @return the ceCaseID
     */
    public int getCeCaseID() {
        return ceCaseID;
    }

    /**
     * @return the ceCase
     */
    public CECase getCeCase() {
        return ceCase;
    }

    /**
     * @param ceCaseID the ceCaseID to set
     */
    public void setCeCaseID(int ceCaseID) {
        this.ceCaseID = ceCaseID;
    }

    /**
     * @param ceCase the ceCase to set
     */
    public void setCeCase(CECase ceCase) {
        this.ceCase = ceCase;
    }

  
    
}
