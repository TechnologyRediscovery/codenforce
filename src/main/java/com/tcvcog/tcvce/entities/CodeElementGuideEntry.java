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

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CodeElementGuideEntry implements Serializable {
    
    private int guideEntryID;
    private String category;
    private String subCategory;
    private String description;
    // rich text content
    private String enforcementGuidelines;
    // rich text content
    private String inspectionGuidelines;
    private boolean priority;

    /**
     * @return the guideEntryID
     */
    public int getGuideEntryID() {
        return guideEntryID;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @return the subCategory
     */
    public String getSubCategory() {
        return subCategory;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the enforcementGuidelines
     */
    public String getEnforcementGuidelines() {
        return enforcementGuidelines;
    }

    /**
     * @return the inspectionGuidelines
     */
    public String getInspectionGuidelines() {
        return inspectionGuidelines;
    }

    /**
     * @return the priority
     */
    public boolean isPriority() {
        return priority;
    }

    /**
     * @param guideEntryID the guideEntryID to set
     */
    public void setGuideEntryID(int guideEntryID) {
        this.guideEntryID = guideEntryID;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @param subCategory the subCategory to set
     */
    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param enforcementGuidelines the enforcementGuidelines to set
     */
    public void setEnforcementGuidelines(String enforcementGuidelines) {
        this.enforcementGuidelines = enforcementGuidelines;
    }

    /**
     * @param inspectionGuidelines the inspectionGuidelines to set
     */
    public void setInspectionGuidelines(String inspectionGuidelines) {
        this.inspectionGuidelines = inspectionGuidelines;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(boolean priority) {
        this.priority = priority;
    }
    
   
    
}
