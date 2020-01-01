/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities.reports;

import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author sylvia
 */
public class ReportConfigCECase extends Report{
    
    private CECaseDataHeavy cse;
    
    private List<EventCnF> eventListForReport;
    private List<NoticeOfViolation> noticeListForReport;
    
    private boolean includeCaseName; // add to ui
    
    private boolean includeHiddenEvents;
    private boolean includeInactiveEvents;
    
    private boolean includeMunicipalityDiclosedEvents;
    private boolean includeOfficerOnlyEvents;
    
    private boolean includeEventNotes;
    private boolean includeRequestedActionFields;
    private boolean includeEventMetadata; // add to ui
    
    private boolean includeAllNotices;
    private boolean includeNoticeFullText;
     
    private boolean includeFullOrdinanceText;
    private boolean includeViolationPhotos;  // add to 

    /**
     * @return the cse
     */
    public CECaseDataHeavy getCse() {
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
    public void setCse(CECaseDataHeavy cse) {
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
     * @return the includeOfficerOnlyEvents
     */
    public boolean isIncludeOfficerOnlyEvents() {
        return includeOfficerOnlyEvents;
    }

    /**
     * @param includeOfficerOnlyEvents the includeOfficerOnlyEvents to set
     */
    public void setIncludeOfficerOnlyEvents(boolean includeOfficerOnlyEvents) {
        this.includeOfficerOnlyEvents = includeOfficerOnlyEvents;
    }

    /**
     * @return the includeCaseName
     */
    public boolean isIncludeCaseName() {
        return includeCaseName;
    }

    /**
     * @param includeCaseName the includeCaseName to set
     */
    public void setIncludeCaseName(boolean includeCaseName) {
        this.includeCaseName = includeCaseName;
    }

    /**
     * @return the includeEventMetadata
     */
    public boolean isIncludeEventMetadata() {
        return includeEventMetadata;
    }

    /**
     * @param includeEventMetadata the includeEventMetadata to set
     */
    public void setIncludeEventMetadata(boolean includeEventMetadata) {
        this.includeEventMetadata = includeEventMetadata;
    }

    /**
     * @return the includeViolationPhotos
     */
    public boolean isIncludeViolationPhotos() {
        return includeViolationPhotos;
    }

    /**
     * @param includeViolationPhotos the includeViolationPhotos to set
     */
    public void setIncludeViolationPhotos(boolean includeViolationPhotos) {
        this.includeViolationPhotos = includeViolationPhotos;
    }

    /**
     * @return the eventListForReport
     */
    public List<EventCnF> getEventListForReport() {
        return eventListForReport;
    }

    /**
     * @return the noticeListForReport
     */
    public List<NoticeOfViolation> getNoticeListForReport() {
        return noticeListForReport;
    }

    /**
     * @param eventListForReport the eventListForReport to set
     */
    public void setEventListForReport(List<EventCnF> eventListForReport) {
        this.eventListForReport = eventListForReport;
    }

    /**
     * @param noticeListForReport the noticeListForReport to set
     */
    public void setNoticeListForReport(List<NoticeOfViolation> noticeListForReport) {
        this.noticeListForReport = noticeListForReport;
    }
    
    
}

