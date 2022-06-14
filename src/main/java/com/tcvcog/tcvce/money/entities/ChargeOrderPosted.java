/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.money.entities;

import java.time.LocalDateTime;

/**
 * Represents a charge that has been posted to an account ledger
 * @author sylvia
 */
public      class   ChargeOrderPosted
            extends ChargeOrder{

    private int trxID;
    private int chargeOrderID;
    
    private LocalDateTime chgOrderPostedCreatedTS;
    private int chgOrderPostedCreatedByUserID;
    
    private LocalDateTime chgOrderPostedLastUpdatedTS;
    private int chgOrderPostedLastUpdatedByUserID;
    
    private LocalDateTime chgOrderPostedDeactivatedTS;
    private int chgOrderPostedDeactivatedByUserID;
    
    private String chgOrderPostedNotes;

    public ChargeOrderPosted(ChargeOrder chgOrder){
        super(chgOrder);
    }
    
    public ChargeOrderPosted(ChargeOrderPosted chgOrderPosted){
        super(chgOrderPosted);
        this.trxID = chgOrderPosted.trxID;
        this.chargeOrderID = chgOrderPosted.chargeOrderID;
        this.chgOrderPostedCreatedTS = chgOrderPosted.chgOrderPostedCreatedTS;
        this.chgOrderPostedCreatedByUserID = chgOrderPosted.chgOrderPostedCreatedByUserID;
        this.chgOrderPostedLastUpdatedTS = chgOrderPosted.chgOrderPostedLastUpdatedTS;
        this.chgOrderPostedLastUpdatedByUserID = chgOrderPosted.chgOrderPostedLastUpdatedByUserID;
        this.chgOrderPostedDeactivatedTS = chgOrderPosted.chgOrderPostedDeactivatedTS;
        this.chgOrderPostedDeactivatedByUserID = chgOrderPosted.chgOrderPostedDeactivatedByUserID;
        this.chgOrderPostedNotes = chgOrderPosted.chgOrderPostedNotes;
    }
    
    
    /**
     * @return the trxID
     */
    public int getTrxID() {
        return trxID;
    }

    /**
     * @return the chargeOrderID
     */
    public int getChargeOrderID() {
        return chargeOrderID;
    }

    /**
     * @return the chgOrderPostedCreatedTS
     */
    public LocalDateTime getChgOrderPostedCreatedTS() {
        return chgOrderPostedCreatedTS;
    }

    /**
     * @return the chgOrderPostedCreatedByUserID
     */
    public int getChgOrderPostedCreatedByUserID() {
        return chgOrderPostedCreatedByUserID;
    }

    /**
     * @return the chgOrderPostedLastUpdatedTS
     */
    public LocalDateTime getChgOrderPostedLastUpdatedTS() {
        return chgOrderPostedLastUpdatedTS;
    }

    /**
     * @return the chgOrderPostedLastUpdatedByUserID
     */
    public int getChgOrderPostedLastUpdatedByUserID() {
        return chgOrderPostedLastUpdatedByUserID;
    }

    /**
     * @return the chgOrderPostedDeactivatedTS
     */
    public LocalDateTime getChgOrderPostedDeactivatedTS() {
        return chgOrderPostedDeactivatedTS;
    }

    /**
     * @return the chgOrderPostedDeactivatedByUserID
     */
    public int getChgOrderPostedDeactivatedByUserID() {
        return chgOrderPostedDeactivatedByUserID;
    }

    /**
     * @return the chgOrderPostedNotes
     */
    public String getChgOrderPostedNotes() {
        return chgOrderPostedNotes;
    }

    /**
     * @param trxID the trxID to set
     */
    public void setTrxID(int trxID) {
        this.trxID = trxID;
    }

    /**
     * @param chargeOrderID the chargeOrderID to set
     */
    public void setChargeOrderID(int chargeOrderID) {
        this.chargeOrderID = chargeOrderID;
    }

    /**
     * @param chgOrderPostedCreatedTS the chgOrderPostedCreatedTS to set
     */
    public void setChgOrderPostedCreatedTS(LocalDateTime chgOrderPostedCreatedTS) {
        this.chgOrderPostedCreatedTS = chgOrderPostedCreatedTS;
    }

    /**
     * @param chgOrderPostedCreatedByUserID the chgOrderPostedCreatedByUserID to set
     */
    public void setChgOrderPostedCreatedByUserID(int chgOrderPostedCreatedByUserID) {
        this.chgOrderPostedCreatedByUserID = chgOrderPostedCreatedByUserID;
    }

    /**
     * @param chgOrderPostedLastUpdatedTS the chgOrderPostedLastUpdatedTS to set
     */
    public void setChgOrderPostedLastUpdatedTS(LocalDateTime chgOrderPostedLastUpdatedTS) {
        this.chgOrderPostedLastUpdatedTS = chgOrderPostedLastUpdatedTS;
    }

    /**
     * @param chgOrderPostedLastUpdatedByUserID the chgOrderPostedLastUpdatedByUserID to set
     */
    public void setChgOrderPostedLastUpdatedByUserID(int chgOrderPostedLastUpdatedByUserID) {
        this.chgOrderPostedLastUpdatedByUserID = chgOrderPostedLastUpdatedByUserID;
    }

    /**
     * @param chgOrderPostedDeactivatedTS the chgOrderPostedDeactivatedTS to set
     */
    public void setChgOrderPostedDeactivatedTS(LocalDateTime chgOrderPostedDeactivatedTS) {
        this.chgOrderPostedDeactivatedTS = chgOrderPostedDeactivatedTS;
    }

    /**
     * @param chgOrderPostedDeactivatedByUserID the chgOrderPostedDeactivatedByUserID to set
     */
    public void setChgOrderPostedDeactivatedByUserID(int chgOrderPostedDeactivatedByUserID) {
        this.chgOrderPostedDeactivatedByUserID = chgOrderPostedDeactivatedByUserID;
    }

    /**
     * @param chgOrderPostedNotes the chgOrderPostedNotes to set
     */
    public void setChgOrderPostedNotes(String chgOrderPostedNotes) {
        this.chgOrderPostedNotes = chgOrderPostedNotes;
    }

    
}
