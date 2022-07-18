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
 * Represents possible outcomes for code violations attached to a citation
 * @author sylvia
 */
public enum CitationViolationStatusEnum {

    FILED               ("Filed", false),
    AWAITING_PLEA       ("Awaiting Plea", false),
    CONTINUED           ("Continued", false),
    GUILTY              ("Guilty", true),
    NO_CONTEST          ("No Contest", true),
    DISMISSED           ("Dismissed", true),
    COMPLIANCE          ("Compliance", true),
    INVALID             ("Deemed invalid by judge", true),
    WITHDRAWN           ("Withdrawn", true),
    NOT_GUILTY          ("Not Guilty", true),
    OTHER               ("Other", false);
    
    private final String label;
    protected final boolean terminalStatus;
    
    
    private CitationViolationStatusEnum(String lab, boolean term){
        this.label = lab;
        this.terminalStatus = term;
    }
    
    public String getLabel(){
        return label;
    }

    /**
     * @return the terminalStatus
     */
    public boolean isTerminalStatus() {
        return terminalStatus;
    }
    

    
    
    
}
