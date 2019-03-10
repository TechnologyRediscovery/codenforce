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
public enum CaseStage {
    
    Investigation("Investigation"),
    Enforcement("Enforcement"),
    Citation("Citation"),
    Closed("Closed");
    
    private final String label;
    
    private CaseStage(String label){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    }
    
    
}
