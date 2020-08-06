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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.Municipality;
import java.util.List;
import java.util.Objects;

/**
 * Represents a set of space types and their associated code elements
 * that are SET TO BE inspected in each. When an occupancy inspection is underway, this
 * set of code elements is used to populate the list of OccInspectedSpaceElements
 * insdie the OccInspectedSpace container
 * 
 * @author Eric Darsow
 */
public class OccChecklistTemplate  {
    
    // note we have a lexicon switch here: in the DB, this object is
    // derived from an inspectionchecklist row. In Javaland, we make
    // a ChecklistTemplate object that contains data from sevearl tables
    private int inspectionChecklistID;
    private Municipality muni;
    private String title;
    private String description;
    private boolean active;
    private CodeSource governingCodeSource;
    
    private List<OccSpaceTypeInspectionDirective> occSpaceTypeTemplateList;

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
     * @return the occSpaceTypeTemplateList
     */
    public List<OccSpaceTypeInspectionDirective> getOccSpaceTypeTemplateList() {
        return occSpaceTypeTemplateList;
    }

    /**
     * @param occSpaceTypeTemplateList the occSpaceTypeTemplateList to set
     */
    public void setOccSpaceTypeTemplateList(List<OccSpaceTypeInspectionDirective> occSpaceTypeTemplateList) {
        this.occSpaceTypeTemplateList = occSpaceTypeTemplateList;
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

    /**
     * @return the governingCodeSource
     */
    public CodeSource getGoverningCodeSource() {
        return governingCodeSource;
    }

    /**
     * @param governingCodeSource the governingCodeSource to set
     */
    public void setGoverningCodeSource(CodeSource governingCodeSource) {
        this.governingCodeSource = governingCodeSource;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.inspectionChecklistID;
        hash = 79 * hash + Objects.hashCode(this.muni);
        hash = 79 * hash + Objects.hashCode(this.title);
        hash = 79 * hash + Objects.hashCode(this.description);
        hash = 79 * hash + (this.active ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.governingCodeSource);
        hash = 79 * hash + Objects.hashCode(this.occSpaceTypeTemplateList);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OccChecklistTemplate other = (OccChecklistTemplate) obj;
        if (this.inspectionChecklistID != other.inspectionChecklistID) {
            return false;
        }
        if (this.active != other.active) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.muni, other.muni)) {
            return false;
        }
        if (!Objects.equals(this.governingCodeSource, other.governingCodeSource)) {
            return false;
        }
        if (!Objects.equals(this.occSpaceTypeTemplateList, other.occSpaceTypeTemplateList)) {
            return false;
        }
        return true;
    }

  
    
}
