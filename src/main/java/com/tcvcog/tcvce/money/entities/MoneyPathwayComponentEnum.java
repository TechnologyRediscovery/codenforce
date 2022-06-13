/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.money.entities;

import com.tcvcog.tcvce.money.entities.TnxTypeEnum;

/**
 * Enumerates the various components the user can be provided by the UI
 * @author sylvia
 */
public enum MoneyPathwayComponentEnum {
    PAY_MUNICIPAY ( TnxTypeEnum.PAYMENT,
                    "pay-municipay",
                    20),
    PAY_CHECK(TnxTypeEnum.PAYMENT,
                    "pay-check",
                    10),
    CHARGE_AUTOMATIC(TnxTypeEnum.CHARGE,
                    "charge-auto",
                    30),
    CHARGE_MANUAL(TnxTypeEnum.CHARGE,
                    "charge-manual",
                    40),
    ADJUSTMENT(TnxTypeEnum.ADJUSTMENT,
                    "adjustment",
                    50);
    
    private final TnxTypeEnum tnxType;
    private final String componentID;
    private final int tnxSourceID;
    
    private MoneyPathwayComponentEnum(TnxTypeEnum tpe, String comp, int srcid){
        tnxType = tpe;
        componentID = comp;
        tnxSourceID = srcid;
    }

    /**
     * @return the tnxType
     */
    public TnxTypeEnum getTnxType() {
        return tnxType;
    }

    /**
     * @return the componentID
     */
    public String getComponentID() {
        return componentID;
    }

    /**
     * @return the tnxSourceID
     */
    public int getTnxSourceID() {
        return tnxSourceID;
    }
    
    
    
    
}
