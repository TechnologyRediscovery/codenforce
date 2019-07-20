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
 *
 * @author sylvia
 */
public class EventRuleImplementation extends EventRuleAbstract implements Serializable{
    
    protected LocalDateTime attachedTS;
    protected User attachedBy;
    protected LocalDateTime lastEvaluatedTS;
    protected LocalDateTime passedRuleTS;
    
    public EventRuleImplementation(EventRuleAbstract rule){
        
        this.ruleid = rule.getRuleid();
        this.title = rule.getTitle();
        this.description = rule.getDescription();
        this.requiredeventtype = rule.getRequiredeventtype();
        this.forbiddeneventtype = rule.getForbiddeneventtype();
        this.requiredEventCat = rule.getRequiredEventCat();
        this.forbiddenEventCat = rule.getForbiddenEventCat();

        this.requiredeventcatthresholdtypeintorder = rule.isRequiredeventcatthresholdtypeintorder();
        this.requiredeventcatupperboundtypeintorder = rule.isRequiredeventcatupperboundtypeintorder();

        this.requiredeventcatthresholdglobalorder = rule.isRequiredeventcatthresholdglobalorder();
        this.requiredeventcatupperboundglobalorder = rule.isRequiredeventcatupperboundglobalorder();

        this.forbiddeneventcatthresholdtypeintorder = rule.isForbiddeneventcatthresholdtypeintorder();
        this.forbiddeneventcatupperboundtypeintorder = rule.isForbiddeneventcatupperboundtypeintorder();

        this.forbiddeneventcatthresholdglobalorder = rule.isForbiddeneventcatthresholdglobalorder();
        this.forbiddeneventcatupperboundglobalorder = rule.isForbiddeneventcatupperboundglobalorder();

        this.mandatorypassreqtocloseentity = rule.isMandatorypassreqtocloseentity();
        this.autoremoveonentityclose = rule.isAutoremoveonentityclose();
        this.promptingProposal = rule.getPromptingProposal();

        this.triggeredeventcatonpass = rule.getTriggeredeventcatonpass();
        this.triggeredeventcatonfail = rule.getTriggeredeventcatonfail();

        this.active = rule.isActive();
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
