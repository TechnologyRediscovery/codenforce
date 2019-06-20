/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 *
 * @author sylvia
 */
public class EventProposalImplementation {
    
    private int implementationID;
    
    
    private int proposalID;
    private EventProposal proposal;
    private int generatingEventID;
    private boolean currentUserCanEvaluateProposal;
    
    private User initiator;
    
    private User responderIntended;
    private User responderActual;
    
    private LocalDateTime activatesOn;
    private String activatesOnPretty;
    
    private LocalDateTime expiresOn;
    private String expiresOnPretty;
    
    private LocalDateTime responseTimestamp;
    private String responseTimePrettyDate;
    
    private boolean expiredorinactive;
    
    private int responseEventID;
    /**
     * Populating this has the danger of inciting infinite loops if somehow
     * the generating event is aso the response event
     */
    private Event responseEvent;
    
    private String notes;
    private boolean proposalRejected;

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
     * @return the responseEventID
     */
    public int getResponseEventID() {
        return responseEventID;
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
     * @param responseEventID the responseEventID to set
     */
    public void setResponseEventID(int responseEventID) {
        this.responseEventID = responseEventID;
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
     * @return the implementationID
     */
    public int getImplementationID() {
        return implementationID;
    }

    /**
     * @param implementationID the implementationID to set
     */
    public void setImplementationID(int implementationID) {
        this.implementationID = implementationID;
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
     * @return the responseEvent
     */
    public Event getResponseEvent() {
        return responseEvent;
    }

    /**
     * @param responseEvent the responseEvent to set
     */
    public void setResponseEvent(Event responseEvent) {
        this.responseEvent = responseEvent;
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
     * @return the expiredorinactive
     */
    public boolean isExpiredorinactive() {
        return expiredorinactive;
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
     * @param expiredorinactive the expiredorinactive to set
     */
    public void setExpiredorinactive(boolean expiredorinactive) {
        this.expiredorinactive = expiredorinactive;
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
     * @return the proposal
     */
    public EventProposal getProposal() {
        return proposal;
    }

    /**
     * @param proposal the proposal to set
     */
    public void setProposal(EventProposal proposal) {
        this.proposal = proposal;
    }

    /**
     * @return the currentUserCanEvaluateProposal
     */
    public boolean isCurrentUserCanEvaluateProposal() {
        return currentUserCanEvaluateProposal;
    }

    /**
     * @param currentUserCanEvaluateProposal the currentUserCanEvaluateProposal to set
     */
    public void setCurrentUserCanEvaluateProposal(boolean currentUserCanEvaluateProposal) {
        this.currentUserCanEvaluateProposal = currentUserCanEvaluateProposal;
    }
    
}
