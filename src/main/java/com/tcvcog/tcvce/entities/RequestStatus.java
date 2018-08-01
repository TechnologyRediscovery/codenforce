/*
 * Copyright (C) 2017 Eric C. Darsow
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
 * @author Eric C. Darsow
 */
public enum RequestStatus {
    
    AwaitingReview("Awaiting Review"), 
    UnderInvestigation("Under Investigation"), 
    NoViolationFound("No Violation Found"), 
    CitationFiled("Citation Filed"),
    Resolved("Request Process Resolved");
    
    private final String label;
    
    private RequestStatus(String label){
        this.label = label;
        
    }
    
    public String getLabel(){
        return label;
    
    }
    
}
