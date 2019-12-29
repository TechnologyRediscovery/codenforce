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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author sylvia
 */
public enum SearchParamsPropertyDateFields {
    LAST_UPDATED("Last updated on"),
    UNFIT_START("Declared to be unfit - start date"),
    UNFIT_STOP("Declared to be unfit - stop date"),
    ABANDONED_START("Declared abandoned - start date"),
    ABANDONED_STOP("Declared abandoned - stop date"),
    VACANT_START("Declared vacant - start date"),
    VACANT_STOP("Declared vacant - start date"),
    EXTERNAL_DATA_LASTUPDATED("Last recorded update date of external data");
    
     private final String title;
    
    private SearchParamsPropertyDateFields(String t){
        this.title = t;
    }
    
    public String getTitle(){
        return title;
    }
     
    public Enum getEnumByTitle(String title){
        for(SearchParamsPropertyDateFields field: SearchParamsPropertyDateFields.values()){
            if(field.getTitle().equals(title)){
                return field;
            }
        }
        return null;
    }
    
    public List<Enum> getAllTitles(){
        List<Enum> output = new ArrayList<>();
        output.add(SearchParamsPropertyDateFields.ABANDONED_START);
        output.add(SearchParamsPropertyDateFields.ABANDONED_STOP);
        output.add(SearchParamsPropertyDateFields.EXTERNAL_DATA_LASTUPDATED);
        output.add(SearchParamsPropertyDateFields.LAST_UPDATED);
        output.add(SearchParamsPropertyDateFields.UNFIT_START);
        output.add(SearchParamsPropertyDateFields.UNFIT_STOP);
        output.add(SearchParamsPropertyDateFields.VACANT_START);
        output.add(SearchParamsPropertyDateFields.VACANT_STOP);
        return output;
}
     
    
}
