/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import com.tcvcog.tcvce.entities.search.QueryCEAREnum;
import com.tcvcog.tcvce.entities.search.QueryEventCECase;
import com.tcvcog.tcvce.entities.search.SearchParams;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.entities.search.SearchParamsCECases;
import com.tcvcog.tcvce.entities.search.SearchParamsEventCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsPersons;
import com.tcvcog.tcvce.entities.search.SearchParamsProperties;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public class SearchCoordinator extends BackingBeanUtils implements Serializable{

   
    
    /**
     * Creates a new instance of SearchCoordinator
     */
    public SearchCoordinator() {
    }
    
    @PostConstruct
    public void initBean(){
        
    }
    
    public List<QueryCEAR> buildCEARQueryList(User u, Municipality m){
        QueryCEAREnum[] titleArr = QueryCEAREnum.values();
        List<QueryCEAR> qList = new ArrayList<>();
        qList.add(buildCEARQuery(QueryCEAREnum.ALL_PAST30, u, m));
//        for(QueryCEAREnum qTit: titleArr){
//            qList.add(buildCEARQuery(qTit, u, m));
//        }
        return qList;
    }
    
    
    
    
    public QueryCEAR buildCEARQuery(QueryCEAREnum qTitle, User u, Municipality m){
        QueryCEAR q = new QueryCEAR(m);
        q.setQueryTitle(qTitle.getDesc());
        
        switch(qTitle){
            case UNPROCESSED:
                q.addSearchParams(generateParams_CEAR_Unprocessed(m));
                break;
//            case ATTACHED_TO_CECASE:
//                break;
//            
//            case ALL_TODAY:
//                break;
//                
//            case ALL_PAST7DAYS:
//                break;
//                
            case ALL_PAST30:
                break;
//                
//            case ALL_PASTYEAR:
//                break;
                
            default:
                q.addSearchParams(generateParams_CEAR_Unprocessed(m));
        }
        
        return q;
    }
    
    
    
    
     public SearchParamsCEActionRequests generateParams_CEAR_Unprocessed(Municipality m){
            
        SearchCoordinator sc = getSearchCoordinator();

        SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

        sps.setMuni(m);
        LocalDateTime pastTenYears = LocalDateTime.now().minusYears(10);
        sps.setStartDate(pastTenYears);

        // action requests cannot have a time stamp past the current datetime
        sps.setEndDate(LocalDateTime.now());

        sps.setUseAttachedToCase(true);
        sps.setAttachedToCase(false);
        sps.setUseMarkedUrgent(false);
        sps.setUseNotAtAddress(false);
        sps.setUseRequestStatus(false);
        
        return sps;
    }
    
    
    
     public SearchParamsCEActionRequests generateParams_CEAR_past30Days(Municipality m){
            
        SearchCoordinator sc = getSearchCoordinator();

        SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

        sps.setMuni(m);
        LocalDateTime past30Days = LocalDateTime.now().minusDays(30);
        sps.setStartDate(past30Days);

        // action requests cannot have a time stamp past the current datetime
        sps.setEndDate(LocalDateTime.now());

        sps.setUseAttachedToCase(false);
        sps.setUseMarkedUrgent(false);
        sps.setUseNotAtAddress(false);
        sps.setUseRequestStatus(false);
        
        return sps;
    }
    
    
    
    
//    CODE ENFORCEMENT CASE QUERIES
   
    
    /**
     * Returns a SearchParams subclass for retrieving all open
     * cases in a given municipality. Open cases are defined as a 
     * case whose closing date is null.
     * @param m
     * @return a SearchParams subclass with mem vars ready to send
     * into the Integrator for case list retrieval
     */
    public SearchParamsCECases getDefaultSearchParams_CECase_allOpen(Municipality m){
        SearchParamsCECases params = new SearchParamsCECases();
        
        // superclass 
        params.setFilterByMuni(true);
        params.setMuni(m);
        params.setFilterByObjectID(false);
        params.setLimitResultCountTo100(true);
        
        // subclass specific
        params.setUseIsOpen(true);
        params.setIsOpen(true);
        
        params.setDateToSearchCECases("Opening date of record");
        params.setUseCaseManager(false);
        
        params.setUseCasePhase(false);
        params.setUseCaseStage(false);
        params.setUseProperty(false);
        params.setUsePropertyInfoCase(false);
        params.setUseCaseManager(false);
        
        return params;
    }
    
     /**
     * Returns a SearchParams subclass for retrieving all open
     * cases in a given municipality. Open cases are defined as a 
     * case whose closing date is null.
     * @param m
     * @return a SearchParams subclass with mem vars ready to send
     * into the Integrator for case list retrieval
     */
    public SearchParamsCECases getSearchParams_CECase_closedPast30Days (Municipality m){
        SearchParamsCECases params = new SearchParamsCECases();
        params.setSearchName("CECases");
        
        // superclass 
        params.setFilterByMuni(true);
        params.setMuni(m);
        params.setFilterByObjectID(false);
        params.setLimitResultCountTo100(true);
        
        // subclass specific
        params.setUseIsOpen(true);
        params.setIsOpen(true);
        
        params.setDateToSearchCECases("Opening date of record");
        params.setUseCaseManager(false);
        
        params.setUseCasePhase(false);
        params.setUseCaseStage(false);
        params.setUseProperty(false);
        params.setUsePropertyInfoCase(false);
        params.setUseCaseManager(false);
        
        return params;
    }
    
   
    
    protected SearchParamsProperties getSearchParamsSkeletonProperties(){
        SearchParamsProperties propParams = new SearchParamsProperties();
        // superclass
        propParams.setFilterByStartEndDate(false);
        propParams.setFilterByObjectID(false);
        propParams.setLimitResultCountTo100(true);
        
        // subclass SearchParamsProperties
        propParams.setFilterByLotAndBlock(false);
        propParams.setFilterByParcelID(false);
        propParams.setFilterByAddressPart(true);
        propParams.setFilterByStreetPart(true);
        propParams.setFilterByCECaseStartEndDate(false);
        propParams.setFilterByRental(false);
        propParams.setFilterByVacant(false);
        propParams.setFilterByUnits(false);
        propParams.setFilterBySource(false);
        propParams.setFilterByPropertyUseType(false);
        propParams.setFilterByPerson(false);
        
        return propParams;
    }
    
    public List<Query> getEventQueryList(User u, Municipality m){
        List<Query> queryList = new ArrayList<>();
        
        QueryEventCECase eq = new QueryEventCECase("Compliance follow-up events: Today", m);
        eq.setEventSearchParams(getSearchParamsEventsRequiringAction(u, m));
        queryList.add(eq);
        
        eq = new QueryEventCECase("Officer Activity Report", m);
        eq.setEventSearchParams(getSearchParamsOfficerActivity(u, m));
        queryList.add(eq);
        
        
        eq = new QueryEventCECase("Compliance events: Past Month", m);
        eq.setEventSearchParams(getSearchParamsComplianceEvPastMonth(m));
        queryList.add(eq);
        
        return queryList;
        
    }
    
    public SearchParamsEventCECase getSearchParamsEventsRequiringAction(User u, Municipality muni){
        EventCoordinator ec = getEventCoordinator();
        
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        EventCategory timelineEventTypeCategory = ec.getInitializedEventCateogry();
        timelineEventTypeCategory.setEventType(EventType.Timeline);
        
        SearchParamsEventCECase eventParams = new SearchParamsEventCECase();
        
        eventParams.setFilterByMuni(true);
        eventParams.setMuni(muni);
        eventParams.setFilterByStartEndDate(false);
        eventParams.setFilterByObjectID(false);
        eventParams.setLimitResultCountTo100(true);
        
        eventParams.setFilterByEventCategory(false);
        eventParams.setFilterByEventType(false);
        
        eventParams.setFilterByCaseID(false);
        
        eventParams.setFilterByEventOwner(false);
        eventParams.setOwnerUserID(u);
        
        eventParams.setFilterByActive(true);
        eventParams.setIsActive(true);
        
        eventParams.setFilterByrequestsAction(true);
        eventParams.setRequestsAction(true);
        
        eventParams.setFilterByHasResponseEvent(true);
        eventParams.setHasResponseEvent(false);
        
        eventParams.setFilterByPerson(false);
        eventParams.setUseRespondedAtDateRange(false);
        
        eventParams.setFilterByHidden(false);
        
        return eventParams;
    }
    
    public SearchParamsEventCECase getSearchParamsOfficerActivity(User u, Municipality m){
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        
        SearchParamsEventCECase eventParams = new SearchParamsEventCECase();
        
        eventParams.setFilterByMuni(true);
        eventParams.setMuni(m);
        
        eventParams.setFilterByStartEndDate(true);
        eventParams.setUseRelativeDates(true);
        
        eventParams.setUseDateOfRecord(true);
        // query from a week ago to now
        eventParams.setStartDateRelativeDays(-30);
        eventParams.setEndDateRelativeDays(0);
        
        eventParams.setFilterByObjectID(false);
        eventParams.setLimitResultCountTo100(true);
        
        eventParams.setFilterByEventCategory(false);
        eventParams.setFilterByEventType(false);
        
        eventParams.setFilterByCaseID(false);
        
        eventParams.setFilterByEventOwner(true);
        eventParams.setOwnerUserID(u);
        
        eventParams.setFilterByActive(true);
        eventParams.setIsActive(true);
        
        eventParams.setFilterByrequestsAction(false);
        
        eventParams.setFilterByHasResponseEvent(false);
        eventParams.setHasResponseEvent(false);
        
        eventParams.setFilterByPerson(false);
        eventParams.setUseRespondedAtDateRange(false);
        
        eventParams.setFilterByHidden(false);
        
        return eventParams;
    }
    
    
    public SearchParamsEventCECase getSearchParamsComplianceEvPastMonth(Municipality m){
        EventCoordinator ec = getEventCoordinator();
        
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        EventCategory complianceEventCategory = ec.getInitializedEventCateogry();
        complianceEventCategory.setEventType(EventType.Compliance);
        
        
        SearchParamsEventCECase eventParams = new SearchParamsEventCECase();
        
        eventParams.setFilterByMuni(true);
        eventParams.setMuni(m);
        eventParams.setFilterByStartEndDate(true);
        eventParams.setUseRelativeDates(true);
        eventParams.setUseDateOfRecord(true);
        // query from a week ago to now
        eventParams.setStartDateRelativeDays(-400);
        eventParams.setEndDateRelativeDays(0);
        
        eventParams.setFilterByObjectID(false);
        eventParams.setLimitResultCountTo100(true);
        
        eventParams.setFilterByEventCategory(false);
        eventParams.setFilterByEventType(true);
        eventParams.setEvtType(EventType.Compliance);
        
        eventParams.setFilterByCaseID(false);
        
        eventParams.setFilterByEventOwner(false);
        
        eventParams.setFilterByActive(true);
        eventParams.setIsActive(true);
        
        eventParams.setFilterByrequestsAction(false);
        
        eventParams.setFilterByHasResponseEvent(false);
        eventParams.setHasResponseEvent(false);
        
        eventParams.setFilterByPerson(false);
        eventParams.setUseRespondedAtDateRange(false);
        
        eventParams.setFilterByHidden(false);
        
        return eventParams;
    }

   
    
    
}
