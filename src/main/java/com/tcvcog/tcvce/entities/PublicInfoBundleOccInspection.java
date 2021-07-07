/*
 * Copyright (C) 2020 Technology Rediscovery LLC.
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

import com.tcvcog.tcvce.entities.occupancy.OccInspection;
import java.time.LocalDateTime;

/**
 * A wrapper class that stores a CECase that is stripped of all sensitive
 * information.
 * Look at the JavaDocs of the PublicInfoBundle Class for more information.
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundleOccInspection extends PublicInfoBundle {
    
    private OccInspection bundledInspection;
    
    @Override
    public String toString(){
        
        return this.getClass().getName() + bundledInspection.getInspectionID();
        
    }

    public OccInspection getBundledInspection() {
        return bundledInspection;
    }
    
    /**
     * Remove all sensitive data from the OccInspection and set it in the
     * bundledInspection field.
     * @param input 
     */
    public void setBundledInspection(OccInspection input) {
        
        setPacc(input.getPacc());
        
        input.setCreationTS(LocalDateTime.MIN);
        
        // TODO: Revisit after humanization
        input.setThirdPartyInspector(null);
        
        input.setThirdPartyApprovalBy(new User());
        
        input.setNotes("*****");
        
        bundledInspection = input;
    }
    
}