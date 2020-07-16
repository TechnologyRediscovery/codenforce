/*
 * Copyright (C) 2020 TechnologyRediscovery LLC.
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

import java.sql.Timestamp;

/**
 * Public users compile lists of changes they would like to make to objects.
 * Before applying any changes to our records, they await approval as one of 
 * this class' subclasses.
 *
 * @author Nathan Dietz
 */
public class ChangeOrder extends BOb {
    
    protected boolean removed;
    protected boolean added;
    protected java.sql.Timestamp changedOn;
    protected java.sql.Timestamp approvedOn; //If null, it has not been approved
    protected int propertyUnitID;
    protected User approvedBy; 
    protected ChangeOrderAction action; //not in the database, used by interface
    protected boolean active;
    protected int changedBy;

    public ChangeOrder() {
    }

    /**
     * Compares two strings. Only returns true if strings are the same and neither string is null
     * @param first
     * @param second
     * @return 
     */
    public final boolean compareStrings(String first, String second) {
        
        return first != null && second != null && first.contentEquals(second);
        
    }
    
    /**
     * Every changeOrder should overwrite this method to check whether or not any fields were changed.
     * @return 
     */
    public boolean changedOccured(){
       
        return added || removed; 
        
    }
    
    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public boolean isAdded() {
        return added;
    }

    public void setAdded(boolean added) {
        this.added = added;
    }

    public Timestamp getChangedOn() {
        return changedOn;
    }

    public void setChangedOn(Timestamp changedOn) {
        this.changedOn = changedOn;
    }

    public Timestamp getApprovedOn() {
        return approvedOn;
    }

    public void setApprovedOn(Timestamp approvedOn) {
        this.approvedOn = approvedOn;
    }

    public int getPropertyUnitID() {
        return propertyUnitID;
    }

    public void setPropertyUnitID(int propertyUnitID) {
        this.propertyUnitID = propertyUnitID;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ChangeOrderAction getAction() {
        return action;
    }

    public void setAction(ChangeOrderAction action) {
        this.action = action;
    }

    public int getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(int changedBy) {
        this.changedBy = changedBy;
    }
    
}
