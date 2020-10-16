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

import com.tcvcog.tcvce.entities.occupancy.OccPeriod;
import com.tcvcog.tcvce.entities.occupancy.OccPermitApplication;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundleOccPermitApplication extends PublicInfoBundle {
    
    private OccPermitApplication bundledApplication;
    private PublicInfoBundlePropertyUnit applicationPropertyUnit;
    private PublicInfoBundlePerson applicantPerson;
    private PublicInfoBundlePerson preferredContact;
    private PublicInfoBundleOccPeriod connectedPeriod;
    private List<PublicInfoBundlePersonOccApplication> attachedPersons;
    
    @Override
    public String toString(){
        
        return this.getClass().getName() + bundledApplication.getId();
        
    }

    public OccPermitApplication getBundledApplication() {
        return bundledApplication;
    }

    public void setBundledApplication(OccPermitApplication input) {
        
        input.setInternalNotes("*****");
        
        input.setApplicationPropertyUnit(new PropertyUnit());
        
        input.setApplicantPerson(new Person());
        
        input.setPreferredContact(new Person());
        
        input.setAttachedPersons(new ArrayList<PersonOccApplication>());
        
        input.setConnectedPeriod(new OccPeriod());
        
        bundledApplication = input;
    }

    public PublicInfoBundlePropertyUnit getApplicationPropertyUnit() {
        return applicationPropertyUnit;
    }

    public void setApplicationPropertyUnit(PublicInfoBundlePropertyUnit applicationPropertyUnit) {
        this.applicationPropertyUnit = applicationPropertyUnit;
    }

    public PublicInfoBundlePerson getApplicantPerson() {
        return applicantPerson;
    }

    public void setApplicantPerson(PublicInfoBundlePerson applicantPerson) {
        this.applicantPerson = applicantPerson;
    }

    public PublicInfoBundlePerson getPreferredContact() {
        return preferredContact;
    }

    public void setPreferredContact(PublicInfoBundlePerson preferredContact) {
        this.preferredContact = preferredContact;
    }

    public PublicInfoBundleOccPeriod getConnectedPeriod() {
        return connectedPeriod;
    }

    public void setConnectedPeriod(PublicInfoBundleOccPeriod connectedPeriod) {
        this.connectedPeriod = connectedPeriod;
    }

    public List<PublicInfoBundlePersonOccApplication> getAttachedPersons() {
        return attachedPersons;
    }

    public void setAttachedPersons(List<PublicInfoBundlePersonOccApplication> attachedPersons) {
        this.attachedPersons = attachedPersons;
    }
    
}
