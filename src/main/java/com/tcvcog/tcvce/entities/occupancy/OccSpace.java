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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.entities.CodeElement;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Eric Darsow echocharliedelta@protonmail.com
 */
public class OccSpace extends BackingBeanUtils implements Serializable {
    
    protected int spaceID;
    protected int occSpaceTypeID;
    protected String name;
    protected boolean required;
    protected String description;
    
    protected List<OccSpaceElement> spaceElementList;

  //xiaohong add
    protected boolean selected;

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
   

    /**
     * @return the spaceID
     */
    public int getSpaceID() {
        return spaceID;
    }

    /**
     * @param spaceID the spaceID to set
     */
    public void setSpaceID(int spaceID) {
        this.spaceID = spaceID;
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

    /**
     * @return the required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @return the occSpaceTypeID
     */
    public int getOccSpaceTypeID() {
        return occSpaceTypeID;
    }

    /**
     * @param occSpaceTypeID the occSpaceTypeID to set
     */
    public void setOccSpaceTypeID(int occSpaceTypeID) {
        this.occSpaceTypeID = occSpaceTypeID;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the spaceElementList
     */
    public List<OccSpaceElement> getSpaceElementList() {
        return spaceElementList;
    }

    /**
     * @param spaceElementList the spaceElementList to set
     */
    public void setSpaceElementList(List<OccSpaceElement> spaceElementList) {
        this.spaceElementList = spaceElementList;
    }

   
    
   
}
