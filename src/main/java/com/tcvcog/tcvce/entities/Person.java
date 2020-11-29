/*
 * Copyright (C) 2017 Turtle Creek Valley Council of Governements
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

import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import java.util.List;

/**
 * Model object representing a person in the system. A Person has a type
 coordinated through the enum @PersonType. Contains getters and setters for
 database fields related to a Person, stored in the person table. 
 * 
 * @author Eric Darsow
 */
public  class       Person 
        extends     Human
        implements  IFace_Loggable{
    
    protected List<ContactEmail> emailList;
    protected List<ContactPhone> phoneList;
    protected List<MailingAddress> addressList;

    public Person() {
    }

    /**
     * Method for cloning Person objects
     * 
     * @param input The person we would like to clone
     */
    public Person(Person input) {
       
    }

    /**
     * @return the emailList
     */
    public List<ContactEmail> getEmailList() {
        return emailList;
    }

    /**
     * @return the phoneList
     */
    public List<ContactPhone> getPhoneList() {
        return phoneList;
    }

    /**
     * @return the addressList
     */
    public List<MailingAddress> getAddressList() {
        return addressList;
    }

    /**
     * @param emailList the emailList to set
     */
    public void setEmailList(List<ContactEmail> emailList) {
        this.emailList = emailList;
    }

    /**
     * @param phoneList the phoneList to set
     */
    public void setPhoneList(List<ContactPhone> phoneList) {
        this.phoneList = phoneList;
    }

    /**
     * @param addressList the addressList to set
     */
    public void setAddressList(List<MailingAddress> addressList) {
        this.addressList = addressList;
    }
    
    
    

}
