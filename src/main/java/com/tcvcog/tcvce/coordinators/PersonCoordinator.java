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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CitationDocketRecord;
import com.tcvcog.tcvce.entities.ContactEmail;
import com.tcvcog.tcvce.entities.ContactPhone;
import com.tcvcog.tcvce.entities.ContactPhoneType;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.Human;
import com.tcvcog.tcvce.entities.HumanLink;
import com.tcvcog.tcvce.entities.IFace_addressListHolder;
import com.tcvcog.tcvce.entities.IFace_humanListHolder;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonLinkHeavy;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.PersonWithChanges;
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
import java.util.ArrayList;
import java.util.List;
import com.tcvcog.tcvce.entities.LinkedObjectFamilyEnum;
import com.tcvcog.tcvce.entities.LinkedObjectSchemaEnum;
import com.tcvcog.tcvce.entities.MailingAddress;
import com.tcvcog.tcvce.entities.MailingAddressLink;
import com.tcvcog.tcvce.entities.Parcel;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import java.time.LocalDateTime;
import java.util.Collections;

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
        Human h = pi.getHuman(humanID);
        h = configureHuman(h);
         return h;
    }
    
    
    
    /**
     * Creates a HumanLink from a given Human PRIOR to being
     * written in the database. Skeleton objects are those which
     * have an ID of 0
     * @param hu to turn into a HumanLink
     * @return the given Human wrapped in a Link. Null if input is null.
     */
    public HumanLink createHumanLinkSkeleton(Human hu){
        if(hu != null){
            return new HumanLink(new Person(hu));
            
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
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<HumanLink> getHumanLinkList(IFace_humanListHolder hlh) throws IntegrationException, BObStatusException{
        PersonIntegrator pi = getPersonIntegrator();
        List<HumanLink> hll = null;
        
        if(hlh != null){
            hll = pi.getHumanLinks(hlh);
        }
        
        return hll;
    }
    
    /**
     * retrieves a single human link from the db
     * @param linkID
     * @param lose
     * @return 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
   public HumanLink getHumanLink(int linkID, LinkedObjectSchemaEnum lose) throws BObStatusException, IntegrationException{
       if(linkID == 0 || lose == null){
           throw new BObStatusException("Cannot get link without nonzero ID and non null link enum");
       }
       PersonIntegrator pi = getPersonIntegrator();
       HumanLink hl = pi.getHumanLink(linkID, lose);
       
       return hl;
   }
    
    /**
     * Grand staircase entrance for connecting a human holder to a human
     * @param hlh
     * @param hlink if all you have is a human (or Person subclass), use the 
     * convenience method createHumanLink method
     * @param ua the user doing the linking
     * @return the link ID for the freshly inserted link
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public int insertHumanLink(IFace_humanListHolder hlh, HumanLink hlink, UserAuthorized ua) throws BObStatusException, IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        if(hlh == null || hlink == null || ua == null){
            throw new BObStatusException("Cannot link human with null human or human holder or null user");
        }
        
        hlink.setLinkCreatedByUserID(ua.getUserID());
        hlink.setLinkLastUpdatedByUserID(ua.getUserID());
        if(hlink.getLinkSource() == null){
            hlink.setLinkSource(sc.getBObSource(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                                .getString("bobSourceHumanLinkDefault"))));
        }
        if(hlink.getLinkedObjectRole() == null){
            determineAndSetDefaultLinkedObjectRole(hlh, hlink);
        }

        return pi.insertHumanLink(hlh, hlink);
    }
    
    /**
     * Logic block for human ink updates
     * @param hl
     * @param hlh
     * @param ua 
     */
    public void updateHumanLink(IFace_humanListHolder hlh, HumanLink hl, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(hl == null || ua == null){
            throw new BObStatusException("Cannot update human link with null link or user");
        }
        
        PersonIntegrator pi = getPersonIntegrator();
        hl.setLinkLastUpdatedByUserID(ua.getUserID());
        pi.updateHumanLink(hlh, hl);
        
    }
    
    /**
     * Uses instanceof to check what type of human holder we have and sets the default
     * role code so the DB record is complete.
     * @param hlink
     * @return 
     */
    private HumanLink determineAndSetDefaultLinkedObjectRole(IFace_humanListHolder hlh, HumanLink hlink) throws BObStatusException, IntegrationException{
        if(hlh == null || hlink == null){
            throw new BObStatusException("Cannot determine default linked object role with null list holder or human linke");
        }
        
        SystemIntegrator si = getSystemIntegrator();
        
        if(hlh instanceof OccPermitApplication){
            hlink.setLinkRole(si.getLinkedObjectRole(
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("default_linkedobjectrole_human_occapp"))));
            
        } else if (hlh instanceof CECase){
            hlink.setLinkRole(si.getLinkedObjectRole(
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("default_linkedobjectrole_human_cecase"))));
        } else if (hlh instanceof OccPeriod){
            hlink.setLinkRole(si.getLinkedObjectRole(
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("default_linkedobjectrole_human_occperiod"))));
            
        } else if (hlh instanceof Parcel){
            hlink.setLinkRole(si.getLinkedObjectRole(
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("default_linkedobjectrole_human_parcel"))));
            
        } else if (hlh instanceof PropertyUnit){
            hlink.setLinkRole(si.getLinkedObjectRole(
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("default_linkedobjectrole_human_parcelunit"))));
            
        } else if (hlh instanceof Citation){
            hlink.setLinkRole(si.getLinkedObjectRole(
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("default_linkedobjectrole_human_citation"))));
            
        } else if (hlh instanceof CitationDocketRecord){
            hlink.setLinkRole(si.getLinkedObjectRole(
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("default_linkedobjectrole_human_docket"))));
            
        } else if (hlh instanceof EventCnF){
            hlink.setLinkRole(si.getLinkedObjectRole(
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("default_linkedobjectrole_human_event"))));
            
        } else if (hlh instanceof Municipality){
            hlink.setLinkRole(si.getLinkedObjectRole(
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("default_linkedobjectrole_human_muni"))));
            
        } else if (hlh instanceof MailingAddress){
            hlink.setLinkRole(si.getLinkedObjectRole(
                    Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                            .getString("default_linkedobjectrole_human_mailing"))));
            
        } else {
            throw new BObStatusException("Cannot determine a default linked object role for given list holder");
        }
        
        return hlink;
        
    }
    
    
    /**
     * Grand staircase entrance for deactivating a human holder and one of its humans
     * @param hl to deactivate
     * @param ua the user doing the deactivating
     * @throws BObStatusException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public void deactivateHumanLink(HumanLink hl, UserAuthorized ua) 
            throws BObStatusException, IntegrationException{
        
        PersonIntegrator pi = getPersonIntegrator();
        
        if(ua == null || hl == null){
            throw new BObStatusException("Cannot deactivate human link with null user or link");
        }
        hl.setLinkLastUpdatedByUserID(ua.getUserID());
        hl.setLinkDeactivatedByUserID(ua.getUserID());
        pi.deactivateHumanLink(hl);
    }
    
    /**
     * Takes in a human link and builds the note infrastructure and sends
     * to be integrated
     * @param hl without note appended, just the old notes in the notes field
     * @param noteToAppend the string value of the note to append
     * @param u doing the noting
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void appendNoteToHumanLink(HumanLink hl, String noteToAppend, UserAuthorized u) 
            throws BObStatusException, IntegrationException{
        SystemCoordinator sc = getSystemCoordinator();
        PersonIntegrator pi =getPersonIntegrator();
        MessageBuilderParams mbp = new MessageBuilderParams();
        if(hl  != null){
            mbp.setExistingContent(hl.getNotes());
            mbp.setUser(u);
            mbp.setNewMessageContent(noteToAppend);
            mbp.setHeader("LINK NOTE");
            hl.setNotes(sc.appendNoteBlock(mbp));
            pi.updateHumanLinkNotes(hl);
        }
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
     * Access point for getting a person 
 Convenience method for calling getPersonByHumanID that takes 
 a human object
     * @param humanID
     * @return
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    public Person getPersonByHumanID(int humanID) throws IntegrationException, BObStatusException{
        Human h = getHuman(humanID);
        Person p = getPerson(h);
        return p;
        
    }
    
    /**
     * A Person is a Human with lists of MailingAddresses, phone numbers, and emails
     * @param hum
     * @return the fully-baked human (i.e. a person)
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public Person getPerson(Human hum) throws IntegrationException, BObStatusException{
        if(hum == null){
            return null;
        }
        Person p = new Person(hum);
        p = (Person) configurePerson(p);
        return p;
    }
    
    /**
     * Utility method for building a list of Persons given a list of IDs
     * @param humanIDList
     * @return
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<Person> getPersonList(List<Integer> humanIDList) throws IntegrationException, BObStatusException{
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
    public List<Person> getPersonListFromHumanLinkList(List<HumanLink> humanLinkList) throws IntegrationException, BObStatusException{
        List<Person> pl = new ArrayList<>();
        if(humanLinkList != null && !humanLinkList.isEmpty()){
            for(HumanLink hl: humanLinkList){
                pl.add(getPerson(hl));
            }
        }
        return pl;
        
    }
    
  
    /**
     * Builds a data heavy version of a person
     * Implements logic depending on if the person is a skeleton or not
     * @param pers
     * @return the fully baked person with a link list in its belly
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public PersonLinkHeavy assemblePersonLinkHeavy(Person pers) throws IntegrationException, BObStatusException{
        PersonLinkHeavy persLinkHeavy = null;
        PersonIntegrator pi = getPersonIntegrator();
        SystemCoordinator sc = getSystemCoordinator();
        if(pers == null){
            throw new BObStatusException("Cannot assemble person link heavy with null input person");
            
        }
        persLinkHeavy = new PersonLinkHeavy(pers);
        List<HumanLink> hll = new ArrayList<>();
        List<LinkedObjectSchemaEnum> schemaList = sc.assembleLinkedObjectSchemaEnumListByFamily(LinkedObjectFamilyEnum.HUMAN);
        if(schemaList != null && !schemaList.isEmpty()){
            // iterate over all the enums that describe possible human links
            // and get their linked object IDs, injecting them to the master list
            // for display in the UI
            for(LinkedObjectSchemaEnum lose: schemaList){
                if(lose.isACTIVELINK()){
                    System.out.println("PersonCoordinator.assemblePersonLinkHeavy | Checking enum " + lose.getLinkingTableName());
                    List<HumanLink> hllinternal = pi.getHumanLinksByLinkedObjectEnum(lose, persLinkHeavy);
                    System.out.println("PersonCoordinator.assemblePersonLinkHeavy | Links:  " + hllinternal.size());
                    hll.addAll(hllinternal);
                }
            }
        }       
        persLinkHeavy.setHumanLinkList(hll);
        return persLinkHeavy;
    }
    
    
    /**
     * Logic container for Person object assembly
     * from an underlying human record
     * 
     * @param p
     * @return the configured person
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     */
    public Person configurePerson(Person p) throws IntegrationException, BObStatusException{
        PersonIntegrator pi = getPersonIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();
        
        if(p == null){
            throw new BObStatusException("Cannot configure null person");
        }
        
        
        p.setMailingAddressLinkList(pc.getMailingAddressLinkList(p));
        List<ContactPhone> phl = pi.getContactPhoneList(p.getHumanID());
        if(phl != null && phl.size() >= 2){
            Collections.sort(phl);
        }
        p.setPhoneList(phl);
        List<ContactEmail> eml = pi.getContactEmailList(p.getHumanID());
        if(eml != null && eml.size() >= 2){
            Collections.sort(eml);
        }
        p.setEmailList(eml);
        
        generateMADLinkListPretty(p);
        generatePhoneListPretty(p);
        generateEmailListPretty(p);
        
        return p;
        
    }
    
    /**
     * Internal logic for building an HTML string
     * of the mailing addresses linked to this person
     * @param p 
     */
    private void generateMADLinkListPretty(Person p){
        if(p != null && p.getMailingAddressLinkList() != null && !p.getMailingAddressLinkList().isEmpty()){
            StringBuilder sb = new StringBuilder("");
            for(MailingAddressLink madlink: p.getMailingAddressLinkList()){
                sb.append(madlink.getLinkedObjectRole().getTitle());
                sb.append("<br />");
                sb.append(madlink.getAddressPretty2LineEscapeFalse());
            }
            p.setMailingAddressListPretty(sb.toString());
        }
    }
    
    /**
     * Internal logic for making a nice phone list with
     * breaks and type, etc.
     * @param p 
     */
    private void generatePhoneListPretty(Person p){
        if(p != null && p.getPhoneList() != null && !p.getPhoneList().isEmpty()){
            StringBuilder sb = new StringBuilder("");
            for(ContactPhone ph: p.getPhoneList()){
                sb.append(ph.getPhoneNumber());
                sb.append(" (");
                sb.append(ph.getPhoneType().getTitle());
                sb.append(")");
                if(ph.getExtension() != 0){
                    sb.append("[ext. ");
                    sb.append(ph.getExtension());
                    sb.append("] ");
                }
                sb.append("<br />");
            }
            p.setPhoneListPretty(sb.toString());
        }
    }
    
    
    /**
     * Internal logic for generating an HTML string 
     * of a person's email list.
     * @param p 
     */
    private void generateEmailListPretty(Person p){
         if(p != null && p.getEmailList()!= null && !p.getEmailList().isEmpty()){
            StringBuilder sb = new StringBuilder("");
            for(ContactEmail ce: p.getEmailList()){
                sb.append(ce.getEmailaddress());
                sb.append("<br />");
            }
            p.setMailingAddressListPretty(sb.toString());
        }
    }
    
    
    /**
     * Logic intermediary for Updates to the human listing
     * @param h
     * @param u
     * @throws IntegrationException 
     */
    public void humanEdit(Human h, User u) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        if(h != null && u != null){
            h.setLastUpdatedBy(u);
            pi.updateHuman(h);
        }
    }
    
    /**
     * Factory method for Human objects not in the DB
     * @return an empty Skeleton of a Human
     */
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
    public Person createPersonSkeleton(Municipality m){
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
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public Person insertHuman(Human h, UserAuthorized ua) throws IntegrationException, BObStatusException{
        SystemIntegrator si = getSystemIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
       
        h.setLastUpdatedBy(ua);
        h.setCreatedBy(ua);
        h.setSource( si.getBOBSource(Integer.parseInt(getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE)
                                .getString("bobsourcePersonInternal"))));
        return getPersonByHumanID(pi.insertHuman(h));
        
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
     * Queries DB for all humans who have been linked to a User in the table login
     * @return a list, perhaps with the Humans mapped to hers
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<Human> getHumansMappedToUsers() throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        List<Human> hl = new ArrayList<>();
        List<Integer> idl = pi.getAllHumansDesignatedAsUserHumans();
        if(!idl.isEmpty()){
            for(Integer i: idl){
                hl.add(getHuman(i));
            }
        }
        return hl;
    }
    
    // *******************************************************
    // *********  CONTACT PHONE AND EMAIL STUFF **************
    // *******************************************************
    
    /**
     * Factory method for ContactPhone objects
     * @return 
     */
    public ContactPhone createContactPhoneSkeleton(){
        return new ContactPhone();
    }
    
    public List<ContactPhone> getContactPhoneList(List<Integer> phidl) throws IntegrationException{
        List<ContactPhone> phoneList = new ArrayList<>();
        if(phidl != null && !phidl.isEmpty()){
            for(Integer i: phidl){
                phoneList.add(getContactPhone(i));
            }
        }
       return phoneList;
    }

    /**
     * Type safe getter
     * @param ph
     * @return 
     */
    public ContactPhone getContactPhone(ContactPhone ph) throws IntegrationException{
        if(ph != null){
            return getContactPhone(ph.getPhoneID());
        } else {
            return null;
        }
    }
    
    /**
     * Extracts a phone by ID from the DB
     * @param phid
     * @return
     * @throws IntegrationException 
     */
    public ContactPhone getContactPhone(int phid) throws IntegrationException{
        if(phid != 0){
            PersonIntegrator pi = getPersonIntegrator();
            return configureContactPhone(pi.getContactPhone(phid));
        } else {
            throw new IntegrationException("cannot fetch phone with ID of 0");
            
        }
        
    }
    
    /**
     * Logic configuration intermediary for ContactPhone objects
     * @param ph
     * @return the passed in object that has been configured
     */
    private ContactPhone configureContactPhone(ContactPhone ph){
        // nothing to do here yet
        return ph;
    }
    
    /**
     * Logic intermediary for updating phone numbers
     * @param phone
     * @param ua 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void contactPhoneUpdate(ContactPhone phone, UserAuthorized ua) throws IntegrationException, BObStatusException{
        PersonIntegrator pi = getPersonIntegrator();
        if(phone == null || ua == null){
            throw new BObStatusException("Cannot update phone witn null phone or user");
        }
        phone.setLastUpdatedBy(ua);
        pi.updateContactPhone(phone);
    }
    
    /**
     * Logic block for deactivating a phone number record
     * @param phone to deactivate
     * @param ua doing the deactivation
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void contactPhoneDeactivate(ContactPhone phone, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(phone == null || ua == null){
            throw new BObStatusException("Cannot deactivate phone with null phone or user");
        }
        PersonIntegrator pi = getPersonIntegrator();
        phone.setDeactivatedBy(ua);
        phone.setDeactivatedTS(LocalDateTime.now());
        phone.setLastUpdatedBy(ua);
        pi.updateContactPhone(phone);
        
        
    }
    
    
    /**
     * Logic intermediary for adding a new phone number to the DB
     * @param phone
     * @param p
     * @param ua
     * @return 
     */
    public ContactPhone contactPhoneAdd(ContactPhone phone, Person p, UserAuthorized ua) throws IntegrationException, BObStatusException{
        PersonIntegrator pi = getPersonIntegrator();
        if(phone == null || p == null || ua == null){
            throw new BObStatusException("Cannot insert phone with null phone, person, or user");
        }
        phone.setCreatedBy(ua);
        phone.setLastUpdatedBy(ua);
        phone.setHumanID(p.getHumanID());
        return getContactPhone(pi.insertContactPhone(phone));
        
    }
    
    /** EMAILS ****/
    
    /**
     * Factory method for ContactEmail objects
     * @return 
     */
    public ContactEmail createContactEmailSkeleton(){
        return new ContactEmail();
        
    }
    
    /**
     * Convenience method for extracting an entire list of emails
     * from the database, all configured and ready for injection into a Person!
     * @param emids
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public List<ContactEmail> getContactEmailList(List<Integer> emids) throws IntegrationException{
        List<ContactEmail> emailList = new ArrayList<>();
        if(emids != null && !emids.isEmpty()){
            for(Integer i: emids){
                emailList.add(getContactEmail(i));
            }
        }
        return emailList;
    }
    
    /**
     * Type safe email getter
     * @param ce
     * @return
     * @throws IntegrationException 
     */
    public ContactEmail getContactEmail(ContactEmail ce) throws IntegrationException{
        if(ce != null){
            return getContactEmail(ce.getEmailID());
        } else {
            return null;
        }
    }
    
    
    /**
     * Retrieves a single ContactEmail from the DB
     * @param emid
     * @return 
     */
    public ContactEmail getContactEmail(int emid) throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        return configureContactEmail(pi.getContactEmail(emid));
    }
    
    /**
     * Internal logic method for configuring the ContactEmail objects
     * @param ce
     * @return 
     */
    private ContactEmail configureContactEmail(ContactEmail ce){
        // Nothing to do here yet
        return ce;
    }
    
    
    /**
     * Logic intermediary for updating email records 
     * @param em
     * @param ua 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void contactEmailUpdate(ContactEmail em, UserAuthorized ua) throws IntegrationException, BObStatusException{
        PersonIntegrator pi = getPersonIntegrator();
        if(em == null || ua == null){
            throw new BObStatusException("Cannot update email with null email, or user");
        }
        em.setLastUpdatedBy(ua);
        pi.updateContactEmail(em);
        
    }
    
    /**
     * Logic intermediary for adding new email addresses to the DB
     * @param em
     * @param p
     * @param ua 
     * @return the new ContactEmail with a DB key
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public ContactEmail contactEmailAdd(ContactEmail em, Person p, UserAuthorized ua) throws IntegrationException, BObStatusException{
        PersonIntegrator pi = getPersonIntegrator();
        if(em == null || p == null || ua == null){
            throw new BObStatusException("Cannot write new email with null email, person, or user");
        }
        em.setCreatedBy(ua);
        em.setLastUpdatedBy(ua);
        em.setHumanID(p.getPersonID());
        
        return getContactEmail(pi.insertContactEmail(em));
    }
    
    /**
     * Logic block for deactivating an email
     * @param em to deac
     * @param ua doing the deactivation
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public void contactEmailDeactivate(ContactEmail em, UserAuthorized ua) throws BObStatusException, IntegrationException{
        if(em == null || ua == null){
            throw new BObStatusException("Cannot Deac an email with null email or user");
        }
        PersonIntegrator pi = getPersonIntegrator();
        em.setDeactivatedBy(ua);
        em.setDeactivatedTS(LocalDateTime.now());
        em.setLastUpdatedBy(ua);
        pi.updateContactEmail(em);
        
        
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
     * does not have a session List to work from. Currently it just grabs
     * the UserAuthorized's Person
     * @param cred
     * @return the selected person proposed for becoming the sessionPerson
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public Person selectDefaultPerson(Credential cred) throws IntegrationException{
        UserCoordinator uc = getUserCoordinator();
        PersonCoordinator pc = getPersonCoordinator();
        try {
            User u = uc.user_getUser(cred.getGoverningAuthPeriod().getUserID());
            System.out.println("PersonCoordinator.selectDefaultPerson: " + u);
            return pc.getPerson(u.getHuman());
        } catch (BObStatusException ex) {
            throw new IntegrationException(ex.getMessage());
        }
        
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
//                pl.add(pi.getPersonByHumanID(idList.remove(0)));
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
    public List<Person> assemblePersonListFromHumanList(List<Human> hl) throws IntegrationException, BObStatusException{
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
     * Returns the master list of all contact phone types
     * e.g. cell phone, home, work, etc. 
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public List<ContactPhoneType> getContactPhoneTypeList() throws IntegrationException{
        PersonIntegrator pi = getPersonIntegrator();
        return pi.getContactPhoneTypeList();
        
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
        
//        PersonWithChanges skeleton = new PersonWithChanges(getPersonByHumanID(personID));
        
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
