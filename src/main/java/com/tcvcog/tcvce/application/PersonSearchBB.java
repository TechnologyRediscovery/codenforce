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
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonDataHeavy;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Ellen Bascomb
 */
public class PersonSearchBB extends BackingBeanUtils{

    private PersonDataHeavy currentPerson;
    
    private boolean onPageLoad_viewCurrentPersonProfile;
    private boolean onPageLoad_editCurrentPerson;
    
    private QueryPerson querySelected;
    private List<QueryPerson> queryList;
    private SearchParamsPerson paramsSelected;
    private String queryLog;

    private List<Person> personList;
    private List<Person> filteredPersonList;
    private boolean appendResultsToList;
    
    // from PersonInfo
    private boolean connectToActiveProperty;
    
    private String fieldDump;
    
    private String formNotes;
    
    
    
    
    private PersonType[] personTypes;
    
    /**
     * Creates a new instance of PersonBB
     */
    public PersonSearchBB() {
      
    }
    
    @PostConstruct
    public void initBean(){
       PersonCoordinator pc = getPersonCoordinator();
       SearchCoordinator sc = getSearchCoordinator();
       
       personList = getSessionBean().getSessPersonList();
       
       /*
        How to choose the currentPerson? This is the priority List implemented here:
        1. Session's queued person
        2. Session's person
        3. First person in the session person list which starts with person history
        4. The user's internal person
        */
       Credential cred = getSessionBean().getSessUser().getKeyCard();
       
       try{
            if(getSessionBean().getSessPersonQueued() != null){
                    currentPerson = pc.assemblePersonDataHeavy(getSessionBean().getSessPersonQueued(),
                            cred);
                 getSessionBean().setSessPerson(currentPerson);
                 getSessionBean().setSessPersonQueued(null);
            } else if (getSessionBean().getSessPerson() != null){
                 currentPerson = pc.assemblePersonDataHeavy(getSessionBean().getSessPerson(),
                            cred);
            } else if(personList != null && !personList.isEmpty()){
                 currentPerson = pc.assemblePersonDataHeavy(personList.get(0),
                            cred);
            } else {
                currentPerson = pc.assemblePersonDataHeavy(pc.getPerson(getSessionBean().getSessUser().getHuman()),
                            cred);
            }
       } catch (IntegrationException | BObStatusException ex){
           System.out.println(ex);
       }
       
       if(currentPerson != null){
             getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Your current person is PersonID: " + currentPerson.getHumanID() + " ("+ currentPerson.getLastName() + ")", ""));
       }
       
       
       
       personTypes = PersonType.values();
       
       setupQueryInfrastructure();
       
       // check session for requests to load profile on page load
       onPageLoad_viewCurrentPersonProfile = getSessionBean().isOnPageLoad_sessionSwitch_viewProfile();
       getSessionBean().setOnPageLoad_sessionSwitch_viewProfile(false);
       
    }
    
    public void clearResultList(ActionEvent ev){
        personList.clear();
        
    }
    
    
    public int getPersonListSize(){
        int s = 0;
        if(personList != null && !personList.isEmpty()){
            s = personList.size();
        }
        return s;
    }
    
    private void setupQueryInfrastructure(){
        SearchCoordinator sc = getSearchCoordinator();

        try {
//              queryList = getSessionBean().getQueryPersonList();
            queryList = sc.buildQueryPersonList(getSessionBean().getSessUser().getMyCredential());
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        if(queryList != null && !queryList.isEmpty()){
            querySelected = queryList.get(0);
        }
            
        
        if(querySelected != null && querySelected.getPrimaryParams() != null){
            paramsSelected = querySelected.getPrimaryParams();
        }
    
    }
        
    
    /**
     * Listener method for changes to the query drop down box
     */
    public void changeQuerySelected(){
        
        if(querySelected != null && querySelected.getPrimaryParams() != null){
            paramsSelected = querySelected.getPrimaryParams();
        }
    
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "New query loaded!", ""));

        
    }
    
    public void executeQuery(ActionEvent event){
        PersonCoordinator pc = getPersonCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        try {
            List<Person> pl = pc.assemblePersonListFromHumanList(sc.runQuery(querySelected).getBOBResultList());
            
            if(!isAppendResultsToList()){
                personList.clear();
            } 
            personList.addAll(pl);
            queryLog = querySelected.getQueryLog();
            
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
    
    
    public void resetCurrentQuery(ActionEvent ev){
        setupQueryInfrastructure();
        System.out.println("PersonSearchBB.resetCurrentQuery ");
//        querySelected = sc.initQuery(QueryPersonEnum.valueOf(querySelected.getQueryName().toString()),
//                getSessionBean().getSessUser().getMyCredential());
        
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Query list rebuilt; filters reset", ""));
        
        queryLog = null;
        
    }
    
    
    
        
    /**
     * Action (i.e. navigation) method for jumping into a Person from search
     * @param p
     
     */
    public void explorePerson(Person p){
        SystemIntegrator si = getSystemIntegrator();
        PersonCoordinator pc = getPersonCoordinator();
        try {
            getSessionBean().setSessPerson(pc.assemblePersonDataHeavy(p, getSessionBean().getSessUser().getMyCredential()));
            currentPerson = pc.assemblePersonDataHeavy(p, getSessionBean().getSessUser().getKeyCard());
            si.logObjectView_OverwriteDate(getSessionBean().getSessUser(), p);
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Person history logging is broken!",""));
        }
    }

    
    /**
     * TODO Adapt to humanization
     * 
     */
    public void loadPersonHistory(){
        PersonCoordinator pc = getPersonCoordinator();
        
        
    }
    
      
    public void personEditInit(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        // keep a stashed copy of our oroginal field values to write to notes
        // on a succesful update
        // TODO: create way to track changes in humanization schema

        
        
    }
    
    /**
     * Listener method for person updates
     * Writes field dump to notes
     *  
     * @return  
     */
    public String personEditCommit(){
        PersonCoordinator pc = getPersonCoordinator();
        SystemCoordinator sc = getSystemCoordinator();

        try {
            pc.humanEdit(currentPerson, getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_INFO, 
                     "Edits of Person saved to database!", ""));
            // with a successful update, write field dump of previous values to person notes
            pc.addNotesToPerson(currentPerson, getSessionBean().getSessUser(), fieldDump);
            // refresh our current person
            refreshCurrentPerson();
            sc.logObjectView(getSessionBean().getSessUser(), currentPerson);
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                     "Edits failed on person due to a database bug!", ""));
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                 new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                     ex.getMessage(), ""));
            
        }
        onPageLoad_viewCurrentPersonProfile = true;
        getSessionBean().setOnPageLoad_sessionSwitch_viewProfile(onPageLoad_viewCurrentPersonProfile);
        return "personSearch";
    }
    
    
     public String viewPersonAssociatedProperty(Property p){
         getSessionBean().setSessProperty(p);
        return "propertyInfo";
    }
    
    
     /**
      * Listener for user requests to start the person creation process
      * Builds a skeleton person
      * @param ev 
      */
    public void personCreateInit(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            currentPerson = pc.assemblePersonDataHeavy(
                    pc.createPersonSkeleton(getSessionBean().getSessUser().getMyCredential().getGoverningAuthPeriod().getMuni()),
                    getSessionBean().getSessUser().getKeyCard());
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        }
    }
    
    /**
     * Action listener for creation of new person objects
     * @return  
     */
    public String personCreateCommit(){
        PersonCoordinator pc = getPersonCoordinator();
        SystemCoordinator sc = getSystemCoordinator();
    
        try {
            Person p = pc.humanAdd(currentPerson, getSessionBean().getSessUser());
            getSessionBean().setSessPerson(pc.assemblePersonDataHeavy(p,getSessionBean().getSessUser().getKeyCard()));
               if(isConnectToActiveProperty()){
                   
                   Property property = getSessionBean().getSessProperty();
//                   pc.linkHuman();
                   getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Successfully added " + currentPerson.getFirstName() + " to the Database!" 
                                + " and connected to " + property.getAddress(), ""));
               } else {

                   getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                                "Successfully added " + currentPerson.getFirstName() + " to the Database!", ""));
               }
               sc.logObjectView(getSessionBean().getSessUser(), currentPerson);
           } catch (IntegrationException | BObStatusException ex) {
               System.out.println(ex.toString());
                  getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                               "Unable to add new person to the database, my apologies!", ""));
           }
        return "personSearch";
    }
    
    public void refreshCurrentPerson(){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            currentPerson = pc.assemblePersonDataHeavy(currentPerson, getSessionBean().getSessUser().getKeyCard());
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
        }
    }
    
    
    /**
     * listener for user requests to add new note to a person
     * @param ev 
     */
    public void onNoteInit(ActionEvent ev){
        formNotes = "";
        
    }
    
    /**
     * Listener for user requests to complete the note writing process and 
     * attach note to currentPerson
     * @return  nav page
     */
    public String onNoteCommit(){
        PersonCoordinator pc = getPersonCoordinator();
        SystemCoordinator sc = getSystemCoordinator();

        try {
            pc.addNotesToPerson(currentPerson, getSessionBean().getSessUser(), formNotes);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO
                    , "Done: Notes added to person ID:" + currentPerson.getHumanID(),"" ));
            sc.logObjectView(getSessionBean().getSessUser(), currentPerson);
//            refreshCurrentPerson();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR
                    , "Unable to update notes, sorry!"
                    , getResourceBundle(Constants.MESSAGE_TEXT).getString("systemLevelError")));
        } catch (BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR
                    , ex.getMessage()
                    , ""));
            
        }
        onPageLoad_viewCurrentPersonProfile = true;
        getSessionBean().setOnPageLoad_sessionSwitch_viewProfile(onPageLoad_viewCurrentPersonProfile);
        return "personSearch";
        
    }
    
 
    
    
    /**
     * @return the currentPerson
     */
    public PersonDataHeavy getCurrentPerson() {
        return currentPerson;
    }

    /**
     * @param currentPerson the currentPerson to set
     */
    public void setCurrentPerson(PersonDataHeavy currentPerson) {
        this.currentPerson = currentPerson;
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
     * @return the paramsSelected
     */
    public SearchParamsPerson getParamsSelected() {
        return paramsSelected;
    }

    /**
     * @param paramsSelected the paramsSelected to set
     */
    public void setParamsSelected(SearchParamsPerson paramsSelected) {
        this.paramsSelected = paramsSelected;
    }

    /**
     * @return the appendResultsToList
     */
    public boolean isAppendResultsToList() {
        return appendResultsToList;
    }

    /**
     * @param appendResultsToList the appendResultsToList to set
     */
    public void setAppendResultsToList(boolean appendResultsToList) {
        this.appendResultsToList = appendResultsToList;
    }

    /**
     * @return the queryLog
     */
    public String getQueryLog() {
        return queryLog;
    }

    /**
     * @param queryLog the queryLog to set
     */
    public void setQueryLog(String queryLog) {
        this.queryLog = queryLog;
    }

    
    /**
     * @return the connectToActiveProperty
     */
    public boolean isConnectToActiveProperty() {
        return connectToActiveProperty;
    }

    /**
     * @return the fieldDump
     */
    public String getFieldDump() {
        return fieldDump;
    }

    /**
     * @return the formNotes
     */
    public String getFormNotes() {
        return formNotes;
    }

    /**
     * @param connectToActiveProperty the connectToActiveProperty to set
     */
    public void setConnectToActiveProperty(boolean connectToActiveProperty) {
        this.connectToActiveProperty = connectToActiveProperty;
    }

    /**
     * @param fieldDump the fieldDump to set
     */
    public void setFieldDump(String fieldDump) {
        this.fieldDump = fieldDump;
    }

    /**
     * @param formNotes the formNotes to set
     */
    public void setFormNotes(String formNotes) {
        this.formNotes = formNotes;
    }

    /**
     * @return the onPageLoad_viewCurrentPersonProfile
     */
    public boolean isOnPageLoad_viewCurrentPersonProfile() {
        return onPageLoad_viewCurrentPersonProfile;
    }

    /**
     * @return the onPageLoad_editCurrentPerson
     */
    public boolean isOnPageLoad_editCurrentPerson() {
        return onPageLoad_editCurrentPerson;
    }

    /**
     * @param onPageLoad_viewCurrentPersonProfile the onPageLoad_viewCurrentPersonProfile to set
     */
    public void setOnPageLoad_viewCurrentPersonProfile(boolean onPageLoad_viewCurrentPersonProfile) {
        this.onPageLoad_viewCurrentPersonProfile = onPageLoad_viewCurrentPersonProfile;
    }

    /**
     * @param onPageLoad_editCurrentPerson the onPageLoad_editCurrentPerson to set
     */
    public void setOnPageLoad_editCurrentPerson(boolean onPageLoad_editCurrentPerson) {
        this.onPageLoad_editCurrentPerson = onPageLoad_editCurrentPerson;
    }
    
    
}
