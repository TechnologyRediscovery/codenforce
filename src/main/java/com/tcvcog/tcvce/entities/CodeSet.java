/*
 * Copyright (C) 2017 Turtle Creek Valley
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class CodeSet implements Serializable {
    
    private int codeSetID;
    
    private Municipality muni;
    private String codeSetName;
    private String codeSetDescription;
    protected boolean active;
    private List<EnforcableCodeElement> enfCodeElementList;
    

    /**
     * @return the codeSetID
     */
    public int getCodeSetID() {
        return codeSetID;
    }

    /**
     * @param codeSetID the codeSetID to set
     */
    public void setCodeSetID(int codeSetID) {
        this.codeSetID = codeSetID;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @return the codeSetName
     */
    public String getCodeSetName() {
        return codeSetName;
    }

    /**
     * @param codeSetName the codeSetName to set
     */
    public void setCodeSetName(String codeSetName) {
        this.codeSetName = codeSetName;
    }

    /**
     * @return the codeSetDescription
     */
    public String getCodeSetDescription() {
        return codeSetDescription;
    }

    /**
     * @param codeSetDescription the codeSetDescription to set
     */
    public void setCodeSetDescription(String codeSetDescription) {
        this.codeSetDescription = codeSetDescription;
    }

   
  

    /**
     * @return the enfCodeElementList
     */
    public List<EnforcableCodeElement> getEnfCodeElementList() {
        return enfCodeElementList;
    }

    /**
     * @param enfCodeElementList the enfCodeElementList to set
     */
    public void setEnfCodeElementList(List<EnforcableCodeElement> enfCodeElementList) {
        this.enfCodeElementList = enfCodeElementList;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 200 * hash + this.codeSetID;
        hash = 200 * hash + Objects.hashCode(this.codeSetName);
        hash = 200 * hash + Objects.hashCode(this.codeSetDescription);
        
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
        final CodeSet other = (CodeSet) obj;
        if (this.codeSetID != other.codeSetID) {
            return false;
        }
        if (!Objects.equals(this.codeSetName, other.codeSetName)) {
            return false;
        }
        if (!Objects.equals(this.codeSetDescription, other.codeSetDescription)) {
            return false;
        }
        return true;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    
}
