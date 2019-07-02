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

/**
 *
 * @author Sylvia Baskem
 */
public class CECase extends CECaseBaseClass implements Cloneable{
    
    private List<CodeViolation> violationList;
    private List<CodeViolation> violationListResolved;
    private List<CodeViolation> violationListUnresolved;
    
    private List<CECaseEvent> visibleEventList;
    private List<CECaseEvent> activeEventList;
    private boolean showHiddenEvents;
    private boolean showInactiveEvents;
    private List<CECaseEvent> completeEventList;
    
    
    private List<CECaseEvent> eventProposalList;
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
     * @param cnl 
     */
    public CECase(CECaseBaseClass cnl){
        this.caseID = cnl.caseID;
        this.publicControlCode = cnl.publicControlCode;
        this.paccEnabled = cnl.paccEnabled;
        this.allowForwardLinkedPublicAccess = cnl.allowForwardLinkedPublicAccess;
        this.property = cnl.property;
        this.propertyUnit = cnl.propertyUnit;
        this.caseManager = cnl.caseManager;
        this.caseName = cnl.caseName;
        this.casePhase = cnl.casePhase;
        this.casePhaseIcon = cnl.casePhaseIcon;
        this.originationDate = cnl.originationDate;
        this.closingDate = cnl.closingDate;
        this.creationTimestamp = cnl.creationTimestamp;
        this.notes = cnl.notes;
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
        visibleEventList.clear();
        for (CECaseEvent ev : completeEventList) {
            if (!ev.isActive() && !showInactiveEvents) {
                continue;
            }
            if (ev.isHidden() && !showHiddenEvents) {
                continue;
            }
            visibleEventList.add(ev);
        } // close for   
        return visibleEventList;
    }

    /**
     * @param visibleEventList the visibleEventList to set
     */
    public void setVisibleEventList(List<CECaseEvent> visibleEventList) {
        this.visibleEventList = visibleEventList;
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
     * @return the eventProposalList
     */
    public List<CECaseEvent> getEventProposalList() {

        eventProposalList = new ArrayList<>();
        if(completeEventList !=  null && completeEventList.size() >= 1){
            for(CECaseEvent ev: completeEventList){
                if(ev.getEventProposalImplementation()!= null 
                        && 
                    ev.getEventProposalImplementation().getResponseTimestamp() != null
                        &&
                    ev.isActive()
                        &&
                    !ev.isHidden()){
                    // event is a case action request so add it!
                    eventProposalList.add(ev);
                }
            }
        }
        
        return eventProposalList;
    }

    /**
     * @param eventProposalList the eventProposalList to set
     */
    public void setEventProposalList(List<CECaseEvent> eventProposalList) {
        this.eventProposalList = eventProposalList;
    }

    /**
     * @return the violationListUnresolved
     */
    public List<CodeViolation> getViolationListUnresolved() {
        
        violationListUnresolved = new ArrayList<>();
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
     * @param violationListUnresolved the violationListUnresolved to set
     */
    public void setViolationListUnresolved(List<CodeViolation> violationListUnresolved) {
        this.violationListUnresolved = violationListUnresolved;
    }

    /**
     * @return the violationListResolved
     */
    public List<CodeViolation> getViolationListResolved() {
        violationListResolved = new ArrayList<>();
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
     * @param violationListResolved the violationListResolved to set
     */
    public void setViolationListResolved(List<CodeViolation> violationListResolved) {
        this.violationListResolved = violationListResolved;
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
        if(completeEventList != null){
            Iterator<CECaseEvent> iter = completeEventList.iterator();
                while(iter.hasNext()){
                    CECaseEvent ev = iter.next();
                    if(ev.isActive()){
                        activeEventList.add(ev);
                    }
                }
            }
        return activeEventList;
    }

    /**
     * @param activeEventList the activeEventList to set
     */
    public void setActiveEventList(List<CECaseEvent> activeEventList) {
        this.activeEventList = activeEventList;
    }

  
    
    
    
    
}
