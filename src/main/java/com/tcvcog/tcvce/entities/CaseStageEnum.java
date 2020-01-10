/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 *
 * @author sylvia
 */
public enum CaseStageEnum {
    
    Investigation("Investigation", "caseStageInvestigationIconID"),
    Enforcement("Enforcement", "caseStageEnforcementIconID"),
    Citation("Citation", "caseStageCitationIconID"),
    Closed("Closed", "caseStageClosedIconID"),
    Unknown("Unknown", "caseStageUnknownIconID");
    
    private final String label;
    private final String iconPropertyLookup;
    
    private CaseStageEnum(String label, String icon){
        this.label = label;
        this.iconPropertyLookup = icon;
    }
    
    public String getLabel(){
        return label;
    }
    
    public String getIconPropertyLookup(){
        return iconPropertyLookup;
    }
    
    
}
