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
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonDataHeavy;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import com.tcvcog.tcvce.integration.SystemIntegrator;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author Ellen Bascomb
 */
public class PersonSearchBB extends BackingBeanUtils{

    private PersonDataHeavy currPerson;
    
    private QueryPerson querySelected;
    private List<QueryPerson> queryList;
    private SearchParamsPerson paramsSelected;
    private String queryLog;

    private List<Person> personList;
    private List<Person> filteredPersonList;
    private boolean appendResultsToList;
    
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
       
       if(getSessionBean().getSessPersonQueued() != null){
            currPerson = pc.assemblePersonDataHeavy(getSessionBean().getSessPersonQueued(), 
                    getSessionBean().getSessUser().getKeyCard());
             getSessionBean().setSessPerson(currPerson);
            getSessionBean().setSessPersonQueued(null);
       } else {
            currPerson = (getSessionBean().getSessPerson());
       }
       
       personList = getSessionBean().getSessPersonList();
       personTypes = PersonType.values();
       
       setupQueryInfrastructure();
       
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
        
    public void changeQuerySelected(){
        
        if(querySelected != null && querySelected.getPrimaryParams() != null){
            paramsSelected = querySelected.getPrimaryParams();
        }
    
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "New query loaded!", ""));

        
    }
    
    public void executeQuery(ActionEvent event){
        
        SearchCoordinator sc = getSearchCoordinator();
        try {
            List<Person> pl = sc.runQuery(querySelected).getBOBResultList();
            if(!isAppendResultsToList()){
                personList.clear();
            } 
            personList.addAll(pl);
            queryLog = querySelected.getQueryLog();
            
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Your search completed with " + pl.size() + " results", ""));
            
        } catch (SearchException ex) {
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
     * @return 
     */
    public String explorePerson(Person p){
        SystemIntegrator si = getSystemIntegrator();
        PersonCoordinator pc = getPersonCoordinator();
        try {
            getSessionBean().setSessPerson(pc.assemblePersonDataHeavy(p, getSessionBean().getSessUser().getMyCredential()));
            si.logObjectView_OverwriteDate(getSessionBean().getSessUser(), p);
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Person history logging is broken!",""));
        }
        return "personInfo";
    }

    
    public void loadPersonHistory(){
        PersonCoordinator pc = getPersonCoordinator();
        try {
            personList = pc.assemblePersonHistory(getSessionBean().getSessUser().getMyCredential());
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
    
    /**
     * @return the currPerson
     */
    public PersonDataHeavy getCurrPerson() {
        return currPerson;
    }

    /**
     * @param currPerson the currPerson to set
     */
    public void setCurrPerson(PersonDataHeavy currPerson) {
        this.currPerson = currPerson;
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
    
    
}
