/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.EventCnF;
import java.io.Serializable;

/**
 * Subclass used during first pass through with duplicate event infrastructure 
 * for both CEEvents and OccEvents. Replaced with the EventDomain enum flag on 
 EventCnF which signals which ID should be read.
 * @deprecated 
 * @author sylvia
 */
public  class       EventOccPeriod 
        extends     EventCnF 
        implements  Serializable {
    private int occPeriodID;
    
    public EventOccPeriod(EventCnF ev){
        this.eventID = ev.getEventID();
        this.category = ev.getCategory();

        this.dateOfRecord = ev.getDateOfRecord();
        this.timestamp = ev.getTimestamp();
        this.description = ev.getDescription();
        
        this.owner = ev.getOwner();
        this.discloseToMunicipality = ev.isDiscloseToMunicipality(); 
        this.discloseToPublic = ev.isDiscloseToPublic();
        this.active = ev.isActive();
        
        this.hidden = ev.isHidden();
        this.notes = ev.getNotes();
        this.personList = ev.getPersonList();
    }

    /**
     * @return the occPeriodID
     */
    public int getOccPeriodID() {
        return occPeriodID;
    }

    /**
     * @param occPeriodID the occPeriodID to set
     */
    public void setOccPeriodID(int occPeriodID) {
        this.occPeriodID = occPeriodID;
    }
    
    
}
