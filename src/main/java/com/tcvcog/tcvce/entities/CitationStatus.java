/*
 * Copyright (C) 2021 Technology Rediscovery LLC
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
 *  Subclass of Status which represents the current business
 * state of a citation log entry
 * 
 * @author Ellen Bascomb of Apartment 31Y
 */
public class CitationStatus 
        extends Status{
    
    protected boolean editsForbidden;
    private EventRuleAbstract eventRuleAbstract;

    /**
     * @return the editsForbidden
     */
    public boolean isEditsForbidden() {
        return editsForbidden;
    }

  
    /**
     * @param editsForbidden the editsForbidden to set
     */
    public void setEditsForbidden(boolean editsForbidden) {
        this.editsForbidden = editsForbidden;
    }

    
    /**
     * @return the eventRuleAbstract
     */
    public EventRuleAbstract getEventRuleAbstract() {
        return eventRuleAbstract;
    }

    /**
     * @param eventRuleAbstract the eventRuleAbstract to set
     */
    public void setEventRuleAbstract(EventRuleAbstract eventRuleAbstract) {
        this.eventRuleAbstract = eventRuleAbstract;
    }
    
}
