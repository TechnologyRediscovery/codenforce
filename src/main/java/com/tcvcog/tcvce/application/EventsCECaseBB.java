/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.UserCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECaseDataHeavy;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.EventCnFCasePropBundle;
import com.tcvcog.tcvce.entities.reports.ReportConfigCEEventList;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import com.tcvcog.tcvce.entities.search.QueryEvent;
import com.tcvcog.tcvce.entities.search.SearchParamsEvent;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
public class EventsCECaseBB extends BackingBeanUtils implements Serializable {

    
    private SearchParamsEvent searchParams;
    
    private int actionRequestsUserType;
    
    private List<EventType> eventTypesList;
    private List<EventCategory> eventCatList;
    
    private List<UserAuthorized> userList;
    
    private List<QueryEvent> queryList;
    private Query selectedBOBQuery;
    
    private List<EventCnFCasePropBundle> eventList;
   
    private List<EventCnFCasePropBundle> filteredEventList;
    
    private ReportConfigCEEventList reportConfig;
    
    
    /**
     * Creates a new instance of CEEventsBB
     */
    public EventsCECaseBB() {
    }
     
    @PostConstruct
    public void initBean(){
        SearchCoordinator sc = getSearchCoordinator();
        UserCoordinator uc = getUserCoordinator();
        
        
        queryList = sc.buildQueryEventCnFList(getSessionBean().getSessionMuni(),getSessionBean().getSessionUser());
        selectedBOBQuery = sc.getQueryInitialEventCnF(getSessionBean().getSessionMuni(), getSessionBean().getSessionUser());
        if(!selectedBOBQuery.isExecutedByIntegrator()){
            try {
                sc.runQuery((QueryEvent) selectedBOBQuery);
            } catch (IntegrationException| BObStatusException ex) {
                System.out.println(ex);
            }
            
        }
        eventList = selectedBOBQuery.getBOBResultList();
        
        searchParams = (SearchParamsEvent) selectedBOBQuery.getParmsList().get(0);
        // grab previously loaded event config from the session bean
        // which would have been placed there by the generateReport method in this bean
        reportConfig = getSessionBean().getReportConfigCEEventList();
//        try {
//            userList = uc.getUserAuthorizedListForConfig(getSessionBean().getSessionMuni());
            
        // **************************************************************
        // *****FIX ME!!!*********************************************************
        // **************************************************************
            userList = new ArrayList<>();
//        } catch (IntegrationException ex) {
//            System.out.println(ex);
//        } catch (AuthorizationException ex) {
//            System.out.println(ex);
//        }
    }
    
    public void hideEvent(EventCnFCasePropBundle ev){
        EventIntegrator ei = getEventIntegrator();
        try {
            ei.updateEvent(ev.getEvent());
            eventList.remove(ev);
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not hide event ID " + ev.getEvent().getEventID(), ""));
            
        }
    }
    
    public void executeManualQuery(){
        EventCoordinator ec = getEventCoordinator();
        try {
            eventList = ec.queryEvents( searchParams, 
                                        getSessionBean().getSessionUser());
            if(eventList != null){
                Collections.sort(eventList);
                Collections.reverse(eventList);
            }
            
            generateQueryResultMessage();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Case lifecycle exception.", ""));
        }
    }
    
    public void executeQuery(){
        System.out.println("CEEventsBB.executeQuery");
        EventCoordinator ec = getEventCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        QueryEvent eventQuery = (QueryEvent) selectedBOBQuery;
        searchParams = (SearchParamsEvent) eventQuery.getParamsList().get(0);
        try {
            eventList = sc.runQuery(eventQuery).getBOBResultList();
            if(eventList != null){
                Collections.sort(eventList);
                Collections.reverse(eventList);
            }
            
            generateQueryResultMessage();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        } catch (BObStatusException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Case lifecycle exception.", ""));
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
    
    public String editEventInCaseManager(EventCnFCasePropBundle ev){
        CaseIntegrator ci = getCaseIntegrator();
        CECase caseNoLists = ev.getEventCaseBare();
//        try {
//            getSessionBean().getSessionCECaseList().add(0, ci.generateCECase(caseNoLists));
//        } catch (SQLException ex) {
//            System.out.println(ex);
//        } catch (IntegrationException ex) {
//            System.out.println(ex);
//        }
        
        return "ceCases";
    }

    /**
     * @return the searchParams
     */
    public SearchParamsEvent getSearchParams() {
        return searchParams;
    }
    
   
    /**
     * Not used
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsEvent searchParams) {
        this.searchParams = searchParams;
    }
    
   public void prepareEventReport(){
       if(eventList != null && eventList.size() > 0){
           
            EventCoordinator ec = getEventCoordinator();
            reportConfig = ec.getDefaultReportConfigCEEventList();
            reportConfig.setMuni(getSessionBean().getSessionMuni());
            reportConfig.setCreator(getSessionBean().getSessionUser());
            if(selectedBOBQuery != null){
                 reportConfig.setTitle(selectedBOBQuery.getQueryTitle());
            }
//            reportConfig.setQueryParams(searchParams);
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
       
       if(!selectedBOBQuery.isExecutedByIntegrator()){
            try {
                selectedBOBQuery = sc.runQuery((QueryEvent) selectedBOBQuery);
            } catch (IntegrationException| BObStatusException ex) {
                System.out.println(ex);
            }
            
        }
       reportConfig.setBOBQuery(selectedBOBQuery);
       getSessionBean().setSessionEventWithCasePropList(eventList);
       getSessionBean().setReportConfigCEEventList(reportConfig);
       getSessionBean().setSessionReport(reportConfig);
       return "reportCEEventList";
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
    public List<UserAuthorized> getUserList() {
        
        return userList;
    }

    /**
     * @return the eventList
     */
    public List<EventCnFCasePropBundle> getEventList() {
        
        return eventList;
    }

    /**
     * @return the filteredEventList
     */
    public List<EventCnFCasePropBundle> getFilteredEventList() {
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
    public void setUserList(List<UserAuthorized> userList) {
        this.userList = userList;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<EventCnFCasePropBundle> eventList) {
        this.eventList = eventList;
    }

    /**
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(List<EventCnFCasePropBundle> filteredEventList) {
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

    /**
     * @return the selectedBOBQuery
     */
    public Query getSelectedBOBQuery() {
        return selectedBOBQuery;
    }

    /**
     * @param selectedBOBQuery the selectedBOBQuery to set
     */
    public void setSelectedBOBQuery(Query selectedBOBQuery) {
        QueryEvent eq = (QueryEvent) selectedBOBQuery;
        searchParams = (SearchParamsEvent) eq.getParamsList().get(0);
        this.selectedBOBQuery = selectedBOBQuery;
    }

    /**
     * @return the queryList
     */
    public List<QueryEvent> getQueryList() {
        return queryList;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryEvent> queryList) {
        this.queryList = queryList;
    }

    /**
     * @return the reportConfig
     */
    public ReportConfigCEEventList getReportConfig() {
        return reportConfig;
    }

    /**
     * @param reportConfig the reportConfig to set
     */
    public void setReportConfig(ReportConfigCEEventList reportConfig) {
        this.reportConfig = reportConfig;
    }

    
}
