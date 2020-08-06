/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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
 * Bundling container for CECasePhase and Icons for UI config
 * @author sylvia
 */
public class CECaseStatus {
    private CasePhaseEnum phase;
    private Icon phaseIcon;

    /**
     * @return the phase
     */
    public CasePhaseEnum getPhase() {
        return phase;
    }

    /**
     * @param phase the phase to set
     */
    public void setPhase(CasePhaseEnum phase) {
        this.phase = phase;
    }

    /**
     * @return the phaseIcon
     */
    public Icon getPhaseIcon() {
        return phaseIcon;
    }

    /**
     * @param phaseIcon the phaseIcon to set
     */
    public void setPhaseIcon(Icon phaseIcon) {
        this.phaseIcon = phaseIcon;
    }
    
}
