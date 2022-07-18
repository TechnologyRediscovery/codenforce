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

import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Data container object for a CECase that also contains a 
 * Property object and potentially a PropertyUnit object (CECases are not
 * directly attached to PropertyUnit objects yet)
 * 
 * @author Ellen Bascomb (Apartment 31Y)
 */
public class CECasePropertyUnitHeavy
        extends CECase {

    protected Property property;
    protected PropertyUnit propUnit;
    
    
    public CECasePropertyUnitHeavy(CECase cse) {
        super(cse);
    }
    
    public CECasePropertyUnitHeavy(CECasePropertyUnitHeavy csepuh){
        this.property = csepuh.property;
        this.propUnit = csepuh.propUnit;
    }

    /**
     * @return the property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * @return the propUnit
     */
    public PropertyUnit getPropUnit() {
        return propUnit;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * @param propUnit the propUnit to set
     */
    public void setPropUnit(PropertyUnit propUnit) {
        this.propUnit = propUnit;
    }

   

}
