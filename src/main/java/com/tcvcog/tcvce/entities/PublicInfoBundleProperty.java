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

import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundleProperty extends PublicInfoBundle {
    
    private Property bundledProperty;
    private List<PublicInfoBundlePropertyUnit> unitList;

    public PublicInfoBundleProperty() {
    }
    
    @Override
    public String toString(){
        
        return this.getClass().getName() + bundledProperty.getPropertyID();
    }

    public Property getBundledProperty() {
        return bundledProperty;
    }
    
    public void setBundledProperty(Property input) {
        
        
        setMuni(input.getMuni());
        setAddress(input);
        
        input.setMuniCode(0);
        input.setNotes("*****");
        
        input.setCreationTS(LocalDateTime.MIN);
        input.setLastUpdatedTS(LocalDateTime.MIN);
        input.setLastUpdatedBy(new User());
        input.setLocationDescriptor(new OccLocationDescriptor());
        
        input.setBobSource(new BOBSource());
        input.setUnfitDateStart(LocalDateTime.MIN);
        input.setUnfitDateStop(LocalDateTime.MIN);
        input.setUnfitBy(new User());
        
        input.setAbandonedDateStart(LocalDateTime.MIN);
        input.setAbandonedDateStop(LocalDateTime.MIN);
        input.setAbandonedDateUtilStart(new Date());
        input.setAbandonedDateUtilStop(new Date());
        input.setAbandonedBy(new User());
        
        input.setVacantDateStart(LocalDateTime.MIN);
        input.setVacantDateStop(LocalDateTime.MIN);
        input.setVacantBy(new User());
        input.setCondition(new IntensityClass());
        
        input.setSaleYear(0);
        input.setSalePrice(0);
        input.setLandValue(0);
        input.setBuildingValue(0);
        input.setAssessmentYear(0);
        input.setYearBuilt(0);
        input.setLivingArea(0);
        input.setTaxStatus(false);
        input.setTaxYear(0);
        
        bundledProperty = input;
    }

    public List<PublicInfoBundlePropertyUnit> getUnitList() {
        return unitList;
    }

    public void setUnitList(List<PublicInfoBundlePropertyUnit> unitList) {
        this.unitList = unitList;
    }
    
}
