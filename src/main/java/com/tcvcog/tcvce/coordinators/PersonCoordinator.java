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
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.Credential;

import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonChangeOrder;
import com.tcvcog.tcvce.entities.PersonDataHeavy;
import com.tcvcog.tcvce.entities.PersonOccApplication;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.PersonWithChanges;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.QueryCECaseEnum;
import com.tcvcog.tcvce.entities.search.QueryEvent;
import com.tcvcog.tcvce.entities.search.QueryEventEnum;
import com.tcvcog.tcvce.entities.search.QueryOccPeriod;
import com.tcvcog.tcvce.entities.search.QueryOccPeriodEnum;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import com.tcvcog.tcvce.entities.search.QueryPropertyEnum;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ellen bascomb of apt 31y
 */
public class PersonCoordinator extends BackingBeanUtils implements Serializable{

    private final PersonType[] personTypes;
    
    
    /**
     * Creates a new instance of PersonCoordinator
     */
    public PersonCoordinator() {
        personTypes = PersonType.values();
    }
    
    /**
     * Logic intermediary for receiving requests for a Person 
     * @param personID
     * @return
     * @throws IntegrationException 
     */
    public Person getPerson(int personID) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        
        if(personID == 0){
            return null;
        }
        
        return configurePerson(pi.getPerson(personID));
    }
    
    /**
     * Utility method for building a list of Persons given a list of IDs
     * @param pidList
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<Person> getPersonList(List<Integer> pidList) throws IntegrationException{
        List<Person> pList = new ArrayList<>();
        if(pidList != null && !pidList.isEmpty()){
            for(Integer i: pidList){
                pList.add(getPerson(i));
            }
        }
        return pList;
    }
    
    
    public PersonDataHeavy assemblePersonDataHeavy(Person pers, Credential cred){
        PersonDataHeavy pdh = new PersonDataHeavy(pers, cred);
        SearchCoordinator sc = getSearchCoordinator();
        
        try {
            QueryCECase qcse = sc.initQuery(QueryCECaseEnum.PERSINFOCASES, cred);
            qcse.getPrimaryParams().setPersonInfoCaseID_val(pers);
            pdh.setCaseList(sc.runQuery(qcse).getResults());
        
            QueryOccPeriod qop = sc.initQuery(QueryOccPeriodEnum.PERSONS, cred);
            qop.getPrimaryParams().setPerson_val(pers);
            pdh.setPeriodList(sc.runQuery(qop).getBOBResultList());
            
            QueryProperty qprop = sc.initQuery(QueryPropertyEnum.PERSONS, cred);
            qprop.getPrimaryParams().setPerson_val(pers);
            pdh.setPropertyList(sc.runQuery(qprop).getBOBResultList());
            
            QueryEvent qe = sc.initQuery(QueryEventEnum.PERSONS, cred);
            qe.getPrimaryParams().setPerson_val(pers);
            pdh.setEventList(sc.runQuery(qe).getBOBResultList());
        
        } catch (SearchException ex) {
            System.out.println(ex);
        }
        
        return pdh;
    }
    
    /**
     * Logic container for Person object assembly
     * @param p
     * @return 
     */
    private Person configurePerson(Person p){
        // check stuff, build stuff
        return p;
        
    }
   
    
    
    /**
     * Logic intermediary for Updates to the Person listing
     * @param p
     * @param u
     * @throws IntegrationException 
     */
    public void personEdit(Person p, User u) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        pi.updatePerson(p);
    }
    
    public Person personCreateMakeSkeleton(Municipality m){
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
    
    public int personCreate(Person p, UserAuthorized ua) throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
       
        p.setCreatorUserID(ua.getUserID());
        p.setCreationTimeStamp(LocalDateTime.now());
        p.setSource( si.getBOBSource(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                                .getString("bobsourcePersonInternal"))));
        return pi.insertPerson(p);
        
        
    }
    
    
    /**
     * Prepares note text from the backing bean and sends Person all ready to
     * be updated by the Integrator
     * 
     * @param p to which the note should be attached
     * @param u doing the attaching
     * @param noteToAdd new note text
     * @throws IntegrationException 
     */
    public void addNotesToPerson(Person p, UserAuthorized u, String noteToAdd) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        SystemCoordinator sc = getSystemCoordinator();

        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("personRecordNotesGeneral"));
        mbp.setExistingContent(p.getNotes());
        mbp.setNewMessageContent(noteToAdd);
        mbp.setUser(u);
        mbp.setCred(u.getMyCredential());
        p.setNotes(sc.appendNoteBlock(mbp));
        pi.updatePerson(p);
        
    }
    
    /**
     * Logic holder for pass-through calls to object connection methods on the Integrator
     * @param person
     * @param prop
     * @throws IntegrationException 
     */
    public void connectPersonToProperty(Person person, Property prop) throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        pi.connectPersonToProperty(person, prop);
        
    }
    
    
    /**
     * Logic holder for pass-through calls to object connection methods on the Integrator
     * @param person
     * @param prop
     * @throws IntegrationException 
     */
    public void connectRemovePersonToProperty(Person person, Property prop) throws IntegrationException, BObStatusException {
        if(person == null || prop == null){
            throw new BObStatusException("Cannot remove person link if either Person or Property is NULL");
        }
        PersonIntegrator pi = getPersonIntegrator();
        pi.connectRemovePersonToProperty(person, prop);
        
    }
    
    /**
     * Logic holder for pass-through calls to object connection methods on the Integrator
     * @param cit
     * @param pers
     * @throws IntegrationException 
     */
    public void connectPersonToCitation(Citation cit, Person pers) throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        pi.connectPersonToCitation(cit, pers);
        
    }
    
    /**
     * Logic holder for pass-through calls to object connection methods on the Integrator
     * @param cit
     * @param persList
     * @throws IntegrationException 
     */
    public void connectPersonsToCitation(Citation cit, List<Person> persList) throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        pi.connectPersonsToCitation(cit, persList);
        
    }
    
    /**
     * Logic holder for pass-through calls to object connection methods on the Integrator
     * @param munui
     * @param p
     * @throws IntegrationException 
     */
    public void connectPersonToMunicipality(Municipality munui, Person p) throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        pi.connectPersonToMunicipality(munui, p);
            
    }
    
    /**
     * Logic holder for pass-through calls to object connection methods on the Integrator
     * @param munuiList
     * @param p
     * @throws IntegrationException 
     */
    public void connectPersonToMunicipalities(List<Municipality> munuiList, Person p) throws IntegrationException {
        PersonIntegrator pi = getPersonIntegrator();
        pi.connectPersonToMunicipalities(munuiList, p);
        
    }
    
    /**
     * Logic container method for creating a Ghost from a given Person--which
     * is a copy of a person at a given time to be used in recreating official documents
     * with addresses and such, like Citations, NOVs, etc.
     * @param p of which you would like to create a Ghost
     * @param u doing the connecting
     * @return the database identifier of the sent in Person's very own ghost
     * @throws IntegrationException 
     */
    public int createChostPerson(Person p, User u) throws IntegrationException, BObStatusException{
        PersonIntegrator pi = getPersonIntegrator();
        int newGhostID = pi.createGhost(p, u);
        return newGhostID;        
        
    }
    
    /**
     * Logic container for creating a clone of a person, which is an exact copy of 
     * a Person at a given point in time that is used to safely edit person info 
     * without allowing certain levels of users access to the primary Person record
     * from which there is no recovery of core info
     * 
     * Ghosts are friendly and invited; clones are an unedesirable manifestation
     * of the modern biotechnological era
     * 
     * @param p of which you would like to create a clone
     * @param u doing the creating of clone
     * @return the database identifer of the inputted Person's clone
     * @throws IntegrationException 
     */
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
        return uc.user_getUser(cred.getGoverningAuthPeriod().getUserID()).getPerson();
        
    }

    
    /**
     * Intermediary logic unit for configuring histories of Person object views
 given an authorization context
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
     *
     * @param idNumList A list of person IDs from the database.
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<Person> assemblePersonList(List<Integer> idNumList) throws IntegrationException{
        
        ArrayList<Person> skeletonHorde = new ArrayList<>();
        PersonIntegrator pi = getPersonIntegrator();
        
        for (int idNum : idNumList){
            skeletonHorde.add(pi.getPerson(idNum));
        }
        return skeletonHorde;
    }

    /**
     * Utility method for dumping PersonType values in an Enum to an array
     * @return the personTypes
     */
    public PersonType[] getPersonTypes() {
        
        return personTypes;
    }

   /**
    * Logic and permissions check for main delete person method on integrator
    * @param p
    * @param cred
    * @throws IntegrationException
    * @throws AuthorizationException must be sys admin or higher
    */
    public void personNuke(Person p, Credential cred) throws IntegrationException, AuthorizationException{
        PersonIntegrator pi = getPersonIntegrator();
        if(cred.isHasSysAdminPermissions()){
            pi.deletePerson(p);
        } else {
            throw new AuthorizationException("Must have sys admin permissions or higher to delete");
        }
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
    
    /**
     * Anonymizes Person object member variables, for use in the case of a public search. The 
     * anonymized fields should still be recognizable if one knows what they are likely to be, but
     * someone without that knowledge should not be able to guess the address, phone number, email etc.
     * @param person
     * @return 
     */
    public PersonOccApplication anonymizePersonData(PersonOccApplication person){
        
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
    
    public String dumpPerson(Person p){
        SystemCoordinator sc = getSystemCoordinator();
        StringBuilder sb = new StringBuilder();
        
        sb.append(Constants.FMT_HTML_BREAK);
        sb.append(Constants.FMT_HTML_BREAK);
        sb.append(Constants.FMT_NOTE_SEP_INTERNAL);
        sb.append("Field dump of Person ID: ");
        sb.append(p.getPersonID());
        sb.append(Constants.FMT_HTML_BREAK);
        sb.append("Timestamp: ");
        sb.append(sc.stampCurrentTimeForNote());
        sb.append(Constants.FMT_HTML_BREAK);
        
        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getPersonType().getLabel());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getMuniCode());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getMuniName());
        sb.append(Constants.FMT_HTML_BREAK);

        if(p.getSource() != null){
            sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
            sb.append(p.getSource().getTitle());
            sb.append(Constants.FMT_HTML_BREAK);
        }

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getCreatorUserID());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getCreationTimeStamp());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getFirstName());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getLastName());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.isCompositeLastName());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.isBusinessEntity());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getJobTitle());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getPhoneCell());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getPhoneHome());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getPhoneWork());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getEmail());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getAddressStreet());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getAddressCity());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getAddressZip());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getAddressState());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.isUseSeparateMailingAddress());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getMailingAddressStreet());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getMailingAddressThirdLine());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getMailingAddressCity());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getMailingAddressZip());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getMailingAddressState());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getNotes());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getLastUpdated());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getLastUpdatedPretty());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.isCanExpire());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getExpiryDate());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getExpireString());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getExpiryNotes());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.isActive());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getLinkedUserID());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.isUnder18());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getVerifiedByUserID());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.isReferencePerson());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getGhostCreatedDate());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getGhostCreatedDatePretty());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getGhostOf());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getGhostCreatedByUserID());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getCloneCreatedDate());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getCloneCreatedDatePretty());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getCloneOf());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getCloneCreatedByUserID());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getGhostsList());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getCloneList());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getMergedList());
        sb.append(Constants.FMT_HTML_BREAK);
        
        return sb.toString();
    }
    
    public PersonWithChanges getPersonWithChanges(int personID) throws IntegrationException{
        
        PersonWithChanges skeleton = new PersonWithChanges(getPerson(personID));
        
        PersonIntegrator pi = getPersonIntegrator();
        
        skeleton.setChangeOrderList(pi.getPersonChangeOrderListAll(personID));
        
        return skeleton;
        
    }
    
    public List<PersonWithChanges> getPersonWithChangesList(List<Person> personList) throws IntegrationException{
        
        List<PersonWithChanges> skeletonHorde = new ArrayList<>();
        
        for(Person input : personList){
            
            skeletonHorde.add(getPersonWithChanges(input.getPersonID()));
            
        }
        
        return skeletonHorde;
        
    }
    
    public List<PersonWithChanges> getPersonWithChangesListUsingID(List<Integer> personIDList) throws IntegrationException{
        
        List<PersonWithChanges> skeletonHorde = new ArrayList<>();
        
        for(Integer input : personIDList){
            
            skeletonHorde.add(getPersonWithChanges(input));
            
        }
        
        return skeletonHorde;
        
    }
    
    public void implementPersonChangeOrder(PersonChangeOrder order) throws IntegrationException {

        PersonIntegrator pi = getPersonIntegrator();

        //If the user added the person, their changes will already be in the database. No need to update
        if (!order.isAdded()) {
            Person skeleton = getPerson(order.getPersonID());
            if (order.isRemoved()) {
                skeleton.setActive(false); //just deactivate the person.
            } else {
                if (order.getFirstName() != null) {
                    skeleton.setFirstName(order.getFirstName());
                }

                if (order.getLastName() != null) {
                    skeleton.setLastName(order.getLastName());
                }

                if (order.getCompositeLastName() != null) {
                    skeleton.setCompositeLastName(order.isCompositeLastName());
                }

                if (order.getPhoneCell() != null) {
                    skeleton.setPhoneCell(order.getPhoneCell());
                }

                if (order.getPhoneHome() != null) {
                    skeleton.setPhoneHome(order.getPhoneHome());
                }

                if (order.getPhoneWork() != null) {
                    skeleton.setPhoneWork(order.getPhoneWork());
                }

                if (order.getEmail() != null) {
                    skeleton.setEmail(order.getEmail());
                }

                if (order.getAddressStreet() != null) {
                    skeleton.setAddressStreet(order.getAddressStreet());
                }

                if (order.getAddressCity() != null) {
                    skeleton.setAddressCity(order.getAddressCity());
                }

                if (order.getAddressState() != null) {
                    skeleton.setAddressState(order.getAddressState());
                }

                if (order.getAddressZip() != null) {
                    skeleton.setAddressZip(order.getAddressZip());
                }

                if (order.getUseSeparateMailingAddress() != null) {
                    skeleton.setUseSeparateMailingAddress(order.isUseSeparateMailingAddress());
                }

                if (order.getMailingAddressStreet() != null) {
                    skeleton.setMailingAddressStreet(order.getMailingAddressStreet());
                }

                if (order.getMailingAddressThirdLine() != null) {
                    skeleton.setMailingAddressThirdLine(order.getMailingAddressThirdLine());
                }

                if (order.getMailingAddressCity() != null) {
                    skeleton.setMailingAddressCity(order.getMailingAddressCity());
                }

                if (order.getMailingAddressState() != null) {
                    skeleton.setMailingAddressState(order.getMailingAddressState());
                }

                if (order.getMailingAddressZip() != null) {
                    skeleton.setMailingAddressZip(order.getMailingAddressZip());
                }
            }

            pi.updatePerson(skeleton);

        }

        // update change order now that it's been approved.
        order.setActive(false);
        order.setApprovedOn(Timestamp.valueOf(LocalDateTime.now()));
        order.setApprovedBy(getSessionBean().getSessUser());

        pi.updatePersonChangeOrder(order);

    }
    
}
