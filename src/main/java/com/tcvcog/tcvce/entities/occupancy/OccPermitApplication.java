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

import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PropertyUnit;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class OccPermitApplication {
    private int id;
    private OccPermitApplicationReason reason;
    private LocalDateTime submissionDate;
    private Date submissionDateUtilDate;;
    private String submissionNotes;
    private String internalNotes;
    private PropertyUnit applicationPropertyUnit;
    private Person applicantPerson;
    private Person preferredContact;
    
    /**
    * This will contain either existing Person objects, new Person objects created by user, or 
 clones of existing Person objects whose reference persons data was changed as part of the 
 application. The occupancy coordinator will digest this list to determine if the requirements 
    * have been satisfied.
    */
    private List<Person> attachedPersons;

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
        submissionDateUtilDate = java.util.Date.from(submissionDate.atZone(ZoneId.systemDefault()).toInstant());
        return submissionDateUtilDate;
    }

    public void setSubmissionDateUtilDate(Date submissionDateUtilDate) {
        this.submissionDateUtilDate = submissionDateUtilDate;
        submissionDate = submissionDateUtilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
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
    public List<Person> getAttachedPersons() {
        return attachedPersons;
    }

    /**
     * @param attachedPersons the attachedPersons to set
     */
    public void setAttachedPersons(List<Person> attachedPersons) {
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

}
