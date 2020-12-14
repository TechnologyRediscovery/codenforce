/*
 * Copyright (C) 2017 Turtle Creek Valley
 * Council of Governments, PA
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
import com.tcvcog.tcvce.domain.BObStatusException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.SearchException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.*;
import com.tcvcog.tcvce.entities.reports.ReportConfigCEEventList;
import com.tcvcog.tcvce.entities.UserAuthorized;
import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPeriodDataHeavy;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.integration.PersonIntegrator;
import java.io.Serializable;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.faces.application.FacesMessage;
import com.tcvcog.tcvce.util.MessageBuilderParams;
import java.util.HashMap;
import java.util.Map;

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
     * Extracts events for bobs that hold them
     * @param evHolder
     * @return
     * @throws IntegrationException 
     */
    public List<EventCnF> getEventList(IFace_EventHolder evHolder) throws IntegrationException{
        
        EventIntegrator ei = getEventIntegrator();
        List<Integer> evidl = ei.getEventList(evHolder);
        List<EventCnF> evList = new ArrayList<>();
        for(Integer i: evidl){
            evList.add(getEvent(i));
        }
        
        return evList;
    }
    
    /**
     * Retrieves event Categories by Type
     * @param et
     * @return
     * @throws IntegrationException 
     */
    public List<EventCategory> getEventCategeryList(EventType et) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        List<EventCategory> evCatList = new ArrayList<>();
        if(et != null){
            evCatList.addAll(ei.getEventCategoryList(et));
        }
        return evCatList;
        
    }
    
    public List<EventCnF> getEventList(List<Integer> evIDList) throws IntegrationException{
        List<EventCnF> evList = new ArrayList<>();
        if(evIDList != null && !evIDList.isEmpty()){
            for(Integer i: evIDList){
                evList.add(getEvent(i));
            }
        }
        return evList;
    }
    
    public List<EventCnFPropUnitCasePeriodHeavy> getEventHistoryList(UserAuthorized ua) throws IntegrationException, EventException, SearchException{
        EventIntegrator ei = getEventIntegrator();
        return assembleEventCnFPropUnitCasePeriodHeavyList(getEventList(ei.getEventHistory(ua.getUserID())));
        
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
    public EventCnFPropUnitCasePeriodHeavy assembleEventCnFPropUnitCasePeriodHeavy(EventCnF ev) 
                           throws EventException, IntegrationException, SearchException{

        OccupancyCoordinator oc = getOccupancyCoordinator();
        CaseCoordinator cc = getCaseCoordinator();
        EventCnFPropUnitCasePeriodHeavy edh;
        if(ev == null) return null;
        if(ev.getEventID() != 0){
             edh = new EventCnFPropUnitCasePeriodHeavy(getEvent(ev.getEventID()));
        } else {
             edh = new EventCnFPropUnitCasePeriodHeavy(ev);
        }
        if(ev.getDomain() == EventDomainEnum.OCCUPANCY && ev.getOccPeriodID() != 0){
            edh.setPeriod(oc.getOccPeriodPropertyUnitHeavy(edh.getOccPeriodID()));
        } else if(ev.getDomain() == EventDomainEnum.CODE_ENFORCEMENT && ev.getCeCaseID() != 0){
            edh.setCecase(cc.cecase_assembleCECasePropertyUnitHeavy(cc.cecase_getCECase(edh.getCeCaseID())));
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
    public List<EventCnFPropUnitCasePeriodHeavy> assembleEventCnFPropUnitCasePeriodHeavyList(List<EventCnF> evList) 
            throws EventException, IntegrationException, SearchException{
        List<EventCnFPropUnitCasePeriodHeavy> edhList = new ArrayList<>();
        if(evList != null && !evList.isEmpty() ){
            for(EventCnF ev: evList){
                edhList.add(assembleEventCnFPropUnitCasePeriodHeavy(ev));
            }
        }
        return edhList;
    }
    
    /**
     * I Create new EventCnF objects!
     * The only public method for creating a new event on an ERG. My guts will
     * call a bunch of methods in related coordinators to trigger appropriate
     * workflow actions.
     * 
     * @param ev with only user-supplied data inserted; I'll fill in unified stuff
     * like eventcreator and such
     * @param erg as of June 2020 V.0.9 implementers are CECase and OccPeriod objects
     * @param ua
     * @return A list of freshly inserted events, all freshly extracted from the DB
     * so what's in this list is what's in the DB. The head of the list is the
     * event that was passed into this method
     * @throws com.tcvcog.tcvce.domain.BObStatusException
     * @throws com.tcvcog.tcvce.domain.EventException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     */
    public List<EventCnF> addEvent(     EventCnF ev, 
                                        IFace_EventRuleGoverned erg, 
                                        UserAuthorized ua) 
                            throws      BObStatusException, 
                                        EventException, 
                                        IntegrationException{
        
        if(ev == null || erg == null || ua == null){
            throw new BObStatusException("Cannot process event with incomplete args");
        }
        
        // *************************
        // Check Connections to the mother BOb
        // *************************
        if(erg instanceof OccPeriodDataHeavy && ev.getOccPeriodID() == 0){
            ev.setOccPeriodID(erg.getBObID());
        } else if (erg instanceof CECaseDataHeavy && ev.getCeCaseID() == 0){
            ev.setCeCaseID(erg.getBObID());
        } 
        
        configureEventTimes(ev, ua);
        
        LocalDateTime now = LocalDateTime.now();
        
        // ****************
        // Event essentials
        // ****************
        ev.setUserCreator(ua);
        ev.setLastUpdatedBy(ua);
        ev.setLastUpdatedTS(now);
        
        ev.setActive(true);
        ev.setHidden(false);
        
        // **********************************
        // Allow domain coordinators to check
        // **********************************
        
        List<EventCnF> evsToAddQu = new ArrayList<>();
        
        // position our primary event at the head of the list
        evsToAddQu.add(ev);
        // then let the other domain folks add to this stack if needed
        
            return addEvent_processStack(evsToAddQu);
        

        
    }
    
    /**
     * Internal method for checking and tweaking if necessary the time stamps
     * on event objects
     * 
     * @param ev
     * @param ua 
     */
    private void configureEventTimes(EventCnF ev, UserAuthorized ua){
        if(ev == null || ua == null){
            return;
        }
        SystemCoordinator sc = getSystemCoordinator();
        
        // *************
        // TIME AUDITING
        // *************
        
        LocalDateTime now = LocalDateTime.now();
        
        // check and adjust start/end times
        if(ev.getTimeStart() == null){
            ev.setTimeStart(now);
        }
        
        // compute default end time in case we need it

        MessageBuilderParams mbp;
        LocalDateTime timeEndComputed = null;
        
        // deal with no end time
        if(ev.getTimeEnd() == null){
            timeEndComputed = ev.getTimeStart().plusMinutes(ev.getCategory().getDefaultdurationmins());
            ev.setTimeEnd(timeEndComputed);
     
            mbp = new MessageBuilderParams();
            mbp.setExistingContent(ev.getNotes());
            mbp.setHeader("Auto-edit of event details");
            mbp.setUser(ua);
            mbp.setCred(ua.getMyCredential());
            mbp.setExplanation("No end time was specified on incoming event so " +
                    "an end time was computed using the event category's default duration");
            ev.setNotes(sc.appendNoteBlock(mbp));
        }
        
        // Deal with non-chronological start and end times
        if(ev.getTimeEnd().isBefore(ev.getTimeStart())){
            ev.setTimeEnd(timeEndComputed);
            mbp = new MessageBuilderParams();
            mbp.setExistingContent(ev.getNotes());
            mbp.setHeader("Auto-edit of event details");
            mbp.setUser(ua);
            mbp.setCred(ua.getMyCredential());
            mbp.setExplanation("The end time of the incoming event cannot occur "
                    + "before the start time; They must be in forward chrono order;"
                    + "A new end time was automatically computed using the event category's"
                    + "default duration");
            ev.setNotes(sc.appendNoteBlock(mbp));
        }
        
    }
    
    
    /**
     * I Iterate over a List of events that works like a Queue in that I'll
     * insert one after the other but stop if any of them don't make it into DB.
     * 
     * @param qu
     * @return
     * @throws IntegrationException 
     */
    private List<EventCnF> addEvent_processStack(List<EventCnF> qu) throws IntegrationException, BObStatusException{
        EventIntegrator ei = getEventIntegrator();
        List<EventCnF> doneList = new ArrayList<>();
        if(qu != null && !qu.isEmpty()){
            for(EventCnF ev: qu){
                int id = ei.insertEvent(ev);
                if(id != 0){
                    doneList.add(getEvent(id));
                } else {
                    break;
                }
            }
        }
        
        return doneList;
        
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
        PersonIntegrator pi = getPersonIntegrator();
        PersonCoordinator pc = getPersonCoordinator();
        
        if(ev == null){
            return null;
        }
        
        // Declare this event as either in the CE or Occ domain with 
        // our hacky little enum thingy
            if(ev.getCeCaseID() !=0 && ev.getOccPeriodID() != 0 ){
                throw new EventException("EventCnF cannot have a non-zero CECase and OccPeriod ID");
            }
            if(ev.getCeCaseID() != 0){
                ev.setDomain(EventDomainEnum.CODE_ENFORCEMENT);
            } else if(ev.getOccPeriodID() != 0){
                ev.setDomain(EventDomainEnum.OCCUPANCY);
            } else {
                throw new EventException("EventCnF must have either an occupancy period ID, or CECase ID");
            }
       
        ev.setPersonList(pc.getPersonList(pi.eventPersonAssembleList(ev)));
        
        return ev;
    }
    
    /**
     * Implements business logic to ensure EventCnF correctness; called by insert
     * and edit event methods; Throws an EventException if there's an error
     * 
     * @param ev
     * @throws com.tcvcog.tcvce.domain.EventException
     */
    private void auditEvent(EventCnF ev) throws EventException{
        if(ev.getTimeStart() == null){
            throw new EventException("Events must have a start time");
        }
        if(ev.getTimeEnd() != null){
            if(ev.getTimeEnd().isBefore(ev.getTimeStart())){
                throw new EventException("Events with end times must not have an end time before start time");
            }
        }
    }
    
    public void auditEventPersonList(EventCnF ev){
        List<Person> pl;
        if(ev != null){
            pl = ev.getPersonList();
            if(pl != null && !pl.isEmpty()){
                
            }
        }
        
    }
    
    
    /**
     * Business rule aware pathway to update fields on EventCnF objects
     * When updating Person links, this method clears all previous connections
     * and rebuilds the mapping from scratch on each update.
     * 
     * @param ev
     * @param ua
     * @throws IntegrationException 
     * @throws com.tcvcog.tcvce.domain.EventException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     */
    public void updateEvent(EventCnF ev, UserAuthorized ua) throws IntegrationException, EventException, BObStatusException{
        PersonIntegrator pi = getPersonIntegrator();
        EventIntegrator ei = getEventIntegrator();
         if(ev == null || ua == null){
            throw new BObStatusException("Event and User cannot be null");
        }
        if(ev.getCategory().getUserRankMinimumToUpdate() > ua.getRole().getRank()){
            throw new BObStatusException("User's rank does not allow deactivation of given event");
        }
        
        auditEvent(ev);
        ev.setLastUpdatedBy(ua);
        ev.setLastUpdatedTS(LocalDateTime.now());
        ei.updateEvent(ev);
        if(ev.getPersonList() != null && !ev.getPersonList().isEmpty()){
            pi.eventPersonClear(ev);
            pi.eventPersonConnect(ev);
        }
        
    }
    
    /**
     * Writes the given mbp to the notes field of a given event
     * @param mbp
     * @param ev
     * @param ua 
     */
    public void updateEventNotes(MessageBuilderParams mbp, EventCnF ev, UserAuthorized ua) throws IntegrationException, BObStatusException{
        SystemCoordinator sc = getSystemCoordinator();
        EventIntegrator ei = getEventIntegrator();
        if(mbp == null || ev == null || ua == null){
            throw new BObStatusException("Cannot update notes with null message, ev, or user");
        }
        ev.setNotes(sc.appendNoteBlock(mbp));
        ei.updateEventNotes(ev);        
        
    }
    
    
    /**
     * Akin to delete
     * @param ev
     * @param ua
     * @throws IntegrationException 
     */
    public void removeEvent(EventCnF ev, UserAuthorized ua) throws IntegrationException, BObStatusException{
        if(ev == null || ua == null){
            throw new BObStatusException("Event and User cannot be null");
        }
        if(ev.getCategory().getUserRankMinimumToUpdate() > ua.getRole().getRank()){
            throw new BObStatusException("User's rank does not allow deactivation of given event");
        }
        SystemCoordinator sc = getSystemCoordinator();
        EventIntegrator ei = getEventIntegrator();
        ev.setActive(false);
        ev.setLastUpdatedBy(ua);
        ev.setLastUpdatedTS(LocalDateTime.now());
        ev.setNotes(sc.formatAndAppendNote(ua, "Event deactivated by User with credential sig " + ua.getMyCredential().getSignature(), ev.getNotes()));
        ei.updateEvent(ev);
        
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
        
        CECaseDataHeavy cse = null;
        OccPeriod op = null;
        
        // the moment of event instantiaion!!!!
        EventCnF e = new EventCnF();
        
        if(erg != null){
            if(erg instanceof CECaseDataHeavy){
                cse = (CECaseDataHeavy) erg;
                if(cse.getStatusBundle() != null){
                    
                    if(cse.getStatusBundle().getPhase() == CasePhaseEnum.Closed && 
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
                } 
                e.setCeCaseID(cse.getCaseID());
                e.setDomain(EventDomainEnum.CODE_ENFORCEMENT);
            } else if (erg instanceof OccPeriod){
                op = (OccPeriod) erg;
                e.setOccPeriodID(op.getPeriodID());
                e.setDomain(EventDomainEnum.OCCUPANCY);
            }
        }
        
        if(ec != null){
            
            e.setCategory(ec);

            e.setTimeStart(LocalDateTime.now());
            e.setTimeEnd(e.getTimeStart().plusMinutes(ec.getDefaultdurationmins()));
        }
        
        e.setActive(true);
        e.setHidden(false);
        
        return e;
    }
    
    
    public String buildEventInfoMessage(List<EventCnF> evDoneList){
        StringBuilder sb = new StringBuilder();
            for(EventCnF evZ: evDoneList){
                sb = new StringBuilder();

                sb.append(evZ.getCategory().getEventType().getLabel());
                sb.append(": ");
                sb.append(evZ.getCategory().getEventCategoryTitle());
                sb.append(" (");
                sb.append(evZ.getCategory().getEventCategoryDesc());
                sb.append(") ");
            }
        return sb.toString();
    }
    
    
//    --------------------------------------------------------------------------
//    ***************************** SEARCH AND VIEW *************************************
//    --------------------------------------------------------------------------
   
    /**
     * Logic container for choosing a sensible event view enum value
     * @param ua not used
     * @return 
     */
   
    public ViewOptionsActiveHiddenListsEnum determineDefaultEventView(UserAuthorized ua){
        return ViewOptionsActiveHiddenListsEnum.VIEW_ACTIVE_NOTHIDDEN;
    }
    
    
      
    /**
     * Generator method for the EventCnF object
     * @return 
     */
    public ReportConfigCEEventList initDefaultReportConfigEventList(){
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
     * Logic intermediary retrieval method for EventCategories
     * @param catID
     * @return
     * @throws IntegrationException 
     */
    public EventCategory getEventCategory(int catID) throws IntegrationException{
        
        EventIntegrator ei = getEventIntegrator();
        return ei.getEventCategory(catID);
    }
    
    /**
     * Assembles a subset of EventType and EventCategory objects for 
     * viewing by a given user; based on the rank of the User passed in
     * @param ua
     * @return 
     */
    public Map<EventType, List<EventCategory>> assembleEventTypeCatMap_toView(UserAuthorized ua){
       Map<EventType, List<EventCategory>> typeCatMap = new HashMap<>();
       List<EventType> typeList = new ArrayList();
       typeList.addAll(Arrays.asList(EventType.values()));
       if(!typeList.isEmpty()){
           for(EventType typ: typeList){
               try {
                   typeCatMap.put(typ, determinePermittedEventCategories_toView(typ, ua));
               } catch (IntegrationException ex) {
                   System.out.println(ex);
               }
           }
       }
       
       return typeCatMap;
    }
    
    
    /**
     * Internal method for selecting only EventCategories that the given UserAuthorized
     * is allowed to view
     * @param et
     * @param ua
     * @return
     * @throws IntegrationException 
     */
    private List<EventCategory> determinePermittedEventCategories_toView(EventType et, UserAuthorized ua) throws IntegrationException{
        EventIntegrator ei = getEventIntegrator();
        List<EventCategory> catList = ei.getEventCategoryList(et);
        List<EventCategory> allowedCats = new ArrayList<>();
        if(catList != null &&!catList.isEmpty()){
            for(EventCategory ec: catList){
                if(ec.getUserRankMinimumToView() >= ua.getMyCredential().getGoverningAuthPeriod().getRole().getRank()){
                    allowedCats.add(ec);
                }
            }
        }
        return allowedCats;
    }
    
    
    
    
    /**
     * Creates a Mapping of EventTypes and permitted EventCategories for enacting 
     * and event
     * @param domain
     * @param erg
     * @param ua
     * @return 
     */
    public Map<EventType, List<EventCategory>> assembleEventTypeCatMap_toEnact(
                                                EventDomainEnum domain,
                                                IFace_EventRuleGoverned erg, 
                                                UserAuthorized ua){
       Map<EventType, List<EventCategory>> typeCatMap = new HashMap<>();
       List<EventType> typeList = determinePermittedEventTypes(domain, erg, ua);
       if(typeList != null && !typeList.isEmpty()){
           for(EventType typ: typeList){
               typeCatMap.put(typ, determinePermittedEventCategories(typ, ua));
           }
       }
       return typeCatMap;
    }
    
    /**
     * Entry point for logic components that select which EventType objects
     * and later, event cats, the user can see on a given load of the events 
     * viewer. This logic will review what our event domain is, the thing
     * onto which we might be attaching events (which as of Jun 2020 are CECase
     * objects or OccPeriod objects), and the attacher
     * @param domain
     * @param erg which will be interrogated for is open/closed status
     * @param ua doing potential creation of an event
     * @return 
     */
    public List<EventType> determinePermittedEventTypes(    EventDomainEnum domain, 
                                                            IFace_EventRuleGoverned erg, 
                                                            UserAuthorized ua){
        List<EventType> typeList = new ArrayList<>();
        if(domain == null || erg == null || ua == null){
            return typeList;
        }
        
        // implement logic based on event domain and check for sensible matches
        switch(domain){
            case CODE_ENFORCEMENT:
                if(erg instanceof CECaseDataHeavy){
                    typeList.addAll(determinePermittedEventTypesForCECase((CECaseDataHeavy) erg, ua));
                }
                break;
            case OCCUPANCY:
                if(erg instanceof OccPeriodDataHeavy){
                    typeList.addAll(determinePermittedEventTypesForOcc((OccPeriodDataHeavy) erg, ua));
                }
                break;
            case UNIVERSAL:
                typeList.add(EventType.Custom);
                typeList.add(EventType.Meeting);
                typeList.add(EventType.Communication);
                break;
            default:
        }
        return typeList;
    }
    
    
     /**
     * Implements business rules for determining which event types are allowed
     * to be attached to the given CECaseDataHeavy based on the case's phase and the
     * user's permissions in the system.
     *
     * @param c the CECaseDataHeavy on which the event would be attached
     * @param u the User doing the attaching
     * @return allowed EventTypes for attaching to the given case
     */
    private List<EventType> determinePermittedEventTypesForCECase(CECaseDataHeavy c, UserAuthorized u) {
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
    
    
    /**
     * Business rule logic container for choosing permitted eventTypes
     * @param period
     * @param u
     * @return 
     */
    private List<EventType> determinePermittedEventTypesForOcc(OccPeriod period, UserAuthorized u) {
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
    
    /**
     * Logic intermediary for extracting all EventTypes in the system
     * @return 
     */
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
  
    
    /**
     * Extracts a complete list of event categories
     * @return 
     */
    public List<EventCategory> getEventCategoryList(){
        EventIntegrator ei = getEventIntegrator();
        try {
            return ei.getEventCategoryList();
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        return null;
    }
    
    /**
     * Utility method for iterating over the event cat list and only
     * passing on active ones
     * @param catList
     * @return
     */
    public List<EventCategory> assembleEventCategoryListActiveOnly(List<EventCategory> catList) {
        EventIntegrator ei = getEventIntegrator();
        List<EventCategory> catListActiveOnly = new ArrayList<>();
        for(EventCategory cat: catList){
            if(cat.isActive()){
                catListActiveOnly.add(cat);
            }
        }
        return catListActiveOnly;
    }
    
    
    /**
     * Logic container for choosing which event categories to allow the user
     * to use for event creation UI. Used to build a mapping of EventTypes and categories
     * 
     * @param et
     * @param u
     * @return 
     */
    public List<EventCategory> determinePermittedEventCategories(EventType et, UserAuthorized u) {
        EventIntegrator ei = getEventIntegrator();
        List<EventCategory> rawCats = new ArrayList<>();
        List<EventCategory> allowedCats = new ArrayList<>();
        if(et == null){
            return allowedCats;
        }
        try {
            rawCats.addAll(ei.getEventCategoryList(et));
        } catch (IntegrationException ex) {
            System.out.println(ex);
        }
        
        for(EventCategory cat: rawCats){
            boolean include = false;
            if(cat.isActive()){
                if(u != null && u.getKeyCard() != null){
                    // ensure that the user's rank is at least what is required 
                    // to enact an event of this Category
                    if(u.getKeyCard().getGoverningAuthPeriod().getRole().getRank() >= cat.getUserRankMinimumToEnact()){
                            include = true;
                    } 
                }       
            }
            if(include){
                allowedCats.add(cat);
            }
        }
        return allowedCats;
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
     * @param ua
     * @throws IntegrationException bubbled up from the integrator
     * @throws EventException 
     * @throws com.tcvcog.tcvce.domain.BObStatusException 
     * @throws com.tcvcog.tcvce.domain.ViolationException 
     */
    public void generateAndInsertCodeViolationUpdateEvent(CECaseDataHeavy ceCase, CodeViolation cv, EventCnF event, UserAuthorized ua) 
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
        event.setUserCreator(getSessionBean().getSessUser());
        // disclose to muni from violation coord
        // disclose to public from violation coord
        event.setActive(true);
        addEvent(event, ceCase, ua);
        
        
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
        event.setUserCreator(uc.user_getUserRobot());
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
        
        StringBuilder sb = new StringBuilder();
        sb.append("Compliance with the following code violations was observed:");
        sb.append("<br /><br />");
        
            sb.append(violation.getViolatedEnfElement().getOrdchapterNo());
            sb.append(".");
            sb.append(violation.getViolatedEnfElement().getOrdSecNum());
            sb.append(".");
            sb.append(violation.getViolatedEnfElement().getOrdSubSecNum());
            sb.append(":");
            sb.append(violation.getViolatedEnfElement().getOrdSubSecTitle());
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
        sb.append(currentCase.getStatusBundle().getPhase().toString());
        sb.append("\' by a a case officer.");
        event.setDescription(sb.toString());
        
        event.setCeCaseID(currentCase.getCaseID());
//        event.set(LocalDateTime.now());
        // not sure if I can access the session level info for the specific user here in the
        // coordinator bean
        event.setUserCreator(getSessionBean().getSessUser());
        event.setActive(true);
        
//        cc.events_addEvent_processForCECaseDomain(currentCase, event, null);
        
        getFacesContext().addMessage(null,
            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                    "The case phase has been changed", ""));
    }
    
    
     

    
    
} // close class