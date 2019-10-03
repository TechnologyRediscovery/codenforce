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
 * Abstract because you can't propose to add an item in a set of things to nothin'
 * Mapped to database table choiceproposal
 * 
 * @author sylvia
 */
public class Proposal extends EntityUtils implements Serializable, Comparable<Proposal> {
    
    protected int proposalID ;
    protected Directive directive;
    
    protected boolean readOnlyCurrentUser;
    
    protected User initiator;
    protected User responderIntended;
    protected User responderActual;
    
    protected LocalDateTime activatesOn;
    protected LocalDateTime expiresOn;
    protected LocalDateTime responseTS;
    
    protected boolean active;
    protected boolean hidden;
    
    protected int generatingEventID;
    protected Event generatingEvent;
    protected Event responseEvent;

    protected String notes;
    protected boolean proposalRejected;
    
    protected int order;
    
    protected Proposable chosenChoice;

    /**
     * @param responderIntended the responderIntended to set
     */
    public void setResponderIntended(User responderIntended) {
        this.responderIntended = responderIntended;
    }


    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @return the responderActual
     */
    public User getResponderActual() {
        return responderActual;
    }

   

    /**
     * @param proposalRejected the proposalRejected to set
     */
    public void setProposalRejected(boolean proposalRejected) {
        this.proposalRejected = proposalRejected;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

   
    /**
     * @return the proposalRejected
     */
    public boolean isProposalRejected() {
        return proposalRejected;
    }

    /**
     * @return the responderIntended
     */
    public User getResponderIntended() {
        return responderIntended;
    }

    /**
     * @param responseTS the responseTS to set
     */
    public void setResponseTS(LocalDateTime responseTS) {
        this.responseTS = responseTS;
    }


    /**
     * @return the responseTS
     */
    public LocalDateTime getResponseTS() {
        return responseTS;
    }

    /**
     * @return the initiator
     */
    public User getInitiator() {
        return initiator;
    }

    /**
     * @param initiator the initiator to set
     */
    public void setInitiator(User initiator) {
        this.initiator = initiator;
    }

    /**
     * @return the proposalID
     */
    public int getProposalID() {
        return proposalID;
    }

    /**
     * @param proposalID the proposalID to set
     */
    public void setProposalID(int proposalID) {
        this.proposalID = proposalID;
    }

    

    /**
     * @param responderActual the responderActual to set
     */
    public void setResponderActual(User responderActual) {
        this.responderActual = responderActual;
    }


    
    /**
     * @return the activatesOn
     */
    public LocalDateTime getActivatesOn() {
        return activatesOn;
    }

    

    /**
     * @return the expiresOn
     */
    public LocalDateTime getExpiresOn() {
        return expiresOn;
    }

    

    /**
     * @param activatesOn the activatesOn to set
     */
    public void setActivatesOn(LocalDateTime activatesOn) {
        this.activatesOn = activatesOn;
    }

    

    /**
     * @param expiresOn the expiresOn to set
     */
    public void setExpiresOn(LocalDateTime expiresOn) {
        this.expiresOn = expiresOn;
    }

    


    /**
     * @return the generatingEventID
     */
    public int getGeneratingEventID() {
        return generatingEventID;
    }

    /**
     * @param generatingEventID the generatingEventID to set
     */
    public void setGeneratingEventID(int generatingEventID) {
        this.generatingEventID = generatingEventID;
    }


    /**
     * @return the readOnlyCurrentUser
     */
    public boolean isReadOnlyCurrentUser() {
        return readOnlyCurrentUser;
    }

    /**
     * @param readOnlyCurrentUser the readOnlyCurrentUser to set
     */
    public void setReadOnlyCurrentUser(boolean readOnlyCurrentUser) {
        this.readOnlyCurrentUser = readOnlyCurrentUser;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the directive
     */
    public Directive getDirective() {
        return directive;
    }

    /**
     * @param directive the directive to set
     */
    public void setDirective(Directive directive) {
        this.directive = directive;
    }

    /**
     * @return the hidden
     */
    public boolean isHidden() {
        return hidden;
    }

    /**
     * @return the generatingEvent
     */
    public Event getGeneratingEvent() {
        return generatingEvent;
    }

    /**
     * @return the responseEvent
     */
    public Event getResponseEvent() {
        return responseEvent;
    }

    /**
     * @param hidden the hidden to set
     */
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    /**
     * @param generatingEvent the generatingEvent to set
     */
    public void setGeneratingEvent(Event generatingEvent) {
        this.generatingEvent = generatingEvent;
    }

    /**
     * @param responseEvent the responseEvent to set
     */
    public void setResponseEvent(Event responseEvent) {
        this.responseEvent = responseEvent;
    }

    /**
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * @param order the order to set
     */
    public void setOrder(int order) {
        this.order = order;
    }

   


    @Override
    public int compareTo(Proposal p) {
        if(this.order < p.getOrder()){
            return -1;
            
        } else if (this.order > p.getOrder()){
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.proposalID;
        hash = 47 * hash + Objects.hashCode(this.directive);
        hash = 47 * hash + this.generatingEventID;
        hash = 47 * hash + (this.readOnlyCurrentUser ? 1 : 0);
        hash = 47 * hash + Objects.hashCode(this.initiator);
        hash = 47 * hash + Objects.hashCode(this.responderIntended);
        hash = 47 * hash + Objects.hashCode(this.responderActual);
        hash = 47 * hash + Objects.hashCode(this.activatesOn);
        hash = 47 * hash + Objects.hashCode(this.expiresOn);
        hash = 47 * hash + Objects.hashCode(this.responseTS);
        hash = 47 * hash + (this.active ? 1 : 0);
        hash = 47 * hash + (this.hidden ? 1 : 0);
        hash = 47 * hash + Objects.hashCode(this.generatingEvent);
        hash = 47 * hash + Objects.hashCode(this.responseEvent);
        hash = 47 * hash + Objects.hashCode(this.notes);
        hash = 47 * hash + (this.proposalRejected ? 1 : 0);
        hash = 47 * hash + this.order;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Proposal other = (Proposal) obj;
        if (this.proposalID != other.proposalID) {
            return false;
        }
        if (!Objects.equals(this.directive, other.directive)) {
            return false;
        }
        return true;
    }

    /**
     * @return the chosenChoice
     */
    public Proposable getChosenChoice() {
        return chosenChoice;
    }

    /**
     * @param chosenChoice the chosenChoice to set
     */
    public void setChosenChoice(Proposable chosenChoice) {
        this.chosenChoice = chosenChoice;
    }

    
    
    
}
