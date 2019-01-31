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


import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.search.SearchParamsPersons;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Ellen Baskem
 */
public class PersonsBB extends BackingBeanUtils implements Serializable{

    private List<Person> personList;
    private Person selectedPerson;
    private List<Person> filteredPersonList;
    private PersonType[] personTypes;
    private String notesToAppend;
    private String updateDescription;
    
    private SearchParamsPersons searchParams;
    
    
    /**
     * Creates a new instance of PersonBB
     */
    public PersonsBB() {
      
    }
    
    @PostConstruct
    public void initBean(){
        try {
            PersonCoordinator pc = getPersonCoordinator();
            PersonIntegrator pi = getPersonIntegrator();
            searchParams = pc.getDefaultSearchParamsPersons(getSessionBean().getActiveMuni());
            selectedPerson = pi.getPerson(100);
        } catch (IntegrationException ex) {
            Logger.getLogger(PersonsBB.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public String viewPersonAssociatedProperty(Property p){
        getSessionBean().setActiveProp(p);
        return "properties";
    }
    
    public void attachNoteToPerson(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();

        try {
            pc.addNotesToPerson(selectedPerson, getSessionBean().getFacesUser(), notesToAppend);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO
                    , "Done: Notes added to person ID:" + selectedPerson.getPersonID(),"" ));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR
                    , "Unable to update notes, sorry!"
                    , getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }
        
    }
    
    
    public void updatePerson(ActionEvent ev){
        System.out.println("PersonsBB.updatePerson");
        PersonIntegrator pi = getPersonIntegrator();
        PersonCoordinator pc = getPersonCoordinator();
        try {
            pc.updatePerson(selectedPerson, getSessionBean().getFacesUser(), updateDescription);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Person updated! This updated person is now your 'active person'", ""));
            // go get the new data:
            selectedPerson = pi.getPerson(selectedPerson.getPersonID());
            System.out.println("PersonsBB.updatePerson : completed update and reloaded bean person");
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to update person, my apologies", ""));
            
        }
        
    }
    
    public void initiatePersonCreation(){
        PersonCoordinator pc = getPersonCoordinator();
        selectedPerson = pc.getNewPersonSkeleton(getSessionBean().getActiveMuni());
        
    }
    
    public void loadPersonHistory(ActionEvent ev){
        System.out.println("PersonsBB.LoadPersonHistory");
        PersonIntegrator pi = getPersonIntegrator();
        try {
            personList = pi.getPersonHistory(getSessionBean().getFacesUser());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "History was loaded!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Could not load history, sorry", ""));
            System.out.println(ex);
        }
        
    }
    
    public void createNewPerson(){
        PersonCoordinator pc = getPersonCoordinator();
        PersonIntegrator pi = getPersonIntegrator();
        UserIntegrator ui = getUserIntegrator();
        
        int newPersonID;
        try {
            newPersonID = pc.addNewPerson(selectedPerson);
            selectedPerson = pi.getPerson(newPersonID);
            getSessionBean().setActivePerson(selectedPerson);
            ui.logObjectView(getSessionBean().getFacesUser(), selectedPerson);
            
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Person created! This person is now your active one and has been added to your history.'", ""));
        
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Unable to create new Person in the system.'", ""));
        }
    }
    
    public void searchForPersons(ActionEvent event){
        System.out.println("PersonBB.searchForPersons");
        // clear past search results on bean and on the session
        personList = null;
        getSessionBean().setActivePersonList(null);
        // this will trigger database lookup logic inside
        // getPersonList() when we tell the search result table to clear itself
    }
    
    
    public void searchForPersonsByNameOnly(ActionEvent event){
        System.out.println("PersonBB.searchForPersonsByNameOnly");
        PersonCoordinator pc = getPersonCoordinator();
        searchParams = pc.getDefaultSearchParamsPersons(getSessionBean().getActiveMuni());
        // clear past search results on bean and on the session
        personList = null;
        getSessionBean().setActivePersonList(null);
        // this will trigger database lookup logic inside
        // getPersonList() when we tell the search result table to clear itself
    }
    
    
    public void selectPerson(Person p){
        UserIntegrator ui = getUserIntegrator();
        try {
            ui.logObjectView(getSessionBean().getFacesUser(), p);
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Person history logging is broken!",""));
        }
        getSessionBean().setActivePerson(selectedPerson);
        System.out.println("PersonsBB.selectPreson | person: " + selectedPerson.getPersonID());
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
    public List<Person> getPersonList() {

        System.out.println("PersonBB.getPersonList");
        PersonIntegrator integrator = getPersonIntegrator();
        List<Person> sessionPersonList = getSessionBean().getActivePersonList();

        // first check if our view-scoped list is emtpy, if so, we need a list!
        if (personList == null) {
            System.out.println("PersonBB.getPersonList | found Null person List");
            if (sessionPersonList != null) { // if we've got a session list, use that before going to DB
                personList = sessionPersonList;
                System.out.println("PersonBB.getPersonList | loaded list from session");
            } else {
                try {
                    personList = integrator.getPersonList(searchParams); // go to Integrator with searchParams
                    getSessionBean().setActivePersonList(personList);
                    if (personList.isEmpty()) {
                        System.out.println("PersonBB.getPersonList | Emtpy list");
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_WARN,
                                        "Database search returned 0 Persons",
                                        "Please try again, perhaps by removing some letters from your name text"));

                    } else {
                        System.out.println("PersonBB.getPersonList | at least 1 in list");
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Database search returned " + personList.size() + " Persons", ""));
                    }

                } catch (IntegrationException ex) {
                    System.out.println(ex);
                    getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "System-related search error", "This issue requires administrator attention, sorry"));
                }
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
    public List<Person> getFilteredPersonList() {
        return filteredPersonList;
    }

    /**
     * @param filteredPersonList the filteredPersonList to set
     */
    public void setFilteredPersonList(List<Person> filteredPersonList) {
        this.filteredPersonList = filteredPersonList;
    }

    /**
     * @return the searchParams
     */
    public SearchParamsPersons getSearchParams() {
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

    /**
     * @return the notesToAppend
     */
    public String getNotesToAppend() {
        return notesToAppend;
    }

    /**
     * @param notesToAppend the notesToAppend to set
     */
    public void setNotesToAppend(String notesToAppend) {
        this.notesToAppend = notesToAppend;
    }

    /**
     * @return the updateDescription
     */
    public String getUpdateDescription() {
        return updateDescription;
    }

    /**
     * @param updateDescription the updateDescription to set
     */
    public void setUpdateDescription(String updateDescription) {
        this.updateDescription = updateDescription;
    }
    
}
