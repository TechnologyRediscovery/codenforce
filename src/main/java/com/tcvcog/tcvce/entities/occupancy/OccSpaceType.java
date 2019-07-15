/*
 * Copyright (C) 2018 Adam Gutonski
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

import java.util.List;

/**
 * Mapped to fields of the spacetype table
 * @author EC Darsow
 */
public class OccSpaceType {
    protected int spaceTypeID;
    protected String spaceTypeTitle;
    protected String spaceTypeDescription;
    protected boolean required;

    /**
     * @return the spaceTypeID
     */
    public int getSpaceTypeID() {
        return spaceTypeID;
    }

    /**
     * @param spaceTypeID the spaceTypeID to set
     */
    public void setSpaceTypeID(int spaceTypeID) {
        this.spaceTypeID = spaceTypeID;
    }

    /**
     * @return the spaceTypeTitle
     */
    public String getSpaceTypeTitle() {
        return spaceTypeTitle;
    }

    /**
     * @param spaceTypeTitle the spaceTypeTitle to set
     */
    public void setSpaceTypeTitle(String spaceTypeTitle) {
        this.spaceTypeTitle = spaceTypeTitle;
    }

    /**
     * @return the spaceTypeDescription
     */
    public String getSpaceTypeDescription() {
        return spaceTypeDescription;
    }

    /**
     * @param spaceTypeDescription the spaceTypeDescription to set
     */
    public void setSpaceTypeDescription(String spaceTypeDescription) {
        this.spaceTypeDescription = spaceTypeDescription;
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


    
}
