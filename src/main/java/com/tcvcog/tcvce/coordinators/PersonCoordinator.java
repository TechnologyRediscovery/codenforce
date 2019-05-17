/*
 * Copyright (C) 2017 Turtle Creek Valley
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
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParamsPersons;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Eric C. Darsow
 */
public class PersonCoordinator extends BackingBeanUtils implements Serializable{

    private PersonType[] personTypes;
    
    
    /**
     * Creates a new instance of PersonCoordinator
     */
    public PersonCoordinator() {
    }
    
    public int addNewPerson(Person p) throws IntegrationException{
        int newid;
        PersonIntegrator pi = getPersonIntegrator();
        newid = pi.insertPerson(p);
        return newid;
    }
    
    
    public SearchParamsPersons getDefaultSearchParamsPersons(Municipality m){
        SearchParamsPersons spp = new SearchParamsPersons();
        // on the parent class SearchParams
        spp.setFilterByStartEndDate(false);
        spp.setLimitResultCountTo100(true);
        spp.setMuni(m);
        
        // on the subclass SearchParamsPersons
        spp.setFilterByFirstName(true);
        spp.setFilterByLastName(true);
        spp.setOnlySearchCompositeLastNames(false);
        
        spp.setFilterByPersonTypes(false);
        spp.setFilterByEmail(false);
        spp.setFilterByAddressStreet(false);
        
        spp.setFilterByActiveSwitch(false);
        spp.setFilterByVerifiedSwitch(false);
        
        
        return spp;
        
    }
    
    
    public void updatePerson(Person p, User u, String updateNotes) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        StringBuilder sb = new StringBuilder();
        // create the new note header
        sb.append(getResourceBundle(Constants.MESSAGE_TEXT).getString("personRecordUpdateHeader"));
        sb.append("<br />");
        sb.append(updateNotes);
        p.setNotes(appendNoteBlock(p.getNotes(), sb.toString()));
        pi.updatePerson(p);
        pi.updatePersonNotes(p);
        
    }
    
    public Person getNewPersonSkeleton(Municipality m){
        Person newP = new Person();
        newP.setPersonType(PersonType.Public);
        newP.setActive(true);
        newP.setPersonID(0);
        newP.setCanExpire(false);
        newP.setBusinessEntity(false);
        newP.setCompositeLastName(false);
        newP.setUseSeparateMailingAddress(false);
        newP.setMuniCode(m.getMuniCode());
        newP.setAddressState("PA");
        return newP;
        
    }
    
    public void addNotesToPerson(Person p, User u, String noteToAdd) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        StringBuilder sb = new StringBuilder();
        // create the general note header
        sb.append(getResourceBundle(Constants.MESSAGE_TEXT).getString("personRecordNotesGeneral"));
        sb.append("<br />");
        sb.append(noteToAdd);
        p.setNotes(appendNoteBlock(p.getNotes(), sb.toString()));
        pi.updatePerson(p);
        
    }
    
    private String appendNoteBlock(String previousNotes, String newNotes){
        StringBuilder sb = new StringBuilder();
        sb.append(previousNotes);
        sb.append("<br/>**************************************<br/>");
        sb.append("NOTE CREATED BY: ");
        sb.append(getSessionBean().getFacesUser().getPerson().getFirstName());
        sb.append(" ");
        sb.append(getSessionBean().getFacesUser().getPerson().getLastName());
        sb.append(" (User ID ");
        sb.append(String.valueOf(getSessionBean().getFacesUser().getUserID()));
        sb.append(") on ");
        sb.append(getPrettyDate(LocalDateTime.now()));
        sb.append(":<br/>");
        sb.append(newNotes);
        sb.append("<br/>");
        sb.append("**************************************<br/>");
        return sb.toString();
        
    }
    
    public int createChostPerson(Person p, User u) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        int newGhostID = pi.createGhost(p, u);
        return newGhostID;        
        
    }
    
    public int createClonedPerson(Person p, User u) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        int newCloneID = pi.createClone(p, u);
        return newCloneID;
    }
    
    public List<Person> loadPersonHistoryList(User u) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        return pi.getPersonHistory(u);
        
    }
    
    

    /**
     * @return the personTypes
     */
    public PersonType[] getPersonTypes() {
        personTypes = PersonType.values();
        return personTypes;
    }

    /**
     * @param personTypes the personTypes to set
     */
    public void setPersonTypes(PersonType[] personTypes) {
        this.personTypes = personTypes;
    }
    /**
     * To be used in public search, no muni required
     * @return 
     */
    public SearchParamsPersons  getDefaultSearchParamsPersons(){
        SearchParamsPersons params = new SearchParamsPersons();
        
        // superclass parameters
        params.setFilterByMuni(false);
        params.setFilterByObjectID(false);
        params.setFilterByStartEndDate(false);
        params.setLimitResultCountTo100(true);
        
        // subclass specific parameters
        params.setFilterByLastName(true);
        params.setFilterByAddressStreet(true);
        
        params.setFilterByFirstName(false);
        params.setFilterByPhoneNumber(false);
        params.setFilterByEmail(false);        
        params.setFilterByCity(false);
        params.setFilterByZipCode(false);
        
        return params;
    }
    
    public List<Person> queryPersons(SearchParamsPersons params) throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        List<Person> results = pi.queryPersons(params);
        
        return results;
    }
    
    public List<Person> queryPersonsAnonymized(SearchParamsPersons params) throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        List<Person> results = pi.queryPersons(params);
        results = anonymizePersonList(results);
        
        return results;
    }    
    
    public List<Person> anonymizePersonList(List<Person> personList) {
        for (Person person:personList){
            anonymizePersonData(person);
        }
        return personList; 
    }
    
    public Person anonymizePersonData(Person person){
        
        // anonymize first name
        StringBuilder first = new StringBuilder(person.getFirstName());
        for (int i = 2; i < first.length(); i++){
            first.setCharAt(i, '*');
        }
        person.setFirstName(first.toString());
        
        // anonymize last name
        StringBuilder last = new StringBuilder(person.getLastName());
        for (int i = 2; i < last.length(); i++){
            last.setCharAt(i, '*');
        }
        person.setLastName(last.toString());
        
        // anonymize email
        StringBuilder email = new StringBuilder(person.getEmail());
        for (int i = 3; i < email.length() &&  email.charAt(i) != '@'; i++){
            email.setCharAt(i, '*');
        }
        person.setEmail(email.toString());
        
        // anonymize cell phone number
        StringBuilder cellNumber = new StringBuilder(person.getPhoneCell());
        for (int i = cellNumber.length() - 1; i > cellNumber.length() - 5 ; i--){
            cellNumber.setCharAt(i, '*');
        }
        person.setPhoneCell(cellNumber.toString());
        
        // anonymize work phone number
        StringBuilder workNumber = new StringBuilder(person.getPhoneWork());
        for (int i = workNumber.length() - 1; i > workNumber.length() - 5 ; i--){
            workNumber.setCharAt(i, '*');
        }
        person.setPhoneWork(workNumber.toString());
        
        // anonymize home phone number
        StringBuilder homeNumber = new StringBuilder(person.getPhoneHome());
        for (int i = homeNumber.length() - 1; i > homeNumber.length() - 5 ; i--){
            homeNumber.setCharAt(i, '*');
        }
        person.setPhoneHome(homeNumber.toString());
        
        
        // anonymize address
        StringBuilder address = new StringBuilder(person.getAddressStreet());
        for (int i = 0; i < address.length(); i++){
            address.setCharAt(i, '*');
        }
        person.setAddressStreet(address.toString());
        
        return person;
    }
}
