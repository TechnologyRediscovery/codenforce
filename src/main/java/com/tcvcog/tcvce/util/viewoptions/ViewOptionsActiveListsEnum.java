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
package com.tcvcog.tcvce.util.viewoptions;

/**
 *
 * @author Nathan Dietz
 */
public enum ViewOptionsActiveListsEnum {
    
    VIEW_ALL("Show all items"),
    VIEW_ACTIVE("Show only active items"),
    VIEW_INACTIVE("Show inactive (aka deleted) items");
    
    private final String label;
     
     private ViewOptionsActiveListsEnum(String l){
         this.label = l;
     }
     
     public String getLabel(){
         return label;
     }
    
}
