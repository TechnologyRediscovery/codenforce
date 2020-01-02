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
package com.tcvcog.tcvce.application;

import com.tcvcog.tcvce.domain.IntegrationException;
import com.tcvcog.tcvce.entities.Property;
import com.tcvcog.tcvce.integration.PropertyIntegrator;
import javax.faces.application.FacesMessage;

/**
 *
 * @author sylvia
 */
public class PropertyCreateBB 
        extends BackingBeanUtils{

     private Property prop;
    
    /**
     * Creates a new instance of PropertyCreateBB
     */
    public PropertyCreateBB() {
    }
    
    public void initiatePropertyCreation(ActionEvent ev){
        
        
    }
    
    
    public String insertProp(){
        PropertyIntegrator pi = getPropertyIntegrator();
        try {
            getProp().setPropertyID(pi.insertProperty(getProp()));
//            getSessionBean().setActivePropWithLists(getProp());
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Successfully inserted property with ID " + getProp().getPropertyID() 
                                + ", which is now your 'active property'", ""));
            return "properties";
            
        } catch (IntegrationException ex) {
            System.out.println(ex);
            getFacesContext().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        "Unable to insert property in database, sorry. ", 
                        "Please make sure all required fields are completed. "));
            return "";
        }
    }

    /**
     * @return the prop
     */
    public Property getProp() {
        return prop;
    }

    /**
     * @param prop the prop to set
     */
    public void setProp(Property prop) {
        this.prop = prop;
    }
    
    
}
