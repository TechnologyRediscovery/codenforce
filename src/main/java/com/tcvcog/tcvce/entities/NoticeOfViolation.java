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

import com.tcvcog.tcvce.util.DateTimeUtil;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public  class NoticeOfViolation  
        extends BOb 
        implements Serializable, 
        Comparable<NoticeOfViolation> {
    
    private int noticeID;
    
    /**
     * Not deprecated with humanization
     */
    private Person recipient;
    private MailingAddress recipientMailingAddress;
    /**
     * Records a timestamp for when the human and connected mailing address
     *  is transferred into these NOV-specific fields, such that we can always
     * resurrect exactly where an NOV was sent, even when the underlying 
     * records get updated after mailing
     */
    private LocalDateTime fixedAddrXferTS;
    private String fixedRecipientName;
    private String fixedRecipientBldgNo;
    private String fixedRecipientStreet;
    private String fixedRecipientCity;
    private String fixedRecipientState;
    private String fixedRecipientZip;
    
    private String noticeTextBeforeViolations;
    private List<TextBlock> blocksBeforeViolations;
    
    private List<CodeViolationDisplayable> violationList;
    private String noticeTextAfterViolations;
    private List<TextBlock> blocksAfterViolations;
    
    
    private LocalDateTime dateOfRecord;
    
    private LocalDateTime creationTS;
   
    private User creationBy;
    
    private User notifyingOfficer;
    private Person notifyingOfficerPerson;
    
    private String fixedNotifyingOfficerName;
    private String fixedNotifyingOfficerTitle;
    private String fixedNotifyingOfficerPhone;
    private String fixedNotifyingOfficerEmail;
    
    private int headerImageID;
    
    private boolean injectViolations;
   
    private LocalDateTime lockedAndqueuedTS;
    private String lockedAndQueuedTSPretty;
    private User lockedAndQueuedBy;
    
    private LocalDateTime sentTS;
    private String sentTSPretty;
    private User sentBy;
    
    private LocalDateTime returnedTS;
    private String returnedTSPretty;
    private User returnedBy;
    
    private String notes;
   
    private PrintStyle style;
    private boolean useSignatureImage;
    private boolean includeViolationPhotoAttachment;
    
    private int followupEventDaysRequest;
    private EventCnF followupEvent;
    
    private boolean active;
    

    @Override
    public int compareTo(NoticeOfViolation nv) {
        return dateOfRecord.compareTo(nv.getDateOfRecord());
    }
    
    

    /**
     * @return the noticeTextBeforeViolations
     */
    public String getNoticeTextBeforeViolations() {
        return noticeTextBeforeViolations;
    }

    /**
     * @param noticeTextBeforeViolations the noticeTextBeforeViolations to set
     */
    public void setNoticeTextBeforeViolations(String noticeTextBeforeViolations) {
        this.noticeTextBeforeViolations = noticeTextBeforeViolations;
    }

    /**
     * @return the noticeID
     */
    public int getNoticeID() {
        return noticeID;
    }

    /**
     * @return the creationTS
     */
    public LocalDateTime getCreationTS() {
        return creationTS;
    }

    /**
     * @return the dateOfRecord
     */
    public LocalDateTime getDateOfRecord() {
        return dateOfRecord;
    }

    /**
     * @return the sentTS
     */
    public LocalDateTime getSentTS() {
        return sentTS;
    }

    /**
     * @param noticeID the noticeID to set
     */
    public void setNoticeID(int noticeID) {
        this.noticeID = noticeID;
    }

    /**
     * @param creationTS the creationTS to set
     */
    public void setCreationTS(LocalDateTime creationTS) {
        this.creationTS = creationTS;
    }

    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(LocalDateTime dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }


    /**
     * @param sentTS the sentTS to set
     */
    public void setSentTS(LocalDateTime sentTS) {
        this.sentTS = sentTS;
    }

   
    /**
     * @return the sentTSPretty
     */
    public String getSentTSPretty() {
        if(sentTS != null){
            sentTSPretty = DateTimeUtil.getPrettyDate(sentTS);
        }
        return sentTSPretty;
    }

  

    /**
     * @param sentTSPretty the sentTSPretty to set
     */
    public void setSentTSPretty(String sentTSPretty) {
        this.sentTSPretty = sentTSPretty;
    }

    /**
     * @return the returnedTS
     */
    public LocalDateTime getReturnedTS() {
        return returnedTS;
    }

    /**
     * @param returnedTS the returnedTS to set
     */
    public void setReturnedTS(LocalDateTime returnedTS) {
        this.returnedTS = returnedTS;
    }

    /**
     * @return the returnedTSPretty
     */
    public String getReturnedTSPretty() {
        if(returnedTS != null){
            returnedTSPretty = DateTimeUtil.getPrettyDate(returnedTS);
            
        }
        return returnedTSPretty;
    }

    /**
     * @param returnedTSPretty the returnedTSPretty to set
     */
    public void setReturnedTSPretty(String returnedTSPretty) {
        this.returnedTSPretty = returnedTSPretty;
    }

    /**
     * @return the recipient
     */
    public Person getRecipient() {
        return recipient;
    }

    /**
     * @param recipient the recipient to set
     */
    public void setRecipient(Person recipient) {
        this.recipient = recipient;
    }

    /**
     * @return the noticeTextAfterViolations
     */
    public String getNoticeTextAfterViolations() {
        return noticeTextAfterViolations;
    }

    /**
     * @param noticeTextAfterViolations the noticeTextAfterViolations to set
     */
    public void setNoticeTextAfterViolations(String noticeTextAfterViolations) {
        this.noticeTextAfterViolations = noticeTextAfterViolations;
    }

    /**
     * @return the violationList
     */
    public List<CodeViolationDisplayable> getViolationList() {
        return violationList;
    }

    /**
     * @param violationList the violationList to set
     */
    public void setViolationList(List<CodeViolationDisplayable> violationList) {
        this.violationList = violationList;
    }

    /**
     * @return the lockedAndqueuedTS
     */
    public LocalDateTime getLockedAndqueuedTS() {
        return lockedAndqueuedTS;
    }

    /**
     * @return the lockedAndQueuedTSPretty
     */
    public String getLockedAndQueuedTSPretty() {
        if(lockedAndqueuedTS != null){
            lockedAndQueuedTSPretty = DateTimeUtil.getPrettyDate(lockedAndqueuedTS);
        }
        return lockedAndQueuedTSPretty;
    }

    /**
     * @return the lockedAndQueuedBy
     */
    public User getLockedAndQueuedBy() {
        return lockedAndQueuedBy;
    }

    /**
     * @return the sentBy
     */
    public User getSentBy() {
        return sentBy;
    }

    /**
     * @return the returnedBy
     */
    public User getReturnedBy() {
        return returnedBy;
    }

    /**
     * @param lockedAndqueuedTS the lockedAndqueuedTS to set
     */
    public void setLockedAndqueuedTS(LocalDateTime lockedAndqueuedTS) {
        this.lockedAndqueuedTS = lockedAndqueuedTS;
    }

    /**
     * @param lockedAndQueuedTSPretty the lockedAndQueuedTSPretty to set
     */
    public void setLockedAndQueuedTSPretty(String lockedAndQueuedTSPretty) {
        this.lockedAndQueuedTSPretty = lockedAndQueuedTSPretty;
    }

    /**
     * @param lockedAndQueuedBy the lockedAndQueuedBy to set
     */
    public void setLockedAndQueuedBy(User lockedAndQueuedBy) {
        this.lockedAndQueuedBy = lockedAndQueuedBy;
    }

    /**
     * @param sentBy the sentBy to set
     */
    public void setSentBy(User sentBy) {
        this.sentBy = sentBy;
    }

    /**
     * @param returnedBy the returnedBy to set
     */
    public void setReturnedBy(User returnedBy) {
        this.returnedBy = returnedBy;
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
     * @return the creationBy
     */
    public User getCreationBy() {
        return creationBy;
    }

    /**
     * @param creationBy the creationBy to set
     */
    public void setCreationBy(User creationBy) {
        this.creationBy = creationBy;
    }

    
    /**
     * @return the useSignatureImage
     */
    public boolean isUseSignatureImage() {
        return useSignatureImage;
    }

   
    /**
     * @param useSignatureImage the useSignatureImage to set
     */
    public void setUseSignatureImage(boolean useSignatureImage) {
        this.useSignatureImage = useSignatureImage;
    }

    /**
     * @return the includeViolationPhotoAttachment
     */
    public boolean isIncludeViolationPhotoAttachment() {
        includeViolationPhotoAttachment = false;
        if(!violationList.isEmpty()){
            Iterator<CodeViolationDisplayable> iter = violationList.iterator();
            while(iter.hasNext()){
                CodeViolationDisplayable cvd = iter.next();
                if(cvd.isIncludeViolationPhotos()){
                    includeViolationPhotoAttachment = true;
                }
            }
        }
        return includeViolationPhotoAttachment;
    }

    /**
     * @param includeViolationPhotoAttachment the includeViolationPhotoAttachment to set
     */
    public void setIncludeViolationPhotoAttachment(boolean includeViolationPhotoAttachment) {
        this.includeViolationPhotoAttachment = includeViolationPhotoAttachment;
    }

   
  
    /**
     * @return the headerImageID
     */
    public int getHeaderImageID() {
        return headerImageID;
    }

    /**
     * @param headerImageID the headerImageID to set
     */
    public void setHeaderImageID(int headerImageID) {
        this.headerImageID = headerImageID;
    }

    /**
     * @return the style
     */
    public PrintStyle getStyle() {
        return style;
    }

    /**
     * @param style the style to set
     */
    public void setStyle(PrintStyle style) {
        this.style = style;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the blocksBeforeViolations
     */
    public List<TextBlock> getBlocksBeforeViolations() {
        return blocksBeforeViolations;
    }

    /**
     * @return the blocksAfterViolations
     */
    public List<TextBlock> getBlocksAfterViolations() {
        return blocksAfterViolations;
    }

    /**
     * @param blocksBeforeViolations the blocksBeforeViolations to set
     */
    public void setBlocksBeforeViolations(List<TextBlock> blocksBeforeViolations) {
        this.blocksBeforeViolations = blocksBeforeViolations;
    }

    /**
     * @param blocksAfterViolations the blocksAfterViolations to set
     */
    public void setBlocksAfterViolations(List<TextBlock> blocksAfterViolations) {
        this.blocksAfterViolations = blocksAfterViolations;
    }

    /**
     * @return the injectViolations
     */
    public boolean isInjectViolations() {
        return injectViolations;
    }

    /**
     * @param injectViolations the injectViolations to set
     */
    public void setInjectViolations(boolean injectViolations) {
        this.injectViolations = injectViolations;
    }

    /**
     * @return the followupEvent
     */
    public EventCnF getFollowupEvent() {
        return followupEvent;
    }

    /**
     * @param followupEvent the followupEvent to set
     */
    public void setFollowupEvent(EventCnF followupEvent) {
        this.followupEvent = followupEvent;
    }

    /**
     * @return the followupEventDaysRequest
     */
    public int getFollowupEventDaysRequest() {
        return followupEventDaysRequest;
    }

    /**
     * @param followupEventDaysRequest the followupEventDaysRequest to set
     */
    public void setFollowupEventDaysRequest(int followupEventDaysRequest) {
        this.followupEventDaysRequest = followupEventDaysRequest;
    }

    /**
     * @return the notifyingOfficer
     */
    public User getNotifyingOfficer() {
        return notifyingOfficer;
    }

    /**
     * @param notifyingOfficer the notifyingOfficer to set
     */
    public void setNotifyingOfficer(User notifyingOfficer) {
        this.notifyingOfficer = notifyingOfficer;
    }

    /**
     * @return the fixedAddrXferTS
     */
    public LocalDateTime getFixedAddrXferTS() {
        return fixedAddrXferTS;
    }

    /**
     * @return the fixedRecipientName
     */
    public String getFixedRecipientName() {
        return fixedRecipientName;
    }

    /**
     * @return the fixedRecipientBldgNo
     */
    public String getFixedRecipientBldgNo() {
        return fixedRecipientBldgNo;
    }

    /**
     * @return the fixedRecipientStreet
     */
    public String getFixedRecipientStreet() {
        return fixedRecipientStreet;
    }

    /**
     * @return the fixedRecipientCity
     */
    public String getFixedRecipientCity() {
        return fixedRecipientCity;
    }

    /**
     * @return the fixedRecipientState
     */
    public String getFixedRecipientState() {
        return fixedRecipientState;
    }

    /**
     * @return the fixedRecipientZip
     */
    public String getFixedRecipientZip() {
        return fixedRecipientZip;
    }

  

    /**
     * @param fixedAddrXferTS the fixedAddrXferTS to set
     */
    public void setFixedAddrXferTS(LocalDateTime fixedAddrXferTS) {
        this.fixedAddrXferTS = fixedAddrXferTS;
    }

    /**
     * @param fixedRecipientName the fixedRecipientName to set
     */
    public void setFixedRecipientName(String fixedRecipientName) {
        this.fixedRecipientName = fixedRecipientName;
    }

    /**
     * @param fixedRecipientBldgNo the fixedRecipientBldgNo to set
     */
    public void setFixedRecipientBldgNo(String fixedRecipientBldgNo) {
        this.fixedRecipientBldgNo = fixedRecipientBldgNo;
    }

    /**
     * @param fixedRecipientStreet the fixedRecipientStreet to set
     */
    public void setFixedRecipientStreet(String fixedRecipientStreet) {
        this.fixedRecipientStreet = fixedRecipientStreet;
    }

    /**
     * @param fixedRecipientCity the fixedRecipientCity to set
     */
    public void setFixedRecipientCity(String fixedRecipientCity) {
        this.fixedRecipientCity = fixedRecipientCity;
    }

    /**
     * @param fixedRecipientState the fixedRecipientState to set
     */
    public void setFixedRecipientState(String fixedRecipientState) {
        this.fixedRecipientState = fixedRecipientState;
    }

    /**
     * @param fixedRecipientZip the fixedRecipientZip to set
     */
    public void setFixedRecipientZip(String fixedRecipientZip) {
        this.fixedRecipientZip = fixedRecipientZip;
    }

    /**
     * @return the notifyingOfficerPerson
     */
    public Person getNotifyingOfficerPerson() {
        return notifyingOfficerPerson;
    }

    /**
     * @param notifyingOfficerPerson the notifyingOfficerPerson to set
     */
    public void setNotifyingOfficerPerson(Person notifyingOfficerPerson) {
        this.notifyingOfficerPerson = notifyingOfficerPerson;
    }

    /**
     * @return the fixedNotifyingOfficerName
     */
    public String getFixedNotifyingOfficerName() {
        return fixedNotifyingOfficerName;
    }

    /**
     * @return the fixedNotifyingOfficerTitle
     */
    public String getFixedNotifyingOfficerTitle() {
        return fixedNotifyingOfficerTitle;
    }

    /**
     * @return the fixedNotifyingOfficerPhone
     */
    public String getFixedNotifyingOfficerPhone() {
        return fixedNotifyingOfficerPhone;
    }

    /**
     * @return the fixedNotifyingOfficerEmail
     */
    public String getFixedNotifyingOfficerEmail() {
        return fixedNotifyingOfficerEmail;
    }

    /**
     * @param fixedNotifyingOfficerName the fixedNotifyingOfficerName to set
     */
    public void setFixedNotifyingOfficerName(String fixedNotifyingOfficerName) {
        this.fixedNotifyingOfficerName = fixedNotifyingOfficerName;
    }

    /**
     * @param fixedNotifyingOfficerTitle the fixedNotifyingOfficerTitle to set
     */
    public void setFixedNotifyingOfficerTitle(String fixedNotifyingOfficerTitle) {
        this.fixedNotifyingOfficerTitle = fixedNotifyingOfficerTitle;
    }

    /**
     * @param fixedNotifyingOfficerPhone the fixedNotifyingOfficerPhone to set
     */
    public void setFixedNotifyingOfficerPhone(String fixedNotifyingOfficerPhone) {
        this.fixedNotifyingOfficerPhone = fixedNotifyingOfficerPhone;
    }

    /**
     * @param fixedNotifyingOfficerEmail the fixedNotifyingOfficerEmail to set
     */
    public void setFixedNotifyingOfficerEmail(String fixedNotifyingOfficerEmail) {
        this.fixedNotifyingOfficerEmail = fixedNotifyingOfficerEmail;
    }

    /**
     * @return the recipientMailingAddress
     */
    public MailingAddress getRecipientMailingAddress() {
        return recipientMailingAddress;
    }

    /**
     * @param recipientMailingAddress the recipientMailingAddress to set
     */
    public void setRecipientMailingAddress(MailingAddress recipientMailingAddress) {
        this.recipientMailingAddress = recipientMailingAddress;
    }

   
}
