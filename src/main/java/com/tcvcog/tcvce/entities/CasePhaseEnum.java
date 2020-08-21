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
                                        "Record violation of a specific ordinance",
                                        "howto-violation-add",
                                        1, 
                                        CaseStageEnum.Investigation, 
                                        true,
                                        true,
                                        true,
                                        true,
                                        true),
    
    IssueNotice                  (   "Generating and sending notice of violation", 
                                        "Print and mail notice of violation",
                                        "howto-notices",
                                        2, 
                                        CaseStageEnum.Investigation, 
                                        true,
                                        true,
                                        true,
                                        true,
                                        true),
    
    InsideComplianceWindow      (   "Inside violation complaince window", 
                                    "Allow time for violation resolution; monitor property for intensifications",
                                    "howto-insidetimeframe",
                                        3, 
                                        CaseStageEnum.Enforcement, 
                                        true,
                                        true,
                                        true,
                                        true,
                                        true),
    
    TimeframeExpiredNotCited    (   "Compliance window has expired; no citations issued",
                                    "Verify compliance status; record compliance, extend timeframe, or issue a citation",
                                    "howto-expiredtimeframe",
                                    4, 
                                    CaseStageEnum.Enforcement, 
                                    true,
                                    true,
                                    true,
                                    true,
                                    true),
    
    AwaitingHearingDate             (   "Citation issued; Awaiting Hearing Date", 
                                        "Monitor mail for court documents; create event when hearing is scheduled",
                                        "howto-recordhearingdate",
                                        5, 
                                        CaseStageEnum.Citation, 
                                        true,
                                        true,
                                        true,
                                        true,
                                        true),
    
    HearingPreparation              (   "Hearing scheduled; Case preparation", 
                                        "Print case profile and prepare for court hearing",
                                        "howto-hearingprep",
                                        6, 
                                        CaseStageEnum.Citation, 
                                        true,
                                        true,
                                        true,
                                        true,
                                        true),
    
    InsideCourtOrderedComplianceTimeframe("Inside court-ordered extended violation compliance timeframe", 
                                            "Allow time for violation resolution; monitor property for intensification",
                                            "howto-insidecourttimeframe",
                                            7, 
                                            CaseStageEnum.Citation, 
                                            true,
                                            true,
                                            true,
                                            true,
                                            true),
    
    CourtOrderedComplainceTimeframeExpired("Court-ordered complaince window extension expired", 
                                            "Verify compliance status; record compliance, issue additional notices, or extend timeframe ",
                                            "howto-expiredcourttimeframe",
                                            8, 
                                            CaseStageEnum.Citation, 
                                            true,
                                            true,
                                            true,
                                            true,
                                            true),
    
    InactiveHolding                 (       "Inactive Holding", 
                                            "Case is stuck; monitor for changes in property status",
                                            "howto-inactive",
                                            9, 
                                            CaseStageEnum.Unknown, 
                                            true,
                                            true,
                                            true,
                                            true,
                                            true),
    
    Closed                          (       "Closed", 
                                            "No next steps; if code violations exist, open a new case",
                                            "howto-closed",
                                            10, 
                                            CaseStageEnum.Closed, 
                                            true,
                                            true,
                                            true,
                                            true,
                                            true),
    
    Container                  (       "Data container case for property and persons", 
                                        "Not a real case; used for storing administrative data",
                                        "howto-container",
                                            11, 
                                            CaseStageEnum.Unknown, 
                                            true,
                                            true,
                                            true,
                                            true,
                                            true);
    
    private final String label;
    private final String nextStep;
    private final String nextStepHelpPanelID;
    private final int phaseOrder;
    private final CaseStageEnum stage;
    private final boolean caseOpen;
    private final boolean allowPropertyChange;
    private final boolean allowNewViolations;
    private final boolean allowNewNotices;
    private final boolean allowNewCitations;
    
    private CasePhaseEnum(  String label, 
                            String nextDescr,
                            String nextStepHelp,
                            int ord, 
                            CaseStageEnum s, 
                            boolean openCase,
                            boolean propChange,
                            boolean attachViol,
                            boolean createnotices,
                            boolean issueCitations){
        this.label = label;
        this.nextStep = nextDescr;
        this.nextStepHelpPanelID = nextStepHelp;
        this.phaseOrder = ord;
        this.stage = s;
        this.caseOpen = openCase;
        this.allowPropertyChange = propChange;
        this.allowNewViolations = attachViol;
        this.allowNewNotices = createnotices;
        this.allowNewCitations = issueCitations;
    }
    
    public String getLabel(){
        return label;
    }
    
    public int getOrder(){
        return getPhaseOrder();
    }
    
    public CaseStageEnum getCaseStage(){
        return getStage();
    }
    
    public boolean isCaseOpen(){
        return caseOpen;
    }

    /**
     * @return the allowPropertyChange
     */
    public boolean isAllowPropertyChange() {
        return allowPropertyChange;
    }

    /**
     * @return the allowNewViolations
     */
    public boolean isAllowNewViolations() {
        return allowNewViolations;
    }

    /**
     * @return the allowNewNotices
     */
    public boolean isAllowNewNotices() {
        return allowNewNotices;
    }

    /**
     * @return the allowNewCitations
     */
    public boolean isAllowNewCitations() {
        return allowNewCitations;
    }

    /**
     * @return the nextStep
     */
    public String getNextStep() {
        return nextStep;
    }

    /**
     * @return the phaseOrder
     */
    public int getPhaseOrder() {
        return phaseOrder;
    }

    /**
     * @return the stage
     */
    public CaseStageEnum getStage() {
        return stage;
    }

    /**
     * @return the nextStepHelpPanelID
     */
    public String getNextStepHelpPanelID() {
        return nextStepHelpPanelID;
    }
    
}


