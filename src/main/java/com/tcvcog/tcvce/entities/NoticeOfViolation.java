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
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Eric C. Darsow
 */
public class NoticeOfViolation extends EntityUtils implements Serializable, Comparable<NoticeOfViolation> {
    
    private int noticeID;
    private Person recipient;
    
    private String noticeTextBeforeViolations;
    private List<CodeViolationDisplayable> violationList;
    private String noticeTextAfterViolations;
    
    private LocalDateTime dateOfRecord;
    private java.util.Date dateOfRecordUtilDate;
    private String dateOfRecordPretty;
    
    private LocalDateTime creationTS;
    private String creationTSPretty;
    private User creationBy;
    
    private int headerImageID;
   
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
     * @return the creationTSPretty
     */
    public String getCreationTSPretty() {
        if(creationTS != null){
            creationTSPretty = getPrettyDate(creationTS);
        }
        return creationTSPretty;
    }

    /**
     * @return the dateOfRecordPretty
     */
    public String getDateOfRecordPretty() {
        if(dateOfRecord != null){
            dateOfRecordPretty = getPrettyDate(dateOfRecord);
        }
        return dateOfRecordPretty;
    }

    /**
     * @return the sentTSPretty
     */
    public String getSentTSPretty() {
        if(sentTS != null){
            sentTSPretty = getPrettyDate(sentTS);
        }
        return sentTSPretty;
    }

    /**
     * @param creationTSPretty the creationTSPretty to set
     */
    public void setCreationTSPretty(String creationTSPretty) {
        this.creationTSPretty = creationTSPretty;
    }

    /**
     * @param dateOfRecordPretty the dateOfRecordPretty to set
     */
    public void setDateOfRecordPretty(String dateOfRecordPretty) {
        this.dateOfRecordPretty = dateOfRecordPretty;
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
            returnedTSPretty = getPrettyDate(returnedTS);
            
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
            lockedAndQueuedTSPretty = getPrettyDate(lockedAndqueuedTS);
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
     * @return the dateOfRecordUtilDate
     */
    public java.util.Date getDateOfRecordUtilDate() {
        dateOfRecordUtilDate = java.util.Date.from(getDateOfRecord().atZone(ZoneId.systemDefault()).toInstant());
        return dateOfRecordUtilDate;
    }

    /**
     * @param dateOfRecordUtilDate the dateOfRecordUtilDate to set
     */
    public void setDateOfRecordUtilDate(java.util.Date dateOfRecordUtilDate) {
        if(dateOfRecordUtilDate != null){
            dateOfRecord = dateOfRecordUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        this.dateOfRecordUtilDate = dateOfRecordUtilDate;
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
    
}
