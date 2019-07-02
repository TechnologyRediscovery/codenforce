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
public class EventCECaseCasePropBundle 
        implements Serializable, Comparable<EventCECaseCasePropBundle> {
    
    private CECaseBaseClass eventCase;
    private CECaseEvent event;

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
    public CECaseEvent getEvent() {
        return event;
    }

  

    /**
     * @param event the event to set
     */
    public void setEvent(CECaseEvent event) {
        this.event = event;
    }

    @Override
    public int compareTo(EventCECaseCasePropBundle ev) {
        int c = this.event.getDateOfRecord().compareTo(ev.event.getDateOfRecord());
        return c;
    }

    
}
