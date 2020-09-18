/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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

import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.coordinators.CaseCoordinator;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.MunicipalityCoordinator;
import com.tcvcog.tcvce.coordinators.PersonCoordinator;
import com.tcvcog.tcvce.coordinators.PropertyCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.EventListTypeEnum;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PropertyUseType;
import com.tcvcog.tcvce.entities.reports.ReportConfigCEEventList;
import com.tcvcog.tcvce.entities.search.QueryEvent;
import com.tcvcog.tcvce.entities.search.SearchParamsEvent;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.event.ActionEvent;

/**
 * The cornerstone backing bean for the grand unified event (GUE) subsystem:
 * displays and allows the user to manipulate EventCnF objects for either
 * CECase objects or OccPeriod objects--our two workflow-enabled BObs
 * @author sylvia
 */
public class EventsBB extends BackingBeanUtils implements Serializable{
    
    private PageModeEnum currentMode;
    private List<PageModeEnum> pageModes;
    
    private EventDomainEnum currentEventDomain;
    private IFace_EventRuleGoverned currentERGBOb;
    private EventCnFPropUnitCasePeriodHeavy currentEvent;
    
    private EventListTypeEnum listType;
    private List<EventListTypeEnum> listTypeList;
    private List<EventCnFPropUnitCasePeriodHeavy> eventList;
    private List<EventCnFPropUnitCasePeriodHeavy> filteredEventList;
    
    private List<ViewOptionsActiveHiddenListsEnum> eventsViewOptionsCandidates;
    private ViewOptionsActiveHiddenListsEnum selectedEventView;
    
    private Map<EventType, List<EventCategory>> typeCatMap;
    private boolean updateNewEventFieldsWithCatChange;
    private Map<EventType, List<EventCategory>> typeCatMapForSearch;
    
    private List<EventType> eventTypeCandidates;
    private EventType eventTypeSelected;
    
    private List<EventCategory> eventCategoryCandidates;
    private EventCategory eventCategorySelected;
    
    private List<Person> personCandidates;
    private int personIDForLookup;
    private Person personSelected;
    
    private String formNoteText;
    
    // Absorbed from EventSearchBB
    
    private boolean appendResultsToList;
    
    private List<QueryEvent> queryList;
    private QueryEvent querySelected;
    
    private SearchParamsEvent searchParamsSelected;
    
    private List<EventCategory> eventCategoryListSearch;
    private List<EventType> eventTypeListSearch;
    
    private int actionRequestsUserType;
    
    private ReportConfigCEEventList reportConfig;
    
    private List<PropertyUseType> propUseTypeList;
  
    /**
     * Creates a new instance of EventsBB
     */
    public EventsBB() {
    }
    
    /**
     * We've got to setup this bean to work with either event domain: OccPeriod
     * or CECase, which we'll get by asking the session bean what it is set to. If
     * the SessionBean isn't clear on the matter, implement some basic logic 
     * to choose and tell the user
     * 
     * We'll also setup possible new events based on the status of the 
     * active workflow-enabled BOb
     * 
     */
    @PostConstruct
    public void initBean() {
        EventCoordinator ec = getEventCoordinator();
        MunicipalityCoordinator mc = getMuniCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        SearchCoordinator sc = getSearchCoordinator();
        SessionBean sb = getSessionBean();
        
        eventsViewOptionsCandidates = Arrays.asList(ViewOptionsActiveHiddenListsEnum.values());
        selectedEventView = ec.determineDefaultEventView(sb.getSessUser());
        listTypeList = Arrays.asList(EventListTypeEnum.values());
        personCandidates = new ArrayList<>();
        
        
        typeCatMap = ec.assembleEventTypeCatMap_toEnact(currentEventDomain, currentERGBOb, getSessionBean().getSessUser());
        updateNewEventFieldsWithCatChange = true;
        
        eventTypeCandidates = new ArrayList<>(getTypeCatMap().keySet());
        eventCategoryCandidates = new ArrayList<>();
        
        if(eventTypeCandidates != null && !eventTypeCandidates.isEmpty()){
            eventTypeSelected = getEventTypeCandidates().get(0);
            eventCategoryCandidates.addAll(typeCatMap.get(eventTypeSelected));
        }
        eventList = new ArrayList<>();
        filteredEventList = new ArrayList<>();
        
        
        setPageModes(new ArrayList<PageModeEnum>());
        getPageModes().add(PageModeEnum.LOOKUP);
        getPageModes().add(PageModeEnum.INSERT);
        getPageModes().add(PageModeEnum.UPDATE);
        getPageModes().add(PageModeEnum.REMOVE);
        
        EventCnF sessEv =  getSessionBean().getSessEvent();
        if(sessEv != null){
            currentEventDomain = currentEvent.getDomain();
            
            try {
                currentEvent = ec.assembleEventCnFPropUnitCasePeriodHeavy(sessEv);
                
            } catch (EventException | IntegrationException | SearchException ex) {
                System.out.println("EventsBB.initbean: Current event loading error");
            }
                
        } else { // we don't have a session event, so set page domain based on domain request on sessionbean
            
            // Based on who sent us to this page, we'll load in either the CECase or OccPeriod
            // on the SessionBean
            EventDomainEnum domain = sb.getSessEventsPageEventDomainRequest();
            // uses domain in a switch{} for setting property values of other page modes
            if(domain == null){
                // set a default value
                domain = EventDomainEnum.OCCUPANCY;
            }
            currentEventDomain = domain;
        }
        
        
        if(currentEvent != null && currentEvent.getEventID() == 0){
            currentMode = PageModeEnum.INSERT;
            
        }  else if (currentEvent != null && currentEvent.getEventID() != 0){
            currentMode = PageModeEnum.VIEW;
        } else {
            currentMode = PageModeEnum.LOOKUP;
            currentEventDomain = EventDomainEnum.UNIVERSAL;
        }
        
        configureEventDomainAndEventList(currentEventDomain);
         
        //**************************************
        //************** SEARCH ****************
        //**************************************
         
        queryList = sc.buildQueryEventList(getSessionBean().getSessUser().getMyCredential());
        
        // Setting default query
        if(queryList != null && !queryList.isEmpty()){
            querySelected = queryList.get(0);
        }
        
        typeCatMapForSearch = ec.assembleEventTypeCatMap_toView(getSessionBean().getSessUser());
        
        eventTypeListSearch = new ArrayList(getTypeCatMap().keySet());
        if(eventTypeListSearch != null && !eventTypeListSearch.isEmpty()){
            eventCategoryListSearch = typeCatMapForSearch.get(eventTypeListSearch.get(0));
        }
        
        propUseTypeList = pc.getPropertyUseTypeList();
        
        appendResultsToList = false;
        
        
        // grab previously loaded event config from the session bean
        // which would have been placed there by the generateReport method in this bean
        reportConfig = getSessionBean().getReportConfigCEEventList();
        configureParameters();
    }
    
      /**
     * Responds to the user clicking one of the page modes: LOOKUP, ADD, UPDATE,
     * REMOVE
     *
     * @param mode
     */
    public void setCurrentMode(PageModeEnum mode) {

        //store currentMode into tempCurMode as a temporary value, in case the currenMode equal null
        PageModeEnum tempCurMode = this.getCurrentMode();
        //reset default setting every time the Mode has been selected 
//        loadDefaultPageConfig();
        //check the currentMode == null or not
        if (mode == null) {
            this.currentMode = tempCurMode;
        } else {
            this.currentMode = mode;
        }
        switch (currentMode) {
            case LOOKUP:
                onModeLookupInit();
                break;
            case INSERT:
                onModeInsertInit();
                break;
            case UPDATE:
                onModeUpdateInit();
                break;
            case REMOVE:
                onModeRemoveInit();
                break;
            default:
                break;
        }
    }
    
    
    /**
     * Internal logic container for changes to page mode: Lookup
     */
    private void onModeLookupInit() {
    }
    
    /**
     * Internal container for mode add init
     */
    private void onModeInsertInit(){
        initiateNewEvent();
    }

    
     /**
     * Listener for beginning of update process
     */
    public void onModeUpdateInit() {
        // nothign to do here yet since the user is selected
    }

    /**
     * Listener for the start of the case remove process
     */
    public void onModeRemoveInit() {

    }
    
    /**
     * Based on the requested domain, session objects are grabbed and 
     * used for setting the bean's central members
     * @param domain 
     */
    private void configureEventDomainAndEventList(EventDomainEnum domain){
        
        SessionBean sb = getSessionBean();
        EventCoordinator ec = getEventCoordinator();
        PropertyCoordinator pc = getPropertyCoordinator();
        if(domain != null){

            try {
               switch(domain){
                   case CODE_ENFORCEMENT:
                       currentERGBOb = sb.getSessCECase();
                       currentEventDomain = domain;
                       // in this case our ERGBob is a CECase
                       eventList.addAll(ec.assembleEventCnFPropUnitCasePeriodHeavyList(getCurrentERGBOb().getEventList(getSelectedEventView())));
                       listType = EventListTypeEnum.CODE_ENFORCEMENT;
                       break;
                   case OCCUPANCY:
                       currentERGBOb = sb.getSessOccPeriod();
                       currentEventDomain = domain;
                       // in this case our ERGBOb is an OccPeriod
                       eventList.addAll(ec.assembleEventCnFPropUnitCasePeriodHeavyList(getCurrentERGBOb().getEventList(getSelectedEventView())));
                       listType = EventListTypeEnum.OCCUPANCY;
                       break;
                   case UNIVERSAL:
                       // We'll just grab all the events on the session and load up
                       // the muni property info
                       currentEventDomain = domain;
                       listType = EventListTypeEnum.CUSTOM;
                       if(sb.getSessEventList() != null && !sb.getSessEventList().isEmpty()){
                           eventList.addAll(sb.getSessEventList());
                       }
                       // ask the Prop Coor to figure out a sensible ERG when we're viewing
                       // an arbitrary event list
                       currentERGBOb = (IFace_EventRuleGoverned) pc.determineGoverningPropertyInfoCase(sb.getSessMuni().getMuniPropertyDH());
                       break;
                   // "Shouldn't happen"
                   default:
                       listType = EventListTypeEnum.CUSTOM;
                       if(sb.getSessEventList() != null && !sb.getSessEventList().isEmpty()){
                           eventList.addAll(sb.getSessEventList());
                       }
                       setCurrentERGBOb((IFace_EventRuleGoverned) pc.determineGoverningPropertyInfoCase(sb.getSessMuni().getMuniPropertyDH()));
               }
           } catch (EventException |IntegrationException | SearchException ex) {
                   System.out.println("EventsBB.initBean: problem setting page event domain");
                   System.out.println(ex);
           }
        }
    }
    
      
    /**
     * Event search setup infrastructure
     */
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
    
    
    //check if current mode == Lookup
    public boolean getActiveLookupMode() {
        // hard-wired on since there's always a property loaded
        return PageModeEnum.LOOKUP.equals(currentMode);
    }

    /**
     * Provide UI elements a boolean true if the mode is UPDATE
     *
     * @return
     */
    public boolean getActiveUpdateMode() {
        return PageModeEnum.UPDATE.equals(currentMode);
    }

    public boolean getActiveViewMode() {
        if (PageModeEnum.LOOKUP.equals(currentMode) || PageModeEnum.VIEW.equals(currentMode)) {
            if (currentEvent != null) {
                return true;
            }
        }
        return false;
    }

    //check if current mode == Insert
    public boolean getActiveInsertUpdateMode() {
        return PageModeEnum.INSERT.equals(currentMode) || PageModeEnum.UPDATE.equals(currentMode);
    }

    //check if current mode == Insert
    public boolean getActiveInsertMode() {
        return PageModeEnum.INSERT.equals(getCurrentMode());
    }

    //check if current mode == Remove
    public boolean getActiveRemoveMode() {
        return PageModeEnum.REMOVE.equals(getCurrentMode());
    }

    /**
     * Primary listener method which copies a reference to the selected user
     * from the list and sets it on the selected user perch
     *
     * @param ev
     */
    public void onObjectViewButtonChange(EventCnFPropUnitCasePeriodHeavy ev) {

        if (ev != null) {
            getSessionBean().setSessEvent(ev);
            currentEvent = ev;
            System.out.println("EventsBB.onObjectViewButtonChange: " + ev.getEventID());
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
    
    /**
     * Listener for changes to the event list mode, which will
     * trigger changes to the overall event domain
     * 
     * @param elte
     * @return 
     */
    public String onListTypeMenuChange(EventListTypeEnum elte){
        System.out.println("EventsBB.onListTypeMenuChange: " + elte.getTitle());
        listType = elte;
        switch(elte){
            case CODE_ENFORCEMENT:
                configureEventDomainAndEventList(EventDomainEnum.CODE_ENFORCEMENT);
                break;
            case OCCUPANCY:
                configureEventDomainAndEventList(EventDomainEnum.OCCUPANCY);
                break;
            case SEARCH_RESULT:
                configureEventDomainAndEventList(EventDomainEnum.UNIVERSAL);
                break;
            case CUSTOM:
                configureEventDomainAndEventList(EventDomainEnum.UNIVERSAL);
                break;
        }

        return "";
        
    }
    
    /**
     * Listener for user changes to the selected event type
     */
    public void onEventTypeMunuChange(){
        System.out.println("EventsBB.onEventTypeMenuChange");
        refreshAvailableEventCategories();
        
    }
    
    /**
     * Listener for user changes to the event category list on event add
     */
    public void onEventCategoryMenuChange(){
        
        configureCurrentEventFields();
    }
    
    
    /**
     * Sets current event field values to those suggested by the 
     * selected event category
     */
    private void configureCurrentEventFields(){
        if(eventCategorySelected != null 
                && currentEvent != null 
                && updateNewEventFieldsWithCatChange){
            currentEvent.setTimeStart(LocalDateTime.now());
            currentEvent.setTimeEnd(LocalDateTime.now().plusMinutes(eventCategorySelected.getDefaultdurationmins()));
            currentEvent.setDescription(eventCategorySelected.getHostEventDescriptionSuggestedText());
        }
    }
    
    public void onTimeStartChange(){
        currentEvent.setTimeEnd(LocalDateTime.now().plusMinutes(eventCategorySelected.getDefaultdurationmins()));
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
            listType = EventListTypeEnum.SEARCH_RESULT;
            
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
            reportConfig = ec.initDefaultReportConfigEventList();
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
        
        return eventTypeListSearch;
    }

    /**
     * @return the eventCatList
     */
    public List<EventCategory> getEventCatList() {
        return eventCategoryListSearch;
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
     * Actionlistener Called when the user selects their own EventCategory to add to the case
     * and is a pass-through method to the initiateNewEvent method
     *
     * @param ev
     */
    public void initiateUserChosenEventCreation(ActionEvent ev) {
        initiateNewEvent();
    }
    
    
    /**
     * Listener method for starting event edits
     * @param ev 
     */
    public void initiateEventEdit(EventCnFPropUnitCasePeriodHeavy ev){
        currentEvent = ev;
    }
    

    /**
     * Logic container for setting up new event which will be displayed 
     * in the overlay window for the User
     */
    private void initiateNewEvent() {
        System.out.println("EventsBB.initiateNewEvent");
        EventCoordinator ec = getEventCoordinator();
        
        EventCnF ev = null;
            
            try {
                ev = ec.initEvent(currentERGBOb, null);
//                ev.setCategory(eventCategorySelected);
                switch(currentEventDomain){
                    case CODE_ENFORCEMENT:
                        ev.setCeCaseID(getCurrentERGBOb().getBObID());
                        break;
                    case OCCUPANCY:
                        ev.setOccPeriodID(getCurrentERGBOb().getBObID());
                        break;
                    default:
                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot add event to non CE or Occ domain", ""));
                        break;
                }
                ev.setUserCreator(getSessionBean().getSessUser());
                currentEvent = ec.assembleEventCnFPropUnitCasePeriodHeavy(ev);
                
            } catch (BObStatusException | EventException | IntegrationException | SearchException ex) {
                System.out.println(ex);
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, ex.getMessage(), ""));
            }
    }

    /**
     * All Code Enforcement case events are funneled through this method which
     * has to carry out a number of checks based on the type of event being
     * created. The event is then passed to the addEvent_processForCECaseDomain on the
     * Event Coordinator who will do some more checking about the event before
     * writing it to the DB
     *
     * @return 
     */
    public String onEventAddCommitButtonChange() {
        System.out.println("EventsBB.onEventAddCommitButtonChange");
        EventCoordinator ec = getEventCoordinator();
        
        List<EventCnFPropUnitCasePeriodHeavy> evDoneList;
            
        // category is already set from initialization sequence

        try {
            currentEvent.setCategory(eventCategorySelected);
            currentEvent.setDomain(currentEventDomain);
            if(currentEvent.getDomain() == null && currentEvent.getDomain() == EventDomainEnum.UNIVERSAL){
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Event must have a domain that's not universal", ""));
                return "";
            }
            if(eventCategorySelected == null){
                getFacesContext().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Event must have a category ", ""));
                return "";
            } else {
                List<EventCnF> evSimpleList = ec.addEvent(currentEvent, currentERGBOb, getSessionBean().getSessUser());
                evDoneList = ec.assembleEventCnFPropUnitCasePeriodHeavyList(evSimpleList);

                if(evDoneList != null && !evDoneList.isEmpty()){
                    for(EventCnF evt: evDoneList){

                        getFacesContext().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_INFO,
                                        "Successfully logged event with an ID " + evt.getEventID() + " ", ""));
                    }
                    currentEvent = evDoneList.get(0);
                }
                getSessionBean().setSessEvent(currentEvent);
                return "events";
            }
    
        } catch (IntegrationException | BObStatusException | EventException | SearchException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        } 

        // nullify the session's case so that the reload of currentCase
        // no the cecaseProfile.xhtml will trigger a new DB read
        return "";
    }
    
    /**
     * Listener for user requests to update an event
     * @return 
     */
    public String onEventUpdateCommitButtonChange(){
        
        return "events";
    }
    
    public void onPersonListCommitButtonChange(ActionEvent ev){
        
        // nothing to do on the back end
        
    }
    
    /**
     * Listener for user requests to search for a person by ID
     * @param ev 
     */
    public void onPersonLookupByIDButtonChange(ActionEvent ev){
        PersonCoordinator pc = getPersonCoordinator();
        if(personIDForLookup == 0){
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Shall not look up person with ID of 0",
                            "Please enter a positive, non-zero ID"));
             return;
            
        }
        try {
            personCandidates.add(pc.getPerson(personIDForLookup));
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Located " + personCandidates.size() + " persons with this ID",
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        } catch (IntegrationException ex) {
             getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            ex.getMessage(),
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        }
        
        
    }
  
    /**
     * Listener method for changes in EventType selected by User
     */
    public void refreshAvailableEventCategories(){
        
        if(eventTypeSelected != null){
            eventCategoryCandidates.clear();
            eventCategoryCandidates.addAll(typeCatMap.get(eventTypeSelected));
            if(!eventCategoryCandidates.isEmpty()){
                eventCategorySelected = eventCategoryCandidates.get(0);
            }
            System.out.println("EventsBB.refreshavailableEventCategories");
        }
    }
    
   
    

    /**
     * Listener pass through method for finalizing event edits
     * @param ev 
     */
    public void finalizeEventUpdateListener(ActionEvent ev){
        finalizeEventUpdate();
    }
    
    /**
     * Event update processing
     */
    public void finalizeEventUpdate() {
        EventCoordinator ec = getEventCoordinator();
        try {
            ec.updateEvent(getCurrentEvent(), getSessionBean().getSessUser());
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Event udpated!", ""));
        } catch (IntegrationException | EventException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    ex.getMessage(),
                    ""));
        } 

    }

    /**
     * Listener method for adding the selected person to a queue
     * @param ev 
     */
    public void queueSelectedPerson(ActionEvent ev) {
        EventCoordinator ec = getEventCoordinator();
        if (getPersonSelected() != null) {
            getCurrentEvent().getPersonList().add(getPersonSelected());
        } else {
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Please select one or more people to attach to this event",
                            "This is a non-user system-level error that must be fixed by your Sys Admin"));
        }
    }

    public void deQueuePersonFromEvent(Person p) {
        if (getCurrentEvent().getPersonList() != null) {
            getCurrentEvent().getPersonList().remove(p);
        }
    }

   
      /**
     * Listener for commencement of note writing process
     *
     * @param ev
     */
    public void onNoteInitButtonChange(ActionEvent ev) {
        setFormNoteText(null);

    }

    /**
     * Listener for user requests to commit new note content to the current
     * object
     *
     * @return
     */
    public String onNoteCommitButtonChange() {
        EventCoordinator ec = getEventCoordinator();

        MessageBuilderParams mbp = new MessageBuilderParams();
        mbp.setCred(getSessionBean().getSessUser().getKeyCard());
        mbp.setExistingContent(getCurrentEvent().getNotes());
        mbp.setNewMessageContent(getFormNoteText());
        mbp.setHeader("Notice of Violation Note");
        mbp.setUser(getSessionBean().getSessUser());

        try {

            ec.updateEventNotes(mbp, getCurrentEvent(), getSessionBean().getSessUser());
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succesfully appended note!", ""));
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Fatal error appending note; apologies!", ""));
            return "";
        }

        return "events";

    }



    /**
     * @return the eventCategoryCandidates
     */
    public List<EventCategory> getEventCategoryCandidates() {
        return eventCategoryCandidates;
    }

    /**
     * @return the eventCategorySelected
     */
    public EventCategory getEventCategorySelected() {
        return eventCategorySelected;
    }

    /**
     * @return the eventTypeCandidates
     */
    public List<EventType> getEventTypeCandidates() {
        return eventTypeCandidates;
    }

    /**
     * @return the eventTypeSelected
     */
    public EventType getEventTypeSelected() {
        return eventTypeSelected;
    }

    /**
     * @param eventCategoryCandidates the eventCategoryCandidates to set
     */
    public void setEventCategoryCandidates(List<EventCategory> eventCategoryCandidates) {
        this.eventCategoryCandidates = eventCategoryCandidates;
    }

    /**
     * @param eventCategorySelected the eventCategorySelected to set
     */
    public void setEventCategorySelected(EventCategory eventCategorySelected) {
        this.eventCategorySelected = eventCategorySelected;
    }

    /**
     * @param eventTypeCandidates the eventTypeCandidates to set
     */
    public void setEventTypeCandidates(List<EventType> eventTypeCandidates) {
        this.eventTypeCandidates = eventTypeCandidates;
    }

    /**
     * @param eventTypeSelected the eventTypeSelected to set
     */
    public void setEventTypeSelected(EventType eventTypeSelected) {
        this.eventTypeSelected = eventTypeSelected;
    }

    /**
     * @return the currentEvent
     */
    public EventCnF getCurrentEvent() {
        return currentEvent;
    }

    /**
     * @param currentEvent the currentEvent to set
     */
    public void setCurrentEvent(EventCnFPropUnitCasePeriodHeavy currentEvent) {
        this.currentEvent = currentEvent;
    }

    

    /**
     * @return the personSelected
     */
    public Person getPersonSelected() {
        return personSelected;
    }

    /**
     * @param personSelected the personSelected to set
     */
    public void setPersonSelected(Person personSelected) {
        this.personSelected = personSelected;
    }

    /**
     * @return the currentEventDomain
     */
    public EventDomainEnum getCurrentEventDomain() {
        return currentEventDomain;
    }

    /**
     * @param currentEventDomain the currentEventDomain to set
     */
    public void setCurrentEventDomain(EventDomainEnum currentEventDomain) {
        this.currentEventDomain = currentEventDomain;
    }


    /**
     * @return the filteredEventList
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getFilteredEventList() {
        return filteredEventList;
    }

    /**
     * @return the eventsViewOptionsCandidates
     */
    public List<ViewOptionsActiveHiddenListsEnum> getEventsViewOptionsCandidates() {
        return eventsViewOptionsCandidates;
    }

    /**
     * @return the selectedEventView
     */
    public ViewOptionsActiveHiddenListsEnum getSelectedEventView() {
        return selectedEventView;
    }


    /**
     * @return the personCandidates
     */
    public List<Person> getPersonCandidates() {
        return personCandidates;
    }

    /**
     * @param filteredEventList the filteredEventList to set
     */
    public void setFilteredEventList(List<EventCnFPropUnitCasePeriodHeavy> filteredEventList) {
        this.filteredEventList = filteredEventList;
    }

    /**
     * @param eventsViewOptionsCandidates the eventsViewOptionsCandidates to set
     */
    public void setEventsViewOptionsCandidates(List<ViewOptionsActiveHiddenListsEnum> eventsViewOptionsCandidates) {
        this.eventsViewOptionsCandidates = eventsViewOptionsCandidates;
    }

    /**
     * @param selectedEventView the selectedEventView to set
     */
    public void setSelectedEventView(ViewOptionsActiveHiddenListsEnum selectedEventView) {
        this.selectedEventView = selectedEventView;
    }


    /**
     * @param personCandidates the personCandidates to set
     */
    public void setPersonCandidates(List<Person> personCandidates) {
        this.personCandidates = personCandidates;
    }

    /**
     * @return the currentERGBOb
     */
    public IFace_EventRuleGoverned getCurrentERGBOb() {
        return currentERGBOb;
    }

    /**
     * @param currentERGBOb the currentERGBOb to set
     */
    public void setCurrentERGBOb(IFace_EventRuleGoverned currentERGBOb) {
        this.currentERGBOb = currentERGBOb;
    }

    /**
     * @return the eventList
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getEventList() {
        return eventList;
    }

    /**
     * @param eventList the eventList to set
     */
    public void setEventList(List<EventCnFPropUnitCasePeriodHeavy> eventList) {
        this.eventList = eventList;
    }

    /**
     * @return the pageModes
     */
    public List<PageModeEnum> getPageModes() {
        return pageModes;
    }

    /**
     * @param pageModes the pageModes to set
     */
    public void setPageModes(List<PageModeEnum> pageModes) {
        this.pageModes = pageModes;
    }

    /**
     * @return the currentMode
     */
    public PageModeEnum getCurrentMode() {
        return currentMode;
    }

   

    /**
     * @return the formNoteText
     */
    public String getFormNoteText() {
        return formNoteText;
    }

    /**
     * @param formNoteText the formNoteText to set
     */
    public void setFormNoteText(String formNoteText) {
        this.formNoteText = formNoteText;
    }

    /**
     * @return the typeCatMap
     */
    public Map<EventType, List<EventCategory>> getTypeCatMap() {
        return typeCatMap;
    }

    /**
     * @return the appendResultsToList
     */
    public boolean isAppendResultsToList() {
        return appendResultsToList;
    }

    /**
     * @return the queryList
     */
    public List<QueryEvent> getQueryList() {
        return queryList;
    }

    /**
     * @return the querySelected
     */
    public QueryEvent getQuerySelected() {
        return querySelected;
    }

    /**
     * @return the searchParamsSelected
     */
    public SearchParamsEvent getSearchParamsSelected() {
        return searchParamsSelected;
    }

   

    /**
     * @return the eventCategoryListSearch
     */
    public List<EventCategory> getEventCategoryListSearch() {
        return eventCategoryListSearch;
    }

    /**
     * @return the eventTypeListSearch
     */
    public List<EventType> getEventTypeListSearch() {
        return eventTypeListSearch;
    }

    /**
     * @return the actionRequestsUserType
     */
    public int getActionRequestsUserType() {
        return actionRequestsUserType;
    }

    /**
     * @return the reportConfig
     */
    public ReportConfigCEEventList getReportConfig() {
        return reportConfig;
    }

    /**
     * @return the propUseTypeList
     */
    public List<PropertyUseType> getPropUseTypeList() {
        return propUseTypeList;
    }

    /**
     * @param typeCatMap the typeCatMap to set
     */
    public void setTypeCatMap(Map<EventType, List<EventCategory>> typeCatMap) {
        this.typeCatMap = typeCatMap;
    }

    /**
     * @param appendResultsToList the appendResultsToList to set
     */
    public void setAppendResultsToList(boolean appendResultsToList) {
        this.appendResultsToList = appendResultsToList;
    }

    /**
     * @param queryList the queryList to set
     */
    public void setQueryList(List<QueryEvent> queryList) {
        this.queryList = queryList;
    }

    /**
     * @param querySelected the querySelected to set
     */
    public void setQuerySelected(QueryEvent querySelected) {
        this.querySelected = querySelected;
    }

    /**
     * @param searchParamsSelected the searchParamsSelected to set
     */
    public void setSearchParamsSelected(SearchParamsEvent searchParamsSelected) {
        this.searchParamsSelected = searchParamsSelected;
    }

   

    /**
     * @param eventCategoryListSearch the eventCategoryListSearch to set
     */
    public void setEventCategoryListSearch(List<EventCategory> eventCategoryListSearch) {
        this.eventCategoryListSearch = eventCategoryListSearch;
    }

    /**
     * @param eventTypeListSearch the eventTypeListSearch to set
     */
    public void setEventTypeListSearch(List<EventType> eventTypeListSearch) {
        this.eventTypeListSearch = eventTypeListSearch;
    }

    /**
     * @param actionRequestsUserType the actionRequestsUserType to set
     */
    public void setActionRequestsUserType(int actionRequestsUserType) {
        this.actionRequestsUserType = actionRequestsUserType;
    }

    /**
     * @param reportConfig the reportConfig to set
     */
    public void setReportConfig(ReportConfigCEEventList reportConfig) {
        this.reportConfig = reportConfig;
    }

    /**
     * @param propUseTypeList the propUseTypeList to set
     */
    public void setPropUseTypeList(List<PropertyUseType> propUseTypeList) {
        this.propUseTypeList = propUseTypeList;
    }

    /**
     * @return the listType
     */
    public EventListTypeEnum getListType() {
        return listType;
    }

    /**
     * @param listType the listType to set
     */
    public void setListType(EventListTypeEnum listType) {
        this.listType = listType;
    }

    /**
     * @return the listTypeList
     */
    public List<EventListTypeEnum> getListTypeList() {
        return listTypeList;
    }

    /**
     * @param listTypeList the listTypeList to set
     */
    public void setListTypeList(List<EventListTypeEnum> listTypeList) {
        this.listTypeList = listTypeList;
    }

    /**
     * @return the personIDForLookup
     */
    public int getPersonIDForLookup() {
        return personIDForLookup;
    }

    /**
     * @param personIDForLookup the personIDForLookup to set
     */
    public void setPersonIDForLookup(int personIDForLookup) {
        this.personIDForLookup = personIDForLookup;
    }

    /**
     * @return the typeCatMapForSearch
     */
    public Map<EventType, List<EventCategory>> getTypeCatMapForSearch() {
        return typeCatMapForSearch;
    }

    /**
     * @param typeCatMapForSearch the typeCatMapForSearch to set
     */
    public void setTypeCatMapForSearch(Map<EventType, List<EventCategory>> typeCatMapForSearch) {
        this.typeCatMapForSearch = typeCatMapForSearch;
    }

    /**
     * @return the updateNewEventFieldsWithCatChange
     */
    public boolean isUpdateNewEventFieldsWithCatChange() {
        return updateNewEventFieldsWithCatChange;
    }

    /**
     * @param updateNewEventFieldsWithCatChange the updateNewEventFieldsWithCatChange to set
     */
    public void setUpdateNewEventFieldsWithCatChange(boolean updateNewEventFieldsWithCatChange) {
        this.updateNewEventFieldsWithCatChange = updateNewEventFieldsWithCatChange;
    }

}
