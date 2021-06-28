/*
 * Copyright (C) 2020 Technology Rediscovery LLC.
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
import java.util.ArrayList;
import java.util.Date;

/**
 * A wrapper class that stores a Person that is stripped of all sensitive
 * information.
 * Look at the JavaDocs of the PublicInfoBundle Class for more information.
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundlePerson extends PublicInfoBundle {
    
    private Person bundledPerson;
    
    @Override
    public String toString(){
        return this.getClass().getName() + bundledPerson.getHumanID();
    }
    
    public Person getBundledPerson() {
        return bundledPerson;
    }
    
    /**
     * Remove all sensitive data from the Person and set it in the
     * bundledPerson field.
     * @param input 
     */
    public void setBundledPerson(Person input) {
        
        //If this person is a minor, they should be hidden from the public
        setPaccEnabled(!input.isUnder18());
        
        input.setCreatorUserID(0);
        
        input.setCreationTimeStamp(LocalDateTime.MIN);
        
        input.setJobTitle("*****");
        
        input.setUseSeparateMailingAddress(false);
        input.setMailingAddressStreet("*****");
        input.setMailingAddressThirdLine("*****");
        input.setMailingAddressCity("*****");
        input.setMailingAddressZip("*****");
        input.setMailingAddressState("*****");
        
        input.setNotes("*****");
        
        input.setLastUpdated(LocalDateTime.MIN);
        input.setLastUpdatedPretty("*****");
        
        input.setCanExpire(false);
        
        input.setExpiryDate(LocalDateTime.MIN);
        input.setExpireString("*****");
        input.setExpiryDateUtilDate(new Date());
        input.setExpiryNotes("*****");
        
        input.setLinkedUserID(0);
        
        input.setVerifiedByUserID(0);
        input.setGhostCreatedDate(LocalDateTime.MIN);
        input.setGhostCreatedDatePretty("*****");
        input.setGhostOf(0);
        input.setGhostCreatedByUserID(0);
        
        input.setCloneCreatedDate(LocalDateTime.MIN);
        input.setCloneCreatedDatePretty("*****");
        input.setCloneOf(0);
        input.setCloneCreatedByUserID(0);
        
        input.setGhostsList(new ArrayList<Integer>());
        input.setCloneList(new ArrayList<Integer>());
        input.setMergedList(new ArrayList<Integer>());
        
        bundledPerson = input;
    }
    
}