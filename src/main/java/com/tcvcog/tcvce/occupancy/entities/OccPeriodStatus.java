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
package com.tcvcog.tcvce.occupancy.entities;

import com.tcvcog.tcvce.entities.*;

/**
 *
 * @author Eric C. Darsow
 */
public enum OccPeriodStatus {

    UNKNOWN("Unknown", 0, "occperstatus_unknown"),
    KNOWN_UNAUTHORIZED("Known to be occupied without authorization", 0, "known_unauthorized"),
    
    AUTHORIZED("Pending review by code officer", 1, "occperstatus_unknown");
    
    private final String label;
    private final int phaseOrder;
    private final String iconPropertyLookup;
    
    private OccPeriodStatus(String label, int ord, String iconLkup){
        this.label = label;
        this.phaseOrder = ord;
        this.iconPropertyLookup = iconLkup;
    }
    
    public String getLabel(){
        return label;
    }
    
    public int getOrder(){
        return phaseOrder;
    }
    
    public String getIconPropertyLookup(){
        return iconPropertyLookup;
    }
}


