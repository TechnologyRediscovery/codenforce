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

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author Eric C. Darsow
 */
public class NoticeOfViolation extends EntityUtils {
    
    private int noticeID;
    private Person recipient;
    
    private String noticeTextBeforeViolations;
    private List<CodeViolation> violationList;
    private String noticeTextAfterViolations;
    
    private LocalDateTime dateOfRecord;
    private String dateOfRecordPretty;
    
    private LocalDateTime creationTS;
    private String creationTSPretty;

    private LocalDateTime lockedAndqueuedTS;
    private String lockedAndQueuedTSPretty;
    private Person lockedAndQueuedBy;
    
    private LocalDateTime sentTS;
    private String sentTSPretty;
    private Person sentBy;
    
    private LocalDateTime returnedTS;
    private String returnedTSPretty;
    private Person returnedBy;
    
    private String notes;

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
        return creationTSPretty;
    }

    /**
     * @return the dateOfRecordPretty
     */
    public String getDateOfRecordPretty() {
        return dateOfRecordPretty;
    }

    /**
     * @return the sentTSPretty
     */
    public String getSentTSPretty() {
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
    public List<CodeViolation> getViolationList() {
        return violationList;
    }

    /**
     * @param violationList the violationList to set
     */
    public void setViolationList(List<CodeViolation> violationList) {
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
        return lockedAndQueuedTSPretty;
    }

    /**
     * @return the lockedAndQueuedBy
     */
    public Person getLockedAndQueuedBy() {
        return lockedAndQueuedBy;
    }

    /**
     * @return the sentBy
     */
    public Person getSentBy() {
        return sentBy;
    }

    /**
     * @return the returnedBy
     */
    public Person getReturnedBy() {
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
    public void setLockedAndQueuedBy(Person lockedAndQueuedBy) {
        this.lockedAndQueuedBy = lockedAndQueuedBy;
    }

    /**
     * @param sentBy the sentBy to set
     */
    public void setSentBy(Person sentBy) {
        this.sentBy = sentBy;
    }

    /**
     * @param returnedBy the returnedBy to set
     */
    public void setReturnedBy(Person returnedBy) {
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
    
}
