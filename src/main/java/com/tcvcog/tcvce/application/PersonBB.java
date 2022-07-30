/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.session.SessionBean;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.BOBSource;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.ContactEmail;
import com.tcvcog.tcvce.entities.ContactPhone;
import com.tcvcog.tcvce.entities.ContactPhoneType;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.Human;
import com.tcvcog.tcvce.entities.HumanLink;
import com.tcvcog.tcvce.entities.IFace_humanListHolder;
import com.tcvcog.tcvce.entities.IFace_noteHolder;
import com.tcvcog.tcvce.entities.LinkedObjectRole;
import com.tcvcog.tcvce.entities.LinkedObjectSchemaEnum;
import com.tcvcog.tcvce.entities.MailingAddress;
import com.tcvcog.tcvce.entities.MailingAddressLink;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonLinkHeavy;
import com.tcvcog.tcvce.entities.PropertyUnit;
import com.tcvcog.tcvce.entities.PropertyUnitDataHeavy;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

/**
 * The inaugural humanized PersonBB for all basic person stuff
 * Backing the personTools.xhtml which gets embedded in the nav container
 * @author sylvia
 */
public class PersonBB extends BackingBeanUtils {
    
    
    private Person currentPerson;
    private boolean currentPersonHumanFieldsEditMode;
    private boolean currentHumanLinkEditMode;
    
    private IFace_noteHolder currentNoteHolder;
    private String formNewNoteUniversal;
    private String pageComponentToUpdateAfterNoteCommit;
    
    private IFace_humanListHolder currentHumanListHolder;
    private List<IFace_humanListHolder> upstreamPersonPoolList;
    private List<LinkedObjectRole> linkRoleCandidateList;
    private LinkedObjectRole selecetedLinkedObjetRole;
    private PropertyUnit selectedUnitForHumanLinkTarget;
    private PropertyUnitDataHeavy selectedUnitForHumanLinkTargetDataHeavy;
    
    private PersonLinkHeavy currentPersonLinkHeavy;
    private HumanLink currentHumanLink;
    private String formHumanLinkNotes;
    
    private MailingAddress currentMailingAddress;
    private boolean currentMailingAddressEditMode;
    
    // PHONE STUFF
    private ContactPhone currentContactPhone;
    private boolean currentContactPhoneEditMode;
    private List<ContactPhoneType> phoneTypeList;
    private boolean currentContactPhoneDisconnected;
    
    
    // EMAIL STUFF
    private ContactEmail currentContactEmail;
    private boolean currentContactEmailEditMode;
    
    private List<BOBSource> sourceList;
    
    
    // ********************************************************
    // *************** MIGRATED SEARCH STUFF ******************
    // ********************************************************
    private QueryPerson querySelected;
    private List<QueryPerson> queryList;
    private SearchParamsPerson paramsSelected;
    private String queryLog;
    
    private List<Person> personList;
    private List<Person> filteredPersonList;
    private boolean appendResultsToList;
    
    private List<HumanLink> humanLinkListQueued;
    private String personListComponentIDToUpdatePostLinkingOperation; 
    
    
    /**
     * Creates a new instance of PersonBB
     */
    public PersonBB() {
    }
    
     @PostConstruct
    public void initBean(){
        SystemCoordinator sc = getSystemCoordinator();
        PersonCoordinator pc = getPersonCoordinator();
        
        currentPerson = getSessionBean().getSessPerson();
        System.out.println("PersonBB.initBean()");
        currentPersonHumanFieldsEditMode = false;
        currentContactPhoneEditMode = false;
        currentContactEmailEditMode = false;
        
        
        try {
            loadLinkedObjectRoleList();
            phoneTypeList = pc.getContactPhoneTypeList();
            sourceList = sc.getBobSourceListComplete();
            currentHumanListHolder = getSessionBean().getSessProperty();
            loadLinkedObjectRoleList();
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        }
        setupQueryInfrastructure();
        humanLinkListQueued = new ArrayList<>();
    }
    
    /**
     * Utility method for loading link roles based on session list holder
     * @throws IntegrationException
     * @throws BObStatusException 
     */
    private void loadLinkedObjectRoleList() throws IntegrationException, BObStatusException{
        SystemCoordinator sc = getSystemCoordinator();
        if(currentHumanListHolder == null){
            currentHumanListHolder = getSessionBean().getSessHumanListHolder();
        }
        if(currentHumanListHolder != null){
            linkRoleCandidateList = sc.assembleLinkedObjectRolesBySchema(currentHumanListHolder.getHUMAN_LINK_SCHEMA_ENUM());
            if(linkRoleCandidateList != null){
                System.out.println("PersonBB.loadLinkedObjectRoleListUsingSessionHLH | roleListSize = " + linkRoleCandidateList.size());
            }
        }
        
    }
    
    /**
     * Sets up person pools based on the current Human List Holder
     */
    private void configureUpstreamPersonPools() throws IntegrationException, EventException, AuthorizationException, BObStatusException{
        upstreamPersonPoolList = new ArrayList<>();
        SessionBean sb = getSessionBean();
        PropertyCoordinator pc = getPropertyCoordinator();
        if(currentHumanListHolder != null){
            if(currentHumanListHolder instanceof CECase){
                upstreamPersonPoolList.add(sb.getSessProperty());
                CECase cse = (CECase) currentHumanListHolder;
                if(cse.getPropertyUnitID() != 0){
                    upstreamPersonPoolList.add(pc.getPropertyUnitDataHeavy(sb.getSessPropertyUnit(), sb.getSessUser()));
                }
            } else if(currentHumanListHolder instanceof PropertyUnit){
                upstreamPersonPoolList.add(sb.getSessProperty());
            } else if(currentHumanListHolder instanceof EventCnF){
                EventCnF ev = (EventCnF) currentHumanListHolder;
                if(ev.getDomain() == DomainEnum.CODE_ENFORCEMENT){
                    upstreamPersonPoolList.add(sb.getSessCECase());
                }
            }
        }
    }
    
    /**********************************************************/
    /************** SEARCH ORGANS - MIGRATED!! ****************/
    /**********************************************************/
        
    private void setupQueryInfrastructure(){
        SearchCoordinator sc = getSearchCoordinator();

        personList = new ArrayList<>();
        appendResultsToList = false;
        filteredPersonList = new ArrayList<>();
        try {
            setQueryList(sc.buildQueryPersonList(getSessionBean().getSessUser().getMyCredential()));
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        if(queryList != null && !queryList.isEmpty()){
            querySelected = getQueryList().get(0);
        }
        
        if(querySelected != null && querySelected.getPrimaryParams() != null){
            paramsSelected = getQuerySelected().getPrimaryParams();
        }
    
    }
    
    /**
     * Listener for user clicks of the "search" link for persons
     * @param ev 
     */
    public void onPersonSearchInitLinkClick(ActionEvent ev){
        getSessionBean().setSessHumanListHolder(getSessionBean().getSessProperty());
        System.out.println("Search for Persons INIT");
    }
    
    /**
     * Listener for user requests to clear the person list
     * @param ev 
     */
    public void clearResultList(ActionEvent ev){
        getPersonList().clear();
        
    }
      /**
     * Listener method for changes to the query drop down box
     */
    public void changeQuerySelected(){
        
        if(getQuerySelected() != null && getQuerySelected().getPrimaryParams() != null){
            setParamsSelected(getQuerySelected().getPrimaryParams());
        }
    
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "New query loaded!", ""));

        
    }
    
    /**
     * Listener for user request to execute the query!
     * 
     * @param event 
     */
    public void executeQuery(ActionEvent event){
        PersonCoordinator pc = getPersonCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        try {
            List<Person> pl = pc.assemblePersonListFromHumanList(sc.runQuery(getQuerySelected()).getBOBResultList());
            
            if(!appendResultsToList && personList != null){
                personList.clear();
            } 
            getPersonList().addAll(pl);
            setQueryLog(getQuerySelected().getQueryLog());
            
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Your search completed with " + pl.size() + " results", ""));
            
        } catch (SearchException | IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to complete search! ", ""));
        }
    }
    
    /**
     * listener for user requests to clear the current query
     * @param ev 
     */
    public void resetCurrentQuery(ActionEvent ev){
        setupQueryInfrastructure();
        System.out.println("PersonSearchBB.resetCurrentQuery ");
//        querySelected = sc.initQuery(QueryPersonEnum.valueOf(querySelected.getQueryName().toString()),
//                getSessionBean().getSessUser().getMyCredential());
        
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Query list rebuilt; filters reset", ""));
        
        setQueryLog(null);
        
    }
        
    /**
     * Responds to the user wanting to view a person from the search result list
     * @param p
     */
    public void explorePerson(Person p){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            Person per = pc.getPerson(p);
            getSessionBean().setSessPerson(per);
            currentPerson = per;
            onLoadHumanLinksToCurrentPerson(null);
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Person exploring is broken",""));
        }
    }
    
    /**
     * Adaptor method for calling explorePerson with a human
     * @param h 
     */
    public void exploreHuman(Human h){
        PersonCoordinator pc = getPersonCoordinator();
        Person p = null;
        try {
            p = pc.getPerson(h);
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        } 
        if(p != null){
            explorePerson(p);
        }
        
    }

    
    /**********************************************************/
    /************** REFRESHING  *******************************/
    /**********************************************************/
    
    
    /**
     * Listener for user wanting to view the current person
     * @param ev 
     */
    public void onViewCurrentPersonLinkClick(ActionEvent ev){
        refreshCurrentPersonAndUpdateSessionPerson();
        
    }
    
    /**
     * Refreshes the current person
     */
    private void refreshCurrentPersonAndUpdateSessionPerson(){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            currentPerson = pc.getPerson(currentPerson);
            onLoadHumanLinksToCurrentPerson(null);
            getSessionBean().setSessPerson(currentPerson);
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not refresh current Person", ""));
        }
    }
    
    
    /**********************************************************/
    /************** HUMANS INFRASTRUCTURE *********************/
    /**********************************************************/ 
    
    
    /**
     * Listener for user requests to view a human
     * @param h 
     */
    public void onHumanViewLinkClick(Human h){
        System.out.println("PersonBB.onHumanViewLinkClick : " + h.getName());
        PersonCoordinator pc = getPersonCoordinator();
        try {
            currentPerson = pc.getPerson(h);
            onLoadHumanLinksToCurrentPerson(null);
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        } 
    }
    
    /**
     * Listener for user clicks of the human edit button
     * @param ev
     */
    public void toggleHumanEditMode(ActionEvent ev){
        System.out.println("PersonBB.toggleHumanEditMode: toggle val: " + currentPersonHumanFieldsEditMode);
        if(currentPersonHumanFieldsEditMode){
            if(currentPerson != null && currentPerson.getHumanID() == 0){
                // we've got a new record, so commit our add to DB
                onPersonAddCommit();
                System.out.println("PersonBB.toggleHumanEditMode: new person; committed");
                   getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully added new person record: " + currentPerson.getHumanID(), ""));
            } else {
                // we've got an existing human, so commit updates
                onHumanEditCommitButtonChange(null);
                System.out.println("PersonBB.toggleHumanEditMode: person edit done");
            }
            refreshCurrentPersonAndUpdateSessionPerson();
        }
        currentPersonHumanFieldsEditMode = !currentPersonHumanFieldsEditMode;
    }
    
    /**
     * Listener for user clicks of the human link edit button
     * @param ev
     */
    public void toggleHumanLinkEditMode(ActionEvent ev){
        System.out.println("PersonBB.toggleHumanLinkEditMode: toggle val: " + currentHumanLinkEditMode);
        if(currentHumanLinkEditMode){
           
            onHumanLinkEditCommit();
            System.out.println("PersonBB.toggleHumanLinkEditMode: person link edit done");
            refreshCurrentPersonAndUpdateSessionPerson();
        }
        currentHumanLinkEditMode = !currentHumanLinkEditMode;
    }
    
    /**
     * 
     * @param ev 
     */
    private void onHumanLinkEditCommit(){
       PersonCoordinator pc = getPersonCoordinator();
        try {
            pc.updateHumanLink(currentHumanListHolder, currentHumanLink, getSessionBean().getSessUser());
            refreshCurrentHumanLink();
            injectSessionHumanLinkListForComponentRefresh();
            getFacesContext().addMessage(null,
             new FacesMessage(FacesMessage.SEVERITY_INFO,
                     "Successfully updated human link ID: " + currentHumanLink.getLinkID(), ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
             new FacesMessage(FacesMessage.SEVERITY_ERROR,
                     "Fatal error updating human link", ""));
        } 
    }
    
    /**
     * Refreshes current human link (how could you have guessed?)
     */
    private void refreshCurrentHumanLink(){
        PersonCoordinator pc = getPersonCoordinator();
        if(currentHumanLink != null){
            try {
                currentHumanLink = pc.getHumanLink(currentHumanLink.getLinkID(), currentHumanListHolder.getHUMAN_LINK_SCHEMA_ENUM());
                System.out.println("PersonBB.refreshCurrentHumanLink | Refreshed human link: " + currentHumanLink.getLinkID());
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
            }
        }
    }
    
    /**
     *  Listener for user requests to start the humanlink view process
     * @param hl 
     * @param hlh 
     */
    public void onHumanLinkEditInitButtonChange(HumanLink hl, IFace_humanListHolder hlh){
        currentHumanLink = hl;
        if(hlh != null){
            currentHumanListHolder = hlh;
            currentNoteHolder = hl;
            
        }
        extractHumanLinkListComponentIDFromHTTPRequest();
        try {
            loadLinkedObjectRoleList();
            
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
             new FacesMessage(FacesMessage.SEVERITY_ERROR,
                     "Fatal error preparing human link view edit operation", ""));
        }
        
    }
    
    /**
     * Listener for user requests to cease editing a human link
     * @param ev 
     */
    public void onHumanLinkEditOpertationAbortButtonChange(ActionEvent ev){
        currentHumanLinkEditMode = false;
    }
    
    
    /**
     * Listener for user requests to add a human
     * @param ev
     */
    public void onPersonCreateInitButtonChange(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        currentPerson = pc.createPersonSkeleton(getSessionBean().getSessMuni());
        currentPersonHumanFieldsEditMode = true;
        System.out.println("PersonBB.onHumanAddInitButtonChange");
    }
    
    /**
     * Listener for user requests to finalize human changes
     * @param ev 
     */
    private void onPersonAddCommit(){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            currentPerson = pc.insertHuman(currentPerson, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully added person to database!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "FATAL ERROR CODE P001: Could not add new Person to the database!", ""));
        }    
    }
    
    /**
     * Listener for user requests to start the human edit process
     * @param h
      */
    public void onHumanEditInitButtonChange(Human h){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            currentPerson = pc.getPerson(h);
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        }
    }
    
    /**
     * Listener for user requests to commit the human edit process
     * @param ev
     */
    public void onHumanEditCommitButtonChange(ActionEvent ev){
        
        PersonCoordinator pc = getPersonCoordinator();
        try {
            pc.humanEdit(currentPerson, getSessionBean().getSessUser());
            refreshCurrentPersonAndUpdateSessionPerson();
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully updated " + currentPerson.getName() + ", id " + currentPerson.getHumanID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "ERROR updating " + currentPerson.getName() + ", id " + currentPerson.getHumanID(), ""));
            
        }
        
        
    }
    
    /**
     * Listener for user requests to abort a human add or edit operation
     * @param ev 
     */
    public void onHumanOperationAbortButtonChange(ActionEvent ev){
        System.out.println("PersonBB.onHumanOperationAbortButtonChange");
        currentPersonHumanFieldsEditMode = false;
        getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                         "Abort successful; no changes made", ""));
    }
    
    
   
    
    /***********************************************************/
    /************** Phone number INFRASTRUCTURE ****************/
    /**********************************************************/
    
    /**
     * Listener for user clicks of the phone edit button
     */
    public void togglePhoneEditMode(){
        System.out.println("PersonBB.togglePhoneEditMode: toggle mode " + currentContactPhoneEditMode);
        if(currentContactPhoneEditMode){
            if(currentContactPhone != null && currentContactPhoneDisconnected){
                currentContactPhone.setDisconnectRecordedBy(getSessionBean().getSessUser());
                currentContactPhone.setDisconnectTS(LocalDateTime.now());
            } else {
                currentContactPhone.setDisconnectRecordedBy(null);
                currentContactPhone.setDisconnectTS(null);
            }
            if(currentContactPhone != null && currentContactPhone.getPhoneID() == 0){
                onPhoneAddCommitButtonChange();
                System.out.println("PersonBB.togglePhoneEditMode: added new phone");
            } else {
                onPhoneEditCommitButtonChange();
                System.out.println("PersonBB.togglePhoneEditMode: edited phone");
           }
        }
        
        currentContactPhoneEditMode = !currentContactPhoneEditMode;
        
    }
    
    /**
     * Listener for user requests to start adding a new phone number
     * @param ev 
     */
    public void onPhoneAddInitButtonChange(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        currentContactPhone = pc.createContactPhoneSkeleton();
        currentContactPhoneEditMode = true;
        System.out.println("PersonBB.onPhoneAddInitButtonChange");
        
        
    }
    
    /**
     * Listener for user requests to finalize a new phone number
     */
    public void onPhoneAddCommitButtonChange(){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            currentContactPhone = pc.contactPhoneAdd(currentContactPhone, currentPerson, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Succesfully inserted new phone number with ID " + currentContactPhone.getPhoneID(), ""));
            refreshCurrentPersonAndUpdateSessionPerson();
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to insert new ContactPhone, sorry. This problem must be fixed by an dev.", ""));
        }
    }
    
    
    /**
     * Listener for user requests to start the phone editing process
     * @param ph
     */
    public void onPhoneEditInitButtonChange(ContactPhone ph){
        if(ph != null){
            currentContactPhone = ph;
            currentContactPhoneDisconnected = ph.getDisconnectTS() != null;
        }
        currentContactPhoneEditMode = true;
    }
    
    /**
     * Listener for user requests to finalize the phone edit operation
     */
    public void onPhoneEditCommitButtonChange(){
         PersonCoordinator pc = getPersonCoordinator();
        try {
            if(currentContactPhoneDisconnected){
                currentContactPhone.setDisconnectTS(LocalDateTime.now());
                currentContactPhone.setDisconnectRecordedBy(getSessionBean().getSessUser());
                getFacesContext().addMessage(null,
                     new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Marked phone number ID " + currentContactPhone.getPhoneID() + " as disconnected!", ""));
            }
            pc.contactPhoneUpdate(currentContactPhone, getSessionBean().getSessUser());
            currentContactPhone = pc.getContactPhone(currentContactPhone);
            // reset our form
            currentContactPhoneDisconnected = false;
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Succesfully Updated new phone number with ID " + currentContactPhone.getPhoneID(), ""));
            refreshCurrentPersonAndUpdateSessionPerson();
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Unable to Update phone number, sorry. This problem must be fixed by an dev.", ""));
        }
    }
    
    /**
     * Listener for user requests to abort any phone operation
     * @param ev 
     */
    public void onPhoneOperationAbortButtonChange(ActionEvent ev){
        currentContactPhoneEditMode = false;
        getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Aborted; no changes made!", ""));
        

    }

    /**
     * Listener for user requests to start the phone removal process
     * @param ev 
     */
    public void onPhoneRemoveInitLinkClick(ActionEvent ev){
        System.out.println("PersonBB.onPhoneRemoveInitLinkClick");
        
    }
    
    /**
     * Listener for user requests to confirm phone removal
     * @param ev 
     */
    public void onPhoneRemoveCommitButtonPress(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        try {
            pc.contactPhoneDeactivate(currentContactPhone, getSessionBean().getSessUser());
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully removed phone ID: " + currentContactPhone.getPhoneID(), ""));
            StringBuilder sb = new StringBuilder();
            sb.append("Phone ID: ");
            sb.append(currentContactPhone.getPhoneID());
            sb.append(" | Phone number: ");
            sb.append(currentContactPhone.getPhoneNumber());
            
            
            MessageBuilderParams mbp = new MessageBuilderParams(
                    currentPerson.getNotes(), 
                    "Phone Removal", 
                    "The following phone number was removed from this person record: ", 
                    sb.toString(), 
                    getSessionBean().getSessUser(), null);
            currentPerson.setNotes(sc.appendNoteBlock(mbp));
            sc.writeNotes(currentPerson, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Appended note to Person ID: " + currentPerson.getHumanID() , ""));
             refreshCurrentPersonAndUpdateSessionPerson();
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Phone removal fatal error: " + ex.getMessage() , ""));
        } 
        
    }
    
    
    /***********************************************************/
    /********************* Email INFRASTRUCTURE ****************/
    /**********************************************************/
    
    /**
     * Listener for user clicks of the edit button for emails
     * @param ev
     */
    public void toggleEmailEditMode(ActionEvent ev){
        System.out.println("PersonBB.toggleEmailEditMode | toggle mode: " + currentContactEmailEditMode);
        if(currentContactEmailEditMode){
            if(currentContactEmail != null && currentContactEmail.getEmailID() == 0){
                onEmailAddCommitButtonChange();
                System.out.println("PersonBB.toggleEmailEditMode | commited new email");
            } else {
                onEmailEditCommitButtonChange();
                System.out.println("PersonBB.toggleEmailEditMode | edited new email");
            }
        }
        currentContactEmailEditMode = !currentContactEmailEditMode;
    }
    
    
     /**
     * Listener for user requests to start adding a new email 
     * @param ev 
     */
    public void onEmailAddInitButtonChange(ActionEvent ev){
        System.out.println("PersonPP.onEmailAddInitButtonChange");
        PersonCoordinator pc = getPersonCoordinator();
        currentContactEmail = pc.createContactEmailSkeleton();
        currentContactEmailEditMode = true;
        
        
    }
    
    /**
     * Listener for user requests to finalize a new email 
     */
    public void onEmailAddCommitButtonChange(){
          PersonCoordinator pc = getPersonCoordinator();
        try {
            currentContactEmail = pc.contactEmailAdd(currentContactEmail, currentPerson, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully updated " + currentContactEmail.getEmailaddress()+ ", id " + currentPerson.getHumanID(), ""));
            refreshCurrentPersonAndUpdateSessionPerson();
        } catch (IntegrationException |  BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "ERROR updating email address with id " + currentContactEmail.getEmailID(), ""));
        }
    }
    
    
    /**
     * Listener for user requests to start the email editing process
     * @param ce 
     */
    public void onEmailEditInitButtonChange(ContactEmail ce){
        currentContactEmail = ce;
        currentContactEmailEditMode = true;
        
    }
    
    /**
     * Listener for user requests to start the removal process
     * @param ev 
     */
    public void onEmailRemoveInitButtonChange(ActionEvent ev){
        System.out.println("PersonBB.onEmailRemoveInitButtonChange");
    }
    
    /**
     * Listener for user confirmation to remove an email
     * @param ev 
     */
    public void onEmailRemoveCommitButtonChange(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
        try {
            pc.contactEmailDeactivate(currentContactEmail, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Successfully removed email ID: " + currentContactEmail.getEmailID() , ""));
            
            StringBuilder sb = new StringBuilder();
            sb.append("Email ID: ");
            sb.append(currentContactEmail.getEmailID());
            sb.append(" | Email address: ");
            sb.append(currentContactEmail.getEmailaddress());
            
            
            MessageBuilderParams mbp = new MessageBuilderParams(
                    currentPerson.getNotes(), 
                    "Email Removal", 
                    "The following email address was removed from this person record: ", 
                    sb.toString(), 
                    getSessionBean().getSessUser(), null);
            currentPerson.setNotes(sc.appendNoteBlock(mbp));
            sc.writeNotes(currentPerson, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Appended note to Person ID: " + currentPerson.getHumanID() , ""));
            refreshCurrentPersonAndUpdateSessionPerson();
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Email removal fatal error: " + ex.getMessage() , ""));
            
        }
    }
    
    /**
     * Listener for user requests to finalize the email edit operation
     */
    public void onEmailEditCommitButtonChange(){
          PersonCoordinator pc = getPersonCoordinator();
        try {
            pc.contactEmailUpdate(currentContactEmail, getSessionBean().getSessUser());
            currentContactEmail = pc.getContactEmail(currentContactEmail);
            refreshCurrentPersonAndUpdateSessionPerson();
              getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully updated " + currentPerson.getName() + ", id " + currentPerson.getHumanID(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "ERROR updating " + currentPerson.getName() + ", id " + currentPerson.getHumanID(), ""));
            
        }
        
    }
    
    /**
     * Listener for user requests to abort any email operation
     * @param ev 
     */
    public void onEmailOperationAbortButtonChange(ActionEvent ev){
          getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Aborted; No changes made!", ""));
          currentContactEmailEditMode = false;
        
    }
    
   
    
    
    /***********************************************************/
    /********************* MISC STUFF           ****************/
    /***********************************************************/
    
    /**
     * Starts the note adding process on a general note holding object
     * @param holder 
     */
    public void onNoteAppendUniversalInitLinkClick(IFace_noteHolder holder){
        currentNoteHolder = holder;
        formNewNoteUniversal = "";
        pageComponentToUpdateAfterNoteCommit = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("component-to-update");
        System.out.println("PersonBB.onNoteAppendUniversalInitLinkClick | page comp: " + pageComponentToUpdateAfterNoteCommit);
    }
    
    /**
     * Listener for user aborts of the noting process
     * @param ev 
     */
    public void onNoteAppendOperationAbort(ActionEvent ev){
        formNewNoteUniversal = "";
        
    }
    
    /**
     * Listener for user to commit notes to a universal note holder
     * @param ev
     */
    public void onNoteCommitOnNoteHolder(ActionEvent ev){
        SystemCoordinator sc = getSystemCoordinator();
        if(currentNoteHolder != null){
            MessageBuilderParams mbp = new MessageBuilderParams(currentNoteHolder.getNotes(), 
                    null, null, formNewNoteUniversal, getSessionBean().getSessUser(), null);
            currentNoteHolder.setNotes(sc.appendNoteBlock(mbp));
            try {
                sc.writeNotes(currentNoteHolder, getSessionBean().getSessUser());
                refreshCurrentNoteHolder();
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Note write success on :  " + currentNoteHolder.getNoteHolderFriendlyName(), ""));
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal note error:  " + ex.getMessage(), ""));
                
            } 
            formNewNoteUniversal = "";
        }
    }
    
    /**
     * Internal method for asking what type the NoteHolder is
     * and fetching a new copy from the DB. The UI will update itself
     */
    private void refreshCurrentNoteHolder() throws IntegrationException{
        PersonCoordinator pc = getPersonCoordinator();
        if(currentNoteHolder != null){
            if(currentNoteHolder instanceof ContactEmail){
                currentContactEmail = pc.getContactEmail(currentContactEmail);
                System.out.println("PersonBB.refreshCurrentNoteHolder | refreshing email");
            } else if(currentNoteHolder instanceof ContactPhone){
                currentContactPhone = pc.getContactPhone(currentContactPhone);
                System.out.println("PersonBB.refreshCurrentNoteHolder | refreshing phone");
            } else if(currentNoteHolder instanceof Person){
                refreshCurrentPersonAndUpdateSessionPerson();
                System.out.println("PersonBB.refreshCurrentNoteHolder | refreshing person");
            } else if(currentNoteHolder instanceof HumanLink){
                refreshCurrentHumanLink();
                System.out.println("PersonBB.refreshCurrentNoteHolder | refreshing humanlink");
            } 
        }
    }
    
       
    /***********************************************************/
    /********************* FANCY LINK MANAGEMENT STUFF *********/
    /***********************************************************/    
    /***********************************************************/
    /************** LINK MANAGE INFRASTRUCTURE *****************/
    /**********************************************************/
    
    /**
     * Listener for user requests to load person links
     * @param ev 
     */
    public void onLoadHumanLinksToCurrentPerson(ActionEvent ev){
        if(currentPerson != null){
            PersonCoordinator pc = getPersonCoordinator();
            try {
                currentPersonLinkHeavy = pc.assemblePersonLinkHeavy(currentPerson);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Loaded person " + currentPersonLinkHeavy.getHumanLinkList().size() + " links!", ""));
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal: Could not load person links, sorry!", ""));
                
            } 
        }
    }
    
    
    /**
     * Listener for user requests to load information about the human link
     * @param hl 
     */
    public void onHumanLinkSelectLinkClick(HumanLink hl){
        currentHumanLink = hl;
        
    }

    /**
     * Listener for user requests to start the change target operation
     * @param ev 
     */
    public void onChangeHumanLinkTargetLinkClick(ActionEvent ev){
         getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Choosing new person link target" , ""));
    }
    
    /**
     * Listener for user clicks of a human link holder
     * @param hlh 
     */
    public void onActivateNewHumanLinkTarget(IFace_humanListHolder hlh){
        try {
            currentHumanListHolder = hlh;
            loadLinkedObjectRoleList();
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "You are now ready to link to the session " + hlh.getHUMAN_LINK_SCHEMA_ENUM().getTARGET_OBJECT_FRIENDLY_NAME(), ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "FATAL ERROR: Could not setup link target, sorry!", ""));
        } 
        getSessionBean().setSessHumanListHolder(hlh);
    }
    
    /**
     * Listener for user clicks of the link representing the
     * target of a human link, such as a case or an event
     *
     * TODO: Finish me!
     *
     * @param hl the human link to explore
     * @return page nav route
     */
    public String onHumanLinkTargetIDLinkClick(HumanLink hl){
        
        return "";
    }
    
    /**
     * Listener for user requests to complete the note appending process
     * on a human link
     * @deprecated  in favor of universal note holder interface
     * @param ev 
     */
    public void onHumanLinkNoteAppendCommitButtonChange(ActionEvent ev){
       PersonCoordinator pc = getPersonCoordinator();
        try {
            pc.appendNoteToHumanLink(currentHumanLink, formHumanLinkNotes, getSessionBean().getSessUser());
            refreshCurrentHumanLink();
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Note write success!", ""));
        } catch (BObStatusException | IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Fatal: Could not append note: " + ex.getMessage(), ""));
        }
        
    }
    
    /**
     * Listener for user requests to start the deac process
     * for human links
     * @param ev 
     */
    public void onHumanLinkDeactivatInitLinkClick(ActionEvent ev){
        System.out.println("PersonBB.onHumanLinkDeactivatInitLinkClick");
    }
    
    /**
     * Listener for user requests to complete the note appending process
     * on a human link
     * @param ev 
     */
    public void onHumanLinkDeactivateButtonChange(ActionEvent ev){
       PersonCoordinator pc = getPersonCoordinator();
        try {
            pc.deactivateHumanLink(currentHumanLink, getSessionBean().getSessUser());
            injectSessionHumanLinkListForComponentRefresh();
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Link deactivate success!", ""));
        } catch (BObStatusException | IntegrationException ex) {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Fatal: Could not deactivate human link: " + ex.getMessage(), ""));
        }
        
    }
    
    /**
     * Listener to user requests to start the creation process
     * for a new human link. Checks to make sure the session
     * has a HumanLinkHolder. If it doesn't, inject the current CECase, 
     * if that's null, inject the current session OccPeriod
     * 
     * @param ev 
     */
    public void onHumanLinkAddInitButtonChange(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        currentHumanLink = pc.createHumanLinkSkeleton(currentPerson);
        
        if(getSessionBean().getSessHumanListHolder() == null){
            if(getSessionBean().getSessCECase() != null){
                getSessionBean().setSessHumanListHolder(getSessionBean().getSessCECase() );
            } else if(getSessionBean().getSessOccPeriod() != null){
                getSessionBean().setSessHumanListHolder(getSessionBean().getSessOccPeriod() );
            }
        }
        
         try {
            loadLinkedObjectRoleList();
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        } 
    }
    
    
    /**
     * Listener for user requests to link the current IFace_HumanListHolder
     * to the current person with the currently selected role
     * @param ev 
     */
    public void onHumanLinkCreateCommitButtonChange(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            currentHumanLink.setLinkRole(selecetedLinkedObjetRole);
            
            pc.insertHumanLink(getSessionBean().getSessHumanListHolder(), currentHumanLink, getSessionBean().getSessUser());
            refreshCurrentPersonAndUpdateSessionPerson();
            onLoadHumanLinksToCurrentPerson(null);
            injectSessionHumanLinkListForComponentRefresh();
            getFacesContext().addMessage(null,
                  new FacesMessage(FacesMessage.SEVERITY_INFO,
                          "Successfully linked human!", ""));
        } catch (BObStatusException | IntegrationException ex) {
          
          getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Fatal: Could not create human link: " + ex.getMessage(), ""));
        } 
    }
    
    /**
     * Gets a new set of human links from the coordinator and injects it into the
     * session's master list of human links. It's the job of the refreshed
     * component to ask the session for this list and inject it into its 
     * HumanListHolder for immediate viewing by the user in the browser.
     * Note that this means the only call to the DB is made here via
     * the person coordinator and the calling object family need not 
     * rebuild its entire data heaby object!
     */
    private void injectSessionHumanLinkListForComponentRefresh(){
        PersonCoordinator pc = getPersonCoordinator();
        List<HumanLink> hllist = null;
        if(currentHumanListHolder != null){
            try {
                hllist = pc.getHumanLinkList(currentHumanListHolder);
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
            }
            getSessionBean().setSessHumanListRefreshedList(hllist);
        }
    }
    
    
      /**
       *
     * @return the linkRoleCandidateList
     */
    public List<LinkedObjectRole> getLinkRoleCandidateList() {
       
        return linkRoleCandidateList;
    }
    
    /**
     * Big cheese listener for user requests to start the process
     * of linking humans to a human list holder. I basically
     * inject the HLH into the session and keep a record of the 
     * name of the component I should update when the
     * linking is done
     * 
     * @param hlh to which we should start linking persons
     */
    public void onSelectAndLinkPersonsInit(IFace_humanListHolder hlh){
        
        currentHumanListHolder = hlh;
        getSessionBean().setSessHumanListHolder(currentHumanListHolder);
        
        extractHumanLinkListComponentIDFromHTTPRequest();
        try {
            loadLinkedObjectRoleList();
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        } 
         getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Commencing linking of Persons to " + hlh.getHUMAN_LINK_SCHEMA_ENUM().getTARGET_OBJECT_FRIENDLY_NAME() + " ID: " + hlh.getHostPK(), ""));
        System.out.println("PersonBB.onSelectAndLinkPersonsInit | " + hlh.getHUMAN_LINK_SCHEMA_ENUM().getTARGET_OBJECT_FRIENDLY_NAME()  + " |  PK: " +hlh.getHostPK());
        
    }
    
    /**
     * Extracts the value of key person-list-component-to-update
     * from the HTTP request for later updating
     */
    private void extractHumanLinkListComponentIDFromHTTPRequest(){
            personListComponentIDToUpdatePostLinkingOperation = 
                FacesContext.getCurrentInstance()
                        .getExternalContext()
                        .getRequestParameterMap()
                        .get("person-list-component-to-update");
        System.out.println("PersonBB.extractHumanLinkListComponentIDFromHTTPRequest | got component ID: " +personListComponentIDToUpdatePostLinkingOperation);
    }
    
    /**
     * Special getter that checks the session for updates on just this person.
     * @return 
     */
    public List<MailingAddressLink> getCurrentPersonMADLinkList(){
        if(currentPerson != null){
            List<MailingAddressLink> sessLinkList = getSessionBean().getSessMailingAddressLinkRefreshedList();
            if(sessLinkList != null){
                currentPerson.setMailingAddressLinkList(sessLinkList);
                getSessionBean().setSessMailingAddressLinkRefreshedList(null);
            }
            return currentPerson.getMailingAddressLinkList();
        }
        return new ArrayList<>();
    }
    
    
    /**
     * Listener for user requests to load person on the current property, 
     * which is a human list holder
     * @param ev 
     */
    public void onLoadPersonListOnSessionPropertyLinkClick(ActionEvent ev){
        System.out.println("PersonBB.loadPersonListOnSessionProperty");
        PersonCoordinator pc =getPersonCoordinator();
        if(personList != null && !personList.isEmpty()){
            if(getSessionBean().getSessProperty() != null && getSessionBean().getSessProperty().getHumanLinkList() != null){
                if(!getSessionBean().getSessProperty().getHumanLinkList().isEmpty()){
                    personList.clear();
                    try {
                        personList.addAll(pc.getPersonListFromHumanLinkList(getSessionBean().getSessProperty().getHumanLinkList()));
                        getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Loaded " + personList.size() + " persons from the current property!", ""));
                    } catch (IntegrationException | BObStatusException ex) {
                        System.out.println(ex);
                        getFacesContext().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error converting human links into persons for display", ""));
                    }
                } else {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Session property has a person list but that list is sans persons", ""));
                }
            } else {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Session property does not contain a list of persons", ""));
            }
        } else {
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                "Fatal error: Person search list improperly setup", ""));
        }
    }
    
    /**
     * Listener for user requests to add a person from the person search to the 
     * list of selected humans ready to be linked to the session humanListHolder
     * @param per 
     */
    public void onAddPersonToSelectedList(Person per){
        PersonCoordinator pc = getPersonCoordinator();
        humanLinkListQueued.add(pc.createHumanLinkSkeleton(per));
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO,
            "Added " + per.getName() + " to selected list.", ""));
    }
    
      /**
     * listener for user requests to add all of the persons in the working
     * person list to the queued for linking list
     * @param ev 
     */
    public void onQueueAllPersonsForLinking(ActionEvent ev){
        if(personList != null && !personList.isEmpty() && humanLinkListQueued != null){
            for(Person p: personList){
                onAddPersonToSelectedList(p);
            }
        }
    }
    
    
    /**
     * Listener for user requests to remove a person from the
     * session list of selected persons
     * @param hl
     */
    public void onRemovePersonFromSelectedList(HumanLink hl){
        humanLinkListQueued.remove(hl);
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO,
            "Removed" + hl.getName() + " to selected list.", ""));
        
        
    }
    
    
    /**
     * Listener for user requests to extract the humans from a list of human links 
     * and make them part of our list queued for linking
     * @param ev 
     */
    public void onTransferLinkedPersonsToWorkingPersonList(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        if(currentHumanListHolder != null && currentHumanListHolder.getHumanLinkList() != null && personList != null ){
            if(!appendResultsToList){
                personList.clear();
            }
            try {
                System.out.println("PersonBB.onTransferLinkedPersonsToWorkingPersonList | Transferring " + currentHumanListHolder.getHumanLinkList().size() + " persons!");                personList.addAll(pc.getPersonListFromHumanLinkList(currentHumanListHolder.getHumanLinkList()));
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Transferred " + currentHumanListHolder.getHumanLinkList().size() + " persons to working list for queuing.", ""));
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Fatal error: transferring persons to working list!", ""));
            } 
        }
    }

  
    
    /**
     * Listener for user requests to start the linking process
     * @deprecated users must select a link role first
     * @param ev 
     */
    public void onLinkPersonToCurrentParcelInit(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        currentHumanLink = pc.createHumanLinkSkeleton(currentPerson);
        getSessionBean().setSessHumanListHolder(getSessionBean().getSessProperty());
        getFacesContext().addMessage(null,
              new FacesMessage(FacesMessage.SEVERITY_INFO,
              "Choose your link role", ""));
        
        
    }
    
    /**
     * Quick workaround to link a person to the session property
     * @param ev 
     */
    public void onLinkPersonToCurrentParcel(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        if(currentPerson != null && getSessionBean().getSessProperty() != null){
            try {
                pc.insertHumanLink(getSessionBean().getSessProperty(), pc.createHumanLinkSkeleton(currentPerson), getSessionBean().getSessUser());
                  getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Linked " + currentPerson.getName() + " to Property ID " + getSessionBean().getSessProperty().getCountyParcelID(), ""));
            } catch (BObStatusException | IntegrationException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error linking person: " + ex.getMessage(), ""));
            } 
        }
        
        
    }
    
    
    /**
     * listener for user requests to undertake the linking
     * operation of the selected humans to the session's 
     * humanlisthold
     * 
     * @param ev 
     */
    public void onLinkSelectedPersonsToPersonHolder(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        if(humanLinkListQueued != null && !humanLinkListQueued.isEmpty()){
            for(HumanLink hl: humanLinkListQueued){
                try {
                    pc.insertHumanLink(currentHumanListHolder,
                            hl,
                            getSessionBean().getSessUser());
                    System.out.println("PersonBB.onLinkSelectedPersonsToPersonHolder | linked p " + hl.getName());
                    
                     getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Linked " + hl.getName() + " to Object ID " + currentHumanListHolder.getHostPK(), ""));
                } catch (BObStatusException | IntegrationException ex) {
                    System.out.println(ex);
                     getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error linking person: " + ex.getMessage(), ""));
                } 
            }
            refreshHumanListHolder();
            humanLinkListQueued.clear();
        }
    }
    
    /**
     * Refreshes our human link list
     */
    public void refreshHumanListHolder(){
        if(currentHumanListHolder != null){
            PersonCoordinator pc = getPersonCoordinator();
            try {
                currentHumanListHolder.setHumanLinkList(pc.getHumanLinkList(currentHumanListHolder));
                getSessionBean().setSessHumanListHolder(currentHumanListHolder);
            } catch (IntegrationException | BObStatusException ex) {
                System.out.println(ex);
            }
        }
    }
    
    /**
     * Listener for user requests to remove the given human
     * from the session's human list holder permanently
     * @param hl 
     */
    public void onRemoveHumanFromSessionHumanListHolder(HumanLink hl){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            pc.deactivateHumanLink(hl, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_INFO,
                       "Successfully deactivated person link " + hl.getLinkID(), ""));
        } catch (BObStatusException | IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error removing person link: " + ex.getMessage(), ""));
        } 
        
    }
    
    
    private void convertSelectedUnitToDataHeavyVersion(){
        PropertyCoordinator pc = getPropertyCoordinator();
        if(selectedUnitForHumanLinkTarget != null){
            try {
                selectedUnitForHumanLinkTargetDataHeavy = pc.getPropertyUnitDataHeavy(
                        selectedUnitForHumanLinkTarget,
                        getSessionBean().getSessUser());
            } catch (IntegrationException | AuthorizationException | BObStatusException | EventException ex) {
                System.out.println(ex);
            } 
        }
    }
    
    
    
    /***********************************************************/
    /********************* GETTERS AND SETTERS  ****************/
    /**********************************************************/
    

   

    /**
     * @return the currentPersonHumanFieldsEditMode
     */
    public boolean isCurrentPersonHumanFieldsEditMode() {
        return currentPersonHumanFieldsEditMode;
    }

    /**
     * @return the currentMailingAddress
     */
    public MailingAddress getCurrentMailingAddress() {
        return currentMailingAddress;
    }

    /**
     * @return the currentMailingAddressEditMode
     */
    public boolean isCurrentMailingAddressEditMode() {
        return currentMailingAddressEditMode;
    }

    /**
     * @return the currentContactPhone
     */
    public ContactPhone getCurrentContactPhone() {
        return currentContactPhone;
    }

    /**
     * @return the currentContactPhoneEditMode
     */
    public boolean isCurrentContactPhoneEditMode() {
        return currentContactPhoneEditMode;
    }

    /**
     * @return the currentContactEmail
     */
    public ContactEmail getCurrentContactEmail() {
        return currentContactEmail;
    }

    /**
     * @return the currentContactEmailEditMode
     */
    public boolean isCurrentContactEmailEditMode() {
        return currentContactEmailEditMode;
    }

  

    /**
     * @param currentPersonHumanFieldsEditMode the currentPersonHumanFieldsEditMode to set
     */
    public void setCurrentPersonHumanFieldsEditMode(boolean currentPersonHumanFieldsEditMode) {
        this.currentPersonHumanFieldsEditMode = currentPersonHumanFieldsEditMode;
    }

    /**
     * @param currentMailingAddress the currentMailingAddress to set
     */
    public void setCurrentMailingAddress(MailingAddress currentMailingAddress) {
        this.currentMailingAddress = currentMailingAddress;
    }

    /**
     * @param currentMailingAddressEditMode the currentMailingAddressEditMode to set
     */
    public void setCurrentMailingAddressEditMode(boolean currentMailingAddressEditMode) {
        this.currentMailingAddressEditMode = currentMailingAddressEditMode;
    }

    /**
     * @param currentContactPhone the currentContactPhone to set
     */
    public void setCurrentContactPhone(ContactPhone currentContactPhone) {
        this.currentContactPhone = currentContactPhone;
    }

    /**
     * @param currentContactPhoneEditMode the currentContactPhoneEditMode to set
     */
    public void setCurrentContactPhoneEditMode(boolean currentContactPhoneEditMode) {
        this.currentContactPhoneEditMode = currentContactPhoneEditMode;
    }

    /**
     * @param currentContactEmail the currentContactEmail to set
     */
    public void setCurrentContactEmail(ContactEmail currentContactEmail) {
        this.currentContactEmail = currentContactEmail;
    }

    /**
     * @param currentContactEmailEditMode the currentContactEmailEditMode to set
     */
    public void setCurrentContactEmailEditMode(boolean currentContactEmailEditMode) {
        this.currentContactEmailEditMode = currentContactEmailEditMode;
    }


    /**
     * @return the currentPerson
     */
    public Person getCurrentPerson() {
        return currentPerson;
    }

    /**
     * @param currentPerson the currentPerson to set
     */
    public void setCurrentPerson(Person currentPerson) {
        this.currentPerson = currentPerson;
    }

    /**
     * @return the phoneTypeList
     */
    public List<ContactPhoneType> getPhoneTypeList() {
        return phoneTypeList;
    }

    /**
     * @param phoneTypeList the phoneTypeList to set
     */
    public void setPhoneTypeList(List<ContactPhoneType> phoneTypeList) {
        this.phoneTypeList = phoneTypeList;
    }

    /**
     * @return the currentContactPhoneDisconnected
     */
    public boolean isCurrentContactPhoneDisconnected() {
        return currentContactPhoneDisconnected;
    }

    /**
     * @param currentContactPhoneDisconnected the currentContactPhoneDisconnected to set
     */
    public void setCurrentContactPhoneDisconnected(boolean currentContactPhoneDisconnected) {
        this.currentContactPhoneDisconnected = currentContactPhoneDisconnected;
    }

    /**
     * @return the sourceList
     */
    public List<BOBSource> getSourceList() {
        return sourceList;
    }

    /**
     * @param sourceList the sourceList to set
     */
    public void setSourceList(List<BOBSource> sourceList) {
        this.sourceList = sourceList;
    }

    /**
     * @return the currentPersonLinkHeavy
     */
    public PersonLinkHeavy getCurrentPersonLinkHeavy() {
        return currentPersonLinkHeavy;
    }

    /**
     * @param currentPersonLinkHeavy the currentPersonLinkHeavy to set
     */
    public void setCurrentPersonLinkHeavy(PersonLinkHeavy currentPersonLinkHeavy) {
        this.currentPersonLinkHeavy = currentPersonLinkHeavy;
    }

    /**
     * @return the currentHumanLink
     */
    public HumanLink getCurrentHumanLink() {
        return currentHumanLink;
    }

    /**
     * @param currentHumanLink the currentHumanLink to set
     */
    public void setCurrentHumanLink(HumanLink currentHumanLink) {
        this.currentHumanLink = currentHumanLink;
    }

    /**
     * @return the formHumanLinkNotes
     */
    public String getFormHumanLinkNotes() {
        return formHumanLinkNotes;
    }

    /**
     * @param formHumanLinkNotes the formHumanLinkNotes to set
     */
    public void setFormHumanLinkNotes(String formHumanLinkNotes) {
        this.formHumanLinkNotes = formHumanLinkNotes;
    }


    /**
     * @return the selecetedLinkedObjetRole
     */
    public LinkedObjectRole getSelecetedLinkedObjetRole() {
        return selecetedLinkedObjetRole;
    }

    /**
     * @param selecetedLinkedObjetRole the selecetedLinkedObjetRole to set
     */
    public void setSelecetedLinkedObjetRole(LinkedObjectRole selecetedLinkedObjetRole) {
        this.selecetedLinkedObjetRole = selecetedLinkedObjetRole;
    }

  

    /**
     * @param linkRoleCandidateList the linkRoleCandidateList to set
     */
    public void setLinkRoleCandidateList(List<LinkedObjectRole> linkRoleCandidateList) {
        this.linkRoleCandidateList = linkRoleCandidateList;
    }

    /**
     * @return the querySelected
     */
    public QueryPerson getQuerySelected() {
        return querySelected;
    }

    /**
     * @return the queryList
     */
    public List<QueryPerson> getQueryList() {
        return queryList;
    }

    /**
     * @return the paramsSelected
     */
    public SearchParamsPerson getParamsSelected() {
        return paramsSelected;
    }

    /**
     * @return the queryLog
     */
    public String getQueryLog() {
        return queryLog;
    }

    /**
     * @return the filteredPersonList
     */
    public List<Person> getFilteredPersonList() {
        return filteredPersonList;
    }

    /**
     * @return the appendResultsToList
     */
    public boolean isAppendResultsToList() {
        return appendResultsToList;
    }

    /**
     * @param querySelected the querySelected to set
     */
    public void setQuerySelected(QueryPerson querySelected) {
        this.querySelected = querySelected;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryPerson> queryList) {
        this.queryList = queryList;
    }

    /**
     * @param paramsSelected the paramsSelected to set
     */
    public void setParamsSelected(SearchParamsPerson paramsSelected) {
        this.paramsSelected = paramsSelected;
    }

    /**
     * @param queryLog the queryLog to set
     */
    public void setQueryLog(String queryLog) {
        this.queryLog = queryLog;
    }

    /**
     * @param filteredPersonList the filteredPersonList to set
     */
    public void setFilteredPersonList(List<Person> filteredPersonList) {
        this.filteredPersonList = filteredPersonList;
    }

    /**
     * @param appendResultsToList the appendResultsToList to set
     */
    public void setAppendResultsToList(boolean appendResultsToList) {
        this.appendResultsToList = appendResultsToList;
    }

    /**
     * @return the personList
     */
    public List<Person> getPersonList() {
        return personList;
    }

    /**
     * @param personList the personList to set
     */
    public void setPersonList(List<Person> personList) {
        this.personList = personList;
    }

    /**
     * @return the currentNoteHolder
     */
    public IFace_noteHolder getCurrentNoteHolder() {
        return currentNoteHolder;
    }

    /**
     * @param currentNoteHolder the currentNoteHolder to set
     */
    public void setCurrentNoteHolder(IFace_noteHolder currentNoteHolder) {
        this.currentNoteHolder = currentNoteHolder;
    }

    /**
     * @return the formNewNoteUniversal
     */
    public String getFormNewNoteUniversal() {
        return formNewNoteUniversal;
    }

    /**
     * @param formNewNoteUniversal the formNewNoteUniversal to set
     */
    public void setFormNewNoteUniversal(String formNewNoteUniversal) {
        this.formNewNoteUniversal = formNewNoteUniversal;
    }

    /**
     * @return the pageComponentToUpdateAfterNoteCommit
     */
    public String getPageComponentToUpdateAfterNoteCommit() {
        return pageComponentToUpdateAfterNoteCommit;
    }

    /**
     * @param pageComponentToUpdateAfterNoteCommit the pageComponentToUpdateAfterNoteCommit to set
     */
    public void setPageComponentToUpdateAfterNoteCommit(String pageComponentToUpdateAfterNoteCommit) {
        this.pageComponentToUpdateAfterNoteCommit = pageComponentToUpdateAfterNoteCommit;
    }

    /**
     * @return the humanLinkListQueued
     */
    public List<HumanLink> getHumanLinkListQueued() {
        return humanLinkListQueued;
    }

    /**
     * @param humanLinkListQueued the humanLinkListQueued to set
     */
    public void setHumanLinkListQueued(List<HumanLink> humanLinkListQueued) {
        this.humanLinkListQueued = humanLinkListQueued;
    }

    /**
     * @return the personListComponentIDToUpdatePostLinkingOperation
     */
    public String getPersonListComponentIDToUpdatePostLinkingOperation() {
        return personListComponentIDToUpdatePostLinkingOperation;
    }

    /**
     * @param personListComponentIDToUpdatePostLinkingOperation the personListComponentIDToUpdatePostLinkingOperation to set
     */
    public void setPersonListComponentIDToUpdatePostLinkingOperation(String personListComponentIDToUpdatePostLinkingOperation) {
        this.personListComponentIDToUpdatePostLinkingOperation = personListComponentIDToUpdatePostLinkingOperation;
    }

    /**
     * @return the currentHumanLinkEditMode
     */
    public boolean isCurrentHumanLinkEditMode() {
        return currentHumanLinkEditMode;
    }

    /**
     * @param currentHumanLinkEditMode the currentHumanLinkEditMode to set
     */
    public void setCurrentHumanLinkEditMode(boolean currentHumanLinkEditMode) {
        this.currentHumanLinkEditMode = currentHumanLinkEditMode;
    }

    /**
     * @return the currentHumanListHolder
     */
    public IFace_humanListHolder getCurrentHumanListHolder() {
        return currentHumanListHolder;
    }

    /**
     * @param currentHumanListHolder the currentHumanListHolder to set
     */
    public void setCurrentHumanListHolder(IFace_humanListHolder currentHumanListHolder) {
        this.currentHumanListHolder = currentHumanListHolder;
    }

    /**
     * @return the selectedUnitForHumanLinkTarget
     */
    public PropertyUnit getSelectedUnitForHumanLinkTarget() {
        return selectedUnitForHumanLinkTarget;
    }

    /**
     * @param selectedUnitForHumanLinkTarget the selectedUnitForHumanLinkTarget to set
     */
    public void setSelectedUnitForHumanLinkTarget(PropertyUnit selectedUnitForHumanLinkTarget) {
        convertSelectedUnitToDataHeavyVersion();
        this.selectedUnitForHumanLinkTarget = selectedUnitForHumanLinkTarget;
    }

    /**
     * @return the selectedUnitForHumanLinkTargetDataHeavy
     */
    public PropertyUnitDataHeavy getSelectedUnitForHumanLinkTargetDataHeavy() {
        return selectedUnitForHumanLinkTargetDataHeavy;
    }

    /**
     * @param selectedUnitForHumanLinkTargetDataHeavy the selectedUnitForHumanLinkTargetDataHeavy to set
     */
    public void setSelectedUnitForHumanLinkTargetDataHeavy(PropertyUnitDataHeavy selectedUnitForHumanLinkTargetDataHeavy) {
        this.selectedUnitForHumanLinkTargetDataHeavy = selectedUnitForHumanLinkTargetDataHeavy;
    }

    /**
     * @return the upstreamPersonPoolList
     */
    public List<IFace_humanListHolder> getUpstreamPersonPoolList() {
        return upstreamPersonPoolList;
    }

    /**
     * @param upstreamPersonPoolList the upstreamPersonPoolList to set
     */
    public void setUpstreamPersonPoolList(List<IFace_humanListHolder> upstreamPersonPoolList) {
        this.upstreamPersonPoolList = upstreamPersonPoolList;
    }
    
     
    
    
    
}
