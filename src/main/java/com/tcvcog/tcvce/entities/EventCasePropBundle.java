/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 * A composite class that contains the composing objects
 * namely Case associated with this event.
 * Designed to allow a list of these objects to exist all by itself
 * and the event can show the reader all of its related data.
 * 
 * 
 * @author sylvia
 */
public class EventCasePropBundle implements Serializable {
    
    private CECaseBaseClass eventCase;
    private EventCECase event;

    /**
     * @return the eventCase
     */
    public CECaseBaseClass getEventCaseBare() {
        return eventCase;
    }

    /**
     * @param eventCase the eventCase to set
     */
    public void setEventCaseBare(CECaseBaseClass eventCase) {
        this.eventCase = eventCase;
    }

    /**
     * @return the event
     */
    public EventCECase getEvent() {
        return event;
    }

  

    /**
     * @param event the event to set
     */
    public void setEvent(EventCECase event) {
        this.event = event;
    }

    
}
