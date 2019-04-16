/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author sylvia
 */
public class CasePhaseChangeRule extends EntityUtils implements Serializable {
    
    private int ruleID;
    private String title;
        
    private CasePhase targetCasePhase;
    private CasePhase requiredCurrentCasePhase;
    private CasePhase forbiddenCurrentCasePhase;
    
    private EventType requiredExtantEventType;
    private EventType forbiddenExtantEventType;
    
    private EventCategory requiredExtantEventCat;
    private EventCategory forbiddenExtantEventCat;
    
    private EventCategory triggeredEventCategory;
    private EventCategory triggeredEventCategoryRequestedEvent;
    
    private boolean active;
    private boolean mandatory;
    private boolean treatRequiredPhaseAsThreshold;
    private boolean treatForbiddenPhaseAsThreshold;
    private boolean rejectRuleHostIfRuleFails;
    private String description;
   

    /**
     * @return the targetCasePhase
     */
    public CasePhase getTargetCasePhase() {
        return targetCasePhase;
    }

    /**
     * @param targetCasePhase the targetCasePhase to set
     */
    public void setTargetCasePhase(CasePhase targetCasePhase) {
        this.targetCasePhase = targetCasePhase;
    }

    /**
     * @return the ruleID
     */
    public int getRuleID() {
        return ruleID;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the requiredCurrentCasePhase
     */
    public CasePhase getRequiredCurrentCasePhase() {
        return requiredCurrentCasePhase;
    }

    /**
     * @return the forbiddenCurrentCasePhase
     */
    public CasePhase getForbiddenCurrentCasePhase() {
        return forbiddenCurrentCasePhase;
    }

    /**
     * @return the requiredExtantEventType
     */
    public EventType getRequiredExtantEventType() {
        return requiredExtantEventType;
    }

    /**
     * @return the forbiddenExtantEventType
     */
    public EventType getForbiddenExtantEventType() {
        return forbiddenExtantEventType;
    }

    /**
     * @return the requiredExtantEventCat
     */
    public EventCategory getRequiredExtantEventCat() {
        return requiredExtantEventCat;
    }

    /**
     * @return the forbiddenExtantEventCat
     */
    public EventCategory getForbiddenExtantEventCat() {
        return forbiddenExtantEventCat;
    }

    /**
     * @param ruleID the ruleID to set
     */
    public void setRuleID(int ruleID) {
        this.ruleID = ruleID;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @param requiredCurrentCasePhase the requiredCurrentCasePhase to set
     */
    public void setRequiredCurrentCasePhase(CasePhase requiredCurrentCasePhase) {
        this.requiredCurrentCasePhase = requiredCurrentCasePhase;
    }

    /**
     * @param forbiddenCurrentCasePhase the forbiddenCurrentCasePhase to set
     */
    public void setForbiddenCurrentCasePhase(CasePhase forbiddenCurrentCasePhase) {
        this.forbiddenCurrentCasePhase = forbiddenCurrentCasePhase;
    }

    /**
     * @param requiredExtantEventType the requiredExtantEventType to set
     */
    public void setRequiredExtantEventType(EventType requiredExtantEventType) {
        this.requiredExtantEventType = requiredExtantEventType;
    }

    /**
     * @param forbiddenExtantEventType the forbiddenExtantEventType to set
     */
    public void setForbiddenExtantEventType(EventType forbiddenExtantEventType) {
        this.forbiddenExtantEventType = forbiddenExtantEventType;
    }

    /**
     * @param requiredExtantEventCat the requiredExtantEventCat to set
     */
    public void setRequiredExtantEventCat(EventCategory requiredExtantEventCat) {
        this.requiredExtantEventCat = requiredExtantEventCat;
    }

    /**
     * @param forbiddenExtantEventCat the forbiddenExtantEventCat to set
     */
    public void setForbiddenExtantEventCat(EventCategory forbiddenExtantEventCat) {
        this.forbiddenExtantEventCat = forbiddenExtantEventCat;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.ruleID);
        hash = 11 * hash + Objects.hashCode(this.title);
        hash = 11 * hash + Objects.hashCode(this.targetCasePhase);
        hash = 11 * hash + Objects.hashCode(this.requiredCurrentCasePhase);
        hash = 11 * hash + Objects.hashCode(this.forbiddenCurrentCasePhase);
        hash = 11 * hash + Objects.hashCode(this.requiredExtantEventType);
        hash = 11 * hash + Objects.hashCode(this.forbiddenExtantEventType);
        hash = 11 * hash + Objects.hashCode(this.requiredExtantEventCat);
        hash = 11 * hash + Objects.hashCode(this.forbiddenExtantEventCat);
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
        final CasePhaseChangeRule other = (CasePhaseChangeRule) obj;
        if (!Objects.equals(this.ruleID, other.ruleID)) {
            return false;
        }
        if (!Objects.equals(this.title, other.title)) {
            return false;
        }
        if (this.targetCasePhase != other.targetCasePhase) {
            return false;
        }
        if (this.requiredCurrentCasePhase != other.requiredCurrentCasePhase) {
            return false;
        }
        if (this.forbiddenCurrentCasePhase != other.forbiddenCurrentCasePhase) {
            return false;
        }
        if (this.requiredExtantEventType != other.requiredExtantEventType) {
            return false;
        }
        if (this.forbiddenExtantEventType != other.forbiddenExtantEventType) {
            return false;
        }
        if (!Objects.equals(this.requiredExtantEventCat, other.requiredExtantEventCat)) {
            return false;
        }
        if (!Objects.equals(this.forbiddenExtantEventCat, other.forbiddenExtantEventCat)) {
            return false;
        }
        return true;
    }

    /**
     * @return the triggeredEventCategory
     */
    public EventCategory getTriggeredEventCategory() {
        return triggeredEventCategory;
    }

    /**
     * @return the triggeredEventCategoryRequestedEvent
     */
    public EventCategory getTriggeredEventCategoryRequestedEvent() {
        return triggeredEventCategoryRequestedEvent;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @return the mandatory
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * @return the treatRequiredPhaseAsThreshold
     */
    public boolean isTreatRequiredPhaseAsThreshold() {
        return treatRequiredPhaseAsThreshold;
    }

    /**
     * @return the treatForbiddenPhaseAsThreshold
     */
    public boolean isTreatForbiddenPhaseAsThreshold() {
        return treatForbiddenPhaseAsThreshold;
    }

    /**
     * @return the rejectRuleHostIfRuleFails
     */
    public boolean isRejectRuleHostIfRuleFails() {
        return rejectRuleHostIfRuleFails;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param triggeredEventCategory the triggeredEventCategory to set
     */
    public void setTriggeredEventCategory(EventCategory triggeredEventCategory) {
        this.triggeredEventCategory = triggeredEventCategory;
    }

    /**
     * @param triggeredEventCategoryRequestedEvent the triggeredEventCategoryRequestedEvent to set
     */
    public void setTriggeredEventCategoryRequestedEvent(EventCategory triggeredEventCategoryRequestedEvent) {
        this.triggeredEventCategoryRequestedEvent = triggeredEventCategoryRequestedEvent;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @param mandatory the mandatory to set
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * @param treatRequiredPhaseAsThreshold the treatRequiredPhaseAsThreshold to set
     */
    public void setTreatRequiredPhaseAsThreshold(boolean treatRequiredPhaseAsThreshold) {
        this.treatRequiredPhaseAsThreshold = treatRequiredPhaseAsThreshold;
    }

    /**
     * @param treatForbiddenPhaseAsThreshold the treatForbiddenPhaseAsThreshold to set
     */
    public void setTreatForbiddenPhaseAsThreshold(boolean treatForbiddenPhaseAsThreshold) {
        this.treatForbiddenPhaseAsThreshold = treatForbiddenPhaseAsThreshold;
    }

    /**
     * @param rejectRuleHostIfRuleFails the rejectRuleHostIfRuleFails to set
     */
    public void setRejectRuleHostIfRuleFails(boolean rejectRuleHostIfRuleFails) {
        this.rejectRuleHostIfRuleFails = rejectRuleHostIfRuleFails;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    
    
    
    
    
}
