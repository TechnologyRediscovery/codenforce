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

import java.time.LocalDateTime;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public  class       CodeElement 
        extends     BOb{
    
    protected int elementID;
    
    protected CodeElementGuideEntry guideEntry;
    protected int guideEntryID;
    protected CodeSource source;
    
    protected int ordchapterNo;
    
    protected String ordchapterTitle;
    protected String ordSecNum;
    protected String ordSecTitle;
    
    protected String ordSubSecNum;
    protected String ordSubSecTitle;
    
    protected String ordSubSubSecNum;
    
    protected String ordTechnicalText;
    
    protected String ordHumanFriendlyText;
    protected boolean isActive;
    protected boolean useInjectedValues;
    
    protected String resourceURL;
    
    protected String notes;
    protected int legacyID;
    
    protected String headerString;
    /** Humanization Object standard fields **/
    protected LocalDateTime createdTS;
    protected User createdBy;
    protected LocalDateTime lastUpdatedTS;
    protected User lastupdatedBy;
    protected LocalDateTime deactivatedTS;
    protected User deactivatedBy;
    

    
    
    
    /**
     * @return the createdTS
     */
    public LocalDateTime getCreatedTS() {
        return createdTS;
    }

    /**
     * @return the createdBy
     */
    public User getCreatedBy() {
        return createdBy;
    }

    /**
     * @return the lastUpdatedTS
     */
    public LocalDateTime getLastUpdatedTS() {
        return lastUpdatedTS;
    }

    /**
     * @return the lastupdatedBy
     */
    public User getLastupdatedBy() {
        return lastupdatedBy;
    }

    /**
     * @return the deactivatedTS
     */
    public LocalDateTime getDeactivatedTS() {
        return deactivatedTS;
    }

    /**
     * @return the deactivatedBy
     */
    public User getDeactivatedBy() {
        return deactivatedBy;
    }

   

    /**
     * @param createdTS the createdTS to set
     */
    public void setCreatedTS(LocalDateTime createdTS) {
        this.createdTS = createdTS;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * @param lastUpdatedTS the lastUpdatedTS to set
     */
    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }

    /**
     * @param lastupdatedBy the lastupdatedBy to set
     */
    public void setLastupdatedBy(User lastupdatedBy) {
        this.lastupdatedBy = lastupdatedBy;
    }

    /**
     * @param deactivatedTS the deactivatedTS to set
     */
    public void setDeactivatedTS(LocalDateTime deactivatedTS) {
        this.deactivatedTS = deactivatedTS;
    }

    /**
     * @param deactivatedBy the deactivatedBy to set
     */
    public void setDeactivatedBy(User deactivatedBy) {
        this.deactivatedBy = deactivatedBy;
    }
    
    /**
     * @return the elementID
     */
    public int getElementID() {
        return elementID;
    }

    /**
     * @param elementID the elementID to set
     */
    public void setElementID(int elementID) {
        this.elementID = elementID;
    }

  
 

    /**
     * @return the ordchapterNo
     */
    public int getOrdchapterNo() {
        return ordchapterNo;
    }

    /**
     * @param ordchapterNo the ordchapterNo to set
     */
    public void setOrdchapterNo(int ordchapterNo) {
        this.ordchapterNo = ordchapterNo;
    }

    /**
     * @return the ordchapterTitle
     */
    public String getOrdchapterTitle() {
        return ordchapterTitle;
    }

    /**
     * @param ordchapterTitle the ordchapterTitle to set
     */
    public void setOrdchapterTitle(String ordchapterTitle) {
        this.ordchapterTitle = ordchapterTitle;
    }

    /**
     * @return the ordSecNum
     */
    public String getOrdSecNum() {
        return ordSecNum;
    }

    /**
     * @param ordSecNum the ordSecNum to set
     */
    public void setOrdSecNum(String ordSecNum) {
        this.ordSecNum = ordSecNum;
    }

    /**
     * @return the ordSecTitle
     */
    public String getOrdSecTitle() {
        return ordSecTitle;
    }

    /**
     * @param ordSecTitle the ordSecTitle to set
     */
    public void setOrdSecTitle(String ordSecTitle) {
        this.ordSecTitle = ordSecTitle;
    }

    /**
     * @return the ordSubSecNum
     */
    public String getOrdSubSecNum() {
        return ordSubSecNum;
    }

    /**
     * @param ordSubSecNum the ordSubSecNum to set
     */
    public void setOrdSubSecNum(String ordSubSecNum) {
        this.ordSubSecNum = ordSubSecNum;
    }

    /**
     * @return the ordSubSecTitle
     */
    public String getOrdSubSecTitle() {
        return ordSubSecTitle;
    }

    /**
     * @param ordSubSecTitle the ordSubSecTitle to set
     */
    public void setOrdSubSecTitle(String ordSubSecTitle) {
        this.ordSubSecTitle = ordSubSecTitle;
    }

    /**
     * @return the ordTechnicalText
     */
    public String getOrdTechnicalText() {
        return ordTechnicalText;
    }

    /**
     * @param ordTechnicalText the ordTechnicalText to set
     */
    public void setOrdTechnicalText(String ordTechnicalText) {
        this.ordTechnicalText = ordTechnicalText;
    }

    /**
     * @return the ordHumanFriendlyText
     */
    public String getOrdHumanFriendlyText() {
        return ordHumanFriendlyText;
    }

    /**
     * @param ordHumanFriendlyText the ordHumanFriendlyText to set
     */
    public void setOrdHumanFriendlyText(String ordHumanFriendlyText) {
        this.ordHumanFriendlyText = ordHumanFriendlyText;
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



    /**
     * @return the resourceURL
     */
    public String getResourceURL() {
        return resourceURL;
    }

    /**
     * @param resourceURL the resourceURL to set
     */
    public void setResourceURL(String resourceURL) {
        this.resourceURL = resourceURL;
    }

   
  

    /**
     * @return the source
     */
    public CodeSource getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(CodeSource source) {
        this.source = source;
    }

    /**
     * @return the guideEntry
     */
    public CodeElementGuideEntry getGuideEntry() {
        return guideEntry;
    }

    /**
     * @param guideEntry the guideEntry to set
     */
    public void setGuideEntry(CodeElementGuideEntry guideEntry) {
        this.guideEntry = guideEntry;
    }

    /**
     * @return the guideEntryID
     */
    public int getGuideEntryID() {
        return guideEntryID;
    }

    /**
     * @param guideEntryID the guideEntryID to set
     */
    public void setGuideEntryID(int guideEntryID) {
        this.guideEntryID = guideEntryID;
    }

    /**
     * @return the headerString
     */
    public String getHeaderString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ordchapterTitle);
        sb.append(":");
        sb.append(ordSecTitle);
        sb.append("-");
        sb.append(ordSubSecNum);
        sb.append(":");
        sb.append(ordSubSecTitle);
        headerString = sb.toString();
        
        return headerString;
    }

    /**
     * @param headerString the headerString to set
     */
    public void setHeaderString(String headerString) {
        this.headerString = headerString;
    }

    /**
     * @return the ordSubSubSecNum
     */
    public String getOrdSubSubSecNum() {
        return ordSubSubSecNum;
    }

    /**
     * @param ordSubSubSecNum the ordSubSubSecNum to set
     */
    public void setOrdSubSubSecNum(String ordSubSubSecNum) {
        this.ordSubSubSecNum = ordSubSubSecNum;
    }

    /**
     * @return the useInjectedValues
     */
    public boolean isUseInjectedValues() {
        return useInjectedValues;
    }

    /**
     * @param useInjectedValues the useInjectedValues to set
     */
    public void setUseInjectedValues(boolean useInjectedValues) {
        this.useInjectedValues = useInjectedValues;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the legacyID
     */
    public int getLegacyID() {
        return legacyID;
    }

    /**
     * @param legacyID the legacyID to set
     */
    public void setLegacyID(int legacyID) {
        this.legacyID = legacyID;
    }


    
}
