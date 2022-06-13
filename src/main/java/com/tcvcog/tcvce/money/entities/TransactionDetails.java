/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.money.entities;

import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.IFace_trackedEntityLink;
import com.tcvcog.tcvce.entities.LinkedObjectRole;
import java.time.LocalDateTime;

/** 
 * Superclass of the sub-trees of classes that represent 
 * Charges and Payments as of June 2022 and perhaps later Adjustments
 * @author sylvia
 */
public class TransactionDetails 
        extends Transaction
 implements  IFace_trackedEntityLink{
    
    protected LocalDateTime trxCreatedTS;
    protected int trxCreatedByUserID;
    
    protected LocalDateTime trxLastUpdatedTS;
    protected int trxLastUpdatedByUserID;
    
    protected LocalDateTime trxDeactivatedTS;
    protected int trxDeactivatedByUserID;
    
    protected String trxNotes;
    
    public TransactionDetails(){
        
    }
    
    public TransactionDetails(TransactionDetails det){
        super(det);
        this.trxCreatedTS = det.trxCreatedTS;
        this.trxCreatedByUserID = det.trxCreatedByUserID;
        this.trxLastUpdatedTS = det.trxLastUpdatedTS;
        this.trxLastUpdatedByUserID = det.trxLastUpdatedByUserID;  
        this.trxDeactivatedTS = det.trxDeactivatedTS;
        this.trxDeactivatedByUserID = det.trxDeactivatedByUserID;
        this.trxNotes = det.trxNotes;
        
    }
    
     

    @Override
    public LocalDateTime getLinkCreatedTS() {
        return trxCreatedTS;
    }

    @Override
    public void setLinkCreatedTS(LocalDateTime ts) {
        trxCreatedTS = ts;
    }

    @Override
    public int getLinkCreatedByUserID() {
        return trxCreatedByUserID;
    }

    @Override
    public void setLinkCreatedByUserID(int userID) {
        trxCreatedByUserID = userID;
    }

    @Override
    public void setLinkSource(BOBSource source) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public BOBSource getLinkSource() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public LocalDateTime getLinkLastUpdatedTS() {
        return trxLastUpdatedTS;
    }

    @Override
    public void setLinkLastUpdatedTS(LocalDateTime ts) {
        this.trxLastUpdatedTS = ts;
    }

    @Override
    public int getLinkLastUpdatedByUserID() {
        return trxLastUpdatedByUserID;
    }

    @Override
    public void setLinkLastUpdatedByUserID(int userID) {
        this.trxLastUpdatedByUserID = userID;
    }

    @Override
    public LocalDateTime getLinkDeactivatedTS() {
        return this.trxDeactivatedTS;
    }

    @Override
    public void setLinkDeactivatedTS(LocalDateTime ts) {
        this.trxDeactivatedTS = ts;
    }

    @Override
    public int getLinkDeactivatedByUserID() {
        return this.trxDeactivatedByUserID;
    }

    @Override
    public void setLinkDeactivatedByUserID(int usrID) {
        this.trxDeactivatedByUserID = usrID;
    }

    @Override
    public boolean isLinkDeactivated() {
        return trxDeactivatedTS != null;
    }

    @Override
    public void setLinkNotes(String n) {
        this.trxNotes = n;
    }

    @Override
    public String getLinkNotes() {
        return trxNotes;
    }

    @Override
    public LinkedObjectRole getLinkedObjectRole() {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public void setLinkedObjectRole(LinkedObjectRole lor) {
        throw new UnsupportedOperationException("Not supported yet."); 
    }

    @Override
    public int getParentObjectID() {
        return transactionID;
    }
    
    
}
