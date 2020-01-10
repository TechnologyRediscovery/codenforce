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
public enum CasePhaseEnum {

    PrelimInvestigationPending      (   "Preliminary Investigation Pending", 
                                        1, 
                                        CaseStageEnum.Investigation, 
                                        true                                    ),
    
    NoticeDelivery                  (   "Notice of Violation Delivery", 
                                        2, 
                                        CaseStageEnum.Investigation, 
                                        true                                    ),
    
    InitialComplianceTimeframe      (   "Initial Compliance Timeframe", 
                                        3, 
                                        CaseStageEnum.Enforcement, 
                                        true                                    ),
    
    SecondaryComplianceTimeframe    (   "Secondary Compliance Timeframe",
                                        4, 
                                        CaseStageEnum.Enforcement, 
                                        true                                    ),
    
    AwaitingHearingDate             (   "Awaiting Hearing", 
                                        5, 
                                        CaseStageEnum.Citation, 
                                        true                                    ),
    
    HearingPreparation              (   "Hearing Preparation", 
                                        6, 
                                        CaseStageEnum.Citation, 
                                        true                                    ),
    
    InitialPostHearingComplianceTimeframe("Initial Post-Hearing Compliance Timeframe", 
                                            7, 
                                            CaseStageEnum.Citation, 
                                            true                                ),
    
    SecondaryPostHearingComplianceTimeframe("Secondary Post-Hearing Compliance Timeframe", 
                                            8, 
                                            CaseStageEnum.Citation, 
                                            true                                ),
    
    InactiveHolding                 (       "Inactive Holding", 
                                            9, 
                                            CaseStageEnum.Unknown, 
                                            false                               ),
    
    Closed                          (       "Closed", 
                                            10, 
                                            CaseStageEnum.Closed, 
                                            false                               ),
    
    LegacyImported                  (       "Legacy data container case", 
                                            11, 
                                            CaseStageEnum.Unknown, 
                                            false                               );
    
    private final String label;
    private final int phaseOrder;
    private final CaseStageEnum stage;
    private final Boolean caseOpen;
    
    private CasePhaseEnum(String label, int ord, CaseStageEnum s, Boolean oc){
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
    
    public CaseStageEnum getCaseStage(){
        return stage;
    }
    
    public boolean isCaseOpen(){
        return caseOpen;
    }
    
}


