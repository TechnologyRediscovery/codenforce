/*
 * Copyright (C) 2019 Technology Rediscovery LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.entities;

/**
 * A data-rich subclass of EventCnF to hold a CECase
 when we want to display the event outside of its CECase context
 but still want to know where it came from
 * 
 * @author Ellen Bascomb
 */
public  class   EventCaseHeavy 
        extends EventCnF{
    
    protected CECase ceCase;
    
    public EventCaseHeavy(EventCnF ev){
        this.eventID = ev.getEventID();
        this.category = ev.getCategory();
        
        this.domain = ev.getDomain();
        this.ceCaseID = ev.getCeCaseID();
        this.occPeriodID = ev.getOccPeriodID();

        this.timeStart = ev.getTimeStart();
        this.timeEnd = ev.getTimeEnd();
        
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
     * @return the ceCase
     */
    public CECase getCeCase() {
        return ceCase;
    }

    /**
     * @param ceCase the ceCase to set
     */
    public void setCeCase(CECase ceCase) {
        this.ceCase = ceCase;
    }

   
}
