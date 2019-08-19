/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.CaseLifecycleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.Query;
import com.tcvcog.tcvce.entities.search.QueryCEAR;
import com.tcvcog.tcvce.entities.search.QueryCEAREnum;
import com.tcvcog.tcvce.entities.search.QueryCECase;
import com.tcvcog.tcvce.entities.search.QueryCECaseEnum;
import com.tcvcog.tcvce.entities.search.QueryEventCECase;
import com.tcvcog.tcvce.entities.search.QueryEventCECaseEnum;
import com.tcvcog.tcvce.entities.search.QueryOccPeriod;
import com.tcvcog.tcvce.entities.search.QueryOccPeriodEnum;
import com.tcvcog.tcvce.entities.search.QueryPerson;
import com.tcvcog.tcvce.entities.search.QueryPersonEnum;
import com.tcvcog.tcvce.entities.search.QueryProperty;
import com.tcvcog.tcvce.entities.search.QueryPropertyEnum;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.entities.search.SearchParamsCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsEventCECase;
import com.tcvcog.tcvce.entities.search.SearchParamsOccPeriod;
import com.tcvcog.tcvce.entities.search.SearchParamsPerson;
import com.tcvcog.tcvce.entities.search.SearchParamsProperty;
import com.tcvcog.tcvce.entities.search.SearchParamsPropertyDateFields;
import com.tcvcog.tcvce.integration.CEActionRequestIntegrator;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import com.tcvcog.tcvce.occupancy.integration.OccupancyIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    
     public QueryCEAR getQueryInitialCEAR(User u, Municipality m) throws IntegrationException{
        return assembleQueryCEAR(QueryCEAREnum.UNPROCESSED, u, m, null);
        
    }
    
    /**
     * These are just skeleton subQuery objects for Action Requests used for
     * user selection drop down boxes on faces pages
     * @param u session user
     * @param m session user's current municipality
     * @return each existing CEAR Query capable of being passed into runQuery()
     */
    public List<QueryCEAR> buildQueryCEARList(User u, Municipality m) throws IntegrationException{
        QueryCEAREnum[] nameArray = QueryCEAREnum.values();
        List<QueryCEAR> queryList = new ArrayList<>();
//        qList.add(assembleQueryCEAR(QueryCEAREnum.ALL_PAST30, u, m));
        for(QueryCEAREnum queryTitle: nameArray){
            // THE FACTORY CALL for QueryCEAR objects!!!!!!!!!!!!!!
            queryList.add(assembleQueryCEAR(queryTitle, u, m, null));
        }
        return queryList;
    }
    
    /**
     * Single point of entry for queries on Code Enforcement Requests
     * 
     * @param query an assembled QueryCEAR
     * @return a reference to the same QueryCEAR instance passed in with the business
     * objects returned from the integrator accessible via getResults()
     * @throws AuthorizationException thrown when the quering User's rank is below the Query's 
     * minimum required rank accessible via queryinstance.getUserRankAccessMinimum()
     * @throws IntegrationException fatal error in the integration code
     */
    public QueryCEAR runQuery(QueryCEAR query) throws AuthorizationException, IntegrationException{
        query.clearResultList();
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
//        if(query.getUser().getRoleType().getRank() > query.getQueryName().getUserRankMinimum() ){
        //TODO: get this to actually work
        if(query.getUser().getRoleType().getRank() >9999 ){
            throw new AuthorizationException("User/owner of query does not meet rank minimum specified by the Query");
        }
        
        if(query.getQueryName().logQueryRun()){
            logRun(query);
        }
        return ceari.runQueryCEAR(query);
    }
    
    public void logRun(Query q){
        // TODO: write guts for query logging
    }
    
    /**
     * Factory method for Query subclass: QueryCEAR.
     * 
     * @param qName an instance of the Enum QueryCEAREnum which is used by a switch
     * to grab the assigned SearchParams subclass
     * @param u the requesting User
     * @param m the requesting Municipality. NOTE: It's up to the CaseCoordinator
     * to enforce access rules here
     * @param params optional params. If this object is not null, the query will
     * automatically become a custom query
     * @return assembled instance ready for sending to runQuery
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public QueryCEAR assembleQueryCEAR(QueryCEAREnum qName, User u, Municipality m, SearchParamsCEActionRequests params) throws IntegrationException{
        QueryCEAR query;
        List<SearchParamsCEActionRequests> paramList = new ArrayList<>();
        
        if(params != null){
            qName = QueryCEAREnum.CUSTOM;
        }
        
        switch(qName){
            case UNPROCESSED:
                paramList.add(generateParams_CEAR_Unprocessed(m));
                break;
            case ATTACHED_TO_CECASE:
                paramList.add(generateParams_CEAR_attachedToCase(m, 30));
                break;
            
            case ALL_TODAY:
                paramList.add(generateParams_CEAR_pastXDays(m, 1));
                break;
                
            case ALL_PAST7DAYS:
                paramList.add(generateParams_CEAR_pastXDays(m, 7));
                break;
                
            case ALL_PAST30:
                paramList.add(generateParams_CEAR_pastXDays(m, 30));
                break;

            case ALL_PASTYEAR:
                paramList.add(generateParams_CEAR_pastXDays(m, 365));
                break;
                
            case BY_CURRENT_USER:
                paramList.add(generateParams_CEAR_Unprocessed(m));
                break;
                
            case CUSTOM:
                paramList.add(params);
                break;
                
            default:
                paramList.add(generateParams_CEAR_Unprocessed(m));
        }
        
        query = new QueryCEAR(qName, m, paramList, u);
        query.setExecutedByIntegrator(false);
        return query;
    }
    
    
    
    
     public SearchParamsCEActionRequests generateParams_CEAR_Unprocessed(Municipality m) throws IntegrationException{
            
        CEActionRequestIntegrator cari = getcEActionRequestIntegrator();

        SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

        sps.setMuni(m);
        LocalDateTime pastTenYears = LocalDateTime.now().minusYears(10);
        sps.setStartDate(pastTenYears);
        // action requests cannot have a time stamp past the current datetime
        sps.setEndDate(LocalDateTime.now());
        
        sps.setLimitResultCountTo100(true);
        sps.setUseAttachedToCase(false);
        sps.setAttachedToCase(false);
        sps.setUseMarkedUrgent(false);
        sps.setUseNotAtAddress(false);
        sps.setUseRequestStatus(true);
        sps.setRequestStatus(cari.getRequestStatus(Integer.parseInt(
                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("actionRequestInitialStatusCode"))));
        
        return sps;
    }
    
     /**
      * TODO : Finish!
      * @param m
      * @param u
      * @return
      * @throws IntegrationException 
      */
     public SearchParamsCEActionRequests generateParams_CEAR_RequestorCurrentU(Municipality m, User u) throws IntegrationException{
            
        CEActionRequestIntegrator cari = getcEActionRequestIntegrator();

        SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

        sps.setMuni(m);
        LocalDateTime pastTenYears = LocalDateTime.now().minusDays(30);
        // action requests cannot have a time stamp past the current datetime
        sps.setEndDate(LocalDateTime.now());
        
        sps.setLimitResultCountTo100(true);
        sps.setUseAttachedToCase(false);
        sps.setAttachedToCase(false);
        sps.setUseMarkedUrgent(false);
        sps.setUseNotAtAddress(false);
        
        
        return sps;
    }
    
    
    
     public SearchParamsCEActionRequests generateParams_CEAR_pastXDays(Municipality m, int days){
            
        SearchCoordinator sc = getSearchCoordinator();

        SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

        sps.setMuni(m);
        LocalDateTime pastxDays = LocalDateTime.now().minusDays(days);
        sps.setStartDate(pastxDays);

        // action requests cannot have a time stamp past the current datetime
        sps.setEndDate(LocalDateTime.now());

        sps.setUseAttachedToCase(false);
        sps.setUseMarkedUrgent(false);
        sps.setUseNotAtAddress(false);
        sps.setUseRequestStatus(false);
        
        return sps;
    }
    
    
     public SearchParamsCEActionRequests generateParams_CEAR_attachedToCase(Municipality m, int days){
            
        SearchCoordinator sc = getSearchCoordinator();

        SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

        sps.setMuni(m);
        LocalDateTime pastxDays = LocalDateTime.now().minusDays(days);
        sps.setStartDate(pastxDays);

        // action requests cannot have a time stamp past the current datetime
        sps.setEndDate(LocalDateTime.now());

        sps.setUseAttachedToCase(true);
        sps.setAttachedToCase(true);
        
        sps.setUseMarkedUrgent(false);
        sps.setUseNotAtAddress(false);
        sps.setUseRequestStatus(false);
        
        return sps;
    }
    
//    CODE ENFORCEMENT CASE QUERIES
   
     public QueryCECase getQueryInitialCECASE(Municipality m, User u){
         return assembleQueryCECase(QueryCECaseEnum.OPENCASES, u, m, null);
         
     }
     
     public List<QueryCECase> buildQueryCECaseList(Municipality m, User u){
        QueryCECaseEnum[] nameArray = QueryCECaseEnum.values();
        List<QueryCECase> queryList = new ArrayList<>();
//        for(QueryCECaseEnum queryTitle: nameArray){
//            // THE FACTORY CALL for QueryCEAR objects!!!!!!!!!!!!!!
//            queryList.add(assembleQueryCECase(queryTitle, u, m, null));
//        }
        queryList.add(assembleQueryCECase(QueryCECaseEnum.OPENCASES, u, m, null));
        return queryList;
     }
     
     public QueryCECase runQuery(QueryCECase query) throws IntegrationException, CaseLifecycleException{
         query.clearResultList();
         CaseIntegrator ci = getCaseIntegrator();
         if(query.getQueryName().logQueryRun()){
            logRun(query);
        }
        return ci.runQueryCECase(query);
     }
     
     public QueryCECase assembleQueryCECase(QueryCECaseEnum qName, User u, Municipality m, SearchParamsCECase params){
         QueryCECase query;
         List<SearchParamsCECase> paramsList = new ArrayList<>();
         
         if(params != null){
             qName = QueryCECaseEnum.CUSTOM;
         }
         
         switch(qName){
            case OPENCASES:
                paramsList.add(getDefaultSearchParams_CECase_allOpen(m));
                 break;
            case EXPIRED_TIMEFRAMES:
                break;
            case CURRENT_TIMEFRAMES:
                break;
            case OPENED_30DAYS:
                break;
            case CLOSED_30DAYS:
                paramsList.add(getSearchParams_CECase_closedPast30Days(m));
                break;
            case UNRESOLVED_CITATIONS:
                break;
            case ANY_ACTIVITY_7Days:
                break;
            case ANY_ACTIVITY_30Days:
                break;
            case CUSTOM:
                break;
            default:
         }
         
         query = new QueryCECase(qName, m, paramsList, u);
         query.setExecutedByIntegrator(false);
         return query;
     }
     
     
    
    /**
     * Returns a SearchParams subclass for retrieving all open
     * cases in a given municipality. Open cases are defined as a 
     * case whose closing date is null.
     * @param m
     * @return a SearchParams subclass with mem vars ready to send
     * into the Integrator for case list retrieval
     */
    public SearchParamsCECase getDefaultSearchParams_CECase_allOpen(Municipality m){
        SearchParamsCECase params = new SearchParamsCECase();
        
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
    public SearchParamsCECase getSearchParams_CECase_closedPast30Days (Municipality m){
        SearchParamsCECase params = new SearchParamsCECase();
        params.setSearchName("CECases");
        
        // superclass 
        params.setFilterByMuni(true);
        params.setMuni(m);
        params.setFilterByObjectID(false);
        params.setLimitResultCountTo100(true);
        
        // subclass specific
        params.setUseIsOpen(false);
        
        params.setDateToSearchCECases("Closing date");
        params.setUseCaseManager(false);
        
        LocalDateTime pastXDays = LocalDateTime.now().minusDays(30);
        
        params.setStartDate(pastXDays);
        params.setEndDate(LocalDateTime.now());
        
        params.setUseCasePhase(false);
        params.setUseCaseStage(false);
        params.setUseProperty(false);
        params.setUsePropertyInfoCase(false);
        params.setUseCaseManager(false);
        
        return params;
    }
    
   
    
    protected SearchParamsProperty getSearchParamsSkeletonProperties(){
        SearchParamsProperty propParams = new SearchParamsProperty();
        // superclass
        propParams.setFilterByStartEndDate(false);
        propParams.setFilterByObjectID(false);
        propParams.setLimitResultCountTo100(true);
        
        // subclass SearchParamsProperty
        propParams.setFilterByLotAndBlock(false);
        propParams.setFilterByParcelID(false);
        propParams.setFilterByAddressPart(true);

        
        return propParams;
    }
    
    
    // CODE ENFORCEMENT EVENTS
    
     public QueryEventCECase getQueryInitialEventCECASE(Municipality m, User u){
         return assembleQueryEventCECase(QueryEventCECaseEnum.REQUESTED_ACTIONS, u, m, null);
         
     }
     
     public List<QueryEventCECase> buildQueryEventCECaseList(Municipality m, User u){
        QueryEventCECaseEnum[] nameArray = QueryEventCECaseEnum.values();
        List<QueryEventCECase> queryList = new ArrayList<>();
        for(QueryEventCECaseEnum queryTitle: nameArray){
            // THE FACTORY CALL for QueryCEAR objects!!!!!!!!!!!!!!
            queryList.add(assembleQueryEventCECase(queryTitle, u, m, null));
        }

        return queryList;
         
     }
     
     public QueryEventCECase runQuery(QueryEventCECase query) throws IntegrationException, CaseLifecycleException{
         EventIntegrator ei = getEventIntegrator();
         query.clearResultList();
         
         if(query.getQueryName().logQueryRun()){
            logRun(query);
        }
        return ei.runQueryEventCECase(query);
     }
     
     public QueryEventCECase assembleQueryEventCECase(QueryEventCECaseEnum qName, User u, Municipality m, SearchParamsEventCECase params){
         QueryEventCECase query;
         List<SearchParamsEventCECase> paramsList = new ArrayList<>();
         
         if(params != null){
             qName = QueryEventCECaseEnum.CUSTOM;
         }
         
         switch(qName){
             case REQUESTED_ACTIONS:
                paramsList.add(getSearchParamsEventsRequiringAction(u, m));
                break;
             case MUNICODEOFFICER_ACTIVITY_PAST30DAYS:
                 paramsList.add(getSearchParamsOfficerActivity(u, m));
                 break;
             case COMPLIANCE_EVENTS:
                 paramsList.add(getSearchParamsComplianceEvPastMonth(m));
                 break;
            case CUSTOM:
                paramsList.add(params);
                break;
            default:
         }
         
         query = new QueryEventCECase(qName, m, u, paramsList);
         query.setExecutedByIntegrator(false);
         return query;
     }
    
    /**
     * First gen queries. Replaced by the rest of this class
     * 
     * @deprecated 
     * @param u
     * @param m
     * @return 
     */
    public List<Query> getEventQueryList(User u, Municipality m){
        List<Query> queryList = new ArrayList<>();
//        
//        QueryEventCECase eq = new QueryEventCECase("Compliance follow-up events: Today", m);
//        eq.setEventSearchParams(getSearchParamsEventsRequiringAction(u, m));
//        queryList.add(eq);
//        
//        eq = new QueryEventCECase("Officer Activity Report", m);
//        eq.setEventSearchParams(getSearchParamsOfficerActivity(u, m));
//        queryList.add(eq);
//        
//        
//        eq = new QueryEventCECase("Compliance events: Past Month", m);
//        eq.setEventSearchParams(getSearchParamsComplianceEvPastMonth(m));
//        queryList.add(eq);
        
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
        
        eventParams.setActive_filterBy(true);
        eventParams.setIsActive(true);
        
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
        
        eventParams.setApplyDateSearchToDateOfRecord(true);
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
        
        eventParams.setActive_filterBy(true);
        eventParams.setIsActive(true);
        
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
        eventParams.setApplyDateSearchToDateOfRecord(true);
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
        
        eventParams.setActive_filterBy(true);
        eventParams.setIsActive(true);
        
        eventParams.setFilterByPerson(false);
        eventParams.setUseRespondedAtDateRange(false);
        
        eventParams.setFilterByHidden(false);
        
        return eventParams;
    }

    
    public QueryOccPeriod assembleQueryOccPeriod(QueryOccPeriodEnum qName, User u, Municipality m, SearchParamsOccPeriod params){
         QueryOccPeriod  query;
         List<SearchParamsOccPeriod> paramsList = new ArrayList<>();
         
         if(params != null){
             qName = QueryOccPeriodEnum.CUSTOM;
         }
         
         switch(qName){
            case AUTHWORKINPROGRESS:
                paramsList.add(generateParams_occPeriod_wip(m));
                break;
            case RENTAL_ALL:
                break;
            case RENTAL_REGISTERED:
                break;
            case RENTAL_UNREGISTERED:
                break;
            
         }
         query = new QueryOccPeriod(qName, m, paramsList, u);
         query.setExecutedByIntegrator(false);
         return query;
     }
    
    private SearchParamsOccPeriod generateParams_occPeriod_wip(Municipality m){
        SearchParamsOccPeriod params = new SearchParamsOccPeriod();
        
        params.setSearchName("Periods with outstanding inspections");
        params.setSearchDescription("Inspections have been started by not certified as passed");
        
        params.setInspectionPassed_filterBy(true);
        params.setInspectionPassed_switch_passedInspection(false);
        
        return params;
        
    }
    
    public List<QueryOccPeriod> buildQueryOccPeriodList(User u, Municipality m) throws IntegrationException{
        QueryOccPeriodEnum[] nameArray = QueryOccPeriodEnum.values();
        List<QueryOccPeriod> queryList = new ArrayList<>();
        for(QueryOccPeriodEnum queryTitle: nameArray){
            // THE FACTORY CALL for QueryCEAR objects!!!!!!!!!!!!!!
            queryList.add(assembleQueryOccPeriod(queryTitle, u, m, null));
        }
        return queryList;
    }
    
    /**
     * Single point of entry for queries on Occupancy Periods
     * 
     * @param query an assembled QueryOccPeriod
     * @return a reference to the same QueryOccPeriod instance passed in with the business
     * objects returned from the integrator accessible via getResults()
     * @throws AuthorizationException thrown when the quering User's rank is below the Query's 
     * minimum required rank accessible via queryinstance.getUserRankAccessMinimum()
     * @throws IntegrationException fatal error in the integration code
     */
    public QueryOccPeriod runQuery(QueryOccPeriod query, User u) throws AuthorizationException, IntegrationException, EventException{
        QueryOccPeriod qop = null;
        query.clearResultList();
        OccupancyIntegrator oi = getOccupancyIntegrator();
//        if(query.getUser().getRoleType().getRank() > query.getQueryName().getUserRankMinimum() ){
        //TODO: get this to actually work
        if(query.getUser().getRoleType().getRank() < query.getUserRankAccessMinimum().getRank() ){
            throw new AuthorizationException("User/owner of query does not meet rank minimum specified by the Query");
        }
        
        if(query.getQueryName().logQueryRun()){
            logRun(query);
        }
        try {
            qop = oi.runQueryOccPeriod(query, u);
        } catch (CaseLifecycleException | ViolationException ex) {
            System.out.println(ex);
        }
        
        return qop;
    }
    
    
    public QueryPerson assembleQueryPerson(QueryPersonEnum qName, User u, Municipality m, SearchParamsPerson params){
         QueryPerson  query;
         List<SearchParamsPerson> paramsList = new ArrayList<>();
         
         if(params != null){
             qName = QueryPersonEnum.CUSTOM;
         }
         
         switch(qName){
            case ACTIVE_PERSONS:
                paramsList.add(generateParams_persons_active(m));
                break;
            
         }
         query = new QueryPerson(qName, m, paramsList, u);
         query.setExecutedByIntegrator(false);
         return query;
     }
    
    private SearchParamsPerson generateParams_persons_active(Municipality m){
        SearchParamsPerson params = new SearchParamsPerson();
        
        params.setSearchName("Public person types");
        params.setSearchDescription("All persons declared to be public");
        
        params.setFilterByPersonTypes(true);
        List<PersonType> pList = new ArrayList<>();
        pList.add(PersonType.Public);
        params.setPersonTypes(pList);
        
        return params;
        
    }
    
    public List<QueryPerson> buildQueryPersonList(User u, Municipality m) throws IntegrationException{
        QueryPersonEnum[] nameArray = QueryPersonEnum.values();
        List<QueryPerson> queryList = new ArrayList<>();
        for(QueryPersonEnum queryTitle: nameArray){
            // THE FACTORY CALL for QueryCEAR objects!!!!!!!!!!!!!!
            queryList.add(assembleQueryPerson(queryTitle, u, m, null));
        }
        return queryList;
    }
    
    /**
     * Single point of entry for queries on Person objects
     * 
     * @param query an assembled QueryOccPeriod
     * @return a reference to the same QueryOccPeriod instance passed in with the business
     * objects returned from the integrator accessible via getResults()
     * @throws AuthorizationException thrown when the quering User's rank is below the Query's 
     * minimum required rank accessible via queryinstance.getUserRankAccessMinimum()
     * @throws IntegrationException fatal error in the integration code
     */
    public QueryPerson runQuery(QueryPerson query) throws AuthorizationException, IntegrationException{
        query.clearResultList();
        PersonIntegrator pi = getPersonIntegrator();
        if(query.getUser().getRoleType().getRank() < query.getUserRankAccessMinimum().getRank() ){
            throw new AuthorizationException("User/owner of query does not meet rank minimum specified by the Query");
        }
        
        if(query.getQueryName().logQueryRun()){
            logRun(query);
        }
        return pi.runQueryPerson(query);
    }
    
     
    public QueryProperty assembleQueryProperty(QueryPropertyEnum qName, User u, Municipality m, SearchParamsProperty params){
         QueryProperty  query;
         List<SearchParamsProperty> paramsList = new ArrayList<>();
         
         if(params != null){
             qName = QueryPropertyEnum.CUSTOM;
         }
         
         switch(qName){
            case OPENCECASES_OCCPERIODSINPROCESS:
                paramsList.add(generateParams_property_active(m));
                break;
            case CUSTOM:
                paramsList.add(params);
                break;
            
         }
         query = new QueryProperty(qName, m, paramsList, u);
         query.setExecutedByIntegrator(false);
         return query;
     }
    
    private SearchParamsProperty generateParams_property_active(Municipality m){
        SearchParamsProperty params = new SearchParamsProperty();
        
        params.setSearchName("Properties updated in the past month");
        params.setSearchDescription("Applies to properties with any field updated");
        
        params.setFilterByMuni(true);
        params.setMuni(m);
        
        params.setFilterByStartEndDate(true);
        params.setUseRelativeDates(true);
        params.setDateField(SearchParamsPropertyDateFields.LAST_UPDATED);
        params.setStartDateRelativeDays(-30);
        params.setEndDateRelativeDays(0);
        
        params.setActive_filterBy(true);
        params.setActive(true);
        

        return params;
        
    }
    
    public List<QueryProperty> buildQueryPropertyList(User u, Municipality m) throws IntegrationException{
        QueryPropertyEnum[] nameArray = QueryPropertyEnum.values();
        List<QueryProperty> queryList = new ArrayList<>();
        for(QueryPropertyEnum queryTitle: nameArray){
            // THE FACTORY CALL for QueryCEAR objects!!!!!!!!!!!!!!
            queryList.add(assembleQueryProperty(queryTitle, u, m, null));
        }
        return queryList;
    }
    
    /**
     * Single point of entry for queries on Property objects
     * 
     * @param query an assembled QueryOccPeriod
     * @return a reference to the same QueryOccPeriod instance passed in with the business
     * objects returned from the integrator accessible via getResults()
     * @throws AuthorizationException thrown when the quering User's rank is below the Query's 
     * minimum required rank accessible via queryinstance.getUserRankAccessMinimum()
     * @throws IntegrationException fatal error in the integration code
     */
    public QueryProperty runQuery(QueryProperty query) throws AuthorizationException, IntegrationException{
        query.clearResultList();
        PropertyIntegrator pi = getPropertyIntegrator();
        if(query.getUser().getRoleType().getRank() < query.getUserRankAccessMinimum().getRank() ){
            throw new AuthorizationException("User/owner of query does not meet rank minimum specified by the Query");
        }
        
        if(query.getQueryName().logQueryRun()){
            logRun(query);
        }
        return pi.runQueryProperties(query);
    }
    
}
