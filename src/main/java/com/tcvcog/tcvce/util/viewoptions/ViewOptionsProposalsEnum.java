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
public enum ViewOptionsProposalsEnum {
    VIEW_ALL("all proposals"),
    VIEW_ACTIVE_NOTHIDDEN("active and not hidden (default)"),
    VIEW_ACTIVE_HIDDEN("active but hidden"),
    VIEW_NOT_EVALUATED("not evaluated"),
    VIEW_EVALUATED("evaluated"),
    VIEW_INACTIVE("inactive (i.e. deleted)");
    
    private final String label;
     
     private ViewOptionsProposalsEnum(String l){
         this.label = l;
     }
     
     public String getLabel(){
         return label;
     }
}
