/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseNoLists;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.EventCasePropBundle;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParamsCEEvents;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class CEEventsBB extends BackingBeanUtils implements Serializable {

    
    private SearchParamsCEEvents searchParams;
    private int actionRequestsUserType;
    private List<EventType> eventTypesList;
    private List<EventCategory> eventCatList;
    private List<User> userList;
    
    private List<EventCasePropBundle> eventList;
    private List<EventCasePropBundle> filteredEventList;
    
    /**
     * Creates a new instance of CEEventsBB
     */
    public CEEventsBB() {
    }
     
    @PostConstruct
    public void initBean(){
        EventCoordinator ec = getEventCoordinator();
        searchParams = ec.getSearchParamsCEEventsRequiringAction(
                getSessionBean().getFacesUser(), getSessionBean().getActiveMuni());
    }
    
    public void executeQuery(){
        System.out.println("CEEventsBB.executeQuery");
        EventCoordinator ec = getEventCoordinator();
        try {
            eventList = ec.queryEvents(searchParams, 
                        getSessionBean().getFacesUser(), 
                        getSessionBean().getUserAuthMuniList());
            generateQueryResultMessage();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        }
    }
    
    private void generateQueryResultMessage(){
        int listSize = 0;
        if(eventList != null){
            listSize = eventList.size();
        }
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Your query completed with " + listSize + " results", ""));
        
        
    }
    
    public String editEventInCaseManager(EventCasePropBundle ev){
        CaseIntegrator ci = getCaseIntegrator();
        CECase c = getSessionBean().getcECaseQueue().remove(0);
        CECaseNoLists caseNoLists = ev.getEventCaseBare();
        try {
            getSessionBean().getcECaseQueue().set(0, ci.generateCECase(caseNoLists));
        } catch (SQLException ex) {
            System.out.println(ex);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        getSessionBean().getcECaseQueue().add(c);
        
        return "ceCases";
    }

    /**
     * @return the searchParams
     */
    public SearchParamsCEEvents getSearchParams() {
        return searchParams;
    }
    
    public void loadEventsRequiringAction(ActionEvent ev){
        System.out.println("CEEventsBB.loadOfficerActivity");
        EventCoordinator ec = getEventCoordinator();
        searchParams = ec.getSearchParamsCEEventsRequiringAction(
                getSessionBean().getFacesUser(), getSessionBean().getActiveMuni());
        try {
            eventList = ec.queryEvents(searchParams, 
                        getSessionBean().getFacesUser(), 
                        getSessionBean().getUserAuthMuniList());
            generateQueryResultMessage();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        }
    }
    
    
    public void loadOfficerActivityPastWeek(ActionEvent ev){
        System.out.println("CEEventsBB.loadOfficerActivity");
        EventCoordinator ec = getEventCoordinator();
        searchParams = ec.getSearchParamsOfficerActibityPastWeek(getSessionBean().getFacesUser(),
                getSessionBean().getActiveMuni());
        try {
            eventList = ec.queryEvents(searchParams, 
                        getSessionBean().getFacesUser(), 
                        getSessionBean().getUserAuthMuniList());
            generateQueryResultMessage();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        }
    }
    
    public void loadComplianceEventsPastMonth(ActionEvent ev){
        System.out.println("CEEventsBB.loadComplianceEvents");
        EventCoordinator ec = getEventCoordinator();
        searchParams = ec.getSearchParamsComplianceEvPastMonth(getSessionBean().getActiveMuni());
        try {
            eventList = ec.queryEvents(searchParams, 
                        getSessionBean().getFacesUser(), 
                        getSessionBean().getUserAuthMuniList());
            generateQueryResultMessage();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        }
        
    }

    /**
     * Not used
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsCEEvents searchParams) {
        this.searchParams = searchParams;
    }
    
    public void refreshCurrentEventList() {
        EventIntegrator ei = getEventIntegrator();
        EventCoordinator ec = getEventCoordinator();
        EventCasePropBundle e;
        List<EventCasePropBundle> refreshedList = new ArrayList<>();
        Iterator<EventCasePropBundle> iter = eventList.iterator();
        try {
            while (iter.hasNext()) {
                e = iter.next();
                e = ei.getEventCasePropBundle(e.getEvent().getEventID());
                refreshedList.add(e);
            }
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not refresh event list, sorry!", ""));
            
        }
        eventList = refreshedList;
    }

   
    /**
     * @return the eventTypesList
     */
    public List<EventType> getEventTypesList() {
        EventType[] evTypeList = EventType.values();
        List<EventType> l = new ArrayList<>();
        for(int i = 0; i <evTypeList.length ; i++){
            l.add(evTypeList[i]);
        }
        eventTypesList = l;
        return eventTypesList;
    }

    /**
     * @return the eventCatList
     */
    public List<EventCategory> getEventCatList() {
         EventIntegrator ei = getEventIntegrator();
        
        if(searchParams.getEvtType() != null){
            
            try {
                eventCatList = ei.getEventCategoryList(searchParams.getEvtType() );
            } catch (IntegrationException ex) {
                // do nothing
            }
        }
        return eventCatList;
    }

    /**
     * @return the userList
     */
    public List<User> getUserList() {
        UserIntegrator ui = getUserIntegrator();
        try {
            userList = ui.getCompleteActiveUserList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return userList;
    }

    /**
     * @return the eventList
     */
    public List<EventCasePropBundle> getEventList() {
        
        return eventList;
    }

    /**
     * @return the filteredEventList
     */
    public List<EventCasePropBundle> getFilteredEventList() {
        return filteredEventList;
    }

    /**
     * @param eventTypesList the eventTypesList to set
     */
    public void setEventTypesList(List<EventType> eventTypesList) {
        this.eventTypesList = eventTypesList;
    }

    /**
     * @param eventCatList the eventCatList to set
     */
    public void setEventCatList(List<EventCategory> eventCatList) {
        this.eventCatList = eventCatList;
    }

    /**
     * @param userList the userList to set
     */
    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<EventCasePropBundle> eventList) {
        this.eventList = eventList;
    }

    /**
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(List<EventCasePropBundle> filteredEventList) {
        this.filteredEventList = filteredEventList;
    }

    /**
     * @return the actionRequestsUserType
     */
    public int getActionRequestsUserType() {
        return actionRequestsUserType;
    }

    /**
     * @param actionRequestsUserType the actionRequestsUserType to set
     */
    public void setActionRequestsUserType(int actionRequestsUserType) {
        this.actionRequestsUserType = actionRequestsUserType;
    }
    
    
}
