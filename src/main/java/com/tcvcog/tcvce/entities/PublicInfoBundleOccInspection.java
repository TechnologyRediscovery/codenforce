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
    
    public void setBundledInspection(OccInspection input) {
        
        setPacc(input.getPacc());
        
        input.setCreationTS(LocalDateTime.MIN);
        
        input.setThirdPartyInspector(new Person());
        
        input.setThirdPartyApprovalBy(new User());
        
        input.setNotes("*****");
        
        bundledInspection = input;
    }
    
}