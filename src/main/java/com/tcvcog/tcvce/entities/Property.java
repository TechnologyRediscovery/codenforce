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
 * That is an extension of a parcel with mailing addresses and units
 * 
 * @author Ellen Bascomb
 */

public class    Property 
        extends Parcel 
        implements IFace_Loggable,
                    IFace_ActivatableBOB{
    
    protected List<PropertyUnit> unitList;
    
    protected List<ParcelMailingAddressLink> addresses;
    
    
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
    public String getAddress(){
        if(addresses != null && !addresses.isEmpty()){
            return addresses.get(0).buildingNo + " " + addresses.get(0).street;
        } else {
            return "[No Address]";
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
}
