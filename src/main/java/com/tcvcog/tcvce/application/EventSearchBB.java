/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.coordinators.SystemCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.reports.ReportConfigCEEventList;
import com.tcvcog.tcvce.entities.search.QueryEvent;
import com.tcvcog.tcvce.entities.search.SearchParamsEvent;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * Backs event activity search
 * @author sylvia
 */
public class EventSearchBB extends BackingBeanUtils{

    private QueryEvent querySelected;
    private List<QueryEvent> queryList;
    private SearchParamsEvent searchParamsSelected;
    private boolean appendResultsToList;
    
    private EventCnFPropUnitCasePeriodHeavy currentEvent;
    
    private List<EventCnFPropUnitCasePeriodHeavy> eventListRaw;
    private List<EventCnFPropUnitCasePeriodHeavy> eventListManaged;
    private List<EventCnFPropUnitCasePeriodHeavy> eventListFiltered;
    private List<ViewOptionsActiveHiddenListsEnum> eventViewList;
    private ViewOptionsActiveHiddenListsEnum eventViewSelected;
    
    private ReportConfigCEEventList reportConfig;
    
    private List<EventType> eventTypeList;
    private List<EventCategory> eventCategoryList;
    
    /**
     * Creates a new instance of EventSearchBB
     */
    public EventSearchBB() {
    }
    
     @PostConstruct
    public void initBean(){
        SearchCoordinator sc = getSearchCoordinator();
        EventCoordinator ec = getEventCoordinator();
        queryList = sc.buildQueryEventList(getSessionBean().getSessUser().getMyCredential());
        appendResultsToList = false;
        
          if(querySelected == null && !queryList.isEmpty()){
            querySelected = queryList.get(0);
        }
      
        // setup search
        configureParameters();
        
        eventListRaw = getSessionBean().getSessEventList();
        if(eventListRaw == null){
            eventListRaw = new ArrayList<>();
        }
        eventListManaged = new ArrayList<>();
        manageRawEventList();
        eventListFiltered = new ArrayList<>();
        eventViewList = Arrays.asList(ViewOptionsActiveHiddenListsEnum.values());
        eventTypeList = Arrays.asList(EventType.values());
        eventViewSelected = ViewOptionsActiveHiddenListsEnum.VIEW_ACTIVE_NOTHIDDEN;
        try {
            eventCategoryList = ec.getEventCategoryList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
    }
    
     /**
     * Sets up search parameters for properties
     */
    private void configureParameters(){
        SystemCoordinator sc = getSystemCoordinator();
        if(querySelected != null 
                && 
            querySelected.getParamsList() != null 
                && 
            !querySelected.getParamsList().isEmpty()){
            
            setSearchParamsSelected(querySelected.getParamsList().get(0));
        }
      
    }
    
    public void clearEventList(ActionEvent ev){
        if(eventListManaged != null && !eventListManaged.isEmpty()){
            eventListManaged.clear();
              getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Event List Reset!", ""));
        }
        
    }
    
    
      /**
     * Action listener for the user's request to run the query
     * @param event 
     */
    public void executeQuery(ActionEvent event){
        SearchCoordinator sc = getSearchCoordinator();
        if(querySelected != null){
            System.out.println("EventSearchBB.executeQuery | querySelected: " + querySelected.getQueryTitle());
        }
        List<EventCnFPropUnitCasePeriodHeavy> evList;
        try {
            evList = sc.runQuery(querySelected).getBOBResultList();
            if(!appendResultsToList && eventListManaged != null){
                eventListRaw.clear();
            } 
            if(evList != null && !evList.isEmpty()){
                eventListRaw.addAll(evList);
                getSessionBean().setSessEventList(eventListRaw);
                eventViewSelected = ViewOptionsActiveHiddenListsEnum.VIEW_ALL;
                manageRawEventList();
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Your search completed with " + evList.size() + " results", ""));
            }else {
                getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Your search had no results", ""));
            }
        } catch (SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to complete search! ", ""));
        }
    }
    
    /**
     * listens for user views of a event
     * @param evpucph 
     */
    public void onViewEvent(EventCnFPropUnitCasePeriodHeavy evpucph){
        currentEvent = evpucph;
        getSessionBean().setSessEvent(currentEvent);
    }
    
    /**
     * Listener to go see the property on which an event is attached
     * @param evpucph 
     * @return  page ID for nav
     */
    public String onViewEventProperty(EventCnFPropUnitCasePeriodHeavy evpucph){
        try {
            return getSessionBean().navigateToPageCorrespondingToObject(evpucph.getProperty());
        } catch (BObStatusException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "FATAL ERROR: Unable to navigate to property, sorry! ", ""));
            return "";
        }
        
    }
    
    /**
     * Listener to go see the cecase or 
     * @param evpucph 
     * @return  page ID for navigation
     */
    public String onViewEventHolder(EventCnFPropUnitCasePeriodHeavy evpucph){
         try {
            switch(evpucph.getDomain()){
                case CODE_ENFORCEMENT:
                    return getSessionBean().navigateToPageCorrespondingToObject(evpucph.getCecase());
                case OCCUPANCY:
                    return getSessionBean().navigateToPageCorrespondingToObject(evpucph.getPeriod());
                case UNIVERSAL:
                    getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                               "FATAL ERROR: Unable to navigate to case or file, sorry! ", ""));
                    return "";
                default:
                    getFacesContext().addMessage(null,
                       new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                               "FATAL ERROR: Unable to navigate to case or file, sorry! ", ""));
                    return "";
            }
        } catch (BObStatusException ex) {
            System.out.println(ex);
             getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "FATAL ERROR: Unable to navigate to property, sorry! ", ""));
            return "";
        }
        
    }
    
    /**
     * Listener for the event list view options drop down list change
     */
    public void manageRawEventList(){
        EventCoordinator ec = getEventCoordinator();
        if(eventListManaged != null && eventListRaw != null && !eventListRaw.isEmpty() && eventViewSelected != null){
            System.out.println("EventSearchBB.filterEventList | selected view " + eventViewSelected.name());
            eventListManaged = ec.filterEventPropUnitCasePeriodHeavyList(eventListRaw, eventViewSelected);
        }
    }
    
    /**
     * Listener method for changes in the selected query;
     * Updates search params and UI updates based on this changed value
     */
    public void changeQuerySelected(){
        System.out.println("EventSearchBB.changeQuerySelected | querySelected: " + querySelected.getQueryTitle());
        configureParameters();
        
    }
    
    /**
     * Listener for hides
     * @param evpucph 
     */
    public void onEventHide(EventCnFPropUnitCasePeriodHeavy evpucph){
        evpucph.setHidden(true);
        manageRawEventList();
        
        
    }
    
    /**
     * Listener for unhides
     * @param evpucph 
     */
    public void onEventUnHide(EventCnFPropUnitCasePeriodHeavy evpucph){
        evpucph.setHidden(false);
        manageRawEventList();
        
    }
    
    
    
    /**
     * Event listener for resetting a query after it's run
     * @param event 
     */
    public void resetQuery(ActionEvent event){
        SearchCoordinator sc = getSearchCoordinator();
        //        querySelected = sc.initQuery(querySelected.getQueryName(), getSessionBean().getSessUser().getMyCredential());
        queryList = sc.buildQueryEventList(getSessionBean().getSessUser().getMyCredential());
        if(queryList != null && !queryList.isEmpty()){
            querySelected = queryList.get(0);
        }
        if(appendResultsToList == false){
            if(eventListManaged != null && !eventListManaged.isEmpty()){
                eventListManaged.clear();
            }
        }
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "Query reset ", ""));

     
        configureParameters();
    }
    
    
    /**
     * Starts the report building process
     * @param ev 
     */
    public void prepareEventReport(ActionEvent ev){
        EventCoordinator ec = getEventCoordinator();
        reportConfig = ec.initDefaultReportConfigEventList();
        
        
    }
    
    /**
     * Listener to view the final report
     * @return 
     */
    public String generateEventReport(){
        EventCoordinator ec = getEventCoordinator();
        getSessionBean().setReportConfigEventList(reportConfig);
        
        return "reportEventList";
        
    }
    
    
    // ***************************************************************
    // ******************* GETTERS AND SETTERS ***********************
    // ***************************************************************
    
    
    

    /**
     * @return the querySelected
     */
    public QueryEvent getQuerySelected() {
        return querySelected;
    }

    /**
     * @return the queryList
     */
    public List<QueryEvent> getQueryList() {
        return queryList;
    }

    /**
     * @param querySelected the querySelected to set
     */
    public void setQuerySelected(QueryEvent querySelected) {
        this.querySelected = querySelected;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryEvent> queryList) {
        this.queryList = queryList;
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
     * @return the searchParamsSelected
     */
    public SearchParamsEvent getSearchParamsSelected() {
        return searchParamsSelected;
    }

    /**
     * @param searchParamsSelected the searchParamsSelected to set
     */
    public void setSearchParamsSelected(SearchParamsEvent searchParamsSelected) {
        this.searchParamsSelected = searchParamsSelected;
    }

    /**
     * @return the eventListManaged
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getEventListManaged() {
        return eventListManaged;
    }

    /**
     * @return the eventListFiltered
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getEventListFiltered() {
        return eventListFiltered;
    }

    /**
     * @param eventListManaged the eventListManaged to set
     */
    public void setEventListManaged(List<EventCnFPropUnitCasePeriodHeavy> eventListManaged) {
        this.eventListManaged = eventListManaged;
    }

    /**
     * @param eventListFiltered the eventListFiltered to set
     */
    public void setEventListFiltered(List<EventCnFPropUnitCasePeriodHeavy> eventListFiltered) {
        this.eventListFiltered = eventListFiltered;
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

    /**
     * @return the eventViewList
     */
    public List<ViewOptionsActiveHiddenListsEnum> getEventViewList() {
        return eventViewList;
    }

    /**
     * @return the eventViewSelected
     */
    public ViewOptionsActiveHiddenListsEnum getEventViewSelected() {
        return eventViewSelected;
    }

    /**
     * @param eventViewList the eventViewList to set
     */
    public void setEventViewList(List<ViewOptionsActiveHiddenListsEnum> eventViewList) {
        this.eventViewList = eventViewList;
    }

    /**
     * @param eventViewSelected the eventViewSelected to set
     */
    public void setEventViewSelected(ViewOptionsActiveHiddenListsEnum eventViewSelected) {
        this.eventViewSelected = eventViewSelected;
    }

    /**
     * @return the currentEvent
     */
    public EventCnFPropUnitCasePeriodHeavy getCurrentEvent() {
        return currentEvent;
    }

    /**
     * @param currentEvent the currentEvent to set
     */
    public void setCurrentEvent(EventCnFPropUnitCasePeriodHeavy currentEvent) {
        this.currentEvent = currentEvent;
    }

    /**
     * @return the eventTypeList
     */
    public List<EventType> getEventTypeList() {
        return eventTypeList;
    }

    /**
     * @return the eventCategoryList
     */
    public List<EventCategory> getEventCategoryList() {
        return eventCategoryList;
    }

    /**
     * @param eventTypeList the eventTypeList to set
     */
    public void setEventTypeList(List<EventType> eventTypeList) {
        this.eventTypeList = eventTypeList;
    }

    /**
     * @param eventCategoryList the eventCategoryList to set
     */
    public void setEventCategoryList(List<EventCategory> eventCategoryList) {
        this.eventCategoryList = eventCategoryList;
    }
    
    
    
}
