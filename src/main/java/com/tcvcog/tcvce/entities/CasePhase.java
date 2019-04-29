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
 * @author Eric C. Darsow
 */
public enum CasePhase {

    PrelimInvestigationPending("Preliminary Investigation Pending", 1),
    NoticeDelivery("Notice of Violation Delivery", 2 ),
    InitialComplianceTimeframe("Initial Compliance Timeframe", 3),
    SecondaryComplianceTimeframe("Secondary Compliance Timeframe",4 ),
    AwaitingHearingDate("Awaiting Hearing", 5),
    HearingPreparation("Hearing Preparation", 6),
    InitialPostHearingComplianceTimeframe("Initial Post-Hearing Compliance Timeframe", 7),
    SecondaryPostHearingComplianceTimeframe("Secondary Post-Hearing Compliance Timeframe", 8),
    InactiveHolding("Inactive Holding", 9),
    Closed("Closed", 10),
    LegacyImported("Legacy data container case", 11);
    
    private final String label;
    private final int phaseOrder;
    
    private CasePhase(String label, int ord){
        this.label = label;
        this.phaseOrder = ord;
    }
    
    public String getLabel(){
        return label;
    }
    
    public int getOrder(){
        return phaseOrder;
    }
}


