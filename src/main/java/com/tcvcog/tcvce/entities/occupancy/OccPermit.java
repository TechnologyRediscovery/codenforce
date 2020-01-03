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
package com.tcvcog.tcvce.entities.occupancy;

import com.tcvcog.tcvce.entities.CodeSource;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.User;
import java.time.LocalDateTime;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class OccPermit {
    
    private int permitID;
    // used for storing municipality-generated IDs associated with the permit
    private String referenceNo;
    private int periodID;
    private User issuedBy;
    private Person issuedTo;
    private LocalDateTime dateIssued;
    private String permitAdditionalText;
    private String notes;
    
    /**
     * @return the permitID
     */
    public int getPermitID() {
        return permitID;
    }

    /**
     * @return the referenceNo
     */
    public String getReferenceNo() {
        return referenceNo;
    }

   

    /**
     * @return the dateIssued
     */
    public LocalDateTime getDateIssued() {
        return dateIssued;
    }

  

    /**
     * @return the permitAdditionalText
     */
    public String getPermitAdditionalText() {
        return permitAdditionalText;
    }

    /**
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * @param permitID the permitID to set
     */
    public void setPermitID(int permitID) {
        this.permitID = permitID;
    }

    /**
     * @param referenceNo the referenceNo to set
     */
    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    
    /**
     * @param dateIssued the dateIssued to set
     */
    public void setDateIssued(LocalDateTime dateIssued) {
        this.dateIssued = dateIssued;
    }

   

    /**
     * @param permitAdditionalText the permitAdditionalText to set
     */
    public void setPermitAdditionalText(String permitAdditionalText) {
        this.permitAdditionalText = permitAdditionalText;
    }

    /**
     * @param notes the notes to set
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }


    /**
     * @return the periodID
     */
    public int getPeriodID() {
        return periodID;
    }

    /**
     * @param periodID the periodID to set
     */
    public void setPeriodID(int periodID) {
        this.periodID = periodID;
    }

    /**
     * @return the issuedBy
     */
    public User getIssuedBy() {
        return issuedBy;
    }

    /**
     * @param issuedBy the issuedBy to set
     */
    public void setIssuedBy(User issuedBy) {
        this.issuedBy = issuedBy;
    }

    /**
     * @return the issuedTo
     */
    public Person getIssuedTo() {
        return issuedTo;
    }

    /**
     * @param issuedTo the issuedTo to set
     */
    public void setIssuedTo(Person issuedTo) {
        this.issuedTo = issuedTo;
    }
    
    
    
}
