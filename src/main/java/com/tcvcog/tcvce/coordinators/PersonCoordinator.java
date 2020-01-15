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
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class PersonCoordinator extends BackingBeanUtils implements Serializable{

    private PersonType[] personTypes;
    
    
    /**
     * Creates a new instance of PersonCoordinator
     */
    public PersonCoordinator() {
    }
    
    /**
     * Logic intermediary for receiving requests for a Person 
     * @param personID
     * @return
     * @throws IntegrationException 
     */
    public Person getPerson(int personID) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        return pi.getPerson(personID);
    }
    
    
    /**
     * Entry point for new Person object requests
     * @param p
     * @return
     * @throws IntegrationException 
     */
    public int createPerson(Person p) throws IntegrationException{
        int newid;
        PersonIntegrator pi = getPersonIntegrator();
        newid = pi.insertPerson(p);
        return newid;
    }
    
    
    public SearchParamsPerson getDefaultSearchParamsPersons(Municipality m){
        SearchParamsPerson spp = new SearchParamsPerson();
        // on the parent class SearchParams
        spp.setDate_startEnd_ctl(false);
        spp.setLimitResultCount_ctl(true);
        spp.setMuni_val(m);
        
        // on the subclass SearchParamsPerson
        spp.setName_first_ctl(true);
        spp.setName_last_ctl(true);
        spp.setName_compositeLNameOnly_ctl(false);
        
        spp.setPersonType_ctl(false);
        spp.setEmail_ctl(false);
        spp.setAddress_streetNum_ctl(false);
        
        spp.setVerified_ctl(false);
        spp.setVerified_val(false);
        
        
        return spp;
        
    }
    
    /**
     * Logic intermediary for Updates to the Person listing
     * @param p
     * @param u
     * @param updateNotes
     * @throws IntegrationException 
     */
    public void editPerson(Person p, User u, String updateNotes) throws IntegrationException{
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
    
    public Person initPerson(Municipality m){
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
        sb.append("<br />**************************************<br />");
        sb.append("NOTE CREATED BY: ");
        sb.append(getSessionBean().getSessionUser().getPerson().getFirstName());
        sb.append(" ");
        sb.append(getSessionBean().getSessionUser().getPerson().getLastName());
        sb.append(" (User ID ");
        sb.append(String.valueOf(getSessionBean().getSessionUser().getUserID()));
        sb.append(") on ");
        sb.append(getPrettyDate(LocalDateTime.now()));
        sb.append(":<br />");
        sb.append(newNotes);
        sb.append("<br />");
        sb.append("**************************************<br />");
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
    
    
    /**
     * Logic container for choosing a default person if the SessionInitializer
     * does not have a session List to work from
     * @param cred
     * @return the selected person proposed for becoming the sessionPerson
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public Person selectDefaultPerson(Credential cred) throws IntegrationException{
        UserCoordinator uc = getUserCoordinator();
        return uc.getUser(cred.getGoverningAuthPeriod().getUserID()).getPerson();
        
    }

    
    /**
     * Intermediary logic unit for configuring histories of Person object views
     * given an authorization context
     * @param cred
     * @return
     * @throws IntegrationException 
     */
    public List<Person> assemblePersonHistory(Credential cred) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        List<Person> pl = new ArrayList<>();
        List<Integer> idList = null;
        if(cred != null){
            idList = pi.getPersonHistory(cred.getGoverningAuthPeriod().getUserID());
            while(!idList.isEmpty() && pl.size() <= Constants.MAX_BOB_HISTORY_SIZE){
                pl.add(pi.getPerson(idList.remove(0)));
            }
        }
        return pl;
    }   

    /**
     * @return the personTypes
     */
    public PersonType[] getPersonTypes() {
        personTypes = PersonType.values();
        return personTypes;
    }

   
    /**
     * Returns SearchParamsPerson object with its member variables set to default values.
     * @return params
     */
    public SearchParamsPerson  getDefaultSearchParamsPersons(){
        SearchParamsPerson params = new SearchParamsPerson();
        
        // superclass parameters
        params.setMuni_ctl(false);
        params.setBobID_ctl(false);
        params.setDate_startEnd_ctl(false);
        params.setLimitResultCount_ctl(true);
        
        // subclass specific parameters
        params.setName_last_ctl(true);
        params.setAddress_streetNum_ctl(false);
        
        params.setName_first_ctl(false);
        params.setPhoneNumber_ctl(false);
        params.setEmail_ctl(false);        
        params.setAddress_city_ctl(false);
        params.setAddress_zip_ctl(false);
        
        return params;
    }
   
    
    
    
    public List<Person> anonymizePersonList(List<Person> personList) {
        for (Person person:personList){
            anonymizePersonData(person);
        }
        return personList; 
    }
    

    /**
     * Anonymizes Person object member variables, for use in the case of a public search. The 
     * anonymized fields should still be recognizable if one knows what they are likely to be, but
     * someone without that knowledge should not be able to guess the address, phone number, email etc.
     * @param person
     * @return 
     */
    public Person anonymizePersonData(Person person){
        
        // anonymize all but first two characters of first name
        if(person.getFirstName() != null) {
            StringBuilder first = new StringBuilder(person.getFirstName());
            for (int i = 2; i < first.length() && i >=0; i++){
                first.setCharAt(i, '*');
            }
            person.setFirstName(first.toString());
        }
        
        // anonymize all but first two characters of last name
        if(person.getLastName() != null) {
            StringBuilder last = new StringBuilder(person.getLastName());
            for (int i = 2; i < last.length() && i >= 0; i++){
                last.setCharAt(i, '*');
            }
            person.setLastName(last.toString());
        }
        
        // anonymize all but first three characters and the domain of an email address
        if (person.getEmail() != null) {
            StringBuilder email = new StringBuilder(person.getEmail());
            for (int i = 3; i < email.length() &&  email.charAt(i) != '@' && i >= 0; i++){
                email.setCharAt(i, '*');
            }
            person.setEmail(email.toString());
        }
        
        // anonymize all but last four digits of cell phone number
        if(person.getPhoneCell() != null) {        
            StringBuilder cellNumber = new StringBuilder(person.getPhoneCell());
            for (int i = cellNumber.length() - 5; i >= 0; i--){
                cellNumber.setCharAt(i, '*');
            }
            person.setPhoneCell(cellNumber.toString());
        }

        // anonymize all but last four digits of work phone number
        if(person.getPhoneWork() != null) {
            StringBuilder workNumber = new StringBuilder(person.getPhoneWork());
            for (int i = workNumber.length() - 5; i >= 0; i--){
                workNumber.setCharAt(i, '*');
            }
            person.setPhoneWork(workNumber.toString());
        }     
        
        // anonymize all but last four digits of home phone number
        if(person.getPhoneHome() != null) {
            StringBuilder homeNumber = new StringBuilder(person.getPhoneHome());
            for (int i = homeNumber.length() - 5; i >= 0; i--){
                homeNumber.setCharAt(i, '*');
            }
            person.setPhoneHome(homeNumber.toString());      
        }
           
        // anonymize all but first five characters of address
        if(person.getAddressStreet() != null) {
            StringBuilder address = new StringBuilder(person.getAddressStreet());
            for (int i = 5; i < address.length() && i >= 0; i++){
                address.setCharAt(i, '*');
            }
            person.setAddressStreet(address.toString());
        }
        
        return person;
    }
}
