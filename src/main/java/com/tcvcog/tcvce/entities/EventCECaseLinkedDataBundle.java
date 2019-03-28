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
public class EventCECaseLinkedDataBundle {
    private EventCECase event;
    private String cecaseName;
    private Property property;

    /**
     * @return the property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * @return the event
     */
    public EventCECase getEvent() {
        return event;
    }

    /**
     * @return the cecaseName
     */
    public String getCecaseName() {
        return cecaseName;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(EventCECase event) {
        this.event = event;
    }

    /**
     * @param cecaseName the cecaseName to set
     */
    public void setCecaseName(String cecaseName) {
        this.cecaseName = cecaseName;
    }
}
