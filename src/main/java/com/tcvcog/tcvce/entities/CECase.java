/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.integration.EventIntegrator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.application.interfaces.IFace_ProposalDriven;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsEventRulesEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsProposalsEnum;

/**
 *
 * @author Ellen Baskem
 */
public class CECase 
        extends CECaseBase 
        implements Cloneable,
                   IFace_ProposalDriven, 
                   IFace_EventRuleGoverned{
    
    private List<CodeViolation> violationList;
    
    private boolean showHiddenEvents;
    private boolean showInactiveEvents;
    private List<CECaseEvent> completeEventList;
    
    // accessed through methods specified in the interfaces
    private List<Proposal> proposalList;
    private List<Event> eventList;
    private List<EventRuleImplementation> eventRuleList;
    
    private List<Citation> citationList;
    private List<NoticeOfViolation> noticeList;
    private List<CEActionRequest> ceActionRequestList;
    
    public CECase(){
    }

    /**
     * Constructor used to create an instance of this object with a
     * CECase without any lists. Transfers the member variables
     * from the incoming object to this sublcass
     * 
     * ** CONSTRUCTORS ARE NOT INHERITED!
     * ** but member variables and methods sure are!
     * 
     * @param cse 
     */
    public CECase(CECaseBase cse){
        this.caseID = cse.caseID;
        this.publicControlCode = cse.publicControlCode;
        this.paccEnabled = cse.paccEnabled;
        this.allowForwardLinkedPublicAccess = cse.allowForwardLinkedPublicAccess;
        this.property = cse.property;
        this.propertyUnit = cse.propertyUnit;
        this.caseManager = cse.caseManager;
        this.caseName = cse.caseName;
        this.casePhase = cse.casePhase;
        this.casePhaseIcon = cse.casePhaseIcon;
        this.originationDate = cse.originationDate;
        this.closingDate = cse.closingDate;
        this.creationTimestamp = cse.creationTimestamp;
        this.notes = cse.notes;
    }
    
    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */
    @Override
    public CECase clone() throws CloneNotSupportedException{
        super.clone();
        return null;
    }
    
     @Override
    public List<Event> assembleEventList(ViewOptionsActiveHiddenListsEnum voahle) {
         List<Event> visEventList = new ArrayList<>();
        if(eventList != null){
            for (Event ev : eventList) {
                switch(voahle){
                    case VIEW_ACTIVE_HIDDEN:
                        if (ev.isActive()
                                && ev.isHidden()) {
                            visEventList.add(ev);
                        }
                        break;
                    case VIEW_ACTIVE_NOTHIDDEN:
                        if (ev.isActive()
                                && !ev.isHidden()) {
                            visEventList.add(ev);
                        }
                        break;
                    case VIEW_ALL:
                        visEventList.add(ev);
                        break;
                    case VIEW_INACTIVE:
                        if (!ev.isActive()) {
                            visEventList.add(ev);
                        }
                        break;
                    default:
                        visEventList.add(ev);
                } // close switch
            } // close for   
        } // close null check
        return visEventList;
    }

    @Override
    public List<EventRuleImplementation> assembleEventRuleList(ViewOptionsEventRulesEnum voere) {
         List<EventRuleImplementation> evRuleList = new ArrayList<>();
        if(eventRuleList != null){
            for(EventRuleImplementation eri: eventRuleList){
                switch(voere){
                    case VIEW_ACTIVE_NOT_PASSED:
                        if(eri.isActiveRuleImp()
                                && eri.getPassedRuleTS() == null){
                            evRuleList.add(eri);
                        }
                        break;
                    case VIEW_ACTIVE_PASSED:
                        if(eri.isActiveRuleImp()
                                && eri.getPassedRuleTS() != null){
                            evRuleList.add(eri);
                        }
                        break;
                    case VIEW_ALL:
                        evRuleList.add(eri);
                        break;
                    case VIEW_INACTIVE:
                        if(!eri.isActiveRuleImp()){
                            evRuleList.add(eri);
                        }
                        break;
                    default:
                        evRuleList.add(eri);
                } // close switch
            } // close loop
        } // close null check
        return evRuleList;
    }

    @Override
    public boolean isAllRulesPassed() {
        boolean allPassed = true;
        for(EventRuleImplementation er: eventRuleList){
            if(er.getPassedRuleTS() == null){
                allPassed = false;
                break;
            }
        }
        return allPassed;
    }

    @Override
    public List<Proposal> assembleProposalList(ViewOptionsProposalsEnum vope) {
        List<Proposal> proposalListVisible = new ArrayList<>();
        if(proposalList != null && !proposalList.isEmpty()){
            for(Proposal p: proposalList){
                switch(vope){
                    case VIEW_ALL:
                        proposalListVisible.add(p);
                        break;
                    case VIEW_ACTIVE_HIDDEN:
                        if(p.isActive() 
                                && p.isHidden()){
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_ACTIVE_NOTHIDDEN:
                        if(p.isActive() 
                                && !p.isHidden()
                                && !p.getDirective().isRefuseToBeHidden()){
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_EVALUATED:
                        if(p.getResponseTS() != null){
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_INACTIVE:
                        if(!p.isActive()){
                            proposalListVisible.add(p);
                        }
                        break;
                    case VIEW_NOT_EVALUATED:
                        if(p.getResponseTS() == null){
                            proposalListVisible.add(p);
                        }
                        break;
                    default:
                        proposalListVisible.add(p);
                } // switch
            } // for
        } // if
        return proposalListVisible;
    }
    
    /**
     * @param eventRuleList the eventRuleList to set
     */
    public void setEventRuleList(List<EventRuleImplementation> eventRuleList) {
        this.eventRuleList = eventRuleList;
    }
    
    /**
     * @return the violationList
     */
    public List<CodeViolation> getViolationList() {
        return violationList;
    }

    /**
     * @param violationList the violationList to set
     */
    public void setViolationList(List<CodeViolation> violationList) {
        this.violationList = violationList;
    }

    /**
     * Implements logic to check each event for hidden status and inactive 
     * status and based on the value of the showHiddenEvents and showInactiveEvents
     * flags, add the event from the complete list to the visible list
     * @return the visibleEventList
     */
    public List<CECaseEvent> getVisibleEventList() {
        List<CECaseEvent> visEventList = new ArrayList<>();
        for (CECaseEvent ev : completeEventList) {
            if (!ev.isActive() && !showInactiveEvents) {
                continue;
            }
            if (ev.isHidden() && !showHiddenEvents) {
                continue;
            }
            visEventList.add(ev);
        } // close for   
        return visEventList;
    }


    /**
     * @return the citationList
     */
    public List<Citation> getCitationList() {
        return citationList;
    }

    /**
     * @param citationList the citationList to set
     */
    public void setCitationList(List<Citation> citationList) {
        this.citationList = citationList;
    }

    /**
     * @return the noticeList
     */
    public List<NoticeOfViolation> getNoticeList() {
        return noticeList;
    }

    /**
     * @param noticeList the noticeList to set
     */
    public void setNoticeList(List<NoticeOfViolation> noticeList) {
        this.noticeList = noticeList;
    }

    /**
     * @return the ceActionRequestList
     */
    public List<CEActionRequest> getCeActionRequestList() {
        return ceActionRequestList;
    }

    /**
     * @param ceActionRequestList the ceActionRequestList to set
     */
    public void setCeActionRequestList(List<CEActionRequest> ceActionRequestList) {
        this.ceActionRequestList = ceActionRequestList;
    }

    

    /**
     * @return the violationListUnresolved
     */
    public List<CodeViolation> getViolationListUnresolved() {
        
        List<CodeViolation> violationListUnresolved = new ArrayList<>();
        if(violationList != null && violationList.size() > 0){
            for(CodeViolation v: violationList){
                if(v.getActualComplianceDate() == null){
                    violationListUnresolved.add(v);
                }
            }
        }
        

        return violationListUnresolved;
    }


    /**
     * @return the violationListResolved
     */
    public List<CodeViolation> getViolationListResolved() {
        List<CodeViolation>violationListResolved = new ArrayList<>();
        if(violationList != null && violationList.size() > 0){
            for(CodeViolation v: violationList){
                if(v.getActualComplianceDate() != null){
                    violationListResolved.add(v);
                }
            }
        }
        
        return violationListResolved;
    }


    /**
     * @return the completeEventList
     */
    public List<CECaseEvent> getCompleteEventList() {
        return completeEventList;
    }

    /**
     * @param completeEventList the completeEventList to set
     */
    public void setCompleteEventList(List<CECaseEvent> completeEventList) {
        this.completeEventList = completeEventList;
    }

    /**
     * @return the showInactiveEvents
     */
    public boolean isShowInactiveEvents() {
        return showInactiveEvents;
    }

    /**
     * @param showInactiveEvents the showInactiveEvents to set
     */
    public void setShowInactiveEvents(boolean showInactiveEvents) {
        this.showInactiveEvents = showInactiveEvents;
    }

    /**
     * @return the showHiddenEvents
     */
    public boolean isShowHiddenEvents() {
        return showHiddenEvents;
    }

    /**
     * @param showHiddenEvents the showHiddenEvents to set
     */
    public void setShowHiddenEvents(boolean showHiddenEvents) {
        this.showHiddenEvents = showHiddenEvents;
    }

    /**
     * @return the activeEventList
     */
    public List<CECaseEvent> getActiveEventList() {
        List<CECaseEvent> actEvList = new ArrayList<>();
            Iterator<CECaseEvent> iter = completeEventList.iterator();
                while(iter.hasNext()){
                    CECaseEvent ev = iter.next();
                    if(ev.isActive()){
                        actEvList.add(ev);
                    }
                }
        return actEvList;
    }


    /**
     * @return the proposalList
     */
    public List<Proposal> getProposalList() {
        return proposalList;
    }

    /**
     * @param proposalList the proposalList to set
     */
    public void setProposalList(List<Proposal> proposalList) {
        this.proposalList = proposalList;
    }

    /**
     * @return the eventList
     */
    public List<Event> getEventList() {
        return eventList;
    }

    /**
     * @param eventList the eventList to set
     */
    @Override
    public void setEventList(List<Event> eventList) {
        this.eventList = eventList;
    }

        
  
    
    
    
    
}
