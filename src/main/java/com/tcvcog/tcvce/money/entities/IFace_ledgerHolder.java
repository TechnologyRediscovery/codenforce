/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.money.entities;

import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.IFace_keyIdentified;

/**
 * Declares a small family of objects which hold a ledger in their bellies;
 * Implementing objects included only OccPeriod and CECase
 * @author sylvia
 */
public interface IFace_ledgerHolder 
        extends IFace_keyIdentified{
    
    public void setMoneyLedger(MoneyLedger ledger);
    public MoneyLedger getMoneyLedger();
    public DomainEnum getDomain();
    
}
