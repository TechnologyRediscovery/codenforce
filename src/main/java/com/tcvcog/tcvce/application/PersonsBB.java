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
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.MunicipalityIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private List<Property> propertyPersonList;
    
    private List<Person> filteredPersonList;
    private PersonType[] personTypes;
    private String notesToAppend;
    private String updateDescription;
    private Map<String, Integer> muniNameIDMap;
    
    private List<Property> propertyCandidateList;
    private Property selectedProperty;
    
    private SearchParamsPerson searchParams;
    
    
    /**
     * Creates a new instance of PersonBB
     */
    public PersonsBB() {
      
    }
    
    @PostConstruct
    public void initBean(){
        PersonCoordinator pc = getPersonCoordinator();
        MunicipalityIntegrator mi = getMunicipalityIntegrator();
        searchParams = pc.getDefaultSearchParamsPersons(getSessionBean().getSessionMuni());
        // the selected person should be initiated using logic in getSelectedPerson
        selectedPerson = getSessionBean().getSessionPerson();
        try {
            muniNameIDMap = mi.getMunicipalityStringIDMap();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        propertyCandidateList = getSessionBean().getSessionPropertyList();
        loadPersonHistory();
    }
    
    public String viewPersonAssociatedProperty(Property p){
        getSessionBean().getSessionPropertyList().add(0, p);
        return "properties";
    }
    
    public void attachNoteToPerson(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();

        try {
            pc.addNotesToPerson(selectedPerson, getSessionBean().getSessionUser(), notesToAppend);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO
                    , "Done: Notes added to person ID:" + selectedPerson.getPersonID(),"" ));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR
                    , "Unable to update notes, sorry!"
                    , getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        }
    }
    
    
    public String updatePerson(){
        System.out.println("PersonsBB.updatePerson");
        PersonIntegrator pi = getPersonIntegrator();
        PersonCoordinator pc = getPersonCoordinator();
        try {
            pc.updatePerson(selectedPerson, getSessionBean().getSessionUser(), updateDescription);
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
        return "persons";
    }
    
    public void initiatePersonUpdate(ActionEvent ev){
        
        
    }
    
    public void connectCurrentPersonToProperty(ActionEvent ev){
        PersonIntegrator pi = getPersonIntegrator();
        try {
            pi.connectPersonToProperty(selectedPerson, selectedProperty);
             getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Successfully connected person ID " + selectedPerson.getPersonID() + " to property ID " + selectedProperty.getPropertyID() , ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        ex.getMessage(), ""));
        }
        // clear list so the list itself looks properties up again
        propertyPersonList = null;
    }
    
    public void initiatePersonCreation(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        selectedPerson = pc.getNewPersonSkeleton(getSessionBean().getSessionMuni());
        System.out.println("PersonsBB.initiatePersonCreation : selected person id: " + selectedPerson.getPersonID());
    }
    
    public void loadPersonHistory(ActionEvent ev){
        loadPersonHistory();
    }
    
    public void loadPersonHistory(){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            personList = pc.assemblePersonHistory(getSessionBean().getSessionUser().getMyCredential());
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
    
    public String createNewPerson(){
        PersonCoordinator pc = getPersonCoordinator();
        PersonIntegrator pi = getPersonIntegrator();
        UserIntegrator ui = getUserIntegrator();
        
        System.out.println("PersonsBB.createNewPerson | before insert selected person: " + selectedPerson.getPersonID());
        
        int newPersonID;
        try {
            selectedPerson.setCreatorUserID(getSessionBean().getSessionUser().getUserID());
            newPersonID = pc.addNewPerson(selectedPerson);
            selectedPerson = pi.getPerson(newPersonID);
            getSessionBean().setSessionPerson(selectedPerson);
            getSessionBean().getSessionPersonList().add(selectedPerson);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Person created with ID " + newPersonID + "! This person is now your active one and has been added to your history.'", ""));
        
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Unable to create new Person in the system.'", ""));
        }
        return "persons";
        
    }
    
    public void searchForPersons(ActionEvent event){
        System.out.println("PersonBB.searchForPersons");
        // clear past search results on bean and on the session
        personList = null;
        getSessionBean().setSessionPersonList(null);
        // this will trigger database lookup logic inside
        // getPersonList() when we tell the search result table to clear itself
    }
    
    
    public void searchForPersonsByNameOnly(ActionEvent event){
        System.out.println("PersonBB.searchForPersonsByNameOnly");
        PersonCoordinator pc = getPersonCoordinator();
        searchParams = pc.getDefaultSearchParamsPersons(getSessionBean().getSessionMuni());
        // clear past search results on bean and on the session
        personList = null;
        getSessionBean().setSessionPersonList(null);
        // this will trigger database lookup logic inside
        // getPersonList() when we tell the search result table to clear itself
    }
    
    
    public void selectPerson(Person p){
        UserIntegrator ui = getUserIntegrator();
        PropertyIntegrator pi = getPropertyIntegrator();
        SystemIntegrator si = getSystemIntegrator();
        try {
            si.logObjectView_OverwriteDate(getSessionBean().getSessionUser(), p);
            propertyPersonList = pi.getProperties(p);
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Person history logging is broken!",""));
        }
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
        List<Person> sessionPersonList = getSessionBean().getSessionPersonList();

        // first check if our view-scoped list is emtpy, if so, we need a list!
        if (personList == null) {
            System.out.println("PersonBB.getPersonList | found Null person List");
            if (sessionPersonList != null) { // if we've got a session list, use that before going to DB
                personList = sessionPersonList;
                System.out.println("PersonBB.getPersonList | loaded list from session");
            } else {
                try {
                    personList = integrator.getPersonList(searchParams); // go to Integrator with searchParams
                    getSessionBean().setSessionPersonList(personList);
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
            selectedPerson = getSessionBean().getSessionPerson();
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
    public SearchParamsPerson getSearchParams() {
        return searchParams;
    }

    /**
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsPerson searchParams) {
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

    /**
     * @return the muniNameIDMap
     */
    public Map<String, Integer> getMuniNameIDMap() {
        return muniNameIDMap;
    }

    /**
     * @param muniNameIDMap the muniNameIDMap to set
     */
    public void setMuniNameIDMap(Map<String, Integer> muniNameIDMap) {
        this.muniNameIDMap = muniNameIDMap;
    }

    /**
     * @return the propertyCandidateList
     */
    public List<Property> getPropertyCandidateList() {
        return propertyCandidateList;
    }

    /**
     * @param propertyCandidateList the propertyCandidateList to set
     */
    public void setPropertyCandidateList(List<Property> propertyCandidateList) {
        this.propertyCandidateList = propertyCandidateList;
    }

    /**
     * @return the selectedProperty
     */
    public Property getSelectedProperty() {
        return selectedProperty;
    }

    /**
     * @param selectedProperty the selectedProperty to set
     */
    public void setSelectedProperty(Property selectedProperty) {
        this.selectedProperty = selectedProperty;
    }

    /**
     * @return the propertyPersonList
     */
    public List<Property> getPropertyPersonList() {
        PropertyIntegrator pi = getPropertyIntegrator();
        if(propertyPersonList == null){
            try {
                propertyPersonList = pi.getProperties(selectedPerson);
            } catch (IntegrationException ex) {
                System.out.println(ex);
            }
        }
        return propertyPersonList;
    }
    

    /**
     * @param propertyPersonList the propertyPersonList to set
     */
    public void setPropertyPersonList(List<Property> propertyPersonList) {
        this.propertyPersonList = propertyPersonList;
    }
    
}
