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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.entities.PropertyDataHeavy;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.util.List;

/**
 *
 * @author Nathan Dietz
 */
public class PropertyUnitChangesBB
        extends BackingBeanUtils{
    
    private PropertyDataHeavy currProp;
    private PropertyUnit currPropUnit;
    
    private List<PropertyUnitDataHeavy> heavyDisplayList;
    
    private List<ViewOptionsActiveListsEnum> allViewOptions;
    private ViewOptionsActiveListsEnum currentViewOption;

    public PropertyDataHeavy getCurrProp() {
        return currProp;
    }

    public void setCurrProp(PropertyDataHeavy currProp) {
        this.currProp = currProp;
    }

    public PropertyUnit getCurrPropUnit() {
        return currPropUnit;
    }

    public void setCurrPropUnit(PropertyUnit currPropUnit) {
        this.currPropUnit = currPropUnit;
    }

    public List<PropertyUnitDataHeavy> getHeavyDisplayList() {
        return heavyDisplayList;
    }

    public void setHeavyDisplayList(List<PropertyUnitDataHeavy> heavyDisplayList) {
        this.heavyDisplayList = heavyDisplayList;
    }

    public List<ViewOptionsActiveListsEnum> getAllViewOptions() {
        return allViewOptions;
    }

    public void setAllViewOptions(List<ViewOptionsActiveListsEnum> allViewOptions) {
        this.allViewOptions = allViewOptions;
    }

    public ViewOptionsActiveListsEnum getCurrentViewOption() {
        return currentViewOption;
    }

    public void setCurrentViewOption(ViewOptionsActiveListsEnum currentViewOption) {
        this.currentViewOption = currentViewOption;
    }
    
    
    
    
}
