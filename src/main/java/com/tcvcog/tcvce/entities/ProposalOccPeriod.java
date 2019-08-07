/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor. 
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * Mapped to database table choiceproposal
 * 
 * @author sylvia
 */
public class ProposalOccPeriod extends Proposal {
    
    private int occperiodID;
    private OccPeriod period;
    
    public ProposalOccPeriod(Proposal pr){
        
        this.proposalID  = pr.proposalID;
        this.directive = pr.directive;

        this.generatingEventID = pr.generatingEventID;
        this.readOnlyCurrentUser = pr.readOnlyCurrentUser;

        this.initiator = pr.initiator;
        this.responderIntended = pr.responderIntended;
        this.responderActual = pr.responderActual;

        this.activatesOn = pr.activatesOn;

        this.expiresOn = pr.expiresOn;

        this.responseTimestamp = pr.responseTimestamp;

        this.active = pr.active;
        this.hidden = pr.hidden;

        this.generatingEvent = pr.generatingEvent;
        this.responseEvent = pr.responseEvent;

        this.notes = pr.notes;
        this.proposalRejected = pr.proposalRejected;

        this.order = pr.order;
    }

    /**
     * @return the occperiodID
     */
    public int getOccperiodID() {
        return occperiodID;
    }

    /**
     * @return the period
     */
    public OccPeriod getPeriod() {
        return period;
    }

    /**
     * @param occperiodID the occperiodID to set
     */
    public void setOccperiodID(int occperiodID) {
        this.occperiodID = occperiodID;
    }

    /**
     * @param period the period to set
     */
    public void setPeriod(OccPeriod period) {
        this.period = period;
    }
    

  
    
    
    
}
