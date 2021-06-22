/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
 * Creates unified method for asking an object
 * for its corresponding database table name
 * ** POTENTIALLY A VIOLATION OF MVC DESIGN PRINCIPLES **
 * Should a business object know where it lives in the DB?
 * Ellen says: YES, they shall know this.
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public interface IFace_TableNameHolder {
  
    public String getDBTableName();
  
    
    
}
