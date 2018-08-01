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


import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Eric C. Darsow
 */
public class PersonBB extends BackingBeanUtils implements Serializable{

    private ArrayList<Person> personList;
    private Person selectedPerson;
    private ArrayList<Person> filteredPersonList;
    
    private String formFName;
    private String formLName;
    private int formPersonID;
    
    
    /**
     * Creates a new instance of PersonBB
     */
    public PersonBB() {
    }
    
    public void searchForPersonByName(ActionEvent event){
        // clear past search results
        personList = null;
        
        PersonIntegrator integrator = getPersonIntegrator();
        
        try {
            personList = integrator.searchForPerson(formFName, formLName);
            if(personList.isEmpty()){
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Database search returned 0 Persons", 
                            "Please try again, perhaps by removing some letters from your name text"));
                
            } else {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Database search returned "+ personList.size() + " Persons", 
                                "Please try again, perhaps by removing some letters from your name text"));
            }
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "System-related search error", "This issue requires administrator attention, sorry"));
            
        }
        
        
    }
    
    public void searchForPersonByID(ActionEvent event){
        
        
    }
    
    public String viewPersonProfile(){
        if(selectedPerson != null){
            
            getSessionBean().setActivePerson(selectedPerson);
            return "personProfile";
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Please select a person from the table to view details", ""));
            return "";
            
        }
    }

    public String deletePerson(){
        System.out.println("PersonBB.deletePerson | in method");
        PersonIntegrator pi = getPersonIntegrator();
        try {
            pi.deletePerson(selectedPerson.getPersonID());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        selectedPerson.getFirstName() + " has been permanently deleted; Goodbye " 
                                + selectedPerson.getFirstName() 
                                + ". Search results have been cleared.", ""));
            
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Cannot delete person." + ex.toString(), "Best not to delete folks anyway..."));
            
        }
        personList = null;
        return "";
    }
    
    /**
     * @return the personList
     */
    public ArrayList<Person> getPersonList() {
        return personList;
    }

    /**
     * @return the formFName
     */
    public String getFormFName() {
        return formFName;
    }

    /**
     * @return the formLName
     */
    public String getFormLName() {
        return formLName;
    }

    /**
     * @return the formPersonID
     */
    public int getFormPersonID() {
        return formPersonID;
    }

    /**
     * @param personList the personList to set
     */
    public void setPersonList(ArrayList<Person> personList) {
        this.personList = personList;
    }

    /**
     * @param formFName the formFName to set
     */
    public void setFormFName(String formFName) {
        this.formFName = formFName;
    }

    /**
     * @param formLName the formLName to set
     */
    public void setFormLName(String formLName) {
        this.formLName = formLName;
    }

    /**
     * @param formPersonID the formPersonID to set
     */
    public void setFormPersonID(int formPersonID) {
        this.formPersonID = formPersonID;
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
