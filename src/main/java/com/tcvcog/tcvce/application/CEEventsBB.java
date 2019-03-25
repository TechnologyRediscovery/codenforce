/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.EventWithCasePropInfo;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParamsCEEvents;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
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
    private List<EventType> eventTypesList;
    private List<EventCategory> eventCatList;
    private List<User> userList;
    
    private List<EventWithCasePropInfo> eventList;
    private List<EventWithCasePropInfo> filteredEventList;
    
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
            eventList = ec.queryEvents(searchParams, getSessionBean().getFacesUser());
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
            eventList = ec.queryEvents(searchParams, getSessionBean().getFacesUser());
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
            eventList = ec.queryEvents(searchParams, getSessionBean().getFacesUser());
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
            eventList = ec.queryEvents(searchParams, getSessionBean().getFacesUser());
            generateQueryResultMessage();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        }
        
    }

    /**
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsCEEvents searchParams) {
        this.searchParams = searchParams;
    }
    
    public void refreshCurrentEventList() {
        EventIntegrator ei = getEventIntegrator();
        EventCoordinator ec = getEventCoordinator();
        EventWithCasePropInfo e;
        List<EventWithCasePropInfo> refreshedList = new ArrayList<>();
        Iterator<EventWithCasePropInfo> iter = eventList.iterator();
        try {
            while (iter.hasNext()) {
                e = iter.next();
                e = ei.getEventWithCaseAndPropInfo(e.getEventID());
                e.setCurrentUserCanTakeAction(
                        ec.determineUserActionRequestEventAuthorization(e, getSessionBean().getFacesUser()));
                refreshedList.add(e);
            }
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not refresh event list, sorry!", ""));
            
        }
        eventList = refreshedList;
    }

    public void logActionResponse(EventWithCasePropInfo ev){
        EventCoordinator ec = getEventCoordinator();
        try {
            ev.setResponderActual(getSessionBean().getFacesUser());
            ec.logResponseToActionRequest(ev);
            refreshCurrentEventList();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Registered view confirmation!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not confirm view, sorry.", ""));
        }
    }
    
    public void clearActionResponse(EventWithCasePropInfo ev){
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.clearActionResponse(ev);
            refreshCurrentEventList();
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Action response: cleared!", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not clear action response, sorry.", ""));
        }
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
    public List<EventWithCasePropInfo> getEventList() {
        
        return eventList;
    }

    /**
     * @return the filteredEventList
     */
    public List<EventWithCasePropInfo> getFilteredEventList() {
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
    public void setEventList(List<EventWithCasePropInfo> eventList) {
        this.eventList = eventList;
    }

    /**
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(List<EventWithCasePropInfo> filteredEventList) {
        this.filteredEventList = filteredEventList;
    }
    
    
}
