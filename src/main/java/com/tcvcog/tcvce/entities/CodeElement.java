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

import com.tcvcog.tcvce.util.Constants;
import java.time.LocalDateTime;

/**
 * The root class of the Code Element family!
 * @author ellen bascomb of apt 31y
 */
public class CodeElement
        extends BOb
        implements Comparable<Object>{
    
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
    protected String ordSubSubSecTitle;
    
    protected String ordTechnicalText;
    
    protected String ordHumanFriendlyText;
    protected boolean active;
    protected boolean usingInjectedValues;
    
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

    public CodeElement() {}

    public CodeElement(CodeElement codeElement) {
        if(codeElement != null){

            this.elementID = codeElement.getElementID();
            this.guideEntry = codeElement.getGuideEntry();
            this.guideEntryID = codeElement.getGuideEntryID();
            this.source = codeElement.getSource();
            this.ordchapterNo = codeElement.getOrdchapterNo();
            this.ordchapterTitle = codeElement.getOrdchapterTitle();
            this.ordSecNum = codeElement.getOrdSecNum();
            this.ordSecTitle = codeElement.getOrdSecTitle();
            this.ordSubSecNum = codeElement.getOrdSubSecNum();
            this.ordSubSecTitle = codeElement.getOrdSubSecTitle();
            this.ordSubSubSecNum = codeElement.getOrdSubSubSecNum();
            this.ordSubSubSecTitle = codeElement.getOrdSubSubSecTitle();
            this.ordTechnicalText = codeElement.getOrdTechnicalText();
            this.ordHumanFriendlyText = codeElement.getOrdHumanFriendlyText();
            this.active = codeElement.isActive();
            this.usingInjectedValues = codeElement.isUsingInjectedValues();
            this.resourceURL = codeElement.getResourceURL();
            this.notes = codeElement.getNotes();
            this.legacyID = codeElement.getLegacyID();
            this.headerString = codeElement.getHeaderString();
            this.createdTS = codeElement.getCreatedTS();
            this.createdBy = codeElement.getCreatedBy();
            this.lastUpdatedTS = codeElement.getLastUpdatedTS();
            this.lastupdatedBy = codeElement.getLastupdatedBy();
            this.deactivatedTS = codeElement.getDeactivatedTS();
            this.deactivatedBy = codeElement.getDeactivatedBy();
        }
    }
    
   
    /**
     * Compares based on chapter no, then sec no, then subsec no, then subsub sec no
     * @param o
     * @return 
     */
    @Override
    public int compareTo(Object o) {
        if(o == null){
            throw new NullPointerException("Cannot compare myself to a null");
        }
        if(!(o instanceof CodeElement)){
            throw new ClassCastException("Cannot cast given object to CodeElement");
        }
        CodeElement inel = (CodeElement) o;
        
        int compChapterNo = 0;
        
        if(this.ordchapterNo < inel.ordchapterNo){
            compChapterNo = -1;
        } else if(this.ordchapterNo > inel.ordchapterNo){
            compChapterNo = 1;
        }
        
        // Same chapter, so now we have to use sec number
        if(compChapterNo != 0){
            return compChapterNo;
        } else {
            if(ordSecNum != null && inel.ordSecNum != null){
                int compSecNo = this.ordSecNum.compareTo(inel.ordSecNum);
                if(compSecNo != 0){
                    return compSecNo;
                } else{
                    if(ordSubSecNum != null && inel.ordSubSecNum != null){
                        int compSubSecNo = this.ordSubSecNum.compareTo((inel.ordSubSecNum));
                        if(compSubSecNo != 0){
                            return compSubSecNo;
                        } else{
                            if(ordSubSubSecNum != null && inel.ordSubSubSecNum != null){
                                return this.ordSubSubSecNum.compareTo(inel.ordSubSubSecNum);
                            } else {
                                return compSubSecNo;
                            }
                        }
                    } else {
                        return compSecNo;
                    }
                }
            } else {
                return compChapterNo;
            }
        }
    }

    
    
     /**
     * @return the headerString
     */
    public String getHeaderString() {
       
        
        return headerString;
    }


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
    public boolean isActive() {
        return active;
    }

    /**
     * @param isActive the isActive to set
     */
    public void setActive(boolean isActive) {
        this.active = isActive;
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
    public boolean isUsingInjectedValues() {
        return usingInjectedValues;
    }

    /**
     * @param usingInjectedValues the useInjectedValues to set
     */
    public void setUsingInjectedValues(boolean usingInjectedValues) {
        this.usingInjectedValues = usingInjectedValues;
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

    /**
     * @return the ordSubSubSecTitle
     */
    public String getOrdSubSubSecTitle() {
        return ordSubSubSecTitle;
    }

    /**
     * @param ordSubSubSecTitle the ordSubSubSecTitle to set
     */
    public void setOrdSubSubSecTitle(String ordSubSubSecTitle) {
        this.ordSubSubSecTitle = ordSubSubSecTitle;
    }

    

    
}
