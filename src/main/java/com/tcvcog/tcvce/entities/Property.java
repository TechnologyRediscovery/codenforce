/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import java.util.List;
import com.tcvcog.tcvce.application.interfaces.IFace_ActivatableBOB;
import com.tcvcog.tcvce.util.DateTimeUtil;

/**
 * Foundational entity for the system: Property
 * That is an extension of a parcel with mailing addresses and units
 * 
 * @author Ellen Bascomb
 */

public class        Property 
        extends     Parcel 
        implements  IFace_Loggable,
                    IFace_ActivatableBOB{
    
    protected List<PropertyUnit> unitList;
    
    protected List<ParcelMailingAddressLink> addresses;
    private String addressPretty2LineEscapeFalse;
    private String addressPretty1Line;
    
   
    
    
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
        this.addresses = prop.addresses;
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
        if(addresses != null && !addresses.isEmpty()){
            return addresses.get(0);
        } else {
            return null;
        }
        
    }
    
    /**
     * Utility method for extracting the first linked mailing address in a given
     * list of mailing address links
     * @return 
     */
    public ParcelMailingAddressLink getPrimaryAddressLink(){
        if(addresses != null && !addresses.isEmpty()){
            return addresses.get(0);
            
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
     * @return the addresses
     */
    public List<ParcelMailingAddressLink> getAddresses() {
        return addresses;
    }

    /**
     * @param addresses the addresses to set
     */
    public void setAddresses(List<ParcelMailingAddressLink> addresses) {
        this.addresses = addresses;
    }

    /**
     * @return the addressPretty2LineEscapeFalse
     */
    public String getAddressPretty2LineEscapeFalse() {
        return addressPretty2LineEscapeFalse;
    }

    /**
     * @return the addressPretty1Line
     */
    public String getAddressPretty1Line() {
        return addressPretty1Line;
    }

    /**
     * @param addressPretty2LineEscapeFalse the addressPretty2LineEscapeFalse to set
     */
    public void setAddressPretty2LineEscapeFalse(String addressPretty2LineEscapeFalse) {
        this.addressPretty2LineEscapeFalse = addressPretty2LineEscapeFalse;
    }

    /**
     * @param addressPretty1Line the addressPretty1Line to set
     */
    public void setAddressPretty1Line(String addressPretty1Line) {
        this.addressPretty1Line = addressPretty1Line;
    }
}
