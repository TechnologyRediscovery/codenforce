/*
 * Copyright (C) 2017 ellen bascomb of apt 31y
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

/**
 *
 * @author ellen bascomb of apt 31y
 */
public enum PersonType{
    User("System user"),
    Owner("Owner"),
    CogStaff("TCVCOG Staff"),
    NonCogOfficial("Non-TCVCOG Official"),
    MuniStaff("Municipality Staff"),
    Tenant("Tenant"),
    OwnerOccupant("Owner-Occupant"),
    OwnerNonOccupant("Owner-Non Occupant"),
    FutureOwner("Future owner"),
    Manager("Manager"),
    ElectedOfficial("Elected Official"),
    Public("Public"),
    LawEnforcement("Law Enforcement"),
    Other("Other"),
    ownercntylookup("Owner from county database"),
    LegacyOwner("Legacy owner"),
    LegacyAgent("Legacy Agent");
    
    private final String label;
    
    private PersonType(String label){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    }
    
}
