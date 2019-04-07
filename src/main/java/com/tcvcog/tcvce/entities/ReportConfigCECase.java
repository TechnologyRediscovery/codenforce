/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;

/**
 *
 * @author sylvia
 */
public class ReportConfigCECase extends ReportConfig{
    
    private CECase ceCase;
    
    private boolean includeHiddenEvents;
    private boolean includeInactiveEvents;
    private boolean includeEventNotes;
    private boolean includeRequestedActionFields;
    
    private boolean includeAllNotices;
    private boolean includeNoticeFullText;
     
    private boolean includeFullOrdinanceText;

    /**
     * @return the ceCase
     */
    public CECase getCeCase() {
        return ceCase;
    }

    /**
     * @return the includeHiddenEvents
     */
    public boolean isIncludeHiddenEvents() {
        return includeHiddenEvents;
    }

    /**
     * @return the includeInactiveEvents
     */
    public boolean isIncludeInactiveEvents() {
        return includeInactiveEvents;
    }

    /**
     * @return the includeEventNotes
     */
    public boolean isIncludeEventNotes() {
        return includeEventNotes;
    }

    /**
     * @return the includeRequestedActionFields
     */
    public boolean isIncludeRequestedActionFields() {
        return includeRequestedActionFields;
    }

    /**
     * @return the includeNoticeFullText
     */
    public boolean isIncludeNoticeFullText() {
        return includeNoticeFullText;
    }

    /**
     * @return the includeFullOrdinanceText
     */
    public boolean isIncludeFullOrdinanceText() {
        return includeFullOrdinanceText;
    }

    /**
     * @param ceCase the ceCase to set
     */
    public void setCeCase(CECase ceCase) {
        this.ceCase = ceCase;
    }

    /**
     * @param includeHiddenEvents the includeHiddenEvents to set
     */
    public void setIncludeHiddenEvents(boolean includeHiddenEvents) {
        this.includeHiddenEvents = includeHiddenEvents;
    }

    /**
     * @param includeInactiveEvents the includeInactiveEvents to set
     */
    public void setIncludeInactiveEvents(boolean includeInactiveEvents) {
        this.includeInactiveEvents = includeInactiveEvents;
    }

    /**
     * @param includeEventNotes the includeEventNotes to set
     */
    public void setIncludeEventNotes(boolean includeEventNotes) {
        this.includeEventNotes = includeEventNotes;
    }

    /**
     * @param includeRequestedActionFields the includeRequestedActionFields to set
     */
    public void setIncludeRequestedActionFields(boolean includeRequestedActionFields) {
        this.includeRequestedActionFields = includeRequestedActionFields;
    }

    /**
     * @param includeNoticeFullText the includeNoticeFullText to set
     */
    public void setIncludeNoticeFullText(boolean includeNoticeFullText) {
        this.includeNoticeFullText = includeNoticeFullText;
    }

    /**
     * @param includeFullOrdinanceText the includeFullOrdinanceText to set
     */
    public void setIncludeFullOrdinanceText(boolean includeFullOrdinanceText) {
        this.includeFullOrdinanceText = includeFullOrdinanceText;
    }

    /**
     * @return the includeAllNotices
     */
    public boolean isIncludeAllNotices() {
        return includeAllNotices;
    }

    /**
     * @param includeAllNotices the includeAllNotices to set
     */
    public void setIncludeAllNotices(boolean includeAllNotices) {
        this.includeAllNotices = includeAllNotices;
    }
    
    
}

