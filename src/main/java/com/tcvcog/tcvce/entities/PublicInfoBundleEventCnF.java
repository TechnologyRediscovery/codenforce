/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper class that stores an EventCnF that is stripped of all sensitive
 * information.
 * Look at the JavaDocs of the PublicInfoBundle Class for more information.
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundleEventCnF extends PublicInfoBundle{
    
    private EventCnF bundledEvent;
    private int cecaseID;
    private int periodID;
    private List<PublicInfoBundlePerson> personList; //A list of anonymized persons attached to the Event
    
    @Override
    public String toString(){
        
        return this.getClass().getName() + bundledEvent.getEventID();
        
    }

    public EventCnF getBundledEvent() {
        return bundledEvent;
    }

    /**
     * Remove all sensitive data from the EventCnF and set it in the
     * bundledEvent field.
     * @param input 
     */
    public void setBundledEvent(EventCnF input) {
        
        input.setUserCreator(new User());
        input.setCreationts(LocalDateTime.MIN);
        
        input.setLastUpdatedBy(new User());
        input.setLastUpdatedTS(LocalDateTime.MIN);
        
        input.setNotes("*****");
        input.setPersonList(new ArrayList<>());
        
        bundledEvent = input;
    }
    
    public int getCecaseID() {
        return cecaseID;
    }

    public void setCecaseID(int input) {
        
        cecaseID = input;
    }

    public int getPeriodID() {
        return periodID;
    }

    public void setPeriodID(int input) {
        
        periodID = input;
    }

    public List<PublicInfoBundlePerson> getPersonList() {
        return personList;
    }

    public void setPersonList(List<PublicInfoBundlePerson> personList) {
        this.personList = personList;
    }
    
}
