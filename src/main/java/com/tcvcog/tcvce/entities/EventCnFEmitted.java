/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 * A special subclass of EventCnF that contains emission information, 
 * most notably the emission enum that controls what to do when parent 
 * emitters are deactivated.
 * 
 * @author sylvia and Ellen Bascomb of Apartment 31Y
 */
public class EventCnFEmitted extends EventCnF{
    
    private int emissionID;
    private LocalDateTime emissionTS;
    private EventEmissionEnum emissionEnum;
    private User emissionUser;
    
    public EventCnFEmitted(EventCnF ev){
        super(ev);
    }
    
    public EventCnFEmitted(EventCnFEmitted evEm){
        super(evEm);
        this.emissionID = evEm.emissionID;
        this.emissionEnum = evEm.emissionEnum;
        this.emissionTS =  evEm.emissionTS;
        this.emissionUser = evEm.emissionUser;
    }

    /**
     * @return the emissionID
     */
    public int getEmissionID() {
        return emissionID;
    }

    /**
     * @return the emissionTS
     */
    public LocalDateTime getEmissionTS() {
        return emissionTS;
    }

    /**
     * @return the emissionEnum
     */
    public EventEmissionEnum getEmissionEnum() {
        return emissionEnum;
    }

    /**
     * @param emissionID the emissionID to set
     */
    public void setEmissionID(int emissionID) {
        this.emissionID = emissionID;
    }

    /**
     * @param emissionTS the emissionTS to set
     */
    public void setEmissionTS(LocalDateTime emissionTS) {
        this.emissionTS = emissionTS;
    }

    /**
     * @param emissionEnum the emissionEnum to set
     */
    public void setEmissionEnum(EventEmissionEnum emissionEnum) {
        this.emissionEnum = emissionEnum;
    }

    /**
     * @return the emissionUser
     */
    public User getEmissionUser() {
        return emissionUser;
    }

    /**
     * @param emissionUser the emissionUser to set
     */
    public void setEmissionUser(User emissionUser) {
        this.emissionUser = emissionUser;
    }
    
}
