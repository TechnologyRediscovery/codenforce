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
import java.util.List;

/**
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundlePropertyUnit extends PublicInfoBundle {
    
    private PropertyUnit bundledUnit;
    private List<PublicInfoBundleOccPeriod> periodList;
       
    @Override
    public String toString() {
        return this.getClass().getName() + bundledUnit.getUnitID();
    }
    
    public PropertyUnit getBundledUnit() {
        return bundledUnit;
    }
    
        public void setBundledUnit(PropertyUnit input) {
        
        input.setRentalNotes("*****");
        input.setConditionIntensityClassID(0);
        input.setLastUpdatedTS(LocalDateTime.MIN);
        
        bundledUnit = input;
    }

    public List<PublicInfoBundleOccPeriod> getPeriodList() {
        return periodList;
    }

    public void setPeriodList(List<PublicInfoBundleOccPeriod> periodList) {
        this.periodList = periodList;
    }
    
}
