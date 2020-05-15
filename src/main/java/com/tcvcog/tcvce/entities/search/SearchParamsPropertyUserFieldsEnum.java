/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.entities.search;

/**
 *
 * @author sylvia
 */
public enum SearchParamsPropertyUserFieldsEnum
        implements IFace_userFieldHolder{
    
    UNFIT_BY("Declared unfit for occupancy by", "unfitby_userid" ),
    ABANDONED_BY("Declared abandoned by", "abandonedby_userid"),
    VACANT_BY("Declared vacant by", "vacantby_userid"),
    PROPERTY_UPDATEDBY("Property data last updated by", "property.lastupdatedby");
    
    private final String title;
    private final String dbField;
    
    private SearchParamsPropertyUserFieldsEnum (String t, String db){
        this.title = t;
        dbField = db;
    }
    
    public String getTitle(){
        return title;
    }

    @Override
    public String extractUserFieldString() {
        return dbField;
    }
     
    
}
