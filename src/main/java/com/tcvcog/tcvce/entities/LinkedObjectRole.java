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

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Humanization standard
 * 
 * Java incarnation of a record from the linkedobjectrole table
 * that describes how a objects/records are related
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class LinkedObjectRole implements Serializable {
    
    
    protected String linkingTableName;
    protected LinkedObjectSchemaEnum schema;
    protected int roleID;
    protected String title;
    protected LocalDateTime createdTS;
    protected String description;
    protected Municipality muni;
    protected LocalDateTime deactivatedTS;
    protected String notes;
    
    public LinkedObjectRole(){
        
        
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.linkingTableName);
        hash = 97 * hash + Objects.hashCode(this.schema);
        hash = 97 * hash + this.roleID;
        hash = 97 * hash + Objects.hashCode(this.title);
        hash = 97 * hash + Objects.hashCode(this.createdTS);
        hash = 97 * hash + Objects.hashCode(this.description);
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
        final LinkedObjectRole other = (LinkedObjectRole) obj;
        if (this.roleID != other.roleID) {
            return false;
        }
        return true;
    }

    
    
    /**
     * @return the roleID
     */
    public int getRoleID() {
        return roleID;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the createdTS
     */
    public LocalDateTime getCreatedTS() {
        return createdTS;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @return the deactivatedTS
     */
    public LocalDateTime getDeactivatedTS() {
        return deactivatedTS;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param roleID the roleID to set
     */
    public void setRoleID(int roleID) {
        this.roleID = roleID;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param createdTS the createdTS to set
     */
    public void setCreatedTS(LocalDateTime createdTS) {
        this.createdTS = createdTS;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @param deactivatedTS the deactivatedTS to set
     */
    public void setDeactivatedTS(LocalDateTime deactivatedTS) {
        this.deactivatedTS = deactivatedTS;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the linkingTableName
     */
    public String getLinkingTableName() {
        return linkingTableName;
    }

    /**
     * @return the schema
     */
    public LinkedObjectSchemaEnum getSchema() {
        return schema;
    }

    /**
     * @param schema the schema to set
     */
    public void setSchema(LinkedObjectSchemaEnum schema) {
        this.schema = schema;
    }
    
    

    
    
    
}
