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
import com.tcvcog.tcvce.entities.Human;
import com.tcvcog.tcvce.entities.HumanLink;
import com.tcvcog.tcvce.entities.IFace_humanListHolder;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonChangeOrder;
import com.tcvcog.tcvce.entities.PersonDataHeavy;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.PersonWithChanges;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.QueryCECaseEnum;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The master controller class for Humans and their Java incarnation called
 * Person, which is a human with a list of its Addresses, Emails, and Phone numbers
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
     * Primary retrieval method for extracting Humans from the DB
     * Remember a Human has a name, DOB, and stuff like that, but NO
     * Addresses, Emails, or Phone numbers--get a Person instead
     * 
     * @param humanID
     * @return the Human object 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public Human getHuman(int humanID) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        if(humanID == 0){
            return null;
        }
        return configureHuman(pi.getHuman(humanID));
    }
    
    /**
     * Creates a HumanLink from a given Human PRIOR to being
     * written in the database. Skeleton objects are those which
     * have an ID of 0
     * @param hu to turn into a HumanLink
     * @return the given Human wrapped in a Link. Null if input is null.
     */
    public HumanLink getHumanLinkSkeleton(Human hu){
        if(hu != null){
            return new HumanLink(hu);
            
        } else {
            return null;
        }
        
        
    }
    
    
    /**
     * Access point for retrieving and injecting a list of Human objects
     * associated with a Business Object
     * 
     * @param hlh BOB implementing this interface
     * @return The BOB with Humans and their link metatadata already assembled.
     * Note the caller will probably want to cast back to the original type
     */
    public List<HumanLink> assembleLinkedHumanLinks(IFace_humanListHolder hlh) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        List<HumanLink> hll = null;
        
        if(hlh != null){
            hll = pi.getHumanLinks(hlh);
        }
        
        return hll;
    }
    
    
    /**
     * Grand staircase entrance for connecting a human holder to a human
     * @param hlh
     * @param hum
     * @param ua the user doing the linking
     * @return the link ID for the freshly inserted link
     * @throws BObStatusException 
     */
    public int linkHuman(IFace_humanListHolder hlh, HumanLink hum, UserAuthorized ua) throws BObStatusException, IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        
        if(hlh == null || hum == null){
            throw new BObStatusException("Cannot link human with null human or human holder");
        }
        
        hum.setCreatedBy(ua);
        hum.setLinkLastUpdatedBy(ua);
        
        return pi.insertHumanLink(hlh, hum);
    }
    
    
    /**
     * Grand staircase entrance for deactivating a human holder and one of its humans
     * @param hlh the host BOB
     * @param hl to deactivate
     * @param ua the user doing the deactivating
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void deactivateLinkedHuman(IFace_humanListHolder hlh, HumanLink hl, UserAuthorized ua) 
            throws BObStatusException, IntegrationException{
        
        PersonIntegrator pi = getPersonIntegrator();
        
        if(hlh == null || hl == null){
            throw new BObStatusException("Cannot link human with null human or human holder");
        }
        hl.setLastUpdatedBy(ua);
        hl.setDeceasedBy(ua);
        pi.deactivateHumanLink(hlh, hl);
    }
    
    
    /**
     * Logic container for configuring a Human object =
     * @param hum
     * @return 
     */
    private Human configureHuman(Human hum){
        
        
        // config logic for Human's go heres
  
        return hum;
    
    
    }
    
    
    
    
    /**
     * A Person is a Human with lists of MailingAddresses, phone numbers, and emails
     * @param hum
     * @return the fully-baked human (i.e. a person)
     * @throws IntegrationException 
     */
    public Person getPerson(Human hum) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        
        if(hum != null){
            return null;
        }
        Person p = new Person(hum);
        
        
        return configurePerson(p);
    }
    
    /**
     * Utility method for building a list of Persons given a list of IDs
     * @param humanIDList
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<Person> getPersonList(List<Integer> humanIDList) throws IntegrationException{
        List<Person> pList = new ArrayList<>();
        if(humanIDList != null && !humanIDList.isEmpty()){
            for(Integer i: humanIDList){
                pList.add(getPerson(getHuman(i)));
            }
        }
        return pList;
    }
    
    
    /**
     * Utility method for iterating over a list of HumanLinks and getting
     * Persons and shoving them all in a list
     * 
     * @param humanLinkList     
     * @return return a list, possibly with Person objs in it!
     * @throws IntegrationException 
     */
    public List<Person> getPersonListFromLinkList(List<HumanLink> humanLinkList) throws IntegrationException{
        List<Person> pl = new ArrayList<>();
        for(HumanLink hl: humanLinkList){
            pl.add(getPerson(hl));
        }
        return pl;
        
    }
    
    /**
     * Builds a data heavy version of a person
     * Implements logic depending on if the person is a skeleton or not
     * @param pers
     * @param cred
     * @return
     * @throws IntegrationException 
     */
    public PersonDataHeavy assemblePersonDataHeavy(Person pers, Credential cred) throws IntegrationException{
        PersonDataHeavy pdh = null;
        if(pers != null && cred != null){
            // if we have a skeleton person, don't try to get a person from the DB, since there's no ID
            if(pers.getHumanID() == 0){
                pdh = new PersonDataHeavy(pers, cred);
                
            } else {

               pdh = new PersonDataHeavy(getPerson(pers), cred);
               SearchCoordinator sc = getSearchCoordinator();

               try {
                   QueryCECase qcse = sc.initQuery(QueryCECaseEnum.PERSINFOCASES, cred);
                   qcse.getPrimaryParams().setPersonInfoCaseID_val(pers);
                   pdh.setCaseList(sc.runQuery(qcse).getResults());

       //            QueryOccPeriod qop = sc.initQuery(QueryOccPeriodEnum.PERSONS, cred);
       //            qop.getPrimaryParams().setPerson_val(pers);
       //            pdh.setPeriodList(sc.runQuery(qop).getBOBResultList());

       //            TURNED OFF TO MIGRATE TO HUMANIZATION
       //            QueryProperty qprop = sc.initQuery(QueryPropertyEnum.PERSONS, cred);
       //            qprop.getPrimaryParams().setPerson_val(pers);
       //            pdh.setPropertyList(sc.runQuery(qprop).getBOBResultList());

       //            QueryEvent qe = sc.initQuery(QueryEventEnum.PERSONS, cred);
       //            qe.getPrimaryParams().setPerson_val(pers);
       //            pdh.setEventList(sc.runQuery(qe).getBOBResultList());

               } catch (SearchException ex) {
                   System.out.println(ex);
               }
            }
        }
        
        return pdh;
    }
    
    /**
     * Logic container for Person object assembly
     * from an underlying human record
     * 
     * @param p
     * @return the configured person
     */
    private Person configurePerson(Person p) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        PropertyIntegrator propi = getPropertyIntegrator();
        
        p.setAddressList(propi.getMailingAddressListByHuman(p.getHumanID()));
        p.setPhoneList(pi.getContactPhoneList(p.getHumanID()));
        p.setEmailList(pi.getContactEmailList(p.getHumanID()));
        
        return p;
        
    }
   
    
    
    /**
     * Logic intermediary for Updates to the Person listing
     * @param h
     * @param u
     * @throws IntegrationException 
     */
    public void humanEdit(Human h, User u) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        pi.updateHuman(h);
    }
    
    public Human humanInit(){
        Human h = new Human();
        return h;
    }
    
    /**
     * Creates skeleton or starter person for new person creation
     * TODO: Finish my guts
     * 
     * @param m
     * @return 
     */
    public Person personInit(Municipality m){
        Person newP = new Person(humanInit()); 
        newP.setBusinessEntity(false);
        return newP;
        
    }
    
    /**
     * Checks logic of incoming person objects and passes write off to Integrator
     * @param h
     * @param ua
     * @return
     * @throws IntegrationException 
     */
    public int humanAdd(Human h, UserAuthorized ua) throws IntegrationException{
        SystemIntegrator si = getSystemIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
       
        h.setCreatedBy(ua);
        h.setSource( si.getBOBSource(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                                .getString("bobsourcePersonInternal"))));
        return pi.insertHuman(h);
        
    }
    
    
    /**
     * Prepares note text from the backing bean and sends Person all ready to
     * be updated by the Integrator
     * 
     * @param p to which the note should be attached
     * @param u doing the attaching
     * @param noteToAdd new note text
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void addNotesToPerson(Person p, UserAuthorized u, String noteToAdd) throws IntegrationException, BObStatusException{
        PersonIntegrator pi = getPersonIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        if(p != null && u != null && noteToAdd != null){

            MessageBuilderParams mbp = new MessageBuilderParams();
            mbp.setHeader(getResourceBundle(Constants.MESSAGE_TEXT).getString("personRecordNotesGeneral"));
            mbp.setExistingContent(p.getNotes());
            mbp.setNewMessageContent(noteToAdd);
            mbp.setUser(u);
            mbp.setCred(u.getMyCredential());
            p.setNotes(sc.appendNoteBlock(mbp));
            pi.updatePersonNotes(p);
            System.out.println("PersonCoordinator.addNotesToPerson: person: " + p.getHumanID() + " notes: " + noteToAdd);
        } else {
            throw new BObStatusException("cannot append note given a null person, user, or note string");
            
        }
        
    }
    
   
    
    
    /**
     * Logic container method for creating a Ghost from a given Person--which
     * is a copy of a person at a given time to be used in recreating official documents
     * with addresses and such, like Citations, NOVs, etc.
     * @param p of which you would like to create a Ghost
     * @param u doing the connecting
     * @return the database identifier of the sent in Person's very own ghost
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
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
 * 
 *  TODO: Update for humanization
 * 
     * @param cred
     * @return
     * @throws IntegrationException 
     */
    public List<Human> assembleHumanHistory(Credential cred) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        List<Human> pl = new ArrayList<>();
        List<Integer> idList = null;
        if(cred != null){
            idList = pi.getPersonHistory(cred.getGoverningAuthPeriod().getUserID());
            while(!idList.isEmpty() && pl.size() <= Constants.MAX_BOB_HISTORY_SIZE){
//                pl.add(pi.getPerson(idList.remove(0)));
            }
        }
        return pl;
    }   
    
    /**
     *
     * @param hidl A list of person IDs from the database.
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<Human> assembleHumanList(List<Integer> hidl) throws IntegrationException{
        
        List<Human> skeletonHorde = new ArrayList<>();
        PersonIntegrator pi = getPersonIntegrator();
        
        for (int idNum : hidl){
            skeletonHorde.add(pi.getHuman(idNum));
        }
        return skeletonHorde;
    }
    
    /**
     * Utility method for getting a list of Person objects from a list of
     * Human objs
     * @param hl
     * @return a list, possibly not null, of Person objs
     * @throws IntegrationException 
     */
    public List<Person> assemblePersonListFromHumanList(List<Human> hl) throws IntegrationException{
        List<Person> pl = new ArrayList<>();
        if(hl != null && !hl.isEmpty()){
            for(Human h: hl){
                pl.add(getPerson(h));
            }
        }
        return pl;
        
    }

    /**
     * Utility method for dumping PersonType values in an Enum to an array
     * @return the personTypes
     */
    public PersonType[] getPersonTypes() {
        
        return personTypes;
    }

    
    /**
     * TODO: Finsih for humanization
     * @param personList
     * @return 
     */
    public List<Person> anonymizePersonList(List<Person> personList) {
        for (Person person:personList){
//            anonymizePersonData(person);
        }
        return personList; 
    }
    

    /**
     * Anonymizes Person object member variables, for use in the case of a public search. The 
     * anonymized fields should still be recognizable if one knows what they are likely to be, but
     * someone without that knowledge should not be able to guess the address, phone number, email etc.
     *
     * TODO: Upgrade for Humanization
     * 
     * 
     * @param person
     * @return 
     */
    public Person anonymizePersonData(Person person){
        
        
        // anonymize all but first two characters of last name
        if(person.getLastName() != null) {
            StringBuilder last = new StringBuilder(person.getLastName());
            for (int i = 2; i < last.length() && i >= 0; i++){
                last.setCharAt(i, '*');
            }
            person.setName(last.toString());
        }
        
        // anonymize all but first three characters and the domain of an email address
        if (person.getEmail() != null) {
            StringBuilder email = new StringBuilder(person.getEmail());
            for (int i = 3; i < email.length() &&  email.charAt(i) != '@' && i >= 0; i++){
                email.setCharAt(i, '*');
            }
//            person.setEmail(email.toString());
        }
        
        // anonymize a
        return person;
    }
    
 
    /**
     * Attempt at archiving state of a pre-human person
     * to track changes to field names; come up with new approach
     * Post humanization
     * 
     * @deprecated 
     * @param p
     * @return 
     */
    public String dumpPerson(Person p){
        SystemCoordinator sc = getSystemCoordinator();
        StringBuilder sb = new StringBuilder();
        
        sb.append(Constants.FMT_HTML_BREAK);
        sb.append(Constants.FMT_HTML_BREAK);
        sb.append(Constants.FMT_NOTE_SEP_INTERNAL);
        sb.append("Field dump of Person ID: ");
        sb.append(p.getHumanID());
        sb.append(Constants.FMT_HTML_BREAK);
        sb.append("Timestamp: ");
        sb.append(sc.stampCurrentTimeForNote());
        sb.append(Constants.FMT_HTML_BREAK);
        
        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getPersonType().getLabel());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getMuniCode());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getMuniName());
        sb.append(Constants.FMT_HTML_BREAK);

        if(p.getSource() != null){
            sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
            sb.append(p.getSource().getTitle());
            sb.append(Constants.FMT_HTML_BREAK);
        }

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getCreatorUserID());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getCreationTimeStamp());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getFirstName());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getLastName());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.isCompositeLastName());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.isBusinessEntity());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getJobTitle());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getPhoneCell());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getPhoneHome());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getPhoneWork());
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
//        sb.append(p.isUseSeparateMailingAddress());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getMailingAddressStreet());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getMailingAddressThirdLine());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getMailingAddressCity());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getMailingAddressZip());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getMailingAddressState());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.getNotes());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getLastUpdated());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getLastUpdatedPretty());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.isCanExpire());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getExpiryDate());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getExpireString());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getExpiryNotes());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.isActive());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getLinkedUserID());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
        sb.append(p.isUnder18());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getVerifiedByUserID());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.isReferencePerson());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getGhostCreatedDate());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getGhostCreatedDatePretty());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getGhostOf());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getGhostCreatedByUserID());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getCloneCreatedDate());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getCloneCreatedDatePretty());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getCloneOf());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getCloneCreatedByUserID());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getGhostsList());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getCloneList());
        sb.append(Constants.FMT_HTML_BREAK);

        sb.append(Constants.FMT_FIELDKVSEP_WSPACE);
//        sb.append(p.getMergedList());
        sb.append(Constants.FMT_HTML_BREAK);
        
        return sb.toString();
    }
    
    /**
     * TODO Upgrade for humanization
     * @param personID
     * @return
     * @throws IntegrationException 
     */
    public PersonWithChanges getPersonWithChanges(int personID) throws IntegrationException{
        
//        PersonWithChanges skeleton = new PersonWithChanges(getPerson(personID));
        
        PersonIntegrator pi = getPersonIntegrator();
        
//        skeleton.setChangeOrderList(pi.getPersonChangeOrderListAll(personID));
        
        return null;
//        return skeleton;
        
    }
    
    public List<PersonWithChanges> getPersonWithChangesList(List<Person> personList) throws IntegrationException{
        
        List<PersonWithChanges> skeletonHorde = new ArrayList<>();
        
        for(Person input : personList){
            
            skeletonHorde.add(getPersonWithChanges(input.getHumanID()));
            
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
}
