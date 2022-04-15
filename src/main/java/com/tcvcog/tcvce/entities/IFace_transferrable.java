/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 * Interface specifying getters and setters for
 * TransferredTS
 * TransferredBy
 * and TransferredToCECasedID and the enum
 * 
 * @author sylvia
 */
public interface IFace_transferrable 
        extends IFace_keyIdentified {
    
    public void setTransferredTS(LocalDateTime ts);
    public void setTransferredBy(User usr);
    public void setTransferredToCECaseID(int ceCaseID);
    
    public LocalDateTime getTransferredTS();
    public User getTransferredBy();
    public int getTransferredToCECaseID();
    
    public TransferrableEnum getTransferEnum();
    
    
}
