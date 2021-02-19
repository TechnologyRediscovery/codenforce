/*
 * Copyright (C) 2020 Turtle Creek Valley
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
package com.tcvcog.tcvce.domain;

/**
 *
 * @author Nathan Dietz
 */
public class MetadataException extends BaseException{
    
    //flags that this error is thrown as the result of a null metadata column
    boolean mapNullError = false; 
    
    public MetadataException(){
        super();
        
    }
    
    public MetadataException(String message){
        super(message);
    }
    
    public MetadataException(Exception e){
        super(e);
    }
    
    public MetadataException(String message, Exception e){
        super(message, e);
        
    }

    public boolean isMapNullError() {
        return mapNullError;
    }

    public void setMapNullError(boolean mapNullError) {
        this.mapNullError = mapNullError;
    }
    
}
