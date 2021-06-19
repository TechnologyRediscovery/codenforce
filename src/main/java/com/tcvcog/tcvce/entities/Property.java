/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tcvcog.tcvce.entities;

import com.tcvcog.tcvce.application.interfaces.IFace_Loggable;
import com.tcvcog.tcvce.entities.occupancy.OccLocationDescriptor;
import java.time.LocalDateTime;
import java.util.List;
import com.tcvcog.tcvce.application.interfaces.IFace_ActivatableBOB;

/**
 * Foundational entity for the system: Property
 * @author Ellen Baskem
 */

public class    Property 
        extends Parcel 
        implements IFace_Loggable{
    
    protected List<PropertyUnit> unitList;
    
    protected List<ParcelMailingAddressLink> addresses;
    
    
    /**
     * Creates a new instance of Property
     */
    public Property() {
      
    }


    /**
     * For compatability - builds an address String
     * from building No and street
     * @deprecated 
     * @return 
     */

    public String getAddress(){
        if(addresses != null && !addresses.isEmpty()){
            return addresses.get(0).buildingNo + " " + addresses.get(0).street;
        } else {
            return "No Address";
        }
        
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
