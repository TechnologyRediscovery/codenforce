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

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Models a property use type, such as residential - single family, etc.
 * 
 * @author sylvia
 */
public class PropertyUseType implements Manageable{
    
    private int typeID;
    private String name;
    private String description;
    private Icon icon;
    private String zoneClass;
    private LocalDateTime deactivatedts;
    private final static ManagedSchemaEnum MANAGABLE_SCHEMA = ManagedSchemaEnum.PropertyUseType;
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.typeID;
        hash = 73 * hash + Objects.hashCode(this.name);
        hash = 73 * hash + Objects.hashCode(this.description);
        hash = 73 * hash + Objects.hashCode(this.icon);
        hash = 73 * hash + Objects.hashCode(this.zoneClass);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PropertyUseType other = (PropertyUseType) obj;
        if (this.typeID != other.typeID) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.icon, other.icon)) {
            return false;
        }
        if (!Objects.equals(this.zoneClass, other.zoneClass)) {
            return false;
        }
        return true;
    }
    
    /**
     * @return the typeID
     */
    public int getTypeID() {
        return typeID;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the icon
     */
    @Override
    public Icon getIcon() {
        return icon;
    }

    /**
     * @return the zoneClass
     */
    public String getZoneClass() {
        return zoneClass;
    }

    /**
     * @param typeID the typeID to set
     */
    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    /**
     * @param name the name to set
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     * @param zoneClass the zoneClass to set
     */
    public void setZoneClass(String zoneClass) {
        this.zoneClass = zoneClass;
    }


    @Override
    public ManagedSchemaEnum getMANAGEABLE_SCHEMA() {
        return MANAGABLE_SCHEMA;
    }

    @Override
    public int getID() {
         return typeID;
    }

    @Override
    public void setID(int ID) {
        this.typeID = ID;
    }

    /**
     * @return the deactivatedts
     */
    @Override
    public LocalDateTime getDeactivatedts() {
        return deactivatedts;
    }

    /**
     * @param deactivatedts the deactivatedts to set
     */
    @Override
    public void setDeactivatedts(LocalDateTime deactivatedts) {
        this.deactivatedts = deactivatedts;
    }
}
