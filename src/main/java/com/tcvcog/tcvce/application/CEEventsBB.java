/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CECaseBaseClass;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.EventCasePropBundle;
import com.tcvcog.tcvce.entities.ReportConfigCEEventList;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.BOBQuery;
import com.tcvcog.tcvce.entities.search.EventQuery;
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
    
    private List<BOBQuery> queryList;
    private BOBQuery selectedBOBQuery;
    
    private List<EventCasePropBundle> eventList;
    private List<EventCasePropBundle> filteredEventList;
    
    private ReportConfigCEEventList reportConfig;
    
    
    /**
     * Creates a new instance of CEEventsBB
     */
    public CEEventsBB() {
    }
     
    @PostConstruct
    public void initBean(){
        EventCoordinator ec = getEventCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        eventList = getSessionBean().getcEEventWCPIQueue();
        searchParams = ec.getSearchParamsCEEventsRequiringAction(
                getSessionBean().getFacesUser(), getSessionBean().getActiveMuni());
        queryList = sc.getEventQueryList(getSessionBean().getFacesUser(), getSessionBean().getActiveMuni());
        reportConfig = ec.getDefaultReportConfigCEEventList();
        reportConfig.setMuni(getSessionBean().getActiveMuni());
        reportConfig.setCreator(getSessionBean().getFacesUser());
        
        
    }
    
    public void hideEvent(EventCasePropBundle ev){
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
                                        getSessionBean().getFacesUser(), 
                                        getSessionBean().getUserAuthMuniList());
            
            generateQueryResultMessage();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        } catch (CaseLifecyleException ex) {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Case lifecycle exception.", ""));
        }
    }
    
    public void executeQuery(){
        System.out.println("CEEventsBB.executeQuery");
        EventCoordinator ec = getEventCoordinator();
        EventQuery eq = (EventQuery) selectedBOBQuery;
        searchParams = eq.getEventSearchParams();
        try {
            eventList = ec.queryEvents( eq.getEventSearchParams(), 
                                        getSessionBean().getFacesUser(), 
                                        getSessionBean().getUserAuthMuniList());
            
            generateQueryResultMessage();
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Could not query the database, sorry.", ""));
        } catch (CaseLifecyleException ex) {
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
    
    public String editEventInCaseManager(EventCasePropBundle ev){
        CaseIntegrator ci = getCaseIntegrator();
        CECaseBaseClass caseNoLists = ev.getEventCaseBare();
        try {
            getSessionBean().getcECaseQueue().add(0, ci.generateCECase(caseNoLists));
        } catch (SQLException ex) {
            System.out.println(ex);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        return "ceCases";
    }

    /**
     * @return the searchParams
     */
    public SearchParamsCEEvents getSearchParams() {
        return searchParams;
    }
    
   
    /**
     * Not used
     * @param searchParams the searchParams to set
     */
    public void setSearchParams(SearchParamsCEEvents searchParams) {
        this.searchParams = searchParams;
    }
    
   public void prepareEventReport(){
       if(selectedBOBQuery != null){
            reportConfig.setTitle(selectedBOBQuery.getQueryTitle());
       }
   }
   
   public String generateEventReport(){
       // put the current event list on the session bean for extraction when
       // we generate the report (and must reload the backing bean)
       getSessionBean().setcEEventWCPIQueue(eventList);
       getSessionBean().setReportConfigCEEventList(reportConfig);
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

    /**
     * @return the selectedBOBQuery
     */
    public BOBQuery getSelectedBOBQuery() {
        return selectedBOBQuery;
    }

    /**
     * @param selectedBOBQuery the selectedBOBQuery to set
     */
    public void setSelectedBOBQuery(BOBQuery selectedBOBQuery) {
        EventQuery eq = (EventQuery) selectedBOBQuery;
        searchParams = eq.getEventSearchParams();
        this.selectedBOBQuery = selectedBOBQuery;
    }

    /**
     * @return the queryList
     */
    public List<BOBQuery> getQueryList() {
        return queryList;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<BOBQuery> queryList) {
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
