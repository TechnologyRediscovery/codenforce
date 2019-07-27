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

    PrelimInvestigationPending("Preliminary Investigation Pending", 1, CaseStage.Investigation, true),
    NoticeDelivery("Notice of Violation Delivery", 2, CaseStage.Investigation, true ),
    InitialComplianceTimeframe("Initial Compliance Timeframe", 3, CaseStage.Enforcement, true),
    SecondaryComplianceTimeframe("Secondary Compliance Timeframe",4, CaseStage.Enforcement, true ),
    AwaitingHearingDate("Awaiting Hearing", 5, CaseStage.Citation, true),
    HearingPreparation("Hearing Preparation", 6, CaseStage.Citation, true),
    InitialPostHearingComplianceTimeframe("Initial Post-Hearing Compliance Timeframe", 7, CaseStage.Citation, true),
    SecondaryPostHearingComplianceTimeframe("Secondary Post-Hearing Compliance Timeframe", 8, CaseStage.Citation, true),
    InactiveHolding("Inactive Holding", 9, CaseStage.Unknown, false),
    Closed("Closed", 10, CaseStage.Closed, false),
    LegacyImported("Legacy data container case", 11, CaseStage.Unknown, false);
    
    private final String label;
    private final int phaseOrder;
    private final CaseStage stage;
    private final Boolean caseOpen;
    
    private CasePhase(String label, int ord, CaseStage s, Boolean oc){
        this.label = label;
        this.phaseOrder = ord;
        this.stage = s;
        this.caseOpen = oc;
    }
    
    public String getLabel(){
        return label;
    }
    
    public int getOrder(){
        return phaseOrder;
    }
    
    public CaseStage getCaseStage(){
        return stage;
    }
    
    public boolean isCaseOpen(){
        return caseOpen;
    }
    
}


