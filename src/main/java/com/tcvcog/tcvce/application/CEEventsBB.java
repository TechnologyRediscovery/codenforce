/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.EventWithCasePropInfo;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParamsCEEvents;
import java.io.Serializable;
import java.util.ArrayList;
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
        CaseCoordinator cc = getCaseCoordinator();
        searchParams = cc.getDefaultSearchParamsCEEventsRequiringView(
                getSessionBean().getFacesUser().getUserID());
        
    }
    
    public void executeQuery(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        int listSize = 0;
        try {
            eventList = ec.queryEvents(searchParams);
            if(eventList != null){
                listSize = eventList.size();
            }
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Your search completed with " + listSize + "results", ""));
        } catch (IntegrationException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
            
        }
        
    }

    /**
     * @return the searchParams
     */
    public SearchParamsCEEvents getSearchParams() {
        return searchParams;
    }

    /**
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsCEEvents searchParams) {
        this.searchParams = searchParams;
    }

    

   
    /**
     * @return the eventTypesList
     */
    public List<EventType> getEventTypesList() {
        return eventTypesList;
    }

    /**
     * @return the eventCatList
     */
    public List<EventCategory> getEventCatList() {
        return eventCatList;
    }

    /**
     * @return the userList
     */
    public List<User> getUserList() {
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
