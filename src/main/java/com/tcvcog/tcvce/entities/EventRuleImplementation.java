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

import java.io.Serializable;
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
        extends     EventRuleAbstract 
        implements  Serializable{
    
    protected LocalDateTime attachedTS;
    protected User attachedBy;
    protected LocalDateTime lastEvaluatedTS;
    protected LocalDateTime passedRuleTS;
    
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
    }

    /**
     * @return the attachedTS
     */
    public LocalDateTime getAttachedTS() {
        return attachedTS;
    }

    /**
     * @return the attachedBy
     */
    public User getAttachedBy() {
        return attachedBy;
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
     * @param attachedTS the attachedTS to set
     */
    public void setAttachedTS(LocalDateTime attachedTS) {
        this.attachedTS = attachedTS;
    }

    /**
     * @param attachedBy the attachedBy to set
     */
    public void setAttachedBy(User attachedBy) {
        this.attachedBy = attachedBy;
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
    
    
    
}
