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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.coordinators.OccupancyCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.entities.PersonOccApplication;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Dominic Pimpinella
 */
public class PersonEditPublicBB extends BackingBeanUtils implements Serializable {
    private Person person;
    private Person clonePerson;
    
    private boolean editableLastName;
    private boolean editableFirstName;
    private boolean editableAddress;
    private boolean editableHomePhone;
    private boolean editableCellPhone;
    private boolean editableWorkPhone;
    private boolean editableEmail;

    @PostConstruct
    public void initBean(){
        try{
            createClone(getSessionBean().getActiveAnonPerson());
        } catch (IntegrationException ex) {
            System.out.println("PersonEditPublicBB.initBean | Cannot create clone " + ex.toString());
        }
        setPerson(getSessionBean().getActiveAnonPerson());
        setEditableLastName(false);
        setEditableFirstName(false);
        setEditableAddress(false);
        setEditableHomePhone(false);
        setEditableCellPhone(false);
        setEditableWorkPhone(false);
        setEditableEmail(false);        
    }
    
    public void createClone(Person person) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        User u = new User();
        // THIS IS HARDCODED, DEFAULT, PUBLIC USER SHOULD BE PARAMETER
        u.setUserID(102);
        int cloneId = pi.createClone(person, u);
        person = pi.getPerson(cloneId);
        PersonCoordinator pc = getPersonCoordinator();
        person = pc.anonymizePersonData(person);
        clonePerson = person;
//        clonePerson.setPersonType(getSessionBean().getActivePersonType());
    }
    
    public String addCloneToApplicantPersons(Person clone) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        pi.updatePerson(clone);
        getSessionBean().getSessOccPermitApplication().getAttachedPersons().add((PersonOccApplication) clone);
        OccupancyCoordinator oc = getOccupancyCoordinator();
        try {
        oc.verifyOccPermitPersonsRequirement(getSessionBean().getSessOccPermitApplication());
        } catch(BObStatusException ex){
            System.out.println("PersonEditPublicBB.addCloneToApplicantPersons() | ERROR: " + ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        ex.toString(), ""));
        }
        return "managePeople";
    }
    
    
    /**
     * @return the person
     */
    public Person getClonePerson() {
        return clonePerson;
    }

    /**
     * @param clonePerson the person to set
     */
    public void setClonePerson(Person clonePerson) {
        this.clonePerson = clonePerson;
    }

    /**
     * @return the editableLastName
     */
    public boolean isEditableLastName() {
        return editableLastName;
    }

    /**
     * @param editableLastName the editableLastName to set
     */
    public void setEditableLastName(boolean editableLastName) {
        this.editableLastName = editableLastName;
    }

    /**
     * @return the editableFirstName
     */
    public boolean isEditableFirstName() {
        return editableFirstName;
    }

    /**
     * @param editableFirstName the editableFirstName to set
     */
    public void setEditableFirstName(boolean editableFirstName) {
        this.editableFirstName = editableFirstName;
    }

    /**
     * @return the editableAddress
     */
    public boolean isEditableAddress() {
        return editableAddress;
    }

    /**
     * @param editableAddress the editableAddress to set
     */
    public void setEditableAddress(boolean editableAddress) {
        this.editableAddress = editableAddress;
    }

    /**
     * @return the editableHomePhone
     */
    public boolean isEditableHomePhone() {
        return editableHomePhone;
    }

    /**
     * @param editableHomePhone the editableHomePhone to set
     */
    public void setEditableHomePhone(boolean editableHomePhone) {
        this.editableHomePhone = editableHomePhone;
    }

    /**
     * @return the editableCellPhone
     */
    public boolean isEditableCellPhone() {
        return editableCellPhone;
    }

    /**
     * @param editableCellPhone the editableCellPhone to set
     */
    public void setEditableCellPhone(boolean editableCellPhone) {
        this.editableCellPhone = editableCellPhone;
    }

    /**
     * @return the editableWorkPhone
     */
    public boolean isEditableWorkPhone() {
        return editableWorkPhone;
    }

    /**
     * @param editableWorkPhone the editableWorkPhone to set
     */
    public void setEditableWorkPhone(boolean editableWorkPhone) {
        this.editableWorkPhone = editableWorkPhone;
    }

    /**
     * @return the editableEmail
     */
    public boolean isEditableEmail() {
        return editableEmail;
    }

    /**
     * @param editableEmail the editableEmail to set
     */
    public void setEditableEmail(boolean editableEmail) {
        this.editableEmail = editableEmail;
    }    

    /**
     * @return the person
     */
    public Person getPerson() {
        return person;
    }

    /**
     * @param person the person to set
     */
    public void setPerson(Person person) {
        this.person = person;
    }
    
}
