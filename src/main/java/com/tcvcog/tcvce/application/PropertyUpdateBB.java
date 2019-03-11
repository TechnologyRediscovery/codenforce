/*
 * Copyright (C) 2018 Turtle Creek Valley
Council of Governments, PA
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
package com.tcvcog.tcvce.application;


import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.entities.PropertyWithLists;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;

/**
 *
 * @author Eric C. Darsow
 */
public class PropertyUpdateBB extends BackingBeanUtils implements Serializable {
    
    
    private PropertyWithLists currProp;
//    private Property property;
//    
//    private int formMuniCode;
//    private String formParID;
//    
//    private String formLotAndBlock;
//    private String formAddress;
//    private String formPropertyUseType;
    // have not wired up property use type as an accessory table
//    private int formPropertyUseTypeID; 
    // also not needed
    private HashMap propertyUseTypeMap;

//    private boolean formRental;
//    private boolean formMultiUnit;
    
//    private String formUseGroup;
//    private String formConstructionType;
//    private String formCountyCode;
    
//    private String formNotes;
    
    
    /**
     * Creates a new instance of PropertyUpdateBB
     */
    public PropertyUpdateBB() {
    }
    
    public String updateProperty(){
        PropertyIntegrator pi = getPropertyIntegrator();
    //    Property p = new Property();
        
        
    //    p.setLotAndBlock(lotAndBlock);
    //    p.setLotAndBlock(formLotAndBlock);
    //    p.setAddress(formAddress);
    //    p.setPropertyUseType(formPropertyUseType);
    //    
    //    p.setUseGroup(formUseGroup);
    //    p.setConstructionType(formConstructionType);
    //    p.setCountyCode(formCountyCode);
    //    
    //    p.setNotes(formNotes);
        
        try {
            pi.updateProperty(getCurrProp());
            // pull a new version of the property from the DB and store that in
            // the session to avoid errors in viewing any data that's not in the DB
            
            // TODO: ^this^ (the integrator can only update & select as of this comment) --3/7/2019
            getSessionBean().setActiveProp(getCurrProp());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Successfully updated property with ID " + getCurrProp().getPropertyID() 
                                + ", which is now your 'active property'", ""));
            return "propertyProfile";
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to update property in database, sorry. ", 
                        "Property updates are tricky--please inform your administrator. "));
            return "";
        }
    }
    
    @PostConstruct
    public void initBean(){
        this.setCurrProp(getSessionBean().getActivePropWithLists());
    }

    /**
     * @return the currProp
     */
    public PropertyWithLists getCurrProp() {
        return currProp;
    }

    /**
     * @param currProp the currProp to set
     */
    public void setCurrProp(PropertyWithLists currProp) {
        this.currProp = currProp;
    }

    
    
    
    
    
}
