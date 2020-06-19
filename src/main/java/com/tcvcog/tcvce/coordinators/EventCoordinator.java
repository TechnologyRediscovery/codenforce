/*
 * Copyright (C) 2017 Turtle Creek Valley
Council of Governments, PA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.O
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tcvcog.tcvce.coordinators;

import com.tcvcog.tcvce.application.BackingBeanUtils;
import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.reports.ReportConfigCEEventList;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import java.io.Serializable;
import com.tcvcog.tcvce.util.Constants;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.faces.application.FacesMessage;

/**
 *
 * Object playing the role of coordinator in the MVC framework by interfacing between
 * the JSF backing beans and the database integration classes.
 * 
 * This role involves several duties:
 * 
 * <ol>
 * <li>Generating new events for Code Enforcement and occupancy</li>
 * <li>Checking event creation permissions before allowing events of
 * restricted categories to be attached to other system objects.</li>
 * </ol>
 * 
 * 
 * @author Ellen Bascomb
 */
public class EventCoordinator extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of EventCoordinator
     */
    public EventCoordinator() {
        
    }
    
    
//    --------------------------------------------------------------------------
//    *************************** EVENT MAIN ***********************************
//    --------------------------------------------------------------------------
    
    
    /**
     * Primary access point for events by ID
     * @param eventID
     * @return 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    public EventCnF getEvent(int eventID) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        if(eventID == 0){
            return null;
        }
        EventCnF ev = ei.getEvent(eventID);
        try {
            configureEvent(ev);
        } catch (EventException ex) {
            System.out.println(ex);
        }
        return ev;
    }
    
    
    /**
     * Creates a data-rich subclass of our events that contains a Property object
     * and Property unit data useful for displaying the event without its parent
     * object context (i.e. in an activity report)
     * @param ev
     * @return the data-rich subclass of EventCnF
     * @throws EventException
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.SearchException 
     */
    public EventCnFPropUnitCasePeriodHeavy assembleEventCnFPropUnitCasePeriodHeavy(EventCnF ev) throws EventException, IntegrationException, SearchException{
        PropertyCoordinator pc = getPropertyCoordinator();
        OccupancyCoordinator oc = getOccupancyCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        
        if(ev == null) return null;
        EventCnFPropUnitCasePeriodHeavy edh = new EventCnFPropUnitCasePeriodHeavy(ev);
        if(ev.getDomain() == EventDomainEnum.OCCUPANCY && ev.getOccPeriodID() != 0){
            edh.setPeriod(oc.getOccPeriodPropertyUnitHeavy(edh.getOccPeriodID()));
        } else if(ev.getDomain() == EventDomainEnum.CODE_ENFORCEMENT && ev.getCeCaseID() != 0){
            edh.setCecase(cc.assembleCECasePropertyUnitHeavy(cc.getCECase(edh.getCeCaseID()), getSessionBean().getSessUser().getMyCredential()));
            // note that a Property object is already inside our CECase base class
        } else {
            throw new EventException("Cannot build data heavy event");
        }
        return edh;
    }
    
    /**
     * Utility method for assembling a list of data-heavy events from a List of 
     * plain old events
     * @param evList
     * @return the list of data heavy events, never null
     * @throws EventException
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.SearchException 
     */
    public List<EventCnFPropUnitCasePeriodHeavy> assembleEventCnFPropUnitCasePeriodHeavyList(List<EventCnF> evList) throws EventException, IntegrationException, SearchException{
        List<EventCnFPropUnitCasePeriodHeavy> edhList = new ArrayList<>();
        if(evList != null && !evList.isEmpty() ){
            for(EventCnF ev: evList){
                edhList.add(assembleEventCnFPropUnitCasePeriodHeavy(ev));
            }
        }
        return edhList;
    }
    
     /**
     * Utility for downcasting a list of CECasePropertyUnitDataHeavy 
     * to the base class
     * @param evHeavyList
     * @return 
     */
    public List<EventCnF> downcastEventCnFPropertyUnitHeavy(List<EventCnFPropUnitCasePeriodHeavy> evHeavyList){
        List<EventCnF> evList = new ArrayList<>();
        if(evHeavyList != null && !evHeavyList.isEmpty()){
            for(EventCnFPropUnitCasePeriodHeavy e: evHeavyList){
                evList.add((EventCnF) e);
            }
            
        }
        return evList;
    }
    
    /**
     * Called by the EventIntegrator and other getEventCnF methods to set member variables based on business
     * rules before sending the event onto its requesting method. Checks for request processing, 
     * sets intended responder for action requests, etc.
     * @param ev
     * @param user
     * @return a nicely configured EventCEEcase
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    private EventCnF configureEvent(EventCnF ev) throws IntegrationException, EventException{
        
        // Declare this event as either in the CE or Occ domain with 
        // our hacky little enum thingy
        if(ev != null){
            if(ev.getCeCaseID() !=0 && ev.getOccPeriodID() != 0 ){
                throw new EventException("EventCnF cannot have a non-zero CECase and OccPeriod ID");
            }
            if(ev.getCeCaseID() != 0){
                ev.setDomain(EventDomainEnum.CODE_ENFORCEMENT);
            } else if(ev.getCeCaseID() != 0){
                ev.setDomain(EventDomainEnum.OCCUPANCY);
            } else {
                throw new EventException("EventCnF must have either an occupancy period ID, or CECase ID");
            }
        }
       
        // begin configuring the event proposals assocaited with this event
        // remember: event proposals are specified in an EventCategory object
        // but when we build an EventCnF object, the ProposalImplementation lives on the EventCnF itself
        // 
//        if(ev.getCategory().getDirective() != null){
//            TODO: OccBeta
//            Proposal imp = ei.getProposalImplAssociatedWithEvent(ev);
//            imp.setCurrentUserCanEvaluateProposal(determineCanUserEvaluateProposal(ev, user, userAuthMuniList));
//            ev.setEventProposalImplementation(imp);
//        }
//        ev.setPersonList(pi.getPersonOccPeriodList(ev));
        
        return ev;
    }
    
    /**
     * Implements business logic to ensure EventCnF correctness; called by insert
     * and edit event methods; Throws an EventException if there's an error
     * 
     * @param ev
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    public void auditEvent(EventCnF ev) throws EventException{
        if(ev.getTimeStart() == null){
            throw new EventException("Events must have a start time");
        }
        if(ev.getTimeEnd() != null){
            if(ev.getTimeEnd().isBefore(ev.getTimeStart())){
                throw new EventException("Events with end times must not have an end time before start time");
            }
        }
    }
    
    
    /**
     * Business rule aware pathway to update fields on EventCnF objects
 When updating Person links, this method clears all previous connections
 and rebuilds the mapping from scratch on each update.
     * 
     * @param ev
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.EventException 
     */
    public void editEvent(EventCnF ev) throws IntegrationException, EventException{
        PersonIntegrator pi = getPersonIntegrator();
        EventIntegrator ei = getEventIntegrator();
        
        auditEvent(ev);
        ei.updateEvent(ev);
        pi.eventPersonClear(ev);
        pi.eventPersonsConnect(ev, ev.getPersonList());
        
    }
    
    public void deactivateEvent(EventCnF ev, UserAuthorized ua) throws IntegrationException{
        SystemCoordinator sc = getSystemCoordinator();
        EventIntegrator ei = getEventIntegrator();
        ev.setActive(false);
        ev.setNotes(sc.formatAndAppendNote(ua, "Event deactivated by User with credential sig " + ua.getMyCredential().getSignature(), ev.getNotes()));
        ei.updateEvent(ev);
        
    }
    
    public void deleteEvent(EventCnF ev, UserAuthorized u) throws AuthorizationException{
        EventIntegrator ei = getEventIntegrator();
        try {
            if(u.getMyCredential().isHasDeveloperPermissions()){
                ei.deleteEvent(ev);
            } else {
                throw new AuthorizationException("Must have sys admin permissions "
                        + "to delete event; marking an event as inactive is like deleting it");
            }
        } catch (IntegrationException ex) {

        }
    }
  
    /**
     * Core coordinator method called by all other classes who want to 
     * create their own event. Restricts the event type based on current
     * case phase (closed cases cannot have action, origination, or compliance events.
     * Includes the instantiation of EventCnF objects
     * 
     * @param erg which for Beta v0.9 includes CECaseDataHeavy and OccPeriod object; null means 
     * caller will need to insert the BOb ID later
     * @param ec the type of event to attach to the case
     * @return an initialized event with basic properties set
     * @throws BObStatusException thrown if the case is in an improper state for proposed event
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    public EventCnF initEvent(IFace_EventRuleGoverned erg, EventCategory ec) throws BObStatusException, EventException{
        
        if(ec == null) return null;
        
        CECaseDataHeavy cse = null;
        OccPeriod op = null;
        
        // the moment of event instantiaion!!!!
        EventCnF e = new EventCnF();
        
        if(erg != null){
            if(erg instanceof CECaseDataHeavy){
                cse = (CECaseDataHeavy) erg;
                 if(cse.getCasePhase() == CasePhaseEnum.Closed && 
                    (
                        ec.getEventType() == EventType.Action
                        || 
                        ec.getEventType() == EventType.Origination
                        ||
                        ec.getEventType() == EventType.Compliance
                    )
                ){
                    throw new BObStatusException("This event cannot be attached to a closed case");
                }
                e.setCeCaseID(cse.getCaseID());
                e.setDomain(EventDomainEnum.CODE_ENFORCEMENT);
            } else if (erg instanceof OccPeriod){
                op = (OccPeriod) erg;
                e.setOccPeriodID(op.getPeriodID());
                e.setDomain(EventDomainEnum.OCCUPANCY);
            }
        }
        
        auditEvent(e);
        
        // Long-term TODO: check against permitted event types
        e.setCategory(ec);
        
        e.setTimeStart(LocalDateTime.now());
        e.setTimeEnd(e.getTimeStart().plusMinutes(ec.getDefaultdurationmins()));
        
        e.setActive(true);
        e.setHidden(false);
        
        return e;
    }
    
    
    
//    --------------------------------------------------------------------------
//    ***************************** SEARCH *************************************
//    --------------------------------------------------------------------------
    
    
    
    
      
    public ReportConfigCEEventList getDefaultReportConfigCEEventList(){
        SearchCoordinator sc = getSearchCoordinator();
        ReportConfigCEEventList config = new ReportConfigCEEventList();
        config.setIncludeAttachedPersons(true);
        config.setIncludeCaseActionRequestInfo(false);
        config.setGenerationTimestamp(LocalDateTime.now());
        config.setIncludeEventTypeSummaryChart(true);
        config.setIncludeActiveCaseListing(false);
        config.setIncludeCompleteQueryParamsDump(false);
        config.setSortInRevChrono(true);
        return config;
    }
    
//    --------------------------------------------------------------------------
//    ************************* EVENT CATEGORIES *******************************
//    --------------------------------------------------------------------------
    
     /**
     * Implements business rules for determining which event types are allowed
 to be attached to the given CECaseDataHeavy based on the case's phase and the
 user's permissions in the system.
     *
     * Used for displaying the appropriate event types to the user on the
     * cecases.xhtml page
     *
     * @param c the CECaseDataHeavy on which the event would be attached
     * @param u the User doing the attaching
     * @return allowed EventTypes for attaching to the given case
     */
    public List<EventType> getPermittedEventTypesForCECase(CECaseDataHeavy c, UserAuthorized u) {
        List<EventType> typeList = new ArrayList<>();
        RoleType role = u.getRole();
        if (role == RoleType.EnforcementOfficial || u.getRole() == RoleType.Developer) {
            typeList.add(EventType.Action);
            typeList.add(EventType.Timeline);
        }
        if (role != RoleType.MuniReader) {
            typeList.add(EventType.Communication);
            typeList.add(EventType.Meeting);
            typeList.add(EventType.Custom);
        }
        return typeList;
    }
    
    
    public List<EventType> getPermittedEventTypesForOcc(OccPeriod period, UserAuthorized u) {
        List<EventType> typeList = new ArrayList<>();
        RoleType role = u.getRole();
        if (role == RoleType.EnforcementOfficial || u.getRole() == RoleType.Developer) {
            typeList.add(EventType.Action);
            typeList.add(EventType.Timeline);
            typeList.add(EventType.Occupancy);
        }
        if (role != RoleType.MuniReader) {
            typeList.add(EventType.Communication);
            typeList.add(EventType.Meeting);
            typeList.add(EventType.Custom);
        }
        return typeList;
    }
    
    public List<EventType> getEventTypesAll(){
        List<EventType> typeList = new ArrayList<>();
        typeList.addAll(Arrays.asList(EventType.values()));
        return typeList;
    }
    
    
    /**
     * Factory method for creating bare event categories.
     * This is used when creating search parameter objects where we want event
     * types without a specific category, but we need a EventCategory shell
     * in which to insert the EventType
     * 
     * @return an EventCategory container with basic properties set
     */
    public EventCategory initEventCategory(){
        EventCategory ec =  new EventCategory();
        ec.setUserdeployable(true);
        ec.setHidable(true);
        // TODO: finishing autoconfiguring these 
        return ec;
    }
    
    /**
     * Factory method for creating event categories when only the categoryID
     * is available. This is handy since the insertEvent method on the integrator
     * only needs the categoryID for storing in the database
     * @param catID the categoryID of the EventCategory you want
     * @return an instantiated EventCategory object
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public EventCategory initEventCategory(int catID) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        EventCategory ec =  ei.getEventCategory(catID);
        return ec;
    }
  
    
    public List<EventCategory> getEventCategoryListActive() throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        List<EventCategory> catList = ei.getEventCategoryList();
        for(EventCategory cat: catList){
            // TODO: remove inactive EventCategories
            // TODO: add active flag in DB and int methods
        }
        return catList;
    }
    
      
    public List<EventCategory> loadEventCategoryListUserAllowed(EventType et, UserAuthorized u) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        // TODO: add logic for only allowing certain event cats based on user needs
        return ei.getEventCategoryList(et);
        
    }
    
    
//    --------------------------------------------------------------------------
//    **************** OPERATION SPECIFIC EVENT CONFIG *************************
//    --------------------------------------------------------------------------
    
    /**
     * Creates a populated event to log the change of a code violation update. 
     * The event is coming to us from the violationEditBB with the description and disclosures flags
     * correct. This method needs to set the description from the resource bundle, and 
     * set the date of record to the current date
     * @param ceCase the CECaseDataHeavy whose violation was updated
     * @param cv the code violation being updated
     * @param event An initialized event
     * @throws IntegrationException bubbled up from the integrator
     * @throws EventException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.ViolationException 
     */
    public void generateAndInsertCodeViolationUpdateEvent(CECaseDataHeavy ceCase, CodeViolation cv, EventCnF event) 
            throws IntegrationException, EventException, BObStatusException, ViolationException{
        EventIntegrator ei = getEventIntegrator();
        CaseCoordinator cc = getCaseCoordinator();
        String updateViolationDescr = getResourceBundle(Constants.MESSAGE_TEXT).getString("violationChangeEventDescription");
       
        // hard coded for now
//        event.setCategory(ei.getEventCategory(117));
        event.setCeCaseID(ceCase.getCaseID());
//        event.setDateOfRecord(LocalDateTime.now());
        event.setDescription(updateViolationDescr);
        //even descr set by violation coordinator
        event.setOwner(getSessionBean().getSessUser());
        // disclose to muni from violation coord
        // disclose to public from violation coord
        event.setActive(true);
        
        cc.attachNewEventToCECase(ceCase, event, null);
    }
    
      /**
     * Takes in a well-formed event message from the CaseCoordinator and
     * initializes the appropriate properties on the event before insertion
     * @param caseID id of the case to which the event should be attached
     * @param message the text of the event description message
     * @throws IntegrationException in the case of broken integration process
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    public void attachPublicMessagToCECase(int caseID, String message) throws IntegrationException, BObStatusException, EventException{
        EventIntegrator ei = getEventIntegrator();
        UserCoordinator uc = getUserCoordinator();
        
        int publicMessageEventCategory = Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("publicCECaseMessage"));
        EventCategory ec = initEventCategory(publicMessageEventCategory);
        
        // setup all the event properties
        EventCnF event =  initEvent(null, ec);
        event.setCategory(ec);
//        event.setDateOfRecord(LocalDateTime.now());
        event.setDescription(message);
        event.setOwner(uc.getUserRobot());
        event.setDiscloseToMunicipality(true);
        event.setDiscloseToPublic(true);
        event.setActive(true);
        event.setHidden(false);
        event.setNotes("Event created by a public user");
        
        // sent the built event to the integrator!
        ei.insertEvent(event);
    }
    
    
    
    
    
    /**
     * Configures an event which represents the moment of compliance with
     * a code violation attached to a code enforcement case
     * 
     * @param violation
     * @return a partially-baked event ready for inserting
     * @throws IntegrationException 
     */
    public EventCnF generateViolationComplianceEvent(CodeViolation violation) throws IntegrationException{
        EventCnF e = new EventCnF();
        EventIntegrator ei = getEventIntegrator();
          e.setCategory(ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("complianceEvent"))));
        e.setDescription("Compliance with municipal code achieved");
        e.setActive(true);
        e.setDiscloseToMunicipality(true);
        e.setDiscloseToPublic(true);
        
        StringBuilder sb = new StringBuilder();
        sb.append("Compliance with the following code violations was observed:");
        sb.append("<br /><br />");
        
            sb.append(violation.getViolatedEnfElement().getCodeElement().getOrdchapterNo());
            sb.append(".");
            sb.append(violation.getViolatedEnfElement().getCodeElement().getOrdSecNum());
            sb.append(".");
            sb.append(violation.getViolatedEnfElement().getCodeElement().getOrdSubSecNum());
            sb.append(":");
            sb.append(violation.getViolatedEnfElement().getCodeElement().getOrdSubSecTitle());
            sb.append(" (ID ");
            sb.append(violation.getViolationID());
            sb.append(")");
            sb.append("<br /><br />");
        e.setNotes(sb.toString());
        EventCnF cev = new EventCnF(e);
        return cev;
    }
    
  
    
    
    
    /**
     * 
     * 
     * @deprecated since phases aren't DB stored fields, no need to do any overriding
     * @param currentCase
     * @param pastPhase
     * @throws IntegrationException
     * @throws BObStatusException
     * @throws ViolationException
     * @throws EventException 
     */
    public void generateAndInsertManualCasePhaseOverrideEvent(CECaseDataHeavy currentCase, CasePhaseEnum pastPhase) 
            throws IntegrationException, BObStatusException, ViolationException, EventException{
        
          EventIntegrator ei = getEventIntegrator();
          CaseCoordinator cc = getCaseCoordinator();
        
        EventCnF event = initEvent(currentCase, ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("casePhaseManualOverride"))));
        
        StringBuilder sb = new StringBuilder();
        sb.append("Manual case phase change from  \'");
        sb.append(pastPhase.toString());
        sb.append("\' to \'");
        sb.append(currentCase.getCasePhase().toString());
        sb.append("\' by a a case officer.");
        event.setDescription(sb.toString());
        
        event.setCeCaseID(currentCase.getCaseID());
//        event.set(LocalDateTime.now());
        // not sure if I can access the session level info for the specific user here in the
        // coordinator bean
        event.setOwner(getSessionBean().getSessUser());
        event.setActive(true);
        
        cc.attachNewEventToCECase(currentCase, event, null);
        
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "The case phase has been changed", ""));
    }
    
    
     

    
    
} // close class