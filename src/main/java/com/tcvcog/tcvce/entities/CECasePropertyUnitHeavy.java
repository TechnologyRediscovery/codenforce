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

/**
 *
 * @author sylvia
 */
public  class   CECasePropertyUnitHeavy
        extends CECase{
    
    protected Property property;
    protected PropertyUnit propUnit;
    
      public CECasePropertyUnitHeavy(CECase cse) {
        this.caseID = cse.caseID;
        this.publicControlCode = cse.publicControlCode;
        this.paccEnabled = cse.paccEnabled;
        
        this.allowForwardLinkedPublicAccess = cse.allowForwardLinkedPublicAccess;
        
        this.propertyID = cse.propertyID;
        this.propertyUnitID = cse.propertyUnitID;
        
        this.caseManager = cse.caseManager;
        this.caseName = cse.caseName;
        
        this.casePhase = cse.casePhase;
        this.casePhaseIcon = cse.casePhaseIcon;
        
        this.originationDate = cse.originationDate;
        this.closingDate = cse.closingDate;
        this.creationTimestamp = cse.creationTimestamp;
        
        this.notes = cse.notes;
        
        this.source = cse.source;
        
        this.citationList = cse.citationList;
        this.noticeList = cse.noticeList;
        this.violationList = cse.violationList;
        
        this.active = cse.active;
        
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
