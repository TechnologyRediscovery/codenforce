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
 * Utility enum for allowing users to choose a date field for building query
 * search param objects. Contains direct mapping of DB field names to 
 * human friendly descriptions of dates available
 * 
 * @author ellen bascomb
 */
public enum SearchParamsPersonDateFieldsEnum  
        implements  IFace_dateFieldHolder {
    
    CREATED_TS            ("Creation timestamp", 
                            "person.creationtimestamp"), 
    
    LAST_UPDATED            ("Last update  timestamp", 
                            "person.lastupdated"), 
    
    
    GHOSTCREATION_TS        ("Ghost creation timestamp", 
                            "ghosttimestamp"), 
    
    
    CLONECREATION_TS        ("Clone creation timestamp", 
                            "ghosttimestamp"), 
    
    EXPIRY                  ("Expiry date", 
                            "person.expirydate"); 
   
    
    private final String title;
    private final String dbField;
    
    private SearchParamsPersonDateFieldsEnum(String t, String db){
        title = t;
        dbField = db;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    @Override
    public String extractDateFieldString() {
        return dbField;
    }
    
}
