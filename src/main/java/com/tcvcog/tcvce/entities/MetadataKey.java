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
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 *
 * @author Nathan Dietz
 */
public class MetadataKey implements Serializable {

    private String label;
    private String key;
    
    
    public MetadataKey(String nodeName){
        key = nodeName;
        //Place a space between all capital letters and the letters infront of them
        //i.e. OriginalText -> Original Text
        label = nodeName.replaceAll("(.)([A-Z])", "$1 $2");
    }
    
    public String getLabel(){
        return label;
    }

    public String getKey() {
        return key;
    }
    
}
