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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.Proposal;
import com.tcvcog.tcvce.entities.User;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class OccEventPropUnit extends OccEvent {
    
    private PropertyUnit propUnit;
    
    public OccEventPropUnit(OccEvent ev){

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
     * @return the propUnit
     */
    public PropertyUnit getPropUnit() {
        return propUnit;
    }

    /**
     * @param propUnit the propUnit to set
     */
    public void setPropUnit(PropertyUnit propUnit) {
        this.propUnit = propUnit;
    }
    
}
