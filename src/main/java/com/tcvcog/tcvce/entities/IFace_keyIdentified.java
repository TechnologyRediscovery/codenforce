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
 * Implemented during humanization; specifies an interface for retrieving
 * the object's DB key identifier
 * @author sylvia
 */
public interface IFace_keyIdentified {
    /**
     * Used by entities who come from a DB record with an int primary key
     * @return the primary key of the record
     */
    public int getDBKey();
    
     /**
     * Implemented by entities which  map to a single table in the DB
     * @return the table's identifier in the DB
     */
    public String getTableName();
}
