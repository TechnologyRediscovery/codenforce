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
public class EventProposalResponse {
    
    private int reponseID;
    
    private User initiator;
    
    private User responderIntended;
    private User responderActual;
    
    private LocalDateTime responseTimestamp;
    private String responseTimePrettyDate;
    private String notes;
    private boolean proposalRejected;
    private int responseEventID;

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
     * @return the reponseID
     */
    public int getReponseID() {
        return reponseID;
    }

    /**
     * @param reponseID the reponseID to set
     */
    public void setReponseID(int reponseID) {
        this.reponseID = reponseID;
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
    
}
