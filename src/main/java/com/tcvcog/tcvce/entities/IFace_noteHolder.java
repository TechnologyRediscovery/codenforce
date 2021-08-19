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
 * Species an interface of a single getter and a single setter for a DB notes field
 * Note that this interface extends IFace_keyIdentified which means you can ask
 * a noteholder for its DB table name, primary key field name, and the object's PK/ID
 * 
 * @author sylvia
 */
public interface IFace_noteHolder 
        extends IFace_keyIdentified{
    /**
     * Getter for the note field
     * @return the notes on the object
     */
    public String getNotes();
    
    /**
     * Setter for the note field
     * @param n the notes to set
     */
    public void setNotes(String n);
}
