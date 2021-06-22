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

/**
 * A wrapper class that stores a OccApplicationHumanLink that is stripped of all sensitive
 information.
 * Look at the JavaDocs of the PublicInfoBundle Class for more information.
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundlePersonOccApplication extends PublicInfoBundle {
    
    private OccApplicationHumanLink bundledPerson;
    
    @Override
    public String toString(){
        return this.getClass().getName() + bundledPerson.getPersonID();
    }

    public OccApplicationHumanLink getBundledPerson() {
        return bundledPerson;
    }

    /**
     * Remove all sensitive data from the OccApplicationHumanLink and set it in the
 bundledPerson field.
     * @param input 
     */
    public void setBundledPerson(OccApplicationHumanLink input) {
        
        //PersonOccPeriod and person share most of the same fields,
        //so let's use its anonymization method
        PublicInfoBundlePerson temp = new PublicInfoBundlePerson();
        
        temp.setBundledPerson(input);
        
        //take it out and then transfer the OccApplicationHumanLink-specific fields
        OccApplicationHumanLink skeleton = new OccApplicationHumanLink(temp.getBundledPerson());
        
        skeleton.setApplicant(input.isApplicant());
        
        skeleton.setPreferredContact(input.isPreferredContact());
        
        skeleton.setApplicationPersonType(input.getApplicationPersonType());
        
        skeleton.setLinkActive(input.isLinkActive());
        
        skeleton.setApplicationID(input.getApplicationID());
        
        bundledPerson = input;
    }
    
}