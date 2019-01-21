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

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Eric C. Darsow
 */
public class PropertyWithLists extends Property implements Serializable{
    
    // cases store code enforcement data
    private ArrayList<CECase> ceCaseList;
    // property units store occupancy data
    private ArrayList<PropertyUnit> unitList;
    // both are connected to Person objects all over the place
    private ArrayList<Person> personList;

    /**
     * @return the ceCaseList
     */
    public ArrayList<CECase> getCeCaseList() {
        return ceCaseList;
    }

    /**
     * @return the unitList
     */
    public ArrayList<PropertyUnit> getUnitList() {
        return unitList;
    }

    /**
     * @return the personList
     */
    public ArrayList<Person> getPersonList() {
        return personList;
    }

    /**
     * @param ceCaseList the ceCaseList to set
     */
    public void setCeCaseList(ArrayList<CECase> ceCaseList) {
        this.ceCaseList = ceCaseList;
    }

    /**
     * @param unitList the unitList to set
     */
    public void setUnitList(ArrayList<PropertyUnit> unitList) {
        this.unitList = unitList;
    }

    /**
     * @param personList the personList to set
     */
    public void setPersonList(ArrayList<Person> personList) {
        this.personList = personList;
    }
    
}
