/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 * Enumerates business objects which spawn events
 * 
 * @author sylvia
 */
public enum EventEmissionEnum {
    
    NOTICE_OF_VIOLATION_SENT("Notice of Violation: Sent", false),
    NOTICE_OF_VIOLATION_FOLLOWUP("Notice of Violation: Followup", true),
    NOTICE_OF_VIOLATION_RETURNED("Notice of Violation: Returned", false),
    TRANSACTION("Transaction", false),
    CITATION_HEARING("Citation: Hearing scheduled", true);
    
    private final String emissionTitle;
    private final boolean deactivateEventOnParentDeactivation;
    
    private EventEmissionEnum(String t, boolean deac){
        emissionTitle = t;
        deactivateEventOnParentDeactivation = deac;
    }

    /**
     * @return the emissionTitle
     */
    public String getEmissionTitle() {
        return emissionTitle;
    }

    /**
     * @return the deactivateEventOnParentDeactivation
     */
    public boolean isDeactivateEventOnParentDeactivation() {
        return deactivateEventOnParentDeactivation;
    }
}
