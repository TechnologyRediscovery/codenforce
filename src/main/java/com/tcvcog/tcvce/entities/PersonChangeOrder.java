/*
 * Copyright (C) 2020 Technology Rediscovery LLC
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
public class PersonChangeOrder extends ChangeOrder {

    //As you will see, many fields from the person object are missing
    //We are only storing changes that a public user could make
    private int personChangeID;
    private int personID;
    private String firstName;
    private String lastName;

    // first, middle initial, and last all in lastName
    private String compositeLastName;

    private String phoneCell;
    private String phoneHome;
    private String phoneWork;

    private String email;
    private String addressStreet;
    private String addressCity;
    private String addressZip;
    private String addressState;

    private String useSeparateMailingAddress;
    private String mailingAddressStreet;
    private String mailingAddressThirdLine;
    private String mailingAddressCity;
    private String mailingAddressZip;

    private String mailingAddressState;
    
    //Needed by the public occ application flow
    private PersonType personType;
    
    public PersonChangeOrder() {
    }

    /**
     * This constructor compares a proposed person object against the original
     * person object. If there is a difference between the original and the
     * proposed, the value of the proposed is saved.
     *
     * @param original
     * @param proposed
     */
    public PersonChangeOrder(Person original, Person proposed) {

        personID = proposed.getPersonID();

        //check each field for changes
        if (!compareStrings(original.getFirstName(), proposed.getFirstName())) {
            firstName = proposed.getFirstName();
        }
        
        if (!compareStrings(original.getLastName(), proposed.getLastName())) {
            lastName = proposed.getLastName();
        }
        
        if (proposed.isCompositeLastName() != original.isCompositeLastName()) {
            setCompositeLastName(proposed.isCompositeLastName());
        }
        
        if (!compareStrings(original.getPhoneCell(), proposed.getPhoneCell())) {
            phoneCell = proposed.getPhoneCell();
        }
        
        if (!compareStrings(original.getPhoneHome(), proposed.getPhoneHome())) {
            phoneHome = proposed.getPhoneHome();
        }
        
        if (!compareStrings(original.getPhoneWork(), proposed.getPhoneWork())) {
            phoneWork = proposed.getPhoneWork();
        }
        
        if (!compareStrings(original.getEmail(), proposed.getEmail())) {
            email = proposed.getEmail();
        }
        
        if (!compareStrings(original.getAddressStreet(), proposed.getAddressStreet())) {
            addressStreet = proposed.getAddressStreet();
        }
        
        if (!compareStrings(original.getAddressCity(), proposed.getAddressCity())) {
            addressCity = proposed.getAddressCity();
        }
        
        if (!compareStrings(original.getAddressState(), proposed.getAddressState())) {
            addressState = proposed.getAddressState();
        }
        
        if (!compareStrings(original.getAddressZip(), proposed.getAddressZip())) {
            addressZip = proposed.getAddressZip();
        }
        
        if(original.isUseSeparateMailingAddress() != proposed.isUseSeparateMailingAddress()){
            setUseSeparateMailingAddress(proposed.isUseSeparateMailingAddress());
        }

        if (!compareStrings(original.getMailingAddressStreet(), proposed.getMailingAddressStreet())) {
            addressStreet = proposed.getMailingAddressStreet();
        }
        
        if (!compareStrings(original.getMailingAddressCity(), proposed.getMailingAddressCity())) {
            addressCity = proposed.getMailingAddressCity();
        }
        
        if (!compareStrings(original.getMailingAddressState(), proposed.getMailingAddressState())) {
            addressState = proposed.getMailingAddressState();
        }
        
        if (!compareStrings(original.getMailingAddressZip(), proposed.getMailingAddressZip())) {
            addressZip = proposed.getMailingAddressZip();
        }
        
        personType = proposed.getPersonType();

    }

    public PersonChangeOrder(Person input) {
        personID = input.getPersonID();
        personType = input.getPersonType();
        firstName = input.getFirstName();
        lastName = input.getLastName();
        setCompositeLastName(input.isCompositeLastName());
        phoneCell = input.getPhoneCell();
        phoneHome = input.getPhoneHome();
        phoneWork = input.getPhoneWork();
        email = input.getEmail();
        addressStreet = input.getAddressStreet();
        addressCity = input.getAddressCity();
        addressZip = input.getAddressZip();
        addressState = input.getAddressState();
        setUseSeparateMailingAddress(input.isUseSeparateMailingAddress());
        mailingAddressStreet = input.getMailingAddressStreet();
        mailingAddressThirdLine = input.getMailingAddressThirdLine();
        mailingAddressCity = input.getMailingAddressCity();
        mailingAddressZip = input.getMailingAddressZip();
        mailingAddressState = input.getAddressState();
    }

    public Person toPerson() {

        Person skeleton = new Person();

        skeleton.setPersonID(personID);
        skeleton.setPersonType(personType);
        skeleton.setFirstName(firstName);
        skeleton.setLastName(lastName);
        skeleton.setCompositeLastName(Boolean.getBoolean(compositeLastName));

        skeleton.setPhoneCell(phoneCell);
        skeleton.setPhoneHome(phoneHome);
        skeleton.setPhoneWork(phoneWork);
        skeleton.setEmail(email);

        skeleton.setAddressStreet(addressStreet);
        skeleton.setAddressCity(addressCity);
        skeleton.setAddressState(addressState);
        skeleton.setAddressZip(addressZip);

        skeleton.setUseSeparateMailingAddress(Boolean.getBoolean(useSeparateMailingAddress));
        skeleton.setMailingAddressStreet(mailingAddressStreet);
        skeleton.setMailingAddressThirdLine(mailingAddressThirdLine);
        skeleton.setMailingAddressCity(mailingAddressCity);
        skeleton.setMailingAddressState(mailingAddressState);
        skeleton.setMailingAddressZip(mailingAddressZip);

        return skeleton;

    }

    /**
     * Detects if the person object has actually been changed.
     *
     * @return
     */
    @Override
    public boolean changedOccured() {

        if (firstName != null) {
            return true;
        }

        if (lastName != null) {
            return true;
        }

        if (compositeLastName != null) {
            return true;
        }

        if (phoneCell != null) {
            return true;
        }

        if (phoneHome != null) {
            return true;
        }

        if (phoneWork != null) {
            return true;
        }

        if (email != null) {
            return true;
        }

        if (addressStreet != null) {
            return true;
        }

        if (addressCity != null) {
            return true;
        }

        if (addressState != null) {
            return true;
        }

        if (addressZip != null) {
            return true;
        }

        if (useSeparateMailingAddress != null) {
            return true;
        }

        if (mailingAddressStreet != null) {
            return true;
        }

        if (mailingAddressThirdLine != null) {
            return true;
        }

        if (mailingAddressCity != null) {
            return true;
        }

        if (mailingAddressState != null) {
            return true;
        }

        if (mailingAddressZip != null) {
            return true;
        }

        //If none of the above apply, atleast check if it has been added or removed.
        return added || removed;
    }

    public int getPersonChangeID() {
        return personChangeID;
    }

    public void setPersonChangeID(int personChangeID) {
        this.personChangeID = personChangeID;
    }

    public int getPersonID() {
        return personID;
    }

    public void setPersonID(int personID) {
        this.personID = personID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompositeLastName() {
        return compositeLastName;
    }
    
    public boolean isCompositeLastName(){
        return Boolean.getBoolean(compositeLastName);
    }

    public final void setCompositeLastName(boolean compositeLastName) {
        this.compositeLastName = "" + compositeLastName;
    }

    public final void setCompositeLastName(String compositeLastName) {
        this.compositeLastName = compositeLastName;
    }

    public String getPhoneCell() {
        return phoneCell;
    }

    public void setPhoneCell(String phoneCell) {
        this.phoneCell = phoneCell;
    }

    public String getPhoneHome() {
        return phoneHome;
    }

    public void setPhoneHome(String phoneHome) {
        this.phoneHome = phoneHome;
    }

    public String getPhoneWork() {
        return phoneWork;
    }

    public void setPhoneWork(String phoneWork) {
        this.phoneWork = phoneWork;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddressStreet() {
        return addressStreet;
    }

    public void setAddressStreet(String addressStreet) {
        this.addressStreet = addressStreet;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public void setAddressCity(String addressCity) {
        this.addressCity = addressCity;
    }

    public String getAddressZip() {
        return addressZip;
    }

    public void setAddressZip(String addressZip) {
        this.addressZip = addressZip;
    }

    public String getAddressState() {
        return addressState;
    }

    public void setAddressState(String addressState) {
        this.addressState = addressState;
    }

    public String getUseSeparateMailingAddress() {
        return useSeparateMailingAddress;
    }
    
    public boolean isUseSeparateMailingAddress(){
        return Boolean.getBoolean(useSeparateMailingAddress);
    }

    public final void setUseSeparateMailingAddress(boolean useSeparateMailingAddress) {
        this.useSeparateMailingAddress = "" + useSeparateMailingAddress;
    }

    public final void setUseSeparateMailingAddress(String useSeparateMailingAddress) {
        this.useSeparateMailingAddress = useSeparateMailingAddress;
    }

    public String getMailingAddressStreet() {
        return mailingAddressStreet;
    }

    public void setMailingAddressStreet(String mailingAddressStreet) {
        this.mailingAddressStreet = mailingAddressStreet;
    }

    public String getMailingAddressThirdLine() {
        return mailingAddressThirdLine;
    }

    public void setMailingAddressThirdLine(String mailingAddressThirdLine) {
        this.mailingAddressThirdLine = mailingAddressThirdLine;
    }

    public String getMailingAddressCity() {
        return mailingAddressCity;
    }

    public void setMailingAddressCity(String mailingAddressCity) {
        this.mailingAddressCity = mailingAddressCity;
    }

    public String getMailingAddressZip() {
        return mailingAddressZip;
    }

    public void setMailingAddressZip(String mailingAddressZip) {
        this.mailingAddressZip = mailingAddressZip;
    }

    public String getMailingAddressState() {
        return mailingAddressState;
    }

    public void setMailingAddressState(String mailingAddressState) {
        this.mailingAddressState = mailingAddressState;
    }

    public PersonType getPersonType() {
        return personType;
    }

    public void setPersonType(PersonType personType) {
        this.personType = personType;
    }

}
