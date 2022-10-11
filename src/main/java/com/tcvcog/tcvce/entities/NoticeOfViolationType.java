/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 * Represents a type of letter sent to a person, such as a notice of violation
 * or a request for compliance
 * @author sylvia
 */
public class NoticeOfViolationType {
    
    private int typeID;
    private String title;
    private String description;
    
    private EventCategory eventCatSent;
    private EventCategory eventCatFollowUp;
    private EventCategory eventCatReturned;
    private int followUpWindowDays;
    
    private TextBlockCategory textBlockCategory;
    private PrintStyle printStyle;
    
    private BlobLight novHeaderBlob;
    private Municipality muni;
    
    private boolean courtDocument;
    private boolean injectViolations;
    private boolean includeStipCompDate;
    
    private LocalDateTime deactivatedTS;

    /**
     * @return the typeID
     */
    public int getTypeID() {
        return typeID;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }


    /**
     * @return the followUpWindowDays
     */
    public int getFollowUpWindowDays() {
        return followUpWindowDays;
    }

    /**
     * @return the muni
     */
    public Municipality getMuni() {
        return muni;
    }

    /**
     * @return the courtDocument
     */
    public boolean isCourtDocument() {
        return courtDocument;
    }

    /**
     * @return the injectViolations
     */
    public boolean isInjectViolations() {
        return injectViolations;
    }

    /**
     * @return the deactivatedTS
     */
    public LocalDateTime getDeactivatedTS() {
        return deactivatedTS;
    }

    /**
     * @param typeID the typeID to set
     */
    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

  

    /**
     * @param followUpWindowDays the followUpWindowDays to set
     */
    public void setFollowUpWindowDays(int followUpWindowDays) {
        this.followUpWindowDays = followUpWindowDays;
    }

    /**
     * @param muni the muni to set
     */
    public void setMuni(Municipality muni) {
        this.muni = muni;
    }

    /**
     * @param courtDocument the courtDocument to set
     */
    public void setCourtDocument(boolean courtDocument) {
        this.courtDocument = courtDocument;
    }

    /**
     * @param injectViolations the injectViolations to set
     */
    public void setInjectViolations(boolean injectViolations) {
        this.injectViolations = injectViolations;
    }

    /**
     * @param deactivatedTS the deactivatedTS to set
     */
    public void setDeactivatedTS(LocalDateTime deactivatedTS) {
        this.deactivatedTS = deactivatedTS;
    }

    /**
     * @return the textBlockCategory
     */
    public TextBlockCategory getTextBlockCategory() {
        return textBlockCategory;
    }

    /**
     * @param textBlockCategory the textBlockCategory to set
     */
    public void setTextBlockCategory(TextBlockCategory textBlockCategory) {
        this.textBlockCategory = textBlockCategory;
    }

    /**
     * @return the eventCatSent
     */
    public EventCategory getEventCatSent() {
        return eventCatSent;
    }

    /**
     * @return the eventCatFollowUp
     */
    public EventCategory getEventCatFollowUp() {
        return eventCatFollowUp;
    }

    /**
     * @return the eventCatReturned
     */
    public EventCategory getEventCatReturned() {
        return eventCatReturned;
    }

    /**
     * @param eventCatSent the eventCatSent to set
     */
    public void setEventCatSent(EventCategory eventCatSent) {
        this.eventCatSent = eventCatSent;
    }

    /**
     * @param eventCatFollowUp the eventCatFollowUp to set
     */
    public void setEventCatFollowUp(EventCategory eventCatFollowUp) {
        this.eventCatFollowUp = eventCatFollowUp;
    }

    /**
     * @param eventCatReturned the eventCatReturned to set
     */
    public void setEventCatReturned(EventCategory eventCatReturned) {
        this.eventCatReturned = eventCatReturned;
    }

    /**
     * @return the novHeaderBlob
     */
    public BlobLight getNovHeaderBlob() {
        return novHeaderBlob;
    }

    /**
     * @param novHeaderBlob the novHeaderBlob to set
     */
    public void setNovHeaderBlob(BlobLight novHeaderBlob) {
        this.novHeaderBlob = novHeaderBlob;
    }

    /**
     * @return the printStyle
     */
    public PrintStyle getPrintStyle() {
        return printStyle;
    }

    /**
     * @param printStyle the printStyle to set
     */
    public void setPrintStyle(PrintStyle printStyle) {
        this.printStyle = printStyle;
    }

    /**
     * @return the includeStipCompDate
     */
    public boolean isIncludeStipCompDate() {
        return includeStipCompDate;
    }

    /**
     * @param includeStipCompDate the includeStipCompDate to set
     */
    public void setIncludeStipCompDate(boolean includeStipCompDate) {
        this.includeStipCompDate = includeStipCompDate;
    }
    
}
