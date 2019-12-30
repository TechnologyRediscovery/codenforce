/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
public enum RoleType {
    
    Developer("Developer", 7),
    SysAdmin("System Administrator", 6),
    EnforcementOfficial("Enforcement Authorized Staff", 5),
    CogStaff("TCVCOG Staff Member", 4),
    MuniStaff("Municipal Government Staff", 3),
    MuniReader("Municipal Government Viewer", 2),
    Public("Public at Large", 1); 
   
    private final String label;
    private final int rank;
    
    private RoleType(String label, int rnk){
        this.label = label;
        this.rank = rnk;
    }
    
    public String getLabel(){
        return label;
    }
    
    public int getRank(){
        return rank;
    }
    
}
