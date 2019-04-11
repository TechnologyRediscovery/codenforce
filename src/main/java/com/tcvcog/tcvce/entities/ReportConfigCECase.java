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
    
    private CECase cse;
    
    private boolean includeHiddenEvents;
    private boolean includeInactiveEvents;
    
    private boolean includeMunicipalityDiclosedEvents;
    private boolean includeOfficeOnlyEvents;
    
    private boolean includeEventNotes;
    private boolean includeRequestedActionFields;
    
    private boolean includeAllNotices;
    private boolean includeNoticeFullText;
     
    private boolean includeFullOrdinanceText;

    /**
     * @return the cse
     */
    public CECase getCse() {
        return cse;
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
     * @param cse the cse to set
     */
    public void setCse(CECase cse) {
        this.cse = cse;
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

    /**
     * @return the includeMunicipalityDiclosedEvents
     */
    public boolean isIncludeMunicipalityDiclosedEvents() {
        return includeMunicipalityDiclosedEvents;
    }

    /**
     * @param includeMunicipalityDiclosedEvents the includeMunicipalityDiclosedEvents to set
     */
    public void setIncludeMunicipalityDiclosedEvents(boolean includeMunicipalityDiclosedEvents) {
        this.includeMunicipalityDiclosedEvents = includeMunicipalityDiclosedEvents;
    }

    /**
     * @return the includeOfficeOnlyEvents
     */
    public boolean isIncludeOfficeOnlyEvents() {
        return includeOfficeOnlyEvents;
    }

    /**
     * @param includeOfficeOnlyEvents the includeOfficeOnlyEvents to set
     */
    public void setIncludeOfficeOnlyEvents(boolean includeOfficeOnlyEvents) {
        this.includeOfficeOnlyEvents = includeOfficeOnlyEvents;
    }
    
    
}

