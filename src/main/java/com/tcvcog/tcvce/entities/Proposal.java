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
public class Proposal extends EntityUtils implements Serializable, Comparable<Proposal> {
    
    private int proposalID ;
    private Directive directive;
    
    private int generatingEventID;
    private boolean readOnlyCurrentUser;
    
    private User initiator;
    private User responderIntended;
    private User responderActual;
    
    private LocalDateTime activatesOn;
    private String activatesOnPretty;
    
    private LocalDateTime expiresOn;
    private String expiresOnPretty;
    
    private LocalDateTime responseTimestamp;
    private String responseTimePrettyDate;
    
    private boolean active;
    private boolean hidden;
    
    private Event generatingEvent;
    private Event responseEvent;

    private String notes;
    private boolean proposalRejected;
    
    private int order;
    private int cecaseID;
    private int occperiodID;

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
     * @param event
     * @return the responseTimePrettyDate
     */
    public String getResponseTimePrettyDate(Event event) {
        String pretty = event.getPrettyDate(responseTimestamp);
        responseTimePrettyDate = pretty;
        return getResponseTimePrettyDate();
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
     * @param responseTimePrettyDate the responseTimePrettyDate to set
     */
    public void setResponseTimePrettyDate(String responseTimePrettyDate) {
        this.responseTimePrettyDate = responseTimePrettyDate;
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
     * @param responseTimestamp the responseTimestamp to set
     */
    public void setResponseTimestamp(LocalDateTime responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }


    /**
     * @return the responseTimestamp
     */
    public LocalDateTime getResponseTimestamp() {
        return responseTimestamp;
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
     * @return the responseTimePrettyDate
     */
    public String getResponseTimePrettyDate() {
        return responseTimePrettyDate;
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
     * @return the activatesOnPretty
     */
    public String getActivatesOnPretty() {
        return activatesOnPretty;
    }

    /**
     * @return the expiresOn
     */
    public LocalDateTime getExpiresOn() {
        return expiresOn;
    }

    /**
     * @return the expiresOnPretty
     */
    public String getExpiresOnPretty() {
        return expiresOnPretty;
    }


    /**
     * @param activatesOn the activatesOn to set
     */
    public void setActivatesOn(LocalDateTime activatesOn) {
        this.activatesOn = activatesOn;
    }

    /**
     * @param activatesOnPretty the activatesOnPretty to set
     */
    public void setActivatesOnPretty(String activatesOnPretty) {
        this.activatesOnPretty = activatesOnPretty;
    }

    /**
     * @param expiresOn the expiresOn to set
     */
    public void setExpiresOn(LocalDateTime expiresOn) {
        this.expiresOn = expiresOn;
    }

    /**
     * @param expiresOnPretty the expiresOnPretty to set
     */
    public void setExpiresOnPretty(String expiresOnPretty) {
        this.expiresOnPretty = expiresOnPretty;
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

    /**
     * @return the cecaseID
     */
    public int getCecaseID() {
        return cecaseID;
    }

    /**
     * @return the occperiodID
     */
    public int getOccperiodID() {
        return occperiodID;
    }

    /**
     * @param cecaseID the cecaseID to set
     */
    public void setCecaseID(int cecaseID) {
        this.cecaseID = cecaseID;
    }

    /**
     * @param occperiodID the occperiodID to set
     */
    public void setOccperiodID(int occperiodID) {
        this.occperiodID = occperiodID;
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
        hash = 47 * hash + Objects.hashCode(this.activatesOnPretty);
        hash = 47 * hash + Objects.hashCode(this.expiresOn);
        hash = 47 * hash + Objects.hashCode(this.expiresOnPretty);
        hash = 47 * hash + Objects.hashCode(this.responseTimestamp);
        hash = 47 * hash + Objects.hashCode(this.responseTimePrettyDate);
        hash = 47 * hash + (this.active ? 1 : 0);
        hash = 47 * hash + (this.hidden ? 1 : 0);
        hash = 47 * hash + Objects.hashCode(this.generatingEvent);
        hash = 47 * hash + Objects.hashCode(this.responseEvent);
        hash = 47 * hash + Objects.hashCode(this.notes);
        hash = 47 * hash + (this.proposalRejected ? 1 : 0);
        hash = 47 * hash + this.order;
        hash = 47 * hash + this.cecaseID;
        hash = 47 * hash + this.occperiodID;
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
    
    
    
}
