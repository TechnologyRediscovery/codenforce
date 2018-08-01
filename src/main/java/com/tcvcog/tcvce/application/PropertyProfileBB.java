package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import java.io.Serializable;
import java.util.ArrayList;

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

/**
 *
 * @author Eric C. Darsow
 */
public class PropertyProfileBB extends BackingBeanUtils implements Serializable{
    private Property currentProperty;
    private ArrayList<Person> propertyPersonList;
    private Person selectedPerson;
    private ArrayList<Person> filteredPersonList;
    
    
    private ArrayList<CECase> ceCaseList;
    private ArrayList<CEActionRequest> ceActionRequestList;

    /**
     * Creates a new instance of PropertyProfileBB
     */
    public PropertyProfileBB() {
    }
    
    
    public String createCase(){
        
        
        return "addNewCase";
    }
    
    public String viewPersonProfile(){
        getSessionBean().setActivePerson(selectedPerson);
        return "personProfile";
    }
    

    
    /**
     * @return the currentProperty
     */
    public Property getCurrentProperty() {
        
        currentProperty = getSessionBean().getActiveProp();
        return currentProperty;
    }

    /**
     * @param currentProperty the currentProperty to set
     */
    public void setCurrentProperty(Property currentProperty) {
        this.currentProperty = currentProperty;
    }
    
    public String updateProperty(){
        System.out.println("PropertyProfileBB.updateProperty");
        return "propertyUpdate";
        
    }

    /**
     * @return the propertyPersonList
     */
    public ArrayList<Person> getPropertyPersonList() {
        PersonIntegrator pi = getPersonIntegrator();
        try {
            propertyPersonList = pi.getPersonList(currentProperty);
        } catch (IntegrationException ex) {
            // do nothing
        }
        return propertyPersonList;
    }

    /**
     * @return the ceCaseList
     */
    public ArrayList<CECase> getCeCaseList() {
        return ceCaseList;
    }

    /**
     * @return the ceActionRequestList
     */
    public ArrayList<CEActionRequest> getCeActionRequestList() {
        return ceActionRequestList;
    }

    /**
     * @param propertyPersonList the propertyPersonList to set
     */
    public void setPropertyPersonList(ArrayList<Person> propertyPersonList) {
        this.propertyPersonList = propertyPersonList;
    }

    /**
     * @param ceCaseList the ceCaseList to set
     */
    public void setCeCaseList(ArrayList<CECase> ceCaseList) {
        this.ceCaseList = ceCaseList;
    }

    /**
     * @param ceActionRequestList the ceActionRequestList to set
     */
    public void setCeActionRequestList(ArrayList<CEActionRequest> ceActionRequestList) {
        this.ceActionRequestList = ceActionRequestList;
    }

    /**
     * @return the selectedPerson
     */
    public Person getSelectedPerson() {
        return selectedPerson;
    }

    /**
     * @param selectedPerson the selectedPerson to set
     */
    public void setSelectedPerson(Person selectedPerson) {
        this.selectedPerson = selectedPerson;
    }

    /**
     * @return the filteredPersonList
     */
    public ArrayList<Person> getFilteredPersonList() {
        return filteredPersonList;
    }

    /**
     * @param filteredPersonList the filteredPersonList to set
     */
    public void setFilteredPersonList(ArrayList<Person> filteredPersonList) {
        this.filteredPersonList = filteredPersonList;
    }
    
    
    
    
    
}
