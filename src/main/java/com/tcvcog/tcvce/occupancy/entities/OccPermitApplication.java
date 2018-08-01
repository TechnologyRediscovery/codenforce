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
package com.tcvcog.tcvce.occupancy.entities;

import com.tcvcog.tcvce.entities.Person;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author Eric C. Darsow
 */
public class OccPermitApplication {
    private int id;
    private OccPermitApplicationReason reason;
    private boolean multiUnit;
    private LocalDateTime submissionDate;
    private Person currentOwner;
    private Person contactPerson;
    private ArrayList<Person> newOccupants;
    private Person newOwner;
    private String submissionNotes;
    private String internalNotes;

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
     * @return the multiUnit
     */
    public boolean isMultiUnit() {
        return multiUnit;
    }

    /**
     * @return the submissionDate
     */
    public LocalDateTime getSubmissionDate() {
        return submissionDate;
    }

    /**
     * @return the currentOwner
     */
    public Person getCurrentOwner() {
        return currentOwner;
    }

    /**
     * @return the contactPerson
     */
    public Person getContactPerson() {
        return contactPerson;
    }

    /**
     * @return the newOccupants
     */
    public ArrayList<Person> getNewOccupants() {
        return newOccupants;
    }

    /**
     * @return the newOwner
     */
    public Person getNewOwner() {
        return newOwner;
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
     * @param multiUnit the multiUnit to set
     */
    public void setMultiUnit(boolean multiUnit) {
        this.multiUnit = multiUnit;
    }

    /**
     * @param submissionDate the submissionDate to set
     */
    public void setSubmissionDate(LocalDateTime submissionDate) {
        this.submissionDate = submissionDate;
    }

    /**
     * @param currentOwner the currentOwner to set
     */
    public void setCurrentOwner(Person currentOwner) {
        this.currentOwner = currentOwner;
    }

    /**
     * @param contactPerson the contactPerson to set
     */
    public void setContactPerson(Person contactPerson) {
        this.contactPerson = contactPerson;
    }

    /**
     * @param newOccupants the newOccupants to set
     */
    public void setNewOccupants(ArrayList<Person> newOccupants) {
        this.newOccupants = newOccupants;
    }

    /**
     * @param newOwner the newOwner to set
     */
    public void setNewOwner(Person newOwner) {
        this.newOwner = newOwner;
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
    
}
