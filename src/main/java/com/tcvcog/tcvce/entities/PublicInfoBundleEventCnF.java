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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundleEventCnF extends PublicInfoBundle implements Serializable{
    
    private EventCnF bundledEvent;
    private PublicInfoBundleCECase cecase;
    private PublicInfoBundleOccPeriod period;
    private List<PublicInfoBundlePerson> personList;
    
    public void setBundledEvent(EventCnF input) {
        
        input.setUserCreator(new User());
        input.setCreationts(LocalDateTime.MIN);
        
        input.setLastUpdatedBy(new User());
        input.setLastUpdatedTS(LocalDateTime.MIN);
        
        input.setNotes("*****");
        
        bundledEvent = input;
    }
    
    @Override
    public String toString(){
        
        return this.getClass().getName() + bundledEvent.getEventID();
        
    }

    public EventCnF getBundledEvent() {
        return bundledEvent;
    }

    public PublicInfoBundleCECase getCecase() {
        return cecase;
    }

    public void setCecase(PublicInfoBundleCECase cecase) {
        this.cecase = cecase;
    }

    public PublicInfoBundleOccPeriod getPeriod() {
        return period;
    }

    public void setPeriod(PublicInfoBundleOccPeriod period) {
        this.period = period;
    }

    public List<PublicInfoBundlePerson> getPersonList() {
        return personList;
    }

    public void setPersonList(List<PublicInfoBundlePerson> personList) {
        this.personList = personList;
    }
    
}
