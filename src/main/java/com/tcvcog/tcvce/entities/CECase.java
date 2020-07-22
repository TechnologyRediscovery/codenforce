/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class        CECase 
        extends     CECasePublic
        implements  IFace_Openable,
                    Cloneable,
                    IFace_Loggable,
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
    
    protected CasePhaseEnum casePhase;
    protected Icon casePhaseIcon;
    
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
    
    public CECase(){
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
    public boolean isOpen() {
        if(this.casePhase != null){
            return this.casePhase.isCaseOpen();
        } else if (this.getClosingDate() != null){
            return true;
        } else {
            return false;
        }
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
     * @return the casePhase
     */
    public CasePhaseEnum getCasePhase() {
        return casePhase;
    }

    /**
     * @param casePhase the casePhase to set
     */
    public void setCasePhase(CasePhaseEnum casePhase) {
        this.casePhase = casePhase;
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

   

    /**
     * @return the casePhaseIcon
     */
    public Icon getCasePhaseIcon() {
        return casePhaseIcon;
    }

    /**
     * @param casePhaseIcon the casePhaseIcon to set
     */
    public void setCasePhaseIcon(Icon casePhaseIcon) {
        this.casePhaseIcon = casePhaseIcon;
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
        hash = 53 * hash + Objects.hashCode(this.casePhase);
        hash = 53 * hash + Objects.hashCode(this.casePhaseIcon);
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
        if(closingDate != null){
            return  java.util.Date.from(closingDate.atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * @param cd     
     */
    public void setClosingDateUtilDate(java.util.Date cd) {
        if(cd != null){
            this.closingDate = cd.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
    }

    /**
     * @return the originationDateUtilDate
     */
    public java.util.Date getOriginationDateUtilDate() {
        if(originationDate != null){
            return  java.util.Date.from(originationDate.atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }

    /**
     * @param od     
     */
    public void setOriginationDateUtilDate(java.util.Date od) {
        if(od != null){
            this.originationDate = od.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
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

    
}
