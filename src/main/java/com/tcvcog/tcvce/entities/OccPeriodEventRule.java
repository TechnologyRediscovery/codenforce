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


import com.tcvcog.tcvce.occupancy.entities.OccEvent;

/**
 *
 * @author sylvia
 */
public class OccPeriodEventRule extends EventRuleImplementation{
    
    private int OccPeriodID;
    private OccEvent passedRuleEvent;
    
    public OccPeriodEventRule(EventRuleImplementation imp){
         this.ruleid = imp.getRuleid();
        this.title = imp.getTitle();
        this.description = imp.getDescription();
        this.requiredeventtype = imp.getRequiredeventtype();
        this.forbiddeneventtype = imp.getForbiddeneventtype();
        this.requiredEventCat = imp.getRequiredEventCat();
        this.forbiddenEventCat = imp.getForbiddenEventCat();

        this.requiredeventcatthresholdtypeintorder = imp.isRequiredeventcatthresholdtypeintorder();
        this.requiredeventcatupperboundtypeintorder = imp.isRequiredeventcatupperboundtypeintorder();

        this.requiredeventcatthresholdglobalorder = imp.isRequiredeventcatthresholdglobalorder();
        this.requiredeventcatupperboundglobalorder = imp.isRequiredeventcatupperboundglobalorder();


        this.forbiddeneventcatthresholdtypeintorder = imp.isForbiddeneventcatthresholdtypeintorder();
        this.forbiddeneventcatupperboundtypeintorder = imp.isForbiddeneventcatupperboundtypeintorder();

        this.forbiddeneventcatthresholdglobalorder = imp.isForbiddeneventcatthresholdglobalorder();
        this.forbiddeneventcatupperboundglobalorder = imp.isForbiddeneventcatupperboundglobalorder();

        this.mandatorypassreqtocloseentity = imp.isMandatorypassreqtocloseentity();
        this.autoremoveonentityclose = imp.isAutoremoveonentityclose();
        this.promptingProposal = imp.getPromptingProposal();

        this.triggeredeventcatonpass = imp.getTriggeredeventcatonpass();
        this.triggeredeventcatonfail = imp.getTriggeredeventcatonfail();

        this.active = imp.isActive();
        this.notes = imp.getNotes();
        
        // implementation subclass
        this.attachedTS = imp.getAttachedTS();
        this.attachedBy = imp.getAttachedBy();
        this.lastEvaluatedTS = imp.getLastEvaluatedTS();
        this.passedRuleTS = imp.getPassedRuleTS();
    }

    /**
     * @return the OccPeriodID
     */
    public int getOccPeriodID() {
        return OccPeriodID;
    }

    /**
     * @return the passedRuleEvent
     */
    public OccEvent getPassedRuleEvent() {
        return passedRuleEvent;
    }

    /**
     * @param OccPeriodID the OccPeriodID to set
     */
    public void setOccPeriodID(int OccPeriodID) {
        this.OccPeriodID = OccPeriodID;
    }

    /**
     * @param passedRuleEvent the passedRuleEvent to set
     */
    public void setPassedRuleEvent(OccEvent passedRuleEvent) {
        this.passedRuleEvent = passedRuleEvent;
    }
    
    
    
    
}
