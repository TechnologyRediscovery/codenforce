/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 * Specifies table and key names for entities that can be transferred to a ce case
 * 
 * @author sylvia
 */
public enum TransferrableEnum {
   
    CODE_VIOLATION      ("codeviolation",
                         "violationid"),
    INSPECTED_ELEMENT   ("occinspectedspaceelement",
                         "inspectedspaceelementid");
    
    protected final String targetTableID;
    protected final String targetPKField;
    
    private TransferrableEnum(String ttid, String pkfield){
        targetTableID = ttid;
        targetPKField = pkfield;
    }

    /**
     * @return the targetTableID
     */
    public String getTargetTableID() {
        return targetTableID;
    }

    /**
     * @return the targetPKField
     */
    public String getTargetPKField() {
        return targetPKField;
    }
    
    
}
