/*
 * Copyright (C) 2019 Technology Rediscovery LLC
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
package com.tcvcog.tcvce.entities;

import java.time.LocalDateTime;

/**
 * Represents an EventRuleAbstract that has been attached to a specific 
 * business object, which in Dec 2019 were CECase and OccPeriod objects
 * 
 * The original creation of this object tree involved subtypes for OccPeriod
 * and CECase objects, since they had Event objects subtyped to them at that time.
 * 
 * During the Grand Event Reunification GER, this class's subclasses were deprecated
 * in favor of employing the same EventDomain enum flag.
 * 
 * @author Ellen Bascomb
 */
public  class       EventRuleImplementation 
        extends     EventRuleAbstract {
    
    protected int implementationID;
    protected EventDomainEnum domain;
    protected int ceCaseID;
    protected int occPeriodID;
    
    protected LocalDateTime implementationTS;
    protected User implementedBy;
    
    protected LocalDateTime lastEvaluatedTS;
    protected LocalDateTime passedRuleTS;
    
    protected EventCnF triggeredEvent;
    
    protected LocalDateTime waivedTS;
    protected User waivedBy;
    
    protected LocalDateTime passOverrideTS;
    protected User passOverrideBy;
    
    protected LocalDateTime deactivatedTS;
    protected User deactivatedBy;
    
    protected String impNotes;
    
    public EventRuleImplementation(EventRuleAbstract rule){
        
        this.ruleid = rule.getRuleid();
        this.title = rule.getTitle();
        this.description = rule.getDescription();
        this.requiredEventType = rule.getRequiredEventType();
        this.forbiddenEventType = rule.getForbiddenEventType();
        this.requiredEventCategory = rule.getRequiredEventCategory();
        this.forbiddenEventCategory = rule.getForbiddenEventCategory();

        this.requiredECThreshold_typeInternalOrder = rule.isRequiredeventcatthresholdtypeintorder();
        this.requiredECThreshold_typeInternalOrder_treatAsUpperBound = rule.isRequiredECThreshold_typeInternalOrder_treatAsUpperBound();

        this.requiredECThreshold_globalOrder = rule.isRequiredeventcatthresholdglobalorder();
        this.requiredECThreshold_globalOrder_treatAsUpperBound = rule.isRequiredECThreshold_globalOrder_treatAsUpperBound();

        this.forbiddenECThreshold_typeInternalOrder = rule.isForbiddeneventcatthresholdtypeintorder();
        this.forbiddenECThreshold_typeInternalOrder_treatAsUpperBound = rule.isForbiddenECThreshold_typeInternalOrder_treatAsUpperBound();

        this.forbiddenECThreshold_globalOrder = rule.isForbiddeneventcatthresholdglobalorder();
        this.forbiddenECThreshold_globalOrder_treatAsUpperBound = rule.isForbiddenECThreshold_globalOrder_treatAsUpperBound();

        this.mandatoryRulePassRequiredToCloseEntity = rule.isMandatoryRulePassRequiredToCloseEntity();
        this.inactivateRuleOnEntityClose = rule.isInactivateRuleOnEntityClose();
        this.promptingDirective = rule.getPromptingDirective();

        this.triggeredECOnRulePass = rule.getTriggeredECOnRulePass();
        this.triggeredECOnRuleFail = rule.getTriggeredECOnRuleFail();

        this.activeRuleAbstract = rule.isActiveRuleAbstract();
        this.notes = rule.getNotes();
        
        this.userRankMinToConfigure = rule.getUserRankMinToConfigure();
        this.userRankMinToImplement = rule.getUserRankMinToImplement();
        this.userRankMinToWaive = rule.getUserRankMinToWaive();
        this.userRankMinToOverride = rule.getUserRankMinToOverride();
        this.userRankMinToDeactivate = rule.getUserRankMinToDeactivate();
    }

    /**
     * @return the implementationID
     */
    public int getImplementationID() {
        return implementationID;
    }

    /**
     * @return the domain
     */
    public EventDomainEnum getDomain() {
        return domain;
    }

    /**
     * @return the ceCaseID
     */
    public int getCeCaseID() {
        return ceCaseID;
    }

    /**
     * @return the occPeriodID
     */
    public int getOccPeriodID() {
        return occPeriodID;
    }

    /**
     * @return the implementationTS
     */
    public LocalDateTime getImplementationTS() {
        return implementationTS;
    }

    /**
     * @return the implementedBy
     */
    public User getImplementedBy() {
        return implementedBy;
    }

    /**
     * @return the lastEvaluatedTS
     */
    public LocalDateTime getLastEvaluatedTS() {
        return lastEvaluatedTS;
    }

    /**
     * @return the passedRuleTS
     */
    public LocalDateTime getPassedRuleTS() {
        return passedRuleTS;
    }

    /**
     * @return the triggeredEvent
     */
    public EventCnF getTriggeredEvent() {
        return triggeredEvent;
    }

    /**
     * @return the waivedTS
     */
    public LocalDateTime getWaivedTS() {
        return waivedTS;
    }

    /**
     * @return the passOverrideTS
     */
    public LocalDateTime getPassOverrideTS() {
        return passOverrideTS;
    }

    /**
     * @return the deactivatedTS
     */
    public LocalDateTime getDeactivatedTS() {
        return deactivatedTS;
    }

    /**
     * @return the impNotes
     */
    public String getImpNotes() {
        return impNotes;
    }

    /**
     * @param implementationID the implementationID to set
     */
    public void setImplementationID(int implementationID) {
        this.implementationID = implementationID;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(EventDomainEnum domain) {
        this.domain = domain;
    }

    /**
     * @param ceCaseID the ceCaseID to set
     */
    public void setCeCaseID(int ceCaseID) {
        this.ceCaseID = ceCaseID;
    }

    /**
     * @param occPeriodID the occPeriodID to set
     */
    public void setOccPeriodID(int occPeriodID) {
        this.occPeriodID = occPeriodID;
    }

    /**
     * @param implementationTS the implementationTS to set
     */
    public void setImplementationTS(LocalDateTime implementationTS) {
        this.implementationTS = implementationTS;
    }

    /**
     * @param implementedBy the implementedBy to set
     */
    public void setImplementedBy(User implementedBy) {
        this.implementedBy = implementedBy;
    }

    /**
     * @param lastEvaluatedTS the lastEvaluatedTS to set
     */
    public void setLastEvaluatedTS(LocalDateTime lastEvaluatedTS) {
        this.lastEvaluatedTS = lastEvaluatedTS;
    }

    /**
     * @param passedRuleTS the passedRuleTS to set
     */
    public void setPassedRuleTS(LocalDateTime passedRuleTS) {
        this.passedRuleTS = passedRuleTS;
    }

    /**
     * @param triggeredEvent the triggeredEvent to set
     */
    public void setTriggeredEvent(EventCnF triggeredEvent) {
        this.triggeredEvent = triggeredEvent;
    }

    /**
     * @param waivedTS the waivedTS to set
     */
    public void setWaivedTS(LocalDateTime waivedTS) {
        this.waivedTS = waivedTS;
    }

    /**
     * @param passOverrideTS the passOverrideTS to set
     */
    public void setPassOverrideTS(LocalDateTime passOverrideTS) {
        this.passOverrideTS = passOverrideTS;
    }

    /**
     * @param deactivatedTS the deactivatedTS to set
     */
    public void setDeactivatedTS(LocalDateTime deactivatedTS) {
        this.deactivatedTS = deactivatedTS;
    }

    /**
     * @param impNotes the impNotes to set
     */
    public void setImpNotes(String impNotes) {
        this.impNotes = impNotes;
    }

    /**
     * @return the waivedBy
     */
    public User getWaivedBy() {
        return waivedBy;
    }

    /**
     * @return the passOverrideBy
     */
    public User getPassOverrideBy() {
        return passOverrideBy;
    }

    /**
     * @return the deactivatedBy
     */
    public User getDeactivatedBy() {
        return deactivatedBy;
    }

    /**
     * @param waivedBy the waivedBy to set
     */
    public void setWaivedBy(User waivedBy) {
        this.waivedBy = waivedBy;
    }

    /**
     * @param passOverrideBy the passOverrideBy to set
     */
    public void setPassOverrideBy(User passOverrideBy) {
        this.passOverrideBy = passOverrideBy;
    }

    /**
     * @param deactivatedBy the deactivatedBy to set
     */
    public void setDeactivatedBy(User deactivatedBy) {
        this.deactivatedBy = deactivatedBy;
    }

   
}
