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
public enum EventType {
    Origination("Case Origination", 5, 1),
    Action("Officer Action", 5, 1),
    CaseAdmin("Case Admin", 3, 2),
    PhaseChange("Case Phase Change", 5, 1),
    Closing("Case Closing", 5, 1),
    Timeline("Case Timeline", 4, 3),
    Communication("Communication", 5, 2),
    Meeting("Meeting", 3, 3),
    Notice("Notice", 3, 3),
    Custom("Custom", 3, 3),
    Compliance("Compliance", 5, 3),
    Citation("Citation", 5, 3),
    Occupancy("Occupancy", 5, 3),
    PropertyInfoCase("Property Info Case", 3, 3),
    Workflow("WOrkflow", 5, 3),
    Court("Court-related", 3, 1);

    private final String label;
    protected final int userRankMinimumToEnact;
    protected final int userRankMinimumToView;

    private EventType(String label, int enactMin, int viewMin) {
        this.label = label;
        this.userRankMinimumToEnact = enactMin;
        this.userRankMinimumToView = viewMin;
    }

    public String getLabel() {
        return label;
    }

    /**
     * @return the userRankMinimumToEnact
     */
    public int getUserRankMinimumToEnact() {
        return userRankMinimumToEnact;
    }

    /**
     * @return the userRankMinimumToView
     */
    public int getUserRankMinimumToView() {
        return userRankMinimumToView;
    }
    
    
    
    
}
