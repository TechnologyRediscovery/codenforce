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

import java.util.ArrayList;

/**
 *
 * @author Eric C. Darsow
 */
public class CodeSet {
    
    private int codeSetID;
    private int muniCode;
    private Municipality muni;
    private String codeSetName;
    private String codeSetDescription;
    private ArrayList<EnforcableCodeElement> enfCodeElementList;

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
     * @return the muniCode
     */
    public int getMuniCode() {
        return muniCode;
    }

    /**
     * @param muniCode the muniCode to set
     */
    public void setMuniCode(int muniCode) {
        this.muniCode = muniCode;
    }

    /**
     * @return the enfCodeElementList
     */
    public ArrayList<EnforcableCodeElement> getEnfCodeElementList() {
        return enfCodeElementList;
    }

    /**
     * @param enfCodeElementList the enfCodeElementList to set
     */
    public void setEnfCodeElementList(ArrayList<EnforcableCodeElement> enfCodeElementList) {
        this.enfCodeElementList = enfCodeElementList;
    }
    
}
