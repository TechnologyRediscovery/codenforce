/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Ushering a code enforcement case through these phases is driven by a set of rules which are encapsulated in an object called the `CaseChangeRule`. These objects live on `EventCategory` objects and, when present on a given `Event`, are used by the `CaseCoordinator` during event processing to make two distinct but related determinations:
 
 1) Should this case's phase be changed? If so, to what?
 2) Do I need to create an Event that requests an action from a user? If so, what `EventCategory` should be requested?

 Information for making both of these determinations is bundled in the single `CaseChangeRule` object because many requested event categories themselves trigger case phase changes, so designing the relationships together and writing them to a single object simplifies what is already a somewhat convoluted determination process.
 * @author sylvia
 */
public class CaseChangeRule extends EntityUtils implements Serializable {
    
    /**
     * Rule's unique ID pulled from DB
     */
    private int ruleID;
    /**
     * Human-friendly title of rule
     */
    private String title;
    /**
     * Human friendly description of this rule
     */
    private String description;
        
    /**
     * The case to which the Code Enforcent case will be assigned if the rule
     * passes. 
     */
    private CasePhase targetCasePhase;
    /**
     * For rule to pass, the case  must currently be in this CasePhase
     */
    private CasePhase requiredCurrentCasePhase;
    /**
     * CasePhases are ordered. If this memvar is true, consider 
     * requiredCurrentCasePhase value as the UPPER END boundary
     * of a rang of CasePhases extending from the initial phase to the specified
     * CasePhase
     * 
     */
    private boolean treatRequiredPhaseAsThreshold;
    
    /**
     * For rule to pass, the case must NOT be in this CasePhase
     */
    private CasePhase forbiddenCurrentCasePhase;
    /**
     * CasePhases are ordered. If this memvar is true, consider the 
     * forbiddenCurrentCasePhase value the LOWER END of a range of CasePhases
     * extending to the highest ordinal CasePhase, which is CasePhase.Closed
     */
    private boolean treatForbiddenPhaseAsThreshold;
    
    /**
     * For the rule to pass, an event of this EventTYpe must exist on the case 
     */
    private EventType requiredExtantEventType;
    /**
     * For the rule to pass, an event of this EventType may NOT exist on the case.
     * This is handy to avoid adding multiple Events triggered by the presence of 
     * a different event. E.g. create a rule that says "Add a reminder to create a 
     * NOV as long as there is a CodeViolation attachment event not BUT 
     * a NoticeQueued event
     */
    private EventType forbiddenExtantEventType;
    /**
     * For the rule to pass, an event of this EventCategory id MUST exist on the case 
     */
    private int requiredExtantEventCatID;
    /**
     * For the rule to pass, an event of this EventCategory id MUST NOT exist on the case 
     */
    private int forbiddenExtantEventCatID;
    
    /**
     * If the rule passes, create an Event with this category ID
     */
    private int triggeredEventCategoryID;
    /**
     * If the rule passes and a triggeredEventCategoryID is specified, 
     * load up that Event with an EventCategory with this ID
     */
    /**
     * Declares the rule active or inactive; inactiev rules are completely ignored
     */
    private boolean active;
    /**
     * Objects containing this rule must have the rule processes
     */
    private boolean mandatory;
    /**
     * If toggled to true, if this rule failes, trigger an exception and do not
     * process any actions assocaited with the Event or CitationStatus object
     */
    private boolean rejectRuleHostIfRuleFails;
    
   

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
        final CaseChangeRule other = (CaseChangeRule) obj;
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
      
        return true;
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

    /**
     * @return the triggeredEventCategoryID
     */
    public int getTriggeredEventCategoryID() {
        return triggeredEventCategoryID;
    }

    /**
     * @param triggeredEventCategoryID the triggeredEventCategoryID to set
     */
    public void setTriggeredEventCategoryID(int triggeredEventCategoryID) {
        this.triggeredEventCategoryID = triggeredEventCategoryID;
    }

    /**
     * @return the requiredExtantEventCatID
     */
    public int getRequiredExtantEventCatID() {
        return requiredExtantEventCatID;
    }

    /**
     * @return the forbiddenExtantEventCatID
     */
    public int getForbiddenExtantEventCatID() {
        return forbiddenExtantEventCatID;
    }


    /**
     * @param requiredExtantEventCatID the requiredExtantEventCatID to set
     */
    public void setRequiredExtantEventCatID(int requiredExtantEventCatID) {
        this.requiredExtantEventCatID = requiredExtantEventCatID;
    }

    /**
     * @param forbiddenExtantEventCatID the forbiddenExtantEventCatID to set
     */
    public void setForbiddenExtantEventCatID(int forbiddenExtantEventCatID) {
        this.forbiddenExtantEventCatID = forbiddenExtantEventCatID;
    }

    
    
}
