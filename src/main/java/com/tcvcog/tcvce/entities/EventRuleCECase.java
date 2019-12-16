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

/**
 *
 * @author sylvia
 */
public class EventRuleCECase extends EventRuleImplementation{
    
    private int ceCaseID;
    
    private Event passedRuleEvent;
    
    public EventRuleCECase(EventRuleImplementation imp){
        super(imp);
        // implementation subclass
        this.attachedTS = imp.getAttachedTS();
        this.attachedBy = imp.getAttachedBy();
        this.lastEvaluatedTS = imp.getLastEvaluatedTS();
        this.passedRuleTS = imp.getPassedRuleTS();
    }

    /**
     * @return the ceCaseID
     */
    public int getCeCaseID() {
        return ceCaseID;
    }

    /**
     * @return the passedRuleEvent
     */
    public Event getPassedRuleEvent() {
        return passedRuleEvent;
    }

    /**
     * @param ceCaseID the ceCaseID to set
     */
    public void setCeCaseID(int ceCaseID) {
        this.ceCaseID = ceCaseID;
    }

    /**
     * @param passedRuleEvent the passedRuleEvent to set
     */
    public void setPassedRuleEvent(EventCECase passedRuleEvent) {
        this.passedRuleEvent = passedRuleEvent;
    }
    
}
