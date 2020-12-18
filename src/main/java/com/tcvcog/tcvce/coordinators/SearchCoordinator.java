/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.entities.CEActionRequest;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.Credential;
import com.tcvcog.tcvce.entities.EventCnF;
import com.tcvcog.tcvce.entities.EventDomainEnum;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.PersonType;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.RoleType;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodPropertyUnitHeavy;
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
import javax.annotation.PostConstruct;

/**
 *
 * @author sylvia
 */
public class SearchCoordinator extends BackingBeanUtils implements Serializable{
    
    private static final RoleType MIN_ROLETYPEFORMULTIMUNI_QUERY = RoleType.SysAdmin;
    private static final RoleType PUBLIC_SEARCH_ROLETYPE = RoleType.Public;
    private static final int RESULT_COUNT_LIMIT_DEFAULT = 100;
    private static final int FILTER_OFF_DEFVALUE_INT = 0;
    
    private static final int PASTPERIOD_RECENT = 30;
    private static final int PASTPERIOD_WEEK = 7;
    private static final int PASTPERIOD_MONTH = 30;
    private static final int PASTPERIOD_YEAR = 365;
    private static final int PASTPERIOD_TODAY = 0;
    
    
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

        List<SearchParamsProperty> paramsList = q.getParamsList();
        List<Property> propTempList = new ArrayList<>();
        
        for(SearchParamsProperty sp: paramsList){
            propTempList.clear();
                try {
                    for(Integer i: pi.searchForProperties(sp)){
                        propTempList.add(pc.getProperty(i));
                    }
                } catch (IntegrationException ex) {
                    throw new SearchException(ex);
                }
            // extract log and append to Query-level log
                
            q.appendToQueryLog(sp);
            q.addToResults(propTempList);
        } // close loop over param list
        
        
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

        List<SearchParamsPerson> paramsList = q.getParamsList();
        List<Person> persTempList = new ArrayList<>();
        
        for(SearchParamsPerson sp: paramsList){
            persTempList.clear();

            try {
                List<Integer> idList = pi.searchForPersons(sp);
                for(Integer i: idList){
                    persTempList.add(pc.getPerson(i));
                }
            } catch (IntegrationException ex) {
                System.out.println(ex);
                throw new SearchException(ex);
            }
            q.addToResults(persTempList);
            q.appendToQueryLog(sp);
            
        } // close for over params
        postRunConfigureQuery(q);
        
        return q;
    }
     
    /**
     * Single point of entry for queries against the EventCnF tables
     * @param q
     * @return
     * @throws SearchException 
     */
     public QueryEvent runQuery(QueryEvent q) throws SearchException{
         EventCoordinator ec = getEventCoordinator();
         if(q == null) return null;
        
        prepareQueryForRun(q);

        List<SearchParamsEvent> paramsList = q.getParamsList();
        List<EventCnF> evTempList = new ArrayList<>();
        
        for(SearchParamsEvent sp: paramsList){
            evTempList.clear();
            // audit the params and get the result list back
            if(sp.getEventDomain_val() == EventDomainEnum.UNIVERSAL){
                // query for Code Enf
                sp.setEventDomain_val(EventDomainEnum.CODE_ENFORCEMENT);
                runQuery_event_IntegratorCall(sp, evTempList);
                // now add Occ events as well
                sp.setEventDomain_val(EventDomainEnum.OCCUPANCY);
                runQuery_event_IntegratorCall(sp, evTempList);
            } else {
                runQuery_event_IntegratorCall(sp, evTempList);
            }
             try {
                 // add each batch of OccPeriod objects from the SearchParam run to our
                 // ongoing list
                 q.addToResults(ec.assembleEventCnFPropUnitCasePeriodHeavyList(evTempList));
             } catch (EventException | IntegrationException ex) {
                 System.out.println(ex);
             }
            q.appendToQueryLog(sp);
        } // close parameter for
        
        postRunConfigureQuery(q);
        
        return q;
     }
     
     private void runQuery_event_IntegratorCall(SearchParamsEvent params, List<EventCnF> evList) throws SearchException{
        EventCoordinator ec = getEventCoordinator();
        
        EventIntegrator ei = getEventIntegrator();
        if(evList == null){
            return;
        } 
        try {
            for(Integer i: ei.searchForEvents(params)){
                evList.add(ec.getEvent(i));
            }
        } catch (IntegrationException | BObStatusException ex) {
            System.out.println(ex);
            throw new SearchException("Integration or CaseLifecycle exception in query run;");
        }
         
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

        List<SearchParamsOccPeriod> paramsList = q.getParamsList();
        List<OccPeriodPropertyUnitHeavy> periodListTemp = new ArrayList<>();
        
        for(SearchParamsOccPeriod sp: paramsList){
            periodListTemp.clear();
            for(Integer i: oi.searchForOccPeriods(sp)){
                try {
                    periodListTemp.add(oc.getOccPeriodPropertyUnitHeavy(i));
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                    throw new SearchException("Integration exception when querying OccPeriods");
                }
            }
            q.addToResults(periodListTemp);
            q.appendToQueryLog(sp);
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

        List<SearchParamsCECase> paramsList = q.getParamsList();
        List<CECase> caseListTemp = new ArrayList<>();
        
        for(SearchParamsCECase params: paramsList){
            caseListTemp.clear();
            try {
            // the integrator will only look at the single muni val, 
            // so we'll call searchForXXX once for each muni
            for(Integer i: ci.searchForCECases(params)){
                CECase cse = cc.cecase_getCECase(i);
                // Case Phases only exist in JavaJavaLand, so we'll evaluate the
                // search params here before adding the new objects to the
                // final query result list
                if(params.isCaseStage_ctl() && params.getCaseStage_val() != null){
                    if(cse.getStatusBundle().getPhase().getCaseStage() == params.getCaseStage_val()){
                        caseListTemp.add(cse);
                    } else {
                        // skip adding
                    }
                // if the filter is off, or we don't have an object, add it to the list
                } else {
                    caseListTemp.add(cse);
                }
            }
                q.addToResults(cc.cecase_assembleCECasePropertyUnitHeavyList(caseListTemp));
            } catch (IntegrationException | BObStatusException ex) {
                throw new SearchException("Exception during search: " + ex.toString());
            }
            // add each batch of OccPeriod objects from the SearchParam run to our
            // ongoing list
            q.appendToQueryLog(params);
        } // close for over parameters
        
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

        List<SearchParamsCEActionRequests> paramsList = q.getParamsList();
        List<CEActionRequest> ceariListTemp = new ArrayList<>();
        
        for(SearchParamsCEActionRequests sp: paramsList){
            ceariListTemp.clear();
                try {
                    for(Integer i: ceari.searchForCEActionRequests(sp)){
                            ceariListTemp.add(cc.cear_getCEActionRequest(i));
                    }
                } catch (IntegrationException ex) {
                    System.out.println(ex);
                    throw new SearchException("Integration error when querying CEARS");
                }
            q.addToResults(ceariListTemp);
            q.appendToQueryLog(sp);
        }
        postRunConfigureQuery(q);
        return q;
    }
    
//    --------------------------------------------------------------------------
//    ***************************** UTILITIES **********************************
//    --------------------------------------------------------------------------
    
    
    
    
    /**
     * Chooses the default date field to search for any subclass of our SearchParams
     * family of objects
     * @param sp
     * @return 
     */
    public String selectDefaultDateFieldString(SearchParams sp){
        String f = null;
        if(sp != null){

            if(sp.getDate_field() != null && sp.getDate_field().extractDateFieldString() != null){
                f = sp.getDate_field().extractDateFieldString();
            } else {
                if(sp instanceof SearchParamsProperty){
                    f = SearchParamsPropertyDateFieldsEnum.CREATIONTS.extractDateFieldString();
                } else if(sp instanceof SearchParamsPerson){
                    f = SearchParamsPersonDateFieldsEnum.CREATED_TS.extractDateFieldString();
                } else if(sp instanceof SearchParamsEvent){
                    f = SearchParamsEventDateFieldsEnum.CREATED_TS.extractDateFieldString();
                } else if(sp instanceof SearchParamsOccPeriod){
                    f = SearchParamsOccPeriodDateFieldsEnum.CREATED_TS.extractDateFieldString();
                } else if(sp instanceof SearchParamsCECase){
                    f = SearchParamsCECaseDateFieldsEnum.ORIGINATIONTS.extractDateFieldString();
                } else if(sp instanceof SearchParamsCEActionRequests){
                    f = SearchParamsCEActionRequestsDateFieldsEnum.SUBMISSION_TS.extractDateFieldString();
                } else {
                    return null;
                }
            }
            
        } // close logic for default date field name selection
        
        return f;
    }
    
    
    /**
     * Shared SQL builder for the search fields on the SearchParams superclass 
     * whose subclasses are passed to the searchForXXX(SearchParamsXXX params) method family spread across the
     * main Integrators. We remove duplication of building these shared search criteria across all 6 searchable
     * BOBs as of the beta: Property, Person, Event, OccPeriod, CECase, and CEActionRequest
     * @param params
     * @param muniDBField
     * @param activeField
     * @return the configured apram for      */
    public SearchParams assembleBObSearchSQL_muniDatesUserActive(   SearchParams params, 
                                                                    String muniDBField,
                                                                    String activeField){
        
         // ****************************
            // **         MUNI           **
            // ****************************
             if(params.isMuni_ctl()){
                if(params.getMuni_val() != null){
                    params.appendSQL("AND ");
                    params.appendSQL(muniDBField);
                    params.appendSQL("=? ");
                } else {
                    params.setMuni_ctl(false);
                    params.appendToParamLog("MUNI: found null Muni value for filter; Muni filter turned off; | ");
                 }
             }
            
            // ****************************
            // **         DATES          **
            // ****************************
            if (params.isDate_startEnd_ctl()) {
                params.appendSQL("AND ");
                if(params.getDate_field() != null && params.getDate_field().extractDateFieldString() != null){
                    params.appendSQL(params.getDate_field().extractDateFieldString());
                } else {
                    params.appendSQL(selectDefaultDateFieldString(params));
                }
                params.appendSQL(" BETWEEN ? AND ? ");
            } 

            // ****************************
            // **         USER           **
            // **************************** 
            if (params.isUser_ctl()) {
                if(params.getUser_field() != null && params.getUser_val() != null){
                    params.appendSQL("AND ");
                    params.appendSQL(params.getUser_field().extractUserFieldString());
                    params.appendSQL("=? ");
                } else {
                    // if the parameter wasn't set and a user wasn't passed in, turn off date and note
                    params.setUser_ctl(false);
                    params.appendToParamLog("USER: found null User object ref; User filter turned off; | ");
                }
            }
            
            // ****************************
            // **         ACTIVE         **
            // **************************** 
            if(params.isActive_ctl()){
                    params.appendSQL("AND ");
                    if(activeField == null){
                        params.appendSQL("active");
                    } else {
                        params.appendSQL(activeField);
                    }
                if(params.isActive_val()){
                    params.appendSQL(" = TRUE ");
                } else {
                    params.appendSQL(" = FALSE ");
                }
            } 
        
        return params;
        
    }
    
    /**
     * Container for consolidating calls to any methods that need to be run
     * on a Query before passing it to an Integrator class
     * @param q
     * @throws SearchException 
     */
    private void prepareQueryForRun(Query q) throws SearchException{
       List<SearchParams> plist = q.getParamsList();
       
       for(int index = 0; index < plist.size(); index++){
           //We use an indexed for loop so that our changes to each param will be kept
           SearchParams params = plist.get(index);
           
            if (params.getMuni_val() == null) {
                params.setMuni_val(q.getCredential().getGoverningAuthPeriod().getMuni());
            }
            //This caused issues at some point by removing good SQL we built 
            //for the search. If it's needed, uncomment it, 
            //but make sure it doesn't remove SQL that we need.
//           params.clearSQL();
//           System.out.println("SearchCoordinator.prepareQueryForRun | SQL: " + params.extractRawSQL());

            //If our params are searching for a property, then let's make sure 
            //to replace all spaces in the address with wildcards.
            if(params instanceof SearchParamsProperty){
//                SearchParamsProperty temp = (SearchParamsProperty) params;
//                temp.setAddress_val(temp.getAddress_val().replaceAll(" ", "%"));
//                params = temp;
                
            }
            
            //update the list with the edited params
            
            plist.set(index, params);

       }
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
        if(q.getUserRankAccessMinimum() != null){
            if( q.getCredential().getGoverningAuthPeriod().getRole().getRank() 
                        < 
                    q.getUserRankAccessMinimum().getRank()){

                throw new SearchException("Credential below rank for running query");
            }
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
        
        List<SearchParams> splst = new ArrayList<>();
        
        for (Iterator it = q.getParamsList().iterator(); it.hasNext();) {
            SearchParams sp = (SearchParams) it.next();
            // logic could be run here on SPs before adding them to the list to run
            splst.add(sp);
        }
        
        for(SearchParams sp: splst){
            // if user doesn't meet rank requirements, override all muni settings and allow only one search
            //unless they are public, because then we don't know what muni they're from
            if(q.getCredential().getGoverningAuthPeriod().getRole().getRank() < MIN_ROLETYPEFORMULTIMUNI_QUERY.getRank() &&
                    q.getCredential().getGoverningAuthPeriod().getRole().getRank() != PUBLIC_SEARCH_ROLETYPE.getRank()){
                sp.setMuni_ctl(true);
                sp.setMuni_val(q.getCredential().getGoverningAuthPeriod().getMuni());
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
        System.out.println("****** QUERY RESULT ******");
        if(q.getBOBResultList() != null){
            System.out.println("SearchCoordinator.logQueryRun | result size: " + q.getBOBResultList().size());
        }
        System.out.print("Query type: ");
        System.out.println(q.getClass().getTypeName());
        
    }
    
     
    /**
     * Logic container applied to all implemented members of the SearchParams family
     * Unifies certain RoleType minimums for various search parameter options
     * 
     * @param q
     * @param cred
     * @return 
     */
    private Query initQueryFinalizeInit(Query q){
        for(SearchParams sp: (List<SearchParams>) q.getParamsList()){
            
            sp.setMuni_rtMin(RoleType.Developer);
            sp.setMuni_ctl(true);
            sp.setMuni_val(q.getCredential().getGoverningAuthPeriod().getMuni());
            // hack to get person queries to work without muni restriction
            if(q instanceof QueryPerson){
                sp.setMuni_ctl(false);
            }
            
            sp.setLimitResultCount_rtMin(RoleType.EnforcementOfficial);
            sp.setLimitResultCount_ctl(true);
            sp.setLimitResultCount_val(RESULT_COUNT_LIMIT_DEFAULT);
            
            sp.setActive_rtMin(RoleType.MuniStaff);
            sp.setActive_ctl(true);
            sp.setActive_val(true);
        }
        return q;
    }
    
//    --------------------------------------------------------------------------
//    ***************************** INITIALIZERS *******************************
//    --------------------------------------------------------------------------
    
    
    /**
     * Container for query initialization logic based on the given Enum val
     * for this method's associated Query subclass. Delegates configuration of
     * filter-level settings on parameter objects to methods grouped later in this
     * class prefixed by genParams_XXXX
     * @param qName the desired Query to be configured
     * @param cred of the requesting User
     * @return 
     */
    public QueryProperty initQuery(QueryPropertyEnum qName, Credential cred){
         QueryProperty  query;
         List<SearchParamsProperty> paramsList = new ArrayList<>();
         SearchParamsProperty params = genParams_property_initParams(cred);
         
         
         switch(qName){
            case OPENCECASES_OCCPERIODSINPROCESS:
                paramsList.add(genParams_property_recentlyUpdated(params, cred));
                break;
            case HOUSESTREETNUM:
                paramsList.add(genParams_property_address(params, cred));
                break;
            case PERSONS:
                paramsList.add(genParams_property_persons(params, cred));
                break;
            case CUSTOM:
                break;
         }
         query = new QueryProperty(qName, paramsList, cred);
         return (QueryProperty) initQueryFinalizeInit(query);
     }
    
    
    
    /**
     * Container for query initialization logic based on the given Enum val
     * for this method's associated Query subclass. Delegates configuration of
     * filter-level settings on parameter objects to methods grouped later in this
     * class prefixed by genParams_XXXX
     * @param qName the desired Query to be configured
     * @param cred of the requesting User
     * @return 
     */
    public QueryPerson initQuery(QueryPersonEnum qName, Credential cred){
         QueryPerson  query;
         List<SearchParamsPerson> paramsList = new ArrayList<>();
         SearchParamsPerson params = genParams_person_initParams(cred);
         
         
         switch(qName){
            case ACTIVE_PERSONS:
                paramsList.add(generateParams_persons_active(params, cred));
                break;
            case USER_PERSONS:
                paramsList.add(generateParams_persons_users(params, cred));
                break;
            case PROPERTY_PERSONS:
                paramsList.add(generateParams_person_prop(params, cred));
                break;
            case OCCPERIOD_PERSONS:
                paramsList.add(generateParams_persons_occPeriod(params, cred));
                break;
            case PERSON_NAME:
                paramsList.add(generateParams_persons_name(params, cred));
                break;
                
         }
         
         query = new QueryPerson(qName, paramsList, cred);
         query = (QueryPerson) initQueryFinalizeInit(query);
         
         return query;
     }
     
    
    /**
     * Container for query initialization logic based on the given Enum val
     * for this method's associated Query subclass. Delegates configuration of
     * filter-level settings on parameter objects to methods grouped later in this
     * class prefixed by genParams_XXXX
     * @param qName the desired Query to be configured
     * @param cred of the requesting User
     * @return 
     */
     public QueryEvent initQuery(QueryEventEnum qName, Credential cred){
         QueryEvent query;
         List<SearchParamsEvent> paramsList = new ArrayList<>();
         SearchParamsEvent params = genPerams_event_initParams(cred);
         
         
         switch(qName){
             case MUNI_MONTHYACTIVITY:
                 paramsList.add(genParams_event_muniMonthly(params, cred));
                 break;
             case OCCPERIOD:
                 paramsList.add(genParams_event_occperid(params, cred));
                 break;
             case CECASE:
                 paramsList.add(genParams_event_cecase(params, cred));
                 break;
             case PERSONS:
                 paramsList.add(genParams_event_persons(params, cred));
                 break;
            case CUSTOM:
                paramsList.add(genParams_event_custom(params, cred));
                break;
            default:
         }
         
         query = new QueryEvent(qName, paramsList, cred);
         return (QueryEvent) initQueryFinalizeInit(query);
     }
    
    /**
     * Container for query initialization logic based on the given Enum val
     * for this method's associated Query subclass. Delegates configuration of
     * filter-level settings on parameter objects to methods grouped later in this
     * class prefixed by genParams_XXXX
     * @param qName the desired Query to be configured
     * @param cred of the requesting User
     * @return 
     */
    public  QueryOccPeriod initQuery(QueryOccPeriodEnum qName, Credential cred){
            QueryOccPeriod  query;
            List<SearchParamsOccPeriod> paramsList = new ArrayList<>();
            SearchParamsOccPeriod params = genParams_occPeriod_initParams(cred);
         
         switch(qName){
            
            case ALL_PERIODS_IN_MUNI:
                paramsList.add(genParams_occPeriod_allPeriodsInMuni(params, cred));
                break;
            case AUTHWORKINPROGRESS:
                paramsList.add(generateParams_occPeriod_wip(params, cred));
                break;
            case RENTAL_ALL:
                break;
            case RENTAL_REGISTERED:
                break;
            case PERSONS:
                paramsList.add(genParams_occPeriod_persons(params, cred));
                break;
            case RENTAL_UNREGISTERED:
                break;
            
         }
         query = new QueryOccPeriod(qName, paramsList, cred);
         return (QueryOccPeriod) initQueryFinalizeInit(query);
     }
 
    
    /**
     * Container for query initialization logic based on the given Enum val
     * for this method's associated Query subclass. Delegates configuration of
     * filter-level settings on parameter objects to methods grouped later in this
     * class prefixed by genParams_XXXX
     * @param qName the desired Query to be configured
     * @param cred of the requesting User
     * @return 
     */
     public QueryCECase initQuery(QueryCECaseEnum qName, Credential cred){
         QueryCECase query;
         List<SearchParamsCECase> paramsList = new ArrayList<>();
         SearchParamsCECase params = genParams_ceCase_initParams(cred);
         
         switch(qName){
            case OPENCASES:
                paramsList.add(getDefaultSearchParams_CECase_allOpen(params, cred));
                 break;
            case EXPIRED_TIMEFRAMES:
                break;
            case CURRENT_TIMEFRAMES:
                break;
            case OPENED_30DAYS:
                paramsList.add(genParams_CECase_openedInDateRange(params, cred));
                break;
            case CLOSED_CASES:
                paramsList.add(genParams_CECase_closedInDateRange(params, cred));
                break;
            case UNRESOLVED_CITATIONS:
                break;
            case ANY_ACTIVITY_7Days:
                break;
            case ANY_ACTIVITY_30Days:
                break;
            case PROPERTY:
                paramsList.add(getDefaultSearchParams_CasesByProp(params, cred));
                break;
            case PROPINFOCASES:
                paramsList.add(genParams_cecase_propInfo(params, cred));
                break;
            case PERSINFOCASES:
                paramsList.add(genParams_ceCase_personinfo(params, cred));
                break;
            case PACC:
                paramsList.add(genParams_cecase_pacc(params, cred));
            case CUSTOM:
                break;
            case MUNI_ALL:
                paramsList.add(genParams_ceCase_muniAllActive(params, cred));
            default:
         }
         
         query = new QueryCECase(qName, paramsList, cred);
         return (QueryCECase) initQueryFinalizeInit(query);
     }
     
     
    /**
     * Container for query initialization logic based on the given Enum val
     * for this method's associated Query subclass. Delegates configuration of
     * filter-level settings on parameter objects to methods grouped later in this
     * class prefixed by genParams_XXXX
     * @param qName the desired Query to be configured
     * @param cred of the requesting User
     * @return 
     */
    public QueryCEAR initQuery(QueryCEAREnum qName, Credential cred) {
            QueryCEAR query = null;
            List<SearchParamsCEActionRequests> paramList = new ArrayList<>();
            SearchParamsCEActionRequests params = genParams_CEAR_initParams(cred);
            
            switch(qName){
                case UNPROCESSED:
                    paramList.add(generateParams_CEAR_Unprocessed(params, cred));
                    break;
                
                case ATTACHED_TO_CECASE:
                    paramList.add(generateParams_CEAR_attachedToCase(params, cred));
                    break;
                
                case BY_CURRENT_USER:
                    paramList.add(generateParams_CEAR_Unprocessed(params, cred));
                    break;
                    
                case CUSTOM:
                    paramList.add(generateParams_CEAR_Unprocessed(params, cred));
                    break;
                    
                default:
                    break;
            }
            query = new QueryCEAR(qName, paramList, cred);
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
        System.out.println("SearchCoordinator.buildQueryPropertyList | returning list of size " + queryList.size());
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
                QueryEvent qe = initQuery(queryTitle, cred);
                if(qe.getParamsListSize() != 0){
                    queryList.add(qe);
                }
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
        QueryCECase q;
        List<QueryCECase> queryList = new ArrayList<>();
        for(QueryCECaseEnum queryTitle: nameArray){
            if(checkAuthorizationToAddQueryToList(queryTitle, cred)){
                q = initQuery(queryTitle, cred);
                // skip adding to query list if it doesn't have a param bundle
                if(q.getParamsListSize() != 0){
                    queryList.add(q);    
                }
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
    
    /**
     * Sets switches on all shared parameter filter bundles in the SearchParams
     * family during init building process
     * 
     * @param params the caller should make an instance of its subclass
     * and pass it in for configuration
     * @param cred
     * @return 
     */
    private SearchParams genParams_initParams(SearchParams params, Credential cred){
        
        // Shared param #1
        params.setMuni_rtMin(RoleType.Developer);
        params.setMuni_ctl(true);
        params.setMuni_val(cred.getGoverningAuthPeriod().getMuni());
        
        // Shared param #2
        params.setDate_startEnd_ctl(false);
        params.setDate_field(null);
        params.setDate_rtMin(RoleType.MuniReader);
        params.setDate_start_val(null);
        params.setDate_end_val(null);
        
        // Shared param #3
        params.setDate_relativeDates_ctl(false);
        params.setDate_relativeDates_start_val(FILTER_OFF_DEFVALUE_INT);
        params.setDate_realtiveDates_end_val(FILTER_OFF_DEFVALUE_INT);

        // Shared param #4
        params.setUser_ctl(false);
        params.setUser_rtMin(RoleType.MuniStaff);
        params.setUser_field(null);
        params.setUser_val(null);
        
        // Shared param #5
        params.setBobID_ctl(false);
        params.setBobID_rtMin(null);
        params.setBobID_val(FILTER_OFF_DEFVALUE_INT);
        
        // Shared param #6
        params.setLimitResultCount_rtMin(RoleType.EnforcementOfficial);
        params.setLimitResultCount_ctl(true);
        params.setLimitResultCount_val(RESULT_COUNT_LIMIT_DEFAULT);

        // Shared param #7
        params.setActive_rtMin(RoleType.MuniStaff);
        params.setActive_ctl(true);
        params.setActive_val(true);
        
        return params;
    }
    
    
    
    /* --------------------------------------------
                     III. Property
       -------------------------------------------- */
    
    
    /**
     * Factory and config method for Property search params. They are delivered
     * to the caller with the most sensible defaults on all parameter sets 
     * so all it has to do is set the ones specific to its role
     * @return a configured instance
     */
    private SearchParamsProperty genParams_property_initParams(Credential cred){
        SearchParamsProperty params = new SearchParamsProperty();
        
        params = (SearchParamsProperty) genParams_initParams(params, cred);
        
        // ------------------------------------------------
        // ********* PROPERTY-SPECIFIC PARAMETERS *********
        // ------------------------------------------------
        
        // Param #PROP-1
        params.setZip_ctl(false);
        params.setZip_val(null);
        
        // Param #PROP-2
        params.setLotblock_ctl(false);
        params.setLotblock_val(null);
        
        // Param #PROP-3
        params.setBobID_ctl(false);
        params.setBobSource_val(null);
        
        // Param #PROP-4
        params.setParcelid_ctl(false);
        params.setParcelid_val(null);
        
        // Param #PROP-5
        params.setAddress_ctl(false);
        params.setAddress_val(null);
        
        // Param #PROP-6
        params.setCondition_ctl(false);
        params.setCondition_intensityClass_val(null);
        
        // Param #PROP-7
        params.setLandbankprospect_ctl(false);
        params.setLandbankprospect_intensityClass_val(null);
        
        // Param #PROP-8
        params.setLandbankheld_ctl(false);
        params.setLandbankheld_val(false);
        
        // Param #PROP-9
        params.setNonaddressable_ctl(false);
        params.setNonaddressable_val(false);
        
        // Param #PROP-10
        params.setUseType_ctl(false);
        params.setUseType_val(null);
        
        // Param #PROP-11
        params.setZoneClass_ctl(false);
        params.setZoneClass_val(null);
        
        // Param #PROP-12
        params.setTaxStatus_ctl(false);
        params.setTaxStatus_val(0);
        
        // Param #PROP-13
        params.setPropValue_ctl(false);
        params.setPropValue_max_val(FILTER_OFF_DEFVALUE_INT);
        params.setPropValue_min_val(FILTER_OFF_DEFVALUE_INT);
        
        // Param #PROP-14
        params.setConstructionYear_ctl(false);
        params.setConstructionYear_min_val(FILTER_OFF_DEFVALUE_INT);
        params.setConstructionYear_max_val(FILTER_OFF_DEFVALUE_INT);
        
        // Param #PROP-15
        params.setPerson_ctl(false);
        params.setPerson_val(null);
        
        return params;
    }
    
    private SearchParamsProperty genParams_property_persons(SearchParamsProperty params, Credential cred){
        params.setPerson_ctl(true);
        // downstream injects Person
        return params;
    }
    
    
    private SearchParamsProperty genParams_property_address(SearchParamsProperty params, Credential cred){
        params.setFilterName("Lead parameter bundle for property search by address");
        params.setFilterDescription("search by address");
        params.setAddress_ctl(true);

        return params;
        
    }
    
    private SearchParamsProperty genParams_property_recentlyUpdated(SearchParamsProperty params, Credential cred){
        params.setFilterName("Properties updated in the past month");
        params.setFilterDescription("Applies to properties with any field updated");
        
        params.setDate_startEnd_ctl(true);
        params.setDate_relativeDates_ctl(true);
        params.setDate_field(SearchParamsPropertyDateFieldsEnum.LAST_UPDATED);
        params.setDate_relativeDates_start_val(PASTPERIOD_RECENT);
        params.setDate_realtiveDates_end_val(PASTPERIOD_TODAY);
        
        return params;
        
    }

    
    // END V PROPERTY
    
    
    /* --------------------------------------------
                    IV. Person
       -------------------------------------------- */
   
    
    private SearchParamsPerson genParams_person_initParams(Credential cred){
        SearchParamsPerson params = new SearchParamsPerson();
        
        
        params = (SearchParamsPerson) genParams_initParams(params, cred);
        
        // filter PERS-1
        params.setNames_rtMin(RoleType.MuniStaff);
        params.setName_first_ctl(false);
        params.setName_first_val(null);
        
        // filter PERS-2
        params.setName_last_ctl(false);
        params.setName_last_val(null);
        
        // filter PERS-3
        params.setName_compositeLNameOnly_ctl(false);
        params.setName_compositeLNameOnly_val(false);
        
        // filter PERS-4
        params.setPhoneNumber_rtMin(RoleType.MuniStaff);
        params.setPhoneNumber_ctl(false);
        params.setPhoneNumber_val(null);
        
        // filter PERS-5
        params.setEmail_rtMin(RoleType.MuniStaff);
        params.setEmail_ctl(false);
        params.setEmail_val(null);
        
        // filter PERS-6
        params.setAddress_rtMin(RoleType.MuniStaff);
        params.setAddress_streetNum_ctl(false);
        params.setAddress_streetNum_val(null);
        
        // filter PERS-7
        params.setAddress_city_ctl(false);
        params.setAddress_city_val(null);
        
        // filter PERS-8
        params.setAddress_zip_ctl(false);
        params.setAddress_zip_val(null);
        
        // filter PERS-9
        params.setPersonType_rtMin(RoleType.MuniStaff);
        params.setPersonType_ctl(false);
        params.setPersonType_val(null);
        
        // filter PERS-10
        params.setVerified_rtMin(RoleType.MuniStaff);
        params.setVerified_ctl(false);
        params.setVerified_val(false);
       
        // filter PERS-11
        params.setSource_ctl(false);
        params.setSource_val(null);
                
        return params;
                
    }
    
    private SearchParamsPerson generateParams_persons_active(SearchParamsPerson params, Credential cred){
        
        
        params.setFilterName("Public person types");
        params.setFilterDescription("All persons declared to be public");
        
        params.setDate_startEnd_ctl(true);
        params.setDate_field(SearchParamsPersonDateFieldsEnum.LAST_UPDATED);
        params.setDate_relativeDates_ctl(true);
        params.setDate_relativeDates_start_val(PASTPERIOD_RECENT);
        params.setDate_realtiveDates_end_val(PASTPERIOD_TODAY);
        
         return params;
        
    }
    
    private SearchParamsPerson generateParams_persons_name(SearchParamsPerson params, Credential cred){
        params.setFilterName("First and last name filters");
        params.setName_first_ctl(true);
        params.setName_last_ctl(true);
        // downstream responsible for name parts
        return params;
    }
    
    private SearchParamsPerson generateParams_persons_occPeriod(SearchParamsPerson params, Credential cred){
        params.setFilterName("Occ period id filter");
        params.setFilterDescription("Persons whose type is a User");
        
        params.setOccPeriod_ctl(true);
        // user responsible for downstream occ period
        
        return params;
    }
    
    private SearchParamsPerson generateParams_persons_users(SearchParamsPerson params, Credential cred){
        params.setFilterName("User Persons");
        params.setFilterDescription("Persons whose type is a User");
        
        params.setPersonType_ctl(true);
        params.setPersonType_val(PersonType.User);
        
        return params;
    }
    
    private SearchParamsPerson generateParams_person_prop(SearchParamsPerson params, Credential cred){
        // turn off muni ctl to try to get query working
        params.setMuni_ctl(false);
        params.setFilterName("Persons at property X");
        params.setFilterDescription("Across all units");
        
        params.setProperty_ctl(true);
        // how do we signal we need downstream data?
        
        return params;
    }
   
    // END IV PERSON
    
    /* --------------------------------------------
                    V. Event
       -------------------------------------------- */
    private SearchParamsEvent genPerams_event_initParams(Credential cred){
        SearchParamsEvent params = new SearchParamsEvent();
        params = (SearchParamsEvent) genParams_initParams(params, cred);
        
        // filter EVENT-1
        params.setEventCat_ctl(false);
        params.setEventCat_val(null);
        
        // filter EVENT-2
        params.setEventType_ctl(false);
        params.setEventType_val(null);
        
        // filter EVENT-3
        params.setEventDomain_ctl(false);
        params.setEventDomain_val(null);

        // filter EVENT-4
        params.setEventDomainPK_ctl(false);
        params.setEventDomainPK_val(FILTER_OFF_DEFVALUE_INT);
        
        // filter EVENT-5
        params.setPerson_ctl(false);
        params.setPerson_val(null);
        
        // filter EVENT-6
        params.setDiscloseToMuni_ctl(false);
        params.setDiscloseToMuni_val(false);
        
        // filter EVENT-7
        params.setDiscloseToPublic_ctl(false);
        params.setDiscloseToPublic_val(false);
        
        return params;
        
    }
    
    
    public SearchParamsEvent genParams_event_persons(SearchParamsEvent params, Credential cred ){
        params.setPerson_ctl(true);
        // downstream injects person
        return params;
    }
    
    public SearchParamsEvent genParams_event_custom(SearchParamsEvent params, Credential cred ){
        // downstream injects person
        return params;
    }
    
    public SearchParamsEvent genParams_event_occperid(SearchParamsEvent params, Credential cred ){
        params.setEventDomain_ctl(true);
        params.setEventDomain_val(EventDomainEnum.OCCUPANCY);
        params.setBobID_ctl(true);
        return params;
    }
    
    public SearchParamsEvent genParams_event_cecase(SearchParamsEvent params, Credential cred ){
        params.setEventDomain_ctl(true);
        params.setEventDomain_val(EventDomainEnum.CODE_ENFORCEMENT);
        params.setBobID_ctl(true);
        return params;
        
    }
    
    
    public SearchParamsEvent genParams_event_muniMonthly(SearchParamsEvent params, Credential cred ){
        params.setDate_startEnd_ctl(true);
        params.setDate_field(SearchParamsEventDateFieldsEnum.CREATED_TS);
        params.setDate_end_val(LocalDateTime.now());
        params.setDate_start_val(LocalDateTime.now().minusDays(30));
        params.setLimitResultCount_ctl(false);
        params.setEventDomain_ctl(true);
        params.setEventDomain_val(EventDomainEnum.CODE_ENFORCEMENT);
        
        // all other event controls are off by default
        
        return params;
        
    }
    
    
    public SearchParamsEvent genParams_event_recentUserEvents(SearchParamsEvent params, Credential cred ){
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        
        
        params.setDate_startEnd_ctl(true);
        params.setDate_relativeDates_ctl(true);
        
//        params.setApplyDateSearchToDateOfRecord(true);
        // query from a week ago to now
        params.setDate_relativeDates_start_val(PASTPERIOD_MONTH);
        params.setDate_realtiveDates_end_val(PASTPERIOD_TODAY);
        
        params.setUser_ctl(true);
        
        return params;
    }
    
    
    
    // END V EVENT
    
    /* --------------------------------------------
                   VI. OccPeriod
       -------------------------------------------- */
    
    private SearchParamsOccPeriod genParams_occPeriod_initParams(Credential cred){
        
        SearchParamsOccPeriod params = new SearchParamsOccPeriod();
        params = (SearchParamsOccPeriod) genParams_initParams(params, cred);
        
        // filter OCC-1
        params.setProperty_ctl(false);
        params.setProperty_val(null);
        
        // filter OCC-2
        params.setPropertyUnit_ctl(false);
        params.setPropertyUnit_val(null);
        
        // filter OCC-3
        params.setPeriodType_ctl(false);
        params.setPeriodType_val(null);
        
        // filter OCC-4
        params.setPermitIssuance_ctl(false);
        params.setPermitIssuance_val(false);
        
        // filter OCC-5
        params.setInspectionPassed_ctl(false);
        params.setInspectionPassed_val(false);
        
        // filter OCC-6
        params.setThirdPartyInspector_ctl(false);
        params.setThirdPartyInspector_registered_val(false);
        params.setThirdPartyInspector_approved_val(false);
        
        // filter OCC-7
        params.setPacc_ctl(false);
        params.setPacc_val(false);
        
        return params;
    }
    
    
    private SearchParamsOccPeriod genParams_occPeriod_allPeriodsInMuni(SearchParamsOccPeriod params, Credential cred){
        params.setFilterName("All periods");
        params.setFilterDescription("All periods in muni");
        
        params.setActive_ctl(true);
        params.setActive_val(true);
        
        return params;
        
        
    }
  
    private SearchParamsOccPeriod genParams_occPeriod_persons(SearchParamsOccPeriod params, Credential cred){
        params.setPerson_ctl(true);
        // user injects Person
        return params;
    }
    
    private SearchParamsOccPeriod generateParams_occPeriod_wip(SearchParamsOccPeriod params, Credential cred){
        
        params.setFilterName("Periods with outstanding inspections");
        params.setFilterDescription("Inspections have been started by not certified as passed");
        
        params.setInspectionPassed_ctl(true);
        params.setInspectionPassed_val(false);
        
        return params;
        
    }
    
   
    // END VI OccPeriod
    
    
    /* --------------------------------------------
                     VII. CECase
       -------------------------------------------- */
    
    
    private SearchParamsCECase genParams_ceCase_initParams(Credential cred){
        SearchParamsCECase params = new SearchParamsCECase();
        params = (SearchParamsCECase) genParams_initParams(params, cred);
        
        // filter CECASE-1
        params.setCaseOpen_ctl(false);
        params.setCaseOpen_val(false);
        
        // filter CECASE-2
        params.setProperty_ctl(false);
        params.setProperty_val(null);
        
        // filter CECASE-3
        params.setPropertyUnit_ctl(false);
        params.setPropertyUnit_val(null);
        
        // filter CECASE-4
        params.setPropInfoCase_ctl(true);
        params.setPropInfoCase_val(false);
        
        // filter CECASE-5
        params.setPersonInfoCase_ctl(false);
        params.setPersonInfoCase_val(false);
        
        // filter CECASE-6
        params.setPersonInfoCaseID_ctl(false);
        params.setPersonInfoCaseID_val(null);
        
        // filter CECASE-7
        params.setSource_ctl(false);
        params.setSource_val(null);
        
        // filter CECASE-8
        params.setPacc_ctl(false);
        params.setPacc_val(false);
        
        // filter CECASE-9
        params.setCaseStage_ctl(false);
        params.setCaseStage_val(null);

        return params;
    }
    
    public SearchParamsCECase genParams_ceCase_personinfo(SearchParamsCECase params, Credential cred){
        params.setPersonInfoCaseID_ctl(true);
        params.setPersonInfoCase_ctl(true);
        // downstream puts in a Person
        
        return params;
    }
    
    /**
     * Returns a SearchParams subclass for retrieving all open
     * cases in a given municipality. Open cases are defined as a 
     * case whose closing date is null.
     * @param params
     * @param cred
     * @return a SearchParams subclass with mem vars ready to send
     * into the Integrator for case list retrieval
     */
    public SearchParamsCECase getDefaultSearchParams_CECase_allOpen(SearchParamsCECase params, Credential cred){
        params.setFilterName("All open cases");
        params.setFilterDescription("All open cases");
        
        
        // subclass specific
        params.setCaseOpen_ctl(true);
        params.setCaseOpen_val(true);
//        
//        params.setDateToSearchCECases("Opening date of record");
//        params.setUseCaseManager(false);
//        
//        params.setUseCasePhase(false);
//        params.setUseCaseStage(false);
//        params.setProperty_ctl(false);
//        params.setPropInfoCase_ctl(false);
//        params.setUseCaseManager(false);
        
        return params;
    }
    /**
     * Returns a SearchParams subclass for retrieving all open
     * cases in a given municipality. Open cases are defined as a 
     * case whose closing date is null.
     * @param params
     * @param cred
     * @return a SearchParams subclass with mem vars ready to send
     * into the Integrator for case list retrieval
     */
    public SearchParamsCECase genParams_CECase_openedInDateRange(SearchParamsCECase params, Credential cred){
        params.setFilterName("Cases opened in date range");
        params.setFilterDescription("Cases opened in date range");
        
        params.setDate_startEnd_ctl(true);
        params.setDate_field(SearchParamsCECaseDateFieldsEnum.ORIGINATIONTS);
        // subclass specific
//        params.setCaseOpen_ctl(true);
//        params.setCaseOpen_val(true);
//        
//        params.setDateToSearchCECases("Opening date of record");
//        params.setUseCaseManager(false);
//        
//        params.setUseCasePhase(false);
//        params.setUseCaseStage(false);
//        params.setProperty_ctl(false);
//        params.setPropInfoCase_ctl(false);
//        params.setUseCaseManager(false);
        
        return params;
    }
    /**
     * Returns a SearchParams subclass for retrieving all open
     * cases in a given municipality. Open cases are defined as a 
     * case whose closing date is null.
     * @param params
     * @param cred
     * @return a SearchParams subclass with mem vars ready to send
     * into the Integrator for case list retrieval
     */
    public SearchParamsCECase genParams_CECase_closedInDateRange(SearchParamsCECase params, Credential cred){
        params.setFilterName("All closed cases");
        params.setFilterDescription("Any closed cases");
        
        params.setCaseOpen_ctl(true);
        params.setCaseOpen_val(false);
        params.setDate_startEnd_ctl(false);
        params.setDate_field(SearchParamsCECaseDateFieldsEnum.CLOSE);
        // subclass specific
//        params.setCaseOpen_ctl(true);
//        params.setCaseOpen_val(true);
//        
//        params.setDateToSearchCECases("Opening date of record");
//        params.setUseCaseManager(false);
//        
//        params.setUseCasePhase(false);
//        params.setUseCaseStage(false);
//        params.setProperty_ctl(false);
//        params.setPropInfoCase_ctl(false);
//        params.setUseCaseManager(false);
        
        return params;
    }
    
    public SearchParamsCECase genParams_cecase_pacc(SearchParamsCECase params, Credential cred){
        params.setFilterName("PACC only");
        params.setFilterDescription("PACC only");
        params.setPacc_ctl(true);
        
        return params;
        
    }
    
    public SearchParamsCECase genParams_cecase_propInfo(SearchParamsCECase params, Credential cred){
        params.setFilterName("Prop info only");
        params.setFilterDescription("Prop info only");
        
        params.setProperty_ctl(true);
        // downstream must set Property object
        params.setPropInfoCase_ctl(true);
        params.setPersonInfoCase_val(true);
        
        return params;
        
    }
    
    public SearchParamsCECase getDefaultSearchParams_CasesByProp(SearchParamsCECase params, Credential cred){
        params.setFilterName("Property only");
        params.setFilterDescription("Property only");
        
        params.setProperty_ctl(true);
        return params;
    }
    
     /**
     * Returns a SearchParams subclass for retrieving all open
     * cases in a given municipality. Open cases are defined as a 
     * case whose closing date is null.
     * @param params
     * @return a SearchParams subclass with mem vars ready to send
     * into the Integrator for case list retrieval
     */
    public SearchParamsCECase genParams_cecase_closedPast30Days(SearchParamsCECase params, Credential cred){
        params.setFilterName("CECases closed in past month");

        params.setDate_startEnd_ctl(true);
        params.setDate_relativeDates_ctl(true);
        params.setDate_field(SearchParamsCECaseDateFieldsEnum.CLOSE);
        params.setDate_relativeDates_start_val(PASTPERIOD_YEAR);
        params.setDate_realtiveDates_end_val(PASTPERIOD_TODAY);
        
        return params;
    }
    
    public SearchParamsCECase genParams_ceCase_muniAllActive(SearchParamsCECase params, Credential cred){
        params.setFilterName("All active in muni");
        params.setActive_ctl(true);
        params.setActive_val(true);
        
        params.setPersonInfoCase_ctl(true);
        params.setPersonInfoCase_val(false);
        
        params.setPropInfoCase_ctl(true);
        params.setPropInfoCase_val(false);
        
        return params;
    }
    
   
    // END VII CECase
    
    /* --------------------------------------------
                  VIII. CEActionRequest
       -------------------------------------------- */
    
    private SearchParamsCEActionRequests genParams_CEAR_initParams(Credential cred){
        SearchParamsCEActionRequests params = new SearchParamsCEActionRequests();
        
        params = (SearchParamsCEActionRequests) genParams_initParams(params, cred);
        
        // filter CEAR-1
        params.setRequestStatus_ctl(false);
        params.setRequestStatus_val(null);
        
        // filter CEAR-2
        params.setIssueType_ctl(false);
        params.setIssueType_val(null);
        
        // filter CEAR-3
        params.setNonaddressable_ctl(false);
        params.setNonaddressable_val(false);
        
        // filter CEAR-4
        params.setUrgent_ctl(false);
        params.setUrgent_val(false);
        
        // filter CEAR-5
        params.setCaseAttachment_ctl(false);
        params.setCaseAttachment_val(false);
        
        // filter CEAR-6
        params.setCecase_ctl(false);
        params.setCecase_val(null);
        
        // filter CEAR-7
        params.setPacc_ctl(false);
        params.setPacc_val(false);
        
        // filter CEAR-8
        params.setRequestorPerson_ctl(false);
        params.setRequestorPerson_val(null);
        
        // filter CEAR-9
        params.setProperty_ctl(false);
        params.setProperty_val(null);
        
        return params;
    }
    
    
     /**
      * TODO : Finish!
     * @param params
     * @param cred
      * @param m
      * @param u
      * @return
      * @throws IntegrationException 
      */
     public SearchParamsCEActionRequests generateParams_CEAR_RequestorCurrentU(SearchParamsCEActionRequests params, Credential cred) throws IntegrationException{
        UserCoordinator uc = getUserCoordinator();
        
        params.setFilterName("Action requests you've made");
         
        params.setUser_ctl(true);
        params.setUser_val(uc.user_getUser(cred.getGoverningAuthPeriod().getUserID()));
            
        return params;

    }
    
    
     public SearchParamsCEActionRequests generateParams_CEAR_Unprocessed(SearchParamsCEActionRequests params, Credential cred) {
         
            
        CEActionRequestIntegrator cari = getcEActionRequestIntegrator();

        params.setCaseAttachment_ctl(false);
        params.setRequestStatus_ctl(true);
        
        try {
            params.setRequestStatus_val(cari.getRequestStatus(Integer.parseInt(
                    getResourceBundle(Constants.DB_FIXED_VALUE_BUNDLE).getString("actionRequestInitialStatusCode"))));
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        return params;
    }
    
    
    
    
     public SearchParamsCEActionRequests generateParams_CEAR_attachedToCase(SearchParamsCEActionRequests params, Credential cred){
            
        params.setCaseAttachment_ctl(true);
        params.setCaseAttachment_val(true);
        
        return params;
    }
    
    // END VIII. CEActionRequest
    
    
}
