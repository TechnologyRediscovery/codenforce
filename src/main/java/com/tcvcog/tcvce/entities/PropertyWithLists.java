/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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

import java.util.ArrayList;

/**
 *
 * @author Eric C. Darsow
 */
public class PropertyWithLists extends Property{
    
    // cases store code enforcement data
    private ArrayList<CECase> propertyCaseList;
    // property units store occupancy data
    private ArrayList<PropertyUnit> propertyUnitList;
    // both are connected to Person objects all over the place
    private ArrayList<Person> propertyPersonList;

    /**
     * @return the propertyCaseList
     */
    public ArrayList<CECase> getPropertyCaseList() {
        return propertyCaseList;
    }

    /**
     * @return the propertyUnitList
     */
    public ArrayList<PropertyUnit> getPropertyUnitList() {
        return propertyUnitList;
    }

    /**
     * @return the propertyPersonList
     */
    public ArrayList<Person> getPropertyPersonList() {
        return propertyPersonList;
    }

    /**
     * @param propertyCaseList the propertyCaseList to set
     */
    public void setPropertyCaseList(ArrayList<CECase> propertyCaseList) {
        this.propertyCaseList = propertyCaseList;
    }

    /**
     * @param propertyUnitList the propertyUnitList to set
     */
    public void setPropertyUnitList(ArrayList<PropertyUnit> propertyUnitList) {
        this.propertyUnitList = propertyUnitList;
    }

    /**
     * @param propertyPersonList the propertyPersonList to set
     */
    public void setPropertyPersonList(ArrayList<Person> propertyPersonList) {
        this.propertyPersonList = propertyPersonList;
    }
    
}
