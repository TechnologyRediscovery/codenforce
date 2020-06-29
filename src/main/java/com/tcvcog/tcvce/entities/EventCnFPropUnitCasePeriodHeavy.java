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

import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;

/**
 * A data-rich subclass of EventCnF for use in contexts
 where the event needs to carry info about its parent objects
 in a non-parental way (i.e. the subclass having member vars to 
 * hold these BObs.
 * 
 * The companion to this class is EventCaseHeavy which contains a CECase object
 * 
 * We need both the Property and the PropertyUnit objects since PropertyUnit objects
 * don't hold an actual Property, so we can't get an address
 * 
 * @author Ellen Bascomb
 */
public  class   EventCnFPropUnitCasePeriodHeavy 
        extends EventCnF{
    
    private OccPeriodPropertyUnitHeavy period;
    private CECasePropertyUnitHeavy cecase;
    
    public EventCnFPropUnitCasePeriodHeavy(EventCnF ev){
        this.eventID = ev.getEventID();
        this.category = ev.getCategory();
        this.description = ev.getDescription();
        
        this.domain = ev.getDomain();
        this.ceCaseID = ev.getCeCaseID();
        this.occPeriodID = ev.getOccPeriodID();

        this.timeStart = ev.getTimeStart();
        this.timeEnd = ev.getTimeEnd();
        
        this.userCreator = ev.getUserCreator();
        this.creationts = ev.getCreationts();
        
        this.lastUpdatedBy = ev.getLastUpdatedBy();
        this.lastUpdatedTS = ev.getLastUpdatedTS();
        
        this.active = ev.isActive();
        this.hidden = ev.isHidden();
        
        this.notes = ev.getNotes();
        this.personList = ev.getPersonList();
    }

    public Property getProperty(){
        Property p = null;
        switch(domain){
            case CODE_ENFORCEMENT:
                if(cecase != null){
                    p = cecase.getProperty();
                }
                break;
            case OCCUPANCY:
                if(period != null){
                    p = period.getPropUnitProp().getProperty();
                }
        }
        return p;
    }
    
    public PropertyUnit getPropertyUnit(){
        PropertyUnit pu = null;
        switch(domain){
            case OCCUPANCY:
                if(period != null){
                    pu = period.getPropUnitProp();
                }
                break;
            case CODE_ENFORCEMENT:
                if(cecase != null){
                    pu = cecase.getPropUnit();
                }
        }
        return pu;
    }
    

    /**
     * @return the period
     */
    public OccPeriod getPeriod() {
        return period;
    }

    /**
     * @return the cecase
     */
    public CECase getCecase() {
        return cecase;
    }

    /**
     * @param cecase the cecase to set
     */
    public void setCecase(CECasePropertyUnitHeavy cecase) {
        this.cecase = cecase;
    }

    /**
     * @param period the period to set
     */
    public void setPeriod(OccPeriodPropertyUnitHeavy period) {
        this.period = period;
    }
    
}
