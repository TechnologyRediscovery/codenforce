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

import com.tcvcog.tcvce.application.interfaces.IFace_ActivatableBOB;
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
        implements  IFace_Loggable,
                    IFace_addressListHolder,
                    IFace_ActivatableBOB{
    
    final static LinkedObjectSchemaEnum PERSON_ADDRESS_LOSE = LinkedObjectSchemaEnum.MailingaddressHuman;
    
    protected List<MailingAddressLink> mailingAddressLinkList;
    protected List<ContactEmail> emailList;
    protected List<ContactPhone> phoneList;
    
    private String mailingAddressListPretty;
    private String emailListPretty;
    private String phoneListPretty;

    
  

    /**
     * Method for cloning Person objects
     * 
     * @param p The person we would like to clone
     */
    public Person(Person p) {
       super(p);
       this.mailingAddressLinkList = p.mailingAddressLinkList;
       this.emailList = p.emailList;
       this.phoneList = p.phoneList;
        
       this.mailingAddressListPretty = p.mailingAddressListPretty;
       this.emailListPretty = p.emailListPretty;
       this.phoneListPretty = p.phoneListPretty;
    }
    
    /**
     * Creates a person out of a human
     * @param h 
     */
    public Person(Human h){
      super(h);
      
        
    }

    /**
     * Reverse compat method to help with humanization
     * @return 
     */
    public int getPersonID(){
        return humanID;
    }
    
    /**
     * Utility method for extracting the first address
     * in the list
     * @return the address at position 0 or null if no addresses present
     */
    public MailingAddress getPrimaryMailingAddress(){
        if(mailingAddressLinkList != null && !mailingAddressLinkList.isEmpty()){
            return mailingAddressLinkList.get(0);
        }
        return null;
        
    }
    
    
    /**
     * Adaptor method for reverse compat when Person's had address fields
     * right on their chest
     * @return 
     */
    public String getAddressStreet(){
        MailingAddress ma = getPrimaryMailingAddress();
        if(ma != null){
            return ma.buildingNo + " " + ma.getStreet();
        }
        return "";
    }
    
    /**
     * Adaptor method for reverse compat when Person's had address fields
     * right on their chest
     * @return 
     */
    public String getAddressCity(){
        MailingAddress ma = getPrimaryMailingAddress();
        if(ma != null){
            return ma.street.getCityStateZip().getCity();
        }
        return "";
    }

    /**
     * Adaptor method for reverse compat when Person's had address fields
     * right on their chest
     * @return 
     */
    public String getAddressState(){
        MailingAddress ma = getPrimaryMailingAddress();
        if(ma != null){
            return ma.street.getCityStateZip().getState();
        }
        return "";
    }
    
    /**
     * Adaptor method for reverse compat when Person's had address fields
     * right on their chest
     * @return 
     */
    public String getAddressZip(){
        MailingAddress ma = getPrimaryMailingAddress();
        if(ma != null){
            return ma.street.getCityStateZip().getZipCode();
        }
        return "";
    }
    
    /**
     * Adaptor method for reverse compat when Person's had address fields
     * right on their chest
     * @return 
     */

    public String getEmail(){
        ContactEmail em = getPrimaryEmail();
        if(em != null){
            return em.emailaddress;
        }
        return "";
    }


    /**
     * Utility method for extracting the first email
     * in the list
     * @return the email at position 0 or null if no email present
     */
    public ContactEmail getPrimaryEmail(){
        if(emailList != null && !emailList.isEmpty()){
            return emailList.get(0);
        }
        return null;
        
    }
    
    /**
     * Utility method for extracting the first phone number
     * in the list
     * @return the phone at position 0 or null if no phone present
     */
    public ContactPhone getPrimaryPhone(){
        if(phoneList != null && !phoneList.isEmpty()){
            return phoneList.get(0);
        }
        return null;
        
    }
    
    
    /**
     * Adaptor method for legacy compatability with Person objects
     * who had first and last names
     * @return empty string!!!!
     */    
    public String getFirstName(){
        return "";
    }
    
    /**
     * Adaptor method for legacy compatability with Person objects
     * who had first and last names
     * @return the underlying Human's full name, i.e. their name
     */
    public String getLastName(){
        return name;
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
     * @return the mailingAddressLinkList
     */
    @Override
    public List<MailingAddressLink> getMailingAddressLinkList() {
        return mailingAddressLinkList;
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
     * @param mailingAddressLinkList the mailingAddressLinkList to set
     */
    @Override
    public void setMailingAddressLinkList(List<MailingAddressLink> mailingAddressLinkList) {
        this.mailingAddressLinkList = mailingAddressLinkList;
    }

    

    @Override
    public LinkedObjectSchemaEnum getLinkedObjectSchemaEnum() {
        return PERSON_ADDRESS_LOSE;
    }

    @Override
    public int getTargetObjectPK() {
        return humanID;
    }

    /**
     * @return the mailingAddressListPretty
     */
    public String getMailingAddressListPretty() {
        return mailingAddressListPretty;
    }

    /**
     * @return the emailListPretty
     */
    public String getEmailListPretty() {
        return emailListPretty;
    }

    /**
     * @return the phoneListPretty
     */
    public String getPhoneListPretty() {
        return phoneListPretty;
    }

    /**
     * @param mailingAddressListPretty the mailingAddressListPretty to set
     */
    public void setMailingAddressListPretty(String mailingAddressListPretty) {
        this.mailingAddressListPretty = mailingAddressListPretty;
    }

    /**
     * @param emailListPretty the emailListPretty to set
     */
    public void setEmailListPretty(String emailListPretty) {
        this.emailListPretty = emailListPretty;
    }

    /**
     * @param phoneListPretty the phoneListPretty to set
     */
    public void setPhoneListPretty(String phoneListPretty) {
        this.phoneListPretty = phoneListPretty;
    }
    


 
    

}
