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
public class UserWithAccessData extends User{

    private UserAccessRecord accessRecord;
    
    
    public UserWithAccessData(User u){
        this.userID = u.getUserID();
        this.roleType = u.getRoleType();
        this.username = u.getUsername();
        this.person = u.getPerson();
        this.personID = u.getPersonID();
        this.notes = u.getNotes();
        this.badgeNumber = u.getBadgeNumber();
        this.oriNumber = u.getOriNumber();
        
        
    }

    /**
     * @return the accessRecord
     */
    public UserAccessRecord getAccessRecord() {
        return accessRecord;
    }

    /**
     * @param accessRecord the accessRecord to set
     */
    public void setAccessRecord(UserAccessRecord accessRecord) {
        this.accessRecord = accessRecord;
    }

       
}
