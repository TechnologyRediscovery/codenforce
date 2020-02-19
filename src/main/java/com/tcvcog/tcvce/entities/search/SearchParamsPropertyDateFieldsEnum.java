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
public  enum        SearchParamsPropertyDateFieldsEnum 
        implements  IFace_dateFieldHolder{
    CREATIONTS("Database insert timestamp", "property.creationts"),    
    LAST_UPDATED("Last updated on", "property.lastupdated"),
    UNFIT_START("Declared to be unfit - start date", "unfitdatestart"),
    UNFIT_STOP("Declared to be unfit - stop date", "unfitdatestop"),
    ABANDONED_START("Declared abandoned - start date", "abandoneddatestart"),
    ABANDONED_STOP("Declared abandoned - stop date", "abandoneddatestop"),
    VACANT_START("Declared vacant - start date", "vacantdatestart"),
    VACANT_STOP("Declared vacant - start date", "vacantdatestop"),
    EXTERNAL_DATA_LASTUPDATED("Last recorded update date of external data", "propertyexternaldata.lastupdated");
    
     private final String title;
     private final String dbField;
    
    private SearchParamsPropertyDateFieldsEnum(String t, String db){
        this.title = t;
        this.dbField = db;
    }
    
    public String getTitle(){
        return title;
    }
     
    public Enum getEnumByTitle(String title){
        for(SearchParamsPropertyDateFieldsEnum field: SearchParamsPropertyDateFieldsEnum.values()){
            if(field.getTitle().equals(title)){
                return field;
            }
        }
        return null;
    }
    
    public List<Enum> getAllTitles(){
        List<Enum> output = new ArrayList<>();
        output.add(SearchParamsPropertyDateFieldsEnum.ABANDONED_START);
        output.add(SearchParamsPropertyDateFieldsEnum.ABANDONED_STOP);
        output.add(SearchParamsPropertyDateFieldsEnum.EXTERNAL_DATA_LASTUPDATED);
        output.add(SearchParamsPropertyDateFieldsEnum.LAST_UPDATED);
        output.add(SearchParamsPropertyDateFieldsEnum.UNFIT_START);
        output.add(SearchParamsPropertyDateFieldsEnum.UNFIT_STOP);
        output.add(SearchParamsPropertyDateFieldsEnum.VACANT_START);
        output.add(SearchParamsPropertyDateFieldsEnum.VACANT_STOP);
        return output;
}

    @Override
    public String extractDateFieldString() {
        return dbField;
    }
     
    
}
