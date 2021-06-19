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
 * Represents a database connection between a human and a mailing address
 * Stores values like "resides at" or "tax mailing"
 * @author sylvia
 */
public class HumanMailingAddressRole 
        extends LinkedObjectRole{
    
    public HumanMailingAddressRole(LinkedObjectRole role, String table){
        this.linkingTableName = table;
        this.roleID = role.roleID;
        this.title = role.title;
        this.createdTS = role.createdTS;
        this.description = role.description;
        this.muni = role.muni;
        this.deactivatedTS = role.deactivatedTS;
        this.notes = role.notes;
        
    }
    
}
