/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECaseBase;
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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public class SearchCoordinator extends BackingBeanUtils implements Serializable{
    
    private static final RoleType MIN_ROLETYPEFORMULTIMUNI_QUERY = RoleType.CogStaff;
    
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
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public QueryProperty runQuery(QueryProperty q) throws SearchException{
        PropertyIntegrator pi = getPropertyIntegrator();
        PropertyCoordinator pc = getPropertyCoordinator();
        
        if(q == null) return null;
        
        prepareQueryForRun(q);

        List<SearchParamsProperty> paramsList = q.getParmsList();
        List<Property> propTempList = new ArrayList<>();
        
        for(SearchParamsProperty sp: paramsList){
            propTempList.clear();
            for(Municipality muni: sp.getMuniList_val()){
                sp.setMuni_val(muni);
                try {
                    for(Integer i: pi.searchForProperties(sp)){
                        propTempList.add(pc.getProperty(i));
                    }
                } catch (IntegrationException ex) {
                    Logger.getLogger(SearchCoordinator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
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
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public QueryPerson runQuery(QueryPerson q) throws SearchException{
        PersonIntegrator pi = getPersonIntegrator();
        PersonCoordinator pc = getPersonCoordinator();
        
        if(q == null) return null;
        
        prepareQueryForRun(q);

        List<SearchParamsPerson> paramsList = q.getParmsList();
        List<Person> persTempList = new ArrayList<>();
        
        for(SearchParamsPerson sp: paramsList){
            persTempList.clear();
            for(Municipality muni: sp.getMuniList_val()){
                sp.setMuni_val(muni);
                try {
                    for(Integer i: pi.searchForPersons(sp)){
                        persTempList.add(pc.getPerson(i));
                    }
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                    throw new SearchException("Integration error when querying properties");
                }
            }
            q.addToResults(persTempList);
        }
        postRunConfigureQuery(q);
        return q;
    }
     
    /**
     * Single point of entry for queries against the Event tables
     * @param q
     * @return
     * @throws SearchException 
     */
     public QueryEvent runQuery(QueryEvent q) throws SearchException{
         EventIntegrator ei = getEventIntegrator();
         EventCoordinator ec = getEventCoordinator();
         
         if(q == null) return null;
        
        prepareQueryForRun(q);

        List<SearchParamsEvent> paramsList = q.getParmsList();
        List<Event> evTempList = new ArrayList<>();
        
        for(SearchParamsEvent sp: paramsList){
            evTempList.clear();
            // audit the params and get the result list back
            for(Municipality muni: sp.getMuniList_val()){
                sp.setMuni_val(muni);
                try {
                    for(Integer i: ei.searchForEvents(sp)){
                        evTempList.add(ec.getEvent(i));
                    }
                } catch (IntegrationException | BObStatusExceptionex) {
                    System.out.println(ex);
                    throw new SearchException("Integration or CaseLifecycle exception in query run;");
                }
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
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public QueryOccPeriod runQuery(QueryOccPeriod q) throws SearchException{
        OccupancyIntegrator oi = getOccupancyIntegrator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        
        if(q == null){ return null; }
        
        prepareQueryForRun(q);

        List<SearchParamsOccPeriod> paramsList = q.getParmsList();
        List<OccPeriod> periodListTemp = new ArrayList<>();
        
        for(SearchParamsOccPeriod sp: paramsList){
            periodListTemp.clear();
            for(Municipality muni: sp.getMuniList_val()){
                sp.setMuni_val(muni);
                for(Integer i: oi.searchForOccPeriods(sp)){
                    try {
                        periodListTemp.add(oc.getOccPeriod(i));
                    } catch (IntegrationException ex) {
                        System.out.println(ex);
                        throw new SearchException("Integration exception when querying OccPeriods");
                    }
                }
            }
            q.addToResults(periodListTemp);
        }
        
        postRunConfigureQuery(q);
        
        return q;
    }
    
    
    
     /**
      * Single point of entry for queries against the CECase table
      * @param q search params with the credential set
      * @return a Query subclass with results accessible via q.getResults
      * @throws SearchException 
      */
     public QueryCECase runQuery(QueryCECase q) throws SearchException{
        CaseIntegrator ci = getCaseIntegrator();
        CaseCoordinator cc = getCaseCoordinator();
        
        if(q == null){ return null; }
        
        prepareQueryForRun(q);

        List<SearchParamsCECase> paramsList = q.getParmsList();
        List<CECaseBase> caseListTemp = new ArrayList<>();
        
        for(SearchParamsCECase params: paramsList){
            caseListTemp.clear();
            for(Municipality muni: params.getMuniList_val()){
                try {
                    // the integrator will only look at the single muni val, 
                    // so we'll call searchForXXX once for each muni
                    params.setMuni_val(muni);
                    for(Integer i: ci.searchForCECases(params)){
                        caseListTemp.add(cc.getCECase(i));
                    }
                } catch (IntegrationException | BObStatusExceptionex) {
                    throw new SearchException("Exception during search: " + ex.toString());
                }
            }
            // add each batch of OccPeriod objects from the SearchParam run to our
            // ongoing list
            q.addToResults(caseListTemp);
        }
        
        postRunConfigureQuery(q);
        
        return q;
     }
     
    /**
     * Single point of entry for queries on Code Enforcement Requests
     * 
     * @param q must be initialized
     * @return a reference to the same QueryCEAR instance passed in with the business
     * objects returned from the integrator accessible via getResults()
     * @throws com.tcvcog.tcvce.domain.SearchException
     */
    public QueryCEAR runQuery(QueryCEAR q) throws SearchException{
        
        CEActionRequestIntegrator ceari = getcEActionRequestIntegrator();
        CaseCoordinator cc = getCaseCoordinator();
        
        if(q == null){
            return null;
        }
        
        prepareQueryForRun(q);

        List<SearchParamsCEActionRequests> paramsList = q.getParmsList();
        List<CEActionRequest> ceariListTemp = new ArrayList<>();
        
        for(SearchParamsCEActionRequests sp: paramsList){
            ceariListTemp.clear();
            for(Municipality muni: sp.getMuniList_val()){
                sp.setMuni_val(muni);
                try {
                    for(Integer i: ceari.searchForCEActionRequests(sp)){
                            ceariListTemp.add(cc.getCEActionRequest(i));
                    }
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                    throw new SearchException("Integration error when querying CEARS");
                }
            }
            q.addToResults(ceariListTemp);
        }
        postRunConfigureQuery(q);
        return q;
    }
    
//    --------------------------------------------------------------------------
//    ***************************** UTILITIES **********************************
//    --------------------------------------------------------------------------
    
    
    /**
     * Container for consolidating calls to any methods that need to be run
     * on a Query before passing it to an Integrator class
     * @param q
     * @throws SearchException 
     */
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
        
        UserCoordinator uc = getUserCoordinator();
        
        if(q == null){
            throw new SearchException("Cannot audit null query");
        }
        
        Credential cred = q.getCredential();
        List<Municipality> muniSafeList = new ArrayList<>();
        List<SearchParams> splst = new ArrayList<>();
        
        
        
        for (Iterator it = q.getParmsList().iterator(); it.hasNext();) {
            SearchParams sp = (SearchParams) it.next();
            // logic could be run here on SPs before adding them to the list to run
            splst.add(sp);
        }
        
        for(SearchParams sp: splst){
            if(sp.getMuniList_val().size() > 1){
                if(q.getCredential().getGoverningAuthPeriod().getRole().getRank() < MIN_ROLETYPEFORMULTIMUNI_QUERY.getRank()){
                    throw new SearchException(MIN_ROLETYPEFORMULTIMUNI_QUERY.getLabel() + " is required for muli-muni searching");
                }
                try {
                    muniSafeList = uc.getUnauthorizedMunis(uc.getUser(cred.getGoverningAuthPeriod().getUserID()));
                } catch (IntegrationException ex) {
                    throw new SearchException("Could not load user associated with Credential; IntegrationException ");
                }
                for(Municipality m: sp.getMuniList_val()){
                    if(!muniSafeList.contains(m)){
                        throw new SearchException("Invalid attempt to search for objects in an unauthorized Muni");
                    }
                }
            } else if(sp.getMuniList_val().size() == 1){
                if(sp.getMuniList_val().get(0).getMuniCode() != q.getCredential().getGoverningAuthPeriod().getMuni().getMuniCode()){
                    throw new SearchException("Requested muni for search does not match credential's governing auth period");
                }
            } else {
                sp.addMuni(q.getCredential().getGoverningAuthPeriod().getMuni());
            }
        }        
    }
    
    /**
     * Called after a Query is passed to its respective integrator
     * @param q 
     */
    private void postRunConfigureQuery(Query q){
        q.setExecutionTimestamp(LocalDateTime.now());
        logQueryRun(q);
        
    }
    
    private void logQueryRun(Query q){
        // TODO: write guts for query logging
    }
    
     
    /**
     * 
     * @param q
     * @param cred
     * @return 
     */
    private Query initQueryFinalizeInit(Query q){
        
        
        return q;
    }
    
//    --------------------------------------------------------------------------
//    ***************************** INITIALIZERS *******************************
//    --------------------------------------------------------------------------
    
    
    public QueryProperty initQuery(QueryPropertyEnum qName, Credential cred){
         QueryProperty  query;
         List<SearchParamsProperty> paramsList = new ArrayList<>();
         
         switch(qName){
            case OPENCECASES_OCCPERIODSINPROCESS:
                paramsList.add(generateParams_property_active());
                break;
            case CUSTOM:
                break;
         }
         
         query = new QueryProperty(qName, paramsList, cred);
         return (QueryProperty) initQueryFinalizeInit(query);
     }
    
    
    
    public QueryPerson initQuery(QueryPersonEnum qName, Credential cred){
         QueryPerson  query;
         List<SearchParamsPerson> paramsList = new ArrayList<>();
         
         switch(qName){
            case ACTIVE_PERSONS:
                paramsList.add(generateParams_persons_active());
                break;
            case USER_PERSONS:
                paramsList.add(generateParams_persons_users());
            
         }
         query = new QueryPerson(qName, paramsList, cred);
         return (QueryPerson) initQueryFinalizeInit(query);
     }
     
    
     public QueryEvent initQuery(QueryEventEnum qName, Credential cred){
         QueryEvent query;
         List<SearchParamsEvent> paramsList = new ArrayList<>();
         
         switch(qName){
             case REQUESTED_ACTIONS:
                paramsList.add(getSearchParamsEventsRequiringAction());
                break;
             case MUNICODEOFFICER_ACTIVITY_PAST30DAYS:
                 paramsList.add(getSearchParamsOfficerActivity());
                 break;
             case COMPLIANCE_EVENTS:
                 paramsList.add(getSearchParamsComplianceEvPastMonth());
                 break;
            case CUSTOM:
                break;
            default:
         }
         
         query = new QueryEvent(qName, paramsList, cred);
         return (QueryEvent) initQueryFinalizeInit(query);
     }
     
    
     
     
    
    public QueryOccPeriod initQuery(QueryOccPeriodEnum qName, Credential cred){
         QueryOccPeriod  query;
         List<SearchParamsOccPeriod> paramsList = new ArrayList<>();
         
         switch(qName){
            
            case ALL_PERIODS_IN_MUNI:
                 
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
         query = new QueryOccPeriod(qName, paramsList, null);
         return (QueryOccPeriod) initQueryFinalizeInit(query);
     }
 
    
     public QueryCECase initQuery(QueryCECaseEnum qName, Credential cred){
         QueryCECase query;
         List<SearchParamsCECase> paramsList = new ArrayList<>();
         
         switch(qName){
            case OPENCASES:
                paramsList.add(getDefaultSearchParams_CECase_allOpen());
                 break;
            case EXPIRED_TIMEFRAMES:
                break;
            case CURRENT_TIMEFRAMES:
                break;
            case OPENED_30DAYS:
                break;
            case CLOSED_30DAYS:
                paramsList.add(getSearchParams_CECase_closedPast30Days());
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
         
         query = new QueryCECase(qName, paramsList, cred);
         return (QueryCECase) initQueryFinalizeInit(query);
     }
     
     
    
     /**
     * Factory method for Query subclass: QueryCEAR.
     * 
     * @param qName an instance of the Enum QueryCEAREnum which is used by a switch
     * to grab the assigned SearchParams subclass
     * @param cred
     * @return assembled instance ready for sending to runQuery
     */
    public QueryCEAR initQuery(QueryCEAREnum qName, Credential cred) {
            QueryCEAR query = null;
            List<SearchParamsCEActionRequests> paramList = new ArrayList<>();
            
        try {
            switch(qName){
                case UNPROCESSED:
                    paramList.add(generateParams_CEAR_Unprocessed());
                    break;
                case ATTACHED_TO_CECASE:
                    paramList.add(generateParams_CEAR_attachedToCase(30));
                    break;
                    
                case ALL_TODAY:
                    paramList.add(generateParams_CEAR_pastXDays(1));
                    break;
                    
                case ALL_PAST7DAYS:
                    paramList.add(generateParams_CEAR_pastXDays(7));
                    break;
                    
                case ALL_PAST30:
                    paramList.add(generateParams_CEAR_pastXDays(30));
                    break;
                    
                case ALL_PASTYEAR:
                    paramList.add(generateParams_CEAR_pastXDays(365));
                    break;
                    
                case BY_CURRENT_USER:
                    paramList.add(generateParams_CEAR_Unprocessed());
                    break;
                    
                case CUSTOM:
                    break;
                    
                default:
                    paramList.add(generateParams_CEAR_Unprocessed());
            }
            
            query = new QueryCEAR(qName, paramList, null);
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return (QueryCEAR) initQueryFinalizeInit(query);
    }
    
    
    
//    --------------------------------------------------------------------------
//    ***************************** QUERY LIST BUILDERS ************************
//    --------------------------------------------------------------------------
    
        
    /**
     * Logic container to check if a Query from the main set can be added to a 
     * particular User's list of possible queries
     * 
     * @param params
     * @param cred
     * @return 
     */
    private boolean checkAuthorizationToAddQueryToList(IFace_RankLowerBounded params, Credential cred){
        boolean isAuthToAdd = false;
        
        if(params.getRequiredRoleMin().getRank() <= cred.getGoverningAuthPeriod().getRole().getRank()){
            isAuthToAdd = true;
        }
        
        return isAuthToAdd;
        
    }
    
    
    /**
     * Assembles a list of Query objects available to each user given their 
     * Credential object. Calls internal method for verifying rank minimums
     * @param cred
     * @return
     * @throws IntegrationException 
     */
    public List<QueryProperty> buildQueryPropertyList(Credential cred) throws IntegrationException{
        QueryPropertyEnum[] nameArray = QueryPropertyEnum.values();
        List<QueryProperty> queryList = new ArrayList<>();
       
        for(QueryPropertyEnum queryTitle: nameArray){
            if(checkAuthorizationToAddQueryToList(queryTitle, cred)){
                queryList.add(initQuery(queryTitle, cred));
            }
        }
        
        return queryList;
    }
    
    
    /**
     * Assembles a list of Query objects available to each user given their 
     * Credential object. Calls internal method for verifying rank minimums
     * @param cred
     * @return
     * @throws IntegrationException 
     */
     public List<QueryPerson> buildQueryPersonList(Credential cred) throws IntegrationException{
        QueryPersonEnum[] nameArray = QueryPersonEnum.values();
        List<QueryPerson> queryList = new ArrayList<>();
        for(QueryPersonEnum queryTitle: nameArray){
            if(checkAuthorizationToAddQueryToList(queryTitle, cred)){
                queryList.add(initQuery(queryTitle, cred));
            }
        }
        return queryList;
    }
    
     /**
     * Assembles a list of Query objects available to each user given their 
     * Credential object. Calls internal method for verifying rank minimums
     * @param cred
     * @return 
     */
    public List<QueryEvent> buildQueryEventList(Credential cred){
        QueryEventEnum[] nameArray = QueryEventEnum.values();
        List<QueryEvent> queryList = new ArrayList<>();
        for(QueryEventEnum queryTitle: nameArray){
            if(checkAuthorizationToAddQueryToList(queryTitle, cred)){
                queryList.add(initQuery(queryTitle, cred));
            }
        }
        return queryList;
    }
      
    
    /**
     * Assembles a list of Query objects available to each user given their 
     * Credential object. Calls internal method for verifying rank minimums
     * @param cred
     * @param m
     * @return 
     */
    public List<QueryOccPeriod> buildQueryOccPeriodList(Credential cred){
        QueryOccPeriodEnum[] nameArray = QueryOccPeriodEnum.values();
        List<QueryOccPeriod> queryList = new ArrayList<>();
        for(QueryOccPeriodEnum queryTitle: nameArray){
            if(checkAuthorizationToAddQueryToList(queryTitle, cred)){
               queryList.add(initQuery(queryTitle, cred));
            }
        }
        return queryList;
    }
    

    /**
     * Assembles a list of Query objects available to each user given their 
     * Credential object. Calls internal method for verifying rank minimums
     * @param cred
     * @return 
     */
     public List<QueryCECase> buildQueryCECaseList(Credential cred){
        QueryCECaseEnum[] nameArray = QueryCECaseEnum.values();
        List<QueryCECase> queryList = new ArrayList<>();
        for(QueryCECaseEnum queryTitle: nameArray){
            if(checkAuthorizationToAddQueryToList(queryTitle, cred)){
                queryList.add(initQuery(queryTitle, cred));
            }
        }
        return queryList;
     }
     
    
    /**
     * Assembles a list of Query objects available to each user given their 
     * Credential object. Calls internal method for verifying rank minimums
     * @param cred
     * @return each existing CEAR Query capable of being passed into runQuery()
     */
    public List<QueryCEAR> buildQueryCEARList(Credential cred){
        QueryCEAREnum[] nameArray = QueryCEAREnum.values();
        List<QueryCEAR> queryList = new ArrayList<>();
        for(QueryCEAREnum queryTitle: nameArray){
            if(checkAuthorizationToAddQueryToList(queryTitle, cred)){
                queryList.add(initQuery(queryTitle, cred));
            }
        }
        return queryList;
    }
    
    
//    --------------------------------------------------------------------------
//    ***************************** PARAM GENERATORS ***************************
//    --------------------------------------------------------------------------
    
    private SearchParamsOccPeriod generateParams_occPeriod_wip(){
        SearchParamsOccPeriod params = new SearchParamsOccPeriod();
        
        params.setSearchName("Periods with outstanding inspections");
        params.setSearchDescription("Inspections have been started by not certified as passed");
        
        params.setInspectionPassed_filterBy(true);
        params.setInspectionPassed_switch_passedInspection(false);
        
        return params;
        
    }
    
    private SearchParamsOccPeriod generateParams_occPeriod(){
        MunicipalityCoordinator mc = getMuniCoordinator();
        SearchParamsOccPeriod params = new SearchParamsOccPeriod();
        
        params.setSearchName("All periods in credentialed muni");
        params.setSearchDescription("All periods regardless of status");
        
        params.setInspectionPassed_filterBy(false);
        params.setInspectionPassed_switch_passedInspection(false);
        
        return params;
        
    }
    
   
    
    private SearchParamsPerson generateParams_persons_active(){
        SearchParamsPerson params = new SearchParamsPerson();
        
        params.setSearchName("Public person types");
        params.setSearchDescription("All persons declared to be public");
        
        params.setPersonType_ctl(true);
        List<PersonType> pList = new ArrayList<>();
        pList.add(PersonType.Public);
        params.setPersonType_val(pList);
        
         return params;
        
    }
    
    private SearchParamsPerson generateParams_persons_users(){
        SearchParamsPerson params = new SearchParamsPerson();
        params.setSearchName("User Persons");
        params.setSearchDescription("Persons whose type is a User");
        
        params.setPersonType_ctl(true);
        List<PersonType> pList = new ArrayList<>();
        pList.add(PersonType.User);
        params.setPersonType_val(pList);
        
        return params;
    }
   
    
  
     
    private SearchParamsProperty generateParams_property_active(){
        SearchParamsProperty params = new SearchParamsProperty();
        
        params.setSearchName("Properties updated in the past month");
        params.setSearchDescription("Applies to properties with any field updated");
        
        params.setMuni_ctl(true);
        
        params.setDate_startEnd_ctl(true);
        params.setDate_relativeDates_ctl(true);
        params.setDateField(SearchParamsPropertyDateFields.LAST_UPDATED);
        params.setDate_relativeDates_start_val(-30);
        params.setDate_realtiveDates_end_val(0);
        
        params.setActive_ctl(true);
        params.setActive_val(true);
        

        return params;
        
    }


       
    public SearchParamsEvent getSearchParamsEventsRequiringAction(){
        EventCoordinator ec = getEventCoordinator();
        
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        EventCategory timelineEventTypeCategory = ec.getInitializedEventCateogry();
        timelineEventTypeCategory.setEventType(EventType.Timeline);
        
        SearchParamsEvent eventParams = new SearchParamsEvent();
        
        eventParams.setMuni_ctl(true);
        eventParams.setDate_startEnd_ctl(false);
        eventParams.setBobID_ctl(false);
        eventParams.setLimitResultCount_ctl(true);
        
        eventParams.setFilterByEventCategory(false);
        eventParams.setFilterByEventType(false);
        
        eventParams.setFilterByCaseID(false);
        
        eventParams.setFilterByEventOwner(false);
        
        eventParams.setActive_ctl(true);
        eventParams.setIsActive(true);
        
        eventParams.setFilterByPerson(false);
        eventParams.setUseRespondedAtDateRange(false);
        
        eventParams.setFilterByHidden(false);
        
        return eventParams;
    }
    
    public SearchParamsEvent getSearchParamsOfficerActivity(){
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        
        SearchParamsEvent eventParams = new SearchParamsEvent();
        
        eventParams.setMuni_ctl(true);
        
        eventParams.setDate_startEnd_ctl(true);
        eventParams.setDate_relativeDates_ctl(true);
        
        eventParams.setApplyDateSearchToDateOfRecord(true);
        // query from a week ago to now
        eventParams.setDate_relativeDates_start_val(-30);
        eventParams.setDate_realtiveDates_end_val(0);
        
        eventParams.setBobID_ctl(false);
        eventParams.setLimitResultCount_ctl(true);
        
        eventParams.setFilterByEventCategory(false);
        eventParams.setFilterByEventType(false);
        
        eventParams.setFilterByCaseID(false);
        
        eventParams.setFilterByEventOwner(true);
        
        eventParams.setActive_ctl(true);
        eventParams.setIsActive(true);
        
        eventParams.setFilterByPerson(false);
        eventParams.setUseRespondedAtDateRange(false);
        
        eventParams.setFilterByHidden(false);
        
        return eventParams;
    }
    
    
    public SearchParamsEvent getSearchParamsComplianceEvPastMonth(){
        EventCoordinator ec = getEventCoordinator();
        
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        EventCategory complianceEventCategory = ec.getInitializedEventCateogry();
        complianceEventCategory.setEventType(EventType.Compliance);
        
        SearchParamsEvent eventParams = new SearchParamsEvent();
        
        eventParams.setMuni_ctl(true);
        eventParams.setDate_startEnd_ctl(true);
        eventParams.setDate_relativeDates_ctl(true);
        eventParams.setApplyDateSearchToDateOfRecord(true);
        // query from a week ago to now
        eventParams.setDate_relativeDates_start_val(-400);
        eventParams.setDate_realtiveDates_end_val(0);
        
        eventParams.setBobID_ctl(false);
        eventParams.setLimitResultCount_ctl(true);
        
        eventParams.setFilterByEventCategory(false);
        eventParams.setFilterByEventType(true);
        eventParams.setEvtType(EventType.Compliance);
        
        eventParams.setFilterByCaseID(false);
        
        eventParams.setFilterByEventOwner(false);
        
        eventParams.setActive_ctl(true);
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
    public SearchParamsCECase getDefaultSearchParams_CECase_allOpen(){
        SearchParamsCECase params = new SearchParamsCECase();
        
        // superclass 
        params.setMuni_ctl(true);
        params.setBobID_ctl(false);
        params.setLimitResultCount_ctl(true);
        
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
    public SearchParamsCECase getSearchParams_CECase_closedPast30Days(){
        SearchParamsCECase params = new SearchParamsCECase();
        params.setSearchName("CECases");
        
        // superclass 
        params.setMuni_ctl(true);
        params.setBobID_ctl(false);
        params.setLimitResultCount_ctl(true);
        
        // subclass specific
        params.setUseIsOpen(false);
        
        params.setDateToSearchCECases("Closing date");
        params.setUseCaseManager(false);
        
        LocalDateTime pastXDays = LocalDateTime.now().minusDays(30);
        
        params.setDate_start_val(pastXDays);
        params.setDate_end_val(LocalDateTime.now());
        
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
        propParams.setDate_startEnd_ctl(false);
        propParams.setBobID_ctl(false);
        propParams.setLimitResultCount_ctl(true);
        
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
     public SearchParamsCEActionRequests generateParams_CEAR_RequestorCurrentU(User u) throws IntegrationException{
            
        CEActionRequestIntegrator cari = getcEActionRequestIntegrator();

        SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

        LocalDateTime pastTenYears = LocalDateTime.now().minusDays(30);
        // action requests cannot have a time stamp past the current datetime
        sps.setDate_end_val(LocalDateTime.now());
        
        sps.setLimitResultCount_ctl(true);
        sps.setUseAttachedToCase(false);
        sps.setAttachedToCase(false);
        sps.setUseMarkedUrgent(false);
        sps.setUseNotAtAddress(false);
        
        
        return sps;
    }
    
    
     public SearchParamsCEActionRequests generateParams_CEAR_Unprocessed() throws IntegrationException{
            
        CEActionRequestIntegrator cari = getcEActionRequestIntegrator();

        SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

        LocalDateTime pastTenYears = LocalDateTime.now().minusYears(10);
        sps.setDate_start_val(pastTenYears);
        // action requests cannot have a time stamp past the current datetime
        sps.setDate_end_val(LocalDateTime.now());
        
        sps.setLimitResultCount_ctl(true);
        sps.setUseAttachedToCase(false);
        sps.setAttachedToCase(false);
        sps.setUseMarkedUrgent(false);
        sps.setUseNotAtAddress(false);
        sps.setUseRequestStatus(true);
        sps.setRequestStatus(cari.getRequestStatus(Integer.parseInt(
                getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("actionRequestInitialStatusCode"))));
        
        return sps;
    }
    
     public SearchParamsCEActionRequests generateParams_CEAR_pastXDays(int days){
            
        SearchCoordinator sc = getSearchCoordinator();

        SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

        LocalDateTime pastxDays = LocalDateTime.now().minusDays(days);
        sps.setDate_start_val(pastxDays);

        // action requests cannot have a time stamp past the current datetime
        sps.setDate_end_val(LocalDateTime.now());

        sps.setUseAttachedToCase(false);
        sps.setUseMarkedUrgent(false);
        sps.setUseNotAtAddress(false);
        sps.setUseRequestStatus(false);
        
        return sps;
    }
    
    
     public SearchParamsCEActionRequests generateParams_CEAR_attachedToCase(int days){
            
        SearchCoordinator sc = getSearchCoordinator();

        SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

        LocalDateTime pastxDays = LocalDateTime.now().minusDays(days);
        sps.setDate_start_val(pastxDays);

        // action requests cannot have a time stamp past the current datetime
        sps.setDate_end_val(LocalDateTime.now());

        sps.setUseAttachedToCase(true);
        sps.setAttachedToCase(true);
        
        sps.setUseMarkedUrgent(false);
        sps.setUseNotAtAddress(false);
        sps.setUseRequestStatus(false);
        
        return sps;
    }
}
