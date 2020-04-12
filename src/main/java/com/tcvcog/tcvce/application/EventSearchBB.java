/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.entities.reports.ReportConfigCEEventList;
import com.tcvcog.tcvce.entities.search.QueryEvent;
import com.tcvcog.tcvce.entities.search.SearchParamsEvent;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 *
 * @author sylvia
 */
public class EventSearchBB
        extends     BackingBeanUtils
        implements  Serializable {

    
    private List<EventCnFPropUnitCasePeriodHeavy> eventList;
    private List<EventCnFPropUnitCasePeriodHeavy> filteredEventList;
    
    private boolean appendResultsToList;
    
    private List<QueryEvent> queryList;
    private QueryEvent querySelected;
    
    private SearchParamsEvent searchParamsSelected;
    
    private EventCnF selectedEvent;

    private List<EventCategory> eventCategoryList;
    private List<EventType> eventTypeList;
    
    private int actionRequestsUserType;
    
    private ReportConfigCEEventList reportConfig;
    
    private List<PropertyUseType> putList;
    
    
    @PostConstruct
    public void initBean() {
        EventCoordinator ec = getEventCoordinator();
        SessionBean sb = getSessionBean();
        PropertyIntegrator pi = getPropertyIntegrator();
        SearchCoordinator sc = getSearchCoordinator();
       
        queryList = sc.buildQueryEventList(getSessionBean().getSessUser().getMyCredential());
        
        if(queryList != null && !queryList.isEmpty()){
            querySelected = queryList.get(0);
        }
        
        eventList = getSessionBean().getSessEventList();
        if(eventList == null){
            eventList = new ArrayList<>();
        }
        
        eventTypeList = ec.getEventTypesAll();
        try {
            eventCategoryList = ec.getEventCategoryListActive();
            putList = pi.getPropertyUseTypeList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        setAppendResultsToList(false);
        
        
        // grab previously loaded event config from the session bean
        // which would have been placed there by the generateReport method in this bean
        reportConfig = getSessionBean().getReportConfigCEEventList();
        configureParameters();
    }
    /**
     * Creates a new instance of CECaseEventsBB
     */
    public EventSearchBB() {
    }
    
    
    private void configureParameters(){
        if(querySelected != null 
                && 
            querySelected.getParamsList() != null 
                && 
            !querySelected.getParamsList().isEmpty()){
            
            searchParamsSelected = querySelected.getParamsList().get(0);
        } else {
            searchParamsSelected = null;
        }
    }
    
    public int getEventListSize(){
        int s = 0;
        if(eventList != null && !eventList.isEmpty()){
            s = eventList.size();
        }
        return s;
    }
    
    
    public void clearEventList(ActionEvent ev){
        if(eventList != null){
            eventList.clear();
        }
    
    }
    
    public void loadEventHistory(ActionEvent ev){
        eventList = getSessionBean().getSessEventList();
        
    }
    
    
    public String jumpToParentObject(EventCnFPropUnitCasePeriodHeavy ev){
        if(ev != null){
            switch(ev.getDomain()){
                case CODE_ENFORCEMENT:
                    return "ceCaseWorkflow";
                case OCCUPANCY:
                    return "occPeriodWorkflow";
            }
        }
        return "";
        
    }
    
    
    public void rejectRequestedEvent(EventCnF ev) {
        setSelectedEvent(ev);
//        rejectedEventListIndex = currentCase.getEventProposalList().indexOf(ev);
    }
    
    /**
     * Action listener pass-through method for calling executeQuery from the front end
     * @param ev 
     */
    public void runQuery(ActionEvent ev){
        executeQuery();
    }  
    
    public void executeQuery(){
        System.out.println("EventSearchBB.executeQuery");
        SearchCoordinator sc = getSearchCoordinator();
        try {
            if(eventList != null){
                if(!appendResultsToList){
                    eventList.clear();
                }
                eventList = sc.runQuery(querySelected).getBOBResultList();
                    Collections.sort(eventList);
                    Collections.reverse(eventList);
                }
            
            generateQueryResultMessage();
        } catch (SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        }
    }
    
    public void resetQuery(ActionEvent ev){
        
        SearchCoordinator sc = getSearchCoordinator();
        queryList = sc.buildQueryEventList(getSessionBean().getSessUser().getMyCredential());
        if(queryList != null && !queryList.isEmpty()){
            querySelected = queryList.get(0);
        }
        
    }
    
    public void changeQuerySelected(){
        System.out.println("EventSearchBB.changeQuerySelected()");
        configureParameters();
        getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "New query loaded!", ""));
        
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
    
    
   public void prepareEventReport(){
       if(eventList != null && eventList.size() > 0){
           
            EventCoordinator ec = getEventCoordinator();
            reportConfig = ec.getDefaultReportConfigCEEventList();
            reportConfig.setMuni(getSessionBean().getSessMuni());
            reportConfig.setCreator(getSessionBean().getSessUser());
            if(querySelected != null){
                 reportConfig.setTitle(querySelected.getQueryTitle());
            }
//            reportConfig.setQueryParams(searchParamsSelected);
       } else {
           getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Yikes! No events; You may only generate a report for an event list of size 1 or greater. "
                                    + "Please revise your query.", ""));
       }
   }
   
   public String generateEventReport(){
       SearchCoordinator sc = getSearchCoordinator();
       // put the current event list on the session bean for extraction when
       // we generate the report (and must reload the backing bean)
       if(eventList != null){
            Collections.sort(eventList);
            if(reportConfig.isSortInRevChrono()){
                Collections.reverse(eventList);
            } 
       }
       
       if(querySelected.getExecutionTimestamp() == null){
            
            
        }
       reportConfig.setBOBQuery(querySelected);
//       getSessionBean().setSessionEventWithCasePropList(eventList);

       getSessionBean().setReportConfigCEEventList(reportConfig);
       getSessionBean().setSessReport(reportConfig);
       return "reportCEEventList";
   }

   
    /**
     * @return the eventTypesList
     */
    public List<EventType> getEventTypesList() {
        
        return eventTypeList;
    }

    /**
     * @return the eventCatList
     */
    public List<EventCategory> getEventCatList() {
        return eventCategoryList;
    }
    
    
    public void hideEvent(EventCnF event){
        EventIntegrator ei = getEventIntegrator();
        event.setHidden(true);
        try {
            ei.updateEvent(event);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Success! event ID: " + event.getEventID() + " is now hidden", ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not hide event, sorry; this is a system erro", ""));
        }
    }
    
    public void unHideEvent(EventCnF event){
        EventIntegrator ei = getEventIntegrator();
        event.setHidden(false);
        try {
            ei.updateEvent(event);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_INFO,
                           "Success! Unhid event ID: " + event.getEventID(), ""));
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                   new FacesMessage(FacesMessage.SEVERITY_ERROR,
                           "Could not unhide event, sorry; this is a system erro", ""));
        }
    }
    
    
 

    /**
     * @return the filteredEventList
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getFilteredEventList() {
        return filteredEventList;
    }

    /**
     * @return the selectedEvent
     */
    public EventCnF getSelectedEvent() {
        return selectedEvent;
    }

  

    /**
     * @return the eventCategoryList
     */
    public List<EventCategory> getEventCategoryList() {
       
        return eventCategoryList;
    }

 

    /**
     * @return the eventTypeList
     */
    public List<EventType> getEventTypeList() {
        
        return eventTypeList;
    }

   

    /**
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(List<EventCnFPropUnitCasePeriodHeavy> filteredEventList) {
        this.filteredEventList = filteredEventList;
    }

    /**
     * @param selectedEvent the selectedEvent to set
     */
    public void setSelectedEvent(EventCnF selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

  

    /**
     * @param eventCategoryList the eventCategoryList to set
     */
    public void setEventCategoryList(List<EventCategory> eventCategoryList) {
        this.eventCategoryList = eventCategoryList;
    }

   
    /**
     * @param eventTypeList the eventTypeList to set
     */
    public void setEventTypeList(List<EventType> eventTypeList) {
        this.eventTypeList = eventTypeList;
    }

    

    /**
     * @return the searchParamsSelected
     */
    public SearchParamsEvent getSearchParamsSelected() {
        return searchParamsSelected;
    }

    /**
     * @return the actionRequestsUserType
     */
    public int getActionRequestsUserType() {
        return actionRequestsUserType;
    }


    /**
     * @return the queryList
     */
    public List<QueryEvent> getQueryList() {
        return queryList;
    }

  
    /**
     * @return the eventList
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getEventList() {
        return eventList;
    }

    /**
     * @return the reportConfig
     */
    public ReportConfigCEEventList getReportConfig() {
        return reportConfig;
    }

    /**
     * @param searchParamsSelected the searchParamsSelected to set
     */
    public void setSearchParamsSelected(SearchParamsEvent searchParamsSelected) {
        this.searchParamsSelected = searchParamsSelected;
    }

    /**
     * @param actionRequestsUserType the actionRequestsUserType to set
     */
    public void setActionRequestsUserType(int actionRequestsUserType) {
        this.actionRequestsUserType = actionRequestsUserType;
    }

   

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryEvent> queryList) {
        this.queryList = queryList;
    }

  

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<EventCnFPropUnitCasePeriodHeavy> eventList) {
        this.eventList = eventList;
    }

    /**
     * @param reportConfig the reportConfig to set
     */
    public void setReportConfig(ReportConfigCEEventList reportConfig) {
        this.reportConfig = reportConfig;
    }

    /**
     * @return the querySelected
     */
    public QueryEvent getQuerySelected() {
        return querySelected;
    }

    /**
     * @param querySelected the querySelected to set
     */
    public void setQuerySelected(QueryEvent querySelected) {
        this.querySelected = querySelected;
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
     * @return the putList
     */
    public List<PropertyUseType> getPutList() {
        return putList;
    }

    /**
     * @param putList the putList to set
     */
    public void setPutList(List<PropertyUseType> putList) {
        this.putList = putList;
    }
    
}
