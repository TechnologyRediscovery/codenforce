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

/**
 *  Represents a codesetelement stated with fidelity to the ERD
 * @author Eric C. Darsow
 */
public class EnforcableCodeElement extends CodeElement{
    
    public EnforcableCodeElement(){
    
    }
    
    // code set elements and enforcable code elments are equivalent
    // TODO: unify these names
    private int codeSetElementID;
    private CodeElement codeElement;
    private double maxPenalty;
    private double minPenalty;
    private double normPenalty;
    private String penaltyNotes;
    private int normDaysToComply;
    private String daysToComplyNotes;
    private String muniSpecificNotes;

    /**
     * @return the maxPenalty
     */
    public double getMaxPenalty() {
        return maxPenalty;
    }

    /**
     * @param maxPenalty the maxPenalty to set
     */
    public void setMaxPenalty(double maxPenalty) {
        this.maxPenalty = maxPenalty;
    }

    /**
     * @return the minPenalty
     */
    public double getMinPenalty() {
        return minPenalty;
    }

    /**
     * @param minPenalty the minPenalty to set
     */
    public void setMinPenalty(double minPenalty) {
        this.minPenalty = minPenalty;
    }

    /**
     * @return the normPenalty
     */
    public double getNormPenalty() {
        return normPenalty;
    }

    /**
     * @param normPenalty the normPenalty to set
     */
    public void setNormPenalty(double normPenalty) {
        this.normPenalty = normPenalty;
    }

    /**
     * @return the penaltyNotes
     */
    public String getPenaltyNotes() {
        return penaltyNotes;
    }

    /**
     * @param penaltyNotes the penaltyNotes to set
     */
    public void setPenaltyNotes(String penaltyNotes) {
        this.penaltyNotes = penaltyNotes;
    }

    /**
     * @return the normDaysToComply
     */
    public int getNormDaysToComply() {
        return normDaysToComply;
    }

    /**
     * @param normDaysToComply the normDaysToComply to set
     */
    public void setNormDaysToComply(int normDaysToComply) {
        this.normDaysToComply = normDaysToComply;
    }

    /**
     * @return the daysToComplyNotes
     */
    public String getDaysToComplyNotes() {
        return daysToComplyNotes;
    }

    /**
     * @param daysToComplyNotes the daysToComplyNotes to set
     */
    public void setDaysToComplyNotes(String daysToComplyNotes) {
        this.daysToComplyNotes = daysToComplyNotes;
    }

    /**
     * @return the codeElement
     */
    public CodeElement getCodeElement() {
        return codeElement;
    }

    /**
     * @param codeElement the codeElement to set
     */
    public void setCodeElement(CodeElement codeElement) {
        this.codeElement = codeElement;
    }

    /**
     * @return the codeSetElementID
     */
    public int getCodeSetElementID() {
        return codeSetElementID;
    }

    /**
     * @param codeSetElementID the codeSetElementID to set
     */
    public void setCodeSetElementID(int codeSetElementID) {
        this.codeSetElementID = codeSetElementID;
    }

    /**
     * @return the muniSpecificNotes
     */
    public String getMuniSpecificNotes() {
        return muniSpecificNotes;
    }

    /**
     * @param muniSpecificNotes the muniSpecificNotes to set
     */
    public void setMuniSpecificNotes(String muniSpecificNotes) {
        this.muniSpecificNotes = muniSpecificNotes;
    }
    
}
