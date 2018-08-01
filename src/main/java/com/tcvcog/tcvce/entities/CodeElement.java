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
 * @author Eric C. Darsow
 */
public class CodeElement {
    
    private int elementID;
    
    private CodeElementGuideEntry guideEntry;
    private int guideEntryID;
    private CodeSource source;
    
    private int ordchapterNo;
    
    private String ordchapterTitle;
    private String ordSecNum;
    private String ordSecTitle;
    
    private String ordSubSecNum;
    private String ordSubSecTitle;
    private String ordTechnicalText;
    
    private String ordHumanFriendlyText;
    private boolean isActive;
    
    private String resourceURL;
    private LocalDateTime dateCreated;

    
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
     * @return the dateCreated
     */
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    /**
     * @param dateCreated the dateCreated to set
     */
    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
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


    
}
