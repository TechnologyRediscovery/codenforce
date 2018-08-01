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
package com.tcvcog.tcvce.occupancy.entities;

import com.tcvcog.tcvce.entities.Municipality;
import java.util.ArrayList;

/**
 * Represents a set of space types and their associated code elements
 * that are inspected in each. When an occupancy inspection is underway, this
 * set of code elements is used to populate an "implemented inspected space element"
 * checklist that contains pass/fail, comments, date of pass/fail, etc.
 * 
 * @author Eric Darsow
 */
public class ChecklistBlueprint {
    
    // note we have a lexicon switch here: in the DB, this object is 
    // derived from an inspectionchecklist row. In Javaland, we make
    // an InspectionBlueprint object that contains data from sevearl tables
    private int inspectionChecklistID;
    private Municipality muni;
    private String title;
    private String description;
    private boolean active;
    
    private ArrayList<Space> spaceList;

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

   

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the spaceList
     */
    public ArrayList<Space> getSpaceList() {
        return spaceList;
    }

    /**
     * @param spaceList the spaceList to set
     */
    public void setSpaceList(ArrayList<Space> spaceList) {
        this.spaceList = spaceList;
    }

    /**
     * @return the inspectionChecklistID
     */
    public int getInspectionChecklistID() {
        return inspectionChecklistID;
    }

    /**
     * @param inspectionChecklistID the inspectionChecklistID to set
     */
    public void setInspectionChecklistID(int inspectionChecklistID) {
        this.inspectionChecklistID = inspectionChecklistID;
    }

  
    
}
