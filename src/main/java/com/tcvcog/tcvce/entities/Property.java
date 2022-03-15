/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import java.util.List;
import com.tcvcog.tcvce.application.interfaces.IFace_ActivatableBOB;

/**
 * Foundational entity for the system: Property
 That is an extension of a parcel with mailing mailingAddressLinkList and units
 * 
 * @author Ellen Bascomb
 */

public class        Property 
        extends     Parcel 
        implements  IFace_Loggable,
                    IFace_addressListHolder,
                    IFace_ActivatableBOB{
    
    final static LinkedObjectSchemaEnum PROPERTY_ADDRESS_LOSE = LinkedObjectSchemaEnum.ParcelMailingaddress;
    
    protected List<PropertyUnit> unitList;
    protected List<MailingAddressLink> mailingAddressLinkList;

    /**
     * Creates a new instance of Property
     * @param par
     */

    public Property(Parcel par) {
      super(par);
    }
    
    public Property(Property prop){
        super(prop);
        this.unitList = prop.unitList;
        this.mailingAddressLinkList = prop.mailingAddressLinkList;
    }

 
    /**
     * For compatability - builds an address String
     * from building No and street   
     * @return a string rep of building no & street
     */

    public String getAddressString(){
       return "Don't use me--useAddressPretty 1 or 2 line";
       
    }
    
 
    /**
     * Extracts the address from this property's address list
     * with index 0 or null if none is in the list
     * @return 
     */
    public MailingAddress getAddress(){
        if(mailingAddressLinkList != null && !mailingAddressLinkList.isEmpty()){
            return mailingAddressLinkList.get(0);
        } else {
            return null;
        }
        
    }
    
    /**
     * Utility method for extracting the first linked mailing address in a given
     * list of mailing address links
     * @return 
     */
    public MailingAddressLink getPrimaryAddressLink(){
        if(mailingAddressLinkList != null && !mailingAddressLinkList.isEmpty()){
            return mailingAddressLinkList.get(0);
            
        }
        return null;
    }
    
    /**
     * Convenience method for reverse comptability with property IDs
     * @return the internal parcelKEY
     * 
     */
    public int getPropertyID(){
        return parcelKey;
    }



    /**
     * @return the unitList
     */
    public List<PropertyUnit> getUnitList() {
        return unitList;
    }

    /**
     * @param unitList the unitList to set
     */
    public void setUnitList(List<PropertyUnit> unitList) {
        this.unitList = unitList;
    }

  

    /**
     * @return the mailingAddressLinkList
     */
    @Override
    public List<MailingAddressLink> getMailingAddressLinkList() {
        return mailingAddressLinkList;
    }

    /**
     * @param addresses the mailingAddressLinkList to set
     */
    @Override
    public void setMailingAddressLinkList(List<MailingAddressLink> addresses) {
        this.mailingAddressLinkList = addresses;
    }

    @Override
    public LinkedObjectSchemaEnum getLinkedObjectSchemaEnum() {
           return PROPERTY_ADDRESS_LOSE;
    }

    @Override
    public int getTargetObjectPK() {
        return parcelKey;
    }

  
}
