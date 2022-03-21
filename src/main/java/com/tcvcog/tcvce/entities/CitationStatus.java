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
        extends Status
implements Comparable<Object>{
    
    protected boolean editsForbidden;
    private EventRuleAbstract eventRuleAbstract;
    protected int displayOrder;

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

    /**
     * @return the displayOrder
     */
    public int getDisplayOrder() {
        return displayOrder;
    }

    /**
     * @param displayOrder the displayOrder to set
     */
    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof CitationStatus){
            CitationStatus st = (CitationStatus) o;
            if(this.displayOrder > st.displayOrder){
                return 1;
            } else if (this.displayOrder == st.displayOrder){
                return 0;
            } else {
                return -1;
            }
        }
        else return 0;
    }
    
}
