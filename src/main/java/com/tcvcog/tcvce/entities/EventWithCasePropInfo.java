/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 * A subclass of EventCECase that contains the composing objects
 * namely the Property and Case associted with this event.
 * Designed to allow a list of these objects to exist all by itself
 * and the event can show the reader all of its related data.
 * 
 * Heavy for loading as each object has potentially hundreds of other objects
 gb* inside it
 * 
 * @author sylvia
 */
public class EventWithCasePropInfo extends EventCECase implements Serializable {
    
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
