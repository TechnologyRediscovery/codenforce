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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.CodeElement;

/**
 * Represents a specific ordinance that MIGHT be inspected
 * when its host OccSpaceType is chosen during an inspection
 * i.e. The SpaceType is kitchen
 * And it has inside it a couple of elements to inspect:
 *  1) Drain traps
 * 2) GFCI outlets
 * 3) Exhaust fan
 * 
 * 
 * This is a wrapper of a CodeElement and basically holds just a 
 * required flag.
 * 
 * @author sylvia
 */
public class OccSpaceElement extends CodeElement {
    
    protected int occChecklistSpaceTypeElementID;
    protected boolean requiredForInspection;

    public OccSpaceElement() {}

    public OccSpaceElement(CodeElement codeElement) {
        super(codeElement);
    }

    public OccSpaceElement(OccSpaceElement occSpaceElement) {
        super(occSpaceElement);
        this.occChecklistSpaceTypeElementID = occSpaceElement.getOccChecklistSpaceTypeElementID();
        this.requiredForInspection = occSpaceElement.isRequiredForInspection();
    }


    /**
     * @return the occChecklistSpaceTypeElementID
     */
    public int getOccChecklistSpaceTypeElementID() {
        return occChecklistSpaceTypeElementID;
    }

    /**
     * @param occChecklistSpaceTypeElementID the occChecklistSpaceTypeElementID to set
     */
    public void setOccChecklistSpaceTypeElementID(int occChecklistSpaceTypeElementID) {
        this.occChecklistSpaceTypeElementID = occChecklistSpaceTypeElementID;
    }

    /**
     * @return the requiredForInspection
     */
    public boolean isRequiredForInspection() {
        return requiredForInspection;
    }

    /**
     * @param requiredForInspection the requiredForInspection to set
     */
    public void setRequiredForInspection(boolean requiredForInspection) {
        this.requiredForInspection = requiredForInspection;
    }

   
}
