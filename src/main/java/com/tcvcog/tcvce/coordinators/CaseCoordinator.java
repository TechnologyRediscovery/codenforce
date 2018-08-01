/*
 * Copyright (C) 2017 Turtle Creek Valley
Council of Governments, PA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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
import com.tcvcog.tcvce.domain.CaseLifecyleException;
import com.tcvcog.tcvce.domain.EventException;
import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.domain.ViolationException;
import com.tcvcog.tcvce.entities.CECase;
import com.tcvcog.tcvce.entities.CasePhase;
import com.tcvcog.tcvce.entities.Citation;
import com.tcvcog.tcvce.entities.CodeViolation;
import com.tcvcog.tcvce.entities.EventCase;
import com.tcvcog.tcvce.entities.EventCategory;
import com.tcvcog.tcvce.entities.EventType;
import com.tcvcog.tcvce.entities.NoticeOfViolation;
import com.tcvcog.tcvce.entities.Person;
import com.tcvcog.tcvce.integration.CaseIntegrator;
import com.tcvcog.tcvce.integration.CitationIntegrator;
import com.tcvcog.tcvce.integration.CodeViolationIntegrator;
import com.tcvcog.tcvce.integration.EventIntegrator;
import com.tcvcog.tcvce.util.Constants;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class CaseCoordinator extends BackingBeanUtils implements Serializable{

    /**
     * Creates a new instance of CaseCoordinator
     */
    public CaseCoordinator() {
    
    }
    
    public void createNewCECase(CECase newCase) throws IntegrationException{
        
        CaseIntegrator ci = getCaseIntegrator();
        
        // set default status to prelim investigation pending
        newCase.setCasePhase(CasePhase.PrelimInvestigationPending);
        
        CECase newlyAddedCase = ci.insertNewCECase(newCase);
        
        getSessionBean().setActiveCase(newlyAddedCase);
        
    }
    
    /**
     * Called by the CaseManageBB when the user requests to change the case phase manually.
     * Note that this method is responsible for storing the case's phase before the change, 
     * updating the case Object itself, and then passing the updated CECase object
     * and the phase phase to the EventCoordinator which will take care of logging the event
     * 
     * @param c the case before its phase has been changed
     * @param newPhase the CasePhase to which we to change the case
     * @throws IntegrationException
     * @throws CaseLifecyleException 
     */
    public void manuallyChangeCasePhase(CECase c, CasePhase newPhase) throws IntegrationException, CaseLifecyleException{
        EventCoordinator ec = getEventCoordinator();
        CaseIntegrator ci = getCaseIntegrator();
        CasePhase pastPhase = c.getCasePhase();
        // this call to changeCasePhase requires that the case we pass in already has
        // its phase changed
        c.setCasePhase(newPhase);
        ci.changeCECasePhase(c);
        
        ec.generateAndInsertManualCasePhaseOverrideEvent(c, pastPhase);
        refreshCase(c);
    }
    
    
    /**
     * Primary event life cycle control method which is called
     * each time an event is added to the case. The primary business
     * logic related to which events can be attached to a case at any
     * given case phase is implemented in this coordinator.
     * 
     * Its core operation is to check case and event related qualities
     * and delegate further processing to event-type specific methods
     * also found in this coordinator
     * 
     * @param c the case to which the event should be added
     * @param e the event to add to the case also included in this call
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException
     * @throws com.tcvcog.tcvce.domain.IntegrationException
     * @throws com.tcvcog.tcvce.domain.ViolationException
     */
    public void processCEEvent(CECase c, EventCase e) 
            throws CaseLifecyleException, IntegrationException, ViolationException{
        EventType eventType = e.getCategory().getEventType();
        
        switch(eventType){
            case Action:
                processActionEvent(c, e);
                break;
            case Compliance:
                processComplianceEvent(c, e);
                break;
            case Closing:
                processClosingEvent(c, e);
                break;
            default:
                processGeneralEvent(c, e);
        } // close switch
    } // close method
   
    /**
     * Core business logic method for recording compliance for CodeViolations
     * Checks for timeline fidelity before updating each violation.
     * If all is well, a call to updateViolation on the ViolationCoordinato is called.
     * After the violations have been marked with compliance, a review of the entire case
     * is conducted and if all violations on the case have a compliance date, 
     * the case phase is automatically changed to closed due to compliance
     * 
     * @param c the current case
     * @param e the Compliance event
     * @throws ViolationException in the case of a malformed violation
     * @throws IntegrationException in the case of a DB error
     * @throws CaseLifecyleException in the case of date mismatch
     */
    private void processComplianceEvent(CECase c, EventCase e) 
            throws ViolationException, IntegrationException, CaseLifecyleException{
        
        
        ViolationCoordinator vc = getViolationCoordinator();
        EventCoordinator ec = getEventCoordinator();
        
        ArrayList<CodeViolation> activeViolationList = getSessionBean().getActiveViolationList();
        ListIterator<CodeViolation> li = activeViolationList.listIterator();
        CodeViolation cv;
        
        while(li.hasNext()){
            cv = li.next();
            cv.setActualComplianceDate(e.getDateOfRecord());
            vc.updateCodeViolation(cv);
        } // close while
        
        // first insert our nice compliance event for all selected violations
        ec.insertEvent(e);
        // then look at the whole case and close if necessary
        checkForFullComplianceAndCloseCaseIfTriggered(c);
        
        refreshCase(c);
    } // close method
    
    /**
     * Iterates over all of a case's violations and checks for compliance. 
     * If all of the violations have a compliance date, the case is automatically
     * closed and a case closing event is generated and added to the case
     * 
     * @param c the case whose violations should be checked for compliance
     * @throws IntegrationException
     * @throws CaseLifecyleException
     * @throws ViolationException 
     */
    private void checkForFullComplianceAndCloseCaseIfTriggered(CECase c) 
            throws IntegrationException, CaseLifecyleException, ViolationException{
        
        EventCoordinator ec = getEventCoordinator();
        EventIntegrator ei = getEventIntegrator();
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        
        
        ArrayList caseViolationList = cvi.getCodeViolations(c);
        boolean complianceWithAllViolations = false;
        ListIterator<CodeViolation> fullViolationLi = caseViolationList.listIterator();
        CodeViolation cv;
        
        while(fullViolationLi.hasNext())
        {
            cv = fullViolationLi.next();
                   
            // if there are any outstanding code violations, toggle switch to 
            // false and exit the loop. Phase change will not occur
            if(cv.getActualComplianceDate() != null){
                complianceWithAllViolations = true;
                
                System.out.println("CaseCoordinator.processComplianceEvent | "
                    + "Found violation with a compliance date and toggled to true: " + cv.getActualComplianceDate());
                
            } else {
                complianceWithAllViolations = false;
                
                System.out.println("CaseCoordinator.processComplianceEvent | Found uncomplied violations, toggling to false and breaking out of while");
                
                break;
            }
            System.out.println("CaseCoordinator.processComplianceEvent | "
                    + "inside while loop for compliance check with all violations: " + complianceWithAllViolations);
        } // close while
        
        EventCase complianceClosingEvent;
        
        if (complianceWithAllViolations){
            System.out.println("CaseCoordinator.processComplianceEvent | "
                    + "Inside clase closing if");
                   
            complianceClosingEvent = ec.getInitializedEvent(c, ei.getEventCategory(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("closingAfterFullCompliance"))));
            processCEEvent(c, complianceClosingEvent);
            
        } // close if
        
    }
    
     
    private void processClosingEvent(CECase c, EventCase e) throws IntegrationException, CaseLifecyleException{
        CaseIntegrator ci = getCaseIntegrator();
        EventIntegrator ei = getEventIntegrator();
        
        CasePhase closedPhase = CasePhase.Closed;
        c.setCasePhase(closedPhase);
        ci.changeCECasePhase(c);
        
        c.setClosingDate(LocalDateTime.now());
        updateCase(c);
        // now load up the closing event before inserting it
        // we'll probably want to get this text from a resource file instead of
        // hardcoding it down here in the Java
        e.setDateOfRecord(LocalDateTime.now());
        e.setEventOwnerUser(getFacesUser());
        e.setEventDescription(getResourceBundle(Constants.MESSAGE_BUNDLE).getString("automaticClosingEventDescription"));
        e.setNotes(getResourceBundle(Constants.MESSAGE_BUNDLE).getString("automaticClosingEventNotes"));
        e.setCaseID(c.getCaseID());
        ei.insertEvent(e);
        refreshCase(c);
    }
    
    
    
    /**
     * Main controller method for event-related life cycle events. Requires event to be
     * loaded up with a caseID and an eventType. No eventID is required since it
     * has not yet been logged into the db.
     * @param c code enforcement case
     * @param e event to process
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException 
     * @throws com.tcvcog.tcvce.domain.IntegrationException 
     */
    private void processActionEvent(CECase c, EventCase e) throws CaseLifecyleException, IntegrationException{
        
        EventCoordinator ec = getEventCoordinator();
        // insert the triggering action event
        ec.insertEvent(e); 
        //then pass event to check for phase changes
        checkForAndCarryOutCasePhaseChange(c, e);
        refreshCase(c);
    }
    
    private void checkForAndCarryOutCasePhaseChange(CECase c, EventCase e) throws CaseLifecyleException, IntegrationException{
        
        CaseIntegrator ci = getCaseIntegrator();
        CasePhase initialCasePhase = c.getCasePhase();
        EventCoordinator ec = getEventCoordinator();
        // this value is used to compare to the category IDs listed in the resource bundle
        int evCatID = e.getCategory().getCategoryID();
        
        // check to see if the event triggers a case phase chage. 
        
        if(
            (initialCasePhase == CasePhase.PrelimInvestigationPending 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToNoticeDelivery")))

            ||

            (initialCasePhase == CasePhase.NoticeDelivery 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToInitialComplianceTimeframe")))

            ||

            (initialCasePhase == CasePhase.InitialComplianceTimeframe 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToSecondaryComplianceTimeframe")))

            ||

            (initialCasePhase == CasePhase.SecondaryComplianceTimeframe 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToAwaitingHearingDate")))

            ||

            (initialCasePhase == CasePhase.AwaitingHearingDate 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToHearingPreparation")))

            ||

            (initialCasePhase == CasePhase.HearingPreparation 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToInitialPostHearingComplianceTimeframe")))

            || 

            (initialCasePhase == CasePhase.InitialPostHearingComplianceTimeframe 
            && evCatID == Integer.parseInt(getResourceBundle(
            Constants.EVENT_CATEGORY_BUNDLE).getString("advToSecondaryPostHearingComplianceTimeframe")))
        ){
            // write the phase change to the DB
            // we must ship the case to the integrator with the case phase updated
            // because the integrator does not implement any business logic
            c.setCasePhase(getNextCasePhase(c));
            ci.changeCECasePhase(c);
            
            // generate event for phase change and write
            ec.generateAndInsertPhaseChangeEvent(c, initialCasePhase); 

        } 
        
        refreshCase(c);
        
    }
    
    
    /**
     * A catch-the-rest method that simple adds the event to the case without
     * any additional logic or processing. Called by the default case in the
     * event delegator method
     * @param c the case to which the event should be attached
     * @param e the event to be attached
     * @throws IntegrationException thrown if the integrator cannot get the data
     * into the DB
     */
    private void processGeneralEvent(CECase c, EventCase e) throws IntegrationException{
        EventCoordinator ec = getEventCoordinator();
        ec.insertEvent(e);
        refreshCase(c);
        
    }
    
    /**
     * Utility method for determining which CasePhase follows any given case's CasePhase. 
     * @param c the case whose set CasePhase will be read to determine the next CasePhase
     * @return the ONLY CasePhase to which any case can be changed to without a manual protocol
     * override request
     * @throws CaseLifecyleException thrown when no next CasePhase exists or the next
     * CasePhase cannot be determined
     */
    public CasePhase getNextCasePhase(CECase c) throws CaseLifecyleException{
        CasePhase currentPhase = c.getCasePhase();
        CasePhase nextPhaseInSequence = null;
        
        switch(currentPhase){
            
            case PrelimInvestigationPending:
                nextPhaseInSequence = CasePhase.NoticeDelivery;
                break;
                // conduct inital investigation
                // compose and deply notice of violation
            case NoticeDelivery:
                nextPhaseInSequence = CasePhase.InitialComplianceTimeframe;
                break;
                // Letter marked with a send date
            case InitialComplianceTimeframe:
                nextPhaseInSequence = CasePhase.SecondaryComplianceTimeframe;
                break;
                // compliance inspection
            case SecondaryComplianceTimeframe:
                nextPhaseInSequence = CasePhase.AwaitingHearingDate;
                break;
                // Filing of citation
            case AwaitingHearingDate:
                nextPhaseInSequence = CasePhase.HearingPreparation;
                break;
                // hearing date scheduled
            case HearingPreparation:
                nextPhaseInSequence = CasePhase.InitialPostHearingComplianceTimeframe;
                break;
                // hearing not resulting in a case closing
            case InitialPostHearingComplianceTimeframe:
                nextPhaseInSequence = CasePhase.SecondaryPostHearingComplianceTimeframe;
                break;
            
            case SecondaryPostHearingComplianceTimeframe:
                nextPhaseInSequence = CasePhase.HearingPreparation;
                break;
                
            case Closed:
                // TODO deal with this later
//                throw new CaseLifecyleException("Cannot advance a closed case to any other phase");
                break;
            case InactiveHolding:
                throw new CaseLifecyleException("Cases in inactive holding must have "
                        + "their case phase overriden manually to return to the case management flow");
                
            default:
                throw new CaseLifecyleException("Unable to determine next case phase, sorry");
        }
        
        return nextPhaseInSequence;
    }
    
    
    public ArrayList retrieveViolationList(CECase ceCase) throws IntegrationException{
        ArrayList<CodeViolation> al;
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        al = cvi.getCodeViolations(ceCase);
        return al;
    }
    
    
    public void queueNoticeOfViolation(CECase c, NoticeOfViolation nov) 
            throws CaseLifecyleException, IntegrationException, EventException{
        
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        
        EventCoordinator evCoord = getEventCoordinator();
        
        // togglign this switch puts the notice in the queue for sending
        // flag violation letter as ready to send
        // this will also need to trigger a letter mailing process that hasn't been implemented as
        // of 2 March 2018
        if(nov.isRequestToSend() == false){
            nov.setRequestToSend(true);
        } else {
            throw new CaseLifecyleException("Notice is already queued for sending");
        }
        
        // new letters won't have a LocalDateTime object
        // so insert instead of update in this case
        if(nov.getInsertionTimeStamp() == null){
            cvi.insertViolationLetter(c, nov);
            
        } else {
            cvi.updateViolationLetter(nov);
            
        }
        
        EventCase noticeEvent = new EventCase();
        EventCategory ec = new EventCategory();
        ec.setCategoryID(Integer.parseInt(getResourceBundle(
                Constants.EVENT_CATEGORY_BUNDLE).getString("noticeQueued")));
        noticeEvent.setCategory(ec);
        noticeEvent.setCaseID(c.getCaseID());
        noticeEvent.setDateOfRecord(LocalDateTime.now());
        
        String queuedNoticeEventNotes = getResourceBundle(Constants.MESSAGE_BUNDLE).getString("noticeQueuedEventDesc");
        noticeEvent.setEventDescription(queuedNoticeEventNotes);
        
        noticeEvent.setEventOwnerUser(getFacesUser());
        noticeEvent.setActiveEvent(true);
        noticeEvent.setDiscloseToMunicipality(true);
        noticeEvent.setDiscloseToPublic(true);
        noticeEvent.setRequiresViewConfirmation(false);
        noticeEvent.setHidden(false);
        
        ArrayList<Person> al = new ArrayList();
        al.add(nov.getRecipient());
        noticeEvent.setEventPersons(al);
        
        evCoord.insertEvent(noticeEvent);
        
        refreshCase(c);
        
    }
    
    public void refreshCase(CECase c) throws IntegrationException{
        System.out.println("CaseCoordinator.refreshCase");
        CaseIntegrator ci = getCaseIntegrator();
        
        getSessionBean().setActiveCase(ci.getCECase(c.getCaseID()));
        
    }
    
    public void markNoticeOfViolationAsSent(CECase ceCase, NoticeOfViolation nov) throws CaseLifecyleException, EventException, IntegrationException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        nov.setLetterSentDate(LocalDateTime.now());
        nov.setLetterSentDatePretty(getPrettyDate(LocalDateTime.now()));
        cvi.updateViolationLetter(nov);   
        //advanceToNextCasePhase(ceCase);
            
    }
    
    public void processReturnedNotice(CECase c, NoticeOfViolation nov) throws IntegrationException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();
        
        nov.setLetterReturnedDate(LocalDateTime.now());
        
        cvi.updateViolationLetter(nov);
        refreshCase(c);
    } 
    
    public void deleteNoticeOfViolation(NoticeOfViolation nov) throws CaseLifecyleException{
        CodeViolationIntegrator cvi = getCodeViolationIntegrator();

        //cannot delete a letter that was already sent
        if(nov != null && nov.getLetterSentDate() != null){
            throw new CaseLifecyleException("Cannot delete a letter that has been sent");
        } else {
            try {
                cvi.deleteViolationLetter(nov);
            } catch (IntegrationException ex) {
                System.out.println(ex);
                 getFacesContext().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                    "Unable to delete notice of violation due to a database error", ""));
            }
        }
    }
    
   
   public Citation generateNewCitation(ArrayList<CodeViolation> violationList){
       Citation newCitation = new Citation();
       ArrayList<CodeViolation> al = new ArrayList<>();
       ListIterator<CodeViolation> li = violationList.listIterator();
       CodeViolation cv;
       
       while(li.hasNext()){
           
           cv = li.next();
           System.out.println("CaseCoordinator.generateNewCitation | linked list item: " 
                   + cv.getDescription());
           al.add(cv);
           
       }
       newCitation.setViolationList(al);
       return newCitation;
   }
   
   public void updateCase(CECase c) throws CaseLifecyleException, IntegrationException{
       CaseIntegrator ci = getCaseIntegrator();
       if(c.getClosingDate() != null){
            if(c.getClosingDate().isBefore(c.getOriginationDate())){
                throw new CaseLifecyleException("You cannot update a case's origination date to be after its closing date");
            }
       }
       ci.updateCECase(c);
   }
   
   public void deleteCitation(Citation c) throws IntegrationException{
       CitationIntegrator citint = getCitationIntegrator();
       citint.deleteCitation(c);
              
   }
   
   public void issueCitation(Citation c) throws IntegrationException{
       CitationIntegrator citint = getCitationIntegrator();
       citint.insertCitation(c);
       
   }
   
   public void updateCitation(Citation c) throws IntegrationException{
       CitationIntegrator citint = getCitationIntegrator();
       citint.updateCitation(c);
       
   }
}
