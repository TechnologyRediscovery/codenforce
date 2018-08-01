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

/**
 *
 * @author Eric C. Darsow
 */
public class NoticeOfViolation {
    
    private int noticeID;
    private Person recipient;
    
    private String noticeText;
    private LocalDateTime insertionTimeStamp;
    private String insertionTimeStampPretty;
    
    private LocalDateTime dateOfRecord;
    private String dateOfRecordPretty;
    
    private boolean requestToSend;
    
    private LocalDateTime letterSentDate;
    private String letterSentDatePretty;
    
    private LocalDateTime letterReturnedDate;
    private String letterReturnedDatePretty;

    /**
     * @return the noticeText
     */
    public String getNoticeText() {
        return noticeText;
    }

    /**
     * @param noticeText the noticeText to set
     */
    public void setNoticeText(String noticeText) {
        this.noticeText = noticeText;
    }

    /**
     * @return the noticeID
     */
    public int getNoticeID() {
        return noticeID;
    }

    /**
     * @return the insertionTimeStamp
     */
    public LocalDateTime getInsertionTimeStamp() {
        return insertionTimeStamp;
    }

    /**
     * @return the dateOfRecord
     */
    public LocalDateTime getDateOfRecord() {
        return dateOfRecord;
    }

    /**
     * @return the requestToSend
     */
    public boolean isRequestToSend() {
        return requestToSend;
    }

    /**
     * @return the letterSentDate
     */
    public LocalDateTime getLetterSentDate() {
        return letterSentDate;
    }

    /**
     * @param noticeID the noticeID to set
     */
    public void setNoticeID(int noticeID) {
        this.noticeID = noticeID;
    }

    /**
     * @param insertionTimeStamp the insertionTimeStamp to set
     */
    public void setInsertionTimeStamp(LocalDateTime insertionTimeStamp) {
        this.insertionTimeStamp = insertionTimeStamp;
    }

    /**
     * @param dateOfRecord the dateOfRecord to set
     */
    public void setDateOfRecord(LocalDateTime dateOfRecord) {
        this.dateOfRecord = dateOfRecord;
    }

    /**
     * @param requestToSend the requestToSend to set
     */
    public void setRequestToSend(boolean requestToSend) {
        this.requestToSend = requestToSend;
    }

    /**
     * @param letterSentDate the letterSentDate to set
     */
    public void setLetterSentDate(LocalDateTime letterSentDate) {
        this.letterSentDate = letterSentDate;
    }

    /**
     * @return the insertionTimeStampPretty
     */
    public String getInsertionTimeStampPretty() {
        return insertionTimeStampPretty;
    }

    /**
     * @return the dateOfRecordPretty
     */
    public String getDateOfRecordPretty() {
        return dateOfRecordPretty;
    }

    /**
     * @return the letterSentDatePretty
     */
    public String getLetterSentDatePretty() {
        return letterSentDatePretty;
    }

    /**
     * @param insertionTimeStampPretty the insertionTimeStampPretty to set
     */
    public void setInsertionTimeStampPretty(String insertionTimeStampPretty) {
        this.insertionTimeStampPretty = insertionTimeStampPretty;
    }

    /**
     * @param dateOfRecordPretty the dateOfRecordPretty to set
     */
    public void setDateOfRecordPretty(String dateOfRecordPretty) {
        this.dateOfRecordPretty = dateOfRecordPretty;
    }

    /**
     * @param letterSentDatePretty the letterSentDatePretty to set
     */
    public void setLetterSentDatePretty(String letterSentDatePretty) {
        this.letterSentDatePretty = letterSentDatePretty;
    }

    /**
     * @return the letterReturnedDate
     */
    public LocalDateTime getLetterReturnedDate() {
        return letterReturnedDate;
    }

    /**
     * @param letterReturnedDate the letterReturnedDate to set
     */
    public void setLetterReturnedDate(LocalDateTime letterReturnedDate) {
        this.letterReturnedDate = letterReturnedDate;
    }

    /**
     * @return the letterReturnedDatePretty
     */
    public String getLetterReturnedDatePretty() {
        return letterReturnedDatePretty;
    }

    /**
     * @param letterReturnedDatePretty the letterReturnedDatePretty to set
     */
    public void setLetterReturnedDatePretty(String letterReturnedDatePretty) {
        this.letterReturnedDatePretty = letterReturnedDatePretty;
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
    
}
