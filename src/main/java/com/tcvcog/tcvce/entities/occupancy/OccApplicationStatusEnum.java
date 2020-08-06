/*
 * Copyright (C) 2020 Technology Rediscovery LLC.
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
package com.tcvcog.tcvce.entities.occupancy;

/**
 *
 * @author Nathan Dietz
 */
public enum OccApplicationStatusEnum {
    
    Waiting("Submitted, Awaiting Review", 0, "inspectionStatusIcon_notinspected"),
    NewUnit("Attached to new Property Unit", 1, "inspectionStatusIcon_pass"),
    OldUnit("Attached to existing Property Unit", 2, "inspectionStatusIcon_pass"),
    Rejected("Application Rejected", 3, "inspectionStatusIcon_fail"),
    Invalid("Invalid or duplicate application", 4, "inspectionStatusIcon_fail");
    
    private final String label;
    private final int pathID;
    private final String iconPropertyLookup;
    
    private OccApplicationStatusEnum(String label, int ord, String iconLkup){
        this.label = label;
        this.pathID = ord;
        this.iconPropertyLookup = iconLkup;
    }
    
    public String getLabel(){
        return label;
    }
    
    public int getOrder(){
        return pathID;
    }
    
    public String getIconPropertyLookup(){
        return iconPropertyLookup;
    }
    
}