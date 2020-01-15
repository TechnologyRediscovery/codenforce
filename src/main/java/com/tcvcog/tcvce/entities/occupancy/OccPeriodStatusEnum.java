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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.*;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public enum OccPeriodStatusEnum {

    UNKNOWN("Unknown", 0, "occperstatus_unknown", false),
    ROBOT_FLAGGED("Period created and flagged for review by a human", 0, "occperstatus_robotflag", true),
    PENDING_PERIOD_CERTIFICATION("Known to be occupied without authorization", 0, "occperstatus_pendingperiodcert", true),
    PENDING_INSPECTIONANDPROPREV("Inspection underway; Property review in progress", 1, "occperstatus_auth_inspection_underway", true),
    UNAUTHORIZED_FAILEDINSPECTION("Period unauthorized due to failed inspection", 2, "occperstatus_unauth_failedinsp", false),
    UNAUTHORIZED_PROPREVIEW("Period unauthorized due to failed property review", 2, "occperstatus_unauth_failedrev", false),
    AUTHORIZED("Authorized for permit issuance", 1, "occperstatus_auth", false);
    
    private final String label;
    private final int phaseOrder;
    private final boolean openPeriod;
    private final String iconPropertyLookup;
    
    private OccPeriodStatusEnum(String label, int ord, String iconLkup, boolean isOpen){
        this.label = label;
        this.phaseOrder = ord;
        this.iconPropertyLookup = iconLkup;
        this.openPeriod = isOpen;
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

    /**
     * @return the openPeriod
     */
    public boolean isOpenPeriod() {
        return openPeriod;
    }

}


