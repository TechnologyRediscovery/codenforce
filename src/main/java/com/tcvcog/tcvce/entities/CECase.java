/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.application.IFace_pinnable;
import com.tcvcog.tcvce.application.interfaces.IFace_ActivatableBOB;
import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.DateTimeUtil;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Business object representing a single code enforcement case
 * @author sylvia
 */
public class        CECase 
        extends     EntityUtils
        implements  Cloneable,
                    IFace_Loggable,
                    IFace_EventHolder,
                    IFace_StatusLogHolder,
                    Comparable<CECase>,
                    IFace_ActivatableBOB,
                    IFace_noteHolder,
                    IFace_pinnable,
                    IFace_stateful{
    
    final static String CASE_TABLE_NAME = "cecase";
    final static String CASE_PK_FIELD = "caseid";
    final static String CASE_FRIENDLY_NAME = "Code Enforcement Case";
    final static DomainEnum CECASE_ENUM = DomainEnum.CODE_ENFORCEMENT;
    final static String PIN_TABLE_NAME = "public.cecasepin";
    final static String PIN_FK_FIELD = "cecase_caseid";
    
    
    protected int caseID;
    protected int publicControlCode;
    protected boolean paccEnabled;
    protected boolean pinned;
    protected User pinner;
    
    /**
     * Code enforcement action requests are generally linked
     * to a code enforcement case by the code officers.
     * This switch allows the release of the allowed
     * case info to any holder of a PACC for a CEActionRequest
     * that contains a link to this case.
     */
    protected boolean allowForwardLinkedPublicAccess;
    
    protected int parcelKey;
    protected int propertyUnitID;
    
    protected User caseManager;
    protected String caseName;
    
    protected PriorityEnum priority;
    protected String priorityAssignmentLog;
    private StringBuilder pLog;
    protected Icon priorityIcon;
    
    protected CECaseStatus statusBundle;
    protected String statusAssignmentLog;
    
    protected LocalDateTime originationDate;
    protected LocalDateTime closingDate;
    protected LocalDateTime creationTimestamp;
    
    protected String notes;
    
    protected BOBSource source;
    
    protected List<Citation> citationList;
    protected List<NoticeOfViolation> noticeList;
    protected List<CodeViolation> violationList;
    
    protected boolean active;
    protected boolean propertyInfoCase;
    protected int personInfoPersonID;
    
    protected User lastUpdatedBy;
    protected LocalDateTime lastUpdatedTS;
    
    protected List<EventCnF> eventList;
    protected EventCnF mostRecentPastEvent;
    
    protected EventCnF originationEvent;
    protected EventCnF closingEvent;
    protected long daysSinceLastEvent;
    
    
    public CECase(){
        
    }

    public CECase(CECase input){
        caseID = input.getCaseID();
        publicControlCode = input.getPublicControlCode();
        paccEnabled = input.isPaccEnabled();
        pinned = input.pinned;
        allowForwardLinkedPublicAccess = input.isAllowForwardLinkedPublicAccess();
        parcelKey = input.getParcelKey();
        propertyUnitID = input.getPropertyUnitID();
        caseManager = input.getCaseManager();
        caseName = input.getCaseName();
        
        this.priority = input.getPriority();
        this.priorityAssignmentLog = input.getPriorityAssignmentLog();
        this.priorityIcon = input.priorityIcon;
        
        statusBundle = input.getStatusBundle();
        statusAssignmentLog = input.getStatusLog();
        originationDate = input.getOriginationDate();
        closingDate = input.getClosingDate();
        creationTimestamp = input.getCreationTimestamp();
        notes = input.getNotes();
        source = input.getSource();
        citationList = input.getCitationList();
        noticeList = input.getNoticeList();
        violationList = input.getViolationList();
        active = input.isActive();
        personInfoPersonID = input.getPersonInfoPersonID();
        propertyInfoCase = input.isPropertyInfoCase();
        lastUpdatedBy = input.getLastUpdatedBy();
        lastUpdatedTS = input.getLastUpdatedTS();
        eventList = input.getEventList();
        mostRecentPastEvent = input.getMostRecentPastEvent();
        originationEvent = input.originationEvent;
        closingEvent = input.closingEvent;
        daysSinceLastEvent = input.daysSinceLastEvent;
        
    }
    
    
    /**
     * Writes a string to the priority assignment log and then a break
     * @param msg 
     * @param postpendBreak if true, an HTML break is inserted
     */
    public void logPriorityAssignmentMessage(String msg, boolean postpendBreak){
        if(pLog == null){
            pLog = new StringBuilder();
        }
        pLog.append(msg);
        if(postpendBreak) {
            pLog.append(Constants.FMT_HTML_BREAK);
        }
        
    }
    
    /**
     * Special getter for the priority log which comes from an internal StringBuilder
     * @return the priorityAssignmentLog
     */
    public String getPriorityAssignmentLog() {
        if(pLog != null){
            priorityAssignmentLog = pLog.toString();
        }
        return priorityAssignmentLog;
    }

    
    
    @Override
    public String toString() {
        return caseName;
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
    public List<EventCnF> getEventList() {
        return eventList;
    }
    
    public List<EventCnF> getFutureEventList(){
        List<EventCnF> futureEvents = new ArrayList<>();
        // get active not hidden
        List<EventCnF> candidateEvents = getEventList(ViewOptionsActiveHiddenListsEnum.VIEW_ACTIVE_NOTHIDDEN);
        for(EventCnF ev: candidateEvents){
            if(ev.timeStart != null && ev.timeStart.isAfter(LocalDateTime.now())){
                futureEvents.add(ev);
            }
        }
        return futureEvents;
    }
    
    
     

    @Override
    public List<EventCnF> getEventList(ViewOptionsActiveHiddenListsEnum voahle) {
        List<EventCnF> visEventList = new ArrayList<>();
        if (eventList != null) {
            for (EventCnF ev : eventList) {
                switch (voahle) {
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
        Collections.sort(visEventList);
        return visEventList;
    }

    @Override
    public int getBObID() {
        return caseID;
    }

    /**
     * Builds our violation list based on inputted view options
     * @param viewOption
     * @return 
     */
    public List<CodeViolation> assembleViolationList(ViewOptionsActiveListsEnum viewOption){
        List<CodeViolation> displayedViolations = new ArrayList<>();
        if(violationList != null){
            for(CodeViolation cv: violationList){
                switch(viewOption){
                    case VIEW_ACTIVE:
                        if(cv.isActive()){
                            if(cv.getStatus() != null && !cv.getStatus().isTerminalStatus()){
                                displayedViolations.add(cv);
                            }
                        }
                        break;
                    case VIEW_ALL:
                            displayedViolations.add(cv);
                        break;
                    case VIEW_INACTIVE:
                        if(!cv.isActive()){
                            displayedViolations.add(cv);
                        }
                        break;
                    default: 
                        
                }
            }
        }
        Collections.sort(displayedViolations);
        Collections.reverse(displayedViolations);
        return displayedViolations;
    }
    
    /**
     * Builds our violation list based on inputted view options. Sorts by date DESC
     * 
     * @param viewOption
     * @return 
     */
    public List<NoticeOfViolation> assembleNoticeList(ViewOptionsActiveListsEnum viewOption){
        List<NoticeOfViolation> displayedNOVs = new ArrayList<>();
        if(noticeList != null){
            for(NoticeOfViolation nov: noticeList){
                switch(viewOption){
                    case VIEW_ACTIVE:
                        if(nov.isActive()){
                            displayedNOVs.add(nov);
                        }
                        break;
                    case VIEW_ALL:
                            displayedNOVs.add(nov);
                        break;
                    case VIEW_INACTIVE:
                        if(!nov.isActive()){
                            displayedNOVs.add(nov);
                        }
                        break;
                    default: 
                        
                }
            }
        }
        Collections.sort(displayedNOVs);
        Collections.reverse(displayedNOVs);
        return displayedNOVs;
    }
    
    /**
     * Builds our citation list based on inputted view options
     * @param viewOption
     * @return 
     */
    public List<Citation> assembleCitationList(ViewOptionsActiveListsEnum viewOption){
        List<Citation> dispCits = new ArrayList<>();
        if(citationList != null){
            for(Citation cit: citationList){
                switch(viewOption){
                    case VIEW_ACTIVE:
                        if(cit.isActive()){
                            CitationStatusLogEntry csle = cit.getMostRecentStatusLogEntry();
                            if(csle != null && csle.getStatus() != null && !csle.getStatus().isTerminalStatus()){
                                dispCits.add(cit);
                            }
                        }
                        break;
                    case VIEW_ALL:
                            dispCits.add(cit);
                        break;
                    case VIEW_INACTIVE:
                        if(!cit.isActive() || cit.getMostRecentStatusLogEntry() != null){
                            dispCits.add(cit);
                        }
                        break;
                    default: 
                        
                }
            }
        }
        
        return dispCits;
    }
    
    /**
     * Sorts through this case's citations and returns only those
     * not in draft state
     * @return 
     */
    public List<Citation> assembleCitationListNonDrafts(){
        List<Citation> citl = new ArrayList<>();
        if(citationList != null && !citationList.isEmpty()){
            System.out.println("CECase.assembleCitationListNonDraft: list size" + citationList.size());
            for(Citation cit: citationList){
                // look for citations which are not in draft state
                if(cit.getMostRecentStatusLogEntry().isNonStatusEditsForbidden()){
                    citl.add(cit);
                    System.out.println("CECase.assembleCitationListNonDraft: found non-draft citation ID " + cit.getCitationID());
                }
            }
        }
        return citl;
    }

   
    
    public long getCaseAge() {
        if(closingDate != null){
            return DateTimeUtil.getTimePeriodAsDays(originationDate, closingDate);
        } else {
            return DateTimeUtil.getTimePeriodAsDays(originationDate, LocalDateTime.now());
        }
    }
    
    public long getCaseAgeAsOf(LocalDateTime ageEndTime){
        return DateTimeUtil.getTimePeriodAsDays(originationDate, ageEndTime);
        
    }
    
      /**
     * @return the violationListUnresolved
     */
    public List<CodeViolation> getViolationListUnresolved() {

        List<CodeViolation> violationListUnresolved = new ArrayList<>();
        if (violationList != null && violationList.size() > 0) {
            for (CodeViolation v : violationList) {
                if (v.getActualComplianceDate() == null) {
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
        List<CodeViolation> violationListResolved = new ArrayList<>();
        if (violationList != null && violationList.size() > 0) {
            for (CodeViolation v : violationList) {
                if (v.getActualComplianceDate() != null) {
                    violationListResolved.add(v);
                }
            }
        }

        return violationListResolved;
    }

    
    /**
     * @return the caseID
     */
    public int getCaseID() {
        return caseID;
    }

    /**
     * @param caseID the caseID to set
     */
    public void setCaseID(int caseID) {
        this.caseID = caseID;
    }

    /**
     * @return the caseManager
     */
    public User getCaseManager() {
        return caseManager;
    }

    /**
     * @param caseManager the caseManager to set
     */
    public void setCaseManager(User caseManager) {
        this.caseManager = caseManager;
    }

    /**
     * @return the caseName
     */
    public String getCaseName() {
        return caseName;
    }

    /**
     * @param caseName the caseName to set
     */
    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

   

    /**
     * @return the originationDate
     */
    public LocalDateTime getOriginationDate() {
        return originationDate;
    }

    /**
     * @param originationDate the originationDate to set
     */
    public void setOriginationDate(LocalDateTime originationDate) {
        this.originationDate = originationDate;
    }

    /**
     * @return the closingDate
     */
    public LocalDateTime getClosingDate() {
        return closingDate;
    }

    /**
     * @param closingDate the closingDate to set
     */
    public void setClosingDate(LocalDateTime closingDate) {
        this.closingDate = closingDate;
    }

    /**
     * @return the notes
     */
    @Override
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
    @Override
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * @return the publicControlCode
     */
    public int getPublicControlCode() {
        return publicControlCode;
    }

    /**
     * @param publicControlCode the publicControlCode to set
     */
    public void setPublicControlCode(int publicControlCode) {
        this.publicControlCode = publicControlCode;
    }

    /**
     * @return the creationTimestamp
     */
    public LocalDateTime getCreationTimestamp() {
        return creationTimestamp;
    }

    /**
     * @param creationTimestamp the creationTimestamp to set
     */
    public void setCreationTimestamp(LocalDateTime creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

   
    
    /**
     * @return the paccEnabled
     */
    public boolean isPaccEnabled() {
        return paccEnabled;
    }

    /**
     * @param paccEnabled the paccEnabled to set
     */
    public void setPaccEnabled(boolean paccEnabled) {
        this.paccEnabled = paccEnabled;
    }

    /**
     * @return the allowForwardLinkedPublicAccess
     */
    public boolean isAllowForwardLinkedPublicAccess() {
        return allowForwardLinkedPublicAccess;
    }

    /**
     * @param allowForwardLinkedPublicAccess the allowForwardLinkedPublicAccess to set
     */
    public void setAllowForwardLinkedPublicAccess(boolean allowForwardLinkedPublicAccess) {
        this.allowForwardLinkedPublicAccess = allowForwardLinkedPublicAccess;
    }
    
    @Override
    public int compareTo(CECase cse) {
         
        int c = 0;
        if(this.originationDate != null && cse.getOriginationDate() != null){
             c = this.originationDate.compareTo(cse.getOriginationDate());
        } else if(this.creationTimestamp != null && cse.creationTimestamp != null){
             c = this.creationTimestamp.compareTo(cse.creationTimestamp);
        } 
        return c;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.caseID;
        hash = 53 * hash + this.publicControlCode;
        hash = 53 * hash + (this.paccEnabled ? 1 : 0);
        hash = 53 * hash + (this.allowForwardLinkedPublicAccess ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.caseManager);
        hash = 53 * hash + Objects.hashCode(this.caseName);
        hash = 53 * hash + Objects.hashCode(this.parcelKey);
        hash = 53 * hash + Objects.hashCode(this.propertyUnitID);
        hash = 53 * hash + Objects.hashCode(this.originationDate);
        hash = 53 * hash + Objects.hashCode(this.closingDate);
        hash = 53 * hash + Objects.hashCode(this.creationTimestamp);
        hash = 53 * hash + Objects.hashCode(this.notes);
        return hash;
    }

    /**
     * Cases are equal if they have the same ID;
     * CAUTION: Since only IDs are the only input
     * to equality, cases whose contents may be different
     * will still be equal if they are keyed the same.
     * 
     * @param obj
     * @return 
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CECase other = (CECase) obj;
        if (this.caseID != other.caseID) {
            return false;
        }
        return true;
    }

  

   
    /**
     * @return the source
     */
    public BOBSource getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(BOBSource source) {
        this.source = source;
    }

    /**
     * @return the citationList
     */
    public List<Citation> getCitationList() {
        return citationList;
    }

    /**
     * @return the noticeList
     */
    public List<NoticeOfViolation> getNoticeList() {
        return noticeList;
    }

    /**
     * @return the violationList
     */
    public List<CodeViolation> getViolationList() {
        return violationList;
    }

    /**
     * @param citationList the citationList to set
     */
    public void setCitationList(List<Citation> citationList) {
        this.citationList = citationList;
    }

    /**
     * @param noticeList the noticeList to set
     */
    public void setNoticeList(List<NoticeOfViolation> noticeList) {
        this.noticeList = noticeList;
    }

    /**
     * @param violationList the violationList to set
     */
    public void setViolationList(List<CodeViolation> violationList) {
        this.violationList = violationList;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the parcelKey
     */
    public int getParcelKey() {
        return parcelKey;
    }

    /**
     * @return the propertyUnitID
     */
    public int getPropertyUnitID() {
        return propertyUnitID;
    }

    /**
     * @param parcelKey the parcelKey to set
     */
    public void setParcelKey(int parcelKey) {
        this.parcelKey = parcelKey;
    }

    /**
     * @param propertyUnitID the propertyUnitID to set
     */
    public void setPropertyUnitID(int propertyUnitID) {
        this.propertyUnitID = propertyUnitID;
    }

    /**
     * @return the propertyInfoCase
     */
    public boolean isPropertyInfoCase() {
        return propertyInfoCase;
    }

    /**
     * @param propertyInfoCase the propertyInfoCase to set
     */
    public void setPropertyInfoCase(boolean propertyInfoCase) {
        this.propertyInfoCase = propertyInfoCase;
    }

    /**
     * @return the personInfoPersonID
     */
    public int getPersonInfoPersonID() {
        return personInfoPersonID;
    }

    /**
     * @param personInfoPersonID the personInfoPersonID to set
     */
    public void setPersonInfoPersonID(int personInfoPersonID) {
        this.personInfoPersonID = personInfoPersonID;
    }

    /**
     * @return the lastUpdatedBy
     */
    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    /**
     * @return the lastUpdatedTS
     */
    public LocalDateTime getLastUpdatedTS() {
        return lastUpdatedTS;
    }

    /**
     * @param lastUpdatedBy the lastUpdatedBy to set
     */
    public void setLastUpdatedBy(User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    /**
     * @param lastUpdatedTS the lastUpdatedTS to set
     */
    public void setLastUpdatedTS(LocalDateTime lastUpdatedTS) {
        this.lastUpdatedTS = lastUpdatedTS;
    }

    /**
     * @return the statusBundle
     */
    public CECaseStatus getStatusBundle() {
        return statusBundle;
    }

    /**
     * @param statusBundle the statusBundle to set
     */
    public void setStatusBundle(CECaseStatus statusBundle) {
        this.statusBundle = statusBundle;
    }

   
    @Override
    public void setEventList(List<EventCnF> eventList) {
        this.eventList = eventList;
    }

 

    @Override
    public String getStatusLog() {
        return statusAssignmentLog;
    }

    @Override
    public void logStatusNote(String note) {
        StringBuilder sb;
        if(note != null){
            sb = new StringBuilder();
            sb.append(statusAssignmentLog);
            sb.append(Constants.FMT_HTML_BREAK);
            sb.append(note);
            statusAssignmentLog = sb.toString();
        }
    }

    @Override
    public String getPKFieldName() {
        return CASE_PK_FIELD;
    }

    @Override
    public int getDBKey() {
        return caseID;
    }

    @Override
    public String getDBTableName() {
        return CASE_TABLE_NAME;
    }

    @Override
    public String getNoteHolderFriendlyName() {
        return CASE_FRIENDLY_NAME;
    }

    @Override
    public DomainEnum getEventDomain() {
        return CECASE_ENUM;
    }

    /**
     * @return the originationEvent
     */
    public EventCnF getOriginationEvent() {
        return originationEvent;
    }

    /**
     * @param originationEvent the originationEvent to set
     */
    public void setOriginationEvent(EventCnF originationEvent) {
        this.originationEvent = originationEvent;
    }

    /**
     * @return the closingEvent
     */
    public EventCnF getClosingEvent() {
        return closingEvent;
    }

    /**
     * @param closingEvent the closingEvent to set
     */
    public void setClosingEvent(EventCnF closingEvent) {
        this.closingEvent = closingEvent;
    }

    /**
     * @return the daysSinceLastEvent
     */
    public Long getDaysSinceLastEvent() {
        
        return daysSinceLastEvent;
    }

    /**
     * @param daysSinceLastEvent the daysSinceLastEvent to set
     */
    public void setDaysSinceLastEvent(Long daysSinceLastEvent) {
        this.daysSinceLastEvent = daysSinceLastEvent;
    }

    /**
     * @return the priority
     */
    public PriorityEnum getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(PriorityEnum priority) {
        this.priority = priority;
    }

    /**
     * @return the priorityIcon
     */
    public Icon getPriorityIcon() {
        return priorityIcon;
    }

    /**
     * @param priorityIcon the priorityIcon to set
     */
    public void setPriorityIcon(Icon priorityIcon) {
        this.priorityIcon = priorityIcon;
    }

    /**
     * @return the mostRecentPastEvent
     */
    public EventCnF getMostRecentPastEvent() {
        return mostRecentPastEvent;
    }

    /**
     * @param mostRecentPastEvent the mostRecentPastEvent to set
     */
    public void setMostRecentPastEvent(EventCnF mostRecentPastEvent) {
        this.mostRecentPastEvent = mostRecentPastEvent;
    }

    /**
     * @return the pinned
     */
    @Override
    public boolean isPinned() {
        return pinned;
    }

    /**
     * @param pinned the pinned to set
     */
    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    @Override
    public void setPinner(User usr) {
        pinner = usr;
    }

    @Override
    public User getPinner() {
        return pinner;
    }

    @Override
    public String getPinTableFKString() {
        return PIN_FK_FIELD;
    }

    @Override
    public String getPinTableName() {
        return PIN_TABLE_NAME;
    }

    @Override
    public StateEnum getState() {
        if(closingDate == null){
            return StateEnum.OPEN;
        } else {
            return StateEnum.CLOSED;
        }
                
    }
   
}
