/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.domain.CaseLifecyleException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class CECaseNoLists extends EntityUtils implements Serializable, Cloneable {
    
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
    protected Property property;
    protected PropertyUnit propertyUnit;
    protected User caseManager;
    protected String caseName;
    protected CasePhase casePhase;
    protected CaseStage caseStage;
    private String caseOpenClosed;
    
    protected Icon casePhaseIcon;
    protected LocalDateTime originationDate;
    protected String originiationDatePretty;
    protected LocalDateTime closingDate;
    protected String closingDatePretty;
    protected LocalDateTime creationTimestamp;
    protected String notes;

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
    public CECaseNoLists clone() throws CloneNotSupportedException{
        super.clone();
        return null;
        
        
    }

    public long getCaseAge() {
        return getTimePeriodAsDays(originationDate, LocalDateTime.now());
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
     * @return the property
     */
    public Property getProperty() {
        return property;
    }

    /**
     * @param property the property to set
     */
    public void setProperty(Property property) {
        this.property = property;
    }

    /**
     * @return the propertyUnit
     */
    public PropertyUnit getPropertyUnit() {
        return propertyUnit;
    }

    /**
     * @param propertyUnit the propertyUnit to set
     */
    public void setPropertyUnit(PropertyUnit propertyUnit) {
        this.propertyUnit = propertyUnit;
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
    public CasePhase getCasePhase() {
        return casePhase;
    }

    /**
     * @param casePhase the casePhase to set
     */
    public void setCasePhase(CasePhase casePhase) {
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
        originiationDatePretty = getPrettyDate(originationDate);
        return originiationDatePretty;
    }

    /**
     * @return the closingDatePretty
     */
    public String getClosingDatePretty() {
        return closingDatePretty;
    }

    /**
     * @param originiationDatePretty the originiationDatePretty to set
     */
    public void setOriginiationDatePretty(String originiationDatePretty) {
        this.originiationDatePretty = originiationDatePretty;
    }

    /**
     * @param closingDatePretty the closingDatePretty to set
     */
    public void setClosingDatePretty(String closingDatePretty) {
        this.closingDatePretty = closingDatePretty;
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
     * @return the caseStage
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException
     */
    public CaseStage getCaseStage() throws CaseLifecyleException {
        CaseStage stage;
        switch (casePhase) {
            case PrelimInvestigationPending:
                stage = CaseStage.Investigation;
                break;
            case NoticeDelivery:
                stage = CaseStage.Investigation;
                break;
        // Letter marked with a send date
            case InitialComplianceTimeframe:
                stage = CaseStage.Enforcement;
                break;
        // compliance inspection
            case SecondaryComplianceTimeframe:
                stage = CaseStage.Enforcement;
                break;
        // Filing of citation
            case AwaitingHearingDate:
                stage = CaseStage.Citation;
                break;
        // hearing date scheduled
            case HearingPreparation:
                stage = CaseStage.Citation;
                break;
        // hearing not resulting in a case closing
            case InitialPostHearingComplianceTimeframe:
                stage = CaseStage.Citation;
                break;
            case SecondaryPostHearingComplianceTimeframe:
                stage = CaseStage.Citation;
                break;
            case Closed:
                stage = CaseStage.Closed;
                // TODO deal with this later
                //                throw new CaseLifecyleException("Cannot advance a closed case to any other phase");
                break;
            case InactiveHolding:
                stage = CaseStage.Closed;
                break;
            default:
                stage = CaseStage.Closed;
        }
        caseStage = stage;
        return caseStage;
    }

    /**
     * @param caseStage the caseStage to set
     */
    public void setCaseStage(CaseStage caseStage) {
        this.caseStage = caseStage;
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
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.caseID;
        hash = 53 * hash + this.publicControlCode;
        hash = 53 * hash + (this.paccEnabled ? 1 : 0);
        hash = 53 * hash + (this.allowForwardLinkedPublicAccess ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.property);
        hash = 53 * hash + Objects.hashCode(this.propertyUnit);
        hash = 53 * hash + Objects.hashCode(this.caseManager);
        hash = 53 * hash + Objects.hashCode(this.caseName);
        hash = 53 * hash + Objects.hashCode(this.casePhase);
        hash = 53 * hash + Objects.hashCode(this.caseStage);
        hash = 53 * hash + Objects.hashCode(this.casePhaseIcon);
        hash = 53 * hash + Objects.hashCode(this.originationDate);
        hash = 53 * hash + Objects.hashCode(this.originiationDatePretty);
        hash = 53 * hash + Objects.hashCode(this.closingDate);
        hash = 53 * hash + Objects.hashCode(this.closingDatePretty);
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
        final CECaseNoLists other = (CECaseNoLists) obj;
        if (this.caseID != other.caseID) {
            return false;
        }
        return true;
    }

    /**
     * @return the caseOpenClosed
     * @throws com.tcvcog.tcvce.domain.CaseLifecyleException
     */
    public String getCaseOpenClosed() throws CaseLifecyleException {
        String stat = null;
        if(getCaseStage() != CaseStage.Closed){
            stat = "Open";
        } else {
            stat = "Closed";
        }
        
        caseOpenClosed = stat;
        return caseOpenClosed;
    }

    /**
     * @param caseOpenClosed the caseOpenClosed to set
     */
    public void setCaseOpenClosed(String caseOpenClosed) {
        this.caseOpenClosed = caseOpenClosed;
    }
    
    
    
}
