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

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the current state of a Citation
 * @author ellen bascomb of apt 31y
 */
public class CitationStatus extends Status {
    private boolean nonStatusEditsForbidden;
    private EventRuleAbstract phaseChangeRule;
    

    /**
     * @return the nonStatusEditsForbidden
     */
    public boolean isNonStatusEditsForbidden() {
        return nonStatusEditsForbidden;
    }

    /**
     * @param nonStatusEditsForbidden the nonStatusEditsForbidden to set
     */
    public void setNonStatusEditsForbidden(boolean nonStatusEditsForbidden) {
        this.nonStatusEditsForbidden = nonStatusEditsForbidden;
    }

    /**
     * @return the phaseChangeRule
     */
    public EventRuleAbstract getPhaseChangeRule() {
        return phaseChangeRule;
    }

    /**
     * @param phaseChangeRule the phaseChangeRule to set
     */
    public void setPhaseChangeRule(EventRuleAbstract phaseChangeRule) {
        this.phaseChangeRule = phaseChangeRule;
    }

    
    
    
}
