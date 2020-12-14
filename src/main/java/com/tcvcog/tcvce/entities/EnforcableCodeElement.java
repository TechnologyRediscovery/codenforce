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
import java.time.LocalDateTime;
import java.util.List;

/**
 *  Represents a codesetelement stated with fidelity to the ERD
 * @author ellen bascomb of apt 31y
 */
public class EnforcableCodeElement 
        extends CodeElement 
        implements Serializable, Cloneable {

    public EnforcableCodeElement() {

    }
    
    public EnforcableCodeElement(CodeElement ele){
        this.elementID = ele.elementID;
        this.guideEntry = ele.guideEntry;
        this.guideEntryID = ele.guideEntryID;
        this.source = ele.source;
        this.ordchapterNo = ele.ordchapterNo;
        this.ordchapterTitle = ele.ordchapterTitle;
        this.ordSecNum = ele.ordSecNum;
        this.ordSecTitle = ele.ordSecTitle;
        this.ordSubSecNum = ele.ordSubSecNum;
        this.ordSubSecTitle = ele.ordSubSecTitle;
        this.ordSubSubSecNum = ele.ordSubSubSecNum;
        this.ordTechnicalText = ele.ordTechnicalText;
        this.ordHumanFriendlyText = ele.ordHumanFriendlyText;
        this.isActive = ele.isActive;
        this.useInjectedValues = ele.useInjectedValues;
        this.resourceURL = ele.resourceURL;
        this.notes = ele.notes;
        this.legacyID = ele.legacyID;
        this.headerString = ele.headerString;
        this.createdTS = ele.createdTS;
        this.createdBy = ele.createdBy;
        this.lastUpdatedTS = ele.lastUpdatedTS;
        this.lastupdatedBy = ele.lastupdatedBy;
        this.deactivatedTS = ele.deactivatedTS;
        this.deactivatedBy = ele.deactivatedBy;
        
    }

    // code set elements and enforcable code elments are equivalent
    // TODO: unify these names
    private int codeSetElementID;
    protected int codeSetID;
    
    // THIS SHOULDN'T Be here, inheritance, not composition!!
    
    private double maxPenalty;
    private double minPenalty;
    private double normPenalty;
    private String penaltyNotes;
    private int normDaysToComply;
    private String daysToComplyNotes;
    private String muniSpecificNotes;
    private List<Fee> feeList;
    private String defaultViolationDescription;
    
    private List<String> injectedValues;
    
    
     /** Humanization Object standard fields **/
    protected LocalDateTime eceCreatedTS;
    protected User eceCreatedBy;
    protected LocalDateTime eceLastUpdatedTS;
    protected User eceLastupdatedBy;
    protected LocalDateTime eceDeactivatedTS;
    protected User eceDeactivatedBy;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return getHeaderString();
    }

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

    public List<Fee> getFeeList() {
        return feeList;
    }

    public void setFeeList(List<Fee> feeList) {
        this.feeList = feeList;
    }

    /**
     * @return the injectedValues
     */
    public List<String> getInjectedValues() {
        return injectedValues;
    }

    /**
     * @param injectedValues the injectedValues to set
     */
    public void setInjectedValues(List<String> injectedValues) {
        this.injectedValues = injectedValues;
    }

    /**
     * @return the defaultViolationDescription
     */
    public String getDefaultViolationDescription() {
        return defaultViolationDescription;
    }

    /**
     * @param defaultViolationDescription the defaultViolationDescription to set
     */
    public void setDefaultViolationDescription(String defaultViolationDescription) {
        this.defaultViolationDescription = defaultViolationDescription;
    }

    /**
     * @return the eceCreatedTS
     */
    public LocalDateTime getEceCreatedTS() {
        return eceCreatedTS;
    }

    /**
     * @return the eceCreatedBy
     */
    public User getEceCreatedBy() {
        return eceCreatedBy;
    }

    /**
     * @return the eceLastUpdatedTS
     */
    public LocalDateTime getEceLastUpdatedTS() {
        return eceLastUpdatedTS;
    }

    /**
     * @return the eceLastupdatedBy
     */
    public User getEceLastupdatedBy() {
        return eceLastupdatedBy;
    }

    /**
     * @return the eceDeactivatedTS
     */
    public LocalDateTime getEceDeactivatedTS() {
        return eceDeactivatedTS;
    }

    /**
     * @return the eceDeactivatedBy
     */
    public User getEceDeactivatedBy() {
        return eceDeactivatedBy;
    }

    /**
     * @param eceCreatedTS the eceCreatedTS to set
     */
    public void setEceCreatedTS(LocalDateTime eceCreatedTS) {
        this.eceCreatedTS = eceCreatedTS;
    }

    /**
     * @param eceCreatedBy the eceCreatedBy to set
     */
    public void setEceCreatedBy(User eceCreatedBy) {
        this.eceCreatedBy = eceCreatedBy;
    }

    /**
     * @param eceLastUpdatedTS the eceLastUpdatedTS to set
     */
    public void setEceLastUpdatedTS(LocalDateTime eceLastUpdatedTS) {
        this.eceLastUpdatedTS = eceLastUpdatedTS;
    }

    /**
     * @param eceLastupdatedBy the eceLastupdatedBy to set
     */
    public void setEceLastupdatedBy(User eceLastupdatedBy) {
        this.eceLastupdatedBy = eceLastupdatedBy;
    }

    /**
     * @param eceDeactivatedTS the eceDeactivatedTS to set
     */
    public void setEceDeactivatedTS(LocalDateTime eceDeactivatedTS) {
        this.eceDeactivatedTS = eceDeactivatedTS;
    }

    /**
     * @param eceDeactivatedBy the eceDeactivatedBy to set
     */
    public void setEceDeactivatedBy(User eceDeactivatedBy) {
        this.eceDeactivatedBy = eceDeactivatedBy;
    }

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

}
