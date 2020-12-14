/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.application.interfaces.IFace_EventRuleGoverned;
import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import com.tcvcog.tcvce.util.Constants;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveHiddenListsEnum;
import com.tcvcog.tcvce.util.viewoptions.ViewOptionsActiveListsEnum;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class        CECase 
        extends     EntityUtils
        implements  Cloneable,
                    IFace_Loggable,
                    IFace_EventHolder,
                    IFace_StatusLogHolder,
                    Comparable<CECase>{
    
    protected int caseID;
    protected int publicControlCode;
    protected boolean paccEnabled;
    /**
     * Code enforcement action requests are generally linked
     * to a code enforcement case by the code officers.
     * This switch allows the release of the allowed
     * case info to any holder of a PACC for a CEActionRequest
     * that contains a link to this case.
     */
    protected boolean allowForwardLinkedPublicAccess;
    
    protected int propertyID;
    protected int propertyUnitID;
    
    protected User caseManager;
    protected String caseName;
    
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
    protected int personInfoPersonID;
    protected boolean propertyInfoCase;
    
    protected User lastUpdatedBy;
    protected LocalDateTime lastUpdatedTS;
    
    protected List<EventCnF> eventList;
    protected List<EventCnF> eventListMaster;
    
    public CECase(){
        
    }

    public CECase(CECaseDataHeavy input){
        caseID = input.getCaseID();
        publicControlCode = input.getPublicControlCode();
        paccEnabled = input.isPaccEnabled();
        allowForwardLinkedPublicAccess = input.isAllowForwardLinkedPublicAccess();
        propertyID = input.getPropertyID();
        propertyUnitID = input.getPropertyUnitID();
        caseManager = input.getCaseManager();
        caseName = input.getCaseName();
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
        eventListMaster = input.getEventList();
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
        return visEventList;
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
                            displayedViolations.add(cv);
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
        return displayedViolations;
    }
    
    /**
     * Builds our violation list based on inputted view options
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
                        if(cit.isIsActive()){
                            dispCits.add(cit);
                        }
                        break;
                    case VIEW_ALL:
                            dispCits.add(cit);
                        break;
                    case VIEW_INACTIVE:
                        if(!cit.isIsActive()){
                            dispCits.add(cit);
                        }
                        break;
                    default: 
                        
                }
            }
        }
        return dispCits;
    }

   
    
    public long getCaseAge() {
        return EntityUtils.getTimePeriodAsDays(originationDate, LocalDateTime.now());
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
    public String getNotes() {
        return notes;
    }

    /**
     * @param notes the notes to set
     */
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
     * @return the originiationDatePretty
     */
    public String getOriginiationDatePretty() {
        if(originationDate != null){
            return EntityUtils.getPrettyDate(originationDate);
        }
        return null;
    }

    /**
     * @return the closingDatePretty
     */
    public String getClosingDatePretty() {
        if(closingDate != null){
            return EntityUtils.getPrettyDate(closingDate);
        }
        return null;
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
        hash = 53 * hash + Objects.hashCode(this.propertyID);
        hash = 53 * hash + Objects.hashCode(this.propertyUnitID);
        hash = 53 * hash + Objects.hashCode(this.originationDate);
        hash = 53 * hash + Objects.hashCode(this.closingDate);
        hash = 53 * hash + Objects.hashCode(this.creationTimestamp);
        hash = 53 * hash + Objects.hashCode(this.notes);
        return hash;
    }

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
     * @return the closingDateUtilDate
     */
    public java.util.Date getClosingDateUtilDate() {
        return convertUtilDate(closingDate);
    }

    /**
     * @param cd     
     */
    public void setClosingDateUtilDate(java.util.Date cd) {
        closingDate = convertUtilDate(cd);
    }

    /**
     * @return the originationDateUtilDate
     */
    public java.util.Date getOriginationDateUtilDate() {
        return convertUtilDate(originationDate);
    }

    /**
     * @param od     
     */
    public void setOriginationDateUtilDate(java.util.Date od) {
        originationDate = convertUtilDate(od);
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
     * @return the propertyID
     */
    public int getPropertyID() {
        return propertyID;
    }

    /**
     * @return the propertyUnitID
     */
    public int getPropertyUnitID() {
        return propertyUnitID;
    }

    /**
     * @param propertyID the propertyID to set
     */
    public void setPropertyID(int propertyID) {
        this.propertyID = propertyID;
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

   
    public List<EventCnF> getEventListMaster() {
        return eventListMaster;
    }

    @Override
    public void setEventList(List<EventCnF> eventList) {
        this.eventList = eventList;
    }

    public void setEventListMaster(List<EventCnF> eventListMaster) {
        if (eventListMaster != null) {
            this.eventListMaster = eventListMaster;
        }
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
    
}
