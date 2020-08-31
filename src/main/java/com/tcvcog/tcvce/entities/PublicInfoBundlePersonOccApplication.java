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
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundlePersonOccApplication extends PublicInfoBundle {
    
    private PersonOccApplication bundledPerson;
    
    @Override
    public String toString(){
        return this.getClass().getName() + bundledPerson.getPersonID();
    }

    public PersonOccApplication getBundledPerson() {
        return bundledPerson;
    }

    public void setBundledPerson(PersonOccApplication input) {
        
        //PersonOccPeriod and person share most of the same fields,
        //so let's use its anonymization method
        PublicInfoBundlePerson temp = new PublicInfoBundlePerson();
        
        temp.setBundledPerson(input);
        
        //take it out and then transfer the PersonOccApplication-specific fields
        PersonOccApplication skeleton = new PersonOccApplication(temp.getBundledPerson());
        
        skeleton.setApplicant(input.isApplicant());
        
        skeleton.setPreferredContact(input.isPreferredContact());
        
        skeleton.setApplicationPersonType(input.getApplicationPersonType());
        
        skeleton.setLinkActive(input.isLinkActive());
        
        skeleton.setApplicationID(input.getApplicationID());
        
        bundledPerson = input;
    }
    
    
}
