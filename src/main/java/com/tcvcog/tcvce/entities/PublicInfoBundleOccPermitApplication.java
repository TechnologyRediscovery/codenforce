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
 * A wrapper class that stores a OccPermitApplication that is stripped of all sensitive
 * information.
 * Look at the JavaDocs of the PublicInfoBundle Class for more information.
 *
 * @author Nathan Dietz
 */
public class PublicInfoBundleOccPermitApplication extends PublicInfoBundle {
    
    private OccPermitApplication bundledApplication;
    
    //An anonymized version of the unit the application is for.
    private PublicInfoBundlePropertyUnit applicationPropertyUnit;
    //An anonymized version of the applicant.
    private PublicInfoBundlePerson applicantPerson;
    //An anonymized version of the preferredContact.
    private PublicInfoBundlePerson preferredContact;
    //An anonmyized version of the OccPeriod the application is attached to.
    private PublicInfoBundleOccPeriod connectedPeriod;
    //Anonymized versions of the various persons that are related to this occupation.
    private List<PublicInfoBundlePersonOccApplication> attachedPersons;
    
    @Override
    public String toString(){
        
        return this.getClass().getName() + bundledApplication.getId();
        
    }

    public OccPermitApplication getBundledApplication() {
        return bundledApplication;
    }

    /**
     * Remove all sensitive data from the OccPermitApplication and set it in the
     * bundledApplication field.
     * @param input 
     */
    public void setBundledApplication(OccPermitApplication input) {
        
        input.setInternalNotes("*****");
        
        input.setApplicationPropertyUnit(new PropertyUnit());
        
        input.setApplicantPerson(null);
        
        input.setPreferredContact(null);
        
        input.setAttachedPersons(new ArrayList<>());
        
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
