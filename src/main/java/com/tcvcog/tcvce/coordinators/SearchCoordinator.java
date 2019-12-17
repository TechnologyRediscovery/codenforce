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
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.Event;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.search.*;
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
    
    
    
//    --------------------------------------------------------------------------
//    ***************************** RUN QUERIES ********************************
//    --------------------------------------------------------------------------
       
    /**
     * Single point of entry for queries on Property objects
     * 
     * @param q an assembled QueryOccPeriod
     * @return a reference to the same QueryOccPeriod instance passed in with the business
     * objects returned from the integrator accessible via getResults()
     * @throws AuthorizationException thrown when the quering User's rank is below the Query's 
     * minimum required rank accessible via queryinstance.getUserRankAccessMinimum()
     * @throws IntegrationException fatal error in the integration code
     */
    public QueryProperty runQuery(QueryProperty q) throws AuthorizationException, IntegrationException, SearchException{
        PropertyIntegrator pi = getPropertyIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();
        
        if(q == null) return null;
        
        prepareQueryForRun(q);

        List<SearchParamsProperty> paramsList = q.getParmsList();
        List<Property> propTempList = new ArrayList<>();
        
        for(SearchParamsProperty sp: paramsList){
            propTempList.clear();
            // audit the params and get the result list back
            for(Integer i: pi.searchForProperties(sp)){
                propTempList.add(pc.getProperty(i));
            }
            // add each batch of OccPeriod objects from the SearchParam run to our
            // ongoing list
            q.addToResults(propTempList);
        }
        
        postRunConfigureQuery(q);
        
        return q;
    }
    
      /**
     * Single point of entry for queries on Person objects
     * 
     * @param q an assembled QueryOccPeriod
     * @return a reference to the same QueryOccPeriod instance passed in with the business
     * objects returned from the integrator accessible via getResults()
     * @throws AuthorizationException thrown when the quering User's rank is below the Query's 
     * minimum required rank accessible via queryinstance.getUserRankAccessMinimum()
     * @throws IntegrationException fatal error in the integration code
     */
    public QueryPerson runQuery(QueryPerson q) throws AuthorizationException, IntegrationException, SearchException{
        PersonIntegrator pi = getPersonIntegrator();
        PersonCoordinator pc = getPersonCoordinator();
        
        if(q == null) return null;
        
        prepareQueryForRun(q);

        List<SearchParamsPerson> paramsList = q.getParmsList();
        List<Person> persTempList = new ArrayList<>();
        
        for(SearchParamsPerson sp: paramsList){
            persTempList.clear();
            // audit the params and get the result list back
            for(Integer i: pi.searchForPersons(sp)){
                persTempList.add(pc.getPerson(i));
            }
            // add each batch of OccPeriod objects from the SearchParam run to our
            // ongoing list
            q.addToResults(persTempList);
        }
        
        postRunConfigureQuery(q);
        
        return q;
    }
     
     public QueryEvent runQuery(QueryEvent q) throws IntegrationException, CaseLifecycleException, SearchException{
         EventIntegrator ei = getEventIntegrator();
         EventCoordinator ec = getEventCoordinator();
         
         if(q == null) return null;
        
        prepareQueryForRun(q);

        List<SearchParamsEvent> paramsList = q.getParmsList();
        List<Event> evTempList = new ArrayList<>();
        
        for(SearchParamsEvent sp: paramsList){
            evTempList.clear();
            // audit the params and get the result list back
            for(Integer i: ei.searchForEvents(sp)){
                evTempList.add(ec.getEvent(i));
            }
            // add each batch of OccPeriod objects from the SearchParam run to our
            // ongoing list
            q.addToResults(evTempList);
        }
        
        postRunConfigureQuery(q);
        
        return q;
     }
    
     /**
     * Single point of entry for queries on Occupancy Periods
     * As of Dec 2019 this is the first of the runQuery methods to implement
     * the upgraded separation of concerns such that Integration classes ONLY
     * see SearchParam objects and leave all the manipulation of Query objects
     * to this Search Coordinator. 
     * 
     * Additionally, the runQuery methods in this class are responsible for
     * interacting with the appropriate object Coordinators to retrieve and properly 
     * configure any objects before injecting them into the Query objects and 
     * returning them to the callers.
     * 
     * @param q an assembled QueryOccPeriod
     * @return a reference to the same QueryOccPeriod instance passed in with the business
     * objects returned from the integrator accessible via getResults()
     * @throws AuthorizationException thrown when the quering User's rank is below the Query's 
     * minimum required rank accessible via queryinstance.getUserRankAccessMinimum()
     * @throws IntegrationException fatal error in the integration code
     */
    public QueryOccPeriod runQuery(QueryOccPeriod q) throws AuthorizationException, IntegrationException, EventException, SearchException{
        OccupancyIntegrator oi = getOccupancyIntegrator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        
        if(q == null){
            return null;
        }
        
        prepareQueryForRun(q);

        List<SearchParamsOccPeriod> paramsList = q.getParmsList();
        List<OccPeriod> periodListTemp = new ArrayList<>();
        
        for(SearchParamsOccPeriod sp: paramsList){
            periodListTemp.clear();
            // audit the params and get the result list back
            for(Integer i: oi.searchForOccPeriods(sp)){
                periodListTemp.add(oc.getOccPeriod(i));
            }
            // add each batch of OccPeriod objects from the SearchParam run to our
            // ongoing list
            q.addToResults(periodListTemp);
        }
        
        postRunConfigureQuery(q);
        
        return q;
    }
    
    
    
     
     public QueryCECase runQuery(QueryCECase q) throws IntegrationException, CaseLifecycleException, AuthorizationException, SearchException{
        CaseIntegrator ci = getCaseIntegrator();
        
        prepareQueryForRun(q);
        
        q = ci.runQueryCECase(q);
        
         postRunConfigureQuery(q);
        return q;
     }
     
    /**
     * Single point of entry for queries on Code Enforcement Requests
     * 
     * @param q must be initialized
     * @param cred
     * @return a reference to the same QueryCEAR instance passed in with the business
     * objects returned from the integrator accessible via getResults()
     * @throws AuthorizationException thrown when the quering User's rank is below the Query's 
     * minimum required rank accessible via queryinstance.getUserRankAccessMinimum()
     * @throws IntegrationException fatal error in the integration code
     */
    public QueryCEAR runQuery(QueryCEAR q) throws AuthorizationException, IntegrationException, SearchException{
        prepareQueryForRun(q);
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();

        postRunConfigureQuery(q);
        return ceari.runQueryCEAR(q);
    }
    
//    --------------------------------------------------------------------------
//    ***************************** UTILITIES **********************************
//    --------------------------------------------------------------------------
    
    
    private void prepareQueryForRun(Query q) throws SearchException{
        q.clearResultList();
        auditQueryBeforeRun(q);
        
    }
    
    
    /**
     * Security and authorization logic container -- throws exceptions for all errors
     * Query must have a Credential object and the Credential must meet rank requirements
     * for the Query
     * 
     * @param q happy to accept any subclass :)
     */
    private void auditQueryBeforeRun(Query q) throws SearchException{
        if(q == null){
            throw new SearchException("A null query object is not authorized for running");
        }
        if( q.getCredential().getGoverningAuthPeriod().getRole().getRank() 
                    < 
                q.getUserRankAccessMinimum().getRank()){
            
            throw new SearchException("Credential below rank for running query");
        }
        auditSearchParams(q);
        
    }
 
    /**
     * Logic container for checking the configuration of any instance of the 
     * SearchParams family of objects. Only throws exceptions for errors with
     * useful error messages meant for the testing user. Normal query operations
     * should not trigger an audit Exception since configXXX methods should 
     * be doing their jobs
     * 
     * @param q
     * @throws SearchException 
     */
    private void auditSearchParams(Query q) throws SearchException{
        
        List<SearchParams> splst = new ArrayList<>();
        for(Object o: q.getParmsList()){
            splst.add((SearchParams) o);
        }
        
        for(SearchParams sp: splst){
            // unless you're COGStaff or higher, you can only search in your 
            // authorized Muni
            if(!q.getCredential().isHasCOGStaffPermissions()){
                if(     !sp.isFilterByMuni() 
                            && 
                        sp.getMuni() != null 
                            && 
                        sp.getMuni().getMuniCode() != q.getCredential().getGoverningAuthPeriod().getMuni().getMuniCode()){
                    throw new SearchException("Params run by credentials below CogStaff must filter by their muni");
                }                
            }
        }        
    }
    
    public void postRunConfigureQuery(Query q){
        q.setExecutionTimestamp(LocalDateTime.now());
        q.setExecutedByIntegrator(true);
        logQueryRun(q);
        
    }
    
    public void logQueryRun(Query q){
        // TODO: write guts for query logging
    }
    
  
  
    
//    --------------------------------------------------------------------------
//    ***************************** INITIALIZERS *******************************
//    --------------------------------------------------------------------------
    
     
    
    public QueryPerson initQuery(QueryPersonEnum qName){
         QueryPerson  query;
         List<SearchParamsPerson> paramsList = new ArrayList<>();
         RoleType rt = null;
         
         if(params != null){
             qName = QueryPersonEnum.CUSTOM;
         }
         
         switch(qName){
            case ACTIVE_PERSONS:
                paramsList.add(generateParams_persons_active(m));
                rt = RoleType.MuniReader;
                break;
            case USER_PERSONS:
                paramsList.add(generateParams_persons_users());
                rt = RoleType.MuniReader;
            
         }
         query = new QueryPerson(qName, m, paramsList, u);
         query.setUserRankAccessMinimum(rt);
         query.setExecutedByIntegrator(false);
         return query;
     }
     
    
     public QueryEvent initQuery(QueryEventEnum qName){
         QueryEvent query;
         List<SearchParamsEvent> paramsList = new ArrayList<>();
         
//         if(params != null){
//             qName = QueryEventEnum.CUSTOM;
//         }
         
         switch(qName){
             case REQUESTED_ACTIONS:
                paramsList.add(getSearchParamsEventsRequiringAction(cred, m));
                break;
             case MUNICODEOFFICER_ACTIVITY_PAST30DAYS:
                 paramsList.add(getSearchParamsOfficerActivity(cred, m));
                 break;
             case COMPLIANCE_EVENTS:
                 paramsList.add(getSearchParamsComplianceEvPastMonth(m));
                 break;
            case CUSTOM:
                paramsList.add(params);
                break;
            default:
         }
         
         query = new QueryEvent(qName, m, cred, paramsList);
         query.setExecutedByIntegrator(false);
         return query;
     }
     
    
     
     
     public QueryCECase initQuery(QueryCECaseEnum qName){
         QueryCECase query;
         List<SearchParamsCECase> paramsList = new ArrayList<>();
         
         if(params != null){
             qName = QueryCECaseEnum.CUSTOM;
         }
         
         switch(qName){
            case OPENCASES:
                paramsList.add(getDefaultSearchParams_CECase_allOpen(cred));
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
         
         query = new QueryCECase(qName, m, paramsList, cred);
         query.setExecutedByIntegrator(false);
         return query;
     }
     
     
    
     /**
     * Factory method for Query subclass: QueryCEAR.
     * 
     * @param qName an instance of the Enum QueryCEAREnum which is used by a switch
     * to grab the assigned SearchParams subclass
     * @param cred
     * @param m the requesting Municipality. NOTE: It's up to the CaseCoordinator
     * to enforce access rules here
     * @param params optional params. If this object is not null, the query will
     * automatically become a custom query
     * @return assembled instance ready for sending to runQuery
     */
    public QueryCEAR initQuery( QueryCEAREnum qName, 
                                        Credential cred, 
                                        SearchParamsCEActionRequests params) {
            QueryCEAR query = null;
            List<SearchParamsCEActionRequests> paramList = new ArrayList<>();
            
            if(params != null){
                qName = QueryCEAREnum.CUSTOM;
            }
            
        try {
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
            
            query = new QueryCEAR(qName, paramList, u);
            query.setExecutedByIntegrator(false);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return query;
    }
    
    
    
    
//    --------------------------------------------------------------------------
//    ***************************** QUERY LIST BUILDERS ************************
//    --------------------------------------------------------------------------
    
        
    public List<QueryProperty> buildQueryPropertyList(Credential cred, Municipality m) throws IntegrationException{
        QueryPropertyEnum[] nameArray = QueryPropertyEnum.values();
        List<QueryProperty> queryList = new ArrayList<>();
        for(QueryPropertyEnum queryTitle: nameArray){
            // THE FACTORY CALL for QueryCEAR objects!!!!!!!!!!!!!!
            queryList.add(prepareQueryProperty(queryTitle, cred, m, null));
        }
        return queryList;
    }
    
     /**
     * First gen queries. Replaced by the rest of this class
     * 
     * @deprecated 
     * @param u
     * @param m
     * @return 
     */
    public List<Query> getEventQueryList(User cred, Municipality m){
        List<Query> queryList = new ArrayList<>();
//        
//        QueryEvent eq = new QueryEvent("Compliance follow-up events: Today", m);
//        eq.setEventSearchParams(getSearchParamsEventsRequiringAction(cred, m));
//        queryList.add(eq);
//        
//        eq = new QueryEvent("Officer Activity Report", m);
//        eq.setEventSearchParams(getSearchParamsOfficerActivity(cred, m));
//        queryList.add(eq);
//        
//        
//        eq = new QueryEvent("Compliance events: Past Month", m);
//        eq.setEventSearchParams(getSearchParamsComplianceEvPastMonth(m));
//        queryList.add(eq);
        
        return queryList;
    }
     
     public List<QueryEvent> buildQueryEventCECaseList(Municipality m, Credential cred){
        QueryEventEnum[] nameArray = QueryEventEnum.values();
        List<QueryEvent> queryList = new ArrayList<>();
        for(QueryEventEnum queryTitle: nameArray){
            // THE FACTORY CALL for QueryCEAR objects!!!!!!!!!!!!!!
            queryList.add(initQuery(queryTitle, cred, m, null));
        }

        return queryList;
         
     }
     
     
     public List<QueryCECase> prepareQueryCECaseList(Credential cred){
        QueryCECaseEnum[] nameArray = QueryCECaseEnum.values();
        List<QueryCECase> queryList = new ArrayList<>();
//        for(QueryCECaseEnum queryTitle: nameArray){
//            // THE FACTORY CALL for QueryCEAR objects!!!!!!!!!!!!!!
//            queryList.add(initQuery(queryTitle, cred, m, null));
//        }
        queryList.add(SearchCoordinator.this.initQuery(QueryCECaseEnum.OPENCASES, cred, null));
        return queryList;
     }
     
    
    /**
     * These are just skeleton subQuery objects for Action Requests used for
     * user selection drop down boxes on faces pages
     * @param cred
     * @param m session user's current municipality
     * @return each existing CEAR Query capable of being passed into runQuery()
     */
    public List<QueryCEAR> buildQueryCEARList(Credential cred) throws IntegrationException{
        QueryCEAREnum[] nameArray = QueryCEAREnum.values();
        List<QueryCEAR> queryList = new ArrayList<>();
//        qList.add(initQuery(QueryCEAREnum.ALL_PAST30, cred, m));
        for(QueryCEAREnum queryTitle: nameArray){
            // THE FACTORY CALL for QueryCEAR objects!!!!!!!!!!!!!!
            queryList.add(initQuery(queryTitle, cred, null));
        }
        return queryList;
    }
    
    
    
    
    
    
    public QueryOccPeriod prepareQueryOccPeriod(    QueryOccPeriodEnum qName, 
                                                    Credential cred, 
                                                    SearchParamsOccPeriod params){
         QueryOccPeriod  query;
         List<SearchParamsOccPeriod> paramsList = new ArrayList<>();
         
         if(params != null){
             qName = QueryOccPeriodEnum.CUSTOM;
         }
         
         switch(qName){
            
            case ALL_PERIODS_IN_MUNI:
                 paramsList.add(generateParams_occPeriod_allMuni(cred));
            case AUTHWORKINPROGRESS:
                paramsList.add(generateParams_occPeriod_wip());
                break;
            case RENTAL_ALL:
                break;
            case RENTAL_REGISTERED:
                break;
            case RENTAL_UNREGISTERED:
                break;
            
         }
         query = new QueryOccPeriod(qName, paramsList, cred);
         query.setExecutedByIntegrator(false);
         return query;
     }
    
    private SearchParamsOccPeriod generateParams_occPeriod_wip(){
        SearchParamsOccPeriod params = new SearchParamsOccPeriod();
        
        params.setSearchName("Periods with outstanding inspections");
        params.setSearchDescription("Inspections have been started by not certified as passed");
        
        params.setInspectionPassed_filterBy(true);
        params.setInspectionPassed_switch_passedInspection(false);
        
        return params;
        
    }
    
    private SearchParamsOccPeriod generateParams_occPeriod_allMuni(Credential cred){
        MunicipalityCoordinator mc = getMuniCoordinator();
        SearchParamsOccPeriod params = new SearchParamsOccPeriod();
        
        params.setSearchName("All periods in credentialed muni");
        params.setSearchDescription("All periods regardless of status");
        
        params.setInspectionPassed_filterBy(false);
        params.setInspectionPassed_switch_passedInspection(false);
        
        params.setMuni(cred.getGoverningAuthPeriod().getMuni());
        
    }
    
    
    public List<QueryOccPeriod> buildQueryOccPeriodList(Credential cred, Municipality m) throws IntegrationException{
        QueryOccPeriodEnum[] nameArray = QueryOccPeriodEnum.values();
        List<QueryOccPeriod> queryList = new ArrayList<>();
        for(QueryOccPeriodEnum queryTitle: nameArray){
            // THE FACTORY CALL for QueryCEAR objects!!!!!!!!!!!!!!
            queryList.add(prepareQueryOccPeriod(queryTitle, cred, m, null));
        }
        return queryList;
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
    
    private SearchParamsPerson generateParams_persons_users(){
        SearchParamsPerson params = new SearchParamsPerson();
        params.setSearchName("User Persons");
        params.setSearchDescription("Persons whose type is a User");
        
        params.setFilterByPersonTypes(true);
        List<PersonType> pList = new ArrayList<>();
        pList.add(PersonType.User);
        params.setPersonTypes(pList);
        
        return params;
    }
    
    public List<QueryPerson> buildQueryPersonList(Credential cred, Municipality m) throws IntegrationException{
        QueryPersonEnum[] nameArray = QueryPersonEnum.values();
        List<QueryPerson> queryList = new ArrayList<>();
        for(QueryPersonEnum queryTitle: nameArray){
            // THE FACTORY CALL for QueryCEAR objects!!!!!!!!!!!!!!
            queryList.add(SearchCoordinator.this.initQuery(queryTitle, cred, m, null));
        }
        return queryList;
    }
    
  
     
    public QueryProperty prepareQueryProperty(QueryPropertyEnum qName, Credential cred, Municipality m, SearchParamsProperty params){
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

 
    
//    --------------------------------------------------------------------------
//    ***************************** PARAM GENERATORS ***************************
//    --------------------------------------------------------------------------

       
    public SearchParamsEvent getSearchParamsEventsRequiringAction(User cred, Municipality muni){
        EventCoordinator ec = getEventCoordinator();
        
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        EventCategory timelineEventTypeCategory = ec.getInitializedEventCateogry();
        timelineEventTypeCategory.setEventType(EventType.Timeline);
        
        SearchParamsEvent eventParams = new SearchParamsEvent();
        
        eventParams.setFilterByMuni(true);
        eventParams.setMuni(muni);
        eventParams.setFilterByStartEndDate(false);
        eventParams.setObjectID_filterBy(false);
        eventParams.setLimitResultCountTo100(true);
        
        eventParams.setFilterByEventCategory(false);
        eventParams.setFilterByEventType(false);
        
        eventParams.setFilterByCaseID(false);
        
        eventParams.setFilterByEventOwner(false);
        
        eventParams.setActive_filterBy(true);
        eventParams.setIsActive(true);
        
        eventParams.setFilterByPerson(false);
        eventParams.setUseRespondedAtDateRange(false);
        
        eventParams.setFilterByHidden(false);
        
        return eventParams;
    }
    
    public SearchParamsEvent getSearchParamsOfficerActivity(User cred, Municipality m){
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        
        SearchParamsEvent eventParams = new SearchParamsEvent();
        
        eventParams.setFilterByMuni(true);
        eventParams.setMuni(m);
        
        eventParams.setFilterByStartEndDate(true);
        eventParams.setUseRelativeDates(true);
        
        eventParams.setApplyDateSearchToDateOfRecord(true);
        // query from a week ago to now
        eventParams.setStartDateRelativeDays(-30);
        eventParams.setEndDateRelativeDays(0);
        
        eventParams.setObjectID_filterBy(false);
        eventParams.setLimitResultCountTo100(true);
        
        eventParams.setFilterByEventCategory(false);
        eventParams.setFilterByEventType(false);
        
        eventParams.setFilterByCaseID(false);
        
        eventParams.setFilterByEventOwner(true);
        eventParams.setUserID(cred.getUserID());
        
        eventParams.setActive_filterBy(true);
        eventParams.setIsActive(true);
        
        eventParams.setFilterByPerson(false);
        eventParams.setUseRespondedAtDateRange(false);
        
        eventParams.setFilterByHidden(false);
        
        return eventParams;
    }
    
    
    public SearchParamsEvent getSearchParamsComplianceEvPastMonth(Municipality m){
        EventCoordinator ec = getEventCoordinator();
        
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        EventCategory complianceEventCategory = ec.getInitializedEventCateogry();
        complianceEventCategory.setEventType(EventType.Compliance);
        
        SearchParamsEvent eventParams = new SearchParamsEvent();
        
        eventParams.setFilterByMuni(true);
        eventParams.setMuni(m);
        eventParams.setFilterByStartEndDate(true);
        eventParams.setUseRelativeDates(true);
        eventParams.setApplyDateSearchToDateOfRecord(true);
        // query from a week ago to now
        eventParams.setStartDateRelativeDays(-400);
        eventParams.setEndDateRelativeDays(0);
        
        eventParams.setObjectID_filterBy(false);
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
        params.setObjectID_filterBy(false);
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
        params.setObjectID_filterBy(false);
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
        propParams.setObjectID_filterBy(false);
        propParams.setLimitResultCountTo100(true);
        
        // subclass SearchParamsProperty
        propParams.setFilterByLotAndBlock(false);
        propParams.setFilterByParcelID(false);
        propParams.setFilterByAddressPart(true);

        
        return propParams;
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
}
