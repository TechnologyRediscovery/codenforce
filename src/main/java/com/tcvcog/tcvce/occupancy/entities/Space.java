/*
 * Copyright (C) 2018 Emily
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
package com.tcvcog.tcvce.occupancy.entities;

import com.tcvcog.tcvce.entities.CodeElement;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Eric Darsow echocharliedelta@protonmail.com
 */
public class Space implements Serializable {
    
    private int spaceid;
    private SpaceType spaceType;
    private String name;
    private ArrayList<CodeElement> elementList;

    /**
     * @return the spaceType
     */
    public SpaceType getSpaceType() {
        return spaceType;
    }

    /**
     * @return the elementList
     */
    public ArrayList<CodeElement> getElementList() {
        return elementList;
    }

    /**
     * @param spaceType the spaceType to set
     */
    public void setSpaceType(SpaceType spaceType) {
        this.spaceType = spaceType;
    }

    /**
     * @param elementList the elementList to set
     */
    public void setElementList(ArrayList<CodeElement> elementList) {
        this.elementList = elementList;
    }

    /**
     * @return the spaceid
     */
    public int getSpaceid() {
        return spaceid;
    }

    /**
     * @param spaceid the spaceid to set
     */
    public void setSpaceid(int spaceid) {
        this.spaceid = spaceid;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
    
   
}
