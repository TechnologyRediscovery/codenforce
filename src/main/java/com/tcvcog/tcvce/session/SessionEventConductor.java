/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.session;

import com.tcvcog.tcvce.application.ActivatableRouteEnum;
import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.coordinators.SearchCoordinator;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.DomainEnum;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventCnFPropUnitCasePeriodHeavy;
import com.tcvcog.tcvce.entities.PageModeEnum;
import com.tcvcog.tcvce.entities.search.QueryEvent;
import com.tcvcog.tcvce.entities.search.QueryEventEnum;
import com.tcvcog.tcvce.session.entities.EventCalendarDay;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;

/**
 * The premier session-scoped, object family specific bean
 * whose job is the mighty session conducting of event related
 * tasks, including the dashboard calendar whose objects are managed
 * by this class.
 * 
 * @author Ellen Bascomb (of Apartment 31Y)
 */
public class SessionEventConductor extends BackingBeanUtils{
    
    static final int HOURS_IN_1_DAY = 24;
    
    private List<EventCalendarDay> eventCalendar6Day;
    
    private List<EventCnF> sessEventListForRefreshUptake;
    private List<EventCnFPropUnitCasePeriodHeavy> sessEventList;
    private EventCnF sessEvent;
    private ActivatableRouteEnum sessEventRoute;
    private ActivatableRouteEnum sessEventListRoute;
    /* >>> -------------------------------------------------------------- <<< */
    /* >>>                   V Event                                      <<< */
    /* >>> -------------------------------------------------------------- <<< */
    /**
     * There is no longer a notion of a session event domain, only a requested
     * event domain for event manipulators to use as guidance to figure out
     * if the relevant event list is extracted from the proper session business
     * objects
     */
    private PageModeEnum sessEventsPagePageModeRequest;
    private DomainEnum sessEventsPageEventDomainRequest;
    private QueryEvent queryEventFuture7Days;
    /* >>> QUERY EVENT <<< */
    private QueryEvent queryEvent;
    private List<QueryEvent> queryEventList;

    
    
    /**
     * Creates a new instance of SessionEvents
     */
    public SessionEventConductor() {
        
        
    }
    
    
    
    @PostConstruct
    public void initBean()  {
        System.out.println("SessionEventConductor.initBean");
        SearchCoordinator sc = getSearchCoordinator();
        queryEventList= sc.buildQueryEventList(getSessionBean().getSessUser().getKeyCard());
        if(!queryEventList.isEmpty()){
            queryEvent = queryEventList.get(0);
        }
        // start with default CE domain
        sessEventsPageEventDomainRequest = DomainEnum.CODE_ENFORCEMENT;
        
//        QueryEvent futureEvents = sc.initQuery(QueryEventEnum.MUINI_FUTURE_7DAYS, cred);
//        try {
            // TODO: Debug hanging issues
//            sb.setQueryEventFuture7Days(sc.runQuery(futureEvents));
//        } catch (SearchException ex) {
//            System.out.println(ex);
//        }
        try {
            initEventCalendar6Day();
        } catch (SearchException ex) {
            System.out.println(ex);
            
        }
    }
    
    /**
     * Listener for user requests to run calendar event query again
     * @param ev 
     */
    public void refreshCalendar(ActionEvent ev){
        try {
            initEventCalendar6Day();
              getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Calendar refreshed!", ""));
        } catch (SearchException ex) {
            System.out.println(ex);
              getFacesContext().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Could not load calendar: " + ex.getMessage(), ""));
        }
    }
    
    /**
     * Conducts a day-by-day event query for yesterday, today, and 4 future days
     * and injects the resulting composite object into this bean's member
     * 
     */
    private void initEventCalendar6Day() throws SearchException{
        SearchCoordinator sc = getSearchCoordinator();
 
        List<EventCalendarDay> dayList = new ArrayList<>();
        Map<Integer, String> prefixMap = new HashMap<>();

        prefixMap.put(-1, "Yesterday: ");
        prefixMap.put(0, "Today: ");

        for(int dayCount = -1; dayCount <=4; dayCount++){
            EventCalendarDay day = new EventCalendarDay();
            LocalDate ld;
            
            if(dayCount < 0){
                int minusOffset = dayCount * -1; // make day count a positive int for .minusDays()
                ld = LocalDate.now().minusDays(minusOffset);
            } else if (dayCount == 0){
                ld = LocalDate.now();
            } else {
                ld = LocalDate.now().plusDays(dayCount);
            }
            
            day.setDay(ld);
            day.setLdtLowerBound(ld.atStartOfDay());
            day.setLdtUpperBound(day.getLdtLowerBound().plusHours(HOURS_IN_1_DAY));
           
            // set the prefix if we have one
            String s = prefixMap.get(dayCount);
            if(s != null){
                day.setDayPrettyPrefix(s);
            } else {
                day.setDayPrettyPrefix("");
            }
                        
            QueryEvent evq = sc.initQuery(QueryEventEnum.CALENDAR, getSessionBean().getSessUser().getKeyCard());
            evq.getPrimaryParams().setDate_start_val(day.getLdtLowerBound());
            evq.getPrimaryParams().setDate_end_val(day.getLdtUpperBound());
           
            day.setEvList(sc.runQuery(evq, getSessionBean().getSessUser()).getBOBResultList());
            dayList.add(day);
           
        } // end for() over each day in the calendar
       
       eventCalendar6Day = dayList;
        
    }

    /**
     *
     *
     * @param sessEventList the sessEventList to set
     */
    public void setSessEventList(List<EventCnFPropUnitCasePeriodHeavy> sessEventList) {
        this.sessEventList = sessEventList;
    }

    /**
     *
     *
     * @param sessEventsPagePageModeRequest the sessEventsPagePageModeRequest to set
     */
    public void setSessEventsPagePageModeRequest(PageModeEnum sessEventsPagePageModeRequest) {
        this.sessEventsPagePageModeRequest = sessEventsPagePageModeRequest;
    }

    /**
     *
     *
     * @return the sessEvent
     */
    public EventCnF getSessEvent() {
        return sessEvent;
    }

    /**
     *
     *
     * @param sessEventsPageEventDomainRequest the sessEventsPageEventDomainRequest to set
     */
    public void setSessEventsPageEventDomainRequest(DomainEnum sessEventsPageEventDomainRequest) {
        this.sessEventsPageEventDomainRequest = sessEventsPageEventDomainRequest;
    }

    /**
     *
     *
     * @param sessEventListForRefreshUptake the sessEventListForRefreshUptake to set
     */
    public void setSessEventListForRefreshUptake(List<EventCnF> sessEventListForRefreshUptake) {
        this.sessEventListForRefreshUptake = sessEventListForRefreshUptake;
    }

    /**
     *
     *
     * @return the sessEventList
     */
    public List<EventCnFPropUnitCasePeriodHeavy> getSessEventList() {
        return sessEventList;
    }

    /**
     *
     *
     * @return the sessEventsPagePageModeRequest
     */
    public PageModeEnum getSessEventsPagePageModeRequest() {
        return sessEventsPagePageModeRequest;
    }


    /**
     *
     *
     * @return the sessEventListForRefreshUptake
     */
    public List<EventCnF> getSessEventListForRefreshUptake() {
        return sessEventListForRefreshUptake;
    }

    /**
     *
     *
     * @param sessEvent the sessEvent to set
     */
    public void setSessEvent(EventCnF sessEvent) {
        this.sessEvent = sessEvent;
    }

    /**
     *
     *
     * @return the sessEventsPageEventDomainRequest
     */
    public DomainEnum getSessEventsPageEventDomainRequest() {
        return sessEventsPageEventDomainRequest;
    }

    /**
     * @return the eventCalendar6Day
     */
    public List<EventCalendarDay> getEventCalendar6Day() {
        return eventCalendar6Day;
    }

    /**
     * @param eventCalendar6Day the eventCalendar6Day to set
     */
    public void setEventCalendar6Day(List<EventCalendarDay> eventCalendar6Day) {
        this.eventCalendar6Day = eventCalendar6Day;
    }

    /**
     *
     *
     * @param queryEvent the queryEvent to set
     */
    public void setQueryEvent(QueryEvent queryEvent) {
        this.queryEvent = queryEvent;
    }

    /**
     *
     *
     * @return the queryEventList
     */
    public List<QueryEvent> getQueryEventList() {
        return queryEventList;
    }

    /**
     *
     *
     * @return the queryEventFuture7Days
     */
    public QueryEvent getQueryEventFuture7Days() {
        return queryEventFuture7Days;
    }

    /**
     *
     *
     * @param queryEventList the queryEventList to set
     */
    public void setQueryEventList(List<QueryEvent> queryEventList) {
        this.queryEventList = queryEventList;
    }

    /**
     *
     *
     * @param queryEventFuture7Days the queryEventFuture7Days to set
     */
    public void setQueryEventFuture7Days(QueryEvent queryEventFuture7Days) {
        this.queryEventFuture7Days = queryEventFuture7Days;
    }

    /**
     *
     *
     * @return the queryEvent
     */
    public QueryEvent getQueryEvent() {
        return queryEvent;
    }
    
    
    
    
}
