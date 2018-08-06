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
public class EventWithCasePropInfo extends EventCase {
    
    private Property eventProp;
    private CECase eventCase;

    /**
     * @return the eventProp
     */
    public Property getEventProp() {
        return eventProp;
    }

    /**
     * @param eventProp the eventProp to set
     */
    public void setEventProp(Property eventProp) {
        this.eventProp = eventProp;
    }

    /**
     * @return the eventCase
     */
    public CECase getEventCase() {
        return eventCase;
    }

    /**
     * @param eventCase the eventCase to set
     */
    public void setEventCase(CECase eventCase) {
        this.eventCase = eventCase;
    }
    
}
