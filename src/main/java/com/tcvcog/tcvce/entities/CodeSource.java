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
 *
 * @author ellen bascomb of apt 31y
 */
public  class CodeSource 
        implements Serializable{
    
    private int sourceID;
    private String sourceName;
    private int sourceYear;
    
    private String sourceDescription;
    private String url;
    private boolean isActive;
    private String sourceNotes;

    /**
     * @return the sourceID
     */
    public int getSourceID() {
        return sourceID;
    }

    /**
     * @param sourceID the sourceID to set
     */
    public void setSourceID(int sourceID) {
        this.sourceID = sourceID;
    }

    /**
     * @return the sourceName
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * @param sourceName the sourceName to set
     */
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * @return the sourceYear
     */
    public int getSourceYear() {
        return sourceYear;
    }

    /**
     * @param sourceYear the sourceYear to set
     */
    public void setSourceYear(int sourceYear) {
        this.sourceYear = sourceYear;
    }

    /**
     * @return the sourceDescription
     */
    public String getSourceDescription() {
        return sourceDescription;
    }

    /**
     * @param sourceDescription the sourceDescription to set
     */
    public void setSourceDescription(String sourceDescription) {
        this.sourceDescription = sourceDescription;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the sourceNotes
     */
    public String getSourceNotes() {
        return sourceNotes;
    }

    /**
     * @param sourceNotes the sourceNotes to set
     */
    public void setSourceNotes(String sourceNotes) {
        this.sourceNotes = sourceNotes;
    }

    /**
     * @return the isActive
     */
    public boolean isIsActive() {
        return isActive;
    }

    /**
     * @param isActive the isActive to set
     */
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }
    
    @Override
    public int hashCode() {
        int hash = 10;
        hash = 300 * hash + this.sourceID;
        hash = 300 * hash + this.sourceYear;
        hash = 300 * hash + Objects.hashCode(this.sourceName);
        hash = 300 * hash + Objects.hashCode(this.sourceDescription);
        hash = 300 * hash + Objects.hashCode(this.url);
        hash = 300 * hash + Objects.hashCode(this.sourceNotes);
        hash = 300 * hash + (this.isActive ? 1 : 0);
        
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
        final CodeSource other = (CodeSource) obj;
        if (this.sourceID != other.sourceID) {
            return false;
        }
        if (this.sourceYear != other.sourceYear) {
            return false;
        }
        if (this.isActive != other.isActive) {
            return false;
        }
        if (!Objects.equals(this.sourceName, other.sourceName)) {
            return false;
        }
        if (!Objects.equals(this.sourceDescription, other.sourceDescription)) {
            return false;
        }
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        if (!Objects.equals(this.sourceNotes, other.sourceNotes)) {
            return false;
        }
        return true;
    }
    
}
