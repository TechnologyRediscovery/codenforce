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

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundleCodeViolation extends PublicInfoBundle {
 
    private CodeViolation bundledViolation;
    
    @Override
    public String toString(){
        
        return this.getClass().getName() + bundledViolation.getViolationID();
        
    }

    public CodeViolation getBundledViolation() {
        return bundledViolation;
    }

    public void setBundledViolation(CodeViolation input) {
        
        input.setNotes("*****");
        input.setCreatedBy(new User());
        
        input.setCitationIDList(new ArrayList<Integer>());
        input.setNoticeIDList(new ArrayList<Integer>());
        
        input.setLeagacyImport(false);
        
        input.setComplianceUser(new User());
        
        input.setComplianceTFExpiryPropID(0);
        input.setComplianceTFExpiryProp(new Proposal());
        
        input.setSeverityIntensity(new IntensityClass());
        
        input.setLastUpdatedTS(LocalDateTime.MIN);
        input.setLastUpdatedUser(new User());
        
        bundledViolation = input;
    }
    
}
