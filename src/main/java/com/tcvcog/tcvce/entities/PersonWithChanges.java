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

import java.util.List;

/**
 *
 * @author Nathan Dietz
 */
public class PersonWithChanges 
        extends Person{

        private List<PersonChangeOrder> changeOrderList;

    public PersonWithChanges() {
    }
        
    public PersonWithChanges(Person input){
        this.personID = input.getPersonID();
        this.personType = input.getPersonType();
        this.muniCode = input.getMuniCode();
        this.muniName = input.getMuniName();
        this.source = input.getSource();
        this.creatorUserID = input.getCreatorUserID();
        this.creationTimeStamp = input.getCreationTimeStamp();
        this.firstName = input.getFirstName();
        this.lastName = input.getLastName();
        this.compositeLastName = input.isCompositeLastName();
        this.businessEntity = input.isBusinessEntity();
        this.jobTitle = input.getJobTitle();
        this.phoneCell = input.getPhoneCell();
        this.phoneHome = input.getPhoneHome();
        this.phoneWork = input.getPhoneWork();
        this.email = input.getEmail();
        this.addressStreet = input.getAddressStreet();
        this.addressCity = input.getAddressCity();
        this.addressZip = input.getAddressZip();
        this.addressState = input.getAddressState();
        this.useSeparateMailingAddress = input.isUseSeparateMailingAddress();
        this.mailingAddressStreet = input.getMailingAddressStreet();
        this.mailingAddressThirdLine = input.getMailingAddressThirdLine();
        this.mailingAddressCity = input.getMailingAddressCity();
        this.mailingAddressZip = input.getMailingAddressZip();
        this.mailingAddressState = input.getMailingAddressState();
        this.notes = input.getNotes();
        this.lastUpdated = input.getLastUpdated();
        this.lastUpdatedPretty = input.getLastUpdatedPretty();
        this.canExpire = input.isCanExpire();
        this.expiryDate = input.getExpiryDate();
        this.expireString = input.getExpireString();
        this.expiryDateUtilDate = input.getExpiryDateUtilDate();
        this.expiryNotes = input.getExpiryNotes();
        this.active = input.isActive();
        this.linkedUserID = input.getLinkedUserID();
        this.under18 = input.isUnder18();
        this.verifiedByUserID = input.getVerifiedByUserID();
        this.referencePerson = input.isReferencePerson();
        this.ghostCreatedDate = input.getGhostCreatedDate();
        this.ghostCreatedDatePretty = input.getGhostCreatedDatePretty();
        this.ghostOf = input.getGhostOf();
        this.ghostCreatedByUserID = input.getGhostCreatedByUserID();
        this.cloneCreatedDate = input.getCloneCreatedDate();
        this.cloneCreatedDatePretty = input.getCloneCreatedDatePretty();
        this.cloneOf = input.getCloneOf();
        this.cloneCreatedByUserID = input.getCloneCreatedByUserID();
        this.ghostsList = input.getGhostsList();
        this.cloneList = input.getCloneList();
        this.mergedList = input.getMergedList();
    }

    public List<PersonChangeOrder> getChangeOrderList() {
        return changeOrderList;
    }

    public void setChangeOrderList(List<PersonChangeOrder> changeOrderList) {
        this.changeOrderList = changeOrderList;
    }
    
}
