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
 * Set by the CaseCoordinator during creation of a violation from the DB;
 * Used extensively throughout case processing:
 * - Assigning a case phase
 * - Assigning a case's stage
 * 
 * @author ellen bascomb of apt 31y
 */
public enum CodeViolationStatusEnum {

    RESOLVED(                               "Compliance achieved within reporting period", 
                                            "Issue no longer remains on property",
                                            "codeviolation_resolved_iconid",
                                            0,
                                            true),
    
    UNRESOLVED_WITHINCOMPTIMEFRAME(         "Within compliance timeframe",
                                            "Compliance days remaining: ",
                                            "codeviolation_unresolved_withincomptimeframe_iconid",
                                            1,
                                            false),
    
    UNRESOLVED_EXPIREDCOMPLIANCETIMEFRAME(  "Requiring ongoing officer action", 
                                            "Days since end of compliance timeframe: ",
                                            "codeviolation_unresolved_overdue_iconid",
                                             2,
                                            false),
    
    UNRESOLVED_CITED(                       "Cited", 
                                            "Days since end of compliance timeframe: ",
                                            "codeviolation_unresolved_citation_iconid",
                                            3,
                                            false),
    
    NULLIFIED(                              "Nullified",
                                            "",
                                            "codeviolation_nullified_iconid",
                                            -1,
                                            true),
    TRANSFERRED(                              "Transferred",
                                            "",
                                            "codeviolation_nullified_iconid",
                                            -1,
                                            true);
    
    
    private final String label;
    private final String leadText;
    private final String iconPropertyName;
    private final int phaseOrder;
    private final boolean terminalStatus;
    
    
    private CodeViolationStatusEnum(String label, String lt, String icn, int ord, boolean term){
        this.label = label;
        this.leadText = lt;
        this.iconPropertyName = icn;
        this.phaseOrder = ord;
        this.terminalStatus = term;
    }
    
    public String getLabel(){
        return label;
    }
    
    public int getOrder(){
        return phaseOrder;
    }
    
    public String getLeadText(){
        return leadText;
    }
    
    public String getIconPropertyName(){
        return iconPropertyName;
    }

    /**
     * @return the terminalStatus
     */
    public boolean isTerminalStatus() {
        return terminalStatus;
    }
}


