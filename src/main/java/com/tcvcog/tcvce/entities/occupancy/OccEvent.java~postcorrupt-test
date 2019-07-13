/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.Event;
import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class OccEvent 
        extends Event 
        implements Serializable {
    
    private int occPeriodID;
    
    public OccEvent(Event ev){
        this.eventID = ev.getEventID();
        this.muniCode = ev.getMuniCode();
        this.muniName = ev.getMuniName();
        this.propertyID = ev.getPropertyID();
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
