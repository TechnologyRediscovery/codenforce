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
 * @author sylvia
 */
public enum ViewOptionsEventRulesEnum {
    VIEW_ALL("all rules"),
    VIEW_ACTIVE_NOT_PASSED("not yet passed rules"),
    VIEW_ACTIVE_PASSED("passed rules"),
    VIEW_INACTIVE("inactive (i.e. deleted)");
    
    private final String label;
     
     private ViewOptionsEventRulesEnum(String l){
         this.label = l;
     }
     
     public String getLabel(){
         return label;
     }
}
