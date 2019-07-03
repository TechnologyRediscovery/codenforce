/*
 * Copyright (C) 2018 Technology Rediscovery, LLC.
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
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

/**
 *
 * @author Nathan Dietz
 */
public class CodeSetElement {

    private int codeSetElementID;
    private int codeSetID;
    private int codeElementID;
    private int elementMaxPenalty;
    private int elementMinPenalty;
    private int elementNormPenalty;
    private String penaltyNotes;
    private int normDaysToComply;
    private String daysToComplyNotes;
    private String muniSpecificNotes;
    private int defaultSeverityClassID;
    private int feeID;
    
    public CodeSetElement() {
    }

    public int getCodeSetElementID() {
        return codeSetElementID;
    }

    public void setCodeSetElementID(int codeSetElementID) {
        this.codeSetElementID = codeSetElementID;
    }

    public int getCodeSetID() {
        return codeSetID;
    }

    public void setCodeSetID(int codeSetID) {
        this.codeSetID = codeSetID;
    }

    public int getCodeElementID() {
        return codeElementID;
    }

    public void setCodeElementID(int codeElementID) {
        this.codeElementID = codeElementID;
    }

    public int getElementMaxPenalty() {
        return elementMaxPenalty;
    }

    public void setElementMaxPenalty(int elementMaxPenalty) {
        this.elementMaxPenalty = elementMaxPenalty;
    }

    public int getElementMinPenalty() {
        return elementMinPenalty;
    }

    public void setElementMinPenalty(int elementMinPenalty) {
        this.elementMinPenalty = elementMinPenalty;
    }

    public int getElementNormPenalty() {
        return elementNormPenalty;
    }

    public void setElementNormPenalty(int elementNormPenalty) {
        this.elementNormPenalty = elementNormPenalty;
    }

    public String getPenaltyNotes() {
        return penaltyNotes;
    }

    public void setPenaltyNotes(String penaltyNotes) {
        this.penaltyNotes = penaltyNotes;
    }

    public int getNormDaysToComply() {
        return normDaysToComply;
    }

    public void setNormDaysToComply(int normDaysToComply) {
        this.normDaysToComply = normDaysToComply;
    }

    public String getDaysToComplyNotes() {
        return daysToComplyNotes;
    }

    public void setDaysToComplyNotes(String daysToComplyNotes) {
        this.daysToComplyNotes = daysToComplyNotes;
    }

    public String getMuniSpecificNotes() {
        return muniSpecificNotes;
    }

    public void setMuniSpecificNotes(String muniSpecificNotes) {
        this.muniSpecificNotes = muniSpecificNotes;
    }

    public int getDefaultSeverityClassID() {
        return defaultSeverityClassID;
    }

    public void setDefaultSeverityClassID(int defaultSeverityClassID) {
        this.defaultSeverityClassID = defaultSeverityClassID;
    }

    public int getFeeID() {
        return feeID;
    }

    public void setFeeID(int feeID) {
        this.feeID = feeID;
    }
   
    
    
}
