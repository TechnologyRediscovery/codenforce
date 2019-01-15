/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.coordinators.EventCoordinator;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.search.SearchParams;
import com.tcvcog.tcvce.entities.search.SearchParamsCEActionRequests;
import com.tcvcog.tcvce.entities.search.SearchParamsCECases;
import com.tcvcog.tcvce.entities.search.SearchParamsCEEvents;
import com.tcvcog.tcvce.entities.search.SearchParamsPersons;
import com.tcvcog.tcvce.entities.search.SearchParamsProperties;
import java.io.Serializable;
import java.time.LocalDateTime;

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
    
    
    
    /**
     * The temporarily hard-coded values for default search parameters for various
     * types of search Param objects
     * 
     * @return an search params object for CEAction requests with default values
     * which amount to requests that aren't attached to a case and were submitted
     * within the past 10 years
     */
    public SearchParamsCEActionRequests getDefaultSearchParamsCEActionRequests(){
        
            System.out.println("CaseCoordinator.configureDefaultSearchParams "
                    + "| found actionrequest param object");
            
            SearchParamsCEActionRequests sps = new SearchParamsCEActionRequests();

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
    
    /**
     * Returns a SearchParams subclass for retrieving all open
     * cases in a given municipality. Open cases are defined as a 
     * case whose closing date is null.
     * @return a SearchParams subclass with mem vars ready to send
     * into the Integrator for case list retrieval
     */
    public SearchParamsCECases getDefaultSearchParamsCECase(){
        SearchParamsCECases spcecase = new SearchParamsCECases();
        
        // superclass 
        spcecase.setFilterByStartEndDate(false);
        spcecase.setFilterByObjectID(false);
        spcecase.setLimitResultCountTo100(true);
        
        // subclass specific
        spcecase.setUseIsOpen(true);
        spcecase.setIsOpen(true);
        spcecase.setUseCaseCloseDateRange(false);
        spcecase.setUseCaseManagerID(false);
        spcecase.setUseLegacy(false);
        
        return spcecase;
    }
    
    public SearchParamsPersons getDeafaultSearchParamsPersons(){
        SearchParamsPersons spp = new SearchParamsPersons();
        // on the parent class SearchParams
        spp.setFilterByStartEndDate(false);
        spp.setLimitResultCountTo100(true);
        
        // on the subclass SearchParamsPersons
        spp.setFilterByFirstName(false);
        spp.setFilterByLastName(true);
        spp.setOnlySearchCompositeLastNames(false);
        
        spp.setFilterByPersonTypes(false);
        spp.setFilterByEmail(false);
        spp.setFilterByAddressStreet(false);
        
        spp.setFilterByActiveSwitch(false);
        spp.setFilterByVerifiedSwitch(false);
        spp.setFilterByPropertySwitch(false);
        
        return spp;
        
    }
    
    public SearchParamsProperties getDefaultSearchParamsProperties(){
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
    
    public SearchParamsCEEvents getSearchParamsEventsRequiringView(int ownerID){
        EventCoordinator ec = getEventCoordinator();
        
        // event types are always bundled in an EventCategory
        // so in this case of this query, we don't care about the Category title,
        // only the type
        EventCategory timelineEventTypeCategory = ec.getInitializedEventCateogry();
        timelineEventTypeCategory.setEventType(EventType.Timeline);
        
        SearchParamsCEEvents eventParams = new SearchParamsCEEvents();
        
        eventParams.setFilterByStartEndDate(false);
        eventParams.setFilterByObjectID(false);
        eventParams.setLimitResultCountTo100(true);
        
        eventParams.setFilterByEventCategory(false);
        eventParams.setFilterByEventType(true);
        eventParams.setEventCategory(timelineEventTypeCategory);
        
        eventParams.setFilterByCaseID(false);
        
        eventParams.setFilterByEventOwner(true);
        eventParams.setOwnerUserID(ownerID);
        
        eventParams.setFilterByActive(true);
        eventParams.setIsActive(true);
        
        eventParams.setFilterByRequiresViewConfirmation(true);
        eventParams.setIsViewConfirmationRequired(true);
        
        eventParams.setFilterByViewed(true);
        eventParams.setIsViewed(false);
        
        eventParams.setFilterByViewConfirmedBy(false);
        eventParams.setFilterByViewConfirmedAtDateRange(false);
        
        eventParams.setFilterByHidden(false);
        
        return eventParams;
    }
}
