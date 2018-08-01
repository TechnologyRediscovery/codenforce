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

    PrelimInvestigationPending("Preliminary Investigation Pending"),
    NoticeDelivery("Notice of Violation Delivery"),
    InitialComplianceTimeframe("Intitial Compliance Timeframe"),
    SecondaryComplianceTimeframe("Secondary Compliance Timeframe"),
    AwaitingHearingDate("Awaiting Hearing"),
    HearingPreparation("Hearing Preparation"),
    InitialPostHearingComplianceTimeframe("Initial Post-Hearing Compliance Timeframe"),
    SecondaryPostHearingComplianceTimeframe("Secondary Post-Hearing Compliance Timeframe"),
    InactiveHolding("Inactive Holding"),
    Closed("Closed"),
    LegacyImported("Legacy data container case");
    
    private final String label;
    
    private CasePhase(String label){
        this.label = label;
    }
    
    public String getLabel(){
        return label;
    }

    
}


