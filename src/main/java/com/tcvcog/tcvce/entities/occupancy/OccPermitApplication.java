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

import com.tcvcog.tcvce.entities.BOb;
import com.tcvcog.tcvce.entities.EntityUtils;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonOccApplication;
import com.tcvcog.tcvce.entities.PropertyUnit;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class OccPermitApplication extends BOb {
    private int id;
    private OccPermitApplicationReason reason;
    private OccApplicationStatusEnum status;
    private LocalDateTime submissionDate;
    private Date submissionDateUtilDate;
    private String submissionNotes;
    private String internalNotes;
    private String externalPublicNotes;
    private PropertyUnit applicationPropertyUnit;
    private Person applicantPerson;
    private Person preferredContact;
    private OccPeriod connectedPeriod;
    private int publicControlCode;
    private boolean paccEnabled;
    private boolean uplinkAccess;
    
    /**
    * This will contain either existing Person objects, new Person objects created by user, or 
 clones of existing Person objects whose reference persons data was changed as part of the 
 application. The occupancy coordinator will digest this list to determine if the requirements 
    * have been satisfied.
    */
    private List<PersonOccApplication> attachedPersons;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the reason
     */
    public OccPermitApplicationReason getReason() {
        return reason;
    }


    /**
     * @return the submissionDate
     */
    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }
    
    /**
     * @return the submissionDate
     */
    public String getSubmissionDatePretty() {
       
        return EntityUtils.getPrettyDate(submissionDate);
    }
    
    /**
     * @return the submissionNotes
     */
    public String getSubmissionNotes() {
        return submissionNotes;
    }

    /**
     * @return the internalNotes
     */
    public String getInternalNotes() {
        return internalNotes;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(OccPermitApplicationReason reason) {
        this.reason = reason;
    }


    /**
     * @param submissionDate the submissionDate to set
     */
    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    /**
     * @param submissionNotes the submissionNotes to set
     */
    public void setSubmissionNotes(String submissionNotes) {
        this.submissionNotes = submissionNotes;
    }

    /**
     * @param internalNotes the internalNotes to set
     */
    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public Date getSubmissionDateUtilDate() {
        submissionDateUtilDate = convertUtilDate(submissionDate);
        return submissionDateUtilDate;
    }

    public void setSubmissionDateUtilDate(Date submissionDateUtilDate) {
        this.submissionDateUtilDate = submissionDateUtilDate;
        submissionDate = convertUtilDate(submissionDateUtilDate);
    }

    public PropertyUnit getApplicationPropertyUnit() {
        return applicationPropertyUnit;
    }

    public void setApplicationPropertyUnit(PropertyUnit applicationPropertyUnit) {
        this.applicationPropertyUnit = applicationPropertyUnit;
    }    

    /**
     * @return the applicantPerson
     */
    public Person getApplicantPerson() {
        return applicantPerson;
    }

    /**
     * @param applicantPerson the applicantPerson to set
     */
    public void setApplicantPerson(Person applicantPerson) {
        this.applicantPerson = applicantPerson;
    }

    /**
     * @return the attachedPersons
     */
    public List<PersonOccApplication> getAttachedPersons() {
        return attachedPersons;
    }

    /**
     * @param attachedPersons the attachedPersons to set
     */
    public void setAttachedPersons(List<PersonOccApplication> attachedPersons) {
        this.attachedPersons = attachedPersons;
    }

    /**
     * @return the preferredContact
     */
    public Person getPreferredContact() {
        return preferredContact;
    }

    /**
     * @param preferredContact the preferredContact to set
     */
    public void setPreferredContact(Person preferredContact) {
        this.preferredContact = preferredContact;
    }

    public OccPeriod getConnectedPeriod() {
        return connectedPeriod;
    }

    public void setConnectedPeriod(OccPeriod connectedPeriod) {
        this.connectedPeriod = connectedPeriod;
    }

    public OccApplicationStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OccApplicationStatusEnum status) {
        this.status = status;
    }

    public String getExternalPublicNotes() {
        return externalPublicNotes;
    }

    public void setExternalPublicNotes(String externalPublicNotes) {
        this.externalPublicNotes = externalPublicNotes;
    }

    public int getPublicControlCode() {
        return publicControlCode;
    }

    public void setPublicControlCode(int publicControlCode) {
        this.publicControlCode = publicControlCode;
    }

    public boolean isPaccEnabled() {
        return paccEnabled;
    }

    public void setPaccEnabled(boolean paccEnabled) {
        this.paccEnabled = paccEnabled;
    }

    public boolean isUplinkAccess() {
        return uplinkAccess;
    }

    public void setUplinkAccess(boolean uplinkAccess) {
        this.uplinkAccess = uplinkAccess;
    }
    
}
