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

import java.util.Objects;

/**
 *
 * @author Eric C. Darsow
 */
public class CitationStatus {
    private int citationStatusID;
    private String statusTitle;
    private String description;

    /**
     * @return the citationStatusID
     */
    public int getCitationStatusID() {
        return citationStatusID;
    }

    /**
     * @return the statusTitle
     */
    public String getStatusTitle() {
        return statusTitle;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param citationStatusID the citationStatusID to set
     */
    public void setCitationStatusID(int citationStatusID) {
        this.citationStatusID = citationStatusID;
    }

    /**
     * @param statusTitle the statusTitle to set
     */
    public void setStatusTitle(String statusTitle) {
        this.statusTitle = statusTitle;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.citationStatusID;
        hash = 79 * hash + Objects.hashCode(this.statusTitle);
        hash = 79 * hash + Objects.hashCode(this.description);
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
        final CitationStatus other = (CitationStatus) obj;
        if (this.citationStatusID != other.citationStatusID) {
            return false;
        }
        if (!Objects.equals(this.statusTitle, other.statusTitle)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        return true;
    }
    
}
