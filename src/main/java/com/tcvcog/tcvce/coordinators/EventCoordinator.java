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
import com.tcvcog.tcvce.domain.AuthorizationException;
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.CaseChangeRule;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EventCECase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.EventCECaseCasePropBundle;
import com.tcvcog.tcvce.entities.EventProposal;
import com.tcvcog.tcvce.entities.EventProposalImplementation;
import com.tcvcog.tcvce.entities.Municipality;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.entities.reports.ReportConfigCEEventList;
import com.tcvcog.tcvce.entities.User;
import com.tcvcog.tcvce.entities.search.SearchParamsEventCECase;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import com.tcvcog.tcvce.integration.UserIntegrator;
import java.io.Serializable;
import com.tcvcog.tcvce.util.Constants;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 * @author Eric C. Darsow
 */
public class EventCoordinator extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of EventCoordinator
     */
    public EventCoordinator() {
        
    }
    
   
    
    
    public SearchParamsEventCECase getSearchParamsCEEventsRequiringAction(User user, Municipality m){
        SearchCoordinator sc = getSearchCoordinator();
        return sc.getSearchParamsEventsRequiringAction(user,m);
        
    }
    
    public SearchParamsEventCECase getSearchParamsOfficerActibityPastWeek(User user, Municipality m){
        SearchCoordinator sc = getSearchCoordinator();
        return sc.getSearchParamsOfficerActivity(user, m);
        
    }
    
    
    public SearchParamsEventCECase getSearchParamsComplianceEvPastMonth(Municipality m){
        SearchCoordinator sc = getSearchCoordinator();
        return sc.getSearchParamsComplianceEvPastMonth(m);
    }
    
    
    /**
     * Utility method for calling configureEvent on all EventCECase objects
     * in a list passed back from a call to Query events
     * @param evList
     * @param user
     * @param userAuthMuniList
     * @return
     * @throws IntegrationException 
     */
    public List<EventCECaseCasePropBundle> configureEventBundleList(    List<EventCECaseCasePropBundle> evList, 
                                                                         User user, 
                                                                         List<Municipality> userAuthMuniList) throws IntegrationException{
        Iterator<EventCECaseCasePropBundle> iter = evList.iterator();
        while(iter.hasNext()){
            configureEvent(iter.next().getEvent(), user, userAuthMuniList);
        }
        return evList;
    }
    
    public ReportConfigCEEventList getDefaultReportConfigCEEventList(){
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
    
    
    
    /**
     * Called by the EventIntegrator and other getEventCECase methods to set member variables based on business
 rules before sending the event onto its requesting method. Checks for request processing, 
     * sets intended responder for action requests, etc.
     * @param ev
     * @param user
     * @param userAuthMuniList
     * @return a nicely configured EventCEEcase
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public EventCECase configureEvent(EventCECase ev, User user, List<Municipality> userAuthMuniList) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        PersonIntegrator pi = getPersonIntegrator();
       
        // begin configuring the event proposals assocaited with this event
        // remember: event proposals are specified in an EventCategory object
        // but when we build an Event object, the ProposalImplementation lives on the Event itself
        // 
        if(ev.getCategory().getEventProposal() != null){
            EventProposalImplementation imp = ei.getProposalImplAssociatedWithEvent(ev);
            imp.setCurrentUserCanEvaluateProposal(determineCanUserEvaluateProposal(ev, user, userAuthMuniList));
            ev.setEventProposalImplementation(imp);
        }
        ev.setPersonList(pi.getPersonList(ev));
        
        return ev;
    }
    
    
    
    
    
     /**
     * Pathway for injecting business logic into the event search process. Now its just a pass through.
     * @param params
     * @param user the current user
     * @param userAuthMuniList
     * @return
     * @throws IntegrationException 
     */
    public List<EventCECaseCasePropBundle> queryEvents(SearchParamsEventCECase params, User user, List<Municipality> userAuthMuniList) throws IntegrationException, CaseLifecyleException{
        EventIntegrator ei = getEventIntegrator();
        List<EventCECaseCasePropBundle> evList = configureEventBundleList(  ei.getEventsCECase(params),
                                                                            user,
                                                                            userAuthMuniList);
        return evList;
    }
    
    /**
     * Utility method for setting view confirmation authorization 
     * at the event level by user
     * @param ev
     * @param u the User viewing the list of CEEvents
     * @param muniList
     * @return 
     */
    public boolean determineCanUserEvaluateProposal(EventCECase ev, User u, List<Municipality> muniList){
        boolean canEvaluateProposal = false;
        EventProposal evProp = ev.getCategory().getEventProposal();
        
        // direct event assignment allows view conf to cut across regular permissions
        // checks
        if(ev.getOwner().equals(u) || u.getKeyCard().isHasDeveloperPermissions()){
            return true;
            // check that the event is associated with the user's auth munis
        } else if(isMunicodeInMuniList(ev.getMuniCode(), muniList)){
            // sys admins for a muni can confirm everything
            if(u.getKeyCard().isHasSysAdminPermissions()){
                return true;
                // only code officers can enact timeline events
            } else if(evProp.isDirectPropToDefaultMuniCEO() && u.getKeyCard().isHasEnfOfficialPermissions()){
                return true;
            } else if(evProp.isDirectPropToDefaultMuniStaffer() && u.getKeyCard().isHasMuniStaffPermissions()){
                return true;
            } 
        }
        return canEvaluateProposal;
    }
    
    public void deleteEvent(EventCECase ev, User u) throws AuthorizationException{
        EventIntegrator ei = getEventIntegrator();
        try {
            if(u.getKeyCard().isHasSysAdminPermissions()){
                ei.deleteEvent(ev);
            } else {
                throw new AuthorizationException("Must have sys admin permissions "
                        + "to delete event; marking an event as inactive is like deleting it");
                
            }
        } catch (IntegrationException ex) {
            Logger.getLogger(EventCoordinator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Core coordinator method called by all other classes who want to 
     * create their own event. Restricts the event type based on current
     * case phase (closed cases cannot have action, origination, or compliance events.
     * Includes the instantiation of Event objects
     * 
     * @param c the case to which the event should be attached
     * @param ec the type of event to attach to the case
     * @return an initialized event with basic properties set
     * @throws CaseLifecyleException thrown if the case is in an improper state for proposed event
     */
    public EventCECase getInitializedEvent(CECase c, EventCategory ec) throws CaseLifecyleException{
        
        // check to make sure the case isn't closed before allowing event into the switched blocks
        if(c.getCasePhase() == CasePhase.Closed && 
                (
                    ec.getEventType() == EventType.Action
                    || 
                    ec.getEventType() == EventType.Origination
                    ||
                    ec.getEventType() == EventType.Compliance
                )
        ){
            throw new CaseLifecyleException("This event cannot be attached to a closed case");
        }
        
        // the moment of event instantiaion!!!!
        EventCECase event = new EventCECase();
        event.setCategory(ec);
        event.setDateOfRecord(LocalDateTime.now());
        event.setActive(true);
        event.setHidden(false);
        event.setCaseID(c.getCaseID());
        return event;
    }
    
    /**
     * Skeleton event factory
     * For use by the public messaging system which attaches events to code enforcement
     * cases without having access to the entire CECase object--only the caseid
     * @param caseID to which the event should be attached
     * @return an instantiated EventCECase object ready to be configured
     */
    public EventCECase getInitializedEvent(int caseID){
        EventCECase event = new EventCECase();
        event.setCaseID(caseID);
        return event;
        
    }
    
    
    /**
     * Factory method for creating bare event categories.
     * This is used when creating search parameter objects where we want event
     * types without a specific category, but we need a EventCategory shell
     * in which to insert the EventType
     * 
     * @return an EventCategory container with basic properties set
     */
    public EventCategory getInitializedEventCateogry(){
        EventCategory ec =  new EventCategory();
        ec.setUserdeployable(true);
        ec.setHidable(true);
        // TODO: finishing autoconfiguring these 
        return ec;
    }
    
    /**
     * Factory method for creating event categories when only the categoryID
     * is available. This is handy since the insertEvent method on the integrator
     * really only needs the categoryID for storing in the database
     * @param catID the categoryID of the EventCategory you want
     * @return an instantiated EventCategory object
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public EventCategory getInitiatlizedEventCategory(int catID) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        EventCategory ec =  ei.getEventCategory(catID);
        return ec;
    }
    
    /**
     * Takes in a well-formed event message from the CaseCoordinator and
     * initializes the appropriate properties on the event before insertion
     * @param caseID id of the case to which the event should be attached
     * @param message the text of the event description message
     * @throws IntegrationException in the case of broken integration process
     */
    public void attachPublicMessagToCECase(int caseID, String message) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        UserCoordinator uc = getUserCoordinator();
        
        int publicMessageEventCategory = Integer.parseInt(getResourceBundle(Constants.EVENT_CATEGORY_BUNDLE).getString("publicCECaseMessage"));
        EventCategory ec = getInitiatlizedEventCategory(publicMessageEventCategory);
        
        // setup all the event properties
        EventCECase event = getInitializedEvent(caseID);
        event.setCategory(ec);
        event.setDateOfRecord(LocalDateTime.now());
        event.setDescription(message);
        event.setOwner(uc.getRobotUser());
        event.setDiscloseToMunicipality(true);
        event.setDiscloseToPublic(true);
        event.setActive(true);
        event.setHidden(false);
        event.setNotes("Event created by a public user");
        
        // sent the built event to the integrator!
        ei.insertEvent(event);
    }
    
    public void editEvent(EventCECase evcase, User u) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        System.out.println("EventCoordinator.editEvent");
        ei.updateEvent(evcase);
    }
    
    

    /**
     * Creates a populated event to log the change of a code violation update. 
     * The event is coming to us from the violationEditBB with the description and disclosures flags
     * correct. This method needs to set the description from the resource bundle, and 
     * set the date of record to the current date
     * @param ceCase the CECase whose violation was updated
     * @param cv the code violation being updated
     * @param event An initialized event
     * @throws IntegrationException bubbled up from the integrator
     * @throws EventException 
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException 
     * @throws com.tcvcog.tcvce.domain.ViolationException 
     */
    public void generateAndInsertCodeViolationUpdateEvent(CECase ceCase, CodeViolation cv, EventCECase event) 
            throws IntegrationException, EventException, CaseLifecyleException, ViolationException{
        EventIntegrator ei = getEventIntegrator();
        CaseCoordinator cc = getCaseCoordinator();
        String updateViolationDescr = getResourceBundle(Constants.MESSAGE_TEXT).getString("violationChangeEventDescription");
       
       
        // hard coded for now
//        event.setCategory(ei.getEventCategory(117));
        event.setCaseID(ceCase.getCaseID());
        event.setDateOfRecord(LocalDateTime.now());
        event.setDescription(updateViolationDescr);
        //even descr set by violation coordinator
        event.setOwner(getFacesUser());
        // disclose to muni from violation coord
        // disclose to public from violation coord
        event.setActive(true);
        
        cc.attachNewEventToCECase(ceCase, event, null);
    }
    
    
    /**
     * Configures an event which represents the moment of compliance with
     * a code violation attached to a code enforcement case
     * 
     * @param violation
     * @return a partially-baked event ready for inserting
     * @throws IntegrationException 
     */
    public EventCECase generateViolationComplianceEvent(CodeViolation violation) throws IntegrationException{
        EventCECase e = new EventCECase();
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
        return e;
    }
    
    
    
    /**
     * At its current impelementation, this amounts to a factory for ArrayLists
     * that are populated by the user when creating events
     * @return 
     */
    public ArrayList<Person> getEmptyEventPersonList(){
        return new ArrayList<>();
    }
    
    
   
    
    
    
    
    
    
    public List getEventList(CECase currentCase) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        List<EventCECase> ll = ei.getEventsByCaseID(currentCase.getCaseID());
        return ll;
    }
    
    /**
     * The currentCase argument must be a CECase with the desired case phase set
     * The past phase is passed in separately, allowing for phase changes to
     * any phase from any other phase
     * @param currentCase
     * @param pastPhase
     * @param rule
     * @throws IntegrationException
     * @throws CaseLifecyleException 
     * @throws com.tcvcog.tcvce.domain.ViolationException 
     */
    public void generateAndInsertPhaseChangeEvent(CECase currentCase, CasePhase pastPhase, CaseChangeRule rule) 
            throws IntegrationException, CaseLifecyleException, ViolationException{
        
        EventIntegrator ei = getEventIntegrator();
        CaseCoordinator cc = getCaseCoordinator();
        
        
        EventCECase event = getInitializedEvent(currentCase, ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("casePhaseChangeEventCatID"))));
        
        StringBuilder sb = new StringBuilder();
        sb.append("Case phase changed from  \'");
        sb.append(pastPhase.toString());
        sb.append("\' to \'");
        sb.append(currentCase.getCasePhase().toString());
        sb.append("\'");
        if(rule != null){
            sb.append("following the passing of CasePhaseChangeRule:  ");
            sb.append(rule.getTitle());
            sb.append(", no. ");
            sb.append(rule.getRuleID());
        }
        event.setDescription(sb.toString());
        
        event.setCaseID(currentCase.getCaseID());
        event.setDateOfRecord(LocalDateTime.now());
        // not sure if I can access the session level info for the specific user here in the
        // coordinator bean
        event.setOwner(getFacesUser());
        event.setActive(true);
        
        cc.attachNewEventToCECase(currentCase, event, null);
        
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "The case phase has been changed", ""));

    } // close method
    
    public void logResponseToActionRequest(EventCECase ev) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        ei.logResponseToProposal(ev);
    }
    
    
    public void clearActionResponse(EventCECase ev) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        ei.clearResponseToActionRequest(ev);
    }
    
    
    public void generateAndInsertManualCasePhaseOverrideEvent(CECase currentCase, CasePhase pastPhase) 
            throws IntegrationException, CaseLifecyleException, ViolationException{
        
          EventIntegrator ei = getEventIntegrator();
          CaseCoordinator cc = getCaseCoordinator();
        
        EventCECase event = getInitializedEvent(currentCase, ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("casePhaseManualOverride"))));
        
        StringBuilder sb = new StringBuilder();
        sb.append("Manual case phase change from  \'");
        sb.append(pastPhase.toString());
        sb.append("\' to \'");
        sb.append(currentCase.getCasePhase().toString());
        sb.append("\' by a a case officer.");
        event.setDescription(sb.toString());
        
        event.setCaseID(currentCase.getCaseID());
        event.setDateOfRecord(LocalDateTime.now());
        // not sure if I can access the session level info for the specific user here in the
        // coordinator bean
        event.setOwner(getFacesUser());
        event.setActive(true);
        
        cc.attachNewEventToCECase(currentCase, event, null);
        
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "The case phase has been changed", ""));
    }
    
    
    
    /**
     * An unused (but very schnazzy) method for generating the appropriate event that will advance a case
     * to the next phase of its life cycle. Currently called by the method 
     * getEventForTriggeringCasePhaseAdvancement in CaseManageBB
     * @deprecated 
     * @param c
     * @return
     * @throws IntegrationException
     * @throws CaseLifecyleException 
     */
    public EventCECase getActionEventForCaseAdvancement(CECase c) throws IntegrationException, CaseLifecyleException{
        CasePhase cp = c.getCasePhase();
        EventIntegrator ei = getEventIntegrator();
        EventCECase e = new EventCECase();
        
        switch(cp){
            case PrelimInvestigationPending:

                e.setCategory(ei.getEventCategory(Integer.parseInt(getResourceBundle(
                   Constants.EVENT_CATEGORY_BUNDLE).getString("advToNoticeDelivery"))));
               return e;
             
            case NoticeDelivery:
                
                e.setCategory(ei.getEventCategory(Integer.parseInt(getResourceBundle(
                    Constants.EVENT_CATEGORY_BUNDLE).getString("advToInitialComplianceTimeframe"))));
                return e;
            
            case InitialComplianceTimeframe:
            
               e.setCategory(ei.getEventCategory(Integer.parseInt(getResourceBundle(
               Constants.EVENT_CATEGORY_BUNDLE).getString("advToSecondaryComplianceTimeframe"))));
               return e;
             
            case SecondaryComplianceTimeframe:
             
                e.setCategory(ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("advToAwaitingHearingDate"))));
                return e;
         
            case AwaitingHearingDate:
             
                e.setCategory(ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("advToHearingPreparation"))));
                return e;

            case HearingPreparation:
             
                e.setCategory(ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("advToInitialPostHearingComplianceTimeframe"))));
                return e;

            case InitialPostHearingComplianceTimeframe:
             
                e.setCategory(ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("advToSecondaryPostHearingComplianceTimeframe"))));
                return e;
            
            case SecondaryPostHearingComplianceTimeframe:
             
                e.setCategory(ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("advToAwaitingHearingDate"))));
                return e;
         
            default: 
                // this is a holding default event to allow for debugging without other issues
                e.setCategory(ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("casePhaseManualOverride"))));
                return e;
                
                //throw new CaseLifecyleException("Cannot determine next action in case protocol");
            
         } // close switch
    } // close method
    
   
    
    
} // close class
