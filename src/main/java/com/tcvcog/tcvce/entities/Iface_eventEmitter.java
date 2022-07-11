/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.util.List;

/**
 * Specifies getters implemented by BObs that spawn events;
 * @author sylvia
 */
public interface Iface_eventEmitter extends IFace_keyIdentified {

    /**
     * Asks the implementer for the categories that it emits
     * @param eeenum should not explode if passed in as null; Used for objects that
     * emit more than one category of event, and passing in this value will get
     * you the desired category from the appropriate member
     * @return 
     */
    public EventCategory getEventCategoryEmitted(EventEmissionEnum eeenum);
    
    /**
     * Setter for emitted events
     * @param evList 
     */
    public void setEmittedEvents(List<EventCnFEmitted> evList);
    
    /**
     * GEtter for emitted events
     * @return 
     */
    public List<EventCnFEmitted> getEmittedEvents();
    
    

    
}
