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
public enum EventType {
    Origination("Case Origination"),
    Action("Officer Action"),
    CaseAdmin("Case Admin"),
    PhaseChange("Case Phase Change"),
    Closing("Case Closing"),
    Timeline("Case Timeline"),
    Communication("Communication"),
    Meeting("Meeting"),
    Notice("Notice"),
    Custom("Custom"),
    Compliance("Compliance"),
    Citation("Citation");

    private final String label;

    private EventType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
