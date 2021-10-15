/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
 * @author sylvia
 */
public enum CitationViolationStatusEnum {

    FILED ("Filed"),
    AWAITING_PLEA ("Awaiting Plea"),
    CONTINUED ("Continued"),
    GUILTY ("Guilty"),
    NO_CONTEST("No Contest"),
    DISMISSED ("Dismissed"),
    COMPLIANCE ("Compliance"),
    INVALID ("Deemed invalid by judge"),
    WITHDRAWN ("Withdrawn"),
    NOT_GUILTY ("Not Guilty"),
    OTHER ("Other");
    
    private final String label;
    
    private CitationViolationStatusEnum(String lab){
        label = lab;
    }
    
    public String getLable(){
        return label;
    }
    

    
    
    
}
