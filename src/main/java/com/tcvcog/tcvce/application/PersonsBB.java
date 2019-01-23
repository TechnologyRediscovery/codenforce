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
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.search.SearchParamsPersons;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Ellen Baskem
 */
public class PersonsBB extends BackingBeanUtils implements Serializable{

    private ArrayList<Person> personList;
    private Person selectedPerson;
    private ArrayList<Person> filteredPersonList;
    private PersonType[] personTypes;
    
    private SearchParamsPersons searchParams;
    
    
    /**
     * Creates a new instance of PersonBB
     */
    public PersonsBB() {
        personList = new ArrayList<>();
    }
    
    public String viewPersonAssociatedProperty(Property p){
        getSessionBean().setActiveProp(p);
        return "properties";
    }
    
    
    public void updatePerson(ActionEvent ev){
        
        PersonCoordinator pc = getPersonCoordinator();
        try {
            pc.updatePerson(selectedPerson, getSessionBean().getFacesUser());
            getSessionBean().setActivePerson(selectedPerson);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Person updated! This updated person is now your 'active person'", ""));
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to update person, my apologies", ""));
            
        }
        
    }
    
    public void searchForPersons(ActionEvent event){
        System.out.println("PersonBB.searchForPersons");
        // clear past search results
        personList = null;
    }
    
    
    public void selectPerson(Person p){
        selectedPerson = p;
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
        
        System.out.println("PersonBB.getPersonList");
        PersonIntegrator integrator = getPersonIntegrator();
        if(personList == null){
            System.out.println("PersonBB.getPersonList | found Null person List");

            try {
                personList = integrator.getPersonList(searchParams);
                if(personList.isEmpty()){
                    System.out.println("PersonBB.getPersonList | Emtpy list");
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                                "Database search returned 0 Persons", 
                                "Please try again, perhaps by removing some letters from your name text"));

                } else {
                    System.out.println("PersonBB.getPersonList | at least 1 in list");
                    getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                                "Database search returned "+ personList.size() + " Persons", ""));
                }
            } catch (IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "System-related search error", "This issue requires administrator attention, sorry"));
            }
        }
        return personList;
    }
    

    /**
     * @param personList the personList to set
     */
    public void setPersonList(ArrayList<Person> personList) {
        this.personList = personList;
    }

   
    /**
     * @return the selectedPerson
     */
    public Person getSelectedPerson() {
        if(selectedPerson == null){
            selectedPerson = getSessionBean().getActivePerson();
        }
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

    /**
     * @return the searchParams
     */
    public SearchParamsPersons getSearchParams() {
        SearchCoordinator sc = getSearchCoordinator();
        if(searchParams == null){
            searchParams = sc.getDeafaultSearchParamsPersons();
            searchParams.setMuni(getSessionBean().getActiveMuni());
        }
        return searchParams;
    }

    /**
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsPersons searchParams) {
        this.searchParams = searchParams;
    }

    /**
     * @return the personTypes
     */
    public PersonType[] getPersonTypes() {
        return personTypes;
    }

    /**
     * @param personTypes the personTypes to set
     */
    public void setPersonTypes(PersonType[] personTypes) {
        this.personTypes = personTypes;
    }
    
}
