/*
 * Copyright (C) Technology Rediscovery LLC. 2020
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
 * A wrapper class that stores a CEActionRequest that is stripped of all sensitive
 * information.
 * Look at the JavaDocs of the PublicInfoBundle Class for more information.
 * @author Nathan Dietz
 */
public class PublicInfoBundleCEActionRequest extends PublicInfoBundle {
    
    //************************************************
    //*******Action request case public data********
    //************************************************
    
    private CEActionRequest bundledRequest;
    private PublicInfoBundlePerson requestor; //stores an anonymized version of the requestor
    private PublicInfoBundleProperty requestProperty; //stores an anonymized version of the requestProperty

    public CEActionRequest getBundledRequest() {
        return bundledRequest;
    }
    
    /**
     * Remove all sensitive data from the CEActionRequest and set it in the
     * bundledRequest field.
     * @param input 
     */
    public void setBundledRequest(CEActionRequest input) {
        
        setMuni(input.getMuni());
        setPacc(input.getRequestPublicCC());
        setAddressAssociated(!input.getNotAtAddress());
        
        input.setRequestor(new Person());
        
        input.setRequestProperty(new Property());
        input.setMuniCode(0);
        input.setCaseAttachmentUser(new User());
        input.setCogInternalNotes("*****");
        input.setMuniNotes("*****");
        
        bundledRequest = input;
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + bundledRequest.getRequestID();
    }

    public PublicInfoBundlePerson getRequestor() {
        return requestor;
    }

    public void setRequestor(PublicInfoBundlePerson requestor) {
        this.requestor = requestor;
    }

    public PublicInfoBundleProperty getRequestProperty() {
        return requestProperty;
    }

    public void setRequestProperty(PublicInfoBundleProperty input) {
        setAddressAssociated(input.isAddressAssociated());
        setPropertyAddress(input.getPropertyAddress());
        requestProperty = input;
    }
    
}
