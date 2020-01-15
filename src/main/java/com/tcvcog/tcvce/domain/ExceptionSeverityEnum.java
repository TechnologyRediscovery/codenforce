/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.domain;

/**
 *
 * @author sylvia
 */
public enum ExceptionSeverityEnum {
    
    // follows burn severity, with 4th degree burn proceedding through the skin
    // and destroying the underlying nerve endings
    
    SESSION_FATAL(4, "A complete session cannot be composed"),
    SESSION_RESTRICTING_FAILURE(3, "A session will be built but core functions limited"),
    NONCRITICAL_FAILURE(2, "A non-core subsystem is improperly functioning"),
    TESTING_FAILURE(1, "Test or fully support-only systems are non-functional");
    
    
    private final int severityDegree;
    private final String description;
    
    
    private ExceptionSeverityEnum(int deg, String des){
        severityDegree = deg;
        description = des;
    }

    /**
     * @return the severityDegree
     */
    public int getSeverityDegree() {
        return severityDegree;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
}
